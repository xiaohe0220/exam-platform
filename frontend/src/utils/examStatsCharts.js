/** 根据单场考试统计数据生成图表用系列（成绩分布为基于均值/方差的估算，非逐卷真实直方图） */

function seededRandom(seed) {
  let s = Math.abs(Math.floor(seed)) % 2147483647
  if (s <= 0) s += 2147483646
  return () => {
    s = (s * 16807) % 2147483647
    return (s - 1) / 2147483646
  }
}

function gaussianPair(rand) {
  const u = rand() || 1e-9
  const v = rand()
  return Math.sqrt(-2 * Math.log(u)) * Math.cos(2 * Math.PI * v)
}

export function estimateScoreBins(avg, min, max, submittedCount, seed = 1) {
  const bins = [0, 0, 0, 0, 0]
  const labels = ['0–60', '60–70', '70–80', '80–90', '90–100']
  if (!submittedCount || submittedCount <= 0) {
    return { labels, values: bins }
  }
  const rand = seededRandom(seed)
  const a = Number(avg)
  const lo = Number(min)
  const hi = Number(max)
  const sigma = Math.max(6, (hi - lo) / 5 || 10)
  for (let i = 0; i < submittedCount; i++) {
    let score = a + gaussianPair(rand) * sigma
    score = Math.max(lo, Math.min(hi, score))
    const b = score < 60 ? 0 : score < 70 ? 1 : score < 80 ? 2 : score < 90 ? 3 : 4
    bins[b]++
  }
  return { labels, values: bins }
}

export function knowledgeRadarSeries(passRate, avgRatio, seed = 1) {
  const rand = seededRandom(seed)
  const base = Math.min(100, Math.max(0, passRate * 100))
  const ar = Math.min(100, Math.max(0, avgRatio * 100))
  const labels = ['概念理解', '计算应用', '综合分析', '拓展迁移', '规范表达']
  return labels.map((name, i) => {
    const jitter = (rand() - 0.5) * 12
    const v = i % 2 === 0 ? ar * 0.92 + jitter : base * 0.88 + jitter
    return { name, value: Math.round(Math.min(100, Math.max(35, v))) }
  })
}

export function topWrongBarMock(seed = 1) {
  const rand = seededRandom(seed)
  const titles = ['题1 单选', '题2 多选', '题3 判断', '题4 填空', '题5 简答']
  return titles.map((t) => ({
    name: t,
    value: Math.round(15 + rand() * 55)
  })).sort((a, b) => b.value - a.value)
}
