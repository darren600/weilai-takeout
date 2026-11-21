import { userHttp } from '@/api/request'

/** 按分类查询在售套餐；不传 categoryId 时查询全部 */
export function listSetmeal(categoryId?: number) {
  return userHttp.get('/user/setmeal/list', { params: { categoryId } })
}

/** 查询套餐内菜品明细 */
export function listSetmealDish(id: number) {
  return userHttp.get(`/user/setmeal/dish/${id}`)
}
