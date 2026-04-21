-- 计算机题库（扩展版：基础/中等/拔高）
-- 用法：
--   mysql -uroot -p exam_platform < seed_computer_questions_large.sql
--
-- 说明：
-- 1) 默认挂到 teacher 账号下；若不存在则回退首个 TEACHER
-- 2) 先清理 chapter=计算机题库扩展导入 的旧题，避免重复

SET NAMES utf8mb4;

SET @teacher_id = (SELECT id FROM users WHERE username = 'teacher' LIMIT 1);
SET @teacher_id = COALESCE(@teacher_id, (SELECT id FROM users WHERE role='TEACHER' ORDER BY id LIMIT 1));
SET @must_have_teacher = IF(@teacher_id IS NULL, (SELECT 1/0), 1);

DELETE FROM questions
WHERE creator_id = @teacher_id
  AND chapter = '计算机题库扩展导入';

INSERT INTO questions (
  creator_id, type, title, content, options_json, correct_answer_json,
  difficulty, chapter, knowledge_point, answer_analysis, created_at
)
VALUES
-- =========================
-- 基础（difficulty=1）
-- =========================
(@teacher_id,'SINGLE_CHOICE','CPU 的英文全称是？',
 '<p>CPU 的英文全称是？</p>',
 '["Central Processing Unit","Computer Personal Unit","Central Program Unit","Control Process Unit"]','"A"',
 1,'计算机题库扩展导入','计算机基础','<p>CPU 即中央处理器。</p>',NOW()),

(@teacher_id,'SINGLE_CHOICE','以下哪项属于输入设备？',
 '<p>以下哪项属于输入设备？</p>',
 '["显示器","键盘","打印机","音箱"]','"B"',
 1,'计算机题库扩展导入','计算机基础','<p>键盘用于输入数据。</p>',NOW()),

(@teacher_id,'SINGLE_CHOICE','二进制数由哪两个数字组成？',
 '<p>二进制数由哪两个数字组成？</p>',
 '["0和1","1和2","2和3","0和9"]','"A"',
 1,'计算机题库扩展导入','数据表示','<p>二进制只包含 0 与 1。</p>',NOW()),

(@teacher_id,'SINGLE_CHOICE','URL 中用于分隔协议和地址的是？',
 '<p>URL 中用于分隔协议和地址的是？</p>',
 '["://","::","//","=="]','"A"',
 1,'计算机题库扩展导入','Web基础','<p>例如 https://example.com。</p>',NOW()),

(@teacher_id,'SINGLE_CHOICE','下列哪个是开源操作系统？',
 '<p>下列哪个是开源操作系统？</p>',
 '["Linux","Windows","macOS","iOS"]','"A"',
 1,'计算机题库扩展导入','操作系统','<p>Linux 为开源系统。</p>',NOW()),

(@teacher_id,'MULTIPLE_CHOICE','下列哪些属于浏览器？',
 '<p>下列哪些属于浏览器？</p>',
 '["Chrome","Firefox","Nginx","Edge"]','["A","B","D"]',
 1,'计算机题库扩展导入','Web基础','<p>Nginx 是 Web 服务器而非浏览器。</p>',NOW()),

(@teacher_id,'MULTIPLE_CHOICE','下列哪些是常见编程语言？',
 '<p>下列哪些是常见编程语言？</p>',
 '["Java","Python","MySQL","C++"]','["A","B","D"]',
 1,'计算机题库扩展导入','编程基础','<p>MySQL 是数据库管理系统。</p>',NOW()),

(@teacher_id,'MULTIPLE_CHOICE','下列哪些是文件扩展名？',
 '<p>下列哪些是文件扩展名？</p>',
 '[".txt",".jpg","/home"," .pdf"]','["A","B","D"]',
 1,'计算机题库扩展导入','计算机基础','<p>/home 是路径，不是扩展名。</p>',NOW()),

(@teacher_id,'TRUE_FALSE','1KB 等于 1024B。',
 '<p>1KB 等于 1024B。</p>',
 '["正确","错误"]','true',
 1,'计算机题库扩展导入','数据表示','<p>在计算机基础语境常按 1024 换算。</p>',NOW()),

