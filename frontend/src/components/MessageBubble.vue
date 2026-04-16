<template>
  <div class="message-row" :class="[isMine ? 'outgoing' : 'incoming', { ai: isAiMessage }]">
    <div class="msg-avatar" :style="avatarStyle">{{ avatarText }}</div>
    <div class="msg-body">
      <div v-if="showSenderName" class="msg-sender">
        <span v-if="isAiMessage" class="ai-badge">AI</span>
        {{ senderName }}
      </div>
      <div class="msg-bubble">
        <div v-if="message.recall" class="recalled">消息已撤回</div>
        <template v-else>
          <div v-if="message.contentType === 'TEXT'" class="text-msg">{{ message.content }}</div>
          <div v-else-if="message.contentType === 'IMAGE'" class="image-msg">
            <img :src="message.content" alt="图片" />
          </div>
          <div v-else-if="message.contentType === 'FILE'" class="file-msg">
            📎 {{ message.content }}
          </div>
          <div v-else-if="message.contentType === 'SYSTEM'" class="system-msg">{{ message.content }}</div>
          <div v-else class="text-msg">{{ message.content }}</div>
        </template>
      </div>
      <div class="msg-time">{{ formatTime(message.createTime) }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import dayjs from 'dayjs'
import type { Message } from '@/types'

const props = defineProps<{ message: Message; isMine: boolean }>()

const showSenderName = computed(() => !props.isMine && props.message.senderName)
const senderName = computed(() => props.message.senderName || '用户')
const isAiMessage = computed(() => props.message.contentType === 'AI')

const avatarText = computed(() => {
  if (props.isMine) return '我'
  if (isAiMessage.value) return '🤖'
  return senderName.value[0] || '?'
})

const avatarStyle = computed(() => {
  if (props.isMine) {
    return { background: 'var(--primary)', color: 'white' }
  }
  if (isAiMessage.value) {
    return { background: 'linear-gradient(135deg, #4F46E5, #7C3AED)', color: 'white' }
  }
  const colors = ['#DBEAFE', '#D1FAE5', '#FCE7F3', '#FEF3C7', '#FEE2E2', '#F3E8FF']
  const textColors: Record<string, string> = { '#DBEAFE': '#2563EB', '#D1FAE5': '#059669', '#FCE7F3': '#DB2777', '#FEF3C7': '#D97706', '#FEE2E2': '#DC2626', '#F3E8FF': '#7C3AED' }
  const idx = (senderName.value.charCodeAt(0) || 0) % colors.length
  return { background: colors[idx], color: textColors[colors[idx]] }
})

const statusIcon = computed(() => {
  const map: Record<string, string> = { SENDING: '⏳', SENT: '✓', DELIVERED: '✓✓', READ: '✓✓', RECALLED: '✕' }
  return map[props.message.status] || ''
})

function formatTime(time: string) {
  return dayjs(time).format('HH:mm')
}
</script>

<style scoped>
.message-row {
  display: flex;
  gap: 8px;
  max-width: 72%;
}
.message-row.outgoing {
  align-self: flex-end;
  flex-direction: row-reverse;
}
.message-row.incoming {
  align-self: flex-start;
}
.msg-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 600;
  flex-shrink: 0;
  align-self: flex-end;
}
.msg-body {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.msg-sender {
  font-size: 11px;
  color: var(--text-secondary);
  font-weight: 500;
  margin-bottom: 2px;
  margin-left: 4px;
  display: flex;
  align-items: center;
}
.ai-badge {
  font-size: 10px;
  background: var(--primary);
  color: white;
  padding: 1px 5px;
  border-radius: 4px;
  margin-right: 4px;
  font-weight: 600;
}
.msg-bubble {
  padding: 8px 12px;
  border-radius: 12px;
  font-size: 13px;
  line-height: 1.5;
  word-break: break-word;
  max-width: 100%;
}
.message-row.incoming .msg-bubble {
  background: var(--surface-3);
  color: var(--text-primary);
  border-bottom-left-radius: 4px;
}
.message-row.outgoing .msg-bubble {
  background: var(--primary);
  color: white;
  border-bottom-right-radius: 4px;
}
.message-row.ai .msg-bubble {
  background: linear-gradient(135deg, #EEF2FF, #f5f3ff);
  border: 1px solid #c7d2fe;
  color: var(--primary);
}
.image-msg img {
  max-width: 200px;
  max-height: 200px;
  border-radius: 8px;
  cursor: pointer;
}
.file-msg {
  background: var(--surface-2);
  padding: 8px 12px;
  border-radius: 8px;
  font-size: 13px;
  color: var(--primary);
}
.system-msg {
  text-align: center;
  color: var(--text-secondary);
  font-size: 12px;
  background: none;
}
.recalled { color: var(--text-secondary); font-size: 13px; font-style: italic; padding: 4px 8px; }
.msg-time {
  font-size: 10px;
  color: var(--text-secondary);
  margin-top: 2px;
  margin-left: 4px;
}
.message-row.outgoing .msg-time { text-align: right; }
</style>
