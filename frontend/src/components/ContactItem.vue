<template>
  <div class="contact-item">
    <el-avatar :size="40" :src="user.avatar">{{ user.username?.[0] }}</el-avatar>
    <div class="contact-info">
      <span class="name">{{ user.username }}</span>
      <span class="status" :class="user.userStatus?.toLowerCase()">{{ statusText }}</span>
    </div>
    <div class="contact-actions">
      <el-button size="small" @click="$emit('chat', user)">发消息</el-button>
      <el-button size="small" type="danger" @click="$emit('delete', user.id)">删除</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { User } from '@/types'

const props = defineProps<{ user: User }>()
defineEmits<{
  (e: 'chat', user: User): void
  (e: 'delete', id: number): void
}>()

const statusText = computed(() => {
  const map: Record<string, string> = { ONLINE: '在线', OFFLINE: '离线', AWAY: '离开', DND: '勿扰' }
  return map[props.user.userStatus] || '离线'
})
</script>

<style scoped>
.contact-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 8px;
  transition: background 0.15s;
}
.contact-item:hover { background: var(--surface-2); }
.contact-info { flex: 1; display: flex; flex-direction: column; }
.name { font-weight: 500; }
.status { font-size: 12px; color: #9CA3AF; }
.status.online { color: #10B981; }
.status.away { color: #F59E0B; }
.status.dnd { color: #EF4444; }
.contact-actions { display: flex; gap: 6px; }
</style>
