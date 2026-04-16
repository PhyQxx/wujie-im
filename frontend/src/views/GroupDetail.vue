<template>
  <div class="group-detail">
    <div class="group-header">
      <el-avatar :size="60" :src="group?.avatar" :icon="UserFilled">{{ group?.name?.[0] }}</el-avatar>
      <div class="group-info">
        <h2>{{ group?.name }}</h2>
        <p>{{ members.length }} 名成员</p>
        <el-tag v-if="group?.type === 'PRIVATE'" size="small" type="warning">私密群</el-tag>
        <el-tag v-else size="small" type="success">公开群</el-tag>
      </div>
    </div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="成员">
        <GroupMemberList :group-id="Number(route.params.id)" />
      </el-tab-pane>
      <el-tab-pane label="公告">
        <div class="announcement-section">
          <div v-if="!editingAnnouncement">
            <div class="announcement-content">{{ group?.announcement || '暂无公告' }}</div>
            <el-button v-if="canManage" size="small" type="primary" @click="editingAnnouncement = true" style="margin-top:12px">
              {{ group?.announcement ? '编辑公告' : '发布公告' }}
            </el-button>
          </div>
          <div v-else>
            <el-input v-model="announcementText" type="textarea" :rows="4" placeholder="请输入群公告..." />
            <div style="margin-top:8px">
              <el-button size="small" type="primary" @click="saveAnnouncement">保存</el-button>
              <el-button size="small" @click="editingAnnouncement = false">取消</el-button>
            </div>
          </div>
        </div>
      </el-tab-pane>
      <el-tab-pane label="入群申请" v-if="canManage">
        <div class="request-list">
          <div v-for="req in joinRequests" :key="req.id" class="request-item">
            <el-avatar :size="36">{{ req.userId }}</el-avatar>
            <div class="req-info">
              <span>用户ID: {{ req.userId }}</span>
              <span class="req-reason">{{ req.reason || '申请加入' }}</span>
            </div>
            <div v-if="req.status === 'PENDING'" class="req-actions">
              <el-button type="success" size="small" @click="handleRequest(req.id, 'AGREE')">同意</el-button>
              <el-button type="danger" size="small" @click="handleRequest(req.id, 'REJECT')">拒绝</el-button>
            </div>
            <el-tag v-else :type="req.status === 'AGREED' ? 'success' : 'danger'" size="small">
              {{ req.status === 'AGREED' ? '已同意' : '已拒绝' }}
            </el-tag>
          </div>
          <div v-if="!joinRequests.length" class="empty">暂无入群申请</div>
        </div>
      </el-tab-pane>
      <el-tab-pane label="群设置" v-if="canManage">
        <el-form label-width="80px" style="max-width:400px">
          <el-form-item label="群名称">
            <el-input v-model="editForm.name" />
          </el-form-item>
          <el-form-item label="群公告">
            <el-input v-model="editForm.announcement" type="textarea" :rows="3" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveGroupInfo">保存</el-button>
          </el-form-item>
        </el-form>
        <div style="margin-top:24px">
          <el-button type="danger" @click="handleDissolve">解散群聊</el-button>
          <el-button @click="handleLeave">退出群聊</el-button>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useGroupStore } from '@/stores/group'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'
import GroupMemberList from '@/components/GroupMemberList.vue'

const route = useRoute()
const router = useRouter()
const groupStore = useGroupStore()
const userStore = useUserStore()

const activeTab = ref('members')
const editingAnnouncement = ref(false)
const announcementText = ref('')
const joinRequests = ref<any[]>([])
const editForm = ref({ name: '', announcement: '' })

const groupId = computed(() => Number(route.params.id))
const members = computed(() => groupStore.members)
const group = computed(() => {
  const g = groupStore.groups.find(g => g.id === groupId.value)
  if (g) {
    editForm.value.name = g.name || ''
    editForm.value.announcement = g.announcement || ''
    announcementText.value = g.announcement || ''
  }
  return g
})

const currentUserId = computed(() => userStore.currentUser?.id)
const currentMember = computed(() => members.value.find(m => m.userId === currentUserId.value))
const canManage = computed(() => currentMember.value && (currentMember.value.role === 'OWNER' || currentMember.value.role === 'ADMIN'))

onMounted(async () => {
  await groupStore.fetchGroups()
  await groupStore.fetchMembers(groupId.value)
  await loadJoinRequests()
})

watch(activeTab, async (tab) => {
  if (tab === '入群申请') await loadJoinRequests()
})

async function loadJoinRequests() {
  const res = await groupStore.getJoinRequests(groupId.value)
  joinRequests.value = res.data || []
}

async function handleRequest(requestId: number, action: string) {
  await groupStore.handleJoinRequest(requestId, action as 'agree' | 'reject')
  ElMessage.success(action === 'AGREE' ? '已同意' : '已拒绝')
  await loadJoinRequests()
}

async function saveAnnouncement() {
  await groupStore.updateGroup(groupId.value, { announcement: announcementText.value })
  editingAnnouncement.value = false
  ElMessage.success('公告已更新')
}

async function saveGroupInfo() {
  await groupStore.updateGroup(groupId.value, { name: editForm.value.name, announcement: editForm.value.announcement })
  ElMessage.success('群信息已保存')
}

async function handleLeave() {
  await ElMessageBox.confirm('确定要退出该群吗？', '提示', { type: 'warning' })
  await groupStore.leaveGroup(groupId.value)
  ElMessage.success('已退出群聊')
  router.push('/conversation')
}

async function handleDissolve() {
  await ElMessageBox.confirm('确定要解散该群吗？此操作不可恢复！', '警告', { type: 'error' })
  await groupStore.dissolveGroup(groupId.value)
  ElMessage.success('群已解散')
  router.push('/conversation')
}
</script>

<style scoped>
.group-detail { padding: 24px; height: 100%; overflow: auto; }
.group-header { display: flex; align-items: center; gap: 16px; margin-bottom: 24px; }
.group-info h2 { font-size: 20px; font-weight: 700; margin-bottom: 4px; }
.group-info p { color: var(--text-secondary); font-size: 14px; margin-bottom: 6px; }
.announcement-content { padding: 16px; background: var(--surface-2); border-radius: 8px; color: var(--text-secondary); white-space: pre-wrap; }
.request-list { display: flex; flex-direction: column; gap: 8px; }
.request-item { display: flex; align-items: center; gap: 12px; padding: 12px; background: var(--surface-2); border-radius: 8px; }
.req-info { flex: 1; display: flex; flex-direction: column; }
.req-reason { font-size: 12px; color: var(--text-muted); }
.req-actions { display: flex; gap: 8px; }
.empty { text-align: center; color: var(--text-muted); padding: 32px; }
</style>
