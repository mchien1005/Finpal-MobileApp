package com.example.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "cai_dat_nguoi_dung")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_nguoi_dung", nullable = false, unique = true)
    private Long userId;

    @Column(name = "don_vi_tien_te", length = 3)
    @Builder.Default
    private String currency = "VND";

    @Column(name = "ngon_ngu", length = 10)
    @Builder.Default
    private String language = "vi";

    @Column(name = "bat_thong_bao")
    @Builder.Default
    private Boolean notificationsEnabled = true;

    @Column(name = "thong_bao_email")
    @Builder.Default
    private Boolean emailNotifications = true;

    @Column(name = "thong_bao_day")
    @Builder.Default
    private Boolean pushNotifications = true;

    @Column(name = "canh_bao_giao_dich")
    @Builder.Default
    private Boolean transactionAlert = true;

    @Column(name = "canh_bao_ngan_sach")
    @Builder.Default
    private Boolean budgetAlert = true;

    @Column(name = "nhac_nho_muc_tieu")
    @Builder.Default
    private Boolean goalReminder = true;

    // Periodic reports
    @Column(name = "bao_cao_tuan")
    @Builder.Default
    private Boolean weeklyReport = false;

    @Column(name = "bao_cao_thang")
    @Builder.Default
    private Boolean monthlyReport = true;

    @Column(name = "nguong_canh_bao_ngan_sach")
    @Builder.Default
    private Integer budgetAlertThreshold = 70;

    @Column(name = "giao_dien", length = 20)
    @Builder.Default
    private String theme = "light";

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
}
