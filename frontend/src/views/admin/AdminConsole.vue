<template>
  <div class="wrap admin-shell">
    <el-header class="header">
      <router-link to="/admin" class="brand">
        <span class="brand-mark">A</span>
        <span>教务运维</span>
      </router-link>
      <div class="nav">
        <router-link v-for="item in navItems" :key="item.to" class="nav-link" :to="item.to">{{ item.label }}</router-link>
        <el-button link type="primary" @click="logout">退出</el-button>
      </div>
    </el-header>

    <el-main class="admin-main ep-surface-cards">
      <section class="hero">
        <div>
          <span class="eyebrow">Admin Console</span>
          <h1>平台运行总览</h1>
          <p>用户规模、考试资产与答卷记录在这里集中呈现。</p>
        </div>
        <div class="hero-status">
          <span class="status-dot" />
          <strong>服务正常</strong>
          <small>{{ nowText }}</small>
        </div>
      </section>

      <el-row :gutter="16" class="stats">
        <el-col v-for="s in statTiles" :key="s.k" :xs="24" :sm="12" :md="8">
          <div class="metric-card stat-card">
            <span class="metric-card__label">{{ s.label }}</span>
            <strong class="metric-card__value">{{ stats?.[s.k] ?? '—' }}</strong>
            <small>{{ s.hint }}</small>
          </div>
        </el-col>
      </el-row>

      <section class="ops-grid">
        <el-card shadow="never" class="card import-card">
          <template #header>
            <span class="h">用户批量导入</span>
          </template>
          <p class="tip">上传 Excel 创建账号，适合开考前批量导入学生与教师。</p>
          <div class="import-actions">
            <el-button type="primary" @click="downloadTpl">下载模板</el-button>
            <el-upload :show-file-list="false" accept=".xlsx,.xls" :http-request="doUpload">
              <el-button>选择文件并导入</el-button>
            </el-upload>
          </div>
          <el-alert v-if="importMsg" :title="importMsg" type="info" class="mt" show-icon />
        </el-card>

        <el-card shadow="never" class="card checklist-card">
          <template #header>
            <span class="h">上线巡检</span>
          </template>
          <ul class="checklist">
            <li><span />JWT 密钥已配置</li>
            <li><span />公开注册按环境控制</li>
            <li><span />考试超时服务端校验</li>
            <li><span />管理员可重置用户密码</li>
          </ul>
        </el-card>
      </section>
    </el-main>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../../api/http'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const store = useUserStore()
const importMsg = ref('')
const stats = ref(null)
const nowText = computed(() => new Date().toLocaleDateString())

const navItems = [
  { to: '/admin', label: '概览' },
  { to: '/admin/users', label: '用户' },
  { to: '/admin/data', label: '数据' },
  { to: '/admin/platform', label: '平台设置' },
  { to: '/admin/monitor', label: '监控' },
  { to: '/admin/audit', label: '审计' },
  { to: '/admin/feedback', label: '留言' },
  { to: '/admin/profile', label: '资料' },
  { to: '/admin/settings', label: '外观' }
]

const statTiles = [
  { k: 'userTotal', label: '用户总数', hint: '全平台账号' },
  { k: 'studentCount', label: '学生', hint: '可参加考试' },
  { k: 'teacherCount', label: '教师', hint: '命题与阅卷' },
  { k: 'adminCount', label: '管理员', hint: '教务运维' },
  { k: 'examCount', label: '试卷', hint: '草稿与已发布' },
  { k: 'attemptCount', label: '答卷记录', hint: '历史提交' }
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
.admin-shell {
  min-height: 100vh;
  background:
    linear-gradient(180deg, rgba(15, 139, 141, 0.08), transparent 300px),
    var(--ds-bg);
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 64px !important;
  padding: 0 22px;
  background: rgba(255, 255, 255, 0.9);
  border-bottom: 1px solid var(--ds-border);
  backdrop-filter: blur(18px);
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--ds-text);
  font-weight: 900;
  text-decoration: none;
}

.brand-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: var(--ds-radius);
  color: #fff;
  background: var(--ds-text);
  font-weight: 900;
}

.nav {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
  max-width: min(980px, 78vw);
}

.nav-link {
  color: var(--ds-text-secondary);
  font-size: 13px;
  font-weight: 750;
  text-decoration: none;
}

.admin-main {
  width: min(1180px, 100%);
  margin: 0 auto;
  padding: 24px 20px 46px;
}

.hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 18px;
  padding: 22px;
  border: 1px solid var(--ds-border);
  border-radius: var(--ds-radius);
  background: #fff;
  box-shadow: var(--ds-shadow-sm);
}

.eyebrow {
  color: var(--ds-primary);
  font-size: 11px;
  font-weight: 900;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.hero h1 {
  margin: 8px 0 6px;
  color: var(--ds-text);
  font-size: 26px;
  font-weight: 900;
}

.hero p {
  margin: 0;
  color: var(--ds-text-secondary);
  font-size: 14px;
}

.hero-status {
  display: grid;
  justify-items: end;
  gap: 5px;
  min-width: 140px;
}

.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--ds-success);
  box-shadow: 0 0 0 5px var(--ds-success-soft);
}

.hero-status strong {
  color: var(--ds-text);
  font-size: 14px;
}

.hero-status small,
.stat-card small {
  color: var(--ds-text-muted);
  font-size: 12px;
}

.stats {
  margin-bottom: 18px;
}

.stat-card {
  margin-bottom: 14px;
}

.ops-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(280px, 0.9fr);
  gap: 16px;
}

.card {
  min-height: 220px;
}

.h {
  color: var(--ds-text);
  font-weight: 850;
}

.tip {
  margin: 0 0 18px;
  color: var(--ds-text-secondary);
  font-size: 14px;
  line-height: 1.6;
}

.import-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.mt {
  margin-top: 16px;
}

.checklist {
  display: grid;
  gap: 12px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.checklist li {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--ds-text-secondary);
  font-size: 14px;
}

.checklist span {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--ds-success);
  box-shadow: 0 0 0 4px var(--ds-success-soft);
}

@media (max-width: 880px) {
  .hero {
    align-items: flex-start;
    flex-direction: column;
  }

  .hero-status {
    justify-items: start;
  }

  .ops-grid {
    grid-template-columns: 1fr;
  }
}
</style>