(@teacher_id,'TRUE_FALSE','HTTP 是一种数据库语言。',
 '<p>HTTP 是一种数据库语言。</p>',
 '["正确","错误"]','false',
 1,'计算机题库扩展导入','应用层协议','<p>HTTP 是超文本传输协议。</p>',NOW()),

(@teacher_id,'FILL_BLANK','Linux 中查看当前工作目录命令是 _____。',
 '<p>Linux 中查看当前工作目录命令是 _____。</p>',
 NULL,'"pwd"',
 1,'计算机题库扩展导入','Linux基础','<p>pwd: print working directory。</p>',NOW()),

(@teacher_id,'FILL_BLANK','HTTP 默认端口是 _____。',
 '<p>HTTP 默认端口是 _____。</p>',
 NULL,'"80"',
 1,'计算机题库扩展导入','应用层协议','<p>HTTPS 默认端口是 443。</p>',NOW()),

(@teacher_id,'SHORT_ANSWER','简述你理解的“操作系统”作用（20字以上）。',
 '<p>简述你理解的“操作系统”作用（20字以上）。</p>',
 NULL,NULL,
 1,'计算机题库扩展导入','操作系统','<p>可从资源管理、进程调度、人机交互角度作答。</p>',NOW()),

-- =========================
-- 中等（difficulty=2-3）
-- =========================
(@teacher_id,'SINGLE_CHOICE','DNS 主要用于解决什么问题？',
 '<p>DNS 主要用于解决什么问题？</p>',
 '["域名解析","数据加密","页面渲染","压缩传输"]','"A"',
 2,'计算机题库扩展导入','计算机网络','<p>DNS 将域名映射到 IP。</p>',NOW()),

(@teacher_id,'SINGLE_CHOICE','SQL 中用于分组统计的关键字是？',
 '<p>SQL 中用于分组统计的关键字是？</p>',
 '["ORDER BY","GROUP BY","LIMIT","HAVING ONLY"]','"B"',
 2,'计算机题库扩展导入','数据库基础','<p>GROUP BY 与聚合函数常配合使用。</p>',NOW()),

(@teacher_id,'SINGLE_CHOICE','在 Linux 中修改文件权限最常用命令是？',
 '<p>在 Linux 中修改文件权限最常用命令是？</p>',
 '["chown","chmod","ps","touch"]','"B"',
 2,'计算机题库扩展导入','Linux基础','<p>chmod 用于修改读写执行权限。</p>',NOW()),

(@teacher_id,'SINGLE_CHOICE','RESTful 设计中，删除资源常用哪种方法？',
 '<p>RESTful 设计中，删除资源常用哪种方法？</p>',
 '["GET","POST","PUT","DELETE"]','"D"',
 2,'计算机题库扩展导入','Web开发','<p>DELETE 语义用于删除。</p>',NOW()),

(@teacher_id,'SINGLE_CHOICE','下列哪项最能降低 SQL 注入风险？',
 '<p>下列哪项最能降低 SQL 注入风险？</p>',
 '["字符串拼接SQL","参数化查询","关闭数据库日志","增加CPU核数"]','"B"',
 3,'计算机题库扩展导入','网络安全','<p>参数化查询可避免恶意拼接。</p>',NOW()),

(@teacher_id,'SINGLE_CHOICE','事务隔离级别中，哪一个隔离性最高？',
 '<p>事务隔离级别中，哪一个隔离性最高？</p>',
 '["READ UNCOMMITTED","READ COMMITTED","REPEATABLE READ","SERIALIZABLE"]','"D"',
 3,'计算机题库扩展导入','数据库事务','<p>SERIALIZABLE 隔离最高，代价也最高。</p>',NOW()),

(@teacher_id,'MULTIPLE_CHOICE','下列哪些属于常见 NoSQL 数据库？',
 '<p>下列哪些属于常见 NoSQL 数据库？</p>',
 '["Redis","MongoDB","MySQL","Elasticsearch"]','["A","B","D"]',
 2,'计算机题库扩展导入','数据库基础','<p>MySQL 属于关系型数据库。</p>',NOW()),

