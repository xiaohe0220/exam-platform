package com.campus.exam.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 自助注册：学生填学号、教师填工号，角色由前端选择。
 */
public record RegisterRequest(
        @NotBlank
        @Size(min = 3, max = 64)
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "账号仅允许字母、数字、点、下划线与短横线")
        String username,
        @NotBlank
        @Size(min = 6, max = 100)
        String password,
        @NotBlank
        @Pattern(regexp = "STUDENT|TEACHER", message = "角色须为学生或教师")
        String role,
        @Size(max = 100)
        String displayName,
        /** 学生可选填班级 */
        @Size(max = 100)
        String className,
        @Size(max = 100)
        String college
) {
}
