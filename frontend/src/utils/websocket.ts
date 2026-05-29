import { ElMessage } from 'element-plus'
import { decrypt } from './crypto'
import type { WsMessage } from '@/types'

class WsClient {
  private ws: WebSocket | null = null
  private url: string
  private handlers: Map<string, Function[]> = new Map()
  private reconnectTimer: number | null = null
  private heartbeatTimer: number | null = null
  private manualClose = false

  constructor(url: string) {
    this.url = url
  }

  connect(token: string) {
    if (this.ws?.readyState === WebSocket.OPEN || this.ws?.readyState === WebSocket.CONNECTING) return
    this.manualClose = false
    this.stopHeartbeat()
    this.ws?.close()
    this.ws = new WebSocket(this.url)

    this.ws.onopen = () => {
      // 使用 refreshToken 认证（有效期7天），避免 accessToken（15分钟）过期导致 WS 掉线
      const refreshToken = localStorage.getItem('refreshToken')
      this.send({ type: 'auth', token: refreshToken })
      this.startHeartbeat()
    }

    this.ws.onmessage = (event) => {
      console.log('[WS] Raw message received:', event.data)
      try {
        const raw = JSON.parse(event.data)
        const msg: WsMessage = { type: raw.type, data: raw.data }
        console.log('[WS] Parsed message type:', msg.type)
        
        // type=message 时 data 字段是加密的，需要解密
        if (msg.type === 'message' && typeof msg.data === 'string') {
          try {
            console.log('[WS] Decrypting message data...')
            const decrypted = decrypt(msg.data)
            console.log('[WS] Decrypted JSON:', decrypted)
            msg.data = JSON.parse(decrypted)
          } catch (e) {
            console.error('[WS] 解密 message data 失败', e)
          }
        }
        
        const handlers = this.handlers.get(msg.type)
        if (handlers) {
          console.log(`[WS] Calling ${handlers.length} handlers for type ${msg.type}`)
          handlers.forEach(h => h(msg.data))
        } else {
          console.warn('[WS] No handlers for message type:', msg.type)
        }
      } catch (e) {
        console.error('WsMessage parse error', e)
      }
    }

    this.ws.onerror = (error) => {
      console.error('WebSocket error', error)
    }

    this.ws.onclose = () => {
      this.stopHeartbeat()
      if (!this.manualClose) {
        this.scheduleReconnect()
      }
    }
  }

  send(data: any) {
    if (this.ws?.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(data))
    }
  }

  on(type: string, handler: Function) {
    if (!this.handlers.has(type)) {
      this.handlers.set(type, [])
    }
    this.handlers.get(type)!.push(handler)
  }

  off(type: string, handler: Function) {
    const handlers = this.handlers.get(type)
    if (handlers) {
      const idx = handlers.indexOf(handler)
      if (idx > -1) handlers.splice(idx, 1)
    }
  }

  private startHeartbeat() {
    this.heartbeatTimer = window.setInterval(() => {
      this.send({ type: 'heartbeat', ts: Date.now() })
    }, 30000)
  }

  private stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }
  }

  private scheduleReconnect() {
    if (this.reconnectTimer) return
    this.reconnectTimer = window.setTimeout(() => {
      const refreshToken = localStorage.getItem('refreshToken')
      const accessToken = localStorage.getItem('accessToken')
      if (refreshToken || accessToken) {
        this.connect(refreshToken || accessToken!)
      }
      this.reconnectTimer = null
    }, 3000)
  }

  close() {
    this.manualClose = true
    this.stopHeartbeat()
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }
    this.ws?.close()
    this.ws = null
  }
}

const wsProtocol = location.protocol === 'https:' ? 'wss:' : 'ws:'
export const wsClient = new WsClient(`${wsProtocol}//${location.host}/ws`)

export default wsClient
