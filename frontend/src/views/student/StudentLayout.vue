<template>
  <el-container class="layout student-lab-theme" :class="{ 'is-exam': isExamRoute }">
    <el-aside v-if="!isExamRoute" width="240px" class="aside">
      <div class="brand">
        <div class="brand-title">学生学习中心</div>
        <div class="brand-sub">在线考试 · 学情分析</div>
      </div>
      <el-menu :default-active="route.path" router class="menu" :ellipsis="false">
        <el-menu-item index="/student">
          <span class="mi"><span class="mi-icon">▸</span>考试大厅</span>
        </el-menu-item>
        <el-menu-item index="/student/analytics">
          <span class="mi"><span class="mi-icon">▤</span>成绩分析</span>
        </el-menu-item>
        <el-menu-item index="/student/wrong">
          <span class="mi"><span class="mi-icon">✎</span>错题整理</span>
        </el-menu-item>
        <el-menu-item index="/student/feedback">
          <span class="mi"><span class="mi-icon">💬</span>留言反馈</span>
        </el-menu-item>
        <el-menu-item index="/student/profile">
          <span class="mi"><span class="mi-icon">◇</span>个人资料</span>
        </el-menu-item>
        <el-menu-item index="/student/settings">
          <span class="mi"><span class="mi-icon">⚙</span>个性化设置</span>
        </el-menu-item>
      </el-menu>
      <div class="aside-foot">
        <p class="aside-tip">交卷前请确认答案已保存；全屏考试请勿随意切换窗口。</p>
      </div>
    </el-aside>
    <el-container>
      <el-header v-if="!isExamRoute" class="header">
        <div class="header-left">
          <span class="title">在线考试</span>
          <span class="crumb">{{ headerCrumb }}</span>
        </div>
        <div class="right">
          <el-dropdown trigger="click" placement="bottom-end" @visible-change="onNotifyOpen">
            <span class="notify-bell" :class="{ dot: notifyUnread > 0 }">
              <span class="bell-ico">🔔</span>
              <el-badge v-if="notifyUnread > 0" :value="notifyUnread > 99 ? '99+' : notifyUnread" class="nb" />
            </span>
            <template #dropdown>
              <div class="notify-panel">
                <div class="notify-head">
                  <span>通知</span>
                  <el-button link type="primary" size="small" @click="markAllRead">全部已读</el-button>
                </div>
                <el-empty v-if="!notifyList.length" description="暂无通知" />
                <ul v-else class="notify-ul">
                  <li
                    v-for="n in notifyList"
                    :key="n.id"
                    class="notify-li"
                    :class="{ unread: !n.read }"
                    @click="openNotify(n)"
                  >
                    <div class="nt">{{ n.title }}</div>
                    <div class="nbd">{{ n.body }}</div>
                    <div class="ntime">{{ formatN(n.createdAt) }}</div>
                  </li>
                </ul>
              </div>
            </template>
          </el-dropdown>
          <span class="name">{{ store.profile?.displayName }} · {{ store.profile?.className || '—' }}</span>
          <el-button link type="primary" @click="logout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main" :class="{ 'main-exam': isExamRoute }">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import http from '../../api/http'

const route = useRoute()
const isExamRoute = computed(() => route.path.includes('/student/exam/'))
const router = useRouter()
const store = useUserStore()

const headerCrumb = computed(() => {
  const p = route.path
  if (p === '/student' || p === '/student/') return '考试大厅'
  if (p.startsWith('/student/analytics')) return '成绩分析'
  if (p.startsWith('/student/wrong')) return '错题本'
  if (p.startsWith('/student/feedback')) return '留言反馈'
  if (p.startsWith('/student/profile')) return '个人资料'
  if (p.startsWith('/student/settings')) return '设置'
  if (p.startsWith('/student/result')) return '答卷结果'
  return ''
})

const notifyList = ref([])
const notifyUnread = ref(0)

async function refreshNotifyCount() {
  try {
    const { data } = await http.get('/user/notifications/unread-count')
    notifyUnread.value = data?.count ?? 0
  } catch {
    notifyUnread.value = 0
  }
}

async function loadNotifyList() {
  try {
    const { data } = await http.get('/user/notifications', { params: { limit: 20 } })
    notifyList.value = data || []
  } catch {
    notifyList.value = []
  }
}

function onNotifyOpen(v) {
  if (v) {
    loadNotifyList()
    refreshNotifyCount()
  }
}

