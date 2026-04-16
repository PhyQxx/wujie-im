import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { User } from '@/types'
import request from '@/utils/request'

export const useUserStore = defineStore('user', () => {
  const currentUser = ref<User | null>(null)
  const token = ref<string>(localStorage.getItem('accessToken') || '')

  async function login(username: string, password: string) {
    const res = await request.post('/auth/login', { username, password })
    token.value = res.data.accessToken
    localStorage.setItem('accessToken', res.data.accessToken)
    localStorage.setItem('refreshToken', res.data.refreshToken)
    localStorage.setItem('userId', String(res.data.userId))
    await fetchCurrentUser()
    return res
  }

  async function register(username: string, password: string, phone?: string) {
    return await request.post('/auth/register', { username, password, phone })
  }

  async function fetchCurrentUser() {
    const userId = localStorage.getItem('userId')
    if (!userId) return
    const res = await request.get(`/user/${userId}`)
    currentUser.value = res.data
  }

  async function updateProfile(data: Partial<User>) {
    const userId = localStorage.getItem('userId')
    const res = await request.put(`/user/${userId}`, data)
    currentUser.value = res.data
    return res
  }

  async function updateStatus(status: string) {
    const res = await request.put('/user/status', { status })
    if (currentUser.value) currentUser.value.userStatus = status as any
    return res
  }

  function logout() {
    token.value = ''
    currentUser.value = null
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('userId')
  }

  return { currentUser, token, login, register, fetchCurrentUser, updateProfile, updateStatus, logout }
})
