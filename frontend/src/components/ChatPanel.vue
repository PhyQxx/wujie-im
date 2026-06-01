<template>
  <div class="chat-panel">
    <template v-if="currentConversation">
      <div class="chat-window-header">
        <div class="chat-target-avatar" :style="{ background: getAvatarBg(currentConversation) }">
          {{ getAvatarInitial(currentConversation) }}
        </div>
        <div class="chat-target-info">
          <div class="chat-target-name">{{ getConvName(currentConversation) }}</div>
          <div v-if="currentConversation.type !== 'GROUP'" class="chat-target-status" :class="{ offline: !isOnline(currentConversation) }">
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
          <button class="header-btn" :class="{ active: showSidebar }" @click="showSidebar = !showSidebar" title="侧边栏">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path></svg>
          </button>
          <button class="header-btn" @click="$router.push('/contacts')" title="联系人">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"></path></svg>
          </button>
        </div>
      </div>

      <div class="chat-window-main">
        <div class="chat-messages-container">
          <div class="messages" ref="messageListRef" @scroll="onMessageScroll">
            <!-- 加载更多指示器 -->
            <div v-if="loadingMore" class="loading-more">
              <span class="loading-spinner"></span>
              <span>加载中...</span>
            </div>
            <div v-else-if="!hasMore && messages.length > 0" class="loading-more no-more">
              — 已加载全部消息 —
            </div>
            <div class="date-divider"><span>今天</span></div>
            <MessageBubble
              v-for="msg in messages"
              :key="msg.id"
              :id="'msg-' + msg.id"
              :message="msg"
              :is-mine="msg.senderId === currentUserId"
              @reply="(m) => setReplyingTo(m)"
            />
            <!-- 机器人打字提示 -->
            <div v-if="isRobotTyping" class="typing-indicator">
              <div class="typing-avatar">🤖</div>
              <div class="typing-bubble">
                <div class="dot"></div>
                <div class="dot"></div>
                <div class="dot"></div>
              </div>
            </div>
          </div>

          <!-- 回到底部按钮 -->
          <transition name="fade">
            <button v-if="userHasScrolledUp" class="scroll-bottom-btn" @click="scrollToBottom">
              <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 14l-7 7-7-7m14-8l-7 7-7-7"></path></svg>
              <span>有新消息</span>
            </button>
          </transition>
        </div>

        <!-- 侧边栏：中心区域 -->
        <transition name="slide-right">
          <div v-if="showSidebar" class="chat-sidebar">
            <div class="sidebar-header-tabs">
              <div class="sidebar-tab" :class="{ active: activeSidebarTab === 'files' }" @click="activeSidebarTab = 'files'">文件</div>
              <div class="sidebar-tab" :class="{ active: activeSidebarTab === 'search' }" @click="activeSidebarTab = 'search'">搜索</div>
              <button class="close-sidebar-icon" @click="showSidebar = false">✕</button>
            </div>

            <!-- 文件列表 -->
            <div v-if="activeSidebarTab === 'files'" class="sidebar-content">
              <div class="file-list">
                <div v-for="file in recentFiles" :key="file.id" class="file-item">
                  <div class="file-icon">📄</div>
                  <div class="file-info">
                    <div class="file-name">{{ getFileName(file) }}</div>
                    <div class="file-meta">{{ formatSize(getFileSize(file)) }} · {{ formatTime(file.createTime) }}</div>
                  </div>
                  <button class="file-download" @click="downloadFile(file.content)">↓</button>
                </div>
                <div v-if="!recentFiles.length" class="empty-sidebar">暂无文件记录</div>
              </div>
            </div>

            <!-- 消息搜索 -->
            <div v-if="activeSidebarTab === 'search'" class="sidebar-content search-content">
              <div class="search-input-wrap">
                <el-input v-model="searchKeyword" placeholder="搜索关键词..." prefix-icon="Search" clearable @input="doSearch" />
              </div>
              <div class="search-results">
                <div v-for="res in searchResults" :key="res.id" class="search-result-item" @click="jumpToMessage(res.id)">
                  <div class="search-res-header">
                    <span class="search-res-sender">{{ res.senderName || '用户' }}</span>
                    <span class="search-res-time">{{ formatTime(res.createTime) }}</span>
                  </div>
                  <div class="search-res-text" v-html="highlightKeyword(res.content, searchKeyword)" />
                </div>
                <div v-if="searchKeyword && !searchResults.length && !searching" class="empty-sidebar">无匹配结果</div>
                <div v-if="searching" class="empty-sidebar">搜索中...</div>
                <div v-if="!searchKeyword" class="empty-sidebar">输入内容开始搜索</div>
              </div>
            </div>
          </div>
        </transition>
      </div>

      <div class="input-area">
        <!-- 回复引用条 -->
        <div v-if="replyingTo" class="reply-banner">
          <span class="reply-label">回复 {{ replyingTo.senderId === currentUserId ? '自己' : '' }}</span>
          <span class="reply-content">{{ replyingTo.content }}</span>
          <button class="reply-close" @click="replyingTo = null">✕</button>
        </div>
        <!-- 待发送图片缩略图 -->
        <div v-if="pendingImages.length" class="pending-images">
          <div v-for="(url, idx) in pendingImages" :key="idx" class="pending-img-wrap">
            <img :src="url" class="pending-img" />
            <button class="pending-img-remove" @click="removePendingImage(idx)">✕</button>
          </div>
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
        <div class="input-row" style="position: relative;">
          <!-- @ 提及列表移至此处以避免被 overflow hidden/auto 裁剪 -->
          <transition name="fade">
            <div v-if="showMentionList && filteredMembers.length" class="mention-list-popover">
              <div
                v-for="(m, idx) in filteredMembers"
                :key="m.isAll ? 'all' : m.userId"
                class="mention-item"
                :class="{ active: idx === mentionIndex, 'all-option': m.isAll }"
                @mousedown.prevent="insertMention(m)"
              >
                <div class="mention-avatar" :style="{ background: m.isAll ? '#F59E0B' : getAvatarBgFromUser(m.user) }">
                  {{ m.isAll ? '📢' : (m.user?.nickname || m.user?.username)?.[0] }}
                </div>
                <span class="mention-name">{{ m.user?.nickname || m.user?.username }}</span>
              </div>
            </div>
          </transition>

          <div class="md-editor-wrap" @click="startEditing">
            <!-- 渲染层：失去焦点时显示 -->
            <div v-if="!isEditing && inputText" class="md-render-layer" v-html="getRenderedLines()" />
            
            <!-- 编辑层：聚焦时显示 -->
            <textarea
              v-if="isEditing"
              ref="textareaRef"
              class="md-editor"
              v-model="inputText"
              placeholder="输入消息，支持 Markdown..."
              :style="{ height: editorHeight + 'px' }"
              @focus="isEditing = true"
              @blur="onEditorBlur"
              @input="onEditorInput"
              @keydown="handleKeydown"
            />
            <!-- 默认占位：未编辑且无内容时 -->
            <div v-if="!inputText && !isEditing" class="md-placeholder" style="height: 40px">输入消息，支持 Markdown...</div>
          </div>
          <button class="send-btn" @click="sendMessage" :disabled="!inputText.trim() && !pendingImages.length && !uploading">
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
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { useConversationStore } from '@/stores/conversation'
import { useMessageStore } from '@/stores/message'
import { useGroupStore } from '@/stores/group'
import { ElMessage } from 'element-plus'
import { messageApi } from '@/api/message'
import { marked } from 'marked'
import dayjs from 'dayjs'
import MessageBubble from '@/components/MessageBubble.vue'
import type { Conversation } from '@/types'

