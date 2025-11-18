package com.example.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryType type = CategoryType.EXPENSE;

    @Column(length = 50)
    private String icon;

    @Column(length = 7)
    private String color;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "is_system")
    private Boolean isSystem = false;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
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
