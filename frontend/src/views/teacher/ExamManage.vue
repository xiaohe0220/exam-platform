<template>
  <el-card shadow="never" class="card">
    <template #header>
      <div class="head">
        <span>考试</span>
        <div class="actions">
          <el-button type="primary" plain @click="openFixed">手动组卷</el-button>
          <el-button type="primary" @click="openRandom">智能组卷</el-button>
        </div>
      </div>
    </template>
    <el-table v-loading="loading" :data="rows" border stripe>
      <el-table-column prop="title" label="名称" min-width="200" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column prop="durationMinutes" label="时长(分)" width="100" />
      <el-table-column prop="startAt" label="开始" width="180" />
      <el-table-column prop="endAt" label="结束" width="180" />
      <el-table-column prop="rankingVisible" label="公开排名" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="row.rankingVisible !== false ? 'success' : 'info'">
            {{ row.rankingVisible !== false ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="460" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 'DRAFT'" link type="primary" @click="publish(row)">发布</el-button>
          <el-button link @click="loadStats(row)">统计</el-button>
          <el-button link type="success" @click="viewAttempts(row)">答卷</el-button>
          <el-button link @click="openRanking(row)">排名</el-button>
          <el-button link type="info" @click="openExamMeta(row)">考试设置</el-button>
          <el-button v-if="row.status === 'PUBLISHED'" link type="warning" @click="openExtend(row)">延长</el-button>
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
  </el-card>

  <el-dialog v-model="dlgFixed" title="手动组卷" width="560px">
    <el-form label-width="90px">
      <el-form-item label="标题"><el-input v-model="fix.title" /></el-form-item>
      <el-form-item label="说明"><el-input v-model="fix.description" type="textarea" rows="2" /></el-form-item>
      <el-form-item label="时长(分)"><el-input-number v-model="fix.durationMinutes" :min="10" /></el-form-item>
      <el-form-item label="切屏上限"><el-input-number v-model="fix.switchLimit" :min="1" /></el-form-item>
      <el-form-item label="对象班级">
        <el-input v-model="fix.targetClasses" placeholder="逗号分隔，留空表示不限" />
      </el-form-item>
      <el-form-item label="题目 ID">
        <el-input v-model="fix.ids" type="textarea" rows="3" placeholder="逗号分隔题目数据库 ID，将按顺序组卷" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dlgFixed = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="submitFixed">创建草稿</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="dlgRandom" title="智能随机组卷" width="560px">
    <el-form label-width="110px">
      <el-form-item label="标题"><el-input v-model="rnd.title" /></el-form-item>
      <el-form-item label="每题分值"><el-input-number v-model="rnd.scorePerQuestion" :min="1" /></el-form-item>
      <el-form-item label="时长(分)"><el-input-number v-model="rnd.durationMinutes" :min="10" /></el-form-item>
      <el-form-item label="章节关键字"><el-input v-model="rnd.chapterKeyword" placeholder="可选" /></el-form-item>
      <el-form-item label="单选数量"><el-input-number v-model="rnd.cSingle" :min="0" /></el-form-item>
      <el-form-item label="多选数量"><el-input-number v-model="rnd.cMulti" :min="0" /></el-form-item>
      <el-form-item label="判断数量"><el-input-number v-model="rnd.cTf" :min="0" /></el-form-item>
      <el-form-item label="填空数量"><el-input-number v-model="rnd.cFill" :min="0" /></el-form-item>
      <el-form-item label="简答数量"><el-input-number v-model="rnd.cEssay" :min="0" /></el-form-item>
      <el-form-item label="对象班级"><el-input v-model="rnd.targetClasses" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dlgRandom = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="submitRandom">生成草稿</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="dlgAttempts" title="考试答卷" width="960px">
    <el-table :data="attempts" border size="small">
      <el-table-column prop="id" label="答卷ID" width="90" />
      <el-table-column prop="userId" label="学生" width="90" />
      <el-table-column prop="status" label="状态" width="120" />
      <el-table-column prop="objectiveScore" label="客观" width="90" />
      <el-table-column prop="subjectiveScore" label="主观" width="90" />
      <el-table-column prop="totalScore" label="总分" width="90" />
      <el-table-column prop="switchCount" label="切屏" width="80" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button v-if="row.status === 'IN_PROGRESS'" link type="danger" @click="forceSubmit(row)">强制收卷</el-button>
          <el-button link type="primary" @click="openGrade(row)">批阅</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-dialog>

  <el-dialog v-model="dlgExtend" title="延长考试结束时间" width="400px" @close="extendRow = null">
    <p v-if="extendRow" class="tip">「{{ extendRow.title }}」将在当前结束时间基础上顺延。</p>
    <el-form label-width="120px">
      <el-form-item label="延长（分钟）">
        <el-input-number v-model="extendMinutes" :min="1" :max="1440" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dlgExtend = false">取消</el-button>
      <el-button type="primary" :loading="extending" @click="doExtend">确定</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="dlgRanking" title="排名" width="720px">
    <el-table v-loading="rankingLoading" :data="rankingRows" border size="small" max-height="420">
      <el-table-column prop="rank" label="名次" width="70" />
      <el-table-column prop="displayName" label="姓名" width="120" />
      <el-table-column prop="className" label="班级" width="120" />
      <el-table-column prop="totalScore" label="总分" width="90" />
      <el-table-column prop="submittedAt" label="交卷时间" min-width="160">
        <template #default="{ row }">{{ formatIso(row.submittedAt) }}</template>
      </el-table-column>
    </el-table>
  </el-dialog>

  <el-dialog v-model="dlgMeta" title="考试设置" width="440px" @close="metaRow = null">
    <el-form v-if="metaRow" label-width="120px">
      <el-form-item label="向考生公开排名">
        <el-switch v-model="metaForm.rankingVisible" />
      </el-form-item>
      <el-form-item label="可重考次数">
        <el-input-number v-model="metaForm.maxRetakes" :min="1" :max="20" />
      </el-form-item>
      <el-form-item label="考试时长(分)">
        <el-input-number v-model="metaForm.durationMinutes" :min="5" :max="600" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dlgMeta = false">取消</el-button>
      <el-button type="primary" :loading="metaSaving" @click="saveExamMeta">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="dlgStats" title="成绩统计" width="520px">
    <el-descriptions v-if="stats" :column="1" border size="small">
      <el-descriptions-item label="答卷总数">{{ stats.totalAttempts }}</el-descriptions-item>
      <el-descriptions-item label="已交卷">{{ stats.submittedCount }}</el-descriptions-item>
      <el-descriptions-item label="卷面满分">{{ stats.paperFullScore }}</el-descriptions-item>
      <el-descriptions-item label="平均分">{{ stats.avgScore }}</el-descriptions-item>
      <el-descriptions-item label="最高分">{{ stats.maxScore }}</el-descriptions-item>
      <el-descriptions-item label="最低分">{{ stats.minScore }}</el-descriptions-item>
      <el-descriptions-item label="及格人数（≥{{ stats.passLine }}% 卷面分）">{{ stats.passCount }}</el-descriptions-item>
    </el-descriptions>
  </el-dialog>

  <el-dialog v-model="dlgGrade" title="批阅与答题明细" width="920px" destroy-on-close>
    <template v-if="detail">
      <p class="sub">
        {{ detail.examTitle }} · 学生 {{ detail.studentName }}（{{ detail.className || '—' }}）· 答卷 #{{
          detail.attemptId
        }}
      </p>
      <el-table :data="detail.lines" border size="small" max-height="420">
        <el-table-column prop="title" label="题目" min-width="180" show-overflow-tooltip />
        <el-table-column prop="type" label="题型" width="130" />
        <el-table-column prop="studentAnswer" label="作答" min-width="160" show-overflow-tooltip />
        <el-table-column prop="referenceAnswer" label="参考答案" width="140" show-overflow-tooltip />
        <el-table-column prop="maxScore" label="满分" width="70" />
        <el-table-column v-if="hasSubjective" label="主观给分" width="220">
          <template #default="{ row }">
            <template v-if="row.subjective">
              <el-input-number
                v-model="gradeScores[row.questionId]"
                :min="0"
                :max="Number(row.maxScore) || 100"
                size="small"
              />
              <el-button size="small" type="primary" link @click="saveGrade(row)">保存</el-button>
            </template>
            <span v-else>—</span>
          </template>
        </el-table-column>
      </el-table>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../../api/http'
