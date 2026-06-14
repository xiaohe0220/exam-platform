<template>
  <div class="wrap">
    <header class="header">
      <span class="title">用户管理</span>
      <div class="nav">
        <router-link class="nav-link" to="/admin">概览</router-link>
        <router-link class="nav-link" to="/admin/users">用户</router-link>
        <router-link class="nav-link" to="/admin/data">数据</router-link>
        <router-link class="nav-link" to="/admin/platform">平台设置</router-link>
        <router-link class="nav-link" to="/admin/monitor">监控</router-link>
        <router-link class="nav-link" to="/admin/audit">审计</router-link>
        <router-link class="nav-link" to="/admin/feedback">留言</router-link>
        <el-button link type="primary" @click="logout">退出</el-button>
      </div>
    </header>
    <el-main>
      <el-card shadow="never" class="card">
        <template #header>
          <div class="head">
            <span class="h">账号列表</span>
            <div class="head-actions">
              <el-button size="small" type="primary" plain @click="openInviteManager">教师邀请码</el-button>
              <el-button size="small" type="primary" plain @click="exportUsers">导出用户</el-button>
            </div>
          </div>
        </template>
        <p class="tip">可禁用异常账号、调整角色（勿随意改管理员账号）。</p>
        <div class="toolbar">
          <el-input
            v-model.trim="keyword"
            clearable
            placeholder="搜索账号 / 姓名 / 班级"
            style="width: 260px"
          />
          <el-select v-model="roleFilter" clearable placeholder="筛选角色" style="width: 180px">
            <el-option label="学生" value="STUDENT" />
            <el-option label="教师" value="TEACHER" />
            <el-option label="教务" value="COLLEGE_ADMIN" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
          <span class="count">显示 {{ filteredList.length }} / {{ list.length }} 个账号</span>
        </div>
        <el-table v-loading="loading" :data="filteredList" border stripe size="small">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="username" label="账号" width="120" />
          <el-table-column prop="displayName" label="姓名" width="120" />
          <el-table-column prop="role" label="角色" width="140" />
          <el-table-column prop="className" label="班级" width="120" />
          <el-table-column label="邮箱" min-width="160">
            <template #default="{ row }">
              <el-input v-model="row.email" size="small" placeholder="通知用" @blur="saveEmail(row)" />
            </template>
          </el-table-column>
          <el-table-column label="手机号" min-width="140">
            <template #default="{ row }">
              <el-input v-model="row.phone" size="small" placeholder="短信通知" @blur="savePhone(row)" />
            </template>
          </el-table-column>
          <el-table-column label="启用" width="90" align="center">
            <template #default="{ row }">
              <el-switch v-model="row.enabled" @change="patch(row, { enabled: row.enabled })" />
            </template>
          </el-table-column>
          <el-table-column label="角色调整" width="200">
            <template #default="{ row }">
              <el-select
                v-model="row.role"
                size="small"
                style="width: 100%"
                @change="patch(row, { role: row.role })"
              >
                <el-option label="STUDENT" value="STUDENT" />
                <el-option label="TEACHER" value="TEACHER" />
                <el-option label="COLLEGE_ADMIN" value="COLLEGE_ADMIN" />
                <el-option label="ADMIN" value="ADMIN" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="280" align="center" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openResetPassword(row)">重置密码</el-button>
              <el-button v-if="row.role === 'TEACHER'" link type="success" @click="generateInvite(row)">
                生成邀请码
              </el-button>
              <el-button
                link
                type="danger"
                :disabled="row.id === store.profile?.id"
                @click="deleteUser(row)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </el-main>

    <el-dialog v-model="resetDlg" title="重置用户密码" width="420px" destroy-on-close @closed="resetTarget = null">
      <p v-if="resetTarget" class="tip">
        正在为 {{ resetTarget.displayName }}（{{ resetTarget.username }}）设置新密码。
      </p>
      <el-form label-position="top">
        <el-form-item label="新密码">
          <el-input v-model="resetForm.password" type="password" show-password placeholder="至少 8 位" />
        </el-form-item>
        <el-form-item label="确认新密码">
          <el-input v-model="resetForm.password2" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetDlg = false">取消</el-button>
        <el-button type="primary" :loading="resetLoading" @click="submitAdminPassword">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="inviteDlg" title="教师邀请码" width="820px" destroy-on-close @opened="loadInvites">
      <p class="tip">学生自主注册时必须填写教师邀请码。邀请码由教务生成，可按教师追踪使用次数。</p>
      <el-table v-loading="inviteLoading" :data="inviteRows" border stripe size="small" max-height="420">
        <el-table-column prop="code" label="邀请码" width="140" />
        <el-table-column prop="teacherName" label="教师" width="120" />
        <el-table-column prop="college" label="学院" min-width="140" show-overflow-tooltip />
        <el-table-column label="使用次数" width="110">
          <template #default="{ row }">{{ row.usedCount || 0 }} / {{ row.maxUses || '不限' }}</template>
        </el-table-column>
        <el-table-column label="过期时间" min-width="160">
          <template #default="{ row }">{{ formatTime(row.expiresAt) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag size="small" :type="row.enabled !== false ? 'success' : 'info'">
              {{ row.enabled !== false ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="copyInvite(row)">复制</el-button>
            <el-button link type="danger" @click="deleteInvite(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../../api/http'
import { useUserStore } from '../../stores/user'
import { saveBlob } from '../../utils/download'

const router = useRouter()
const store = useUserStore()
const list = ref([])
const loading = ref(false)
const keyword = ref('')
const roleFilter = ref('')
const resetDlg = ref(false)
const resetLoading = ref(false)
const resetTarget = ref(null)
const resetForm = reactive({ password: '', password2: '' })
const inviteDlg = ref(false)
const inviteLoading = ref(false)
const inviteRows = ref([])

const filteredList = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  return list.value.filter((u) => {
    if (roleFilter.value && u.role !== roleFilter.value) return false
    if (!kw) return true
    return [u.username, u.displayName, u.className, u.email, u.phone]
      .filter(Boolean)
      .some((v) => String(v).toLowerCase().includes(kw))
  })
})

