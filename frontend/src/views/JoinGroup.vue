<template>
  <div class="join-group-view">
    <!-- 动态背景 -->
    <div v-if="group?.avatar" class="bg-blur" :style="{ backgroundImage: `url(${group.avatar})` }"></div>
    <div class="join-card" v-if="!joined">
      <div class="loading" v-if="loading">正在获取群组信息...</div>
      <template v-else-if="group">
        <transition name="fade-up" appear>
          <div class="group-invite-content">
            <div class="group-avatar-wrap">
              <el-image v-if="group.avatar" :src="group.avatar" fit="cover" class="group-avatar-img" />
              <div v-else class="group-avatar-initial" :style="{ background: getAvatarBg(group.name) }">
                {{ group.name?.[0] }}
              </div>
            </div>
            <h3 class="invite-title">邀请你加入群组</h3>
            <h2 class="group-name">{{ group.name }}</h2>
            <div class="group-meta-tags">
              <el-tag size="small" round effect="plain">{{ group.memberCount || 0 }} 位成员</el-tag>
              <el-tag v-if="group.type === 'PUBLIC'" size="small" type="success" round effect="plain">公开群组</el-tag>
            </div>
            <div class="announcement-box">
              <div class="ann-label">群公告</div>
              <p class="ann-text">{{ group.announcement || '这个群组很神秘，还没有写下公告...' }}</p>
            </div>
            <el-button type="primary" class="join-btn" @click="handleJoin" :loading="joining">
              <span v-if="!joining">立即加入</span>
              <span v-else>正在加入...</span>
            </el-button>
          </div>
        </transition>
      </template>
      <div v-else class="error">
        <div class="error-icon">⚠️</div>
        <p>{{ errorMsg || '邀请链接已失效' }}</p>
        <el-button @click="$router.push('/')">回到首页</el-button>
      </div>
    </div>
    <div class="join-card success" v-else>
      <div class="success-icon">✅</div>
      <h2>成功加入群组！</h2>
      <p>正在为你进入聊天...</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useGroupStore } from '@/stores/group'
import { useConversationStore } from '@/stores/conversation'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const groupStore = useGroupStore()
const conversationStore = useConversationStore()

const token = route.params.token as string
const loading = ref(true)
const joining = ref(false)
const joined = ref(false)
const group = ref<any>(null)
const errorMsg = ref('')

onMounted(async () => {
  try {
    // 后端需要一个通过 token 获取群信息的接口，或者这里直接尝试加入
    // 这里先简单处理，直接尝试加入，或者后端先返回群信息
    // 假设后端有 /api/group/invite-info/{token}
    const res = await request.get(`/group/invite-info/${token}`)
    group.value = res.data
  } catch (e: any) {
    errorMsg.value = e.response?.data?.msg || '邀请链接无效'
  } finally {
    loading.value = false
  }
})

async function handleJoin() {
  joining.value = ref(true)
  try {
    const userId = localStorage.getItem('userId')
    await request.post('/group/join-by-token', { token, userId })
    joined.value = true
    ElMessage.success('成功加入群组')
    
    // 跳转到会话
    setTimeout(async () => {
      const conv = await conversationStore.createConversation('GROUP', group.value.id)
      conversationStore.setCurrentConversation(conv)
      router.push('/conversation')
    }, 1500)
  } catch (e: any) {
    ElMessage.error(e.response?.data?.msg || '加入失败')
  } finally {
    joining.value = false
  }
}

function getAvatarBg(name: string) {
  if (!name) return '#ccc'
  const colors = ['#4F46E5', '#10B981', '#F59E0B', '#EF4444', '#8B5CF6']
  return colors[name.charCodeAt(0) % colors.length]
}
</script>

<style scoped>
.join-group-view {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f3f4f6;
  position: relative;
  overflow: hidden;
}
.bg-blur {
  position: absolute;
  top: -10%; left: -10%; right: -10%; bottom: -10%;
  background-size: cover;
  background-position: center;
  filter: blur(40px) brightness(0.9);
  opacity: 0.4;
  z-index: 0;
}
.join-card {
  width: 440px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 48px;
  border-radius: 24px;
  box-shadow: 0 20px 50px rgba(0,0,0,0.1);
  text-align: center;
  position: relative;
  z-index: 1;
}
.group-avatar-wrap {
  width: 100px;
  height: 100px;
  margin: 0 auto 24px;
  border-radius: 28px;
  overflow: hidden;
  box-shadow: 0 8px 16px rgba(0,0,0,0.1);
}
.group-avatar-img { width: 100%; height: 100%; }
.group-avatar-initial {
  width: 100%; height: 100%;
  display: flex; align-items: center; justify-content: center;
  font-size: 40px; font-weight: 700; color: white;
}
.invite-title { color: #6b7280; font-size: 14px; margin-bottom: 12px; font-weight: 500; text-transform: uppercase; letter-spacing: 1px; }
.group-name { font-size: 28px; font-weight: 800; margin-bottom: 12px; color: #111827; }
.group-meta-tags { display: flex; justify-content: center; gap: 8px; margin-bottom: 28px; }
.announcement-box {
  background: #f8fafc;
  padding: 20px;
  border-radius: 16px;
  margin-bottom: 36px;
  text-align: left;
  border: 1px solid #f1f5f9;
}
.ann-label { font-size: 12px; font-weight: 600; color: #94a3b8; margin-bottom: 8px; }
.ann-text { font-size: 14px; color: #475569; line-height: 1.6; margin: 0; }
.join-btn { width: 100%; height: 54px; font-size: 16px; font-weight: 600; border-radius: 14px; box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3); }

/* Animations */
.fade-up-enter-active { transition: all 0.5s ease-out; }
.fade-up-enter-from { opacity: 0; transform: translateY(20px); }

.error-icon { font-size: 48px; margin-bottom: 16px; }
.success-icon { font-size: 64px; margin-bottom: 24px; animation: bounce 1s infinite; }
@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}
</style>
