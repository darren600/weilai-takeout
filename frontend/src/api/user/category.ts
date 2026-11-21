import { userHttp } from '@/api/request'

/** 按类型查询分类（1 菜品 2 套餐） */
export function listCategory(type: number) {
  return userHttp.get('/user/category/list', { params: { type } })
}
