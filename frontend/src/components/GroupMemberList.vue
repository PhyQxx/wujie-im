<template>
  <div class="group-member-list">
    <div class="list-header">
      <span>成员列表 ({{ members.length }})</span>
      <el-button v-if="canManage" size="small" type="primary" @click="showInvite = true">添加成员</el-button>
    </div>
    <div class="search-bar">
      <el-input v-model="searchKey" placeholder="搜索成员" prefix-icon="Search" size="small" clearable />
    </div>
    <div class="members">
      <div v-for="m in filteredMembers" :key="m.id" class="member-item">
        <el-avatar :size="36" :src="m.user?.avatar">{{ m.user?.username?.[0] }}</el-avatar>
        <div class="member-info">
          <span class="member-name">{{ m.user?.username }}</span>
          <el-tag size="small" :type="roleType(m.role)">{{ roleLabel(m.role) }}</el-tag>
          <el-tag v-if="m.muted" type="warning" size="small">已禁言</el-tag>
        </div>
        <div v-if="canManage && m.role !== 'OWNER' && m.userId !== currentUserId" class="member-actions">
          <el-dropdown trigger="click">
            <el-button size="small" link>···</el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="toggleMute(m)">
                  {{ m.muted ? '解除禁言' : '禁言' }}
                </el-dropdown-item>
                <el-dropdown-item @click="toggleAdmin(m)">
                  {{ m.role === 'ADMIN' ? '撤销管理员' : '设为管理员' }}
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleRemove(m)" style="color: #EF4444">
                  移出群聊
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>

    <!-- 添加成员对话框 -->
    <el-dialog v-model="showInvite" title="添加成员" width="480px">
      <el-input v-model="searchUserKey" placeholder="搜索用户" @input="searchUsers" />
      <div class="user-list">
        <div v-for="u in searchResults" :key="u.id" class="user-item">
          <el-avatar :size="32" :src="u.avatar">{{ u.username?.[0] }}</el-avatar>
          <span class="u-name">{{ u.username }}</span>
          <el-button size="small" type="primary" @click="inviteUser(u.id)">邀请</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useGroupStore, type GroupMember } from '@/stores/group'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const props = defineProps<{ groupId: number }>()
const groupStore = useGroupStore()
const userStore = useUserStore()

const showInvite = ref(false)
const searchKey = ref('')
const searchUserKey = ref('')
const searchResults = ref<any[]>([])

const members = computed(() => groupStore.members)
const currentUserId = computed(() => userStore.currentUser?.id)
const currentMember = computed(() => members.value.find(m => m.userId === currentUserId.value))
const canManage = computed(() => currentMember.value && (currentMember.value.role === 'OWNER' || currentMember.value.role === 'ADMIN'))

const filteredMembers = computed(() => {
  if (!searchKey.value) return members.value
  return members.value.filter(m => m.user?.username?.includes(searchKey.value))
})

onMounted(() => groupStore.fetchMembers(props.groupId))

function roleLabel(role: string) {
  const map: Record<string, string> = { OWNER: '群主', ADMIN: '管理员', MEMBER: '成员' }
  return map[role] || role
}

function roleType(role: string): any {
  const map: Record<string, string> = { OWNER: 'danger', ADMIN: 'warning', MEMBER: '' }
  return map[role] || ''
}

async function toggleMute(m: GroupMember) {
  if (m.muted) {
    await groupStore.unmuteMember(props.groupId, m.userId)
    ElMessage.success('已解除禁言')
  } else {
    await groupStore.muteMember(props.groupId, m.userId, 0)
    ElMessage.success('已禁言')
  }
}

async function toggleAdmin(m: GroupMember) {
  await groupStore.setAdmin(props.groupId, m.userId, m.role !== 'ADMIN')
  ElMessage.success(m.role === 'ADMIN' ? '已撤销管理员' : '已设为管理员')
}

async function handleRemove(m: GroupMember) {
  await ElMessageBox.confirm(`确定要将 ${m.user?.username} 移出群聊吗？`, '提示', { type: 'warning' })
  await groupStore.removeMember(props.groupId, m.userId)
  ElMessage.success('已移出')
}

async function searchUsers() {
  if (!searchUserKey.value.trim()) { searchResults.value = []; return }
  searchResults.value = (await request.get('/user/search', { params: { keyword: searchUserKey.value } })).data || []
}

async function inviteUser(userId: number) {
  await groupStore.inviteMembers(props.groupId, [userId])
  ElMessage.success('已添加')
  searchResults.value = searchResults.value.filter(u => u.id !== userId)
}
</script>

<style scoped>
.group-member-list { padding: 12px; }
.list-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; font-weight: 600; }
.search-bar { margin-bottom: 12px; }
.members { display: flex; flex-direction: column; gap: 8px; max-height: 500px; overflow-y: auto; }
.member-item { display: flex; align-items: center; gap: 10px; padding: 8px; background: var(--surface-2); border-radius: 8px; }
.member-info { flex: 1; display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.member-name { font-weight: 500; }
.member-actions { display: flex; gap: 4px; }
.user-list { margin-top: 12px; display: flex; flex-direction: column; gap: 8px; max-height: 300px; overflow-y: auto; }
.user-item { display: flex; align-items: center; gap: 10px; padding: 6px; }
.u-name { flex: 1; }
</style>
