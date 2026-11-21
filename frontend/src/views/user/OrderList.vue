<template>
  <!-- 订单列表（H5）：横向状态胶囊 + 订单卡片 + 分页 -->
  <div class="order-list-page">
    <div class="page-heading"><h1>我的订单</h1></div>

    <!-- 横向滚动状态筛选 -->
    <div class="status-scroll">
      <button
        v-for="tab in statusTabs"
        :key="tab.value"
        type="button"
        class="status-pill"
        :class="{ active: activeStatus === tab.value }"
        @click="selectStatus(tab.value)"
      >
        {{ tab.label }}
      </button>
    </div>

    <el-empty v-if="!loading && orders.length === 0" description="暂无订单" :image-size="72" />

    <div class="order-list">
      <div v-for="order in orders" :key="order.id" class="order-card">
        <div class="order-header">
          <span class="order-number">订单号 {{ order.number }}</span>
          <el-tag :type="statusTagType(order.status)" effect="light" size="small">{{ statusText(order.status) }}</el-tag>
        </div>
        <div class="order-time">{{ order.orderTime }}</div>

        <div class="order-body">
          <div v-for="detail in order.orderDetailList" :key="detail.id" class="order-goods">
            <img :src="detail.image" :alt="detail.name" class="goods-image" @error="handleImageError">
            <span class="goods-name">{{ detail.name }}</span>
            <span class="goods-number">×{{ detail.number }}</span>
          </div>
        </div>

        <div class="order-footer">
          <span class="order-amount">实付 <strong>¥ {{ order.amount }}</strong></span>
          <div class="order-actions">
            <button class="mini-btn" type="button" @click="router.push(`/user/order/${order.id}`)">详情</button>
            <button v-if="order.status === 1 || order.status === 2" class="mini-btn danger" type="button" @click="handleCancel(order)">取消</button>
            <button v-if="order.status === 3 || order.status === 4" class="mini-btn warn" type="button" @click="handleReminder(order)">催单</button>
            <button v-if="order.status === 5 || order.status === 6" class="mini-btn primary" type="button" @click="handleRepetition(order)">再来一单</button>
          </div>
        </div>
      </div>
    </div>

    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        :pager-count="5"
        small
        layout="prev, pager, next"
        @current-change="loadOrders"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageUserOrder, cancelUserOrder, reminderOrder, repetitionOrder } from '@/api/user/order'
import { handleImageError } from '@/utils/image'
import { useSessionStore } from '@/stores/session'

interface OrderDetail { id: number; name: string; number: number; image?: string }
interface OrderLike {
  id: number
  number: string
  status: number
  orderTime: string
  amount: number
  orderDetailList: OrderDetail[]
}

const router = useRouter()
const session = useSessionStore()
const activeStatus = ref<number>(-1)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const orders = ref<OrderLike[]>([])
const loading = ref(false)

/** 状态筛选（横向胶囊） */
const statusTabs = [
  { label: '全部', value: -1 },
  { label: '待付款', value: 1 },
  { label: '待接单', value: 2 },
  { label: '已接单', value: 3 },
  { label: '派送中', value: 4 },
  { label: '已完成', value: 5 },
  { label: '已取消', value: 6 }
]

const statusMap: Record<number, string> = {
  1: '待付款', 2: '待接单', 3: '已接单', 4: '派送中', 5: '已完成', 6: '已取消', 7: '退款'
}
function statusText(status: number): string { return statusMap[status] || '未知' }
function statusTagType(status: number): string {
  if (status === 5) return 'success'
  if (status === 6 || status === 7) return 'info'
  if (status === 3 || status === 4) return 'primary'
  return 'warning'
}

function selectStatus(value: number): void {
  activeStatus.value = value
  page.value = 1
  void loadOrders()
}

async function loadOrders(): Promise<void> {
  loading.value = true
  try {
    const result = (await pageUserOrder({
      page: page.value,
      pageSize: pageSize.value,
      status: activeStatus.value === -1 ? undefined : activeStatus.value
    })) as { total: number; records: OrderLike[] }
    total.value = result.total
    orders.value = result.records
  } catch {
    orders.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

async function handleCancel(order: OrderLike): Promise<void> {
  await ElMessageBox.confirm(`确认取消订单 ${order.number} 吗？`, '提示', { type: 'warning' })
  await cancelUserOrder(order.id)
  ElMessage.success('订单已取消')
  await loadOrders()
}

async function handleReminder(order: OrderLike): Promise<void> {
  await reminderOrder(order.id)
  ElMessage.success('已催单，商家会尽快处理')
}

async function handleRepetition(order: OrderLike): Promise<void> {
  await repetitionOrder(order.id)
  ElMessage.success('已加入购物车')
  session.setCartCount(session.cartCount + 1)
  router.push('/user/cart')
}

onMounted(loadOrders)
</script>

<style scoped>
.order-list-page {
  padding-bottom: 8px;
}
.page-heading h1 {
  margin: 0 0 8px;
  font-size: 18px;
  color: var(--wl-text);
}
.status-scroll {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  overflow-y: hidden;
  white-space: nowrap;
  flex-wrap: nowrap;
  padding: 4px 2px 10px;
  scrollbar-width: none;
  -webkit-overflow-scrolling: touch;
  -webkit-mask-image: linear-gradient(90deg, #000 calc(100% - 22px), transparent);
  mask-image: linear-gradient(90deg, #000 calc(100% - 22px), transparent);
}
.status-scroll::-webkit-scrollbar {
  display: none;
}
.status-pill {
  flex: 0 0 auto;
  border: 1px solid var(--wl-card-border);
  background: #fff;
  color: var(--wl-text);
  padding: 5px 13px;
  border-radius: 15px;
  font-size: 12px;
  cursor: pointer;
  white-space: nowrap;
}
.status-pill.active {
  background: var(--wl-primary);
  border-color: var(--wl-primary);
  color: #fff;
  font-weight: 600;
}
.order-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.order-card {
  background: #fff;
  border-radius: 14px;
  padding: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}
.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.order-number {
  font-size: 12px;
  font-weight: 600;
  color: var(--wl-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 70%;
}
.order-time {
  font-size: 11px;
  color: var(--wl-text-muted);
  margin: 3px 0 6px;
}
.order-body {
  padding: 4px 0;
}
.order-goods {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 0;
}
.goods-image {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  object-fit: cover;
  background: var(--wl-bg);
}
.goods-name {
  flex: 1;
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.goods-number {
  color: var(--wl-text-muted);
  font-size: 12px;
}
.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px dashed var(--wl-card-border);
  padding-top: 9px;
  margin-top: 4px;
}
.order-amount {
  font-size: 12px;
  color: var(--wl-text-muted);
}
.order-amount strong {
  color: var(--wl-primary);
  font-size: 16px;
}
.order-actions {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  justify-content: flex-end;
}
.mini-btn {
  border: 1px solid var(--wl-card-border);
  background: #fff;
  color: var(--wl-text);
  font-size: 12px;
  padding: 4px 11px;
  border-radius: 14px;
  cursor: pointer;
}
.mini-btn.danger { color: #f56c6c; border-color: #fbc4c4; }
.mini-btn.warn { color: #e6a23c; border-color: #f7d9a8; }
.mini-btn.primary { color: var(--wl-primary); border-color: #ffc9b3; }
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 14px;
}
</style>
