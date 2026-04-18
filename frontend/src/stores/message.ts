import { defineStore } from 'pinia'
import { ref, nextTick } from 'vue'
import type { Message } from '@/types'
import request from '@/utils/request'
import wsClient from '@/utils/websocket'
import { useConversationStore } from './conversation'

export const useMessageStore = defineStore('message', () => {
  const messages = ref<Message[]>([])
  const loading = ref(false)
  const lastReadId = ref<number>(0)

  function initWsListener() {
    wsClient.on('message', (msg: any) => {
      if (msg && msg.conversationId) {
        const currentUserId = Number(localStorage.getItem('userId'))
        if (msg.senderId === currentUserId) return
        const conversationStore = useConversationStore()
        const currentConvId = conversationStore.currentConversation?.id
        const isCurrentConv = currentConvId && msg.conversationId === currentConvId
        const exists = messages.value.some(m => m.id === msg.id)
        if (!exists) {
          console.log('[WS message] added msg.id=' + msg.id + ' convId=' + msg.conversationId + ' curConvId=' + currentConvId)
          messages.value.push(msg)
          if (isCurrentConv) {
            // 当前会话消息，滚动到底部
            nextTick(() => {
              const el = document.querySelector('.messages')
              if (el) el.scrollTop = el.scrollHeight
            })
          } else {
            // 非当前会话消息，更新会话列表的未读数
            const conv = conversationStore.conversations.find(c => c.id === msg.conversationId)
            if (conv) {
              conv.unreadCount = (conv.unreadCount || 0) + 1
            }
            conversationStore.updateLastMessage(msg.conversationId, msg.content, msg.createTime)
          }
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
        if (msg) { msg.recall = true; msg.content = '消息已撤回' }
      }
    })
  }

  async function fetchMessages(conversationId: number, beforeId?: number) {
    loading.value = true
    try {
      const res = await request.get('/conversation/' + conversationId + '/messages', {
        params: beforeId ? { beforeId } : {}
      })
      const newMessages: Message[] = res.data || []
      console.log('[fetchMessages] convId=' + conversationId + ' count=' + newMessages.length + ' beforeId=' + beforeId)
      if (beforeId) {
        messages.value.unshift(...newMessages)
      } else {
        console.log('[fetchMessages] SET messages to ' + newMessages.length)
        messages.value = newMessages
      }
    } finally {
      loading.value = false
    }
  }

  async function sendMessage(params: { conversationId: number; content: string; contentType: string; meta?: string; replyId?: number }) {
    const tempId = Date.now()
    const tempMsg: Message = {
      id: tempId,
      conversationId: params.conversationId,
      senderId: Number(localStorage.getItem('userId')),
      content: params.content,
      contentType: params.contentType as any,
      meta: params.meta,
      replyId: params.replyId,
      status: 'SENDING',
      createTime: new Date().toISOString()
    }
    messages.value.push(tempMsg)
    try {
      const res = await request.post('/message/send', { ...params, senderId: Number(localStorage.getItem('userId')) })
      const idx = messages.value.findIndex(m => m.id === tempId)
      if (idx > -1) messages.value[idx] = { ...res.data, status: 'DELIVERED' }
      return res.data
    } catch (e: any) {
      const idx = messages.value.findIndex(m => m.id === tempId)
      if (idx > -1) messages.value[idx].status = 'FAILED'
      throw e
    }
  }

  async function recallMessage(messageId: number) {
    await request.put(`/message/${messageId}/recall`)
    const msg = messages.value.find(m => m.id === messageId)
    if (msg) { msg.recall = true; msg.content = '消息已撤回'; msg.status = 'RECALLED' }
  }

  async function markAsRead(conversationId: number, messageId: number) {
    await request.put('/message/read', { userId: Number(localStorage.getItem('userId')), conversationId, messageId })
  }

  function clear() {
    messages.value = []
    lastReadId.value = 0
  }

  return {
    messages, loading, lastReadId,
    initWsListener, fetchMessages, sendMessage, recallMessage, markAsRead, clear
  }
})
