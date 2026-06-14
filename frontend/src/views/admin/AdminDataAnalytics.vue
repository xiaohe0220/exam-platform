<template>
  <div class="wrap">
    <header class="header">
      <span class="title">数据中心</span>
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

    <el-main class="main">
      <section class="top-actions">
        <div>
          <h1>平台数据与基础资料</h1>
          <p>汇总考试效果，维护班级课程，并导出核心业务备份。</p>
        </div>
        <div class="action-row">
          <el-button type="primary" plain @click="exportAnalytics">导出分析</el-button>
          <el-button type="warning" plain @click="exportBackup">导出备份</el-button>
        </div>
      </section>

      <el-row v-if="data" :gutter="16" class="stat-row">
        <el-col v-for="s in stats" :key="s.label" :xs="12" :sm="12" :md="6">
          <div class="stat-card">
            <div class="sl">{{ s.label }}</div>
            <div class="sn">{{ s.value }}</div>
          </div>
        </el-col>
      </el-row>

      <el-tabs v-model="activeTab" class="tabs">
        <el-tab-pane label="考试效果" name="analytics">
          <div class="panel">
            <div class="panel-head">
              <span>考试效果 TOP10</span>
              <el-button size="small" :loading="loading" @click="loadAnalytics">刷新</el-button>
            </div>
            <el-table v-if="data?.examEffects?.length" :data="data.examEffects" border size="small">
              <el-table-column prop="title" label="考试" min-width="220" />
              <el-table-column prop="attemptCount" label="交卷" width="100" />
              <el-table-column prop="avgScore" label="平均分" width="120">
                <template #default="{ row }">{{ row.avgScore != null ? Number(row.avgScore).toFixed(2) : '—' }}</template>
              </el-table-column>
            </el-table>
            <el-empty v-else description="暂无数据" />
          </div>
        </el-tab-pane>

        <el-tab-pane label="班级管理" name="classes">
          <div class="panel">
            <div class="panel-head">
              <span>班级</span>
              <el-button type="primary" size="small" @click="openClass()">新增班级</el-button>
            </div>
            <el-table v-loading="classLoading" :data="classRows" border size="small">
              <el-table-column prop="name" label="班级" min-width="150" />
              <el-table-column prop="college" label="学院" min-width="140" />
              <el-table-column prop="major" label="专业" min-width="140" />
              <el-table-column prop="grade" label="年级" width="100" />
              <el-table-column prop="studentCount" label="学生数" width="90" />
              <el-table-column label="启用" width="80">
                <template #default="{ row }">
                  <el-tag :type="row.enabled ? 'success' : 'info'" size="small">{{ row.enabled ? '是' : '否' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="150" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" @click="openClass(row)">编辑</el-button>
                  <el-button link type="danger" @click="removeClass(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <el-tab-pane label="课程管理" name="courses">
          <div class="panel">
            <div class="panel-head">
              <span>课程</span>
              <el-button type="primary" size="small" @click="openCourse()">新增课程</el-button>
            </div>
            <el-table v-loading="courseLoading" :data="courseRows" border size="small">
              <el-table-column prop="code" label="代码" width="130" />
              <el-table-column prop="name" label="课程" min-width="180" />
              <el-table-column prop="college" label="学院" min-width="140" />
              <el-table-column prop="teacherName" label="任课教师" width="120" />
              <el-table-column label="启用" width="80">
                <template #default="{ row }">
                  <el-tag :type="row.enabled ? 'success' : 'info'" size="small">{{ row.enabled ? '是' : '否' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="150" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" @click="openCourse(row)">编辑</el-button>
                  <el-button link type="danger" @click="removeCourse(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <el-tab-pane label="备份" name="backup">
          <div class="panel backup">
            <div>
              <h2>核心数据备份</h2>
              <p>导出用户、题库、考试、答卷、审计、班级和课程数据。生产环境仍建议配合数据库定时备份。</p>
              <p v-if="backupStatus" class="status-line">
                数据库：{{ backupStatus.databaseOk ? '正常' : '异常' }} · Redis：{{
                  backupStatus.redisConfigured ? (backupStatus.redisOk ? '正常' : '异常') : '未启用'
                }}
              </p>
            </div>
            <el-button type="warning" @click="exportBackup">导出备份工作簿</el-button>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-main>

    <el-dialog v-model="classDlg" :title="classForm.id ? '编辑班级' : '新增班级'" width="460px">
      <el-form label-position="top">
        <el-form-item label="班级名称" required><el-input v-model="classForm.name" /></el-form-item>
        <el-form-item label="学院"><el-input v-model="classForm.college" /></el-form-item>
        <el-form-item label="专业"><el-input v-model="classForm.major" /></el-form-item>
        <el-form-item label="年级"><el-input v-model="classForm.grade" /></el-form-item>
        <el-form-item label="启用"><el-switch v-model="classForm.enabled" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="classDlg = false">取消</el-button>
        <el-button type="primary" :loading="classSaving" @click="saveClass">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="courseDlg" :title="courseForm.id ? '编辑课程' : '新增课程'" width="460px">
      <el-form label-position="top">
        <el-form-item label="课程代码"><el-input v-model="courseForm.code" /></el-form-item>
        <el-form-item label="课程名称" required><el-input v-model="courseForm.name" /></el-form-item>
        <el-form-item label="学院"><el-input v-model="courseForm.college" /></el-form-item>
        <el-form-item label="任课教师"><el-input v-model="courseForm.teacherName" /></el-form-item>
        <el-form-item label="启用"><el-switch v-model="courseForm.enabled" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="courseDlg = false">取消</el-button>
        <el-button type="primary" :loading="courseSaving" @click="saveCourse">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../../api/http'
import { useUserStore } from '../../stores/user'
import { saveBlob } from '../../utils/download'

const router = useRouter()
const store = useUserStore()
const activeTab = ref('analytics')
const data = ref(null)
const loading = ref(false)
const classRows = ref([])
const courseRows = ref([])
const classLoading = ref(false)
const courseLoading = ref(false)
const backupStatus = ref(null)

const classDlg = ref(false)
const courseDlg = ref(false)
const classSaving = ref(false)
const courseSaving = ref(false)

const classForm = reactive({ id: null, name: '', college: '', major: '', grade: '', enabled: true })
const courseForm = reactive({ id: null, code: '', name: '', college: '', teacherName: '', enabled: true })

const stats = computed(() => {
  const d = data.value
  if (!d) return []
  return [
    { label: '学生', value: d.studentCount },
    { label: '教师', value: d.teacherCount },
    { label: '已发布考试', value: d.publishedExamCount },
    { label: '已交卷人次', value: d.submittedAttemptCount },
    { label: '参与率', value: pct(d.participationRate) },
    { label: '全卷均分', value: d.avgScoreAllAttempts ?? '—' },
    { label: '题库平均难度', value: d.avgQuestionDifficulty?.toFixed(2) ?? '—' }
  ]
})

function pct(r) {
  if (r == null || Number.isNaN(r)) return '—'
  return `${(r * 100).toFixed(1)}%`
}

function logout() {
  store.logout()
  router.push('/login')
}

async function loadAnalytics() {
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

async function loadClasses() {
  classLoading.value = true
  try {
    const { data } = await http.get('/admin/academic/classes')
    classRows.value = data || []
  } finally {
    classLoading.value = false
  }
}

async function loadCourses() {
  courseLoading.value = true
  try {
    const { data } = await http.get('/admin/academic/courses')
    courseRows.value = data || []
  } finally {
    courseLoading.value = false
  }
}

async function loadBackupStatus() {
  const { data } = await http.get('/admin/backup/status')
  backupStatus.value = data
}

function openClass(row) {
  Object.assign(classForm, row ? { ...row } : { id: null, name: '', college: '', major: '', grade: '', enabled: true })
  classDlg.value = true
}

async function saveClass() {
  if (!classForm.name.trim()) {
    ElMessage.warning('请填写班级名称')
    return
  }
  classSaving.value = true
  try {
    const body = { ...classForm, name: classForm.name.trim() }
    if (classForm.id) await http.put(`/admin/academic/classes/${classForm.id}`, body)
    else await http.post('/admin/academic/classes', body)
    ElMessage.success('已保存')
    classDlg.value = false
    await loadClasses()
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    classSaving.value = false
  }
}

async function removeClass(row) {
  try {
    await ElMessageBox.confirm(`确认删除班级「${row.name}」？`, '删除班级', { type: 'warning' })
    await http.delete(`/admin/academic/classes/${row.id}`)
    ElMessage.success('已删除')
    await loadClasses()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message)
  }
}

function openCourse(row) {
  Object.assign(courseForm, row ? { ...row } : { id: null, code: '', name: '', college: '', teacherName: '', enabled: true })
  courseDlg.value = true
}

async function saveCourse() {
  if (!courseForm.name.trim()) {
    ElMessage.warning('请填写课程名称')
    return
  }
  courseSaving.value = true
  try {
    const body = { ...courseForm, name: courseForm.name.trim() }
    if (courseForm.id) await http.put(`/admin/academic/courses/${courseForm.id}`, body)
    else await http.post('/admin/academic/courses', body)
    ElMessage.success('已保存')
    courseDlg.value = false
    await loadCourses()
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    courseSaving.value = false
  }
}

async function removeCourse(row) {
  try {
    await ElMessageBox.confirm(`确认删除课程「${row.name}」？`, '删除课程', { type: 'warning' })
    await http.delete(`/admin/academic/courses/${row.id}`)
    ElMessage.success('已删除')
    await loadCourses()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message)
  }
}

async function exportAnalytics() {
  try {
    const res = await http.get('/admin/analytics/export', { responseType: 'blob' })
    saveBlob(res.data, 'admin_analytics.xlsx')
  } catch (e) {
    ElMessage.error(e.message)
  }
}

async function exportBackup() {
  try {
    const res = await http.get('/admin/backup/export', { responseType: 'blob' })
    saveBlob(res.data, 'exam_platform_backup.xlsx')
  } catch (e) {
    ElMessage.error(e.message)
  }
}

onMounted(() => {
  loadAnalytics()
  loadClasses()
  loadCourses()
  loadBackupStatus().catch(() => {})
})
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
  min-height: 52px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  padding: 0 20px;
}
.title {
  font-weight: 700;
  color: #0f766e;
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
.nav-link:hover,
.nav-link.router-link-active {
  color: #0f766e;
}
.main {
  max-width: 1180px;
  margin: 0 auto;
}
.top-actions {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-end;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e5e7eb;
}
.top-actions h1 {
  margin: 0 0 6px;
  font-size: 22px;
}
.top-actions p {
  margin: 0;
  color: #6b7280;
  font-size: 13px;
}
.action-row,
.tools {
  display: flex;
  gap: 10px;
}
.stat-row {
  margin-bottom: 16px;
}
.stat-card,
.panel {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}
.stat-card {
  padding: 14px 16px;
  margin-bottom: 12px;
}
.sl {
  font-size: 13px;
  color: #6b7280;
}
.sn {
  margin-top: 8px;
  font-size: 22px;
  font-weight: 700;
  color: #111827;
}
.tabs {
  background: transparent;
}
.panel {
  padding: 16px;
}
.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
  font-weight: 700;
}
.backup {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
}
.backup h2 {
  margin: 0 0 8px;
  font-size: 18px;
}
.backup p {
  margin: 0 0 6px;
  color: #6b7280;
  line-height: 1.6;
}
.status-line {
  font-size: 13px;
}
@media (max-width: 720px) {
  .top-actions,
  .backup {
    display: block;
  }
  .action-row {
    margin-top: 12px;
    flex-wrap: wrap;
  }
}
</style>
