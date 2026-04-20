package com.campus.exam.web.dto;

import java.time.Instant;

public record WrongQuestionItemDto(
        Long questionId,
        String questionTitle,
        String content,
        String optionsJson,
        Long examId,
        String examTitle,
        String chapter,
        String knowledgePoint,
        String yourAnswer,
        String correctAnswer,
        String questionType,
        String answerAnalysis,
        Instant submittedAt
) {
}
