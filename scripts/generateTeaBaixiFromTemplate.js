const fs = require("fs");
const path = require("path");

const templatePath =
  "C:/Users/PC/.cursor/projects/c-Users-PC-Desktop-111/assets/c__Users_PC_AppData_Roaming_Cursor_User_workspaceStorage_148d68ef850d49b5d64575bd1cae3850_images_d136cdd78e7c0eb64a16a2402aae4c4-f53753c2-241d-4cd7-9265-6243d55c4973.png";

const outputDir = path.join(__dirname, "..", "output");
if (!fs.existsSync(outputDir)) fs.mkdirSync(outputDir, { recursive: true });

const imgBase64 = fs.readFileSync(templatePath).toString("base64");
const width = 736;
const height = 1060;

const svg = `<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" width="${width}" height="${height}" viewBox="0 0 ${width} ${height}">
  <image href="data:image/png;base64,${imgBase64}" x="0" y="0" width="${width}" height="${height}" />
  <rect x="8" y="8" width="720" height="1044" fill="none" stroke="#111" stroke-width="1"/>

  <!-- 顶部标题区 -->
  <rect x="16" y="12" width="370" height="120" fill="#ffffff" opacity="0.9"/>
  <text x="26" y="72" font-size="64" font-weight="800" fill="#111">茶百戏</text>
  <text x="26" y="110" font-size="20" font-weight="600" fill="#111">Tea Art in Motion</text>
  <text x="390" y="42" font-size="24" font-weight="700" fill="#111">可饮可观的东方艺术</text>
  <text x="390" y="70" font-size="12" fill="#222">宋代点茶技艺与茶汤纹样的可视化研究</text>
  <text x="390" y="88" font-size="10" fill="#222">源于点茶传统，成于文人雅集，兴于茶汤纹样表达</text>

  <!-- 时间线 -->
  <rect x="16" y="138" width="335" height="165" fill="#fff" opacity="0.9"/>
  <text x="20" y="160" font-size="16" font-weight="700">历史脉络</text>
  <text x="20" y="182" font-size="10">唐末：末茶法成熟</text>
  <text x="20" y="202" font-size="10">北宋：点茶程序规范化</text>
  <text x="20" y="222" font-size="10">南宋：茶百戏与纹样审美兴盛</text>
  <text x="20" y="242" font-size="10">元后：散茶冲泡逐步普及</text>
  <text x="20" y="262" font-size="10">当代：非遗传播与视觉再设计</text>

  <!-- 右上环形说明 -->
  <rect x="420" y="138" width="300" height="170" fill="#fff" opacity="0.9"/>
  <text x="426" y="160" font-size="16" font-weight="700">审美维度</text>
  <text x="426" y="184" font-size="10">白度 / 细腻 / 持久 / 纹样 / 香气</text>
  <text x="426" y="204" font-size="10">以“咬盏”“立沫”衡量点茶功力</text>
  <text x="426" y="224" font-size="10">茶百戏体现可视化的东方审美逻辑</text>

  <!-- 左中折线 -->
  <rect x="16" y="310" width="300" height="106" fill="#fff" opacity="0.9"/>
  <text x="20" y="330" font-size="15" font-weight="700">核心流程（8步）</text>
  <text x="20" y="350" font-size="10">备器→温盏→置茶→注汤→击拂→调膏→成沫→品鉴</text>
  <text x="20" y="370" font-size="10">动作关键：茶筅起落频率与注汤节奏协同</text>

  <!-- 中央圆盘标签 -->
  <rect x="244" y="450" width="250" height="42" fill="#fff" opacity="0.95"/>
  <text x="252" y="476" font-size="22" font-weight="800">主图：点茶过程示意</text>
  <text x="270" y="495" font-size="10">（此区可替换你后续生图）</text>

  <!-- 右中条形说明 -->
  <rect x="598" y="350" width="122" height="120" fill="#fff" opacity="0.9"/>
  <text x="603" y="368" font-size="13" font-weight="700">对比维度</text>
  <text x="603" y="388" font-size="10">点茶：末茶+击拂</text>
  <text x="603" y="406" font-size="10">现代：散茶+浸泡</text>
  <text x="603" y="424" font-size="10">共通：社交与审美</text>
  <text x="603" y="442" font-size="10">价值：文化再生</text>

  <!-- 中下部说明 -->
  <rect x="530" y="486" width="190" height="140" fill="#fff" opacity="0.9"/>
  <text x="536" y="506" font-size="14" font-weight="700">地域与传播</text>
  <text x="536" y="526" font-size="10">福建建窑：建盏核心产地</text>
  <text x="536" y="544" font-size="10">浙江杭州：文人茶席中心</text>
  <text x="536" y="562" font-size="10">江西景德镇：瓷器协同</text>
  <text x="536" y="580" font-size="10">水路网络推动茶事扩散</text>
  <text x="536" y="598" font-size="10">形成跨区域文化传播链</text>

  <!-- 左下器具 -->
  <rect x="16" y="620" width="210" height="120" fill="#fff" opacity="0.9"/>
  <text x="22" y="640" font-size="14" font-weight="700">关键器具</text>
  <text x="22" y="660" font-size="10">建盏 / 茶筅 / 汤瓶 / 茶则</text>
  <text x="22" y="678" font-size="10">茶匙 / 茶磨 / 罗合 / 盏托</text>
  <text x="22" y="696" font-size="10">器以载道，技以传神</text>

  <!-- 底部时间轴 -->
  <rect x="250" y="740" width="470" height="55" fill="#fff" opacity="0.9"/>
  <text x="256" y="760" font-size="13" font-weight="700">当代转化路径</text>
  <text x="256" y="780" font-size="10">非遗教育 → 文创产品 → 茶空间设计 → 青年传播</text>

  <!-- 底部三图 -->
  <rect x="16" y="800" width="250" height="110" fill="#fff" opacity="0.9"/>
  <text x="22" y="820" font-size="14" font-weight="700">文化价值</text>
  <text x="22" y="840" font-size="10">可饮：技艺层</text>
  <text x="22" y="858" font-size="10">可观：审美层</text>
  <text x="22" y="876" font-size="10">可传：教育层</text>

  <rect x="270" y="800" width="220" height="110" fill="#fff" opacity="0.9"/>
  <text x="276" y="820" font-size="14" font-weight="700">视觉语言</text>
  <text x="276" y="840" font-size="10">色系：米白/茶褐/黛青</text>
  <text x="276" y="858" font-size="10">形态：圆盘/轨迹/节点</text>
  <text x="276" y="876" font-size="10">风格：古雅、克制、清晰</text>

  <rect x="494" y="800" width="226" height="110" fill="#fff" opacity="0.9"/>
  <text x="500" y="820" font-size="14" font-weight="700">结论</text>
  <text x="500" y="840" font-size="10">茶百戏是传统技艺与视觉艺术的复合体</text>
  <text x="500" y="858" font-size="10">适合在信息可视化中进行跨媒介表达</text>
  <text x="500" y="876" font-size="10">具有竞赛展示与文化传播双重价值</text>

  <rect x="16" y="930" width="704" height="110" fill="#111" opacity="0.9"/>
  <text x="30" y="965" font-size="15" fill="#fff">参赛类别：AI+信息可视化设计</text>
  <text x="30" y="990" font-size="12" fill="#fff">主题：中国传统文化数字化表达（茶百戏）</text>
  <text x="30" y="1015" font-size="12" fill="#fff">作者：________  学校：________  指导教师：________</text>
</svg>`;

const svgPath = path.join(outputDir, "茶百戏_按模板排版.svg");
fs.writeFileSync(svgPath, svg, "utf8");
console.log(`Generated: ${svgPath}`);
