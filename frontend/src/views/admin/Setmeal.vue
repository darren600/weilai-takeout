<template>
  <!-- 套餐管理：分页表格 + 新增/编辑抽屉 + 起售停售 + 套餐内菜品多选 -->
  <div class="admin-setmeal-page">
    <h1 class="admin-page-title">套餐管理</h1>
    <el-card class="admin-card">
      <div class="admin-toolbar">
        <el-input v-model="filters.name" placeholder="套餐名称" clearable style="width: 200px" />
        <el-button type="primary" @click="loadSetmeals">查询</el-button>
        <el-button type="success" @click="openDialog()">新增套餐</el-button>
      </div>

      <el-table :data="setmeals" v-loading="loading" class="admin-table" border stripe>
        <el-table-column prop="name" label="套餐名称" min-width="140" />
        <el-table-column label="图片" width="90">
          <template #default="{ row }">
            <img :src="row.image" class="admin-thumb" @error="handleImageError">
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="90">
          <template #default="{ row }">¥ {{ row.price }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="light">
              {{ row.status === 1 ? '起售' : '停售' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-button link size="small" :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
              {{ row.status === 1 ? '停售' : '起售' }}
            </el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="admin-pagination">
        <el-pagination v-model:current-page="page" v-model:page-size="pageSize" :total="total"
          :page-sizes="[10, 20]" layout="total, sizes, prev, pager, next"
          @size-change="loadSetmeals" @current-change="loadSetmeals" />
      </div>
    </el-card>

    <!-- 新增/编辑抽屉 -->
    <el-drawer v-model="dialogVisible" :title="editingId ? '编辑套餐' : '新增套餐'" size="540px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="套餐名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="套餐分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="套餐图片">
          <el-upload class="dish-uploader" :action="uploadUrl" :headers="uploadHeaders"
            :show-file-list="false" :on-success="handleUploadSuccess">
            <img v-if="form.image" :src="form.image" class="upload-preview" @error="handleImageError">
            <el-icon v-else class="upload-plus"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="选择菜品" prop="setmealDishes">
          <el-checkbox-group v-model="selectedDishIds" class="dish-checkboxes">
            <el-checkbox v-for="d in allDishes" :key="d.id" :value="d.id">
              {{ d.name }} (¥{{ d.price }})
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  pageSetmeal, addSetmeal, updateSetmeal, deleteSetmeal, updateSetmealStatus, getSetmealById
} from '@/api/admin/setmeal'
import { listCategory } from '@/api/admin/category'
import { listDish } from '@/api/admin/dish'
import { getAdminToken } from '@/utils/auth'
import { handleImageError } from '@/utils/image'
import '@/styles/admin.css'

interface SetmealLike { id: number; name: string; price: number; image?: string; description?: string; categoryId: number; status: number }
interface CategoryLike { id: number; name: number | string }
interface DishOption { id: number; name: string; price: number }

const uploadUrl = '/api/admin/common/upload'
const uploadHeaders = { token: getAdminToken() || '' }

const setmeals = ref<SetmealLike[]>([])
const categories = ref<CategoryLike[]>([])
const allDishes = ref<DishOption[]>([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)
const filters = reactive({ name: '' })

const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const saving = ref(false)
const selectedDishIds = ref<number[]>([])
const formRef = ref<FormInstance>()
const form = reactive({
  name: '', categoryId: undefined as number | undefined, price: 0,
  image: '', description: ''
})
const rules: FormRules = {
  name: [{ required: true, message: '请输入套餐名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

async function loadSetmeals(): Promise<void> {
  loading.value = true
  try {
    const result = (await pageSetmeal({
      page: page.value, pageSize: pageSize.value, name: filters.name || undefined
    })) as { total: number; records: SetmealLike[] }
    total.value = result.total
    setmeals.value = result.records
  } catch {
    setmeals.value = []
  } finally {
    loading.value = false
  }
}

async function loadOptions(): Promise<void> {
  try {
    // 套餐分类（type=2）用于套餐自身的分类下拉
    const packageCategories = (await listCategory(2)) as CategoryLike[]
    categories.value = packageCategories || []
    // 套餐可选菜品来自全部菜品分类：后端 dish/list 必须带 categoryId，
    // 故先取菜品分类（type=1），再逐分类查询后合并为候选菜品列表
    const dishCategories = (await listCategory(1)) as CategoryLike[]
    const dishGroups = await Promise.all(
      (dishCategories || []).map((category) => listDish(category.id) as Promise<DishOption[]>)
    )
    allDishes.value = dishGroups.flatMap((group) => group || [])
  } catch {
    categories.value = []
    allDishes.value = []
  }
}

/** 打开抽屉：编辑时查套餐内菜品回填勾选 */
async function openDialog(row?: SetmealLike): Promise<void> {
  editingId.value = row?.id || null
  form.name = row?.name || ''
  form.categoryId = row?.categoryId
  form.price = row?.price || 0
  form.image = row?.image || ''
  form.description = row?.description || ''
  selectedDishIds.value = []
  if (row?.id) {
    try {
      const detail = (await getSetmealById(row.id)) as { setmealDishes?: Array<{ dishId: number }> }
      selectedDishIds.value = (detail.setmealDishes || []).map((d) => d.dishId)
    } catch {
      selectedDishIds.value = []
    }
  }
  dialogVisible.value = true
}

function handleUploadSuccess(response: { code: number; data: string }): void {
  if (response?.code === 1) {
    form.image = response.data
    ElMessage.success('上传成功')
  } else {
    ElMessage.error('上传失败')
  }
}

async function handleSave(): Promise<void> {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const setmealDishes = selectedDishIds.value.map((dishId) => {
      const dish = allDishes.value.find((d) => d.id === dishId)
      return { dishId, name: dish?.name || '', price: dish?.price || 0, copies: 1 }
    })
    const payload: Record<string, unknown> = {
      name: form.name, categoryId: form.categoryId, price: form.price,
      image: form.image, description: form.description, setmealDishes
    }
    if (editingId.value) {
      await updateSetmeal({ ...payload, id: editingId.value })
      ElMessage.success('修改成功')
    } else {
      await addSetmeal(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await loadSetmeals()
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row: SetmealLike): Promise<void> {
  const next = row.status === 1 ? 0 : 1
  await updateSetmealStatus(next, row.id)
  ElMessage.success(next === 1 ? '已起售' : '已停售')
  await loadSetmeals()
}

async function handleDelete(row: SetmealLike): Promise<void> {
  await ElMessageBox.confirm(`确认删除套餐「${row.name}」吗？`, '提示', { type: 'warning' })
  await deleteSetmeal([row.id])
  ElMessage.success('已删除')
  await loadSetmeals()
}

onMounted(() => {
  void loadSetmeals()
  void loadOptions()
})
</script>

<style scoped>
.dish-uploader :deep(.el-upload) {
  border: 1px dashed #d9d9d9; border-radius: 6px; cursor: pointer;
  width: 120px; height: 120px; display: flex; align-items: center; justify-content: center;
}
.upload-preview { width: 118px; height: 118px; object-fit: cover; border-radius: 6px; }
.upload-plus { font-size: 32px; color: #8c939d; }
.dish-checkboxes { display: flex; flex-wrap: wrap; gap: 8px 18px; }
</style>
