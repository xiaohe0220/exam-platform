package com.campus.exam.service;

import com.campus.exam.domain.Exam;
import com.campus.exam.web.dto.ExamSummaryDto;

public final class TeachersExamMapper {

    private TeachersExamMapper() {
    }

    public static ExamSummaryDto toSummary(Exam e) {
        return new ExamSummaryDto(
                e.getId(),
                e.getTitle(),
                e.getDescription(),
                e.getStatus(),
                e.getStartAt(),
                e.getEndAt(),
                e.getDurationMinutes(),
                e.getMaxRetakes(),
                e.getSwitchLimit(),
                e.getFullscreenRequired(),
                e.getTargetClasses(),
                e.getRankingVisible());
    }
}
