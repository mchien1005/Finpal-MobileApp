package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParsedSMSData {

    private BigDecimal amount;

    private String type; // INCOME or EXPENSE

    private String merchant;

    private LocalDateTime transactionDate;

    private String bankCode;

    private String accountNumber;
}
