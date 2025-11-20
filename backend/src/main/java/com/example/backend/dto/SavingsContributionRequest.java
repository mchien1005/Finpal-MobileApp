package com.example.backend.dto;

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
public class SavingsContributionRequest {

    @NotNull(message = "Số tiền đóng góp không được để trống")
    @Positive(message = "Số tiền đóng góp phải lớn hơn 0")
    private BigDecimal amount;

    private LocalDate contributionDate; // Default = today if null

    private String notes;
}
