<template>
  <!-- 订单详情（H5）：状态时间轴 + 收货信息 + 商品明细 + 金额 -->
  <div class="order-detail-page">
    <div class="page-heading">
      <h1>订单详情</h1>
    </div>

    <el-empty v-if="!order" description="订单加载中或不存在" :image-size="80" />

    <template v-else>
      <div class="panel">
        <div class="detail-header">
          <span class="order-number">{{ order.number }}</span>
          <el-tag :type="statusTagType(order.status)" effect="dark" size="small">{{ statusText(order.status) }}</el-tag>
        </div>
        <!-- 状态时间轴 -->
        <el-steps :active="activeStep" finish-status="success" align-center class="status-steps">
          <el-step title="已下单" />
          <el-step title="已接单" />
          <el-step title="派送中" />
          <el-step title="已完成" />
        </el-steps>
      </div>

      <div class="panel">
        <div class="panel-title">收货信息</div>
        <div class="info-line"><span>收货人</span>{{ order.consignee }}</div>
        <div class="info-line"><span>手机号</span>{{ order.phone }}</div>
        <div class="info-line"><span>地址</span>{{ order.address }}</div>
        <div class="info-line"><span>下单时间</span>{{ order.orderTime }}</div>
        <div v-if="order.cancelReason" class="info-line"><span>取消原因</span>{{ order.cancelReason }}</div>
      </div>

      <div class="panel">
        <div class="panel-title">商品明细</div>
        <div v-for="detail in order.orderDetailList" :key="detail.id" class="detail-goods">
          <img :src="detail.image" :alt="detail.name" class="goods-image" @error="handleImageError">
          <div class="goods-info">
            <div class="goods-name">{{ detail.name }}</div>
            <div class="goods-flavor">{{ detail.dishFlavor || '默认规格' }}</div>
          </div>
          <span class="goods-number">×{{ detail.number }}</span>
          <span class="goods-amount">¥{{ detail.amount }}</span>
        </div>
        <div class="amount-total">实付金额 <strong>¥ {{ order.amount }}</strong></div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getUserOrderDetail } from '@/api/user/order'
import { handleImageError } from '@/utils/image'

interface OrderDetail { id: number; name: string; number: number; amount: number; image?: string; dishFlavor?: string }
interface OrderLike {
  id: number
  number: string
  status: number
  orderTime: string
  amount: number
  consignee: string
  phone: string
  address: string
  cancelReason?: string
  orderDetailList: OrderDetail[]
}

const route = useRoute()
const order = ref<OrderLike | null>(null)

const statusMap: Record<number, string> = { 1: '待付款', 2: '待接单', 3: '已接单', 4: '派送中', 5: '已完成', 6: '已取消', 7: '退款' }
function statusText(status: number): string { return statusMap[status] || '未知' }
function statusTagType(status: number): string {
  if (status === 5) return 'success'
  if (status === 6 || status === 7) return 'info'
  if (status === 3 || status === 4) return 'primary'
  return 'warning'
}

/** 时间轴进度：状态 5→4步，6→停在第1步 */
const activeStep = computed(() => {
  const status = order.value?.status || 1
  if (status >= 5) return 4
  if (status === 6 || status === 7) return 0
  return status
})

onMounted(async () => {
  const id = Number(route.params.id)
  if (!id) return
  try {
    order.value = (await getUserOrderDetail(id)) as OrderLike
  } catch {
    order.value = null
  }
})
</script>

<style scoped>
.order-detail-page {
  padding-bottom: 8px;
}
.page-heading {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.page-heading h1 {
  margin: 0;
  font-size: 18px;
  color: var(--wl-text);
}
.link-btn {
  border: none;
  background: transparent;
  color: var(--wl-text-muted);
  font-size: 13px;
  cursor: pointer;
}
.panel {
  background: #fff;
  border-radius: 14px;
  padding: 12px 14px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}
.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}
.order-number {
  font-size: 13px;
  font-weight: 600;
  color: var(--wl-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 72%;
}
.status-steps {
  padding: 12px 0 4px;
}
.status-steps :deep(.el-step__title) {
  font-size: 11px;
}
.panel-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--wl-text);
  margin-bottom: 8px;
}
.info-line {
  display: flex;
  gap: 10px;
  padding: 4px 0;
  font-size: 13px;
  color: var(--wl-text);
}
.info-line span {
  flex: 0 0 60px;
  color: var(--wl-text-muted);
}
.detail-goods {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 0;
  border-bottom: 1px dashed var(--wl-card-border);
}
.goods-image {
  width: 42px;
  height: 42px;
  border-radius: 8px;
  object-fit: cover;
  background: var(--wl-bg);
}
.goods-info {
  flex: 1;
  min-width: 0;
}
.goods-name {
  font-size: 13px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.goods-flavor {
  font-size: 11px;
  color: var(--wl-text-muted);
}
.goods-number {
  color: var(--wl-text-muted);
  font-size: 12px;
}
.goods-amount {
  font-size: 13px;
  font-weight: 600;
}
.amount-total {
  text-align: right;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid var(--wl-card-border);
  font-size: 13px;
}
.amount-total strong {
  color: var(--wl-primary);
  font-size: 18px;
}
</style>
