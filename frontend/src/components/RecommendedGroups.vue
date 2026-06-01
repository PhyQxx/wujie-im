<template>
  <div class="recommended-groups">
    <div class="recommended-header">
      <span class="recommended-title">推荐群聊</span>
      <button class="create-btn" @click="$emit('create-group')">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="10" />
          <line x1="12" y1="8" x2="12" y2="16" />
          <line x1="8" y1="12" x2="16" y2="12" />
        </svg>
        发起群聊
      </button>
    </div>
    <div class="recommended-list" v-if="groupStore.recommendedGroups.length">
      <div
        v-for="group in groupStore.recommendedGroups"
        :key="group.id"
        class="group-item"
      >
        <div class="group-avatar" :style="{ background: getAvatarBg(group.name) }">
          {{ group.name?.[0] || '?' }}
        </div>
        <div class="group-info">
          <div class="group-name">{{ group.name }}</div>
          <div class="group-meta">{{ group.memberCount ?? 0 }} 位成员</div>
        </div>
        <button class="join-btn" @click="handleJoin(group)" :disabled="joiningId === group.id">
          {{ joiningId === group.id ? '处理中...' : (group.needAudit ? '申请' : '加入') }}
        </button>
      </div>
    </div>
    <div v-else class="empty-tip">
      暂无推荐群聊
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useGroupStore } from '@/stores/group'
import { useConversationStore } from '@/stores/conversation'
import { ElMessage } from 'element-plus'
import type { GroupInfo } from '@/types'

defineEmits<{ (e: 'create-group'): void }>()

const groupStore = useGroupStore()
const conversationStore = useConversationStore()
const joiningId = ref<number | null>(null)

function getAvatarBg(name?: string) {
  const colors = ['#4F46E5', '#7C3AED', '#2563EB', '#059669', '#D97706', '#DC2626', '#DB2777']
  const idx = (name?.charCodeAt(0) || 0) % colors.length
  return colors[idx]
}

async function handleJoin(group: GroupInfo) {
  joiningId.value = group.id
  try {
    if (group.needAudit) {
      await groupStore.joinGroup(group.id)
      ElMessage.success('已提交申请，等待审核')
    } else {
      await groupStore.joinGroup(group.id)
      await conversationStore.createConversation('GROUP', group.id)
      await groupStore.fetchGroups()
      ElMessage.success(`已加入「${group.name}」`)
    }
  } catch {
    ElMessage.error('加入失败')
  } finally {
    joiningId.value = null
  }
}
</script>

<style scoped>
.recommended-groups {
  border-top: 1px solid var(--border, #E5E7EB);
  flex-shrink: 0;
  max-height: 240px;
  display: flex;
  flex-direction: column;
}
.recommended-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
}
.recommended-title {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary, #6B7280);
}
.create-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  border: none;
  background: transparent;
  font-size: 12px;
  color: var(--primary, #4F46E5);
  cursor: pointer;
  font-family: inherit;
  padding: 2px 6px;
  border-radius: 4px;
  transition: background 0.15s;
}
.create-btn:hover { background: #EEF2FF; }
.create-btn svg { width: 14px; height: 14px; }
.recommended-list {
  flex: 1;
  overflow-y: auto;
}
.group-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  transition: background 0.15s;
}
.group-item:hover { background: var(--surface-2, #F9FAFB); }
.group-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  color: white;
  flex-shrink: 0;
}
.group-info { flex: 1; min-width: 0; }
.group-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary, #111827);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.group-meta {
  font-size: 11px;
  color: var(--text-secondary, #6B7280);
}
.join-btn {
  border: 1px solid var(--primary, #4F46E5);
  background: transparent;
  color: var(--primary, #4F46E5);
  font-size: 11px;
  padding: 3px 10px;
  border-radius: 4px;
  cursor: pointer;
  font-family: inherit;
  white-space: nowrap;
  transition: all 0.15s;
}
.join-btn:hover:not(:disabled) {
  background: var(--primary, #4F46E5);
  color: white;
}
.join-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.empty-tip {
  text-align: center;
  font-size: 12px;
  color: var(--text-secondary, #6B7280);
  padding: 12px;
}
</style>
