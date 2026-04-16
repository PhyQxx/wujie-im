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

  // 群聊管理
  groups: (keyword?: string) =>
    request.get('/admin/groups', { params: { keyword } }),
  dissolveGroup: (groupId: number) =>
    request.delete(`/admin/groups/${groupId}`),
  groupMemberCount: (groupId: number) =>
    request.get(`/admin/groups/${groupId}/members`),

  // 机器人管理
  robots: (keyword?: string) =>
    request.get('/admin/robots', { params: { keyword } }),

  // 敏感词
  sensitiveWords: () => request.get('/admin/sensitive-words'),
  addSensitiveWord: (word: { word: string; level?: number }) =>
    request.post('/admin/sensitive-word', word),
  deleteSensitiveWord: (id: number) =>
    request.delete(`/admin/sensitive-word/${id}`)
}
