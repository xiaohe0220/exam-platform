package com.campus.exam.repository;

import com.campus.exam.domain.AttemptStatus;
import com.campus.exam.domain.ExamAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExamAttemptRepository extends JpaRepository<ExamAttempt, Long> {

    List<ExamAttempt> findByUserId(Long userId);

    List<ExamAttempt> findByExamId(Long examId);

    List<ExamAttempt> findByExamIdAndUserId(Long examId, Long userId);

    Optional<ExamAttempt> findByExamIdAndUserIdAndStatus(Long examId, Long userId, AttemptStatus status);

    long countByExamIdAndUserId(Long examId, Long userId);

    long countByExamId(Long examId);
}
