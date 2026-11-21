<template>
  <!-- 确认订单（H5）：地址 + 配送信息 + 商品清单 + 金额明细 + 提交（单列） -->
  <div class="order-confirm-page">
    <div class="page-heading">
      <h1>确认订单</h1>
    </div>

    <el-empty v-if="cartItems.length === 0" description="购物车是空的" :image-size="80">
      <el-button type="primary" round @click="router.push('/user/home')">去点餐</el-button>
    </el-empty>

    <template v-else>
      <!-- 收货地址 -->
      <div class="panel">
        <div class="panel-header">
          <span>收货地址</span>
          <button class="link-btn" type="button" @click="router.push('/user/address')">
            {{ defaultAddress ? '更换' : '去添加' }}
          </button>
        </div>
        <div v-if="defaultAddress" class="address-card">
          <div class="address-line">
            <strong>{{ defaultAddress.consignee }}</strong>
            <span>{{ defaultAddress.phone }}</span>
            <el-tag v-if="defaultAddress.isDefault === 1" size="small" type="success" effect="light">默认</el-tag>
          </div>
          <div class="address-detail">
            {{ defaultAddress.provinceName }}{{ defaultAddress.cityName }}{{ defaultAddress.districtName }}{{ defaultAddress.detail }}
          </div>
        </div>
        <el-empty v-else description="暂无默认地址，请先添加" :image-size="56" />
      </div>

      <!-- 配送与餐具 -->
      <div class="panel">
        <div class="panel-header"><span>配送信息</span></div>
        <el-form label-position="top" class="delivery-form">
          <el-form-item label="送达时间">
            <el-radio-group v-model="deliveryTimeType">
              <el-radio :label="0">尽快送达</el-radio>
              <el-radio :label="1">指定时间</el-radio>
            </el-radio-group>
            <el-date-picker
              v-if="deliveryTimeType === 1"
              v-model="estimatedDeliveryTime"
              type="datetime"
              placeholder="选择送达时间"
              class="time-picker"
            />
          </el-form-item>
          <el-form-item label="餐具数量">
            <el-input-number v-model="tablewareNumber" :min="0" :max="20" size="small" />
          </el-form-item>
          <el-form-item label="订单备注">
            <el-input v-model="remark" type="textarea" :rows="2" placeholder="口味偏好、配送要求等（选填）" />
          </el-form-item>
        </el-form>
      </div>

      <!-- 商品清单 + 金额 -->
      <div class="panel">
        <div class="panel-header"><span>商品清单</span></div>
        <div class="goods-list">
          <div v-for="(item, index) in cartItems" :key="index" class="goods-row">
            <img :src="item.image" :alt="item.name" class="goods-image" @error="handleImageError">
            <span class="goods-name">{{ item.name }}</span>
            <span class="goods-number">×{{ item.number }}</span>
            <span class="goods-amount">¥{{ (item.amount * item.number).toFixed(2) }}</span>
          </div>
        </div>
        <div class="amount-lines">
          <div class="amount-line"><span>商品金额</span><span>¥ {{ goodsAmount.toFixed(2) }}</span></div>
          <div class="amount-line"><span>打包费</span><span>¥ {{ packAmount.toFixed(2) }}</span></div>
          <div class="amount-line total"><span>实付金额</span><span>¥ {{ payableAmount.toFixed(2) }}</span></div>
        </div>
      </div>

      <!-- 底部提交条 -->
      <div class="submit-bar">
        <div class="submit-sum">实付 <strong>¥ {{ payableAmount.toFixed(2) }}</strong></div>
        <button class="submit-button" type="button" :disabled="submitting" @click="handleSubmit">
          {{ submitting ? '提交中…' : '提交订单' }}
        </button>
      </div>
      <p class="submit-tip">提交后可在「我的订单」查看进度，本演示仅为模拟下单流程</p>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { listCart } from '@/api/user/cart'
import { getDefaultAddress } from '@/api/user/address'
import { submitOrder } from '@/api/user/order'
import { handleImageError } from '@/utils/image'
import { useSessionStore } from '@/stores/session'

interface CartItem {
  dishId?: number
  setmealId?: number
  dishFlavor?: string
  name: string
  image?: string
  amount: number
  number: number
}
interface AddressLike {
  id: number
  consignee: string
  phone: string
  provinceName?: string
  cityName?: string
  districtName?: string
  detail: string
  isDefault?: number
}

