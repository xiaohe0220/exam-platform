package com.campus.exam.service;

import com.campus.exam.domain.UserRole;
import com.campus.exam.repository.ExamAttemptRepository;
import com.campus.exam.repository.ExamRepository;
import com.campus.exam.repository.UserAccountRepository;
import com.campus.exam.web.dto.AdminOverviewDto;
import org.springframework.stereotype.Service;

@Service
public class AdminStatsService {

    private final UserAccountRepository userAccountRepository;
    private final ExamRepository examRepository;
    private final ExamAttemptRepository examAttemptRepository;

    public AdminStatsService(
            UserAccountRepository userAccountRepository,
            ExamRepository examRepository,
            ExamAttemptRepository examAttemptRepository) {
        this.userAccountRepository = userAccountRepository;
        this.examRepository = examRepository;
        this.examAttemptRepository = examAttemptRepository;
    }

    public AdminOverviewDto overview() {
        long students = userAccountRepository.countByRole(UserRole.STUDENT);
        long teachers = userAccountRepository.countByRole(UserRole.TEACHER);
        long admins = userAccountRepository.countByRole(UserRole.ADMIN)
                + userAccountRepository.countByRole(UserRole.COLLEGE_ADMIN);
        long total = userAccountRepository.count();
        long exams = examRepository.count();
        long attempts = examAttemptRepository.count();
        return new AdminOverviewDto(total, students, teachers, admins, exams, attempts);
    }
}
