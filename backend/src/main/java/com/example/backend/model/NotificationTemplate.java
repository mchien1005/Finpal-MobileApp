package com.example.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Model cho Mẫu Thông báo (Notification Templates)
 * Admin có thể tạo các template và gửi cho users
 */
@Entity
@Table(name = "mau_thong_bao")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_mau", unique = true, nullable = false, length = 20)
    private String templateCode; // NOT001, NOT002, etc.

    @Column(name = "tieu_de", nullable = false)
    private String title;

    @Column(name = "noi_dung_mau", columnDefinition = "TEXT", nullable = false)
    private String messageTemplate; // Template với placeholders như {amount}, {category}

    @Enumerated(EnumType.STRING)
    @Column(name = "loai", nullable = false)
    private TemplateType type = TemplateType.INFO;

    @Column(name = "so_lan_gui")
    private Integer sentCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false)
    private TemplateStatus status = TemplateStatus.ACTIVE;

    @Column(name = "nguoi_tao")
    private Long createdBy;

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

    public enum TemplateType {
        WARNING, // Cảnh báo (màu vàng)
        ALERT, // Thông báo khẩn (màu đỏ)
        SUCCESS, // Thành công (màu xanh lá)
        INFO // Thông tin (màu xanh dương)
    }

    public enum TemplateStatus {
        ACTIVE, // Đang hoạt động
        INACTIVE, // Tạm ngưng
        DRAFT // Bản nháp
    }
}
