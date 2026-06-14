const fs = require("fs");
const path = require("path");

const width = 3000;
const height = 4000;
const margin = 160;
const gap = 36;
const cols = 2;
const rows = 3;
const panelW = Math.floor((width - margin * 2 - gap) / cols);
const panelH = 780;
const gridTop = 940;

const panels = [
  { title: "赵州桥", subtitle: "结构之美", c1: "#8ab0b7", c2: "#2c6e7a" },
  { title: "风雨桥", subtitle: "人文之桥", c1: "#92b9a8", c2: "#3d7c5f" },
  { title: "古桥夜景", subtitle: "光影叙事", c1: "#7696b4", c2: "#284d74" },
  { title: "徽派民居", subtitle: "白墙黛瓦", c1: "#b5b7bd", c2: "#565b66" },
  { title: "四合院", subtitle: "礼序空间", c1: "#b59f79", c2: "#7a5f33" },
  { title: "古镇民居群", subtitle: "聚落肌理", c1: "#9bb2ab", c2: "#446a60" },
];

const panelSvg = panels
  .map((p, i) => {
    const col = i % cols;
    const row = Math.floor(i / cols);
    const x = margin + col * (panelW + gap);
    const y = gridTop + row * (panelH + gap);
    const id = `grad${i}`;
    return `
      <defs>
        <linearGradient id="${id}" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0%" stop-color="${p.c1}" />
          <stop offset="100%" stop-color="${p.c2}" />
        </linearGradient>
      </defs>
      <rect x="${x}" y="${y}" width="${panelW}" height="${panelH}" rx="18" fill="url(#${id})" opacity="0.95"/>
      <rect x="${x + 34}" y="${y + 40}" width="${panelW - 68}" height="${
      panelH - 210
    }" rx="14" fill="rgba(255,255,255,0.16)" />
      <text x="${x + 54}" y="${y + panelH - 88}" font-size="64" fill="#f6f2e9" font-weight="700">${p.title}</text>
      <text x="${x + 54}" y="${y + panelH - 36}" font-size="40" fill="#e8dfcc">${p.subtitle}</text>
    `;
  })
  .join("\n");

const svg = `<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" width="${width}" height="${height}" viewBox="0 0 ${width} ${height}">
  <defs>
    <linearGradient id="bg" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#d6ebe7" />
      <stop offset="55%" stop-color="#7da8a9" />
      <stop offset="100%" stop-color="#2f5f69" />
    </linearGradient>
  </defs>

  <rect width="${width}" height="${height}" fill="url(#bg)" />
  <rect x="0" y="0" width="${width}" height="540" fill="rgba(255,255,255,0.26)"/>
  <text x="${width / 2}" y="250" text-anchor="middle" font-size="176" font-weight="800" fill="#173941">营造千年</text>
  <text x="${width / 2}" y="350" text-anchor="middle" font-size="66" fill="#214853">1911年前中国古代桥梁与民居数字视觉叙事</text>
  <text x="${width / 2}" y="450" text-anchor="middle" font-size="46" fill="#2d5d69">中国大学生计算机设计大赛西北地区赛  AI+数媒静态设计</text>

  ${panelSvg}

  <rect x="0" y="${height - 290}" width="${width}" height="290" fill="rgba(17,42,47,0.55)"/>
  <text x="${width / 2}" y="${height - 170}" text-anchor="middle" font-size="54" fill="#f2f2ed">
    结构之美 · 空间之序 · 生活之韵
  </text>
  <text x="${width / 2}" y="${height - 92}" text-anchor="middle" font-size="40" fill="#dde4e2">
    团队：____________  指导教师：____________  学校：____________
  </text>
</svg>`;

const outputDir = path.join(__dirname, "..", "output");
if (!fs.existsSync(outputDir)) {
  fs.mkdirSync(outputDir, { recursive: true });
}

const outputFile = path.join(outputDir, "营造千年_代码生成海报.svg");
fs.writeFileSync(outputFile, svg, "utf8");
console.log(`Poster generated: ${outputFile}`);
