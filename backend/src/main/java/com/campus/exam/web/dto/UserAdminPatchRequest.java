package com.campus.exam.web.dto;

import com.campus.exam.domain.UserRole;

public record UserAdminPatchRequest(
        Boolean enabled,
        UserRole role,
        String email
) {
}
