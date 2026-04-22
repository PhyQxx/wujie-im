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
      const userId = localStorage.getItem('userId')
      const res = await request.get(`/conversation/list/${userId}`)
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
    const userId = localStorage.getItem('userId')
    let res
    if (type === 'SINGLE') {
      res = await request.post('/conversation/single', { userId, otherUserId: typeId })
    } else {
      res = await request.post('/conversation/group', { userId, groupId: typeId })
    }
    conversations.value.unshift(res.data)
    return res.data
  }

  function updateLastMessage(conversationId: number, content: string, time: string) {
    const idx = conversations.value.findIndex(c => c.id === conversationId)
    if (idx > -1) {
      conversations.value[idx].lastMessageContent = content
      conversations.value[idx].lastMessageTime = time
      // move to front
      const conv = conversations.value.splice(idx, 1)[0]
      conversations.value.unshift(conv)
    }
  }

  return { conversations, currentConversation, loading, fetchConversations, setCurrentConversation, createConversation, updateLastMessage }
})
