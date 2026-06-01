<template>
  <div class="announcement-page">
    <div class="page-header">
      <h2>通知公告</h2>
    </div>

    <div class="announcement-list">
      <div
        v-for="a in announcementStore.announcements"
        :key="a.id"
        class="announcement-item"
        :class="{ unread: !a.isRead }"
        @click="handleClick(a)"
      >
        <div class="ann-icon">
          <span v-if="a.type === 'IMPORTANT'">📢</span>
          <span v-else>📋</span>
        </div>
        <div class="ann-content">
          <div class="ann-title">{{ a.title }}</div>
          <div class="ann-text">{{ a.content }}</div>
          <div class="ann-time">{{ formatTime(a.publishTime || a.createTime) }}</div>
        </div>
        <div v-if="!a.isRead" class="unread-dot"></div>
      </div>

      <div v-if="!announcementStore.announcements.length" class="empty">
        暂无公告
      </div>
    </div>

    <el-dialog v-model="showDetail" :title="currentAnnouncement?.title" width="520px">
      <div class="detail-content" v-html="currentAnnouncement?.content"></div>
      <template #footer>
        <el-button type="primary" @click="handleMarkRead">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAnnouncementStore } from '@/stores/announcement'
import type { Announcement } from '@/types'

const announcementStore = useAnnouncementStore()
const showDetail = ref(false)
const currentAnnouncement = ref<Announcement | null>(null)

onMounted(() => {
  announcementStore.fetchPublished()
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

function handleClick(a: Announcement) {
  currentAnnouncement.value = a
  showDetail.value = true
}

async function handleMarkRead() {
  if (currentAnnouncement.value && !currentAnnouncement.value.isRead) {
    await announcementStore.markRead(currentAnnouncement.value.id)
  }
  showDetail.value = false
}
</script>

<style scoped>
.announcement-page {
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
.announcement-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.announcement-item {
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
.announcement-item:hover { background: var(--surface-3, #F3F4F6); }
.announcement-item.unread { background: #EEF2FF; }
.ann-icon {
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
.ann-content { flex: 1; }
.ann-title { font-weight: 600; font-size: 14px; margin-bottom: 4px; }
.ann-text {
  font-size: 13px;
  color: var(--text-secondary, #6B7280);
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.ann-time { font-size: 12px; color: #9CA3AF; }
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
.detail-content {
  font-size: 14px;
  line-height: 1.8;
  white-space: pre-wrap;
}
</style>
