package com.campus.exam.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SecurityEventRequest(
        @NotBlank @Size(max = 80) String eventType,
        @Size(max = 500) String detail
) {
}
