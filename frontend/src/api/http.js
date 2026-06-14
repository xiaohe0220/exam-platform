import axios from 'axios'

const http = axios.create({
  baseURL: '/api',
  timeout: 60000
})

http.interceptors.request.use((config) => {
  const t = localStorage.getItem('exam_token')
  if (t) {
    config.headers.Authorization = `Bearer ${t}`
  }
  return config
})

http.interceptors.response.use(
  (r) => r,
  (err) => {
    const status = err.response?.status
    const d = err.response?.data
    let msg =
      d?.error ||
      d?.message ||
      (Array.isArray(d?.errors) && d.errors[0]?.defaultMessage) ||
      err.message ||
      '请求失败'

    if (status === 401) {
      msg = '登录已过期或未登录，请重新登录后再试'
    }
    if (status === 429) {
      msg = d?.error || '请求过于频繁，请稍后再试'
    }
    if (status === 404 && err.config?.url?.includes('/agent/')) {
      msg = '智能助手接口未找到，请确认后端已更新并重启，且地址为 /api/agent/chat'
    }
    if (d?.detail) {
      msg = `${msg}，${d.detail}`
    }
    return Promise.reject(new Error(msg))
  }
)

export default http
