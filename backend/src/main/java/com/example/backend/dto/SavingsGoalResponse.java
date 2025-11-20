package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO cho Savings Goal
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavingsGoalResponse {

    private Long id;
    private Long userId;
    private String name;
    private String description;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private LocalDate deadline;
    private String icon;
    private String color;
    private String status; // ACTIVE, COMPLETED, CANCELLED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;

    // Progress information
    private Double progressPercentage;
    private BigDecimal remainingAmount;
    private Integer daysRemaining;
    private String progressStatus; // ON_TRACK, AT_RISK, OVERDUE, COMPLETED

    // Recent contributions
    private List<SavingsContributionResponse> recentContributions;
}
