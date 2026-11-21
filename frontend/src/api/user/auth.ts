import { userHttp } from '@/api/request'

/** 微信登录（C 端小程序入口，浏览器演示一般用 mockLogin） */
export function wxLogin<T = unknown>(data: { code: string }) {
  return userHttp.post<T>('/user/user/login', data)
}

/** 网站登录（手机号，仅 dev 环境开放） */
export function mockLogin<T = unknown>(data: { phone: string }) {
  return userHttp.post<T>('/user/user/mockLogin', data)
}

/**
 * 发送短信验证码（仅 dev 环境开放）。
 * 无真实短信通道，dev 环境后端会把 6 位验证码直接放在 data 中返回，供演示自动填入。
 */
export function sendCode<T = string>(phone: string) {
  return userHttp.post<T>('/user/user/sendCode', { phone })
}

/** 短信验证码登录 / 自动注册（仅 dev 环境开放），未注册手机号会自动注册 */
export function smsLogin<T = unknown>(data: { phone: string; code: string }) {
  return userHttp.post<T>('/user/user/smsLogin', data)
}
