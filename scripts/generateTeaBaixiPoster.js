const fs = require("fs");
const path = require("path");

const width = 3000;
const height = 4200;
const margin = 120;

const sections = [
  { t: "历史脉络", x: 120, y: 760, w: 1320, h: 360 },
  { t: "核心流程", x: 1560, y: 760, w: 1320, h: 360 },
  { t: "关键器具", x: 120, y: 1160, w: 870, h: 980 },
  { t: "地域分布", x: 1020, y: 1160, w: 900, h: 980 },
  { t: "审美特征", x: 1950, y: 1160, w: 930, h: 980 },
  { t: "社会场景", x: 120, y: 2180, w: 1320, h: 840 },
  { t: "现代连接", x: 1560, y: 2180, w: 1320, h: 840 },
];

function sectionBox(s) {
  return `
    <rect x="${s.x}" y="${s.y}" width="${s.w}" height="${s.h}" rx="20" fill="rgba(255,255,255,0.72)" stroke="#6f4a3d" stroke-width="3"/>
    <rect x="${s.x + 20}" y="${s.y + 18}" width="220" height="52" rx="12" fill="#7d4a3a"/>
    <text x="${s.x + 130}" y="${s.y + 54}" text-anchor="middle" font-size="34" fill="#f8efe8" font-weight="700">${s.t}</text>
  `;
}

const timelineItems = [
  "唐末：末茶法成熟",
  "北宋：点茶规范化",
  "南宋：茶百戏兴盛",
  "元后：泡茶法普及",
];

const processItems = [
  "备器",
  "温盏",
  "置茶",
  "注汤",
  "击拂",
  "调膏",
  "成沫",
  "品鉴",
];

const toolsItems = ["建盏", "茶筅", "茶则", "汤瓶", "茶磨", "罗合", "茶匙", "盏托"];

const sceneItems = ["宫廷茶宴", "文人雅集", "市井茶肆", "节令礼仪"];

const imageSlots = [
  { name: "主视觉人物", x: 170, y: 2290, w: 500, h: 640 },
  { name: "茶汤纹样", x: 700, y: 2290, w: 700, h: 300 },
  { name: "器具陈列", x: 700, y: 2630, w: 700, h: 300 },
  { name: "建盏特写", x: 1610, y: 2290, w: 600, h: 300 },
  { name: "点茶动作", x: 2240, y: 2290, w: 600, h: 300 },
  { name: "茶席场景", x: 1610, y: 2630, w: 1230, h: 300 },
];

