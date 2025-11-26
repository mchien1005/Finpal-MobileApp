package com.example.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO cho đóng góp vào mục tiêu tiết kiệm
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Đóng góp vào mục tiêu tiết kiệm")
public class SavingsContributionRequest {

    @NotNull(message = "Số tiền đóng góp không được để trống")
    @Positive(message = "Số tiền đóng góp phải lớn hơn 0")
    @Schema(description = "Số tiền đóng góp", example = "500000", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal amount;

    @Schema(description = "Ngày đóng góp (mặc định = hôm nay nếu không truyền)", example = "2025-11-20")
    private LocalDate contributionDate;

    @Schema(description = "Ghi chú", example = "Tiền thưởng tháng 11")
    private String notes;
}
