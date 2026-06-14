const fs = require("fs");
const path = require("path");

const templatePath =
  "C:/Users/PC/.cursor/projects/c-Users-PC-Desktop-111/assets/c__Users_PC_AppData_Roaming_Cursor_User_workspaceStorage_148d68ef850d49b5d64575bd1cae3850_images_d136cdd78e7c0eb64a16a2402aae4c4-c36ce031-369b-438f-b3c9-e9ddf89b649d.png";

const outputDir = path.join(__dirname, "..", "output");
if (!fs.existsSync(outputDir)) fs.mkdirSync(outputDir, { recursive: true });

const imgBase64 = fs.readFileSync(templatePath).toString("base64");
const width = 736;
const height = 1060;

const svg = `<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" width="${width}" height="${height}" viewBox="0 0 ${width} ${height}">
  <image href="data:image/png;base64,${imgBase64}" x="0" y="0" width="${width}" height="${height}" />

  <!-- 标题区 -->
  <rect x="8" y="8" width="720" height="132" fill="#ffffff" opacity="0.96"/>
  <text x="24" y="72" font-size="68" font-weight="900" fill="#111">茶百戏</text>
  <text x="24" y="108" font-size="20" font-weight="700" fill="#111">Information Visualization Title</text>
  <text x="340" y="40" font-size="24" font-weight="700" fill="#111">可饮可观的东方艺术</text>
  <text x="340" y="68" font-size="12" fill="#222">宋代点茶技艺与茶汤纹样的可视化研究</text>
  <text x="340" y="88" font-size="10" fill="#222">以“流程、器具、地域、审美”四线并行解构茶百戏文化体系</text>

  <!-- 左上：时间轴 -->
  <rect x="12" y="146" width="340" height="136" fill="#ffffff" opacity="0.94"/>
  <text x="18" y="165" font-size="14" font-weight="700">历史脉络</text>
  <text x="18" y="184" font-size="10">唐末：末茶法成熟</text>
  <text x="18" y="201" font-size="10">北宋：点茶程序规范化</text>
  <text x="18" y="218" font-size="10">南宋：茶百戏审美兴盛</text>
  <text x="18" y="235" font-size="10">元后：散茶冲泡普及</text>
  <text x="18" y="252" font-size="10">当代：非遗传播与文创转化</text>

  <!-- 右上：审美维度 -->
  <rect x="420" y="146" width="302" height="136" fill="#ffffff" opacity="0.94"/>
  <text x="426" y="165" font-size="14" font-weight="700">审美维度</text>
  <text x="426" y="184" font-size="10">白度 / 细腻 / 持久 / 纹样 / 香气</text>
  <text x="426" y="201" font-size="10">“咬盏”与“立沫”是关键评判指标</text>
  <text x="426" y="218" font-size="10">茶汤纹样兼具观赏与文化叙事功能</text>
  <text x="426" y="235" font-size="10">体现东方审美中的克制与留白</text>

  <!-- 左中：流程 -->
  <rect x="12" y="298" width="300" height="104" fill="#ffffff" opacity="0.94"/>
  <text x="18" y="318" font-size="14" font-weight="700">核心流程（8步）</text>
  <text x="18" y="338" font-size="10">备器→温盏→置茶→注汤</text>
  <text x="18" y="356" font-size="10">击拂→调膏→成沫→品鉴</text>
  <text x="18" y="374" font-size="10">节奏控制决定泡沫质感与稳定性</text>

  <!-- 中央主图圆盘区域标注 -->
  <rect x="262" y="498" width="214" height="44" fill="#ffffff" opacity="0.96"/>
  <text x="272" y="526" font-size="20" font-weight="800">主图：点茶结构图</text>

  <!-- 右中：对比 -->
  <rect x="596" y="348" width="126" height="116" fill="#ffffff" opacity="0.94"/>
  <text x="602" y="368" font-size="13" font-weight="700">点茶对比</text>
  <text x="602" y="386" font-size="10">点茶：末茶+击拂</text>
  <text x="602" y="404" font-size="10">冲泡：散茶+浸泡</text>
  <text x="602" y="422" font-size="10">共通：社交审美</text>
  <text x="602" y="440" font-size="10">价值：文化再生</text>

  <!-- 右中下：地域 -->
  <rect x="530" y="486" width="192" height="140" fill="#ffffff" opacity="0.94"/>
  <text x="536" y="506" font-size="14" font-weight="700">地域分布</text>
  <text x="536" y="525" font-size="10">福建建窑：建盏核心产区</text>
  <text x="536" y="543" font-size="10">浙江杭州：文人茶席中心</text>
  <text x="536" y="561" font-size="10">江西景德镇：瓷器协同</text>
  <text x="536" y="579" font-size="10">川蜀与两浙：流通网络节点</text>
  <text x="536" y="597" font-size="10">形成跨区域传播链路</text>

  <!-- 左下：器具 -->
  <rect x="12" y="620" width="216" height="118" fill="#ffffff" opacity="0.94"/>
  <text x="18" y="640" font-size="14" font-weight="700">关键器具</text>
  <text x="18" y="659" font-size="10">建盏 / 茶筅 / 汤瓶 / 茶则</text>
  <text x="18" y="677" font-size="10">茶匙 / 茶磨 / 罗合 / 盏托</text>
  <text x="18" y="695" font-size="10">器以载道，技以传神</text>

  <!-- 中下：发展链 -->
  <rect x="250" y="742" width="472" height="52" fill="#ffffff" opacity="0.94"/>
  <text x="256" y="760" font-size="13" font-weight="700">当代转化路径</text>
  <text x="256" y="779" font-size="10">非遗教育 → 文创设计 → 茶空间体验 → 青年传播</text>

  <!-- 底部三栏 -->
  <rect x="12" y="800" width="252" height="110" fill="#ffffff" opacity="0.94"/>
  <text x="18" y="820" font-size="14" font-weight="700">文化价值</text>
  <text x="18" y="840" font-size="10">可饮：技艺实践</text>
  <text x="18" y="858" font-size="10">可观：视觉审美</text>
  <text x="18" y="876" font-size="10">可传：文化教育</text>

  <rect x="268" y="800" width="222" height="110" fill="#ffffff" opacity="0.94"/>
  <text x="274" y="820" font-size="14" font-weight="700">视觉语言</text>
  <text x="274" y="840" font-size="10">色系：米白 / 茶褐 / 黛青</text>
  <text x="274" y="858" font-size="10">图形：圆盘 / 节点 / 时间轴</text>
  <text x="274" y="876" font-size="10">风格：古雅、克制、清晰</text>

  <rect x="494" y="800" width="228" height="110" fill="#ffffff" opacity="0.94"/>
  <text x="500" y="820" font-size="14" font-weight="700">结论</text>
  <text x="500" y="840" font-size="10">茶百戏是技艺与审美的复合体</text>
  <text x="500" y="858" font-size="10">适用于信息可视化的跨媒介表达</text>
  <text x="500" y="876" font-size="10">兼具赛事展示与传播价值</text>

  <!-- 页脚 -->
  <rect x="12" y="930" width="710" height="114" fill="#111" opacity="0.9"/>
  <text x="24" y="964" font-size="15" fill="#fff">参赛类别：AI+信息可视化设计</text>
  <text x="24" y="990" font-size="12" fill="#fff">主题：茶百戏——可饮可观的东方艺术</text>
  <text x="24" y="1016" font-size="12" fill="#fff">作者：________  学校：________  指导教师：________</text>
</svg>`;

const svgPath = path.join(outputDir, "茶百戏_模板文案图.svg");
const pngPath = path.join(outputDir, "茶百戏_模板文案图.png");

fs.writeFileSync(svgPath, svg, "utf8");
console.log(`SVG generated: ${svgPath}`);
console.log(`PNG target: ${pngPath}`);
