package com.campus.exam.web.dto;

public record AuthResponse(
        String token,
        Long userId,
        String username,
        String displayName,
        String role,
        String className,
        String college,
        String personalNote,
        String settingsJson
) {
}