import { unwrapPage } from '../../api/paging'

const rows = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)
const attempts = ref([])
const dlgAttempts = ref(false)
const dlgFixed = ref(false)
const dlgRandom = ref(false)
const dlgStats = ref(false)
const dlgGrade = ref(false)
const stats = ref(null)
const detail = ref(null)
const gradeScores = reactive({})
const currentExamId = ref(null)
const saving = ref(false)
const dlgExtend = ref(false)
const extendRow = ref(null)
const extendMinutes = ref(30)
const extending = ref(false)
const dlgRanking = ref(false)
const rankingRows = ref([])
const rankingLoading = ref(false)
const dlgMeta = ref(false)
const metaRow = ref(null)
const metaForm = reactive({
  rankingVisible: true,
  maxRetakes: 2,
  durationMinutes: 90
})
const metaSaving = ref(false)

const hasSubjective = computed(() => detail.value?.lines?.some((l) => l.subjective))

const fix = reactive({
  title: '期末考试',
  description: '',
  durationMinutes: 90,
  switchLimit: 3,
  targetClasses: '计算机2101',
  ids: ''
})

const rnd = reactive({
  title: '随机测验',
  scorePerQuestion: 10,
  durationMinutes: 60,
  chapterKeyword: '',
  cSingle: 2,
  cMulti: 1,
  cTf: 1,
  cFill: 1,
  cEssay: 0,
  targetClasses: '计算机2101'
})

