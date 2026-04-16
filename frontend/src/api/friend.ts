import request from '@/utils/request'

export const friendApi = {
  list: () => request.get('/friend/list'),
  requestList: () => request.get('/friend/request/list'),
  sendRequest: (toUserId: number, reason?: string) => request.post('/friend/request', { toUserId, reason }),
  agreeRequest: (requestId: number) => request.put(`/friend/request/${requestId}/agree`),
  rejectRequest: (requestId: number) => request.put(`/friend/request/${requestId}/reject`),
  delete: (friendId: number) => request.delete(`/friend/${friendId}`)
}
