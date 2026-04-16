<template>
  <div class="message-bubble" :class="{ mine: isMine }">
    <el-avatar v-if="!isMine" :size="32" :src="message.senderId ? undefined : undefined" class="avatar">
      {{ message.senderId }}
    </el-avatar>

    <div class="bubble-content">
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

      <div class="msg-meta">
        <span class="msg-time">{{ formatTime(message.createTime) }}</span>
        <span v-if="isMine" class="msg-status">{{ statusIcon }}</span>
      </div>
    </div>

    <el-avatar v-if="isMine" :size="32" class="avatar">我</el-avatar>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import dayjs from 'dayjs'
import type { Message } from '@/types'

const props = defineProps<{ message: Message; isMine: boolean }>()

const statusIcon = computed(() => {
  const map: Record<string, string> = {
    SENDING: '⏳', SENT: '✓', DELIVERED: '✓✓', READ: '✓✓', RECALLED: '✕'
  }
  return map[props.message.status] || ''
})

function formatTime(time: string) {
  return dayjs(time).format('HH:mm')
}
</script>

<style scoped>
.message-bubble {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  max-width: 70%;
}
.message-bubble.mine {
  margin-left: auto;
  flex-direction: row-reverse;
}
.avatar { flex-shrink: 0; font-size: 11px; }
.bubble-content { display: flex; flex-direction: column; }
.mine .bubble-content { align-items: flex-end; }
.text-msg {
  background: var(--surface-2);
  padding: 8px 12px;
  border-radius: 12px 12px 12px 2px;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
  max-width: 320px;
}
.mine .text-msg {
  background: #4F46E5;
  color: #fff;
  border-radius: 12px 12px 2px 12px;
}
.image-msg img { max-width: 200px; max-height: 200px; border-radius: 8px; cursor: pointer; }
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
.msg-meta { display: flex; align-items: center; gap: 4px; margin-top: 2px; }
.msg-time { font-size: 11px; color: var(--text-secondary); }
.msg-status { font-size: 11px; color: #10B981; }
</style>
