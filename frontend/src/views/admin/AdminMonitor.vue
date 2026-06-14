<template>
  <div class="monitor">
    <header class="mh">
      <div class="mh-left">
        <span class="mh-title">运维监控</span>
        <span class="mh-sub">真实探测 · {{ checkedAt }}</span>
      </div>
      <div class="mh-nav">
        <router-link to="/admin" class="mh-link">概览</router-link>
        <router-link to="/admin/users" class="mh-link">用户</router-link>
        <router-link to="/admin/data" class="mh-link">数据</router-link>
        <router-link to="/admin/platform" class="mh-link">平台设置</router-link>
        <router-link to="/admin/monitor" class="mh-link">监控</router-link>
        <router-link to="/admin/audit" class="mh-link">审计</router-link>
        <router-link to="/admin/feedback" class="mh-link">留言</router-link>
        <el-button type="primary" link @click="logout">退出</el-button>
      </div>
    </header>

    <main class="mmain">
      <aside class="maside">
        <div class="aside-h">节点状态</div>
        <ul class="nodes">
          <li v-for="n in nodes" :key="n.name" class="node" :class="n.ok ? 'ok' : 'bad'">
            <span class="dot" />
            <span>{{ n.name }}</span>
            <span class="ms">{{ n.ms }}ms</span>
            <small>{{ n.message }}</small>
          </li>
        </ul>
      </aside>

      <section class="mcontent">
        <el-row :gutter="16" class="stat-strip">
          <el-col :span="6" :xs="12">
            <div class="s-card">
              <div class="s-l">在线考试会话</div>
              <div class="s-n">{{ live?.onlineExamSessions ?? '—' }}</div>
            </div>
          </el-col>
          <el-col :span="6" :xs="12">
            <div class="s-card">
              <div class="s-l">注册用户</div>
              <div class="s-n">{{ live?.userTotal ?? '—' }}</div>
            </div>
          </el-col>
          <el-col :span="6" :xs="12">
            <div class="s-card warn">
              <div class="s-l">CPU</div>
              <div class="s-n">{{ fmtPct(live?.cpuLoadPercent) }}</div>
            </div>
          </el-col>
          <el-col :span="6" :xs="12">
            <div class="s-card ok">
              <div class="s-l">JVM 内存</div>
              <div class="s-n">{{ live ? `${live.heapUsedMb}/${live.heapMaxMb}MB` : '—' }}</div>
            </div>
          </el-col>
        </el-row>

        <div class="panel">
          <div class="panel-h">
            <span>在线会话与 CPU</span>
            <el-button size="small" link type="primary" :loading="loading" @click="load">刷新</el-button>
          </div>
          <div ref="lineRef" class="chart" />
        </div>

        <div class="panel">
          <div class="panel-h">进行中的考试</div>
          <el-table :data="live?.activeExams || []" size="small" class="tbl" :empty-text="'暂无考试进行中'">
            <el-table-column prop="title" label="考试" min-width="220" />
            <el-table-column prop="status" label="状态" width="110" />
            <el-table-column label="进度" min-width="220">
              <template #default="{ row }">
                <el-progress :percentage="row.progressPercent" :stroke-width="10" :color="row.warn ? '#ff6b6b' : '#4a90e2'" />
              </template>
            </el-table-column>
            <el-table-column prop="online" label="在线" width="90" />
            <el-table-column prop="submitted" label="已交卷" width="90" />
            <el-table-column prop="abnormalSwitchCount" label="切屏预警" width="100" />
            <el-table-column label="状态" width="80" align="center">
              <template #default="{ row }">
                <el-tooltip v-if="row.warn" content="存在高切屏或高并发风险" placement="top">
                  <span class="icon-warn">!</span>
                </el-tooltip>
                <span v-else class="icon-ok">✓</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import http from '../../api/http'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const store = useUserStore()
const live = ref(null)
const loading = ref(false)
const lineRef = ref(null)
const history = ref([])
let chart = null
let timer = null

const nodes = computed(() => live.value?.nodes || [])
const checkedAt = computed(() => (live.value?.checkedAt ? new Date(live.value.checkedAt).toLocaleString() : '等待探测'))

function logout() {
  store.logout()
  router.push('/login')
}

function fmtPct(v) {
  if (v == null) return '—'
  return `${Number(v).toFixed(1)}%`
}

async function load() {
  loading.value = true
  try {
    const { data } = await http.get('/admin/monitor/live')
    live.value = data
    history.value.push({
      t: new Date(data.checkedAt).toLocaleTimeString(),
      online: data.onlineExamSessions || 0,
      cpu: Number(data.cpuLoadPercent || 0)
    })
    history.value = history.value.slice(-24)
    await nextTick()
    renderChart()
  } finally {
    loading.value = false
  }
}

