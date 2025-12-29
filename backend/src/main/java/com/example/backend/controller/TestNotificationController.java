package com.example.backend.controller;

import com.example.backend.model.Notification;
import com.example.backend.model.User;
import com.example.backend.repository.NotificationRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.scheduler.SmartNotificationScheduler;
import com.example.backend.service.FcmService;
import com.example.backend.service.NotificationService;
import com.example.backend.service.NotificationTemplateService;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller để test tất cả các loại thông báo trong hệ thống
 * 
 * Sử dụng: Gọi các endpoints với userId để test từng loại notification
 * 
 * LƯU Ý: Chỉ dùng cho môi trường development/staging
 */
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
public class TestNotificationController {

    private final FcmService fcmService;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final NotificationTemplateService templateService;
    private final SmartNotificationScheduler smartNotificationScheduler;

    // =====================================================
    // FCM BASIC TESTS
    // =====================================================

    /**
     * Test gửi notification với notification payload (giống Firebase Console)
     */
    @GetMapping("/notification-payload/{userId}")
    public ResponseEntity<String> sendWithNotificationPayload(@PathVariable Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getFcmToken() == null) {
            return ResponseEntity.badRequest().body("User not found or no FCM token");
        }

        String title = "🔔 Test Notification Payload";
        String body = "Message này có notification payload - Giống Firebase Console";

