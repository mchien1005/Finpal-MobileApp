package com.example.backend.scheduler;

import com.example.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler for automatic notification generation
 */
@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(value = "scheduler.notifications.enabled", havingValue = "true", matchIfMissing = true)
public class NotificationScheduler {

    private final NotificationService notificationService;

    /**
     * Generate and send daily budget alerts
     * Runs every day at 8:00 AM
     */
    @Scheduled(cron = "${scheduler.notifications.budget-alerts.cron:0 0 8 * * *}")
    public void generateDailyBudgetAlerts() {
        log.info("Starting daily budget alerts generation...");

        try {
            notificationService.generateBudgetAlerts();
            log.info("Daily budget alerts generation completed");
        } catch (Exception e) {
            log.error("Error generating daily budget alerts", e);
        }
    }

    /**
     * Generate and send weekly savings suggestions
     * Runs every Sunday at 9:00 AM
     */
    @Scheduled(cron = "${scheduler.notifications.savings-suggestions.cron:0 0 9 * * SUN}")
    public void generateWeeklySavingsSuggestions() {
        log.info("Starting weekly savings suggestions generation...");

        try {
            notificationService.generateSavingsSuggestions();
            log.info("Weekly savings suggestions generation completed");
        } catch (Exception e) {
            log.error("Error generating weekly savings suggestions", e);
        }
    }

    /**
     * Generate proactive spending insights
     * Runs every day at 7:00 PM
     */
    @Scheduled(cron = "${scheduler.notifications.spending-insights.cron:0 0 19 * * *}")
    public void generateProactiveInsights() {
        log.info("Starting proactive spending insights generation...");

        try {
            notificationService.generateProactiveInsights();
            log.info("Proactive spending insights generation completed");
        } catch (Exception e) {
            log.error("Error generating proactive insights", e);
        }
    }

    /**
     * Cleanup old read notifications
     * Runs every day at 2:00 AM
     */
    @Scheduled(cron = "${scheduler.notifications.cleanup.cron:0 0 2 * * *}")
    public void cleanupOldNotifications() {
        log.info("Starting old notifications cleanup...");

        try {
            notificationService.cleanupOldNotifications();
            log.info("Old notifications cleanup completed");
        } catch (Exception e) {
            log.error("Error cleaning up old notifications", e);
        }
    }
}
