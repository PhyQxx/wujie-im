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
          <!-- 混合内容（图文混合） -->
          <template v-if="contentBlocks.length > 1">
            <div v-for="(block, idx) in contentBlocks" :key="idx" class="content-block">
              <div v-if="block.type === 'text'" class="text-msg" v-html="renderMarkdown(block.content)" />
              <div v-else-if="block.type === 'image'" class="image-msg">
                <el-image :src="block.url" :preview-src-list="[block.url]" fit="cover" class="msg-image" preview-teleported>
                  <template #placeholder>
                    <div class="image-placeholder">
                      <div class="loading-spinner"></div>
                    </div>
                  </template>
                  <template #error>
                    <div class="image-error">图片加载失败</div>
                  </template>
                </el-image>
              </div>
            </div>
          </template>
          <!-- 单条图片（contentType为IMAGE或content是图片JSON） -->
          <div v-else-if="message.contentType === 'IMAGE' || isSingleImageBlock" class="image-msg">
            <el-image v-if="message.content.startsWith('http')" :src="message.content" :preview-src-list="[message.content]" fit="cover" class="msg-image" preview-teleported>
              <template #placeholder>
                <div class="image-placeholder">
                  <div class="loading-spinner"></div>
                </div>
              </template>
              <template #error>
                <div class="image-error">图片加载失败</div>
              </template>
            </el-image>
            <el-image v-else-if="message.content.startsWith('[')" :src="getImageUrlFromJson(message.content)" :preview-src-list="[getImageUrlFromJson(message.content)]" fit="cover" class="msg-image" preview-teleported>
              <template #placeholder>
                <div class="image-placeholder">
                  <div class="loading-spinner"></div>
                </div>
              </template>
              <template #error>
                <div class="image-error">图片加载失败</div>
              </template>
            </el-image>
          </div>
          <!-- 单条文件 -->
          <div v-else-if="message.contentType === 'FILE'" class="file-msg">📎 {{ message.content }}</div>
          <!-- 系统消息 -->
          <div v-else-if="message.contentType === 'SYSTEM'" class="system-msg">{{ message.content }}</div>
          <!-- 纯文本/Markdown -->
          <div v-else class="text-msg" v-html="renderMarkdown(message.content)" />
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
import { marked } from 'marked'
import type { Message } from '@/types'

marked.setOptions({ breaks: true })

const props = defineProps<{ message: Message; isMine: boolean }>()
defineEmits<{ reply: [msg: Message] }>()

const showSenderName = computed(() => !props.isMine && props.message.senderName)
const senderName = computed(() => props.message.senderName || '用户')
const isAiMessage = computed(() => props.message.contentType === 'AI')
const isAtAll = computed(() => props.message.meta?.includes('atAll'))

// 解析消息内容为内容块数组
interface ContentBlock { type: 'text' | 'image'; content?: string; url?: string }
const contentBlocks = computed<ContentBlock[]>(() => {
  const c = props.message.content
  if (!c) return []
  // 尝试解析为 JSON 混合内容
  if (c.startsWith('[')) {
    try {
      const arr = JSON.parse(c)
      return arr.map((item: any) => ({
        type: item.type || 'text',
        content: item.content,
        url: item.url
      }))
    } catch { /* fall through */ }
  }
  return []
})

// 检查是否单个图片块
const isSingleImageBlock = computed(() => {
  return contentBlocks.value.length === 1 && contentBlocks.value[0].type === 'image'
})

function renderMarkdown(text: string): string {
  if (!text) return ''
  try {
    return marked.parse(text) as string
  } catch {
    return text
  }
}

function getImageUrlFromJson(jsonStr: string): string {
  try {
    const arr = JSON.parse(jsonStr)
    if (Array.isArray(arr) && arr[0]?.url) {
      return arr[0].url
    }
  } catch { /* ignore */ }
  return jsonStr
}

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
.msg-image {
  max-width: 200px;
  max-height: 200px;
  border-radius: 8px;
  cursor: pointer;
  display: block;
}
.image-placeholder {
  width: 120px;
  height: 120px;
  background: linear-gradient(135deg, #f0f0f0 0%, #e0e0e0 100%);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.loading-spinner {
  width: 24px;
  height: 24px;
  border: 3px solid #d0d0d0;
  border-top-color: #4F46E5;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}
.image-error {
  width: 120px;
  height: 120px;
  background: #f5f5f5;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 12px;
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
.content-block { margin-bottom: 4px; }
.content-block:last-child { margin-bottom: 0; }
.content-block .text-msg { line-height: 1.5; }
/* Markdown 样式 */
.text-msg :deep(a) { color: inherit; text-decoration: underline; }
.text-msg :deep(code) { background: rgba(0,0,0,0.08); padding: 1px 4px; border-radius: 3px; font-size: 12px; font-family: monospace; }
.text-msg :deep(pre) { background: rgba(0,0,0,0.08); padding: 8px; border-radius: 6px; overflow-x: auto; margin: 4px 0; }
.text-msg :deep(pre code) { background: none; padding: 0; }
.text-msg :deep(strong) { font-weight: 600; }
.text-msg :deep(p) { margin: 0 0 4px 0; }
.text-msg :deep(p:last-child) { margin-bottom: 0; }
</style>
