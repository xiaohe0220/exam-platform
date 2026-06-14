<template>
  <div
    v-if="paper"
    class="exam-root exam-protect"
    @copy.prevent="recordSecurity('COPY_BLOCKED')"
    @paste.prevent="recordSecurity('PASTE_BLOCKED')"
    @cut.prevent="recordSecurity('CUT_BLOCKED')"
    @contextmenu.prevent="recordSecurity('CONTEXT_MENU_BLOCKED')"
  >
    <header class="exam-topbar">
      <div class="exam-topbar-main">
        <h1 class="exam-title">{{ paper.exam.title }}</h1>
        <p class="exam-meta">
          时长 {{ paper.exam.durationMinutes }} 分钟 · 切屏 {{ paper.switchCount }} /
          {{ paper.exam.switchLimit }}
        </p>
      </div>
      <div class="exam-topbar-actions">
        <div v-if="savedFlash" class="save-toast" aria-live="polite">
          <span class="save-ico">✓</span>
          已保存
        </div>
        <div
          class="exam-timer"
          :class="{ critical: remainSec <= 600, warn: remainSec > 600 && remainSec <= 900 }"
        >
          <span class="exam-timer-label">剩余时间</span>
          <span class="exam-timer-digits">{{ formatTime(remainSec) }}</span>
        </div>
        <el-button type="primary" class="submit-btn" size="large" @click="confirmSubmit">交卷</el-button>
      </div>
    </header>

    <div class="exam-body">
      <aside class="exam-aside">
        <div class="aside-cap">题目导航</div>
        <div class="nav-pills">
          <button
            v-for="(q, idx) in paper.questions"
            :key="q.id"
            type="button"
            class="pill"
            :class="{
              done: isDone(q),
              current: idx === activeIdx,
              mark: isMarked(q.id)
            }"
            @click="goQuestion(idx)"
          >
            {{ idx + 1 }}
          </button>
        </div>
        <p class="kbd-hint">单选题可按键盘 1–4 选择当前屏选项</p>
      </aside>

      <main class="exam-main">
        <el-card
          v-for="(q, idx) in paper.questions"
          :key="q.id"
          :id="'q-' + idx"
          class="q-card"
          shadow="never"
        >
          <div class="q-head">
            <div class="q-head-left">
              <span class="q-no">{{ idx + 1 }}</span>
              <span class="q-type">{{ typeLabel(q.type) }}</span>
              <span class="q-tit">{{ q.title }}</span>
            </div>
            <div class="q-head-actions">
              <el-button size="small" plain class="err-btn" @click="openFeedback(idx)">题目纠错</el-button>
              <el-button
                size="small"
                :type="isMarked(q.id) ? 'warning' : 'default'"
                plain
                @click="toggleMark(q.id)"
              >
                {{ isMarked(q.id) ? '已标记待查' : '标记待检查' }}
              </el-button>
            </div>
          </div>
          <div class="q-html" v-html="q.content" />

          <div v-if="q.type === 'SINGLE_CHOICE'" class="opts">
            <el-radio-group v-model="answers[q.id]" class="opt-group" @change="onAnswerChange">
              <el-radio v-for="(opt, i) in parseOpts(q)" :key="i" :label="letter(i)" class="opt-line">
                {{ letter(i) }}. {{ opt }}
              </el-radio>
            </el-radio-group>
          </div>

          <div v-else-if="q.type === 'MULTIPLE_CHOICE'" class="opts">
            <el-checkbox-group v-model="answersMulti[q.id]" class="opt-group" @change="onAnswerChange">
              <el-checkbox v-for="(opt, i) in parseOpts(q)" :key="i" :label="letter(i)" class="opt-line">
                {{ letter(i) }}. {{ opt }}
              </el-checkbox>
            </el-checkbox-group>
          </div>

          <div v-else-if="q.type === 'TRUE_FALSE'" class="opts">
            <el-radio-group v-model="answers[q.id]" class="opt-group" @change="onAnswerChange">
              <el-radio label="true" class="opt-line">正确</el-radio>
              <el-radio label="false" class="opt-line">错误</el-radio>
            </el-radio-group>
          </div>

          <div v-else-if="q.type === 'FILL_BLANK'" class="opts">
            <el-input v-model="answers[q.id]" placeholder="请输入答案" size="large" class="input-wide" />
          </div>

          <div v-else class="opts">
            <el-input
              v-model="answers[q.id]"
              type="textarea"
              :rows="6"
              placeholder="简答"
              class="input-wide"
            />
          </div>
        </el-card>

        <div class="exam-footer">
          <el-button type="primary" size="large" class="submit-btn-lg" @click="confirmSubmit">交卷</el-button>
        </div>
      </main>
    </div>

    <el-dialog v-model="fbDlg" title="题目纠错 / 留言" width="520px" destroy-on-close @closed="fbContent = ''">
      <p class="fb-hint">将提交至教师与教务，请尽量写清题号与错误说明（最多 300 字）。</p>
      <el-input v-model="fbSubject" maxlength="40" show-word-limit placeholder="主题" class="fb-field" />
      <el-input
        v-model="fbContent"
        type="textarea"
        :rows="6"
        maxlength="300"
        show-word-limit
        placeholder="描述题目或解析问题"
        class="fb-field"
      />
      <template #footer>
        <el-button @click="fbDlg = false">取消</el-button>
        <el-button type="primary" :loading="fbSaving" @click="submitFeedback">提交</el-button>
      </template>
    </el-dialog>
  </div>
  <div v-else class="loading">加载中...</div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../../api/http'
