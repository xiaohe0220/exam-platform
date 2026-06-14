package com.campus.exam.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record TeacherInviteCreateRequest(
        @NotNull
        Long teacherId,
        @Min(1)
        @Max(10000)
        Integer maxUses,
        Instant expiresAt
) {
}
