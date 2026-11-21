import { ref } from 'vue'
import { defineStore } from 'pinia'
import { clearAllTokens, currentRole, type UserRole } from '@/utils/auth'

/**
 * 会话状态：只保存当前身份与购物车角标，不引持久化插件（令牌本身在 localStorage）。
 */
export const useSessionStore = defineStore('session', () => {
  /** 当前身份，默认从本地令牌推断 */
  const role = ref<UserRole>(currentRole())
  /** 购物车数量角标 */
  const cartCount = ref(0)

  /** 设置身份 */
  function setRole(nextRole: UserRole): void {
    role.value = nextRole
  }

  /** 更新购物车角标 */
  function setCartCount(count: number): void {
    cartCount.value = count
  }

  /** 退出登录：清令牌 + 重置状态 */
  function logout(): void {
    clearAllTokens()
    role.value = null
    cartCount.value = 0
  }

  return { role, cartCount, setRole, setCartCount, logout }
})
