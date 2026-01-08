package com.example.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "yeu_cau_nguoi_dung")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nguoi_dung", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai_yeu_cau", nullable = false, length = 20)
    private RequestType requestType;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false, length = 20)
    private RequestStatus status = RequestStatus.PENDING;

    @Column(name = "ly_do_yeu_cau", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "ghi_chu_admin", columnDefinition = "TEXT")
    private String adminNote;

    @Column(name = "ngay_tao", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "ngay_duyet")
    private LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_admin_duyet")
    private User approvedBy;

    @Column(name = "duong_dan_file", length = 500)
    private String filePath;

    @Column(name = "ngay_gui_email")
    private LocalDateTime emailSentAt;

    @Column(name = "ngay_du_kien_xoa")
    private LocalDateTime scheduledDeletionAt; // Thời gian dự kiến xóa tài khoản (24h sau khi approve)

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = RequestStatus.PENDING;
        }
    }

    public enum RequestType {
        EXPORT_DATA, // Xuất dữ liệu cá nhân
        DELETE_ACCOUNT // Xóa tài khoản
    }

    public enum RequestStatus {
        PENDING, // Chờ duyệt
        APPROVED, // Đã duyệt (với DELETE_ACCOUNT: đang chờ 24h để xóa)
        REJECTED, // Từ chối
        COMPLETED, // Hoàn thành (đã gửi email hoặc đã xóa tài khoản)
        CANCELLED // Đã hủy (dùng cho DELETE_ACCOUNT khi hủy trong 24h)
    }
}
