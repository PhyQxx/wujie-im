import request from '@/utils/request'

export const messageApi = {
  list: (params: { conversationId: number; page?: number; size?: number }) =>
    request.get('/message/list', { params }),
  send: (data: { conversationId: number; content: string; contentType: string; replyId?: number }) =>
    request.post('/message/send', data),
  recall: (messageId: number) => request.put(`/message/${messageId}/recall`),
  uploadFile: (formData: FormData) => request.post('/message/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
