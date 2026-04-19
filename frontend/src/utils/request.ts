import axios from 'axios'
import { ElMessage } from 'element-plus'
import { encrypt, decrypt } from './crypto'

// 创建 axios 实例
const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 不需要加密的接口
const NO_ENCRYPT_PATHS = ['/auth/login', '/auth/register', '/auth/refresh', '/auth/debug-verify', '/upload/image', '/upload/file']

function shouldEncrypt(url: string): boolean {
  return !NO_ENCRYPT_PATHS.some(path => url.includes(path))
}

// 请求拦截器：添加JWT
request.interceptors.request.use(config => {
  const token = localStorage.getItem('accessToken')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器：处理普通响应
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.msg || '请求失败')
      return Promise.reject(res)
    }
    return res
  },
  error => {
    if (error.response?.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      localStorage.removeItem('accessToken')
      window.location.href = '/login'
    } else {
      ElMessage.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

// 封装的请求方法
const api = {
  get: <T = any>(url: string, config?: any) => {
    if (shouldEncrypt(url)) {
      return fetchEncrypted('GET', url) as Promise<T>
    }
    return request.get<T>(url, config)
  },

  post: <T = any>(url: string, data?: any, config?: any) => {
    if (shouldEncrypt(url) && data !== undefined) {
      // 使用 fetch 发送加密请求
      return fetchEncrypted('POST', url, data) as Promise<T>
    }
    return request.post<T>(url, data, config)
  },

  put: <T = any>(url: string, data?: any, config?: any) => {
    if (shouldEncrypt(url) && data !== undefined) {
      // 使用 fetch 发送加密请求
      return fetchEncrypted('PUT', url, data) as Promise<T>
    }
    return request.put<T>(url, data, config)
  },

  delete: <T = any>(url: string, config?: any) => request.delete<T>(url, config)
}

// 使用 fetch 发送加密请求
async function fetchEncrypted(method: 'GET' | 'POST' | 'PUT', url: string, data?: any): Promise<any> {
  const token = localStorage.getItem('accessToken')

  const headers: Record<string, string> = {
    'Content-Type': 'application/json'
  }
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }

  let body
  if (method !== 'GET' && data !== undefined) {
    const requestJson = JSON.stringify(data)
    body = encrypt(requestJson)
  }

  const response = await fetch('/api' + url, {
    method,
    headers,
    body
  })

  const text = await response.text()
  console.log('[fetchEncrypted] url:', url, 'response前50:', text.substring(0, 50))

  // 检查是否是明文JSON响应
  let res
  if (text.trim().startsWith('{')) {
    // 明文JSON，直接解析
    console.log('[fetchEncrypted] 明文响应，直接解析')
    try {
      res = JSON.parse(text)
    } catch {
      throw new Error('响应格式错误')
    }
  } else {
    // 加密响应，需要解密
    let decryptedJson
    try {
      decryptedJson = decrypt(text)
    } catch (e: any) {
      console.error('[fetchEncrypted] 解密失败:', e.message)
      throw new Error('响应解密失败')
    }

    try {
      res = JSON.parse(decryptedJson)
    } catch {
      console.error('[fetchEncrypted] JSON解析失败, decrypted:', decryptedJson)
      throw new Error('响应格式错误')
    }
  }

  if (res.code !== 200) {
    ElMessage.error(res.msg || '请求失败')
    throw res
  }
  return res
}

export default api
