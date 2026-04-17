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
          @reply="(m) => setReplyingTo(m)"
        />
      </div>

      <div class="input-area">
        <!-- 回复引用条 -->
        <div v-if="replyingTo" class="reply-banner">
          <span class="reply-label">回复 {{ replyingTo.senderId === currentUserId ? '自己' : '' }}</span>
          <span class="reply-content">{{ replyingTo.content }}</span>
          <button class="reply-close" @click="replyingTo = null">✕</button>
        </div>
        <div class="input-actions-top">
          <el-popover placement="top" :width="320" trigger="click">
            <template #reference>
              <button class="input-action-btn" title="表情">
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M14.828 14.828a4 4 0 01-5.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
              </button>
            </template>
            <div class="emoji-grid">
              <span v-for="e in emojis" :key="e" class="emoji" @click="insertEmoji(e)">{{ e }}</span>
            </div>
          </el-popover>
          <input ref="imageInput" type="file" accept="image/*" style="display:none" @change="handleImageChange" />
          <button class="input-action-btn" title="图片" @click="imageInput?.click()">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
          </button>
          <input ref="fileInput" type="file" style="display:none" @change="handleFileChange" />
          <button class="input-action-btn" title="文件" @click="fileInput?.click()">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 00-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486L20.5 13"></path></svg>
          </button>
          <button v-if="currentConversation?.type === 'GROUP'" class="input-action-btn at-all-btn" title="@全体成员" @click="insertAtAll">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"></path></svg>
          </button>
        </div>
        <div class="input-row">
          <textarea
            ref="textareaRef"
            class="input-box"
            v-model="inputText"
            placeholder="输入消息..."
            rows="1"
            @keydown.enter.exact.prevent="sendMessage"
          ></textarea>
          <button class="send-btn" @click="sendMessage" :disabled="!inputText.trim() && !uploading">
            <span v-if="uploading" class="uploading-spinner">⏳</span>
            <template v-else>
              <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8"></path></svg>
              发送
            </template>
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
import { computed, nextTick, ref, watch } from 'vue'
import { useConversationStore } from '@/stores/conversation'
import { useMessageStore } from '@/stores/message'
import { ElMessage } from 'element-plus'
import { messageApi } from '@/api/message'
import MessageBubble from '@/components/MessageBubble.vue'
import type { Conversation } from '@/types'

const conversationStore = useConversationStore()
const messageStore = useMessageStore()

const inputText = ref('')
const messageListRef = ref<HTMLElement>()
const imageInput = ref<HTMLInputElement>()
const fileInput = ref<HTMLInputElement>()
const textareaRef = ref<HTMLTextAreaElement>()
const uploading = ref(false)
const replyingTo = ref<any>(null)
const atAllActive = ref(false)

const emojis = ['😀','😃','😄','😁','😆','😅','😂','🤣','😊','😇','🙂','🙃','😉','😌','😍','🥰','😘','😗','😙','😚','😋','😛','😜','🤪','😝','🤑','🤗','🤭','🤫','🤔','🤐','🤨','😐','😑','😶','😏','😒','🙄','😬','🤥','😌','😔','😪','🤤','😴','😷','🤒','🤕','🤢','🤮','🤧','🥵','🥶','🥴','😵','🤯','🤠','🥳','😎','🤓','🧐','😕','😟','🙁','😮','😯','😲','😳','🥺','😦','😧','😨','😰','😥','😢','😭','😱','😖','😣','😞','😓','😩','😫','🥱','😤','😡','😠','🤬','😈','👿','💀','☠️','💩','🤡','👹','👺','👻','👽','👾','🤖']

const currentConversation = computed(() => conversationStore.currentConversation)
const messages = computed(() => messageStore.messages)
const currentUserId = computed(() => Number(localStorage.getItem('userId')))

// 切换会话时加载历史消息
watch(currentConversation, (conv) => {
  if (conv) {
    messageStore.fetchMessages(conv.id)
    nextTick(() => scrollToBottom())
  }
}, { immediate: true })

