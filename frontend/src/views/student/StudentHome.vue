<template>
  <div class="student-home">
    <section class="hero">
      <div class="hero-text">
        <h1>你好，{{ store.profile?.displayName || '同学' }}</h1>
        <p class="hero-sub">{{ store.profile?.college || '校内通识' }} · {{ store.profile?.className || '请完善班级信息' }}</p>
      </div>
      <div class="hero-actions">
        <el-button type="primary" round @click="$router.push('/student/analytics')">成绩分析</el-button>
        <el-button round @click="$router.push('/student/wrong')">错题本</el-button>
      </div>
    </section>

    <el-row :gutter="16" class="stat-row">
      <el-col v-for="s in statTiles" :key="s.k" :xs="12" :sm="12" :md="6">
        <div class="stat-card" @click="s.to && $router.push(s.to)">
          <div class="stat-icon" :class="s.tone">{{ s.icon }}</div>
          <div class="stat-body">
            <div class="stat-label">{{ s.label }}</div>
            <div class="stat-value">{{ formatStat(s.k) }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-card shadow="never" class="card main-card">
      <template #header>
        <div class="card-head">
          <span class="h">可参加考试</span>
          <el-tag v-if="total > 0" type="info" effect="plain" round>共 {{ total }} 场</el-tag>
        </div>
      </template>
      <el-empty v-if="!list.length && !loading" description="当前没有可参加的考试" />
      <template v-else>
        <el-table v-loading="loading" :data="list" border stripe class="exam-table">
          <el-table-column prop="title" label="考试名称" min-width="220" show-overflow-tooltip />
          <el-table-column prop="durationMinutes" label="时长" width="100">
            <template #default="{ row }">{{ row.durationMinutes }} 分钟</template>
          </el-table-column>
          <el-table-column label="开放时间" min-width="200">
            <template #default="{ row }">
              <span class="muted">{{ formatRange(row.startAt, row.endAt) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="要求" width="120">
            <template #default="{ row }">
              <el-tag v-if="row.fullscreenRequired" size="small" type="warning" effect="plain">全屏</el-tag>
              <span v-else class="muted">—</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="enter(row)">进入考试</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div v-if="total > pageSize" class="pager-wrap">
          <el-pagination
            v-model:current-page="page"
            :page-size="pageSize"
            :total="total"
            layout="total, prev, pager, next"
            background
            @current-change="load"
          />
        </div>
      </template>
    </el-card>

    <el-row :gutter="16" class="quick-row">
      <el-col :xs="24" :md="12">
        <el-card shadow="hover" class="hint-card" @click="$router.push('/student/analytics')">
          <div class="hint-title">📊 成绩与排名</div>
          <p>查看平均分、班级/校级排名与近期成绩曲线。</p>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card shadow="hover" class="hint-card" @click="$router.push('/student/settings')">
          <div class="hint-title">⚙️ 个性化</div>
          <p>主题、字体与作答习惯，让长时间考试更舒适。</p>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../../api/http'
import { unwrapPage } from '../../api/paging'
import { useUserStore } from '../../stores/user'

const store = useUserStore()
const list = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const overview = ref(null)
const router = useRouter()

const statTiles = [
  { k: 'avgScore', label: '平均分', icon: '∑', tone: 't1', to: '/student/analytics' },
  { k: 'examFinished', label: '已完成', icon: '✓', tone: 't2', to: '/student/analytics' },
  { k: 'classRank', label: '班级排名', icon: '▤', tone: 't3', to: '/student/analytics' },
  { k: 'schoolRank', label: '校级排名', icon: '◎', tone: 't4', to: '/student/analytics' }
]

function formatStat(k) {
  const o = overview.value
  if (!o) return '—'
  if (k === 'avgScore') return o.avgScore ?? '—'
  if (k === 'examFinished') return o.examFinishedCount ?? 0
  if (k === 'classRank') return rankText(o.classRank, o.classPeerCount)
  if (k === 'schoolRank') return rankText(o.schoolRank, o.schoolPeerCount)
  return '—'
}

function rankText(rank, peers) {
  if (rank == null || !peers) return '—'
  return `${rank} / ${peers}`
}

function formatRange(start, end) {
  if (!start && !end) return '不限'
  const a = start ? new Date(start).toLocaleString() : '—'
  const b = end ? new Date(end).toLocaleString() : '—'
  return `${a} ~ ${b}`
}

async function load() {
  loading.value = true
  try {
    const [{ data: pageData }, { data: ov }] = await Promise.all([
      http.get('/student/exams/available', { params: { page: page.value - 1, size: pageSize.value } }),
      http.get('/student/analytics/overview').catch(() => ({ data: null }))
    ])
    overview.value = ov
    const { items, total: t } = unwrapPage(pageData)
    list.value = items
    total.value = t
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}

async function enter(row) {
  try {
    const { data } = await http.post(`/student/exams/${row.id}/start`)
    router.push(`/student/exam/${data.id}`)
  } catch (e) {
    ElMessage.error(e.message)
  }
}

onMounted(load)
</script>

<style scoped>
.student-home {
  max-width: 1100px;
  margin: 0 auto;
}
.hero {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 20px 22px;
  margin-bottom: 16px;
  border-radius: var(--lab-radius, 8px);
  background: linear-gradient(
    120deg,
    var(--lab-primary-soft, rgba(230, 67, 64, 0.08)) 0%,
    #fff 48%,
    var(--lab-blue-soft, rgba(91, 143, 249, 0.15)) 100%
  );
  border: 1px solid var(--lab-border, #f0f0f0);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}
.hero h1 {
  margin: 0 0 6px;
  font-size: 22px;
  font-weight: 700;
  color: var(--lab-text, #262626);
}
.hero-sub {
  margin: 0;
  color: var(--lab-text-secondary, #8c8c8c);
  font-size: 14px;
}
.hero-actions {
  display: flex;
  gap: 10px;
}
.stat-row {
  margin-bottom: 16px;
}
.stat-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-radius: var(--lab-radius, 8px);
  background: var(--lab-surface, #fff);
  border: 1px solid var(--lab-border, #f0f0f0);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
  margin-bottom: 12px;
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}
.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.08);
}
.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 700;
  color: #fff;
}
.stat-icon.t1 {
  background: linear-gradient(135deg, #e64340, #ff7875);
}
.stat-icon.t2 {
  background: linear-gradient(135deg, #50c878, #7fe0a0);
}
.stat-icon.t3 {
  background: linear-gradient(135deg, #f5a623, #ffc56d);
}
.stat-icon.t4 {
  background: linear-gradient(135deg, #5b8ff9, #85a5ff);
}
.stat-label {
  font-size: 12px;
  color: var(--lab-text-secondary, #8c8c8c);
}
.stat-value {
  margin-top: 4px;
  font-size: 18px;
  font-weight: 700;
  color: var(--lab-text, #262626);
}
.card {
  border-radius: var(--lab-radius, 8px);
  border: 1px solid var(--lab-border, #f0f0f0);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}
.main-card {
  background: linear-gradient(180deg, #fff 0%, var(--lab-primary-soft, rgba(230, 67, 64, 0.08)) 140%);
}
.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.h {
  font-weight: 700;
  font-size: 16px;
  color: var(--lab-text, #262626);
}
.muted {
  color: var(--lab-text-secondary, #8c8c8c);
  font-size: 13px;
}
.pager-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
.quick-row {
  margin-top: 4px;
}
.hint-card {
  border-radius: var(--lab-radius, 8px);
  border: 1px solid var(--lab-border, #f0f0f0);
  cursor: pointer;
  margin-bottom: 16px;
  transition: box-shadow 0.15s ease;
}
.hint-card:hover {
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.08);
}
.hint-title {
  font-weight: 600;
  margin-bottom: 8px;
  color: var(--lab-text, #262626);
}
.hint-card p {
  margin: 0;
  color: var(--lab-text-secondary, #8c8c8c);
  font-size: 14px;
  line-height: 1.6;
}
.exam-table {
  border-radius: 8px;
  overflow: hidden;
}
</style>
