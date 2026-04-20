<template>
  <div class="monitor">
    <header class="mh">
      <div class="mh-left">
        <span class="mh-title">运维监控</span>
        <span class="mh-sub">实时 · 深色模式</span>
      </div>
      <div class="mh-nav">
        <router-link to="/admin" class="mh-link">概览</router-link>
        <router-link to="/admin/monitor" class="mh-link active">监控</router-link>
        <router-link to="/admin/audit" class="mh-link">审计</router-link>
        <router-link to="/admin/feedback" class="mh-link">留言</router-link>
        <router-link to="/admin/profile" class="mh-link">资料</router-link>
        <el-button type="primary" link @click="logout">退出</el-button>
      </div>
    </header>

    <main class="mmain">
      <aside class="maside">
        <div class="aside-h">筛选</div>
        <el-select v-model="region" placeholder="区域" size="small" class="mb">
          <el-option label="全校" value="all" />
          <el-option label="本部" value="main" />
        </el-select>
        <div class="aside-h">节点状态</div>
        <ul class="nodes">
          <li v-for="n in nodes" :key="n.id" class="node" :class="n.ok ? 'ok' : 'bad'">
            <span class="dot" />
            {{ n.name }}
            <span class="ms">{{ n.ms }}ms</span>
          </li>
        </ul>
      </aside>

      <section class="mcontent">
        <el-row :gutter="16" class="stat-strip">
          <el-col :span="6" :xs="12">
            <div class="s-card">
              <div class="s-l">在线会话</div>
              <div class="s-n">{{ overview?.attemptCount ?? '—' }}</div>
            </div>
          </el-col>
          <el-col :span="6" :xs="12">
            <div class="s-card">
              <div class="s-l">注册用户</div>
              <div class="s-n">{{ overview?.userTotal ?? '—' }}</div>
            </div>
          </el-col>
          <el-col :span="6" :xs="12">
            <div class="s-card warn">
              <div class="s-l">模拟负载</div>
              <div class="s-n">{{ loadPct }}%</div>
            </div>
          </el-col>
          <el-col :span="6" :xs="12">
            <div class="s-card ok">
              <div class="s-l">健康节点</div>
              <div class="s-n">{{ healthyNodes }}/{{ nodes.length }}</div>
            </div>
          </el-col>
        </el-row>

        <div class="panel">
          <div class="panel-h">并发与负载（模拟）</div>
          <div ref="lineRef" class="chart" />
        </div>

        <div class="panel">
          <div class="panel-h">进行中的考试</div>
          <el-table :data="examRows" size="small" class="tbl" :empty-text="'暂无考试进行中'">
            <el-table-column prop="title" label="考试" min-width="200" />
            <el-table-column prop="status" label="状态" width="100" />
            <el-table-column label="进度" min-width="220">
              <template #default="{ row }">
                <el-progress :percentage="row.pct" :stroke-width="10" :color="row.warn ? '#ff6b6b' : '#4a90e2'" />
              </template>
            </el-table-column>
            <el-table-column prop="online" label="在线" width="90" />
            <el-table-column label="告警" width="80" align="center">
              <template #default="{ row }">
                <el-tooltip v-if="row.warn" content="切屏次数异常偏高" placement="top">
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
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import http from '../../api/http'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const store = useUserStore()
const overview = ref(null)
const region = ref('all')
const lineRef = ref(null)
let chart = null
let timer = null
let t = 0

const nodes = ref([
  { id: 1, name: 'API-1', ok: true, ms: 12 },
  { id: 2, name: 'API-2', ok: true, ms: 18 },
  { id: 3, name: 'DB-主', ok: true, ms: 4 },
  { id: 4, name: 'Cache', ok: false, ms: 220 }
])

const healthyNodes = computed(() => nodes.value.filter((n) => n.ok).length)

