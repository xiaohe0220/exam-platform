package com.campus.exam.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StudentMessageReplyRequest(
        @NotBlank @Size(max = 2000) String reply
) {
}
