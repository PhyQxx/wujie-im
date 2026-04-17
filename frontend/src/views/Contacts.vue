<template>
  <div class="contacts-page">
    <!-- 左侧联系人面板 -->
    <div class="contacts-panel">
      <div class="panel-header">
        <h2>联系人</h2>
        <el-dropdown trigger="click" @command="handleAddCommand">
          <button class="btn-sm btn-primary-sm">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path></svg>
            添加
          </button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="friend">添加好友</el-dropdown-item>
              <el-dropdown-item command="group">创建群组</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>

      <div class="panel-tabs">
        <button class="tab-btn" :class="{ active: tab === 'friends' }" @click="tab = 'friends'">好友</button>
        <button class="tab-btn" :class="{ active: tab === 'groups' }" @click="tab = 'groups'">群组</button>
        <button class="tab-btn" :class="{ active: tab === 'blacklist' }" @click="tab = 'blacklist'">黑名单</button>
      </div>

      <div class="panel-search">
        <input type="text" v-model="search" placeholder="搜索联系人..." @input="onSearch" />
      </div>

      <div class="contacts-list">
        <!-- 好友申请 -->
        <div v-if="pendingRequests.length > 0" class="friend-requests">
          <div class="group-title">
            好友申请
            <span class="request-badge">{{ pendingRequests.length }}</span>
          </div>
          <div v-for="req in pendingRequests" :key="req.id" class="request-item">
            <div class="contact-avatar-wrap">
              <div class="contact-avatar" :style="{ background: getAvatarBg(req.fromUser), color: getAvatarColor(req.fromUser) }">
                {{ req.fromUser?.username?.[0] }}
              </div>
            </div>
            <div class="request-info">
              <div class="request-name">{{ req.fromUser?.username }}</div>
              <div class="request-hint">{{ req.reason || '请求添加好友' }}</div>
            </div>
            <div class="request-actions">
              <button class="btn-accept" @click="handleRequest(req.id, 'agree')">接受</button>
              <button class="btn-decline" @click="handleRequest(req.id, 'reject')">拒绝</button>
            </div>
          </div>
        </div>

        <!-- 好友列表 -->
        <template v-if="tab === 'friends'">
          <div v-for="(friends, letter) in groupedFriends" :key="letter" class="contact-group">
            <div class="group-title">{{ letter }}</div>
            <div
              v-for="friend in friends"
              :key="friend.id"
              class="contact-item"
              :class="{ active: selectedFriend?.id === friend.id }"
              @click="selectFriend(friend)"
            >
              <div class="contact-avatar-wrap">
                <div class="contact-avatar" :style="{ background: getAvatarBg(friend), color: getAvatarColor(friend) }">
                  {{ friend.username?.[0] }}
                </div>
                <span class="online-dot" :class="friend.userStatus === 'ONLINE' ? 'online' : 'offline'"></span>
              </div>
              <div class="contact-info">
                <div class="contact-name">{{ friend.username }}</div>
                <div class="contact-status-text">{{ getStatusText(friend) }}</div>
              </div>
            </div>
          </div>
        </template>

        <!-- 群组列表 -->
        <template v-else-if="tab === 'groups'">
          <div v-for="group in groups" :key="group.id" class="contact-group">
            <div
              class="contact-item"
              :class="{ active: selectedGroup?.id === group.id }"
              @click="selectGroup(group)"
            >
              <div class="contact-avatar-wrap">
                <div class="contact-avatar" :style="{ background: '#DBEAFE', color: '#2563EB' }">
                  {{ group.name?.[0] }}
                </div>
              </div>
              <div class="contact-info">
                <div class="contact-name">{{ group.name }}</div>
                <div class="contact-status-text">{{ group.memberCount || 0 }} 位成员</div>
              </div>
            </div>
          </div>
        </template>

        <!-- 黑名单 -->
        <template v-else>
          <div v-for="user in blacklistedUsers" :key="user.id" class="contact-group">
            <div class="contact-item">
              <div class="contact-avatar-wrap">
                <div class="contact-avatar" :style="{ background: getAvatarBg(user), color: getAvatarColor(user) }">
                  {{ user.username?.[0] }}
                </div>
              </div>
              <div class="contact-info">
                <div class="contact-name">{{ user.username }}</div>
                <div class="contact-status-text">黑名单</div>
              </div>
            </div>
          </div>
        </template>

        <div v-if="!filteredFriends.length && tab === 'friends'" class="empty-list">暂无好友</div>
        <div v-if="!groups.length && tab === 'groups'" class="empty-list">暂无群组</div>
      </div>
    </div>

    <!-- 右侧详情面板 -->
    <div class="detail-panel">
      <template v-if="selectedFriend && tab === 'friends'">
        <div class="detail-header">
          <div class="detail-avatar" :style="{ background: getAvatarBg(selectedFriend), color: getAvatarColor(selectedFriend) }">
            {{ selectedFriend.username?.[0] }}
          </div>
          <div class="detail-info">
            <h2>{{ selectedFriend.username }}</h2>
            <p>{{ getStatusText(selectedFriend) }}</p>
          </div>
          <div class="detail-actions">
            <button class="btn-sm btn-primary-sm" @click="startChat">
              <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"></path></svg>
              发消息
            </button>
          </div>
        </div>
        <div class="detail-tabs">
          <button class="detail-tab" :class="{ active: detailTab === 'profile' }" @click="detailTab = 'profile'">个人资料</button>
          <button class="detail-tab" :class="{ active: detailTab === 'groups' }" @click="detailTab = 'groups'">共同群聊</button>
          <button class="detail-tab" :class="{ active: detailTab === 'chat' }" @click="detailTab = 'chat'">聊天记录</button>
        </div>
        <div class="detail-content">
          <div class="info-section">
            <h4>基本信息</h4>
            <div class="info-card">
              <div class="info-row">
                <span class="info-label">
                  <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M10 6H5a2 2 0 00-2 2v9a2 2 0 002 2h14a2 2 0 002-2V8a2 2 0 00-2-2h-5m-4 0V5a2 2 0 114 0v1m-4 0a2 2 0 104 0m-5 8a2 2 0 100-4 2 2 0 000 4zm0 0c1.306 0 2.417.835 2.83 2M9 14a3.001 3.001 0 00-2.83 2M15 11h3m-3 4h2"></path></svg>
                  用户名
                </span>
                <span class="info-value">@{{ selectedFriend.username }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">
                  <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"></path></svg>
                  手机
                </span>
                <span class="info-value">{{ selectedFriend.phone || '未填写' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">
                  <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
                  注册时间
                </span>
                <span class="info-value">{{ selectedFriend.createTime ? formatDate(selectedFriend.createTime) : '-' }}</span>
              </div>
            </div>
          </div>
        </div>
      </template>

      <template v-else-if="selectedGroup && tab === 'groups'">
        <div class="detail-header">
          <div class="detail-avatar" style="background:#DBEAFE;color:#2563EB;">
            {{ selectedGroup.name?.[0] }}
          </div>
          <div class="detail-info">
            <h2>{{ selectedGroup.name }}</h2>
            <p>{{ selectedGroup.memberCount || 0 }} 位成员</p>
          </div>
          <div class="detail-actions">
            <button class="btn-sm btn-primary-sm" @click="enterGroupChat">
              <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"></path></svg>
              发消息
            </button>
          </div>
        </div>
        <div class="detail-tabs">
          <button class="detail-tab active">群信息</button>
        </div>
        <div class="detail-content">
          <div class="info-section">
            <h4>群组信息</h4>
            <div class="info-card">
              <div class="info-row">
                <span class="info-label">群号</span>
                <span class="info-value">{{ selectedGroup.id }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">群主</span>
                <span class="info-value">用户{{ selectedGroup.ownerId }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">类型</span>
                <span class="info-value">{{ selectedGroup.type === 'PRIVATE' ? '私密群' : '公开群' }}</span>
              </div>
            </div>
          </div>
        </div>
      </template>

      <div v-else class="detail-empty">
        <span>选择一个联系人查看详情</span>
      </div>
    </div>

    <!-- 添加联系人弹窗 -->
    <el-dialog v-model="showAddDialog" title="添加联系人" width="400px">
      <el-input v-model="searchUser" placeholder="搜索用户名/手机号" @input="doSearchUser" />
      <div class="search-results">
        <div v-for="u in searchResults" :key="u.id" class="search-item">
          <div class="contact-avatar small" :style="{ background: getAvatarBg(u), color: getAvatarColor(u) }">
            {{ u.username?.[0] }}
          </div>
          <span class="s-name">{{ u.username }}</span>
          <el-button type="primary" size="small" @click="sendRequest(u.id)">添加</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 创建群组弹窗 -->
    <el-dialog v-model="showCreateGroupDialog" title="创建群组" width="420px">
      <el-form :model="groupForm" label-width="80px">
        <el-form-item label="群名称">
          <el-input v-model="groupForm.name" placeholder="请输入群名称" />
        </el-form-item>
        <el-form-item label="群类型">
          <el-select v-model="groupForm.type" style="width:100%">
            <el-option label="公开群" value="PUBLIC" />
            <el-option label="私密群" value="PRIVATE" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateGroupDialog = false">取消</el-button>
        <el-button type="primary" @click="createGroup">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useFriendStore } from '@/stores/friend'
import { useGroupStore } from '@/stores/group'
import { useConversationStore } from '@/stores/conversation'
import { useUserStore } from '@/stores/user'
import { groupApi } from '@/api/group'
import { ElMessage } from 'element-plus'
import type { User, Group } from '@/types'

const router = useRouter()
const friendStore = useFriendStore()
const groupStore = useGroupStore()
const conversationStore = useConversationStore()
const userStore = useUserStore()

const tab = ref('friends')
const detailTab = ref('profile')
const search = ref('')
const showAddDialog = ref(false)
const showCreateGroupDialog = ref(false)
const searchUser = ref('')
const searchResults = ref<User[]>([])
const selectedFriend = ref<User | null>(null)
const selectedGroup = ref<Group | null>(null)
const groupForm = ref({ name: '', type: 'PUBLIC' })

onMounted(async () => {
  await friendStore.fetchFriends()
  await friendStore.fetchRequests()
  await groupStore.fetchGroups()
})

const pendingRequests = computed(() =>
  friendStore.requests.filter(r => r.status === 'PENDING')
)

const filteredFriends = computed(() => {
  if (!search.value) return friendStore.friends
  return friendStore.friends.filter(f =>
    f.username?.includes(search.value)
  )
})

const groupedFriends = computed(() => {
  const groups: Record<string, User[]> = {}
  filteredFriends.value.forEach(f => {
    const letter = (f.username?.[0] || '#').toUpperCase()
    if (!groups[letter]) groups[letter] = []
    groups[letter].push(f)
  })
  return groups
})

const groups = computed(() => groupStore.groups)

const blacklistedUsers = ref<User[]>([])
let searchTimer: ReturnType<typeof setTimeout> | null = null

function selectFriend(friend: User) {
  selectedFriend.value = friend
  selectedGroup.value = null
}

function selectGroup(group: Group) {
  selectedGroup.value = group
  selectedFriend.value = null
}

function getAvatarBg(user: any) {
  const colors = ['#D1FAE5', '#FCE7F3', '#FEF3C7', '#FEE2E2', '#DBEAFE', '#F3E8FF']
  return colors[(user.username?.charCodeAt(0) || 0) % colors.length]
}

function getAvatarColor(user: any) {
  const colors = ['#059669', '#DB2777', '#D97706', '#DC2626', '#2563EB', '#7C3AED']
  return colors[(user.username?.charCodeAt(0) || 0) % colors.length]
}

function getStatusText(user: any) {
  if (!user) return ''
  if (user.userStatus === 'ONLINE') return '在线'
  if (user.userStatus === 'AWAY') return '离开'
  if (user.userStatus === 'DND') return '勿扰'
  return '离线'
}

function formatDate(date?: string) {
  if (!date) return '-'
  return new Date(date).toLocaleDateString()
}

async function onSearch() {
  // 本地过滤
}

function handleAddCommand(command: string) {
  if (command === 'friend') showAddDialog.value = true
  else if (command === 'group') showCreateGroupDialog.value = true
}

async function handleRequest(id: number, action: 'agree' | 'reject') {
  await friendStore.handleRequest(id, action)
  ElMessage.success(action === 'agree' ? '已同意' : '已拒绝')
}

async function startChat() {
  if (!selectedFriend.value) return
  await conversationStore.createConversation('SINGLE', selectedFriend.value.id)
  router.push('/conversation')
}

async function enterGroupChat() {
  if (!selectedGroup.value) return
  await conversationStore.createConversation('GROUP', selectedGroup.value.id)
  router.push('/conversation')
}

async function doSearchUser() {
  if (searchTimer) clearTimeout(searchTimer)
  if (!searchUser.value.trim()) { searchResults.value = []; return }
  searchTimer = setTimeout(async () => {
    const currentUserId = Number(localStorage.getItem('userId'))
    const results = await friendStore.searchUsers(searchUser.value)
    searchResults.value = (results || []).filter((u: User) => u.id !== currentUserId)
  }, 300)
}

async function sendRequest(userId: number) {
  await friendStore.sendRequest(userId)
  ElMessage.success('好友请求已发送')
}

async function createGroup() {
  if (!groupForm.value.name.trim()) {
    ElMessage.warning('请输入群名称')
    return
  }
  await groupApi.create({
    name: groupForm.value.name.trim(),
    type: groupForm.value.type,
    ownerId: Number(localStorage.getItem('userId'))
  })
  ElMessage.success('群组创建成功')
  showCreateGroupDialog.value = false
  groupForm.value = { name: '', type: 'PUBLIC' }
  await groupStore.fetchGroups()
}
</script>

<style scoped>
.contacts-page {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.contacts-panel {
  width: 340px;
  border-right: 1px solid var(--border, #E5E7EB);
  display: flex;
  flex-direction: column;
  background: var(--bg-primary, #fff);
  flex-shrink: 0;
}

.panel-header {
  padding: 16px;
  border-bottom: 1px solid var(--border, #E5E7EB);
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.panel-header h2 { font-size: 15px; font-weight: 600; }

.btn-sm {
  height: 34px;
  padding: 0 16px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.15s;
  display: flex;
  align-items: center;
  gap: 6px;
  border: none;
}
.btn-sm svg { width: 16px; height: 16px; }
.btn-primary-sm { background: var(--primary, #4F46E5); color: white; }
.btn-primary-sm:hover { background: var(--primary-hover, #4338CA); }

.panel-tabs {
  display: flex;
  gap: 4px;
  padding: 8px 16px;
  border-bottom: 1px solid var(--border, #E5E7EB);
}
.tab-btn {
  padding: 6px 12px;
  border: none;
  background: transparent;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary, #6B7280);
  cursor: pointer;
  font-family: inherit;
  transition: all 0.15s;
}
.tab-btn.active { background: var(--primary, #4F46E5); color: white; }

.panel-search {
  padding: 10px 16px;
  border-bottom: 1px solid var(--border, #E5E7EB);
}
.panel-search input {
  width: 100%;
  height: 34px;
  padding: 0 12px;
  border: 1px solid var(--border, #E5E7EB);
  border-radius: 8px;
  font-size: 13px;
  font-family: inherit;
  outline: none;
  background: var(--bg-secondary, #F9FAFB);
  color: var(--text-primary, #111827);
}
.panel-search input::placeholder { color: var(--text-muted, #9CA3AF); }

.contacts-list {
  flex: 1;
  overflow-y: auto;
}

.friend-requests {
  padding: 8px 0;
  border-bottom: 1px solid var(--border, #E5E7EB);
}
.request-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
}
.request-info { flex: 1; }
.request-name { font-size: 13px; font-weight: 500; }
.request-hint { font-size: 11px; color: var(--text-muted, #9CA3AF); margin-top: 1px; }
.request-actions { display: flex; gap: 6px; }
.btn-accept {
  height: 28px;
  padding: 0 12px;
  background: var(--primary, #4F46E5);
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  font-family: inherit;
  transition: background 0.15s;
}
.btn-accept:hover { background: var(--primary-hover, #4338CA); }
.btn-decline {
  height: 28px;
  padding: 0 10px;
  background: transparent;
  color: var(--text-muted, #9CA3AF);
  border: 1px solid var(--border, #E5E7EB);
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.15s;
}
.btn-decline:hover { border-color: var(--danger, #EF4444); color: var(--danger, #EF4444); }

.contact-group {
  padding: 8px 0;
}
.group-title {
  font-size: 11px;
  font-weight: 600;
  color: var(--text-muted, #9CA3AF);
  text-transform: uppercase;
  padding: 8px 16px 4px;
  display: flex;
  align-items: center;
  gap: 4px;
}
.request-badge {
  background: var(--warning, #F59E0B);
  color: white;
  font-size: 10px;
  padding: 1px 6px;
  border-radius: 10px;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 16px;
  cursor: pointer;
  transition: background 0.15s;
}
.contact-item:hover { background: var(--bg-secondary, #F9FAFB); }
.contact-item.active { background: var(--primary-light, #EEF2FF); }

.contact-avatar-wrap {
  position: relative;
  flex-shrink: 0;
}
.contact-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
}
.contact-avatar.small { width: 32px; height: 32px; font-size: 12px; }
.online-dot {
  position: absolute;
  bottom: 1px;
  right: 1px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  border: 2px solid var(--bg-primary, #fff);
}
.online-dot.online { background: var(--success, #10B981); }
.online-dot.offline { background: var(--text-muted, #9CA3AF); }

.contact-info { flex: 1; min-width: 0; }
.contact-name { font-size: 13px; font-weight: 500; color: var(--text-primary, #111827); }
.contact-status-text { font-size: 11px; color: var(--text-muted, #9CA3AF); margin-top: 1px; }
.empty-list { text-align: center; color: var(--text-muted, #9CA3AF); padding: 32px; font-size: 13px; }

/* 右侧详情面板 */
.detail-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--bg-secondary, #F9FAFB);
  overflow: hidden;
}
.detail-header {
  padding: 24px;
  background: var(--bg-primary, #fff);
  border-bottom: 1px solid var(--border, #E5E7EB);
  display: flex;
  align-items: center;
  gap: 16px;
}
.detail-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 600;
  flex-shrink: 0;
}
.detail-info { flex: 1; }
.detail-info h2 { font-size: 18px; font-weight: 600; }
.detail-info p { font-size: 13px; color: var(--text-secondary, #6B7280); margin-top: 2px; }
.detail-actions { display: flex; gap: 8px; }

.detail-tabs {
  display: flex;
  border-bottom: 1px solid var(--border, #E5E7EB);
  background: var(--bg-primary, #fff);
}
.detail-tab {
  padding: 12px 20px;
  border: none;
  background: transparent;
  border-bottom: 2px solid transparent;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary, #6B7280);
  cursor: pointer;
  font-family: inherit;
  transition: all 0.15s;
}
.detail-tab.active { color: var(--primary, #4F46E5); border-bottom-color: var(--primary, #4F46E5); }

.detail-content {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
}
.info-section { margin-bottom: 24px; }
.info-section h4 {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-muted, #9CA3AF);
  text-transform: uppercase;
  margin-bottom: 12px;
}
.info-card {
  background: var(--bg-primary, #fff);
  border: 1px solid var(--border, #E5E7EB);
  border-radius: 12px;
  padding: 16px;
}
.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid var(--bg-secondary, #F9FAFB);
}
.info-row:last-child { border-bottom: none; }
.info-label {
  font-size: 13px;
  color: var(--text-secondary, #6B7280);
  display: flex;
  align-items: center;
  gap: 6px;
}
.info-label svg { width: 14px; height: 14px; }
.info-value { font-size: 13px; color: var(--text-primary, #111827); font-weight: 500; }

.detail-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-muted, #9CA3AF);
  font-size: 13px;
}

/* 搜索结果 */
.search-results { margin-top: 12px; display: flex; flex-direction: column; gap: 8px; }
.search-item { display: flex; align-items: center; gap: 10px; padding: 8px 0; }
.s-name { flex: 1; font-size: 13px; }
</style>
