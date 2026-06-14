const DEFAULT_PRIMARY = '#4a90e2'

export const DEFAULT_SETTINGS = {
  primaryColor: DEFAULT_PRIMARY,
  compact: false,
  questionBankView: 'grouped',
  assistantAutoOpen: false
}

export function parseSettingsJson(settingsJson) {
  try {
    const o = settingsJson ? JSON.parse(settingsJson) : {}
    return {
      ...DEFAULT_SETTINGS,
      ...o,
      primaryColor: /^#[0-9A-Fa-f]{6}$/.test(o.primaryColor || '')
        ? o.primaryColor
        : DEFAULT_SETTINGS.primaryColor,
      compact: typeof o.compact === 'boolean' ? o.compact : DEFAULT_SETTINGS.compact,
      questionBankView: ['grouped', 'list'].includes(o.questionBankView)
        ? o.questionBankView
        : DEFAULT_SETTINGS.questionBankView,
      assistantAutoOpen:
        typeof o.assistantAutoOpen === 'boolean'
          ? o.assistantAutoOpen
          : DEFAULT_SETTINGS.assistantAutoOpen
    }
  } catch {
    return { ...DEFAULT_SETTINGS }
  }
}

export function applyThemeFromSettings(settingsJson) {
  const settings = parseSettingsJson(settingsJson)
  const primary = settings.primaryColor
  const compact = settings.compact
  const root = document.documentElement
  root.style.setProperty('--campus-primary', primary)
  root.style.setProperty('--ep-color-primary', primary)
  root.style.setProperty('--el-color-primary', primary)
  root.style.setProperty('--campus-accent-soft', hexToRgba(primary, 0.12))
  root.style.setProperty('--campus-accent-mid', hexToRgba(primary, 0.35))
  root.classList.toggle('theme-compact', compact)
}

function hexToRgba(hex, a) {
  const n = hex.replace('#', '')
  const r = parseInt(n.slice(0, 2), 16)
  const g = parseInt(n.slice(2, 4), 16)
  const b = parseInt(n.slice(4, 6), 16)
  return `rgba(${r},${g},${b},${a})`
}

export function buildSettingsJson(partial = {}) {
  return JSON.stringify({
    ...DEFAULT_SETTINGS,
    ...partial,
    primaryColor: partial.primaryColor ?? DEFAULT_PRIMARY,
    compact: Boolean(partial.compact),
    questionBankView: ['grouped', 'list'].includes(partial.questionBankView)
      ? partial.questionBankView
      : DEFAULT_SETTINGS.questionBankView,
    assistantAutoOpen: Boolean(partial.assistantAutoOpen)
  })
}
