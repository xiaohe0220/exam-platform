package com.campus.exam.web.dto;

public record TeacherDashboardDto(
        long examTotal,
        long examPublished,
        long examDraft,
        long examClosed,
        long totalStudentAttempts,
        long questionCount
) {
}
