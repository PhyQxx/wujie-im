import request from '@/utils/request'

export const robotApi = {
  list: () => request.get('/robot/list'),
  create: (data: any) => request.post('/robot/create', data),
  update: (robotId: number, data: any) => request.put(`/robot/${robotId}`, data),
  delete: (robotId: number) => request.delete(`/robot/${robotId}`),
  rules: (robotId: number) => request.get(`/robot/${robotId}/rules`),
  addRule: (robotId: number, rule: any) => request.post(`/robot/${robotId}/rule`, rule),
  updateRule: (ruleId: number, rule: any) => request.put(`/robot/rule/${ruleId}`, rule),
  deleteRule: (ruleId: number) => request.delete(`/robot/rule/${ruleId}`),
  getAiConfig: (robotId: number) => request.get(`/robot/${robotId}/ai-config`),
  saveAiConfig: (robotId: number, config: any) => request.post(`/robot/${robotId}/ai-config`, config)
}