const examRows = ref([
  { title: '人工智能通识 - 样例期末考试', status: 'PUBLISHED', pct: 62, online: 128, warn: false },
  { title: '模拟测验-渠道A', status: 'PUBLISHED', pct: 88, online: 24, warn: true }
])

const loadPct = ref(34)

function logout() {
  store.logout()
  router.push('/login')
}

function tickLine() {
  const el = lineRef.value
  if (!el) return
  if (!chart) chart = echarts.init(el)
  t += 1
  const n = 24
  const x = Array.from({ length: n }, (_, i) => {
    const d = new Date()
    d.setSeconds(d.getSeconds() - (n - i) * 5)
    return `${d.getHours()}:${String(d.getMinutes()).padStart(2, '0')}:${String(d.getSeconds()).padStart(2, '0')}`
  })
  const online = 80 + Math.sin(t / 6) * 25 + Math.random() * 8
  const cpu = 28 + Math.sin(t / 5) * 18 + Math.random() * 6
  loadPct.value = Math.min(95, Math.round(cpu + 5))
  chart.setOption({
    backgroundColor: 'transparent',
    grid: { left: 48, right: 24, top: 28, bottom: 28 },
    legend: { textStyle: { color: '#9aa0a6' }, data: ['在线', 'CPU%'] },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: x, axisLabel: { color: '#9aa0a6', fontSize: 10 } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: 'rgba(255,255,255,0.06)' } }, axisLabel: { color: '#9aa0a6' } },
    series: [
      { name: '在线', type: 'line', smooth: true, data: x.map(() => Math.round(online + Math.random() * 10)), areaStyle: { color: 'rgba(74,144,226,0.15)' }, lineStyle: { color: '#4a90e2' } },
      { name: 'CPU%', type: 'line', smooth: true, data: x.map(() => Math.round(cpu + Math.random() * 5)), lineStyle: { color: '#50c878' } }
    ]
  })
  chart.resize()
}

onMounted(async () => {
  store.loadCache()
  try {
    const { data } = await http.get('/admin/stats/overview')
    overview.value = data
  } catch {
    /* ignore */
  }
  tickLine()
  timer = setInterval(tickLine, 4000)
  window.addEventListener('resize', () => chart?.resize())
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
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
  padding: 0 24px;
  height: 56px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(26, 33, 41, 0.95);
}
.mh-title {
  font-size: 18px;
  font-weight: 700;
  color: #e8eaed;
}
.mh-sub {
  margin-left: 12px;
  font-size: 12px;
  color: #9aa0a6;
}
.mh-nav {
  display: flex;
  align-items: center;
  gap: 16px;
}
.mh-link {
  color: #9aa0a6;
  text-decoration: none;
  font-size: 14px;
}
.mh-link:hover,
.mh-link.active {
  color: #4a90e2;
}
.mmain {
  display: flex;
  min-height: calc(100vh - 56px);
}
.maside {
  width: 240px;
  padding: 20px 16px;
  border-right: 1px solid rgba(255, 255, 255, 0.08);
  background: #141820;
}
.aside-h {
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}
.mb {
  margin-bottom: 20px;
  width: 100%;
}
.nodes {
  list-style: none;
  padding: 0;
  margin: 0;
}
.node {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  font-size: 13px;
  border-radius: 8px;
  margin-bottom: 8px;
  background: rgba(255, 255, 255, 0.04);
}
.node.ok .dot {
  background: #50c878;
  box-shadow: 0 0 10px rgba(80, 200, 120, 0.5);
}
.node.bad .dot {
  background: #ff6b6b;
  box-shadow: 0 0 10px rgba(255, 107, 107, 0.45);
}
.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}
.ms {
  margin-left: auto;
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
  color: #e8eaed;
}
.panel {
  margin-bottom: 20px;
  padding: 16px;
  border-radius: 12px;
  background: #1a2129;
  border: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.35);
}
.panel-h {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #e8eaed;
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
  cursor: default;
}
.icon-ok {
  color: #50c878;
}
</style>
