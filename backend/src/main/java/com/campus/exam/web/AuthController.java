package com.campus.exam.web;

import com.campus.exam.domain.UserAccount;
import com.campus.exam.domain.UserRole;
import com.campus.exam.repository.UserAccountRepository;
import com.campus.exam.security.AuthenticatedUser;
import com.campus.exam.security.JwtService;
import com.campus.exam.security.LoginProtectionService;
import com.campus.exam.web.dto.AuthResponse;
import com.campus.exam.web.dto.LoginRequest;
import com.campus.exam.web.dto.RegisterRequest;
import com.campus.exam.web.dto.ResetPasswordRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginProtectionService loginProtectionService;

    public AuthController(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            LoginProtectionService loginProtectionService) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.loginProtectionService = loginProtectionService;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        String uname = req.username().trim();
        if (userAccountRepository.findByUsername(uname).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "该学号/工号已被注册");
        }
        UserRole role;
        try {
            role = UserRole.valueOf(req.role());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "角色无效");
        }
        if (role != UserRole.STUDENT && role != UserRole.TEACHER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "仅支持注册学生或教师账号");
        }
        UserAccount u = new UserAccount();
        u.setUsername(uname);
        u.setPasswordHash(passwordEncoder.encode(req.password()));
        String dn = req.displayName();
        u.setDisplayName(dn != null && !dn.isBlank() ? dn.trim() : uname);
        u.setRole(role);
        if (role == UserRole.STUDENT) {
            u.setClassName(req.className() != null && !req.className().isBlank() ? req.className().trim() : null);
        } else {
            u.setClassName(null);
        }
        u.setCollege(req.college() != null && !req.college().isBlank() ? req.college().trim() : null);
        u.setEnabled(true);
        userAccountRepository.save(u);
        loginProtectionService.recordSuccess(uname);
        String token = jwtService.createToken(u.getUsername(), u.getId(), u.getRole().name());
        return new AuthResponse(
                token,
                u.getId(),
                u.getUsername(),
                u.getDisplayName(),
                u.getRole().name(),
                u.getClassName(),
                u.getCollege(),
                u.getPersonalNote(),
                u.getSettingsJson());
    }

    @PostMapping("/reset-password")
    public Map<String, String> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        String uname = req.username().trim();
        UserAccount u = userAccountRepository.findByUsername(uname)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "未找到该账号"));
        if (u.getRole() == UserRole.ADMIN || u.getRole() == UserRole.COLLEGE_ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "管理员账号请通过教务重置");
        }
        u.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userAccountRepository.save(u);
        loginProtectionService.recordSuccess(uname);
        return Map.of("message", "密码已重置，请使用新密码登录");
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        loginProtectionService.checkAllowed(req.username());
        UserAccount u = userAccountRepository.findByUsername(req.username()).orElse(null);
        if (u == null || !passwordEncoder.matches(req.password(), u.getPasswordHash())) {
            loginProtectionService.recordFailure(req.username());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "账号或密码错误");
        }
        if (Boolean.FALSE.equals(u.getEnabled())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "账号已被禁用，请联系管理员");
        }
        loginProtectionService.recordSuccess(req.username());
        String token = jwtService.createToken(u.getUsername(), u.getId(), u.getRole().name());
        return new AuthResponse(
                token,
                u.getId(),
                u.getUsername(),
                u.getDisplayName(),
                u.getRole().name(),
                u.getClassName(),
                u.getCollege(),
                u.getPersonalNote(),
                u.getSettingsJson());
    }

    @GetMapping("/me")
    public AuthResponse me(@AuthenticationPrincipal AuthenticatedUser user) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        UserAccount u = userAccountRepository.findById(user.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        return new AuthResponse(
                "",
                u.getId(),
                u.getUsername(),
                u.getDisplayName(),
                u.getRole().name(),
                u.getClassName(),
                u.getCollege(),
                u.getPersonalNote(),
                u.getSettingsJson());
    }
}
