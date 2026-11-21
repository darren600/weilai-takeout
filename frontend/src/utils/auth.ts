/**
 * 本地身份令牌工具。
 *
 * 全项目用两个 key 区分身份：管理员 wl_admin_token、用户 wl_user_token。
 * 两个令牌相互独立，路由守卫与 axios 拦截器都从这里读取。
 */

const ADMIN_TOKEN_KEY = 'wl_admin_token'
const USER_TOKEN_KEY = 'wl_user_token'

/** 身份类型：管理员 / 用户 / 未登录 */
export type UserRole = 'admin' | 'user' | null

/** 读取管理员令牌 */
export function getAdminToken(): string | null {
  return localStorage.getItem(ADMIN_TOKEN_KEY)
}

/** 写入管理员令牌 */
export function setAdminToken(token: string): void {
  localStorage.setItem(ADMIN_TOKEN_KEY, token)
}

/** 读取用户令牌 */
export function getUserToken(): string | null {
  return localStorage.getItem(USER_TOKEN_KEY)
}

/** 写入用户令牌 */
export function setUserToken(token: string): void {
  localStorage.setItem(USER_TOKEN_KEY, token)
}

/** 清除管理员令牌 */
export function removeAdminToken(): void {
  localStorage.removeItem(ADMIN_TOKEN_KEY)
}

/** 清除用户令牌 */
export function removeUserToken(): void {
  localStorage.removeItem(USER_TOKEN_KEY)
}

/** 清除全部令牌（退出登录） */
export function clearAllTokens(): void {
  removeAdminToken()
  removeUserToken()
}

/** 判定当前身份：管理员优先，其次用户，都没有则未登录 */
export function currentRole(): UserRole {
  if (getAdminToken()) return 'admin'
  if (getUserToken()) return 'user'
  return null
}
