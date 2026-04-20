package com.campus.exam.web.dto;

import com.campus.exam.domain.ExamStatus;

import java.time.Instant;

public record ExamSummaryDto(
        Long id,
        String title,
        String description,
        ExamStatus status,
        Instant startAt,
        Instant endAt,
        Integer durationMinutes,
        Integer maxRetakes,
        Integer switchLimit,
        Boolean fullscreenRequired,
        String targetClasses,
        Boolean rankingVisible
) {
}
