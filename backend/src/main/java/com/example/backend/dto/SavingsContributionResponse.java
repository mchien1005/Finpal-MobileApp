package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO cho Savings Contribution
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavingsContributionResponse {

    private Long id;
    private Long savingsGoalId;
    private BigDecimal amount;
    private LocalDate contributionDate;
    private String notes;
    private LocalDateTime createdAt;
}
