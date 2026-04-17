<template>
  <div class="robots-page">
    <!-- 左侧机器人列表 -->
    <div class="robots-list-panel">
      <div class="panel-header">
        <h2>我的机器人</h2>
        <button class="add-btn" @click="showCreate = true">+</button>
      </div>

      <div class="tabs">
        <button class="tab" :class="{ active: tab === 'all' }" @click="tab = 'all'">全部</button>
        <button class="tab" :class="{ active: tab === 'AI' }" @click="tab = 'AI'">AI机器人</button>
        <button class="tab" :class="{ active: tab === 'CUSTOM' }" @click="tab = 'CUSTOM'">自定义</button>
      </div>

      <div class="robot-list">
        <div
          v-for="robot in filteredRobots"
          :key="robot.id"
          class="robot-card"
          :class="{ selected: selectedRobot?.id === robot.id }"
          @click="selectRobot(robot)"
        >
          <div class="robot-avatar">
            <span v-if="robot.type === 'AI'">🤖</span>
            <span v-else>⚙️</span>
          </div>
          <div class="robot-info">
            <div class="robot-name">{{ robot.name }}</div>
            <div class="robot-desc">{{ robot.description || '暂无描述' }}</div>
          </div>
          <el-tag :type="robot.status === 'ACTIVE' ? 'success' : 'info'" size="small">
            {{ robot.status === 'ACTIVE' ? '运行中' : '停用' }}
          </el-tag>
        </div>
        <div v-if="!filteredRobots.length" class="empty">暂无机器人</div>
      </div>
    </div>

    <!-- 右侧机器人详情 -->
    <div class="robot-detail-panel">
      <template v-if="selectedRobot">
        <div class="detail-header">
          <div class="detail-avatar">
            <span v-if="selectedRobot.type === 'AI'">🤖</span>
            <span v-else>⚙️</span>
          </div>
          <div class="detail-name">{{ selectedRobot.name }}</div>
          <div class="detail-desc">{{ selectedRobot.description || '暂无描述' }}</div>
        </div>

        <div class="detail-tabs">
          <button class="detail-tab" :class="{ active: detailTab === 'info' }" @click="detailTab = 'info'">基本信息</button>
          <button class="detail-tab" :class="{ active: detailTab === 'ai' }" @click="detailTab = 'ai'">AI配置</button>
          <button class="detail-tab" :class="{ active: detailTab === 'rules' }" @click="detailTab = 'rules'">规则配置</button>
        </div>

        <div class="detail-content">
          <!-- 基本信息 -->
          <div v-if="detailTab === 'info'" class="tab-content">
            <div class="info-item">
              <span class="label">名称</span>
              <span class="value">{{ selectedRobot.name }}</span>
            </div>
            <div class="info-item">
              <span class="label">类型</span>
              <span class="value">{{ selectedRobot.type === 'AI' ? 'AI 机器人' : '自定义机器人' }}</span>
            </div>
            <div class="info-item">
              <span class="label">状态</span>
              <span class="value">{{ selectedRobot.status === 'ACTIVE' ? '运行中' : '停用' }}</span>
            </div>
          </div>

          <!-- AI配置 -->
          <div v-else-if="detailTab === 'ai'" class="tab-content">
            <template v-if="selectedRobot.type === 'AI'">
              <div class="info-item">
                <span class="label">AI提供商</span>
                <span class="value">{{ selectedRobot.aiConfig?.provider || '未配置' }}</span>
              </div>
              <div class="info-item">
                <span class="label">模型</span>
                <span class="value">{{ selectedRobot.aiConfig?.model || '未配置' }}</span>
              </div>
              <div class="info-item">
                <span class="label">System Prompt</span>
                <span class="value">{{ selectedRobot.aiConfig?.systemPrompt || '未配置' }}</span>
              </div>
            </template>
            <div v-else class="empty-small">自定义机器人无需AI配置</div>
          </div>

          <!-- 规则配置 -->
          <div v-else-if="detailTab === 'rules'" class="tab-content">
            <RobotRuleEditor v-if="selectedRobot" :robot-id="selectedRobot.id" />
          </div>
        </div>

        <div class="detail-actions">
          <button class="btn-primary" @click="configRobot(selectedRobot.id)">配置</button>
          <button
            class="btn-secondary"
            :type="selectedRobot.status === 'ACTIVE' ? 'warning' : 'success'"
            @click="toggleStatus(selectedRobot)"
          >
            {{ selectedRobot.status === 'ACTIVE' ? '停用' : '启用' }}
          </button>
          <button class="btn-danger" @click="deleteRobot(selectedRobot.id)">删除</button>
        </div>
      </template>

      <div v-else class="detail-empty">
        <span>选择一个机器人查看详情</span>
      </div>
    </div>

    <!-- 创建机器人弹窗 -->
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
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useRobotStore } from '@/stores/robot'
import { ElMessage, ElMessageBox } from 'element-plus'
import RobotRuleEditor from '@/components/RobotRuleEditor.vue'
import type { Robot } from '@/types'

