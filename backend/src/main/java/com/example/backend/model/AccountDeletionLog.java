package com.example.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity lưu lịch sử xóa tài khoản người dùng
 * Dùng cho mục đích audit - giữ lại thông tin sau khi user đã bị xóa
 */
@Entity
@Table(name = "lich_su_xoa_tai_khoan")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDeletionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Thông tin người dùng bị xóa (lưu lại vì user sẽ bị xóa)
    @Column(name = "id_nguoi_dung_bi_xoa", nullable = false)
    private Long deletedUserId;

    @Column(name = "ten_nguoi_dung", nullable = false, length = 100)
    private String username;

    @Column(name = "email", nullable = false, length = 200)
    private String email;

    @Column(name = "ho_ten", length = 200)
    private String fullName;

    // Thông tin yêu cầu xóa
    @Column(name = "id_yeu_cau", nullable = false)
    private Long requestId;

    @Column(name = "ly_do_yeu_cau", columnDefinition = "TEXT")
    private String requestReason;

    @Column(name = "ngay_yeu_cau", nullable = false)
    private LocalDateTime requestCreatedAt;

    // Thông tin admin phê duyệt
    @Column(name = "id_admin_duyet")
    private Long approvedByAdminId;

    @Column(name = "ten_admin_duyet", length = 100)
    private String approvedByAdminUsername;

    @Column(name = "ghi_chu_admin", columnDefinition = "TEXT")
    private String adminNote;

    @Column(name = "ngay_duyet")
    private LocalDateTime approvedAt;

    // Thông tin xóa
    @Column(name = "ngay_xoa", nullable = false)
    private LocalDateTime deletedAt;

    @Column(name = "da_gui_email_thong_bao")
    private Boolean emailNotificationSent = false;

    @Column(name = "trang_thai_xoa", length = 20)
    private String deletionStatus = "COMPLETED"; // COMPLETED, FAILED

    @Column(name = "thong_bao_loi", columnDefinition = "TEXT")
    private String errorMessage;

    @PrePersist
    protected void onCreate() {
        if (deletedAt == null) {
            deletedAt = LocalDateTime.now();
        }
    }
}
