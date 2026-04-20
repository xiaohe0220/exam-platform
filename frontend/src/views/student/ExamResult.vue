<template>
  <div class="page" v-if="result">
    <el-card shadow="never" class="card">
      <template #header>
        <div class="head">
          <span class="h">成绩单</span>
          <el-tag v-if="result.status === 'AUTO_SUBMITTED'" type="warning">自动交卷</el-tag>
        </div>
      </template>
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="总分">{{ result.totalScore ?? '—' }}</el-descriptions-item>
        <el-descriptions-item label="客观分">{{ result.objectiveScore ?? '—' }}</el-descriptions-item>
        <el-descriptions-item label="主观分">{{ result.subjectiveScore ?? '—' }}</el-descriptions-item>
        <el-descriptions-item label="交卷时间">{{ formatTime(result.submittedAt) }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card v-if="ranking.length" shadow="never" class="card mt">
      <template #header><span class="h">本场排名</span></template>
      <el-table :data="ranking" border size="small" max-height="360">
        <el-table-column prop="rank" label="名次" width="70" />
        <el-table-column prop="displayName" label="姓名" width="120" />
        <el-table-column prop="className" label="班级" width="120" />
        <el-table-column prop="totalScore" label="总分" width="100" />
        <el-table-column prop="submittedAt" label="交卷时间" min-width="160">
          <template #default="{ row }">{{ formatTime(row.submittedAt) }}</template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card v-if="reviews.length" shadow="never" class="card mt">
      <template #header><span class="h">客观题作答与参考答案</span></template>
      <el-table :data="reviews" border size="small" stripe>
        <el-table-column prop="title" label="题目" min-width="160" show-overflow-tooltip />
        <el-table-column prop="type" label="题型" width="100" />
        <el-table-column prop="yourAnswer" label="你的答案" width="120" show-overflow-tooltip />
        <el-table-column prop="referenceAnswer" label="参考答案" width="140" show-overflow-tooltip />
        <el-table-column label="结果" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.correct ? 'success' : 'danger'" size="small">{{ row.correct ? '正确' : '错误' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="earnedScore" label="得分" width="80" />
      </el-table>
    </el-card>

    <div class="actions">
      <el-button type="primary" @click="goHome">返回学习中心</el-button>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../../api/http'

const route = useRoute()
const router = useRouter()
const attemptId = route.params.attemptId
const result = ref(null)
const reviews = ref([])
const ranking = ref([])

function formatTime(t) {
  if (!t) return '—'
  return new Date(t).toLocaleString()
}

async function load() {
  try {
    const [{ data: r }, { data: rev }] = await Promise.all([
      http.get(`/student/attempts/${attemptId}/result`),
      http.get(`/student/attempts/${attemptId}/objective-review`).catch(() => ({ data: [] }))
    ])
    result.value = r
    reviews.value = rev || []
    ranking.value = []
    if (r?.examId) {
      try {
        const { data: rk } = await http.get(`/student/exams/${r.examId}/ranking`)
        ranking.value = rk || []
      } catch {
        /* 未公开排名或无权限 */
      }
    }
  } catch (e) {
    ElMessage.error(e.message)
    router.push('/student')
  }
}

function goHome() {
  router.push('/student')
}

onMounted(load)
</script>

<style scoped>
.page {
  max-width: 960px;
  margin: 0 auto;
}
.card {
  border-radius: var(--lab-radius, 8px);
  border: 1px solid var(--lab-border, #f0f0f0);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
  background: var(--lab-surface, #fff);
}
.head {
  display: flex;
  align-items: center;
  gap: 12px;
}
.h {
  font-weight: 700;
  font-size: 16px;
  color: var(--lab-text, #262626);
}
.mt {
  margin-top: 16px;
}
.actions {
  margin-top: 24px;
  text-align: center;
}
</style>
