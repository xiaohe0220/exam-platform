<template>
  <div class="fb-page">
    <header class="fb-top">
      <div>
        <h1 class="fb-title">留言反馈</h1>
        <p class="fb-sub">将题目异议、解析问题或考试咨询提交给教师与教务</p>
      </div>
    </header>
    <el-row :gutter="20">
      <el-col :xs="24" :lg="12">
        <el-card shadow="never" class="card form-card">
          <template #header><span class="h">我要留言</span></template>
          <p class="tip">
            若发现题目或解析有误、对考试安排有疑问，可在此提交给教师与教务处理。
          </p>
          <el-form label-position="top" @submit.prevent="submit">
            <el-form-item label="主题" required>
              <el-input
                v-model="form.subject"
                maxlength="40"
                show-word-limit
                placeholder="例如：考试安排咨询、第3题选项有误"
              />
            </el-form-item>
            <el-form-item label="课程" required>
              <el-select v-model="form.course" placeholder="请选择课程" style="width: 100%">
                <el-option v-for="c in courseOptions" :key="c" :label="c" :value="c" />
              </el-select>
            </el-form-item>
            <el-form-item label="班级" required>
              <el-select v-model="form.className" placeholder="请选择班级" style="width: 100%">
                <el-option v-for="c in classOptions" :key="c" :label="c" :value="c" />
              </el-select>
            </el-form-item>
            <el-form-item label="内容" required>
              <el-input
                v-model="form.content"
                type="textarea"
                :rows="4"
                maxlength="300"
                show-word-limit
                placeholder="请说明题目位置、试卷名称或截图说明，便于教师核对"
              />
            </el-form-item>
            <el-button type="primary" :loading="saving" native-type="submit" class="submit-btn">
              提交留言
            </el-button>
          </el-form>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card shadow="never" class="card list-card">
          <template #header><span class="h">我的留言记录</span></template>
          <div v-loading="loading" class="list-wrap">
            <el-empty v-if="!list.length" description="暂无留言" />
            <div v-else class="msg-list">
              <div v-for="m in list" :key="m.id" class="msg-item">
                <div class="msg-head">
                  <span class="msg-sub">{{ m.subject }}</span>
                  <el-tag size="small" :type="m.status === 'REPLIED' ? 'success' : 'warning'">
                    {{ m.status === 'REPLIED' ? '已回复' : '待处理' }}
                  </el-tag>
                </div>
                <div class="meta">{{ m.course }} · {{ m.className }}</div>
                <p class="msg-body">{{ m.content }}</p>
                <div v-if="m.replyContent" class="reply-box">
                  <div class="reply-label">回复（{{ m.repliedByName || '教师' }}）</div>
                  <p class="reply-text">{{ m.replyContent }}</p>
                </div>
                <div class="time">{{ formatTime(m.createdAt) }}</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../../api/http'
import { useUserStore } from '../../stores/user'

const store = useUserStore()
const loading = ref(false)
const saving = ref(false)
const list = ref([])

const courseOptions = [
  '人工智能通识教育',
  '高等数学',
  '大学英语',
  '计算机基础',
  '其他'
]

const classOptionsBase = [
  '2024级-1班',
  '2024级-2班',
  '2023级-1班',
  '其他'
]

const classOptions = computed(() => {
  const cn = store.profile?.className?.trim()
  if (cn && !classOptionsBase.includes(cn)) {
    return [cn, ...classOptionsBase]
  }
  return classOptionsBase
})

const form = reactive({
  subject: '',
  course: '人工智能通识教育',
  className: '',
  content: ''
})

function pickDefaultClass() {
  const cn = store.profile?.className?.trim()
  const opts = classOptions.value
  if (cn && opts.includes(cn)) {
    form.className = cn
  } else if (cn) {
    form.className = cn
  } else {
    form.className = opts[0]
  }
}

async function load() {
  loading.value = true
  try {
    const { data } = await http.get('/student/messages')
    list.value = data || []
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}

async function submit() {
  if (!form.subject?.trim()) {
    ElMessage.warning('请填写主题')
    return
  }
  if (!form.content?.trim()) {
    ElMessage.warning('请填写留言内容')
    return
  }
  saving.value = true
  try {
    await http.post('/student/messages', {
      subject: form.subject.trim(),
      course: form.course,
      className: form.className,
      content: form.content.trim()
    })
    ElMessage.success('提交成功')
    form.subject = ''
    form.content = ''
    await load()
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    saving.value = false
  }
}

function formatTime(t) {
  if (!t) return ''
  return new Date(t).toLocaleString()
}

onMounted(() => {
  pickDefaultClass()
  load()
})
</script>

<style scoped>
.fb-page {
  max-width: 1200px;
  margin: 0 auto;
}
.fb-top {
  margin-bottom: 20px;
  padding: 4px 2px 12px;
  border-bottom: 1px solid var(--lab-border, #f0f0f0);
}
.fb-title {
  margin: 0 0 6px;
  font-size: 20px;
  font-weight: 700;
  color: var(--lab-text, #262626);
  letter-spacing: 0.02em;
}
.fb-sub {
  margin: 0;
  font-size: 13px;
  color: var(--lab-text-secondary, #8c8c8c);
}
.card {
  border-radius: var(--lab-radius, 8px);
  border: 1px solid var(--lab-border, #f0f0f0);
  min-height: 420px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}
.form-card {
  background: var(--lab-surface, #fff);
}
.list-card {
  background: var(--lab-surface, #fff);
}
.h {
  font-weight: 700;
  font-size: 16px;
  color: var(--lab-text, #262626);
}
.tip {
  margin: 0 0 16px;
  font-size: 13px;
  color: var(--lab-text-secondary, #8c8c8c);
  line-height: 1.5;
}
.submit-btn {
  width: 100%;
  margin-top: 8px;
}
.list-wrap {
  min-height: 320px;
}
.msg-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.msg-item {
  padding: 14px;
  border-radius: var(--lab-radius, 8px);
  background: #fafafa;
  border: 1px solid var(--lab-border, #f0f0f0);
}
.msg-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}
.msg-sub {
  font-weight: 600;
  color: var(--lab-text, #262626);
}
.meta {
  font-size: 12px;
  color: var(--lab-text-secondary, #8c8c8c);
  margin: 6px 0 8px;
}
.msg-body {
  margin: 0;
  font-size: 14px;
  color: var(--lab-text, #262626);
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}
.reply-box {
  margin-top: 12px;
  padding: 10px 12px;
  border-radius: 8px;
  background: var(--lab-blue-soft, rgba(91, 143, 249, 0.1));
  border-left: 3px solid var(--lab-blue, #5b8ff9);
}
.reply-label {
  font-size: 12px;
  color: #4e5969;
  margin-bottom: 6px;
}
.reply-text {
  margin: 0;
  font-size: 13px;
  color: #1f2937;
  line-height: 1.55;
  white-space: pre-wrap;
}
.time {
  margin-top: 10px;
  font-size: 12px;
  color: #9ca3af;
}
</style>
