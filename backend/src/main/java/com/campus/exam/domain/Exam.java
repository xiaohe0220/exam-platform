package com.campus.exam.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "exams")
@Getter
@Setter
@NoArgsConstructor
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long creatorId;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** 开考时间 */
    private Instant startAt;

    /** 结束可进入时间 */
    private Instant endAt;

    /** 考试时长（分钟） */
    @Column(nullable = false)
    private Integer durationMinutes = 90;

    @Column(nullable = false)
    private Integer maxRetakes = 1;

    /** 切屏允许次数，超过则自动交卷 */
    @Column(nullable = false)
    private Integer switchLimit = 3;

    @Column(nullable = false)
    private Boolean fullscreenRequired = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExamStatus status = ExamStatus.DRAFT;

    /** 目标班级：逗号分隔，空表示全校可见（由业务层解释） */
    @Column(length = 500)
    private String targetClasses;

    /** 交卷后是否向考生展示本场排名；null 视为展示 */
    @Column
    private Boolean rankingVisible;

    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (rankingVisible == null) {
            rankingVisible = Boolean.TRUE;
        }
    }
}
