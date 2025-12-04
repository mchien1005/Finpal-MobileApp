package com.example.backend.controller;

import com.example.backend.dto.IncomeExpenseComparisonDTO;
import com.example.backend.dto.SpendingInsightDTO;
import com.example.backend.model.Transaction;
import com.example.backend.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * GET /api/statistics/income-expense-comparison
     * So sánh thu chi trong một khoảng thời gian
     * 
     * @param startDate ngày bắt đầu (yyyy-MM-dd)
     * @param endDate   ngày kết thúc (yyyy-MM-dd)
     * @param period    tên kỳ (optional): "This Month", "Last Month", "This
     *                  Year"...
     */
    @GetMapping("/income-expense-comparison")
    public ResponseEntity<IncomeExpenseComparisonDTO> getIncomeExpenseComparison(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String period,
            Authentication authentication) {

        String username = authentication.getName();
        IncomeExpenseComparisonDTO comparison = statisticsService.getIncomeExpenseComparison(
                username, startDate, endDate, period);
        return ResponseEntity.ok(comparison);
    }

    /**
     * GET /api/statistics/spending-insights
     * Phân tích chi tiêu: merchants thường xuyên, categories cao, giao dịch bất
     * thường
     */
    @GetMapping("/spending-insights")
    public ResponseEntity<List<SpendingInsightDTO>> getSpendingInsights(Authentication authentication) {
        String username = authentication.getName();
        List<SpendingInsightDTO> insights = statisticsService.getSpendingInsights(username);
        return ResponseEntity.ok(insights);
    }

    /**
     * GET /api/statistics/anomalies
     * Giao dịch bất thường (isAnomaly = true)
     * 
     * @param startDate ngày bắt đầu (yyyy-MM-dd)
     * @param endDate   ngày kết thúc (yyyy-MM-dd)
     */
    @GetMapping("/anomalies")
    public ResponseEntity<List<Transaction>> getAnomalyTransactions(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {

        String username = authentication.getName();
        List<Transaction> anomalies = statisticsService.getAnomalyTransactions(username, startDate, endDate);
        return ResponseEntity.ok(anomalies);
    }
}
