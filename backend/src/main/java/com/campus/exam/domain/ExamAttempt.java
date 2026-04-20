package com.campus.exam.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "exam_attempts", indexes = {
        @Index(columnList = "examId"),
        @Index(columnList = "userId")
})
@Getter
@Setter
@NoArgsConstructor
public class ExamAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long examId;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private AttemptStatus status = AttemptStatus.IN_PROGRESS;

    private Instant startedAt;

    private Instant submittedAt;

    /** questionId -> 学生答案 JSON */
    @Column(columnDefinition = "LONGTEXT")
    private String answersJson;

    /** questionId -> 简答题教师评分 JSON */
    @Column(columnDefinition = "LONGTEXT")
    private String subjectiveScoresJson;

    /**
     * 题号导航「待检查」标记：JSON 数组，元素为题目的数字 ID 字符串。
     */
    @Column(columnDefinition = "TEXT")
    private String flagsJson;

    /**
     * 客观题选项乱序：questionId -> 排列 perm，perm[显示位下标]=原始选项下标。
     */
    @Column(columnDefinition = "LONGTEXT")
    private String shuffleJson;

    @Column(nullable = false)
    private Integer switchCount = 0;

    @Column(precision = 8, scale = 2)
    private BigDecimal totalScore;

    @Column(precision = 8, scale = 2)
    private BigDecimal objectiveScore;

    @Column(precision = 8, scale = 2)
    private BigDecimal subjectiveScore;
}
