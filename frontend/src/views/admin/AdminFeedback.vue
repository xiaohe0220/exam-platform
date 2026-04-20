<template>
  <div class="wrap">
    <el-header class="header">
      <span class="title">学生留言</span>
      <div class="nav">
        <router-link class="nav-link" to="/admin">概览</router-link>
        <router-link class="nav-link" to="/admin/monitor">监控</router-link>
        <router-link class="nav-link" to="/admin/audit">审计</router-link>
        <router-link class="nav-link active" to="/admin/feedback">留言</router-link>
        <router-link class="nav-link" to="/admin/profile">资料</router-link>
        <router-link class="nav-link" to="/admin/settings">外观</router-link>
        <el-button link type="primary" @click="logout">退出</el-button>
      </div>
    </el-header>
    <el-main>
      <FeedbackInbox />
    </el-main>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import FeedbackInbox from '../shared/FeedbackInbox.vue'

const router = useRouter()
const store = useUserStore()

function logout() {
  store.logout()
  router.push('/login')
}
</script>

<style scoped>
.wrap {
  min-height: 100vh;
  background: var(--campus-bg, #f7f9fc);
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 52px;
  background: #fff;
  border-bottom: 1px solid var(--campus-accent-soft);
}
.title {
  font-weight: 700;
  color: var(--campus-primary);
}
.nav {
  display: flex;
  align-items: center;
  gap: 12px;
}
.nav-link {
  color: #4e5969;
  text-decoration: none;
  font-size: 14px;
}
.nav-link:hover,
.nav-link.active {
  color: var(--campus-primary);
  font-weight: 600;
}
</style>
