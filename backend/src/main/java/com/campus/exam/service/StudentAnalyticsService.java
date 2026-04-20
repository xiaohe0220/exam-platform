package com.campus.exam.service;

import com.campus.exam.domain.*;
import com.campus.exam.repository.*;
import com.campus.exam.security.AuthenticatedUser;
import com.campus.exam.util.AnswerJsonUtil;
import com.campus.exam.util.OptionShuffleUtil;
import com.campus.exam.web.dto.StudentAttemptHistoryItemDto;
import com.campus.exam.web.dto.StudentOverviewDto;
import com.campus.exam.web.dto.WrongQuestionItemDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;

@Service
public class StudentAnalyticsService {

    private final ExamAttemptRepository examAttemptRepository;
    private final ExamRepository examRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final QuestionRepository questionRepository;
    private final UserAccountRepository userAccountRepository;
    private final GradingService gradingService;

    public StudentAnalyticsService(
            ExamAttemptRepository examAttemptRepository,
            ExamRepository examRepository,
            ExamQuestionRepository examQuestionRepository,
            QuestionRepository questionRepository,
            UserAccountRepository userAccountRepository,
            GradingService gradingService) {
        this.examAttemptRepository = examAttemptRepository;
        this.examRepository = examRepository;
        this.examQuestionRepository = examQuestionRepository;
        this.questionRepository = questionRepository;
        this.userAccountRepository = userAccountRepository;
        this.gradingService = gradingService;
    }

    public List<WrongQuestionItemDto> wrongQuestions(AuthenticatedUser user) {
        requireStudent(user);
        List<WrongQuestionItemDto> out = new ArrayList<>();
        List<ExamAttempt> attempts = examAttemptRepository.findByUserId(user.id()).stream()
                .filter(a -> a.getStatus() != AttemptStatus.IN_PROGRESS)
                .toList();
        for (ExamAttempt att : attempts) {
            Exam exam = examRepository.findById(att.getExamId()).orElse(null);
            if (exam == null) {
                continue;
            }
            Map<Long, Question> qmap = loadQuestionMap(att.getExamId());
            Map<Long, String> display = AnswerJsonUtil.toMap(att.getAnswersJson());
            Map<Long, String> graded = OptionShuffleUtil.toOriginalAnswers(display, att.getShuffleJson(), qmap);
            for (ExamQuestion eq : examQuestionRepository.findByExamIdOrderByOrderIndexAsc(att.getExamId())) {
                Question q = qmap.get(eq.getQuestionId());
                if (q == null || q.getType() == QuestionType.SHORT_ANSWER) {
                    continue;
                }
                String ans = graded.get(q.getId());
                if (gradingService.isObjectiveCorrect(q, ans)) {
                    continue;
                }
                out.add(new WrongQuestionItemDto(
                        q.getId(),
                        q.getTitle(),
                        q.getContent(),
                        q.getOptionsJson(),
                        exam.getId(),
                        exam.getTitle(),
                        q.getChapter(),
                        q.getKnowledgePoint(),
                        display.getOrDefault(q.getId(), ""),
                        q.getCorrectAnswerJson(),
                        q.getType().name(),
                        q.getAnswerAnalysis(),
                        att.getSubmittedAt()));
            }
        }
        out.sort(Comparator.comparing(WrongQuestionItemDto::submittedAt, Comparator.nullsLast(Comparator.reverseOrder())));
        return out;
    }

    public StudentOverviewDto overview(AuthenticatedUser user) {
        requireStudent(user);
        UserAccount me = userAccountRepository.findById(user.id()).orElseThrow();
        List<ExamAttempt> mine = examAttemptRepository.findByUserId(user.id()).stream()
                .filter(a -> a.getStatus() != AttemptStatus.IN_PROGRESS && a.getTotalScore() != null)
                .toList();
        BigDecimal avg = BigDecimal.ZERO;
        if (!mine.isEmpty()) {
            BigDecimal s = mine.stream().map(ExamAttempt::getTotalScore).reduce(BigDecimal.ZERO, BigDecimal::add);
            avg = s.divide(BigDecimal.valueOf(mine.size()), 2, RoundingMode.HALF_UP);
        }

        List<UserAccount> classPeers = new ArrayList<>();
        if (me.getClassName() != null && !me.getClassName().isBlank()) {
            classPeers = userAccountRepository.findByClassName(me.getClassName()).stream()
                    .filter(u -> u.getRole() == UserRole.STUDENT)
                    .toList();
        }
        List<UserAccount> schoolStudents = userAccountRepository.findByRole(UserRole.STUDENT);

        Integer classRank = rankAmong(avg, me.getId(), classPeers);
        Integer schoolRank = rankAmong(avg, me.getId(), schoolStudents);

        return new StudentOverviewDto(
                avg,
                mine.size(),
                classRank,
                schoolRank,
                classPeers.size(),
                schoolStudents.size());
    }

    public List<StudentAttemptHistoryItemDto> attemptHistory(AuthenticatedUser user, int limit) {
        requireStudent(user);
        int n = Math.min(Math.max(limit, 1), 50);
        List<ExamAttempt> attempts = examAttemptRepository.findByUserId(user.id()).stream()
                .filter(a -> a.getStatus() != AttemptStatus.IN_PROGRESS && a.getTotalScore() != null)
                .sorted(Comparator.comparing(ExamAttempt::getSubmittedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(n)
                .toList();
        List<StudentAttemptHistoryItemDto> out = new ArrayList<>();
        for (ExamAttempt att : attempts) {
            Exam exam = examRepository.findById(att.getExamId()).orElse(null);
            String title = exam != null ? exam.getTitle() : "(已删除考试)";
            out.add(new StudentAttemptHistoryItemDto(
                    att.getId(),
                    att.getExamId(),
                    title,
                    att.getTotalScore(),
                    att.getSubmittedAt()));
        }
        return out;
    }

    private Integer rankAmong(BigDecimal myAvg, Long myId, List<UserAccount> peers) {
        if (peers.isEmpty()) {
            return null;
        }
        List<ScoreRow> rows = new ArrayList<>();
        for (UserAccount u : peers) {
            List<ExamAttempt> a = examAttemptRepository.findByUserId(u.getId()).stream()
                    .filter(x -> x.getStatus() != AttemptStatus.IN_PROGRESS && x.getTotalScore() != null)
                    .toList();
            BigDecimal avg = BigDecimal.ZERO;
            if (!a.isEmpty()) {
                BigDecimal s = a.stream().map(ExamAttempt::getTotalScore).reduce(BigDecimal.ZERO, BigDecimal::add);
                avg = s.divide(BigDecimal.valueOf(a.size()), 4, RoundingMode.HALF_UP);
            }
            rows.add(new ScoreRow(u.getId(), avg));
        }
        rows.sort((a, b) -> b.avg.compareTo(a.avg));
        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i).userId.equals(myId)) {
                return i + 1;
            }
        }
        return null;
    }

    private record ScoreRow(Long userId, BigDecimal avg) {
    }

    private Map<Long, Question> loadQuestionMap(Long examId) {
        Map<Long, Question> map = new HashMap<>();
        for (ExamQuestion eq : examQuestionRepository.findByExamIdOrderByOrderIndexAsc(examId)) {
            questionRepository.findById(eq.getQuestionId()).ifPresent(q -> map.put(q.getId(), q));
        }
        return map;
    }

    private static void requireStudent(AuthenticatedUser user) {
        if (!UserRole.STUDENT.name().equals(user.role())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "仅学生可用");
        }
    }
}
