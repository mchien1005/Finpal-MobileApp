package com.example.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity Vai trò của Admin
 * Ví dụ: Super Admin, Moderator, Support
 */
@Entity
@Table(name = "vai_tro_admin")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_vai_tro", unique = true, nullable = false, length = 50)
    private String roleCode; // SUPER_ADMIN, MODERATOR, SUPPORT

    @Column(name = "ten_vai_tro", nullable = false, length = 100)
    private String roleName; // Super Admin, Moderator, Support Team

    @Column(name = "mo_ta", length = 255)
    private String description;

    @Column(name = "mau_sac", length = 20)
    private String color; // Màu hiển thị badge: #4CAF50, #2196F3

    @Column(name = "thu_tu")
    private Integer displayOrder;

    @Column(name = "dang_hoat_dong")
    private Boolean isActive = true;

    @Column(name = "ngay_tao", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime updatedAt;

    // Quan hệ nhiều-nhiều với Permission
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "vai_tro_quyen",
            joinColumns = @JoinColumn(name = "id_vai_tro"),
            inverseJoinColumns = @JoinColumn(name = "id_quyen")
    )
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods
    public boolean hasPermission(String permissionCode) {
        return permissions.stream()
                .anyMatch(p -> p.getPermissionCode().equals(permissionCode));
    }

    public boolean hasAnyPermission(String... permissionCodes) {
        for (String code : permissionCodes) {
            if (hasPermission(code)) return true;
        }
        return false;
    }

    public boolean hasAllPermissions(String... permissionCodes) {
        for (String code : permissionCodes) {
            if (!hasPermission(code)) return false;
        }
        return true;
    }
}
