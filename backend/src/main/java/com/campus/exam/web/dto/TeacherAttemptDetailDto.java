package com.campus.exam.web.dto;

import com.campus.exam.domain.AttemptStatus;

import java.math.BigDecimal;
import java.util.List;

public record TeacherAttemptDetailDto(
        Long attemptId,
        Long examId,
        String examTitle,
        Long userId,
        String studentName,
        String className,
        AttemptStatus status,
        BigDecimal objectiveScore,
        BigDecimal subjectiveScore,
        BigDecimal totalScore,
        List<AnswerLineDto> lines
) {

    public record AnswerLineDto(
            Long questionId,
            String title,
            String type,
            BigDecimal maxScore,
            String studentAnswer,
            String referenceAnswer,
            boolean subjective
    ) {
    }
}
