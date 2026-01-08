package com.example.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity lưu trữ lịch sử đăng nhập của người dùng
 * Bao gồm thông tin thiết bị, IP, trạng thái đăng nhập
 */
@Entity
@Table(name = "lich_su_dang_nhap")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nguoi_dung", nullable = false)
    private User user;

    @Column(name = "thoi_gian_dang_nhap", nullable = false)
    private LocalDateTime loginTime;

    @Column(name = "dia_chi_ip", length = 50)
    private String ipAddress;

    @Column(name = "ten_thiet_bi", length = 200)
    private String deviceName;

    @Column(name = "loai_thiet_bi", length = 50)
    private String deviceType; // MOBILE, TABLET, DESKTOP, WEB

    @Column(name = "he_dieu_hanh", length = 100)
    private String operatingSystem;

    @Column(name = "trinh_duyet", length = 100)
    private String browser;

    @Column(name = "vi_tri", length = 200)
    private String location; // Thành phố, Quốc gia

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", length = 20)
    private LoginStatus status;

    @Column(name = "ly_do_that_bai", length = 255)
    private String failureReason;

    @Column(name = "fcm_token", length = 500)
    private String fcmToken;

    public enum LoginStatus {
        SUCCESS,    // Đăng nhập thành công
        FAILED,     // Đăng nhập thất bại (sai mật khẩu)
        BLOCKED,    // Bị chặn (quá nhiều lần thất bại)
        EXPIRED     // Session hết hạn
    }

    @PrePersist
    protected void onCreate() {
        if (loginTime == null) {
            loginTime = LocalDateTime.now();
        }
    }
}
