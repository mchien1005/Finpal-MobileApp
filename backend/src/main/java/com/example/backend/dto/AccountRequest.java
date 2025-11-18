package com.example.backend.dto;

import com.example.backend.model.Account;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountRequest {

    @NotBlank(message = "Bank name is required")
    private String bankName;

    @NotBlank(message = "Account name is required")
    private String accountName;

    @NotNull(message = "Account type is required")
    private String accountType; // BANK, CREDIT_CARD, CASH, E_WALLET

    @NotNull(message = "Initial balance is required")
    @PositiveOrZero(message = "Balance must be zero or positive")
    private BigDecimal balance;

    private String accountNumber; // Optional, encrypted

    private String currency; // Default: VND

    private String icon; // Emoji icon

    private String color; // Hex color code

    private String notes;
}
