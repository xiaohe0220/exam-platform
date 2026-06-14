package com.campus.exam.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Demo-only public password reset request. Production keeps this endpoint disabled. */
public record ResetPasswordRequest(
        @NotBlank
        @Size(max = 64)
        String username,
        @NotBlank
        @Size(min = 8, max = 100)
        String newPassword
) {
}
