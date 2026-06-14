package com.campus.exam.web.dto;

import java.math.BigDecimal;

public record ExamQuestionStatsDto(
        Long questionId,
        String title,
        String type,
        String chapter,
        String knowledgePoint,
        BigDecimal maxScore,
        long answeredCount,
        long correctCount,
        long wrongCount,
        double correctRate,
        BigDecimal avgEarnedScore
) {
}
