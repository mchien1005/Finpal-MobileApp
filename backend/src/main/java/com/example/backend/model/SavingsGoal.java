package com.example.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "muc_tieu_tiet_kiem")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SavingsGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_nguoi_dung", nullable = false)
    private Long userId;

    @Column(name = "ten_muc_tieu", nullable = false, length = 200)
    private String name;

    @Column(name = "mo_ta", columnDefinition = "TEXT")
    private String description;

    @Column(name = "so_tien_muc_tieu", nullable = false, precision = 15, scale = 2)
    private BigDecimal targetAmount;

    @Column(name = "so_tien_hien_tai", precision = 15, scale = 2)
    private BigDecimal currentAmount = BigDecimal.ZERO;

    @Column(name = "han_chot")
    private LocalDate deadline;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false)
    private GoalStatus status = GoalStatus.ACTIVE;

    @Column(name = "ngay_tao", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime updatedAt;

    @Column(name = "ngay_hoan_thanh")
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum GoalStatus {
        ACTIVE, COMPLETED, CANCELLED
    }
}
