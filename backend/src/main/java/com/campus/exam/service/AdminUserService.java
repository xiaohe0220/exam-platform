package com.campus.exam.service;

import com.campus.exam.domain.UserAccount;
import com.campus.exam.repository.UserAccountRepository;
import com.campus.exam.security.AuthenticatedUser;
import com.campus.exam.web.dto.AdminUserListItemDto;
import com.campus.exam.web.dto.UserAdminPatchRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminUserService {

    private final UserAccountRepository userAccountRepository;

    public AdminUserService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
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
        return AdminUserListItemDto.from(userAccountRepository.save(u));
    }
}
