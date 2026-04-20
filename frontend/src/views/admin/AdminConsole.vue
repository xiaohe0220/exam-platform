<template>
  <div class="wrap">
    <el-header class="header">
      <span class="title">教务运维</span>
      <div class="nav">
        <router-link class="nav-link" to="/admin">概览</router-link>
        <router-link class="nav-link" to="/admin/users">用户</router-link>
        <router-link class="nav-link" to="/admin/data">数据</router-link>
        <router-link class="nav-link" to="/admin/platform">平台设置</router-link>
        <router-link class="nav-link" to="/admin/monitor">监控</router-link>
        <router-link class="nav-link" to="/admin/audit">审计</router-link>
        <router-link class="nav-link" to="/admin/feedback">留言</router-link>
        <router-link class="nav-link" to="/admin/profile">资料</router-link>
        <router-link class="nav-link" to="/admin/settings">外观</router-link>
        <el-button link type="primary" @click="logout">退出</el-button>
      </div>
    </el-header>
    <el-main>
      <el-row :gutter="16" class="stats">
        <el-col v-for="s in statTiles" :key="s.k" :xs="24" :sm="12" :md="8">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-label">{{ s.label }}</div>
            <div class="stat-num">{{ stats?.[s.k] ?? '—' }}</div>
          </el-card>
        </el-col>
      </el-row>

      <el-card shadow="never" class="card">
        <template #header><span class="h">用户批量导入（Excel）</span></template>
        <p class="tip">
          第一行为表头：username, password, displayName, role, className, college。角色枚举：ADMIN, COLLEGE_ADMIN,
          TEACHER, STUDENT
        </p>
        <el-space>
          <el-button type="primary" @click="downloadTpl">下载模板</el-button>
          <el-upload :show-file-list="false" accept=".xlsx,.xls" :http-request="doUpload">
            <el-button type="success">选择文件并导入</el-button>
          </el-upload>
        </el-space>
        <el-alert v-if="importMsg" :title="importMsg" type="info" class="mt" show-icon />
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
const importMsg = ref('')
const stats = ref(null)

const statTiles = [
  { k: 'userTotal', label: '用户总数' },
  { k: 'studentCount', label: '学生' },
  { k: 'teacherCount', label: '教师' },
  { k: 'adminCount', label: '管理员' },
  { k: 'examCount', label: '试卷' },
  { k: 'attemptCount', label: '答卷记录' }
]

onMounted(async () => {
  store.loadCache()
  try {
    const { data } = await http.get('/admin/stats/overview')
    stats.value = data
  } catch {
    /* ignore */
  }
})

async function downloadTpl() {
  const res = await http.get('/admin/users/import-template', { responseType: 'blob' })
  const url = window.URL.createObjectURL(new Blob([res.data]))
  const a = document.createElement('a')
  a.href = url
  a.download = 'user_import_template.xlsx'
  a.click()
  window.URL.revokeObjectURL(url)
}

async function doUpload({ file }) {
  const fd = new FormData()
  fd.append('file', file)
  try {
    const { data } = await http.post('/admin/users/import', fd)
    importMsg.value =
      `新建 ${data.created} 条，跳过 ${data.skipped} 条。` +
      (data.messages?.length ? ' ' + data.messages.join('；') : '')
    ElMessage.success('导入完成')
  } catch (e) {
    ElMessage.error(e.message)
  }
}

function logout() {
  store.logout()
  router.push('/login')
}
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
.nav-link:hover {
  color: var(--campus-primary);
}
.stats {
  max-width: 960px;
  margin: 0 auto 20px;
}
.stat-card {
  border-radius: 12px;
  margin-bottom: 12px;
  border: 1px solid var(--campus-accent-soft);
  background: linear-gradient(145deg, #fff 0%, var(--campus-accent-soft) 130%);
}
.stat-label {
  font-size: 13px;
  color: #86909c;
}
.stat-num {
  margin-top: 8px;
  font-size: 22px;
  font-weight: 700;
  color: var(--campus-primary);
}
.card {
  max-width: 720px;
  margin: 0 auto 32px;
  border-radius: 12px;
}
.h {
  font-weight: 600;
}
.tip {
  color: #86909c;
  font-size: 13px;
  line-height: 1.6;
  margin-bottom: 16px;
}
.mt {
  margin-top: 16px;
}
</style>
