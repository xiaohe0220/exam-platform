package com.campus.exam.service;

import com.campus.exam.domain.*;
import com.campus.exam.repository.ExamQuestionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class GradingService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExamQuestionRepository examQuestionRepository;

    public GradingService(ExamQuestionRepository examQuestionRepository) {
        this.examQuestionRepository = examQuestionRepository;
    }

    /**
     * 计算客观题得分；简答题不计分，需教师批阅后累计到主观分。
     */
    public BigDecimal gradeObjective(Long examId, Map<Long, String> answersByQuestion, Map<Long, Question> questionMap) {
        List<ExamQuestion> items = examQuestionRepository.findByExamIdOrderByOrderIndexAsc(examId);
        BigDecimal sum = BigDecimal.ZERO;
        for (ExamQuestion eq : items) {
            Question q = questionMap.get(eq.getQuestionId());
            if (q == null) {
                continue;
            }
            if (q.getType() == QuestionType.SHORT_ANSWER) {
                continue;
            }
            String raw = answersByQuestion.get(q.getId());
            boolean ok = match(q, raw);
            if (ok) {
                sum = sum.add(eq.getScore());
            }
        }
        return sum.setScale(2, RoundingMode.HALF_UP);
    }

    /** 判断单题客观答案是否正确（入参需与题库选项字母一致；已做乱序映射后的答案）。 */
    public boolean isObjectiveCorrect(Question q, String rawAnswer) {
        return match(q, rawAnswer);
    }

    public BigDecimal sumSubjective(String subjectiveScoresJson) {
        if (subjectiveScoresJson == null || subjectiveScoresJson.isBlank()) {
            return BigDecimal.ZERO;
        }
        try {
            JsonNode root = objectMapper.readTree(subjectiveScoresJson);
            BigDecimal total = BigDecimal.ZERO;
            Iterator<String> names = root.fieldNames();
            while (names.hasNext()) {
                JsonNode node = root.get(names.next());
                if (node != null && node.has("score")) {
                    total = total.add(BigDecimal.valueOf(node.get("score").asDouble()));
                }
            }
            return total.setScale(2, RoundingMode.HALF_UP);
        } catch (JsonProcessingException e) {
            return BigDecimal.ZERO;
        }
    }

    private boolean match(Question q, String rawAnswer) {
        if (rawAnswer == null) {
            return false;
        }
        if (q.getType() == QuestionType.SHORT_ANSWER) {
            return false;
        }
        String correct = q.getCorrectAnswerJson();
        if (correct == null) {
            return false;
        }
        try {
            return switch (q.getType()) {
                case SINGLE_CHOICE -> matchSingle(rawAnswer, correct);
                case MULTIPLE_CHOICE -> matchMulti(rawAnswer, correct);
                case TRUE_FALSE -> matchTf(rawAnswer, correct);
                case FILL_BLANK -> normalize(trimQuotes(rawAnswer))
                        .equalsIgnoreCase(normalize(trimQuotes(correct)));
                case SHORT_ANSWER -> false;
            };
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 单选题：题库中答案常存为 JSON 文本（如 "A"），与学生提交的 A 需解析或去引号后再比较。
     */
    private boolean matchSingle(String rawAnswer, String correct) throws JsonProcessingException {
        String s = normalize(trimQuotes(rawAnswer.trim()));
        String cRaw = correct.trim();
        String c;
        try {
            JsonNode node = objectMapper.readTree(cRaw);
            if (node.isTextual()) {
                c = normalize(node.asText());
            } else {
                c = normalize(trimQuotes(cRaw));
            }
        } catch (Exception e) {
            c = normalize(trimQuotes(cRaw));
        }
        if (s.isEmpty() || c.isEmpty()) {
            return false;
        }
        return s.equalsIgnoreCase(c);
    }

    private static String trimQuotes(String s) {
        if (s == null) {
            return "";
        }
        s = s.trim();
        if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
            return s.substring(1, s.length() - 1).trim();
        }
        return s;
    }

    private boolean matchTf(String raw, String correctJson) throws JsonProcessingException {
        boolean a = parseBool(raw);
        boolean c;
        if (correctJson.trim().equalsIgnoreCase("true") || correctJson.trim().equalsIgnoreCase("false")) {
            c = Boolean.parseBoolean(correctJson.trim());
        } else {
            c = objectMapper.readTree(correctJson).asBoolean();
        }
        return a == c;
    }

    private static boolean parseBool(String raw) {
        String s = raw.trim().toLowerCase();
        return s.equals("true") || s.equals("\"true\"") || s.equals("1") || s.equals("yes");
    }

    private boolean matchMulti(String raw, String correct) throws JsonProcessingException {
        Set<String> student = parseChoiceSet(raw);
        Set<String> expected = parseChoiceSet(correct);
        return student.equals(expected);
    }

    private Set<String> parseChoiceSet(String jsonOrRaw) throws JsonProcessingException {
        String t = jsonOrRaw.trim();
        Set<String> set = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        if (t.startsWith("[")) {
            ArrayNode arr = (ArrayNode) objectMapper.readTree(t);
            for (JsonNode n : arr) {
                if (n.isTextual()) {
                    set.add(normalize(trimQuotes(n.asText())));
                } else if (n.isBoolean()) {
                    set.add(String.valueOf(n.booleanValue()));
                } else {
                    set.add(normalize(trimQuotes(String.valueOf(n))));
                }
            }
        } else {
            set.add(normalize(trimQuotes(t)));
        }
        return set;
    }

    private static String normalize(String s) {
        return s == null ? "" : s.trim();
    }
}
