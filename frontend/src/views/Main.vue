<template>
  <div class="conversation-view">
    <!-- 会话列表侧边栏 -->
    <div class="sidebar">
      <div class="sidebar-header">
        <h2>消息</h2>
      </div>
      <div class="sidebar-tabs">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          class="sidebar-tab"
          :class="{ active: activeTab === tab.key }"
          @click="activeTab = tab.key"
        >
          {{ tab.label }}
        </button>
      </div>
      <div class="sidebar-search">
        <input type="text" v-model="searchKeyword" placeholder="搜索聊天..." />
      </div>
      <ConversationList :keyword="searchKeyword" :filter="activeTab" @select="selectConversation" />
    </div>

    <!-- 聊天主区 -->
    <ChatPanel ref="chatPanelRef" />

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
import { onMounted, onUnmounted, ref } from 'vue'
import { useConversationStore } from '@/stores/conversation'
import { useGroupStore } from '@/stores/group'
import { useNotificationStore } from '@/stores/notification'
import { ElMessage } from 'element-plus'
import ConversationList from '@/components/ConversationList.vue'
import ChatPanel from '@/components/ChatPanel.vue'
import wsClient from '@/utils/websocket.ts'
import type { Conversation } from '@/types'

const conversationStore = useConversationStore()
const groupStore = useGroupStore()
const notificationStore = useNotificationStore()

const chatPanelRef = ref()
const searchKeyword = ref('')
const activeTab = ref('all')
const showCreateGroup = ref(false)
const groupForm = ref({ name: '' })

const tabs = [
  { key: 'all', label: '全部' },
  { key: 'unread', label: '未读' },
  { key: 'group', label: '群聊' }
]

onMounted(async () => {
  await notificationStore.fetchNotifications()
  await conversationStore.fetchConversations()
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

async function selectConversation(conv: Conversation) {
  conversationStore.setCurrentConversation(conv)
  chatPanelRef.value?.scrollToBottom()
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
</script>

<style scoped>
.conversation-view {
  display: flex;
  flex: 1;
  overflow: hidden;
  background: var(--surface-1, #fff);
}
.sidebar {
  width: 280px;
  background: var(--surface-1, #fff);
  border-right: 1px solid var(--border, #E5E7EB);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  flex-shrink: 0;
}
.sidebar-header {
  padding: 12px 16px;
  border-bottom: 1px solid var(--border, #E5E7EB);
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.sidebar-header h2 { font-size: 15px; font-weight: 600; margin: 0; }
.add-btn {
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
.add-btn:hover { background: var(--surface-3, #F3F4F6); color: var(--primary, #4F46E5); }
.add-btn svg { width: 18px; height: 18px; }
.sidebar-tabs {
  display: flex;
  padding: 8px 12px;
  gap: 4px;
  border-bottom: 1px solid var(--border, #E5E7EB);
}
.sidebar-tab {
  flex: 1;
  padding: 6px 0;
  border: none;
  background: transparent;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  color: var(--text-secondary, #6B7280);
  cursor: pointer;
  font-family: inherit;
  transition: all 0.15s;
  text-align: center;
}
.sidebar-tab.active { background: #4F46E5; color: white; }
.sidebar-tab:not(.active) { background: #F3F4F6; color: #6B7280; }
.sidebar-search {
  padding: 8px 12px;
  border-bottom: 1px solid var(--border, #E5E7EB);
}
.sidebar-search input {
  width: 100%;
  height: 32px;
  padding: 0 12px;
  border: 1px solid var(--border, #E5E7EB);
  border-radius: 8px;
  font-size: 12px;
  font-family: inherit;
  outline: none;
  background: var(--surface-2, #F9FAFB);
  color: var(--text-primary, #111827);
}
.sidebar-search input::placeholder { color: var(--text-secondary, #6B7280); }
</style>
