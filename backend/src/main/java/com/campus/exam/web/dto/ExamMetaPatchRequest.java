package com.campus.exam.web.dto;

public record ExamMetaPatchRequest(
        Boolean rankingVisible,
        Integer maxRetakes,
        Integer durationMinutes
) {
}
