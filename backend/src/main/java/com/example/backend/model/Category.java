package com.example.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "danh_muc")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ten_danh_muc", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai", nullable = false)
    private CategoryType type = CategoryType.EXPENSE;

    @Column(name = "id_cha")
    private Long parentId;

    @Column(name = "la_he_thong")
    private Boolean isSystem = false;

    @Column(name = "thu_tu_hien_thi")
    private Integer displayOrder = 0;

    @Column(name = "bieu_tuong", length = 100)
    private String icon;

    @Column(name = "mau_sac", length = 20)
    private String color;

    @Column(name = "mo_ta", columnDefinition = "TEXT")
    private String description;

    @Column(name = "ngay_tao", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cha", insertable = false, updatable = false)
    private Category parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private java.util.List<Category> subCategories;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum CategoryType {
        INCOME, EXPENSE
    }
}
