import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'
import LandingView from '../views/LandingView.vue'
import LoginView from '../views/LoginView.vue'
import TeacherLayout from '../views/teacher/TeacherLayout.vue'
import StudentLayout from '../views/student/StudentLayout.vue'

const routes = [
  { path: '/', component: LandingView, meta: { public: true } },
  { path: '/login', component: LoginView, meta: { public: true } },
  {
    path: '/teacher',
    component: TeacherLayout,
    meta: { roles: ['TEACHER', 'ADMIN', 'COLLEGE_ADMIN'] },
    children: [
      { path: '', redirect: '/teacher/dashboard' },
      { path: 'dashboard', component: () => import('../views/teacher/TeacherDashboard.vue') },
      { path: 'questions', component: () => import('../views/teacher/QuestionBank.vue') },
      { path: 'feedback', component: () => import('../views/shared/FeedbackInbox.vue') },
      { path: 'exams', component: () => import('../views/teacher/ExamManage.vue') },
      { path: 'settings', component: () => import('../views/shared/UserPreferences.vue') },
      { path: 'profile', component: () => import('../views/shared/UserProfile.vue') }
    ]
  },
  {
    path: '/student',
    component: StudentLayout,
    meta: { roles: ['STUDENT'] },
    children: [
      { path: '', component: () => import('../views/student/StudentHome.vue') },
      { path: 'analytics', component: () => import('../views/student/StudentAnalytics.vue') },
      { path: 'wrong', component: () => import('../views/student/StudentWrongBook.vue') },
      { path: 'feedback', component: () => import('../views/student/StudentFeedback.vue') },
      { path: 'profile', component: () => import('../views/shared/UserProfile.vue') },
      { path: 'settings', component: () => import('../views/shared/UserPreferences.vue') },
      { path: 'exam/:attemptId', component: () => import('../views/student/ExamTake.vue') },
      { path: 'result/:attemptId', component: () => import('../views/student/ExamResult.vue') }
    ]
  },
  {
    path: '/admin',
    meta: { roles: ['ADMIN', 'COLLEGE_ADMIN'] },
    component: () => import('../views/admin/AdminConsole.vue')
  },
  {
    path: '/admin/settings',
    meta: { roles: ['ADMIN', 'COLLEGE_ADMIN'] },
    component: () => import('../views/shared/UserPreferences.vue')
  },
  {
    path: '/admin/profile',
    meta: { roles: ['ADMIN', 'COLLEGE_ADMIN'] },
    component: () => import('../views/shared/UserProfile.vue')
  },
  {
    path: '/admin/monitor',
    meta: { roles: ['ADMIN', 'COLLEGE_ADMIN'] },
    component: () => import('../views/admin/AdminMonitor.vue')
  },
  {
    path: '/admin/audit',
    meta: { roles: ['ADMIN', 'COLLEGE_ADMIN'] },
    component: () => import('../views/admin/AdminAudit.vue')
  },
  {
    path: '/admin/feedback',
    meta: { roles: ['ADMIN', 'COLLEGE_ADMIN'] },
    component: () => import('../views/admin/AdminFeedback.vue')
  },
  {
    path: '/admin/users',
    meta: { roles: ['ADMIN', 'COLLEGE_ADMIN'] },
    component: () => import('../views/admin/AdminUserManage.vue')
  },
  {
    path: '/admin/platform',
    meta: { roles: ['ADMIN', 'COLLEGE_ADMIN'] },
    component: () => import('../views/admin/AdminPlatformSettings.vue')
  },
  {
    path: '/admin/data',
    meta: { roles: ['ADMIN', 'COLLEGE_ADMIN'] },
    component: () => import('../views/admin/AdminDataAnalytics.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const store = useUserStore()
  store.loadCache()
  if (store.token && to.path === '/login') {
    if (store.isAdmin) return '/admin'
    if (store.isTeacher) return '/teacher'
    if (store.isStudent) return '/student'
  }
  if (to.meta.public) return true
  if (!store.token) return '/login'
  if (to.meta.roles && !to.meta.roles.includes(store.profile?.role)) {
    if (store.isAdmin) return '/admin'
    if (store.isTeacher) return '/teacher'
    if (store.isStudent) return '/student'
    return '/login'
  }
  return true
})

export default router
