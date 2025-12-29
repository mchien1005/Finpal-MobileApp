package com.example.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "giao_dich_dinh_ky")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurringTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_nguoi_dung", nullable = false)
    private Long userId;

    @Column(name = "id_danh_muc", nullable = false)
    private Long categoryId;

    @Column(name = "so_tien", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "tan_suat", nullable = false)
    @Builder.Default
    private Frequency frequency = Frequency.MONTHLY;

    @Column(name = "ngay_bat_dau", nullable = false)
    private LocalDate startDate;

    @Column(name = "ngay_ket_thuc")
    private LocalDate endDate;

    @Column(name = "lan_tiep_theo", nullable = false)
    private LocalDate nextOccurrence;

    @Column(name = "mo_ta", length = 255)
    private String description;

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

    public enum Frequency {
        DAILY, WEEKLY, MONTHLY, YEARLY
    }
}
