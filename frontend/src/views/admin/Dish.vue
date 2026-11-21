<template>
  <!-- 菜品管理：分页表格 + 新增/编辑抽屉 + 起售停售开关 + 图片上传 -->
  <div class="admin-dish-page">
    <h1 class="admin-page-title">菜品管理</h1>
    <el-card class="admin-card">
      <div class="admin-toolbar">
        <el-input v-model="filters.name" placeholder="菜品名称" clearable style="width: 200px" />
        <el-select v-model="filters.categoryId" placeholder="分类" clearable style="width: 160px">
          <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
        <el-button type="primary" @click="loadDishes">查询</el-button>
        <el-button type="success" @click="openDialog()">新增菜品</el-button>
      </div>

      <el-table :data="dishes" v-loading="loading" class="admin-table" border stripe>
        <el-table-column prop="name" label="菜品名称" min-width="140" />
        <el-table-column label="图片" width="90">
          <template #default="{ row }">
            <img :src="row.image" class="admin-thumb" @error="handleImageError">
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" width="120" />
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
            <el-button
              link size="small"
              :type="row.status === 1 ? 'warning' : 'success'"
              @click="toggleStatus(row)"
            >{{ row.status === 1 ? '停售' : '起售' }}</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="admin-pagination">
        <el-pagination
          v-model:current-page="page" v-model:page-size="pageSize" :total="total"
          :page-sizes="[10, 20]" layout="total, sizes, prev, pager, next"
          @size-change="loadDishes" @current-change="loadDishes"
        />
      </div>
    </el-card>

    <!-- 新增/编辑抽屉 -->
    <el-drawer v-model="dialogVisible" :title="editingId ? '编辑菜品' : '新增菜品'" size="520px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="菜品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入菜品名称" />
        </el-form-item>
        <el-form-item label="菜品分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="菜品图片">
          <el-upload
            class="dish-uploader"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleUploadSuccess"
          >
            <img v-if="form.image" :src="form.image" class="upload-preview" @error="handleImageError">
            <el-icon v-else class="upload-plus"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="菜品描述（选填）" />
        </el-form-item>
        <el-form-item label="口味">
          <div class="flavor-list">
            <el-tag
              v-for="(flavor, i) in form.flavors"
              :key="i" closable @close="form.flavors.splice(i, 1)"
            >{{ flavor }}</el-tag>
            <el-input v-model="newFlavor" size="small" placeholder="输入口味后回车" style="width: 150px" @keyup.enter="addFlavor" />
          </div>
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
  pageDish, addDish, updateDish, deleteDish, updateDishStatus, listDish
} from '@/api/admin/dish'
import { listCategory } from '@/api/admin/category'
import { getAdminToken } from '@/utils/auth'
import { handleImageError } from '@/utils/image'
import '@/styles/admin.css'

interface DishLike {
  id: number
  name: string
  price: number
  image?: string
  description?: string
  categoryId: number
  categoryName?: string
  status: number
  flavors?: Array<{ name: string; value: string }>
}
interface CategoryLike { id: number; name: number | string }

const uploadUrl = '/api/admin/common/upload'
const uploadHeaders = { token: getAdminToken() || '' }

const dishes = ref<DishLike[]>([])
const categories = ref<CategoryLike[]>([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)
const filters = reactive({ name: '', categoryId: undefined as number | undefined })

const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const saving = ref(false)
const newFlavor = ref('')
const formRef = ref<FormInstance>()
const form = reactive({
  name: '', categoryId: undefined as number | undefined, price: 0,
  image: '', description: '', flavors: [] as string[]
})
const rules: FormRules = {
  name: [{ required: true, message: '请输入菜品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

async function loadDishes(): Promise<void> {
  loading.value = true
  try {
    const result = (await pageDish({
      page: page.value, pageSize: pageSize.value,
      name: filters.name || undefined, categoryId: filters.categoryId
    })) as { total: number; records: DishLike[] }
    total.value = result.total
    dishes.value = result.records
  } catch {
    dishes.value = []
  } finally {
    loading.value = false
  }
}

async function loadCategories(): Promise<void> {
  try {
    categories.value = (await listCategory(1)) as CategoryLike[]
  } catch {
    categories.value = []
  }
}

/** 打开抽屉：编辑时回填口味（后端 flavors 是 [{name,value}]） */
function openDialog(row?: DishLike): void {
  editingId.value = row?.id || null
  form.name = row?.name || ''
  form.categoryId = row?.categoryId
  form.price = row?.price || 0
  form.image = row?.image || ''
  form.description = row?.description || ''
  form.flavors = (row?.flavors || []).map((f) => f.value)
  dialogVisible.value = true
}

function addFlavor(): void {
  const value = newFlavor.value.trim()
  if (value && !form.flavors.includes(value)) form.flavors.push(value)
  newFlavor.value = ''
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
    const payload: Record<string, unknown> = {
      name: form.name, categoryId: form.categoryId, price: form.price,
      image: form.image, description: form.description,
      flavors: form.flavors.map((value) => ({ name: '口味', value }))
    }
    if (editingId.value) {
      await updateDish({ ...payload, id: editingId.value })
      ElMessage.success('修改成功')
    } else {
      await addDish(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await loadDishes()
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row: DishLike): Promise<void> {
  const next = row.status === 1 ? 0 : 1
  await updateDishStatus(next, row.id)
  ElMessage.success(next === 1 ? '已起售' : '已停售')
  await loadDishes()
}

async function handleDelete(row: DishLike): Promise<void> {
  await ElMessageBox.confirm(`确认删除菜品「${row.name}」吗？`, '提示', { type: 'warning' })
  await deleteDish([row.id])
  ElMessage.success('已删除')
  await loadDishes()
}

onMounted(() => {
  void loadDishes()
  void loadCategories()
})
</script>

<style scoped>
.dish-uploader :deep(.el-upload) {
  border: 1px dashed #d9d9d9; border-radius: 6px; cursor: pointer;
  width: 120px; height: 120px; display: flex; align-items: center; justify-content: center;
}
.upload-preview { width: 118px; height: 118px; object-fit: cover; border-radius: 6px; }
.upload-plus { font-size: 32px; color: #8c939d; }
.flavor-list { display: flex; flex-wrap: wrap; gap: 8px; align-items: center; }
</style>
