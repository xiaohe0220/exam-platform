package com.campus.exam.web.dto;

public record UserProfileUpdateRequest(
        String displayName,
        String email,
        String phone,
        String personalNote,
        String settingsJson
) {
}
