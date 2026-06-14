package com.campus.exam.web;

import com.campus.exam.security.AuthPrincipalUtil;
import com.campus.exam.security.AuthenticatedUser;
import com.campus.exam.service.UserProfileService;
import com.campus.exam.web.dto.ChangePasswordRequest;
import com.campus.exam.web.dto.UserProfileDto;
import com.campus.exam.web.dto.UserProfileUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@PreAuthorize("isAuthenticated()")
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/profile")
    public UserProfileDto profile(Authentication authentication) {
        AuthenticatedUser user = AuthPrincipalUtil.requireUser(authentication);
        return userProfileService.get(user);
    }

    @PutMapping("/profile")
    public UserProfileDto update(Authentication authentication, @RequestBody UserProfileUpdateRequest req) {
        AuthenticatedUser user = AuthPrincipalUtil.requireUser(authentication);
        return userProfileService.update(user, req);
    }

    @PostMapping("/password")
    public Map<String, String> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest req) {
        AuthenticatedUser user = AuthPrincipalUtil.requireUser(authentication);
        userProfileService.changePassword(user, req);
        return Map.of("message", "密码已更新");
    }
}
