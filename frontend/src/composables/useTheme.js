const DEFAULT_PRIMARY = '#4a90e2'

export function applyThemeFromSettings(settingsJson) {
  let primary = DEFAULT_PRIMARY
  let compact = false
  try {
    const o = settingsJson ? JSON.parse(settingsJson) : {}
    if (o.primaryColor && /^#[0-9A-Fa-f]{6}$/.test(o.primaryColor)) {
      primary = o.primaryColor
    }
    if (typeof o.compact === 'boolean') {
      compact = o.compact
    }
  } catch {
    /* ignore */
  }
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

export function buildSettingsJson(partial) {
  return JSON.stringify({
    primaryColor: partial.primaryColor ?? DEFAULT_PRIMARY,
    compact: Boolean(partial.compact)
  })
}
