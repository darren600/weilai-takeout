<template>
  <!-- 登录页左侧品牌宣传区：橙色渐变 + 装饰圆 + 卖点 -->
  <section class="brand-panel">
    <!-- 半透明装饰圆，增加层次感 -->
    <span class="deco deco-one"></span>
    <span class="deco deco-two"></span>
    <span class="deco deco-three"></span>

    <div class="brand-content">
      <!-- 品牌 Logo：白色圆角底 + 橙色碗图标 -->
      <div class="brand-logo">
        <el-icon :size="40"><Bowl /></el-icon>
      </div>

      <h1 class="brand-name">味来 Weilai</h1>
      <p class="brand-slogan">一口好味，如约而来</p>

      <!-- 三条卖点 -->
      <ul class="brand-points">
        <li v-for="point in sellingPoints" :key="point.title" class="point-item">
          <span class="point-icon">
            <el-icon :size="20"><component :is="point.icon"></component></el-icon>
          </span>
          <div class="point-text">
            <strong class="point-title">{{ point.title }}</strong>
            <span class="point-desc">{{ point.desc }}</span>
          </div>
        </li>
      </ul>
    </div>
  </section>
</template>

<script setup lang="ts">
import { Bowl, Timer, Shop, Medal } from '@element-plus/icons-vue'
import type { Component } from 'vue'

/** 卖点数据：标题 + 描述 + 图标组件 */
interface SellingPoint {
  title: string
  desc: string
  icon: Component
}

const sellingPoints: SellingPoint[] = [
  { title: '30 分钟必达', desc: '极速配送，热气腾腾送到手', icon: Timer },
  { title: '精选好店', desc: '严选优质商家，吃得放心', icon: Shop },
  { title: '品质保障', desc: '全程品控，售后无忧', icon: Medal }
]
</script>

<style scoped>
.brand-panel {
  position: relative;
  height: 100%;
  overflow: hidden;
  background: linear-gradient(135deg, #ff6b35 0%, #ff8f66 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 装饰圆 */
.deco {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.12);
}
.deco-one {
  width: 320px;
  height: 320px;
  top: -90px;
  right: -80px;
}
.deco-two {
  width: 200px;
  height: 200px;
  bottom: -60px;
  left: -50px;
  background: rgba(255, 255, 255, 0.1);
}
.deco-three {
  width: 120px;
  height: 120px;
  bottom: 120px;
  right: 60px;
  background: rgba(255, 255, 255, 0.08);
}

.brand-content {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 380px;
  padding: 0 48px;
}

.brand-logo {
  width: 76px;
  height: 76px;
  border-radius: 22px;
  background: #fff;
  color: var(--wl-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.16);
  margin-bottom: 26px;
}

.brand-name {
  margin: 0;
  font-size: 40px;
  font-weight: 800;
  letter-spacing: 2px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.12);
}

.brand-slogan {
  margin: 12px 0 40px;
  font-size: 17px;
  letter-spacing: 3px;
  opacity: 0.92;
}

.brand-points {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 22px;
}
.point-item {
  display: flex;
  align-items: center;
  gap: 14px;
}
.point-icon {
  flex: 0 0 auto;
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.18);
  display: flex;
  align-items: center;
  justify-content: center;
}
.point-text {
  display: flex;
  flex-direction: column;
  line-height: 1.3;
}
.point-title {
  font-size: 16px;
  font-weight: 700;
}
.point-desc {
  font-size: 13px;
  opacity: 0.88;
}
</style>
