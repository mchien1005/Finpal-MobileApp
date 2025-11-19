package com.example.backend.controller;

import com.example.backend.dto.NotificationResponse;
import com.example.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Notification System Controller (FR3.1)
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * GET /api/notifications - Lấy tất cả notifications
     * Query params: isRead (Boolean)
     */
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getAllNotifications(
            Authentication authentication,
            @RequestParam(required = false) Boolean isRead) {
        String username = authentication.getName();
        List<NotificationResponse> notifications = notificationService.getAllNotifications(username, isRead);
        return ResponseEntity.ok(notifications);
    }

    /**
     * GET /api/notifications/unread-count - Đếm số notifications chưa đọc
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        String username = authentication.getName();
        Long count = notificationService.countUnreadNotifications(username);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    /**
     * PUT /api/notifications/{id}/read - Đánh dấu notification là đã đọc
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            Authentication authentication,
            @PathVariable Long id) {
        String username = authentication.getName();
        NotificationResponse notification = notificationService.markAsRead(username, id);
        return ResponseEntity.ok(notification);
    }

    /**
     * PUT /api/notifications/read-all - Đánh dấu tất cả là đã đọc
     */
    @PutMapping("/read-all")
    public ResponseEntity<Map<String, String>> markAllAsRead(Authentication authentication) {
        String username = authentication.getName();
        notificationService.markAllAsRead(username);
        return ResponseEntity.ok(Map.of("message", "All notifications marked as read"));
    }

    /**
     * DELETE /api/notifications/{id} - Xóa notification
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
            Authentication authentication,
            @PathVariable Long id) {
        String username = authentication.getName();
        notificationService.deleteNotification(username, id);
        return ResponseEntity.noContent().build();
    }

    /**
     * DELETE /api/notifications/read - Xóa tất cả notifications đã đọc
     */
    @DeleteMapping("/read")
    public ResponseEntity<Map<String, String>> deleteAllRead(Authentication authentication) {
        String username = authentication.getName();
        notificationService.deleteAllRead(username);
        return ResponseEntity.ok(Map.of("message", "All read notifications deleted"));
    }
}
