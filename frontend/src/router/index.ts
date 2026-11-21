import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import Login from '@/views/Login.vue'
import UserLayout from '@/layout/UserLayout.vue'
import AdminLayout from '@/layout/AdminLayout.vue'
import { getAdminToken, getUserToken } from '@/utils/auth'

/**
 * 完整路由表（T04/T05 页面已接入）：
 * - /user/* 嵌套在 UserLayout（顶栏 + 悬浮购物车栏）
 * - /admin/* 嵌套在 AdminLayout（侧栏 + 顶栏 + 铃铛）
 * - meta.role 标记所需身份，全局前置守卫据此分流
 */
const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: () => {
      if (getAdminToken()) return '/admin/dashboard'
      if (getUserToken()) return '/user/home'
      return '/login'
    }
  },
  {
    path: '/login',
    name: 'login',
    component: Login,
    meta: { title: '登录' }
  },

  // ===== 用户端（UserLayout）=====
  {
    path: '/user',
    component: UserLayout,
    redirect: '/user/home',
    children: [
      { path: 'home', name: 'user-home', component: () => import('@/views/user/Home.vue'), meta: { title: '味来点餐', role: 'user' } },
      { path: 'cart', name: 'user-cart', component: () => import('@/views/user/Cart.vue'), meta: { title: '购物车', role: 'user' } },
      { path: 'order/confirm', name: 'user-order-confirm', component: () => import('@/views/user/OrderConfirm.vue'), meta: { title: '确认订单', role: 'user' } },
      { path: 'order/list', name: 'user-order-list', component: () => import('@/views/user/OrderList.vue'), meta: { title: '我的订单', role: 'user' } },
      { path: 'order/:id', name: 'user-order-detail', component: () => import('@/views/user/OrderDetail.vue'), meta: { title: '订单详情', role: 'user' } },
      { path: 'address', name: 'user-address', component: () => import('@/views/user/Address.vue'), meta: { title: '地址管理', role: 'user' } },
      { path: 'profile', name: 'user-profile', component: () => import('@/views/user/Profile.vue'), meta: { title: '我的', role: 'user' } }
    ]
  },

  // ===== 管理端（AdminLayout）=====
  {
    path: '/admin',
    component: AdminLayout,
    redirect: '/admin/dashboard',
    children: [
      { path: 'dashboard', name: 'admin-dashboard', component: () => import('@/views/admin/Dashboard.vue'), meta: { title: '工作台', role: 'admin' } },
      { path: 'order', name: 'admin-order', component: () => import('@/views/admin/Order.vue'), meta: { title: '订单管理', role: 'admin' } },
      { path: 'dish', name: 'admin-dish', component: () => import('@/views/admin/Dish.vue'), meta: { title: '菜品管理', role: 'admin' } },
      { path: 'setmeal', name: 'admin-setmeal', component: () => import('@/views/admin/Setmeal.vue'), meta: { title: '套餐管理', role: 'admin' } },
      { path: 'category', name: 'admin-category', component: () => import('@/views/admin/Category.vue'), meta: { title: '分类管理', role: 'admin' } },
      { path: 'report', name: 'admin-report', component: () => import('@/views/admin/Report.vue'), meta: { title: '数据统计', role: 'admin' } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

/**
 * 全局前置守卫：身份分流。
 * - 需要 admin 身份而无 wl_admin_token → 回登录页
 * - 需要 user 身份而无 wl_user_token → 回登录页
 * - 已登录访问 /login → 直接回各自首页
 */
router.beforeEach((to) => {
  const adminToken = getAdminToken()
  const userToken = getUserToken()
  const requiredRole = to.meta.role as string | undefined

  if (requiredRole === 'admin' && !adminToken) return '/login'
  if (requiredRole === 'user' && !userToken) return '/login'
  if (to.path === '/login') {
    if (adminToken) return '/admin/dashboard'
    if (userToken) return '/user/home'
  }
  return true
})

export default router
