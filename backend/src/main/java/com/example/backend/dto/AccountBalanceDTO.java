package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for account balance summary
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountBalanceDTO {

    private Long accountId;
    private String bankName;
    private String accountName;
    private String accountType; // BANK, CASH, CREDIT_CARD, E_WALLET
    private BigDecimal balance;
    private String currency;
    private String icon;
    private String color;
    private Integer transactionCount; // số giao dịch trong tháng
}
