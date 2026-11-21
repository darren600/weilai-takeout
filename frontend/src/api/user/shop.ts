import { userHttp } from '@/api/request'

/** 查询店铺营业状态 */
export function getShopStatus() {
  return userHttp.get('/user/shop/status')
}
