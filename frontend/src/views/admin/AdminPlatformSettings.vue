<template>
  <div class="wrap">
    <header class="header">
      <span class="title">平台设置</span>
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
      <el-card v-loading="loading" shadow="never" class="card">
        <template #header><span class="h">题库与考试默认策略</span></template>
        <el-form v-if="form" label-width="180px" class="form">
          <el-form-item label="题目难度下限（1–5）">
            <el-input-number v-model="form.minQuestionDifficulty" :min="1" :max="5" />
          </el-form-item>
          <el-form-item label="题目难度上限（1–5）">
            <el-input-number v-model="form.maxQuestionDifficulty" :min="1" :max="5" />
          </el-form-item>
          <el-form-item label="默认考试时长（分钟）">
            <el-input-number v-model="form.defaultExamDurationMinutes" :min="5" :max="600" />
          </el-form-item>
          <el-form-item label="默认可重考次数">
            <el-input-number v-model="form.defaultMaxRetakes" :min="1" :max="20" />
            <span class="hint">教师创建考试时仍可单独覆盖</span>
          </el-form-item>
          <el-divider />
          <el-form-item label="站内通知">
            <el-switch v-model="form.notifyInAppEnabled" />
          </el-form-item>
          <el-form-item label="邮件通道（需 SMTP）">
            <el-switch v-model="form.notifyEmailEnabled" />
            <span class="hint">开启后需在服务器配置 spring.mail；用户需填写邮箱</span>
          </el-form-item>
          <el-form-item label="短信通道（占位）">
            <el-switch v-model="form.notifySmsEnabled" />
            <span class="hint">对接短信网关后生效</span>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saving" @click="save">保存</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </el-main>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../../api/http'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const store = useUserStore()
const loading = ref(false)
const saving = ref(false)
const form = reactive({
  minQuestionDifficulty: 1,
  maxQuestionDifficulty: 5,
  defaultExamDurationMinutes: 90,
  defaultMaxRetakes: 1,
  notifyInAppEnabled: true,
  notifyEmailEnabled: false,
  notifySmsEnabled: false
})

async function load() {
  loading.value = true
  try {
    const { data } = await http.get('/admin/platform-settings')
    Object.assign(form, data)
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}

async function save() {
  saving.value = true
  try {
    await http.put('/admin/platform-settings', { ...form })
    ElMessage.success('已保存')
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    saving.value = false
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
.card {
  max-width: 720px;
  margin: 0 auto;
}
.h {
  font-weight: 600;
}
.hint {
  margin-left: 12px;
  font-size: 12px;
  color: #86909c;
}
</style>
