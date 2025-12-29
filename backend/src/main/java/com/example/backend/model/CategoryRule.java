package com.example.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "quy_tac_danh_muc")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tu_khoa", nullable = false, length = 255)
    private String keyword;

    @Column(name = "id_danh_muc", nullable = false)
    private Long categoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_danh_muc", insertable = false, updatable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai_khop", nullable = false, length = 20)
    private MatchType matchType = MatchType.CONTAINS;

    @Column(name = "do_uu_tien", nullable = false)
    private Integer priority = 0;

    @Column(name = "dang_hoat_dong")
    private Boolean isActive = true;

    @Column(name = "ngay_tao", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime updatedAt;

    public enum MatchType {
        EXACT, // Khớp chính xác
        CONTAINS, // Chứa keyword
        STARTS_WITH, // Bắt đầu bằng
        ENDS_WITH, // Kết thúc bằng
        REGEX // Regular expression
    }

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
