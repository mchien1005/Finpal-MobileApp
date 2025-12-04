package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for dashboard overview
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDTO {

    // Overall statistics
    private BigDecimal totalBalance; // Tổng số dư = Tổng thu nhập - Tổng chi tiêu
    private BigDecimal monthlyIncome; // thu nhập tháng này
    private BigDecimal monthlyExpense; // chi tiêu tháng này
    private BigDecimal netSavings; // income - expense

    // Comparisons with previous month
    private BigDecimal incomeChangePercent;
    private BigDecimal expenseChangePercent;

    // Counts
    private Integer monthlyTransactions;
    private Integer activeBudgets;
    private Integer activeSavingsGoals;

    // Top categories
    private List<SpendingByCategoryDTO> topExpenseCategories; // top 5 chi tiêu

    // Recent transactions count
    private Integer todayTransactions;
    private Integer weekTransactions;
}
