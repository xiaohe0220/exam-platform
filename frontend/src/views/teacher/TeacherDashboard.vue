<template>
  <div class="dash">
    <section class="welcome">
      <div>
        <h2 class="welcome-title">教学数据中心</h2>
        <p class="welcome-sub">从概览 KPI 到单场考试分布，快速掌握班级整体表现。</p>
      </div>
      <div class="welcome-tags">
        <el-tag type="info" effect="plain" round>客观题自动批阅</el-tag>
        <el-tag type="success" effect="plain" round>主观题教师批阅</el-tag>
        <el-tag type="warning" effect="plain" round>切屏与全屏可配</el-tag>
      </div>
    </section>

    <div class="toolbar">
      <span class="toolbar-title">学情分析</span>
      <el-select
        v-model="selectedExamId"
        placeholder="选择一场考试查看图表"
        clearable
        filterable
        style="width: 320px"
        @change="onExamChange"
      >
        <el-option v-for="e in exams" :key="e.id" :label="e.title + ' · ' + e.status" :value="e.id" />
      </el-select>
    </div>

    <el-row v-if="stats && selectedExamId" :gutter="16" class="kpi-row">
      <el-col v-for="item in kpiTiles" :key="item.k" :xs="12" :sm="12" :md="6">
        <div class="kpi-card">
          <div class="kpi-label">{{ item.label }}</div>
          <div class="kpi-value" :class="item.tone">{{ formatKpi(item.k) }}</div>
          <div v-if="item.hint" class="kpi-hint">{{ item.hint }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row v-if="stats && selectedExamId" :gutter="16" class="chart-row">
      <el-col :xs="24" :lg="14">
        <div class="chart-card">
          <div class="chart-title">成绩分布（估算）</div>
          <p class="chart-note">基于已提交答卷的统计量模拟分数段分布，便于观察整体形态</p>
          <div ref="distRef" class="chart-el" />
        </div>
      </el-col>
      <el-col :xs="24" :lg="10">
        <div class="chart-card">
          <div class="chart-title">知识点掌握度（示意）</div>
          <div ref="radarRef" class="chart-el" />
        </div>
      </el-col>
      <el-col :span="24">
        <div class="chart-card">
          <div class="chart-title">高频错题 Top5（示意）</div>
          <div ref="barRef" class="chart-el bar-h" />
        </div>
      </el-col>
    </el-row>

    <el-row v-if="summary" :gutter="16">
      <el-col v-for="item in tiles" :key="item.k" :xs="24" :sm="12" :md="8">
        <el-card shadow="hover" class="tile">
          <div class="tile-label">{{ item.label }}</div>
          <div class="tile-value">{{ summary?.[item.k] ?? '—' }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="hint-card">
      <p>在「考试发布」中创建试卷、发布与阅卷；在「题库管理」中维护题目。上方下拉选择考试后可查看本场统计与图表。</p>
    </el-card>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import http from '../../api/http'
import { unwrapPage } from '../../api/paging'
import { estimateScoreBins, knowledgeRadarSeries, topWrongBarMock } from '../../utils/examStatsCharts'

const summary = ref(null)
const exams = ref([])
const selectedExamId = ref(null)
const stats = ref(null)

const distRef = ref(null)
const radarRef = ref(null)
const barRef = ref(null)
let chartDist = null
let chartRadar = null
let chartBar = null

const tiles = [
  { k: 'examTotal', label: '试卷总数' },
  { k: 'examPublished', label: '进行中(已发布)' },
  { k: 'examDraft', label: '草稿' },
  { k: 'examClosed', label: '已结束' },
  { k: 'totalStudentAttempts', label: '学生答卷次数' },
  { k: 'questionCount', label: '我的题库题量' }
]

const kpiTiles = computed(() => {
  if (!stats.value) return []
  const s = stats.value
  const passRate =
    s.submittedCount > 0 ? ((s.passCount / s.submittedCount) * 100).toFixed(1) + '%' : '—'
  return [
    { k: 'submittedCount', label: '参考人数', hint: '已提交', tone: '' },
    { k: 'avgScore', label: '平均分', hint: '满分 ' + (s.paperFullScore ?? '—'), tone: 'brand' },
    { k: 'maxScore', label: '最高分', hint: '最低 ' + (s.minScore ?? '—'), tone: 'ok' },
    { k: 'pass', label: '及格率', hint: '及格线 ' + (s.passLine ?? 60) + '% 折算', tone: 'warn' }
  ]
})

function formatKpi(k) {
  if (!stats.value) return '—'
  const s = stats.value
  if (k === 'pass') {
    return s.submittedCount > 0 ? ((s.passCount / s.submittedCount) * 100).toFixed(1) + '%' : '—'
  }
  if (k === 'avgScore' || k === 'maxScore') return s[k] ?? '—'
  if (k === 'submittedCount') return s.submittedCount ?? '—'
  return '—'
}

function disposeCharts() {
  chartDist?.dispose()
  chartRadar?.dispose()
  chartBar?.dispose()
  chartDist = chartRadar = chartBar = null
}

function renderCharts() {
  disposeCharts()
  if (!stats.value || !selectedExamId.value) return
  const s = stats.value
  const seed = Number(selectedExamId.value)
  const { labels, values } = estimateScoreBins(
    s.avgScore,
    s.minScore,
    s.maxScore,
    s.submittedCount,
    seed
  )
  const brand = '#4a90e2'
  if (distRef.value) {
    chartDist = echarts.init(distRef.value)
    chartDist.setOption({
      color: [brand],
      grid: { left: 48, right: 16, top: 24, bottom: 32 },
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: labels, axisLabel: { color: '#6b7280' } },
      yAxis: { type: 'value', splitLine: { lineStyle: { color: '#e5e7eb' } } },
      series: [
        {
          name: '人数',
          type: 'bar',
          data: values,
          barWidth: '48%',
          itemStyle: {
            borderRadius: [6, 6, 0, 0],
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(74,144,226,0.9)' },
              { offset: 1, color: 'rgba(74,144,226,0.35)' }
            ])
          }
        }
      ]
    })
  }

  const radarData = knowledgeRadarSeries(
    s.submittedCount > 0 ? s.passCount / s.submittedCount : 0,
    s.paperFullScore > 0 ? Number(s.avgScore) / Number(s.paperFullScore) : 0,
    seed
  )
  if (radarRef.value) {
    chartRadar = echarts.init(radarRef.value)
    chartRadar.setOption({
      radar: {
        indicator: radarData.map((d) => ({ name: d.name, max: 100 })),
        splitArea: { areaStyle: { color: ['rgba(74,144,226,0.05)', 'rgba(255,255,255,0.02)'] } },
        axisName: { color: '#6b7280' }
      },
      series: [
        {
          type: 'radar',
          data: [{ value: radarData.map((d) => d.value), name: '得分率' }],
          areaStyle: { color: 'rgba(74,144,226,0.25)' },
          lineStyle: { color: brand },
          symbolSize: 6
        }
      ]
    })
  }

  const wrong = topWrongBarMock(seed + 7)
  if (barRef.value) {
    chartBar = echarts.init(barRef.value)
    chartBar.setOption({
      grid: { left: 120, right: 40, top: 16, bottom: 24 },
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'value', max: 100, splitLine: { lineStyle: { color: '#e5e7eb' } } },
      yAxis: {
        type: 'category',
        data: wrong.map((w) => w.name),
        axisLabel: { color: '#6b7280' }
      },
      series: [
        {
          type: 'bar',
          data: wrong.map((w) => w.value),
          barWidth: 14,
          itemStyle: {
            borderRadius: [0, 6, 6, 0],
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
              { offset: 0, color: 'rgba(255,107,107,0.85)' },
              { offset: 1, color: 'rgba(245,166,35,0.75)' }
            ])
          }
        }
      ]
    })
  }

  const onResize = () => {
    chartDist?.resize()
    chartRadar?.resize()
    chartBar?.resize()
  }
  window.addEventListener('resize', onResize)
  return () => window.removeEventListener('resize', onResize)
}

