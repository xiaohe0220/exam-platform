<template>
  <el-container class="layout student-lab-theme app-shell" :class="{ 'is-exam': isExamRoute }">
    <el-aside v-if="!isExamRoute" width="252px" class="aside app-shell__aside">
      <router-link to="/student" class="brand">
        <span class="brand-mark">S</span>
        <span>
          <strong>学生学习中心</strong>
          <small>Exam · Score · Review</small>
        </span>
      </router-link>

      <el-menu :default-active="route.path" router class="menu" :ellipsis="false">
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
          <span class="mi"><span class="mi-icon">{{ item.icon }}</span>{{ item.label }}</span>
        </el-menu-item>
      </el-menu>

      <div class="aside-card">
        <span>当前身份</span>
        <strong>{{ store.profile?.displayName || '同学' }}</strong>
        <small>{{ store.profile?.className || '未设置班级' }}</small>
      </div>
    </el-aside>

    <el-container>
      <el-header v-if="!isExamRoute" class="header app-shell__header">
        <div>
          <div class="eyebrow">Student Console</div>
          <div class="title-row">
            <h1>{{ headerTitle }}</h1>
            <span>{{ headerCrumb }}</span>
          </div>
        </div>
        <div class="right">
          <el-dropdown trigger="click" placement="bottom-end" @visible-change="onNotifyOpen">
            <button class="notify" type="button" :class="{ unread: notifyUnread > 0 }" aria-label="通知">
              N
              <span v-if="notifyUnread > 0">{{ notifyUnread > 99 ? '99+' : notifyUnread }}</span>
            </button>
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
          <span class="name">{{ store.profile?.displayName || '同学' }}</span>
          <el-button link type="primary" @click="logout">退出</el-button>
        </div>
      </el-header>

      <el-main class="main ep-surface-cards" :class="{ 'main-exam': isExamRoute }">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import http from '../../api/http'

const route = useRoute()
const router = useRouter()
const store = useUserStore()
const isExamRoute = computed(() => route.path.includes('/student/exam/'))

const menuItems = [
  { path: '/student', icon: 'H', label: '考试大厅' },
  { path: '/student/analytics', icon: 'A', label: '成绩分析' },
  { path: '/student/wrong', icon: 'W', label: '错题整理' },
  { path: '/student/feedback', icon: 'M', label: '留言反馈' },
  { path: '/student/profile', icon: 'P', label: '个人资料' },
  { path: '/student/settings', icon: 'S', label: '外观设置' }
]

const routeMeta = computed(() => {
  const p = route.path
  if (p === '/student' || p === '/student/') return ['考试大厅', '可参加考试与入口']
  if (p.startsWith('/student/analytics')) return ['成绩分析', '排名、平均分与趋势']
  if (p.startsWith('/student/wrong')) return ['错题整理', '客观题复盘']
  if (p.startsWith('/student/feedback')) return ['留言反馈', '题目纠错与问题反馈']
  if (p.startsWith('/student/profile')) return ['个人资料', '账号与基础信息']
  if (p.startsWith('/student/settings')) return ['外观设置', '界面偏好']
  if (p.startsWith('/student/result')) return ['答卷结果', '成绩与客观题复盘']
  return ['学生学习中心', '']
})

const headerTitle = computed(() => routeMeta.value[0])
const headerCrumb = computed(() => routeMeta.value[1])

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

function logout() {
  store.logout()
  router.push('/login')
}

onMounted(() => {
  store.loadCache()
  refreshNotifyCount()
  window.addEventListener('focus', refreshNotifyCount)
})

onUnmounted(() => {
  window.removeEventListener('focus', refreshNotifyCount)
})
</script>

<style scoped>
.layout {
  min-height: 100vh;
}

.layout.is-exam {
  min-height: 100vh;
  background: var(--lab-bg);
}

.aside {
  display: flex;
  flex-direction: column;
  padding: 18px 14px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 8px 18px;
  color: var(--lab-text);
  text-decoration: none;
}

.brand-mark,
.mi-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
  border-radius: var(--lab-radius);
  font-weight: 850;
  letter-spacing: 0;
}

.brand-mark {
  width: 40px;
  height: 40px;
  color: #fff;
  background: var(--lab-primary);
}

.brand strong {
  display: block;
  color: var(--lab-text);
  font-size: 16px;
}

.brand small,
.aside-card small {
  display: block;
  margin-top: 3px;
  color: var(--lab-text-secondary);
  font-size: 11px;
  font-weight: 700;
}

.menu {
  flex: 1;
  border-right: none;
  background: transparent;
  --el-menu-active-color: var(--lab-primary);
  --el-menu-hover-bg-color: var(--lab-primary-soft);
}

.menu :deep(.el-menu-item) {
  height: 42px;
  margin: 3px 0;
  border-radius: var(--lab-radius);
  color: var(--lab-text-secondary);
  font-weight: 700;
}

.menu :deep(.el-menu-item.is-active) {
  color: var(--lab-primary);
  background: var(--lab-primary-soft) !important;
}

.mi {
  display: flex;
  align-items: center;
  gap: 10px;
}

.mi-icon {
  width: 24px;
  height: 24px;
  color: var(--lab-primary);
  background: var(--lab-primary-soft);
  font-size: 11px;
}

.aside-card {
  margin-top: 14px;
  padding: 14px;
  border: 1px solid var(--lab-border);
  border-radius: var(--lab-radius);
  background: #fff;
}

.aside-card span {
  display: block;
  margin-bottom: 8px;
  color: var(--lab-text-secondary);
  font-size: 12px;
  font-weight: 750;
}

.aside-card strong {
  color: var(--lab-text);
  font-size: 14px;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 72px !important;
  padding: 0 24px;
}

.eyebrow {
  color: var(--lab-primary);
  font-size: 11px;
  font-weight: 850;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.title-row {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-top: 4px;
}

.title-row h1 {
  margin: 0;
  color: var(--lab-text);
  font-size: 20px;
  font-weight: 900;
}

.title-row span,
.name {
  color: var(--lab-text-secondary);
  font-size: 13px;
}

.right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.notify {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border: 1px solid var(--lab-border);
  border-radius: var(--lab-radius);
  color: var(--lab-text);
  background: #fff;
  cursor: pointer;
  font-weight: 850;
}

.notify.unread {
  color: var(--lab-primary);
  border-color: rgba(255, 107, 74, 0.28);
  background: var(--lab-primary-soft);
}

.notify span {
  position: absolute;
  top: -7px;
  right: -7px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 999px;
  color: #fff;
  background: var(--lab-primary);
  font-size: 10px;
  line-height: 18px;
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
  border-bottom: 1px solid var(--lab-border);
  color: var(--lab-text);
  font-size: 14px;
  font-weight: 800;
}

.notify-ul {
  list-style: none;
  margin: 0;
  padding: 0;
}

.notify-li {
  padding: 10px 12px;
  border-bottom: 1px solid rgba(31, 41, 51, 0.07);
  cursor: pointer;
}

.notify-li:hover {
  background: #fafafa;
}

.notify-li.unread {
  background: var(--lab-primary-soft);
}

.nt {
  color: var(--lab-text);
  font-size: 13px;
  font-weight: 750;
}

.nbd {
  margin-top: 4px;
  color: var(--lab-text-secondary);
  font-size: 12px;
  line-height: 1.45;
  white-space: pre-wrap;
  word-break: break-word;
}

.ntime {
  margin-top: 6px;
  color: #9ca3af;
  font-size: 11px;
}

.main {
  padding: 22px;
}

.main-exam {
  padding: 0 !important;
  background: var(--lab-bg) !important;
}
</style>
