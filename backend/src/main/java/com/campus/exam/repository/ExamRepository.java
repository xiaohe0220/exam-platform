package com.campus.exam.repository;

import com.campus.exam.domain.Exam;
import com.campus.exam.domain.ExamStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ExamRepository extends JpaRepository<Exam, Long> {
    List<Exam> findByCreatorIdOrderByIdDesc(Long creatorId);

    Page<Exam> findByCreatorIdOrderByIdDesc(Long creatorId, Pageable pageable);

    List<Exam> findByStatus(ExamStatus status);

    /** 已发布且在时间窗口内（减少全表扫描后再过滤） */
    @Query("SELECT e FROM Exam e WHERE e.status = :st AND (e.startAt IS NULL OR e.startAt <= :now) AND (e.endAt IS NULL OR e.endAt >= :now)")
    List<Exam> findPublishedInWindow(@Param("st") ExamStatus st, @Param("now") Instant now);
}
