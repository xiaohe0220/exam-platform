package com.campus.exam.web.dto;

import java.math.BigDecimal;

public record ExamStatsDto(
        long totalAttempts,
        long submittedCount,
        BigDecimal paperFullScore,
        BigDecimal avgScore,
        BigDecimal maxScore,
        BigDecimal minScore,
        long passCount,
        int passLine
) {
}
