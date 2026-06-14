const fs = require("fs");
const path = require("path");

const W = 2480;
const H = 3508; // A4 300dpi portrait
const outputDir = path.join(__dirname, "..", "output");
if (!fs.existsSync(outputDir)) fs.mkdirSync(outputDir, { recursive: true });

const svg = `<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" width="${W}" height="${H}" viewBox="0 0 ${W} ${H}">
  <defs>
    <linearGradient id="bg" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#f8f2e9"/>
      <stop offset="60%" stop-color="#efe4d4"/>
      <stop offset="100%" stop-color="#e6d5bf"/>
    </linearGradient>
    <linearGradient id="header" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0%" stop-color="#ffffff"/>
      <stop offset="100%" stop-color="#f6ecdd"/>
    </linearGradient>
    <linearGradient id="accent" x1="0" y1="0" x2="1" y2="0">
      <stop offset="0%" stop-color="#5f3d2f"/>
      <stop offset="100%" stop-color="#8c5c46"/>
    </linearGradient>
    <style>
      .t1{font:900 156px 'Microsoft YaHei', 'PingFang SC', sans-serif; fill:#1c1713; letter-spacing:2px}
      .t2{font:700 56px 'Microsoft YaHei', 'PingFang SC', sans-serif; fill:#3a2c24}
      .h{font:700 44px 'Microsoft YaHei', 'PingFang SC', sans-serif; fill:#2d211b}
      .b{font:500 31px 'Microsoft YaHei', 'PingFang SC', sans-serif; fill:#3a2d24}
      .s{font:400 27px 'Microsoft YaHei', 'PingFang SC', sans-serif; fill:#43342a}
      .k{font:700 34px 'Microsoft YaHei', 'PingFang SC', sans-serif; fill:#fff}
      .chip{font:600 28px 'Microsoft YaHei', 'PingFang SC', sans-serif; fill:#f8f0e6}
    </style>
  </defs>

  <rect width="${W}" height="${H}" fill="url(#bg)"/>

  <!-- 顶部标题 -->
  <rect x="70" y="70" width="2340" height="410" rx="28" fill="url(#header)" stroke="#6f4e3d" stroke-width="4"/>
  <rect x="92" y="94" width="14" height="362" rx="7" fill="#6f4e3d"/>
  <text x="110" y="250" class="t1">茶百戏</text>
  <text x="112" y="336" class="t2">Tea Art in Motion</text>
  <text x="112" y="392" class="b">可饮可观的东方艺术 · 宋代点茶技艺与茶汤纹样的可视化研究</text>
  <rect x="1080" y="130" width="1230" height="66" rx="12" fill="url(#accent)"/>
  <text x="1110" y="173" class="chip">研究主线：历史脉络 / 核心流程 / 器具系统 / 审美维度 / 当代转化</text>
  <text x="1080" y="255" class="s">茶百戏源于宋代点茶实践，以击拂形成稳定汤花，并在茶面呈现文字与纹样，</text>
  <text x="1080" y="299" class="s">兼具“可饮”的技艺属性与“可观”的视觉属性，是传统生活美学的代表性样本。</text>
  <text x="1080" y="343" class="s">本海报以信息可视化方式重构其知识结构，为非遗传播与设计转化提供参考路径。</text>

  <!-- 左上 时间线 -->
  <rect x="70" y="530" width="980" height="470" rx="20" fill="#fffaf3" stroke="#6f4e3d" stroke-width="3"/>
  <text x="100" y="560" class="h">历史脉络</text>
  <line x1="120" y1="660" x2="980" y2="660" stroke="#6f4e3d" stroke-width="3" stroke-dasharray="8 8"/>
  <circle cx="170" cy="660" r="10" fill="#6f4e3d"/><text x="136" y="714" class="s">唐末</text>
  <circle cx="390" cy="660" r="10" fill="#6f4e3d"/><text x="356" y="714" class="s">北宋</text>
  <circle cx="610" cy="660" r="10" fill="#6f4e3d"/><text x="576" y="714" class="s">南宋</text>
  <circle cx="830" cy="660" r="10" fill="#6f4e3d"/><text x="796" y="714" class="s">元后</text>
  <text x="100" y="780" class="s">唐末：末茶法趋于成熟，形成研茶与点饮基础。</text>
  <text x="100" y="824" class="s">北宋：宫廷与文人群体推动点茶程序规范化。</text>
  <text x="100" y="868" class="s">南宋：茶百戏纹样表达兴盛，审美价值凸显。</text>
  <text x="100" y="912" class="s">元后：冲泡法普及，点茶转入文化记忆与传承。</text>
  <text x="100" y="956" class="s">当代：以非遗教育、文创设计实现再传播。</text>

  <!-- 右上 审美维度 -->
  <rect x="1110" y="530" width="1300" height="470" rx="20" fill="#fffaf3" stroke="#6f4e3d" stroke-width="3"/>
  <text x="1140" y="590" class="h">审美维度</text>
  <circle cx="1450" cy="760" r="160" fill="none" stroke="#6f4e3d" stroke-width="4"/>
  <circle cx="1450" cy="760" r="104" fill="none" stroke="#6f4e3d" stroke-width="2"/>
  <line x1="1450" y1="600" x2="1450" y2="920" stroke="#6f4e3d" stroke-width="2"/>
  <line x1="1290" y1="760" x2="1610" y2="760" stroke="#6f4e3d" stroke-width="2"/>
  <text x="1650" y="666" class="s">评估指标：白度、细腻、持久、纹样、香气。</text>
  <text x="1650" y="710" class="s">技艺要点：注汤节奏与击拂力度的协同控制。</text>
  <text x="1650" y="754" class="s">审美特征：汤花层次清晰，边界稳定，纹样可辨。</text>
  <text x="1650" y="798" class="s">文化意义：由“器用”转化为“观看”的复合体验。</text>
  <text x="1650" y="842" class="s">传播潜力：适配课程、展陈与社交媒体视觉表达。</text>

  <!-- 中央主图区域 -->
  <rect x="70" y="1070" width="2340" height="1580" rx="24" fill="#fffaf3" stroke="#6f4e3d" stroke-width="4"/>
  <text x="110" y="1142" class="h">核心流程（8步）</text>
  <text x="110" y="1190" class="s">备器 → 温盏 → 置茶 → 注汤 → 击拂 → 调膏 → 成沫 → 品鉴</text>

  <circle cx="1240" cy="1870" r="560" fill="none" stroke="#6f4e3d" stroke-width="5"/>
  <circle cx="1240" cy="1870" r="360" fill="none" stroke="#6f4e3d" stroke-width="4"/>
  <circle cx="1240" cy="1870" r="210" fill="#fff8ef" stroke="#6f4e3d" stroke-width="4"/>
  <text x="1110" y="1828" class="h">主图</text>
  <text x="1035" y="1910" style="font:900 94px 'Microsoft YaHei';fill:#2d211b">点茶结构图</text>

  <!-- 环形节点 -->
  <g fill="#f3e7d7" stroke="#6f4e3d" stroke-width="3">
    <circle cx="1240" cy="1360" r="56"/><circle cx="1548" cy="1460" r="56"/><circle cx="1750" cy="1720" r="56"/>
    <circle cx="1750" cy="2020" r="56"/><circle cx="1548" cy="2280" r="56"/><circle cx="1240" cy="2380" r="56"/>
    <circle cx="932" cy="2280" r="56"/><circle cx="730" cy="2020" r="56"/><circle cx="730" cy="1720" r="56"/>
    <circle cx="932" cy="1460" r="56"/>
  </g>
  <text x="1198" y="1369" class="s">备</text><text x="1506" y="1469" class="s">温</text><text x="1708" y="1729" class="s">置</text>
  <text x="1708" y="2029" class="s">注</text><text x="1506" y="2289" class="s">击</text><text x="1198" y="2389" class="s">调</text>
  <text x="890" y="2289" class="s">成</text><text x="688" y="2029" class="s">品</text><text x="688" y="1729" class="s">鉴</text><text x="890" y="1469" class="s">器</text>

  <!-- 右侧说明 -->
  <rect x="1840" y="1260" width="520" height="600" rx="16" fill="#fff5e8" stroke="#6f4e3d" stroke-width="3"/>
  <text x="1870" y="1330" class="h">点茶对比</text>
  <text x="1870" y="1388" class="s">点茶：末茶 + 击拂 + 观沫</text>
  <text x="1870" y="1436" class="s">冲泡：散茶 + 浸泡 + 闻香</text>
  <text x="1870" y="1484" class="s">共通：社交属性与审美表达</text>
  <text x="1870" y="1532" class="s">价值：文化认同与日常体验重建</text>
  <text x="1870" y="1612" class="h">地域分布</text>
  <text x="1870" y="1660" class="s">福建建窑：黑釉建盏核心产区</text>
  <text x="1870" y="1708" class="s">浙江杭州：茶事活动与文人传播中心</text>
  <text x="1870" y="1756" class="s">江西景德镇：器物体系协同发展</text>
  <text x="1870" y="1804" class="s">川蜀与两浙：交通网络支撑扩散</text>

  <!-- 下方三栏 -->
  <rect x="70" y="2720" width="760" height="560" rx="16" fill="#fffaf3" stroke="#6f4e3d" stroke-width="3"/>
  <text x="100" y="2790" class="h">文化价值</text>
  <text x="100" y="2850" class="s">可饮：保留并验证传统点茶技艺链路。</text>
  <text x="100" y="2896" class="s">可观：以茶汤纹样承载视觉叙事与审美。</text>
  <text x="100" y="2942" class="s">可传：适用于课堂、展陈和公共传播场景。</text>
  <text x="100" y="2988" class="s">可创：可转化为文创产品与空间体验设计。</text>
  <text x="100" y="3034" class="s">可研：可构建“工艺+图像+传播”研究模型。</text>

  <rect x="860" y="2720" width="760" height="560" rx="16" fill="#fffaf3" stroke="#6f4e3d" stroke-width="3"/>
  <text x="890" y="2790" class="h">视觉语言</text>
  <text x="890" y="2850" class="s">色彩系统：米白（底）/ 茶褐（主）/ 黛青（辅）。</text>
  <text x="890" y="2896" class="s">图形系统：圆盘结构、节点轨迹、时间轴并置。</text>
  <text x="890" y="2942" class="s">排版策略：标题强对比，正文网格化分区阅读。</text>
  <text x="890" y="2988" class="s">叙事逻辑：从历史到工艺，再到当代应用转化。</text>
  <text x="890" y="3034" class="s">输出场景：赛事展板、课程汇报、文化科普海报。</text>

  <rect x="1650" y="2720" width="760" height="560" rx="16" fill="#fff5e8" stroke="#6f4e3d" stroke-width="3"/>
  <text x="1680" y="2790" class="h">结论</text>
  <text x="1680" y="2850" class="s">茶百戏是“技艺实践+视觉审美”的复合文化样本。</text>
  <text x="1680" y="2896" class="s">其信息可视化表达可提升复杂知识的可理解性。</text>
  <text x="1680" y="2942" class="s">在非遗传播中，图像化叙事有助于触达青年群体。</text>
  <text x="1680" y="2988" class="s">在设计应用中，可形成跨媒介的品牌与展陈语言。</text>
  <text x="1680" y="3034" class="s">具备赛事展示、教学落地与公共传播三重价值。</text>

  <!-- 页脚 -->
  <rect x="70" y="3320" width="2340" height="120" rx="12" fill="url(#accent)"/>
  <text x="100" y="3320" class="k">参赛类别：AI+信息可视化设计</text>
  <text x="100" y="3370" class="k">主题：茶百戏——可饮可观的东方艺术</text>
  <text x="100" y="3415" class="k">作者：________  学校：________  指导教师：________</text>
</svg>`;

const svgPath = path.join(outputDir, "茶百戏_成品海报_美化版.svg");
const pngPath = path.join(outputDir, "茶百戏_成品海报_美化版.png");
fs.writeFileSync(svgPath, svg, "utf8");
console.log(`SVG generated: ${svgPath}`);
console.log(`PNG target: ${pngPath}`);
