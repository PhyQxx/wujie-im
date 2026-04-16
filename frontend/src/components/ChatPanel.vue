<template>
  <div class="chat-panel">
    <template v-if="currentConversation">
      <div class="chat-window-header">
        <div class="chat-target-avatar" :style="{ background: getAvatarBg(currentConversation) }">
          {{ getAvatarInitial(currentConversation) }}
        </div>
        <div class="chat-target-info">
          <div class="chat-target-name">{{ getConvName(currentConversation) }}</div>
          <div class="chat-target-status" :class="{ offline: !isOnline(currentConversation) }">
            {{ isOnline(currentConversation) ? '在线' : '离线' }}
          </div>
        </div>
        <div class="header-actions">
          <button class="header-btn" title="语音通话">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z"></path></svg>
          </button>
          <button class="header-btn" title="更多">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M5 12h.01M12 12h.01M19 12h.01M5 19h.01M12 19h.01M19 19h.01M5 7h.01M12 7h.01M19 7h.01M12 12h.01"></path></svg>
          </button>
          <button class="header-btn" @click="$router.push('/contacts')" title="联系人">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"></path></svg>
          </button>
        </div>
      </div>

      <div class="messages" ref="messageListRef">
        <div class="date-divider"><span>今天</span></div>
        <MessageBubble
          v-for="msg in messages"
          :key="msg.id"
          :message="msg"
          :is-mine="msg.senderId === currentUserId"
        />
      </div>

      <div class="input-area">
        <div class="input-actions-top">
          <button class="input-action-btn" title="表情">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M14.828 14.828a4 4 0 01-5.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
          </button>
          <button class="input-action-btn" title="图片" @click="sendImage">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
          </button>
          <button class="input-action-btn" title="文件" @click="sendFile">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 00-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486L20.5 13"></path></svg>
          </button>
          <button class="input-action-btn" title="戳一戳">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M7 11.5V14m0-2.5v-6a1.5 1.5 0 113 0m-3 6a1.5 1.5 0 00-3 0v2a7.5 7.5 0 0015 0v-5a1.5 1.5 0 00-3 0m-6-3V11m0-5.5v-1a1.5 1.5 0 013 0v1m0 0V11m0-5.5a1.5 1.5 0 013 0v3m0 0V11"></path></svg>
          </button>
        </div>
        <div class="input-row">
          <textarea
            class="input-box"
            v-model="inputText"
            placeholder="输入消息..."
            rows="1"
            @keydown.enter.exact.prevent="sendMessage"
          ></textarea>
          <button class="send-btn" @click="sendMessage" :disabled="!inputText.trim()">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8"></path></svg>
            发送
          </button>
        </div>
      </div>
    </template>

    <div v-else class="empty-state">
      <div class="empty-icon">💬</div>
      <p>选择一个会话开始聊天</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, ref } from 'vue'
import { useConversationStore } from '@/stores/conversation'
import { useMessageStore } from '@/stores/message'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import MessageBubble from '@/components/MessageBubble.vue'
import type { Conversation } from '@/types'

const router = useRouter()
const conversationStore = useConversationStore()
const messageStore = useMessageStore()

const inputText = ref('')
const messageListRef = ref<HTMLElement>()

const currentConversation = computed(() => conversationStore.currentConversation)
const messages = computed(() => messageStore.messages)
const currentUserId = computed(() => Number(localStorage.getItem('userId')))

function getConvName(conv: Conversation) {
  if (conv.type === 'GROUP') return conv.groupInfo?.name || '群组'
  return conv.targetUser?.username || '用户'
}

function getAvatarInitial(conv: Conversation) {
  const name = getConvName(conv)
  return name[0] || '?'
}

function getAvatarBg(conv: Conversation) {
  if (conv.type === 'GROUP') return 'linear-gradient(135deg, #4F46E5, #7C3AED)'
  const colors = ['#DBEAFE', '#D1FAE5', '#FCE7F3', '#FEF3C7', '#FEE2E2', '#F3E8FF']
  const name = getConvName(conv)
  const idx = name.charCodeAt(0) % colors.length
  return colors[idx]
}

