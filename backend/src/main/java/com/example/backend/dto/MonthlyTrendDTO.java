package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for monthly income/expense trends
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyTrendDTO {

    private Integer year;
    private Integer month;
    private String monthName; // "January", "February"...
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netSavings; // income - expense
    private Long incomeCount;
    private Long expenseCount;

    public MonthlyTrendDTO(Integer year, Integer month, BigDecimal totalIncome,
            BigDecimal totalExpense, Long incomeCount, Long expenseCount) {
        this.year = year;
        this.month = month;
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.incomeCount = incomeCount;
        this.expenseCount = expenseCount;

        // Calculate net savings
        if (totalIncome != null && totalExpense != null) {
            this.netSavings = totalIncome.subtract(totalExpense);
        }
    }
}
