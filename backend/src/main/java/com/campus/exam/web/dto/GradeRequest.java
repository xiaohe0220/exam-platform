package com.campus.exam.web.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record GradeRequest(
        @NotNull Long questionId,
        @NotNull BigDecimal score,
        String comment
) {
}