async function load() {
  loading.value = true
  try {
    const { data } = await http.get('/teacher/exams', { params: { page: page.value - 1, size: pageSize.value } })
    const { items, total: t } = unwrapPage(data)
    rows.value = items
    total.value = t
  } finally {
    loading.value = false
  }
}

function iso(d) {
  return d ? new Date(d).toISOString() : null
}

function formatIso(t) {
  if (!t) return '—'
  return new Date(t).toLocaleString()
}

async function openRanking(row) {
  dlgRanking.value = true
  rankingLoading.value = true
  rankingRows.value = []
  try {
    const { data } = await http.get(`/teacher/exams/${row.id}/ranking`)
    rankingRows.value = data || []
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    rankingLoading.value = false
  }
}

function openExamMeta(row) {
  metaRow.value = row
  metaForm.rankingVisible = row.rankingVisible !== false
  metaForm.maxRetakes = row.maxRetakes ?? 2
  metaForm.durationMinutes = row.durationMinutes ?? 90
  dlgMeta.value = true
}

async function saveExamMeta() {
  if (!metaRow.value) return
  metaSaving.value = true
  try {
    await http.patch(`/teacher/exams/${metaRow.value.id}/meta`, {
      rankingVisible: metaForm.rankingVisible,
      maxRetakes: metaForm.maxRetakes,
      durationMinutes: metaForm.durationMinutes
    })
    ElMessage.success('已保存')
    dlgMeta.value = false
    load()
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    metaSaving.value = false
  }
}

async function submitFixed() {
  const idList = fix.ids
    .split(/[,，]/)
    .map((s) => s.trim())
    .filter(Boolean)
    .map((s) => Number(s))
  const questions = idList.map((id, i) => ({
    questionId: id,
    orderIndex: i + 1,
    score: 10
  }))
  const now = new Date()
  const end = new Date(now.getTime() + 30 * 24 * 3600 * 1000)
  saving.value = true
  try {
    await http.post('/teacher/exams', {
      title: fix.title,
      description: fix.description,
      startAt: iso(now),
      endAt: iso(end),
      durationMinutes: fix.durationMinutes,
      maxRetakes: 2,
      switchLimit: fix.switchLimit,
      fullscreenRequired: true,
      targetClasses: fix.targetClasses,
      questions
    })
    ElMessage.success('已创建草稿，请点「发布」')
    dlgFixed.value = false
    page.value = 1
    load()
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    saving.value = false
  }
}

