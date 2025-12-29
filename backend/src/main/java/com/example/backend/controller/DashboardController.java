package com.example.backend.controller;

import com.example.backend.dto.*;
import com.example.backend.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "📊 Dashboard", description = "API bảng điều khiển trực quan. Xem tổng quan tài chính, biểu đồ thu chi.")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(
        summary = "Tổng quan dòng tiền",
        description = """
            Lấy tổng quan dòng tiền trong tháng:
            - Tổng thu nhập
            - Tổng chi tiêu
            - Số tiền còn lại (net savings)
            - Tỷ lệ tiết kiệm (%)
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công"),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @GetMapping("/cash-flow")
    public ResponseEntity<CashFlowDTO> getCashFlow(
            @Parameter(description = "Tháng cần xem (yyyy-MM-dd), mặc định tháng hiện tại")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month,
            Authentication authentication) {
        String username = authentication.getName();
        CashFlowDTO cashFlow = dashboardService.getCashFlow(username, month);
        return ResponseEntity.ok(cashFlow);
    }

    @Operation(
        summary = "Chi tiêu theo danh mục",
        description = """
            Lấy dữ liệu chi tiêu phân loại theo danh mục (dùng cho biểu đồ tròn).
            
            **Response bao gồm:**
            - Tên danh mục, icon, màu sắc
            - Tổng số tiền chi tiêu
            - Phần trăm so với tổng chi
            - Số lượng giao dịch
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công"),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @GetMapping("/spending-by-category")
    public ResponseEntity<List<SpendingByCategoryDTO>> getSpendingByCategory(
            @Parameter(description = "Từ ngày (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Đến ngày (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {
        String username = authentication.getName();
        List<SpendingByCategoryDTO> spending = dashboardService.getSpendingByCategory(username, startDate, endDate);
        return ResponseEntity.ok(spending);
    }

    @Operation(
        summary = "Tổng quan dashboard",
        description = "Lấy tất cả dữ liệu dashboard trong một request (cash flow + top categories + recent transactions)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công"),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDTO> getDashboardSummary(Authentication authentication) {
        String username = authentication.getName();
        DashboardSummaryDTO summary = dashboardService.getDashboardSummary(username);
        return ResponseEntity.ok(summary);
    }

    @Operation(
        summary = "Xu hướng thu chi theo tháng",
        description = "Lấy dữ liệu xu hướng thu chi N tháng gần nhất (dùng cho biểu đồ đường)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công"),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @GetMapping("/monthly-trend")
    public ResponseEntity<List<MonthlyTrendDTO>> getMonthlyTrend(
            @Parameter(description = "Số tháng cần lấy (mặc định 6)")
            @RequestParam(required = false, defaultValue = "6") Integer months,
            Authentication authentication) {
        String username = authentication.getName();
        List<MonthlyTrendDTO> trends = dashboardService.getMonthlyTrend(username, months);
        return ResponseEntity.ok(trends);
    }

    @Operation(
        summary = "Ngân sách theo danh mục",
        description = "Lấy danh sách ngân sách đang hoạt động theo danh mục, bao gồm tiến độ và thời gian."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công"),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @GetMapping("/category-budgets")
    public ResponseEntity<List<CategoryBudgetDTO>> getCategoryBudgets(Authentication authentication) {
        String username = authentication.getName();
        List<CategoryBudgetDTO> budgets = dashboardService.getCategoryBudgets(username);
        return ResponseEntity.ok(budgets);
    }
}
