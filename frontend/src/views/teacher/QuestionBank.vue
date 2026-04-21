<template>
  <el-card shadow="never" class="card">
    <template #header>
      <div class="head">
        <span>题库</span>
        <el-button type="primary" @click="openEdit()">新建题目</el-button>
      </div>
    </template>

    <el-space wrap class="toolbar">
      <el-input
        v-model="keyword"
        placeholder="搜索题干 / 知识点 / 文件夹"
        clearable
        style="width: 280px"
      />
      <el-select v-model="folderFilter" placeholder="按文件夹筛选" clearable style="width: 220px">
        <el-option v-for="f in folderOptions" :key="f" :label="f" :value="f" />
      </el-select>
      <el-select v-model="typeFilter" placeholder="按题型筛选" clearable style="width: 180px">
        <el-option label="单选题" value="SINGLE_CHOICE" />
        <el-option label="多选题" value="MULTIPLE_CHOICE" />
        <el-option label="判断题" value="TRUE_FALSE" />
        <el-option label="填空题" value="FILL_BLANK" />
        <el-option label="简答题" value="SHORT_ANSWER" />
      </el-select>
      <el-radio-group v-model="viewMode">
        <el-radio-button label="grouped">分组视图</el-radio-button>
        <el-radio-button label="list">列表视图</el-radio-button>
      </el-radio-group>
      <el-button @click="load">刷新</el-button>
    </el-space>

    <div v-if="viewMode === 'grouped'">
      <el-collapse v-model="activeGroups" class="grouped">
        <el-collapse-item
          v-for="g in groupedRows"
          :key="g.key"
          :name="g.key"
        >
          <template #title>
            <span>{{ g.folder }} / {{ typeLabel(g.type) }}（{{ g.items.length }}题）</span>
          </template>
          <el-table :data="g.items" stripe border style="width: 100%">
            <el-table-column prop="title" label="题干" min-width="260" show-overflow-tooltip />
            <el-table-column prop="knowledgePoint" label="知识点" width="180" show-overflow-tooltip />
            <el-table-column prop="difficulty" label="难度" width="80" />
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
                <el-button link type="danger" @click="remove(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-collapse-item>
      </el-collapse>
    </div>

    <el-table v-else :data="filteredRows" stripe border style="width: 100%">
      <el-table-column prop="type" label="题型" width="120">
        <template #default="{ row }">{{ typeLabel(row.type) }}</template>
      </el-table-column>
      <el-table-column prop="title" label="题干" min-width="220" show-overflow-tooltip />
      <el-table-column prop="chapter" label="文件夹" width="180" show-overflow-tooltip />
      <el-table-column prop="knowledgePoint" label="知识点" width="160" show-overflow-tooltip />
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
      <el-form-item label="题目文件夹">
        <el-select
          v-model="form.chapter"
          filterable
          allow-create
          default-first-option
          clearable
          style="width: 100%"
          placeholder="可选择已有文件夹，也可直接输入新文件夹名"
        >
          <el-option v-for="f in folderOptions" :key="f" :label="f" :value="f" />
        </el-select>
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
const keyword = ref('')
const folderFilter = ref('')
const typeFilter = ref('')
const viewMode = ref('grouped')
const activeGroups = ref([])
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

const folderOptions = computed(() =>
  [...new Set(rows.value.map(r => (r.chapter || '').trim()).filter(Boolean))]
)

const filteredRows = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  return rows.value.filter(r => {
    if (folderFilter.value && (r.chapter || '') !== folderFilter.value) return false
    if (typeFilter.value && r.type !== typeFilter.value) return false
    if (!kw) return true
    return [r.title, r.chapter, r.knowledgePoint]
      .filter(Boolean)
      .some(v => String(v).toLowerCase().includes(kw))
  })
})

const groupedRows = computed(() => {
  const map = new Map()
  for (const r of filteredRows.value) {
    const folder = (r.chapter || '未分文件夹').trim() || '未分文件夹'
    const key = `${folder}__${r.type}`
    if (!map.has(key)) map.set(key, { key, folder, type: r.type, items: [] })
    map.get(key).items.push(r)
  }
  return [...map.values()].sort((a, b) => a.folder.localeCompare(b.folder) || a.type.localeCompare(b.type))
})

function typeLabel(t) {
  return {
    SINGLE_CHOICE: '单选题',
    MULTIPLE_CHOICE: '多选题',
    TRUE_FALSE: '判断题',
    FILL_BLANK: '填空题',
    SHORT_ANSWER: '简答题'
  }[t] || t
}

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
  activeGroups.value = groupedRows.value.slice(0, 8).map(g => g.key)
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
.toolbar {
  margin-bottom: 12px;
}
.grouped :deep(.el-collapse-item__header) {
  font-weight: 600;
}
</style>