const conversationStore = useConversationStore()
const messageStore = useMessageStore()
const groupStore = useGroupStore()

const inputText = ref('')
const messageListRef = ref<HTMLElement>()
const imageInput = ref<HTMLInputElement>()
const fileInput = ref<HTMLInputElement>()
const textareaRef = ref<HTMLTextAreaElement>()
const uploading = ref(false)
const replyingTo = ref<any>(null)
const showSidebar = ref(false)
const activeSidebarTab = ref<'files' | 'search'>('files')
const atAllActive = ref(false)
const searchKeyword = ref('')
const searchResults = ref<any[]>([])
const searching = ref(false)
const userHasScrolledUp = ref(false)
const isProgrammaticScroll = ref(false)
const pendingImages = ref<string[]>([]) // 待发送的图片 URL 列表
const isEditing = ref(false) // 编辑器是否获得焦点
const editorHeight = ref(40) // 初始高度
const editorRows = ref(1) // textarea 行数

const showMentionList = ref(false)
const mentionSearch = ref('')
const mentionIndex = ref(0)
const atUserIds = ref<number[]>([])

const filteredMembers = computed(() => {
  const allOption = {
    userId: -1, // 特殊ID标识全体
    user: { username: '全体成员', avatar: '' },
    isAll: true
  }
  
  const members = groupStore.members
  if (!mentionSearch.value) return [allOption, ...members]
  
  const search = mentionSearch.value.toLowerCase()
  const filtered = members.filter(m => 
    m.user?.username?.toLowerCase().includes(search)
  )
  
  if ('全体成员'.includes(search)) {
    return [allOption, ...filtered]
  }
  return filtered
})

