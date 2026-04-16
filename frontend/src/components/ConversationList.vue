<template>
  <div class="chat-list">
    <div
      v-for="conv in filteredConversations"
      :key="conv.id"
      class="chat-item"
      :class="{ active: currentId === conv.id, unread: conv.unreadCount > 0 }"
      @click="$emit('select', conv)"
    >
      <div class="chat-avatar-wrap">
        <div class="chat-avatar" :style="{ background: getAvatarBg(conv) }">
          {{ getInitial(conv) }}
        </div>
        <span v-if="conv.type !== 'GROUP'" class="online-dot" :class="{ online: conv.targetUser?.userStatus === 'ONLINE' }"></span>
      </div>
      <div class="chat-info">
        <div class="chat-name">
          {{ getName(conv) }}
          <span v-if="conv.unreadCount > 0" class="badge">{{ conv.unreadCount > 99 ? '99+' : conv.unreadCount }}</span>
        </div>
        <div class="chat-preview">{{ conv.lastMessageContent || '暂无消息' }}</div>
      </div>
      <div class="chat-meta">
        <span class="chat-time">{{ formatTime(conv.lastMessageTime) }}</span>
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

const props = defineProps<{ keyword?: string; filter?: string }>()
defineEmits<{ (e: 'select', conv: Conversation): void }>()

const conversationStore = useConversationStore()
const currentId = computed(() => conversationStore.currentConversation?.id)

const filteredConversations = computed(() => {
  let list = conversationStore.conversations

  // Filter by tab
  if (props.filter === 'unread') {
    list = list.filter(c => c.unreadCount > 0)
  } else if (props.filter === 'group') {
    list = list.filter(c => c.type === 'GROUP')
  }

  // Filter by keyword
  const kw = props.keyword?.toLowerCase() || ''
  if (kw) {
    list = list.filter(c => getName(c).toLowerCase().includes(kw))
  }

  return list
})

function getName(conv: Conversation) {
  if (conv.type === 'GROUP') return conv.groupInfo?.name || '群组'
  return conv.targetUser?.username || '用户'
}

function getInitial(conv: Conversation) {
  return getName(conv)?.[0] || '?'
}

function getAvatarBg(conv: Conversation) {
  if (conv.type === 'GROUP') {
    return 'linear-gradient(135deg, #4F46E5, #7C3AED)'
  }
  const colors = ['#DBEAFE', '#D1FAE5', '#FCE7F3', '#FEF3C7', '#FEE2E2', '#F3E8FF']
  const name = getName(conv)
  const idx = (name.charCodeAt(0) || 0) % colors.length
  const colorMap: Record<string, string> = { '#DBEAFE': '#2563EB', '#D1FAE5': '#059669', '#FCE7F3': '#DB2777', '#FEF3C7': '#D97706', '#FEE2E2': '#DC2626', '#F3E8FF': '#7C3AED' }
  const bg = colors[idx]
  return bg
}

function getAvatarTextColor(conv: Conversation) {
  if (conv.type === 'GROUP') return 'white'
  const colors = ['#DBEAFE', '#D1FAE5', '#FCE7F3', '#FEF3C7', '#FEE2E2', '#F3E8FF']
  const name = getName(conv)
  const idx = (name.charCodeAt(0) || 0) % colors.length
  const colorMap: Record<string, string> = { '#DBEAFE': '#2563EB', '#D1FAE5': '#059669', '#FCE7F3': '#DB2777', '#FEF3C7': '#D97706', '#FEE2E2': '#DC2626', '#F3E8FF': '#7C3AED' }
  return colorMap[colors[idx]] || '#666'
}

function formatTime(time?: string) {
  if (!time) return ''
  const d = dayjs(time)
  const now = dayjs()
  if (d.isSame(now, 'day')) return d.format('HH:mm')
  if (d.isSame(now.subtract(1, 'day'), 'day')) return '昨天'
  if (d.isSame(now, 'week')) return d.format('dddd')
  return d.format('MM/DD')
}
</script>

<style scoped>
.chat-list { flex: 1; overflow-y: auto; }
.chat-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  cursor: pointer;
  border-left: 3px solid transparent;
  transition: all 0.15s;
}
.chat-item:hover { background: var(--surface-2); }
.chat-item.active { background: #EEF2FF; border-left-color: var(--primary); }
.chat-avatar-wrap { position: relative; flex-shrink: 0; }
.chat-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  font-weight: 600;
  color: white;
}
.online-dot {
  position: absolute;
  bottom: 1px;
  right: 1px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  border: 2px solid var(--surface-1);
}
.online { background: var(--success); }
.offline { background: var(--text-secondary); }
.chat-info { flex: 1; min-width: 0; }
.chat-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 4px;
}
.chat-name .badge {
  background: var(--danger);
  color: white;
  font-size: 10px;
  font-weight: 600;
  padding: 1px 5px;
  border-radius: 10px;
}
.chat-preview {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.chat-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
  flex-shrink: 0;
}
.chat-time { font-size: 10px; color: var(--text-secondary); }
.chat-item.unread .chat-time { color: var(--primary); font-weight: 600; }
.empty { text-align: center; color: var(--text-secondary); padding: 24px; font-size: 13px; }
</style>