const robotStore = useRobotStore()
const router = useRouter()

const tab = ref('all')
const detailTab = ref('info')
const showCreate = ref(false)
const selectedRobot = ref<Robot | null>(null)
const form = ref({ name: '', description: '', type: 'AI' })

onMounted(() => robotStore.fetchRobots())

const filteredRobots = computed(() => {
  if (tab.value === 'all') return robotStore.robots
  return robotStore.robots.filter(r => r.type === tab.value)
})

function selectRobot(robot: Robot) {
  selectedRobot.value = robot
  detailTab.value = 'info'
}

function configRobot(id: number) {
  router.push(`/robot/${id}/config`)
}

async function toggleStatus(robot: Robot) {
  await robotStore.updateRobot(robot.id, { status: robot.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE' })
  ElMessage.success('状态已更新')
  await robotStore.fetchRobots()
  if (selectedRobot.value?.id === robot.id) {
    selectedRobot.value = robotStore.robots.find(r => r.id === robot.id) || null
  }
}

async function deleteRobot(id: number) {
  await ElMessageBox.confirm('确定要删除这个机器人吗？', '提示', { type: 'warning' })
  await robotStore.deleteRobot(id)
  ElMessage.success('已删除')
  if (selectedRobot.value?.id === id) {
    selectedRobot.value = null
  }
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
.robots-page {
  display: flex;
  flex: 1;
  overflow: hidden;
}
.robots-list-panel {
  width: 360px;
  border-right: 1px solid var(--border, #E5E7EB);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}
.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid var(--border, #E5E7EB);
}
.panel-header h2 { font-size: 16px; font-weight: 600; margin: 0; }
.add-btn {
  width: 28px;
  height: 28px;
  background: #4F46E5;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 18px;
  cursor: pointer;
}
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
.robot-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}
.robot-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  border: 2px solid transparent;
}
.robot-card:hover { background: #F3F4F6; }
.robot-card.selected { background: #EEF2FF; border-color: #4F46E5; }
.robot-avatar {
  width: 40px;
  height: 40px;
  background: #F3F4F6;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}
.robot-info { flex: 1; min-width: 0; }
.robot-name { font-size: 14px; font-weight: 500; margin-bottom: 2px; }
.robot-desc { font-size: 12px; color: #6B7280; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.empty { text-align: center; color: #9CA3AF; padding: 32px; font-size: 13px; }

/* 右侧详情面板 */
.robot-detail-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--surface-1, #fff);
}
.detail-header {
  padding: 24px;
  text-align: center;
  border-bottom: 1px solid var(--border, #E5E7EB);
}
.detail-avatar {
  width: 64px;
  height: 64px;
  background: #F3F4F6;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  margin: 0 auto 12px;
}
.detail-name { font-size: 18px; font-weight: 600; margin-bottom: 4px; }
.detail-desc { font-size: 13px; color: #6B7280; }
.detail-tabs {
  display: flex;
  border-bottom: 1px solid var(--border, #E5E7EB);
}
.detail-tab {
  flex: 1;
  height: 44px;
  border: none;
  background: transparent;
  font-size: 13px;
  color: #6B7280;
  cursor: pointer;
  border-bottom: 2px solid transparent;
}
.detail-tab.active { color: #4F46E5; border-bottom-color: #4F46E5; }
.detail-content { flex: 1; overflow-y: auto; padding: 16px; }
.tab-content {}
.info-item {
  display: flex;
  padding: 12px 0;
  border-bottom: 1px solid #F3F4F6;
}
.info-item .label { width: 100px; font-size: 13px; color: #6B7280; }
.info-item .value { flex: 1; font-size: 13px; }
.empty-small { text-align: center; color: #9CA3AF; padding: 24px; }
.detail-actions {
  padding: 16px;
  border-top: 1px solid var(--border, #E5E7EB);
  display: flex;
  gap: 8px;
}
.btn-primary {
  flex: 1;
  height: 36px;
  background: #4F46E5;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
}
.btn-secondary {
  flex: 1;
  height: 36px;
  background: #F3F4F6;
  color: #6B7280;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
}
.btn-danger {
  width: 80px;
  height: 36px;
  background: #FEE2E2;
  color: #EF4444;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
}
.detail-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #9CA3AF;
  font-size: 13px;
}
</style>
