import request from '@/utils/request'

export const adminApi = {
  // 统计数据
  stats: () => request.get('/admin/stats'),

  // 用户管理
  users: (keyword?: string) =>
    request.get('/admin/users', { params: { keyword } }),
  updateUserStatus: (userId: number, status: number) =>
    request.put(`/admin/users/${userId}/status?status=${status}`),
  resetPassword: (userId: number, password: string) =>
    request.put(`/admin/users/${userId}/password?password=${encodeURIComponent(password)}`),
  register: (data: { username: string; password: string; email?: string }) =>
    request.post('/auth/register', data),

  // 群聊管理
  groups: (keyword?: string) =>
    request.get('/admin/groups', { params: { keyword } }),
  dissolveGroup: (groupId: number) =>
    request.delete(`/admin/groups/${groupId}`),
  groupMessages: (groupId: number, limit?: number, beforeId?: number) =>
    request.get(`/admin/group-messages/${groupId}`, { params: { limit, beforeId } }),

  // 机器人管理
  robots: (keyword?: string) =>
    request.get('/admin/robots', { params: { keyword } }),
  createRobot: (data: { name: string; type: string; ownerId: number }) =>
    request.post('/robot/create', data),

  // 敏感词
  sensitiveWords: () => request.get('/admin/sensitive-words'),
  addSensitiveWord: (word: { word: string; level?: number }) =>
    request.post('/admin/sensitive-word', word),
  deleteSensitiveWord: (id: number) =>
    request.delete(`/admin/sensitive-word/${id}`),

  // AI配置
  aiConfigs: (robotId?: number) =>
    request.get('/admin/ai-config', { params: robotId ? { robotId } : {} }),
  saveAiConfig: (config: any) => request.post('/admin/ai-config', config),
  deleteAiConfig: (id: number) => request.delete(`/admin/ai-config/${id}`),

  // 系统配置
  systemConfigs: () => request.get('/admin/system-configs'),
  updateSystemConfig: (key: string, value: string) =>
    request.put(`/admin/system-config/${key}`, { value })
}
