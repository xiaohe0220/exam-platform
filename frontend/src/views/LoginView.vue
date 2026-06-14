<template>
  <div class="page">
    <PublicHeader />

    <main class="main">
      <section class="login-shell" aria-label="登录">
        <aside class="login-side">
          <span class="eyebrow">Secure Campus Access</span>
          <h1>统一身份入口</h1>
          <p class="side-copy">使用本人学号或工号注册并登录。系统会校验学号/工号唯一，确保一人一账号。</p>

          <div class="role-grid" aria-label="角色入口说明">
            <div class="role-item">
              <strong>学生</strong>
              <span>考试大厅、成绩报告、错题复盘</span>
            </div>
            <div class="role-item">
              <strong>教师</strong>
              <span>题库维护、组卷发布、阅卷分析</span>
            </div>
            <div class="role-item">
              <strong>教务</strong>
              <span>用户管理、审计监控、平台配置</span>
            </div>
          </div>

          <div class="security-note">
            <span class="status-dot" />
            <span>{{ registrationText }}</span>
          </div>
        </aside>

        <section class="form-panel">
          <div class="form-heading">
            <span class="form-kicker">Account Login</span>
            <h2>账号登录</h2>
            <p>请输入学号、工号或管理员账号。</p>
          </div>

          <el-form :model="form" label-position="top" @submit.prevent="onSubmit">
            <el-form-item label="账号">
              <el-input
                v-model.trim="form.username"
                autocomplete="username"
                clearable
                placeholder="学号 / 工号 / 管理员账号"
                size="large"
              />
            </el-form-item>

            <el-form-item label="密码">
              <el-input
                v-model="form.password"
                type="password"
                autocomplete="current-password"
                placeholder="请输入密码"
                show-password
                size="large"
              />
            </el-form-item>

            <div class="form-actions">
              <el-button
                v-if="caps.publicRegistrationEnabled"
                link
                type="primary"
                @click="openRegister"
              >
                自主注册
              </el-button>
              <span v-else class="managed-copy">当前未开放自主注册</span>

              <el-button
                v-if="caps.demoPasswordResetEnabled"
                link
                type="primary"
                @click="openForgot"
              >
                忘记密码
              </el-button>
            </div>

            <el-button
              type="primary"
              size="large"
              class="submit-btn"
              :disabled="!canSubmit"
              :loading="loading"
              native-type="submit"
            >
              登录
            </el-button>
          </el-form>

          <div v-if="caps.demoDataEnabled" class="demo-box">
            <span>演示账号</span>
            <button type="button" @click="fillDemo('student', 'student123')">student / student123</button>
            <button type="button" @click="fillDemo('teacher', 'teacher123')">teacher / teacher123</button>
            <button type="button" @click="fillDemo('admin', 'admin123')">admin / admin123</button>
          </div>
        </section>
      </section>
    </main>

    <PublicFooter />

    <el-dialog v-model="dlgReg" title="自主注册" width="500px" destroy-on-close @closed="resetReg">
      <p class="dlg-tip">请使用本人真实学号或工号注册。该编号是唯一身份标识，已注册后不能再次创建账号。</p>
      <el-form label-position="top">
        <el-form-item label="身份">
          <el-radio-group v-model="reg.role">
            <el-radio-button label="STUDENT">学生</el-radio-button>
            <el-radio-button label="TEACHER">教师</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="reg.role === 'STUDENT' ? '学号' : '工号'">
          <el-input
            v-model.trim="reg.username"
            :placeholder="reg.role === 'STUDENT' ? '请输入本人学号' : '请输入本人工号'"
            autocomplete="username"
          />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model.trim="reg.displayName" placeholder="不填则默认使用账号名" />
        </el-form-item>
        <el-form-item v-if="reg.role === 'STUDENT'" label="班级">
          <el-input v-model.trim="reg.className" placeholder="如：计算机2101" />
        </el-form-item>
        <el-form-item label="学院">
          <el-input v-model.trim="reg.college" placeholder="如：计算机学院" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="reg.password" type="password" show-password placeholder="至少 8 位" />
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
      <p class="dlg-tip">该入口仅供演示环境使用；生产环境请登录后修改密码，或联系管理员重置。</p>
      <el-form label-position="top">
        <el-form-item label="账号">
          <el-input v-model.trim="forgot.username" placeholder="学号或工号" autocomplete="username" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="forgot.password" type="password" show-password placeholder="至少 8 位" />
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
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PublicHeader from '../components/public/PublicHeader.vue'
import PublicFooter from '../components/public/PublicFooter.vue'
import { useUserStore } from '../stores/user'
import http from '../api/http'

const router = useRouter()
const store = useUserStore()
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const caps = reactive({
  publicRegistrationEnabled: false,
  demoPasswordResetEnabled: false,
  demoDataEnabled: false
})

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

const canSubmit = computed(() => form.username.trim().length > 0 && form.password.length > 0)
const registrationText = computed(() =>
  caps.publicRegistrationEnabled
    ? '已开放自主注册，学号/工号唯一'
    : '当前未开放自主注册'
)

function openRegister() {
  if (!caps.publicRegistrationEnabled) return
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
  if (!caps.demoPasswordResetEnabled) return
  forgot.username = ''
  forgot.password = ''
  forgot.password2 = ''
  dlgForgot.value = true
}

function fillDemo(username, password) {
  form.username = username
  form.password = password
}

async function loadCapabilities() {
  try {
    const { data } = await http.get('/auth/capabilities')
    caps.publicRegistrationEnabled = data.publicRegistrationEnabled === true
    caps.demoPasswordResetEnabled = data.demoPasswordResetEnabled === true
    caps.demoDataEnabled = data.demoDataEnabled === true
  } catch {
    /* keep secure defaults */
  }
}

