<template>
  <div class="notification-page">
    <div class="page-header">
      <h2>通知</h2>
      <el-button v-if="notificationStore.unreadCount > 0" size="small" @click="markAllRead">全部已读</el-button>
    </div>

    <div class="notification-list">
      <div
        v-for="n in notificationStore.notifications"
        :key="n.id"
        class="notification-item"
        :class="{ unread: !n.isRead }"
        @click="handleClick(n)"
      >
        <div class="notif-icon">
          <span v-if="n.type === 'FRIEND_REQUEST'">👥</span>
          <span v-else-if="n.type === 'GROUP_JOIN'">🏠</span>
          <span v-else-if="n.type === 'MESSAGE'">💬</span>
          <span v-else>🔔</span>
        </div>
        <div class="notif-content">
          <div class="notif-title">{{ n.title }}</div>
          <div class="notif-desc">{{ n.content }}</div>
          <div class="notif-time">{{ formatTime(n.createTime) }}</div>
        </div>
        <div v-if="!n.isRead" class="unread-dot"></div>
      </div>

      <div v-if="!notificationStore.notifications.length" class="empty">
        暂无通知
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useNotificationStore } from '@/stores/notification'
import type { Notification } from '@/types'

const router = useRouter()
const notificationStore = useNotificationStore()

onMounted(() => {
  notificationStore.fetchNotifications()
})

function formatTime(time: string) {
  if (!time) return ''
  const d = new Date(time)
  const now = new Date()
  const diff = now.getTime() - d.getTime()
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  return d.toLocaleDateString()
}

function handleClick(n: Notification) {
  if (!n.isRead) {
    notificationStore.markRead(n.id)
  }
  if (n.type === 'FRIEND_REQUEST' || n.type === 'GROUP_JOIN') {
    router.push('/contacts')
  }
}

function markAllRead() {
  notificationStore.markAllRead()
}
</script>

<style scoped>
.notification-page {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 24px;
  overflow: hidden;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.page-header h2 { font-size: 20px; font-weight: 700; margin: 0; }
.notification-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.notification-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  background: var(--surface-2, #F9FAFB);
  border-radius: 12px;
  cursor: pointer;
  transition: background 0.15s;
  position: relative;
}
.notification-item:hover { background: var(--surface-3, #F3F4F6); }
.notification-item.unread { background: #EEF2FF; }
.notif-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}
.notif-content { flex: 1; }
.notif-title { font-weight: 600; font-size: 14px; margin-bottom: 4px; }
.notif-desc { font-size: 13px; color: var(--text-secondary, #6B7280); margin-bottom: 4px; }
.notif-time { font-size: 12px; color: #9CA3AF; }
.unread-dot {
  width: 8px;
  height: 8px;
  background: var(--primary, #4F46E5);
  border-radius: 50%;
  flex-shrink: 0;
}
.empty {
  text-align: center;
  color: var(--text-secondary, #6B7280);
  padding: 48px;
}
</style>
