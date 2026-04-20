package com.campus.exam.web.dto;

public record UserProfileUpdateRequest(
        String displayName,
        String personalNote,
        String settingsJson
) {
}
