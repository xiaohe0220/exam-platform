package com.campus.exam.web.dto;

public record ObjectiveReviewItemDto(
        Long questionId,
        String title,
        String type,
        String yourAnswer,
        String referenceAnswer,
        boolean correct,
        java.math.BigDecimal questionMaxScore,
        java.math.BigDecimal earnedScore
) {
}
