package com.example.backend.service;

import com.example.backend.dto.NotificationResponse;
import com.example.backend.dto.SavingsSuggestionsResponse;
import com.example.backend.dto.SpendingInsight;
import com.example.backend.model.Budget;
import com.example.backend.model.Notification;
import com.example.backend.model.SavingsGoal;
import com.example.backend.model.User;
import com.example.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service quản lý Hệ thống Thông báo (Notification System) - FR3.1
 * Chức năng: Thông báo ngân sách vượt quá, nhắc nhở mục tiêu, gợi ý tiết kiệm
 * AI, cảnh báo chi tiêu
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final BudgetRepository budgetRepository;
    private final SavingsGoalRepository savingsGoalRepository;
    private final TransactionRepository transactionRepository;
    private final AIInsightsService aiInsightsService;

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
     * Kiểm tra và tạo cảnh báo ngân sách tự động cho user
     * - Vượt 100%: Cảnh báo HIGH
     * - Vượt alertThreshold (ví dụ 80%): Cảnh báo MEDIUM
     */
    @Transactional
    public void checkBudgetAlertsForUser(Long userId) {
        LocalDate today = LocalDate.now();
        List<Budget> activeBudgets = budgetRepository.findActiveBudgetsForDate(userId, today);

        for (Budget budget : activeBudgets) {
            // Tính số tiền đã chi trong ngân sách
            java.math.BigDecimal spentAmount = transactionRepository.sumByUserIdAndCategoryIdAndTypeAndDateRange(
                    userId,
                    budget.getCategoryId(),
                    com.example.backend.model.Transaction.TransactionType.EXPENSE,
                    budget.getStartDate().atStartOfDay(),
                    budget.getEndDate().atTime(23, 59, 59));

            if (spentAmount != null && budget.getAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
                double usagePercentage = spentAmount.divide(budget.getAmount(), 4, java.math.RoundingMode.HALF_UP)
                        .multiply(java.math.BigDecimal.valueOf(100))
                        .doubleValue();

                // Kiểm tra có nên gửi cảnh báo không
                if (usagePercentage >= 100) {
                    // Kiểm tra đã có cảnh báo hôm nay chưa
                    boolean alertExists = notificationRepository.existsByUserIdAndTypeAndActionUrl(
                            userId, "BUDGET_ALERT", "/budgets/" + budget.getId());

                    if (!alertExists) {
                        createBudgetAlert(userId, budget.getId(),
                                String.format("Ngân sách '%s' đã vượt quá! Đã chi %.0f%% (%.0f VND)",
                                        budget.getName(), usagePercentage, spentAmount),
                                "HIGH");
                    }
                } else if (usagePercentage >= budget.getAlertThreshold()) {
                    boolean alertExists = notificationRepository.existsByUserIdAndTypeAndActionUrl(
                            userId, "BUDGET_ALERT", "/budgets/" + budget.getId());

                    if (!alertExists) {
                        createBudgetAlert(userId, budget.getId(),
                                String.format("Ngân sách '%s' sắp hết! Đã chi %.0f%% (%.0f VND)",
                                        budget.getName(), usagePercentage, spentAmount),
                                "MEDIUM");
                    }
                }
            }
        }
    }

    /**
     * Kiểm tra và tạo savings goal reminders tự động
     */
    @Transactional
    public void checkSavingsGoalReminders(Long userId) {
        List<SavingsGoal> activeGoals = savingsGoalRepository.findByUserIdAndStatus(
                userId, SavingsGoal.GoalStatus.ACTIVE);

        for (SavingsGoal goal : activeGoals) {
            if (goal.getDeadline() != null) {
                long daysUntilDeadline = ChronoUnit.DAYS.between(LocalDate.now(), goal.getDeadline());

                // Remind 7 days before deadline
                if (daysUntilDeadline == 7) {
                    createSavingsGoalReminder(userId, goal.getId(),
                            String.format("Mục tiêu '%s' còn 7 ngày! Hiện tại: %.0f/%.0f VND",
                                    goal.getName(), goal.getCurrentAmount(), goal.getTargetAmount()));
                }

                // Remind on deadline day
                if (daysUntilDeadline == 0) {
                    createSavingsGoalReminder(userId, goal.getId(),
                            String.format("Hôm nay là deadline của mục tiêu '%s'! Hiện tại: %.0f/%.0f VND",
                                    goal.getName(), goal.getCurrentAmount(), goal.getTargetAmount()));
                }
            }
        }
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
     * Generate budget alerts for all users (called by scheduler)
     */
    @Transactional
    public void generateBudgetAlerts() {
        log.info("Generating budget alerts for all users...");

        List<User> allUsers = userRepository.findAll();
        int alertCount = 0;

        for (User user : allUsers) {
            try {
                checkBudgetAlertsForUser(user.getId());
                alertCount++;
            } catch (Exception e) {
                log.error("Error checking budget alerts for user {}: {}", user.getId(), e.getMessage());
            }
        }

        log.info("Budget alerts generated for {} users", alertCount);
    }

    /**
     * Generate savings suggestions using AI (called by scheduler)
     */
    @Transactional
    public void generateSavingsSuggestions() {
        log.info("Generating AI-powered savings suggestions for all users...");

        List<User> allUsers = userRepository.findAll();
        int suggestionCount = 0;

        for (User user : allUsers) {
            try {
                // Get AI suggestions
                SavingsSuggestionsResponse suggestions = aiInsightsService.getSavingsSuggestions(user.getId());

                if (suggestions != null && !suggestions.getSuggestions().isEmpty()) {
                    // Create notification with top suggestion
                    SavingsSuggestionsResponse.SavingsSuggestion topSuggestion = suggestions.getSuggestions().get(0);

                    Notification notification = new Notification();
                    notification.setUserId(user.getId());
                    notification.setType("SAVINGS_SUGGESTION");
                    notification.setTitle("💡 Gợi ý Tiết kiệm Thông minh");
                    notification.setContent(String.format(
                            "%s\\n\\n✨ Tổng tiềm năng tiết kiệm: %,.0f VND/tháng",
                            topSuggestion.getMessage(),
                            suggestions.getTotalPotentialSavings()));
                    notification.setActionUrl("/dashboard/insights");
                    notification.setIsRead(false);
                    notification.setPriority(Notification.NotificationPriority.MEDIUM);

                    notificationRepository.save(notification);
                    suggestionCount++;
                }
            } catch (Exception e) {
                log.error("Error generating savings suggestions for user {}: {}", user.getId(), e.getMessage());
            }
        }

        log.info("Savings suggestions generated for {} users", suggestionCount);
    }

    /**
     * Generate proactive spending insights using AI (called by scheduler)
     */
    @Transactional
    public void generateProactiveInsights() {
        log.info("Generating AI-powered proactive insights for all users...");

        List<User> allUsers = userRepository.findAll();
        int insightCount = 0;

        for (User user : allUsers) {
            try {
                // Get AI insights
                List<SpendingInsight> insights = aiInsightsService.getProactiveInsights(user.getId());

                if (insights != null && !insights.isEmpty()) {
                    // Create notifications for high-impact insights
                    for (SpendingInsight insight : insights) {
                        // Only create notification for high-impact or warnings
                        if (insight.getImpactScore() >= 0.7 || "warning".equals(insight.getInsightType())) {

                            String title = switch (insight.getInsightType()) {
                                case "warning" -> "⚠️ Cảnh báo Chi tiêu";
                                case "achievement" -> "🎉 Thành tích Tiết kiệm";
                                case "tip" -> "💡 Mẹo Quản lý Chi tiêu";
                                default -> "📊 Phân tích Chi tiêu";
                            };

                            Notification notification = new Notification();
                            notification.setUserId(user.getId());
                            notification.setType("SPENDING_INSIGHT");
                            notification.setTitle(title);
                            notification.setContent(insight.getMessage());
                            notification.setActionUrl(
                                    insight.getCategory() != null
                                            ? "/transactions?category=" + insight.getCategory()
                                            : "/dashboard/analytics");
                            notification.setIsRead(false);
                            notification.setPriority(
                                    "warning".equals(insight.getInsightType())
                                            ? Notification.NotificationPriority.HIGH
                                            : Notification.NotificationPriority.MEDIUM);

                            notificationRepository.save(notification);
                            insightCount++;
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error generating proactive insights for user {}: {}", user.getId(), e.getMessage());
            }
        }

        log.info("Proactive insights generated: {} notifications created", insightCount);
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
}
