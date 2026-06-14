package com.campus.exam.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AcademicClassRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 120) String college,
        @Size(max = 120) String major,
        @Size(max = 40) String grade,
        Boolean enabled
) {
}
