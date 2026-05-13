import axios, { type AxiosRequestConfig, type InternalAxiosRequestConfig, type AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import { encrypt, decrypt } from './crypto'

// 需要加解密的接口路径前缀
const CRYPTO_PATHS = ['/message', '/conversation']

// 判断是否需要加解密
function needsCrypto(url: string): boolean {
  return CRYPTO_PATHS.some(path => url.startsWith(path))
}

// 创建 axios 实例
const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器：添加 JWT + 请求加密
request.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = localStorage.getItem('accessToken')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }

  // 对 /message 和 /conversation 的 POST/PUT 请求加密 data 字段
  if (config.data && needsCrypto(config.url || '') && ['POST', 'PUT'].includes(config.method?.toUpperCase() || '')) {
    const encryptedData = encrypt(JSON.stringify(config.data))
    config.data = { data: encryptedData }
  }

  return config
})

// 响应拦截器：处理响应加密
request.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    const url = response.config.url || ''

    // 对 /message 和 /conversation 的响应 data 字段解密
    if (needsCrypto(url) && res?.data && typeof res.data === 'string') {
      try {
        res.data = JSON.parse(decrypt(res.data))
      } catch (e: any) {
        console.warn('[crypto] 解密响应 data 失败:', e.message)
      }
    }

    if (res.code !== 200) {
      ElMessage.error(res.msg || '请求失败')
      return Promise.reject(res)
    }
    return res
  },
  (error) => {
    if (error.response?.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      window.location.href = '/login'
    } else {
      ElMessage.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

// 封装的请求方法
const api = {
  get: <T = any>(url: string, config?: AxiosRequestConfig) =>
    request.get<T>(url, config),

  post: <T = any>(url: string, data?: any, config?: AxiosRequestConfig) =>
    request.post<T>(url, data, config),

  put: <T = any>(url: string, data?: any, config?: AxiosRequestConfig) =>
    request.put<T>(url, data, config),

  delete: <T = any>(url: string, config?: AxiosRequestConfig) =>
    request.delete<T>(url, config)
}

export default api
