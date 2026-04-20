package com.campus.exam.service;

import com.campus.exam.domain.AttemptStatus;
import com.campus.exam.domain.ExamAttempt;
import com.campus.exam.domain.ExamQuestion;
import com.campus.exam.repository.ExamAttemptRepository;
import com.campus.exam.repository.ExamQuestionRepository;
import com.campus.exam.security.AuthenticatedUser;
import com.campus.exam.web.dto.ExamStatsDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class ExamStatsService {

    private static final int DEFAULT_PASS_LINE = 60;

    private final ExamAttemptRepository examAttemptRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final ExamService examService;

    public ExamStatsService(
            ExamAttemptRepository examAttemptRepository,
            ExamQuestionRepository examQuestionRepository,
            ExamService examService) {
        this.examAttemptRepository = examAttemptRepository;
        this.examQuestionRepository = examQuestionRepository;
        this.examService = examService;
    }

    public ExamStatsDto stats(AuthenticatedUser user, Long examId) {
        examService.requireOwnedExam(user, examId);
        List<ExamQuestion> qs = examQuestionRepository.findByExamIdOrderByOrderIndexAsc(examId);
        BigDecimal full = qs.stream().map(ExamQuestion::getScore).reduce(BigDecimal.ZERO, BigDecimal::add);

        List<ExamAttempt> attempts = examAttemptRepository.findByExamId(examId);
        long submitted = attempts.stream()
                .filter(a -> a.getStatus() != AttemptStatus.IN_PROGRESS)
                .count();

        List<BigDecimal> scores = attempts.stream()
                .filter(a -> a.getStatus() != AttemptStatus.IN_PROGRESS)
                .map(ExamAttempt::getTotalScore)
                .filter(s -> s != null)
                .toList();

        if (scores.isEmpty()) {
            return new ExamStatsDto(
                    attempts.size(),
                    submitted,
                    full,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    0,
                    DEFAULT_PASS_LINE);
        }

        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal max = scores.get(0);
        BigDecimal min = scores.get(0);
        for (BigDecimal s : scores) {
            sum = sum.add(s);
            if (s.compareTo(max) > 0) {
                max = s;
            }
            if (s.compareTo(min) < 0) {
                min = s;
            }
        }
        BigDecimal avg = sum.divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP);
        BigDecimal passLineBd = full.multiply(BigDecimal.valueOf(DEFAULT_PASS_LINE)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        long pass = scores.stream().filter(s -> s.compareTo(passLineBd) >= 0).count();

        return new ExamStatsDto(
                attempts.size(),
                submitted,
                full,
                avg,
                max,
                min,
                pass,
                DEFAULT_PASS_LINE);
    }
}
