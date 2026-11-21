<template>
  <!-- 点餐首页（H5）：左 88px 分类竖栏 + 右菜品列表，两栏各自滚动 -->
  <div class="home-split">
    <!-- 左侧分类竖栏 -->
    <aside class="cat-rail">
      <button
        v-for="category in categories"
        :key="category.id"
        type="button"
        class="rail-item"
        :class="{ active: category.id === activeCategoryId }"
        @click="selectCategory(category)"
      >
        {{ category.name }}
      </button>
    </aside>

    <!-- 右侧菜品区 -->
    <section ref="dishAreaRef" class="dish-area">
      <div class="dish-area-head">
        <h2>{{ activeCategoryName }}</h2>
        <span class="dish-count">{{ displayItems.length }} 款</span>
      </div>

      <el-alert
        v-if="shopStatus === 0"
        class="shop-closed"
        type="warning"
        :closable="false"
        show-icon
        title="店铺休息中，暂不可下单"
      />

      <el-empty v-if="!loading && displayItems.length === 0" description="该分类暂无在售商品" :image-size="72" />

      <!-- 单列横排菜品卡片 -->
      <div class="dish-list">
        <div
          v-for="item in displayItems"
          :key="`${item.kind}-${item.id}`"
          class="dish-row"
          @click="openDetail(item)"
        >
          <img :src="item.image" :alt="item.name" class="dish-thumb" @error="handleImageError">
          <div class="dish-meta">
            <div class="dish-name">{{ item.name }}</div>
            <div class="dish-desc">{{ item.description || '一道用心的好味道' }}</div>
            <div class="dish-price"><small>¥</small>{{ item.price }}</div>
          </div>
          <button class="dish-add" type="button" @click.stop="openDetail(item)">
            <el-icon :size="16"><Plus /></el-icon>
          </button>
        </div>
      </div>
    </section>

    <!-- 详情弹层（底部半屏抽屉） -->
    <DishDialog
      v-model:visible="dialogVisible"
      :item="selectedItem"
      :is-setmeal="selectedItem?.kind === 'setmeal'"
      @added="onCartAdded"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import { listCategory } from '@/api/user/category'
import { listDish } from '@/api/user/dish'
import { listSetmeal } from '@/api/user/setmeal'
import { getShopStatus } from '@/api/user/shop'
import { handleImageError } from '@/utils/image'
import DishDialog from './DishDialog.vue'

interface CategoryLike { id: number; name: number | string; type: number }
interface DishLike {
  id: number
  name: string
  price: number
  image?: string
  description?: string
  flavors?: unknown
  status?: number
}
/** 统一展示模型：菜品与套餐合并，用 kind 区分 */
interface DisplayItem extends DishLike {
  kind: 'dish' | 'setmeal'
  categoryId?: number
}

const emit = defineEmits<{
  (e: 'cart-changed'): void
}>()

const route = useRoute()
const categories = ref<CategoryLike[]>([])
const activeCategoryId = ref<number | null>(null)
const rawItems = ref<DisplayItem[]>([])
const loading = ref(false)
const shopStatus = ref<number>(1)
const dialogVisible = ref(false)
const selectedItem = ref<DisplayItem | null>(null)
const dishAreaRef = ref<HTMLElement | null>(null)

/** 搜索关键字（来自顶栏或路由 query） */
const keyword = ref('')

/** 当前分类名 */
const activeCategoryName = computed(
  () => categories.value.find((c) => c.id === activeCategoryId.value)?.name || '全部商品'
)

/** 展示列表：按分类 + 关键字过滤，停售品前端再兜一层过滤 */
const displayItems = computed(() => {
  let list = rawItems.value.filter((item) => item.status !== 0)
  if (activeCategoryId.value !== null && activeCategoryId.value !== 0) {
    list = list.filter((item) => item.categoryId === activeCategoryId.value)
  }
  const key = keyword.value.trim()
  if (key) {
    list = list.filter((item) => item.name.includes(key))
  }
  return list
})

/** 加载店铺状态 */
async function loadShopStatus(): Promise<void> {
  try {
    shopStatus.value = (await getShopStatus()) as number
  } catch {
    shopStatus.value = 1
  }
}

/** 加载分类：菜品分类(type=1) + 套餐分类(type=2) 合并展示 */
async function loadCategories(): Promise<void> {
  try {
    const [dishCats, setmealCats] = await Promise.all([
      listCategory(1) as Promise<CategoryLike[]>,
      listCategory(2) as Promise<CategoryLike[]>
    ])
    categories.value = [
      { id: 0, name: '全部', type: 0 } as CategoryLike,
      ...(dishCats || []),
      ...(setmealCats || [])
    ]
    if (categories.value.length > 0) {
      activeCategoryId.value = categories.value[0].id
    }
  } catch {
    categories.value = [{ id: 0, name: '全部', type: 0 } as CategoryLike]
    activeCategoryId.value = 0
  }
}

