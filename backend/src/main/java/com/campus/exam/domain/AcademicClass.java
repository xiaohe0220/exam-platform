package com.campus.exam.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "academic_classes", indexes = @Index(columnList = "name", unique = true))
@Getter
@Setter
@NoArgsConstructor
public class AcademicClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String name;

    @Column(length = 120)
    private String college;

    @Column(length = 120)
    private String major;

    @Column(length = 40)
    private String grade;

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
