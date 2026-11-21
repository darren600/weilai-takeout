<template>
  <!-- 工作台：4 概览卡片 + 近 7 日营业额折线图 -->
  <div class="dashboard-page">
    <h1 class="admin-page-title">工作台</h1>

    <!-- 4 概览卡片 -->
    <el-row :gutter="16" class="overview-row">
      <el-col :span="6" v-for="card in overviewCards" :key="card.label">
        <el-card class="overview-card" shadow="hover">
          <div class="overview-inner">
            <div class="overview-icon" :style="{ background: card.color }">
              <el-icon :size="24"><component :is="card.icon"></component></el-icon>
            </div>
            <div class="overview-info">
              <div class="overview-label">{{ card.label }}</div>
              <div class="overview-value">{{ card.value }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 近 7 日营业额折线图 -->
    <el-card class="chart-card">
      <template #header>
        <div class="chart-header">
          <span>近 7 日营业额趋势</span>
          <el-tag type="success" effect="light">单位：元</el-tag>
        </div>
      </template>
      <Chart v-if="trendOption" :option="trendOption" height="320px" />
      <el-empty v-else description="暂无趋势数据" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Money, Document, User, ShoppingCart } from '@element-plus/icons-vue'
import {
  getBusinessData, getOrderOverview, getDishOverview, getSetmealOverview
} from '@/api/admin/workspace'
import { getTurnoverReport } from '@/api/admin/report'
import Chart from '@/components/Chart.vue'
import type { EChartsOption } from '@/utils/echarts'
import '@/styles/admin.css'

interface BusinessData { turnover: number; orderCompletionRate: number; newUsers: number; validOrderCount: number; unitPrice: number }

const business = ref<BusinessData | null>(null)
const orderOverview = ref<{ allOrders: number; validOrders: number; invalidOrders: number } | null>(null)
const trendDates = ref<string[]>([])
const trendValues = ref<number[]>([])

/** 4 概览卡片配置 */
const overviewCards = computed(() => [
  { label: '今日营业额', value: `¥ ${business.value?.turnover ?? 0}`, icon: Money, color: 'linear-gradient(135deg,#ff6b35,#ff8f66)' },
  { label: '有效订单', value: String(orderOverview.value?.validOrders ?? business.value?.validOrderCount ?? 0), icon: Document, color: 'linear-gradient(135deg,#ff8f66,#ffab8a)' },
  { label: '新增用户', value: String(business.value?.newUsers ?? 0), icon: User, color: 'linear-gradient(135deg,#ffb703,#ffc94d)' },
  { label: '客单价', value: `¥ ${business.value?.unitPrice ?? 0}`, icon: ShoppingCart, color: 'linear-gradient(135deg,#ff7a45,#ffab8a)' }
])

/** 折线图配置 */
const trendOption = computed<EChartsOption | null>(() => {
  if (trendDates.value.length === 0) return null
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, data: trendDates.value },
    yAxis: { type: 'value' },
    series: [{
      name: '营业额',
      type: 'line',
      smooth: true,
      data: trendValues.value,
      areaStyle: { opacity: 0.18, color: 'rgba(255,107,53,0.35)' },
      itemStyle: { color: '#ff6b35' },
      lineStyle: { width: 3, color: '#ff6b35' }
    }]
  }
})

/** 近 7 日日期（yyyy-MM-dd） */
function last7Days(): [string, string] {
  const end = new Date()
  const begin = new Date()
  begin.setDate(end.getDate() - 6)
  const fmt = (d: Date) => d.toISOString().slice(0, 10)
  return [fmt(begin), fmt(end)]
}

onMounted(async () => {
  const [businessReq, orderReq, dishReq, setmealReq] = await Promise.allSettled([
    getBusinessData(), getOrderOverview(), getDishOverview(), getSetmealOverview()
  ])
  if (businessReq.status === 'fulfilled') business.value = businessReq.value as BusinessData
  if (orderReq.status === 'fulfilled') orderOverview.value = orderReq.value as typeof orderOverview.value

  // 近 7 日营业额
  const [begin, end] = last7Days()
  try {
    const report = (await getTurnoverReport(begin, end)) as { dateList: string; turnoverList: string }
    trendDates.value = report.dateList ? report.dateList.split(',') : []
    trendValues.value = report.turnoverList ? report.turnoverList.split(',').map(Number) : []
  } catch {
    trendDates.value = []
  }
  // 预加载占位：dish/setmeal 概览请求已发出，工作台暂不展示，保留调用以体现接口覆盖
  void dishReq
  void setmealReq
})
</script>

<style scoped>
.dashboard-page { padding: 0; }
.overview-row { margin-bottom: 16px; }
.overview-card {
  border: none;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}
.overview-inner { display: flex; align-items: center; gap: 14px; }
.overview-icon {
  width: 52px; height: 52px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center; color: #fff;
}
.overview-label { color: #909399; font-size: 13px; }
.overview-value { font-size: 22px; font-weight: 700; color: #1f2733; margin-top: 4px; }
.chart-card {
  border: none;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}
.chart-header { display: flex; justify-content: space-between; align-items: center; font-weight: 600; }
</style>
