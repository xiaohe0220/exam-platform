package com.campus.exam.repository;

import com.campus.exam.domain.TeacherInviteCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeacherInviteCodeRepository extends JpaRepository<TeacherInviteCode, Long> {

    Optional<TeacherInviteCode> findByCodeIgnoreCase(String code);

    List<TeacherInviteCode> findAllByOrderByIdDesc();

    boolean existsByCodeIgnoreCase(String code);
}
