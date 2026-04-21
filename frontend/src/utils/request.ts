import axios from 'axios'
import { ElMessage } from 'element-plus'
import { encrypt, decrypt } from './crypto'

// 创建 axios 实例
const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

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

/**
 * 只对 /api/message/* 接口的 data 字段加解密
 * 请求格式: { data: AES(业务参数JSON) }
 * 响应格式: { code, msg, data: AES(业务数据) }
 */
async function messageCryptoFetch(method: 'POST' | 'PUT', url: string, data?: any): Promise<any> {
  const token = localStorage.getItem('accessToken')
  const headers: Record<string, string> = { 'Content-Type': 'application/json' }
  if (token) headers['Authorization'] = `Bearer ${token}`

  let body: string | undefined
  if (data !== undefined) {
    // 只加密 data 字段
    const encryptedData = encrypt(JSON.stringify(data))
    body = JSON.stringify({ data: encryptedData })
  }

  const response = await fetch('/api' + url, { method, headers, body })
  const text = await response.text()

  let res: any
  try {
    res = JSON.parse(text)
  } catch {
    throw new Error('响应格式错误')
  }

  // 解密响应中的 data 字段
  if (res && res.data !== undefined && res.data !== null && typeof res.data === 'string') {
    try {
      res.data = JSON.parse(decrypt(res.data))
    } catch (e: any) {
      console.warn('[messageCrypto] 解密响应 data 失败:', e.message)
    }
  }

  if (res.code !== 200) {
    ElMessage.error(res.msg || '请求失败')
    throw res
  }
  return res
}

/** GET 请求的加密响应解密（用于 /api/message/* 和 /api/conversation/*） */
async function cryptoGetFetch(url: string, config?: any): Promise<any> {
  const token = localStorage.getItem('accessToken')
  const headers: Record<string, string> = {}
  if (token) headers['Authorization'] = `Bearer ${token}`

  // 附加 query params
  let finalUrl = '/api' + url
  if (config?.params) {
    const searchParams = new URLSearchParams()
    for (const [k, v] of Object.entries(config.params)) {
      if (v !== undefined && v !== null) searchParams.append(k, String(v))
    }
    const qs = searchParams.toString()
    if (qs) finalUrl += '?' + qs
  }

  const response = await fetch(finalUrl, { method: 'GET', headers })
  const text = await response.text()

  let res: any
  try {
    res = JSON.parse(text)
  } catch {
    throw new Error('响应格式错误')
  }

  // 解密响应中的 data 字段
  if (res && res.data !== undefined && res.data !== null && typeof res.data === 'string') {
    try {
      res.data = JSON.parse(decrypt(res.data))
    } catch (e: any) {
      console.warn('[cryptoGet] 解密响应 data 失败:', e.message)
    }
  }

  if (res.code !== 200) {
    ElMessage.error(res.msg || '请求失败')
    throw res
  }
  return res
}

// 封装的请求方法
const api = {
  get: <T = any>(url: string, config?: any) => {
    if (url.startsWith('/message') || url.startsWith('/conversation')) {
      return cryptoGetFetch(url, config) as Promise<T>
    }
    return request.get<T>(url, config)
  },

  post: <T = any>(url: string, data?: any, config?: any) => {
    if (url.startsWith('/message')) {
      return messageCryptoFetch('POST', url, data) as Promise<T>
    }
    return request.post<T>(url, data, config)
  },

  put: <T = any>(url: string, data?: any, config?: any) => {
    if (url.startsWith('/message')) {
      return messageCryptoFetch('PUT', url, data) as Promise<T>
    }
    return request.put<T>(url, data, config)
  },

  delete: <T = any>(url: string, config?: any) =>
    request.delete<T>(url, config)
}

export default api
