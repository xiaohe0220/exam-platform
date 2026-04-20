package com.campus.exam.web.dto;

public record UserProfileDto(
        Long id,
        String username,
        String displayName,
        String role,
        String className,
        String college,
        String personalNote,
        String settingsJson
) {
}
