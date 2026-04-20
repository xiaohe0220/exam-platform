package com.campus.exam.web.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record StudentAttemptHistoryItemDto(
        Long attemptId,
        Long examId,
        String examTitle,
        BigDecimal totalScore,
        Instant submittedAt
) {
}
