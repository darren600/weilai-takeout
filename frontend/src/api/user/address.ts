import { userHttp } from '@/api/request'

/** 查询当前用户全部地址 */
export function listAddress() {
  return userHttp.get('/user/addressBook/list')
}

/** 新增地址 */
export function addAddress(data: Record<string, unknown>) {
  return userHttp.post('/user/addressBook', data)
}

/** 根据 id 查询地址 */
export function getAddressById(id: number) {
  return userHttp.get(`/user/addressBook/${id}`)
}

/** 修改地址 */
export function updateAddress(data: Record<string, unknown>) {
  return userHttp.put('/user/addressBook', data)
}

/** 设置默认地址 */
export function setDefaultAddress(data: Record<string, unknown>) {
  return userHttp.put('/user/addressBook/default', data)
}

/** 查询默认地址 */
export function getDefaultAddress() {
  return userHttp.get('/user/addressBook/default')
}

/** 删除地址 */
export function deleteAddress(id: number) {
  return userHttp.delete('/user/addressBook', { params: { id } })
}
