package com.example.backend.controller;

import com.example.backend.dto.*;
import com.example.backend.service.AdminActivityService;
import com.example.backend.service.AdminRolePermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller quản lý Vai trò & Phân quyền Admin
 * 
 * Chức năng:
 * - CRUD vai trò admin
 * - Xem danh sách quyền
 * - Gán vai trò cho user
 * - Kiểm tra quyền của user
 * - Xem chi tiết admin với lịch sử hoạt động
 */
@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
@Tag(name = "Admin Role & Permission", description = "API quản lý Vai trò & Phân quyền Admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRolePermissionController {

    private final AdminRolePermissionService rolePermissionService;
    private final AdminActivityService activityService;

    // ==================== ROLE ENDPOINTS ====================

    /**
     * Lấy danh sách vai trò
     */
    @GetMapping
    @Operation(summary = "Lấy danh sách vai trò", description = "Lấy tất cả vai trò admin đang hoạt động")
    public ResponseEntity<List<AdminRoleResponse>> getAllRoles() {
        List<AdminRoleResponse> roles = rolePermissionService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    /**
     * Lấy chi tiết vai trò
     */
    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết vai trò")
    public ResponseEntity<AdminRoleResponse> getRoleById(@PathVariable Long id) {
        AdminRoleResponse role = rolePermissionService.getRoleById(id);
        return ResponseEntity.ok(role);
    }

    /**
     * Tạo vai trò mới
     */
    @PostMapping
    @Operation(summary = "Tạo vai trò mới", description = "Tạo vai trò admin mới với danh sách quyền")
    public ResponseEntity<AdminRoleResponse> createRole(@Valid @RequestBody AdminRoleRequest request) {
        AdminRoleResponse role = rolePermissionService.createRole(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(role);
    }

    /**
     * Cập nhật vai trò
     */
    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật vai trò", description = "Cập nhật thông tin và quyền của vai trò")
    public ResponseEntity<AdminRoleResponse> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody AdminRoleRequest request) {
        AdminRoleResponse role = rolePermissionService.updateRole(id, request);
        return ResponseEntity.ok(role);
    }

    /**
     * Xóa vai trò
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa vai trò", description = "Xóa vai trò (không thể xóa nếu đang được sử dụng)")
    public ResponseEntity<Map<String, String>> deleteRole(@PathVariable Long id) {
        rolePermissionService.deleteRole(id);
        return ResponseEntity.ok(Map.of("message", "Đã xóa vai trò thành công"));
    }

    // ==================== PERMISSION ENDPOINTS ====================

    /**
     * Lấy tất cả quyền
     */
    @GetMapping("/permissions")
    @Operation(summary = "Lấy danh sách quyền", description = "Lấy tất cả quyền trong hệ thống")
    public ResponseEntity<List<PermissionResponse>> getAllPermissions() {
        List<PermissionResponse> permissions = rolePermissionService.getAllPermissions();
        return ResponseEntity.ok(permissions);
    }

    /**
     * Lấy quyền theo nhóm
     */
    @GetMapping("/permissions/grouped")
    @Operation(summary = "Lấy quyền theo nhóm", description = "Lấy danh sách quyền được nhóm theo category")
    public ResponseEntity<Map<String, List<PermissionResponse>>> getPermissionsByGroup() {
        Map<String, List<PermissionResponse>> grouped = rolePermissionService.getPermissionsByGroup();
        return ResponseEntity.ok(grouped);
    }

    // ==================== ADMIN USER ENDPOINTS ====================

    /**
     * Lấy danh sách admin users
     */
    @GetMapping("/users")
    @Operation(summary = "Lấy danh sách admin", description = "Lấy danh sách tất cả admin users với vai trò và quyền")
    public ResponseEntity<List<AdminUserRoleResponse>> getAllAdminUsers() {
        List<AdminUserRoleResponse> admins = rolePermissionService.getAllAdminUsers();
        return ResponseEntity.ok(admins);
    }

    /**
     * Xem chi tiết admin user (popup Chi tiết Admin)
     */
    @GetMapping("/users/{adminUserId}/detail")
    @Operation(summary = "Xem chi tiết admin", 
               description = "Lấy thông tin chi tiết admin: vai trò, quyền, lịch sử hoạt động, thống kê")
    public ResponseEntity<AdminUserFullDetailResponse> getAdminUserDetail(@PathVariable Long adminUserId) {
        AdminUserFullDetailResponse detail = activityService.getAdminFullDetail(adminUserId);
        return ResponseEntity.ok(detail);
    }

    /**
     * Gán vai trò cho user
     */
    @PostMapping("/users/{userId}/assign")
    @Operation(summary = "Gán vai trò cho user", description = "Gán vai trò admin cho một user")
    public ResponseEntity<AdminUserRoleResponse> assignRoleToUser(
            @PathVariable Long userId,
            @RequestParam String roleCode) {
        AdminUserRoleResponse adminUser = rolePermissionService.assignRoleToUser(userId, roleCode);
        return ResponseEntity.ok(adminUser);
    }

    /**
     * Kiểm tra quyền của user hiện tại
     */
    @GetMapping("/my-permissions")
    @Operation(summary = "Lấy quyền của tôi", description = "Lấy danh sách quyền của user đang đăng nhập")
    public ResponseEntity<Map<String, Object>> getMyPermissions(Authentication authentication) {
        String username = authentication.getName();
        List<String> permissions = rolePermissionService.getUserPermissions(username);
        
        return ResponseEntity.ok(Map.of(
                "username", username,
                "permissions", permissions,
                "permissionCount", permissions.size()
        ));
    }

    /**
     * Kiểm tra user có quyền cụ thể không
     */
    @GetMapping("/check-permission")
    @Operation(summary = "Kiểm tra quyền", description = "Kiểm tra user hiện tại có quyền cụ thể không")
    public ResponseEntity<Map<String, Object>> checkPermission(
            @RequestParam String permissionCode,
            Authentication authentication) {
        String username = authentication.getName();
        boolean hasPermission = rolePermissionService.hasPermission(username, permissionCode);
        
        return ResponseEntity.ok(Map.of(
                "permissionCode", permissionCode,
                "hasPermission", hasPermission
        ));
    }

    // ==================== ACTIVITY LOG ENDPOINTS ====================

    /**
     * Lấy lịch sử hoạt động gần đây (Audit Log)
     */
    @GetMapping("/activity-logs")
    @Operation(summary = "Lấy lịch sử hoạt động", description = "Lấy danh sách hoạt động gần đây của tất cả admin")
    public ResponseEntity<List<AdminUserFullDetailResponse.ActivityLogItem>> getRecentActivities(
            @RequestParam(defaultValue = "50") int limit) {
        List<AdminUserFullDetailResponse.ActivityLogItem> activities = activityService.getRecentActivities(limit);
        return ResponseEntity.ok(activities);
    }
}

