import { defineStore } from 'pinia'
import { ref, nextTick } from 'vue'
import type { Message } from '@/types'
import request from '@/utils/request'
import wsClient from '@/utils/websocket'
import { useConversationStore } from './conversation'

export const useMessageStore = defineStore('message', () => {
  const messages = ref<Message[]>([])
  const loading = ref(false)
  const loadingMore = ref(false)
  const hasMore = ref(true)
  const lastReadId = ref<number>(0)
  const isRobotTyping = ref(false)

  function initWsListener() {
    wsClient.on('message', (msg: any) => {
      console.log('[Store] Received message data from WS:', msg)
      if (!msg || !msg.conversationId) return

      const conversationStore = useConversationStore()
      const currentUserId = Number(localStorage.getItem('userId'))
      const currentConvId = conversationStore.currentConversation?.id

      // 如果收到的是机器人的消息，停止打字动画
      if (msg.contentType === 'AI') {
        isRobotTyping.value = false
      }

      // 1. 始终更新会话列表预览
      conversationStore.updateLastMessage(Number(msg.conversationId), msg.content, msg.createTime)

      // 2. 如果是当前会话
      if (currentConvId && Number(currentConvId) === Number(msg.conversationId)) {
        // 解析 meta 检查 clientMsgId (用于去重)
        let clientMsgId: string | null = null
        if (msg.meta) {
          try {
            const metaObj = typeof msg.meta === 'string' ? JSON.parse(msg.meta) : msg.meta
            clientMsgId = metaObj.clientMsgId
          } catch {}
        }

        const idx = messages.value.findIndex(m => {
          // 匹配真实 ID
          if (Number(m.id) === Number(msg.id)) return true
          // 匹配客户端临时 ID (处理自己发送的消息回显)
          if (clientMsgId && m.status === 'SENDING') {
            return String(m.id) === String(clientMsgId)
          }
          return false
        })

        if (idx > -1) {
          // 找到已存在的消息（可能是临时消息或是 POST 先返回了），更新状态
          console.log('[Store] Updating existing message at index:', idx)
          messages.value[idx] = { ...msg, status: 'DELIVERED' }
        } else {
          // 新消息
          console.log('[Store] Pushing new message to list')
          messages.value.push(msg)
          
          if (Number(msg.senderId) !== Number(currentUserId)) {
            markAsRead(Number(msg.conversationId), Number(msg.id))
          }
          
          nextTick(() => {
            const el = document.querySelector('.messages')
            if (el) el.scrollTop = el.scrollHeight
          })
        }
      } else {
        // 3. 非当前会话，且不是自己发的，增加未读计数并发送通知
        if (Number(msg.senderId) !== Number(currentUserId)) {
          const conv = conversationStore.conversations.find(c => Number(c.id) === Number(msg.conversationId))
          if (conv) {
            conv.unreadCount = (conv.unreadCount || 0) + 1
          }
          sendBrowserNotification(msg)
        }
      }
    })

    wsClient.on('message_read', (data: any) => {
      messages.value.forEach(m => {
        if (m.senderId !== data.readBy && m.id <= data.messageId) {
          m.status = 'READ'
        }
      })
    })

    wsClient.on('message_recalled', (data: any) => {
      const messageId = typeof data === 'number' ? data : data?.messageId
      if (messageId) {
        const msg = messages.value.find(m => m.id === messageId)
        if (msg) { 
          msg.recall = true
          msg.content = '消息已撤回' 
          msg.status = 'RECALLED'
        }
      }
    })

    wsClient.on('mention', (data: any) => {
      console.log('[Store] Mentioned in message:', data)
      ElMessage({
        message: `${data.senderName} 在群聊中 @ 了你`,
        type: 'warning',
        duration: 5000,
        showClose: true
      })
    })
  }

  function sendBrowserNotification(msg: any) {
    if ('Notification' in window && Notification.permission === 'granted') {
      const senderName = msg.senderName || '新消息'
      const content = msg.content || ''
      new Notification(senderName, {
        body: content.length > 50 ? content.substring(0, 50) + '...' : content,
        icon: '/favicon.ico',
        tag: msg.id?.toString()
      })
    }
  }

  async function fetchMessages(conversationId: number, beforeId?: number) {
    const isLoadMore = !!beforeId
    if (isLoadMore) {
      loadingMore.value = true
    } else {
      loading.value = true
      hasMore.value = true
    }
    try {
      const res = await request.get('/conversation/' + conversationId + '/messages', {
        params: beforeId ? { beforeId } : {}
      })
      const newMessages: Message[] = res.data || []
      if (isLoadMore) {
        messages.value.unshift(...newMessages)
        if (newMessages.length < 50) {
          hasMore.value = false
        }
      } else {
        messages.value = newMessages
      }
    } finally {
      if (isLoadMore) {
        loadingMore.value = false
      } else {
        loading.value = false
      }
    }
  }

  async function sendMessage(params: { conversationId: number; content: string; contentType: string; meta?: string; replyId?: number }) {
    const currentUserId = Number(localStorage.getItem('userId'))
    const tempId = String(Date.now())
    
    // 自动开启机器人打字提示
    const conversationStore = useConversationStore()
    const conv = conversationStore.conversations.find(c => Number(c.id) === Number(params.conversationId))
    if (conv?.type === 'SINGLE' && conv.targetUser?.role === 'ROBOT') {
      isRobotTyping.value = true
    }

    // 将 tempId 放入 meta 以便去重
    const metaObj = params.meta ? JSON.parse(params.meta) : {}
    metaObj.clientMsgId = tempId
    const finalMeta = JSON.stringify(metaObj)

    const tempMsg: Message = {
      id: Number(tempId),
      conversationId: params.conversationId,
      senderId: currentUserId,
      content: params.content,
      contentType: params.contentType as any,
      meta: finalMeta,
      replyId: params.replyId,
      status: 'SENDING',
      createTime: new Date().toISOString()
    }
    messages.value.push(tempMsg)

    try {
      const res = await request.post('/message/send', { 
        ...params, 
        meta: finalMeta,
        senderId: currentUserId 
      })
      
      const idx = messages.value.findIndex(m => String(m.id) === tempId)
      if (idx > -1) {
        messages.value[idx] = { ...res.data, status: 'DELIVERED' }
      }
      return res.data
    } catch (e: any) {
      const idx = messages.value.findIndex(m => String(m.id) === tempId)
      if (idx > -1) messages.value[idx].status = 'FAILED'
      isRobotTyping.value = false // 发送失败则关闭机器人打字提示
      throw e
    }
  }

  async function recallMessage(messageId: number) {
    await request.put(`/message/${messageId}/recall`)
    const msg = messages.value.find(m => m.id === messageId)
    if (msg) { 
      msg.recall = true
      msg.content = '消息已撤回'
      msg.status = 'RECALLED' 
    }
  }

  async function markAsRead(conversationId: number, messageId: number) {
    await request.put('/message/read', { 
      userId: Number(localStorage.getItem('userId')), 
      conversationId, 
      messageId 
    })
  }

  function clear() {
    messages.value = []
    lastReadId.value = 0
    hasMore.value = true
  }

  return {
    messages, loading, loadingMore, hasMore, lastReadId, isRobotTyping,
    initWsListener, fetchMessages, sendMessage, recallMessage, markAsRead, clear
  }
})
