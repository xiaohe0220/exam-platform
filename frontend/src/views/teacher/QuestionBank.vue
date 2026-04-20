<template>
  <el-card shadow="never" class="card">
    <template #header>
      <div class="head">
        <span>题库</span>
        <el-button type="primary" @click="openEdit()">新建题目</el-button>
      </div>
    </template>
    <el-table :data="rows" stripe border style="width: 100%">
      <el-table-column prop="type" label="题型" width="120" />
      <el-table-column prop="title" label="题干" min-width="220" show-overflow-tooltip />
      <el-table-column prop="chapter" label="章节" width="140" />
      <el-table-column prop="difficulty" label="难度" width="80" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="dlg" :title="editing?.id ? '编辑题目' : '新建题目'" width="640px" destroy-on-close>
    <el-form :model="form" label-width="100px">
      <el-form-item label="题型">
        <el-select v-model="form.type" style="width: 100%">
          <el-option label="单选题" value="SINGLE_CHOICE" />
          <el-option label="多选题" value="MULTIPLE_CHOICE" />
          <el-option label="判断题" value="TRUE_FALSE" />
          <el-option label="填空题" value="FILL_BLANK" />
          <el-option label="简答题" value="SHORT_ANSWER" />
        </el-select>
      </el-form-item>
      <el-form-item label="题干">
        <el-input v-model="form.title" type="textarea" :rows="2" />
      </el-form-item>
      <el-form-item v-if="hasOptions" label="选项(JSON)">
        <el-input v-model="form.optionsJson" type="textarea" :rows="4" placeholder='如 ["A内容","B内容"]' />
      </el-form-item>
      <el-form-item v-if="form.type !== 'SHORT_ANSWER'" label="标准答案">
        <el-input v-model="form.correctAnswerJson" placeholder='单选 "A" ；多选 ["A","C"] ；判断 true / false ；填空 "文本"' />
      </el-form-item>
      <el-form-item label="难度(1-5)">
        <el-input-number v-model="form.difficulty" :min="1" :max="5" />
      </el-form-item>
      <el-form-item label="章节">
        <el-input v-model="form.chapter" />
      </el-form-item>
      <el-form-item label="知识点">
        <el-input v-model="form.knowledgePoint" />
      </el-form-item>
      <el-form-item label="答案解析">
        <el-input
          v-model="form.answerAnalysis"
          type="textarea"
          :rows="4"
          placeholder="学生错题本中展示；支持换行，也可粘贴简单 HTML"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dlg = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="save">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../../api/http'

const rows = ref([])
const dlg = ref(false)
const saving = ref(false)
const editing = ref(null)
const form = reactive({
  type: 'SINGLE_CHOICE',
  title: '',
  content: '',
  optionsJson: '[]',
  correctAnswerJson: '',
  difficulty: 3,
  chapter: '',
  knowledgePoint: '',
  answerAnalysis: ''
})

const hasOptions = computed(() =>
  ['SINGLE_CHOICE', 'MULTIPLE_CHOICE', 'TRUE_FALSE'].includes(form.type)
)

function reset() {
  form.type = 'SINGLE_CHOICE'
  form.title = ''
  form.content = ''
  form.optionsJson = '["","","",""]'
  form.correctAnswerJson = '"A"'
  form.difficulty = 3
  form.chapter = ''
  form.knowledgePoint = ''
  form.answerAnalysis = ''
}

async function load() {
  const { data } = await http.get('/teacher/questions')
  rows.value = data
}

function openEdit(row) {
  editing.value = row || null
  if (row) {
    form.type = row.type
    form.title = row.title
    form.content = row.content || ''
    form.optionsJson = row.optionsJson || '[]'
    form.correctAnswerJson = row.correctAnswerJson || ''
    form.difficulty = row.difficulty
    form.chapter = row.chapter || ''
    form.knowledgePoint = row.knowledgePoint || ''
    form.answerAnalysis = row.answerAnalysis || ''
  } else {
    reset()
  }
  dlg.value = true
}

async function save() {
  saving.value = true
  try {
    const body = {
      type: form.type,
      title: form.title,
      content: form.content,
      optionsJson: hasOptions.value ? form.optionsJson : null,
      correctAnswerJson: form.type === 'SHORT_ANSWER' ? null : form.correctAnswerJson,
      difficulty: form.difficulty,
      chapter: form.chapter,
      knowledgePoint: form.knowledgePoint,
      answerAnalysis: form.answerAnalysis || null
    }
    if (editing.value?.id) {
      await http.put(`/teacher/questions/${editing.value.id}`, body)
      ElMessage.success('已保存')
    } else {
      await http.post('/teacher/questions', body)
      ElMessage.success('已创建')
    }
    dlg.value = false
    await load()
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    saving.value = false
  }
}

async function remove(row) {
  await ElMessageBox.confirm('确认删除该题？', '提示', { type: 'warning' })
  await http.delete(`/teacher/questions/${row.id}`)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>

<style scoped>
.card {
  border-radius: 8px;
}
.head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
