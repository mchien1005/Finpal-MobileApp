package com.example.backend.scheduler;

import com.example.backend.dto.SavingsSuggestionsResponse;
import com.example.backend.dto.SpendingInsight;
import com.example.backend.model.Budget;
import com.example.backend.model.Notification;
import com.example.backend.model.SavingsGoal;
import com.example.backend.model.User;
import com.example.backend.repository.*;
import com.example.backend.service.AIInsightsService;
import com.example.backend.service.FcmService;
import com.example.backend.service.NotificationService;
import com.example.backend.service.NotificationTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Smart Notification Scheduler - Hệ thống Thông báo Chủ động Thông minh
 * 
 * Scheduler này tự động phân tích dữ liệu và gửi thông báo chủ động đến user:
 * 
 * 1. BUDGET ALERTS (Cảnh báo Ngân sách) - Chạy mỗi 4 giờ
 *    - "Bạn đã chi 70% hạn mức 'Ăn ngoài' của tháng này, chỉ còn 10 ngày nữa là hết tháng."
 *    - "Ngân sách 'Mua sắm' đã vượt quá! Đã chi 120%"
 * 
 * 2. SAVINGS SUGGESTIONS (Gợi ý Tiết kiệm từ AI) - Chạy mỗi Chủ nhật 9:00 AM
 *    - "FinPal nhận thấy bạn chi trung bình 200.000đ cho 'Trà sữa' mỗi tuần. 
 *       Nếu bạn giảm còn 100.000đ, bạn sẽ tiết kiệm được 400.000đ/tháng."
 * 
 * 3. ANOMALY DETECTION (Phát hiện Bất thường) - Chạy mỗi ngày 7:00 PM
 *    - "Hóa đơn tiền điện tháng này (500.000đ) cao hơn 30% so với trung bình (350.000đ)."
 * 
 * 4. GOAL REMINDERS (Nhắc nhở Mục tiêu) - Chạy mỗi ngày 8:00 AM
 *    - "Mục tiêu 'Mua iPhone' còn 7 ngày! Tiến độ: 60%"
 * 
 * 5. PROACTIVE INSIGHTS (Phân tích Chi tiêu Proactive) - Chạy mỗi ngày 7:00 PM
 *    - "Chi tiêu 'Ăn ngoài' đang có xu hướng tăng 20% so với tháng trước"
 */
@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(value = "scheduler.smart-notifications.enabled", havingValue = "true", matchIfMissing = true)
public class SmartNotificationScheduler {

    private final UserRepository userRepository;
    private final BudgetRepository budgetRepository;
    private final SavingsGoalRepository savingsGoalRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final NotificationTemplateService templateService;
    private final AIInsightsService aiInsightsService;
    private final FcmService fcmService;

    // Template codes từ bảng mau_thong_bao
    private static final String TPL_BUDGET_WARNING = "NOT006";
    private static final String TPL_BUDGET_EXCEEDED = "NOT007";
    private static final String TPL_SAVINGS_SUGGESTION = "NOT008";
    private static final String TPL_ANOMALY_DETECTED = "NOT009";
    private static final String TPL_SPENDING_ACHIEVEMENT = "NOT010";
    private static final String TPL_SPENDING_TIP = "NOT011";
    private static final String TPL_GOAL_REMINDER = "NOT012";
    private static final String TPL_GOAL_DEADLINE = "NOT013";
    private static final String TPL_GOAL_COMPLETED = "NOT014";
    private static final String TPL_MONTHLY_SUMMARY = "NOT015";

    // ======================== BUDGET ALERTS ========================
    
    /**
     * Kiểm tra và gửi cảnh báo ngân sách
     * Chạy mỗi 4 giờ (0:00, 4:00, 8:00, 12:00, 16:00, 20:00)
     * 
     * Phát hiện:
     * - Ngân sách sắp hết (>= alertThreshold, thường là 80%)
     * - Ngân sách đã vượt (>= 100%)
     * - Context: Còn bao nhiêu ngày đến hết kỳ ngân sách
     */
    @Scheduled(cron = "${scheduler.smart-notifications.budget-alerts.cron:0 0 */4 * * *}")
    @Transactional
    public void checkBudgetAlerts() {
        log.info("🔔 Starting smart budget alerts check...");

        try {
            List<User> activeUsers = userRepository.findAll().stream()
                    .filter(u -> u.getIsActive() != null && u.getIsActive())
                    .filter(u -> u.getNotificationEnabled() == null || u.getNotificationEnabled())
                    .toList();

            int alertCount = 0;
            LocalDate today = LocalDate.now();

            for (User user : activeUsers) {
                try {
                    List<Budget> activeBudgets = budgetRepository.findActiveBudgetsForDate(user.getId(), today);

                    for (Budget budget : activeBudgets) {
                        alertCount += processBudgetAlert(user, budget, today);
                    }
                } catch (Exception e) {
                    log.error("Error checking budget for user {}: {}", user.getId(), e.getMessage());
                }
            }

            log.info("✅ Budget alerts check completed. Sent {} alerts", alertCount);

        } catch (Exception e) {
            log.error("❌ Error in budget alerts scheduler: {}", e.getMessage());
        }
    }

