package com.campus.exam.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PlatformSettingsUpdateRequest(
        @NotNull @Min(1) @Max(5) Integer minQuestionDifficulty,
        @NotNull @Min(1) @Max(5) Integer maxQuestionDifficulty,
        @NotNull @Min(5) @Max(600) Integer defaultExamDurationMinutes,
        @NotNull @Min(1) @Max(20) Integer defaultMaxRetakes,
        @NotNull Boolean notifyInAppEnabled,
        @NotNull Boolean notifyEmailEnabled,
        @NotNull Boolean notifySmsEnabled
) {
}
