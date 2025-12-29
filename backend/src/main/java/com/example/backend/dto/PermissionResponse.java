package com.example.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO response cho Permission
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin Quyền hạn")
public class PermissionResponse {

    @Schema(description = "ID quyền")
    private Long id;

    @Schema(description = "Mã quyền", example = "VIEW_USERS")
    private String permissionCode;

    @Schema(description = "Tên quyền", example = "Xem người dùng")
    private String permissionName;

    @Schema(description = "Nhóm quyền", example = "USER_MANAGEMENT")
    private String permissionGroup;

    @Schema(description = "Mô tả")
    private String description;
}
