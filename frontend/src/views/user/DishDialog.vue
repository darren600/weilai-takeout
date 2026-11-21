<template>
  <!-- 规格选择：底部半屏抽屉（大图 + 分组规格 pill + 数量/合计/加购） -->
  <div v-if="visible" class="sheet-mask" @click.self="handleVisibleChange(false)">
    <div class="sheet-panel">
      <div class="sheet-scroll">
        <!-- 顶部大图 + 关闭 -->
        <div class="sheet-cover">
          <img :src="item?.image" :alt="item?.name" class="cover-img" @error="handleImageError">
          <button class="sheet-close" type="button" @click="handleVisibleChange(false)">
            <el-icon :size="18"><Close /></el-icon>
          </button>
        </div>

        <div class="sheet-body">
          <h2 class="dish-name">{{ item?.name }}</h2>
          <p class="dish-desc">{{ item?.description || '美味来袭，敬请品尝～' }}</p>
          <div class="dish-price"><small>¥</small>{{ item?.price }}</div>

          <!-- 套餐内菜品 -->
          <div v-if="isSetmeal && setmealDishes.length > 0" class="group">
            <div class="group-title">套餐内含</div>
            <div class="setmeal-tags">
              <span v-for="sd in setmealDishes" :key="sd.name" class="setmeal-tag">
                {{ sd.name }} ×{{ sd.copies }}
              </span>
            </div>
          </div>

          <!-- 规格分组（按 flavor.name 分组，每组单选 pill） -->
          <div v-for="group in flavorGroups" :key="group.name" class="group">
            <div class="group-title">{{ group.name }}</div>
            <div class="pill-row">
              <button
                v-for="option in group.options"
                :key="option"
                type="button"
                class="pill"
                :class="{ active: selections[group.name] === option }"
                @click="selections[group.name] = option"
              >
                {{ option }}
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部固定条：数量步进 + 合计 + 加入购物车 -->
      <div class="sheet-footer">
        <div class="stepper">
          <button class="step-btn" type="button" :disabled="quantity <= 1" @click="decrease">
            <el-icon :size="14"><Minus /></el-icon>
          </button>
          <span class="step-num">{{ quantity }}</span>
          <button class="step-btn step-plus" type="button" :disabled="quantity >= 99" @click="increase">
            <el-icon :size="14"><Plus /></el-icon>
          </button>
        </div>
        <div class="total-block">
          <span class="total-label">合计</span>
          <span class="total-amount"><small>¥</small>{{ totalAmount }}</span>
        </div>
        <button class="add-cart-btn" type="button" @click="handleAddCart">加入购物车</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Close, Minus, Plus } from '@element-plus/icons-vue'
import { addCart } from '@/api/user/cart'
import { listSetmealDish } from '@/api/user/setmeal'
import { handleImageError } from '@/utils/image'

/** 后端 flavor 结构：{ name:'温度', value:'冰,热' } */
interface DishFlavorLike { name?: string; value?: string }
interface DisplayItem {
  id: number
  name: string
  price: number
  image?: string
  description?: string
  flavors?: string[] | DishFlavorLike[]
}
/** 分组后的规格 */
interface FlavorGroup { name: string; options: string[] }

const props = defineProps<{
  visible: boolean
  item: DisplayItem | null
  isSetmeal: boolean
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'added'): void
}>()

const quantity = ref(1)
const setmealDishes = ref<Array<{ name: string; copies: number }>>([])
/** 每组选中的值：{ 温度: '冰', 糖度: '半糖' } */
const selections = ref<Record<string, string>>({})

/** 把 flavors 按 name 分组：value 用中英文逗号拆分成多个选项，绝不拼串展示 */
const flavorGroups = computed<FlavorGroup[]>(() => {
  const raw = props.item?.flavors
  if (!Array.isArray(raw)) return []
  const groups: FlavorGroup[] = []
  const ensure = (name: string): FlavorGroup => {
    let g = groups.find((x) => x.name === name)
    if (!g) {
      g = { name, options: [] }
      groups.push(g)
    }
    return g
  }
  for (const f of raw) {
    if (typeof f === 'string') {
      // 兼容旧格式：一堆字符串口味值，归到「口味」组
      const g = ensure('口味')
      if (!g.options.includes(f)) g.options.push(f)
    } else if (f && typeof f === 'object') {
      const name = f.name || '口味'
      const options = String(f.value || '')
        .split(/[,，]/)
        .map((s) => s.trim())
        .filter(Boolean)
      const g = ensure(name)
      for (const o of options) if (!g.options.includes(o)) g.options.push(o)
    }
  }
  return groups.filter((g) => g.options.length > 0)
})

/** 合计金额 = 单价 × 数量 */
const totalAmount = computed(() => {
  const price = Number(props.item?.price) || 0
  return (price * quantity.value).toFixed(2)
})

