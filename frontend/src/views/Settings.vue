<template>
  <div class="settings-page">
    <!-- 左侧设置列表 -->
    <div class="settings-nav">
      <div class="settings-header">
        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.066 2.573c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.573 1.066c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.066-2.573c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"></path><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path></svg>
        <span>设置</span>
      </div>
      <div class="settings-list">
        <div
          v-for="item in settingsItems"
          :key="item.key"
          class="settings-item"
          :class="{ active: activeSetting === item.key }"
          @click="activeSetting = item.key"
        >
          <span class="item-icon" v-html="item.icon"></span>
          <span class="item-label">{{ item.label }}</span>
          <svg class="item-arrow" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path></svg>
        </div>
      </div>
    </div>

    <!-- 右侧设置内容 -->
    <div class="settings-content">
      <!-- 个人资料 -->
      <div v-if="activeSetting === 'profile'" class="settings-panel">
        <h3 class="panel-title">个人资料</h3>
        <div class="profile-avatar">
          <div class="avatar-circle" :style="{ background: avatarBg }">
            {{ (userStore.currentUser?.nickname || userStore.currentUser?.username)?.[0] }}
          </div>
        </div>
        <div class="form-group">
          <label>用户名</label>
          <input type="text" :value="userStore.currentUser?.username" disabled />
        </div>
        <div class="form-group">
          <label>昵称</label>
          <input type="text" v-model="profileForm.nickname" placeholder="请输入昵称" />
        </div>
        <div class="form-group">
          <label>个性签名</label>
          <textarea v-model="profileForm.signature" placeholder="请输入个性签名" rows="2"></textarea>
        </div>
        <div class="form-group">
          <label>手机号</label>
          <input type="text" v-model="profileForm.phone" placeholder="请输入手机号" />
        </div>
        <button class="btn-primary" @click="saveProfile">保存</button>
      </div>

      <!-- 账号安全 -->
      <div v-else-if="activeSetting === 'security'" class="settings-panel">
        <h3 class="panel-title">账号安全</h3>
        <div class="form-group">
          <label>当前密码</label>
          <input type="password" v-model="pwdForm.oldPwd" placeholder="请输入当前密码" />
        </div>
        <div class="form-group">
          <label>新密码</label>
          <input type="password" v-model="pwdForm.newPwd" placeholder="请输入新密码" />
        </div>
        <div class="form-group">
          <label>确认新密码</label>
          <input type="password" v-model="pwdForm.confirmPwd" placeholder="请确认新密码" />
        </div>
        <button class="btn-primary" @click="changePwd">修改密码</button>
      </div>

      <!-- 消息通知 -->
      <div v-else-if="activeSetting === 'notification'" class="settings-panel">
        <h3 class="panel-title">消息通知</h3>
        <div class="setting-row">
          <div class="setting-info">
            <div class="setting-name">接收消息通知</div>
            <div class="setting-desc">接收新消息时显示通知</div>
          </div>
          <el-switch v-model="notifySettings.message" />
        </div>
        <div class="setting-row">
          <div class="setting-info">
            <div class="setting-name">声音提醒</div>
            <div class="setting-desc">收到消息时播放提示音</div>
          </div>
          <el-switch v-model="notifySettings.sound" />
        </div>
        <div class="setting-row">
          <div class="setting-info">
            <div class="setting-name">桌面通知</div>
            <div class="setting-desc">在桌面显示通知</div>
          </div>
          <el-switch v-model="notifySettings.desktop" />
        </div>
      </div>

      <!-- 隐私设置 -->
      <div v-else-if="activeSetting === 'privacy'" class="settings-panel">
        <h3 class="panel-title">隐私设置</h3>
        <div class="setting-row">
          <div class="setting-info">
            <div class="setting-name">显示在线状态</div>
            <div class="setting-desc">其他人可以看到你的在线状态</div>
          </div>
          <el-switch v-model="privacySettings.showOnline" />
        </div>
        <div class="setting-row">
          <div class="setting-info">
            <div class="setting-name">允许私聊</div>
            <div class="setting-desc">允许其他人向你发起私聊</div>
          </div>
          <el-switch v-model="privacySettings.allowPm" />
        </div>
      </div>

      <!-- 关于無界 -->
      <div v-else-if="activeSetting === 'about'" class="settings-panel">
        <h3 class="panel-title">关于無界</h3>
        <div class="about-info">
          <div class="about-logo">無界</div>
          <div class="about-version">版本 1.0.0</div>
          <div class="about-desc">突破边界，沟通无界</div>
        </div>
        <div class="about-github">
          <div class="github-header">
            <svg class="github-icon" viewBox="0 0 16 16" fill="currentColor"><path d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27.68 0 1.36.09 2 .27 1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.013 8.013 0 0016 8c0-4.42-3.58-8-8-8z"></path></svg>
            <span>GitHub 仓库</span>
          </div>
          <a class="github-link" href="https://github.com/PhyQxx/wujie-im" target="_blank" rel="noopener noreferrer">
            PhyQxx/wujie-im
          </a>
          <div class="github-stats" v-if="githubInfo">
            <div class="stat-item">
              <span class="stat-value">{{ githubInfo.stargazers_count }}</span>
              <span class="stat-label">Stars</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ githubInfo.forks_count }}</span>
              <span class="stat-label">Forks</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ githubInfo.open_issues_count }}</span>
              <span class="stat-label">Issues</span>
            </div>
          </div>
          <div class="about-tech">
            <span class="tech-tag">Vue 3</span>
            <span class="tech-tag">Spring Boot</span>
            <span class="tech-tag">WebSocket</span>
            <span class="tech-tag">MySQL</span>
            <span class="tech-tag">Redis</span>
          </div>
        </div>
      </div>

      <!-- 退出登录 -->
      <div v-else-if="activeSetting === 'logout'" class="settings-panel">
        <h3 class="panel-title">退出登录</h3>
        <p class="logout-tip">确定要退出当前账号吗？</p>
        <button class="btn-danger" @click="logout">退出登录</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const userStore = useUserStore()
