package com.example.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO danh sách người dùng cho Admin (phiên bản rút gọn)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin người dùng cho danh sách Admin")
public class AdminUserListResponse {

    @Schema(description = "ID người dùng")
    private Long id;

    @Schema(description = "Mã người dùng", example = "USR001")
    private String userCode;

    @Schema(description = "Tên đăng nhập")
    private String username;

    @Schema(description = "Email")
    private String email;

    @Schema(description = "Họ tên")
    private String fullName;

    @Schema(description = "Số điện thoại")
    private String phone;

    @Schema(description = "Trạng thái")
    private Boolean isActive;

    @Schema(description = "Vai trò")
    private String role;

    @Schema(description = "Ngày đăng ký")
    private LocalDateTime createdAt;

    @Schema(description = "Lần đăng nhập cuối")
    private LocalDateTime lastLoginAt;

    @Schema(description = "Tổng số giao dịch")
    private Long totalTransactions;
}
