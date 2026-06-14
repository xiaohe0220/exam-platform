package com.campus.exam.web.dto;

import com.campus.exam.domain.TeacherInviteCode;

import java.time.Instant;

public record TeacherInviteCodeDto(
        Long id,
        String code,
        Long teacherId,
        String teacherName,
        String college,
        Boolean enabled,
        Integer maxUses,
        Integer usedCount,
        Instant expiresAt,
        Instant createdAt
) {
    public static TeacherInviteCodeDto of(TeacherInviteCode invite) {
        return new TeacherInviteCodeDto(
                invite.getId(),
                invite.getCode(),
                invite.getTeacherId(),
                invite.getTeacherName(),
                invite.getCollege(),
                invite.getEnabled(),
                invite.getMaxUses(),
                invite.getUsedCount(),
                invite.getExpiresAt(),
                invite.getCreatedAt());
    }
}
