package com.campus.exam.repository;

import com.campus.exam.domain.AcademicClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AcademicClassRepository extends JpaRepository<AcademicClass, Long> {

    Optional<AcademicClass> findByName(String name);
}
