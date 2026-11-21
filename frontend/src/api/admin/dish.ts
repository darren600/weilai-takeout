import { adminHttp } from '@/api/request'

/** 新增菜品（含口味） */
export function addDish(data: Record<string, unknown>) {
  return adminHttp.post('/admin/dish', data)
}

/** 菜品分页查询 */
export function pageDish(params: { page: number; pageSize: number; name?: string; categoryId?: number; status?: number }) {
  return adminHttp.get('/admin/dish/page', { params })
}

/** 批量删除菜品 */
export function deleteDish(ids: number[]) {
  return adminHttp.delete('/admin/dish', { params: { ids: ids.join(',') } })
}

/** 根据 id 查询菜品（含口味） */
export function getDishById(id: number) {
  return adminHttp.get(`/admin/dish/${id}`)
}

/** 修改菜品 */
export function updateDish(data: Record<string, unknown>) {
  return adminHttp.put('/admin/dish', data)
}

/** 菜品起售/停售 */
export function updateDishStatus(status: number, id: number) {
  return adminHttp.post(`/admin/dish/status/${status}`, null, { params: { id } })
}

/** 按分类查询菜品 */
export function listDish(categoryId: number) {
  return adminHttp.get('/admin/dish/list', { params: { categoryId } })
}