const router = useRouter()
const session = useSessionStore()
const cartItems = ref<CartItem[]>([])
const defaultAddress = ref<AddressLike | null>(null)
const deliveryTimeType = ref(0)
const estimatedDeliveryTime = ref<Date | null>(null)
const tablewareNumber = ref(1)
const remark = ref('')
const submitting = ref(false)

const goodsAmount = computed(() => cartItems.value.reduce((sum, i) => sum + i.amount * i.number, 0))
const packAmount = computed(() => cartItems.value.length * 1)
const payableAmount = computed(() => goodsAmount.value + packAmount.value)

async function loadData(): Promise<void> {
  cartItems.value = (await listCart()) as CartItem[]
  try {
    const addr = (await getDefaultAddress()) as AddressLike | null
    defaultAddress.value = addr
  } catch {
    defaultAddress.value = null
  }
}

/** 提交订单：生成订单号，跳订单列表（下单即完成，无后续交易动作） */
async function handleSubmit(): Promise<void> {
  if (!defaultAddress.value) {
    ElMessage.warning('请先添加并选择收货地址')
    router.push('/user/address')
    return
  }
  submitting.value = true
  try {
    const result = (await submitOrder({
      addressBookId: defaultAddress.value.id,
      remark: remark.value,
      tablewareNumber: tablewareNumber.value,
      tablewareStatus: tablewareNumber.value > 0 ? 1 : 0,
      packAmount: packAmount.value,
      estimatedDeliveryTime:
        deliveryTimeType.value === 1 && estimatedDeliveryTime.value
          ? estimatedDeliveryTime.value.toISOString()
          : undefined
    })) as { id: number; orderNumber: string }
    session.setCartCount(0)
    ElMessage.success(`下单成功，订单号 ${result.orderNumber}`)
    router.push('/user/order/list')
  } finally {
    submitting.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.order-confirm-page {
  padding-bottom: 8px;
}
.page-heading h1 {
  margin: 0 0 10px;
  font-size: 18px;
  color: var(--wl-text);
}
.panel {
  background: #fff;
  border-radius: 14px;
  padding: 12px 14px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}
.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  font-weight: 700;
  color: var(--wl-text);
  margin-bottom: 10px;
}
.link-btn {
  border: none;
  background: transparent;
  color: var(--wl-primary);
  font-size: 13px;
  cursor: pointer;
}
.address-line {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
}
.address-detail {
  margin-top: 6px;
  color: var(--wl-text-muted);
  font-size: 12px;
  line-height: 1.5;
}
.delivery-form :deep(.el-form-item) {
  margin-bottom: 14px;
}
.time-picker {
  margin-top: 8px;
  width: 100%;
}
.goods-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 0;
  border-bottom: 1px dashed var(--wl-card-border);
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
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.goods-number {
  color: var(--wl-text-muted);
  font-size: 12px;
}
.goods-amount {
  font-size: 13px;
  font-weight: 600;
}
.amount-lines {
  margin-top: 12px;
}
.amount-line {
  display: flex;
  justify-content: space-between;
  padding: 5px 0;
  font-size: 13px;
  color: var(--wl-text-muted);
}
.amount-line.total {
  border-top: 1px solid var(--wl-card-border);
  margin-top: 4px;
  padding-top: 10px;
  color: var(--wl-text);
}
.amount-line.total span:last-child {
  color: var(--wl-primary);
  font-size: 19px;
  font-weight: 800;
}

/* 底部提交条 */
.submit-bar {
  position: sticky;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-radius: 14px;
  padding: 10px 14px;
  box-shadow: 0 -4px 18px rgba(0, 0, 0, 0.08);
}
.submit-sum {
  font-size: 12px;
  color: var(--wl-text-muted);
}
.submit-sum strong {
  color: var(--wl-primary);
  font-size: 19px;
  font-weight: 800;
}
.submit-button {
  border: none;
  border-radius: 22px;
  background: linear-gradient(90deg, var(--wl-primary), var(--wl-primary-light));
  color: #fff;
  font-size: 15px;
  font-weight: 700;
  padding: 11px 30px;
  cursor: pointer;
}
.submit-button:disabled {
  opacity: 0.7;
}
.submit-tip {
  text-align: center;
  font-size: 11px;
  color: var(--wl-text-muted);
  margin: 10px 0 4px;
}
</style>