const editorHeightStyle = computed(() => {
  const lineCount = inputText.value.split('\n').length
  return Math.min(Math.max(lineCount * 24 + 16, 40), 200)
})

watch(inputText, () => {
  editorHeight.value = editorHeightStyle.value
}, { immediate: true })

// 来自 store
const loadingMore = computed(() => messageStore.loadingMore)
const hasMore = computed(() => messageStore.hasMore)

const emojis = ['😀','😃','😄','😁','😆','😅','😂','🤣','😊','😇','🙂','🙃','😉','😌','😍','🥰','😘','😗','😙','😚','😋','😛','😜','🤪','😝','🤑','🤗','🤭','🤫','🤔','🤐','🤨','😐','😑','😶','😏','😒','🙄','😬','🤥','😌','😔','😪','🤤','😴','😷','🤒','🤕','🤢','🤮','🤧','🥵','🥶','🥴','😵','🤯','🤠','🥳','😎','🤓','🧐','😕','😟','🙁','😮','😯','😲','😳','🥺','😦','😧','😨','😰','😥','😢','😭','😱','😖','😣','😞','😓','😩','😫','🥱','😤','😡','😠','🤬','😈','👿','💀','☠️','💩','🤡','👹','👺','👻','👽','👾','🤖']

const currentConversation = computed(() => conversationStore.currentConversation)
const messages = computed(() => messageStore.messages)
const currentUserId = computed(() => Number(localStorage.getItem('userId')))
const isRobotTyping = computed(() => messageStore.isRobotTyping)

const recentFiles = computed(() => {
  return messages.value.filter(m => m.contentType === 'FILE' && !m.recall).reverse()
})

