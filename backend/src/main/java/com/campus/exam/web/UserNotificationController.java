package com.campus.exam.web;

import com.campus.exam.security.AuthenticatedUser;
import com.campus.exam.service.InAppNotificationService;
import com.campus.exam.web.dto.NotificationDto;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/notifications")
public class UserNotificationController {

    private final InAppNotificationService inAppNotificationService;

    public UserNotificationController(InAppNotificationService inAppNotificationService) {
        this.inAppNotificationService = inAppNotificationService;
    }

    @GetMapping
    public List<NotificationDto> list(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(defaultValue = "30") int limit) {
        return inAppNotificationService.listMine(user.id(), limit);
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount(@AuthenticationPrincipal AuthenticatedUser user) {
        return Map.of("count", inAppNotificationService.unreadCount(user.id()));
    }

    @PostMapping("/{id}/read")
    public void markRead(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id) {
        inAppNotificationService.markRead(user.id(), id);
    }

    @PostMapping("/read-all")
    public void markAllRead(@AuthenticationPrincipal AuthenticatedUser user) {
        inAppNotificationService.markAllRead(user.id());
    }
}
