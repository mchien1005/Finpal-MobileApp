package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for budget progress tracking
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetProgressDTO {

    private Long budgetId;
    private Long categoryId;
    private String categoryName;
    private String categoryIcon;
    private String categoryColor;

    private BigDecimal budgetAmount; // ngân sách đề ra
    private BigDecimal spentAmount; // đã chi tiêu
    private BigDecimal remainingAmount; // còn lại
    private Double usagePercent; // % đã dùng

    private String period; // DAILY, WEEKLY, MONTHLY, YEARLY
    private LocalDate startDate;
    private LocalDate endDate;

    private String status; // OK, WARNING, EXCEEDED
    private Integer daysRemaining; // số ngày còn lại trong kỳ
}
