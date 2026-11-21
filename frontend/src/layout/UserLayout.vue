<template>
  <!-- 用户端 H5：桌面居中显示手机容器，移动端铺满 -->
  <div class="user-page">
    <div class="phone-shell">
      <!-- App 顶栏：定位/店铺 + 搜索 + 购物车 -->
      <header class="app-header">
        <div class="header-top">
          <div class="header-left">
            <!-- 非首页显示返回箭头，统一回首页（不清空购物车） -->
            <button v-if="showBack" class="back-btn" type="button" @click="goHome">
              <el-icon :size="20"><ArrowLeft /></el-icon>
            </button>
            <div class="shop-loc" @click="router.push('/user/home')">
              <el-icon :size="18"><Location /></el-icon>
              <span class="shop-name">味来 · 兴宁店</span>
              <el-icon :size="14"><ArrowDown /></el-icon>
            </div>
          </div>
          <div class="header-actions">
            <button class="icon-btn" type="button" @click="toggleSearch">
              <el-icon :size="20"><Search /></el-icon>
            </button>
            <el-badge :value="session.cartCount" :hidden="session.cartCount === 0" class="cart-badge">
              <button class="icon-btn" type="button" @click="router.push('/user/cart')">
                <el-icon :size="20"><ShoppingCart /></el-icon>
              </button>
            </el-badge>
          </div>
        </div>

        <!-- 可展开搜索框 -->
        <div v-if="searchOpen" class="header-search">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索菜品 / 套餐"
            :prefix-icon="Search"
            size="small"
            clearable
            autofocus
            @keyup.enter="handleSearch"
          />
        </div>
      </header>

      <!-- 内容区：可滚动 -->
      <main class="app-content">
        <RouterView v-slot="{ Component }">
          <component :is="Component" @cart-changed="refreshCartCount"></component>
        </RouterView>
      </main>

      <!-- 首页右下角圆形悬浮购物车按钮 -->
      <button v-if="showFloatCart" class="float-cart" type="button" @click="router.push('/user/cart')">
        <el-icon :size="24"><ShoppingCart /></el-icon>
        <span v-if="session.cartCount > 0" class="float-count">{{ session.cartCount }}</span>
      </button>

      <!-- App 底部 Tab 栏 -->
      <nav class="app-tabbar">
        <button
          v-for="tab in tabs"
          :key="tab.path"
          type="button"
          class="tab-item"
          :class="{ active: isTabActive(tab) }"
          @click="router.push(tab.path)"
        >
          <el-icon :size="20"><component :is="tab.icon"></component></el-icon>
          <span>{{ tab.label }}</span>
        </button>
      </nav>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter, RouterView } from 'vue-router'
import {
  Location, ArrowDown, ArrowLeft, Search, ShoppingCart, HomeFilled, Tickets, User
} from '@element-plus/icons-vue'
import { useSessionStore } from '@/stores/session'
import { listCart } from '@/api/user/cart'
import type { Component } from 'vue'

interface TabItem { label: string; path: string; icon: Component }

const route = useRoute()
const router = useRouter()
const session = useSessionStore()
const searchKeyword = ref('')
const searchOpen = ref(false)

/** 底部 Tab 配置：首页 / 订单 / 我的 */
const tabs: TabItem[] = [
  { label: '首页', path: '/user/home', icon: HomeFilled },
  { label: '订单', path: '/user/order/list', icon: Tickets },
  { label: '我的', path: '/user/profile', icon: User }
]

/** 悬浮购物车仅在首页展示（购物车/结算页不需要） */
const showFloatCart = computed(() => route.path === '/user/home')

/** 非首页显示返回箭头 */
const showBack = computed(() => route.path !== '/user/home')

/** 返回首页（仅路由跳转，不清空购物车，用户可继续点单） */
function goHome(): void {
  router.push('/user/home')
}

/** 判断 Tab 是否选中（订单详情等子页归到「订单」） */
function isTabActive(tab: TabItem): boolean {
  if (tab.path === '/user/home') return route.path === '/user/home'
  if (tab.path === '/user/order/list') return route.path.startsWith('/user/order')
  return route.path === '/user/profile'
}

