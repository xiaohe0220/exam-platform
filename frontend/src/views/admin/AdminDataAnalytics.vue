<template>
  <div class="wrap">
    <header class="header">
      <span class="title">数据分析</span>
      <div class="nav">
        <router-link class="nav-link" to="/admin">概览</router-link>
        <router-link class="nav-link" to="/admin/users">用户</router-link>
        <router-link class="nav-link" to="/admin/data">数据</router-link>
        <router-link class="nav-link" to="/admin/platform">平台设置</router-link>
        <router-link class="nav-link" to="/admin/monitor">监控</router-link>
        <router-link class="nav-link" to="/admin/audit">审计</router-link>
        <router-link class="nav-link" to="/admin/feedback">留言</router-link>
        <el-button link type="primary" @click="logout">退出</el-button>
      </div>
    </header>
    <el-main>
      <el-row v-if="data" :gutter="16" class="stat-row">
        <el-col :xs="24" :sm="12" :md="8">
          <el-card shadow="hover" class="s"><div class="sl">学生</div><div class="sn">{{ data.studentCount }}</div></el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <el-card shadow="hover" class="s"><div class="sl">教师</div><div class="sn">{{ data.teacherCount }}</div></el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <el-card shadow="hover" class="s"><div class="sl">已发布考试</div><div class="sn">{{ data.publishedExamCount }}</div></el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <el-card shadow="hover" class="s"><div class="sl">已交卷人次</div><div class="sn">{{ data.submittedAttemptCount }}</div></el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <el-card shadow="hover" class="s"><div class="sl">参与率（人次/学生）</div><div class="sn">{{ pct(data.participationRate) }}</div></el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <el-card shadow="hover" class="s"><div class="sl">全卷均分</div><div class="sn">{{ data.avgScoreAllAttempts ?? '—' }}</div></el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <el-card shadow="hover" class="s"><div class="sl">题库平均难度</div><div class="sn">{{ data.avgQuestionDifficulty?.toFixed(2) ?? '—' }}</div></el-card>
        </el-col>
      </el-row>

      <el-card v-loading="loading" shadow="never" class="card">
        <template #header><span class="h">考试效果（按交卷量 TOP10）</span></template>
        <el-table v-if="data?.examEffects?.length" :data="data.examEffects" border size="small">
          <el-table-column prop="title" label="考试" min-width="200" />
          <el-table-column prop="attemptCount" label="交卷" width="100" />
          <el-table-column prop="avgScore" label="平均分" width="120">

            <template #default="{ row }">{{ row.avgScore != null ? Number(row.avgScore).toFixed(2) : '—' }}</template>

          </el-table-column>

        </el-table>

        <el-empty v-else description="暂无数据" />

      </el-card>

    </el-main>

  </div>

</template>



<script setup>

import { onMounted, ref } from 'vue'

import { useRouter } from 'vue-router'

import { ElMessage } from 'element-plus'

import http from '../../api/http'

import { useUserStore } from '../../stores/user'



const router = useRouter()

const store = useUserStore()

const data = ref(null)

const loading = ref(false)



function pct(r) {

  if (r == null || Number.isNaN(r)) return '—'

  return `${(r * 100).toFixed(1)}%`

}



async function load() {

  loading.value = true

  try {

    const { data: d } = await http.get('/admin/analytics/overview')

    data.value = d

  } catch (e) {

    ElMessage.error(e.message)

  } finally {

    loading.value = false

  }

}



function logout() {

  store.logout()

  router.push('/login')

}



onMounted(load)

</script>



<style scoped>

.wrap {

  min-height: 100vh;

  background: #f5f5f5;

}

.header {

  display: flex;

  justify-content: space-between;

  align-items: center;

  height: 52px !important;

  background: #fff;

  border-bottom: 1px solid #f0f0f0;

  padding: 0 20px;

}

.title {

  font-weight: 700;

  color: #e64340;

}

.nav {

  display: flex;

  align-items: center;

  gap: 12px;

  flex-wrap: wrap;

}

.nav-link {

  color: #4e5969;

  font-size: 13px;

  text-decoration: none;

}

.nav-link:hover {

  color: #e64340;

}

.stat-row {

  max-width: 1100px;

  margin: 0 auto 16px;

}

.s {

  margin-bottom: 12px;

  border-radius: 10px;

}

.sl {

  font-size: 13px;

  color: #86909c;

}

.sn {

  margin-top: 8px;

  font-size: 20px;

  font-weight: 700;

  color: #1d2129;

}

.card {

  max-width: 1100px;

  margin: 0 auto;

}

.h {

  font-weight: 600;

}

</style>