function getConvName(conv: Conversation) {
  if (conv.type === 'GROUP') return conv.groupInfo?.name || '群组'
  return conv.targetUser?.username || '用户'
}
function getAvatarInitial(conv: Conversation) {
  return getConvName(conv)[0] || '?'
}
function getAvatarBg(conv: Conversation) {
  if (conv.type === 'GROUP') return 'linear-gradient(135deg, #4F46E5, #7C3AED)'
  const colors = ['#DBEAFE', '#D1FAE5', '#FCE7F3', '#FEF3C7', '#FEE2E2', '#F3E8FF']
  return colors[getConvName(conv).charCodeAt(0) % colors.length]
}
function isOnline(conv: Conversation) {
  return conv.targetUser?.userStatus === 'ONLINE'
}

async function sendMessage() {
  if (!inputText.value.trim() || !currentConversation.value) return
  const content = inputText.value
  inputText.value = ''
  const meta = atAllActive.value ? JSON.stringify({ atAll: true }) : null
  atAllActive.value = false
  const replyId = replyingTo.value?.id || undefined
  replyingTo.value = null
  try {
    await messageStore.sendMessage({
      conversationId: currentConversation.value.id,
      content,
      contentType: 'TEXT',
      meta: meta || undefined,
      replyId
    })
    await nextTick()
    scrollToBottom()
  } catch (_e) {
    inputText.value = content
  }
}

async function handleImageChange(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file || !currentConversation.value) return
  uploading.value = true
  try {
    const fd = new FormData()
    fd.append('file', file)
    const res = await messageApi.uploadImage(fd)
    if (res.data?.url) {
      await messageStore.sendMessage({
        conversationId: currentConversation.value.id,
        content: res.data.url,
        contentType: 'IMAGE',
        meta: JSON.stringify({ filename: res.data.filename, size: res.data.size })
      })
      await nextTick()
      scrollToBottom()
    }
  } catch (_e) {
    ElMessage.error('图片上传失败')
  } finally {
    uploading.value = false
    if (imageInput.value) imageInput.value.value = ''
  }
}

async function handleFileChange(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file || !currentConversation.value) return
  uploading.value = true
  try {
    const fd = new FormData()
    fd.append('file', file)
    const res = await messageApi.uploadFile(fd)
    if (res.data?.url) {
      await messageStore.sendMessage({
        conversationId: currentConversation.value.id,
        content: res.data.url,
        contentType: 'FILE',
        meta: JSON.stringify({ filename: res.data.filename, size: res.data.size })
      })
      await nextTick()
      scrollToBottom()
    }
  } catch (_e) {
    ElMessage.error('文件上传失败')
  } finally {
    uploading.value = false
    if (fileInput.value) fileInput.value.value = ''
  }
}

function insertEmoji(emoji: string) {
  inputText.value += emoji
  textareaRef.value?.focus()
}

function insertAtAll() {
  atAllActive.value = !atAllActive.value
}

function setReplyingTo(msg: any) {
  replyingTo.value = msg
}