(@teacher_id,'MULTIPLE_CHOICE','下列哪些是提升 SQL 查询性能的常见手段？',
 '<p>下列哪些是提升 SQL 查询性能的常见手段？</p>',
 '["建立合适索引","避免SELECT *","使用分页","所有列都建立索引"]','["A","B","C"]',
 3,'计算机题库扩展导入','数据库优化','<p>过多索引会拖慢写入，不宜所有列建索引。</p>',NOW()),

(@teacher_id,'MULTIPLE_CHOICE','在 Git 中，以下哪些命令会改变提交历史？',
 '<p>在 Git 中，以下哪些命令会改变提交历史？</p>',
 '["rebase","commit --amend","checkout","reset --hard"]','["A","B","D"]',
 3,'计算机题库扩展导入','开发工具','<p>checkout 一般不改历史；其余会改提交结构或指针。</p>',NOW()),

(@teacher_id,'TRUE_FALSE','MySQL 的 InnoDB 引擎支持事务。',
 '<p>MySQL 的 InnoDB 引擎支持事务。</p>',
 '["正确","错误"]','true',
 2,'计算机题库扩展导入','数据库事务','<p>InnoDB 支持事务与行级锁。</p>',NOW()),

(@teacher_id,'TRUE_FALSE','数据库索引越多越好。',
 '<p>数据库索引越多越好。</p>',
 '["正确","错误"]','false',
 2,'计算机题库扩展导入','数据库优化','<p>索引会增加写入成本和存储成本。</p>',NOW()),

(@teacher_id,'FILL_BLANK','TCP 建立连接需要 _____ 次握手。',
 '<p>TCP 建立连接需要 _____ 次握手。</p>',
 NULL,'"3"',
 2,'计算机题库扩展导入','计算机网络','<p>经典三次握手。</p>',NOW()),

(@teacher_id,'FILL_BLANK','SQL 中用于限制返回行数的关键字是 _____。',
 '<p>SQL 中用于限制返回行数的关键字是 _____。</p>',
 NULL,'"LIMIT"',
 2,'计算机题库扩展导入','数据库基础','<p>常与 ORDER BY 一起使用。</p>',NOW()),

(@teacher_id,'SHORT_ANSWER','请说明 Cookie 与 Session 的区别与联系。',
 '<p>请说明 Cookie 与 Session 的区别与联系。</p>',
 NULL,NULL,
 3,'计算机题库扩展导入','Web开发','<p>可从存储位置、容量、安全性、生命周期回答。</p>',NOW()),

-- =========================
-- 拔高（difficulty=4-5）
-- =========================
(@teacher_id,'SINGLE_CHOICE','在高并发系统中，最常用于削峰填谷的组件是？',
 '<p>在高并发系统中，最常用于削峰填谷的组件是？</p>',
 '["消息队列","DNS","CDN","浏览器缓存"]','"A"',
 4,'计算机题库扩展导入','系统设计',
 '<p>消息队列通过异步解耦和缓冲实现削峰。</p>',NOW()),

(@teacher_id,'SINGLE_CHOICE','CAP 理论中，在网络分区发生时必须满足的是？',
 '<p>CAP 理论中，在网络分区发生时必须满足的是？</p>',
 '["一致性与可用性都完全满足","分区容错性","只要高性能","只要低延迟"]','"B"',
 4,'计算机题库扩展导入','分布式系统',
 '<p>分区出现时必须保证 P，再权衡 C 与 A。</p>',NOW()),

(@teacher_id,'SINGLE_CHOICE','以下哪种锁机制通常能减少写写冲突的并发问题？',
 '<p>以下哪种锁机制通常能减少写写冲突的并发问题？</p>',
 '["乐观锁版本号","随机等待","关闭事务","全表扫描"]','"A"',
 4,'计算机题库扩展导入','并发控制',
 '<p>乐观锁通过版本号/时间戳检测冲突。</p>',NOW()),