/** 加载全部菜品与套餐（一次性拉取，前端按分类过滤）；allSettled 保证单接口异常不影响另一个 */
async function loadItems(): Promise<void> {
  loading.value = true
  try {
    const [dishRes, setmealRes] = await Promise.allSettled([
      listDish() as Promise<DishLike[]>,
      listSetmeal() as Promise<DishLike[]>
    ])
    const dishes = dishRes.status === 'fulfilled' ? dishRes.value : []
    const setmeals = setmealRes.status === 'fulfilled' ? setmealRes.value : []
    const dishItems: DisplayItem[] = (dishes || []).map((d) => ({ ...d, kind: 'dish' }))
    const setmealItems: DisplayItem[] = (setmeals || []).map((s) => ({ ...s, kind: 'setmeal' }))
    rawItems.value = [...dishItems, ...setmealItems]
  } catch {
    rawItems.value = []
  } finally {
    loading.value = false
  }
}

/** 选择分类：右侧菜品区滚回顶部 */
function selectCategory(category: CategoryLike): void {
  activeCategoryId.value = category.id
  if (dishAreaRef.value) dishAreaRef.value.scrollTop = 0
}

/** 打开详情弹层 */
function openDetail(item: DisplayItem): void {
  selectedItem.value = item
  dialogVisible.value = true
}

/** 详情弹层加购成功 → 刷新角标 */
function onCartAdded(): void {
  emit('cart-changed')
}

// 监听路由 query 关键字变化（顶栏搜索跳转过来）
watch(
  () => route.query.keyword,
  (val) => {
    keyword.value = typeof val === 'string' ? val : ''
  },
  { immediate: true }
)

onMounted(async () => {
  await Promise.all([loadShopStatus(), loadCategories()])
  await loadItems()
})
</script>

<style scoped>
/* 占满内容区高度，左右两栏各自滚动 */
.home-split {
  height: 100%;
  display: flex;
  overflow: hidden;
  border-radius: 14px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

/* 左侧分类竖栏：88px 固定，灰底，可滚动 */
.cat-rail {
  flex: 0 0 88px;
  width: 88px;
  height: 100%;
  overflow-y: auto;
  background: #f5f5f5;
  -webkit-overflow-scrolling: touch;
}
.cat-rail::-webkit-scrollbar {
  width: 0;
}
.rail-item {
  position: relative;
  width: 100%;
  border: none;
  background: transparent;
  padding: 15px 6px;
  color: #666;
  font-size: 13px;
  line-height: 1.2;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
}
.rail-item.active {
  background: #fff;
  color: var(--wl-primary);
  font-weight: 700;
}
.rail-item.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 5px;
  height: 26px;
  background: var(--wl-primary);
  border-radius: 0 3px 3px 0;
}

/* 右侧菜品区：独立滚动 */
.dish-area {
  flex: 1;
  min-width: 0;
  height: 100%;
  overflow-y: auto;
  padding: 10px 12px 18px;
  background: #fff;
  -webkit-overflow-scrolling: touch;
}
.dish-area-head {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 8px;
}
.dish-area-head h2 {
  margin: 0;
  font-size: 15px;
  color: var(--wl-text);
}
.dish-count {
  color: var(--wl-text-muted);
  font-size: 11px;
}
.shop-closed {
  margin-bottom: 8px;
}

/* 单列菜品卡片 */
.dish-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.dish-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border: 1px solid #f2f2f2;
  border-radius: 12px;
  background: #fff;
  cursor: pointer;
}
.dish-thumb {
  flex: 0 0 auto;
  width: 76px;
  height: 76px;
  border-radius: 10px;
  object-fit: cover;
  background: var(--wl-bg);
}
.dish-meta {
  flex: 1;
  min-width: 0;
}
.dish-name {
  font-size: 14px;
  font-weight: 700;
  color: var(--wl-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.dish-desc {
  font-size: 11px;
  color: var(--wl-text-muted);
  margin: 3px 0 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.dish-price {
  color: var(--wl-primary);
  font-size: 16px;
  font-weight: 800;
}
.dish-price small {
  font-size: 11px;
  margin-right: 1px;
}
.dish-add {
  flex: 0 0 auto;
  width: 30px;
  height: 30px;
  border: none;
  border-radius: 50%;
  background: var(--wl-primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}
.dish-add:active {
  transform: scale(0.9);
}
</style>
