import { adminHttp } from '@/api/request'

/** 新增分类 */
export function addCategory(data: Record<string, unknown>) {
  return adminHttp.post('/admin/category', data)
}

/** 分类分页查询 */
export function pageCategory(params: { page: number; pageSize: number; name?: string; type?: number }) {
  return adminHttp.get('/admin/category/page', { params })
}

/** 删除分类 */
export function deleteCategory(id: number) {
  return adminHttp.delete('/admin/category', { params: { id } })
}

/** 修改分类 */
export function updateCategory(data: Record<string, unknown>) {
  return adminHttp.put('/admin/category', data)
}

/** 启用/禁用分类 */
export function updateCategoryStatus(status: number, id: number) {
  return adminHttp.post(`/admin/category/status/${status}`, null, { params: { id } })
}

/** 按类型查询分类 */
export function listCategory(type?: number) {
  return adminHttp.get('/admin/category/list', { params: { type } })
}
