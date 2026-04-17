import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { GroupInfo } from '@/types'
import request from '@/utils/request'
import { useUserStore } from './user'

export interface GroupMember {
  id: number
  userId: number
  role: string
  muted: number
  mutedUntil: string
  joinTime: string
  user?: any
}

export const useGroupStore = defineStore('group', () => {
  const groups = ref<GroupInfo[]>([])
  const currentGroup = ref<GroupInfo | null>(null)
  const members = ref<GroupMember[]>([])
  const loading = ref(false)

  async function fetchGroups() {
    loading.value = true
    try {
      const userId = localStorage.getItem('userId')
      if (!userId) return
      const res = await request.get(`/group/list/${userId}`)
      groups.value = res.data || []
    } finally {
      loading.value = false
    }
  }

  async function createGroup(name: string, memberIds: number[], type: 'PUBLIC' | 'PRIVATE' = 'PUBLIC') {
    const userId = localStorage.getItem('userId')
    const res = await request.post('/group/create', {
      name,
      type,
      ownerId: userId,
      memberIds
    })
    groups.value.unshift(res.data)
    return res.data
  }

  async function fetchMembers(groupId: number) {
    const res = await request.get(`/group/members/${groupId}`)
    members.value = res.data || []
  }

  async function fetchGroupDetail(groupId: number) {
    const res = await request.get(`/group/${groupId}`)
    currentGroup.value = res.data
    return res.data
  }

  async function updateGroup(groupId: number, data: { name?: string; avatar?: string; announcement?: string }) {
    const userStore = useUserStore()
    await request.put(`/group/update/${groupId}?operatorId=${userStore.currentUser?.id}`, data)
    const idx = groups.value.findIndex(g => g.id === groupId)
    if (idx > -1) Object.assign(groups.value[idx], data)
    if (currentGroup.value?.id === groupId) Object.assign(currentGroup.value, data)
  }

  async function inviteMembers(groupId: number, userIds: number[]) {
    const userStore = useUserStore()
    await request.post(`/group/invite/${groupId}?inviterId=${userStore.currentUser?.id}`, { userIds })
    await fetchMembers(groupId)
  }

  async function removeMember(groupId: number, userId: number) {
    const userStore = useUserStore()
    await request.delete(`/group/member/${groupId}/${userId}?operatorId=${userStore.currentUser?.id}`)
    members.value = members.value.filter(m => m.userId !== userId)
  }

  async function leaveGroup(groupId: number) {
    const userStore = useUserStore()
    await request.post(`/group/leave/${groupId}?userId=${userStore.currentUser?.id}`)
    groups.value = groups.value.filter(g => g.id !== groupId)
    if (currentGroup.value?.id === groupId) currentGroup.value = null
  }

  async function dissolveGroup(groupId: number) {
    const userStore = useUserStore()
    await request.delete(`/group/${groupId}?userId=${userStore.currentUser?.id}`)
    groups.value = groups.value.filter(g => g.id !== groupId)
    if (currentGroup.value?.id === groupId) currentGroup.value = null
  }

  async function muteMember(groupId: number, targetUserId: number, minutes?: number) {
    const userStore = useUserStore()
    await request.put(`/group/mute/${groupId}/${targetUserId}?operatorId=${userStore.currentUser?.id}&minutes=${minutes || 0}`)
    await fetchMembers(groupId)
  }

  async function unmuteMember(groupId: number, targetUserId: number) {
    const userStore = useUserStore()
    await request.put(`/group/unmute/${groupId}/${targetUserId}?operatorId=${userStore.currentUser?.id}`)
    await fetchMembers(groupId)
  }

  async function setAdmin(groupId: number, targetUserId: number, isAdmin: boolean) {
    const userStore = useUserStore()
    await request.put(`/group/admin/${groupId}/${targetUserId}?operatorId=${userStore.currentUser?.id}&isAdmin=${isAdmin}`)
    await fetchMembers(groupId)
  }

  async function getJoinRequests(groupId: number) {
    return request.get(`/group/join-requests/${groupId}`)
  }

  async function getMyRequests() {
    const userStore = useUserStore()
    return request.get(`/group/my-requests/${userStore.currentUser?.id}`)
  }

  async function handleJoinRequest(requestId: number, action: 'agree' | 'reject') {
    const userStore = useUserStore()
    await request.put(`/group/join-request/${requestId}?reviewerId=${userStore.currentUser?.id}&action=${action}`)
  }

  return {
    groups, currentGroup, members, loading,
    fetchGroups, createGroup, fetchMembers, fetchGroupDetail,
    updateGroup, inviteMembers, removeMember, leaveGroup, dissolveGroup,
    muteMember, unmuteMember, setAdmin,
    getJoinRequests, getMyRequests, handleJoinRequest
  }
})
