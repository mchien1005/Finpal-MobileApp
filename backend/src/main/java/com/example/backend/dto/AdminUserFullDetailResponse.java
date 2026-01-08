package com.example.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO chi tiết Admin User (cho popup Chi tiết Admin)
 * Bao gồm: thông tin cơ bản, vai trò, quyền, lịch sử hoạt động
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Chi tiết Admin User đầy đủ")
public class AdminUserFullDetailResponse {

    // ========== THÔNG TIN CƠ BẢN ==========
    @Schema(description = "ID admin user")
    private Long id;

    @Schema(description = "ID user gốc")
    private Long userId;

    @Schema(description = "Email")
    private String email;

    @Schema(description = "Tên hiển thị")
    private String displayName;

    @Schema(description = "Ảnh đại diện")
    private String avatarUrl;

    // ========== VAI TRÒ & QUYỀN ==========
    @Schema(description = "Mã vai trò", example = "SUPER_ADMIN")
    private String roleCode;

    @Schema(description = "Tên vai trò", example = "Super Admin")
    private String roleName;

    @Schema(description = "Màu vai trò")
    private String roleColor;

    @Schema(description = "Trạng thái hoạt động")
    private Boolean isActive;

    @Schema(description = "Danh sách mã quyền")
    private List<String> permissions;

    @Schema(description = "Danh sách tên quyền hiển thị")
    private List<String> permissionNames;

    // ========== THỜI GIAN ==========
    @Schema(description = "Hoạt động lần cuối")
    private LocalDateTime lastActivityAt;

    @Schema(description = "Thời gian hoạt động lần cuối (dạng text)", example = "5 phút trước")
    private String lastActivityText;

    @Schema(description = "Đăng nhập lần cuối")
    private LocalDateTime lastLoginAt;

    @Schema(description = "Ngày tạo tài khoản")
    private LocalDateTime createdAt;

    @Schema(description = "Người tạo")
    private String createdBy;

    // ========== THỐNG KÊ ==========
    @Schema(description = "Tổng số hành động")
    private Long totalActions;

    // ========== LỊCH SỬ HOẠT ĐỘNG ==========
    @Schema(description = "Lịch sử hoạt động gần đây")
    private List<ActivityLogItem> recentActivities;

    /**
     * DTO cho mỗi item lịch sử hoạt động
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityLogItem {
        @Schema(description = "ID log")
        private Long id;

        @Schema(description = "Mô tả hành động", example = "Đã chỉnh sửa category \"Ăn uống\"")
        private String description;

        @Schema(description = "Loại hành động", example = "UPDATE")
        private String action;

        @Schema(description = "Loại đối tượng", example = "CATEGORY")
        private String entityType;

        @Schema(description = "Thời gian")
        private LocalDateTime timestamp;

        @Schema(description = "Thời gian dạng text", example = "24/03/2024 10:30")
        private String timestampText;

        @Schema(description = "Địa chỉ IP")
        private String ipAddress;

        @Schema(description = "Trạng thái", example = "SUCCESS")
        private String status;

        @Schema(description = "Tên admin thực hiện", example = "admin")
        private String adminName;
    }
}
