-- AI 五专题题库批量脚本（共 500 题）
-- 专题：
-- 1. 人工智能概述
-- 2. 提示词功能
-- 3. 智能体
-- 4. 大模型主要介绍
-- 5. 人工智能安全与伦理
--
-- 每个专题 100 题，五大题型均衡：单选/多选/判断/填空/简答 各 20 题
-- 用法：
--   mysql -uroot -p exam_platform < seed_ai_5topics_500_questions.sql

SET NAMES utf8mb4;

SET @teacher_id = (SELECT id FROM users WHERE username = 'teacher' LIMIT 1);
SET @teacher_id = COALESCE(@teacher_id, (SELECT id FROM users WHERE role = 'TEACHER' ORDER BY id LIMIT 1));
SET @must_have_teacher = IF(@teacher_id IS NULL, (SELECT 1/0), 1);

-- 清理同专题旧数据（避免重复导入）
DELETE FROM questions
WHERE creator_id = @teacher_id
  AND chapter IN ('人工智能概述','提示词功能','智能体','大模型主要介绍','人工智能安全与伦理');

WITH RECURSIVE seq(n) AS (
  SELECT 1
  UNION ALL
  SELECT n + 1 FROM seq WHERE n < 20
),
topics AS (
  SELECT 1 AS tid, '人工智能概述' AS chapter UNION ALL
  SELECT 2, '提示词功能' UNION ALL
  SELECT 3, '智能体' UNION ALL
  SELECT 4, '大模型主要介绍' UNION ALL
  SELECT 5, '人工智能安全与伦理'
),
types AS (
  SELECT 'SINGLE_CHOICE' AS qtype, '概念辨析' AS kp, 2 AS diff UNION ALL
  SELECT 'MULTIPLE_CHOICE', '综合理解', 3 UNION ALL
  SELECT 'TRUE_FALSE', '基础判断', 1 UNION ALL
  SELECT 'FILL_BLANK', '关键术语', 2 UNION ALL
  SELECT 'SHORT_ANSWER', '应用分析', 4
),
rows_src AS (
  SELECT
    t.chapter,
    tp.qtype,
    tp.kp,
    tp.diff,
    s.n
  FROM topics t
  CROSS JOIN types tp
  CROSS JOIN seq s
)
INSERT INTO questions (
  creator_id,
  type,
  title,
  content,
  options_json,
  correct_answer_json,
  difficulty,
  chapter,
  knowledge_point,
  answer_analysis,
  created_at
)
SELECT
  @teacher_id AS creator_id,
  r.qtype AS type,
  CASE r.qtype
    WHEN 'SINGLE_CHOICE' THEN CONCAT('【', r.chapter, '】单选题 #', LPAD(r.n, 2, '0'), '：下列对核心概念的描述最准确的是？')
    WHEN 'MULTIPLE_CHOICE' THEN CONCAT('【', r.chapter, '】多选题 #', LPAD(r.n, 2, '0'), '：请选择符合场景的选项（可多选）。')
    WHEN 'TRUE_FALSE' THEN CONCAT('【', r.chapter, '】判断题 #', LPAD(r.n, 2, '0'), '：请判断以下表述是否正确。')
    WHEN 'FILL_BLANK' THEN CONCAT('【', r.chapter, '】填空题 #', LPAD(r.n, 2, '0'), '：请补全关键术语。')
    ELSE CONCAT('【', r.chapter, '】简答题 #', LPAD(r.n, 2, '0'), '：结合实际简述你的理解。')
  END AS title,
  CASE r.qtype
    WHEN 'SINGLE_CHOICE' THEN CONCAT('<p>在“', r.chapter, '”主题中，第 ', r.n, ' 题：请选择最恰当的一项。</p>')
    WHEN 'MULTIPLE_CHOICE' THEN CONCAT('<p>在“', r.chapter, '”主题中，第 ', r.n, ' 题：可多选，请选择所有正确项。</p>')
    WHEN 'TRUE_FALSE' THEN CONCAT('<p>在“', r.chapter, '”主题中，第 ', r.n, ' 题：该陈述是否正确？</p>')
    WHEN 'FILL_BLANK' THEN CONCAT('<p>在“', r.chapter, '”主题中，第 ', r.n, ' 题：请填写空缺词汇 ____ 。</p>')
    ELSE CONCAT('<p>在“', r.chapter, '”主题中，第 ', r.n, ' 题：请从原理、应用与风险三个角度作答。</p>')
  END AS content,
  CASE r.qtype
    WHEN 'SINGLE_CHOICE' THEN JSON_ARRAY(
      CONCAT('选项A：', r.chapter, '中的核心概念定义（标准）'),
      CONCAT('选项B：', r.chapter, '中的常见误区'),
      CONCAT('选项C：', r.chapter, '中的片面表述'),
      CONCAT('选项D：', r.chapter, '中的无关描述')
    )
    WHEN 'MULTIPLE_CHOICE' THEN JSON_ARRAY(
      CONCAT('选项A：', r.chapter, '相关的正确做法1'),
      CONCAT('选项B：', r.chapter, '相关的正确做法2'),
      CONCAT('选项C：', r.chapter, '中的错误做法'),
      CONCAT('选项D：', r.chapter, '相关的正确做法3')
    )
    WHEN 'TRUE_FALSE' THEN JSON_ARRAY('正确', '错误')
    ELSE NULL
  END AS options_json,
  CASE r.qtype
    WHEN 'SINGLE_CHOICE' THEN '"A"'
    WHEN 'MULTIPLE_CHOICE' THEN '["A","B","D"]'
    WHEN 'TRUE_FALSE' THEN IF(MOD(r.n, 2) = 0, 'true', 'false')
    WHEN 'FILL_BLANK' THEN JSON_QUOTE(
      CASE
        WHEN r.chapter = '人工智能概述' THEN '感知-决策-执行'
        WHEN r.chapter = '提示词功能' THEN '角色-任务-约束'
        WHEN r.chapter = '智能体' THEN '感知-规划-行动'
        WHEN r.chapter = '大模型主要介绍' THEN '预训练-微调-推理'
        ELSE '安全-合规-责任'
      END
    )
    ELSE NULL
  END AS correct_answer_json,
  CASE
    WHEN r.qtype = 'SHORT_ANSWER' THEN LEAST(5, r.diff + IF(r.n > 10, 1, 0))
    ELSE r.diff
  END AS difficulty,
  r.chapter,
  CONCAT(r.kp, '（第', r.n, '组）') AS knowledge_point,
  CONCAT('<p>参考解析：本题围绕“', r.chapter, '”的', r.kp, '展开。答题时建议先给出定义，再结合实践场景说明，最后补充边界与风险。</p>') AS answer_analysis,
  NOW() AS created_at
FROM rows_src r;

SELECT
  chapter,
  type,
  COUNT(*) AS cnt
FROM questions
WHERE creator_id = @teacher_id
  AND chapter IN ('人工智能概述','提示词功能','智能体','大模型主要介绍','人工智能安全与伦理')
GROUP BY chapter, type
ORDER BY chapter, type;

SELECT
  chapter,
  COUNT(*) AS total_per_topic
FROM questions
WHERE creator_id = @teacher_id
  AND chapter IN ('人工智能概述','提示词功能','智能体','大模型主要介绍','人工智能安全与伦理')
GROUP BY chapter
ORDER BY chapter;
