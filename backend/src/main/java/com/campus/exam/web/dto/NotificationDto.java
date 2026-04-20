package com.campus.exam.web.dto;

import java.time.Instant;

public record NotificationDto(
        Long id,
        String type,
        String title,
        String body,
        Long examId,
        boolean read,
        Instant createdAt
) {
}
