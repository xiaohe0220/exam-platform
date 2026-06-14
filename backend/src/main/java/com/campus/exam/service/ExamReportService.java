package com.campus.exam.service;

import com.campus.exam.domain.*;
import com.campus.exam.repository.*;
import com.campus.exam.security.AuthenticatedUser;
import com.campus.exam.util.AnswerJsonUtil;
import com.campus.exam.util.OptionShuffleUtil;
import com.campus.exam.web.dto.ExamQuestionStatsDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;

@Service
public class ExamReportService {

    private final ExamRepository examRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final QuestionRepository questionRepository;
    private final UserAccountRepository userAccountRepository;
    private final ExamService examService;
    private final GradingService gradingService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ExamReportService(
            ExamRepository examRepository,
            ExamQuestionRepository examQuestionRepository,
            ExamAttemptRepository examAttemptRepository,
            QuestionRepository questionRepository,
            UserAccountRepository userAccountRepository,
            ExamService examService,
            GradingService gradingService) {
        this.examRepository = examRepository;
        this.examQuestionRepository = examQuestionRepository;
        this.examAttemptRepository = examAttemptRepository;
        this.questionRepository = questionRepository;
        this.userAccountRepository = userAccountRepository;
        this.examService = examService;
        this.gradingService = gradingService;
    }

