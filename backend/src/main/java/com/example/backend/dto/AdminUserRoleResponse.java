package com.example.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO response cho danh sách Admin User
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin Admin User")
public class AdminUserRoleResponse {

    @Schema(description = "ID admin user")
    private Long id;

    @Schema(description = "ID user gốc")
    private Long userId;

    @Schema(description = "Email")
    private String email;

    @Schema(description = "Tên hiển thị")
    private String displayName;

    @Schema(description = "Mã vai trò", example = "SUPER_ADMIN")
    private String roleCode;

    @Schema(description = "Tên vai trò", example = "Super Admin")
    private String roleName;

    @Schema(description = "Màu sắc vai trò")
    private String roleColor;

    @Schema(description = "Danh sách quyền")
    private List<String> permissions;

    @Schema(description = "Tên các quyền hiển thị")
    private List<String> permissionNames;

    @Schema(description = "Hoạt động lần cuối")
    private LocalDateTime lastActivityAt;

    @Schema(description = "Trạng thái hoạt động")
    private Boolean isActive;

    @Schema(description = "Ngày tạo")
    private LocalDateTime createdAt;
}
