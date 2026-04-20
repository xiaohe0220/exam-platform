<template>
  <div class="wrong-root">
    <header class="wrong-topbar">
      <div>
        <h1 class="wrong-title">错题整理</h1>
        <p class="wrong-sub">客观题答错自动收录；对照参考答案与解析巩固知识点。</p>
      </div>
      <el-input
        v-model="keyword"
        class="search"
        clearable
        placeholder="搜索考试名称或题干"
        :prefix-icon="Search"
      />
    </header>

    <el-skeleton v-if="loading" :rows="6" animated />
    <el-empty v-else-if="!filtered.length" description="暂无错题，继续保持！" />

    <div v-else class="wrong-list">
      <el-card
        v-for="(row, idx) in filtered"
        :key="`${row.questionId}-${row.submittedAt}-${idx}`"
        shadow="never"
        class="q-card"
      >
        <div class="q-head">
          <div class="q-head-left">
            <span class="q-no">{{ idx + 1 }}</span>
            <span class="q-type">{{ typeLabel(row.questionType) }}</span>
            <span class="q-exam">{{ row.examTitle }}</span>
          </div>
          <span class="q-time">{{ formatTime(row.submittedAt) }}</span>
        </div>

        <p class="q-tit">{{ row.questionTitle }}</p>
        <div v-if="row.content" class="q-html" v-html="row.content" />

        <div v-if="showOptions(row.questionType)" class="opts">
          <div
            v-for="(opt, i) in parseOpts(row)"
            :key="i"
            class="opt-line"
            :class="{ wrong: isOptionWrong(row, i), ok: isOptionCorrect(row, i) }"
          >
            <span class="opt-letter">{{ letter(i) }}</span>
            <span class="opt-text">{{ opt }}</span>
            <el-tag v-if="isOptionCorrect(row, i)" size="small" type="success" effect="plain">参考答案</el-tag>
            <el-tag v-else-if="isOptionWrong(row, i)" size="small" type="danger" effect="plain">你的选择</el-tag>
          </div>
        </div>

        <div class="ans-panel">
          <div class="ans-line">
            <span class="ans-label">你的答案</span>
            <span class="ans-val wrong-text">{{ formatYour(row) }}</span>
          </div>
          <div class="ans-line">
            <span class="ans-label">参考答案</span>
            <span class="ans-val ok-text">{{ formatCorrect(row) }}</span>
          </div>
        </div>

        <div v-if="row.chapter || row.knowledgePoint" class="meta-tags">
          <el-tag v-if="row.chapter" size="small" effect="plain">{{ row.chapter }}</el-tag>
          <el-tag v-if="row.knowledgePoint" size="small" effect="plain" type="info">{{ row.knowledgePoint }}</el-tag>
        </div>

        <div v-if="row.answerAnalysis" class="analysis">
          <div class="analysis-cap">
            <span>错题解析</span>
            <el-button link type="primary" class="collapse-link" @click="toggleAnalysis(row)">
              {{ isAnalysisOpen(row) ? '收起' : '展开' }}
            </el-button>
          </div>
          <div v-show="isAnalysisOpen(row)" class="analysis-body" v-html="row.answerAnalysis" />
        </div>
        <div v-else class="no-analysis">
          <span>教师暂未录入本题解析。</span>
          <router-link to="/student/feedback" class="link">去留言反馈</router-link>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { Search } from '@element-plus/icons-vue'
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../../api/http'

const list = ref([])
const loading = ref(false)
const keyword = ref('')
/** 解析区展开状态（按当前筛选列表下标） */
/** 默认展开；键：questionId + submittedAt */
const analysisOpen = reactive({})

function rowKey(row) {
  return `${row.questionId}-${row.submittedAt}`
}
function isAnalysisOpen(row) {
  return analysisOpen[rowKey(row)] !== false
}
function toggleAnalysis(row) {
  const k = rowKey(row)
  analysisOpen[k] = !isAnalysisOpen(row)
}

function letter(i) {
  return String.fromCharCode(65 + i)
}

function parseOpts(row) {
  try {
    return JSON.parse(row.optionsJson || '[]')
  } catch {
    return []
  }
}

