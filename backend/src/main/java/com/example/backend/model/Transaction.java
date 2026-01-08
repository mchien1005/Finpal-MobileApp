package com.example.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "giao_dich", indexes = {
        @Index(name = "idx_nguoi_dung_ngay", columnList = "id_nguoi_dung,ngay_giao_dich"),
        @Index(name = "idx_danh_muc", columnList = "id_danh_muc"),
        @Index(name = "idx_nguon_giao_dich", columnList = "nguon_giao_dich")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nguoi_dung", nullable = false)
    private User user;

    @Column(name = "nguon_giao_dich", length = 50)
    private String transactionSource; // VCB, TCB, CASH, MOMO...

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_danh_muc")
    private Category category;

    @Column(name = "so_tien", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai", nullable = false)
    private TransactionType type;

    @Column(name = "mo_ta", columnDefinition = "TEXT")
    private String description;

    @Column(name = "ngay_giao_dich", nullable = false)
    private LocalDateTime transactionDate;

    // SMS related fields
    @Column(name = "tu_dong")
    private Boolean isAuto = false;

    @Column(name = "noi_dung_sms_ma_hoa", columnDefinition = "TEXT")
    private String smsContentEncrypted; // SHA-256 hash của SMS content để check duplicate

    // Status fields
    @Column(name = "da_xac_nhan")
    private Boolean isVerified = false;

    @Column(name = "bat_thuong")
    private Boolean isAnomaly = false;

    // Metadata
    @Column(name = "ghi_chu", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "ngay_tao", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime updatedAt;

    // AI Categorization fields
    @Column(name = "nguon_phan_loai", length = 20)
    private String categorizationSource; // AI, RULE_BASED, USER

    @Column(name = "do_tin_cay_ai")
    private Double aiConfidence; // 0.0 - 1.0

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
