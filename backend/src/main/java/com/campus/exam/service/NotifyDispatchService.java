package com.campus.exam.service;

import com.campus.exam.domain.UserAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 外部通道占位：邮件/短信需在运维配置 SMTP 或短信网关后在此对接。
 * 当前仅记录日志，避免无配置时抛错。
 */
@Service
public class NotifyDispatchService {

    private static final Logger log = LoggerFactory.getLogger(NotifyDispatchService.class);

    public void sendEmailIfPossible(UserAccount user, String subject, String text, boolean emailChannelEnabled) {
        if (!emailChannelEnabled) {
            return;
        }
        String to = user.getEmail();
        if (to == null || to.isBlank()) {
            log.debug("跳过邮件：用户 {} 未绑定邮箱", user.getUsername());
            return;
        }
        log.info("[邮件占位] to={} subject={} body={}", to, subject, text.length() > 120 ? text.substring(0, 120) + "…" : text);
    }

    public void sendSmsIfPossible(String phone, String text, boolean smsChannelEnabled) {
        if (!smsChannelEnabled || phone == null || phone.isBlank()) {
            return;
        }
        log.info("[短信占位] phone={} text={}", phone, text.length() > 80 ? text.substring(0, 80) + "…" : text);
    }
}
