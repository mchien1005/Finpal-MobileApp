package com.example.backend.dto;

import com.example.backend.model.LoginHistory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO chi tiết người dùng cho Admin
 * Bao gồm: thông tin cơ bản, tổng quan tài chính, lịch sử đăng nhập
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Chi tiết người dùng cho Admin")
public class AdminUserDetailResponse {

    // ========== THÔNG TIN CƠ BẢN ==========
    @Schema(description = "ID người dùng")
    private Long id;

    @Schema(description = "Mã người dùng hiển thị", example = "USR001")
    private String userCode;

    @Schema(description = "Tên đăng nhập")
    private String username;

    @Schema(description = "Email")
    private String email;

    @Schema(description = "Số điện thoại")
    private String phone;

    @Schema(description = "Họ tên")
    private String fullName;

    @Schema(description = "Ảnh đại diện URL")
    private String avatarUrl;

    @Schema(description = "Trạng thái tài khoản")
    private Boolean isActive;

    @Schema(description = "Vai trò", example = "USER hoặc ADMIN")
    private String role;

    @Schema(description = "Ngày đăng ký")
    private LocalDateTime createdAt;

    @Schema(description = "Lần đăng nhập cuối")
    private LocalDateTime lastLoginAt;

    @Schema(description = "Lần hoạt động cuối")
    private LocalDateTime lastActiveAt;

    @Schema(description = "Ngân hàng chính (nguồn giao dịch phổ biến nhất)")
    private String primaryBank;

    // ========== TỔNG QUAN TÀI CHÍNH ==========
    @Schema(description = "Tổng số giao dịch")
    private Long totalTransactions;

    @Schema(description = "Tổng thu nhập")
    private BigDecimal totalIncome;

    @Schema(description = "Tổng chi tiêu")
    private BigDecimal totalExpense;

    @Schema(description = "Trung bình mỗi giao dịch")
    private BigDecimal averagePerTransaction;

    @Schema(description = "Số ngân sách đang hoạt động")
    private Long activeBudgets;

    @Schema(description = "Số mục tiêu tiết kiệm đang hoạt động")
    private Long activeSavingsGoals;

    // ========== LỊCH SỬ ĐĂNG NHẬP ==========
    @Schema(description = "Danh sách lịch sử đăng nhập gần nhất")
    private List<LoginHistoryItem> loginHistory;

    @Schema(description = "Tổng số lần đăng nhập")
    private Long totalLogins;

    @Schema(description = "Số lần đăng nhập thất bại")
    private Long failedLogins;

    /**
     * DTO cho mỗi item lịch sử đăng nhập
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginHistoryItem {
        @Schema(description = "ID")
        private Long id;

        @Schema(description = "Thời gian đăng nhập")
        private LocalDateTime loginTime;

        @Schema(description = "Tên thiết bị", example = "iPhone 15 Pro")
        private String deviceName;

        @Schema(description = "Địa chỉ IP")
        private String ipAddress;

        @Schema(description = "Vị trí", example = "Hà Nội")
        private String location;

        @Schema(description = "Trạng thái", example = "SUCCESS hoặc FAILED")
        private String status;

        @Schema(description = "Lý do thất bại (nếu có)")
        private String failureReason;

        public static LoginHistoryItem fromEntity(LoginHistory entity) {
            return LoginHistoryItem.builder()
                    .id(entity.getId())
                    .loginTime(entity.getLoginTime())
                    .deviceName(entity.getDeviceName())
                    .ipAddress(entity.getIpAddress())
                    .location(entity.getLocation())
                    .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                    .failureReason(entity.getFailureReason())
                    .build();
        }
    }
}
