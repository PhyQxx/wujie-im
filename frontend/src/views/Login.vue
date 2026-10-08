<template>
  <div class="login-page">
    <div class="login-wrapper">
      <!-- 品牌区 -->
      <div class="login-brand">
        <div class="brand-content">
          <div class="brand-logo">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M3.055 11H5a2 2 0 012 2v1a2 2 0 002 2 2 2 0 012 2v2.945M8 3.935V5.5A2.5 2.5 0 0010.5 8h.5a2 2 0 012 2 2 2 0 104 0 2 2 0 012-2h1.064M15 20.488V18a2 2 0 012-2h3.064M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
            </svg>
          </div>
          <div class="brand-title">無界</div>
          <div class="brand-subtitle">突破边界，沟通无界</div>
          <div class="brand-features">
            <div class="brand-feature">
              <div class="brand-feature-icon">
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"></path></svg>
              </div>
              <span>多模型 AI 机器人，即问即答</span>
            </div>
            <div class="brand-feature">
              <div class="brand-feature-icon">
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"></path></svg>
              </div>
              <span>群聊频道，灵活管理</span>
            </div>
            <div class="brand-feature">
              <div class="brand-feature-icon">
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path></svg>
              </div>
              <span>端到端加密，消息安全</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 登录表单 -->
      <div class="login-form-area">
        <div class="form-container">
          <h2>欢迎回来 👋</h2>
          <p>请登录您的账号继续使用</p>

          <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent="handleLogin">
            <div class="form-group">
              <label>用户名 / 邮箱</label>
              <div class="input-wrapper">
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path></svg>
                <el-input v-model="form.username" placeholder="请输入用户名或邮箱" size="large" />
              </div>
            </div>

            <div class="form-group">
              <label>密码</label>
              <div class="input-wrapper">
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path></svg>
                <el-input v-model="form.password" type="password" placeholder="请输入密码" size="large" show-password />
              </div>
            </div>

            <div class="form-options">
              <el-checkbox v-model="form.remember">记住我</el-checkbox>
              <a href="#" class="forgot-link">忘记密码？</a>
            </div>

            <el-form-item>
              <button type="submit" class="btn-primary" :loading="loading">
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 16l-4-4m0 0l4-4m-4 4h14m-5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h7a3 3 0 013 3v1"></path></svg>
                登录
              </button>
            </el-form-item>
          </el-form>

          <div class="divider">或</div>

          <div class="social-login">
            <button class="social-btn" style="width: 100%; justify-content: center" @click="loginWithPnkx">
              <svg viewBox="0 0 24 24" fill="currentColor" width="18" height="18"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"></path></svg>
              使用 pnkx 账号登录
            </button>
          </div>
          <el-alert v-if="ssoError" type="error" :title="ssoError" show-icon :closable="false" style="margin-top: 12px" />

          <div class="register-prompt">
            还没有账号？<router-link to="/register">立即注册</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = ref({ username: '', password: '', remember: false })
const route = useRoute()
const ssoError = ref('')
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await userStore.login(form.value.username, form.value.password)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (_e) {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (route.query.sso_error) {
    ssoError.value = String(route.query.sso_error)
  }
})

// 跳转后端发起 pnkx OIDC 授权码流程，完成后回跳 /sso/callback#access_token=...
function loginWithPnkx() {
  window.location.href = '/api/auth/sso/login'
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #EEF2FF 0%, #F9FAFB 50%, #F0F4FF 100%);
  font-family: 'Inter', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  padding: 20px;
}
.login-wrapper {
  display: flex;
  width: 960px;
  min-height: 560px;
  background: #FFFFFF;
  border-radius: 20px;
  box-shadow: 0 10px 15px -3px rgba(0,0,0,0.1), 0 4px 6px -2px rgba(0,0,0,0.05);
  overflow: hidden;
}
.login-brand {
  width: 400px;
  flex-shrink: 0;
  background: linear-gradient(135deg, #4F46E5 0%, #7C3AED 50%, #6366F1 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 40px;
  position: relative;
  overflow: hidden;
}
.login-brand::before {
  content: '';
  position: absolute;
  width: 280px;
  height: 280px;
  background: rgba(255,255,255,0.08);
  border-radius: 50%;
  top: -60px;
  right: -50px;
}
.login-brand::after {
  content: '';
  position: absolute;
  width: 180px;
  height: 180px;
  background: rgba(255,255,255,0.05);
  border-radius: 50%;
  bottom: -30px;
  left: -30px;
}
.brand-content {
  position: relative;
  z-index: 1;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.brand-logo {
  width: 80px;
  height: 80px;
  background: rgba(255,255,255,0.2);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  backdrop-filter: blur(10px);
}
.brand-logo svg { width: 44px; height: 44px; color: white; }
.brand-title {
  font-size: 36px;
  font-weight: 700;
  color: white;
  margin-bottom: 8px;
  letter-spacing: 8px;
}
.brand-subtitle {
  font-size: 14px;
  color: rgba(255,255,255,0.8);
  line-height: 1.6;
  margin-bottom: 36px;
}
.brand-features {
  display: flex;
  flex-direction: column;
  gap: 16px;
  text-align: left;
}
.brand-feature {
  display: flex;
  align-items: center;
  gap: 12px;
  color: rgba(255,255,255,0.95);
  font-size: 13px;
}
.brand-feature-icon {
  width: 32px;
  height: 32px;
  background: rgba(255,255,255,0.15);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.brand-feature-icon svg { width: 16px; height: 16px; }
.login-form-area {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 48px;
}
.form-container {
  width: 100%;
  max-width: 340px;
}
.login-form-area h2 {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 6px;
  color: #111827;
}
.login-form-area > .form-container > p {
  font-size: 14px;
  color: #6B7280;
  margin-bottom: 28px;
}
.form-group { margin-bottom: 18px; }
.form-group label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 6px;
}
.input-wrapper {
  position: relative;
}
.input-wrapper svg {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  width: 18px;
  height: 18px;
  color: #9CA3AF;
  z-index: 1;
  pointer-events: none;
}
.input-wrapper :deep(.el-input__wrapper) {
  padding-left: 40px !important;
}
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  font-size: 13px;
}
.forgot-link { color: #4F46E5; text-decoration: none; font-weight: 500; }
.forgot-link:hover { text-decoration: underline; }
.btn-primary {
  width: 100%;
  height: 44px;
  background: #4F46E5;
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
.btn-primary:hover { background: #4338CA; }
.btn-primary svg { width: 18px; height: 18px; }
.divider {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 20px 0;
  color: #9CA3AF;
  font-size: 12px;
}
.divider::before, .divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: #E5E7EB;
}
.social-login { display: flex; gap: 12px; }
.social-btn {
  flex: 1;
  height: 42px;
  border: 1px solid #E5E7EB;
  border-radius: 10px;
  background: #FFFFFF;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  color: #6B7280;
  transition: all 0.15s ease;
}
.social-btn:hover { background: #F9FAFB; border-color: #9CA3AF; }
.social-btn svg { width: 18px; height: 18px; }
.register-prompt {
  text-align: center;
  margin-top: 20px;
  font-size: 13px;
  color: #6B7280;
}
.register-prompt a {
  color: #4F46E5;
  font-weight: 500;
  text-decoration: none;
}
.register-prompt a:hover { text-decoration: underline; }
</style>
