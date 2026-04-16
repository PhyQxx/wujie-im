<template>
  <div class="group-member-list">
    <div class="list-header">
      <span>成员列表 ({{ members.length }})</span>
      <el-button size="small" type="primary" @click="showAdd = true">添加成员</el-button>
    </div>
    <div class="members">
      <div v-for="m in members" :key="m.id" class="member-item">
        <el-avatar :size="36" :src="m.user?.avatar">{{ m.user?.username?.[0] }}</el-avatar>
        <div class="member-info">
          <span class="member-name">{{ m.user?.username }}</span>
          <el-tag size="small" :type="roleType(m.role)">{{ roleLabel(m.role) }}</el-tag>
        </div>
        <el-tag v-if="m.muted" type="warning" size="small">已禁言</el-tag>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { ref } from 'vue'
import { useGroupStore } from '@/stores/group'

const props = defineProps<{ groupId: number }>()
const groupStore = useGroupStore()
const showAdd = ref(false)
const members = computed(() => groupStore.members)

onMounted(() => groupStore.fetchMembers(props.groupId))

function roleLabel(role: string) {
  const map: Record<string, string> = { OWNER: '群主', ADMIN: '管理员', MEMBER: '成员' }
  return map[role] || role
}

function roleType(role: string): any {
  const map: Record<string, string> = { OWNER: 'danger', ADMIN: 'warning', MEMBER: '' }
  return map[role] || ''
}
</script>

<style scoped>
.group-member-list { padding: 12px; }
.list-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; font-weight: 600; }
.members { display: flex; flex-direction: column; gap: 8px; }
.member-item { display: flex; align-items: center; gap: 10px; padding: 8px; background: var(--surface-2); border-radius: 8px; }
.member-info { flex: 1; display: flex; align-items: center; gap: 8px; }
.member-name { font-weight: 500; }
</style>
