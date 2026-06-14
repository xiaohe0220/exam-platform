<template>
  <el-container class="layout app-shell">
    <el-aside width="252px" class="aside app-shell__aside">
      <router-link to="/teacher/dashboard" class="brand">
        <span class="brand-mark">T</span>
        <span>
          <strong>教师工作台</strong>
          <small>Question · Exam · Review</small>
        </span>
      </router-link>

      <el-menu :default-active="route.path" router class="menu" :ellipsis="false">
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
          <span class="mi"><span class="mi-icon">{{ item.icon }}</span>{{ item.label }}</span>
        </el-menu-item>
      </el-menu>

      <div class="aside-card">
        <span class="aside-card__label">本周工作</span>
        <strong>{{ dashSummary || '等待数据同步' }}</strong>
      </div>
    </el-aside>

    <el-container>
      <el-header class="header app-shell__header">
        <div>
          <div class="eyebrow">Teacher Console</div>
          <div class="title-row">
            <h1>{{ headerTitle }}</h1>
            <span>{{ headerCrumb }}</span>
          </div>
        </div>
        <div class="right">
          <span class="name">{{ store.profile?.displayName || '教师' }}</span>
          <el-button link type="primary" @click="logout">退出</el-button>
        </div>
      </el-header>

      <el-main class="main ep-surface-cards">
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
const router = useRouter()
const store = useUserStore()
const summary = ref(null)

const menuItems = [
  { path: '/teacher/dashboard', icon: 'D', label: '数据概览' },
  { path: '/teacher/exams', icon: 'E', label: '考试发布' },
  { path: '/teacher/questions', icon: 'Q', label: '题库管理' },
  { path: '/teacher/feedback', icon: 'M', label: '学生留言' },
  { path: '/teacher/profile', icon: 'P', label: '个人资料' },
  { path: '/teacher/settings', icon: 'S', label: '外观设置' }
]

const routeMeta = computed(() => {
  const p = route.path
  if (p.includes('/dashboard')) return ['教学数据中心', '班级表现与考试统计']
  if (p.includes('/exams')) return ['考试发布', '组卷、发布、统计与阅卷']
  if (p.includes('/questions')) return ['题库管理', '维护题目与知识点']
  if (p.includes('/feedback')) return ['学生留言', '题目纠错与反馈处理']
  if (p.includes('/profile')) return ['个人资料', '账号与基础信息']
  if (p.includes('/settings')) return ['外观设置', '界面偏好']
  return ['教师工作台', '']
})

const headerTitle = computed(() => routeMeta.value[0])
const headerCrumb = computed(() => routeMeta.value[1])

const dashSummary = computed(() => {
  const s = summary.value
  if (!s) return ''
  return `题库 ${s.questionCount ?? '—'} · 试卷 ${s.examTotal ?? '—'}`
})

onMounted(async () => {
  store.loadCache()
  try {
    const { data } = await http.get('/teacher/dashboard/summary')
    summary.value = data
  } catch {
    summary.value = null
  }
})

function logout() {
  store.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
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
  color: var(--ds-text);
  text-decoration: none;
}

.brand-mark,
.mi-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
  border-radius: var(--ds-radius-sm);
  font-weight: 850;
  letter-spacing: 0;
}

.brand-mark {
  width: 40px;
  height: 40px;
  color: #fff;
  background: var(--ds-primary);
}

.brand strong {
  display: block;
  color: var(--ds-text);
  font-size: 16px;
}

.brand small {
  display: block;
  margin-top: 3px;
  color: var(--ds-text-secondary);
  font-size: 11px;
  font-weight: 700;
}

.menu {
  flex: 1;
  border-right: none;
  background: transparent;
  --el-menu-active-color: var(--ds-primary);
  --el-menu-hover-bg-color: var(--ds-primary-soft);
}

.menu :deep(.el-menu-item) {
  height: 42px;
  margin: 3px 0;
  border-radius: var(--ds-radius);
  color: var(--ds-text-secondary);
  font-weight: 700;
}

.menu :deep(.el-menu-item.is-active) {
  color: var(--ds-primary);
  background: var(--ds-primary-soft) !important;
}

.mi {
  display: flex;
  align-items: center;
  gap: 10px;
}

.mi-icon {
  width: 24px;
  height: 24px;
  color: var(--ds-primary);
  background: rgba(15, 139, 141, 0.1);
  font-size: 11px;
}

.aside-card {
  margin-top: 14px;
  padding: 14px;
  border: 1px solid var(--ds-border);
  border-radius: var(--ds-radius);
  background: var(--ds-surface-subtle);
}

.aside-card__label {
  display: block;
  margin-bottom: 8px;
  color: var(--ds-text-muted);
  font-size: 12px;
  font-weight: 750;
}

.aside-card strong {
  color: var(--ds-text);
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
  color: var(--ds-primary);
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
  color: var(--ds-text);
  font-size: 20px;
  font-weight: 900;
}

.title-row span,
.name {
  color: var(--ds-text-secondary);
  font-size: 13px;
}

.right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.main {
  padding: 22px;
}
</style>
