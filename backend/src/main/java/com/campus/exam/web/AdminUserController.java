package com.campus.exam.web;

import com.campus.exam.security.AuthenticatedUser;
import com.campus.exam.service.AdminUserService;
import com.campus.exam.service.TeacherInviteService;
import com.campus.exam.web.dto.AdminUserListItemDto;
import com.campus.exam.web.dto.TeacherInviteCodeDto;
import com.campus.exam.web.dto.TeacherInviteCreateRequest;
import com.campus.exam.web.dto.UserAdminPatchRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAnyRole('ADMIN','COLLEGE_ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final TeacherInviteService teacherInviteService;

    public AdminUserController(
            AdminUserService adminUserService,
            TeacherInviteService teacherInviteService) {
        this.adminUserService = adminUserService;
        this.teacherInviteService = teacherInviteService;
    }

    @PatchMapping("/users/{userId}")
    public AdminUserListItemDto patchUser(
            @AuthenticationPrincipal AuthenticatedUser admin,
            @PathVariable Long userId,
            @RequestBody UserAdminPatchRequest req) {
        return adminUserService.patchUser(admin, userId, req);
    }

    @DeleteMapping("/users/{userId}")
    public void deleteUser(
            @AuthenticationPrincipal AuthenticatedUser admin,
            @PathVariable Long userId) {
        adminUserService.deleteUser(admin, userId);
    }

    @GetMapping("/teacher-invites")
    public List<TeacherInviteCodeDto> teacherInvites() {
        return teacherInviteService.list();
    }

    @PostMapping("/teacher-invites")
    public TeacherInviteCodeDto createTeacherInvite(
            @AuthenticationPrincipal AuthenticatedUser admin,
            @Valid @RequestBody TeacherInviteCreateRequest req) {
        return teacherInviteService.create(admin, req);
    }

    @DeleteMapping("/teacher-invites/{inviteId}")
    public void deleteTeacherInvite(
            @AuthenticationPrincipal AuthenticatedUser admin,
            @PathVariable Long inviteId) {
        teacherInviteService.delete(admin, inviteId);
    }
}
