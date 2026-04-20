package com.campus.exam.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 单行全局配置（id 固定为 1）。
 */
@Entity
@Table(name = "platform_settings")
@Getter
@Setter
@NoArgsConstructor
public class PlatformSettings {

    @Id
    private Long id = 1L;

    @Column(nullable = false)
    private Integer minQuestionDifficulty = 1;

    @Column(nullable = false)
    private Integer maxQuestionDifficulty = 5;

    @Column(nullable = false)
    private Integer defaultExamDurationMinutes = 90;

    @Column(nullable = false)
    private Integer defaultMaxRetakes = 1;

    /** 站内通知总开关 */
    @Column(nullable = false)
    private Boolean notifyInAppEnabled = true;

    /** 邮件通道（需在运维配置 SMTP 后生效） */
    @Column(nullable = false)
    private Boolean notifyEmailEnabled = false;

    /** 短信通道（需对接服务商） */
    @Column(nullable = false)
    private Boolean notifySmsEnabled = false;
}
