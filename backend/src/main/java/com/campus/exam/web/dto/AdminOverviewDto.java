package com.campus.exam.web.dto;

public record AdminOverviewDto(
        long userTotal,
        long studentCount,
        long teacherCount,
        long adminCount,
        long examCount,
        long attemptCount
) {
}
