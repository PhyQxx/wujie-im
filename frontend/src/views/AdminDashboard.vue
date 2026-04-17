<template>
  <div class="admin-page" :key="activeTab">
    <div class="admin-header">
      <h2>{{ tabTitle }}</h2>
    </div>

    <!-- 数据看板 -->
    <div v-if="activeTab === 'stats'" class="stats-section">
      <div class="stat-cards">
        <div class="stat-card dark" v-for="stat in stats" :key="stat.label">
          <div class="stat-icon" :style="{ background: stat.iconBg }">
            <svg v-if="stat.key === 'totalUsers'" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"></path></svg>
            <svg v-else-if="stat.key === 'todayUsers'" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z"></path></svg>
            <svg v-else-if="stat.key === 'totalGroups'" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"></path></svg>
            <svg v-else-if="stat.key === 'todayMessages'" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"></path></svg>
            <svg v-else xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"></path></svg>
          </div>
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
      </div>
    </div>

    <!-- 运营数据 -->
    <div v-if="activeTab === 'operations'" class="section">
      <div class="stat-cards" style="grid-template-columns: repeat(4, 1fr); margin-bottom: 20px;">
        <div class="stat-card dark" v-for="op in operationStats" :key="op.label">
          <div class="stat-icon" :style="{ background: op.iconBg }">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6"></path></svg>
          </div>
          <div class="stat-value">{{ op.value }}</div>
          <div class="stat-label">{{ op.label }}</div>
        </div>
      </div>
      <div class="section-header">
        <h3>运营概览</h3>
      </div>
      <div class="ops-grid">
        <div class="ops-card">
          <div class="ops-card-title">消息活跃度</div>
          <div class="ops-card-value">{{ stats.find(s => s.key === 'todayMessages')?.value || 0 }} <span>条/今日</span></div>
        </div>
        <div class="ops-card">
          <div class="ops-card-title">新增用户</div>
          <div class="ops-card-value">{{ stats.find(s => s.key === 'todayUsers')?.value || 0 }} <span>人/今日</span></div>
        </div>
        <div class="ops-card">
          <div class="ops-card-title">活跃群组</div>
          <div class="ops-card-value">{{ stats.find(s => s.key === 'totalGroups')?.value || 0 }} <span>个</span></div>
        </div>
        <div class="ops-card">
          <div class="ops-card-title">机器人</div>
          <div class="ops-card-value">{{ stats.find(s => s.key === 'totalRobots')?.value || 0 }} <span>个</span></div>
        </div>
      </div>
    </div>

    <!-- 用户管理 -->
    <div v-if="activeTab === 'users'" class="section">
      <div class="section-header">
        <h3>用户管理</h3>
        <div style="display:flex;gap:8px;">
          <el-button type="primary" size="small" @click="openCreateUserDialog">创建用户</el-button>
          <el-input v-model="userSearch" placeholder="搜索用户/手机号" style="width:200px" clearable @change="loadUsers" />
        </div>
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

      <el-dialog v-model="showCreateUserDialog" title="创建用户" width="400px">
        <el-form :model="createUserForm" label-width="80px">
          <el-form-item label="用户名">
            <el-input v-model="createUserForm.username" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="createUserForm.password" type="password" show-password placeholder="请输入密码" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="createUserForm.email" placeholder="可选" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showCreateUserDialog = false">取消</el-button>
          <el-button type="primary" @click="createUser">创建</el-button>
        </template>
      </el-dialog>
    </div>

    <!-- 内容审核 -->
    <div v-if="activeTab === 'content'" class="section">
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

    <!-- 群组管理 -->
    <div v-if="activeTab === 'groups'" class="section">
      <div class="section-header">
        <h3>群组管理</h3>
        <div style="display:flex;gap:8px;">
          <el-button type="primary" size="small" @click="showCreateGroupDialog = true">创建群组</el-button>
          <el-input v-model="groupSearch" placeholder="搜索群名称" style="width:200px" clearable @change="loadGroups" />
        </div>
      </div>
      <el-table :data="groups" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="群名称" />
        <el-table-column prop="ownerId" label="群主ID" />
        <el-table-column prop="type" label="类型" />
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" @click="viewGroupMessages(row)">查看消息</el-button>
            <el-button size="small" type="danger" @click="dissolveGroup(row)">解散</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-dialog v-model="showCreateGroupDialog" title="创建群组" width="420px">
        <el-form :model="createGroupForm" label-width="80px">
          <el-form-item label="群名称">
            <el-input v-model="createGroupForm.name" placeholder="请输入群名称" />
          </el-form-item>
          <el-form-item label="群类型">
            <el-select v-model="createGroupForm.type" style="width:100%">
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

      <!-- 群消息查看 -->
      <el-dialog v-model="showGroupMessages" :title="'群消息 - ' + (selectedGroup?.name || '')" width="640px" destroy-on-close>
        <div class="msg-list" v-loading="msgLoading">
          <div v-for="msg in groupMessages" :key="msg.id" class="msg-item">
            <span class="msg-sender">用户{{ msg.senderId }}</span>
            <span class="msg-type">[{{ msg.contentType }}]</span>
            <span class="msg-content">{{ msg.content }}</span>
            <span class="msg-time">{{ msg.createTime }}</span>
          </div>
          <div v-if="!groupMessages.length" class="empty-text">暂无消息</div>
        </div>
        <template #footer>
          <el-button size="small" @click="loadMoreMessages" :disabled="!hasMoreMessages">加载更多</el-button>
        </template>
      </el-dialog>
    </div>

    <!-- 机器人管理 -->
    <div v-if="activeTab === 'robots'" class="section">
      <div class="section-header">
        <h3>机器人管理</h3>
        <div style="display:flex;gap:8px;">
          <el-button type="primary" size="small" @click="showCreateRobotDialog = true">新增机器人</el-button>
          <el-input v-model="robotSearch" placeholder="搜索机器人" style="width:200px" clearable @change="loadRobots" />
        </div>
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

      <el-dialog v-model="showCreateRobotDialog" title="新增机器人" width="420px">
        <el-form :model="createRobotForm" label-width="80px">
          <el-form-item label="名称">
            <el-input v-model="createRobotForm.name" placeholder="请输入机器人名称" />
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="createRobotForm.type" style="width:100%">
              <el-option label="AI 机器人" value="AI" />
              <el-option label="通知机器人" value="NOTIFICATION" />
              <el-option label="工具机器人" value="TOOL" />
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showCreateRobotDialog = false">取消</el-button>
          <el-button type="primary" @click="createRobot">创建</el-button>
        </template>
      </el-dialog>
    </div>

    <!-- AI配置 -->
    <div v-if="activeTab === 'ai'" class="section">
      <div class="section-header">
        <h3>AI模型配置</h3>
        <el-button type="primary" size="small" @click="openAiConfigDialog()">添加配置</el-button>
      </div>
      <el-table :data="aiConfigs" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="robotId" label="机器人ID" width="100" />
        <el-table-column prop="provider" label="提供商" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ row.provider }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="model" label="模型" />
        <el-table-column prop="temperature" label="温度" width="80" />
        <el-table-column prop="maxTokens" label="最大Token" width="100" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button size="small" @click="openAiConfigDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteAiConfig(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-dialog v-model="showAiDialog" title="AI配置" width="480px">
        <el-form :model="aiForm" label-width="90px">
          <el-form-item label="机器人ID">
            <el-input v-model.number="aiForm.robotId" type="number" />
          </el-form-item>
          <el-form-item label="提供商">
            <el-select v-model="aiForm.provider" style="width:100%">
              <el-option label="MiniMax" value="MINIMAX" />
              <el-option label="GLM" value="GLM" />
              <el-option label="DeepSeek" value="DEEPSEEK" />
            </el-select>
          </el-form-item>
          <el-form-item label="模型">
            <el-input v-model="aiForm.model" placeholder="如：glm-4-flash, deepseek-chat" />
          </el-form-item>
          <el-form-item label="API Key">
            <el-input v-model="aiForm.apiKey" type="password" show-password />
          </el-form-item>
          <el-form-item label="系统提示词">
            <el-input v-model="aiForm.systemPrompt" type="textarea" :rows="3" />
          </el-form-item>
          <el-form-item label="温度">
            <el-input-number v-model="aiForm.temperature" :min="0" :max="2" :step="0.1" />
          </el-form-item>
          <el-form-item label="最大Token">
            <el-input-number v-model="aiForm.maxTokens" :min="1" :max="32000" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showAiDialog = false">取消</el-button>
          <el-button type="primary" @click="saveAiConfig">保存</el-button>
        </template>
      </el-dialog>
    </div>

    <!-- 系统配置 -->
    <div v-if="activeTab === 'system'" class="section">
      <div class="section-header">
        <h3>系统配置</h3>
        <el-button size="small" @click="loadSystemConfigs">刷新</el-button>
      </div>
      <div class="config-list">
        <div v-for="cfg in systemConfigs" :key="cfg.id" class="config-item">
          <div class="config-info">
            <div class="config-key">{{ cfg.configKey }}</div>
            <div class="config-desc">{{ cfg.description }}</div>
          </div>
          <div class="config-value">
            <el-switch
              v-if="cfg.configValue === 'true' || cfg.configValue === 'false'"
              :model-value="cfg.configValue === 'true'"
              @change="(v: boolean) => updateSystemConfig(cfg.configKey, String(v))"
              active-text="开启" inactive-text="关闭"
            />
            <el-input v-else v-model="cfg.configValue" style="width:200px" size="small">
              <template #append>
                <el-button @click="updateSystemConfig(cfg.configKey, cfg.configValue)">保存</el-button>
              </template>
            </el-input>
          </div>
        </div>
      </div>
      <div v-if="!systemConfigs.length" class="empty-text">暂无配置</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { adminApi } from '@/api/admin'
