import request from '@/utils/request'

export const announcementApi = {
  list: (userId: number) => request.get('/announcement/list', { params: { userId } }),
  unread: (userId: number) => request.get('/announcement/unread', { params: { userId } }),
  markRead: (id: number, userId: number) => request.post(`/announcement/${id}/read`, null, { params: { userId } }),
  adminList: () => request.get('/admin/announcement/list'),
  adminCreate: (data: Record<string, unknown>) => request.post('/admin/announcement', data),
  adminUpdate: (id: number, data: Record<string, unknown>) => request.put(`/admin/announcement/${id}`, data),
  adminDelete: (id: number) => request.delete(`/admin/announcement/${id}`),
  adminPublish: (id: number, data: Record<string, unknown>) => request.post(`/admin/announcement/${id}/publish`, data),
  adminUnpublish: (id: number) => request.post(`/admin/announcement/${id}/unpublish`),
}