async function markAllRead() {
  try {
    await http.post('/user/notifications/read-all')
    await loadNotifyList()
    notifyUnread.value = 0
  } catch {
    /* ignore */
  }
}

async function openNotify(n) {
  if (!n.read) {
    try {
      await http.post(`/user/notifications/${n.id}/read`)
      n.read = true
      refreshNotifyCount()
    } catch {
      /* ignore */
    }
  }
}

function formatN(t) {
  if (!t) return ''
  return new Date(t).toLocaleString()
}

onMounted(() => {
  store.loadCache()
  refreshNotifyCount()
  window.addEventListener('focus', refreshNotifyCount)
})

function logout() {
  store.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
  background: var(--lab-bg, #f5f5f5);
}
.aside {
  background: var(--lab-surface, #fff);
  border-right: 1px solid var(--lab-border, #f0f0f0);
  display: flex;
  flex-direction: column;
}
.brand {
  padding: 22px 18px 14px;
}
.brand-title {
  font-weight: 800;
  font-size: 16px;
  color: var(--lab-primary, #e64340);
  letter-spacing: 0.02em;
}
.brand-sub {
  margin-top: 6px;
  font-size: 12px;
  color: #86909c;
}
.menu {
  border-right: none;
  background: transparent;
  flex: 1;
  padding: 0 8px 12px;
  --el-menu-active-color: var(--lab-primary, #e64340);
  --el-menu-hover-bg-color: var(--lab-primary-soft, rgba(230, 67, 64, 0.08));
}
.layout:not(.is-exam) .menu :deep(.el-menu-item.is-active) {
  background: var(--lab-primary-soft, rgba(230, 67, 64, 0.1)) !important;
  border-radius: var(--lab-radius, 8px);
  margin: 2px 4px;
  width: auto;
}
.mi {
  display: flex;
  align-items: center;
  gap: 8px;
}
.mi-icon {
  opacity: 0.55;
  font-size: 13px;
}
.aside-foot {
  padding: 12px 16px 20px;
  border-top: 1px dashed rgba(15, 23, 42, 0.08);
}
.aside-tip {
  margin: 0;
  font-size: 11px;
  line-height: 1.5;
  color: #9ca3af;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--lab-surface, #fff);
  border-bottom: 1px solid var(--lab-border, #f0f0f0);
  height: 56px !important;
  position: relative;
  box-shadow: 0 1px 0 rgba(0, 0, 0, 0.03);
}
.header::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, var(--lab-primary, #e64340), var(--lab-blue, #5b8ff9));
  opacity: 0.65;
}
.header-left {
  display: flex;
  align-items: baseline;
  gap: 12px;
}
.title {
  font-weight: 700;
  color: var(--lab-text, #262626);
  font-size: 16px;
}
.crumb {
  font-size: 13px;
  color: #86909c;
}
.right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.notify-bell {
  cursor: pointer;
  padding: 4px 8px;
  margin-right: 4px;
  position: relative;
  font-size: 18px;
  line-height: 1;
}
.notify-bell.dot {
  opacity: 1;
}
.nb :deep(.el-badge__content) {
  transform: translate(8px, -4px);
}
.notify-panel {
  width: 320px;
  max-height: 400px;
  overflow: auto;
  padding: 8px 0;
}
.notify-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 12px 8px;
  font-weight: 600;
  font-size: 14px;
  border-bottom: 1px solid #f0f0f0;
}
.notify-ul {
  list-style: none;
  margin: 0;
  padding: 0;
}
.notify-li {
  padding: 10px 12px;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
}
.notify-li:hover {
  background: #fafafa;
}
.notify-li.unread {
  background: rgba(230, 67, 64, 0.06);
}
.nt {
  font-weight: 600;
  font-size: 13px;
  color: #1d2129;
}
.nbd {
  font-size: 12px;
  color: #4e5969;
  margin-top: 4px;
  line-height: 1.4;
  white-space: pre-wrap;
  word-break: break-word;
}
.ntime {
  font-size: 11px;
  color: #86909c;
  margin-top: 6px;
}
.name {
  color: #4e5969;
  font-size: 14px;
}
.main {
  background: var(--lab-bg, #f5f5f5);
  padding: 20px 22px 28px;
}
.layout.is-exam {
  min-height: 100vh;
}
.main-exam {
  padding: 0 !important;
  background: var(--lab-bg, #f5f5f5) !important;
}
</style>
