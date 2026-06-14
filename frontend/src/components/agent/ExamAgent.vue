<template>
  <teleport to="body">
    <div v-if="visible" class="agent-root">
      <transition name="fade">
        <el-button
          v-show="!drawerOpen"
          class="fab"
          type="primary"
          round
          size="large"
          @click="drawerOpen = true"
        >
          <span class="fab-inner">
            <span class="fab-ico" aria-hidden="true">AI</span>
            智能助手
          </span>
        </el-button>
      </transition>

      <el-drawer
        v-model="drawerOpen"
        direction="rtl"
        size="420px"
        class="agent-drawer"
        @opened="scrollBottom"
      >
        <template #header>
          <div class="drawer-head">
            <div class="dh-main">
              <span class="dh-title">考试智能助手</span>
              <el-tag v-if="agentStatus.enabled && agentStatus.configured" size="small" type="success" effect="plain">
                已接入 {{ agentStatus.model || '大模型' }}
              </el-tag>
              <el-tag v-else-if="agentStatus.enabled" size="small" type="info" effect="plain">离线规则</el-tag>
              <el-tag v-else size="small" type="warning" effect="plain">已关闭</el-tag>
              <el-tag v-if="lastFromLlm === true" size="small" type="success" effect="plain">大模型</el-tag>
              <el-tag v-else-if="lastFromLlm === false" size="small" type="info" effect="plain">离线规则</el-tag>
            </div>
            <el-button link type="primary" size="small" @click="clearMessages">清空</el-button>
          </div>
        </template>

        <div class="chat-body" ref="chatBodyRef">
          <p class="intro">
            我可以协助说明考试流程、切屏/全屏规则、成绩与注册等问题。管理员可在服务器配置大模型
            API 以获得更灵活的回答。
          </p>
          <div class="quick-grid">
            <button
              v-for="q in quickPrompts"
              :key="q.text"
              type="button"
              :disabled="loading || !agentStatus.enabled"
              @click="askQuick(q.text)"
            >
              {{ q.label }}
            </button>
          </div>
          <div
            v-for="(m, i) in messages"
            :key="i"
            class="msg"
            :class="m.role === 'user' ? 'msg-user' : 'msg-bot'"
          >
            <div class="bubble">{{ m.content }}</div>
          </div>
          <div v-if="loading" class="msg msg-bot">
            <div class="bubble typing">正在思考…</div>
          </div>
        </div>

        <div class="chat-input">
          <el-input
            v-model="input"
            type="textarea"
            :rows="2"
            :placeholder="inputPlaceholder"
            :disabled="loading || !agentStatus.enabled"
            @keydown.enter.exact.prevent="send"
          />
          <el-button
            type="primary"
            class="send-btn"
            :disabled="!agentStatus.enabled"
            :loading="loading"
            @click="send"
          >
            发送
          </el-button>
        </div>
      </el-drawer>
    </div>
  </teleport>
</template>

