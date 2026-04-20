package com.campus.exam.web.dto;

import com.campus.exam.domain.AttemptStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record AttemptResultDto(
        Long attemptId,
        Long examId,
        AttemptStatus status,
        Instant submittedAt,
        BigDecimal objectiveScore,
        BigDecimal subjectiveScore,
        BigDecimal totalScore,
        String subjectiveDetailJson
) {
}
