package com.example.backend.service;

import com.example.backend.model.Notification;
import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * FCM Service - Dịch vụ gửi Push Notification qua Firebase Cloud Messaging
 * 
 * Service này chịu trách nhiệm:
 * - Gửi push notification đến một thiết bị cụ thể
 * - Gửi push notification đến nhiều thiết bị (multicast)
 * - Gửi notification theo topic
 * - Xử lý các loại thông báo khác nhau (cảnh báo, gợi ý, thành tích, v.v.)
 * 
 * Các notification types:
 * - BUDGET_ALERT: Cảnh báo ngân sách
 * - SAVINGS_SUGGESTION: Gợi ý tiết kiệm từ AI
 * - SPENDING_INSIGHT: Phân tích chi tiêu
 * - GOAL_REMINDER: Nhắc nhở mục tiêu
 * - ANOMALY_ALERT: Cảnh báo giao dịch bất thường
 * - SYSTEM: Thông báo hệ thống
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FcmService {

    private final UserRepository userRepository;

    /**
     * Kiểm tra Firebase đã được khởi tạo chưa
     */
    public boolean isFirebaseInitialized() {
        return !FirebaseApp.getApps().isEmpty();
    }

    /**
     * Gửi push notification đến một user cụ thể
     * 
     * @param userId  ID của user
     * @param title   Tiêu đề thông báo
     * @param body    Nội dung thông báo
     * @param data    Dữ liệu bổ sung (optional)
     * @return true nếu gửi thành công, false nếu thất bại
     */
    @Async
    public void sendPushToUser(Long userId, String title, String body, Map<String, String> data) {
        try {
            if (!isFirebaseInitialized()) {
                log.warn("Firebase not initialized. Skipping push notification to user {}", userId);
                return;
            }

            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                log.warn("User {} not found for push notification", userId);
                return;
            }

            // Kiểm tra user có bật notification không
            if (user.getNotificationEnabled() == null || !user.getNotificationEnabled()) {
                log.debug("User {} has disabled notifications", userId);
                return;
            }

            // Kiểm tra FCM token
            String fcmToken = user.getFcmToken();
            if (fcmToken == null || fcmToken.isEmpty()) {
                log.debug("User {} has no FCM token registered", userId);
                return;
            }

            sendPushNotification(fcmToken, title, body, data);
            log.info("✅ Push notification sent to user {}: {}", userId, title);

        } catch (Exception e) {
            log.error("❌ Failed to send push notification to user {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Gửi push notification đến một FCM token cụ thể
     * 
     * @param token FCM device token
     * @param title Tiêu đề
     * @param body  Nội dung
     * @param data  Dữ liệu bổ sung
     */
    public void sendPushNotification(String token, String title, String body, Map<String, String> data) {
        try {
            if (!isFirebaseInitialized()) {
                log.warn("Firebase not initialized. Cannot send push notification.");
                return;
            }

            // Tạo notification payload
            com.google.firebase.messaging.Notification notification = 
                com.google.firebase.messaging.Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            // Tạo message
            Message.Builder messageBuilder = Message.builder()
                    .setToken(token)
                    .setNotification(notification);

            // Thêm data payload nếu có
            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            // Cấu hình cho Android
            messageBuilder.setAndroidConfig(AndroidConfig.builder()
                    .setPriority(AndroidConfig.Priority.HIGH)
                    .setNotification(AndroidNotification.builder()
                            .setSound("default")
                            .setClickAction("FLUTTER_NOTIFICATION_CLICK")
                            .build())
                    .build());

            // Cấu hình cho iOS
            messageBuilder.setApnsConfig(ApnsConfig.builder()
                    .setAps(Aps.builder()
                            .setSound("default")
                            .setBadge(1)
                            .build())
                    .build());

            // Gửi message
            String response = FirebaseMessaging.getInstance().send(messageBuilder.build());
            log.debug("FCM message sent successfully: {}", response);

        } catch (FirebaseMessagingException e) {
            handleFcmException(token, e);
        } catch (Exception e) {
            log.error("Unexpected error sending FCM message: {}", e.getMessage());
        }
    }

    /**
     * Gửi push notification khi tạo Notification mới trong database
     * Được gọi từ NotificationService khi tạo notification
     * 
     * @param notification Notification entity vừa được tạo
     */
    @Async
    public void sendPushForNotification(Notification notification) {
        if (notification == null) {
            return;
        }

        Map<String, String> data = new HashMap<>();
        data.put("notificationId", String.valueOf(notification.getId()));
        data.put("type", notification.getType());
        data.put("priority", notification.getPriority().toString());
        
        if (notification.getActionUrl() != null) {
            data.put("actionUrl", notification.getActionUrl());
        }

        sendPushToUser(
            notification.getUserId(),
            notification.getTitle(),
            notification.getContent(),
            data
        );
    }

    /**
     * Gửi push notification đến nhiều users cùng lúc (Multicast)
     * 
     * @param userIds List user IDs
     * @param title   Tiêu đề
     * @param body    Nội dung
     * @param data    Dữ liệu bổ sung
     */
    @Async
    public void sendPushToMultipleUsers(List<Long> userIds, String title, String body, Map<String, String> data) {
        try {
            if (!isFirebaseInitialized()) {
                log.warn("Firebase not initialized. Skipping multicast push notification.");
                return;
            }

            // Lấy FCM tokens của các users có bật notification
            List<String> tokens = userRepository.findAllById(userIds).stream()
                    .filter(user -> user.getNotificationEnabled() != null && user.getNotificationEnabled())
                    .filter(user -> user.getFcmToken() != null && !user.getFcmToken().isEmpty())
                    .map(User::getFcmToken)
                    .collect(Collectors.toList());

            if (tokens.isEmpty()) {
                log.debug("No valid FCM tokens found for multicast");
                return;
            }

            // Tạo multicast message
            MulticastMessage.Builder messageBuilder = MulticastMessage.builder()
                    .addAllTokens(tokens)
                    .setNotification(com.google.firebase.messaging.Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build());

            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            // Gửi multicast
            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(messageBuilder.build());
            
            log.info("✅ Multicast push sent: {}/{} successful", 
                    response.getSuccessCount(), tokens.size());

            // Log failed tokens
            if (response.getFailureCount() > 0) {
                log.warn("⚠️ {} push notifications failed", response.getFailureCount());
            }

        } catch (Exception e) {
            log.error("❌ Failed to send multicast push notification: {}", e.getMessage());
        }
    }

    /**
     * Gửi cảnh báo ngân sách (Budget Alert)
     */
    public void sendBudgetAlert(Long userId, String budgetName, double usagePercentage, double spentAmount) {
        String title = "⚠️ Cảnh báo Ngân sách";
        String body;
        
        if (usagePercentage >= 100) {
            body = String.format("Ngân sách '%s' đã vượt quá! Đã chi %.0f%% (%,.0fđ)", 
                    budgetName, usagePercentage, spentAmount);
        } else {
            body = String.format("Ngân sách '%s' sắp hết! Đã chi %.0f%% (%,.0fđ)", 
                    budgetName, usagePercentage, spentAmount);
        }

        Map<String, String> data = new HashMap<>();
        data.put("type", "BUDGET_ALERT");
        data.put("budgetName", budgetName);
        data.put("usagePercentage", String.valueOf(usagePercentage));

        sendPushToUser(userId, title, body, data);
    }

    /**
     * Gửi gợi ý tiết kiệm từ AI (Savings Suggestion)
     */
    public void sendSavingsSuggestion(Long userId, String category, double currentWeeklyAvg, 
                                       double suggestedTarget, double monthlySavings) {
        String title = "💡 Gợi ý Tiết kiệm Thông minh";
        String body = String.format(
                "FinPal nhận thấy bạn chi trung bình %,.0fđ cho '%s' mỗi tuần. " +
                "Nếu bạn giảm còn %,.0fđ, bạn sẽ tiết kiệm được %,.0fđ/tháng.",
                currentWeeklyAvg, category, suggestedTarget, monthlySavings
        );

        Map<String, String> data = new HashMap<>();
        data.put("type", "SAVINGS_SUGGESTION");
        data.put("category", category);
        data.put("potentialSavings", String.valueOf(monthlySavings));

        sendPushToUser(userId, title, body, data);
    }

    /**
     * Gửi cảnh báo giao dịch bất thường (Anomaly Alert)
     */
    public void sendAnomalyAlert(Long userId, String category, double currentAmount, 
                                  double averageAmount, double percentageIncrease) {
        String title = "🔔 Phát hiện Chi tiêu Bất thường";
        String body = String.format(
                "Chi tiêu '%s' tháng này (%,.0fđ) cao hơn %.0f%% so với trung bình (%,.0fđ).",
                category, currentAmount, percentageIncrease, averageAmount
        );

        Map<String, String> data = new HashMap<>();
        data.put("type", "ANOMALY_ALERT");
        data.put("category", category);
        data.put("percentageIncrease", String.valueOf(percentageIncrease));

        sendPushToUser(userId, title, body, data);
    }

    /**
     * Gửi nhắc nhở mục tiêu tiết kiệm (Goal Reminder)
     */
    public void sendGoalReminder(Long userId, String goalName, double currentAmount, 
                                  double targetAmount, int daysRemaining) {
        String title = "🎯 Nhắc nhở Mục tiêu Tiết kiệm";
        String body;
        
        double progress = (currentAmount / targetAmount) * 100;
        
        if (daysRemaining == 0) {
            body = String.format("Hôm nay là deadline của mục tiêu '%s'! Tiến độ: %.0f%% (%,.0fđ/%,.0fđ)",
                    goalName, progress, currentAmount, targetAmount);
        } else {
            body = String.format("Mục tiêu '%s' còn %d ngày! Tiến độ: %.0f%% (%,.0fđ/%,.0fđ)",
                    goalName, daysRemaining, progress, currentAmount, targetAmount);
        }

        Map<String, String> data = new HashMap<>();
        data.put("type", "GOAL_REMINDER");
        data.put("goalName", goalName);
        data.put("daysRemaining", String.valueOf(daysRemaining));
        data.put("progress", String.valueOf(progress));

        sendPushToUser(userId, title, body, data);
    }

    /**
     * Gửi thông báo thành tích (Achievement)
     */
    public void sendAchievementNotification(Long userId, String achievementTitle, String message) {
        String title = "🎉 " + achievementTitle;

        Map<String, String> data = new HashMap<>();
        data.put("type", "ACHIEVEMENT");

        sendPushToUser(userId, title, message, data);
    }

    /**
     * Xử lý FCM exceptions  
     */
    private void handleFcmException(String token, FirebaseMessagingException e) {
        MessagingErrorCode errorCode = e.getMessagingErrorCode();
        
        if (errorCode == MessagingErrorCode.UNREGISTERED || 
            errorCode == MessagingErrorCode.INVALID_ARGUMENT) {
            // Token không hợp lệ hoặc đã hết hạn, cần xóa khỏi database
            log.warn("Invalid FCM token detected, should be removed from database");
            removeInvalidToken(token);
        } else {
            log.error("FCM error [{}]: {}", errorCode, e.getMessage());
        }
    }

    /**
     * Xóa FCM token không hợp lệ khỏi database
     */
    private void removeInvalidToken(String token) {
        try {
            userRepository.findAll().stream()
                    .filter(user -> token.equals(user.getFcmToken()))
                    .forEach(user -> {
                        user.setFcmToken(null);
                        userRepository.save(user);
                        log.info("Removed invalid FCM token for user {}", user.getId());
                    });
        } catch (Exception e) {
            log.error("Failed to remove invalid FCM token: {}", e.getMessage());
        }
    }
}
