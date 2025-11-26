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
        @Index(name = "idx_tai_khoan", columnList = "id_tai_khoan"),
        @Index(name = "idx_danh_muc", columnList = "id_danh_muc")
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tai_khoan", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_danh_muc")
    private Category category;

    @Column(name = "so_tien", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai", nullable = false)
    private TransactionType type;

    @Column(name = "don_vi_chap_nhan", length = 255)
    private String merchant;

    @Column(name = "mo_ta", columnDefinition = "TEXT")
    private String description;

    @Column(name = "ngay_giao_dich", nullable = false)
    private LocalDateTime transactionDate;

    // SMS related fields
    @Column(name = "tu_dong")
    private Boolean isAuto = false;

    @Column(name = "noi_dung_sms_ma_hoa", columnDefinition = "TEXT")
    private String smsContentEncrypted;

    @Column(name = "ma_ngan_hang_sms", length = 20)
    private String smsBankCode;

    // Status fields
    @Column(name = "da_xac_nhan")
    private Boolean isVerified = false;

    @Column(name = "bat_thuong")
    private Boolean isAnomaly = false;

    // Metadata
    @Column(name = "ghi_chu", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "anh_hoa_don", length = 500)
    private String receiptImage;

    @Column(name = "ngay_tao", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "ngay_cap_nhat")
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
