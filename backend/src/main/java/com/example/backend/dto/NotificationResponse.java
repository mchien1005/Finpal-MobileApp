package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO cho Notification
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private Long userId;
    private String type; // BUDGET_ALERT, SAVING_TIP, ANOMALY, REPORT, GOAL_REMINDER
    private String title;
    private String content;
    private String actionUrl;
    private Boolean isRead;
    private LocalDateTime readAt;
    private String priority; // LOW, MEDIUM, HIGH, URGENT
    private LocalDateTime createdAt;
}