(@teacher_id,'MULTIPLE_CHOICE','下列哪些是常见的缓存一致性策略？',
 '<p>下列哪些是常见的缓存一致性策略？</p>',
 '["Cache Aside","Read Through","Write Through","随机淘汰即可一致"]','["A","B","C"]',
 4,'计算机题库扩展导入','系统设计',
 '<p>随机淘汰并不能保证一致性。</p>',NOW()),

(@teacher_id,'MULTIPLE_CHOICE','下列哪些场景适合使用消息队列？',
 '<p>下列哪些场景适合使用消息队列？</p>',
 '["异步发送通知","秒杀削峰","系统解耦","替代所有数据库事务"]','["A","B","C"]',
 4,'计算机题库扩展导入','系统设计',
 '<p>消息队列不能替代数据库事务语义。</p>',NOW()),

(@teacher_id,'TRUE_FALSE','分布式锁一定比单机锁性能更高。',
 '<p>分布式锁一定比单机锁性能更高。</p>',
 '["正确","错误"]','false',
 4,'计算机题库扩展导入','分布式系统',
 '<p>分布式锁引入网络开销与一致性成本。</p>',NOW()),

(@teacher_id,'TRUE_FALSE','幂等设计可降低重复请求造成的副作用风险。',
 '<p>幂等设计可降低重复请求造成的副作用风险。</p>',
 '["正确","错误"]','true',
 4,'计算机题库扩展导入','系统设计',
 '<p>常用于支付、下单、回调等关键接口。</p>',NOW()),

(@teacher_id,'FILL_BLANK','在数据库中，用于回滚到某个事务中间点的机制叫 _____。',
 '<p>在数据库中，用于回滚到某个事务中间点的机制叫 _____。</p>',
 NULL,'"SAVEPOINT"',
 4,'计算机题库扩展导入','数据库事务',
 '<p>SAVEPOINT 可进行部分回滚。</p>',NOW()),

(@teacher_id,'FILL_BLANK','Redis 常见持久化机制有 RDB 和 _____。',
 '<p>Redis 常见持久化机制有 RDB 和 _____。</p>',
 NULL,'"AOF"',
 4,'计算机题库扩展导入','缓存系统',
 '<p>AOF 记录写命令日志。</p>',NOW()),

(@teacher_id,'SHORT_ANSWER','请设计一个“高并发抢课”系统的核心方案，至少包含：限流、库存扣减、幂等与补偿。',
 '<p>请设计一个“高并发抢课”系统的核心方案，至少包含：限流、库存扣减、幂等与补偿。</p>',
 NULL,NULL,
 5,'计算机题库扩展导入','系统设计',
 '<p>重点考察架构拆分、流量控制、消息队列、最终一致性与容错补偿。</p>',NOW()),

(@teacher_id,'SHORT_ANSWER','请比较悲观锁与乐观锁在电商库存场景中的适用性。',
 '<p>请比较悲观锁与乐观锁在电商库存场景中的适用性。</p>',
 NULL,NULL,
 4,'计算机题库扩展导入','并发控制',
 '<p>可从冲突概率、吞吐、实现复杂度、失败重试成本分析。</p>',NOW()),

(@teacher_id,'SHORT_ANSWER','说明你对“分库分表后跨库事务与查询”问题的理解，并给出可行治理策略。',
 '<p>说明你对“分库分表后跨库事务与查询”问题的理解，并给出可行治理策略。</p>',
 NULL,NULL,
 5,'计算机题库扩展导入','分布式系统',
 '<p>可从最终一致性、柔性事务、聚合查询、异步对账等角度回答。</p>',NOW());

SELECT
  COUNT(*) AS total_count,
  SUM(CASE WHEN difficulty = 1 THEN 1 ELSE 0 END) AS basic_count,
  SUM(CASE WHEN difficulty IN (2,3) THEN 1 ELSE 0 END) AS medium_count,
  SUM(CASE WHEN difficulty IN (4,5) THEN 1 ELSE 0 END) AS advanced_count
FROM questions
WHERE creator_id = @teacher_id
  AND chapter = '计算机题库扩展导入';
