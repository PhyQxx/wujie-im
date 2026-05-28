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

    wsClient.on('user_status', (data: any) => {
      const { userId, status } = data
      const friend = friends.value.find(f => f.id === userId)
      if (friend) {
        friend.userStatus = status
      }
    })
  }

  async function fetchFriends() {
    loading.value = true
    try {
      const userId = localStorage.getItem('userId')
      console.log('[FriendStore] Fetching friends for userId:', userId)
      if (!userId) return
      const res = await request.get(`/friend/list/${userId}`)
      console.log('[FriendStore] Raw friends response:', res.data)
      friends.value = (res.data || []).map((item: any) => item.user)
      console.log('[FriendStore] Mapped friends:', friends.value)
    } finally {
      loading.value = false
    }
  }

  async function fetchRequests() {
    const userId = localStorage.getItem('userId')
    if (!userId) return
    console.log('[FriendStore] Fetching requests for userId:', userId)
    const res = await request.get(`/friend/requests/${userId}`)
    console.log('[FriendStore] Raw requests response:', res.data)
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
  const res = await request.get('/user/list', {
    params: { keyword, excludeId }
  })
  return res.data
}

  return { friends, requests, loading, initWsListener, fetchFriends, fetchRequests, sendRequest, handleRequest, deleteFriend, searchUsers }
})
