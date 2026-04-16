<template>
  <div class="main-layout">
    <Sidebar :active="activeTab" @change="activeTab = $event" />

    <div class="conversation-panel">
      <div class="panel-header">
        <el-input v-model="searchKeyword" placeholder="搜索会话" prefix-icon="Search" clearable />
        <el-button :icon="Plus" @click="showCreateGroup = true" />
      </div>
      <ConversationList :keyword="searchKeyword" @select="selectConversation" />
    </div>

    <div class="chat-panel">
      <template v-if="currentConversation">
        <div class="chat-header">
          <div class="chat-title">
            <el-avatar :size="36" :src="currentConversation.targetUser?.avatar">
              {{ currentConversation.targetUser?.username?.[0] }}
            </el-avatar>
            <div class="title-info">
              <span class="username">{{ currentConversation.targetUser?.username }}</span>
              <span class="status" :class="currentConversation.targetUser?.userStatus?.toLowerCase()">
                {{ statusText(currentConversation.targetUser?.userStatus) }}
              </span>
            </div>
          </div>
          <div class="chat-actions">
            <el-button :icon="MoreFilled" circle />
          </div>
        </div>

        <div class="message-list" ref="messageListRef">
          <MessageBubble
            v-for="msg in messages"
            :key="msg.id"
            :message="msg"
            :is-mine="msg.senderId === currentUserId"
          />
        </div>

        <div class="chat-input">
          <el-input
            v-model="inputText"
            type="textarea"
            :rows="3"
            placeholder="输入消息... (Ctrl+Enter 发送)"
            @keydown.enter.ctrl.exact.prevent="sendMessage"
          />
          <div class="input-actions">
            <el-button @click="sendImage">图片</el-button>
            <el-button @click="sendFile">文件</el-button>
            <el-button type="primary" @click="sendMessage" :disabled="!inputText.trim()">
              发送
            </el-button>
          </div>
        </div>
      </template>

      <div v-else class="empty-state">
        <div class="empty-icon">💬</div>
        <p>选择一个会话开始聊天</p>
      </div>
    </div>

    <!-- 创建群组弹窗 -->
    <el-dialog v-model="showCreateGroup" title="创建群组" width="400px">
      <el-form :model="groupForm">
        <el-form-item label="群名称">
          <el-input v-model="groupForm.name" placeholder="请输入群名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateGroup = false">取消</el-button>
        <el-button type="primary" @click="createGroup">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, onMounted, onUnmounted } from 'vue'
import { Plus, MoreFilled } from '@element-plus/icons-vue'
import { useConversationStore } from '@/stores/conversation'
import { useMessageStore } from '@/stores/message'
import { useGroupStore } from '@/stores/group'
import { ElMessage } from 'element-plus'
import wsClient from '@/utils/websocket'
import Sidebar from '@/components/Sidebar.vue'
import ConversationList from '@/components/ConversationList.vue'
import MessageBubble from '@/components/MessageBubble.vue'
import type { Conversation } from '@/types'

const activeTab = ref('message')
const searchKeyword = ref('')
const inputText = ref('')
const messageListRef = ref<HTMLElement>()
const showCreateGroup = ref(false)
const groupForm = ref({ name: '' })

const conversationStore = useConversationStore()
const messageStore = useMessageStore()
const groupStore = useGroupStore()

const currentConversation = computed(() => conversationStore.currentConversation)
const messages = computed(() => messageStore.messages)
const currentUserId = computed(() => Number(localStorage.getItem('userId')))

onMounted(async () => {
  await conversationStore.fetchConversations()
  messageStore.initWsListener()
  wsClient.on('message', (msg: any) => {
    if (msg && msg.conversationId) {
      conversationStore.updateLastMessage(msg.conversationId, msg.content, msg.createTime)
    }
  })
})

onUnmounted(() => {
  wsClient.off('message', () => {})
  wsClient.close()
})

function statusText(status?: string) {
  const map: Record<string, string> = {
    ONLINE: '在线', OFFLINE: '离线', AWAY: '离开', DND: '勿扰'
  }
  return map[status || ''] || '离线'
}

async function selectConversation(conv: Conversation) {
  conversationStore.setCurrentConversation(conv)
  await messageStore.fetchMessages(conv.id)
  scrollToBottom()
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

async function createGroup() {
  if (!groupForm.value.name.trim()) {
    ElMessage.warning('请输入群名称')
    return
  }
  await groupStore.createGroup(groupForm.value.name, [])
  ElMessage.success('群组创建成功')
  showCreateGroup.value = false
  groupForm.value.name = ''
}

function sendImage() { ElMessage.info('图片发送功能开发中') }
function sendFile() { ElMessage.info('文件发送功能开发中') }
</script>

<style scoped>
.main-layout {
  display: flex;
  height: 100vh;
  background: var(--bg);
}
.conversation-panel {
  width: 300px;
  background: var(--surface-1);
  border-right: 1px solid var(--border);
  display: flex;
  flex-direction: column;
}
.panel-header {
  padding: 12px;
  display: flex;
  gap: 8px;
  border-bottom: 1px solid var(--border);
}
.chat-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--surface-1);
  min-width: 0;
}
.chat-header {
  height: 60px;
  padding: 0 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--border);
  flex-shrink: 0;
}
.chat-title { display: flex; align-items: center; gap: 12px; }
.title-info { display: flex; flex-direction: column; }
.username { font-weight: 600; font-size: 15px; }
.status { font-size: 12px; color: #10B981; }
.status.offline { color: #9CA3AF; }
.status.away { color: #F59E0B; }
.status.dnd { color: #EF4444; }
.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.chat-input {
  padding: 12px 16px;
  border-top: 1px solid var(--border);
  flex-shrink: 0;
}
.input-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 8px;
}
.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #9CA3AF;
}
.empty-icon { font-size: 64px; margin-bottom: 16px; }
</style>