<script setup>
import { computed, nextTick, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../../api/http'
import { useUserStore } from '../../stores/user'
import { parseSettingsJson } from '../../composables/useTheme'

const store = useUserStore()
const visible = computed(() => !!store.token)
const settings = computed(() => parseSettingsJson(store.profile?.settingsJson))

const drawerOpen = ref(false)
const messages = ref([])
const input = ref('')
const loading = ref(false)
const chatBodyRef = ref(null)
const lastFromLlm = ref(null)
const agentStatus = ref({
  enabled: true,
  configured: false,
  model: ''
})
let autoOpened = false

const quickPrompts = computed(() => {
  if (store.isAdmin) {
    return [
      { label: '如何删除异常账号', text: '教务如何删除异常账号？需要注意什么？' },
      { label: '如何导入题库', text: '怎样批量导入计算机题库并检查数量？' },
      { label: '如何接入大模型', text: '服务器如何配置大模型 API？' }
    ]
  }
  if (store.isTeacher) {
    return [
      { label: '题库筛选', text: '题库里如何按知识点和难度筛选题目？' },
      { label: '发布考试', text: '教师发布考试前需要检查哪些设置？' },
      { label: '阅卷说明', text: '主观题阅卷和成绩分析怎么操作？' }
    ]
  }
  return [
    { label: '参加考试', text: '学生如何参加考试并提交试卷？' },
    { label: '切屏规则', text: '考试时切屏或退出全屏有什么影响？' },
    { label: '查看成绩', text: '交卷后在哪里查看成绩和错题？' }
  ]
})

const inputPlaceholder = computed(() =>
  agentStatus.value.enabled
    ? '输入你的问题，Enter 发送（Shift+Enter 换行）'
    : '智能助手已关闭'
)

async function loadStatus() {
  if (!store.token) return
  try {
    const { data } = await http.get('/agent/status')
    agentStatus.value = {
      enabled: data.enabled === true,
      configured: data.configured === true,
      model: data.model || ''
    }
  } catch {
    agentStatus.value = { enabled: true, configured: false, model: '' }
  }
}

async function send() {
  const text = input.value?.trim()
  if (!text || loading.value) return
  if (!agentStatus.value.enabled) {
    ElMessage.warning('智能助手当前已关闭')
    return
  }
  messages.value.push({ role: 'user', content: text })
  input.value = ''
  loading.value = true
  lastFromLlm.value = null
  await nextTick()
  scrollBottom()
  try {
    const { data } = await http.post('/agent/chat', { message: text })
    lastFromLlm.value = data.fromLlm
    messages.value.push({ role: 'assistant', content: data.reply })
  } catch (e) {
    ElMessage.error(e.message || '发送失败')
    messages.value.push({ role: 'assistant', content: '请求失败，请稍后重试。' })
  } finally {
    loading.value = false
    await nextTick()
    scrollBottom()
  }
}

function askQuick(text) {
  input.value = text
  send()
}

function clearMessages() {
  messages.value = []
  lastFromLlm.value = null
}

function scrollBottom() {
  const el = chatBodyRef.value
  if (el) el.scrollTop = el.scrollHeight
}

watch(
  () => messages.value.length,
  () => nextTick(() => scrollBottom())
)

watch(drawerOpen, (v) => {
  if (v) loadStatus()
})

watch(
  visible,
  (v) => {
    if (!v) {
      drawerOpen.value = false
      autoOpened = false
      return
    }
    loadStatus()
    if (settings.value.assistantAutoOpen && !autoOpened) {
      drawerOpen.value = true
      autoOpened = true
    }
  },
  { immediate: true }
)
</script>

<style scoped>
.fab {
  position: fixed;
  right: 22px;
  bottom: 22px;
  z-index: 3100;
  box-shadow: 0 10px 28px rgba(59, 108, 255, 0.35);
  padding: 12px 20px !important;
  height: auto !important;
  background: linear-gradient(125deg, #3b6cff 0%, #7c3aed 100%) !important;
  border: none !important;
}
.fab-inner {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}
.fab-ico {
  display: inline-grid;
  place-items: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.18);
  font-size: 12px;
  font-weight: 900;
}
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
.drawer-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  width: 100%;
}
.dh-main {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.dh-title {
  font-weight: 700;
  font-size: 16px;
}
.chat-body {
  flex: 1;
  overflow-y: auto;
  max-height: calc(100vh - 220px);
  padding-right: 4px;
}
.intro {
  margin: 0 0 12px;
  font-size: 12px;
  line-height: 1.55;
  color: #6b7280;
}
.quick-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 8px;
  margin-bottom: 14px;
}
.quick-grid button {
  width: 100%;
  padding: 9px 10px;
  border: 1px solid #d9e1ec;
  border-radius: 8px;
  color: #1f2937;
  background: #fff;
  text-align: left;
  cursor: pointer;
  transition: border-color 0.18s ease, color 0.18s ease;
}
.quick-grid button:hover:not(:disabled) {
  border-color: var(--campus-primary, #4a90e2);
  color: var(--campus-primary, #4a90e2);
}
.quick-grid button:disabled {
  cursor: not-allowed;
  opacity: 0.55;
}
.msg {
  margin-bottom: 12px;
  display: flex;
}
.msg-user {
  justify-content: flex-end;
}
.msg-bot {
  justify-content: flex-start;
}
.bubble {
  max-width: 92%;
  padding: 10px 12px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.55;
  white-space: pre-wrap;
  word-break: break-word;
}
.msg-user .bubble {
  background: linear-gradient(125deg, rgba(59, 108, 255, 0.15), rgba(124, 58, 237, 0.12));
  color: #1f2937;
}
.msg-bot .bubble {
  background: #f3f4f6;
  color: #374151;
}
.typing {
  color: #9ca3af;
  font-style: italic;
}
.chat-input {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #e5e7eb;
}
.send-btn {
  margin-top: 10px;
  width: 100%;
}
</style>

<style>
/* 抽屉 body 用 flex 撑满输入区在底部 */
.agent-drawer .el-drawer__body {
  display: flex;
  flex-direction: column;
  padding: 12px 16px 16px;
}
</style>
