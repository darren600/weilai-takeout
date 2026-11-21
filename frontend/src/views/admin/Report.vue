<template>
  <!-- 数据统计：日期筛选 + 4 类图表 + 导出 Excel -->
  <div class="admin-report-page">
    <h1 class="admin-page-title">数据统计</h1>
    <el-card class="admin-card">
      <div class="admin-toolbar">
        <el-date-picker
          v-model="dateRange" type="daterange" range-separator="至"
          start-placeholder="开始日期" end-placeholder="结束日期"
          value-format="YYYY-MM-DD" style="width: 280px"
        />
        <el-button type="primary" @click="loadReports">查询</el-button>
        <el-button type="success" :loading="exporting" @click="handleExport">导出报表</el-button>
      </div>
    </el-card>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>营业额趋势</template>
          <Chart v-if="turnoverOption" :option="turnoverOption" height="280px" />
          <el-empty v-else description="暂无数据" :image-size="50" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>用户增长</template>
          <Chart v-if="userOption" :option="userOption" height="280px" />
          <el-empty v-else description="暂无数据" :image-size="50" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>订单统计</template>
          <Chart v-if="orderOption" :option="orderOption" height="280px" />
          <el-empty v-else description="暂无数据" :image-size="50" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header">销量 Top10</template>
          <Chart v-if="top10Option" :option="top10Option" height="280px" />
          <el-empty v-else description="暂无数据" :image-size="50" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getTurnoverReport, getUserReport, getOrderReport, getTop10Report, exportBusinessReport
} from '@/api/admin/report'
import Chart from '@/components/Chart.vue'
import type { EChartsOption } from '@/utils/echarts'
import '@/styles/admin.css'

const dateRange = ref<string[]>([])
const exporting = ref(false)
const turnover = ref<{ dateList: string; turnoverList: string } | null>(null)
const user = ref<{ dateList: string; newUserList: string; totalUserList: string } | null>(null)
const order = ref<{ dateList: string; orderCountList: string; validOrderCountList: string } | null>(null)
const top10 = ref<{ nameList: string; numberList: string } | null>(null)

/** 默认最近 30 天 */
function defaultRange(): void {
  const end = new Date()
  const begin = new Date()
  begin.setDate(end.getDate() - 29)
  dateRange.value = [begin.toISOString().slice(0, 10), end.toISOString().slice(0, 10)]
}

const baseAxis = (data: string[]): EChartsOption => ({
  tooltip: { trigger: 'axis' },
  legend: { data: [] },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: { type: 'category', data }
})

const turnoverOption = computed<EChartsOption | null>(() => {
  if (!turnover.value?.dateList) return null
  return {
    ...baseAxis(turnover.value.dateList.split(',')),
    yAxis: { type: 'value' },
    series: [{ name: '营业额', type: 'line', smooth: true, data: turnover.value.turnoverList.split(',').map(Number), itemStyle: { color: '#ff6b35' } }]
  }
})

const userOption = computed<EChartsOption | null>(() => {
  if (!user.value?.dateList) return null
  return {
    ...baseAxis(user.value.dateList.split(',')),
    legend: { data: ['新增用户', '总用户'] },
    yAxis: { type: 'value' },
    series: [
      { name: '新增用户', type: 'line', smooth: true, data: user.value.newUserList.split(',').map(Number), itemStyle: { color: '#ff6b35' } },
      { name: '总用户', type: 'line', smooth: true, data: user.value.totalUserList.split(',').map(Number), itemStyle: { color: '#ffb703' } }
    ]
  }
})

const orderOption = computed<EChartsOption | null>(() => {
  if (!order.value?.dateList) return null
  return {
    ...baseAxis(order.value.dateList.split(',')),
    legend: { data: ['订单总数', '有效订单'] },
    yAxis: { type: 'value' },
    series: [
      { name: '订单总数', type: 'bar', data: order.value.orderCountList.split(',').map(Number), itemStyle: { color: '#ff6b35' } },
      { name: '有效订单', type: 'bar', data: order.value.validOrderCountList.split(',').map(Number), itemStyle: { color: '#ff8f66' } }
    ]
  }
})

const top10Option = computed<EChartsOption | null>(() => {
  if (!top10.value?.nameList) return null
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: top10.value.nameList.split(',') },
    yAxis: { type: 'value' },
    series: [{ name: '销量', type: 'bar', data: top10.value.numberList.split(',').map(Number), itemStyle: { color: '#ffb703' } }]
  }
})

/** 加载 4 类报表（某个失败不影响其它） */
async function loadReports(): Promise<void> {
  if (dateRange.value.length !== 2) {
    ElMessage.warning('请选择日期范围')
    return
  }
  const [begin, end] = dateRange.value
  const results = await Promise.allSettled([
    getTurnoverReport(begin, end),
    getUserReport(begin, end),
    getOrderReport(begin, end),
    getTop10Report(begin, end)
  ])
  if (results[0].status === 'fulfilled') turnover.value = results[0].value as typeof turnover.value
  if (results[1].status === 'fulfilled') user.value = results[1].value as typeof user.value
  if (results[2].status === 'fulfilled') order.value = results[2].value as typeof order.value
  if (results[3].status === 'fulfilled') top10.value = results[3].value as typeof top10.value
}

/** 导出 xlsx：把 blob 转成下载链接 */
async function handleExport(): Promise<void> {
  exporting.value = true
  try {
    const blob = (await exportBusinessReport()) as unknown as Blob
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `味来运营报表_${new Date().toISOString().slice(0, 10)}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch {
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

onMounted(() => {
  defaultRange()
  void loadReports()
})
</script>

<style scoped>
.chart-row { margin-top: 16px; }
.chart-card {
  border: none;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  margin-bottom: 16px;
}
</style>
