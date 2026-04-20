import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import http from '../api/http'
import { applyThemeFromSettings } from '../composables/useTheme'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('exam_token') || '')
  const profile = ref(null)

  const isTeacher = computed(() =>
    ['TEACHER', 'ADMIN', 'COLLEGE_ADMIN'].includes(profile.value?.role)
  )
  const isStudent = computed(() => profile.value?.role === 'STUDENT')
  const isAdmin = computed(() =>
    ['ADMIN', 'COLLEGE_ADMIN'].includes(profile.value?.role)
  )

  function persistProfile() {
    if (profile.value) {
      localStorage.setItem('exam_profile', JSON.stringify(profile.value))
      applyThemeFromSettings(profile.value.settingsJson)
    }
  }

  function applySession(data) {
    token.value = data.token
    profile.value = {
      userId: data.userId,
      id: data.userId,
      username: data.username,
      displayName: data.displayName,
      role: data.role,
      className: data.className,
      college: data.college,
      personalNote: data.personalNote,
      settingsJson: data.settingsJson
    }
    localStorage.setItem('exam_token', data.token)
    persistProfile()
    return data
  }

  async function login(username, password) {
    const { data } = await http.post('/auth/login', { username, password })
    return applySession(data)
  }

  async function register(payload) {
    const { data } = await http.post('/auth/register', payload)
    return applySession(data)
  }

  async function resetPassword(username, newPassword) {
    await http.post('/auth/reset-password', { username, newPassword })
  }

  async function refreshProfile() {
    if (!token.value) return null
    const { data } = await http.get('/user/profile')
    profile.value = {
      ...profile.value,
      userId: data.id,
      username: data.username,
      displayName: data.displayName,
      role: data.role,
      className: data.className,
      college: data.college,
      personalNote: data.personalNote,
      settingsJson: data.settingsJson
    }
    persistProfile()
    return profile.value
  }

  function patchProfile(partial) {
    profile.value = { ...profile.value, ...partial }
    persistProfile()
  }

  function loadCache() {
    const raw = localStorage.getItem('exam_profile')
    if (raw) {
      try {
        profile.value = JSON.parse(raw)
        applyThemeFromSettings(profile.value?.settingsJson)
      } catch {
        profile.value = null
      }
    }
  }

  function logout() {
    token.value = ''
    profile.value = null
    localStorage.removeItem('exam_token')
    localStorage.removeItem('exam_profile')
  }

  return {
    token,
    profile,
    isTeacher,
    isStudent,
    isAdmin,
    login,
    register,
    resetPassword,
    logout,
    loadCache,
    refreshProfile,
    patchProfile,
    persistProfile
  }
})
