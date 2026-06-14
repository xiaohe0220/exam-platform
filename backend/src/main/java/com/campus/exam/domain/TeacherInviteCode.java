package com.campus.exam.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "teacher_invite_codes", indexes = {
        @Index(columnList = "code", unique = true),
        @Index(columnList = "teacher_id")
})
@Getter
@Setter
@NoArgsConstructor
public class TeacherInviteCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String code;

    @Column(name = "teacher_id", nullable = false)
    private Long teacherId;

    @Column(name = "teacher_name", nullable = false, length = 100)
    private String teacherName;

    @Column(length = 120)
    private String college;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(nullable = false)
    private Integer maxUses = 50;

    @Column(nullable = false)
    private Integer usedCount = 0;

    @Column(name = "created_by_id")
    private Long createdById;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (enabled == null) {
            enabled = true;
        }
        if (maxUses == null) {
            maxUses = 50;
        }
        if (usedCount == null) {
            usedCount = 0;
        }
    }
}
