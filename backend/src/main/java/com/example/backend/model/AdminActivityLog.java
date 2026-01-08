package com.example.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity lưu lịch sử hoạt động của Admin
 * Ghi lại mọi thao tác mà admin thực hiện trong hệ thống
 */
@Entity
@Table(name = "lich_su_hoat_dong_admin")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_admin_user")
    private AdminUser adminUser;

    @Column(name = "hanh_dong", nullable = false, length = 100)
    private String action; // CREATE, UPDATE, DELETE, VIEW, LOGIN, LOGOUT

    @Column(name = "loai_doi_tuong", length = 50)
    private String entityType; // USER, CATEGORY, SMS_PARSER, CONTENT, SYSTEM

    @Column(name = "id_doi_tuong")
    private Long entityId;

    @Column(name = "mo_ta", length = 500)
    private String description; // Mô tả hành động: "Đã chỉnh sửa category 'Ăn uống'"

    @Column(name = "du_lieu_cu", columnDefinition = "TEXT")
    private String oldData; // JSON data trước khi thay đổi

    @Column(name = "du_lieu_moi", columnDefinition = "TEXT")
    private String newData; // JSON data sau khi thay đổi

    @Column(name = "dia_chi_ip", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "thoi_gian", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "trang_thai", length = 20)
    private String status; // SUCCESS, FAILED

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    // Action constants
    public static final String ACTION_CREATE = "CREATE";
    public static final String ACTION_UPDATE = "UPDATE";
    public static final String ACTION_DELETE = "DELETE";
    public static final String ACTION_VIEW = "VIEW";
    public static final String ACTION_LOGIN = "LOGIN";
    public static final String ACTION_LOGOUT = "LOGOUT";
    public static final String ACTION_EXPORT = "EXPORT";
    public static final String ACTION_IMPORT = "IMPORT";

    // Entity type constants
    public static final String ENTITY_USER = "USER";
    public static final String ENTITY_CATEGORY = "CATEGORY";
    public static final String ENTITY_SMS_PARSER = "SMS_PARSER";
    public static final String ENTITY_CONTENT = "CONTENT";
    public static final String ENTITY_ROLE = "ROLE";
    public static final String ENTITY_SYSTEM = "SYSTEM";
    public static final String ENTITY_FAQ = "FAQ";
    public static final String ENTITY_TIP = "TIP";
}
