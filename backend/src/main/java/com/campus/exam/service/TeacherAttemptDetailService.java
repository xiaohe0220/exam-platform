package com.campus.exam.service;

import com.campus.exam.domain.*;
import com.campus.exam.repository.*;
import com.campus.exam.security.AuthenticatedUser;
import com.campus.exam.util.AnswerJsonUtil;
import com.campus.exam.web.dto.TeacherAttemptDetailDto;
import com.campus.exam.web.dto.TeacherAttemptDetailDto.AnswerLineDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class TeacherAttemptDetailService {

    private final ExamRepository examRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final QuestionRepository questionRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final UserAccountRepository userAccountRepository;
    private final ExamService examService;

    public TeacherAttemptDetailService(
            ExamRepository examRepository,
            ExamQuestionRepository examQuestionRepository,
            QuestionRepository questionRepository,
            ExamAttemptRepository examAttemptRepository,
            UserAccountRepository userAccountRepository,
            ExamService examService) {
        this.examRepository = examRepository;
        this.examQuestionRepository = examQuestionRepository;
        this.questionRepository = questionRepository;
        this.examAttemptRepository = examAttemptRepository;
        this.userAccountRepository = userAccountRepository;
        this.examService = examService;
    }

    public TeacherAttemptDetailDto detail(AuthenticatedUser user, Long examId, Long attemptId) {
        examService.requireOwnedExam(user, examId);
        ExamAttempt att = examAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!att.getExamId().equals(examId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        Exam exam = examRepository.findById(examId).orElseThrow();
        UserAccount stu = userAccountRepository.findById(att.getUserId()).orElseThrow();
        Map<Long, String> ans = AnswerJsonUtil.toMap(att.getAnswersJson());

        List<AnswerLineDto> lines = new ArrayList<>();
        for (ExamQuestion eq : examQuestionRepository.findByExamIdOrderByOrderIndexAsc(examId)) {
            Question q = questionRepository.findById(eq.getQuestionId()).orElseThrow();
            String raw = ans.getOrDefault(q.getId(), "");
            boolean subj = q.getType() == QuestionType.SHORT_ANSWER;
            lines.add(new AnswerLineDto(
                    q.getId(),
                    q.getTitle(),
                    q.getType().name(),
                    eq.getScore(),
                    raw,
                    q.getCorrectAnswerJson(),
                    subj));
        }

        return new TeacherAttemptDetailDto(
                att.getId(),
                exam.getId(),
                exam.getTitle(),
                stu.getId(),
                stu.getDisplayName(),
                stu.getClassName(),
                att.getStatus(),
                att.getObjectiveScore(),
                att.getSubjectiveScore(),
                att.getTotalScore(),
                lines);
    }
}
