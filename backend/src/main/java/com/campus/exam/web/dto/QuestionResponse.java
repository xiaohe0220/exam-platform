package com.campus.exam.web.dto;

import com.campus.exam.domain.QuestionType;

import java.time.Instant;

public record QuestionResponse(
        Long id,
        QuestionType type,
        String title,
        String content,
        String optionsJson,
        String correctAnswerJson,
        boolean revealAnswer,
        Integer difficulty,
        String chapter,
        String knowledgePoint,
        String answerAnalysis,
        Instant createdAt
) {
}
