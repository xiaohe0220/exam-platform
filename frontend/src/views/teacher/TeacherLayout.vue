<template>
  <el-container class="layout">
    <el-aside width="240px" class="aside">
      <div class="brand">
        <div class="brand-title">教师工作台</div>
        <div class="brand-sub">命题 · 组卷 · 阅卷 · 学情</div>
      </div>
      <el-menu :default-active="route.path" router class="menu" :ellipsis="false">
        <el-menu-item index="/teacher/dashboard">
          <span class="mi"><span class="mi-icon">▣</span>数据概览</span>
        </el-menu-item>
        <el-menu-item index="/teacher/exams">
          <span class="mi"><span class="mi-icon">▸</span>考试发布</span>
        </el-menu-item>
        <el-menu-item index="/teacher/questions">
          <span class="mi"><span class="mi-icon">≡</span>题库管理</span>
        </el-menu-item>
        <el-menu-item index="/teacher/feedback">
          <span class="mi"><span class="mi-icon">💬</span>学生留言</span>
        </el-menu-item>
        <el-menu-item index="/teacher/profile">
          <span class="mi"><span class="mi-icon">◇</span>个人资料</span>
        </el-menu-item>
        <el-menu-item index="/teacher/settings">
          <span class="mi"><span class="mi-icon">⚙</span>个性化设置</span>
        </el-menu-item>
      </el-menu>
      <div class="aside-foot">
        <p class="aside-tip">发布前请核对班级范围与考试时间窗；延长结束时间适用于集体补时。</p>
      </div>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <span class="title">考试管理</span>
          <span class="crumb">{{ headerCrumb }}</span>
        </div>
        <div class="right">
          <span class="pill" v-if="dashSummary">{{ dashSummary }}</span>
          <span class="name">{{ store.profile?.displayName }}</span>
          <el-button link type="primary" @click="logout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main">
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

const headerCrumb = computed(() => {
  const p = route.path
  if (p.includes('/dashboard')) return '学情与图表'
  if (p.includes('/exams')) return '试卷与发布'
  if (p.includes('/questions')) return '题库'
  if (p.includes('/feedback')) return '学生留言'
  if (p.includes('/profile')) return '资料'
  if (p.includes('/settings')) return '设置'
  return ''
})

const dashSummary = computed(() => {
  const s = summary.value
  if (!s) return ''
  return `题库 ${s.questionCount ?? '—'} 题 · 试卷 ${s.examTotal ?? '—'} 份`
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
  background: linear-gradient(180deg, #ffffff 0%, rgba(99, 102, 241, 0.06) 100%);
  border-right: 1px solid var(--campus-accent-soft);
  display: flex;
  flex-direction: column;
}
.brand {
  padding: 22px 18px 14px;
}
.brand-title {
  font-weight: 800;
  font-size: 16px;
  color: var(--campus-primary);
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
  --el-menu-active-color: var(--campus-primary);
  --el-menu-hover-bg-color: var(--campus-accent-soft);
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
  background: #fff;
  border-bottom: 1px solid var(--campus-accent-soft);
  height: 56px !important;
  position: relative;
}
.header::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #3b6cff, #6366f1, #7c3aed);
  opacity: 0.85;
}
.header-left {
  display: flex;
  align-items: baseline;
  gap: 12px;
}
.title {
  font-weight: 700;
  font-size: 16px;
  color: var(--campus-primary);
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
.pill {
  font-size: 12px;
  color: #4e5969;
  background: var(--campus-accent-soft);
  padding: 4px 10px;
  border-radius: 999px;
}
.name {
  color: #4e5969;
  font-size: 14px;
}
.main {
  background: linear-gradient(180deg, var(--campus-bg) 0%, #fff 40%);
  padding: 22px;
}
</style>