/** 打开时重置：数量 1，每组默认选第一个选项；套餐则拉取套餐内菜品 */
watch(
  () => props.visible,
  async (open) => {
    if (!open) return
    quantity.value = 1
    const next: Record<string, string> = {}
    for (const g of flavorGroups.value) next[g.name] = g.options[0]
    selections.value = next
    setmealDishes.value = []
    if (props.isSetmeal && props.item) {
      try {
        setmealDishes.value = (await listSetmealDish(props.item.id)) as Array<{ name: string; copies: number }>
      } catch {
        setmealDishes.value = []
      }
    }
  }
)

function decrease(): void {
  if (quantity.value > 1) quantity.value -= 1
}
function increase(): void {
  if (quantity.value < 99) quantity.value += 1
}

function handleVisibleChange(value: boolean): void {
  emit('update:visible', value)
}

/** 组装 dishFlavor：温度:冰;糖度:半糖 */
function buildDishFlavor(): string {
  return flavorGroups.value
    .map((g) => (selections.value[g.name] ? `${g.name}:${selections.value[g.name]}` : ''))
    .filter(Boolean)
    .join(';')
}

/** 加购 */
async function handleAddCart(): Promise<void> {
  if (!props.item) return
  const payload: Record<string, unknown> = { number: quantity.value }
  if (props.isSetmeal) {
    payload.setmealId = props.item.id
  } else {
    payload.dishId = props.item.id
    const flavor = buildDishFlavor()
    if (flavor) payload.dishFlavor = flavor
  }
  await addCart(payload)
  ElMessage.success('已加入购物车')
  emit('added')
  emit('update:visible', false)
}
</script>

<style scoped>
.sheet-mask {
  position: fixed;
  inset: 0;
  z-index: 2000;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  justify-content: center;
  align-items: flex-end;
}
.sheet-panel {
  width: 390px;
  max-width: 100%;
  background: #fff;
  border-radius: 20px 20px 0 0;
  display: flex;
  flex-direction: column;
  max-height: 82vh;
  overflow: hidden;
  box-shadow: 0 -8px 30px rgba(0, 0, 0, 0.18);
  animation: sheet-up 0.25s ease;
}
@keyframes sheet-up {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}
.sheet-scroll {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
}

/* 顶部大图 */
.sheet-cover {
  position: relative;
  width: 100%;
  height: 180px;
  background: var(--wl-bg);
}
.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.sheet-close {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 30px;
  height: 30px;
  border: none;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.35);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.sheet-body {
  padding: 14px 16px 18px;
}
.dish-name {
  margin: 0;
  font-size: 19px;
  color: var(--wl-text);
}
.dish-desc {
  margin: 5px 0;
  color: var(--wl-text-muted);
  font-size: 12px;
  line-height: 1.5;
}
.dish-price {
  color: var(--wl-primary);
  font-size: 20px;
  font-weight: 800;
}
.dish-price small {
  font-size: 12px;
  margin-right: 1px;
}

/* 规格分组 */
.group {
  margin-top: 16px;
}
.group-title {
  font-size: 13px;
  font-weight: 700;
  color: var(--wl-text);
  margin-bottom: 9px;
}
.pill-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.pill {
  border: 1px solid #ececec;
  background: #f6f6f6;
  color: #555;
  padding: 7px 16px;
  border-radius: 16px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.18s;
}
.pill.active {
  background: var(--wl-primary);
  border-color: var(--wl-primary);
  color: #fff;
  font-weight: 600;
}
.setmeal-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.setmeal-tag {
  background: var(--wl-bg);
  border: 1px solid var(--wl-card-border);
  border-radius: 14px;
  padding: 4px 12px;
  font-size: 12px;
  color: var(--wl-text);
}

/* 底部固定条 */
.sheet-footer {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px calc(10px + env(safe-area-inset-bottom, 0px));
  border-top: 1px solid #f2f2f2;
  background: #fff;
}
.stepper {
  display: flex;
  align-items: center;
  gap: 8px;
}
.step-btn {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  border: 1px solid #e6e6e6;
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
.step-num {
  min-width: 18px;
  text-align: center;
  font-weight: 600;
  font-size: 14px;
}
.total-block {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  line-height: 1.2;
  margin-right: 4px;
}
.total-label {
  font-size: 10px;
  color: var(--wl-text-muted);
}
.total-amount {
  color: var(--wl-primary);
  font-size: 18px;
  font-weight: 800;
}
.total-amount small {
  font-size: 11px;
  margin-right: 1px;
}
.add-cart-btn {
  flex: 0 0 auto;
  height: 42px;
  padding: 0 22px;
  border: none;
  border-radius: 21px;
  background: linear-gradient(90deg, var(--wl-primary), var(--wl-primary-light));
  color: #fff;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  white-space: nowrap;
}
.add-cart-btn:active {
  opacity: 0.9;
}
</style>
