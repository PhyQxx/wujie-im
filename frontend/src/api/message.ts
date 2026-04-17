import request from '@/utils/request'

export const messageApi = {
  list: (params: { conversationId: number; page?: number; size?: number }) =>
    request.get('/message/list', { params }),
  send: (data: { conversationId: number; content: string; contentType: string; replyId?: number; meta?: string }) =>
    request.post('/message/send', data),
  recall: (messageId: number) => request.put(`/message/${messageId}/recall`),
  uploadImage: (formData: FormData) =>
    request.post('/upload/image', formData, { headers: { 'Content-Type': 'multipart/form-data' } }),
  uploadFile: (formData: FormData) =>
    request.post('/upload/file', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
}

