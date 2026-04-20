package com.campus.exam.web.dto;

import java.time.Instant;
import java.util.List;

public record StudentPaperDto(
        ExamSummaryDto exam,
        Long attemptId,
        Instant startedAt,
        int switchCount,
        List<Long> markedQuestionIds,
        List<QuestionResponse> questions
) {
}
