package com.example.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO request tạo/cập nhật Vai trò Admin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request tạo/cập nhật Vai trò Admin")
public class AdminRoleRequest {

    @NotBlank(message = "Mã vai trò không được để trống")
    @Schema(description = "Mã vai trò (unique)", example = "MODERATOR", required = true)
    private String roleCode;

    @NotBlank(message = "Tên vai trò không được để trống")
    @Schema(description = "Tên vai trò", example = "Moderator", required = true)
    private String roleName;

    @Schema(description = "Mô tả vai trò")
    private String description;

    @Schema(description = "Màu sắc hiển thị", example = "#2196F3")
    private String color;

    @Schema(description = "Thứ tự hiển thị")
    private Integer displayOrder;

    @NotNull(message = "Danh sách quyền không được để trống")
    @Schema(description = "Danh sách mã quyền", required = true)
    private List<String> permissionCodes;
}
