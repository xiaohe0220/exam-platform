package com.campus.exam.web.dto;

public record PlatformSettingsDto(
        int minQuestionDifficulty,
        int maxQuestionDifficulty,
        int defaultExamDurationMinutes,
        int defaultMaxRetakes,
        boolean notifyInAppEnabled,
        boolean notifyEmailEnabled,
        boolean notifySmsEnabled
) {
}
