package com.example.backend.controller;

import com.example.backend.dto.AdminUserDetailResponse;
import com.example.backend.dto.AdminUserListResponse;
import com.example.backend.dto.PageResponse;
import com.example.backend.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller quản lý người dùng cho Admin
 * 
 * Chức năng:
 * - Xem danh sách người dùng với tìm kiếm và phân trang
 * - Xem chi tiết 1 người dùng (thông tin, tài chính, lịch sử đăng nhập)
 * - Vô hiệu hóa / Kích hoạt tài khoản
 * - Reset mật khẩu
 * - Xóa tài khoản (soft delete)
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin User Management", description = "API quản lý người dùng cho Admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    /**
     * Lấy danh sách người dùng với phân trang và tìm kiếm
     */
    @GetMapping
    @Operation(summary = "Lấy danh sách người dùng", 
               description = "Lấy danh sách người dùng với phân trang, tìm kiếm theo keyword, lọc theo trạng thái và vai trò")
    public ResponseEntity<PageResponse<AdminUserListResponse>> getAllUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        PageResponse<AdminUserListResponse> response = adminUserService.getAllUsers(
                keyword, isActive, role, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy chi tiết 1 người dùng
     */
    @GetMapping("/{userId}")
    @Operation(summary = "Xem chi tiết người dùng", 
               description = "Lấy thông tin chi tiết của 1 người dùng: thông tin cơ bản, tổng quan tài chính, lịch sử đăng nhập")
    public ResponseEntity<AdminUserDetailResponse> getUserDetail(@PathVariable Long userId) {
        AdminUserDetailResponse response = adminUserService.getUserDetail(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Vô hiệu hóa / Kích hoạt tài khoản
     */
    @PutMapping("/{userId}/toggle-status")
    @Operation(summary = "Vô hiệu hóa / Kích hoạt tài khoản", 
               description = "Chuyển đổi trạng thái tài khoản: Active ↔ Inactive")
    public ResponseEntity<Map<String, String>> toggleUserStatus(
            @PathVariable Long userId,
            Authentication authentication) {
        String adminUsername = authentication.getName();
        adminUserService.toggleUserStatus(userId, adminUsername);
        return ResponseEntity.ok(Map.of("message", "Đã cập nhật trạng thái tài khoản thành công"));
    }

    /**
     * Reset mật khẩu người dùng
     */
    @PostMapping("/{userId}/reset-password")
    @Operation(summary = "Reset mật khẩu", 
               description = "Tạo mật khẩu mới ngẫu nhiên cho người dùng. Mật khẩu mới sẽ được trả về trong response.")
    public ResponseEntity<Map<String, String>> resetUserPassword(
            @PathVariable Long userId,
            Authentication authentication) {
        String adminUsername = authentication.getName();
        String newPassword = adminUserService.resetUserPassword(userId, adminUsername);
        return ResponseEntity.ok(Map.of(
                "message", "Đã reset mật khẩu thành công",
                "newPassword", newPassword
        ));
    }

    /**
     * Xóa tài khoản (soft delete)
     */
    @DeleteMapping("/{userId}")
    @Operation(summary = "Xóa tài khoản", 
               description = "Xóa tài khoản người dùng (soft delete: vô hiệu hóa và đánh dấu). Không thể xóa tài khoản Admin.")
    public ResponseEntity<Map<String, String>> deleteUser(
            @PathVariable Long userId,
            Authentication authentication) {
        String adminUsername = authentication.getName();
        adminUserService.deleteUser(userId, adminUsername);
        return ResponseEntity.ok(Map.of("message", "Đã xóa tài khoản thành công"));
    }

    /**
     * Thống kê tổng quan người dùng
     */
    @GetMapping("/statistics")
    @Operation(summary = "Thống kê người dùng", 
               description = "Lấy thống kê tổng quan: tổng số người dùng, số active, số mới trong tuần/tháng")
    public ResponseEntity<Map<String, Object>> getUserStatistics() {
        // TODO: Implement user statistics
        return ResponseEntity.ok(Map.of(
                "totalUsers", 0,
                "activeUsers", 0,
                "newUsersThisWeek", 0,
                "newUsersThisMonth", 0
        ));
    }
}
