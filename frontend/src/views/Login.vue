<template>
  <div class="login-page">
    <div class="login-split">
      <!-- 左 50%：橙色品牌宣传区 -->
      <BrandPanel class="login-brand" />

      <!-- 右 50%：登录表单区 -->
      <section class="login-form-side">
        <div class="login-form-inner">
          <div class="form-heading">
            <h1 class="form-title">欢迎回来</h1>
            <p class="form-subtitle">选择身份，开启味来之旅</p>
          </div>

          <!-- 身份分段切换（选中态橙色填充，非 el-tabs 下划线） -->
          <div class="role-switch">
            <button
              type="button"
              class="role-btn"
              :class="{ active: role === 'admin' }"
              @click="switchRole('admin')"
            >
              <el-icon><UserFilled /></el-icon>
              管理员
            </button>
            <button
              type="button"
              class="role-btn"
              :class="{ active: role === 'user' }"
              @click="switchRole('user')"
            >
              <el-icon><Avatar /></el-icon>
              用户
            </button>
          </div>

          <!-- 固定最小高度的表单容器：切换身份时高度不跳动 -->
          <div class="form-area">
            <!-- 管理员表单 -->
            <el-form
              v-if="role === 'admin'"
              ref="adminFormRef"
              :model="adminForm"
              :rules="adminRules"
              label-position="top"
              class="login-form"
              @keyup.enter="handleAdminLogin"
            >
              <el-form-item label="账号" prop="username">
                <el-input
                  v-model="adminForm.username"
                  placeholder="请输入管理员账号"
                  :prefix-icon="User"
                  size="large"
                  clearable
                />
              </el-form-item>
              <el-form-item label="密码" prop="password">
                <el-input
                  v-model="adminForm.password"
                  placeholder="请输入密码"
                  :prefix-icon="Lock"
                  type="password"
                  size="large"
                  show-password
                  clearable
                />
              </el-form-item>
              <el-button
                type="primary"
                size="large"
                class="submit-button"
                :loading="loading"
                @click="handleAdminLogin"
              >
                进入管理后台
              </el-button>
            </el-form>

            <!-- 用户表单：手机号 + 短信验证码 -->
            <el-form
              v-else
              ref="userFormRef"
              :model="userForm"
              :rules="userRules"
              label-position="top"
              class="login-form"
              @keyup.enter="handleUserLogin"
            >
              <el-form-item label="手机号" prop="phone">
                <el-input
                  v-model="userForm.phone"
                  placeholder="请输入手机号"
                  :prefix-icon="Iphone"
                  size="large"
                  clearable
                  maxlength="11"
                />
              </el-form-item>
              <el-form-item label="验证码" prop="code">
                <div class="code-row">
                  <el-input
                    v-model="userForm.code"
                    placeholder="请输入 6 位验证码"
                    :prefix-icon="Message"
                    size="large"
                    maxlength="6"
                    class="code-input"
                  />
                  <el-button
                    size="large"
                    class="code-button"
                    :disabled="countdown > 0"
                    :loading="sendingCode"
                    @click="handleSendCode"
                  >
                    {{ countdown > 0 ? `${countdown}s 后重发` : '获取验证码' }}
                  </el-button>
                </div>
              </el-form-item>
              <p class="form-hint">未注册手机号将自动注册</p>
              <el-button
                type="primary"
                size="large"
                class="submit-button"
                :loading="loading"
                @click="handleUserLogin"
              >
                登录 / 注册
              </el-button>
            </el-form>
          </div>

          <div class="form-footer">味来 Weilai · 外卖点餐演示项目</div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Iphone, Message, UserFilled, Avatar } from '@element-plus/icons-vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { login as adminLogin } from '@/api/admin/employee'
import { sendCode, smsLogin } from '@/api/user/auth'
import { setAdminToken, setUserToken } from '@/utils/auth'
import { useSessionStore } from '@/stores/session'
import type { UserRole } from '@/utils/auth'
import BrandPanel from '@/components/login/BrandPanel.vue'

const router = useRouter()
const session = useSessionStore()

/** 当前选中的身份 */
const role = ref<'admin' | 'user'>('admin')
/** 提交按钮 loading */
const loading = ref(false)
/** 发送验证码按钮 loading */
const sendingCode = ref(false)
/** 验证码重发倒计时（秒） */
const countdown = ref(0)
let countdownTimer: number | undefined

/** 管理员表单：默认留空，不预填任何演示账号 */
const adminFormRef = ref<FormInstance>()
const adminForm = reactive({ username: '', password: '' })
const adminRules: FormRules = {
  username: [{ required: true, message: '请输入管理员账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

/** 用户表单（手机号 + 短信验证码） */
const userFormRef = ref<FormInstance>()
const userForm = reactive({ phone: '', code: '' })
const userRules: FormRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '请输入 6 位数字验证码', trigger: 'blur' }
  ]
}

