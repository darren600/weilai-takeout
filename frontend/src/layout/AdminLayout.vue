<template>
  <!-- 管理端布局：白色侧栏 + 橙色选中 + 白色顶栏 + 内容区撑满 -->
  <div class="admin-layout">
    <aside class="admin-sidebar">
      <div class="sidebar-brand">
        <span class="brand-logo"><el-icon :size="20"><Bowl /></el-icon></span>
        <span class="brand-text">味来 · 管理后台</span>
      </div>
      <el-menu
        :default-active="route.path"
        class="side-menu"
        background-color="#ffffff"
        text-color="#5a6572"
        active-text-color="#ff6b35"
        router
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataBoard /></el-icon><span>工作台</span>
        </el-menu-item>
        <el-menu-item index="/admin/order">
          <el-icon><List /></el-icon><span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/dish">
          <el-icon><Dish /></el-icon><span>菜品管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/setmeal">
          <el-icon><SetUp /></el-icon><span>套餐管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/category">
          <el-icon><Files /></el-icon><span>分类管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/report">
          <el-icon><TrendCharts /></el-icon><span>数据统计</span>
        </el-menu-item>
      </el-menu>
    </aside>

    <div class="admin-main">
      <header class="admin-header">
        <div class="breadcrumb">
          <span>首页</span>
          <el-icon><ArrowRight /></el-icon>
          <span class="current">{{ currentTitle }}</span>
        </div>
        <div class="header-right">
          <!-- 来单/催单铃铛 -->
          <el-popover placement="bottom-end" :width="320" trigger="click">
            <template #reference>
              <el-badge :value="notices.length" :hidden="notices.length === 0" class="notice-badge">
                <el-icon :size="20" class="bell-icon"><Bell /></el-icon>
              </el-badge>
            </template>
            <div class="notice-panel">
              <div class="notice-title">
                <span>消息通知</span>
                <el-button link type="primary" size="small" @click="clearNotices">清空</el-button>
              </div>
              <el-empty v-if="notices.length === 0" description="暂无新消息" :image-size="50" />
              <div v-for="(n, i) in notices" :key="i" class="notice-item">
                <el-tag :type="n.type === 2 ? 'danger' : 'warning'" size="small">
                  {{ n.type === 2 ? '催单' : '新订单' }}
                </el-tag>
                <span>{{ n.content }}</span>
              </div>
            </div>
          </el-popover>

          <span class="admin-name">
            <el-icon :size="18"><Avatar /></el-icon> 管理员
          </span>
          <el-button type="primary" plain size="small" @click="handleLogout">退出</el-button>
        </div>
      </header>

      <main class="admin-content">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter, RouterView } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Bowl, DataBoard, List, Dish, SetUp, Files, TrendCharts, ArrowRight, Bell, Avatar
} from '@element-plus/icons-vue'
import { useSessionStore } from '@/stores/session'
import { subscribeOrderPush, connectOrderPush, type OrderPushMessage } from '@/websocket'

interface Notice { type: number; content: string }

const route = useRoute()
const router = useRouter()
const session = useSessionStore()
const notices = ref<Notice[]>([])

const currentTitle = computed(() => (route.meta.title as string) || '工作台')
let unsubscribe: (() => void) | null = null

/** 收到来单/催单推送：加入通知列表并轻提示 */
function onOrderPush(message: OrderPushMessage): void {
  notices.value.unshift({ type: message.type, content: message.content })
  if (notices.value.length > 20) notices.value.pop()
  ElMessage[message.type === 2 ? 'warning' : 'info'](message.content)
}

function clearNotices(): void {
  notices.value = []
}

async function handleLogout(): Promise<void> {
  await ElMessageBox.confirm('确认退出登录吗？', '提示', { type: 'warning' })
  session.logout()
  router.push('/login')
}

onMounted(() => {
  unsubscribe = subscribeOrderPush(onOrderPush)
  connectOrderPush()
})
onUnmounted(() => { unsubscribe?.() })
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: #f5f7fa;
}

/* 白色侧栏（在流内 + sticky，避免 fixed 导致内容右侧空缺） */
.admin-sidebar {
  width: 220px;
  flex-shrink: 0;
  align-self: flex-start;
  position: sticky;
  top: 0;
  height: 100vh;
  overflow-y: auto;
  background: #fff;
  border-right: 1px solid #eef0f3;
}
.sidebar-brand {
  height: 56px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 18px;
  border-bottom: 1px solid #f0f2f5;
}
.brand-logo {
  width: 30px;
  height: 30px;
  border-radius: 9px;
  background: linear-gradient(135deg, var(--wl-primary), var(--wl-primary-light));
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}
.brand-text {
  font-size: 15px;
  font-weight: 800;
  color: var(--wl-primary);
  letter-spacing: 0.5px;
}
.side-menu {
  border-right: none;
  padding: 8px 0;
}
.side-menu :deep(.el-menu-item) {
  height: 46px;
  margin: 2px 10px;
  border-radius: 8px;
  font-size: 14px;
}
.side-menu :deep(.el-menu-item:hover) {
  background: #fff4ee;
}
.side-menu :deep(.el-menu-item.is-active) {
  background: #fff1e9;
  color: var(--wl-primary);
  font-weight: 600;
  box-shadow: inset 3px 0 0 var(--wl-primary);
}

/* 主区撑满剩余宽度 */
.admin-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}
.admin-header {
  height: 56px;
  background: #fff;
  border-bottom: 1px solid #eef0f3;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 22px;
  position: sticky;
  top: 0;
  z-index: 50;
}
.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #909399;
  font-size: 14px;
}
.breadcrumb .current {
  color: #303133;
  font-weight: 600;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 18px;
}
.bell-icon {
  cursor: pointer;
  color: #606266;
}
.notice-badge :deep(.el-badge__content) {
  background: var(--wl-primary);
}
.notice-panel { max-height: 320px; overflow-y: auto; }
.notice-title { display: flex; justify-content: space-between; align-items: center; font-weight: 600; margin-bottom: 8px; }
.notice-item { display: flex; align-items: center; gap: 8px; padding: 8px 0; border-bottom: 1px solid #f0f2f5; font-size: 13px; }
.admin-name { display: flex; align-items: center; gap: 5px; color: #606266; font-size: 14px; }
.admin-content {
  flex: 1;
  min-width: 0;
  padding: 20px;
}
</style>
