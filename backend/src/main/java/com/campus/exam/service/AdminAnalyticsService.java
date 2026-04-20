package com.campus.exam.service;

import com.campus.exam.domain.*;
import com.campus.exam.repository.*;
import com.campus.exam.web.dto.AdminAnalyticsDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminAnalyticsService {

    private final UserAccountRepository userAccountRepository;
    private final ExamRepository examRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final QuestionRepository questionRepository;

    public AdminAnalyticsService(
            UserAccountRepository userAccountRepository,
            ExamRepository examRepository,
            ExamAttemptRepository examAttemptRepository,
            QuestionRepository questionRepository) {
        this.userAccountRepository = userAccountRepository;
        this.examRepository = examRepository;
        this.examAttemptRepository = examAttemptRepository;
        this.questionRepository = questionRepository;
    }

    public AdminAnalyticsDto overview() {
        long students = userAccountRepository.countByRole(UserRole.STUDENT);
        long teachers = userAccountRepository.countByRole(UserRole.TEACHER);
        long pubExams = examRepository.findByStatus(ExamStatus.PUBLISHED).size();

        List<ExamAttempt> submitted = examAttemptRepository.findAll().stream()
                .filter(a -> a.getStatus() != AttemptStatus.IN_PROGRESS && a.getTotalScore() != null)
                .toList();
        long attCount = submitted.size();
        double partRate = students > 0 ? (double) attCount / (double) students : 0.0;

        BigDecimal sum = submitted.stream()
                .map(ExamAttempt::getTotalScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Double avgAll = attCount > 0
                ? sum.divide(BigDecimal.valueOf(attCount), 4, RoundingMode.HALF_UP).doubleValue()
                : null;

        List<Question> qs = questionRepository.findAll();
        double avgDiff = qs.isEmpty()
                ? 0
                : qs.stream().mapToInt(q -> q.getDifficulty() != null ? q.getDifficulty() : 3).average().orElse(0);

        Map<Long, List<ExamAttempt>> byExam = submitted.stream().collect(Collectors.groupingBy(ExamAttempt::getExamId));
        List<AdminAnalyticsDto.ExamEffectRow> effects = byExam.entrySet().stream()
                .map(en -> {
                    Long examId = en.getKey();
                    List<ExamAttempt> list = en.getValue();
                    Exam ex = examRepository.findById(examId).orElse(null);
                    String title = ex != null ? ex.getTitle() : "(考试已删除)";
                    BigDecimal s = list.stream().map(ExamAttempt::getTotalScore).reduce(BigDecimal.ZERO, BigDecimal::add);
                    double avg = s.divide(BigDecimal.valueOf(list.size()), 4, RoundingMode.HALF_UP).doubleValue();
                    return new AdminAnalyticsDto.ExamEffectRow(examId, title, list.size(), avg);
                })
                .sorted(Comparator.comparingLong(AdminAnalyticsDto.ExamEffectRow::attemptCount).reversed())
                .limit(10)
                .toList();

        return new AdminAnalyticsDto(
                students,
                teachers,
                pubExams,
                attCount,
                partRate,
                avgAll,
                avgDiff,
                effects);
    }
}
