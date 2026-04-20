/** 兼容旧版数组与新版 { content, totalElements, ... } */
export function unwrapPage(data) {
  if (!data) return { items: [], total: 0, raw: data }
  if (Array.isArray(data)) return { items: data, total: data.length, raw: data }
  const items = data.content ?? []
  const total = data.totalElements ?? items.length
  return { items, total, raw: data }
}
