package com.campus.exam.web.dto;

import java.util.List;

public record AdminAnalyticsDto(
        long studentCount,
        long teacherCount,
        long publishedExamCount,
        long submittedAttemptCount,
        double participationRate,
        Double avgScoreAllAttempts,
        double avgQuestionDifficulty,
        List<ExamEffectRow> examEffects
) {
    public record ExamEffectRow(
            Long examId,
            String title,
            long attemptCount,
            Double avgScore
    ) {
    }
}
