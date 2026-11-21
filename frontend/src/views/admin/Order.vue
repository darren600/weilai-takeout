<template>
  <!-- 订单管理：状态 Tab + 角标 + 条件筛选 + 分页表格 + 详情抽屉 + 状态流转 -->
  <div class="admin-order-page">
    <h1 class="admin-page-title">订单管理</h1>

    <el-card class="admin-card">
      <!-- 状态 Tab + 角标 -->
      <el-tabs v-model="activeStatus" @tab-change="handleTabChange">
        <el-tab-pane label="全部" name="-1" />
        <el-tab-pane :label="`待接单 (${statusCounts[2] || 0})`" name="2" />
        <el-tab-pane :label="`派送中 (${statusCounts[4] || 0})`" name="4" />
        <el-tab-pane label="已完成" name="5" />
        <el-tab-pane label="已取消" name="6" />
      </el-tabs>

      <!-- 条件筛选 -->
      <div class="admin-toolbar">
        <el-input v-model="filters.number" placeholder="订单号" clearable style="width: 180px" />
        <el-input v-model="filters.phone" placeholder="手机号" clearable style="width: 160px" />
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD HH:mm:ss"
          style="width: 300px"
        />
        <el-button type="primary" @click="loadOrders">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>

      <!-- 表格 -->
      <el-table :data="orders" v-loading="loading" class="admin-table" border stripe>
        <el-table-column prop="number" label="订单号" width="180" />
        <el-table-column prop="consignee" label="收货人" width="100" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="amount" label="金额" width="100">
          <template #default="{ row }">¥ {{ row.amount }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" effect="light">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="orderTime" label="下单时间" width="170" />
        <el-table-column label="操作" min-width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openDetail(row)">详情</el-button>
            <el-button
              v-if="row.status === 2"
              link type="success" size="small"
              @click="handleConfirm(row)"
            >接单</el-button>
            <el-button
              v-if="row.status === 2"
              link type="danger" size="small"
              @click="handleReject(row)"
            >拒单</el-button>
            <el-button
              v-if="row.status === 3"
              link type="warning" size="small"
              @click="handleDeliver(row)"
            >派送</el-button>
            <el-button
              v-if="row.status === 4"
              link type="success" size="small"
              @click="handleComplete(row)"
            >完成</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="admin-pagination">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadOrders"
          @current-change="loadOrders"
        />
      </div>
    </el-card>

    <!-- 订单详情抽屉 -->
    <el-drawer v-model="detailVisible" title="订单详情" size="480px">
      <template v-if="currentOrder">
        <el-descriptions :column="1" border class="detail-desc">
          <el-descriptions-item label="订单号">{{ currentOrder.number }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ statusText(currentOrder.status) }}</el-descriptions-item>
          <el-descriptions-item label="收货人">{{ currentOrder.consignee }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ currentOrder.phone }}</el-descriptions-item>
          <el-descriptions-item label="地址">{{ currentOrder.address }}</el-descriptions-item>
          <el-descriptions-item label="金额">¥ {{ currentOrder.amount }}</el-descriptions-item>
          <el-descriptions-item v-if="currentOrder.remark" label="备注">{{ currentOrder.remark }}</el-descriptions-item>
        </el-descriptions>
        <div class="detail-section-title">商品明细</div>
        <div v-for="d in currentOrder.orderDetailList" :key="d.id" class="detail-goods">
          <span>{{ d.name }}</span>
          <span>×{{ d.number }}</span>
          <span>¥{{ d.amount }}</span>
        </div>
        <div v-if="currentOrder.orderDishes" class="detail-section-title">菜品：{{ currentOrder.orderDishes }}</div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  conditionSearchOrder, countOrderByStatus, getOrderDetail,
  confirmOrder, rejectOrder, deliverOrder, completeOrder
} from '@/api/admin/order'
import '@/styles/admin.css'

interface OrderLike {
  id: number
  number: string
  status: number
  consignee: string
  phone: string
  address: string
  amount: number
  orderTime: string
  remark?: string
  orderDishes?: string
  orderDetailList: Array<{ id: number; name: string; number: number; amount: number }>
}

const activeStatus = ref('-1')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const orders = ref<OrderLike[]>([])
const loading = ref(false)
const detailVisible = ref(false)
const currentOrder = ref<OrderLike | null>(null)
const statusCounts = ref<Record<number, number>>({})
const dateRange = ref<string[] | null>(null)
const filters = reactive({ number: '', phone: '' })

