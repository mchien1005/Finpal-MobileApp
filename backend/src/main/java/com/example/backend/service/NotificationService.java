package com.example.backend.service;

import com.example.backend.dto.NotificationResponse;
import com.example.backend.model.Budget;
import com.example.backend.model.Notification;
import com.example.backend.model.SavingsGoal;
import com.example.backend.model.User;
import com.example.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for Notification System (FR3.1)
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final BudgetRepository budgetRepository;
    private final SavingsGoalRepository savingsGoalRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Lấy tất cả notifications của user
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
     * Đếm số notifications chưa đọc
     */
    @Transactional(readOnly = true)
    public Long countUnreadNotifications(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return notificationRepository.countByUserIdAndIsRead(user.getId(), false);
    }

    /**
     * Đánh dấu notification là đã đọc
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
     * Đánh dấu tất cả notifications là đã đọc
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
     * Xóa notification
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
     * Xóa tất cả notifications đã đọc
     */
    @Transactional
    public void deleteAllRead(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Notification> readNotifications = notificationRepository.findByUserIdAndIsRead(user.getId(), true);

        notificationRepository.deleteAll(readNotifications);
    }

    /**
     * Tạo budget alert notification
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
     * Tạo savings goal reminder notification
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
     * Kiểm tra và tạo budget alerts tự động
     */
    @Transactional
    public void checkBudgetAlertsForUser(Long userId) {
        LocalDate today = LocalDate.now();
        List<Budget> activeBudgets = budgetRepository.findActiveBudgetsForDate(userId, today);

        for (Budget budget : activeBudgets) {
            // Calculate spent amount
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

                // Check if alert should be sent
                if (usagePercentage >= 100) {
                    // Check if alert already exists for this budget today
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
}