    private int processBudgetAlert(User user, Budget budget, LocalDate today) {
        // Tính số tiền đã chi
        BigDecimal spentAmount = transactionRepository.sumByUserIdAndCategoryIdAndTypeAndDateRange(
                user.getId(),
                budget.getCategoryId(),
                com.example.backend.model.Transaction.TransactionType.EXPENSE,
                budget.getStartDate().atStartOfDay(),
                budget.getEndDate().atTime(23, 59, 59));

        if (spentAmount == null || budget.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }

        double usagePercentage = spentAmount.divide(budget.getAmount(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();

        // Tính số ngày còn lại
        long daysRemaining = ChronoUnit.DAYS.between(today, budget.getEndDate());

        // Chỉ gửi cảnh báo nếu chưa có cảnh báo tương tự trong 24h
        String alertKey = "budget_" + budget.getId() + "_" + (usagePercentage >= 100 ? "over" : "warning");
        if (hasRecentNotification(user.getId(), "BUDGET_ALERT", alertKey, 24)) {
            return 0;
        }

        // Xác định level cảnh báo và gửi notification
        if (usagePercentage >= 100) {
            // Đã vượt ngân sách - dùng template NOT007
            java.util.Map<String, Object> placeholders = new java.util.HashMap<>();
            placeholders.put("budget_name", budget.getName() != null ? budget.getName() : "Ngân sách");
            placeholders.put("percentage", usagePercentage);
            placeholders.put("spent_amount", spentAmount.doubleValue());
            placeholders.put("budget_amount", budget.getAmount().doubleValue());

            NotificationTemplateService.RenderedTemplate rendered = templateService.renderTemplate(
                TPL_BUDGET_EXCEEDED,  // NOT007
                placeholders
            );

            String title = rendered != null ? rendered.getTitle() : "🚨 Ngân sách Vượt quá!";
            String message = rendered != null ? rendered.getContent() :
                    String.format("Ngân sách '%s' đã vượt quá! Đã chi %.0f%% (%,.0fđ/%,.0fđ)",
                            budget.getName(), usagePercentage, spentAmount.doubleValue(), budget.getAmount().doubleValue());

            createAndPushNotification(user, "BUDGET_ALERT", title,
                    message, Notification.NotificationPriority.HIGH, "/budgets/" + budget.getId());

            fcmService.sendBudgetAlert(user.getId(), budget.getName(), usagePercentage, spentAmount.doubleValue());
            return 1;

        } else if (usagePercentage >= budget.getAlertThreshold()) {
            // Sắp hết ngân sách - dùng template NOT006
            java.util.Map<String, Object> placeholders = new java.util.HashMap<>();
            placeholders.put("budget_name", budget.getName() != null ? budget.getName() : "Ngân sách");
            placeholders.put("percentage", usagePercentage);
            placeholders.put("spent_amount", spentAmount.doubleValue());
            placeholders.put("budget_amount", budget.getAmount().doubleValue());
            placeholders.put("days_remaining", daysRemaining);

            NotificationTemplateService.RenderedTemplate rendered = templateService.renderTemplate(
                TPL_BUDGET_WARNING,  // NOT006
                placeholders
            );

            String title = rendered != null ? rendered.getTitle() : "⚠️ Cảnh báo Ngân sách";
            String message = rendered != null ? rendered.getContent() :
                    String.format("Bạn đã chi %.0f%% hạn mức '%s' (%,.0fđ/%,.0fđ), còn %d ngày nữa là hết kỳ ngân sách.",
                            usagePercentage, budget.getName(), 
                            spentAmount.doubleValue(), budget.getAmount().doubleValue(),
                            daysRemaining);

            createAndPushNotification(user, "BUDGET_ALERT", title,
                    message, Notification.NotificationPriority.MEDIUM, "/budgets/" + budget.getId());

            fcmService.sendBudgetAlert(user.getId(), budget.getName(), usagePercentage, spentAmount.doubleValue());
            return 1;
        }

        return 0;
    }

    // ======================== AI SAVINGS SUGGESTIONS ========================
    
    /**
     * Gửi gợi ý tiết kiệm thông minh từ AI
     * Chạy mỗi Chủ nhật lúc 9:00 AM
     * 
     * Sử dụng template NOT008 từ bảng mau_thong_bao
     */
    @Scheduled(cron = "${scheduler.smart-notifications.savings-suggestions.cron:0 0 9 * * SUN}")
    @Transactional
    public void sendAISavingsSuggestions() {
        log.info("💡 Starting AI savings suggestions generation...");

        try {
            List<User> activeUsers = getActiveUsersWithNotifications();
            int suggestionCount = 0;

            for (User user : activeUsers) {
                try {
                    SavingsSuggestionsResponse suggestions = aiInsightsService.getSavingsSuggestions(user.getId());

                    if (suggestions != null && suggestions.getSuggestions() != null 
                            && !suggestions.getSuggestions().isEmpty()) {
                        
                        // Lấy gợi ý top 1 có tiềm năng tiết kiệm cao nhất
                        SavingsSuggestionsResponse.SavingsSuggestion topSuggestion = 
                                suggestions.getSuggestions().get(0);

                        // Tạo map placeholders (dùng HashMap vì Map.of không chấp nhận null)
                        java.util.Map<String, Object> placeholders = new java.util.HashMap<>();
                        placeholders.put("category", topSuggestion.getCategory() != null ? topSuggestion.getCategory() : "Chi tiêu");
                        placeholders.put("weekly_avg", topSuggestion.getCurrentWeeklyAvg() != null ? topSuggestion.getCurrentWeeklyAvg() : 0.0);
                        placeholders.put("suggested_weekly", topSuggestion.getSuggestedWeeklyTarget() != null ? topSuggestion.getSuggestedWeeklyTarget() : 0.0);
                        placeholders.put("monthly_savings", topSuggestion.getMonthlySavings() != null ? topSuggestion.getMonthlySavings() : 0.0);

                        // Lấy và render template từ database
                        NotificationTemplateService.RenderedTemplate rendered = templateService.renderTemplate(
                            TPL_SAVINGS_SUGGESTION,  // NOT008
                            placeholders
                        );

                        // Fallback nếu không có template
                        String title = rendered != null ? rendered.getTitle() : "💡 Gợi ý Tiết kiệm Thông minh";
                        String message = rendered != null ? rendered.getContent() : 
                                String.format("FinPal nhận thấy bạn chi trung bình %,.0fđ cho '%s' mỗi tuần. " +
                                        "Nếu bạn giảm còn %,.0fđ, bạn sẽ tiết kiệm được %,.0fđ/tháng!",
                                        topSuggestion.getCurrentWeeklyAvg() != null ? topSuggestion.getCurrentWeeklyAvg() : 0.0,
                                        topSuggestion.getCategory() != null ? topSuggestion.getCategory() : "Chi tiêu",
                                        topSuggestion.getSuggestedWeeklyTarget() != null ? topSuggestion.getSuggestedWeeklyTarget() : 0.0,
                                        topSuggestion.getMonthlySavings() != null ? topSuggestion.getMonthlySavings() : 0.0
                                );

                        createAndPushNotification(user, "SAVINGS_SUGGESTION", 
                                title, message, 
                                rendered != null ? rendered.getPriority() : Notification.NotificationPriority.MEDIUM, 
                                "/dashboard/insights");

                        // Gửi push notification
                        fcmService.sendSavingsSuggestion(
                                user.getId(),
                                topSuggestion.getCategory(),
                                topSuggestion.getCurrentWeeklyAvg(),
                                topSuggestion.getSuggestedWeeklyTarget(),
                                topSuggestion.getMonthlySavings()
                        );

                        suggestionCount++;
                    }
                } catch (Exception e) {
                    log.error("Error generating savings suggestions for user {}: {}", 
                            user.getId(), e.getMessage());
                }
            }

            log.info("✅ AI savings suggestions completed. Sent {} suggestions", suggestionCount);

        } catch (Exception e) {
            log.error("❌ Error in savings suggestions scheduler: {}", e.getMessage());
        }
    }

    // ======================== ANOMALY DETECTION ========================
    
    /**
     * Phát hiện và gửi cảnh báo chi tiêu bất thường
     * Chạy mỗi ngày lúc 7:00 PM
     * 
     * Ví dụ: "Hóa đơn tiền điện tháng này (500.000đ) cao hơn 30% so với trung bình (350.000đ)."
     */
    @Scheduled(cron = "${scheduler.smart-notifications.anomaly-detection.cron:0 0 19 * * *}")
    @Transactional
    public void detectAndAlertAnomalies() {
        log.info("🔍 Starting anomaly detection and alerting...");

        try {
            List<User> activeUsers = getActiveUsersWithNotifications();
            int anomalyCount = 0;

            for (User user : activeUsers) {
                try {
                    List<SpendingInsight> insights = aiInsightsService.getProactiveInsights(user.getId());

                    if (insights != null) {
                        for (SpendingInsight insight : insights) {
                            // Chỉ gửi cảnh báo cho các insights có impact cao hoặc là warning
                            if ("warning".equals(insight.getInsightType()) && insight.getImpactScore() >= 0.7) {
                                
                                // Tránh gửi trùng lặp
                                String alertKey = "anomaly_" + insight.getCategory() + "_" + LocalDate.now();
                                if (hasRecentNotification(user.getId(), "ANOMALY_ALERT", alertKey, 24)) {
                                    continue;
                                }

                                createAndPushNotification(user, "ANOMALY_ALERT",
                                        "🔔 Phát hiện Chi tiêu Bất thường",
                                        insight.getMessage(), 
                                        Notification.NotificationPriority.HIGH,
                                        insight.getCategory() != null 
                                                ? "/transactions?category=" + insight.getCategory()
                                                : "/dashboard/analytics");

                                // Parse message để lấy thông tin chi tiết (simplified)
                                fcmService.sendAnomalyAlert(
                                        user.getId(),
                                        insight.getCategory() != null ? insight.getCategory() : "Tổng chi tiêu",
                                        0, 0, 30 // Simplified - actual values would be parsed
                                );

                                anomalyCount++;
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("Error detecting anomalies for user {}: {}", user.getId(), e.getMessage());
                }
            }

            log.info("✅ Anomaly detection completed. Sent {} alerts", anomalyCount);

        } catch (Exception e) {
            log.error("❌ Error in anomaly detection scheduler: {}", e.getMessage());
        }
    }

    // ======================== GOAL REMINDERS ========================
    
    /**
     * Gửi nhắc nhở mục tiêu tiết kiệm
     * Chạy mỗi ngày lúc 8:00 AM
     * 
     * - Nhắc nhở 7 ngày trước deadline
     * - Nhắc nhở vào ngày deadline
     * - Chúc mừng khi đạt mục tiêu
     */
    @Scheduled(cron = "${scheduler.smart-notifications.goal-reminders.cron:0 0 8 * * *}")
    @Transactional
    public void sendGoalReminders() {
        log.info("🎯 Starting savings goal reminders...");

        try {
            List<User> activeUsers = getActiveUsersWithNotifications();
            int reminderCount = 0;

            for (User user : activeUsers) {
                try {
                    List<SavingsGoal> activeGoals = savingsGoalRepository.findByUserIdAndStatus(
                            user.getId(), SavingsGoal.GoalStatus.ACTIVE);

                    for (SavingsGoal goal : activeGoals) {
                        reminderCount += processGoalReminder(user, goal);
                    }
                } catch (Exception e) {
                    log.error("Error checking goals for user {}: {}", user.getId(), e.getMessage());
                }
            }

            log.info("✅ Goal reminders completed. Sent {} reminders", reminderCount);

        } catch (Exception e) {
            log.error("❌ Error in goal reminders scheduler: {}", e.getMessage());
        }
    }

    private int processGoalReminder(User user, SavingsGoal goal) {
        if (goal.getDeadline() == null) {
            return 0;
        }

        long daysUntilDeadline = ChronoUnit.DAYS.between(LocalDate.now(), goal.getDeadline());
        double progress = goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0
                ? goal.getCurrentAmount().divide(goal.getTargetAmount(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).doubleValue()
                : 0;

        String alertKey = "goal_" + goal.getId() + "_" + daysUntilDeadline;
        if (hasRecentNotification(user.getId(), "GOAL_REMINDER", alertKey, 24)) {
            return 0;
        }

        // Đã đạt mục tiêu - dùng template NOT014
        if (progress >= 100) {
            java.util.Map<String, Object> placeholders = new java.util.HashMap<>();
            placeholders.put("goal_name", goal.getName() != null ? goal.getName() : "Mục tiêu");
            placeholders.put("target_amount", goal.getTargetAmount().doubleValue());

            NotificationTemplateService.RenderedTemplate rendered = templateService.renderTemplate(
                TPL_GOAL_COMPLETED,  // NOT014
                placeholders
            );

            String title = rendered != null ? rendered.getTitle() : "🎉 Chúc mừng! Đạt Mục tiêu!";
            String message = rendered != null ? rendered.getContent() :
                    String.format("Tuyệt vời! Bạn đã hoàn thành mục tiêu '%s' (%,.0fđ)! 🎊",
                            goal.getName(), goal.getTargetAmount().doubleValue());

            createAndPushNotification(user, "GOAL_REMINDER", title, message,
                    Notification.NotificationPriority.HIGH,
                    "/savings-goals/" + goal.getId());

            fcmService.sendAchievementNotification(user.getId(), 
                    "Hoàn thành Mục tiêu!",
                    String.format("Bạn đã đạt mục tiêu '%s'! 🎊", goal.getName()));
            return 1;
        }

        // Reminder 7 ngày trước deadline - dùng template NOT012
        if (daysUntilDeadline == 7) {
            java.util.Map<String, Object> placeholders = new java.util.HashMap<>();
            placeholders.put("goal_name", goal.getName() != null ? goal.getName() : "Mục tiêu");
            placeholders.put("progress", progress);
            placeholders.put("current_amount", goal.getCurrentAmount().doubleValue());
            placeholders.put("target_amount", goal.getTargetAmount().doubleValue());

            NotificationTemplateService.RenderedTemplate rendered = templateService.renderTemplate(
                TPL_GOAL_REMINDER,  // NOT012
                placeholders
            );

            String title = rendered != null ? rendered.getTitle() : "🎯 Mục tiêu còn 7 ngày!";
            String message = rendered != null ? rendered.getContent() :
                    String.format("Mục tiêu '%s' còn 7 ngày! Tiến độ: %.0f%% (%,.0fđ/%,.0fđ). Cố gắng thêm nhé! 💪",
                            goal.getName(), progress, 
                            goal.getCurrentAmount().doubleValue(), goal.getTargetAmount().doubleValue());

            createAndPushNotification(user, "GOAL_REMINDER", title, message,
                    Notification.NotificationPriority.MEDIUM,
                    "/savings-goals/" + goal.getId());

            fcmService.sendGoalReminder(user.getId(), goal.getName(),
                    goal.getCurrentAmount().doubleValue(), goal.getTargetAmount().doubleValue(), 7);
            return 1;
        }

        // Reminder vào ngày deadline - dùng template NOT013
        if (daysUntilDeadline == 0) {
            java.util.Map<String, Object> placeholders = new java.util.HashMap<>();
            placeholders.put("goal_name", goal.getName() != null ? goal.getName() : "Mục tiêu");
            placeholders.put("progress", progress);
            placeholders.put("current_amount", goal.getCurrentAmount().doubleValue());
            placeholders.put("target_amount", goal.getTargetAmount().doubleValue());

            NotificationTemplateService.RenderedTemplate rendered = templateService.renderTemplate(
                TPL_GOAL_DEADLINE,  // NOT013
                placeholders
            );

            String title = rendered != null ? rendered.getTitle() : "⏰ Deadline Mục tiêu Hôm nay!";
            String message = rendered != null ? rendered.getContent() :
                    String.format("Hôm nay là deadline của mục tiêu '%s'! Tiến độ: %.0f%% (%,.0fđ/%,.0fđ)",
                            goal.getName(), progress,
                            goal.getCurrentAmount().doubleValue(), goal.getTargetAmount().doubleValue());

            createAndPushNotification(user, "GOAL_REMINDER", title, message,
                    Notification.NotificationPriority.HIGH,
                    "/savings-goals/" + goal.getId());

            fcmService.sendGoalReminder(user.getId(), goal.getName(),
                    goal.getCurrentAmount().doubleValue(), goal.getTargetAmount().doubleValue(), 0);
            return 1;
        }

        return 0;
    }

    // ======================== PROACTIVE SPENDING INSIGHTS ========================
    
    /**
     * Gửi phân tích chi tiêu chủ động
     * Chạy mỗi ngày lúc 7:30 PM
     * 
     * - Phân tích xu hướng chi tiêu
     * - Gợi ý cải thiện
     * - Thành tích tiết kiệm
     * 
     * Sử dụng templates NOT010 (achievement), NOT011 (tip)
     */
    @Scheduled(cron = "${scheduler.smart-notifications.spending-insights.cron:0 30 19 * * *}")
    @Transactional
    public void sendProactiveSpendingInsights() {
        log.info("📊 Starting proactive spending insights...");

        try {
            List<User> activeUsers = getActiveUsersWithNotifications();
            int insightCount = 0;

            for (User user : activeUsers) {
                try {
                    List<SpendingInsight> insights = aiInsightsService.getProactiveInsights(user.getId());

                    if (insights != null) {
                        for (SpendingInsight insight : insights) {
                            // Gửi achievements và tips với impact score cao
                            if (("achievement".equals(insight.getInsightType()) || 
                                 "tip".equals(insight.getInsightType())) 
                                    && insight.getImpactScore() >= 0.6) {

                                // Lấy title từ template tương ứng
                                String templateCode = switch (insight.getInsightType()) {
                                    case "achievement" -> TPL_SPENDING_ACHIEVEMENT;  // NOT010
                                    case "tip" -> TPL_SPENDING_TIP;  // NOT011
                                    default -> null;
                                };

                                String title;
                                if (templateCode != null) {
                                    var template = templateService.getTemplateByCode(templateCode);
                                    title = template != null ? template.getTitle() : getDefaultTitle(insight.getInsightType());
                                } else {
                                    title = getDefaultTitle(insight.getInsightType());
                                }

                                // Message đã được render từ BackendAI
                                createAndPushNotification(user, "SPENDING_INSIGHT",
                                        title, insight.getMessage(),
                                        Notification.NotificationPriority.LOW,
                                        "/dashboard/analytics");

                                insightCount++;
                                
                                // Chỉ gửi 1 insight/ngày để không spam user
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("Error generating insights for user {}: {}", user.getId(), e.getMessage());
                }
            }

            log.info("✅ Proactive insights completed. Sent {} insights", insightCount);

        } catch (Exception e) {
            log.error("❌ Error in proactive insights scheduler: {}", e.getMessage());
        }
    }

    private String getDefaultTitle(String insightType) {
        return switch (insightType) {
            case "achievement" -> "🎉 Thành tích Tiết kiệm";
            case "tip" -> "💡 Mẹo Quản lý Chi tiêu";
            case "warning" -> "⚠️ Cảnh báo Chi tiêu";
            default -> "📊 Phân tích Chi tiêu";
        };
    }

    // ======================== HELPER METHODS ========================

    /**
     * Lấy danh sách users active và có bật notification
     */
    private List<User> getActiveUsersWithNotifications() {
        return userRepository.findAll().stream()
                .filter(u -> u.getIsActive() != null && u.getIsActive())
                .filter(u -> u.getNotificationEnabled() == null || u.getNotificationEnabled())
                .toList();
    }

    /**
     * Kiểm tra có notification tương tự trong N giờ gần đây không
     * Để tránh gửi thông báo trùng lặp
     */
    private boolean hasRecentNotification(Long userId, String type, String contentKey, int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        List<Notification> recent = notificationRepository.findByUserIdAndTypeAndCreatedAtAfter(
                userId, type, since);
        
        return recent.stream().anyMatch(n -> 
                n.getContent() != null && n.getContent().contains(contentKey));
    }

    /**
     * Tạo notification trong database và gửi push notification
     */
    private void createAndPushNotification(User user, String type, String title, String content,
                                            Notification.NotificationPriority priority, String actionUrl) {
        // Tạo notification trong database
        Notification notification = new Notification();
        notification.setUserId(user.getId());
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setPriority(priority);
        notification.setActionUrl(actionUrl);
        notification.setIsRead(false);

        Notification saved = notificationRepository.save(notification);

        // Gửi push notification (async)
        fcmService.sendPushForNotification(saved);

        log.debug("Created notification for user {}: {}", user.getId(), title);
    }
}