const statusMap: Record<number, string> = { 1: '待付款', 2: '待接单', 3: '已接单', 4: '派送中', 5: '已完成', 6: '已取消', 7: '退款' }
function statusText(status: number): string { return statusMap[status] || '未知' }
function statusTagType(status: number): string {
  if (status === 5) return 'success'
  if (status === 6 || status === 7) return 'info'
  if (status === 3 || status === 4) return 'primary'
  return 'warning'
}

/** 加载订单分页 */
async function loadOrders(): Promise<void> {
  loading.value = true
  try {
    const params: Record<string, unknown> = { page: page.value, pageSize: pageSize.value }
    if (activeStatus.value !== '-1') params.status = Number(activeStatus.value)
    if (filters.number) params.number = filters.number
    if (filters.phone) params.phone = filters.phone
    if (dateRange.value?.length === 2) {
      params.beginTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }
    const result = (await conditionSearchOrder(params)) as { total: number; records: OrderLike[] }
    total.value = result.total
    orders.value = result.records
  } catch {
    orders.value = []
  } finally {
    loading.value = false
  }
}

/** 加载各状态角标数量
 *
 * 后端 /admin/order/statistics 返回的是 OrderStatisticsVO：
 * { toBeConfirmed, confirmed, deliveryInProgress }，只覆盖「待接单/已接单/派送中」三种状态，
 * 且字段名不是状态码。这里显式映射到状态码，避免直接把对象当 statusCounts[2] 取值导致全为 0。
 */
async function loadStatusCounts(): Promise<void> {
  try {
    const data = (await countOrderByStatus()) as {
      toBeConfirmed?: number
      confirmed?: number
      deliveryInProgress?: number
    }
    statusCounts.value = {
      2: data?.toBeConfirmed ?? 0, // 待接单
      3: data?.confirmed ?? 0, // 已接单
      4: data?.deliveryInProgress ?? 0 // 派送中
    }
  } catch {
    statusCounts.value = {}
  }
}

function handleTabChange(): void {
  page.value = 1
  void loadOrders()
}

function resetFilters(): void {
  filters.number = ''
  filters.phone = ''
  dateRange.value = null
  page.value = 1
  void loadOrders()
}

async function openDetail(row: OrderLike): Promise<void> {
  currentOrder.value = (await getOrderDetail(row.id)) as OrderLike
  detailVisible.value = true
}

/** 接单（二次确认） */
async function handleConfirm(row: OrderLike): Promise<void> {
  await ElMessageBox.confirm(`确认接单 ${row.number} 吗？`, '接单确认', { type: 'warning' })
  await confirmOrder({ id: row.id })
  ElMessage.success('接单成功')
  await Promise.all([loadOrders(), loadStatusCounts()])
}

/** 拒单（填写原因） */
async function handleReject(row: OrderLike): Promise<void> {
  const { value: reason } = await ElMessageBox.prompt('请输入拒单原因', '拒单', {
    inputPattern: /\S+/, inputErrorMessage: '请填写拒单原因'
  })
  await rejectOrder({ id: row.id, rejectionReason: reason })
  ElMessage.success('已拒单')
  await Promise.all([loadOrders(), loadStatusCounts()])
}

/** 派送（二次确认） */
async function handleDeliver(row: OrderLike): Promise<void> {
  await ElMessageBox.confirm(`确认派送订单 ${row.number} 吗？`, '派送确认', { type: 'warning' })
  await deliverOrder(row.id)
  ElMessage.success('已派送')
  await Promise.all([loadOrders(), loadStatusCounts()])
}

/** 完成（二次确认） */
async function handleComplete(row: OrderLike): Promise<void> {
  await ElMessageBox.confirm(`确认订单 ${row.number} 已完成吗？`, '完成确认', { type: 'warning' })
  await completeOrder(row.id)
  ElMessage.success('订单已完成')
  await Promise.all([loadOrders(), loadStatusCounts()])
}

onMounted(() => {
  void loadOrders()
  void loadStatusCounts()
})
</script>

<style scoped>
.detail-desc { margin-bottom: 18px; }
.detail-section-title { font-weight: 600; margin: 16px 0 8px; color: #303133; }
.detail-goods { display: flex; justify-content: space-between; padding: 6px 0; border-bottom: 1px dashed #e6e8eb; font-size: 13px; }
</style>
