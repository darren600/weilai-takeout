<template>
  <!-- 地址管理：地址卡列表 + 新增/编辑弹窗（省市区级联）+ 默认标记 -->
  <div class="address-page">
    <div class="page-heading">
      <h1>地址管理</h1>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon> 新增地址
      </el-button>
    </div>

    <el-empty v-if="addresses.length === 0" description="暂无地址，点击右上角新增" />

    <div class="address-grid">
      <el-card
        v-for="addr in addresses"
        :key="addr.id"
        class="address-card"
        :class="{ default: addr.isDefault === 1 }"
        shadow="hover"
      >
        <div class="addr-top">
          <span class="addr-name">{{ addr.consignee }}</span>
          <span class="addr-phone">{{ addr.phone }}</span>
          <el-tag v-if="addr.isDefault === 1" type="success" size="small" effect="light">默认</el-tag>
        </div>
        <div class="addr-detail">
          {{ addr.provinceName }}{{ addr.cityName }}{{ addr.districtName }}{{ addr.detail }}
        </div>
        <div class="addr-actions">
          <el-button link type="primary" size="small" @click="openDialog(addr)">编辑</el-button>
          <el-button
            v-if="addr.isDefault !== 1"
            link
            type="warning"
            size="small"
            @click="handleSetDefault(addr)"
          >设为默认</el-button>
          <el-button link type="danger" size="small" @click="handleDelete(addr)">删除</el-button>
        </div>
      </el-card>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑地址' : '新增地址'" width="360px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="收货人" prop="consignee">
          <el-input v-model="form.consignee" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="所在地区" prop="region">
          <el-cascader
            v-model="selectedRegion"
            :options="regionOptions"
            placeholder="请选择省 / 市 / 区"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="详细地址" prop="detail">
          <el-input v-model="form.detail" type="textarea" :rows="2" placeholder="街道、门牌号等" />
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="form.isDefaultBool" />
        </el-form-item>
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
import { Plus } from '@element-plus/icons-vue'
import {
  listAddress, addAddress, updateAddress, setDefaultAddress, deleteAddress
} from '@/api/user/address'

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
interface RegionOption { value: string; label: string; children?: RegionOption[] }

/** 精简省市区数据（演示用，覆盖常见地区；真实项目应接行政区接口） */
const regionOptions: RegionOption[] = [
  {
    value: '北京', label: '北京', children: [
      { value: '北京', label: '北京', children: [
        { value: '朝阳区', label: '朝阳区' }, { value: '海淀区', label: '海淀区' }, { value: '东城区', label: '东城区' }
      ] }
    ]
  },
  {
    value: '上海', label: '上海', children: [
      { value: '上海', label: '上海', children: [
        { value: '浦东新区', label: '浦东新区' }, { value: '黄浦区', label: '黄浦区' }, { value: '徐汇区', label: '徐汇区' }
      ] }
    ]
  },
  {
    value: '广东', label: '广东', children: [
      { value: '广州', label: '广州', children: [
        { value: '天河区', label: '天河区' }, { value: '越秀区', label: '越秀区' }, { value: '海珠区', label: '海珠区' }
      ] },
      { value: '深圳', label: '深圳', children: [
        { value: '南山区', label: '南山区' }, { value: '福田区', label: '福田区' }, { value: '宝安区', label: '宝安区' }
      ] }
    ]
  },
  {
    value: '浙江', label: '浙江', children: [
      { value: '杭州', label: '杭州', children: [
        { value: '西湖区', label: '西湖区' }, { value: '余杭区', label: '余杭区' }, { value: '滨江区', label: '滨江区' }
      ] }
    ]
  }
]

const addresses = ref<AddressLike[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const saving = ref(false)
const selectedRegion = ref<string[]>([])
const formRef = ref<FormInstance>()
const form = reactive({
  consignee: '',
  phone: '',
  detail: '',
  isDefaultBool: false
})
const rules: FormRules = {
  consignee: [{ required: true, message: '请输入收货人', trigger: 'blur' }],
  phone: [{ required: true, pattern: /^1\d{10}$/, message: '请输入正确手机号', trigger: 'blur' }],
  region: [{ validator: () => selectedRegion.value.length === 3, message: '请选择所在地区', trigger: 'change' }],
  detail: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

async function loadAddresses(): Promise<void> {
  addresses.value = (await listAddress()) as AddressLike[]
}

/** 打开弹窗：编辑时回填表单与级联选择 */
function openDialog(addr?: AddressLike): void {
  editingId.value = addr?.id || null
  form.consignee = addr?.consignee || ''
  form.phone = addr?.phone || ''
  form.detail = addr?.detail || ''
  form.isDefaultBool = addr?.isDefault === 1
  selectedRegion.value = addr
    ? [addr.provinceName || '', addr.cityName || '', addr.districtName || '']
    : []
  dialogVisible.value = true
}

/** 保存：新增或编辑 */
async function handleSave(): Promise<void> {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const payload: Record<string, unknown> = {
      consignee: form.consignee,
      phone: form.phone,
      detail: form.detail,
      provinceName: selectedRegion.value[0] || '',
      cityName: selectedRegion.value[1] || '',
      districtName: selectedRegion.value[2] || '',
      isDefault: form.isDefaultBool ? 1 : 0
    }
    if (editingId.value) {
      await updateAddress({ ...payload, id: editingId.value })
      ElMessage.success('修改成功')
    } else {
      await addAddress(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await loadAddresses()
  } finally {
    saving.value = false
  }
}

/** 设为默认 */
async function handleSetDefault(addr: AddressLike): Promise<void> {
  await setDefaultAddress({ id: addr.id })
  ElMessage.success('已设为默认')
  await loadAddresses()
}

/** 删除 */
async function handleDelete(addr: AddressLike): Promise<void> {
  await ElMessageBox.confirm('确认删除该地址吗？', '提示', { type: 'warning' })
  await deleteAddress(addr.id)
  ElMessage.success('已删除')
  await loadAddresses()
}

onMounted(loadAddresses)
</script>

<style scoped>
.address-page { padding-bottom: 8px; }
.page-heading { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.page-heading h1 { margin: 0; font-size: 18px; color: var(--wl-text); }
.address-grid { display: flex; flex-direction: column; gap: 10px; }
.address-card {
  border-radius: 12px;
  border: 1px solid var(--wl-card-border);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}
.address-card.default { border-color: var(--wl-primary); }
.addr-top { display: flex; align-items: center; gap: 10px; }
.addr-name { font-weight: 700; font-size: 15px; }
.addr-phone { color: var(--wl-text-muted); font-size: 12px; }
.addr-detail { margin: 8px 0; color: var(--wl-text); font-size: 12px; line-height: 1.5; }
.addr-actions { display: flex; gap: 6px; }
</style>
