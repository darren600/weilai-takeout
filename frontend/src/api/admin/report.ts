import { adminHttp } from '@/api/request'

/** 营业额统计 */
export function getTurnoverReport(begin: string, end: string) {
  return adminHttp.get('/admin/report/turnoverStatistics', { params: { begin, end } })
}

/** 用户统计 */
export function getUserReport(begin: string, end: string) {
  return adminHttp.get('/admin/report/userStatistics', { params: { begin, end } })
}

/** 订单统计 */
export function getOrderReport(begin: string, end: string) {
  return adminHttp.get('/admin/report/ordersStatistics', { params: { begin, end } })
}

/** 销量 Top10 排行 */
export function getTop10Report(begin: string, end: string) {
  return adminHttp.get('/admin/report/top10', { params: { begin, end } })
}

/** 导出运营数据报表（返回 blob，由页面触发下载） */
export function exportBusinessReport() {
  return adminHttp.get('/admin/report/export', { responseType: 'blob' })
}
