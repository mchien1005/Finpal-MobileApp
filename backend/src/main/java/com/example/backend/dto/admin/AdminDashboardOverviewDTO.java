package com.example.backend.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO cho tổng quan Admin Dashboard
 * Chứa các thống kê chính hiển thị trên dashboard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin tổng quan Admin Dashboard")
public class AdminDashboardOverviewDTO {

    // === THỐNG KÊ NGƯỜI DÙNG ===
    @Schema(description = "Tổng số người dùng trong hệ thống", example = "12543")
    private Long totalUsers;

    @Schema(description = "Số người dùng hoạt động hôm nay (có đăng nhập hoặc giao dịch)", example = "1234")
    private Long activeUsersToday;

    @Schema(description = "Phần trăm tăng trưởng người dùng so với tháng trước", example = "12.5")
    private Double userGrowthPercent;

    // === THỐNG KÊ GIAO DỊCH HÔM NAY ===
    @Schema(description = "Số giao dịch trong ngày hôm nay", example = "8392")
    private Long transactionsToday;

    @Schema(description = "Tổng giá trị giao dịch hôm nay (VND)", example = "245600000")
    private BigDecimal totalValueToday;

    @Schema(description = "Phần trăm tăng trưởng giao dịch so với hôm qua", example = "8.2")
    private Double transactionGrowthPercent;

    // === AI METRICS (từ BackendAI) ===
    @Schema(description = "Độ chính xác AI categorization (%)", example = "94.2")
    private Double aiAccuracy;

    @Schema(description = "Thay đổi độ chính xác AI so với tuần trước (%)", example = "2.1")
    private Double aiAccuracyChange;

    // === SMS PARSING ===
    @Schema(description = "Tỷ lệ parse SMS thành công (%)", example = "98.7")
    private Double smsParsingRate;

    @Schema(description = "Số SMS parse thất bại hôm nay", example = "128")
    private Long smsFailedToday;
}