import { useUserStore } from '../../stores/user'

const route = useRoute()
const store = useUserStore()
const router = useRouter()
const attemptId = computed(() => route.params.attemptId)
const paper = ref(null)
const answers = reactive({})
const answersMulti = reactive({})
const remainSec = ref(0)
const activeIdx = ref(0)
const savedFlash = ref(false)
/** 题号导航「待检查」与后端 flagsJson 同步 */
const markedIds = ref([])
let timer = null
let saveTimer = null
let submitOnce = false
let io = null
const lastSecurityAt = {}

const fbDlg = ref(false)
const fbSubject = ref('')
const fbContent = ref('')
const fbSaving = ref(false)

function openFeedback(idx) {
  const q = paper.value?.questions[idx]
  if (!q || !paper.value) return
  fbSubject.value = `第${idx + 1}题 · 题目/解析纠错`.slice(0, 40)
  const draft = `【试卷】${paper.value.exam.title}\n【题号】${idx + 1}\n【题干】${q.title}\n\n请描述问题：\n`
  fbContent.value = draft.length > 300 ? draft.slice(0, 297) + '…' : draft
  fbDlg.value = true
}

async function submitFeedback() {
  if (!paper.value) return
  const sub = fbSubject.value?.trim()
  const body = fbContent.value?.trim()
  if (!sub || !body) {
    ElMessage.warning('请填写主题与内容')
    return
  }
  fbSaving.value = true
  try {
    await http.post('/student/messages', {
      subject: sub.slice(0, 40),
      course: (paper.value.exam.title || '考试').slice(0, 100),
      className: (store.profile?.className || '其他').slice(0, 100),
      content: body.slice(0, 300)
    })
    ElMessage.success('已提交')
    fbDlg.value = false
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    fbSaving.value = false
  }
}

function letter(i) {
  return String.fromCharCode(65 + i)
}

