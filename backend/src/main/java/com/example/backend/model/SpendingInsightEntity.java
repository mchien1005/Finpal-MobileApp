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
@Table(name = "phan_tich_chi_tieu", uniqueConstraints = @UniqueConstraint(name = "unique_nguoi_dung_ky_han", columnNames = {
        "id_nguoi_dung", "loai_ky_han", "ngay_bat_dau" }))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpendingInsightEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_nguoi_dung", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai_ky_han", nullable = false)
    private PeriodType periodType;

    @Column(name = "ngay_bat_dau", nullable = false)
    private LocalDate startDate;

    @Column(name = "ngay_ket_thuc", nullable = false)
    private LocalDate endDate;

    @Column(name = "tong_thu_nhap", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalIncome = BigDecimal.ZERO;

    @Column(name = "tong_chi_tieu", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalExpense = BigDecimal.ZERO;

    @Column(name = "id_danh_muc_hang_dau")
    private Long topCategoryId;

    @Column(name = "so_tien_danh_muc_hang_dau", precision = 15, scale = 2)
    private BigDecimal topCategoryAmount;

    @Column(name = "du_lieu_phan_tich", columnDefinition = "JSON")
    private String analysisData;

    @Column(name = "thoi_gian_tao")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum PeriodType {
        DAILY, WEEKLY, MONTHLY
    }
}