/** 切换身份：只切换展示，不清空已填内容，仅清除校验提示，避免多余心智负担 */
function switchRole(next: 'admin' | 'user'): void {
  role.value = next
  adminFormRef.value?.clearValidate()
  userFormRef.value?.clearValidate()
}

/** 启动 60 秒倒计时 */
function startCountdown(): void {
  countdown.value = 60
  countdownTimer = window.setInterval(() => {
    countdown.value -= 1
    if (countdown.value <= 0) {
      window.clearInterval(countdownTimer)
      countdownTimer = undefined
    }
  }, 1000)
}

/** 获取验证码：校验手机号 -> 调后端 -> 仅弹提示，不自动填入，用户自行手输 */
async function handleSendCode(): Promise<void> {
  if (!/^1\d{10}$/.test(userForm.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  sendingCode.value = true
  try {
    const code = await sendCode<string>(userForm.phone)
    // 无真实短信通道：dev 后端把验证码放到 data 返回，这里只提示、不自动写入输入框
    if (code) {
      ElMessage.success(`验证码已发送（演示验证码：${code}）`)
    } else {
      ElMessage.success('验证码已发送')
    }
    startCountdown()
  } finally {
    sendingCode.value = false
  }
}

/** 管理员登录：调 /admin/employee/login，成功写 wl_admin_token */
async function handleAdminLogin(): Promise<void> {
  const valid = await adminFormRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const data = await adminLogin<{ token: string }>({
      username: adminForm.username,
      password: adminForm.password
    })
    setAdminToken(data.token)
    session.setRole('admin' as UserRole)
    ElMessage.success('登录成功，欢迎回来')
    router.push('/admin/dashboard')
  } finally {
    loading.value = false
  }
}

/** 用户登录/注册：调 /user/user/smsLogin，成功写 wl_user_token */
async function handleUserLogin(): Promise<void> {
  const valid = await userFormRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const data = await smsLogin<{ token: string }>({
      phone: userForm.phone,
      code: userForm.code
    })
    setUserToken(data.token)
    // 记录手机号，供「我的」个人中心展示
    localStorage.setItem('wl_user_phone', userForm.phone)
    session.setRole('user' as UserRole)
    ElMessage.success('登录成功，开始点餐吧')
    router.push('/user/home')
  } finally {
    loading.value = false
  }
}

onUnmounted(() => {
  if (countdownTimer) window.clearInterval(countdownTimer)
})
</script>

<style scoped>
.login-page {
  height: 100vh;
  overflow: hidden;
}

/* 左右分栏：左 50% 品牌区，右 50% 表单区 */
.login-split {
  display: flex;
  height: 100%;
}
.login-brand {
  flex: 0 0 50%;
  max-width: 50%;
}
.login-form-side {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: #fff;
  overflow-y: auto;
}
.login-form-inner {
  width: 100%;
  max-width: 400px;
}

.form-heading {
  text-align: center;
  margin-bottom: 24px;
}
.form-title {
  margin: 0;
  font-size: 30px;
  font-weight: 800;
  color: var(--wl-text);
}
.form-subtitle {
  margin: 8px 0 0;
  font-size: 14px;
  color: var(--wl-text-muted);
}

/* 身份分段切换控件 */
.role-switch {
  display: flex;
  gap: 4px;
  padding: 4px;
  margin-bottom: 24px;
  background: #fdf1ea;
  border-radius: 12px;
}
.role-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px 0;
  border: none;
  border-radius: 9px;
  background: transparent;
  color: var(--wl-text-muted);
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}
.role-btn.active {
  background: var(--wl-primary);
  color: #fff;
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.35);
}

/* 固定最小高度：保证管理员/用户两个表单切换时高度一致、不跳动 */
.form-area {
  min-height: 340px;
}

.login-form :deep(.el-form-item__label) {
  font-weight: 600;
  color: var(--wl-text);
}
.form-hint {
  margin: -6px 0 16px;
  font-size: 12px;
  color: var(--wl-text-muted);
}
.code-row {
  display: flex;
  gap: 10px;
  width: 100%;
}
.code-input {
  flex: 1;
}
.code-button {
  flex: 0 0 auto;
  width: 128px;
}
.submit-button {
  width: 100%;
  height: 46px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 10px;
}
.form-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 12px;
  color: var(--wl-text-muted);
}

/* 窄屏：隐藏左侧品牌区，保证右侧表单可用 */
@media (max-width: 900px) {
  .login-brand {
    display: none;
  }
  .login-form-side {
    flex: 1;
  }
}
</style>
