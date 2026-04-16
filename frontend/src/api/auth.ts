import request from '@/utils/request'

export const authApi = {
  login: (username: string, password: string) =>
    request.post('/auth/login', { username, password }),

  register: (data: { username: string; password: string; phone?: string; email?: string }) =>
    request.post('/auth/register', data),

  refreshToken: (refreshToken: string) =>
    request.post('/auth/refresh', { refreshToken }),

  logout: () => request.post('/auth/logout')
}
