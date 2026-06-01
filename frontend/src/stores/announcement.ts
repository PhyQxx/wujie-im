import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Announcement } from '@/types'
import { announcementApi } from '@/api/announcement'

export const useAnnouncementStore = defineStore('announcement', () => {
  const announcements = ref<Announcement[]>([])
  const unreadList = ref<Announcement[]>([])
  const loading = ref(false)

  async function fetchPublished() {
    loading.value = true
    try {
      const userId = Number(localStorage.getItem('userId'))
      const res = await announcementApi.list(userId)
      announcements.value = res.data || []
    } finally {
      loading.value = false
    }
  }

  async function fetchUnread() {
    const userId = Number(localStorage.getItem('userId'))
    const res = await announcementApi.unread(userId)
    unreadList.value = res.data || []
  }

  async function markRead(announcementId: number) {
    const userId = Number(localStorage.getItem('userId'))
    await announcementApi.markRead(announcementId, userId)
    const a = announcements.value.find(a => a.id === announcementId)
    if (a) a.isRead = true
    unreadList.value = unreadList.value.filter(a => a.id !== announcementId)
  }

  return { announcements, unreadList, loading, fetchPublished, fetchUnread, markRead }
})
