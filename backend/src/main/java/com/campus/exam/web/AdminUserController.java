package com.campus.exam.web;

import com.campus.exam.security.AuthenticatedUser;
import com.campus.exam.service.AdminUserService;
import com.campus.exam.web.dto.AdminUserListItemDto;
import com.campus.exam.web.dto.UserAdminPatchRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAnyRole('ADMIN','COLLEGE_ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @PatchMapping("/users/{userId}")
    public AdminUserListItemDto patchUser(
            @AuthenticationPrincipal AuthenticatedUser admin,
            @PathVariable Long userId,
            @RequestBody UserAdminPatchRequest req) {
        return adminUserService.patchUser(admin, userId, req);
    }
}
