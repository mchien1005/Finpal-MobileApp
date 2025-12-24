package com.example.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Model lưu cài đặt thông báo của người dùng
 * Cho phép user bật/tắt từng loại thông báo
 */
@Entity
@Table(name = "cai_dat_thong_bao")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nguoi_dung", nullable = false, unique = true)
    private User user;

    // ============================================
    // Toggle tổng - Tắt hết tất cả thông báo
    // ============================================
    @Column(name = "nhan_thong_bao")
    @Builder.Default
    private Boolean pushEnabled = true;

    // ============================================
    // Loại thông báo
    // ============================================

    // Cảnh báo giao dịch - Thông báo khi có giao dịch mới
    @Column(name = "canh_bao_giao_dich")
    @Builder.Default
    private Boolean transactionAlerts = true;

    // Cảnh báo ngân sách - Thông báo khi vượt ngân sách
    @Column(name = "canh_bao_ngan_sach")
    @Builder.Default
    private Boolean budgetAlerts = true;

    // Nhắc nhở mục tiêu - Nhắc nhở về mục tiêu tiết kiệm
    @Column(name = "nhac_nho_muc_tieu")
    @Builder.Default
    private Boolean goalReminders = true;

    // Cảnh báo bảo mật - Thông báo về hoạt động bất thường
    @Column(name = "canh_bao_bao_mat")
    @Builder.Default
    private Boolean securityAlerts = true;

    // ============================================
    // Báo cáo định kỳ
    // ============================================

    // Báo cáo tuần - Tóm tắt chi tiêu hàng tuần
    @Column(name = "bao_cao_tuan")
    @Builder.Default
    private Boolean weeklyReport = true;

    // Báo cáo tháng - Tóm tắt chi tiêu hàng tháng
    @Column(name = "bao_cao_thang")
    @Builder.Default
    private Boolean monthlyReport = true;

    // ============================================
    // Thông báo khác (có thể thêm sau)
    // ============================================

    // Gợi ý tiết kiệm thông minh
    @Column(name = "goi_y_tiet_kiem")
    @Builder.Default
    private Boolean savingsTips = true;

    // Phân tích chi tiêu
    @Column(name = "phan_tich_chi_tieu")
    @Builder.Default
    private Boolean spendingInsights = true;

    // ============================================
    // Timestamps
    // ============================================
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
