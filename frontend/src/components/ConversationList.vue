<template>
  <div class="conversation-list">
    <div
      v-for="conv in filteredConversations"
      :key="conv.id"
      class="conv-item"
      :class="{ active: currentId === conv.id }"
      @click="$emit('select', conv)"
    >
      <div class="conv-avatar">
        <el-avatar :size="40" :src="getAvatar(conv)">{{ getInitial(conv) }}</el-avatar>
        <span v-if="conv.unreadCount > 0" class="badge">{{ conv.unreadCount > 99 ? '99+' : conv.unreadCount }}</span>
      </div>
      <div class="conv-info">
        <div class="conv-top">
          <span class="conv-name">{{ getName(conv) }}</span>
          <span class="conv-time">{{ formatTime(conv.lastMessageTime) }}</span>
        </div>
        <div class="conv-preview">{{ conv.lastMessageContent || '暂无消息' }}</div>
      </div>
    </div>
    <div v-if="!filteredConversations.length" class="empty">暂无会话</div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useConversationStore } from '@/stores/conversation'
import dayjs from 'dayjs'
import type { Conversation } from '@/types'

const props = defineProps<{ keyword?: string }>()
defineEmits<{ (e: 'select', conv: Conversation): void }>()

const conversationStore = useConversationStore()
const currentId = computed(() => conversationStore.currentConversation?.id)

const filteredConversations = computed(() => {
  const kw = props.keyword?.toLowerCase() || ''
  return conversationStore.conversations.filter(c => {
    if (!kw) return true
    const name = getName(c).toLowerCase()
    return name.includes(kw)
  })
})

function getName(conv: Conversation) {
  if (conv.type === 'GROUP') return conv.groupInfo?.name || '群组'
  return conv.targetUser?.username || '用户'
}

function getAvatar(conv: Conversation) {
  if (conv.type === 'GROUP') return conv.groupInfo?.avatar
  return conv.targetUser?.avatar
}

function getInitial(conv: Conversation) {
  return getName(conv)?.[0] || '?'
}

function formatTime(time?: string) {
  if (!time) return ''
  const d = dayjs(time)
  const now = dayjs()
  if (d.isSame(now, 'day')) return d.format('HH:mm')
  if (d.isSame(now.subtract(1, 'day'), 'day')) return '昨天'
  return d.format('MM/DD')
}
</script>

<style scoped>
.conversation-list { flex: 1; overflow-y: auto; }
.conv-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  cursor: pointer;
  transition: background 0.15s;
}
.conv-item:hover { background: var(--surface-2); }
.conv-item.active { background: #EEF2FF; }
.conv-avatar { position: relative; flex-shrink: 0; }
.badge {
  position: absolute;
  top: -4px;
  right: -4px;
  background: #EF4444;
  color: #fff;
  font-size: 10px;
  border-radius: 10px;
  padding: 1px 4px;
  min-width: 16px;
  text-align: center;
}
.conv-info { flex: 1; min-width: 0; }
.conv-top { display: flex; justify-content: space-between; align-items: center; }
.conv-name { font-weight: 500; font-size: 14px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.conv-time { font-size: 11px; color: var(--text-secondary); flex-shrink: 0; }
.conv-preview { font-size: 12px; color: var(--text-secondary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-top: 2px; }
.empty { text-align: center; color: #9CA3AF; padding: 24px; font-size: 13px; }
</style>
