import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Conversation } from '@/types'
import request from '@/utils/request'

export const useConversationStore = defineStore('conversation', () => {
  const conversations = ref<Conversation[]>([])
  const currentConversation = ref<Conversation | null>(null)
  const loading = ref(false)

  async function fetchConversations() {
    loading.value = true
    try {
      const res = await request.get('/conversation/list')
      conversations.value = res.data || []
    } finally {
      loading.value = false
    }
  }

  function setCurrentConversation(conv: Conversation) {
    currentConversation.value = conv
    // clear unread
    const idx = conversations.value.findIndex(c => c.id === conv.id)
    if (idx > -1) conversations.value[idx].unreadCount = 0
  }

  async function createConversation(type: 'SINGLE' | 'GROUP', typeId: number) {
    const res = await request.post('/conversation/create', { type, typeId })
    conversations.value.unshift(res.data)
    return res.data
  }

  function updateLastMessage(conversationId: number, content: string, time: string) {
    const conv = conversations.value.find(c => c.id === conversationId)
    if (conv) {
      conv.lastMessageContent = content
      conv.lastMessageTime = time
    }
  }

  return { conversations, currentConversation, loading, fetchConversations, setCurrentConversation, createConversation, updateLastMessage }
})
