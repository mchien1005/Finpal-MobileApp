package com.example.backend.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO cho biểu đồ phân bổ danh mục chi tiêu (toàn hệ thống)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dữ liệu phân bổ chi tiêu theo danh mục (dùng cho biểu đồ tròn)")
public class CategoryDistributionDTO {

    @Schema(description = "Danh sách phân bổ theo danh mục")
    private List<CategoryData> categories;

    @Schema(description = "Tổng chi tiêu toàn hệ thống (VND)", example = "8500000000")
    private BigDecimal totalSpending;

    @Schema(description = "Tổng số giao dịch chi tiêu", example = "45678")
    private Long totalTransactions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Dữ liệu chi tiêu theo danh mục")
    public static class CategoryData {

        @Schema(description = "ID của danh mục", example = "1")
        private Long categoryId;

        @Schema(description = "Tên danh mục", example = "Ăn uống")
        private String categoryName;

        @Schema(description = "Icon của danh mục (MDI format)", example = "<mdi:food>")
        private String icon;

        @Schema(description = "Mã màu HEX của danh mục", example = "#FF5733")
        private String color;

        @Schema(description = "Tổng số tiền chi tiêu (VND)", example = "2975000000")
        private BigDecimal amount;

        @Schema(description = "Phần trăm so với tổng chi tiêu", example = "35.0")
        private Double percentage;

        @Schema(description = "Số lượng giao dịch trong danh mục", example = "15987")
        private Long transactionCount;
    }
}
