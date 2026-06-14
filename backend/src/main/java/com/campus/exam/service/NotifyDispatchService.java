package com.campus.exam.service;

import com.campus.exam.domain.UserAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.client.RestClient;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 外部通知通道：站内信由 InAppNotificationService 落库；邮件和短信在配置后发送。
 */
@Service
public class NotifyDispatchService {

    private static final Logger log = LoggerFactory.getLogger(NotifyDispatchService.class);
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final RestClient restClient;
    private final String smsWebhookUrl;

    public NotifyDispatchService(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${app.notify.sms-webhook-url:}") String smsWebhookUrl) {
        this.mailSenderProvider = mailSenderProvider;
        this.smsWebhookUrl = smsWebhookUrl;
        this.restClient = RestClient.create();
    }

    public void sendEmailIfPossible(UserAccount user, String subject, String text, boolean emailChannelEnabled) {
        if (!emailChannelEnabled) {
            return;
        }
        String to = user.getEmail();
        if (to == null || to.isBlank()) {
            log.debug("跳过邮件：用户 {} 未绑定邮箱", user.getUsername());
            return;
        }
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (sender == null) {
            log.warn("邮件通道已开启，但未配置 JavaMailSender：to={} subject={}", to, subject);
            return;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(text);
            sender.send(msg);
        } catch (Exception e) {
            log.warn("邮件发送失败：to={} subject={} reason={}", to, subject, e.getMessage());
        }
    }

    public void sendSmsIfPossible(String phone, String text, boolean smsChannelEnabled) {
        if (!smsChannelEnabled || phone == null || phone.isBlank()) {
            return;
        }
        if (smsWebhookUrl == null || smsWebhookUrl.isBlank()) {
            log.warn("短信通道已开启，但 APP_NOTIFY_SMS_WEBHOOK_URL 未配置：phone={}", phone);
            return;
        }
        try {
            restClient.post()
                    .uri(smsWebhookUrl)
                    .body(Map.of("phone", phone, "text", text))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("短信 Webhook 发送失败：phone={} reason={}", phone, e.getMessage());
        }
    }
}
