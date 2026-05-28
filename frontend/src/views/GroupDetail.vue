<template>
  <div class="group-detail">
    <!-- 左侧成员列表 -->
    <div class="member-list-panel">
      <div class="panel-header">
        <h2>{{ group?.name || '群组' }}</h2>
      </div>

      <div class="tabs">
        <button class="tab" :class="{ active: tab === 'members' }" @click="tab = 'members'">成员</button>
        <button class="tab" :class="{ active: tab === 'chat' }" @click="tab = 'chat'">群聊记录</button>
      </div>

      <div class="search-bar">
        <input type="text" v-model="memberSearch" placeholder="搜索成员..." />
      </div>

      <div v-if="tab === 'members'" class="member-list">
        <div
          v-for="m in filteredMembers"
          :key="m.userId"
          class="member-item"
        >
          <div class="member-avatar" :style="{ background: getAvatarBg(m) }">
            {{ m.user?.username?.[0] || '?' }}
          </div>
          <div class="member-info">
            <div class="member-name">
              {{ m.user?.username }}
              <el-tag v-if="m.role === 'OWNER'" type="danger" size="small">群主</el-tag>
              <el-tag v-else-if="m.role === 'ADMIN'" type="warning" size="small">管理员</el-tag>
            </div>
            <div class="member-status" :class="{ online: m.user?.userStatus === 'ONLINE' }">
              {{ m.user?.userStatus === 'ONLINE' ? '在线' : '离线' }}
            </div>
          </div>
        </div>
        <div v-if="!filteredMembers.length" class="empty">暂无成员</div>
      </div>

      <div v-else class="chat-record-list">
        <div v-if="!chatRecords.length" class="empty">暂无聊天记录</div>
        <div v-for="msg in chatRecords" :key="msg.id" class="chat-record-item">
          <div class="record-avatar">{{ msg.sender?.username?.[0] }}</div>
          <div class="record-info">
            <div class="record-name">{{ msg.sender?.username }}</div>
            <div class="record-content">{{ msg.content }}</div>
          </div>
          <div class="record-time">{{ formatTime(msg.createTime) }}</div>
        </div>
      </div>
    </div>

    <!-- 右侧详情 -->
    <div class="group-info-panel">
      <div class="info-header">
        <h3>{{ group?.name }}</h3>
        <div class="member-count">{{ members.length }} 位成员</div>
      </div>

      <div class="tabs">
        <button class="tab" :class="{ active: infoTab === 'info' }" @click="infoTab = 'info'">群信息</button>
        <button class="tab" :class="{ active: infoTab === 'manage' }" @click="infoTab = 'manage'">成员管理</button>
        <button v-if="canManage" class="tab" :class="{ active: infoTab === 'requests' }" @click="infoTab = 'requests'">入群申请</button>
      </div>

      <div v-if="infoTab === 'requests' && canManage" class="manage-content">
        <div v-if="!joinRequests.length" class="empty">暂无待处理申请</div>
        <div v-for="req in joinRequests" :key="req.id" class="request-item">
          <div class="request-info">
            <div class="request-user">{{ req.user?.username || '用户' + req.userId }}</div>
            <div class="request-reason">理由: {{ req.reason || '无' }}</div>
            <div class="request-time">{{ formatDate(req.createTime) }}</div>
          </div>
          <div v-if="req.status === 'PENDING'" class="request-actions">
            <el-button size="small" type="success" @click="handleJoinRequest(req.id, 'agree')">同意</el-button>
            <el-button size="small" type="danger" @click="handleJoinRequest(req.id, 'reject')">拒绝</el-button>
          </div>
          <div v-else class="request-status">
            {{ req.status === 'AGREED' ? '已同意' : '已拒绝' }}
          </div>
        </div>
      </div>
      <div v-else-if="infoTab === 'info'" class="info-content">
        <div class="info-section">
          <div class="info-item">
            <span class="info-label">群公告</span>
            <span class="info-value announcement">{{ group?.announcement || '暂无公告' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">群号</span>
            <span class="info-value">{{ groupId }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">群主</span>
            <span class="info-value">{{ ownerName || '用户' + group?.ownerId }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">类型</span>
            <span class="info-value">{{ group?.type === 'PRIVATE' ? '私密群' : '公开群' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">创建时间</span>
            <span class="info-value">{{ group?.createTime ? formatDate(group.createTime) : '-' }}</span>
          </div>
        </div>
        <div v-if="canManage" class="info-actions">
          <button class="btn-primary" @click="router.push(`/group/${groupId}/settings`)">编辑群信息</button>
          <div v-if="group?.ownerId === currentUserId" class="owner-actions">
            <el-button type="success" plain class="action-btn" @click="handleShowInvite">邀请好友加入</el-button>
            <el-button type="warning" plain class="action-btn" @click="handleMuteGroup(!(group?.muteAll === 1))">
              {{ group?.muteAll === 1 ? '取消全员禁言' : '全员禁言' }}
            </el-button>
            <el-button type="primary" plain class="action-btn" @click="showTransferDialog = true">转让群主</el-button>
            <el-button type="danger" plain class="action-btn" @click="handleDissolve">解散群组</el-button>
          </div>
          <div v-else-if="currentMember?.role !== 'OWNER'" class="member-actions-bottom">
            <el-button type="danger" plain class="action-btn" @click="handleLeave">退出群组</el-button>
          </div>
        </div>
      </div>
      <div v-else-if="infoTab === 'manage'" class="manage-content">
        <div v-if="canManage" class="invite-section">
          <el-input v-model="inviteUsername" placeholder="输入用户名添加成员" @keydown.enter="handleInvite" style="margin-bottom:8px" />
          <el-button type="primary" size="small" @click="handleInvite">添加</el-button>
        </div>
        <div class="member-manage-list">
          <div v-for="m in members" :key="m.userId" class="member-manage-item">
            <div class="member-avatar small" :style="{ background: getAvatarBg(m) }">
              {{ m.user?.username?.[0] || '?' }}
            </div>
            <div class="member-info">
              <div class="member-name">
                {{ m.user?.username }}
                <el-tag v-if="m.role === 'OWNER'" type="danger" size="small">群主</el-tag>
                <el-tag v-else-if="m.role === 'ADMIN'" type="warning" size="small">管理员</el-tag>
              </div>
            </div>
            <div v-if="canManage && m.role !== 'OWNER'" class="member-actions">
              <el-button v-if="m.role !== 'ADMIN'" size="small" type="warning" @click="handleSetAdmin(m, true)">设管理</el-button>
              <el-button v-else size="small" @click="handleSetAdmin(m, false)">取消管理</el-button>
              <el-button size="small" type="danger" @click="handleRemoveMember(m)">移除</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 对话框移到此处 -->
    <!-- 转让群主对话框 -->
    <el-dialog v-model="showTransferDialog" title="转让群主" width="400px">
      <el-alert title="注意：转让后您将失去管理权限，且操作不可撤销。" type="warning" :closable="false" style="margin-bottom:16px" />
      <el-select v-model="newOwnerId" placeholder="选择新群主" style="width:100%">
        <el-option v-for="m in members.filter(m => m.userId !== currentUserId)" :key="m.userId" :label="m.user?.username" :value="m.userId" />
      </el-select>
      <template #footer>
        <el-button @click="showTransferDialog = false">取消</el-button>
        <el-button type="primary" @click="handleTransfer">确定转让</el-button>
      </template>
    </el-dialog>

    <!-- 邀请对话框 -->
    <el-dialog v-model="showInviteDialog" title="邀请好友加入群组" width="400px" center>
      <div class="invite-dialog-content">
        <div class="qr-code-placeholder">
          <img :src="`https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=${encodeURIComponent(inviteUrl)}`" alt="QR Code" />
        </div>
        <p class="invite-tip">扫描二维码或复制下方链接发送给好友</p>
        <div class="invite-link-box">
          <el-input v-model="inviteUrl" readonly>
            <template #append>
              <el-button @click="copyInviteUrl">复制</el-button>
            </template>
          </el-input>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useGroupStore } from '@/stores/group'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import type { GroupMember } from '@/types'

const route = useRoute()
const router = useRouter()
const groupStore = useGroupStore()
const userStore = useUserStore()

const tab = ref('members')
const infoTab = ref('info')
const memberSearch = ref('')
const chatRecords = ref<any[]>([])
const inviteUsername = ref('')
const joinRequests = ref<any[]>([])
const showTransferDialog = ref(false)
const showInviteDialog = ref(false)
const inviteUrl = ref('')
const newOwnerId = ref<number | null>(null)

const groupId = computed(() => Number(route.params.id))
const members = computed(() => groupStore.members)
const group = computed(() => groupStore.groups.find(g => g.id === groupId.value))

const currentUserId = computed(() => userStore.currentUser?.id)
const currentMember = computed(() => members.value.find(m => m.userId === currentUserId.value))
const canManage = computed(() => currentMember.value && (currentMember.value.role === 'OWNER' || currentMember.value.role === 'ADMIN'))
const ownerName = computed(() => {
  const owner = members.value.find(m => m.role === 'OWNER')
  return owner?.user?.username
})

const filteredMembers = computed(() => {
  if (!memberSearch.value) return members.value
  return members.value.filter(m =>
    m.user?.username?.includes(memberSearch.value)
  )
})

onMounted(async () => {
  await groupStore.fetchGroups()
  await groupStore.fetchMembers(groupId.value)
  if (canManage.value) {
    loadJoinRequests()
  }
})

async function loadJoinRequests() {
  const res = await groupStore.getJoinRequests(groupId.value)
  joinRequests.value = res.data || []
}

async function handleJoinRequest(requestId: number, action: 'agree' | 'reject') {
  try {
    await groupStore.handleJoinRequest(requestId, action)
    ElMessage.success('操作成功')
    await loadJoinRequests()
    if (action === 'agree') await groupStore.fetchMembers(groupId.value)
  } catch (e: any) {
    ElMessage.error(e?.message || '操作失败')
  }
}

async function handleShowInvite() {
  try {
    const res = await request.get(`/group/invite-token/${groupId.value}?operatorId=${currentUserId.value}`)
    const token = res.data
    inviteUrl.value = `${window.location.origin}/join/group/${token}`
    showInviteDialog.value = true
  } catch (e: any) {
    ElMessage.error('获取邀请链接失败')
  }
}

function copyInviteUrl() {
  navigator.clipboard.writeText(inviteUrl.value)
  ElMessage.success('链接已复制到剪贴板')
}

function getAvatarBg(m: GroupMember) {
  const colors = ['#DBEAFE', '#D1FAE5', '#FCE7F3', '#FEF3C7', '#FEE2E2', '#F3E8FF']
  const name = m.user?.username || ''
  return colors[name.charCodeAt(0) % colors.length]
}

function formatTime(time?: string) {
  if (!time) return ''
  const d = new Date(time)
  const now = new Date()
  const diff = now.getTime() - d.getTime()
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  return d.toLocaleDateString()
}

function formatDate(date?: string) {
  if (!date) return '-'
  return new Date(date).toLocaleDateString()
}

async function handleInvite() {
  if (!inviteUsername.value.trim()) return
  try {
    // 通过用户名搜索用户ID
    const userRes = await fetchUserByUsername(inviteUsername.value.trim())
    if (!userRes) {
      ElMessage.error('用户不存在')
      return
    }
    await groupStore.inviteMembers(groupId.value, [userRes.id])
    ElMessage.success('添加成功')
    inviteUsername.value = ''
    await groupStore.fetchMembers(groupId.value)
  } catch (e: any) {
    ElMessage.error(e?.message || '添加失败')
  }
}

async function fetchUserByUsername(username: string): Promise<any> {
  const { userApi } = await import('@/api/user')
  const res = await userApi.search(username)
  const users = res.data || []
  return users.find((u: any) => u.username === username)
}

async function handleSetAdmin(m: GroupMember, isAdmin: boolean) {
  try {
    await groupStore.setAdmin(groupId.value, m.userId, isAdmin)
    ElMessage.success(isAdmin ? '已设为管理员' : '已取消管理员')
  } catch (e: any) {
    ElMessage.error(e?.message || '操作失败')
  }
}

async function handleMuteGroup(mute: boolean) {
  try {
    await groupStore.muteGroup(groupId.value, mute)
    ElMessage.success(mute ? '已开启全员禁言' : '已取消全员禁言')
    await groupStore.fetchGroupDetail(groupId.value)
  } catch (e: any) {
    ElMessage.error(e?.message || '操作失败')
  }
}

async function handleTransfer() {
  if (!newOwnerId.value) return
  try {
    await groupStore.transferOwnership(groupId.value, newOwnerId.value)
    ElMessage.success('转让成功')
    showTransferDialog.value = false
    await groupStore.fetchMembers(groupId.value)
    await groupStore.fetchGroupDetail(groupId.value)
  } catch (e: any) {
    ElMessage.error(e?.message || '转让失败')
  }
}

async function handleLeave() {
  try {
    await ElMessageBox.confirm('确定要退出该群组吗？', '提示', { type: 'warning' })
    await groupStore.leaveGroup(groupId.value)
    ElMessage.success('已退出群组')
    router.push('/main')
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error('退出失败')
  }
}

async function handleDissolve() {
  try {
    await ElMessageBox.confirm('确定要解散该群组吗？此操作不可恢复！', '警告', { type: 'danger' })
    await groupStore.dissolveGroup(groupId.value)
    ElMessage.success('群组已解散')
    router.push('/main')
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

async function handleRemoveMember(m: GroupMember) {
  try {
    await groupStore.removeMember(groupId.value, m.userId)
    ElMessage.success('已移除')
  } catch (e: any) {
    ElMessage.error(e?.message || '移除失败')
  }
}
</script>

<style scoped>
.group-detail {
  display: flex;
  flex: 1;
  overflow: hidden;
}
.member-list-panel {
  width: 360px;
  border-right: 1px solid var(--border, #E5E7EB);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}
.panel-header {
  padding: 16px;
  border-bottom: 1px solid var(--border, #E5E7EB);
}
.panel-header h2 { font-size: 16px; font-weight: 600; margin: 0; }
.tabs {
  display: flex;
  padding: 8px 12px;
  gap: 6px;
  border-bottom: 1px solid var(--border, #E5E7EB);
}
.tab {
  flex: 1;
  height: 30px;
  border: none;
  background: #F3F4F6;
  border-radius: 6px;
  font-size: 12px;
  color: #6B7280;
  cursor: pointer;
}
.tab.active { background: #4F46E5; color: white; }
.search-bar {
  padding: 8px 12px;
  border-bottom: 1px solid var(--border, #E5E7EB);
}
.search-bar input {
  width: 100%;
  height: 32px;
  padding: 0 10px;
  border: 1px solid var(--border, #E5E7EB);
  border-radius: 6px;
  font-size: 12px;
  outline: none;
  background: var(--surface-2, #F9FAFB);
}
.member-list,
.chat-record-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}
.member-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 8px;
  border-radius: 8px;
  cursor: pointer;
}
.member-item:hover { background: #F3F4F6; }
.member-avatar {
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
.member-avatar.small { width: 32px; height: 32px; font-size: 12px; }
.member-info { flex: 1; }
.member-name {
  font-size: 13px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;
}
.member-status { font-size: 11px; color: #9CA3AF; }
.member-status.online { color: #10B981; }
.chat-record-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px 8px;
  border-bottom: 1px solid #F3F4F6;
}
.record-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #DBEAFE;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #2563EB;
  flex-shrink: 0;
}
.record-info { flex: 1; min-width: 0; }
.record-name { font-size: 12px; font-weight: 500; margin-bottom: 2px; }
.record-content { font-size: 12px; color: #6B7280; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.record-time { font-size: 10px; color: #9CA3AF; flex-shrink: 0; }
.empty { text-align: center; color: #9CA3AF; padding: 32px; font-size: 13px; }

/* 右侧群详情 */
.group-info-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}
.info-header {
  text-align: center;
  padding: 24px 24px 0;
}
.info-header h3 { font-size: 18px; font-weight: 600; margin-bottom: 4px; }
.member-count { font-size: 13px; color: #6B7280; }
.info-content {
  padding: 16px 24px;
  flex: 1;
}
.info-section {
  background: var(--surface-2, #F9FAFB);
  border-radius: 12px;
  padding: 16px;
}
.info-item {
  display: flex;
  padding: 12px 0;
  border-bottom: 1px solid #E5E7EB;
}
.info-item:last-child { border-bottom: none; }
.info-label { width: 80px; font-size: 13px; color: #6B7280; flex-shrink: 0; }
.info-value { flex: 1; font-size: 13px; }
.info-value.announcement { color: #111827; white-space: pre-wrap; }
.info-actions { margin-top: 24px; }
.btn-primary {
  width: 100%;
  height: 40px;
  background: #4F46E5;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  margin-bottom: 12px;
}
.owner-actions, .member-actions-bottom {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.action-btn { width: 100%; margin: 0 !important; }

.invite-dialog-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 10px 0;
}
.qr-code-placeholder {
  width: 150px;
  height: 150px;
  background: #f3f4f6;
  border-radius: 12px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.qr-code-placeholder img { width: 100%; height: 100%; object-fit: cover; }
.invite-tip { font-size: 13px; color: #6b7280; margin-bottom: 16px; }
.invite-link-box { width: 100%; }

/* 成员管理 */
.manage-content {
  padding: 16px 24px;
  flex: 1;
  overflow-y: auto;
}
.invite-section {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  align-items: center;
}
.member-manage-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.member-manage-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  background: var(--surface-2, #F9FAFB);
  border-radius: 8px;
}
.member-manage-item .member-info { flex: 1; }
.member-actions {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
}
.request-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: var(--surface-2, #F9FAFB);
  border-radius: 8px;
  margin-bottom: 8px;
}
.request-info { flex: 1; min-width: 0; }
.request-user { font-weight: 600; font-size: 13px; margin-bottom: 2px; }
.request-reason { font-size: 12px; color: var(--text-secondary, #6B7280); margin-bottom: 2px; }
.request-time { font-size: 11px; color: var(--text-muted, #9CA3AF); }
.request-status { font-size: 12px; color: var(--text-muted, #9CA3AF); font-weight: 500; }
</style>
