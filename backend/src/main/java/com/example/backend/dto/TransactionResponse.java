package com.example.backend.dto;

import com.example.backend.model.Transaction;
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
public class TransactionResponse {
    private Long id;
    private Long userId;
    private Long accountId;
    private String accountName;
    private Long categoryId;
    private String categoryName;
    private String categoryIcon;
    private BigDecimal amount;
    private String type; // INCOME or EXPENSE
    private String merchant;
    private String description;
    private LocalDateTime transactionDate;
    private Boolean isAuto;
    private Boolean isVerified;
    private Boolean isAnomaly;
    private String notes;
    private String tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TransactionResponse fromEntity(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .userId(transaction.getUser().getId())
                .accountId(transaction.getAccount().getId())
                .accountName(transaction.getAccount().getAccountName())
                .categoryId(transaction.getCategory() != null ? transaction.getCategory().getId() : null)
                .categoryName(transaction.getCategory() != null ? transaction.getCategory().getName() : null)
                .amount(transaction.getAmount())
                .type(transaction.getType().name())
                .merchant(transaction.getMerchant())
                .description(transaction.getDescription())
                .transactionDate(transaction.getTransactionDate())
                .isAuto(transaction.getIsAuto())
                .isVerified(transaction.getIsVerified())
                .isAnomaly(transaction.getIsAnomaly())
                .notes(transaction.getNotes())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }
}
