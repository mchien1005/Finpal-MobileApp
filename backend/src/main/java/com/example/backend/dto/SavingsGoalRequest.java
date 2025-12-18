package com.example.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO cho tạo/cập nhật mục tiêu tiết kiệm
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Tạo mục tiêu tiết kiệm")
public class SavingsGoalRequest {

    @NotBlank(message = "Tên mục tiêu không được để trống")
    @Schema(description = "Tên mục tiêu", example = "Mua iPhone 16", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotNull(message = "Số tiền mục tiêu không được để trống")
    @Positive(message = "Số tiền mục tiêu phải lớn hơn 0")
    @Schema(description = "Số tiền mục tiêu", example = "35000000", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal targetAmount;

    @Schema(description = "Hạn chót hoàn thành (yyyy-MM-dd)", example = "2026-06-01")
    private LocalDate deadline;
}
