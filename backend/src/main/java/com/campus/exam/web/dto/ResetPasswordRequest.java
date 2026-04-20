package com.campus.exam.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 忘记密码后自助重置（演示环境；生产应配合邮箱/短信验证码） */
public record ResetPasswordRequest(
        @NotBlank
        @Size(max = 64)
        String username,
        @NotBlank
        @Size(min = 6, max = 100)
        String newPassword
) {
}