        try {
            // Sử dụng fully qualified name để tránh xung đột với model Notification
            com.google.firebase.messaging.Notification fcmNotification = 
                com.google.firebase.messaging.Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            Message.Builder messageBuilder = Message.builder()
                    .setToken(user.getFcmToken())
                    .setNotification(fcmNotification);

            Map<String, String> data = new HashMap<>();
            data.put("title", title);
            data.put("body", body);
            data.put("type", "TEST");
            data.put("click_action", "FLUTTER_NOTIFICATION_CLICK");
            messageBuilder.putAllData(data);
            
            messageBuilder.setAndroidConfig(AndroidConfig.builder()
                .setPriority(AndroidConfig.Priority.HIGH)
                .setNotification(AndroidNotification.builder()
                    .setChannelId("high_importance_channel")
                    .setClickAction("FLUTTER_NOTIFICATION_CLICK")
                    .build())
                .build());

            String response = FirebaseMessaging.getInstance().send(messageBuilder.build());
            log.info("✅ Sent notification payload test to user {}: {}", userId, response);
            return ResponseEntity.ok("Sent: " + response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    /**
     * Test gửi data-only message (Flutter tự handle)
     */
    @GetMapping("/data-only/{userId}")
    public ResponseEntity<String> sendDataOnly(@PathVariable Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getFcmToken() == null) {
            return ResponseEntity.badRequest().body("User not found or no FCM token");
        }

        String title = "📱 Test Data Only";
        String body = "Message này chỉ có Data payload - Flutter tự handle";

        try {
            Message.Builder messageBuilder = Message.builder()
                    .setToken(user.getFcmToken());

            Map<String, String> data = new HashMap<>();
            data.put("title", title);
            data.put("body", body);
            data.put("type", "TEST");
            messageBuilder.putAllData(data);
            
            messageBuilder.setAndroidConfig(AndroidConfig.builder()
                .setPriority(AndroidConfig.Priority.HIGH)
                .build());

            String response = FirebaseMessaging.getInstance().send(messageBuilder.build());
            log.info("✅ Sent data-only test to user {}: {}", userId, response);
            return ResponseEntity.ok("Sent: " + response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    // =====================================================
    // BUDGET ALERT TESTS
    // =====================================================

    /**
     * Test gửi cảnh báo ngân sách (80% - WARNING)
     */
    @PostMapping("/notification/budget-warning/{userId}")
    public ResponseEntity<Map<String, Object>> testBudgetWarning(@PathVariable Long userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }

            // Render template NOT006
            Map<String, Object> placeholders = new HashMap<>();
            placeholders.put("budget_name", "Ăn uống");
            placeholders.put("percentage", 85.5);
            placeholders.put("spent_amount", 2565000.0);
            placeholders.put("budget_amount", 3000000.0);
            placeholders.put("days_remaining", 7);

            NotificationTemplateService.RenderedTemplate rendered = templateService.renderTemplate(
                "NOT006", placeholders
            );

            String title = rendered != null ? rendered.getTitle() : "⚠️ Cảnh báo Ngân sách";
            String message = rendered != null ? rendered.getContent() :
                    "Bạn đã chi 85.5% hạn mức 'Ăn uống', còn 7 ngày nữa là hết kỳ ngân sách.";

            // Lưu notification
            com.example.backend.model.Notification notification = new com.example.backend.model.Notification();
            notification.setUserId(userId);
            notification.setType("BUDGET_ALERT");
            notification.setTitle(title);
            notification.setContent(message);
            notification.setPriority(com.example.backend.model.Notification.NotificationPriority.MEDIUM);
            notification.setActionUrl("/budgets/1");
            notification.setIsRead(false);
            
            com.example.backend.model.Notification saved = notificationRepository.save(notification);

            // Gửi FCM
            fcmService.sendPushForNotification(saved);

            log.info("✅ Test BUDGET_WARNING sent to user {}", userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "type", "BUDGET_WARNING",
                "notificationId", saved.getId(),
                "title", title,
                "message", message
            ));
        } catch (Exception e) {
            log.error("❌ Error testing budget warning: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Test gửi cảnh báo ngân sách vượt quá (>100% - EXCEEDED)
     */
    @PostMapping("/notification/budget-exceeded/{userId}")
    public ResponseEntity<Map<String, Object>> testBudgetExceeded(@PathVariable Long userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }

            // Render template NOT007
            Map<String, Object> placeholders = new HashMap<>();
            placeholders.put("budget_name", "Mua sắm");
            placeholders.put("percentage", 125.0);
            placeholders.put("spent_amount", 6250000.0);
            placeholders.put("budget_amount", 5000000.0);

            NotificationTemplateService.RenderedTemplate rendered = templateService.renderTemplate(
                "NOT007", placeholders
            );

            String title = rendered != null ? rendered.getTitle() : "🚨 Ngân sách Vượt quá!";
            String message = rendered != null ? rendered.getContent() :
                    "Ngân sách 'Mua sắm' đã vượt quá! Đã chi 125% (6,250,000đ/5,000,000đ)";

            com.example.backend.model.Notification notification = new com.example.backend.model.Notification();
            notification.setUserId(userId);
            notification.setType("BUDGET_ALERT");
            notification.setTitle(title);
            notification.setContent(message);
            notification.setPriority(com.example.backend.model.Notification.NotificationPriority.HIGH);
            notification.setActionUrl("/budgets/2");
            notification.setIsRead(false);
            
            com.example.backend.model.Notification saved = notificationRepository.save(notification);
            fcmService.sendPushForNotification(saved);

            log.info("✅ Test BUDGET_EXCEEDED sent to user {}", userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "type", "BUDGET_EXCEEDED",
                "notificationId", saved.getId(),
                "title", title,
                "message", message
            ));
        } catch (Exception e) {
            log.error("❌ Error testing budget exceeded: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // =====================================================
    // SAVINGS SUGGESTION TESTS
    // =====================================================

    /**
     * Test gửi gợi ý tiết kiệm từ AI
     */
    @PostMapping("/notification/savings-suggestion/{userId}")
    public ResponseEntity<Map<String, Object>> testSavingsSuggestion(@PathVariable Long userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }

            // Render template NOT008
            Map<String, Object> placeholders = new HashMap<>();
            placeholders.put("category", "Trà sữa");
            placeholders.put("weekly_avg", 200000.0);
            placeholders.put("suggested_weekly", 100000.0);
            placeholders.put("monthly_savings", 400000.0);

            NotificationTemplateService.RenderedTemplate rendered = templateService.renderTemplate(
                "NOT008", placeholders
            );

            String title = rendered != null ? rendered.getTitle() : "💡 Gợi ý Tiết kiệm Thông minh";
            String message = rendered != null ? rendered.getContent() :
                    "FinPal nhận thấy bạn chi trung bình 200,000đ cho 'Trà sữa' mỗi tuần. " +
                    "Nếu bạn giảm còn 100,000đ, bạn sẽ tiết kiệm được 400,000đ/tháng!";

            com.example.backend.model.Notification notification = new com.example.backend.model.Notification();
            notification.setUserId(userId);
            notification.setType("SAVINGS_SUGGESTION");
            notification.setTitle(title);
            notification.setContent(message);
            notification.setPriority(com.example.backend.model.Notification.NotificationPriority.MEDIUM);
            notification.setActionUrl("/dashboard/insights");
            notification.setIsRead(false);
            
            com.example.backend.model.Notification saved = notificationRepository.save(notification);
            fcmService.sendPushForNotification(saved);

            log.info("✅ Test SAVINGS_SUGGESTION sent to user {}", userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "type", "SAVINGS_SUGGESTION",
                "notificationId", saved.getId(),
                "title", title,
                "message", message
            ));
        } catch (Exception e) {
            log.error("❌ Error testing savings suggestion: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // =====================================================
    // ANOMALY DETECTION TESTS
    // =====================================================

    /**
     * Test gửi cảnh báo chi tiêu bất thường
     */
    @PostMapping("/notification/anomaly-alert/{userId}")
    public ResponseEntity<Map<String, Object>> testAnomalyAlert(@PathVariable Long userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }

            // Render template NOT009
            Map<String, Object> placeholders = new HashMap<>();
            placeholders.put("category", "Điện nước");
            placeholders.put("current_amount", 1500000.0);
            placeholders.put("increase_percent", 50.0);
            placeholders.put("average_amount", 1000000.0);

            NotificationTemplateService.RenderedTemplate rendered = templateService.renderTemplate(
                "NOT009", placeholders
            );

            String title = rendered != null ? rendered.getTitle() : "🔔 Phát hiện Chi tiêu Bất thường";
            String message = rendered != null ? rendered.getContent() :
                    "Chi tiêu 'Điện nước' tháng này (1,500,000đ) cao hơn 50% so với trung bình (1,000,000đ).";

            com.example.backend.model.Notification notification = new com.example.backend.model.Notification();
            notification.setUserId(userId);
            notification.setType("ANOMALY_ALERT");
            notification.setTitle(title);
            notification.setContent(message);
            notification.setPriority(com.example.backend.model.Notification.NotificationPriority.HIGH);
            notification.setActionUrl("/transactions?category=Điện nước");
            notification.setIsRead(false);
            
            com.example.backend.model.Notification saved = notificationRepository.save(notification);
            fcmService.sendPushForNotification(saved);

            log.info("✅ Test ANOMALY_ALERT sent to user {}", userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "type", "ANOMALY_ALERT",
                "notificationId", saved.getId(),
                "title", title,
                "message", message
            ));
        } catch (Exception e) {
            log.error("❌ Error testing anomaly alert: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // =====================================================
    // GOAL REMINDER TESTS
    // =====================================================

    /**
     * Test gửi nhắc nhở mục tiêu 7 ngày
     */
    @PostMapping("/notification/goal-reminder/{userId}")
    public ResponseEntity<Map<String, Object>> testGoalReminder7Days(@PathVariable Long userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }

            // Render template NOT012
            Map<String, Object> placeholders = new HashMap<>();
            placeholders.put("goal_name", "Mua iPhone 15");
            placeholders.put("progress", 75.0);
            placeholders.put("current_amount", 22500000.0);
            placeholders.put("target_amount", 30000000.0);

            NotificationTemplateService.RenderedTemplate rendered = templateService.renderTemplate(
                "NOT012", placeholders
            );

            String title = rendered != null ? rendered.getTitle() : "🎯 Mục tiêu còn 7 ngày!";
            String message = rendered != null ? rendered.getContent() :
                    "Mục tiêu 'Mua iPhone 15' còn 7 ngày! Tiến độ: 75% (22,500,000đ/30,000,000đ). Cố gắng thêm nhé! 💪";

            com.example.backend.model.Notification notification = new com.example.backend.model.Notification();
            notification.setUserId(userId);
            notification.setType("GOAL_REMINDER");
            notification.setTitle(title);
            notification.setContent(message);
            notification.setPriority(com.example.backend.model.Notification.NotificationPriority.MEDIUM);
            notification.setActionUrl("/savings-goals/1");
            notification.setIsRead(false);
            
            com.example.backend.model.Notification saved = notificationRepository.save(notification);
            fcmService.sendPushForNotification(saved);

            log.info("✅ Test GOAL_REMINDER sent to user {}", userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "type", "GOAL_REMINDER",
                "notificationId", saved.getId(),
                "title", title,
                "message", message
            ));
        } catch (Exception e) {
            log.error("❌ Error testing goal reminder: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Test gửi thông báo hoàn thành mục tiêu
     */
    @PostMapping("/notification/goal-completed/{userId}")
    public ResponseEntity<Map<String, Object>> testGoalCompleted(@PathVariable Long userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }

            // Render template NOT014
            Map<String, Object> placeholders = new HashMap<>();
            placeholders.put("goal_name", "Quỹ Du lịch");
            placeholders.put("target_amount", 15000000.0);

            NotificationTemplateService.RenderedTemplate rendered = templateService.renderTemplate(
                "NOT014", placeholders
            );

            String title = rendered != null ? rendered.getTitle() : "🎉 Chúc mừng! Đạt Mục tiêu!";
            String message = rendered != null ? rendered.getContent() :
                    "Tuyệt vời! Bạn đã hoàn thành mục tiêu 'Quỹ Du lịch' (15,000,000đ)! 🎊";

            com.example.backend.model.Notification notification = new com.example.backend.model.Notification();
            notification.setUserId(userId);
            notification.setType("GOAL_REMINDER");
            notification.setTitle(title);
            notification.setContent(message);
            notification.setPriority(com.example.backend.model.Notification.NotificationPriority.HIGH);
            notification.setActionUrl("/savings-goals/2");
            notification.setIsRead(false);
            
            com.example.backend.model.Notification saved = notificationRepository.save(notification);
            fcmService.sendPushForNotification(saved);

            log.info("✅ Test GOAL_COMPLETED sent to user {}", userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "type", "GOAL_COMPLETED",
                "notificationId", saved.getId(),
                "title", title,
                "message", message
            ));
        } catch (Exception e) {
            log.error("❌ Error testing goal completed: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // =====================================================
    // SPENDING INSIGHT TESTS
    // =====================================================

    /**
     * Test gửi thành tích tiết kiệm
     */
    @PostMapping("/notification/spending-achievement/{userId}")
    public ResponseEntity<Map<String, Object>> testSpendingAchievement(@PathVariable Long userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }

            // Render template NOT010
            Map<String, Object> placeholders = new HashMap<>();
            placeholders.put("category", "Giải trí");
            placeholders.put("save_percent", 35.0);

            NotificationTemplateService.RenderedTemplate rendered = templateService.renderTemplate(
                "NOT010", placeholders
            );

            String title = rendered != null ? rendered.getTitle() : "🎉 Thành tích Tiết kiệm";
            String message = rendered != null ? rendered.getContent() :
                    "Tuyệt vời! Bạn đã tiết kiệm được trong danh mục 'Giải trí' tháng này. " +
                    "Chi tiêu thấp hơn 35% so với trung bình!";

            com.example.backend.model.Notification notification = new com.example.backend.model.Notification();
            notification.setUserId(userId);
            notification.setType("SPENDING_INSIGHT");
            notification.setTitle(title);
            notification.setContent(message);
            notification.setPriority(com.example.backend.model.Notification.NotificationPriority.LOW);
            notification.setActionUrl("/dashboard/analytics");
            notification.setIsRead(false);
            
            com.example.backend.model.Notification saved = notificationRepository.save(notification);
            fcmService.sendPushForNotification(saved);

            log.info("✅ Test SPENDING_ACHIEVEMENT sent to user {}", userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "type", "SPENDING_ACHIEVEMENT",
                "notificationId", saved.getId(),
                "title", title,
                "message", message
            ));
        } catch (Exception e) {
            log.error("❌ Error testing spending achievement: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Test gửi mẹo quản lý chi tiêu
     */
    @PostMapping("/notification/spending-tip/{userId}")
    public ResponseEntity<Map<String, Object>> testSpendingTip(@PathVariable Long userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }

            // Render template NOT011
            Map<String, Object> placeholders = new HashMap<>();
            placeholders.put("category", "Ăn ngoài");

            NotificationTemplateService.RenderedTemplate rendered = templateService.renderTemplate(
                "NOT011", placeholders
            );

            String title = rendered != null ? rendered.getTitle() : "💡 Mẹo Quản lý Chi tiêu";
            String message = rendered != null ? rendered.getContent() :
                    "Chi tiêu 'Ăn ngoài' đang có xu hướng tăng. Cân nhắc xem xét lại các khoản chi này.";

            com.example.backend.model.Notification notification = new com.example.backend.model.Notification();
            notification.setUserId(userId);
            notification.setType("SPENDING_INSIGHT");
            notification.setTitle(title);
            notification.setContent(message);
            notification.setPriority(com.example.backend.model.Notification.NotificationPriority.LOW);
            notification.setActionUrl("/dashboard/analytics");
            notification.setIsRead(false);
            
            com.example.backend.model.Notification saved = notificationRepository.save(notification);
            fcmService.sendPushForNotification(saved);

            log.info("✅ Test SPENDING_TIP sent to user {}", userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "type", "SPENDING_TIP",
                "notificationId", saved.getId(),
                "title", title,
                "message", message
            ));
        } catch (Exception e) {
            log.error("❌ Error testing spending tip: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // =====================================================
    // SUMMARY TESTS
    // =====================================================

    /**
     * Test gửi tổng kết tháng
     */
    @PostMapping("/notification/monthly-summary/{userId}")
    public ResponseEntity<Map<String, Object>> testMonthlySummary(@PathVariable Long userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }

            // Render template NOT015
            Map<String, Object> placeholders = new HashMap<>();
            placeholders.put("month", "11");
            placeholders.put("total_income", 25000000.0);
            placeholders.put("total_expense", 18000000.0);
            placeholders.put("savings", 7000000.0);
            placeholders.put("savings_percent", "28%");

            NotificationTemplateService.RenderedTemplate rendered = templateService.renderTemplate(
                "NOT015", placeholders
            );

            String title = rendered != null ? rendered.getTitle() : "📊 Tổng kết Tháng 11";
            String message = rendered != null ? rendered.getContent() :
                    "Tháng 11: Tổng thu 25,000,000đ - Tổng chi 18,000,000đ = Tiết kiệm 7,000,000đ (28%)";

            com.example.backend.model.Notification notification = new com.example.backend.model.Notification();
            notification.setUserId(userId);
            notification.setType("MONTHLY_REPORT");
            notification.setTitle(title);
            notification.setContent(message);
            notification.setPriority(com.example.backend.model.Notification.NotificationPriority.LOW);
            notification.setActionUrl("/dashboard/reports?period=monthly");
            notification.setIsRead(false);
            
            com.example.backend.model.Notification saved = notificationRepository.save(notification);
            fcmService.sendPushForNotification(saved);

            log.info("✅ Test MONTHLY_SUMMARY sent to user {}", userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "type", "MONTHLY_SUMMARY",
                "notificationId", saved.getId(),
                "title", title,
                "message", message
            ));
        } catch (Exception e) {
            log.error("❌ Error testing monthly summary: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Test gửi tổng kết tuần
     */
    @PostMapping("/notification/weekly-summary/{userId}")
    public ResponseEntity<Map<String, Object>> testWeeklySummary(@PathVariable Long userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }

            // Render template NOT016
            Map<String, Object> placeholders = new HashMap<>();
            placeholders.put("total_income", 6250000.0);
            placeholders.put("total_expense", 4500000.0);
            placeholders.put("savings", 1750000.0);
            placeholders.put("savings_percent", "28%");

            NotificationTemplateService.RenderedTemplate rendered = templateService.renderTemplate(
                "NOT016", placeholders
            );

            String title = rendered != null ? rendered.getTitle() : "📅 Tổng kết Tuần";
            String message = rendered != null ? rendered.getContent() :
                    "Tuần vừa qua: Tổng thu 6,250,000đ - Tổng chi 4,500,000đ = Tiết kiệm 1,750,000đ (28%)";

            com.example.backend.model.Notification notification = new com.example.backend.model.Notification();
            notification.setUserId(userId);
            notification.setType("WEEKLY_REPORT");
            notification.setTitle(title);
            notification.setContent(message);
            notification.setPriority(com.example.backend.model.Notification.NotificationPriority.LOW);
            notification.setActionUrl("/dashboard/reports?period=weekly");
            notification.setIsRead(false);
            
            com.example.backend.model.Notification saved = notificationRepository.save(notification);
            fcmService.sendPushForNotification(saved);

            log.info("✅ Test WEEKLY_SUMMARY sent to user {}", userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "type", "WEEKLY_SUMMARY",
                "notificationId", saved.getId(),
                "title", title,
                "message", message
            ));
        } catch (Exception e) {
            log.error("❌ Error testing weekly summary: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // =====================================================
    // SMART TIPS TEST
    // =====================================================

    /**
     * Test gửi Smart Tip từ AI
     */
    @PostMapping("/notification/smart-tip/{userId}")
    public ResponseEntity<Map<String, Object>> testSmartTip(@PathVariable Long userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }

            String title = "💡 Quy tắc 50/30/20";
            String message = "Phân bổ thu nhập: 50% cho nhu cầu thiết yếu, 30% cho mong muốn, 20% cho tiết kiệm. " +
                    "Dựa trên chi tiêu của bạn, bạn đang chi 60% cho thiết yếu - hãy xem xét điều chỉnh!";

            com.example.backend.model.Notification notification = new com.example.backend.model.Notification();
            notification.setUserId(userId);
            notification.setType("SMART_TIP");
            notification.setTitle(title);
            notification.setContent(message);
            notification.setPriority(com.example.backend.model.Notification.NotificationPriority.LOW);
            notification.setActionUrl("/dashboard");
            notification.setIsRead(false);
            
            com.example.backend.model.Notification saved = notificationRepository.save(notification);
            fcmService.sendPushForNotification(saved);

            log.info("✅ Test SMART_TIP sent to user {}", userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "type", "SMART_TIP",
                "notificationId", saved.getId(),
                "title", title,
                "message", message
            ));
        } catch (Exception e) {
            log.error("❌ Error testing smart tip: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // =====================================================
    // TEST ALL NOTIFICATIONS
    // =====================================================

    /**
     * Test tất cả các loại notification cùng lúc (delay 2s giữa mỗi loại)
     */
    @PostMapping("/notification/all/{userId}")
    public ResponseEntity<Map<String, Object>> testAllNotifications(@PathVariable Long userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }

            log.info("🚀 Starting test ALL notifications for user {}", userId);

            // Gọi từng loại notification
            testBudgetWarning(userId);
            Thread.sleep(1500);
            
            testBudgetExceeded(userId);
            Thread.sleep(1500);
            
            testSavingsSuggestion(userId);
            Thread.sleep(1500);
            
            testAnomalyAlert(userId);
            Thread.sleep(1500);
            
            testGoalReminder7Days(userId);
            Thread.sleep(1500);
            
            testGoalCompleted(userId);
            Thread.sleep(1500);
            
            testSpendingAchievement(userId);
            Thread.sleep(1500);
            
            testSpendingTip(userId);
            Thread.sleep(1500);
            
            testMonthlySummary(userId);
            Thread.sleep(1500);
            
            testWeeklySummary(userId);
            Thread.sleep(1500);
            
            testSmartTip(userId);

            log.info("✅ Test ALL notifications completed for user {}", userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã gửi 11 loại notification test đến user " + userId,
                "types", java.util.List.of(
                    "BUDGET_WARNING",
                    "BUDGET_EXCEEDED", 
                    "SAVINGS_SUGGESTION",
                    "ANOMALY_ALERT",
                    "GOAL_REMINDER",
                    "GOAL_COMPLETED",
                    "SPENDING_ACHIEVEMENT",
                    "SPENDING_TIP",
                    "MONTHLY_SUMMARY",
                    "WEEKLY_SUMMARY",
                    "SMART_TIP"
                )
            ));
        } catch (Exception e) {
            log.error("❌ Error testing all notifications: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // =====================================================
    // SCHEDULER MANUAL TRIGGER
    // =====================================================

    /**
     * Trigger scheduler thủ công để test scheduled notifications
     */
    @PostMapping("/scheduler/trigger-all")
    public ResponseEntity<Map<String, Object>> triggerAllSchedulers() {
        try {
            log.info("🔄 Triggering all schedulers manually...");

            // Trigger từng scheduler
            smartNotificationScheduler.checkBudgetAlerts();
            smartNotificationScheduler.sendAISavingsSuggestions();
            smartNotificationScheduler.detectAndAlertAnomalies();
            smartNotificationScheduler.sendGoalReminders();
            smartNotificationScheduler.sendProactiveSpendingInsights();

            log.info("✅ All schedulers triggered successfully");
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã trigger 5 schedulers: Budget, Savings, Anomaly, Goals, Insights",
                "schedulersTriggered", java.util.List.of(
                    "checkBudgetAlerts",
                    "sendAISavingsSuggestions",
                    "detectAndAlertAnomalies",
                    "sendGoalReminders",
                    "sendProactiveSpendingInsights"
                )
            ));
        } catch (Exception e) {
            log.error("❌ Error triggering schedulers: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}

