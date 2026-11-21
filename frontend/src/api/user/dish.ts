import { userHttp } from '@/api/request'

/** 按分类查询在售菜品（含口味）；不传 categoryId 时查询全部 */
export function listDish(categoryId?: number) {
  return userHttp.get('/user/dish/list', { params: { categoryId } })
}
