package com.campus.exam.web.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ExamRankingRowDto(
        int rank,
        Long userId,
        String displayName,
        String className,
        BigDecimal totalScore,
        Instant submittedAt
) {
}
