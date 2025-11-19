package com.example.backend.controller;

import com.example.backend.dto.*;
import com.example.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Dashboard Controller - FR2: Module "Bảng điều khiển Trực quan"
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * FR2.1: GET /api/dashboard/cash-flow
     * Tổng quan Dòng tiền: Tổng Thu nhập (Tháng) - Tổng Chi tiêu (Tháng) = Còn lại
     * 
     * Response: {
     * "monthlyIncome": 15000000,
     * "monthlyExpense": 8000000,
     * "netSavings": 7000000,
     * "savingsRate": 46.67,
     * "currentMonth": "2025-11",
     * "totalBalance": 25000000
     * }
     */
    @GetMapping("/cash-flow")
    public ResponseEntity<CashFlowDTO> getCashFlow(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month,
            Authentication authentication) {
        String username = authentication.getName();
        CashFlowDTO cashFlow = dashboardService.getCashFlow(username, month);
        return ResponseEntity.ok(cashFlow);
    }

    /**
     * FR2.2: GET /api/dashboard/spending-by-category
     * Biểu đồ Phân loại: Data cho pie chart chi tiêu theo category
     * 
     * Response: [
     * {
     * "categoryId": 1,
     * "categoryName": "Ăn uống",
     * "icon": "🍔",
     * "color": "#FF5722",
     * "totalAmount": 3200000,
     * "percentage": 40.0,
     * "transactionCount": 25
     * },
     * ...
     * ]
     */
    @GetMapping("/spending-by-category")
    public ResponseEntity<List<SpendingByCategoryDTO>> getSpendingByCategory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {
        String username = authentication.getName();
        List<SpendingByCategoryDTO> spending = dashboardService.getSpendingByCategory(username, startDate, endDate);
        return ResponseEntity.ok(spending);
    }

    /**
     * GET /api/dashboard/summary
     * Tổng quan tổng hợp dashboard (bao gồm cash flow + top categories)
     */
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDTO> getDashboardSummary(Authentication authentication) {
        String username = authentication.getName();
        DashboardSummaryDTO summary = dashboardService.getDashboardSummary(username);
        return ResponseEntity.ok(summary);
    }

    /**
     * GET /api/dashboard/monthly-trend
     * Xu hướng thu chi theo tháng (cho biểu đồ line chart)
     */
    @GetMapping("/monthly-trend")
    public ResponseEntity<List<MonthlyTrendDTO>> getMonthlyTrend(
            @RequestParam(required = false, defaultValue = "6") Integer months,
            Authentication authentication) {
        String username = authentication.getName();
        List<MonthlyTrendDTO> trends = dashboardService.getMonthlyTrend(username, months);
        return ResponseEntity.ok(trends);
    }
}
