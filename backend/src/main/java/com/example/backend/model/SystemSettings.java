package com.example.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity lưu trữ cài đặt hệ thống
 * Dùng cho admin quản lý các thông số AI, thông báo, v.v.
 */
@Entity
@Table(name = "cai_dat_he_thong")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "khoa_cai_dat", unique = true, nullable = false, length = 100)
    private String settingKey;

    @Column(name = "gia_tri", length = 500)
    private String settingValue;

    @Column(name = "mo_ta", length = 255)
    private String description;

    @Column(name = "nhom_cai_dat", length = 50)
    private String settingGroup; // AI, NOTIFICATION, SYSTEM

    @Column(name = "kieu_du_lieu", length = 20)
    private String dataType; // STRING, INTEGER, DOUBLE, BOOLEAN

    @Column(name = "ngay_tao", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime updatedAt;

    @Column(name = "nguoi_cap_nhat")
    private Long updatedBy;

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
