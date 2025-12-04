package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for income vs expense comparison
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncomeExpenseComparisonDTO {

    private String period; // "This Month", "Last Month", "This Year"...
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netSavings;
    private Double savingsRate; // % of income saved

    private Long incomeTransactionCount;
    private Long expenseTransactionCount;

    private BigDecimal averageIncome; // per transaction
    private BigDecimal averageExpense; // per transaction
}