function formatSize(bytes: number) {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

function getFileName(file: any) {
  try {
    const meta = JSON.parse(file.meta)
    return meta.filename || '未知文件'
  } catch {
    return '文件'
  }
}

function getFileSize(file: any) {
  try {
    const meta = JSON.parse(file.meta)
    return Number(meta.size) || 0
  } catch {
    return 0
  }
}

function downloadFile(url: string) {
  window.open(url, '_blank')
}

function formatTime(time: string) {
  return dayjs(time).format('MM-DD HH:mm')
}

// 切换会话时加载历史消息
watch(currentConversation, (conv) => {
  if (conv) {
    userHasScrolledUp.value = false // 重置滚动状态
    if (conv.type === 'GROUP') {
      groupStore.fetchMembers(conv.typeId)
    }
    messageStore.fetchMessages(conv.id).then(() => {
      nextTick(() => {
        scrollToBottom()
        // 记录初始滚动高度
        if (messageListRef.value) {
          (messageListRef.value as any).__oldScrollHeight = messageListRef.value.scrollHeight
        }
      })
      // 标记已读：用当前会话最后一条消息的 ID 通知后端
      const msgs = messageStore.messages
      if (msgs.length > 0) {
        const lastMsg = msgs[msgs.length - 1]
        messageStore.markAsRead(conv.id, lastMsg.id)
      }
    })
  }
}, { immediate: true })

// 监听消息变化，自动滚动到底部
// - 新消息来自别人且用户在底部时，自动滚动
// - 新消息来自自己时，自动滚动（发送后立即可见）
// - 仅当用户主动向上滑超过100px查看历史时才停止自动滚动
watch(messages, (newVal, oldVal) => {
  const addedCount = newVal.length - (oldVal?.length || 0)
  if (addedCount <= 0) return
  // 如果用户没有主动上滑，或者新消息是自己发的，自动滚动
  const lastMsg = newVal[newVal.length - 1]
  if (!userHasScrolledUp.value || lastMsg.senderId === currentUserId.value) {
    nextTick(() => scrollToBottom())
  }
}, { flush: 'post' })

let searchTimer: any = null
function doSearch() {
  if (searchTimer) clearTimeout(searchTimer)
  if (!searchKeyword.value.trim()) {
    searchResults.value = []
    return
  }
  searchTimer = setTimeout(async () => {
    searching.value = true
    try {
      const res = await messageApi.search({
        conversationId: currentConversation.value!.id,
        keyword: searchKeyword.value
      })
      searchResults.value = res.data || []
    } finally {
      searching.value = false
    }
  }, 500)
}

function highlightKeyword(text: string, keyword: string) {
  if (!keyword || !text) return text
  const reg = new RegExp(`(${keyword})`, 'gi')
  return text.replace(reg, '<span class="highlight">$1</span>')
}

function jumpToMessage(msgId: number) {
  const el = document.getElementById(`msg-${msgId}`)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'center' })
    el.classList.add('jump-highlight')
    setTimeout(() => el.classList.remove('jump-highlight'), 2000)
  } else {
    ElMessage.info('该消息较久，请向上滚动加载更多')
  }
}

