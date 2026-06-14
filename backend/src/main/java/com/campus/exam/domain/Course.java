package com.campus.exam.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "courses", indexes = @Index(columnList = "code", unique = true))
@Getter
@Setter
@NoArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 80, unique = true)
    private String code;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(length = 120)
    private String college;

    @Column(length = 120)
    private String teacherName;

    @Column(nullable = false)
    private Boolean enabled = true;

    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (enabled == null) {
            enabled = true;
        }
    }
}
