<template>
  <div class="group-detail">
    <div class="group-header">
      <el-avatar :size="60" :src="group?.avatar">{{ group?.name?.[0] }}</el-avatar>
      <div class="group-info">
        <h2>{{ group?.name }}</h2>
        <p>{{ members.length }} 名成员</p>
      </div>
    </div>

    <el-tabs>
      <el-tab-pane label="成员">
        <GroupMemberList :group-id="Number(route.params.id)" />
      </el-tab-pane>
      <el-tab-pane label="公告">
        <div class="announcement">{{ group?.announcement || '暂无公告' }}</div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useGroupStore } from '@/stores/group'
import GroupMemberList from '@/components/GroupMemberList.vue'

const route = useRoute()
const groupStore = useGroupStore()

const members = computed(() => groupStore.members)
const group = computed(() => groupStore.groups.find(g => g.id === Number(route.params.id)))

onMounted(async () => {
  await groupStore.fetchGroups()
  await groupStore.fetchMembers(Number(route.params.id))
})
</script>

<style scoped>
.group-detail { padding: 24px; }
.group-header { display: flex; align-items: center; gap: 16px; margin-bottom: 24px; }
.group-info h2 { font-size: 20px; font-weight: 700; }
.group-info p { color: var(--text-secondary); font-size: 14px; }
.announcement { padding: 16px; background: var(--surface-2); border-radius: 8px; color: var(--text-secondary); }
</style>
