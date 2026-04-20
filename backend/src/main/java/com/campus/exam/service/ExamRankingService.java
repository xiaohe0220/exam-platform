package com.campus.exam.service;

import com.campus.exam.domain.*;
import com.campus.exam.repository.ExamAttemptRepository;
import com.campus.exam.repository.ExamRepository;
import com.campus.exam.repository.UserAccountRepository;
import com.campus.exam.security.AuthenticatedUser;
import com.campus.exam.web.dto.ExamRankingRowDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ExamRankingService {

    private final ExamRepository examRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final UserAccountRepository userAccountRepository;
    private final ExamService examService;

    public ExamRankingService(
            ExamRepository examRepository,
            ExamAttemptRepository examAttemptRepository,
            UserAccountRepository userAccountRepository,
            ExamService examService) {
        this.examRepository = examRepository;
        this.examAttemptRepository = examAttemptRepository;
        this.userAccountRepository = userAccountRepository;
        this.examService = examService;
    }

    public List<ExamRankingRowDto> rankingForTeacher(AuthenticatedUser user, Long examId) {
        examService.requireOwnedExam(user, examId);
        return buildRanking(examId);
    }

    public List<ExamRankingRowDto> rankingForStudent(AuthenticatedUser user, Long examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (Boolean.FALSE.equals(exam.getRankingVisible())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "本场考试未公开排名");
        }
        boolean participated = examAttemptRepository.findByExamIdAndUserId(examId, user.id()).stream()
                .anyMatch(a -> a.getStatus() != AttemptStatus.IN_PROGRESS);
        if (!participated) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "仅参加本场考试的考生可查看排名");
        }
        return buildRanking(examId);
    }

    private List<ExamRankingRowDto> buildRanking(Long examId) {
        List<ExamAttempt> attempts = examAttemptRepository.findByExamId(examId).stream()
                .filter(a -> a.getStatus() != AttemptStatus.IN_PROGRESS && a.getTotalScore() != null)
                .sorted(Comparator
                        .comparing(ExamAttempt::getTotalScore, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(ExamAttempt::getSubmittedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        List<ExamRankingRowDto> out = new ArrayList<>();
        int rank = 1;
        BigDecimal prev = null;
        for (int i = 0; i < attempts.size(); i++) {
            ExamAttempt a = attempts.get(i);
            if (prev != null && a.getTotalScore().compareTo(prev) != 0) {
                rank = i + 1;
            }
            prev = a.getTotalScore();
            UserAccount u = userAccountRepository.findById(a.getUserId()).orElse(null);
            out.add(new ExamRankingRowDto(
                    rank,
                    a.getUserId(),
                    u != null ? u.getDisplayName() : "—",
                    u != null ? u.getClassName() : null,
                    a.getTotalScore(),
                    a.getSubmittedAt()));
        }
        return out;
    }
}
