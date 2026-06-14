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
            <span class="fab-ico" aria-hidden="true">✨</span>
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
            <span class="dh-title">考试智能助手</span>
            <el-tag v-if="agentStatus.enabled && agentStatus.configured" size="small" type="success" effect="plain">
              已接入 {{ agentStatus.model || '大模型' }}
            </el-tag>
            <el-tag v-else-if="agentStatus.enabled" size="small" type="info" effect="plain">离线规则</el-tag>
            <el-tag v-else size="small" type="warning" effect="plain">已关闭</el-tag>
            <el-tag v-if="lastFromLlm === true" size="small" type="success" effect="plain">大模型</el-tag>
            <el-tag v-else-if="lastFromLlm === false" size="small" type="info" effect="plain">离线规则</el-tag>
          </div>
        </template>

        <div class="chat-body" ref="chatBodyRef">
          <p class="intro">
            我可以协助说明考试流程、切屏/全屏规则、成绩与注册等问题。管理员可在服务器配置大模型
            API 以获得更灵活的回答。
          </p>
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
            placeholder="输入你的问题，Enter 发送（Shift+Enter 换行）"
            :disabled="loading"
            @keydown.enter.exact.prevent="send"
          />
          <el-button type="primary" class="send-btn" :loading="loading" @click="send">发送</el-button>
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

const store = useUserStore()
const visible = computed(() => !!store.token)

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
  font-size: 18px;
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
  gap: 10px;
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