function parseOpts(q) {
  try {
    return JSON.parse(q.optionsJson || '[]')
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

function isDone(q) {
  if (q.type === 'MULTIPLE_CHOICE') {
    return (answersMulti[q.id] || []).length > 0
  }
  return answers[q.id] !== undefined && answers[q.id] !== ''
}

function isMarked(qid) {
  return markedIds.value.includes(qid)
}

function toggleMark(qid) {
  const i = markedIds.value.indexOf(qid)
  if (i >= 0) markedIds.value.splice(i, 1)
  else markedIds.value.push(qid)
  saveRemote(true)
}

function goQuestion(idx) {
  activeIdx.value = idx
  document.getElementById('q-' + idx)?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

function formatTime(s) {
  if (s <= 0) return '00:00'
  const m = Math.floor(s / 60)
  const sec = s % 60
  return `${String(m).padStart(2, '0')}:${String(sec).padStart(2, '0')}`
}

function buildPayload() {
  const o = {}
  for (const q of paper.value.questions) {
    if (q.type === 'MULTIPLE_CHOICE') {
      const arr = answersMulti[q.id] || []
      o[String(q.id)] = [...arr].sort()
    } else {
      o[String(q.id)] = answers[q.id] ?? ''
    }
  }
  return o
}

async function saveRemote(silent = false) {
  if (!paper.value) return
  try {
    const body = { ...buildPayload(), markedQuestionIds: [...markedIds.value] }
    await http.put(`/student/attempts/${attemptId.value}/answers`, body)
    if (!silent) {
      savedFlash.value = true
      setTimeout(() => {
        savedFlash.value = false
      }, 1600)
    }
  } catch {
    /* 静默 */
  }
}

function onAnswerChange() {
  saveRemote()
}

function onKeyDown(e) {
  const t = e.target
  if (t && (t.tagName === 'INPUT' || t.tagName === 'TEXTAREA')) return
  const q = paper.value?.questions[activeIdx.value]
  if (!q || q.type !== 'SINGLE_CHOICE') return
  const k = e.key
  if (k < '1' || k > '4') return
  const i = parseInt(k, 10) - 1
  const opts = parseOpts(q)
  if (i < opts.length) {
    answers[q.id] = letter(i)
    saveRemote()
  }
}

function setupObserver() {
  io?.disconnect()
  io = new IntersectionObserver(
    (entries) => {
      entries.forEach((en) => {
        if (en.isIntersecting && en.intersectionRatio >= 0.35) {
          const id = en.target.id
          const idx = parseInt(id.replace('q-', ''), 10)
          if (!Number.isNaN(idx)) activeIdx.value = idx
        }
      })
    },
    { root: null, threshold: [0.35, 0.5] }
  )
  paper.value?.questions.forEach((_, idx) => {
    const el = document.getElementById('q-' + idx)
    if (el) io.observe(el)
  })
}

function tryFullscreen() {
  const el = document.documentElement
  if (paper.value?.exam?.fullscreenRequired && el.requestFullscreen) {
    el.requestFullscreen().catch(() => {})
  }
}

function exitFullscreenSafe() {
  if (document.fullscreenElement && document.exitFullscreen) {
    document.exitFullscreen().catch(() => {})
  }
}

async function recordSecurity(eventType, detail = '') {
  if (!paper.value || submitOnce) return
  const now = Date.now()
  if (lastSecurityAt[eventType] && now - lastSecurityAt[eventType] < 3000) return
  lastSecurityAt[eventType] = now
  try {
    await http.post(`/student/attempts/${attemptId.value}/security-events`, {
      eventType,
      detail
    })
  } catch {
    /* 安全事件不打断作答 */
  }
}

async function load() {
  const { data } = await http.get(`/student/attempts/${attemptId.value}/paper`)
  paper.value = data
  markedIds.value = [...(data.markedQuestionIds || [])]
  for (const q of data.questions) {
    if (q.type === 'MULTIPLE_CHOICE') {
      answersMulti[q.id] = []
    }
  }
  tryFullscreen()
  const start = new Date(data.startedAt).getTime()
  const dur = (data.exam.durationMinutes || 90) * 60
  const endAt = Math.floor(start / 1000) + dur
  const tick = () => {
    const now = Math.floor(Date.now() / 1000)
    remainSec.value = Math.max(0, endAt - now)
    if (remainSec.value <= 0) {
      submitExam(true)
    }
  }
  tick()
  timer = setInterval(tick, 1000)
  saveTimer = setInterval(() => saveRemote(true), 15000)
  await nextTick()
  setupObserver()
}

async function submitExam(auto) {
  if (submitOnce) return
  submitOnce = true
  clearInterval(timer)
  clearInterval(saveTimer)
  await saveRemote(true)
  try {
    await http.post(`/student/attempts/${attemptId.value}/submit`)
    exitFullscreenSafe()
    if (!auto) ElMessage.success('交卷成功')
    router.push(`/student/result/${attemptId.value}`)
  } catch (e) {
    submitOnce = false
    ElMessage.error(e.message)
  }
}

async function confirmSubmit() {
  try {
    await ElMessageBox.confirm('确认交卷？提交后不可再修改。', '交卷确认', {
      type: 'warning',
      confirmButtonText: '确认交卷',
      cancelButtonText: '继续作答'
    })
    await submitExam(false)
  } catch {
    /* cancel */
  }
}

async function onVisibility() {
  if (document.visibilityState === 'hidden' && paper.value) {
    recordSecurity('VISIBILITY_HIDDEN', '页面进入后台或切换标签')
    try {
      const { data } = await http.post(`/student/attempts/${attemptId.value}/switch`)
      paper.value = { ...paper.value, switchCount: data.switchCount }
    } catch {
      /* ignore */
    }
  }
}

function onFullscreenChange() {
  if (paper.value?.exam?.fullscreenRequired && !document.fullscreenElement && !submitOnce) {
    recordSecurity('FULLSCREEN_EXIT', '考试中退出全屏')
  }
}

onMounted(async () => {
  window.addEventListener('keydown', onKeyDown)
  try {
    await load()
    document.addEventListener('visibilitychange', onVisibility)
    document.addEventListener('fullscreenchange', onFullscreenChange)
  } catch (e) {
    ElMessage.error(e.message)
    router.push('/student')
  }
})

onUnmounted(() => {
  window.removeEventListener('keydown', onKeyDown)
  clearInterval(timer)
  clearInterval(saveTimer)
  io?.disconnect()
  document.removeEventListener('visibilitychange', onVisibility)
  document.removeEventListener('fullscreenchange', onFullscreenChange)
  exitFullscreenSafe()
})
</script>

<style scoped>
.exam-root {
  --exam-bg: #f5f5f5;
  --exam-card: #ffffff;
  --exam-text: #262626;
  --exam-muted: #8c8c8c;
  --exam-brand: #5b8ff9;
  --exam-primary: #e64340;
  --exam-danger: #e64340;
  min-height: 100%;
  margin: 0 auto;
  background: var(--exam-bg);
  font-family: 'Inter', 'Source Han Sans SC', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

.exam-topbar {
  position: sticky;
  top: 0;
  z-index: 50;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 20px 16px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(15, 23, 42, 0.06);
  box-shadow: 0 4px 20px rgba(15, 23, 42, 0.06);
}

.exam-title {
  margin: 0 0 6px;
  font-size: 18px;
  font-weight: 700;
  color: var(--exam-text);
  letter-spacing: 0.02em;
}

.exam-meta {
  margin: 0;
  font-size: 13px;
  color: var(--exam-muted);
}

.exam-topbar-actions {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-shrink: 0;
}

.save-toast {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #50c878;
  animation: saveIn 0.35s ease;
}
.save-ico {
  display: inline-flex;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: rgba(80, 200, 120, 0.2);
  align-items: center;
  justify-content: center;
  font-size: 12px;
  animation: pop 0.45s ease;
}
@keyframes saveIn {
  from {
    opacity: 0;
    transform: translateY(-4px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
@keyframes pop {
  0% {
    transform: scale(0.6);
  }
  70% {
    transform: scale(1.08);
  }
  100% {
    transform: scale(1);
  }
}

.exam-timer {
  text-align: right;
}
.exam-timer-label {
  display: block;
  font-size: 11px;
  color: var(--exam-muted);
  text-transform: uppercase;
  letter-spacing: 0.06em;
}
.exam-timer-digits {
  font-size: 26px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  color: var(--exam-brand);
  line-height: 1.2;
}
.exam-timer.warn .exam-timer-digits {
  color: #f5a623;
}
.exam-timer.critical .exam-timer-digits {
  color: var(--exam-danger);
  animation: pulse 1.1s ease-in-out infinite;
}
@keyframes pulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.55;
  }
}

.submit-btn {
  border-radius: var(--lab-radius, 8px);
  font-weight: 600;
}

.exam-body {
  display: flex;
  gap: 20px;
  padding: 16px 16px 48px;
  max-width: 1280px;
  margin: 0 auto;
}

.exam-aside {
  width: 220px;
  flex-shrink: 0;
  position: relative;
  top: 0;
  align-self: flex-start;
  padding: 14px;
  background: var(--exam-card);
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
}
.aside-cap {
  font-size: 12px;
  font-weight: 600;
  color: var(--exam-muted);
  margin-bottom: 10px;
}
.nav-pills {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.pill {
  width: 40px;
  height: 40px;
  border: 1px solid rgba(15, 23, 42, 0.1);
  border-radius: 10px;
  background: #fff;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  color: var(--exam-text);
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}
.pill:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.08);
}
.pill.done {
  background: var(--lab-blue-soft, rgba(91, 143, 249, 0.15));
  border-color: rgba(91, 143, 249, 0.45);
  color: var(--exam-brand);
}
.pill.current {
  background: var(--exam-brand);
  border-color: var(--exam-brand);
  color: #fff;
  box-shadow: 0 4px 12px rgba(91, 143, 249, 0.45);
}
.pill.mark:not(.current) {
  border-color: rgba(245, 166, 35, 0.85);
  box-shadow: 0 0 0 1px rgba(245, 166, 35, 0.35);
}
.kbd-hint {
  margin: 14px 0 0;
  font-size: 11px;
  line-height: 1.5;
  color: #9ca3af;
}

.exam-main {
  flex: 1;
  min-width: 0;
}

.q-card {
  margin-bottom: 18px;
  border-radius: 12px !important;
  border: none !important;
  box-shadow: 0 8px 28px rgba(15, 23, 42, 0.07) !important;
}
.q-head {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
  line-height: 1.5;
}
.q-head-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}
.err-btn {
  color: var(--exam-primary, #e64340) !important;
  border-color: rgba(230, 67, 64, 0.45) !important;
}
.err-btn:hover {
  background: rgba(230, 67, 64, 0.06) !important;
}
.q-head-left {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: 8px;
  flex: 1;
  min-width: 0;
}
.q-no {
  font-weight: 700;
  color: var(--exam-brand);
  font-size: 16px;
}
.q-type {
  font-size: 12px;
  font-weight: 600;
  color: #fff;
  padding: 2px 10px;
  background: var(--exam-primary, #e64340);
  border-radius: 4px;
}
.q-tit {
  font-weight: 600;
  font-size: 16px;
  color: var(--exam-text);
  flex: 1 1 100%;
}
.q-html {
  margin-bottom: 16px;
  line-height: 1.75;
  color: var(--exam-text);
  font-size: 15px;
}
.opts {
  margin-top: 8px;
}
.opt-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: flex-start;
}
.opt-line {
  width: 100%;
  margin: 0 !important;
  padding: 10px 12px;
  border-radius: 10px;
  transition: background 0.15s ease;
}
.opt-line:hover {
  background: var(--lab-blue-soft, rgba(91, 143, 249, 0.08));
}
.input-wide {
  max-width: 100%;
}

.exam-footer {
  text-align: center;
  padding: 8px 0 32px;
}
.submit-btn-lg {
  min-width: 200px;
  border-radius: var(--lab-radius, 8px);
  font-weight: 600;
}

.fb-hint {
  margin: 0 0 12px;
  font-size: 13px;
  color: var(--exam-muted);
  line-height: 1.5;
}
.fb-field {
  margin-bottom: 12px;
  width: 100%;
}

.loading {
  text-align: center;
  padding: 80px 20px;
  color: #9ca3af;
}

@media (max-width: 960px) {
  .exam-body {
    flex-direction: column;
  }
  .exam-aside {
    position: relative;
    top: 0;
    width: 100%;
  }
  .exam-topbar {
    flex-direction: column;
    align-items: stretch;
  }
  .exam-topbar-actions {
    justify-content: space-between;
  }
}
</style>
