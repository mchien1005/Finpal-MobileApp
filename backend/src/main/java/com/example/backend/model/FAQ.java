package com.example.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Model cho Câu hỏi Thường gặp (FAQ - Frequently Asked Questions)
 * Admin quản lý các câu hỏi và câu trả lời
 */
@Entity
@Table(name = "cau_hoi_thuong_gap")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FAQ {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_faq", unique = true, nullable = false, length = 20)
    private String faqCode; // FAQ001, FAQ002, etc.

    @Column(name = "cau_hoi", nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(name = "cau_tra_loi", nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Enumerated(EnumType.STRING)
    @Column(name = "danh_muc")
    private FAQCategory category = FAQCategory.GETTING_STARTED;

    @Column(name = "luot_xem")
    private Integer viewCount = 0;

    @Column(name = "co_huu_ich")
    private Integer helpfulCount = 0;

    @Column(name = "khong_huu_ich")
    private Integer notHelpfulCount = 0;

    @Column(name = "thu_tu")
    private Integer displayOrder = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false)
    private FAQStatus status = FAQStatus.ACTIVE;

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

    public enum FAQCategory {
        GETTING_STARTED, SECURITY, FEATURES, TROUBLESHOOTING
    }

    public enum FAQStatus {
        ACTIVE, // Đang hiển thị
        INACTIVE, // Tạm ẩn
        DRAFT // Bản nháp
    }
}
