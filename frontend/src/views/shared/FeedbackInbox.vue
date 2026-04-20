<template>
  <div class="inbox">
    <el-card shadow="never" class="card">
      <template #header>
        <div class="head">
          <span class="h">学生留言</span>
          <el-tag type="info" size="small">共 {{ rows.length }} 条</el-tag>
        </div>
      </template>
      <p class="tip">学生可反馈题目错误、解析问题或考试相关咨询，请在此处回复。</p>
      <el-table v-loading="loading" :data="rows" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="studentName" label="学生" width="100" />
        <el-table-column prop="studentUsername" label="学号" width="120" />
        <el-table-column prop="subject" label="主题" min-width="140" show-overflow-tooltip />
        <el-table-column prop="course" label="课程" width="120" />
        <el-table-column prop="className" label="班级" width="110" />
        <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="90" />
        <el-table-column prop="replyContent" label="回复" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openReply(row)">回复</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dlg" title="回复留言" width="520px" destroy-on-close>
      <p v-if="current" class="preview">{{ current.content }}</p>
      <el-input v-model="replyText" type="textarea" :rows="5" maxlength="2000" show-word-limit placeholder="请输入回复内容" />
      <template #footer>
        <el-button @click="dlg = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="doReply">发送回复</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../../api/http'

const loading = ref(false)
const saving = ref(false)
const rows = ref([])
const dlg = ref(false)
const current = ref(null)
const replyText = ref('')

async function load() {
  loading.value = true
  try {
    const { data } = await http.get('/staff/feedback')
    rows.value = data || []
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}

function openReply(row) {
  current.value = row
  replyText.value = row.replyContent || ''
  dlg.value = true
}

async function doReply() {
  if (!current.value) return
  const t = replyText.value?.trim()
  if (!t) {
    ElMessage.warning('请输入回复内容')
    return
  }
  saving.value = true
  try {
    await http.post(`/staff/feedback/${current.value.id}/reply`, { reply: t })
    ElMessage.success('已发送')
    dlg.value = false
    await load()
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.inbox {
  max-width: 1280px;
  margin: 0 auto;
}
.card {
  border-radius: 12px;
}
.head {
  display: flex;
  align-items: center;
  gap: 12px;
}
.h {
  font-weight: 700;
  font-size: 16px;
}
.tip {
  margin: 0 0 12px;
  font-size: 13px;
  color: #6b7280;
}
.preview {
  margin: 0 0 12px;
  padding: 10px;
  background: #f3f4f6;
  border-radius: 8px;
  font-size: 13px;
  color: #374151;
  max-height: 120px;
  overflow-y: auto;
}
</style>
