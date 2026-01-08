package com.example.backend.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO cho biểu đồ khối lượng giao dịch (7 ngày gần nhất)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dữ liệu khối lượng giao dịch theo ngày (dùng cho biểu đồ cột)")
public class TransactionVolumeDTO {

    @Schema(description = "Danh sách dữ liệu theo từng ngày")
    private List<DailyTransactionData> data;

    @Schema(description = "Tổng số giao dịch trong khoảng thời gian", example = "58745")
    private Long totalTransactions;

    @Schema(description = "Tổng giá trị giao dịch (VND)", example = "1250000000")
    private BigDecimal totalValue;

    @Schema(description = "Trung bình số giao dịch mỗi ngày", example = "8392.14")
    private Double avgDailyTransactions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Dữ liệu giao dịch theo ngày")
    public static class DailyTransactionData {

        @Schema(description = "Ký hiệu ngày trong tuần", example = "T2", allowableValues = { "T2", "T3", "T4", "T5",
                "T6", "T7", "CN" })
        private String day;

        @Schema(description = "Ngày đầy đủ (dd/MM/yyyy)", example = "05/01/2026")
        private String date;

        @Schema(description = "Số lượng giao dịch trong ngày", example = "1850")
        private Long transactionCount;

        @Schema(description = "Tổng giá trị giao dịch (triệu VND)", example = "125.50")
        private BigDecimal totalValue;
    }
}
