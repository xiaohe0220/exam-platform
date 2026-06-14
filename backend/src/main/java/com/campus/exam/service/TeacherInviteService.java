package com.campus.exam.service;

import com.campus.exam.domain.TeacherInviteCode;
import com.campus.exam.domain.UserAccount;
import com.campus.exam.domain.UserRole;
import com.campus.exam.repository.TeacherInviteCodeRepository;
import com.campus.exam.repository.UserAccountRepository;
import com.campus.exam.security.AuthenticatedUser;
import com.campus.exam.web.dto.TeacherInviteCodeDto;
import com.campus.exam.web.dto.TeacherInviteCreateRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;

@Service
public class TeacherInviteService {

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final TeacherInviteCodeRepository inviteRepository;
    private final UserAccountRepository userAccountRepository;
    private final AuditService auditService;

    public TeacherInviteService(
            TeacherInviteCodeRepository inviteRepository,
            UserAccountRepository userAccountRepository,
            AuditService auditService) {
        this.inviteRepository = inviteRepository;
        this.userAccountRepository = userAccountRepository;
        this.auditService = auditService;
    }

    public List<TeacherInviteCodeDto> list() {
        return inviteRepository.findAllByOrderByIdDesc().stream()
                .map(TeacherInviteCodeDto::of)
                .toList();
    }

    @Transactional
    public TeacherInviteCodeDto create(AuthenticatedUser admin, TeacherInviteCreateRequest req) {
        UserAccount teacher = userAccountRepository.findByIdAndRole(req.teacherId(), UserRole.TEACHER)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "请选择教师账号生成邀请码"));
        TeacherInviteCode invite = new TeacherInviteCode();
        invite.setCode(generateUniqueCode());
        invite.setTeacherId(teacher.getId());
        invite.setTeacherName(teacher.getDisplayName());
        invite.setCollege(teacher.getCollege());
        invite.setMaxUses(req.maxUses() != null ? req.maxUses() : 50);
        invite.setExpiresAt(req.expiresAt());
        invite.setEnabled(true);
        invite.setUsedCount(0);
        invite.setCreatedById(admin.id());
        inviteRepository.save(invite);
        auditService.log(admin.id(), "TEACHER_INVITE_CREATE", "teacherId=" + teacher.getId());
        return TeacherInviteCodeDto.of(invite);
    }

    @Transactional
    public void delete(AuthenticatedUser admin, Long id) {
        TeacherInviteCode invite = inviteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        inviteRepository.delete(invite);
        auditService.log(admin.id(), "TEACHER_INVITE_DELETE", "id=" + id);
    }

    public TeacherInviteCode requireUsable(String rawCode) {
        if (!StringUtils.hasText(rawCode)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "学生注册必须填写教师邀请码");
        }
        String code = rawCode.trim();
        TeacherInviteCode invite = inviteRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "教师邀请码无效"));
        if (Boolean.FALSE.equals(invite.getEnabled())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "教师邀请码已停用");
        }
        if (invite.getExpiresAt() != null && Instant.now().isAfter(invite.getExpiresAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "教师邀请码已过期");
        }
        if (invite.getMaxUses() != null
                && invite.getUsedCount() != null
                && invite.getUsedCount() >= invite.getMaxUses()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "教师邀请码使用次数已满");
        }
        return invite;
    }

    @Transactional
    public void markUsed(Long id) {
        TeacherInviteCode invite = inviteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "教师邀请码无效"));
        invite.setUsedCount((invite.getUsedCount() == null ? 0 : invite.getUsedCount()) + 1);
        inviteRepository.save(invite);
    }

    private String generateUniqueCode() {
        for (int attempt = 0; attempt < 20; attempt++) {
            String code = randomCode();
            if (!inviteRepository.existsByCodeIgnoreCase(code)) {
                return code;
            }
        }
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "邀请码生成失败，请稍后重试");
    }

    private static String randomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
