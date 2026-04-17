import request from '@/utils/request'

export const friendApi = {
  list: (userId: number) => request.get(`/friend/list/${userId}`),
  requestList: (userId: number) => request.get(`/friend/requests/${userId}`),
  sendRequest: (fromUserId: number, toUserId: number, reason?: string) => request.post('/friend/request', { fromUserId, toUserId, reason }),
  agreeRequest: (requestId: number) => request.put(`/friend/request/${requestId}?action=AGREE`),
  rejectRequest: (requestId: number) => request.put(`/friend/request/${requestId}?action=REJECT`),
  delete: (userId: number, friendId: number) => request.delete(`/friend/${userId}/${friendId}`),
  moveToGroup: (userId: number, friendId: number, groupId: number) =>
    request.put('/friend/move', { userId, friendId, groupId }),
  setRemark: (userId: number, friendId: number, remark: string) =>
    request.put('/friend/remark', { userId, friendId, remark })
}
