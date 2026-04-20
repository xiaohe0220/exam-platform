package com.campus.exam.service;

import com.campus.exam.domain.Exam;
import com.campus.exam.domain.ExamStatus;
import com.campus.exam.repository.ExamAttemptRepository;
import com.campus.exam.repository.ExamRepository;
import com.campus.exam.repository.QuestionRepository;
import com.campus.exam.security.AuthenticatedUser;
import com.campus.exam.web.dto.TeacherDashboardDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TeacherDashboardService {

    private final ExamRepository examRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final QuestionRepository questionRepository;

    public TeacherDashboardService(
            ExamRepository examRepository,
            ExamAttemptRepository examAttemptRepository,
            QuestionRepository questionRepository) {
        this.examRepository = examRepository;
        this.examAttemptRepository = examAttemptRepository;
        this.questionRepository = questionRepository;
    }

    public TeacherDashboardDto summary(AuthenticatedUser user) {
        requireTeacherSide(user);
        List<Exam> exams = examRepository.findByCreatorIdOrderByIdDesc(user.id());
        long pub = exams.stream().filter(e -> e.getStatus() == ExamStatus.PUBLISHED).count();
        long draft = exams.stream().filter(e -> e.getStatus() == ExamStatus.DRAFT).count();
        long closed = exams.stream().filter(e -> e.getStatus() == ExamStatus.CLOSED).count();
        long attempts = 0;
        for (Exam e : exams) {
            attempts += examAttemptRepository.countByExamId(e.getId());
        }
        long qn = questionRepository.countByCreatorId(user.id());
        return new TeacherDashboardDto(exams.size(), pub, draft, closed, attempts, qn);
    }

    private static void requireTeacherSide(AuthenticatedUser user) {
        String r = user.role();
        if (!"TEACHER".equals(r) && !"ADMIN".equals(r) && !"COLLEGE_ADMIN".equals(r)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "仅教师或教务可用");
        }
    }
}
