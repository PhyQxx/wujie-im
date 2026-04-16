import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Robot, RobotRule } from '@/types'
import request from '@/utils/request'

export const useRobotStore = defineStore('robot', () => {
  const robots = ref<Robot[]>([])
  const currentRobot = ref<Robot | null>(null)
  const rules = ref<RobotRule[]>([])
  const loading = ref(false)

  async function fetchRobots() {
    loading.value = true
    try {
      const res = await request.get('/robot/list')
      robots.value = res.data || []
    } finally {
      loading.value = false
    }
  }

  async function createRobot(data: Partial<Robot>) {
    const res = await request.post('/robot/create', data)
    robots.value.unshift(res.data)
    return res.data
  }

  async function updateRobot(robotId: number, data: Partial<Robot>) {
    const res = await request.put(`/robot/${robotId}`, data)
    const idx = robots.value.findIndex(r => r.id === robotId)
    if (idx > -1) robots.value[idx] = res.data
    return res.data
  }

  async function deleteRobot(robotId: number) {
    await request.delete(`/robot/${robotId}`)
    robots.value = robots.value.filter(r => r.id !== robotId)
  }

  async function fetchRules(robotId: number) {
    const res = await request.get(`/robot/${robotId}/rules`)
    rules.value = res.data || []
  }

  async function addRule(robotId: number, rule: Partial<RobotRule>) {
    const res = await request.post(`/robot/${robotId}/rule`, rule)
    rules.value.push(res.data)
    return res.data
  }

  async function updateRule(ruleId: number, rule: Partial<RobotRule>) {
    const res = await request.put(`/robot/rule/${ruleId}`, rule)
    const idx = rules.value.findIndex(r => r.id === ruleId)
    if (idx > -1) rules.value[idx] = res.data
    return res.data
  }

  async function deleteRule(ruleId: number) {
    await request.delete(`/robot/rule/${ruleId}`)
    rules.value = rules.value.filter(r => r.id !== ruleId)
  }

  async function updateAiConfig(robotId: number, config: any) {
    const res = await request.post(`/robot/${robotId}/ai-config`, config)
    const idx = robots.value.findIndex(r => r.id === robotId)
    if (idx > -1) robots.value[idx].aiConfig = res.data
    return res.data
  }

  return { robots, currentRobot, rules, loading, fetchRobots, createRobot, updateRobot, deleteRobot, fetchRules, addRule, updateRule, deleteRule, updateAiConfig }
})
