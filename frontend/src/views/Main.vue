<template>
  <div class="app">
    <!-- 顶栏 -->
    <div class="topbar">
      <a href="/" class="topbar-logo">
        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M3.055 11H5a2 2 0 012 2v1a2 2 0 002 2 2 2 0 012 2v2.945M8 3.935V5.5A2.5 2.5 0 0010.5 8h.5a2 2 0 012 2 2 2 0 104 0 2 2 0 012-2h1.064M15 20.488V18a2 2 0 012-2h3.064M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
        </svg>
        無界
      </a>
      <div class="topbar-search">
        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path></svg>
        <input type="text" v-model="globalSearch" placeholder="搜索消息、联系人..." />
      </div>
      <div class="topbar-actions">
        <div class="notif-wrap">
          <button class="topbar-btn" @click="$router.push('/notification')">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"></path></svg>
          </button>
          <span v-if="notificationCount > 0" class="notif-badge"></span>
        </div>
        <div class="user-avatar" @click="$router.push('/settings')">
          {{ userStore.currentUser?.username?.[0] || '用户' }}
        </div>
      </div>
    </div>

    <div class="main-content">
      <!-- 侧边栏：会话列表 -->
      <div class="sidebar">
        <div class="sidebar-header">
          <h2>消息</h2>
          <button class="topbar-btn" @click="showCreateGroup = true" title="新建聊天">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path></svg>
          </button>
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
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useConversationStore } from '@/stores/conversation'
import { useGroupStore } from '@/stores/group'
import { useNotificationStore } from '@/stores/notification'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import ConversationList from '@/components/ConversationList.vue'
import ChatPanel from '@/components/ChatPanel.vue'
import wsClient from '@/utils/websocket.ts'

const router = useRouter()
const conversationStore = useConversationStore()
const groupStore = useGroupStore()
const notificationStore = useNotificationStore()
const userStore = useUserStore()

const chatPanelRef = ref()
const globalSearch = ref('')
const searchKeyword = ref('')
const activeTab = ref('all')
const showCreateGroup = ref(false)
const groupForm = ref({ name: '' })

const tabs = [
  { key: 'all', label: '全部' },
  { key: 'unread', label: '未读' },
  { key: 'group', label: '群聊' }
]

const notificationCount = computed(() => notificationStore.unreadCount)

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
.app {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  background: var(--bg);
  font-family: 'Inter', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}
.topbar {
  height: 56px;
  background: var(--surface-1);
  border-bottom: 1px solid var(--border);
  display: flex;
  align-items: center;
  padding: 0 16px;
  gap: 12px;
  flex-shrink: 0;
}
.topbar-logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 700;
  font-size: 18px;
  color: var(--primary);
  text-decoration: none;
  flex-shrink: 0;
}
.topbar-logo svg { width: 24px; height: 24px; }
.topbar-search {
  flex: 1;
  max-width: 360px;
  position: relative;
}
.topbar-search svg {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  width: 16px;
  height: 16px;
  color: var(--text-secondary);
  pointer-events: none;
}
.topbar-search input {
  width: 100%;
  height: 36px;
  padding: 0 12px 0 36px;
  background: var(--surface-3);
  border: 1px solid transparent;
  border-radius: 20px;
  font-size: 13px;
  font-family: inherit;
  color: var(--text-primary);
  outline: none;
  transition: border-color 0.15s;
}
.topbar-search input:focus {
  border-color: var(--primary);
  background: white;
}
.topbar-search input::placeholder { color: var(--text-secondary); }
.topbar-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-left: auto;
}
.topbar-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: transparent;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
  transition: background 0.15s;
  flex-shrink: 0;
}
.topbar-btn:hover { background: var(--surface-3); color: var(--text-primary); }
.topbar-btn svg { width: 20px; height: 20px; }
.notif-wrap { position: relative; }
.notif-badge {
  position: absolute;
  top: 5px;
  right: 5px;
  width: 8px;
  height: 8px;
  background: var(--danger);
  border-radius: 50%;
  border: 2px solid var(--surface-1);
}
.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--primary);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 13px;
  cursor: pointer;
  flex-shrink: 0;
}
.main-content {
  display: flex;
  flex: 1;
  overflow: hidden;
}
.sidebar {
  width: 280px;
  background: var(--surface-1);
  border-right: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  flex-shrink: 0;
}
.sidebar-header {
  padding: 12px 16px;
  border-bottom: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.sidebar-header h2 { font-size: 15px; font-weight: 600; }
.sidebar-tabs {
  display: flex;
  padding: 8px 12px;
  gap: 4px;
  border-bottom: 1px solid var(--border);
}
.sidebar-tab {
  flex: 1;
  padding: 6px 0;
  border: none;
  background: transparent;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  color: var(--text-secondary);
  cursor: pointer;
  font-family: inherit;
  transition: all 0.15s;
  text-align: center;
}
.sidebar-tab.active { background: var(--primary); color: white; }
.sidebar-search {
  padding: 8px 12px;
  border-bottom: 1px solid var(--border);
}
.sidebar-search input {
  width: 100%;
  height: 32px;
  padding: 0 12px;
  border: 1px solid var(--border);
  border-radius: 8px;
  font-size: 12px;
  font-family: inherit;
  outline: none;
  background: var(--surface-2);
  color: var(--text-primary);
}
.sidebar-search input::placeholder { color: var(--text-secondary); }
</style>
