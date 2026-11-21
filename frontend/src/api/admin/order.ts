import { adminHttp } from '@/api/request'

/** 订单搜索（分页） */
export function conditionSearchOrder(params: Record<string, unknown>) {
  return adminHttp.get('/admin/order/conditionSearch', { params })
}

/** 各状态订单数量统计 */
export function countOrderByStatus() {
  return adminHttp.get('/admin/order/statistics')
}

/** 查询订单详情 */
export function getOrderDetail(id: number) {
  return adminHttp.get(`/admin/order/details/${id}`)
}

/** 接单 */
export function confirmOrder(data: { id: number }) {
  return adminHttp.put('/admin/order/confirm', data)
}

/** 拒单 */
export function rejectOrder(data: { id: number; rejectionReason: string }) {
  return adminHttp.put('/admin/order/rejection', data)
}

/** 取消订单 */
export function cancelOrder(data: { id: number; cancelReason: string }) {
  return adminHttp.put('/admin/order/cancel', data)
}

/** 派送订单 */
export function deliverOrder(id: number) {
  return adminHttp.put(`/admin/order/delivery/${id}`)
}

/** 完成订单 */
export function completeOrder(id: number) {
  return adminHttp.put(`/admin/order/complete/${id}`)
}
