package com.example.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity Admin User - Người dùng quản trị
 * Liên kết với AdminRole để phân quyền chi tiết
 */
@Entity
@Table(name = "nguoi_dung_admin")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Liên kết với bảng nguoi_dung (User) chính
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nguoi_dung", unique = true)
    private User user;

    // Vai trò admin
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_vai_tro")
    private AdminRole adminRole;

    @Column(name = "ten_hien_thi", length = 100)
    private String displayName; // Tên hiển thị cho admin

    @Column(name = "ghi_chu", length = 500)
    private String notes;

    @Column(name = "dang_hoat_dong")
    private Boolean isActive = true;

    @Column(name = "ngay_tao", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime updatedAt;

    @Column(name = "hoat_dong_lan_cuoi")
    private LocalDateTime lastActivityAt;

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
        if (adminRole == null) return false;
        
        // Super Admin có tất cả quyền
        if (adminRole.hasPermission(Permission.ALL)) return true;
        
        return adminRole.hasPermission(permissionCode);
    }

    public boolean hasAnyPermission(String... permissionCodes) {
        if (adminRole == null) return false;
        if (adminRole.hasPermission(Permission.ALL)) return true;
        return adminRole.hasAnyPermission(permissionCodes);
    }

    public String getRoleName() {
        return adminRole != null ? adminRole.getRoleName() : "Unknown";
    }

    public String getRoleCode() {
        return adminRole != null ? adminRole.getRoleCode() : null;
    }
}
