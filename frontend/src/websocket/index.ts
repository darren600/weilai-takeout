/**
 * 管理端 WebSocket 轻量封装：连接后端 /ws/{sid}，接收来单(type=1)/催单(type=2)推送。
 * 只维护一条连接 + 一组回调，页面按需订阅，退出时不强制关闭（布局常驻）。
 */

/** 推送消息结构（与后端 WebSocketServer 群发 JSON 一致） */
export interface OrderPushMessage {
  type: number
  orderId: number
  content: string
}

type MessageHandler = (message: OrderPushMessage) => void

const handlers = new Set<MessageHandler>()
let socket: WebSocket | null = null
let connected = false

/** 订阅推送，返回取消订阅函数 */
export function subscribeOrderPush(handler: MessageHandler): () => void {
  handlers.add(handler)
  return () => handlers.delete(handler)
}

/** 建立连接（sid 用固定值 admin-${时间戳}，避免多标签页互相顶掉可用随机段） */
export function connectOrderPush(): void {
  if (socket && (connected || socket.readyState === WebSocket.OPEN)) return
  const sid = `admin-${Date.now()}`
  const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws'
  const url = `${protocol}://${window.location.host}/ws/${sid}`
  try {
    socket = new WebSocket(url)
    socket.onopen = () => { connected = true }
    socket.onmessage = (event) => {
      try {
        const message = JSON.parse(event.data) as OrderPushMessage
        handlers.forEach((handler) => handler(message))
      } catch {
        // 非 JSON 消息忽略
      }
    }
    socket.onclose = () => { connected = false }
    socket.onerror = () => { connected = false }
  } catch {
    connected = false
  }
}

/** 断开连接（一般无需调用） */
export function disconnectOrderPush(): void {
  socket?.close()
  socket = null
  connected = false
}
