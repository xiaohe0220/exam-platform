<template>
  <div class="page">
    <div v-if="isAdminPage" class="top">
      <router-link to="/admin" class="back">← 返回教务概览</router-link>
    </div>
    <el-card shadow="never" class="card">
      <template #header><span class="h">个性化设置</span></template>
      <el-form label-width="120px" class="form">
        <el-form-item label="主题色">
          <el-color-picker v-model="color" :show-alpha="false" :predefine="predefine" />
          <span class="hint">用于按钮、导航与强调色</span>
        </el-form-item>
        <el-form-item label="紧凑布局">
          <el-switch v-model="compact" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="save">保存设置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../../api/http'
import { useUserStore } from '../../stores/user'
import { applyThemeFromSettings, buildSettingsJson } from '../../composables/useTheme'

const route = useRoute()
const store = useUserStore()
const isAdminPage = computed(() => route.path.startsWith('/admin'))
const loading = ref(false)
const color = ref('#e64340')
const compact = ref(false)
const predefine = ['#e64340', '#5b8ff9', '#4a90e2', '#722ed1', '#13c2c2', '#50c878', '#f5a623', '#eb2f96']

function parseSettings() {
  try {
    const o = store.profile?.settingsJson ? JSON.parse(store.profile.settingsJson) : {}
    if (o.primaryColor) color.value = o.primaryColor
    compact.value = Boolean(o.compact)
  } catch {
    /* ignore */
  }
}

function normalizeHex(c) {
  if (!c || typeof c !== 'string') return '#e64340'
  const s = c.trim()
  if (s.startsWith('#') && (s.length === 7 || s.length === 9)) {
    return s.length === 9 ? s.slice(0, 7) : s
  }
  return '#165dff'
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
    const settingsJson = buildSettingsJson({
      primaryColor: normalizeHex(color.value),
      compact: compact.value
    })
    const { data } = await http.put('/user/profile', { settingsJson })
    store.patchProfile({ settingsJson: data.settingsJson })
    applyThemeFromSettings(data.settingsJson)
    ElMessage.success('已保存')
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
  max-width: 640px;
  margin: 0 auto;
}
.top {
  margin-bottom: 12px;
}
.back {
  font-size: 14px;
  color: var(--campus-primary);
  text-decoration: none;
}
.card {
  border-radius: var(--lab-radius, 12px);
  border: 1px solid var(--lab-border, rgba(15, 23, 42, 0.08));
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
  background: var(--lab-surface, #fff);
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
