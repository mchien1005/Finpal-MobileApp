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
    private String transactionSource; // VCB, TCB, CASH, MOMO...
    private Long categoryId;
    private String categoryName;
    private String categoryIcon;
    private BigDecimal amount;
    private String type; // INCOME or EXPENSE
    private String description;
    private LocalDateTime transactionDate;
    private Boolean isAuto;
    private Boolean isVerified;
    private Boolean isAnomaly;
    private String notes;
    private String tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Nguồn phân loại category: AI, RULE_BASED, USER, hoặc null
    private String categorizationSource;
    // Độ tin cậy của AI (nếu dùng AI)
    private Double aiConfidence;

    /**
     * Chuyển đổi từ Entity sang DTO (KHÔNG giải mã)
     * Dùng cho các trường hợp không cần giải mã hoặc dữ liệu không mã hóa
     */
    public static TransactionResponse fromEntity(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .userId(transaction.getUser().getId())
                .transactionSource(transaction.getTransactionSource())
                .categoryId(transaction.getCategory() != null ? transaction.getCategory().getId() : null)
                .categoryName(transaction.getCategory() != null ? transaction.getCategory().getName() : null)
                .categoryIcon(transaction.getCategory() != null ? transaction.getCategory().getIcon() : null)
                .amount(transaction.getAmount())
                .type(transaction.getType().name())
                .description(transaction.getDescription())
                .transactionDate(transaction.getTransactionDate())
                .isAuto(transaction.getIsAuto())
                .isVerified(transaction.getIsVerified())
                .isAnomaly(transaction.getIsAnomaly())
                .notes(transaction.getNotes())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .categorizationSource(transaction.getCategorizationSource())
                .aiConfidence(transaction.getAiConfidence())
                .build();
    }

    /**
     * Chuyển đổi từ Entity sang DTO với GIẢI MÃ description và notes
     * Dùng khi cần hiển thị nội dung đã giải mã cho user
     * 
     * @param transaction Entity giao dịch
     * @param decryptFunc Function giải mã (EncryptionUtil::decrypt)
     */
    public static TransactionResponse fromEntityDecrypted(Transaction transaction,
            java.util.function.Function<String, String> decryptFunc) {
        String decryptedDescription = null;
        String decryptedNotes = null;

        try {
            if (transaction.getDescription() != null && !transaction.getDescription().isEmpty()) {
                decryptedDescription = decryptFunc.apply(transaction.getDescription());
            }
        } catch (Exception e) {
            // Nếu giải mã thất bại, có thể dữ liệu chưa được mã hóa (dữ liệu cũ)
            decryptedDescription = transaction.getDescription();
        }

        try {
            if (transaction.getNotes() != null && !transaction.getNotes().isEmpty()) {
                decryptedNotes = decryptFunc.apply(transaction.getNotes());
            }
        } catch (Exception e) {
            // Nếu giải mã thất bại, có thể dữ liệu chưa được mã hóa (dữ liệu cũ)
            decryptedNotes = transaction.getNotes();
        }

        return TransactionResponse.builder()
                .id(transaction.getId())
                .userId(transaction.getUser().getId())
                .transactionSource(transaction.getTransactionSource())
                .categoryId(transaction.getCategory() != null ? transaction.getCategory().getId() : null)
                .categoryName(transaction.getCategory() != null ? transaction.getCategory().getName() : null)
                .categoryIcon(transaction.getCategory() != null ? transaction.getCategory().getIcon() : null)
                .amount(transaction.getAmount())
                .type(transaction.getType().name())
                .description(decryptedDescription)
                .transactionDate(transaction.getTransactionDate())
                .isAuto(transaction.getIsAuto())
                .isVerified(transaction.getIsVerified())
                .isAnomaly(transaction.getIsAnomaly())
                .notes(decryptedNotes)
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .categorizationSource(transaction.getCategorizationSource())
                .aiConfidence(transaction.getAiConfidence())
                .build();
    }
}
