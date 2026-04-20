package com.campus.exam.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

public record ExamCreateRequest(
        @NotBlank String title,
        String description,
        @NotNull Instant startAt,
        @NotNull Instant endAt,
        @NotNull Integer durationMinutes,
        Integer maxRetakes,
        Integer switchLimit,
        Boolean fullscreenRequired,
        String targetClasses,
        Boolean rankingVisible,
        List<ExamQuestionItem> questions
) {

    public record ExamQuestionItem(Long questionId, Integer orderIndex, java.math.BigDecimal score) {
    }
}