function getConvName(conv: Conversation) {
  if (conv.type === 'GROUP') return conv.groupInfo?.name || '群组'
  return conv.targetUser?.nickname || conv.targetUser?.username || '用户'
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

function getAvatarBgFromUser(user: any) {
  if (!user) return '#ccc'
  const colors = ['#DBEAFE', '#D1FAE5', '#FCE7F3', '#FEF3C7', '#FEE2E2', '#F3E8FF']
  const name = user.username || ''
  return colors[name.charCodeAt(0) % colors.length]
}

async function sendMessage() {
  if ((!inputText.value.trim() && !pendingImages.value.length) || !currentConversation.value) return
  
  // 构建元数据
  const metaObj: any = {}
  if (atAllActive.value) metaObj.atAll = true
  if (atUserIds.value.length > 0) {
    // 过滤掉已经在文本中被删掉的@
    const activeAtIds = atUserIds.value.filter(id => {
      const member = groupStore.members.find(m => m.userId === id)
      return member && inputText.value.includes(`@${member.user.username}`)
    })
    if (activeAtIds.length > 0) metaObj.atUserIds = activeAtIds
  }

  const meta = Object.keys(metaObj).length > 0 ? JSON.stringify(metaObj) : null
  
  atAllActive.value = false
  atUserIds.value = []
  const replyId = replyingTo.value?.id || undefined
  replyingTo.value = null

  // 构建消息内容：图文混合 or 纯文本
  let content: string
  let contentType = 'TEXT'
  if (pendingImages.value.length > 0) {
    const blocks: any[] = []
    if (inputText.value.trim()) {
      blocks.push({ type: 'text', content: inputText.value.trim() })
    }
    pendingImages.value.forEach(url => blocks.push({ type: 'image', url }))
    content = JSON.stringify(blocks)
    contentType = 'MIXED'
  } else {
    content = inputText.value.trim()
  }

  const savedText = inputText.value
  inputText.value = ''
  pendingImages.value = []

  try {
    await messageStore.sendMessage({
      conversationId: currentConversation.value.id,
      content,
      contentType,
      meta: meta || undefined,
      replyId
    })
    await nextTick()
    scrollToBottom()
  } catch (_e) {
    inputText.value = savedText
  }
}

async function handleImageChange(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  uploading.value = true
  try {
    const fd = new FormData()
    fd.append('file', file)
    const res = await messageApi.uploadImage(fd)
    if (res.data?.url) {
      pendingImages.value.push(res.data.url)
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

function removePendingImage(idx: number) {
  pendingImages.value.splice(idx, 1)
}

// 将每行文本单独渲染，返回 HTML 数组
function getRenderedLines(): string {
  if (!inputText.value) return ''
  const lines = inputText.value.split('\n')
  return lines.map(line => {
    if (!line.trim()) return '<div class="md-line empty-line"></div>'
    try {
      const html = marked.parse(line) as string
      return `<div class="md-line">${html}</div>`
    } catch {
      return `<div class="md-line">${escapeHtml(line)}</div>`
    }
  }).join('')
}

function escapeHtml(text: string): string {
  return text.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
}

function onEditorBlur() {
  isEditing.value = false
}

function startEditing() {
  if (!isEditing.value) {
    isEditing.value = true
    nextTick(() => {
      textareaRef.value?.focus()
    })
  }
}

function onEditorInput(e: Event) {
  // 1. 更新高度
  editorHeight.value = editorHeightStyle.value

  // 2. @ 功能检测
  const target = e.target as HTMLTextAreaElement
  const pos = target.selectionStart
  const currentVal = target.value
  const textBefore = currentVal.substring(0, pos)
  
  console.log('[Mention] Input detected. textBefore:', textBefore, 'pos:', pos)
  
  if (currentConversation.value?.type === 'GROUP') {
    const lastAtIdx = textBefore.lastIndexOf('@')
    console.log('[Mention] lastAtIdx:', lastAtIdx)
    
    if (lastAtIdx !== -1) {
      const searchPart = textBefore.substring(lastAtIdx + 1)
      console.log('[Mention] searchPart:', searchPart)
      
      // 如果@后面没有空格且没有换行，且长度适中，开启选择
      if (!searchPart.includes(' ') && !searchPart.includes('\n') && searchPart.length < 15) {
        showMentionList.value = true
        mentionSearch.value = searchPart
        mentionIndex.value = 0
        console.log('[Mention] List shown. Search:', searchPart, 'Members count:', filteredMembers.value.length)
      } else {
        showMentionList.value = false
      }
    } else {
      showMentionList.value = false
    }
  } else {
    showMentionList.value = false
  }
}

function handleKeydown(e: KeyboardEvent) {
  if (showMentionList.value) {
    if (e.key === 'ArrowUp') {
      e.preventDefault()
      mentionIndex.value = (mentionIndex.value - 1 + filteredMembers.value.length) % (filteredMembers.value.length || 1)
      return
    } else if (e.key === 'ArrowDown') {
      e.preventDefault()
      mentionIndex.value = (mentionIndex.value + 1) % (filteredMembers.value.length || 1)
      return
    } else if (e.key === 'Enter' || e.key === 'Tab') {
      e.preventDefault()
      const member = filteredMembers.value[mentionIndex.value]
      if (member) insertMention(member)
      return
    } else if (e.key === 'Escape') {
      showMentionList.value = false
      return
    }
  }

  // 正常发送消息 (排除 shift, ctrl, alt, meta)
  if (e.key === 'Enter' && !e.shiftKey && !e.ctrlKey && !e.altKey && !e.metaKey) {
    e.preventDefault()
    sendMessage()
  }
}

function insertMention(member: any) {
  const target = textareaRef.value
  if (!target) return
  
  const pos = target.selectionStart
  const textBefore = inputText.value.substring(0, pos)
  const textAfter = inputText.value.substring(pos)
  
  const lastAtIdx = textBefore.lastIndexOf('@')
  const mentionName = member.isAll ? '全体成员' : member.user.username
  const newTextBefore = textBefore.substring(0, lastAtIdx) + `@${mentionName} `
  
  inputText.value = newTextBefore + textAfter
  showMentionList.value = false
  
  if (member.isAll) {
    atAllActive.value = true
  } else {
    if (!atUserIds.value.includes(member.userId)) {
      atUserIds.value.push(member.userId)
    }
  }

  nextTick(() => {
    target.focus()
    const newPos = newTextBefore.length
    target.setSelectionRange(newPos, newPos)
  })
}

function scrollToBottom() {
  if (messageListRef.value) {
    isProgrammaticScroll.value = true
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    // 滚动完成后清除标志，允许正常检测用户滚动
    setTimeout(() => { isProgrammaticScroll.value = false }, 100)
  }
}

function onMessageScroll() {
  if (!messageListRef.value || isProgrammaticScroll.value) return
  const el = messageListRef.value
  const distToBottom = el.scrollHeight - el.scrollTop - el.clientHeight
  // 用户滚动到距离底部超过100px，认为是主动向上滑动
  userHasScrolledUp.value = distToBottom > 100

  // 滚动到顶部且有更多消息时，加载更多
  if (el.scrollTop < 100 && !loadingMore.value && hasMore.value && messages.value.length > 0) {
    const oldestMsg = messages.value[0]
    if (oldestMsg) {
      messageStore.fetchMessages(currentConversation.value!.id, oldestMsg.id).then(() => {
        nextTick(() => {
          // 恢复滚动位置（保持用户看到的相对位置不变）
          if (messageListRef.value) {
            const newHeight = messageListRef.value.scrollHeight
            const oldHeight = (messageListRef.value as any).__oldScrollHeight || newHeight
            messageListRef.value.scrollTop = newHeight - oldHeight
            ;(messageListRef.value as any).__oldScrollHeight = newHeight
          }
        })
      })
    }
  }
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
.header-btn.active { color: var(--primary, #4F46E5); background: var(--surface-3, #F3F4F6); }
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
.chat-window-main { flex: 1; display: flex; overflow: hidden; position: relative; }
.chat-messages-container { flex: 1; display: flex; flex-direction: column; position: relative; overflow: hidden; }

/* 打字提示动画 */
.typing-indicator {
  display: flex; gap: 8px; align-items: center; align-self: flex-start;
  margin: 4px 0 16px 0; animation: typing-in 0.2s ease-out;
}
@keyframes typing-in {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}
.typing-avatar {
  width: 28px; height: 28px; border-radius: 50%;
  background: linear-gradient(135deg, #4F46E5, #7C3AED);
  display: flex; align-items: center; justify-content: center;
  font-size: 11px;
}
.typing-bubble {
  background: #F3F4F6; padding: 8px 12px; border-radius: 16px;
  border-bottom-left-radius: 4px; display: flex; gap: 4px;
}
.typing-bubble .dot {
  width: 4px; height: 4px; background: #9CA3AF; border-radius: 50%;
  animation: typing-dots 1.4s infinite ease-in-out both;
}
.typing-bubble .dot:nth-child(1) { animation-delay: -0.32s; }
.typing-bubble .dot:nth-child(2) { animation-delay: -0.16s; }
@keyframes typing-dots {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1.0); }
}

.scroll-bottom-btn {
  position: absolute; bottom: 20px; right: 20px; padding: 8px 16px;
  background: var(--primary, #4F46E5); color: white; border: none; border-radius: 20px;
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3); cursor: pointer;
  display: flex; align-items: center; gap: 6px; font-size: 12px; font-weight: 600; z-index: 10;
}
.scroll-bottom-btn svg { width: 14px; height: 14px; }

.chat-sidebar {
  width: 300px; border-left: 1px solid var(--border, #E5E7EB); background: var(--surface-1, #fff);
  display: flex; flex-direction: column; z-index: 20;
}
.sidebar-header-tabs {
  display: flex; height: 48px; border-bottom: 1px solid var(--border, #E5E7EB);
  padding: 0 8px; align-items: center; gap: 4px;
}
.sidebar-tab {
  padding: 0 16px; height: 32px; display: flex; align-items: center;
  font-size: 13px; font-weight: 500; cursor: pointer; border-radius: 6px;
  color: var(--text-secondary, #6B7280); transition: all 0.2s;
}
.sidebar-tab:hover { background: var(--surface-3, #F3F4F6); }
.sidebar-tab.active { background: var(--surface-3, #F3F4F6); color: var(--primary, #4F46E5); }
.close-sidebar-icon {
  margin-left: auto; width: 28px; height: 28px; border: none; background: none;
  cursor: pointer; color: var(--text-secondary); border-radius: 4px;
}
.close-sidebar-icon:hover { background: var(--surface-3); }

.sidebar-content { flex: 1; overflow-y: auto; display: flex; flex-direction: column; }
.search-content { padding: 12px; }
.search-input-wrap { margin-bottom: 12px; }
.search-results { flex: 1; overflow-y: auto; }
.search-result-item {
  padding: 12px; border-radius: 8px; cursor: pointer; transition: background 0.2s;
  border: 1px solid transparent; margin-bottom: 8px;
}
.search-result-item:hover { background: var(--surface-2, #F9FAFB); border-color: var(--border); }
.search-res-header { display: flex; justify-content: space-between; margin-bottom: 4px; font-size: 11px; }
.search-res-sender { font-weight: 600; color: var(--text-primary); }
.search-res-time { color: var(--text-muted); }
.search-res-text { font-size: 12px; color: var(--text-secondary); line-height: 1.5; word-break: break-word; }
.search-res-text :deep(.highlight) { background: rgba(251, 191, 36, 0.3); color: #B45309; font-weight: 600; border-radius: 2px; }

.jump-highlight { animation: jumpPulse 2s ease-out; }
@keyframes jumpPulse {
  0% { background: rgba(79, 70, 229, 0.1); }
  50% { background: rgba(79, 70, 229, 0.3); }
  100% { background: transparent; }
}
.file-list { flex: 1; overflow-y: auto; padding: 12px; }
.file-item {
  display: flex; align-items: center; gap: 10px; padding: 10px;
  border-radius: 8px; transition: background 0.2s; cursor: default;
}
.file-item:hover { background: var(--surface-2, #F9FAFB); }
.file-icon { font-size: 24px; }
.file-info { flex: 1; min-width: 0; }
.file-name { font-size: 13px; font-weight: 500; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.file-meta { font-size: 11px; color: var(--text-muted, #9CA3AF); margin-top: 2px; }
.file-download {
  width: 28px; height: 28px; border: 1px solid var(--border, #E5E7EB); background: white;
  border-radius: 6px; cursor: pointer; color: var(--text-secondary, #6B7280);
}
.file-download:hover { background: var(--primary, #4F46E5); color: white; border-color: var(--primary, #4F46E5); }
.empty-sidebar { text-align: center; color: var(--text-muted, #9CA3AF); margin-top: 40px; font-size: 13px; }

/* Transitions */
.fade-enter-active, .fade-leave-active { transition: opacity 0.3s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

.slide-right-enter-active, .slide-right-leave-active { transition: transform 0.3s ease; }
.slide-right-enter-from, .slide-right-leave-to { transform: translateX(100%); }
.loading-more {
  display: flex; align-items: center; justify-content: center; gap: 6px;
  padding: 8px 0; font-size: 11px; color: var(--text-secondary, #6B7280);
}
.loading-more.no-more { color: var(--text-secondary, #9CA3AF); }
.loading-spinner {
  display: inline-block; width: 12px; height: 12px;
  border: 2px solid var(--border, #E5E7EB); border-top-color: var(--primary, #4F46E5);
  border-radius: 50%; animation: spin 0.8s linear infinite;
}
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
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
.pending-images { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 8px; }
.pending-img-wrap { position: relative; }
.pending-img { width: 60px; height: 60px; object-fit: cover; border-radius: 8px; border: 1px solid var(--border, #E5E7EB); }
.pending-img-remove {
  position: absolute; top: -6px; right: -6px; width: 18px; height: 18px;
  background: rgba(0,0,0,0.6); color: white; border: none; border-radius: 50%;
  font-size: 10px; cursor: pointer; display: flex; align-items: center; justify-content: center; line-height: 1;
}
.pending-img-remove:hover { background: rgba(239,68,68,0.8); }
.input-action-btn.md-active { color: var(--primary, #4F46E5) !important; background: #EEF2FF !important; }

/* Obsidian 风格 Markdown 编辑器 */
.md-editor-wrap {
  flex: 1; border: 1px solid var(--border, #E5E7EB);
  border-radius: 12px; background: var(--surface-2, #F9FAFB);
  max-height: 200px; overflow-y: auto;
  box-sizing: border-box; display: flex; flex-direction: column;
}

/* @ 提及列表样式 */
.mention-list-popover {
  position: absolute; bottom: 100%; left: 0; margin-bottom: 8px;
  width: 180px; max-height: 240px; overflow-y: auto;
  background: white; border: 1px solid var(--border, #E5E7EB);
  border-radius: 12px; box-shadow: 0 10px 25px rgba(0,0,0,0.1);
  z-index: 1000; padding: 6px;
}

.mention-item {
  display: flex; align-items: center; gap: 10px; padding: 8px 10px;
  border-radius: 8px; cursor: pointer; transition: background 0.1s;
}
.mention-item:hover, .mention-item.active { background: var(--surface-3, #F3F4F6); }
.mention-item.active { outline: 1px solid var(--primary); }
.mention-avatar {
  width: 24px; height: 24px; border-radius: 50%; display: flex;
  align-items: center; justify-content: center; font-size: 10px; color: #4F46E5; font-weight: 600;
}
.mention-name { font-size: 13px; color: var(--text-primary); font-weight: 500; }
.md-editor-wrap:focus-within {
  border-color: var(--primary, #4F46E5);
  background: white;
}

/* 渲染层、编辑层与占位符共用基础尺寸样式，确保完全对齐 */
.md-render-layer, .md-editor, .md-placeholder {
  padding: 8px 12px; font-size: 13px; line-height: 24px;
  box-sizing: border-box;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
  word-break: break-word; white-space: pre-wrap;
  border: none; outline: none; margin: 0; display: block;
  vertical-align: top;
}

.md-render-layer {
  color: var(--text-secondary, #6B7280);
}
.md-placeholder {
  color: var(--text-secondary, #6B7280);
}
.md-render-layer :deep(.md-line) { line-height: 24px; }
.md-render-layer :deep(.md-line.empty-line) { height: 24px; }
.md-render-layer :deep(h1), .md-render-layer :deep(h2), .md-render-layer :deep(h3) { font-weight: 600; margin: 0; color: var(--text-primary, #111827); }
.md-render-layer :deep(h1) { font-size: 16px; }
.md-render-layer :deep(h2) { font-size: 15px; }
.md-render-layer :deep(h3) { font-size: 14px; }
.md-render-layer :deep(code) { background: rgba(0,0,0,0.08); padding: 1px 4px; border-radius: 3px; font-size: 12px; font-family: monospace; }
.md-render-layer :deep(pre) { background: rgba(0,0,0,0.08); padding: 8px; border-radius: 6px; overflow-x: auto; margin: 4px 0; }
.md-render-layer :deep(pre code) { background: none; padding: 0; }
.md-render-layer :deep(p) { margin: 0; }
.md-render-layer :deep(strong) { font-weight: 600; color: var(--text-primary, #111827); }
.md-render-layer :deep(em) { font-style: italic; }
.md-render-layer :deep(a) { color: var(--primary, #4F46E5); text-decoration: underline; }
.md-render-layer :deep(ul), .md-render-layer :deep(ol) { margin: 0; padding-left: 20px; }
.md-render-layer :deep(blockquote) { margin: 0; padding-left: 8px; color: var(--text-secondary); border-left: 2px solid var(--primary); }

/* 编辑层 textarea */
.md-editor {
  width: 100%; color: var(--text-primary, #111827); background: transparent;
  border: none; outline: none; resize: none; overflow: hidden;
}
.md-editor::placeholder {
  color: var(--text-secondary, #6B7280) !important;
}


</style>
