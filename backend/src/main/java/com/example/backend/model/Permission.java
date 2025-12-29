package com.example.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity Quyền hạn
 * Ví dụ: VIEW_USERS, EDIT_USERS, DELETE_USERS, VIEW_LOGS, MANAGE_SYSTEM
 */
@Entity
@Table(name = "quyen_han")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_quyen", unique = true, nullable = false, length = 50)
    private String permissionCode; // VIEW_USERS, EDIT_CONTENT, VIEW_LOGS

    @Column(name = "ten_quyen", nullable = false, length = 100)
    private String permissionName; // Xem người dùng, Sửa nội dung, Xem logs

    @Column(name = "nhom_quyen", length = 50)
    private String permissionGroup; // USER_MANAGEMENT, CONTENT, SYSTEM, REPORTS

    @Column(name = "mo_ta", length = 255)
    private String description;

    @Column(name = "thu_tu")
    private Integer displayOrder;

    @Column(name = "ngay_tao", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Các mã quyền hệ thống (constants)
    public static final String VIEW_DASHBOARD = "VIEW_DASHBOARD";
    public static final String VIEW_ANALYTICS = "VIEW_ANALYTICS";
    
    public static final String VIEW_USERS = "VIEW_USERS";
    public static final String EDIT_USERS = "EDIT_USERS";
    public static final String DELETE_USERS = "DELETE_USERS";
    public static final String RESET_PASSWORD = "RESET_PASSWORD";
    
    public static final String VIEW_CATEGORIES = "VIEW_CATEGORIES";
    public static final String EDIT_CATEGORIES = "EDIT_CATEGORIES";
    
    public static final String VIEW_SMS_PARSERS = "VIEW_SMS_PARSERS";
    public static final String EDIT_SMS_PARSERS = "EDIT_SMS_PARSERS";
    
    public static final String VIEW_AI_MODEL = "VIEW_AI_MODEL";
    public static final String MANAGE_AI_MODEL = "MANAGE_AI_MODEL";
    
    public static final String VIEW_CONTENT = "VIEW_CONTENT";
    public static final String EDIT_CONTENT = "EDIT_CONTENT";
    
    public static final String VIEW_LOGS = "VIEW_LOGS";
    public static final String VIEW_AUDIT = "VIEW_AUDIT";
    
    public static final String MANAGE_SYSTEM = "MANAGE_SYSTEM";
    public static final String MANAGE_ROLES = "MANAGE_ROLES";
    public static final String MANAGE_BACKUP = "MANAGE_BACKUP";
    
    public static final String ALL = "ALL"; // Super Admin - có tất cả quyền
}
