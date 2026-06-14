<template>
  <div class="page">
    <div v-if="isAdminPage" class="top">
      <router-link to="/admin" class="back">← 返回教务概览</router-link>
    </div>
    <el-card shadow="never" class="card">
      <template #header><span class="h">个人资料</span></template>
      <el-form label-width="100px" class="form" @submit.prevent="save">
        <el-form-item label="账号">
          <el-input :model-value="store.profile?.username" disabled />
        </el-form-item>
        <el-form-item v-if="store.isStudent" label="班级">
          <el-input :model-value="store.profile?.className || '—'" disabled />
        </el-form-item>
        <el-form-item label="学院">
          <el-input :model-value="store.profile?.college || '—'" disabled />
        </el-form-item>
        <el-form-item label="显示名" required>
          <el-input v-model="form.displayName" maxlength="100" show-word-limit placeholder="在系统中显示的名字" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" maxlength="120" placeholder="用于邮件通知" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" maxlength="40" placeholder="用于短信通知" />
        </el-form-item>
        <el-form-item label="个性签名">
          <el-input
            v-model="form.personalNote"
            type="textarea"
            :rows="3"
            maxlength="300"
            show-word-limit
            placeholder="一句话介绍自己"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" native-type="submit">保存</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="card password-card">
      <template #header><span class="h">修改密码</span></template>
      <el-form label-width="100px" class="form" @submit.prevent="savePassword">
        <el-form-item label="当前密码" required>
          <el-input v-model="passwordForm.currentPassword" type="password" show-password autocomplete="current-password" />
        </el-form-item>
        <el-form-item label="新密码" required>
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            show-password
            autocomplete="new-password"
            placeholder="至少 8 位"
          />
        </el-form-item>
        <el-form-item label="确认新密码" required>
          <el-input v-model="passwordForm.newPassword2" type="password" show-password autocomplete="new-password" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="passwordLoading" native-type="submit">更新密码</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../../api/http'
import { useUserStore } from '../../stores/user'

const route = useRoute()
const store = useUserStore()
const isAdminPage = computed(() => route.path.startsWith('/admin'))
const loading = ref(false)
const passwordLoading = ref(false)
const form = reactive({
  displayName: '',
  email: '',
  phone: '',
  personalNote: ''
})
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  newPassword2: ''
})

async function load() {
  try {
    await store.refreshProfile()
    form.displayName = store.profile?.displayName || ''
    form.email = store.profile?.email || ''
    form.phone = store.profile?.phone || ''
    form.personalNote = store.profile?.personalNote || ''
  } catch (e) {
    ElMessage.error(e.message)
  }
}

async function save() {
  if (!form.displayName.trim()) {
    ElMessage.warning('请填写显示名')
    return
  }
  loading.value = true
  try {
    const { data } = await http.put('/user/profile', {
      displayName: form.displayName.trim(),
      email: form.email.trim(),
      phone: form.phone.trim(),
      personalNote: form.personalNote ?? ''
    })
    store.patchProfile({
      displayName: data.displayName,
      email: data.email,
      phone: data.phone,
      personalNote: data.personalNote
    })
    ElMessage.success('已保存')
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}

async function savePassword() {
  if (!passwordForm.currentPassword) {
    ElMessage.warning('请输入当前密码')
    return
  }
  if (passwordForm.newPassword.length < 8) {
    ElMessage.warning('新密码至少 8 位')
    return
  }
  if (passwordForm.newPassword !== passwordForm.newPassword2) {
    ElMessage.warning('两次新密码不一致')
    return
  }
  passwordLoading.value = true
  try {
    await http.post('/user/password', {
      currentPassword: passwordForm.currentPassword,
      newPassword: passwordForm.newPassword
    })
    passwordForm.currentPassword = ''
    passwordForm.newPassword = ''
    passwordForm.newPassword2 = ''
    ElMessage.success('密码已更新')
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    passwordLoading.value = false
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
.form {
  max-width: 520px;
}
.password-card {
  margin-top: 16px;
}
</style>
