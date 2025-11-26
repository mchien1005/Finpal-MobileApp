package com.example.backend.dto;

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
public class SavingsGoalRequest {

    @NotBlank(message = "Tên mục tiêu không được để trống")
    private String name;

    private String description;

    @NotNull(message = "Số tiền mục tiêu không được để trống")
    @Positive(message = "Số tiền mục tiêu phải lớn hơn 0")
    private BigDecimal targetAmount;

    private LocalDate deadline;
}
