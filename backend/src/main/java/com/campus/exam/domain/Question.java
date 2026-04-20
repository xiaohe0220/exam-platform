package com.campus.exam.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long creatorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private QuestionType type;

    /** 题目标题（列表展示） */
    @Column(nullable = false, length = 500)
    private String title;

    /** 题干富文本/HTML */
    @Column(columnDefinition = "TEXT")
    private String content;

    /** JSON 数组：选项文案 */
    @Column(columnDefinition = "TEXT")
    private String optionsJson;

    /** JSON：标准答案。单选 "A"；多选 ["A","C"]；判断 "true"；填空 "答案" 或 ["空1","空2"] */
    @Column(columnDefinition = "TEXT")
    private String correctAnswerJson;

    @Column(nullable = false)
    private Integer difficulty = 3;

    @Column(length = 200)
    private String chapter;

    @Column(length = 200)
    private String knowledgePoint;

    /** 答案解析（富文本/HTML，学生端仅在错题本等场景展示） */
    @Column(columnDefinition = "TEXT")
    private String answerAnalysis;

    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