function isOnline(conv: Conversation) {
  return conv.targetUser?.userStatus === 'ONLINE'
}

async function sendMessage() {
  if (!inputText.value.trim() || !currentConversation.value) return
  const content = inputText.value
  inputText.value = ''
  try {
    await messageStore.sendMessage({
      conversationId: currentConversation.value.id,
      content,
      contentType: 'TEXT'
    })
    await nextTick()
    scrollToBottom()
  } catch (_e) {
    inputText.value = content
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

function sendImage() { ElMessage.info('图片发送功能开发中') }
function sendFile() { ElMessage.info('文件发送功能开发中') }

// 暴露给父组件调用
defineExpose({ scrollToBottom })
</script>

<style scoped>
.chat-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: var(--surface-1, #fff);
}
.chat-window-header {
  height: 56px;
  padding: 0 16px;
  border-bottom: 1px solid var(--border, #E5E7EB);
  display: flex;
  align-items: center;
  gap: 12px;
  background: var(--surface-1, #fff);
  flex-shrink: 0;
}
.chat-target-avatar {
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
.chat-target-info { flex: 1; }
.chat-target-name { font-size: 14px; font-weight: 600; }
.chat-target-status { font-size: 11px; color: var(--success, #10B981); margin-top: 1px; }
.chat-target-status.offline { color: var(--text-secondary, #6B7280); }
.header-actions { display: flex; gap: 4px; }
.header-btn {
  width: 34px;
  height: 34px;
  border: none;
  background: transparent;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary, #6B7280);
  transition: all 0.15s;
}
.header-btn:hover { background: var(--surface-3, #F3F4F6); color: var(--text-primary, #111827); }
.header-btn svg { width: 18px; height: 18px; }
.messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.date-divider {
  text-align: center;
  font-size: 11px;
  color: var(--text-secondary, #6B7280);
  padding: 4px 0;
  position: relative;
}
.date-divider span {
  background: var(--surface-1, #fff);
  padding: 0 12px;
  position: relative;
  z-index: 1;
}
.date-divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: var(--border, #E5E7EB);
}
.input-area {
  border-top: 1px solid var(--border, #E5E7EB);
  padding: 12px 16px;
  background: var(--surface-1, #fff);
  flex-shrink: 0;
}
.input-actions-top {
  display: flex;
  gap: 4px;
  margin-bottom: 8px;
}
.input-action-btn {
  width: 28px;
  height: 28px;
  border: none;
  background: transparent;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary, #6B7280);
  transition: all 0.15s;
}
.input-action-btn:hover { background: var(--surface-3, #F3F4F6); color: var(--text-primary, #111827); }
.input-action-btn svg { width: 16px; height: 16px; }
.input-row { display: flex; gap: 8px; align-items: flex-end; }
.input-box {
  flex: 1;
  border: 1px solid var(--border, #E5E7EB);
  border-radius: 12px;
  padding: 8px 12px;
  font-size: 13px;
  font-family: inherit;
  color: var(--text-primary, #111827);
  background: var(--surface-2, #F9FAFB);
  resize: none;
  outline: none;
  line-height: 1.5;
  max-height: 120px;
  overflow-y: auto;
  transition: border-color 0.15s;
}
.input-box:focus { border-color: var(--primary, #4F46E5); background: white; }
.input-box::placeholder { color: var(--text-secondary, #6B7280); }
.send-btn {
  height: 36px;
  padding: 0 16px;
  background: var(--primary, #4F46E5);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: background 0.15s;
  flex-shrink: 0;
}
.send-btn:hover { background: var(--primary-dark, #4338CA); }
.send-btn svg { width: 16px; height: 16px; }
.send-btn:disabled { background: var(--surface-3, #F3F4F6); color: var(--text-secondary, #6B7280); cursor: not-allowed; }
.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary, #6B7280);
}
.empty-icon { font-size: 64px; margin-bottom: 16px; }
</style>
