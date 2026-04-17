import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Message } from '@/types'
import request from '@/utils/request'
import wsClient from '@/utils/websocket'

export const useMessageStore = defineStore('message', () => {
  const messages = ref<Message[]>([])
  const loading = ref(false)
  const lastReadId = ref<number>(0)

  function initWsListener() {
    wsClient.on('message', (msg: any) => {
      if (msg && msg.conversationId) {
        // 避免重复：检查是否已存在
        const exists = messages.value.some(m => m.id === msg.id)
        if (!exists) {
          messages.value.push(msg)
        }
      }
    })
    wsClient.on('message_read', (data: any) => {
      // 已读回执：更新所有相关消息状态为"已读"
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
      if (beforeId) {
        messages.value.push(...newMessages)
      } else {
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

  function appendMessage(msg: Message) {
    const exists = messages.value.some(m => m.id === msg.id)
    if (!exists) messages.value.push(msg)
  }

  function clear() {
    messages.value = []
    lastReadId.value = 0
  }

  return {
    messages, loading, lastReadId,
    initWsListener, fetchMessages, sendMessage, recallMessage, markAsRead, appendMessage, clear
  }
})
