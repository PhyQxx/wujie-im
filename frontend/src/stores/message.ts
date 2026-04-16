import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Message } from '@/types'
import request from '@/utils/request'
import wsClient from '@/utils/websocket'

export const useMessageStore = defineStore('message', () => {
  const messages = ref<Message[]>([])
  const loading = ref(false)

  function initWsListener() {
    wsClient.on('message', (msg: any) => {
      if (msg && msg.conversationId) {
        messages.value.unshift(msg)
      }
    })
    wsClient.on('recall_message', (messageId: number) => {
      const idx = messages.value.findIndex(m => m.id === messageId)
      if (idx > -1) {
        messages.value[idx].recall = true
        messages.value[idx].status = 'RECALLED'
      }
    })
  }

  async function fetchMessages(conversationId: number, beforeId?: number) {
    loading.value = true
    try {
      const res = await request.get('/conversation/' + conversationId + '/messages', {
        params: beforeId ? { beforeId } : {}
      })
      const newMessages = res.data || []
      if (beforeId) {
        messages.value.push(...newMessages)
      } else {
        messages.value = newMessages
      }
    } finally {
      loading.value = false
    }
  }

  async function sendMessage(params: { conversationId: number; content: string; contentType: string; replyId?: number }) {
    const tempMsg: Message = {
      id: Date.now(),
      conversationId: params.conversationId,
      senderId: Number(localStorage.getItem('userId')),
      content: params.content,
      contentType: params.contentType as any,
      status: 'SENDING',
      createTime: new Date().toISOString()
    }
    messages.value.push(tempMsg)
    try {
      const res = await request.post('/message/send', params)
      const idx = messages.value.findIndex(m => m.id === tempMsg.id)
      if (idx > -1) messages.value[idx] = { ...res.data, status: 'SENT' }
      return res.data
    } catch (e) {
      const idx = messages.value.findIndex(m => m.id === tempMsg.id)
      if (idx > -1) messages.value[idx].status = 'SENT'
      throw e
    }
  }

  async function recallMessage(messageId: number) {
    await request.put(`/message/${messageId}/recall`)
    const msg = messages.value.find(m => m.id === messageId)
    if (msg) { msg.recall = true; msg.content = '消息已撤回' }
  }

  function appendMessage(msg: Message) {
    messages.value.push(msg)
  }

  function clear() {
    messages.value = []
  }

  return { messages, loading, initWsListener, fetchMessages, sendMessage, recallMessage, appendMessage, clear }
})
