package com.campus.exam.web.dto;

import com.campus.exam.domain.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record QuestionRequest(
        @NotNull QuestionType type,
        @NotBlank String title,
        String content,
        String optionsJson,
        String correctAnswerJson,
        Integer difficulty,
        String chapter,
        String knowledgePoint,
        String answerAnalysis
) {
}
