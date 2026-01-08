package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO cho FR2.1: Tổng quan Dòng tiền
 * Hiển thị rõ ràng: Tổng Thu nhập (Tháng) - Tổng Chi tiêu (Tháng) = Còn lại
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashFlowDTO {

    /**
     * Tổng thu nhập tháng
     */
    private BigDecimal monthlyIncome;

    /**
     * Tổng chi tiêu tháng
     */
    private BigDecimal monthlyExpense;

    /**
     * Còn lại = Thu nhập - Chi tiêu
     */
    private BigDecimal netSavings;

    /**
     * Tỷ lệ tiết kiệm (%) = (netSavings / monthlyIncome) * 100
     */
    private BigDecimal savingsRate;

    /**
     * Tháng hiện tại (yyyy-MM)
     */
    private String currentMonth;

    /**
     * Tổng số dư tất cả tài khoản
     */
    private BigDecimal totalBalance;

    /**
     * So sánh với tháng trước
     */
    private BigDecimal incomeChange;
    private BigDecimal expenseChange;

    /**
     * Số lượng giao dịch trong tháng
     */
    private Integer transactionCount;
}
