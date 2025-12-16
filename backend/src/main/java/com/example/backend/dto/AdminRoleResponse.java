package com.example.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO response cho Vai trò Admin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin Vai trò Admin")
public class AdminRoleResponse {

    @Schema(description = "ID vai trò")
    private Long id;

    @Schema(description = "Mã vai trò", example = "MODERATOR")
    private String roleCode;

    @Schema(description = "Tên vai trò", example = "Moderator")
    private String roleName;

    @Schema(description = "Mô tả")
    private String description;

    @Schema(description = "Màu sắc")
    private String color;

    @Schema(description = "Thứ tự hiển thị")
    private Integer displayOrder;

    @Schema(description = "Trạng thái")
    private Boolean isActive;

    @Schema(description = "Danh sách mã quyền")
    private List<String> permissionCodes;

    @Schema(description = "Danh sách tên quyền")
    private List<String> permissionNames;

    @Schema(description = "Số lượng admin có vai trò này")
    private Long adminCount;
}
