package com.campus.exam.repository;

import com.campus.exam.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByUsername(String username);

    List<UserAccount> findByClassName(String className);

    List<UserAccount> findByRole(com.campus.exam.domain.UserRole role);

    long countByRole(com.campus.exam.domain.UserRole role);
}