const svg = `<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" width="${width}" height="${height}" viewBox="0 0 ${width} ${height}">
  <defs>
    <linearGradient id="bg" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#efe2d2"/>
      <stop offset="70%" stop-color="#e6d6c1"/>
      <stop offset="100%" stop-color="#d6bea4"/>
    </linearGradient>
    <style>
      .small{font-size:30px;fill:#3e2a21}
      .body{font-size:34px;fill:#3e2a21}
      .tag{font-size:28px;fill:#6f4a3d}
      .tiny{font-size:24px;fill:#6f4a3d}
    </style>
  </defs>

  <rect width="${width}" height="${height}" fill="url(#bg)"/>
  <rect x="${margin}" y="110" width="${width - margin * 2}" height="560" rx="30" fill="rgba(255,255,255,0.64)" stroke="#7d4a3a" stroke-width="4"/>
  <text x="180" y="260" font-size="142" fill="#2d1c15" font-weight="800">茶百戏：可饮可观的东方艺术</text>
  <text x="182" y="332" font-size="52" fill="#5f3b30">Tea Art in Motion: A Drinkable and Viewable Eastern Aesthetic</text>
  <text x="182" y="410" font-size="44" fill="#6f4a3d">宋代点茶技艺与茶汤纹样的可视化研究</text>
  <text x="182" y="505" font-size="35" fill="#4a332a">
    茶百戏源于宋代点茶传统，以茶筅击拂形成细腻汤花，并在茶面勾勒山水、花鸟、文字等纹样。
  </text>
  <text x="182" y="558" font-size="35" fill="#4a332a">
    它融合饮茶技法、视觉审美与文人雅趣，是“可饮可观”的东方生活艺术。
  </text>

  ${sections.map(sectionBox).join("\n")}

  ${timelineItems
    .map(
      (t, i) => `
    <circle cx="${210 + i * 320}" cy="950" r="12" fill="#7d4a3a"/>
    <line x1="${210 + i * 320}" y1="950" x2="${210 + i * 320 + 280}" y2="950" stroke="#7d4a3a" stroke-width="3" ${
        i === timelineItems.length - 1 ? 'opacity="0"' : ""
      }/>
    <text x="${170 + i * 320}" y="1010" class="small">${t}</text>
  `
    )
    .join("\n")}

  ${processItems
    .map((t, i) => {
      const x = 1630 + (i % 4) * 300;
      const y = 890 + Math.floor(i / 4) * 140;
      return `
      <rect x="${x}" y="${y}" width="230" height="80" rx="40" fill="#f8f1e8" stroke="#7d4a3a" stroke-width="3"/>
      <text x="${x + 115}" y="${y + 52}" text-anchor="middle" class="body">${i + 1}. ${t}</text>
      `;
    })
    .join("\n")}

  ${toolsItems
    .map((t, i) => {
      const x = 230 + (i % 2) * 390;
      const y = 1270 + Math.floor(i / 2) * 210;
      return `
      <circle cx="${x}" cy="${y}" r="80" fill="#efe2d2" stroke="#7d4a3a" stroke-width="3"/>
      <text x="${x}" y="${y + 12}" text-anchor="middle" class="body">${t}</text>
      `;
    })
    .join("\n")}

  <rect x="1060" y="1260" width="820" height="420" rx="16" fill="#f7efe4" stroke="#7d4a3a" stroke-width="3"/>
  <text x="1090" y="1320" class="small">重点地域：福建建窑、浙江杭州、江西景德镇、四川与两浙。</text>
  <text x="1090" y="1380" class="small">流通逻辑：茶叶产区→水路运输→都市茶席→文人传播。</text>
  <rect x="1110" y="1430" width="760" height="220" rx="12" fill="rgba(125,74,58,0.08)" stroke="#7d4a3a" stroke-dasharray="8 8" stroke-width="2"/>
  <text x="1490" y="1545" text-anchor="middle" class="tag">地图插图占位（可替换生图）</text>

  <circle cx="2410" cy="1510" r="280" fill="#f8f1e8" stroke="#7d4a3a" stroke-width="3"/>
  <circle cx="2410" cy="1510" r="178" fill="none" stroke="#7d4a3a" stroke-width="3"/>
  <line x1="2410" y1="1230" x2="2410" y2="1790" stroke="#7d4a3a" stroke-width="2"/>
  <line x1="2130" y1="1510" x2="2690" y2="1510" stroke="#7d4a3a" stroke-width="2"/>
  <text x="2410" y="1518" text-anchor="middle" class="body">审美维度</text>
  <text x="2410" y="1860" text-anchor="middle" class="tiny">白度 / 细腻 / 持久 / 纹样 / 香气</text>

  ${sceneItems
    .map((t, i) => `
    <rect x="190" y="${3070 + i * 68}" width="1180" height="52" rx="10" fill="rgba(255,255,255,0.5)"/>
    <text x="230" y="${3106 + i * 68}" class="small">• ${t}</text>
  `)
    .join("\n")}

  <rect x="1560" y="3070" width="1320" height="270" rx="16" fill="rgba(255,255,255,0.5)" stroke="#7d4a3a" stroke-width="2"/>
  <text x="1600" y="3135" class="small">点茶（末茶+击拂+观沫）  VS  现代冲泡（散茶+浸泡+闻香）</text>
  <text x="1600" y="3190" class="small">共通点：社交属性、审美表达、文化身份。</text>
  <text x="1600" y="3245" class="small">结论：传统技艺可转化为当代文创与视觉传播语言。</text>

  ${imageSlots
    .map(
      (s) => `
    <rect x="${s.x}" y="${s.y}" width="${s.w}" height="${s.h}" rx="14" fill="rgba(255,255,255,0.35)" stroke="#7d4a3a" stroke-dasharray="10 8" stroke-width="3"/>
    <text x="${s.x + s.w / 2}" y="${s.y + s.h / 2}" text-anchor="middle" class="tag">${s.name}（替换生图）</text>
  `
    )
    .join("\n")}

  <rect x="0" y="${height - 180}" width="${width}" height="180" fill="rgba(58,33,24,0.82)"/>
  <text x="${width / 2}" y="${height - 110}" text-anchor="middle" font-size="36" fill="#f2e7db">
    参赛类别：AI+信息可视化设计  ｜  主题：中国传统文化视觉化表达
  </text>
  <text x="${width / 2}" y="${height - 56}" text-anchor="middle" font-size="30" fill="#dbc9b8">
    作者：________  学校：________  指导教师：________
  </text>
</svg>`;

const outputDir = path.join(__dirname, "..", "output");
if (!fs.existsSync(outputDir)) fs.mkdirSync(outputDir, { recursive: true });

const svgPath = path.join(outputDir, "茶百戏_信息可视化海报.svg");
fs.writeFileSync(svgPath, svg, "utf8");
console.log(`SVG generated: ${svgPath}`);
