package com.example.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO cho tạo/cập nhật ngân sách
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Tạo/cập nhật ngân sách")
public class BudgetRequest {

    @Schema(description = "ID danh mục (null = tổng ngân sách)", example = "5")
    private Long categoryId;

    @NotBlank(message = "Tên ngân sách không được để trống")
    @Schema(description = "Tên ngân sách", example = "Ngân sách ăn uống tháng 12", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotNull(message = "Số tiền không được để trống")
    @Positive(message = "Số tiền phải lớn hơn 0")
    @Schema(description = "Số tiền ngân sách", example = "3000000", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal amount;

    @NotNull(message = "Kỳ ngân sách không được để trống")
    @Schema(description = "Kỳ ngân sách: WEEKLY, MONTHLY, QUARTERLY, YEARLY", example = "MONTHLY", requiredMode = Schema.RequiredMode.REQUIRED)
    private String period;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    @Schema(description = "Ngày bắt đầu (yyyy-MM-dd)", example = "2025-12-01", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    @Schema(description = "Ngày kết thúc (yyyy-MM-dd)", example = "2025-12-31", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate endDate;

    @Min(value = 1, message = "Ngưỡng cảnh báo phải từ 1-100")
    @Max(value = 100, message = "Ngưỡng cảnh báo phải từ 1-100")
    @Builder.Default
    @Schema(description = "Ngưỡng cảnh báo (%) - cảnh báo khi chi tiêu vượt ngưỡng", example = "70", defaultValue = "70")
    private Integer alertThreshold = 70;

}
