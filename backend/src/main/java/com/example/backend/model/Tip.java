package com.example.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Model cho Mẹo và Gợi ý Tiết kiệm (Tips & Suggestions)
 * Admin quản lý các tips hiển thị cho users
 */
@Entity
@Table(name = "meo_goi_y")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_tip", unique = true, nullable = false, length = 20)
    private String tipCode; // TIP001, TIP002, etc.

    @Column(name = "tieu_de", nullable = false)
    private String title;

    @Column(name = "noi_dung", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "danh_muc")
    private TipCategory category = TipCategory.GENERAL;

    @Column(name = "icon", length = 100)
    private String icon; // Icon name or emoji

    @Column(name = "luot_xem")
    private Integer viewCount = 0;

    @Column(name = "luot_thich")
    private Integer likeCount = 0;

    @Column(name = "thu_tu")
    private Integer displayOrder = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false)
    private TipStatus status = TipStatus.ACTIVE;

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

    public enum TipCategory {
        SAVING, // Tiết kiệm
        BUDGETING, // Lập ngân sách
        INVESTING, // Đầu tư
        SPENDING, // Chi tiêu thông minh
        GENERAL // Chung
    }

    public enum TipStatus {
        ACTIVE, // Đang hiển thị
        INACTIVE, // Tạm ẩn
        DRAFT // Bản nháp
    }
}
