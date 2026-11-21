import { adminHttp } from '@/api/request'

/** 管理员登录（无需令牌） */
export function login<T = unknown>(data: { username: string; password: string }) {
  return adminHttp.post<T>('/admin/employee/login', data)
}

/** 管理员退出 */
export function logout() {
  return adminHttp.post('/admin/employee/logout')
}

/** 新增员工 */
export function addEmployee(data: Record<string, unknown>) {
  return adminHttp.post('/admin/employee', data)
}

/** 员工分页查询 */
export function pageEmployee(params: { page: number; pageSize: number; name?: string }) {
  return adminHttp.get('/admin/employee/page', { params })
}

/** 启用/禁用员工 */
export function updateEmployeeStatus(status: number, id: number) {
  return adminHttp.post(`/admin/employee/status/${status}`, null, { params: { id } })
}

/** 根据 id 查询员工 */
export function getEmployeeById(id: number) {
  return adminHttp.get(`/admin/employee/${id}`)
}

/** 编辑员工 */
export function updateEmployee(data: Record<string, unknown>) {
  return adminHttp.put('/admin/employee', data)
}
