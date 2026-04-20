package com.campus.exam.repository;

import com.campus.exam.domain.StudentMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentMessageRepository extends JpaRepository<StudentMessage, Long> {

    List<StudentMessage> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<StudentMessage> findAllByOrderByCreatedAtDesc();
}
