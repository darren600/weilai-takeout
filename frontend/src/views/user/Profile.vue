<template>
  <!-- 我的（个人中心）：头像/手机号 + 订单/地址入口 + 退出登录 -->
  <div class="profile-page">
    <!-- 用户信息卡 -->
    <div class="profile-card">
      <div class="avatar">
        <el-icon :size="30"><Bowl /></el-icon>
      </div>
      <div class="user-info">
        <div class="user-name">味来用户</div>
        <div class="user-phone">{{ phone || '已登录' }}</div>
      </div>
    </div>

    <!-- 功能入口 -->
    <div class="menu-card">
      <button class="menu-item" type="button" @click="router.push('/user/order/list')">
        <span class="menu-left">
          <el-icon :size="18"><Tickets /></el-icon>
          <span>我的订单</span>
        </span>
        <el-icon :size="16" class="menu-arrow"><ArrowRight /></el-icon>
      </button>
      <button class="menu-item" type="button" @click="router.push('/user/address')">
        <span class="menu-left">
          <el-icon :size="18"><Location /></el-icon>
          <span>地址管理</span>
        </span>
        <el-icon :size="16" class="menu-arrow"><ArrowRight /></el-icon>
      </button>
    </div>

    <!-- 退出登录 -->
    <button class="logout-btn" type="button" @click="handleLogout">退出登录</button>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Bowl, Tickets, Location, ArrowRight } from '@element-plus/icons-vue'
import { useSessionStore } from '@/stores/session'

const router = useRouter()
const session = useSessionStore()

/** 登录时写入的手机号（用于展示）；无则显示"已登录" */
const phone = ref(localStorage.getItem('wl_user_phone') || '')

/** 退出登录：清令牌 + 清手机号 + 回登录页 */
async function handleLogout(): Promise<void> {
  await ElMessageBox.confirm('确认退出登录吗？', '提示', { type: 'warning' })
  session.logout()
  localStorage.removeItem('wl_user_phone')
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.profile-page {
  padding-bottom: 8px;
}

/* 用户信息卡 */
.profile-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 20px 18px;
  border-radius: 16px;
  background: linear-gradient(135deg, var(--wl-primary), var(--wl-primary-light));
  color: #fff;
  box-shadow: 0 8px 22px rgba(255, 107, 53, 0.28);
}
.avatar {
  width: 58px;
  height: 58px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.25);
  display: flex;
  align-items: center;
  justify-content: center;
}
.user-name {
  font-size: 18px;
  font-weight: 800;
}
.user-phone {
  font-size: 13px;
  opacity: 0.92;
  margin-top: 4px;
}

/* 功能入口 */
.menu-card {
  margin-top: 14px;
  background: #fff;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}
.menu-item {
  width: 100%;
  border: none;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px 16px;
  font-size: 14px;
  color: var(--wl-text);
  cursor: pointer;
}
.menu-item + .menu-item {
  border-top: 1px solid #f2f2f2;
}
.menu-left {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--wl-text);
}
.menu-left :deep(.el-icon) {
  color: var(--wl-primary);
}
.menu-arrow {
  color: #c0c4cc;
}

/* 退出登录 */
.logout-btn {
  width: 100%;
  margin-top: 24px;
  height: 46px;
  border: 1px solid #fbc4c4;
  border-radius: 23px;
  background: #fff;
  color: #f56c6c;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
}
.logout-btn:active {
  background: #fff5f5;
}
</style>
