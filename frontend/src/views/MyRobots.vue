<template>
  <div class="robots-page">
    <div class="page-header">
      <h2>我的机器人</h2>
      <el-button type="primary" @click="showCreate = true">+ 新建机器人</el-button>
    </div>

    <div class="robot-grid">
      <div v-for="robot in robotStore.robots" :key="robot.id" class="robot-card">
        <div class="robot-avatar">
          <el-avatar :size="48" :src="robot.avatar">{{ robot.name?.[0] }}</el-avatar>
          <el-tag :type="robot.status === 'ACTIVE' ? 'success' : 'info'" size="small">
            {{ robot.status === 'ACTIVE' ? '运行中' : '停用' }}
          </el-tag>
        </div>
        <div class="robot-info">
          <h3>{{ robot.name }}</h3>
          <p>{{ robot.description || '暂无描述' }}</p>
          <p class="robot-type">类型：{{ robot.type === 'AI' ? 'AI 机器人' : '自定义' }}</p>
        </div>
        <div class="robot-actions">
          <el-button size="small" @click="configRobot(robot.id)">配置</el-button>
          <el-button size="small" :type="robot.status === 'ACTIVE' ? 'warning' : 'success'"
            @click="toggleStatus(robot)">
            {{ robot.status === 'ACTIVE' ? '停用' : '启用' }}
          </el-button>
          <el-button size="small" type="danger" @click="deleteRobot(robot.id)">删除</el-button>
        </div>
      </div>
      <div v-if="!robotStore.robots.length" class="empty">暂无机器人，点击新建创建</div>
    </div>

    <el-dialog v-model="showCreate" title="新建机器人" width="480px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.type">
            <el-option label="AI 机器人" value="AI" />
            <el-option label="自定义" value="CUSTOM" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" @click="createRobot">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useRobotStore } from '@/stores/robot'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { Robot } from '@/types'

const robotStore = useRobotStore()
const router = useRouter()
const showCreate = ref(false)
const form = ref({ name: '', description: '', type: 'AI' })

onMounted(() => robotStore.fetchRobots())

function configRobot(id: number) { router.push(`/robot/${id}/config`) }

async function toggleStatus(robot: Robot) {
  await robotStore.updateRobot(robot.id, { status: robot.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE' })
  ElMessage.success('状态已更新')
}

async function deleteRobot(id: number) {
  await ElMessageBox.confirm('确定要删除这个机器人吗？', '提示', { type: 'warning' })
  await robotStore.deleteRobot(id)
  ElMessage.success('已删除')
}

async function createRobot() {
  if (!form.value.name.trim()) { ElMessage.warning('请输入机器人名称'); return }
  await robotStore.createRobot(form.value)
  ElMessage.success('创建成功')
  showCreate.value = false
  form.value = { name: '', description: '', type: 'AI' }
}
</script>

<style scoped>
.robots-page { padding: 24px; height: 100%; overflow: auto; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.page-header h2 { font-size: 20px; font-weight: 700; }
.robot-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 16px; }
.robot-card { background: var(--surface-1); border: 1px solid var(--border); border-radius: 12px; padding: 16px; display: flex; flex-direction: column; gap: 12px; }
.robot-avatar { display: flex; align-items: center; gap: 12px; }
.robot-info h3 { font-weight: 600; margin-bottom: 4px; }
.robot-info p { font-size: 13px; color: var(--text-secondary); }
.robot-type { margin-top: 4px; }
.robot-actions { display: flex; gap: 8px; flex-wrap: wrap; }
.empty { text-align: center; color: #9CA3AF; padding: 48px; grid-column: 1/-1; }
</style>