function scrollToBottom() {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

defineExpose({ scrollToBottom, setReplyingTo })
</script>

<style scoped>
.chat-panel { flex: 1; display: flex; flex-direction: column; overflow: hidden; background: var(--surface-1, #fff); }
.chat-window-header {
  height: 56px; padding: 0 16px; border-bottom: 1px solid var(--border, #E5E7EB);
  display: flex; align-items: center; gap: 12px; background: var(--surface-1, #fff); flex-shrink: 0;
}
.chat-target-avatar {
  width: 36px; height: 36px; border-radius: 50%; display: flex; align-items: center;
  justify-content: center; font-size: 14px; font-weight: 600; color: white; flex-shrink: 0;
}
.chat-target-info { flex: 1; }
.chat-target-name { font-size: 14px; font-weight: 600; }
.chat-target-status { font-size: 11px; color: var(--success, #10B981); margin-top: 1px; }
.chat-target-status.offline { color: var(--text-secondary, #6B7280); }
.header-actions { display: flex; gap: 4px; }
.header-btn {
  width: 34px; height: 34px; border: none; background: transparent; border-radius: 8px;
  cursor: pointer; display: flex; align-items: center; justify-content: center;
  color: var(--text-secondary, #6B7280); transition: all 0.15s;
}
.header-btn:hover { background: var(--surface-3, #F3F4F6); color: var(--text-primary, #111827); }
.header-btn svg { width: 18px; height: 18px; }
.messages {
  flex: 1; overflow-y: auto; padding: 16px; display: flex; flex-direction: column; gap: 12px;
}
.date-divider {
  text-align: center; font-size: 11px; color: var(--text-secondary, #6B7280); padding: 4px 0; position: relative;
}
.date-divider span { background: var(--surface-1, #fff); padding: 0 12px; position: relative; z-index: 1; }
.date-divider::before {
  content: ''; position: absolute; top: 50%; left: 0; right: 0; height: 1px; background: var(--border, #E5E7EB);
}
.input-area { border-top: 1px solid var(--border, #E5E7EB); padding: 12px 16px; background: var(--surface-1, #fff); flex-shrink: 0; }
.input-actions-top { display: flex; gap: 4px; margin-bottom: 8px; }
.input-action-btn {
  width: 28px; height: 28px; border: none; background: transparent; border-radius: 6px;
  cursor: pointer; display: flex; align-items: center; justify-content: center;
  color: var(--text-secondary, #6B7280); transition: all 0.15s;
}
.input-action-btn:hover { background: var(--surface-3, #F3F4F6); color: var(--text-primary, #111827); }
.input-action-btn svg { width: 16px; height: 16px; }
.input-row { display: flex; gap: 8px; align-items: flex-end; }
.input-box {
  flex: 1; border: 1px solid var(--border, #E5E7EB); border-radius: 12px; padding: 8px 12px;
  font-size: 13px; font-family: inherit; color: var(--text-primary, #111827);
  background: var(--surface-2, #F9FAFB); resize: none; outline: none; line-height: 1.5; max-height: 120px;
  overflow-y: auto; transition: border-color 0.15s;
}
.input-box:focus { border-color: var(--primary, #4F46E5); background: white; }
.input-box::placeholder { color: var(--text-secondary, #6B7280); }
.send-btn {
  height: 36px; padding: 0 16px; background: var(--primary, #4F46E5); color: white; border: none;
  border-radius: 10px; font-size: 13px; font-weight: 600; cursor: pointer; font-family: inherit;
  display: flex; align-items: center; gap: 4px; transition: background 0.15s; flex-shrink: 0;
}
.send-btn:hover { background: var(--primary-dark, #4338CA); }
.send-btn:disabled { background: var(--surface-3, #F3F4F6); color: var(--text-secondary, #6B7280); cursor: not-allowed; }
.send-btn svg { width: 16px; height: 16px; }
.uploading-spinner { animation: spin 1s linear infinite; }
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
.empty-state {
  flex: 1; display: flex; flex-direction: column; align-items: center;
  justify-content: center; color: var(--text-secondary, #6B7280);
}
.empty-icon { font-size: 64px; margin-bottom: 16px; }
.emoji-grid { display: grid; grid-template-columns: repeat(8, 1fr); gap: 4px; }
.emoji { font-size: 20px; cursor: pointer; padding: 4px; border-radius: 4px; text-align: center; }
.emoji:hover { background: var(--surface-3, #F3F4F6); }
.reply-banner {
  display: flex; align-items: center; gap: 8px; padding: 6px 12px;
  background: var(--surface-3, #F3F4F6); border-left: 3px solid var(--primary, #4F46E5);
  border-radius: 4px; margin-bottom: 8px; font-size: 12px;
}
.reply-label { color: var(--primary, #4F46E5); font-weight: 600; flex-shrink: 0; }
.reply-content { color: var(--text-secondary, #6B7280); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; flex: 1; }
.reply-close { background: none; border: none; cursor: pointer; color: var(--text-secondary, #6B7280); font-size: 12px; padding: 0 4px; }
.reply-close:hover { color: var(--text-primary, #111827); }
.at-all-btn { color: var(--primary, #4F46E5) !important; }
.at-all-btn:hover { background: #EEF2FF !important; }
</style>
