package com.example.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tai_khoan")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_nguoi_dung", nullable = false)
    private Long userId;

    @Column(name = "ten_ngan_hang", nullable = false, length = 100)
    private String bankName;

    @Column(name = "ten_tai_khoan", length = 100)
    private String accountName;

    @Column(name = "so_tai_khoan_ma_hoa", length = 500)
    private String accountNumberEncrypted;

    @Column(name = "so_tai_khoan", length = 50)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai_tai_khoan")
    @Builder.Default
    private AccountType accountType = AccountType.BANK;

    @Column(name = "so_du", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "don_vi_tien_te", length = 3)
    @Builder.Default
    private String currency = "VND";

    @Column(name = "dang_hoat_dong")
    @Builder.Default
    private Boolean isActive = true;

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

    public enum AccountType {
        BANK, CASH, CREDIT_CARD, E_WALLET
    }
}
