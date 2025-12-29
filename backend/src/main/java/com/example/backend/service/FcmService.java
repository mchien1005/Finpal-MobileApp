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

            // Tạo notification payload (Giống Firebase Console)
            com.google.firebase.messaging.Notification notification = 
                com.google.firebase.messaging.Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            // Tạo message
            Message.Builder messageBuilder = Message.builder()
                    .setToken(token)
                    .setNotification(notification); // Quan trọng: Có notification payload

            // Thêm data payload (để xử lý click action và logic custom)
            Map<String, String> dataWithTitleBody = new HashMap<>();
            if (data != null) {
                dataWithTitleBody.putAll(data);
            }
            // Cũng put title/body vào data để backup
            dataWithTitleBody.put("title", title);
            dataWithTitleBody.put("body", body);
            dataWithTitleBody.put("click_action", "FLUTTER_NOTIFICATION_CLICK");
            messageBuilder.putAllData(dataWithTitleBody);

            // Cấu hình cho Android
            messageBuilder.setAndroidConfig(AndroidConfig.builder()
                    .setPriority(AndroidConfig.Priority.HIGH)
                    .setNotification(AndroidNotification.builder()
                            .setChannelId("high_importance_channel") // Quan trọng cho Android 8+
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
