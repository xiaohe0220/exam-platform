-- 计算机题库初始化脚本（面向教师端组卷）
-- 用法：
--   mysql -uroot -p exam_platform < seed_computer_questions.sql
--
-- 说明：
-- 1) 默认将题目写入 teacher 账号名下（若 teacher 不存在则回退到首个 TEACHER 用户）
-- 2) 为避免重复导入，先删除“计算机题库批量导入”章节下的历史题目再重建

SET NAMES utf8mb4;

SET @teacher_id = (
  SELECT id FROM users WHERE username = 'teacher' LIMIT 1
);
SET @teacher_id = COALESCE(
  @teacher_id,
  (SELECT id FROM users WHERE role = 'TEACHER' ORDER BY id LIMIT 1)
);

-- 若找不到教师用户，终止执行（通过触发除零报错）
SET @must_have_teacher = IF(@teacher_id IS NULL, (SELECT 1/0), 1);

DELETE FROM questions
WHERE chapter = '计算机题库批量导入'
  AND creator_id = @teacher_id;

INSERT INTO questions (
  creator_id, type, title, content, options_json, correct_answer_json,
  difficulty, chapter, knowledge_point, answer_analysis, created_at
)
VALUES
-- 单选题（8）
(@teacher_id, 'SINGLE_CHOICE', 'CPU 中用于临时存放正在执行指令和数据的是哪一项？',
 '<p>CPU 中用于临时存放正在执行指令和数据的是哪一项？</p>',
 '["寄存器","硬盘","U盘","光盘"]', '"A"',
 2, '计算机题库批量导入', '计算机组成原理',
 '<p>寄存器位于 CPU 内部，访问速度最快。</p>', NOW()),

(@teacher_id, 'SINGLE_CHOICE', '下列哪种存储器断电后数据会丢失？',
 '<p>下列哪种存储器断电后数据会丢失？</p>',
 '["ROM","RAM","SSD","机械硬盘"]', '"B"',
 1, '计算机题库批量导入', '存储系统',
 '<p>RAM 为易失性存储，断电数据丢失。</p>', NOW()),

(@teacher_id, 'SINGLE_CHOICE', '在 OSI 七层模型中，IP 协议位于哪一层？',
 '<p>在 OSI 七层模型中，IP 协议位于哪一层？</p>',
 '["传输层","网络层","数据链路层","会话层"]', '"B"',
 2, '计算机题库批量导入', '计算机网络',
 '<p>IP 负责寻址与路由，属于网络层。</p>', NOW()),

(@teacher_id, 'SINGLE_CHOICE', 'HTTP 默认端口号是？',
 '<p>HTTP 默认端口号是？</p>',
 '["21","25","80","443"]', '"C"',
 1, '计算机题库批量导入', '应用层协议',
 '<p>HTTP 默认 80，HTTPS 默认 443。</p>', NOW()),

(@teacher_id, 'SINGLE_CHOICE', '关系型数据库中用于唯一标识一条记录的是？',
 '<p>关系型数据库中用于唯一标识一条记录的是？</p>',
 '["外键","主键","索引","视图"]', '"B"',
 1, '计算机题库批量导入', '数据库基础',
 '<p>主键（Primary Key）用于唯一标识记录。</p>', NOW()),

(@teacher_id, 'SINGLE_CHOICE', 'Linux 中查看当前目录文件列表最常用的命令是？',
 '<p>Linux 中查看当前目录文件列表最常用的命令是？</p>',
 '["pwd","ls","cd","cat"]', '"B"',
 1, '计算机题库批量导入', 'Linux基础',
 '<p>ls 用于列出目录内容。</p>', NOW()),

(@teacher_id, 'SINGLE_CHOICE', '时间复杂度 O(n log n) 常见于哪类算法？',
 '<p>时间复杂度 O(n log n) 常见于哪类算法？</p>',
 '["线性查找","冒泡排序","归并排序","顺序遍历"]', '"C"',
 3, '计算机题库批量导入', '算法与复杂度',
 '<p>归并排序平均和最坏复杂度均为 O(n log n)。</p>', NOW()),

(@teacher_id, 'SINGLE_CHOICE', '下列哪种 HTTP 方法通常用于“幂等更新”资源？',
 '<p>下列哪种 HTTP 方法通常用于“幂等更新”资源？</p>',
 '["POST","PUT","PATCH","CONNECT"]', '"B"',
 3, '计算机题库批量导入', 'Web开发',
 '<p>PUT 通常用于整体更新且幂等。</p>', NOW()),

-- 多选题（6）
(@teacher_id, 'MULTIPLE_CHOICE', '下列属于常见关系型数据库的是？',
 '<p>下列属于常见关系型数据库的是？</p>',
 '["MySQL","PostgreSQL","MongoDB","Oracle"]', '["A","B","D"]',
 2, '计算机题库批量导入', '数据库基础',
 '<p>MongoDB 是文档型 NoSQL，其余为关系型数据库。</p>', NOW()),

(@teacher_id, 'MULTIPLE_CHOICE', '以下哪些属于传输层协议？',
 '<p>以下哪些属于传输层协议？</p>',
 '["TCP","UDP","IP","ICMP"]', '["A","B"]',
 2, '计算机题库批量导入', '计算机网络',
 '<p>TCP/UDP 属于传输层；IP/ICMP 属于网络层。</p>', NOW()),

(@teacher_id, 'MULTIPLE_CHOICE', '面向对象编程三大特性包括？',
 '<p>面向对象编程三大特性包括？</p>',
 '["封装","继承","多态","编译"]', '["A","B","C"]',
 1, '计算机题库批量导入', '编程基础',
 '<p>封装、继承、多态是 OOP 三大特性。</p>', NOW()),

