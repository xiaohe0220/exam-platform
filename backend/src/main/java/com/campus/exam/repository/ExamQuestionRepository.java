package com.campus.exam.repository;

import com.campus.exam.domain.ExamQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamQuestionRepository extends JpaRepository<ExamQuestion, Long> {
    List<ExamQuestion> findByExamIdOrderByOrderIndexAsc(Long examId);

    void deleteByExamId(Long examId);
}
