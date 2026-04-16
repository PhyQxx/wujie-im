import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Notification } from '@/types'
import request from '@/utils/request'

export const useNotificationStore = defineStore('notification', () => {
  const notifications = ref<Notification[]>([])
  const unreadCount = ref(0)
  const loading = ref(false)

  async function fetchNotifications(page = 1, size = 20) {
    loading.value = true
    try {
      const userId = localStorage.getItem('userId')
      const res = await request.get(`/notification/list/${userId}`, { params: { page, size } })
      notifications.value = res.data || []
      unreadCount.value = notifications.value.filter(n => !n.isRead).length
    } finally {
      loading.value = false
    }
  }

  async function fetchUnreadCount() {
    const userId = localStorage.getItem('userId')
    const res = await request.get(`/notification/unread/${userId}`)
    unreadCount.value = res.data?.length || 0
  }

  async function markRead(notificationId: number) {
    await request.put(`/notification/${notificationId}/read`)
    const n = notifications.value.find(n => n.id === notificationId)
    if (n) { n.isRead = true; unreadCount.value = Math.max(0, unreadCount.value - 1) }
  }

  async function markAllRead() {
    await request.put('/notification/read/all')
    notifications.value.forEach(n => n.isRead = true)
    unreadCount.value = 0
  }

  function appendNotification(n: Notification) {
    notifications.value.unshift(n)
    if (!n.isRead) unreadCount.value++
  }

  return { notifications, unreadCount, loading, fetchNotifications, fetchUnreadCount, markRead, markAllRead, appendNotification }
})
