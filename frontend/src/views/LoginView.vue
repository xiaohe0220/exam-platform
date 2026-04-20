<template>
  <div class="page">
    <PublicHeader />
    <main class="main">
      <div class="main-bg" aria-hidden="true" />
      <div class="card-wrap">
        <div class="card">
          <h2 class="card-title">账号登录</h2>
          <p class="card-sub">学生请使用学号，教师请使用工号</p>
          <el-form :model="form" label-position="top" @submit.prevent="onSubmit">
            <el-form-item label="账号">
              <el-input
                v-model="form.username"
                autocomplete="username"
                placeholder="学号/工号"
                size="large"
              />
            </el-form-item>
            <el-form-item label="密码">
              <el-input
                v-model="form.password"
                type="password"
                autocomplete="current-password"
                show-password
                size="large"
              />
            </el-form-item>
            <div class="row-links">
              <el-button link type="primary" @click="openRegister">注册账号</el-button>
              <el-button link type="primary" @click="openForgot">忘记密码</el-button>
            </div>
            <el-button type="primary" size="large" class="w100" :loading="loading" native-type="submit">
              登录
            </el-button>
          </el-form>
          <p class="hint">演示账号：student / teacher / admin（与注册账号互不影响）</p>
        </div>
      </div>
    </main>
    <PublicFooter />

    <el-dialog v-model="dlgReg" title="注册账号" width="480px" destroy-on-close @closed="resetReg">
      <el-form label-position="top">
        <el-form-item label="身份">
          <el-radio-group v-model="reg.role">
            <el-radio-button label="STUDENT">学生（学号）</el-radio-button>
            <el-radio-button label="TEACHER">教师（工号）</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="reg.role === 'STUDENT' ? '学号' : '工号'">
          <el-input v-model="reg.username" :placeholder="reg.role === 'STUDENT' ? '请输入学号' : '请输入工号'" />
        </el-form-item>
        <el-form-item label="姓名（可选）">
          <el-input v-model="reg.displayName" placeholder="默认为账号" />
        </el-form-item>
        <el-form-item v-if="reg.role === 'STUDENT'" label="班级（可选）">
          <el-input v-model="reg.className" placeholder="如 计算机2101" />
        </el-form-item>
        <el-form-item label="学院（可选）">
          <el-input v-model="reg.college" placeholder="如 计算机学院" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="reg.password" type="password" show-password placeholder="至少 6 位" />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="reg.password2" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dlgReg = false">取消</el-button>
        <el-button type="primary" :loading="regLoading" @click="submitRegister">注册并登录</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="dlgForgot" title="重置密码" width="440px" destroy-on-close>
      <p class="dlg-tip">请输入学号/工号与新密码（演示环境直接重置；生产需配合短信/邮箱验证）</p>
      <el-form label-position="top">
        <el-form-item label="账号">
          <el-input v-model="forgot.username" placeholder="学号或工号" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="forgot.password" type="password" show-password placeholder="至少 6 位" />
        </el-form-item>
        <el-form-item label="确认新密码">
          <el-input v-model="forgot.password2" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dlgForgot = false">取消</el-button>
        <el-button type="primary" :loading="forgotLoading" @click="submitForgot">确认重置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PublicHeader from '../components/public/PublicHeader.vue'
import PublicFooter from '../components/public/PublicFooter.vue'
import { useUserStore } from '../stores/user'

const router = useRouter()
const store = useUserStore()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

const dlgReg = ref(false)
const regLoading = ref(false)
const reg = reactive({
  role: 'STUDENT',
  username: '',
  displayName: '',
  className: '',
  college: '',
  password: '',
  password2: ''
})

const dlgForgot = ref(false)
const forgotLoading = ref(false)
const forgot = reactive({ username: '', password: '', password2: '' })

function openRegister() {
  resetReg()
  dlgReg.value = true
}

function resetReg() {
  reg.role = 'STUDENT'
  reg.username = ''
  reg.displayName = ''
  reg.className = ''
  reg.college = ''
  reg.password = ''
  reg.password2 = ''
}

function openForgot() {
  forgot.username = ''
  forgot.password = ''
  forgot.password2 = ''
  dlgForgot.value = true
}

async function onSubmit() {
  loading.value = true
  try {
    const data = await store.login(form.username, form.password)
    ElMessage.success('登录成功')
    routeAfter(data.role)
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}

function routeAfter(role) {
  if (role === 'ADMIN' || role === 'COLLEGE_ADMIN') router.push('/admin')
  else if (role === 'TEACHER') router.push('/teacher')
  else router.push('/student')
}

async function submitRegister() {
  if (!reg.username?.trim()) {
    ElMessage.warning('请填写学号或工号')
    return
  }
  if (reg.password.length < 6) {
    ElMessage.warning('密码至少 6 位')
    return
  }
  if (reg.password !== reg.password2) {
    ElMessage.warning('两次密码不一致')
    return
  }
  regLoading.value = true
  try {
    const data = await store.register({
      username: reg.username.trim(),
      password: reg.password,
      role: reg.role,
      displayName: reg.displayName?.trim() || undefined,
      className: reg.className?.trim() || undefined,
      college: reg.college?.trim() || undefined
    })
    ElMessage.success('注册成功')
    dlgReg.value = false
    routeAfter(data.role)
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    regLoading.value = false
  }
}

async function submitForgot() {
  if (!forgot.username?.trim()) {
    ElMessage.warning('请填写账号')
    return
  }
  if (forgot.password.length < 6) {
    ElMessage.warning('新密码至少 6 位')
    return
  }
  if (forgot.password !== forgot.password2) {
    ElMessage.warning('两次密码不一致')
    return
  }
  forgotLoading.value = true
  try {
    await store.resetPassword(forgot.username.trim(), forgot.password)
    ElMessage.success('密码已重置，请使用新密码登录')
    dlgForgot.value = false
    form.username = forgot.username.trim()
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    forgotLoading.value = false
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #fff;
}
.main {
  position: relative;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 16px 56px;
  min-height: 420px;
}
.main-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(125deg, #3b6cff 0%, #6366f1 38%, #7c3aed 72%, #5b21b6 100%);
}
.main-bg::after {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(ellipse 70% 50% at 50% 0%, rgba(255, 255, 255, 0.12), transparent 55%);
}
.card-wrap {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 440px;
}
.card {
  background: #fff;
  border-radius: 16px;
  padding: 36px 32px 28px;
  box-shadow: 0 24px 48px rgba(15, 23, 42, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.25);
}
.card-title {
  margin: 0 0 8px;
  font-size: 22px;
  font-weight: 700;
  color: #111827;
  text-align: center;
}
.card-sub {
  margin: 0 0 24px;
  text-align: center;
  font-size: 13px;
  color: #6b7280;
}
.row-links {
  display: flex;
  justify-content: space-between;
  margin: -4px 0 16px;
}
.w100 {
  width: 100%;
}
.hint {
  margin-top: 18px;
  font-size: 12px;
  color: #9ca3af;
  text-align: center;
  line-height: 1.5;
}
.dlg-tip {
  margin: 0 0 12px;
  font-size: 12px;
  color: #6b7280;
  line-height: 1.5;
}
</style>
