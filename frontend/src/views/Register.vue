<template>
  <div class="register-page">
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
          <div class="brand-subtitle">加入無界，开始与团队和社区的无缝沟通</div>
        </div>
      </div>

      <!-- 注册表单 -->
      <div class="login-form-area">
        <div class="form-container">
          <h2>创建账号 🚀</h2>
          <p>填写以下信息完成注册</p>

          <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent="handleRegister">
            <div class="form-row">
              <div class="form-group">
                <label>用户名</label>
                <div class="input-wrapper">
                  <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path></svg>
                  <el-input v-model="form.username" placeholder="设置用户名" size="large" />
                </div>
              </div>
              <div class="form-group">
                <label>昵称</label>
                <div class="input-wrapper">
                  <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M11.049 2.927c.3-.921 1.603-.921 1.902 0l1.519 4.674a1 1 0 00.95.69h4.915c.969 0 1.371 1.24.588 1.81l-3.976 2.888a1 1 0 00-.363 1.118l1.518 4.674c.3.922-.755 1.688-1.538 1.118l-3.976-2.888a1 1 0 00-1.176 0l-3.976 2.888c-.783.57-1.838-.197-1.538-1.118l1.518-4.674a1 1 0 00-.363-1.118l-3.976-2.888c-.784-.57-.38-1.81.588-1.81h4.914a1 1 0 00.951-.69l1.519-4.674z"></path></svg>
                  <el-input v-model="form.nickname" placeholder="显示昵称" size="large" />
                </div>
              </div>
            </div>

            <div class="form-group">
              <label>邮箱</label>
              <div class="input-wrapper">
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"></path></svg>
                <el-input v-model="form.email" placeholder="请输入邮箱" size="large" />
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>密码</label>
                <div class="input-wrapper">
                  <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path></svg>
                  <el-input v-model="form.password" type="password" placeholder="设置密码" size="large" show-password />
                </div>
              </div>
              <div class="form-group">
                <label>确认密码</label>
                <div class="input-wrapper">
                  <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"></path></svg>
                  <el-input v-model="form.confirmPassword" type="password" placeholder="再次输入" size="large" show-password />
                </div>
              </div>
            </div>

            <div class="form-group">
              <label>用户类型</label>
              <el-select v-model="form.userType" placeholder="选择用户类型" size="large" style="width: 100%">
                <template #prefix>
                  <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" style="width:18px;height:18px;color:#9CA3AF;"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z"></path></svg>
                </template>
                <el-option label="个人用户" value="PERSONAL" />
                <el-option label="团队/企业" value="TEAM" />
                <el-option label="社区论坛" value="COMMUNITY" />
              </el-select>
            </div>

            <div class="terms-check">
              <el-checkbox v-model="form.agreeTerms">我已阅读并同意</el-checkbox>
              <a href="#">《用户协议》</a> 和 <a href="#">《隐私政策》</a>
            </div>

            <el-form-item>
              <button type="submit" class="btn-primary" :loading="loading">
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z"></path></svg>
                立即注册
              </button>
            </el-form-item>
          </el-form>

          <div class="login-prompt">
            已有账号？<router-link to="/login">立即登录</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = ref({
  username: '',
  nickname: '',
  email: '',
  password: '',
  confirmPassword: '',
  userType: 'PERSONAL',
  agreeTerms: false
})

const validateConfirmPwd = (_rule: any, value: string, callback: Function) => {
  if (value !== form.value.password) {
    callback(new Error('两次密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度 3-20', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入有效邮箱', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPwd, trigger: 'blur' }
  ]
}

async function handleRegister() {
  if (!form.value.agreeTerms) {
    ElMessage.warning('请阅读并同意用户协议和隐私政策')
    return
  }
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await userStore.register(form.value.username, form.value.password, form.value.email, form.value.nickname, form.value.userType)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (_e) {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
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
  min-height: 580px;
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
}
.login-form-area {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px 48px;
  overflow-y: auto;
}
.form-container {
  width: 100%;
  max-width: 380px;
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
  margin-bottom: 20px;
}
.form-row { display: flex; gap: 12px; }
.form-row .form-group { flex: 1; }
.form-group { margin-bottom: 14px; }
.form-group label { display: block; font-size: 13px; font-weight: 500; color: #374151; margin-bottom: 6px; }
.input-wrapper { position: relative; }
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
.terms-check {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 16px;
  font-size: 13px;
  color: #6B7280;
  flex-wrap: wrap;
}
.terms-check a { color: #4F46E5; text-decoration: none; }
.terms-check a:hover { text-decoration: underline; }
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
.login-prompt {
  text-align: center;
  margin-top: 16px;
  font-size: 13px;
  color: #6B7280;
}
.login-prompt a { color: #4F46E5; font-weight: 500; text-decoration: none; }
.login-prompt a:hover { text-decoration: underline; }
</style>