(@teacher_id, 'MULTIPLE_CHOICE', '下列哪些命令可用于查看网络连接或端口状态？',
 '<p>下列哪些命令可用于查看网络连接或端口状态？</p>',
 '["ss","netstat","ping","lsof"]', '["A","B","D"]',
 3, '计算机题库批量导入', '运维基础',
 '<p>ss/netstat/lsof 常用于端口排查；ping 主要测连通性。</p>', NOW()),

(@teacher_id, 'MULTIPLE_CHOICE', '下列哪些属于常见的软件开发生命周期阶段？',
 '<p>下列哪些属于常见的软件开发生命周期阶段？</p>',
 '["需求分析","系统设计","编码实现","单元测试"]', '["A","B","C","D"]',
 1, '计算机题库批量导入', '软件工程',
 '<p>以上都属于典型 SDLC 阶段。</p>', NOW()),

(@teacher_id, 'MULTIPLE_CHOICE', '为提升 Web 应用安全性，以下哪些做法是合理的？',
 '<p>为提升 Web 应用安全性，以下哪些做法是合理的？</p>',
 '["参数化查询防SQL注入","对敏感接口做鉴权","明文存储用户密码","启用HTTPS"]', '["A","B","D"]',
 3, '计算机题库批量导入', '网络安全',
 '<p>明文存储密码是错误做法，应使用加密哈希。</p>', NOW()),

-- 判断题（5）
(@teacher_id, 'TRUE_FALSE', 'TCP 是无连接协议。',
 '<p>TCP 是无连接协议。</p>',
 '["正确","错误"]', 'false',
 1, '计算机题库批量导入', '计算机网络',
 '<p>TCP 是面向连接协议；UDP 才是无连接协议。</p>', NOW()),

(@teacher_id, 'TRUE_FALSE', '二叉搜索树中，左子树所有节点值都小于根节点值。',
 '<p>二叉搜索树中，左子树所有节点值都小于根节点值。</p>',
 '["正确","错误"]', 'true',
 2, '计算机题库批量导入', '数据结构',
 '<p>这是二叉搜索树的核心性质之一。</p>', NOW()),

(@teacher_id, 'TRUE_FALSE', 'HTTPS 比 HTTP 多了一层 TLS/SSL 加密。',
 '<p>HTTPS 比 HTTP 多了一层 TLS/SSL 加密。</p>',
 '["正确","错误"]', 'true',
 1, '计算机题库批量导入', '应用层协议',
 '<p>HTTPS = HTTP over TLS。</p>', NOW()),

(@teacher_id, 'TRUE_FALSE', '索引一定会提升数据库写入性能。',
 '<p>索引一定会提升数据库写入性能。</p>',
 '["正确","错误"]', 'false',
 2, '计算机题库批量导入', '数据库优化',
 '<p>索引会增加写入维护成本，不一定提升写性能。</p>', NOW()),

(@teacher_id, 'TRUE_FALSE', 'Git 是分布式版本控制系统。',
 '<p>Git 是分布式版本控制系统。</p>',
 '["正确","错误"]', 'true',
 1, '计算机题库批量导入', '开发工具',
 '<p>Git 支持分布式协作，每个副本都可独立提交。</p>', NOW()),

-- 填空题（3）
(@teacher_id, 'FILL_BLANK', 'HTTP 状态码 _____ 表示“资源不存在”。',
 '<p>HTTP 状态码 _____ 表示“资源不存在”。</p>',
 NULL, '"404"',
 1, '计算机题库批量导入', 'Web开发',
 '<p>404 Not Found。</p>', NOW()),

(@teacher_id, 'FILL_BLANK', 'SQL 中用于排序结果集的关键字是 _____。',
 '<p>SQL 中用于排序结果集的关键字是 _____。</p>',
 NULL, '"ORDER BY"',
 1, '计算机题库批量导入', '数据库基础',
 '<p>ORDER BY 用于升序/降序排序。</p>', NOW()),

(@teacher_id, 'FILL_BLANK', 'Linux 中切换目录的命令是 _____。',
 '<p>Linux 中切换目录的命令是 _____。</p>',
 NULL, '"cd"',
 1, '计算机题库批量导入', 'Linux基础',
 '<p>cd: change directory。</p>', NOW()),

-- 简答题（3）
(@teacher_id, 'SHORT_ANSWER', '请简述 TCP 三次握手的目的与过程。',
 '<p>请简述 TCP 三次握手的目的与过程。</p>',
 NULL, NULL,
 3, '计算机题库批量导入', '计算机网络',
 '<p>要点：同步序列号、确认双方收发能力、建立可靠连接。</p>', NOW()),

(@teacher_id, 'SHORT_ANSWER', '请说明数据库事务的 ACID 四个特性。',
 '<p>请说明数据库事务的 ACID 四个特性。</p>',
 NULL, NULL,
 2, '计算机题库批量导入', '数据库基础',
 '<p>原子性、一致性、隔离性、持久性。</p>', NOW()),

(@teacher_id, 'SHORT_ANSWER', '结合实际说明什么是“索引失效”，并给出一个避免建议。',
 '<p>结合实际说明什么是“索引失效”，并给出一个避免建议。</p>',
 NULL, NULL,
 4, '计算机题库批量导入', '数据库优化',
 '<p>示例：函数操作、前导模糊查询等导致无法走索引；建议优化SQL并按查询模式建索引。</p>', NOW());

SELECT COUNT(*) AS imported_count
FROM questions
WHERE chapter = '计算机题库批量导入'
  AND creator_id = @teacher_id;
