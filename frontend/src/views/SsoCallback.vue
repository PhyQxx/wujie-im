<template>
  <div class="sso-callback">
    <p>{{ status }}</p>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const status = ref('正在完成登录...')

onMounted(async () => {
  try {
    // 后端 SSO 登录成功后重定向到 /sso/callback#access_token=...&refresh_token=...&user=...
    const params = new URLSearchParams(window.location.hash.substring(1))
    const accessToken = params.get('access_token')
    const refreshToken = params.get('refresh_token')
    const userRaw = params.get('user')

    if (!accessToken || !refreshToken || !userRaw) {
      throw new Error('登录凭据缺失')
    }
    const user = JSON.parse(userRaw)
    localStorage.setItem('accessToken', accessToken)
    localStorage.setItem('refreshToken', refreshToken)
    localStorage.setItem('userId', String(user.userId))
    const role = user.role || 'USER'
    localStorage.setItem('userRole', role)
    localStorage.setItem('isAdmin', role === 'ADMIN' ? 'true' : 'false')

    router.replace('/')
  } catch {
    status.value = '登录失败，正在返回登录页...'
    setTimeout(() => router.replace({ path: '/login', query: { sso_error: 'SSO 回调数据无效' } }), 800)
  }
})
</script>

<style scoped>
.sso-callback {
  display: grid;
  place-items: center;
  min-height: 100vh;
  color: #909399;
  letter-spacing: 1px;
}
</style>
