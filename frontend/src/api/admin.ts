import request from '@/utils/request'

export const adminApi = {
  users: (params?: { page?: number; size?: number; keyword?: string }) =>
    request.get('/admin/users', { params }),
  updateUserStatus: (userId: number, status: number) =>
    request.put(`/admin/user/${userId}/status`, { status }),
  resetPassword: (userId: number, password: string) =>
    request.put(`/admin/user/${userId}/password`, { password }),
  stats: () => request.get('/admin/stats'),
  systemConfig: () => request.get('/admin/config'),
  updateConfig: (key: string, value: string) =>
    request.put('/admin/config', { key, value }),
  robots: (params?: any) => request.get('/admin/robots', { params }),
  groups: (params?: any) => request.get('/admin/groups', { params })
}