function renderChart() {
  if (!lineRef.value) return
  if (!chart) chart = echarts.init(lineRef.value)
  chart.setOption({
    backgroundColor: 'transparent',
    grid: { left: 48, right: 24, top: 28, bottom: 28 },
    legend: { textStyle: { color: '#9aa0a6' }, data: ['在线', 'CPU%'] },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: history.value.map((i) => i.t), axisLabel: { color: '#9aa0a6', fontSize: 10 } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: 'rgba(255,255,255,0.06)' } }, axisLabel: { color: '#9aa0a6' } },
    series: [
      { name: '在线', type: 'line', smooth: true, data: history.value.map((i) => i.online), areaStyle: { color: 'rgba(74,144,226,0.15)' }, lineStyle: { color: '#4a90e2' } },
      { name: 'CPU%', type: 'line', smooth: true, data: history.value.map((i) => i.cpu), lineStyle: { color: '#50c878' } }
    ]
  })
  chart.resize()
}

function onResize() {
  chart?.resize()
}

onMounted(async () => {
  store.loadCache()
  await load()
  timer = setInterval(load, 5000)
  window.addEventListener('resize', onResize)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  window.removeEventListener('resize', onResize)
  chart?.dispose()
})
</script>

<style scoped>
.monitor {
  min-height: 100vh;
  background: #0f1419;
  color: #e8eaed;
}
.mh {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  row-gap: 10px;
  padding: 10px 24px;
  min-height: 56px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(26, 33, 41, 0.95);
}
.mh-title {
  font-size: 18px;
  font-weight: 700;
}
.mh-sub {
  margin-left: 12px;
  font-size: 12px;
  color: #9aa0a6;
}
.mh-nav {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 12px 16px;
  flex: 1;
}
.mh-link {
  color: #9aa0a6;
  text-decoration: none;
  font-size: 14px;
}
.mh-link:hover,
.mh-link.router-link-active {
  color: #4a90e2;
  font-weight: 600;
}
.mmain {
  display: flex;
  min-height: calc(100vh - 56px);
}
.maside {
  width: 260px;
  padding: 20px 16px;
  border-right: 1px solid rgba(255, 255, 255, 0.08);
  background: #141820;
}
.aside-h {
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 8px;
  text-transform: uppercase;
}
.nodes {
  list-style: none;
  padding: 0;
  margin: 0;
}
.node {
  display: grid;
  grid-template-columns: 8px 1fr auto;
  gap: 8px;
  padding: 10px 12px;
  font-size: 13px;
  border-radius: 8px;
  margin-bottom: 8px;
  background: rgba(255, 255, 255, 0.04);
}
.node small {
  grid-column: 2 / 4;
  color: #7b8491;
}
.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-top: 5px;
}
.node.ok .dot {
  background: #50c878;
  box-shadow: 0 0 10px rgba(80, 200, 120, 0.5);
}
.node.bad .dot {
  background: #ff6b6b;
  box-shadow: 0 0 10px rgba(255, 107, 107, 0.45);
}
.ms {
  color: #9aa0a6;
  font-size: 12px;
}
.mcontent {
  flex: 1;
  padding: 20px 24px 40px;
  overflow: auto;
}
.stat-strip {
  margin-bottom: 16px;
}
.s-card {
  padding: 14px 16px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  margin-bottom: 12px;
}
.s-card.warn {
  border-color: rgba(245, 166, 35, 0.35);
}
.s-card.ok {
  border-color: rgba(80, 200, 120, 0.35);
}
.s-l {
  font-size: 12px;
  color: #9aa0a6;
}
.s-n {
  margin-top: 6px;
  font-size: 22px;
  font-weight: 700;
}
.panel {
  margin-bottom: 20px;
  padding: 16px;
  border-radius: 10px;
  background: #1a2129;
  border: 1px solid rgba(255, 255, 255, 0.08);
}
.panel-h {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 12px;
}
.chart {
  height: 280px;
}
.tbl {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: rgba(255, 255, 255, 0.04);
  --el-table-text-color: #e8eaed;
  --el-table-border-color: rgba(255, 255, 255, 0.08);
}
.icon-warn {
  display: inline-flex;
  width: 22px;
  height: 22px;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(245, 166, 35, 0.2);
  color: #f5a623;
  font-weight: 700;
}
.icon-ok {
  color: #50c878;
}
@media (max-width: 860px) {
  .mmain {
    display: block;
  }
  .maside {
    width: auto;
    border-right: 0;
    border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  }
}
</style>
