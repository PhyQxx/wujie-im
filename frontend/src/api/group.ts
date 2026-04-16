import request from '@/utils/request'

export const groupApi = {
  list: () => request.get('/group/list'),
  create: (data: { name: string; memberIds: number[]; type?: string }) => request.post('/group/create', data),
  getById: (groupId: number) => request.get(`/group/${groupId}`),
  update: (groupId: number, data: any) => request.put(`/group/${groupId}`, data),
  members: (groupId: number) => request.get(`/group/${groupId}/members`),
  addMember: (groupId: number, userId: number) => request.post(`/group/${groupId}/member`, { userId }),
  removeMember: (groupId: number, userId: number) => request.delete(`/group/${groupId}/member/${userId}`),
  leave: (groupId: number) => request.post(`/group/${groupId}/leave`),
  dissolve: (groupId: number) => request.delete(`/group/${groupId}`),
  joinRequests: (groupId: number) => request.get(`/group/${groupId}/join-requests`),
  handleJoin: (requestId: number, action: 'agree' | 'reject') => request.put(`/group/join/${requestId}/${action}`)
}
