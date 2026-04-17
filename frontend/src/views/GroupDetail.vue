<template>
  <div class="group-detail">
    <!-- 左侧成员列表 -->
    <div class="member-list-panel">
      <div class="panel-header">
        <h2>{{ group?.name || '群组' }}</h2>
      </div>

      <div class="tabs">
        <button class="tab" :class="{ active: tab === 'members' }" @click="tab = 'members'">成员</button>
        <button class="tab" :class="{ active: tab === 'chat' }" @click="tab = 'chat'">群聊记录</button>
      </div>

      <div class="search-bar">
        <input type="text" v-model="memberSearch" placeholder="搜索成员..." />
      </div>

      <div v-if="tab === 'members'" class="member-list">
        <div
          v-for="m in filteredMembers"
          :key="m.userId"
          class="member-item"
        >
          <div class="member-avatar" :style="{ background: getAvatarBg(m) }">
            {{ m.user?.username?.[0] || '?' }}
          </div>
          <div class="member-info">
            <div class="member-name">
              {{ m.user?.username }}
              <el-tag v-if="m.role === 'OWNER'" type="danger" size="small">群主</el-tag>
              <el-tag v-else-if="m.role === 'ADMIN'" type="warning" size="small">管理员</el-tag>
            </div>
            <div class="member-status" :class="{ online: m.user?.userStatus === 'ONLINE' }">
              {{ m.user?.userStatus === 'ONLINE' ? '在线' : '离线' }}
            </div>
          </div>
        </div>
        <div v-if="!filteredMembers.length" class="empty">暂无成员</div>
      </div>

      <div v-else class="chat-record-list">
        <div v-if="!chatRecords.length" class="empty">暂无聊天记录</div>
        <div v-for="msg in chatRecords" :key="msg.id" class="chat-record-item">
          <div class="record-avatar">{{ msg.sender?.username?.[0] }}</div>
          <div class="record-info">
            <div class="record-name">{{ msg.sender?.username }}</div>
            <div class="record-content">{{ msg.content }}</div>
          </div>
          <div class="record-time">{{ formatTime(msg.createTime) }}</div>
        </div>
      </div>
    </div>

    <!-- 右侧群详情 -->
    <div class="group-info-panel">
      <div class="info-header">
        <h3>{{ group?.name }}</h3>
        <div class="member-count">{{ members.length }} 位成员</div>
      </div>

      <div class="info-section">
        <div class="info-item">
          <span class="info-label">群公告</span>
          <span class="info-value announcement">{{ group?.announcement || '暂无公告' }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">群号</span>
          <span class="info-value">{{ groupId }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">群类型</span>
          <span class="info-value">{{ group?.type === 'PRIVATE' ? '私密群' : '公开群' }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">创建时间</span>
          <span class="info-value">{{ group?.createTime ? formatDate(group.createTime) : '-' }}</span>
        </div>
      </div>

      <div v-if="canManage" class="info-actions">
        <button class="btn-primary" @click="router.push(`/group/${groupId}/settings`)">编辑群信息</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useGroupStore } from '@/stores/group'
import { useUserStore } from '@/stores/user'
import type { GroupMember } from '@/types'

const route = useRoute()
const router = useRouter()
const groupStore = useGroupStore()
const userStore = useUserStore()

const tab = ref('members')
const memberSearch = ref('')
const chatRecords = ref<any[]>([])

const groupId = computed(() => Number(route.params.id))
const members = computed(() => groupStore.members)
const group = computed(() => groupStore.groups.find(g => g.id === groupId.value))

const currentUserId = computed(() => userStore.currentUser?.id)
const currentMember = computed(() => members.value.find(m => m.userId === currentUserId.value))
const canManage = computed(() => currentMember.value && (currentMember.value.role === 'OWNER' || currentMember.value.role === 'ADMIN'))

const filteredMembers = computed(() => {
  if (!memberSearch.value) return members.value
  return members.value.filter(m =>
    m.user?.username?.includes(memberSearch.value)
  )
})

onMounted(async () => {
  await groupStore.fetchGroups()
  await groupStore.fetchMembers(groupId.value)
})

function getAvatarBg(m: GroupMember) {
  const colors = ['#DBEAFE', '#D1FAE5', '#FCE7F3', '#FEF3C7', '#FEE2E2', '#F3E8FF']
  const name = m.user?.username || ''
  return colors[name.charCodeAt(0) % colors.length]
}

function formatTime(time?: string) {
  if (!time) return ''
  const d = new Date(time)
  const now = new Date()
  const diff = now.getTime() - d.getTime()
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  return d.toLocaleDateString()
}

function formatDate(date?: string) {
  if (!date) return '-'
  return new Date(date).toLocaleDateString()
}
</script>

<style scoped>
.group-detail {
  display: flex;
  flex: 1;
  overflow: hidden;
}
.member-list-panel {
  width: 360px;
  border-right: 1px solid var(--border, #E5E7EB);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}
.panel-header {
  padding: 16px;
  border-bottom: 1px solid var(--border, #E5E7EB);
}
.panel-header h2 { font-size: 16px; font-weight: 600; margin: 0; }
.tabs {
  display: flex;
  padding: 8px 12px;
  gap: 6px;
  border-bottom: 1px solid var(--border, #E5E7EB);
}
.tab {
  flex: 1;
  height: 30px;
  border: none;
  background: #F3F4F6;
  border-radius: 6px;
  font-size: 12px;
  color: #6B7280;
  cursor: pointer;
}
.tab.active { background: #4F46E5; color: white; }
.search-bar {
  padding: 8px 12px;
  border-bottom: 1px solid var(--border, #E5E7EB);
}
.search-bar input {
  width: 100%;
  height: 32px;
  padding: 0 10px;
  border: 1px solid var(--border, #E5E7EB);
  border-radius: 6px;
  font-size: 12px;
  outline: none;
  background: var(--surface-2, #F9FAFB);
}
.member-list,
.chat-record-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}
.member-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 8px;
  border-radius: 8px;
  cursor: pointer;
}
.member-item:hover { background: #F3F4F6; }
.member-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  color: white;
  flex-shrink: 0;
}
.member-info { flex: 1; }
.member-name {
  font-size: 13px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;
}
.member-status { font-size: 11px; color: #9CA3AF; }
.member-status.online { color: #10B981; }
.chat-record-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px 8px;
  border-bottom: 1px solid #F3F4F6;
}
.record-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #DBEAFE;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #2563EB;
  flex-shrink: 0;
}
.record-info { flex: 1; min-width: 0; }
.record-name { font-size: 12px; font-weight: 500; margin-bottom: 2px; }
.record-content { font-size: 12px; color: #6B7280; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.record-time { font-size: 10px; color: #9CA3AF; flex-shrink: 0; }
.empty { text-align: center; color: #9CA3AF; padding: 32px; font-size: 13px; }

/* 右侧群详情 */
.group-info-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 24px;
  overflow-y: auto;
}
.info-header {
  text-align: center;
  margin-bottom: 24px;
}
.info-header h3 { font-size: 18px; font-weight: 600; margin-bottom: 4px; }
.member-count { font-size: 13px; color: #6B7280; }
.info-section {
  background: var(--surface-2, #F9FAFB);
  border-radius: 12px;
  padding: 16px;
}
.info-item {
  display: flex;
  padding: 12px 0;
  border-bottom: 1px solid #E5E7EB;
}
.info-item:last-child { border-bottom: none; }
.info-label { width: 80px; font-size: 13px; color: #6B7280; flex-shrink: 0; }
.info-value { flex: 1; font-size: 13px; }
.info-value.announcement { color: #111827; white-space: pre-wrap; }
.info-actions { margin-top: 24px; }
.btn-primary {
  width: 100%;
  height: 40px;
  background: #4F46E5;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
}
</style>
