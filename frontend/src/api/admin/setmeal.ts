import { adminHttp } from '@/api/request'

/** 新增套餐（含菜品） */
export function addSetmeal(data: Record<string, unknown>) {
  return adminHttp.post('/admin/setmeal', data)
}

/** 套餐分页查询 */
export function pageSetmeal(params: { page: number; pageSize: number; name?: string; categoryId?: number; status?: number }) {
  return adminHttp.get('/admin/setmeal/page', { params })
}

/** 批量删除套餐 */
export function deleteSetmeal(ids: number[]) {
  return adminHttp.delete('/admin/setmeal', { params: { ids: ids.join(',') } })
}

/** 根据 id 查询套餐（含菜品） */
export function getSetmealById(id: number) {
  return adminHttp.get(`/admin/setmeal/${id}`)
}

/** 修改套餐 */
export function updateSetmeal(data: Record<string, unknown>) {
  return adminHttp.put('/admin/setmeal', data)
}

/** 套餐起售/停售 */
export function updateSetmealStatus(status: number, id: number) {
  return adminHttp.post(`/admin/setmeal/status/${status}`, null, { params: { id } })
}
