<template>
  <div class="page">
    <div v-if="isAdminPage" class="top">
      <router-link to="/admin" class="back">返回教务概览</router-link>
    </div>

    <div class="layout">
      <section class="settings-panel">
        <div class="panel-head">
          <div>
            <span class="kicker">Personal Settings</span>
            <h1>个性化设置</h1>
          </div>
          <el-button :loading="loading" type="primary" @click="save">保存设置</el-button>
        </div>

        <el-form label-position="top" class="form">
          <div class="section">
            <div class="section-title">界面偏好</div>
            <el-form-item label="主题色">
              <div class="color-row">
                <el-color-picker v-model="color" :show-alpha="false" :predefine="predefine" />
                <button
                  v-for="c in predefine"
                  :key="c"
                  type="button"
                  class="swatch"
                  :class="{ active: normalizeHex(color) === c }"
                  :style="{ backgroundColor: c }"
                  :aria-label="`选择主题色 ${c}`"
                  @click="color = c"
                />
              </div>
            </el-form-item>

            <el-form-item label="紧凑布局">
              <div class="inline-control">
                <el-switch v-model="compact" />
                <span>减少列表和表单留白，适合高频管理场景。</span>
              </div>
            </el-form-item>
          </div>

          <div class="section">
            <div class="section-title">工作流偏好</div>
            <el-form-item label="题库默认视图">
              <el-segmented
                v-model="questionBankView"
                :options="[
                  { label: '分组视图', value: 'grouped' },
                  { label: '列表视图', value: 'list' }
                ]"
              />
            </el-form-item>

            <el-form-item label="智能助手">
              <div class="inline-control">
                <el-switch v-model="assistantAutoOpen" />
                <span>登录后首次进入页面时自动展开助手抽屉。</span>
              </div>
            </el-form-item>
          </div>

          <div class="actions">
            <el-button plain @click="resetDefault">恢复默认</el-button>
            <el-button :loading="loading" type="primary" @click="save">保存设置</el-button>
          </div>
        </el-form>
      </section>

      <aside class="preview-panel">
        <span class="kicker">Preview</span>
        <h2>效果预览</h2>
        <div class="preview-card" :class="{ compact: compact }">
          <div class="preview-top">
            <span class="preview-dot" :style="{ backgroundColor: normalizeHex(color) }" />
            <strong>考试平台</strong>
            <span>{{ questionBankView === 'grouped' ? '分组题库' : '列表题库' }}</span>
          </div>
          <div class="preview-lines">
            <span />
            <span />
            <span />
          </div>
          <button type="button" class="preview-button" :style="{ backgroundColor: normalizeHex(color) }">
            主要操作
          </button>
        </div>
        <p class="preview-note">主题色会影响按钮、导航和重点信息；工作流偏好会在对应页面读取。</p>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../../api/http'
import { useUserStore } from '../../stores/user'
import {
  DEFAULT_SETTINGS,
  applyThemeFromSettings,
  buildSettingsJson,
  parseSettingsJson
} from '../../composables/useTheme'

const route = useRoute()
const store = useUserStore()
const isAdminPage = computed(() => route.path.startsWith('/admin'))
const loading = ref(false)
const color = ref(DEFAULT_SETTINGS.primaryColor)
const compact = ref(DEFAULT_SETTINGS.compact)
const questionBankView = ref(DEFAULT_SETTINGS.questionBankView)
const assistantAutoOpen = ref(DEFAULT_SETTINGS.assistantAutoOpen)
const predefine = ['#0f8b8d', '#165dff', '#4a90e2', '#13c2c2', '#50c878', '#722ed1', '#e64340', '#f5a623']

function parseSettings() {
  const settings = parseSettingsJson(store.profile?.settingsJson)
  color.value = settings.primaryColor
  compact.value = settings.compact
  questionBankView.value = settings.questionBankView
  assistantAutoOpen.value = settings.assistantAutoOpen
}

