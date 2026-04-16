<template>
  <div class="contacts-page">
    <div class="contacts-header">
      <el-input v-model="search" placeholder="搜索联系人" prefix-icon="Search" clearable />
      <el-button type="primary" @click="showAddFriend = true">添加好友</el-button>
    </div>

    <div class="contacts-tabs">
      <el-tabs v-model="tab">
        <el-tab-pane label="好友" name="friends">
          <div class="friend-list">
            <ContactItem
              v-for="friend in filteredFriends"
              :key="friend.id"
              :user="friend"
              @chat="startChat(friend)"
              @delete="deleteFriend(friend.id)"
            />
            <div v-if="!filteredFriends.length" class="empty">暂无好友</div>
          </div>
        </el-tab-pane>

        <el-tab-pane name="requests">
          <template #label>
            请求
            <el-badge v-if="pendingCount > 0" :value="pendingCount" class="tab-badge" />
          </template>
          <div class="request-list">
            <div v-for="req in friendStore.requests" :key="req.id" class="request-item">
              <el-avatar :src="req.fromUser?.avatar" :size="40">
                {{ req.fromUser?.username?.[0] }}
              </el-avatar>
              <div class="req-info">
                <span class="req-name">{{ req.fromUser?.username }}</span>
                <span class="req-reason">{{ req.reason || '请求添加好友' }}</span>
              </div>
              <div v-if="req.status === 'PENDING'" class="req-actions">
                <el-button type="primary" size="small" @click="handleRequest(req.id, 'agree')">同意</el-button>
                <el-button size="small" @click="handleRequest(req.id, 'reject')">拒绝</el-button>
              </div>
              <el-tag v-else :type="req.status === 'AGREED' ? 'success' : 'info'" size="small">
                {{ req.status === 'AGREED' ? '已同意' : '已拒绝' }}
              </el-tag>
            </div>
            <div v-if="!friendStore.requests.length" class="empty">暂无好友请求</div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 添加好友 -->
    <el-dialog v-model="showAddFriend" title="添加好友" width="420px">
      <el-input v-model="searchUser" placeholder="搜索用户名/手机号" @input="doSearchUser" />
      <div class="search-results">
        <div v-for="u in searchResults" :key="u.id" class="search-item">
          <el-avatar :src="u.avatar" :size="36">{{ u.username?.[0] }}</el-avatar>
          <span class="s-name">{{ u.username }}</span>
          <el-button type="primary" size="small" @click="sendRequest(u.id)">添加</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useFriendStore } from '@/stores/friend'
import { useConversationStore } from '@/stores/conversation'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ContactItem from '@/components/ContactItem.vue'
import type { User } from '@/types'

const friendStore = useFriendStore()
const conversationStore = useConversationStore()
const router = useRouter()

const search = ref('')
const tab = ref('friends')
const showAddFriend = ref(false)
const searchUser = ref('')
const searchResults = ref<User[]>([])

onMounted(() => {
  friendStore.fetchFriends()
  friendStore.fetchRequests()
})

const filteredFriends = computed(() =>
  friendStore.friends.filter(f =>
    !search.value || f.username.includes(search.value)
  )
)

const pendingCount = computed(() =>
  friendStore.requests.filter(r => r.status === 'PENDING').length
)

async function startChat(user: User) {
  await conversationStore.createConversation('SINGLE', user.id)
  router.push('/conversation')
}

async function deleteFriend(id: number) {
  await friendStore.deleteFriend(id)
  ElMessage.success('已删除好友')
}

async function handleRequest(id: number, action: 'agree' | 'reject') {
  await friendStore.handleRequest(id, action)
  ElMessage.success(action === 'agree' ? '已同意' : '已拒绝')
}

async function doSearchUser() {
  if (!searchUser.value.trim()) { searchResults.value = []; return }
  searchResults.value = await friendStore.searchUsers(searchUser.value)
}

async function sendRequest(userId: number) {
  await friendStore.sendRequest(userId)
  ElMessage.success('好友请求已发送')
}
</script>

<style scoped>
.contacts-page { height: 100%; display: flex; flex-direction: column; padding: 16px; }
.contacts-header { display: flex; gap: 8px; margin-bottom: 16px; }
.contacts-tabs { flex: 1; overflow: auto; }
.friend-list, .request-list { display: flex; flex-direction: column; gap: 4px; }
.request-item { display: flex; align-items: center; gap: 12px; padding: 12px; background: var(--surface-2); border-radius: 8px; }
.req-info { flex: 1; display: flex; flex-direction: column; }
.req-name { font-weight: 600; }
.req-reason { font-size: 12px; color: var(--text-secondary); }
.req-actions { display: flex; gap: 8px; }
.tab-badge { margin-left: 4px; }
.search-results { margin-top: 12px; display: flex; flex-direction: column; gap: 8px; }
.search-item { display: flex; align-items: center; gap: 12px; padding: 8px; }
.s-name { flex: 1; }
.empty { text-align: center; color: #9CA3AF; padding: 32px; }
</style>
