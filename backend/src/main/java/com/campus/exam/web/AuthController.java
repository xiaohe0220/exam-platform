package com.campus.exam.web;

import com.campus.exam.config.AuthProperties;
import com.campus.exam.domain.AcademicClass;
import com.campus.exam.domain.TeacherInviteCode;
import com.campus.exam.domain.UserAccount;
import com.campus.exam.domain.UserRole;
import com.campus.exam.repository.AcademicClassRepository;
import com.campus.exam.repository.UserAccountRepository;
import com.campus.exam.security.AuthenticatedUser;
import com.campus.exam.security.JwtService;
import com.campus.exam.security.LoginProtectionService;
import com.campus.exam.service.TeacherInviteService;
import com.campus.exam.web.dto.AcademicClassDto;
import com.campus.exam.web.dto.AuthResponse;
import com.campus.exam.web.dto.LoginRequest;
import com.campus.exam.web.dto.RegisterRequest;
import com.campus.exam.web.dto.ResetPasswordRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserAccountRepository userAccountRepository;
    private final AcademicClassRepository academicClassRepository;
    private final TeacherInviteService teacherInviteService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginProtectionService loginProtectionService;
    private final AuthProperties authProperties;
    private final boolean demoDataEnabled;

    public AuthController(
            UserAccountRepository userAccountRepository,
            AcademicClassRepository academicClassRepository,
            TeacherInviteService teacherInviteService,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            LoginProtectionService loginProtectionService,
            AuthProperties authProperties,
            @Value("${app.seed.demo-data-enabled:false}") boolean demoDataEnabled) {
        this.userAccountRepository = userAccountRepository;
        this.academicClassRepository = academicClassRepository;
        this.teacherInviteService = teacherInviteService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.loginProtectionService = loginProtectionService;
        this.authProperties = authProperties;
        this.demoDataEnabled = demoDataEnabled;
    }

    @GetMapping("/capabilities")
    public Map<String, Boolean> capabilities() {
        return Map.of(
                "publicRegistrationEnabled", authProperties.isPublicRegistrationEnabled(),
                "registrationInviteRequired", true,
                "demoPasswordResetEnabled", authProperties.isDemoPasswordResetEnabled(),
                "demoDataEnabled", demoDataEnabled);
    }

    @GetMapping("/registration-options")
    public Map<String, Object> registrationOptions() {
        List<AcademicClassDto> classes = academicClassRepository.findByEnabledTrueOrderByNameAsc().stream()
                .map(c -> AcademicClassDto.of(c, 0))
                .toList();
        List<String> colleges = classes.stream()
                .map(AcademicClassDto::college)
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .distinct()
                .toList();
        return Map.of("classes", classes, "colleges", colleges);
    }

    @Transactional
    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        if (!authProperties.isPublicRegistrationEnabled()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "当前未开放自主注册，请联系管理员创建账号");
        }

        String uname = normalizeUsername(req.username());
        if (userAccountRepository.findByUsernameIgnoreCase(uname).isPresent()) {
            throw duplicateAccount();
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

        String college = requirePublishedCollege(req.college());
        AcademicClass selectedClass = null;
        TeacherInviteCode invite = null;
        if (role == UserRole.STUDENT) {
            selectedClass = requirePublishedClass(req.className(), college);
            invite = teacherInviteService.requireUsable(req.inviteCode());
        }

        UserAccount u = new UserAccount();
        u.setUsername(uname);
        u.setPasswordHash(passwordEncoder.encode(req.password()));
        u.setDisplayName(req.displayName().trim());
        u.setRole(role);
        if (role == UserRole.STUDENT) {
            u.setClassName(selectedClass.getName());
            u.setCollege(selectedClass.getCollege());
        } else {
            u.setClassName(null);
            u.setCollege(college);
        }
        u.setEnabled(true);

        try {
            userAccountRepository.save(u);
        } catch (DataIntegrityViolationException e) {
            throw duplicateAccount();
        }
        if (invite != null) {
            teacherInviteService.markUsed(invite.getId());
        }

        loginProtectionService.recordSuccess(uname);
        String token = jwtService.createToken(u.getUsername(), u.getId(), u.getRole().name());
        return toAuthResponse(u, token);
    }

    @PostMapping("/reset-password")
    public Map<String, String> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        if (!authProperties.isDemoPasswordResetEnabled()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "公开重置密码未开启，请登录后修改或联系管理员");
        }
        String uname = normalizeUsername(req.username());
        UserAccount u = userAccountRepository.findByUsernameIgnoreCase(uname)
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
        String uname = normalizeUsername(req.username());
        loginProtectionService.checkAllowed(uname);
        UserAccount u = userAccountRepository.findByUsernameIgnoreCase(uname).orElse(null);
        if (u == null || !passwordEncoder.matches(req.password(), u.getPasswordHash())) {
            loginProtectionService.recordFailure(uname);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "账号或密码错误");
        }
        if (Boolean.FALSE.equals(u.getEnabled())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "账号已被禁用，请联系管理员");
        }
        loginProtectionService.recordSuccess(uname);
        String token = jwtService.createToken(u.getUsername(), u.getId(), u.getRole().name());
        return toAuthResponse(u, token);
    }

    @GetMapping("/me")
    public AuthResponse me(@AuthenticationPrincipal AuthenticatedUser user) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        UserAccount u = userAccountRepository.findById(user.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        return toAuthResponse(u, "");
    }

    private static String normalizeUsername(String username) {
        return username.trim().toLowerCase(Locale.ROOT);
    }

    private String requirePublishedCollege(String college) {
        if (!StringUtils.hasText(college)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请选择教务发布的学院");
        }
        String cleaned = college.trim();
        if (!academicClassRepository.existsByCollegeAndEnabledTrue(cleaned)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "学院不在教务发布范围内");
        }
        return cleaned;
    }

    private AcademicClass requirePublishedClass(String className, String college) {
        if (!StringUtils.hasText(className)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请选择教务发布的班级");
        }
        return academicClassRepository.findFirstByNameAndCollegeAndEnabledTrue(className.trim(), college)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "班级与学院不匹配或未启用"));
    }

    private static ResponseStatusException duplicateAccount() {
        return new ResponseStatusException(HttpStatus.CONFLICT, "该学号/工号已注册，一个人只能注册一个账号");
    }

    private AuthResponse toAuthResponse(UserAccount u, String token) {
        return new AuthResponse(
                token,
                u.getId(),
                u.getUsername(),
                u.getDisplayName(),
                u.getRole().name(),
                u.getClassName(),
                u.getCollege(),
                u.getEmail(),
                u.getPhone(),
                u.getPersonalNote(),
                u.getSettingsJson());
    }
}