async function load() {
  loading.value = true
  try {
    const { data } = await http.get('/admin/users')
    list.value = (data || []).map((u) => ({
      ...u,
      enabled: u.enabled !== false,
      email: u.email || '',
      phone: u.phone || ''
    }))
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}

async function patch(row, body) {
  try {
    const { data } = await http.patch(`/admin/users/${row.id}`, body)
    row.enabled = data.enabled !== false
    row.role = data.role
    row.email = data.email || ''
    row.phone = data.phone || ''
    ElMessage.success('已保存')
  } catch (e) {
    ElMessage.error(e.message)
    await load()
  }
}

function saveEmail(row) {
  patch(row, { email: row.email || null })
}

function savePhone(row) {
  patch(row, { phone: row.phone || null })
}

function formatTime(t) {
  return t ? new Date(t).toLocaleString() : '长期有效'
}

function openInviteManager() {
  inviteDlg.value = true
}

async function loadInvites() {
  inviteLoading.value = true
  try {
    const { data } = await http.get('/admin/teacher-invites')
    inviteRows.value = data || []
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    inviteLoading.value = false
  }
}

async function generateInvite(row) {
  try {
    const { data } = await http.post('/admin/teacher-invites', {
      teacherId: row.id,
      maxUses: 50
    })
    ElMessage.success(`已生成邀请码：${data.code}`)
    inviteDlg.value = true
    await loadInvites()
  } catch (e) {
    ElMessage.error(e.message)
  }
}

async function copyInvite(row) {
  try {
    await navigator.clipboard.writeText(row.code)
    ElMessage.success('已复制邀请码')
  } catch {
    ElMessage.info(`邀请码：${row.code}`)
  }
}

async function deleteInvite(row) {
  try {
    await ElMessageBox.confirm(`确认删除邀请码 ${row.code}？`, '删除邀请码', {
      type: 'warning',
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      confirmButtonClass: 'el-button--danger'
    })
    await http.delete(`/admin/teacher-invites/${row.id}`)
    ElMessage.success('邀请码已删除')
    await loadInvites()
  } catch (e) {
    if (e === 'cancel' || e === 'close') return
    ElMessage.error(e.message)
  }
}

function openResetPassword(row) {
  resetTarget.value = row
  resetForm.password = ''
  resetForm.password2 = ''
  resetDlg.value = true
}

async function submitAdminPassword() {
  if (!resetTarget.value) return
  if (resetForm.password.length < 8) {
    ElMessage.warning('新密码至少 8 位')
    return
  }
  if (resetForm.password !== resetForm.password2) {
    ElMessage.warning('两次密码不一致')
    return
  }
  resetLoading.value = true
  try {
    await http.patch(`/admin/users/${resetTarget.value.id}`, { newPassword: resetForm.password })
    ElMessage.success('密码已重置')
    resetDlg.value = false
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    resetLoading.value = false
  }
}

async function deleteUser(row) {
  if (row.id === store.profile?.id) {
    ElMessage.warning('不能删除当前登录账号')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确认删除账号 ${row.displayName || row.username}（${row.username}）？该操作不可撤销。`,
      '删除账号',
      {
        type: 'warning',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        confirmButtonClass: 'el-button--danger'
      }
    )
    await http.delete(`/admin/users/${row.id}`)
    ElMessage.success('账号已删除')
    await load()
  } catch (e) {
    if (e === 'cancel' || e === 'close') return
    ElMessage.error(e.message)
  }
}

async function exportUsers() {
  try {
    const res = await http.get('/admin/users/export', { responseType: 'blob' })
    saveBlob(res.data, 'users.xlsx')
  } catch (e) {
    ElMessage.error(e.message)
  }
}

function logout() {
  store.logout()
  router.push('/login')
}

onMounted(load)
</script>

<style scoped>
.wrap {
  min-height: 100vh;
  background: #f5f5f5;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 52px !important;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  padding: 0 20px;
}
.title {
  font-weight: 700;
  color: #e64340;
}
.nav {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.nav-link {
  color: #4e5969;
  font-size: 13px;
  text-decoration: none;
}
.nav-link:hover {
  color: #e64340;
}
.card {
  max-width: 1100px;
  margin: 0 auto;
}
.h {
  font-weight: 600;
}
.tip {
  color: #86909c;
  font-size: 13px;
  margin-bottom: 12px;
}
.toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}
.count {
  color: #86909c;
  font-size: 13px;
}
.head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}
.head-actions {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
</style>
