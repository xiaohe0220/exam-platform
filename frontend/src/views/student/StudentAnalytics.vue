<template>
  <div class="page">
    <el-row :gutter="16" class="mb">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat">
          <div class="stat-label">平均分</div>
          <div class="stat-value">{{ overview?.avgScore ?? '—' }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat">
          <div class="stat-label">已完成考试</div>
          <div class="stat-value">{{ overview?.examFinishedCount ?? 0 }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat">
          <div class="stat-label">班级排名</div>
          <div class="stat-value rank">
            {{ rankText(overview?.classRank, overview?.classPeerCount) }}
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat">
          <div class="stat-label">全校排名</div>
          <div class="stat-value rank">
            {{ rankText(overview?.schoolRank, overview?.schoolPeerCount) }}
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="card">
      <template #header><span class="h">近期成绩</span></template>
      <el-empty v-if="!history.length" description="暂无考试记录" />
      <el-table v-else :data="history" border stripe size="small">
        <el-table-column prop="examTitle" label="考试" min-width="200" />
        <el-table-column prop="totalScore" label="得分" width="100" />
        <el-table-column prop="submittedAt" label="交卷时间" width="200">
          <template #default="{ row }">{{ formatTime(row.submittedAt) }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../../api/http'

const overview = ref(null)
const history = ref([])

function rankText(rank, peers) {
  if (rank == null || !peers) return '—'
  return `${rank} / ${peers}`
}

function formatTime(t) {
  if (!t) return '—'
  return new Date(t).toLocaleString()
}

async function load() {
  try {
    const [o, h] = await Promise.all([
      http.get('/student/analytics/overview'),
      http.get('/student/analytics/attempt-history', { params: { limit: 15 } })
    ])
    overview.value = o.data
    history.value = h.data
  } catch (e) {
    ElMessage.error(e.message)
  }
}

onMounted(load)
</script>

<style scoped>
.page {
  max-width: 1100px;
  margin: 0 auto;
}
.mb {
  margin-bottom: 16px;
}
.stat {
  border-radius: var(--lab-radius, 8px);
  border: 1px solid var(--lab-border, #f0f0f0);
  background: linear-gradient(145deg, #fff 0%, var(--lab-primary-soft, rgba(230, 67, 64, 0.06)) 130%);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}
.stat-label {
  font-size: 13px;
  color: var(--lab-text-secondary, #8c8c8c);
}
.stat-value {
  margin-top: 8px;
  font-size: 22px;
  font-weight: 700;
  color: var(--lab-primary, #e64340);
}
.stat-value.rank {
  font-size: 18px;
}
.card {
  border-radius: var(--lab-radius, 8px);
  border: 1px solid var(--lab-border, #f0f0f0);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
  background: var(--lab-surface, #fff);
}
.h {
  font-weight: 700;
  font-size: 16px;
  color: var(--lab-text, #262626);
}
</style>
