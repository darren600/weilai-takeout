import { userHttp } from '@/api/request'

/** 提交订单 */
export function submitOrder(data: Record<string, unknown>) {
  return userHttp.post('/user/order/submit', data)
}

/** 历史订单分页查询 */
export function pageUserOrder(params: { page: number; pageSize: number; status?: number }) {
  return userHttp.get('/user/order/historyOrders', { params })
}

/** 查询订单详情 */
export function getUserOrderDetail(id: number) {
  return userHttp.get(`/user/order/orderDetail/${id}`)
}

/** 取消订单 */
export function cancelUserOrder(id: number) {
  return userHttp.put(`/user/order/cancel/${id}`)
}

/** 再来一单 */
export function repetitionOrder(id: number) {
  return userHttp.post(`/user/order/repetition/${id}`)
}

/** 催单 */
export function reminderOrder(id: number) {
  return userHttp.get(`/user/order/reminder/${id}`)
}
