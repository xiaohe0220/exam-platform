package com.campus.exam.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "users", indexes = @Index(columnList = "username", unique = true))
@Getter
@Setter
@NoArgsConstructor
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @Column(nullable = false)
    @JsonIgnore
    private String passwordHash;

    @Column(nullable = false, length = 100)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private UserRole role;

    /** 学生所属班级名称，教师/管理员可空 */
    @Column(length = 100)
    private String className;

    @Column(length = 100)
    private String college;

    /** 个性签名 / 备注（学生可自填） */
    @Column(name = "personal_note", length = 300)
    private String personalNote;

    /** 个性化设置 JSON：主题色、紧凑模式等 */
    @Column(name = "settings_json", length = 4000)
    private String settingsJson;

    /** 通知用邮箱（可选，未配置则仅站内信） */
    @Column(length = 120)
    private String email;

    /**
     * 是否允许登录；旧库可为 null，视为启用。
     * 不使用 NOT NULL，避免在已有数据的表上做 ddl 时部分数据库拒绝无默认值的批量更新。
     */
    @Column
    private Boolean enabled;

    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (enabled == null) {
            enabled = Boolean.TRUE;
        }
    }
}
