<template>
  <div class="admin-page">
    <h2>管理后台</h2>

    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card" v-for="stat in stats" :key="stat.label">
        <div class="stat-value">{{ stat.value }}</div>
        <div class="stat-label">{{ stat.label }}</div>
      </div>
    </div>

    <!-- 用户管理 -->
    <div class="section">
      <div class="section-header">
        <h3>用户管理</h3>
        <el-input v-model="userSearch" placeholder="搜索用户" style="width:200px" />
      </div>
      <el-table :data="users" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'"
              @click="toggleUserStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" @click="resetPassword(row.id)">重置密码</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { adminApi } from '@/api/admin'
import { ElMessage } from 'element-plus'

const users = ref<any[]>([])
const userSearch = ref('')
const stats = ref([
  { label: '总用户数', value: 0 },
  { label: '在线用户', value: 0 },
  { label: '群组数', value: 0 },
  { label: '今日消息', value: 0 }
])

onMounted(async () => {
  try {
    const res = await adminApi.users()
    users.value = res.data || []
    const statsRes = await adminApi.stats()
    if (statsRes.data) {
      stats.value[0].value = statsRes.data.totalUsers || 0
      stats.value[1].value = statsRes.data.onlineUsers || 0
      stats.value[2].value = statsRes.data.totalGroups || 0
      stats.value[3].value = statsRes.data.todayMessages || 0
    }
  } catch (_e) {}
})

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
</script>

<style scoped>
.admin-page { padding: 24px; height: 100%; overflow: auto; }
.admin-page h2 { font-size: 20px; font-weight: 700; margin-bottom: 24px; }
.stat-cards { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px; }
.stat-card { background: var(--surface-1); border: 1px solid var(--border); border-radius: 12px; padding: 20px; text-align: center; }
.stat-value { font-size: 32px; font-weight: 700; color: var(--primary); }
.stat-label { font-size: 14px; color: var(--text-secondary); margin-top: 4px; }
.section { background: var(--surface-1); border: 1px solid var(--border); border-radius: 12px; padding: 16px; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.section-header h3 { font-weight: 600; }
</style>
