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
@Table(name = "dong_gop_tiet_kiem")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavingsContribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_muc_tieu", nullable = false)
    private Long savingsGoalId;

    @Column(name = "so_tien", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "ngay_dong_gop", nullable = false)
    private LocalDate contributionDate;

    @Column(name = "ghi_chu", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "ngay_tao", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
