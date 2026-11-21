import { userHttp } from '@/api/request'

/** 加入购物车 */
export function addCart(data: { dishId?: number; setmealId?: number; dishFlavor?: string; number?: number }) {
  return userHttp.post('/user/shoppingCart/add', data)
}

/** 查看购物车 */
export function listCart() {
  return userHttp.get('/user/shoppingCart/list')
}

/** 清空购物车 */
export function cleanCart() {
  return userHttp.delete('/user/shoppingCart/clean')
}

/** 减少购物车中一个商品 */
export function subCart(data: { dishId?: number; setmealId?: number; dishFlavor?: string }) {
  return userHttp.post('/user/shoppingCart/sub', data)
}
