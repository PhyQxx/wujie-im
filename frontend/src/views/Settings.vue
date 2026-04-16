<template>
  <div class="settings-page">
    <h2>个人设置</h2>

    <el-tabs tab-position="left">
      <el-tab-pane label="个人资料">
        <el-form :model="profileForm" label-width="80px">
          <el-form-item label="头像">
            <el-avatar :size="64" :src="userStore.currentUser?.avatar">
              {{ userStore.currentUser?.username?.[0] }}
            </el-avatar>
          </el-form-item>
          <el-form-item label="用户名"><el-input v-model="profileForm.username" disabled /></el-form-item>
          <el-form-item label="昵称"><el-input v-model="profileForm.nickname" /></el-form-item>
          <el-form-item label="签名"><el-input v-model="profileForm.signature" type="textarea" :rows="2" /></el-form-item>
          <el-form-item label="手机号"><el-input v-model="profileForm.phone" /></el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveProfile">保存</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="在线状态">
        <el-form label-width="80px">
          <el-form-item label="当前状态">
            <el-select v-model="currentStatus" @change="updateStatus">
              <el-option label="🟢 在线" value="ONLINE" />
              <el-option label="🌙 离开" value="AWAY" />
              <el-option label="🔴 勿扰" value="DND" />
              <el-option label="⚫ 隐身" value="OFFLINE" />
            </el-select>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="账号安全">
        <el-form :model="pwdForm" label-width="100px">
          <el-form-item label="当前密码"><el-input v-model="pwdForm.oldPwd" type="password" show-password /></el-form-item>
          <el-form-item label="新密码"><el-input v-model="pwdForm.newPwd" type="password" show-password /></el-form-item>
          <el-form-item label="确认新密码"><el-input v-model="pwdForm.confirmPwd" type="password" show-password /></el-form-item>
          <el-form-item>
            <el-button type="primary" @click="changePwd">修改密码</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="退出登录">
        <div style="padding:20px">
          <el-button type="danger" @click="logout">退出登录</el-button>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const userStore = useUserStore()
const router = useRouter()
const currentStatus = ref(userStore.currentUser?.userStatus || 'ONLINE')
const profileForm = ref({
  username: userStore.currentUser?.username || '',
  nickname: userStore.currentUser?.nickname || '',
  signature: userStore.currentUser?.signature || '',
  phone: userStore.currentUser?.phone || ''
})
const pwdForm = ref({ oldPwd: '', newPwd: '', confirmPwd: '' })

onMounted(async () => {
  await userStore.fetchCurrentUser()
  const u = userStore.currentUser
  if (u) {
    profileForm.value = { username: u.username, nickname: u.nickname || '', signature: u.signature || '', phone: u.phone || '' }
    currentStatus.value = u.userStatus
  }
})

async function saveProfile() {
  await userStore.updateProfile({ nickname: profileForm.value.nickname, signature: profileForm.value.signature, phone: profileForm.value.phone })
  ElMessage.success('资料已更新')
}

async function updateStatus(status: string) {
  await userStore.updateStatus(status)
  ElMessage.success('状态已更新')
}

async function changePwd() {
  if (pwdForm.value.newPwd !== pwdForm.value.confirmPwd) { ElMessage.error('两次密码不一致'); return }
  await request.put('/user/password', { oldPassword: pwdForm.value.oldPwd, newPassword: pwdForm.value.newPwd })
  ElMessage.success('密码已修改')
  pwdForm.value = { oldPwd: '', newPwd: '', confirmPwd: '' }
}

function logout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.settings-page { padding: 24px; height: 100%; overflow: auto; }
.settings-page h2 { font-size: 20px; font-weight: 700; margin-bottom: 24px; }
</style>
