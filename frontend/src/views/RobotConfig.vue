<template>
  <div class="robot-config">
    <div class="config-header">
      <el-button @click="$router.back()">← 返回</el-button>
      <h2>机器人配置</h2>
    </div>

    <el-tabs>
      <el-tab-pane label="基本配置">
        <el-form :model="basicForm" label-width="100px">
          <el-form-item label="名称"><el-input v-model="basicForm.name" /></el-form-item>
          <el-form-item label="描述"><el-input v-model="basicForm.description" type="textarea" :rows="3" /></el-form-item>
          <el-form-item label="响应模式">
            <el-select v-model="basicForm.responseMode">
              <el-option label="立即响应" value="IMMEDIATE" />
              <el-option label="延迟响应" value="DELAYED" />
            </el-select>
          </el-form-item>
          <el-form-item label="上下文条数">
            <el-input-number v-model="basicForm.contextSize" :min="1" :max="50" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveBasic">保存基本配置</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="AI 配置">
        <el-form :model="aiForm" label-width="120px">
          <el-form-item label="AI 提供商">
            <el-select v-model="aiForm.provider">
              <el-option label="MiniMax" value="MINIMAX" />
              <el-option label="智谱 GLM" value="GLM" />
              <el-option label="DeepSeek" value="DEEPSEEK" />
            </el-select>
          </el-form-item>
          <el-form-item label="模型"><el-input v-model="aiForm.model" /></el-form-item>
          <el-form-item label="API Key"><el-input v-model="aiForm.apiKey" type="password" show-password /></el-form-item>
          <el-form-item label="系统提示词">
            <el-input v-model="aiForm.systemPrompt" type="textarea" :rows="4" placeholder="设置机器人的角色和行为..." />
          </el-form-item>
          <el-form-item label="Temperature">
            <el-slider v-model="aiForm.temperature" :min="0" :max="2" :step="0.1" show-input />
          </el-form-item>
          <el-form-item label="最大Token">
            <el-input-number v-model="aiForm.maxTokens" :min="100" :max="8000" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveAI">保存 AI 配置</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="规则配置">
        <RobotRuleEditor :robot-id="robotId" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useRobotStore } from '@/stores/robot'
import { ElMessage } from 'element-plus'
import RobotRuleEditor from '@/components/RobotRuleEditor.vue'

const route = useRoute()
const robotStore = useRobotStore()
const robotId = Number(route.params.id)

const basicForm = ref({ name: '', description: '', responseMode: 'IMMEDIATE', contextSize: 10 })
const aiForm = ref({ provider: 'MINIMAX', model: '', apiKey: '', systemPrompt: '', temperature: 0.7, maxTokens: 2000 })

onMounted(async () => {
  const robot = robotStore.robots.find(r => r.id === robotId) || (await robotStore.fetchRobots(), robotStore.robots.find(r => r.id === robotId))
  if (robot) {
    basicForm.value = { name: robot.name, description: robot.description || '', responseMode: robot.responseMode, contextSize: robot.contextSize }
    if (robot.aiConfig) {
      aiForm.value = { provider: robot.aiConfig.provider, model: robot.aiConfig.model, apiKey: robot.aiConfig.apiKey, systemPrompt: robot.aiConfig.systemPrompt || '', temperature: robot.aiConfig.temperature, maxTokens: robot.aiConfig.maxTokens }
    }
  }
})

async function saveBasic() {
  await robotStore.updateRobot(robotId, basicForm.value)
  ElMessage.success('基本配置已保存')
}

async function saveAI() {
  await robotStore.updateAiConfig(robotId, aiForm.value)
  ElMessage.success('AI 配置已保存')
}
</script>

<style scoped>
.robot-config { padding: 24px; height: 100%; overflow: auto; }
.config-header { display: flex; align-items: center; gap: 16px; margin-bottom: 24px; }
.config-header h2 { font-size: 18px; font-weight: 700; }
</style>
