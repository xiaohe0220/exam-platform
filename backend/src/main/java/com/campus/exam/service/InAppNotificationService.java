package com.campus.exam.service;

import com.campus.exam.domain.*;
import com.campus.exam.repository.InAppNotificationRepository;
import com.campus.exam.repository.UserAccountRepository;
import com.campus.exam.web.dto.NotificationDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class InAppNotificationService {

    private final InAppNotificationRepository notificationRepository;
    private final UserAccountRepository userAccountRepository;
    private final PlatformSettingsService platformSettingsService;
    private final NotifyDispatchService notifyDispatchService;

    public InAppNotificationService(
            InAppNotificationRepository notificationRepository,
            UserAccountRepository userAccountRepository,
            PlatformSettingsService platformSettingsService,
            NotifyDispatchService notifyDispatchService) {
        this.notificationRepository = notificationRepository;
        this.userAccountRepository = userAccountRepository;
        this.platformSettingsService = platformSettingsService;
        this.notifyDispatchService = notifyDispatchService;
    }

    public List<NotificationDto> listMine(Long userId, int limit) {
        int n = Math.min(Math.max(limit, 1), 100);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .limit(n)
                .map(InAppNotificationService::toDto)
                .toList();
    }

    public long unreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadAtIsNull(userId);
    }

    @Transactional
    public void markRead(Long userId, Long notificationId) {
        InAppNotification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!n.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (n.getReadAt() == null) {
            n.setReadAt(java.time.Instant.now());
            notificationRepository.save(n);
        }
    }

    @Transactional
    public void markAllRead(Long userId) {
        for (InAppNotification n : notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)) {
            if (n.getReadAt() == null) {
                n.setReadAt(java.time.Instant.now());
                notificationRepository.save(n);
            }
        }
    }

    /** 考试发布：通知可见范围内的学生（站内信 + 可选邮件占位） */
    @Transactional
    public void notifyExamPublished(Exam exam) {
        PlatformSettings ps = platformSettingsService.getOrCreate();
        if (!Boolean.TRUE.equals(ps.getNotifyInAppEnabled())) {
            return;
        }
        Set<Long> userIds = resolveTargetStudentIds(exam);
        String title = "新考试发布";
        String body = "考试「" + exam.getTitle() + "」已发布，请在开放时间内参加。";
        for (Long uid : userIds) {
            saveInApp(uid, NotificationType.EXAM_PUBLISHED, title, body, exam.getId());
            userAccountRepository.findById(uid).ifPresent(u ->
            {
                notifyDispatchService.sendEmailIfPossible(
                        u,
                        title,
                        body,
                        Boolean.TRUE.equals(ps.getNotifyEmailEnabled()));
                notifyDispatchService.sendSmsIfPossible(
                        u.getPhone(),
                        title + "：" + body,
                        Boolean.TRUE.equals(ps.getNotifySmsEnabled()));
            });
        }
    }

    /** 成绩生成（交卷后） */
    @Transactional
    public void notifyGradePublished(Long studentUserId, String examTitle, Long examId) {
        PlatformSettings ps = platformSettingsService.getOrCreate();
        if (!Boolean.TRUE.equals(ps.getNotifyInAppEnabled())) {
            return;
        }
        String title = "成绩已生成";
        String body = "考试「" + examTitle + "」已阅卷计分，请查看成绩单。";
        saveInApp(studentUserId, NotificationType.GRADE_PUBLISHED, title, body, examId);
        userAccountRepository.findById(studentUserId).ifPresent(u ->
        {
            notifyDispatchService.sendEmailIfPossible(
                    u,
                    title,
                    body,
                    Boolean.TRUE.equals(ps.getNotifyEmailEnabled()));
            notifyDispatchService.sendSmsIfPossible(
                    u.getPhone(),
                    title + "：" + body,
                    Boolean.TRUE.equals(ps.getNotifySmsEnabled()));
        });
    }

    private void saveInApp(Long userId, NotificationType type, String title, String body, Long examId) {
        InAppNotification n = new InAppNotification();
        n.setUserId(userId);
        n.setType(type);
        n.setTitle(title);
        n.setBody(body);
        n.setExamId(examId);
        notificationRepository.save(n);
    }

    private Set<Long> resolveTargetStudentIds(Exam exam) {
        String targets = exam.getTargetClasses();
        if (targets == null || targets.isBlank()) {
            return userAccountRepository.findByRole(UserRole.STUDENT).stream()
                    .filter(u -> Boolean.TRUE.equals(u.getEnabled()))
                    .map(UserAccount::getId)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }
        Set<Long> ids = new LinkedHashSet<>();
        for (String c : targets.split(",")) {
            String cn = c.trim();
            if (cn.isEmpty()) {
                continue;
            }
            for (UserAccount u : userAccountRepository.findByClassName(cn)) {
                if (u.getRole() == UserRole.STUDENT && Boolean.TRUE.equals(u.getEnabled())) {
                    ids.add(u.getId());
                }
            }
        }
        return ids;
    }

    private static NotificationDto toDto(InAppNotification n) {
        return new NotificationDto(
                n.getId(),
                n.getType().name(),
                n.getTitle(),
                n.getBody(),
                n.getExamId(),
                n.getReadAt() != null,
                n.getCreatedAt());
    }
}
