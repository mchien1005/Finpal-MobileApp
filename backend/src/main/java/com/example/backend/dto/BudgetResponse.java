package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO cho Budget
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetResponse {

    private Long id;
    private Long userId;
    private Long categoryId;
    private String categoryName;
    private String categoryIcon;
    private String categoryColor;
    private String name;
    private BigDecimal amount;
    private String period; // WEEKLY, MONTHLY, QUARTERLY, YEARLY
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isActive;
    private Integer alertThreshold;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Progress information
    private BigDecimal spentAmount;
    private BigDecimal remainingAmount;
    private Double usagePercentage;
    private String status; // OK, WARNING, EXCEEDED
    private Integer daysRemaining;
}
