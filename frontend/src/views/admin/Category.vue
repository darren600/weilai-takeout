<template>
  <!-- 分类管理：菜品/套餐分组 + 排序 + 启停 + 新增/编辑 -->
  <div class="admin-category-page">
    <h1 class="admin-page-title">分类管理</h1>
    <el-card class="admin-card">
      <div class="admin-toolbar">
        <el-radio-group v-model="activeType" @change="loadCategories">
          <el-radio-button :label="1">菜品分类</el-radio-button>
          <el-radio-button :label="2">套餐分类</el-radio-button>
        </el-radio-group>
        <el-button type="success" class="toolbar-right" @click="openDialog()">新增分类</el-button>
      </div>

      <el-table :data="categories" v-loading="loading" class="admin-table" border stripe>
        <el-table-column prop="name" label="分类名称" min-width="160" />
        <el-table-column prop="sort" label="排序" width="100" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="light">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-button link size="small" :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑分类' : '新增分类'" width="440px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="分类名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="排序" prop="sort"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  pageCategory, addCategory, updateCategory, deleteCategory, updateCategoryStatus
} from '@/api/admin/category'
import '@/styles/admin.css'

interface CategoryLike {
  id: number
  name: string
  type: number
  sort: number
  status: number
  updateTime?: string
}

const activeType = ref(1)
const categories = ref<CategoryLike[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive({ name: '', sort: 0 })
const rules: FormRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

async function loadCategories(): Promise<void> {
  loading.value = true
  try {
    const result = (await pageCategory({
      page: 1, pageSize: 100, type: activeType.value
    })) as { total: number; records: CategoryLike[] }
    categories.value = result.records
  } catch {
    categories.value = []
  } finally {
    loading.value = false
  }
}

function openDialog(row?: CategoryLike): void {
  editingId.value = row?.id || null
  form.name = row?.name || ''
  form.sort = row?.sort || 0
  dialogVisible.value = true
}

async function handleSave(): Promise<void> {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const payload: Record<string, unknown> = {
      name: form.name, sort: form.sort, type: activeType.value
    }
    if (editingId.value) {
      await updateCategory({ ...payload, id: editingId.value })
      ElMessage.success('修改成功')
    } else {
      await addCategory(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await loadCategories()
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row: CategoryLike): Promise<void> {
  const next = row.status === 1 ? 0 : 1
  await updateCategoryStatus(next, row.id)
  ElMessage.success(next === 1 ? '已启用' : '已禁用')
  await loadCategories()
}

async function handleDelete(row: CategoryLike): Promise<void> {
  await ElMessageBox.confirm(`确认删除分类「${row.name}」吗？`, '提示', { type: 'warning' })
  await deleteCategory(row.id)
  ElMessage.success('已删除')
  await loadCategories()
}

onMounted(loadCategories)
</script>

<style scoped>
.toolbar-right { margin-left: auto; }
</style>
