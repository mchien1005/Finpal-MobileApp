package com.example.backend.service;

import com.example.backend.dto.NotificationResponse;
import com.example.backend.dto.SmartTip;
import com.example.backend.dto.SmartTipsResponse;
import com.example.backend.model.Notification;
import com.example.backend.model.Transaction;
import com.example.backend.model.User;
import com.example.backend.repository.NotificationRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service quản lý Hệ thống Thông báo (Notification System) - FR3.1
 * 
 * Chức năng chính:
 * - CRUD thông báo (lấy, đánh dấu đã đọc, xóa)
 * - Tạo thông báo cho user/admin
 * - Gửi cảnh báo giao dịch bất thường
 * - Gửi Smart Tips từ AI
 * - Dọn dẹp thông báo cũ
 * - Kiểm tra cài đặt thông báo của user trước khi gửi
 * 
 * Lưu ý: Các scheduled notifications (budget alerts, goal reminders, insights)
 * được xử lý bởi SmartNotificationScheduler để tránh duplicate.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final AIInsightsService aiInsightsService;
    private final FcmService fcmService;
    @Lazy
    private final NotificationSettingsService notificationSettingsService;

    /**
     * Lấy tất cả thông báo của user
     * 
     * @param username Tên đăng nhập của user
     * @param isRead   Lọc theo trạng thái đã đọc (true/false, nếu null thì lấy tất
     *                 cả)
     * @return List<NotificationResponse> chứa danh sách thông báo
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getAllNotifications(String username, Boolean isRead) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Notification> notifications;
        if (isRead != null) {
            notifications = notificationRepository.findByUserIdAndIsRead(user.getId(), isRead);
        } else {
            notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        }

        return notifications.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Đếm số thông báo chưa đọc (hiển thị badge trên icon chuông)
     */
    @Transactional(readOnly = true)
    public Long countUnreadNotifications(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return notificationRepository.countByUserIdAndIsRead(user.getId(), false);
    }

    /**
     * Đánh dấu thông báo là đã đọc
     * 
     * @param username       Tên đăng nhập của user
     * @param notificationId ID của thông báo
     * @return NotificationResponse chứa thông tin thông báo sau khi cập nhật
     */
    @Transactional
    public NotificationResponse markAsRead(String username, Long notificationId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        // Check ownership
        if (!notification.getUserId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to notification");
        }

        if (!notification.getIsRead()) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
            notification = notificationRepository.save(notification);
        }

        return convertToResponse(notification);
    }

    /**
     * Đánh dấu tất cả thông báo là đã đọc
     * 
     * @param username Tên đăng nhập của user
     */
    @Transactional
    public void markAllAsRead(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndIsRead(user.getId(), false);

        LocalDateTime now = LocalDateTime.now();
        unreadNotifications.forEach(notification -> {
            notification.setIsRead(true);
            notification.setReadAt(now);
        });

        notificationRepository.saveAll(unreadNotifications);
    }

    /**
     * Xóa thông báo
     * 
     * @param username       Tên đăng nhập của user
     * @param notificationId ID của thông báo cần xóa
     */
    @Transactional
    public void deleteNotification(String username, Long notificationId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        // Check ownership
        if (!notification.getUserId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to notification");
        }

        notificationRepository.delete(notification);
    }

    /**
     * Xóa tất cả thông báo đã đọc (dọc dẹp inbox)
     */
    @Transactional
    public void deleteAllRead(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Notification> readNotifications = notificationRepository.findByUserIdAndIsRead(user.getId(), true);

        notificationRepository.deleteAll(readNotifications);
    }

    /**
     * Tạo thông báo cảnh báo ngân sách (budget alert)
     * 
     * @param userId   ID của user
     * @param budgetId ID của ngân sách
     * @param message  Nội dung cảnh báo
     * @param priority Mức độ ưu tiên (LOW/MEDIUM/HIGH)
     */
    @Transactional
    public void createBudgetAlert(Long userId, Long budgetId, String message, String priority) {
        // Kiểm tra user có bật cảnh báo ngân sách không
        if (!notificationSettingsService.isBudgetAlertsEnabled(userId)) {
            log.debug("⏭️ Skipping budget alert for user {} - disabled in settings", userId);
            return;
        }

        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType("BUDGET_ALERT");
        notification.setTitle("Cảnh báo Ngân sách");
        notification.setContent(message);
        notification.setActionUrl("/budgets/" + budgetId);
        notification.setIsRead(false);
        notification.setPriority(Notification.NotificationPriority.valueOf(priority));

        notificationRepository.save(notification);
    }

    /**
     * Tạo thông báo nhắc nhở mục tiêu tiết kiệm
     * 
     * @param userId  ID của user
     * @param goalId  ID của mục tiêu
     * @param message Nội dung nhắc nhở
     */
    @Transactional
    public void createSavingsGoalReminder(Long userId, Long goalId, String message) {
        // Kiểm tra user có bật nhắc nhở mục tiêu không
        if (!notificationSettingsService.isGoalRemindersEnabled(userId)) {
            log.debug("⏭️ Skipping goal reminder for user {} - disabled in settings", userId);
            return;
        }

        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType("GOAL_REMINDER");
        notification.setTitle("Nhắc nhở Mục tiêu Tiết kiệm");
        notification.setContent(message);
        notification.setActionUrl("/savings-goals/" + goalId);
        notification.setIsRead(false);
        notification.setPriority(Notification.NotificationPriority.MEDIUM);

        notificationRepository.save(notification);
    }

    /**
     * Convert Notification to Response DTO
     */
    private NotificationResponse convertToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .type(notification.getType())
                .title(notification.getTitle())
                .content(notification.getContent())
                .actionUrl(notification.getActionUrl())
                .isRead(notification.getIsRead())
                .readAt(notification.getReadAt())
                .priority(notification.getPriority().toString())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    /**
     * Cleanup old read notifications (called by scheduler)
     */
    @Transactional
    public void cleanupOldNotifications() {
        log.info("Cleaning up old read notifications...");

        // Delete notifications that are read and older than 30 days
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);

        int deletedCount = notificationRepository.deleteByIsReadAndCreatedAtBefore(true, cutoffDate);

        log.info("Deleted {} old read notifications", deletedCount);
    }

    /**
     * Generate Smart Tips from BackendAI and send via FCM (called by scheduler)
     * Gọi BackendAI để lấy smart tips và gửi push notification đến điện thoại user
     */
    @Transactional
    public void generateSmartTips() {
        log.info("💡 Generating Smart Tips from AI for all users...");

        List<User> allUsers = userRepository.findAll();
        int tipCount = 0;

        for (User user : allUsers) {
            try {
                // Kiểm tra user có bật gợi ý tiết kiệm không
                if (!notificationSettingsService.isSavingsTipsEnabled(user.getId())) {
                    log.debug("⏭️ Skipping smart tips for user {} - disabled in settings", user.getId());
                    continue;
                }

                // Gọi BackendAI để lấy smart tips
                com.example.backend.dto.SmartTipsResponse tips = aiInsightsService.getSmartTips(user.getId(), 3);

                if (tips != null && tips.getTips() != null && !tips.getTips().isEmpty()) {
                    // Lấy tip đầu tiên (quan trọng nhất)
                    com.example.backend.dto.SmartTip topTip = tips.getTips().get(0);

                    String title = "💡 " + (topTip.getTitle() != null ? topTip.getTitle() : "Gợi ý Thông minh");
                    String content = topTip.getContent() != null ? topTip.getContent() : "Xem gợi ý mới từ FinPal AI";

                    // Tạo notification trong database
                    Notification notification = new Notification();
                    notification.setUserId(user.getId());
                    notification.setType("SMART_TIP");
                    notification.setTitle(title);
                    notification.setContent(content);
                    notification.setActionUrl(topTip.getActionUrl() != null ? topTip.getActionUrl() : "/dashboard");
                    notification.setIsRead(false);
                    notification.setPriority(Notification.NotificationPriority.LOW);

                    Notification savedNotification = notificationRepository.save(notification);
                    tipCount++;

                    // Gửi FCM push notification đến điện thoại
                    try {
                        java.util.Map<String, String> data = new java.util.HashMap<>();
                        data.put("type", "SMART_TIP");
                        data.put("tipType", topTip.getTipType() != null ? topTip.getTipType() : "general");
                        data.put("notificationId", String.valueOf(savedNotification.getId()));
                        if (topTip.getCategory() != null) {
                            data.put("category", topTip.getCategory());
                        }

                        fcmService.sendPushToUser(
                                user.getId(),
                                title,
                                content,
                                data);

                        log.debug("📱 FCM push sent for smart tip to user {}", user.getId());

                    } catch (Exception fcmError) {
                        log.warn("Failed to send FCM for smart tip: {}", fcmError.getMessage());
                    }
                }
            } catch (Exception e) {
                log.error("Error generating smart tips for user {}: {}", user.getId(), e.getMessage());
            }
        }

        log.info("✅ Smart Tips generated: {} notifications created and pushed via FCM", tipCount);
    }

    /**
     * Gửi notification cho tất cả admin users
     */
    @Transactional
    public void sendNotificationToAllAdmins(String title, String message) {
        log.info("Sending notification to all admins: {}", title);

        List<User> admins = userRepository.findByRole(com.example.backend.model.Role.ADMIN);

        for (User admin : admins) {
            try {
                Notification notification = new Notification();
                notification.setUserId(admin.getId());
                notification.setType("SYSTEM");
                notification.setTitle(title);
                notification.setContent(message);
                notification.setCreatedAt(LocalDateTime.now());
                notification.setIsRead(false);
                notification.setPriority(Notification.NotificationPriority.HIGH);
                notification.setActionUrl("/admin/user-requests");

                notificationRepository.save(notification);

                log.debug("Notification sent to admin: {}", admin.getUsername());

            } catch (Exception e) {
                log.error("Error sending notification to admin {}", admin.getUsername(), e);
            }
        }

        log.info("Notification sent to {} admin(s)", admins.size());
    }

    /**
     * Gửi notification cho một user cụ thể
     */
    @Transactional
    public void sendNotificationToUser(Long userId, String title, String message,
            String type,
            Notification.NotificationPriority priority) {
        try {
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setType(type);
            notification.setTitle(title);
            notification.setContent(message);
            notification.setCreatedAt(LocalDateTime.now());
            notification.setIsRead(false);
            notification.setPriority(priority);

            notificationRepository.save(notification);

            log.info("Notification sent to user ID {}: {}", userId, title);

        } catch (Exception e) {
            log.error("Error sending notification to user {}", userId, e);
        }
    }

    /**
     * Gửi cảnh báo giao dịch bất thường (realtime anomaly warning)
     * Được gọi khi user tạo giao dịch mới và AI phát hiện bất thường
     * 
     * Bao gồm:
     * 1. Lưu notification vào database
     * 2. Gửi FCM push notification đến điện thoại user
     * 
     * @param user        User cần gửi cảnh báo
     * @param message     Nội dung cảnh báo (từ AI)
     * @param transaction Giao dịch bị đánh dấu bất thường
     */
    @Transactional
    public void sendAnomalyWarning(User user, String message, Transaction transaction) {
        try {
            // Kiểm tra user có bật cảnh báo bảo mật không
            if (!notificationSettingsService.isSecurityAlertsEnabled(user.getId())) {
                log.debug("⏭️ Skipping anomaly warning for user {} - security alerts disabled", user.getUsername());
                return;
            }

            // 1. Lưu notification vào database
            Notification notification = new Notification();
            notification.setUserId(user.getId());
            notification.setType("ANOMALY_ALERT");
            notification.setTitle("🚨 Giao dịch Bất thường");
            notification.setContent(message);
            notification.setCreatedAt(LocalDateTime.now());
            notification.setIsRead(false);
            notification.setPriority(Notification.NotificationPriority.HIGH);
            // Link đến transaction bị đánh dấu bất thường
            notification.setActionUrl("/transactions/" + transaction.getId());

            Notification savedNotification = notificationRepository.save(notification);

            log.warn("🚨 Anomaly warning saved for user {}: {} - Transaction ID: {}",
                    user.getUsername(), message, transaction.getId());

            // 2. Gửi FCM push notification đến điện thoại user
            try {
                java.util.Map<String, String> data = new java.util.HashMap<>();
                data.put("type", "ANOMALY_ALERT");
                data.put("notificationId", String.valueOf(savedNotification.getId()));
                data.put("transactionId", String.valueOf(transaction.getId()));
                data.put("amount", String.valueOf(transaction.getAmount()));
                data.put("category", transaction.getCategory() != null ? transaction.getCategory().getName() : "Khác");

                fcmService.sendPushToUser(
                        user.getId(),
                        "🚨 Giao dịch Bất thường",
                        message,
                        data);

                log.info("📱 FCM push notification sent to user {}", user.getUsername());

            } catch (Exception fcmError) {
                // FCM thất bại không ảnh hưởng đến notification đã lưu
                log.warn("Failed to send FCM push: {}", fcmError.getMessage());
            }

        } catch (Exception e) {
            log.error("Error sending anomaly warning to user {}: {}", user.getId(), e.getMessage());
        }
    }
}