function typeLabel(t) {
  const m = {
    SINGLE_CHOICE: '单选',
    MULTIPLE_CHOICE: '多选',
    TRUE_FALSE: '判断',
    FILL_BLANK: '填空',
    SHORT_ANSWER: '简答'
  }
  return m[t] || t
}

function showOptions(type) {
  return type === 'SINGLE_CHOICE' || type === 'MULTIPLE_CHOICE'
}

function parseYourLetters(row) {
  const t = row.questionType
  const raw = row.yourAnswer
  if (raw == null || raw === '') return []
  if (t === 'SINGLE_CHOICE') {
    const s = String(raw).trim()
    return s ? [s] : []
  }
  if (t === 'MULTIPLE_CHOICE') {
    try {
      const a = JSON.parse(raw)
      if (Array.isArray(a)) return [...a].map(String)
    } catch {
      /* ignore */
    }
    return String(raw)
      .replace(/^\[|\]$/g, '')
      .split(',')
      .map((s) => s.trim().replace(/^"|"$/g, ''))
      .filter(Boolean)
  }
  return []
}

function parseCorrectLetters(row) {
  const t = row.questionType
  const raw = row.correctAnswer
  if (raw == null || raw === '') return []
  try {
    if (t === 'MULTIPLE_CHOICE') {
      const a = JSON.parse(raw)
      return Array.isArray(a) ? [...a].map(String).sort() : []
    }
    if (t === 'SINGLE_CHOICE') {
      const x = JSON.parse(raw)
      const s = typeof x === 'string' ? x : String(x)
      return s ? [s.replace(/^"|"$/g, '')] : []
    }
  } catch {
    const s = String(raw).replace(/^"|"$/g, '')
    return t === 'SINGLE_CHOICE' && s ? [s] : []
  }
  return []
}

function isOptionCorrect(row, i) {
  const L = letter(i)
  const set = new Set(parseCorrectLetters(row))
  return set.has(L)
}

function isOptionWrong(row, i) {
  const L = letter(i)
  const yours = new Set(parseYourLetters(row))
  const correct = new Set(parseCorrectLetters(row))
  return yours.has(L) && !correct.has(L)
}

function formatYour(row) {
  const t = row.questionType
  const raw = row.yourAnswer
  if (raw == null || raw === '') return '—'
  if (t === 'TRUE_FALSE') {
    if (raw === 'true' || raw === true) return '正确'
    if (raw === 'false' || raw === false) return '错误'
    return String(raw)
  }
  if (t === 'MULTIPLE_CHOICE') {
    try {
      const a = JSON.parse(raw)
      if (Array.isArray(a)) return [...a].sort().join('、')
    } catch {
      /* fallthrough */
    }
  }
  if (t === 'SINGLE_CHOICE') return String(raw).replace(/^"|"$/g, '')
  try {
    const x = JSON.parse(raw)
    if (typeof x === 'string') return x
    if (Array.isArray(x)) return x.join('、')
  } catch {
    /* ignore */
  }
  return String(raw)
}

function formatCorrect(row) {
  const t = row.questionType
  const raw = row.correctAnswer
  if (raw == null || raw === '') return '—'
  try {
    if (t === 'TRUE_FALSE') {
      const v = JSON.parse(raw)
      if (v === true || v === 'true') return '正确'
      if (v === false || v === 'false') return '错误'
    }
    if (t === 'MULTIPLE_CHOICE') {
      const arr = JSON.parse(raw)
      return Array.isArray(arr) ? [...arr].sort().join('、') : raw
    }
    const x = JSON.parse(raw)
    return typeof x === 'string' ? x.replace(/^"|"$/g, '') : String(x)
  } catch {
    return String(raw).replace(/^"|"$/g, '')
  }
}

function formatTime(t) {
  if (!t) return '—'
  return new Date(t).toLocaleString()
}

const filtered = computed(() => {
  const q = keyword.value.trim().toLowerCase()
  if (!q) return list.value
  return list.value.filter(
    (r) =>
      (r.examTitle && r.examTitle.toLowerCase().includes(q)) ||
      (r.questionTitle && r.questionTitle.toLowerCase().includes(q)) ||
      (r.content && r.content.toLowerCase().includes(q))
  )
})

