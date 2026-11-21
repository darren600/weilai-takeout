import { adminHttp } from '@/api/request'

/** 今日营业数据 */
export function getBusinessData() {
  return adminHttp.get('/admin/workspace/businessData')
}

/** 订单概览 */
export function getOrderOverview() {
  return adminHttp.get('/admin/workspace/overviewOrders')
}

/** 菜品概览 */
export function getDishOverview() {
  return adminHttp.get('/admin/workspace/overviewDishes')
}

/** 套餐概览 */
export function getSetmealOverview() {
  return adminHttp.get('/admin/workspace/overviewSetmeals')
}
