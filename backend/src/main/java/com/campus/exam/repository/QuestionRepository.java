package com.campus.exam.repository;

import com.campus.exam.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByCreatorIdOrderByIdDesc(Long creatorId);

    long countByCreatorId(Long creatorId);
}