    public List<ExamQuestionStatsDto> questionStats(AuthenticatedUser user, Long examId) {
        examService.requireOwnedExam(user, examId);
        Map<Long, Question> qmap = loadQuestionMap(examId);
        List<ExamAttempt> attempts = examAttemptRepository.findByExamId(examId).stream()
                .filter(a -> a.getStatus() != AttemptStatus.IN_PROGRESS)
                .toList();

        List<ExamQuestionStatsDto> out = new ArrayList<>();
        for (ExamQuestion eq : examQuestionRepository.findByExamIdOrderByOrderIndexAsc(examId)) {
            Question q = qmap.get(eq.getQuestionId());
            if (q == null) {
                continue;
            }
            long answered = 0;
            long correct = 0;
            BigDecimal earnedSum = BigDecimal.ZERO;
            for (ExamAttempt a : attempts) {
                Map<Long, String> display = AnswerJsonUtil.toMap(a.getAnswersJson());
                String displayAns = display.get(q.getId());
                if (displayAns != null && !displayAns.isBlank()) {
                    answered++;
                }
                BigDecimal earned = earnedScore(a, q, eq, qmap, display);
                if (earned.compareTo(BigDecimal.ZERO) > 0) {
                    earnedSum = earnedSum.add(earned);
                }
                if (earned.compareTo(eq.getScore()) >= 0 && eq.getScore().compareTo(BigDecimal.ZERO) > 0) {
                    correct++;
                }
            }
            long wrong = Math.max(0, answered - correct);
            double rate = answered > 0 ? correct * 1.0 / answered : 0.0;
            BigDecimal avg = answered > 0
                    ? earnedSum.divide(BigDecimal.valueOf(answered), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            out.add(new ExamQuestionStatsDto(
                    q.getId(),
                    q.getTitle(),
                    q.getType().name(),
                    q.getChapter(),
                    q.getKnowledgePoint(),
                    eq.getScore(),
                    answered,
                    correct,
                    wrong,
                    Math.round(rate * 1000.0) / 1000.0,
                    avg));
        }
        return out;
    }

    public byte[] exportAttempts(AuthenticatedUser user, Long examId) {
        examService.requireOwnedExam(user, examId);
        Exam exam = examRepository.findById(examId).orElseThrow();
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Sheet sh = wb.createSheet("attempts");
            String[] headers = {
                    "attemptId", "username", "studentName", "className", "status",
                    "objectiveScore", "subjectiveScore", "totalScore", "switchCount",
                    "startedAt", "submittedAt", "examTitle"
            };
            header(sh, headers);
            int r = 1;
            for (ExamAttempt a : examAttemptRepository.findByExamId(examId)) {
                UserAccount u = userAccountRepository.findById(a.getUserId()).orElse(null);
                Row row = sh.createRow(r++);
                cell(row, 0, a.getId());
                cell(row, 1, u != null ? u.getUsername() : "");
                cell(row, 2, u != null ? u.getDisplayName() : "");
                cell(row, 3, u != null ? u.getClassName() : "");
                cell(row, 4, a.getStatus().name());
                cell(row, 5, a.getObjectiveScore());
                cell(row, 6, a.getSubjectiveScore());
                cell(row, 7, a.getTotalScore());
                cell(row, 8, a.getSwitchCount());
                cell(row, 9, a.getStartedAt());
                cell(row, 10, a.getSubmittedAt());
                cell(row, 11, exam.getTitle());
            }
            autosize(sh, headers.length);
            wb.write(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("导出答卷失败", e);
        }
    }

    public byte[] exportQuestionStats(AuthenticatedUser user, Long examId) {
        List<ExamQuestionStatsDto> stats = questionStats(user, examId);
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Sheet sh = wb.createSheet("question_stats");
            String[] headers = {
                    "questionId", "title", "type", "chapter", "knowledgePoint",
                    "maxScore", "answeredCount", "correctCount", "wrongCount", "correctRate", "avgEarnedScore"
            };
            header(sh, headers);
            int r = 1;
            for (ExamQuestionStatsDto s : stats) {
                Row row = sh.createRow(r++);
                cell(row, 0, s.questionId());
                cell(row, 1, s.title());
                cell(row, 2, s.type());
                cell(row, 3, s.chapter());
                cell(row, 4, s.knowledgePoint());
                cell(row, 5, s.maxScore());
                cell(row, 6, s.answeredCount());
                cell(row, 7, s.correctCount());
                cell(row, 8, s.wrongCount());
                cell(row, 9, s.correctRate());
                cell(row, 10, s.avgEarnedScore());
            }
            autosize(sh, headers.length);
            wb.write(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("导出题目统计失败", e);
        }
    }

    private BigDecimal earnedScore(
            ExamAttempt attempt,
            Question q,
            ExamQuestion eq,
            Map<Long, Question> qmap,
            Map<Long, String> display) {
        if (q.getType() == QuestionType.SHORT_ANSWER) {
            return subjectiveScore(attempt.getSubjectiveScoresJson(), q.getId());
        }
        Map<Long, String> original = OptionShuffleUtil.toOriginalAnswers(display, attempt.getShuffleJson(), qmap);
        return gradingService.isObjectiveCorrect(q, original.get(q.getId()))
                ? eq.getScore()
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal subjectiveScore(String json, Long questionId) {
        if (json == null || json.isBlank()) {
            return BigDecimal.ZERO;
        }
        try {
            JsonNode n = objectMapper.readTree(json).get(String.valueOf(questionId));
            if (n != null && n.has("score")) {
                return BigDecimal.valueOf(n.get("score").asDouble()).setScale(2, RoundingMode.HALF_UP);
            }
        } catch (Exception ignored) {
            /* ignore malformed subjective score json */
        }
        return BigDecimal.ZERO;
    }

    private Map<Long, Question> loadQuestionMap(Long examId) {
        Map<Long, Question> map = new HashMap<>();
        for (ExamQuestion eq : examQuestionRepository.findByExamIdOrderByOrderIndexAsc(examId)) {
            questionRepository.findById(eq.getQuestionId()).ifPresent(q -> map.put(q.getId(), q));
        }
        return map;
    }

    static void header(Sheet sh, String[] headers) {
        Row h = sh.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            h.createCell(i).setCellValue(headers[i]);
        }
    }

    static void autosize(Sheet sh, int n) {
        for (int i = 0; i < n; i++) {
            sh.autoSizeColumn(i);
        }
    }

    static void cell(Row row, int idx, Object value) {
        Cell c = row.createCell(idx);
        if (value == null) {
            c.setCellValue("");
        } else if (value instanceof Number n) {
            c.setCellValue(n.doubleValue());
        } else if (value instanceof BigDecimal bd) {
            c.setCellValue(bd.doubleValue());
        } else if (value instanceof Instant t) {
            c.setCellValue(t.toString());
        } else {
            c.setCellValue(String.valueOf(value));
        }
    }
}
