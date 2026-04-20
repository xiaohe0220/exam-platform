package com.campus.exam.web.dto;

import java.time.Instant;

public record StudentMessageDto(
        Long id,
        Long userId,
        String studentName,
        String studentUsername,
        String subject,
        String course,
        String className,
        String content,
        String status,
        String replyContent,
        Long repliedByUserId,
        String repliedByName,
        Instant repliedAt,
        Instant createdAt
) {
}
