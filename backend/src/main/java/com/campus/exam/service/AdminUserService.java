package com.campus.exam.service;

import com.campus.exam.domain.UserAccount;
import com.campus.exam.domain.UserRole;
import com.campus.exam.repository.UserAccountRepository;
import com.campus.exam.security.AuthenticatedUser;
import com.campus.exam.web.dto.AdminUserListItemDto;
import com.campus.exam.web.dto.UserAdminPatchRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminUserService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AdminUserListItemDto patchUser(AuthenticatedUser admin, Long userId, UserAdminPatchRequest req) {
        UserAccount u = userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (req.enabled() != null) {
            if (Boolean.FALSE.equals(req.enabled()) && userId.equals(admin.id())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "不能禁用当前登录账号");
            }
            u.setEnabled(req.enabled());
        }
        if (req.role() != null) {
            if (userId.equals(admin.id())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "不能修改自己的角色");
            }
            u.setRole(req.role());
        }
        if (req.email() != null) {
            String em = req.email().trim();
            u.setEmail(em.isEmpty() ? null : em);
        }
        if (req.phone() != null) {
            String phone = req.phone().trim();
            u.setPhone(phone.isEmpty() ? null : phone);
        }
        if (req.newPassword() != null) {
            String p = req.newPassword();
            if (p.length() < 8 || p.length() > 100) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "新密码长度需在 8–100 位之间");
            }
            u.setPasswordHash(passwordEncoder.encode(p));
        }
        return AdminUserListItemDto.from(userAccountRepository.save(u));
    }

    @Transactional
    public void deleteUser(AuthenticatedUser admin, Long userId) {
        UserAccount u = userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (userId.equals(admin.id())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "不能删除当前登录账号");
        }
        if (isAdminRole(u.getRole()) && adminAccountCount() <= 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "至少保留一个教务管理员账号");
        }

        try {
            userAccountRepository.delete(u);
            userAccountRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "该账号已有业务记录，建议先禁用账号保留历史数据");
        }
    }

    private long adminAccountCount() {
        return userAccountRepository.countByRole(UserRole.ADMIN)
                + userAccountRepository.countByRole(UserRole.COLLEGE_ADMIN);
    }

    private static boolean isAdminRole(UserRole role) {
        return role == UserRole.ADMIN || role == UserRole.COLLEGE_ADMIN;
    }
}
