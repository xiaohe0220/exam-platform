package com.campus.exam.web.dto;

public record AgentStatusDto(
        boolean enabled,
        boolean configured,
        String model
) {
}