/** 刷新购物车角标 */
async function refreshCartCount(): Promise<void> {
  try {
    const items = (await listCart()) as Array<{ number: number }>
    session.setCartCount((items || []).reduce((sum, item) => sum + (item.number || 0), 0))
  } catch {
    session.setCartCount(0)
  }
}

function toggleSearch(): void {
  searchOpen.value = !searchOpen.value
}

/** 搜索：携关键字跳首页，Home 监听 route.query.keyword */
function handleSearch(): void {
  router.push({ path: '/user/home', query: { keyword: searchKeyword.value } })
}

onMounted(refreshCartCount)
</script>

<style scoped>
/* 外层：衬托手机容器的渐变背景 */
.user-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  background: linear-gradient(135deg, #ffe9dc 0%, #fff8f2 45%, #f0f4ff 100%);
}

/* 手机容器 */
.phone-shell {
  position: relative;
  width: 390px;
  height: min(844px, calc(100vh - 32px));
  display: flex;
  flex-direction: column;
  background: var(--wl-bg);
  border-radius: 28px;
  box-shadow: 0 24px 70px rgba(0, 0, 0, 0.18);
  overflow: hidden;
}

/* 顶栏 */
.app-header {
  flex: 0 0 auto;
  background: linear-gradient(120deg, #9c3d18 0%, #d9541f 48%, #ff6b35 100%);
  color: #fff;
  padding: 10px 14px;
  box-shadow: 0 2px 10px rgba(120, 52, 20, 0.28);
  z-index: 5;
}
.header-top {
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 4px;
  min-width: 0;
}
.back-btn {
  border: none;
  background: transparent;
  color: #fff;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  flex: 0 0 auto;
  margin-right: 2px;
}
.back-btn:active {
  background: rgba(255, 255, 255, 0.18);
}
.shop-loc {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  font-size: 15px;
  font-weight: 600;
  max-width: 210px;
}
.shop-name {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.header-actions {
  display: flex;
  align-items: center;
  gap: 6px;
}
.icon-btn {
  border: none;
  background: transparent;
  color: #fff;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}
.icon-btn:active {
  background: rgba(255, 255, 255, 0.18);
}
.cart-badge :deep(.el-badge__content) {
  background: var(--wl-secondary);
  color: #2b2b2b;
  font-weight: 700;
  border: none;
}
.header-search {
  margin-top: 8px;
}
.header-search :deep(.el-input__wrapper) {
  border-radius: 18px;
}

/* 内容区滚动 */
.app-content {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 12px 12px 16px;
  -webkit-overflow-scrolling: touch;
}

/* 圆形悬浮购物车 */
.float-cart {
  position: absolute;
  right: 16px;
  bottom: 76px;
  width: 52px;
  height: 52px;
  border: none;
  border-radius: 50%;
  background: var(--wl-primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 8px 20px rgba(255, 107, 53, 0.45);
  z-index: 6;
}
.float-cart:active {
  transform: scale(0.94);
}
.float-count {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 20px;
  height: 20px;
  padding: 0 5px;
  border-radius: 10px;
  background: var(--wl-secondary);
  color: #2b2b2b;
  font-size: 12px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 底部 Tab 栏 */
.app-tabbar {
  flex: 0 0 auto;
  height: 56px;
  display: flex;
  background: #fff;
  border-top: 1px solid var(--wl-card-border);
}
.tab-item {
  flex: 1;
  border: none;
  background: transparent;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  color: #9a9a9a;
  font-size: 11px;
  cursor: pointer;
  transition: color 0.2s;
}
.tab-item.active {
  color: var(--wl-primary);
  font-weight: 600;
}

/* 移动端：铺满全屏，去掉圆角与阴影 */
@media (max-width: 480px) {
  .user-page {
    padding: 0;
    background: var(--wl-bg);
  }
  .phone-shell {
    width: 100%;
    height: 100vh;
    max-height: none;
    border-radius: 0;
    box-shadow: none;
  }
}
</style>
