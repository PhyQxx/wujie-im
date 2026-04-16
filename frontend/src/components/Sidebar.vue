<template>
  <div class="sidebar">
    <div class="logo">無界</div>

    <nav class="nav">
      <div
        v-for="item in navItems"
        :key="item.key"
        class="nav-item"
        :class="{ active: active === item.key }"
        @click="$emit('change', item.key)"
      >
        <span class="nav-icon">{{ item.icon }}</span>
        <span class="nav-label">{{ item.label }}</span>
      </div>
    </nav>

    <div class="sidebar-footer">
      <el-avatar
        :size="36"
        :src="userStore.currentUser?.avatar"
        class="user-avatar"
        @click="$emit('change', 'settings')"
      >
        {{ userStore.currentUser?.username?.[0] }}
      </el-avatar>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useUserStore } from '@/stores/user'

defineProps<{ active: string }>()
defineEmits<{ (e: 'change', tab: string): void }>()

const userStore = useUserStore()

const navItems = [
  { key: 'message', icon: '💬', label: '消息' },
  { key: 'contacts', icon: '👥', label: '联系人' },
  { key: 'robots', icon: '🤖', label: '机器人' },
  { key: 'notification', icon: '🔔', label: '通知' },
  { key: 'settings', icon: '⚙️', label: '设置' }
]
</script>

<style scoped>
.sidebar {
  width: 64px;
  height: 100vh;
  background: #1e1f22;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 0;
  flex-shrink: 0;
}
.logo {
  font-size: 14px;
  font-weight: 700;
  color: #fff;
  margin-bottom: 20px;
  text-align: center;
  line-height: 1.2;
}
.nav { flex: 1; display: flex; flex-direction: column; gap: 4px; width: 100%; padding: 0 8px; }
.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px 4px;
  border-radius: 8px;
  cursor: pointer;
  color: #9CA3AF;
  transition: all 0.2s;
}
.nav-item:hover { background: #2a2d30; color: #fff; }
.nav-item.active { background: #4F46E5; color: #fff; }
.nav-icon { font-size: 18px; }
.nav-label { font-size: 10px; margin-top: 2px; }
.sidebar-footer { margin-top: auto; }
.user-avatar { cursor: pointer; border: 2px solid transparent; transition: border-color 0.2s; }
.user-avatar:hover { border-color: #4F46E5; }
</style>
