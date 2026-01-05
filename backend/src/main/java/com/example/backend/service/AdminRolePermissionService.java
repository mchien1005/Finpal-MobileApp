package com.example.backend.service;

import com.example.backend.dto.*;
import com.example.backend.model.*;
import com.example.backend.repository.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service quản lý Vai trò & Phân quyền Admin
 * Chức năng: CRUD vai trò, gán quyền, kiểm tra quyền
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AdminRolePermissionService {

    private final AdminRoleRepository adminRoleRepository;
    private final PermissionRepository permissionRepository;
    private final AdminUserRepository adminUserRepository;
    private final UserRepository userRepository;

    /**
     * Khởi tạo dữ liệu mặc định khi ứng dụng khởi động
     */
    @PostConstruct
    @Transactional
    public void initializeDefaultData() {
        log.info("Đang khởi tạo dữ liệu Vai trò & Quyền mặc định...");
        initializePermissions();
        initializeRoles();
        log.info("Đã khởi tạo xong dữ liệu Vai trò & Quyền mặc định");
    }

    /**
     * Khởi tạo các quyền mặc định
     */
    private void initializePermissions() {
        createPermissionIfNotExists(Permission.ALL, "Tất cả quyền", "SYSTEM", "Super Admin - có tất cả quyền", 0);
        
        // Dashboard & Analytics
        createPermissionIfNotExists(Permission.VIEW_DASHBOARD, "Xem Dashboard", "DASHBOARD", "Xem tổng quan hệ thống", 1);
        createPermissionIfNotExists(Permission.VIEW_ANALYTICS, "Xem Analytics", "DASHBOARD", "Xem báo cáo phân tích", 2);
        
        // User Management
        createPermissionIfNotExists(Permission.VIEW_USERS, "Xem người dùng", "USER_MANAGEMENT", "Xem danh sách và chi tiết người dùng", 10);
        createPermissionIfNotExists(Permission.EDIT_USERS, "Sửa người dùng", "USER_MANAGEMENT", "Cập nhật thông tin người dùng", 11);
        createPermissionIfNotExists(Permission.DELETE_USERS, "Xóa người dùng", "USER_MANAGEMENT", "Xóa/vô hiệu hóa người dùng", 12);
        createPermissionIfNotExists(Permission.RESET_PASSWORD, "Reset mật khẩu", "USER_MANAGEMENT", "Đặt lại mật khẩu người dùng", 13);
        
        // Category Management
        createPermissionIfNotExists(Permission.VIEW_CATEGORIES, "Xem danh mục", "CATEGORY", "Xem danh sách danh mục", 20);
        createPermissionIfNotExists(Permission.EDIT_CATEGORIES, "Sửa danh mục", "CATEGORY", "Thêm/sửa/xóa danh mục", 21);
        
        // SMS Parser
        createPermissionIfNotExists(Permission.VIEW_SMS_PARSERS, "Xem SMS Parser", "SMS_PARSER", "Xem cấu hình SMS parser", 30);
        createPermissionIfNotExists(Permission.EDIT_SMS_PARSERS, "Sửa SMS Parser", "SMS_PARSER", "Thêm/sửa/xóa SMS parser", 31);
        
        // AI Model
        createPermissionIfNotExists(Permission.VIEW_AI_MODEL, "Xem AI Model", "AI", "Xem thông tin mô hình AI", 40);
        createPermissionIfNotExists(Permission.MANAGE_AI_MODEL, "Quản lý AI Model", "AI", "Cấu hình và huấn luyện AI", 41);
        
        // Content Management
        createPermissionIfNotExists(Permission.VIEW_CONTENT, "Xem nội dung", "CONTENT", "Xem tips, FAQs, templates", 50);
        createPermissionIfNotExists(Permission.EDIT_CONTENT, "Sửa nội dung", "CONTENT", "Thêm/sửa/xóa nội dung", 51);
        
        // Logs & Audit
        createPermissionIfNotExists(Permission.VIEW_LOGS, "Xem Logs", "AUDIT", "Xem logs hệ thống", 60);
        createPermissionIfNotExists(Permission.VIEW_AUDIT, "Xem Audit", "AUDIT", "Xem lịch sử audit", 61);
        
        // System
        createPermissionIfNotExists(Permission.MANAGE_SYSTEM, "Quản lý hệ thống", "SYSTEM", "Cấu hình hệ thống", 70);
        createPermissionIfNotExists(Permission.MANAGE_ROLES, "Quản lý vai trò", "SYSTEM", "Thêm/sửa/xóa vai trò admin", 71);
        createPermissionIfNotExists(Permission.MANAGE_BACKUP, "Quản lý backup", "SYSTEM", "Sao lưu và phục hồi dữ liệu", 72);
    }

    private void createPermissionIfNotExists(String code, String name, String group, String description, int order) {
        if (!permissionRepository.existsByPermissionCode(code)) {
            Permission permission = Permission.builder()
                    .permissionCode(code)
                    .permissionName(name)
                    .permissionGroup(group)
                    .description(description)
                    .displayOrder(order)
                    .build();
            permissionRepository.save(permission);
            log.debug("Đã tạo quyền: {}", code);
        }
    }

    /**
     * Khởi tạo các vai trò mặc định
     */
    private void initializeRoles() {
        // Super Admin - có tất cả quyền
        createRoleIfNotExists("SUPER_ADMIN", "Super Admin", "Quản trị viên cao nhất, có tất cả quyền", 
                "#4CAF50", 1, List.of(Permission.ALL));
        
        // Moderator - quản lý nội dung và người dùng
        createRoleIfNotExists("MODERATOR", "Moderator", "Quản lý nội dung và hỗ trợ người dùng",
                "#2196F3", 2, List.of(
                        Permission.VIEW_DASHBOARD, Permission.VIEW_ANALYTICS,
                        Permission.VIEW_USERS, Permission.EDIT_USERS,
                        Permission.VIEW_CONTENT, Permission.EDIT_CONTENT,
                        Permission.VIEW_LOGS
                ));
        
        // Support - hỗ trợ người dùng
        createRoleIfNotExists("SUPPORT", "Support Team", "Nhóm hỗ trợ khách hàng",
                "#9C27B0", 3, List.of(
                        Permission.VIEW_DASHBOARD,
                        Permission.VIEW_USERS,
                        Permission.VIEW_LOGS
                ));
    }

    private void createRoleIfNotExists(String code, String name, String description, String color, 
                                        int order, List<String> permissionCodes) {
        if (!adminRoleRepository.existsByRoleCode(code)) {
            Set<Permission> permissions = new HashSet<>();
            for (String permCode : permissionCodes) {
                permissionRepository.findByPermissionCode(permCode).ifPresent(permissions::add);
            }

            AdminRole role = AdminRole.builder()
                    .roleCode(code)
                    .roleName(name)
                    .description(description)
                    .color(color)
                    .displayOrder(order)
                    .isActive(true)
                    .permissions(permissions)
                    .build();
            adminRoleRepository.save(role);
            log.debug("Đã tạo vai trò: {} với {} quyền", code, permissions.size());
        }
    }

    // ==================== ROLE MANAGEMENT ====================

    /**
     * Lấy tất cả vai trò
     */
    @Transactional(readOnly = true)
    public List<AdminRoleResponse> getAllRoles() {
        return adminRoleRepository.findByIsActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(this::toRoleResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết vai trò
     */
    @Transactional(readOnly = true)
    public AdminRoleResponse getRoleById(Long id) {
        AdminRole role = adminRoleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò"));
        return toRoleResponse(role);
    }

    /**
     * Tạo vai trò mới
     */
    @Transactional
    public AdminRoleResponse createRole(AdminRoleRequest request) {
        if (adminRoleRepository.existsByRoleCode(request.getRoleCode())) {
            throw new RuntimeException("Mã vai trò đã tồn tại: " + request.getRoleCode());
        }

        Set<Permission> permissions = getPermissionsByCode(request.getPermissionCodes());

        AdminRole role = AdminRole.builder()
                .roleCode(request.getRoleCode())
                .roleName(request.getRoleName())
                .description(request.getDescription())
                .color(request.getColor())
                .displayOrder(request.getDisplayOrder())
                .isActive(true)
                .permissions(permissions)
                .build();

        AdminRole saved = adminRoleRepository.save(role);
        log.info("Đã tạo vai trò mới: {}", request.getRoleCode());
        return toRoleResponse(saved);
    }

    /**
     * Cập nhật vai trò
     */
    @Transactional
    public AdminRoleResponse updateRole(Long id, AdminRoleRequest request) {
        AdminRole role = adminRoleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò"));

        // Không cho phép sửa SUPER_ADMIN
        if ("SUPER_ADMIN".equals(role.getRoleCode())) {
            throw new RuntimeException("Không thể sửa vai trò Super Admin");
        }

        role.setRoleName(request.getRoleName());
        role.setDescription(request.getDescription());
        role.setColor(request.getColor());
        role.setDisplayOrder(request.getDisplayOrder());
        role.setPermissions(getPermissionsByCode(request.getPermissionCodes()));

        AdminRole saved = adminRoleRepository.save(role);
        log.info("Đã cập nhật vai trò: {}", role.getRoleCode());
        return toRoleResponse(saved);
    }

    /**
     * Xóa vai trò (soft delete)
     */
    @Transactional
    public void deleteRole(Long id) {
        AdminRole role = adminRoleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò"));

        // Không cho phép xóa SUPER_ADMIN
        if ("SUPER_ADMIN".equals(role.getRoleCode())) {
            throw new RuntimeException("Không thể xóa vai trò Super Admin");
        }

        // Kiểm tra có admin nào đang dùng vai trò này không
        List<AdminUser> adminsWithRole = adminUserRepository.findByAdminRoleIdAndIsActiveTrue(id);
        if (!adminsWithRole.isEmpty()) {
            throw new RuntimeException("Không thể xóa vai trò đang được sử dụng bởi " + adminsWithRole.size() + " admin");
        }

        role.setIsActive(false);
        adminRoleRepository.save(role);
        log.info("Đã xóa vai trò: {}", role.getRoleCode());
    }

    // ==================== PERMISSION MANAGEMENT ====================

    /**
     * Lấy tất cả quyền
     */
    @Transactional(readOnly = true)
    public List<PermissionResponse> getAllPermissions() {
        return permissionRepository.findAllByOrderByPermissionGroupAscDisplayOrderAsc()
                .stream()
                .map(this::toPermissionResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy quyền theo nhóm
     */
    @Transactional(readOnly = true)
    public Map<String, List<PermissionResponse>> getPermissionsByGroup() {
        List<Permission> permissions = permissionRepository.findAllByOrderByPermissionGroupAscDisplayOrderAsc();
        return permissions.stream()
                .map(this::toPermissionResponse)
                .collect(Collectors.groupingBy(PermissionResponse::getPermissionGroup));
    }

    // ==================== ADMIN USER MANAGEMENT ====================

    /**
     * Lấy danh sách admin users
     */
    @Transactional(readOnly = true)
    public List<AdminUserRoleResponse> getAllAdminUsers() {
        return adminUserRepository.findByIsActiveTrueOrderByCreatedAtDesc()
                .stream()
                .map(this::toAdminUserResponse)
                .collect(Collectors.toList());
    }

    /**
     * Gán vai trò cho user
     */
    @Transactional
    public AdminUserRoleResponse assignRoleToUser(Long userId, String roleCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        AdminRole role = adminRoleRepository.findByRoleCode(roleCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò: " + roleCode));

        // Cập nhật role trong User entity
        user.setRole(Role.ADMIN);
        userRepository.save(user);

        // Tạo hoặc cập nhật AdminUser
        AdminUser adminUser = adminUserRepository.findByUserId(userId)
                .orElse(AdminUser.builder()
                        .user(user)
                        .displayName(user.getFullName())
                        .isActive(true)
                        .build());

        adminUser.setAdminRole(role);
        AdminUser saved = adminUserRepository.save(adminUser);

        log.info("Đã gán vai trò {} cho user {}", roleCode, user.getUsername());
        return toAdminUserResponse(saved);
    }

    /**
     * Kiểm tra user có quyền hay không
     */
    @Transactional(readOnly = true)
    public boolean hasPermission(String username, String permissionCode) {
        AdminUser adminUser = adminUserRepository.findByUsername(username).orElse(null);
        if (adminUser == null) return false;
        return adminUser.hasPermission(permissionCode);
    }

    /**
     * Lấy danh sách quyền của user
     */
    @Transactional(readOnly = true)
    public List<String> getUserPermissions(String username) {
        AdminUser adminUser = adminUserRepository.findByUsername(username).orElse(null);
        if (adminUser == null || adminUser.getAdminRole() == null) {
            return Collections.emptyList();
        }

        // Super Admin có tất cả quyền
        if (adminUser.hasPermission(Permission.ALL)) {
            return permissionRepository.findAll().stream()
                    .map(Permission::getPermissionCode)
                    .collect(Collectors.toList());
        }

        return adminUser.getAdminRole().getPermissions().stream()
                .map(Permission::getPermissionCode)
                .collect(Collectors.toList());
    }

    // ==================== HELPER METHODS ====================

    private Set<Permission> getPermissionsByCode(List<String> codes) {
        Set<Permission> permissions = new HashSet<>();
        for (String code : codes) {
            permissionRepository.findByPermissionCode(code).ifPresent(permissions::add);
        }
        return permissions;
    }

    private AdminRoleResponse toRoleResponse(AdminRole role) {
        long adminCount = adminUserRepository.findByAdminRoleIdAndIsActiveTrue(role.getId()).size();

        return AdminRoleResponse.builder()
                .id(role.getId())
                .roleCode(role.getRoleCode())
                .roleName(role.getRoleName())
                .description(role.getDescription())
                .color(role.getColor())
                .displayOrder(role.getDisplayOrder())
                .isActive(role.getIsActive())
                .permissionCodes(role.getPermissions().stream()
                        .map(Permission::getPermissionCode)
                        .collect(Collectors.toList()))
                .permissionNames(role.getPermissions().stream()
                        .map(Permission::getPermissionName)
                        .collect(Collectors.toList()))
                .adminCount(adminCount)
                .build();
    }

    private PermissionResponse toPermissionResponse(Permission permission) {
        return PermissionResponse.builder()
                .id(permission.getId())
                .permissionCode(permission.getPermissionCode())
                .permissionName(permission.getPermissionName())
                .permissionGroup(permission.getPermissionGroup())
                .description(permission.getDescription())
                .build();
    }

    private AdminUserRoleResponse toAdminUserResponse(AdminUser adminUser) {
        AdminRole role = adminUser.getAdminRole();
        User user = adminUser.getUser();

        return AdminUserRoleResponse.builder()
                .id(adminUser.getId())
                .userId(user.getId())
                .email(user.getEmail())
                .displayName(adminUser.getDisplayName() != null ? adminUser.getDisplayName() : user.getFullName())
                .roleCode(role != null ? role.getRoleCode() : null)
                .roleName(role != null ? role.getRoleName() : null)
                .roleColor(role != null ? role.getColor() : null)
                .permissions(role != null ? role.getPermissions().stream()
                        .map(Permission::getPermissionCode)
                        .collect(Collectors.toList()) : Collections.emptyList())
                .permissionNames(role != null ? role.getPermissions().stream()
                        .map(Permission::getPermissionName)
                        .collect(Collectors.toList()) : Collections.emptyList())
                .lastActivityAt(adminUser.getLastActivityAt())
                .isActive(adminUser.getIsActive())
                .createdAt(adminUser.getCreatedAt())
                .build();
    }

    /**
     * Xóa admin user (thu hồi quyền admin)
     */
    @Transactional
    public void removeAdminUser(Long adminUserId, String currentUsername) {
        AdminUser adminUser = adminUserRepository.findById(adminUserId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy admin user"));

        // Không cho phép xóa chính mình
        if (adminUser.getUser().getUsername().equals(currentUsername)) {
            throw new RuntimeException("Không thể xóa quyền admin của chính bạn");
        }

        // Không cho phép xóa SUPER_ADMIN (trừ khi người xóa cũng là SUPER_ADMIN)
        AdminUser currentAdmin = adminUserRepository.findByUser_Username(currentUsername)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin admin hiện tại"));
        
        if (adminUser.getAdminRole().getRoleCode().equals("SUPER_ADMIN") 
            && !currentAdmin.getAdminRole().getRoleCode().equals("SUPER_ADMIN")) {
            throw new RuntimeException("Chỉ SUPER_ADMIN mới có thể xóa quyền của SUPER_ADMIN khác");
        }

        // Xóa admin user
        adminUserRepository.delete(adminUser);
        log.info("Admin {} đã thu hồi quyền admin của user ID: {}", currentUsername, adminUser.getUser().getId());
    }
