package com.campus.exam.service;

import com.campus.exam.domain.Exam;
import com.campus.exam.domain.UserRole;
import com.campus.exam.repository.ExamAttemptRepository;
import com.campus.exam.repository.ExamQuestionRepository;
import com.campus.exam.repository.ExamRepository;
import com.campus.exam.security.AuthenticatedUser;
import com.campus.exam.domain.ExamAttempt;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Service
public class TeacherGradeService {

    private final ExamRepository examRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final GradingService gradingService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TeacherGradeService(
            ExamRepository examRepository,
            ExamQuestionRepository examQuestionRepository,
            ExamAttemptRepository examAttemptRepository,
            GradingService gradingService) {
        this.examRepository = examRepository;
        this.examQuestionRepository = examQuestionRepository;
        this.examAttemptRepository = examAttemptRepository;
        this.gradingService = gradingService;
    }

    @Transactional
    public ExamAttempt gradeSubjective(
            AuthenticatedUser user,
            Long examId,
            Long attemptId,
            Long questionId,
            BigDecimal score,
            String comment) {
        Exam exam = examRepository.findById(examId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!exam.getCreatorId().equals(user.id()) && !UserRole.ADMIN.name().equals(user.role())
                && !UserRole.COLLEGE_ADMIN.name().equals(user.role())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        ExamAttempt att = examAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!att.getExamId().equals(examId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        boolean hasQuestion = examQuestionRepository.findByExamIdOrderByOrderIndexAsc(examId).stream()
                .anyMatch(eq -> eq.getQuestionId().equals(questionId));
        if (!hasQuestion) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "题目不属于本场考试");
        }

        try {
            ObjectNode root;
            if (att.getSubjectiveScoresJson() == null || att.getSubjectiveScoresJson().isBlank()) {
                root = objectMapper.createObjectNode();
            } else {
                root = (ObjectNode) objectMapper.readTree(att.getSubjectiveScoresJson());
            }
            ObjectNode qn = objectMapper.createObjectNode();
            qn.put("score", score.doubleValue());
            if (comment != null && !comment.isBlank()) {
                qn.put("comment", comment);
            }
            root.set(String.valueOf(questionId), qn);
            att.setSubjectiveScoresJson(objectMapper.writeValueAsString(root));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "批阅数据保存失败");
        }

        BigDecimal subjective = gradingService.sumSubjective(att.getSubjectiveScoresJson());
        att.setSubjectiveScore(subjective);
        BigDecimal obj = att.getObjectiveScore() != null ? att.getObjectiveScore() : BigDecimal.ZERO;
        att.setTotalScore(obj.add(subjective));
        return examAttemptRepository.save(att);
    }
}
