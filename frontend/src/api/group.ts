import request from '@/utils/request'

export const groupApi = {
  list: (userId: number) => request.get(`/group/list/${userId}`),
  get: (groupId: number) => request.get(`/group/${groupId}`),
  create: (data: { name: string; avatar?: string; type?: string; ownerId: number }) =>
    request.post('/group/create', data),
  join: (groupId: number, userId: number, reason?: string) =>
    request.post('/group/join', { groupId, userId, reason: reason || '' }),
  update: (groupId: number, data: { name?: string; avatar?: string; announcement?: string }, operatorId: number) =>
    request.put(`/group/update/${groupId}?operatorId=${operatorId}`, data),
  members: (groupId: number) => request.get(`/group/members/${groupId}`),
  invite: (groupId: number, userIds: number[], inviterId: number) =>
    request.post(`/group/invite/${groupId}?inviterId=${inviterId}`, { userIds }),
  removeMember: (groupId: number, userId: number, operatorId: number) =>
    request.delete(`/group/member/${groupId}/${userId}?operatorId=${operatorId}`),
  leave: (groupId: number, userId: number) => request.post(`/group/leave/${groupId}?userId=${userId}`),
  dissolve: (groupId: number, userId: number) => request.delete(`/group/${groupId}?userId=${userId}`),
  joinRequests: (groupId: number) => request.get(`/group/join-requests/${groupId}`),
  myRequests: (userId: number) => request.get(`/group/my-requests/${userId}`),
  handleJoinRequest: (requestId: number, reviewerId: number, action: string) =>
    request.put(`/group/join-request/${requestId}?reviewerId=${reviewerId}&action=${action}`),
  mute: (groupId: number, targetUserId: number, operatorId: number, minutes?: number) =>
    request.put(`/group/mute/${groupId}/${targetUserId}?operatorId=${operatorId}&minutes=${minutes || 0}`),
  unmute: (groupId: number, targetUserId: number, operatorId: number) =>
    request.put(`/group/unmute/${groupId}/${targetUserId}?operatorId=${operatorId}`),
  setAdmin: (groupId: number, targetUserId: number, operatorId: number, isAdmin: boolean) =>
    request.put(`/group/admin/${groupId}/${targetUserId}?operatorId=${operatorId}&isAdmin=${isAdmin}`)
}
