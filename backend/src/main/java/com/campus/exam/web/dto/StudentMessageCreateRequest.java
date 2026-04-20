package com.campus.exam.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StudentMessageCreateRequest(
        @NotBlank @Size(max = 40) String subject,
        @NotBlank @Size(max = 100) String course,
        @NotBlank @Size(max = 100) String className,
        @NotBlank @Size(max = 300) String content
) {
}
