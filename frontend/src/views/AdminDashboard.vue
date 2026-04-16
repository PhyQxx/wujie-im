<template>
  <div class="admin-page">
    <div class="admin-header">
      <h2>管理后台</h2>
      <el-radio-group v-model="activeTab">
        <el-radio-button value="stats">数据看板</el-radio-button>
        <el-radio-button value="users">用户管理</el-radio-button>
        <el-radio-button value="groups">群聊管理</el-radio-button>
        <el-radio-button value="robots">机器人管理</el-radio-button>
        <el-radio-button value="sensitive">敏感词</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 数据看板 -->
    <div v-if="activeTab === 'stats'" class="stats-section">
      <div class="stat-cards">
        <div class="stat-card" v-for="stat in stats" :key="stat.label">
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
      </div>
    </div>

    <!-- 用户管理 -->
    <div v-if="activeTab === 'users'" class="section">
      <div class="section-header">
        <h3>用户管理</h3>
        <el-input v-model="userSearch" placeholder="搜索用户/手机号" style="width:200px" clearable @change="loadUsers" />
      </div>
      <el-table :data="users" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="createTime" label="注册时间" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'"
              @click="toggleUserStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" @click="resetPassword(row.id)">重置密码</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 群聊管理 -->
    <div v-if="activeTab === 'groups'" class="section">
      <div class="section-header">
        <h3>群聊管理</h3>
        <el-input v-model="groupSearch" placeholder="搜索群名称" style="width:200px" clearable @change="loadGroups" />
      </div>
      <el-table :data="groups" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="群名称" />
        <el-table-column prop="ownerId" label="群主ID" />
        <el-table-column prop="type" label="类型" />
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button size="small" type="danger" @click="dissolveGroup(row)">解散</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 机器人管理 -->
    <div v-if="activeTab === 'robots'" class="section">
      <div class="section-header">
        <h3>机器人管理</h3>
        <el-input v-model="robotSearch" placeholder="搜索机器人" style="width:200px" clearable @change="loadRobots" />
      </div>
      <el-table :data="robots" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="type" label="类型" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">
              {{ row.status === 'ACTIVE' ? '运行中' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ownerId" label="创建者ID" />
      </el-table>
    </div>

    <!-- 敏感词 -->
    <div v-if="activeTab === 'sensitive'" class="section">
      <div class="section-header">
        <h3>敏感词管理</h3>
        <el-button type="primary" size="small" @click="showAddWord = true">添加敏感词</el-button>
      </div>
      <div class="word-tags">
        <el-tag v-for="w in sensitiveWords" :key="w.id" closable @close="deleteWord(w.id)">
          {{ w.word }}
        </el-tag>
        <span v-if="!sensitiveWords.length" class="empty-text">暂无敏感词</span>
      </div>
      <el-dialog v-model="showAddWord" title="添加敏感词" width="360px">
        <el-input v-model="newWord" placeholder="输入敏感词" />
        <template #footer>
          <el-button @click="showAddWord = false">取消</el-button>
          <el-button type="primary" @click="addWord">添加</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { adminApi } from '@/api/admin'
import { ElMessage } from 'element-plus'

const activeTab = ref('stats')
const loading = ref(false)

const stats = ref([
  { label: '总用户数', value: 0, key: 'totalUsers' },
  { label: '今日新增用户', value: 0, key: 'todayUsers' },
  { label: '群组数', value: 0, key: 'totalGroups' },
  { label: '今日消息', value: 0, key: 'todayMessages' },
  { label: '机器人数', value: 0, key: 'totalRobots' }
])

const users = ref<any[]>([])
const userSearch = ref('')
const groups = ref<any[]>([])
const groupSearch = ref('')
const robots = ref<any[]>([])
const robotSearch = ref('')
const sensitiveWords = ref<any[]>([])
const showAddWord = ref(false)
const newWord = ref('')

onMounted(() => loadStats())

async function loadStats() {
  try {
    const res = await adminApi.stats()
    if (res.data) {
      stats.value.forEach(s => { s.value = res.data[s.key] || 0 })
    }
  } catch (_e) {}
}

async function loadUsers() {
  loading.value = true
  try {
    const res = await adminApi.users(userSearch.value)
    users.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function loadGroups() {
  loading.value = true
  try {
    const res = await adminApi.groups(groupSearch.value)
    groups.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function loadRobots() {
  loading.value = true
  try {
    const res = await adminApi.robots(robotSearch.value)
    robots.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function loadSensitiveWords() {
  const res = await adminApi.sensitiveWords()
  sensitiveWords.value = res.data || []
}

async function toggleUserStatus(user: any) {
  const newStatus = user.status === 1 ? 0 : 1
  await adminApi.updateUserStatus(user.id, newStatus)
  user.status = newStatus
  ElMessage.success('状态已更新')
}

async function resetPassword(userId: number) {
  await adminApi.resetPassword(userId, '123456')
  ElMessage.success('密码已重置为 123456')
}

async function dissolveGroup(group: any) {
  await adminApi.dissolveGroup(group.id)
  groups.value = groups.value.filter(g => g.id !== group.id)
  ElMessage.success('群已解散')
}

// 切换 tab 时加载数据
import { watch } from 'vue'
watch(activeTab, (tab) => {
  if (tab === 'users') loadUsers()
  else if (tab === 'groups') loadGroups()
  else if (tab === 'robots') loadRobots()
  else if (tab === 'sensitive') loadSensitiveWords()
})

async function addWord() {
  if (!newWord.value.trim()) return
  await adminApi.addSensitiveWord({ word: newWord.value.trim() })
  ElMessage.success('已添加')
  showAddWord.value = false
  newWord.value = ''
  await loadSensitiveWords()
}

async function deleteWord(id: number) {
  await adminApi.deleteSensitiveWord(id)
  sensitiveWords.value = sensitiveWords.value.filter(w => w.id !== id)
  ElMessage.success('已删除')
}
</script>

<style scoped>
.admin-page { padding: 24px; height: 100%; overflow: auto; }
.admin-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.admin-header h2 { font-size: 20px; font-weight: 700; }
.stat-cards { display: grid; grid-template-columns: repeat(5, 1fr); gap: 16px; margin-bottom: 8px; }
.stat-card { background: var(--surface-1); border: 1px solid var(--border); border-radius: 12px; padding: 20px; text-align: center; }
.stat-value { font-size: 32px; font-weight: 700; color: #4F46E5; }
.stat-label { font-size: 13px; color: var(--text-secondary); margin-top: 4px; }
.section { background: var(--surface-1); border: 1px solid var(--border); border-radius: 12px; padding: 20px; margin-top: 16px; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.section-header h3 { font-weight: 600; font-size: 16px; }
.word-tags { display: flex; flex-wrap: wrap; gap: 8px; }
.word-tags .el-tag { font-size: 14px; padding: 8px 12px; }
.empty-text { color: var(--text-muted); padding: 20px; }
</style>