function normalizeHex(c) {
  if (!c || typeof c !== 'string') return DEFAULT_SETTINGS.primaryColor
  const s = c.trim()
  if (s.startsWith('#') && (s.length === 7 || s.length === 9)) {
    return s.length === 9 ? s.slice(0, 7) : s
  }
  return DEFAULT_SETTINGS.primaryColor
}

function resetDefault() {
  color.value = DEFAULT_SETTINGS.primaryColor
  compact.value = DEFAULT_SETTINGS.compact
  questionBankView.value = DEFAULT_SETTINGS.questionBankView
  assistantAutoOpen.value = DEFAULT_SETTINGS.assistantAutoOpen
}

async function load() {
  try {
    await store.refreshProfile()
    parseSettings()
  } catch (e) {
    ElMessage.error(e.message)
  }
}

async function save() {
  loading.value = true
  try {
    const previous = parseSettingsJson(store.profile?.settingsJson)
    const settingsJson = buildSettingsJson({
      ...previous,
      primaryColor: normalizeHex(color.value),
      compact: compact.value,
      questionBankView: questionBankView.value,
      assistantAutoOpen: assistantAutoOpen.value
    })
    const { data } = await http.put('/user/profile', { settingsJson })
    store.patchProfile({ settingsJson: data.settingsJson })
    applyThemeFromSettings(data.settingsJson)
    ElMessage.success('个性化设置已保存')
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.page {
  max-width: 1040px;
  margin: 0 auto;
}
.top {
  margin-bottom: 12px;
}
.back {
  color: var(--campus-primary);
  font-size: 14px;
  text-decoration: none;
}
.layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 18px;
}
.settings-panel,
.preview-panel {
  border: 1px solid var(--lab-border, rgba(15, 23, 42, 0.08));
  border-radius: 8px;
  background: var(--lab-surface, #fff);
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
}
.settings-panel {
  padding: 22px;
}
.preview-panel {
  align-self: start;
  padding: 20px;
}
.panel-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}
.kicker {
  color: var(--campus-primary);
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0;
  text-transform: uppercase;
}
h1,
h2 {
  margin: 6px 0 0;
  color: #1d2129;
  letter-spacing: 0;
}
h1 {
  font-size: 24px;
}
h2 {
  font-size: 18px;
}
.section {
  padding: 16px 0;
  border-top: 1px solid #edf0f5;
}
.section-title {
  margin-bottom: 14px;
  color: #4e5969;
  font-size: 13px;
  font-weight: 700;
}
.color-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.swatch {
  width: 28px;
  height: 28px;
  border: 2px solid transparent;
  border-radius: 50%;
  cursor: pointer;
}
.swatch.active {
  border-color: #111827;
  box-shadow: 0 0 0 3px rgba(17, 24, 39, 0.08);
}
.inline-control {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #6b7280;
  font-size: 13px;
}
.actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding-top: 8px;
}
.preview-card {
  margin-top: 16px;
  padding: 18px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #f8fafc;
}
.preview-card.compact {
  padding: 12px;
}
.preview-top {
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 8px;
  color: #1d2129;
  font-size: 13px;
}
.preview-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}
.preview-lines {
  display: grid;
  gap: 8px;
  margin: 16px 0;
}
.preview-lines span {
  display: block;
  height: 10px;
  border-radius: 6px;
  background: #dfe5ef;
}
.preview-lines span:nth-child(2) {
  width: 76%;
}
.preview-lines span:nth-child(3) {
  width: 54%;
}
.preview-button {
  width: 100%;
  height: 34px;
  border: 0;
  border-radius: 6px;
  color: #fff;
  font-weight: 700;
}
.preview-note {
  margin: 12px 0 0;
  color: #86909c;
  font-size: 12px;
  line-height: 1.6;
}
@media (max-width: 860px) {
  .layout {
    grid-template-columns: 1fr;
  }
  .panel-head {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>
