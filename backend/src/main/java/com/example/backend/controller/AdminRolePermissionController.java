package com.example.backend.controller;

import com.example.backend.dto.*;
import com.example.backend.service.AdminActivityService;
import com.example.backend.service.AdminRolePermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(summary = "Lấy danh sách vai trò", description = "Lấy tất cả vai trò admin đang hoạt động, sắp xếp theo thứ tự hiển thị")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    public ResponseEntity<List<AdminRoleResponse>> getAllRoles() {
        List<AdminRoleResponse> roles = rolePermissionService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    /**
     * Lấy chi tiết vai trò
     */
    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết vai trò", description = "Lấy thông tin chi tiết của một vai trò theo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy vai trò")
    })
    public ResponseEntity<AdminRoleResponse> getRoleById(
            @Parameter(description = "ID của vai trò", example = "1", required = true) @PathVariable Long id) {
        AdminRoleResponse role = rolePermissionService.getRoleById(id);
        return ResponseEntity.ok(role);
    }

    /**
     * Tạo vai trò mới
     */
    @PostMapping
    @Operation(summary = "Tạo vai trò mới", description = "Tạo vai trò admin mới với danh sách quyền. Mã vai trò phải là duy nhất.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tạo vai trò thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc mã vai trò đã tồn tại")
    })
    public ResponseEntity<AdminRoleResponse> createRole(@Valid @RequestBody AdminRoleRequest request) {
        AdminRoleResponse role = rolePermissionService.createRole(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(role);
    }

    /**
     * Cập nhật vai trò
     */
    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật vai trò", description = "Cập nhật thông tin và quyền của vai trò. Không thể sửa vai trò SUPER_ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy vai trò"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc cố tình sửa vai trò hệ thống")
    })
    public ResponseEntity<AdminRoleResponse> updateRole(
            @Parameter(description = "ID của vai trò cần sửa", example = "1", required = true) @PathVariable Long id,
            @Valid @RequestBody AdminRoleRequest request) {
        AdminRoleResponse role = rolePermissionService.updateRole(id, request);
        return ResponseEntity.ok(role);
    }

    /**
     * Xóa vai trò
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa vai trò", description = "Xóa vai trò (soft delete). Không thể xóa nếu đang có admin sử dụng hoặc là vai trò SUPER_ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Xóa thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy vai trò"),
            @ApiResponse(responseCode = "400", description = "Vai trò đang được sử dụng hoặc là vai trò hệ thống")
    })
    public ResponseEntity<Map<String, String>> deleteRole(
            @Parameter(description = "ID của vai trò cần xóa", example = "1", required = true) @PathVariable Long id) {
        rolePermissionService.deleteRole(id);
        return ResponseEntity.ok(Map.of("message", "Đã xóa vai trò thành công"));
    }

    // ==================== PERMISSION ENDPOINTS ====================

    /**
     * Lấy tất cả quyền
     */
    @GetMapping("/permissions")
    @Operation(summary = "Lấy danh sách quyền", description = "Lấy tất cả quyền khả dụng trong hệ thống")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    })
    public ResponseEntity<List<PermissionResponse>> getAllPermissions() {
        List<PermissionResponse> permissions = rolePermissionService.getAllPermissions();
        return ResponseEntity.ok(permissions);
    }

    /**
     * Lấy quyền theo nhóm
     */
    @GetMapping("/permissions/grouped")
    @Operation(summary = "Lấy quyền theo nhóm", description = "Lấy danh sách quyền được gom nhóm theo Category (Ví dụ: USER_MANAGEMENT, SYSTEM, ...)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    })
    public ResponseEntity<Map<String, List<PermissionResponse>>> getPermissionsByGroup() {
        Map<String, List<PermissionResponse>> grouped = rolePermissionService.getPermissionsByGroup();
        return ResponseEntity.ok(grouped);
    }

    // ==================== ADMIN USER ENDPOINTS ====================

    /**
     * Lấy danh sách admin users
     */
    @GetMapping("/users")
    @Operation(summary = "Lấy danh sách admin", description = "Lấy danh sách tất cả admin users bao gồm vai trò và các quyền được cấp")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    })
    public ResponseEntity<List<AdminUserRoleResponse>> getAllAdminUsers() {
        List<AdminUserRoleResponse> admins = rolePermissionService.getAllAdminUsers();
        return ResponseEntity.ok(admins);
    }

    /**
     * Xem chi tiết admin user (popup Chi tiết Admin)
     */
    @GetMapping("/users/{adminUserId}/detail")
    @Operation(summary = "Xem chi tiết admin", description = "Lấy thông tin chi tiết admin: vai trò, quyền cụ thể, lịch sử hoạt động gần đây và thống kê")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy Admin User")
    })
    public ResponseEntity<AdminUserFullDetailResponse> getAdminUserDetail(
            @Parameter(description = "ID của Admin User", example = "1", required = true) @PathVariable Long adminUserId) {
        AdminUserFullDetailResponse detail = activityService.getAdminFullDetail(adminUserId);
        return ResponseEntity.ok(detail);
    }

    /**
     * Gán vai trò cho user
     */
    @PostMapping("/users/{userId}/assign")
    @Operation(summary = "Gán vai trò cho user", description = "Nâng cấp một User bình thường thành Admin bằng cách gán vai trò. Nếu User chưa là Admin sẽ được tạo mới record AdminUser.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Gán vai trò thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy User hoặc Vai trò")
    })
    public ResponseEntity<AdminUserRoleResponse> assignRoleToUser(
            @Parameter(description = "ID của User (bảng nguoi_dung)", example = "100", required = true) @PathVariable Long userId,
            @Parameter(description = "Mã vai trò cần gán", example = "MODERATOR", required = true) @RequestParam String roleCode) {
        AdminUserRoleResponse adminUser = rolePermissionService.assignRoleToUser(userId, roleCode);
        return ResponseEntity.ok(adminUser);
    }

    /**
     * Kiểm tra quyền của user hiện tại
     */
    @GetMapping("/my-permissions")
    @Operation(summary = "Lấy quyền của tôi", description = "Lấy danh sách mã quyền của user đang đăng nhập hiện tại")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    })
    public ResponseEntity<Map<String, Object>> getMyPermissions(Authentication authentication) {
        String username = authentication.getName();
        List<String> permissions = rolePermissionService.getUserPermissions(username);

        return ResponseEntity.ok(Map.of(
                "username", username,
                "permissions", permissions,
                "permissionCount", permissions.size()));
    }

    /**
     * Kiểm tra user có quyền cụ thể không
     */
    @GetMapping("/check-permission")
    @Operation(summary = "Kiểm tra quyền", description = "Kiểm tra xem user hiện tại có một quyền cụ thể nào đó hay không")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Kiểm tra thành công")
    })
    public ResponseEntity<Map<String, Object>> checkPermission(
            @Parameter(description = "Mã quyền cần kiểm tra", example = "view_users", required = true) @RequestParam String permissionCode,
            Authentication authentication) {
        String username = authentication.getName();
        boolean hasPermission = rolePermissionService.hasPermission(username, permissionCode);

        return ResponseEntity.ok(Map.of(
                "permissionCode", permissionCode,
                "hasPermission", hasPermission));
    }

    // ==================== ACTIVITY LOG ENDPOINTS ====================

    /**
     * Lấy lịch sử hoạt động gần đây (Audit Log)
     */
    @GetMapping("/activity-logs")
    @Operation(summary = "Lấy lịch sử hoạt động", description = "Lấy danh sách log hoạt động gần đây của tất cả admin trong hệ thống")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    })
    public ResponseEntity<List<AdminUserFullDetailResponse.ActivityLogItem>> getRecentActivities(
            @Parameter(description = "Số lượng log cần lấy", example = "50") @RequestParam(defaultValue = "50") int limit) {
        List<AdminUserFullDetailResponse.ActivityLogItem> activities = activityService.getRecentActivities(limit);
        return ResponseEntity.ok(activities);
    }
}