import { groupApi } from '@/api/group'
import { ElMessage } from 'element-plus'

const route = useRoute()
const loading = ref(false)

// 从路由 meta 读取当前 tab
const activeTab = computed(() => (route.meta.adminTab as string) || 'stats')

const tabTitles: Record<string, string> = {
  stats: '数据看板',
  operations: '运营数据',
  users: '用户管理',
  content: '内容审核',
  groups: '群组管理',
  robots: '机器人管理',
  ai: 'AI 配置',
  system: '系统配置'
}
const tabTitle = computed(() => tabTitles[activeTab.value] || '管理后台')

const stats = ref([
  { label: '总用户数', value: 0, key: 'totalUsers', iconBg: '#3B82F6' },
  { label: '今日新增用户', value: 0, key: 'todayUsers', iconBg: '#10B981' },
  { label: '群组数', value: 0, key: 'totalGroups', iconBg: '#F59E0B' },
  { label: '今日消息', value: 0, key: 'todayMessages', iconBg: '#EF4444' },
  { label: '机器人数', value: 0, key: 'totalRobots', iconBg: '#8B5CF6' }
])

const operationStats = ref([
  { label: '日活用户', value: 0, iconBg: '#3B82F6' },
  { label: '日均消息', value: 0, iconBg: '#10B981' },
  { label: '消息总量', value: 0, iconBg: '#F59E0B' },
  { label: '活跃群组', value: 0, iconBg: '#8B5CF6' }
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

// AI配置
const aiConfigs = ref<any[]>([])
const showAiDialog = ref(false)
const aiForm = ref<any>({})

// 系统配置
const systemConfigs = ref<any[]>([])

// 创建用户
const showCreateUserDialog = ref(false)
const createUserForm = ref({ username: '', password: '', email: '' })

// 创建群组
const showCreateGroupDialog = ref(false)
const createGroupForm = ref({ name: '', type: 'PUBLIC' })

// 创建机器人
const showCreateRobotDialog = ref(false)
const createRobotForm = ref({ name: '', type: 'AI' })

// 群消息
const showGroupMessages = ref(false)
const selectedGroup = ref<any>(null)
const groupMessages = ref<any[]>([])
const msgLoading = ref(false)
const hasMoreMessages = ref(true)

onMounted(() => {
  loadStats()
  loadTabData()
})

// 创建用户
function openCreateUserDialog() {
  createUserForm.value = { username: '', password: '', email: '' }
  showCreateUserDialog.value = true
}

async function createUser() {
  const f = createUserForm.value
  if (!f.username.trim() || !f.password.trim()) {
    ElMessage.warning('用户名和密码不能为空')
    return
  }
  try {
    await adminApi.register(f)
    ElMessage.success('用户创建成功')
    showCreateUserDialog.value = false
    await loadUsers()
  } catch (_e: any) {
    ElMessage.error(_e?.response?.data?.msg || '创建失败')
  }
}

// 创建群组
async function createGroup() {
  if (!createGroupForm.value.name.trim()) {
    ElMessage.warning('请输入群名称')
    return
  }
  await groupApi.create({
    name: createGroupForm.value.name.trim(),
    type: createGroupForm.value.type,
    ownerId: Number(localStorage.getItem('userId'))
  })
  ElMessage.success('群组创建成功')
  showCreateGroupDialog.value = false
  createGroupForm.value = { name: '', type: 'PUBLIC' }
  await loadGroups()
}

// 创建机器人
async function createRobot() {
  if (!createRobotForm.value.name.trim()) {
    ElMessage.warning('请输入机器人名称')
    return
  }
  try {
    await adminApi.createRobot({
      name: createRobotForm.value.name.trim(),
      type: createRobotForm.value.type,
      ownerId: Number(localStorage.getItem('userId'))
    })
    ElMessage.success('机器人创建成功')
    showCreateRobotDialog.value = false
    createRobotForm.value = { name: '', type: 'AI' }
    await loadRobots()
  } catch (_e: any) {
    ElMessage.error(_e?.response?.data?.msg || '创建失败')
  }
}

// 监听路由变化加载对应数据
watch(() => route.meta.adminTab, () => {
  loadTabData()
})

function loadTabData() {
  const tab = activeTab.value
  if (tab === 'users') loadUsers()
  else if (tab === 'groups') loadGroups()
  else if (tab === 'robots') loadRobots()
  else if (tab === 'content') loadSensitiveWords()
  else if (tab === 'ai') loadAiConfigs()
  else if (tab === 'system') loadSystemConfigs()
  else if (tab === 'operations') loadStats()
}

async function loadStats() {
  try {
    const res = await adminApi.stats()
    if (res.data) {
      stats.value.forEach(s => { s.value = res.data[s.key] || 0 })
      // 同步运营数据
      operationStats.value[0].value = res.data['todayUsers'] || 0
      operationStats.value[1].value = res.data['todayMessages'] || 0
      operationStats.value[2].value = res.data['todayMessages'] || 0
      operationStats.value[3].value = res.data['totalGroups'] || 0
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

// AI配置
async function loadAiConfigs() {
  loading.value = true
  try {
    const res = await adminApi.aiConfigs()
    aiConfigs.value = res.data || []
  } finally { loading.value = false }
}

function openAiConfigDialog(row?: any) {
  if (row) {
    aiForm.value = { ...row }
  } else {
    aiForm.value = { provider: 'MINIMAX', temperature: 0.7, maxTokens: 2048 }
  }
  showAiDialog.value = true
}

async function saveAiConfig() {
  await adminApi.saveAiConfig(aiForm.value)
  showAiDialog.value = false
  await loadAiConfigs()
  ElMessage.success('保存成功')
}

async function deleteAiConfig(id: number) {
  await adminApi.deleteAiConfig(id)
  aiConfigs.value = aiConfigs.value.filter(c => c.id !== id)
  ElMessage.success('已删除')
}

// 系统配置
async function loadSystemConfigs() {
  try {
    const res = await adminApi.systemConfigs()
    systemConfigs.value = res.data || []
  } catch (_e) {}
}

async function updateSystemConfig(key: string, value: string) {
  await adminApi.updateSystemConfig(key, value)
  ElMessage.success('已更新')
}

// 群消息查看
async function viewGroupMessages(group: any) {
  selectedGroup.value = group
  showGroupMessages.value = true
  groupMessages.value = []
  hasMoreMessages.value = true
  await loadGroupMessages()
}

async function loadGroupMessages() {
  if (!selectedGroup.value) return
  msgLoading.value = true
  try {
    const beforeId = groupMessages.value.length ? groupMessages.value[groupMessages.value.length - 1].id : undefined
    const res = await adminApi.groupMessages(selectedGroup.value.id, 30, beforeId)
    const msgs = res.data || []
    if (!msgs.length) { hasMoreMessages.value = false; return }
    groupMessages.value.push(...msgs)
    if (msgs.length < 30) hasMoreMessages.value = false
  } finally {
    msgLoading.value = false
  }
}

async function loadMoreMessages() {
  await loadGroupMessages()
}
</script>

<style scoped>
.admin-page { padding: 24px; height: 100%; overflow: auto; }
.admin-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.admin-header h2 { font-size: 20px; font-weight: 700; }
.stat-cards { display: grid; grid-template-columns: repeat(5, 1fr); gap: 16px; margin-bottom: 8px; }
.stat-card { background: #1F2937; border-radius: 12px; padding: 20px; display: flex; flex-direction: column; align-items: center; gap: 12px; }
.stat-icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; }
.stat-icon svg { width: 24px; height: 24px; color: white; }
.stat-value { font-size: 28px; font-weight: 700; color: white; }
.stat-label { font-size: 13px; color: #9CA3AF; margin-top: -4px; }
.section { background: var(--surface-1); border: 1px solid var(--border); border-radius: 12px; padding: 20px; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.section-header h3 { font-weight: 600; font-size: 16px; }
.word-tags { display: flex; flex-wrap: wrap; gap: 8px; }
.word-tags .el-tag { font-size: 14px; padding: 8px 12px; }
.empty-text { color: var(--text-muted); padding: 20px; }
.config-list { display: flex; flex-direction: column; gap: 12px; }
.config-item { display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; border: 1px solid var(--border); border-radius: 8px; }
.config-info {}
.config-key { font-weight: 600; font-size: 14px; }
.config-desc { font-size: 12px; color: var(--text-secondary); margin-top: 2px; }
.config-value {}
.msg-list { max-height: 400px; overflow-y: auto; display: flex; flex-direction: column; gap: 8px; }
.msg-item { display: flex; align-items: baseline; gap: 6px; font-size: 13px; padding: 6px 8px; border-radius: 6px; }
.msg-item:hover { background: var(--surface-3); }
.msg-sender { color: var(--primary); font-weight: 600; flex-shrink: 0; }
.msg-type { color: var(--text-muted); font-size: 11px; flex-shrink: 0; }
.msg-content { flex: 1; word-break: break-all; }
.msg-time { color: var(--text-muted); font-size: 11px; flex-shrink: 0; }
.ops-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.ops-card { background: white; border: 1px solid var(--border); border-radius: 10px; padding: 16px 20px; }
.ops-card-title { font-size: 13px; color: var(--text-secondary); margin-bottom: 8px; }
.ops-card-value { font-size: 24px; font-weight: 700; color: var(--text-primary); }
.ops-card-value span { font-size: 12px; font-weight: 400; color: var(--text-muted); }
</style>
