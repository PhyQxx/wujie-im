<template>
  <div class="robot-rule-editor">
    <div class="editor-header">
      <span>规则列表 ({{ robotStore.rules.length }})</span>
      <el-button type="primary" size="small" @click="showAdd = true">+ 添加规则</el-button>
    </div>

    <el-table :data="robotStore.rules" stripe size="small">
      <el-table-column prop="ruleType" label="类型" width="90">
        <template #default="{ row }">
          <el-tag size="small">{{ row.ruleType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="keyword" label="关键词/模式" />
      <el-table-column prop="replyContent" label="回复内容" show-overflow-tooltip />
      <el-table-column prop="priority" label="优先级" width="70" />
      <el-table-column label="状态" width="70">
        <template #default="{ row }">
          <el-switch :model-value="row.enabled" @change="toggleRule(row)" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="80">
        <template #default="{ row }">
          <el-button size="small" type="danger" @click="deleteRule(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="showAdd" title="添加规则" width="480px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="规则类型">
          <el-select v-model="form.ruleType">
            <el-option label="关键词" value="KEYWORD" />
            <el-option label="正则" value="REGEX" />
            <el-option label="Webhook" value="WEBHOOK" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.ruleType !== 'WEBHOOK'" label="关键词/模式">
          <el-input v-model="form.keyword" />
        </el-form-item>
        <el-form-item v-if="form.ruleType === 'WEBHOOK'" label="Webhook URL">
          <el-input v-model="form.webhookUrl" />
        </el-form-item>
        <el-form-item label="回复内容">
          <el-input v-model="form.replyContent" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="优先级">
          <el-input-number v-model="form.priority" :min="1" :max="100" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAdd = false">取消</el-button>
        <el-button type="primary" @click="addRule">添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRobotStore } from '@/stores/robot'
import { ElMessage } from 'element-plus'

const props = defineProps<{ robotId: number }>()
const robotStore = useRobotStore()
const showAdd = ref(false)
const form = ref({ ruleType: 'KEYWORD', keyword: '', webhookUrl: '', replyContent: '', priority: 1 })

onMounted(() => robotStore.fetchRules(props.robotId))

async function addRule() {
  await robotStore.addRule(props.robotId, { ...form.value, replyType: 'TEXT', enabled: true })
  ElMessage.success('规则已添加')
  showAdd.value = false
  form.value = { ruleType: 'KEYWORD', keyword: '', webhookUrl: '', replyContent: '', priority: 1 }
}

async function deleteRule(id: number) {
  await robotStore.deleteRule(id)
  ElMessage.success('规则已删除')
}

async function toggleRule(rule: any) {
  await robotStore.updateRule(rule.id, { enabled: !rule.enabled })
}
</script>

<style scoped>
.robot-rule-editor { }
.editor-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; font-weight: 600; }
</style>
