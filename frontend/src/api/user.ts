import request from '@/utils/request'

export const userApi = {
  getById: (id: number) => request.get(`/user/${id}`),
  search: (keyword: string) => request.get('/user/list', { params: { keyword } }),
  update: (id: number, data: any) => request.put(`/user/${id}`, data),
  updateStatus: (status: string) => request.put('/user/status', { status }),
  uploadAvatar: (formData: FormData) => request.post('/user/avatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
