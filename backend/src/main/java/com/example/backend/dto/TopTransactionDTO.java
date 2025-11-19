package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for top/largest transactions
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopTransactionDTO {

    private Long transactionId;
    private String type; // INCOME, EXPENSE
    private BigDecimal amount;
    private String merchant;
    private String description;
    private String categoryName;
    private String categoryIcon;
    private LocalDateTime transactionDate;
    private String accountName;
}
