import axios, { type InternalAxiosRequestConfig, type AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { removeAdminToken, removeUserToken } from '@/utils/auth'

/** 后端 Result{code,msg,data} 结构（拦截器只关心这三个字段） */
interface ResultBody<T = unknown> {
  code: number
  msg: string | null
  data: T
}

/**
 * 鉴权 HTTP 实例的公开类型。
 * 由于响应拦截器已经把 Result.data 解包出来，这里把 get/post/put/delete
 * 的返回类型声明为 Promise<T>（而非 axios 默认的 Promise<AxiosResponse>），
 * 让业务层直接拿到 data，少一层解包。
 */
interface AuthHttp {
  get<T = unknown>(url: string, config?: Record<string, unknown>): Promise<T>
  post<T = unknown>(url: string, data?: unknown, config?: Record<string, unknown>): Promise<T>
  put<T = unknown>(url: string, data?: unknown, config?: Record<string, unknown>): Promise<T>
  delete<T = unknown>(url: string, config?: Record<string, unknown>): Promise<T>
}

/**
 * 创建一个鉴权 axios 实例。
 *
 * @param tokenKey localStorage 中的令牌 key（管理员 wl_admin_token / 用户 wl_user_token）
 * @param headerName 请求头名称（管理员 token / 用户 authentication）
 * @param clearToken 401 时需要清除的令牌函数
 */
function makeInstance(
  tokenKey: string,
  headerName: string,
  clearToken: () => void
): AuthHttp {
  const instance = axios.create({
    baseURL: '/api',
    timeout: 15000
  })

  // 请求拦截：从 localStorage 取令牌注入对应请求头
  instance.interceptors.request.use((config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem(tokenKey)
    if (token) {
      config.headers[headerName] = token
    }
    return config
  })

  // 响应拦截：Result.code===1 直接返回 data；否则报错并拒绝
  instance.interceptors.response.use(
    (response: AxiosResponse<ResultBody>) => {
      const body = response.data
      if (body && body.code === 1) {
        // 拦截器解包后返回 data，这里用 any 跳过 axios 默认的 AxiosResponse 约束
        return body.data as never
      }
      ElMessage.error(body?.msg || '请求失败')
      return Promise.reject(body)
    },
    (error) => {
      if (error.response?.status === 401) {
        // 令牌过期：清掉对应身份令牌并跳回登录页
        clearToken()
        ElMessage.error('登录已过期，请重新登录')
        router.push('/login')
      } else {
        ElMessage.error(error.message || '网络异常，请稍后重试')
      }
      return Promise.reject(error)
    }
  )

  // 拦截器改变了返回语义，用断言把实例类型收敛为 AuthHttp
  return instance as unknown as AuthHttp
}

/** 管理端 HTTP 实例：请求头 token，401 清管理员令牌 */
export const adminHttp = makeInstance('wl_admin_token', 'token', removeAdminToken)

/** 用户端 HTTP 实例：请求头 authentication，401 清用户令牌 */
export const userHttp = makeInstance('wl_user_token', 'authentication', removeUserToken)

export default adminHttp
