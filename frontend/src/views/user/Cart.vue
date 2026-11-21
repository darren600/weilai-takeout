<template>
  <!-- 购物车（H5）：商品列表 + 底部结算条 -->
  <div class="cart-page">
    <div class="page-heading">
      <h1>购物车</h1>
      <button v-if="cartItems.length > 0" class="clear-btn" type="button" @click="handleClean">
        清空
      </button>
    </div>

    <el-empty v-if="cartItems.length === 0" description="购物车空空如也" :image-size="80">
      <el-button type="primary" round @click="router.push('/user/home')">去点餐</el-button>
    </el-empty>

    <div v-else class="cart-list">
      <div v-for="(item, index) in cartItems" :key="index" class="cart-item">
        <img :src="item.image" :alt="item.name" class="item-image" @error="handleImageError">
        <div class="item-info">
          <div class="item-name">{{ item.name }}</div>
          <div class="item-flavor">{{ item.dishFlavor || '默认规格' }}</div>
          <div class="item-price"><small>¥</small>{{ item.amount }}</div>
        </div>
        <div class="item-stepper">
          <button class="step-btn" type="button" :disabled="item.number <= 1" @click="decrease(item)">
            <el-icon :size="14"><Minus /></el-icon>
          </button>
          <span class="item-number">{{ item.number }}</span>
          <button class="step-btn step-plus" type="button" @click="increase(item)">
            <el-icon :size="14"><Plus /></el-icon>
          </button>
        </div>
      </div>
    </div>

    <!-- 底部结算条 -->
    <div v-if="cartItems.length > 0" class="cart-summary">
      <div class="summary-info">
        <span class="summary-total"><small>合计</small><strong>¥ {{ totalAmount.toFixed(2) }}</strong></span>
        <span class="summary-count">共 {{ totalCount }} 件</span>
      </div>
      <button class="summary-button" type="button" @click="router.push('/user/order/confirm')">
        去结算
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Minus, Plus } from '@element-plus/icons-vue'
import { listCart, addCart, subCart, cleanCart } from '@/api/user/cart'
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

const emit = defineEmits<{ (e: 'cart-changed'): void }>()

const router = useRouter()
const session = useSessionStore()
const cartItems = ref<CartItem[]>([])

const totalCount = computed(() => cartItems.value.reduce((sum, i) => sum + i.number, 0))
const totalAmount = computed(() => cartItems.value.reduce((sum, i) => sum + i.amount * i.number, 0))

/** 刷新购物车列表并同步角标 */
async function loadCart(): Promise<void> {
  try {
    cartItems.value = (await listCart()) as CartItem[]
    session.setCartCount(totalCount.value)
  } catch {
    cartItems.value = []
    session.setCartCount(0)
  }
}

/** 数量 +1：复用后端 add（会累加数量） */
async function increase(item: CartItem): Promise<void> {
  const payload: Record<string, unknown> = { number: 1 }
  if (item.setmealId) payload.setmealId = item.setmealId
  else payload.dishId = item.dishId
  if (item.dishFlavor) payload.dishFlavor = item.dishFlavor
  await addCart(payload)
  await loadCart()
  emit('cart-changed')
}

/** 数量 -1：复用后端 sub */
async function decrease(item: CartItem): Promise<void> {
  const payload: Record<string, unknown> = {}
  if (item.setmealId) payload.setmealId = item.setmealId
  else payload.dishId = item.dishId
  if (item.dishFlavor) payload.dishFlavor = item.dishFlavor
  await subCart(payload)
  await loadCart()
  emit('cart-changed')
}

/** 清空购物车 */
async function handleClean(): Promise<void> {
  await ElMessageBox.confirm('确认清空购物车吗？', '提示', { type: 'warning' })
  await cleanCart()
  ElMessage.success('已清空')
  await loadCart()
  emit('cart-changed')
}

onMounted(loadCart)
</script>

<style scoped>
.cart-page {
  padding-bottom: 8px;
}
.page-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}
.page-heading h1 {
  margin: 0;
  font-size: 18px;
  color: var(--wl-text);
}
.clear-btn {
  border: none;
  background: transparent;
  color: var(--wl-text-muted);
  font-size: 13px;
  cursor: pointer;
}
.cart-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.cart-item {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #fff;
  border-radius: 12px;
  padding: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}
.item-image {
  width: 64px;
  height: 64px;
  border-radius: 10px;
  object-fit: cover;
  background: var(--wl-bg);
}
.item-info {
  flex: 1;
  min-width: 0;
}
.item-name {
  font-size: 14px;
  font-weight: 700;
  color: var(--wl-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.item-flavor {
  font-size: 11px;
  color: var(--wl-text-muted);
  margin: 3px 0 4px;
}
.item-price {
  color: var(--wl-primary);
  font-size: 15px;
  font-weight: 700;
}
.item-price small {
  font-size: 11px;
}
.item-stepper {
  display: flex;
  align-items: center;
  gap: 8px;
}
.step-btn {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: 1px solid var(--wl-card-border);
  background: #fff;
  color: var(--wl-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}
.step-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}
.step-plus {
  background: var(--wl-primary);
  border-color: var(--wl-primary);
  color: #fff;
}
.item-number {
  min-width: 18px;
  text-align: center;
  font-weight: 600;
  font-size: 14px;
}

/* 底部结算条 */
.cart-summary {
  position: sticky;
  bottom: 0;
  margin-top: 12px;
  background: #fff;
  border-radius: 14px;
  padding: 10px 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 -4px 18px rgba(0, 0, 0, 0.08);
}
.summary-info {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}
.summary-total small {
  font-size: 12px;
  color: var(--wl-text-muted);
  margin-right: 4px;
}
.summary-total strong {
  color: var(--wl-primary);
  font-size: 20px;
  font-weight: 800;
}
.summary-count {
  font-size: 11px;
  color: var(--wl-text-muted);
}
.summary-button {
  border: none;
  border-radius: 22px;
  background: linear-gradient(90deg, var(--wl-primary), var(--wl-primary-light));
  color: #fff;
  font-size: 15px;
  font-weight: 700;
  padding: 11px 28px;
  cursor: pointer;
}
</style>
