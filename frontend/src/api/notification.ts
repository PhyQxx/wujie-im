import request from '@/utils/request'

export const notificationApi = {
  list: (params?: { page?: number; size?: number }) => request.get('/notification/list', { params }),
  unreadCount: () => request.get('/notification/unread/count'),
  markRead: (id: number) => request.put(`/notification/${id}/read`),
  markAllRead: () => request.put('/notification/read/all')
}