async function load() {
  loading.value = true
  try {
    const { data } = await http.get('/student/analytics/wrong-questions')
    list.value = data || []
    Object.keys(analysisOpen).forEach((k) => delete analysisOpen[k])
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.wrong-root {
  --wr-bg: #f5f5f5;
  --wr-card: #ffffff;
  --wr-text: #262626;
  --wr-muted: #8c8c8c;
  --wr-brand: #5b8ff9;
  --wr-primary: #e64340;
  max-width: 960px;
  margin: 0 auto;
}

.wrong-topbar {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--lab-border, #f0f0f0);
}
.wrong-title {
  margin: 0 0 6px;
  font-size: 18px;
  font-weight: 700;
  color: var(--wr-text);
}
.wrong-sub {
  margin: 0;
  font-size: 13px;
  color: var(--wr-muted);
  line-height: 1.5;
}
.search {
  width: 280px;
  max-width: 100%;
}

.wrong-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.q-card {
  border-radius: 12px !important;
  border: 1px solid var(--lab-border, #f0f0f0) !important;
  box-shadow: 0 8px 28px rgba(15, 23, 42, 0.07) !important;
  background: var(--wr-card);
}

.q-head {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 10px;
}
.q-head-left {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}
.q-no {
  font-weight: 700;
  color: var(--wr-brand);
  font-size: 16px;
}
.q-type {
  font-size: 12px;
  font-weight: 600;
  color: #fff;
  padding: 2px 10px;
  background: var(--wr-primary);
  border-radius: 4px;
}
.q-exam {
  font-size: 13px;
  color: var(--wr-muted);
}
.q-time {
  font-size: 12px;
  color: var(--wr-muted);
}

.q-tit {
  margin: 0 0 10px;
  font-size: 16px;
  font-weight: 600;
  color: var(--wr-text);
  line-height: 1.5;
}
.q-html {
  margin-bottom: 14px;
  line-height: 1.75;
  color: var(--wr-text);
  font-size: 15px;
}

.opts {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 14px;
}
.opt-line {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  background: #fafafa;
  transition: background 0.15s ease;
}
.opt-line.wrong {
  background: rgba(230, 67, 64, 0.08);
  border-color: rgba(230, 67, 64, 0.25);
}
.opt-line.ok {
  background: rgba(91, 143, 249, 0.1);
  border-color: rgba(91, 143, 249, 0.35);
}
.opt-letter {
  font-weight: 700;
  color: var(--wr-brand);
  min-width: 22px;
}
.opt-text {
  flex: 1;
  min-width: 0;
  font-size: 14px;
  color: var(--wr-text);
}

.ans-panel {
  padding: 12px 14px;
  border-radius: 10px;
  background: #fafafa;
  border: 1px solid var(--lab-border, #f0f0f0);
  margin-bottom: 12px;
}
.ans-line {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: baseline;
  font-size: 14px;
  line-height: 1.6;
}
.ans-line + .ans-line {
  margin-top: 8px;
}
.ans-label {
  flex-shrink: 0;
  width: 72px;
  font-size: 13px;
  color: var(--wr-muted);
}
.ans-val {
  font-weight: 600;
  word-break: break-word;
}
.wrong-text {
  color: var(--wr-primary);
}
.ok-text {
  color: #389e0d;
}

.meta-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 8px;
}

.analysis {
  margin-top: 4px;
  padding-top: 12px;
  border-top: 1px dashed var(--lab-border, #f0f0f0);
}
.analysis-cap {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--wr-text);
}
.collapse-link {
  font-size: 13px;
}
.analysis-body {
  font-size: 14px;
  line-height: 1.75;
  color: var(--wr-text);
  padding: 12px 14px;
  border-radius: 10px;
  background: rgba(91, 143, 249, 0.06);
  border-left: 3px solid var(--wr-brand);
}
.analysis-body :deep(p) {
  margin: 0 0 0.5em;
}
.analysis-body :deep(p:last-child) {
  margin-bottom: 0;
}

.no-analysis {
  margin-top: 8px;
  font-size: 13px;
  color: var(--wr-muted);
}
.no-analysis .link {
  margin-left: 6px;
  color: var(--lab-primary, #e64340);
  text-decoration: none;
}
.no-analysis .link:hover {
  text-decoration: underline;
}

@media (max-width: 640px) {
  .search {
    width: 100%;
  }
}
</style>
