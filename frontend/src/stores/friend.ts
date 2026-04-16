import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { User, FriendRequest } from '@/types'
import request from '@/utils/request'
import wsClient from '@/utils/websocket'

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
      const res = await request.get('/friend/list')
      friends.value = res.data || []
    } finally {
      loading.value = false
    }
  }

  async function fetchRequests() {
    const res = await request.get('/friend/request/list')
    requests.value = res.data || []
  }

  async function sendRequest(toUserId: number, reason?: string) {
    return await request.post('/friend/request', { toUserId, reason })
  }

  async function handleRequest(requestId: number, action: 'agree' | 'reject') {
    await request.put(`/friend/request/${requestId}/${action}`)
    await fetchRequests()
    if (action === 'agree') await fetchFriends()
  }

  async function deleteFriend(friendId: number) {
    await request.delete(`/friend/${friendId}`)
    friends.value = friends.value.filter(f => f.id !== friendId)
  }

  async function searchUsers(keyword: string) {
    const res = await request.get('/user/search', { params: { keyword } })
    return res.data || []
  }

  return { friends, requests, loading, initWsListener, fetchFriends, fetchRequests, sendRequest, handleRequest, deleteFriend, searchUsers }
})