const router = useRouter()

const activeSetting = ref('profile')
const profileForm = ref({ nickname: '', signature: '', phone: '' })
const pwdForm = ref({ oldPwd: '', newPwd: '', confirmPwd: '' })
const notifySettings = ref({ message: true, sound: true, desktop: false })
const privacySettings = ref({ showOnline: true, allowPm: true })
const githubInfo = ref<{ stargazers_count: number; forks_count: number; open_issues_count: number } | null>(null)

const settingsItems = [
  { key: 'profile', label: '个人资料', icon: '👤' },
  { key: 'security', label: '账号安全', icon: '🔒' },
  { key: 'notification', label: '消息通知', icon: '🔔' },
  { key: 'privacy', label: '隐私设置', icon: '🛡️' },
  { key: 'about', label: '关于無界', icon: 'ℹ️' },
  { key: 'logout', label: '退出登录', icon: '🚪' }
]

const avatarBg = ref('#4F46E5')

onMounted(async () => {
  await userStore.fetchCurrentUser()
  const u = userStore.currentUser
  if (u) {
    profileForm.value = { nickname: u.nickname || '', signature: u.signature || '', phone: u.phone || '' }
  }
  fetch('https://api.github.com/repos/PhyQxx/wujie-im')
    .then(res => res.ok ? res.json() : null)
    .then(data => { if (data) githubInfo.value = data })
    .catch(() => {})
})

async function saveProfile() {
  await userStore.updateProfile(profileForm.value)
  ElMessage.success('资料已更新')
}

async function changePwd() {
  if (pwdForm.value.newPwd !== pwdForm.value.confirmPwd) {
    ElMessage.error('两次密码不一致')
    return
  }
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
.settings-page {
  display: flex;
  flex: 1;
  overflow: hidden;
}
.settings-nav {
  width: 240px;
  border-right: 1px solid var(--border, #E5E7EB);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}
.settings-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px 16px;
  font-size: 16px;
  font-weight: 600;
  border-bottom: 1px solid var(--border, #E5E7EB);
}
.settings-header svg { width: 20px; height: 20px; color: #6B7280; }
.settings-list { flex: 1; padding: 8px; }
.settings-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  color: #6B7280;
  font-size: 14px;
  border-left: 3px solid transparent;
}
.settings-item:hover { background: #F3F4F6; }
.settings-item.active {
  background: #EEF2FF;
  color: #4F46E5;
  border-left-color: #4F46E5;
}
.item-icon { font-size: 16px; }
.item-label { flex: 1; }
.item-arrow { width: 16px; height: 16px; }
.settings-content {
  flex: 1;
  overflow-y: auto;
  background: var(--surface-1, #fff);
}
.settings-panel {
  max-width: 480px;
  padding: 24px;
}
.panel-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 24px;
}
.profile-avatar {
  text-align: center;
  margin-bottom: 24px;
}
.avatar-circle {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: 600;
  color: white;
}
.form-group { margin-bottom: 16px; }
.form-group label {
  display: block;
  font-size: 13px;
  color: #6B7280;
  margin-bottom: 6px;
}
.form-group input,
.form-group textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--border, #E5E7EB);
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  background: var(--surface-2, #F9FAFB);
}
.form-group input:focus,
.form-group textarea:focus { border-color: #4F46E5; background: white; }
.form-group input:disabled { background: #F3F4F6; color: #9CA3AF; }
.btn-primary {
  width: 100%;
  height: 44px;
  background: #4F46E5;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  margin-top: 8px;
}
.btn-danger {
  width: 100%;
  height: 44px;
  background: #EF4444;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
}
.setting-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 0;
  border-bottom: 1px solid #F3F4F6;
}
.setting-info {}
.setting-name { font-size: 14px; font-weight: 500; margin-bottom: 2px; }
.setting-desc { font-size: 12px; color: #9CA3AF; }
.about-info { text-align: center; padding: 40px 0 24px; }
.about-logo { font-size: 32px; font-weight: 700; color: #4F46E5; margin-bottom: 8px; }
.about-version { font-size: 14px; color: #6B7280; margin-bottom: 8px; }
.about-desc { font-size: 14px; color: #9CA3AF; }
.about-github {
  border: 1px solid var(--border, #E5E7EB);
  border-radius: 12px;
  padding: 20px;
  margin-top: 16px;
}
.github-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 12px;
}
.github-icon { width: 20px; height: 20px; }
.github-link {
  display: block;
  font-size: 15px;
  font-weight: 500;
  color: #4F46E5;
  text-decoration: none;
  margin-bottom: 16px;
}
.github-link:hover { text-decoration: underline; }
.github-stats {
  display: flex;
  gap: 24px;
  padding: 12px 0;
  border-top: 1px solid #F3F4F6;
  border-bottom: 1px solid #F3F4F6;
  margin-bottom: 16px;
}
.stat-item { display: flex; flex-direction: column; align-items: center; }
.stat-value { font-size: 18px; font-weight: 700; color: #111827; }
.stat-label { font-size: 12px; color: #9CA3AF; }
.about-tech {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.tech-tag {
  padding: 4px 10px;
  background: #EEF2FF;
  color: #4F46E5;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
}
.logout-tip { color: #6B7280; margin-bottom: 16px; }
</style>
