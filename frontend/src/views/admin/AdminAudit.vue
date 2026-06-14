<template>
  <div class="wrap">
    <el-header class="header">
      <span class="title">操作审计</span>
      <div class="nav">
        <router-link class="nav-link" to="/admin">概览</router-link>
        <router-link class="nav-link" to="/admin/monitor">监控</router-link>
        <router-link class="nav-link active" to="/admin/audit">审计</router-link>
        <router-link class="nav-link" to="/admin/feedback">留言</router-link>
        <router-link class="nav-link" to="/admin/profile">资料</router-link>
        <el-button link type="primary" @click="logout">退出</el-button>
      </div>
    </el-header>
    <el-main>
      <el-card shadow="never" class="card">
        <template #header>
          <div class="head">
            <span>最近操作记录</span>
            <div class="tools">
              <el-input-number v-model="limit" :min="20" :max="500" :step="20" size="small" @change="load" />
              <el-button size="small" type="primary" plain @click="exportAudit">导出 Excel</el-button>
            </div>
          </div>
        </template>
        <el-table v-loading="loading" :data="rows" border stripe size="small" max-height="560">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="userId" label="用户ID" width="90" />
          <el-table-column prop="action" label="动作" width="180" />
          <el-table-column prop="detail" label="详情" min-width="240" show-overflow-tooltip />
          <el-table-column prop="createdAt" label="时间" width="180">
            <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
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
import { saveBlob } from '../../utils/download'

const router = useRouter()
const store = useUserStore()
const rows = ref([])
const limit = ref(100)
const loading = ref(false)

function formatTime(t) {
  if (!t) return '—'
  return new Date(t).toLocaleString()
}

async function load() {
  loading.value = true
  try {
    const { data } = await http.get('/admin/audit-logs', { params: { limit: limit.value } })
    rows.value = data
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}

async function exportAudit() {
  try {
    const res = await http.get('/admin/audit-logs/export', { responseType: 'blob' })
    saveBlob(res.data, 'audit_logs.xlsx')
  } catch (e) {
    ElMessage.error(e.message)
  }
}

function logout() {
  store.logout()
  router.push('/login')
}

onMounted(() => {
  store.loadCache()
  load()
})
</script>

<style scoped>
.wrap {
  min-height: 100vh;
  background: linear-gradient(180deg, var(--campus-bg) 0%, #fff 55%);
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 52px !important;
  background: #fff;
  border-bottom: 1px solid var(--campus-accent-soft);
  padding: 0 20px;
}
.title {
  font-weight: 700;
  color: var(--campus-primary);
}
.nav {
  display: flex;
  align-items: center;
  gap: 16px;
}
.nav-link {
  color: #4e5969;
  font-size: 14px;
  text-decoration: none;
}
.nav-link:hover,
.nav-link.active {
  color: var(--campus-primary);
}
.card {
  max-width: 1100px;
  margin: 24px auto;
  border-radius: 12px;
}
.head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.tools {
  display: flex;
  gap: 10px;
  align-items: center;
}
</style>