let unResize = null

async function loadSummary() {
  const { data } = await http.get('/teacher/dashboard/summary')
  summary.value = data
}

async function loadExams() {
  const { data } = await http.get('/teacher/exams', { params: { page: 0, size: 200 } })
  const { items } = unwrapPage(data)
  exams.value = items || []
  const pub = exams.value.find((e) => e.status === 'PUBLISHED' || e.status === 'CLOSED')
  if (pub) {
    selectedExamId.value = pub.id
    await loadStats(pub.id)
  }
}

async function loadStats(examId) {
  if (!examId) {
    stats.value = null
    disposeCharts()
    return
  }
  const { data } = await http.get(`/teacher/exams/${examId}/stats`)
  stats.value = data
}

async function onExamChange(id) {
  disposeCharts()
  if (unResize) {
    unResize()
    unResize = null
  }
  await loadStats(id)
  await nextTick()
  await nextTick()
  unResize = renderCharts()
}

onMounted(async () => {
  try {
    await loadSummary()
    await loadExams()
    await nextTick()
    unResize = renderCharts()
  } catch (e) {
    ElMessage.error(e.message)
  }
})

onUnmounted(() => {
  if (unResize) unResize()
  disposeCharts()
})
</script>

<style scoped>
.dash {
  max-width: 1200px;
  margin: 0 auto;
}
.welcome {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
  padding: 18px 20px;
  margin-bottom: 18px;
  border-radius: 14px;
  background: linear-gradient(115deg, rgba(74, 144, 226, 0.12) 0%, #fff 50%, var(--campus-accent-soft) 140%);
  border: 1px solid var(--campus-accent-soft);
}
.welcome-title {
  margin: 0 0 6px;
  font-size: 20px;
  font-weight: 800;
  color: var(--ds-text, #1f2937);
}
.welcome-sub {
  margin: 0;
  font-size: 14px;
  color: var(--ds-text-secondary, #6b7280);
  line-height: 1.5;
}
.welcome-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}
.toolbar-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--ds-text, #1f2937);
}
.kpi-row {
  margin-bottom: 16px;
}
.kpi-card {
  background: var(--ds-surface, #fff);
  border-radius: var(--ds-radius, 10px);
  padding: 16px 18px;
  box-shadow: var(--ds-shadow-md, 0 8px 24px rgba(15, 23, 42, 0.08));
  margin-bottom: 12px;
  border: 1px solid var(--ds-border, rgba(15, 23, 42, 0.08));
}
.kpi-label {
  font-size: 13px;
  color: var(--ds-text-secondary, #6b7280);
}
.kpi-value {
  margin-top: 8px;
  font-size: 26px;
  font-weight: 700;
  color: var(--ds-text, #1f2937);
}
.kpi-value.brand {
  color: #4a90e2;
}
.kpi-value.ok {
  color: #50c878;
}
.kpi-value.warn {
  color: #f5a623;
}
.kpi-hint {
  margin-top: 6px;
  font-size: 12px;
  color: #9ca3af;
}
.chart-row {
  margin-bottom: 16px;
}
.chart-card {
  background: var(--ds-surface, #fff);
  border-radius: var(--ds-radius, 10px);
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: var(--ds-shadow-md, 0 8px 24px rgba(15, 23, 42, 0.08));
  border: 1px solid var(--ds-border, rgba(15, 23, 42, 0.08));
}
.chart-title {
  font-weight: 600;
  font-size: 15px;
  color: var(--ds-text, #1f2937);
}
.chart-note {
  font-size: 12px;
  color: #9ca3af;
  margin: 4px 0 8px;
}
.chart-el {
  height: 280px;
}
.chart-el.bar-h {
  height: 220px;
}
.tile {
  border-radius: 12px;
  margin-bottom: 16px;
  border: 1px solid var(--campus-accent-soft);
  background: linear-gradient(160deg, #fff 0%, var(--campus-accent-soft) 140%);
}
.tile-label {
  font-size: 13px;
  color: #86909c;
}
.tile-value {
  margin-top: 10px;
  font-size: 22px;
  font-weight: 700;
  color: var(--campus-primary);
}
.hint-card {
  margin-top: 8px;
  border-radius: 12px;
  color: #4e5969;
  font-size: 14px;
  line-height: 1.6;
}
</style>
