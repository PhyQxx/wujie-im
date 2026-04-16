import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { GroupInfo, GroupMember } from '@/types'
import request from '@/utils/request'

export const useGroupStore = defineStore('group', () => {
  const groups = ref<GroupInfo[]>([])
  const currentGroup = ref<GroupInfo | null>(null)
  const members = ref<GroupMember[]>([])
  const loading = ref(false)

  async function fetchGroups() {
    loading.value = true
    try {
      const res = await request.get('/group/list')
      groups.value = res.data || []
    } finally {
      loading.value = false
    }
  }

  async function createGroup(name: string, memberIds: number[], type: 'PUBLIC' | 'PRIVATE' = 'PUBLIC') {
    const res = await request.post('/group/create', { name, memberIds, type })
    groups.value.unshift(res.data)
    return res.data
  }

  async function fetchMembers(groupId: number) {
    const res = await request.get(`/group/${groupId}/members`)
    members.value = res.data || []
  }

  async function updateGroup(groupId: number, data: Partial<GroupInfo>) {
    const res = await request.put(`/group/${groupId}`, data)
    const idx = groups.value.findIndex(g => g.id === groupId)
    if (idx > -1) groups.value[idx] = res.data
    return res.data
  }

  async function addMember(groupId: number, userId: number) {
    await request.post(`/group/${groupId}/member`, { userId })
    await fetchMembers(groupId)
  }

  async function removeMember(groupId: number, userId: number) {
    await request.delete(`/group/${groupId}/member/${userId}`)
    members.value = members.value.filter(m => m.userId !== userId)
  }

  async function leaveGroup(groupId: number) {
    await request.post(`/group/${groupId}/leave`)
    groups.value = groups.value.filter(g => g.id !== groupId)
  }

  return { groups, currentGroup, members, loading, fetchGroups, createGroup, fetchMembers, updateGroup, addMember, removeMember, leaveGroup }
})
