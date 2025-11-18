package com.example.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_user_date", columnList = "user_id,transaction_date"),
        @Index(name = "idx_account", columnList = "account_id"),
        @Index(name = "idx_category", columnList = "category_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(length = 255)
    private String merchant;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    // SMS related fields
    @Column(name = "is_auto")
    private Boolean isAuto = false;

    @Column(name = "sms_content_encrypted", columnDefinition = "TEXT")
    private String smsContentEncrypted;

    @Column(name = "sms_bank_code", length = 20)
    private String smsBankCode;

    // Status fields
    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Column(name = "is_anomaly")
    private Boolean isAnomaly = false;

    // Metadata
    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(length = 500)
    private String tags;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum TransactionType {
        INCOME, EXPENSE
    }
}
