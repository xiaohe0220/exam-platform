package com.campus.exam.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CourseRequest(
        @Size(max = 80) String code,
        @NotBlank @Size(max = 160) String name,
        @Size(max = 120) String college,
        @Size(max = 120) String teacherName,
        Boolean enabled
) {
}
