import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Message } from '@/types'
import request from '@/utils/request'

export const useMessageStore = defineStore('message', () => {
  const messages = ref<Message[]>([])
  const loading = ref(false)

  async function fetchMessages(conversationId: number, page = 1, size = 50) {
    loading.value = true
    try {
      const res = await request.get('/message/list', { params: { conversationId, page, size } })
      messages.value = (res.data || []).reverse()
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

  return { messages, loading, fetchMessages, sendMessage, recallMessage, appendMessage, clear }
})
