import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { User, FriendRequest } from '@/types'
import request from '@/utils/request'
import wsClient from '@/utils/websocket'
import { useUserStore } from './user'

export const useFriendStore = defineStore('friend', () => {
  const friends = ref<User[]>([])
  const requests = ref<FriendRequest[]>([])
  const loading = ref(false)

  function initWsListener() {
    wsClient.on('notification', (notif: any) => {
      if (notif.type === 'FRIEND_REQUEST') {
        requests.value.unshift(notif)
      }
    })
  }

  async function fetchFriends() {
    loading.value = true
    try {
      const userId = localStorage.getItem('userId')
      if (!userId) return
      const res = await request.get(`/friend/list/${userId}`)
      friends.value = (res.data || []).map((item: any) => item.user)
    } finally {
      loading.value = false
    }
  }

  async function fetchRequests() {
    const userId = localStorage.getItem('userId')
    if (!userId) return
    const res = await request.get(`/friend/requests/${userId}`)
    requests.value = res.data || []
  }

  async function sendRequest(toUserId: number, reason?: string) {
    const userId = localStorage.getItem('userId')
    return await request.post('/friend/request', {
      fromUserId: userId,
      toUserId,
      reason
    })
  }

  async function handleRequest(requestId: number, action: 'agree' | 'reject') {
    await request.put(`/friend/request/${requestId}?action=${action.toUpperCase()}`)
    await fetchRequests()
    if (action === 'agree') await fetchFriends()
  }

  async function deleteFriend(friendId: number) {
    const userStore = useUserStore()
    await request.delete(`/friend/${userStore.currentUser?.id}/${friendId}`)
    friends.value = friends.value.filter(f => f.id !== friendId)
  }

  async function searchUsers(keyword: string, excludeId?: number) {
    const res = await request.get('/user/list', { params: { keyword, excludeId } })
    return res.data || []
  }

  return { friends, requests, loading, initWsListener, fetchFriends, fetchRequests, sendRequest, handleRequest, deleteFriend, searchUsers }
})