async function onSubmit() {
  if (!canSubmit.value) {
    ElMessage.warning('请填写账号和密码')
    return
  }
  loading.value = true
  try {
    const data = await store.login(form.username.trim(), form.password)
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
  if (!caps.publicRegistrationEnabled) {
    ElMessage.warning('当前未开放自主注册')
    return
  }
  if (!reg.username) {
    ElMessage.warning('请填写学号或工号')
    return
  }
  if (reg.password.length < 8) {
    ElMessage.warning('密码至少 8 位')
    return
  }
  if (reg.password !== reg.password2) {
    ElMessage.warning('两次密码不一致')
    return
  }
  regLoading.value = true
  try {
    const data = await store.register({
      username: reg.username,
      password: reg.password,
      role: reg.role,
      displayName: reg.displayName || undefined,
      className: reg.className || undefined,
      college: reg.college || undefined
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
  if (!forgot.username) {
    ElMessage.warning('请填写账号')
    return
  }
  if (forgot.password.length < 8) {
    ElMessage.warning('新密码至少 8 位')
    return
  }
  if (forgot.password !== forgot.password2) {
    ElMessage.warning('两次密码不一致')
    return
  }
  forgotLoading.value = true
  try {
    await store.resetPassword(forgot.username, forgot.password)
    ElMessage.success('密码已重置，请使用新密码登录')
    dlgForgot.value = false
    form.username = forgot.username
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    forgotLoading.value = false
  }
}

onMounted(loadCapabilities)
</script>

<style scoped>
.page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background:
    linear-gradient(180deg, rgba(15, 139, 141, 0.08), transparent 420px),
    var(--ds-bg);
}

.main {
  flex: 1;
  display: grid;
  place-items: center;
  padding: 42px 20px 58px;
}

.login-shell {
  display: grid;
  grid-template-columns: minmax(320px, 0.92fr) minmax(340px, 430px);
  width: min(980px, 100%);
  overflow: hidden;
  border: 1px solid var(--ds-border);
  border-radius: var(--ds-radius);
  background: #fff;
  box-shadow: var(--ds-shadow-lg);
}

.login-side {
  display: flex;
  flex-direction: column;
  min-height: 560px;
  padding: 38px;
  color: #fff;
  background:
    linear-gradient(146deg, rgba(15, 139, 141, 0.96), rgba(27, 36, 48, 0.98)),
    #111719;
}

.eyebrow,
.form-kicker {
  color: rgba(255, 255, 255, 0.72);
  font-size: 12px;
  font-weight: 850;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.login-side h1 {
  margin: 18px 0 12px;
  font-size: 36px;
  font-weight: 900;
  line-height: 1.16;
  letter-spacing: 0;
}

.side-copy {
  max-width: 420px;
  margin: 0 0 30px;
  color: rgba(255, 255, 255, 0.72);
  font-size: 15px;
  line-height: 1.7;
}

.role-grid {
  display: grid;
  gap: 12px;
}

.role-item {
  min-height: 72px;
  padding: 15px;
  border: 1px solid rgba(255, 255, 255, 0.14);
  border-radius: var(--ds-radius);
  background: rgba(255, 255, 255, 0.08);
}

.role-item strong {
  display: block;
  margin-bottom: 6px;
  color: #fff;
  font-size: 15px;
}

.role-item span {
  color: rgba(255, 255, 255, 0.68);
  font-size: 13px;
  line-height: 1.5;
}

.security-note {
  display: inline-flex;
  align-items: center;
  gap: 9px;
  margin-top: auto;
  padding-top: 28px;
  color: rgba(255, 255, 255, 0.76);
  font-size: 13px;
}

.status-dot {
  width: 9px;
  height: 9px;
  border-radius: 50%;
  background: var(--ds-success);
  box-shadow: 0 0 0 5px rgba(47, 184, 110, 0.18);
}

.form-panel {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 36px 34px 30px;
}

.form-heading {
  margin-bottom: 24px;
}

.form-kicker {
  color: var(--ds-primary);
}

.form-heading h2 {
  margin: 8px 0 8px;
  color: var(--ds-text);
  font-size: 26px;
  font-weight: 900;
}

.form-heading p {
  margin: 0;
  color: var(--ds-text-secondary);
  font-size: 14px;
}

.form-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 30px;
  margin: -4px 0 14px;
  gap: 12px;
}

.managed-copy {
  color: var(--ds-text-muted);
  font-size: 13px;
}

.submit-btn {
  width: 100%;
}

.demo-box {
  display: grid;
  gap: 8px;
  margin-top: 18px;
  padding: 12px;
  border: 1px solid var(--ds-border);
  border-radius: var(--ds-radius);
  background: var(--ds-surface-subtle);
}

.demo-box span {
  color: var(--ds-text-secondary);
  font-size: 12px;
  font-weight: 750;
}

.demo-box button {
  display: block;
  width: 100%;
  padding: 7px 8px;
  border: 1px solid transparent;
  border-radius: var(--ds-radius-sm);
  color: var(--ds-text);
  background: #fff;
  text-align: left;
  cursor: pointer;
  transition: var(--ds-transition);
}

.demo-box button:hover {
  border-color: var(--ds-border-strong);
  color: var(--ds-primary);
}

.dlg-tip {
  margin: 0 0 14px;
  color: var(--ds-text-secondary);
  font-size: 13px;
  line-height: 1.6;
}

@media (max-width: 820px) {
  .main {
    align-items: start;
    padding: 20px 14px 36px;
  }

  .login-shell {
    grid-template-columns: 1fr;
  }

  .login-side {
    min-height: auto;
    padding: 28px;
  }

  .security-note {
    margin-top: 24px;
  }

  .form-panel {
    padding: 28px;
  }
}
</style>
