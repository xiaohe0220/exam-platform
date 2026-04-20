package com.campus.exam.web.dto;

import com.campus.exam.domain.QuestionType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public record RandomComposeRequest(
        @NotNull String title,
        String description,
        @NotNull Instant startAt,
        @NotNull Instant endAt,
        @NotNull Integer durationMinutes,
        Integer maxRetakes,
        Integer switchLimit,
        Boolean fullscreenRequired,
        String targetClasses,
        Boolean rankingVisible,
        String chapterKeyword,
        Integer difficulty,
        @NotNull Map<QuestionType, Integer> countByType,
        @NotNull BigDecimal scorePerQuestion
) {
}
