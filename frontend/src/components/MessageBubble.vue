<template>
  <div class="message-row" :class="[isMine ? 'outgoing' : 'incoming', { ai: isAiMessage }]">
    <div class="msg-avatar" :style="avatarStyle">{{ avatarText }}</div>
    <div class="msg-body">
      <div v-if="showSenderName" class="msg-sender">
        <span v-if="isAiMessage" class="ai-badge">AI</span>
        <span v-if="isAtAll" class="at-all-badge">@全体</span>
        {{ senderName }}
      </div>
      <!-- 回复引用内容 -->
      <div v-if="message.replyId" class="reply-quote">
        <span class="reply-arrow">↳</span> {{ message.meta }}
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
      <div class="msg-actions">
        <button v-if="!message.recall" class="msg-action-btn" @click="$emit('reply', message)" title="回复">↩</button>
      </div>
      <div class="msg-time">
        {{ formatTime(message.createTime) }}
        <span v-if="isMine && message.status" class="status-icon" :class="message.status.toLowerCase()">
          <span v-if="message.status === 'SENDING'" class="sending-icon">⏳</span>
          <span v-else-if="message.status === 'SENT'" class="sent-icon">✓</span>
          <span v-else-if="message.status === 'DELIVERED'" class="delivered-icon">✓✓</span>
          <span v-else-if="message.status === 'READ'" class="read-icon">✓✓</span>
          <span v-else-if="message.status === 'FAILED'" class="failed-icon">⚠</span>
        </span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import dayjs from 'dayjs'
import type { Message } from '@/types'

const props = defineProps<{ message: Message; isMine: boolean }>()
defineEmits<{ reply: [msg: Message] }>()

const showSenderName = computed(() => !props.isMine && props.message.senderName)
const senderName = computed(() => props.message.senderName || '用户')
const isAiMessage = computed(() => props.message.contentType === 'AI')
const isAtAll = computed(() => props.message.meta?.includes('atAll'))

const avatarText = computed(() => {
  if (props.isMine) return '我'
  if (isAiMessage.value) return '🤖'
  if (isAtAll.value) return '@'
  return senderName.value[0] || '?'
})

const avatarStyle = computed(() => {
  if (props.isMine) {
    return { background: '#4F46E5', color: 'white' }
  }
  if (isAiMessage.value) {
    return { background: 'linear-gradient(135deg, #4F46E5, #7C3AED)', color: 'white' }
  }
  const colors = ['#DBEAFE', '#D1FAE5', '#FCE7F3', '#FEF3C7', '#FEE2E2', '#F3E8FF']
  const textColors: Record<string, string> = { '#DBEAFE': '#2563EB', '#D1FAE5': '#059669', '#FCE7F3': '#DB2777', '#FEF3C7': '#D97706', '#FEE2E2': '#DC2626', '#F3E8FF': '#7C3AED' }
  const idx = (senderName.value.charCodeAt(0) || 0) % colors.length
  return { background: colors[idx], color: textColors[colors[idx]] }
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
  animation: msgIn 0.2s ease-out;
}
@keyframes msgIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
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
  background: #4F46E5;
  color: white;
  padding: 1px 5px;
  border-radius: 4px;
  margin-right: 4px;
  font-weight: 600;
}
.at-all-badge {
  font-size: 10px; background: #EF4444; color: white; padding: 1px 5px;
  border-radius: 4px; margin-right: 4px; font-weight: 600;
}
.reply-quote {
  font-size: 11px; color: var(--text-secondary, #6B7280); padding: 4px 8px;
  background: var(--surface-3, #F3F4F6); border-left: 2px solid var(--primary, #4F46E5);
  border-radius: 0 4px 4px 0; margin-bottom: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.reply-arrow { color: var(--primary, #4F46E5); margin-right: 4px; }
.msg-actions {
  display: none; position: absolute; top: -16px;
  right: 0; gap: 2px;
}
.message-row:hover .msg-actions { display: flex; }
.msg-action-btn {
  width: 20px; height: 20px; border: none; background: var(--surface-3, #F3F4F6);
  border-radius: 4px; cursor: pointer; font-size: 12px;
  color: var(--text-secondary, #6B7280); display: flex; align-items: center; justify-content: center;
}
.msg-action-btn:hover { background: var(--primary, #4F46E5); color: white; }
.msg-body { position: relative; }
.msg-bubble {
  padding: 8px 12px;
  border-radius: 16px;
  font-size: 13px;
  line-height: 1.5;
  word-break: break-word;
  max-width: 100%;
}
.message-row.incoming .msg-bubble {
  background: #F3F4F6;
  color: #111827;
  border-bottom-left-radius: 4px;
}
.message-row.outgoing .msg-bubble {
  background: #4F46E5;
  color: white;
  border-bottom-right-radius: 4px;
}
.message-row.ai .msg-bubble {
  background: linear-gradient(135deg, #EEF2FF, #f5f3ff);
  border: 1px solid #c7d2fe;
  color: #4F46E5;
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
  color: #4F46E5;
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
  color: var(--text-muted);
  margin-top: 2px;
  margin-left: 4px;
  display: flex;
  align-items: center;
  gap: 2px;
}
.message-row.outgoing .msg-time { text-align: right; justify-content: flex-end; }
.status-icon { font-size: 11px; }
.sent-icon { color: var(--text-muted); }
.delivered-icon { color: var(--text-secondary); }
.read-icon { color: #3B82F6; }
.failed-icon { color: #EF4444; }
.sending-icon { animation: spin 1s linear infinite; display: inline-block; }
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
</style>
