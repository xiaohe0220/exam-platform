package com.campus.exam.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Self-registration request. Username is the unique student id or employee id.
 */
public record RegisterRequest(
        @NotBlank
        @Size(min = 3, max = 64)
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "账号仅允许字母、数字、点、下划线与短横线")
        String username,
        @NotBlank
        @Size(min = 8, max = 100)
        String password,
        @NotBlank
        @Pattern(regexp = "STUDENT|TEACHER", message = "角色必须为学生或教师")
        String role,
        @NotBlank
        @Size(min = 2, max = 20)
        @Pattern(regexp = "^[\\u4e00-\\u9fa5·]+$", message = "姓名必须为汉字")
        String displayName,
        /** Optional student class name. */
        @Size(max = 100)
        String className,
        @Size(max = 100)
        String college,
        @Size(max = 64)
        String inviteCode
) {
}
