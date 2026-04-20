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
        <template #header><span class="h">账号列表</span></template>
        <p class="tip">可禁用异常账号、调整角色（勿随意改管理员账号）。</p>
        <el-table v-loading="loading" :data="list" border stripe size="small">
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
        </el-table>
      </el-card>
    </el-main>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../../api/http'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const store = useUserStore()
const list = ref([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const { data } = await http.get('/admin/users')
    list.value = (data || []).map((u) => ({
      ...u,
      enabled: u.enabled !== false,
      email: u.email || ''
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
    ElMessage.success('已保存')
  } catch (e) {
    ElMessage.error(e.message)
    await load()
  }
}

function saveEmail(row) {
  patch(row, { email: row.email || null })
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
</style>
