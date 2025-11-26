package com.example.backend.dto;

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
public class BudgetRequest {

    private Long categoryId; // null = tổng ngân sách

    @NotBlank(message = "Tên ngân sách không được để trống")
    private String name;

    @NotNull(message = "Số tiền không được để trống")
    @Positive(message = "Số tiền phải lớn hơn 0")
    private BigDecimal amount;

    @NotNull(message = "Kỳ ngân sách không được để trống")
    private String period; // WEEKLY, MONTHLY, QUARTERLY, YEARLY

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDate endDate;

    @Min(value = 1, message = "Ngưỡng cảnh báo phải từ 1-100")
    @Max(value = 100, message = "Ngưỡng cảnh báo phải từ 1-100")
    @Builder.Default
    private Integer alertThreshold = 70;

}
