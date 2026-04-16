import { ElMessage } from 'element-plus'
import type { WsMessage } from '@/types'

class WsClient {
  private ws: WebSocket | null = null
  private url: string
  private handlers: Map<string, Function[]> = new Map()
  private reconnectTimer: number | null = null
  private heartbeatTimer: number | null = null

  constructor(url: string) {
    this.url = url
  }

  connect(token: string) {
    return new Promise<void>((resolve, _reject) => {
      this.ws = new WebSocket(this.url)

      this.ws.onopen = () => {
        this.send({ type: 'auth', token })
        this.startHeartbeat()
        resolve()
      }

      this.ws.onmessage = (event) => {
        try {
          const msg: WsMessage = JSON.parse(event.data)
          const handlers = this.handlers.get(msg.type)
          if (handlers) {
            handlers.forEach(h => h(msg.data))
          }
        } catch (e) {
          console.error('WsMessage parse error', e)
        }
      }

      this.ws.onerror = (error) => {
        console.error('WebSocket error', error)
        ElMessage.error('WebSocket连接错误')
      }

      this.ws.onclose = () => {
        this.stopHeartbeat()
        this.scheduleReconnect()
      }
    })
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
      const token = localStorage.getItem('accessToken')
      if (token) {
        this.connect(token)
      }
      this.reconnectTimer = null
    }, 5000)
  }

  close() {
    this.stopHeartbeat()
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }
    this.ws?.close()
  }
}

export const wsClient = new WsClient(`ws://${location.host}/ws`)

export default wsClient