async function submitRandom() {
  const now = new Date()
  const end = new Date(now.getTime() + 30 * 24 * 3600 * 1000)
  const countByType = {}
  if (rnd.cSingle) countByType.SINGLE_CHOICE = rnd.cSingle
  if (rnd.cMulti) countByType.MULTIPLE_CHOICE = rnd.cMulti
  if (rnd.cTf) countByType.TRUE_FALSE = rnd.cTf
  if (rnd.cFill) countByType.FILL_BLANK = rnd.cFill
  if (rnd.cEssay) countByType.SHORT_ANSWER = rnd.cEssay
  if (Object.keys(countByType).length === 0) {
    ElMessage.warning('请至少选择一种题型数量')
    return
  }
  saving.value = true
  try {
    await http.post('/teacher/exams/compose-random', {
      title: rnd.title,
      description: '',
      startAt: iso(now),
      endAt: iso(end),
      durationMinutes: rnd.durationMinutes,
      maxRetakes: 2,
      switchLimit: 3,
      fullscreenRequired: true,
      targetClasses: rnd.targetClasses,
      chapterKeyword: rnd.chapterKeyword || null,
      difficulty: null,
      countByType,
      scorePerQuestion: rnd.scorePerQuestion
    })
    ElMessage.success('已生成草稿')
    dlgRandom.value = false
    page.value = 1
    load()
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    saving.value = false
  }
}

async function publish(row) {
  await http.post(`/teacher/exams/${row.id}/publish`)
  ElMessage.success('已发布')
  load()
}

async function viewAttempts(row) {
  currentExamId.value = row.id
  const { data } = await http.get(`/teacher/exams/${row.id}/attempts`)
  attempts.value = data
  dlgAttempts.value = true
}

async function loadStats(row) {
  const { data } = await http.get(`/teacher/exams/${row.id}/stats`)
  stats.value = data
  dlgStats.value = true
}

async function openGrade(attempt) {
  const examId = attempt.examId || currentExamId.value
  if (!examId) {
    ElMessage.warning('无法确定考试')
    return
  }
  currentExamId.value = examId
  const { data } = await http.get(`/teacher/exams/${examId}/attempts/${attempt.id}/detail`)
  detail.value = data
  Object.keys(gradeScores).forEach((k) => delete gradeScores[k])
  dlgGrade.value = true
}

async function saveGrade(row) {
  const examId = currentExamId.value
  const score = gradeScores[row.questionId]
  if (score === undefined || score === null) {
    ElMessage.warning('请输入分数')
    return
  }
  await http.post(`/teacher/exams/${examId}/attempts/${detail.value.attemptId}/grade`, {
    questionId: row.questionId,
    score,
    comment: ''
  })
  ElMessage.success('已保存')
}

function openFixed() {
  dlgFixed.value = true
}

function openRandom() {
  dlgRandom.value = true
}

function openExtend(row) {
  extendRow.value = row
  extendMinutes.value = 30
  dlgExtend.value = true
}

async function doExtend() {
  if (!extendRow.value) return
  extending.value = true
  try {
    await http.post(`/teacher/exams/${extendRow.value.id}/extend-end`, { addMinutes: extendMinutes.value })
    ElMessage.success('已延长结束时间')
    dlgExtend.value = false
    load()
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    extending.value = false
  }
}

async function forceSubmit(attempt) {
  const examId = attempt.examId || currentExamId.value
  if (!examId) {
    ElMessage.warning('无法确定考试')
    return
  }
  try {
    await ElMessageBox.confirm('确定对该生强制收卷并计分？', '强制收卷', { type: 'warning' })
    await http.post(`/teacher/exams/${examId}/attempts/${attempt.id}/force-submit`)
    ElMessage.success('已收卷')
    const { data } = await http.get(`/teacher/exams/${examId}/attempts`)
    attempts.value = data
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message)
  }
}

onMounted(load)
</script>

<style scoped>
.card {
  border-radius: 8px;
}
.head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.actions {
  display: flex;
  gap: 8px;
}
.sub {
  margin: 0 0 12px;
  color: #86909c;
  font-size: 13px;
}
.pager-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
