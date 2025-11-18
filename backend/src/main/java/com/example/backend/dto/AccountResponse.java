package com.example.backend.dto;

import com.example.backend.model.Account;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {

    private Long id;
    private Long userId;
    private String bankName;
    private String accountName;
    private String accountType;
    private BigDecimal balance;
    private String accountNumber; // Masked: **** 1234
    private String currency;
    private Boolean isActive;
    private String icon;
    private String color;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AccountResponse fromEntity(Account account) {
        AccountResponse response = new AccountResponse();
        response.setId(account.getId());
        response.setUserId(account.getUserId());
        response.setBankName(account.getBankName());
        response.setAccountName(account.getAccountName());
        response.setAccountType(account.getAccountType() != null ? account.getAccountType().name() : null);
        response.setBalance(account.getBalance());
        response.setAccountNumber(maskAccountNumber(account.getAccountNumber()));
        response.setCurrency(account.getCurrency());
        response.setIsActive(account.getIsActive());
        response.setIcon(account.getIcon());
        response.setColor(account.getColor());
        response.setNotes(account.getNotes());
        response.setCreatedAt(account.getCreatedAt());
        response.setUpdatedAt(account.getUpdatedAt());
        return response;
    }

    /**
     * Che số tài khoản, chỉ hiện 4 số cuối
     */
    private static String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() <= 4) {
            return accountNumber;
        }
        String last4 = accountNumber.substring(accountNumber.length() - 4);
        return "**** " + last4;
    }
}
