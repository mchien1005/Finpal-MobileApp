package com.example.backend.service;

import com.example.backend.dto.AdminUserFullDetailResponse;
import com.example.backend.model.*;
import com.example.backend.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service quản lý lịch sử hoạt động Admin
 * Chức năng: Ghi log, xem lịch sử, thống kê
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AdminActivityService {

    private final AdminActivityLogRepository activityLogRepository;
    private final AdminUserRepository adminUserRepository;
    private final LoginHistoryRepository loginHistoryRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Ghi log hoạt động của admin
     */
    @Transactional
    public void logActivity(String username, String action, String entityType, Long entityId, 
                           String description, String oldData, String newData) {
        try {
            AdminUser adminUser = adminUserRepository.findByUsername(username).orElse(null);
            if (adminUser == null) {
                log.warn("Không tìm thấy AdminUser cho username: {}", username);
                return;
            }

            String ipAddress = getClientIpAddress();
            String userAgent = getUserAgent();

            AdminActivityLog activityLog = AdminActivityLog.builder()
                    .adminUser(adminUser)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .description(description)
                    .oldData(oldData)
                    .newData(newData)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .timestamp(LocalDateTime.now())
                    .status("SUCCESS")
                    .build();

            activityLogRepository.save(activityLog);

            // Cập nhật thời gian hoạt động cuối của admin
            adminUser.setLastActivityAt(LocalDateTime.now());
            adminUserRepository.save(adminUser);

            log.info("📝 Admin {} - {} {} (ID: {})", username, action, entityType, entityId);
        } catch (Exception e) {
            log.error("Lỗi khi ghi log hoạt động: {}", e.getMessage());
        }
    }

    /**
     * Ghi log đơn giản (không có old/new data)
     */
    @Transactional
    public void logActivity(String username, String action, String entityType, String description) {
        logActivity(username, action, entityType, null, description, null, null);
    }

    /**
     * Lấy chi tiết đầy đủ của Admin User
     */
    @Transactional(readOnly = true)
    public AdminUserFullDetailResponse getAdminFullDetail(Long adminUserId) {
        AdminUser adminUser = adminUserRepository.findById(adminUserId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Admin User"));

        User user = adminUser.getUser();
        AdminRole role = adminUser.getAdminRole();

        // Lấy lịch sử hoạt động gần đây
        List<AdminActivityLog> recentLogs = activityLogRepository
                .findTop10ByAdminUserIdOrderByTimestampDesc(adminUserId);

        // Đếm tổng số hoạt động
        Long totalActions = activityLogRepository.countByAdminUserId(adminUserId);

        // Lấy lần đăng nhập cuối
        LocalDateTime lastLoginAt = user.getLastLoginAt();

        return AdminUserFullDetailResponse.builder()
                .id(adminUser.getId())
                .userId(user.getId())
                .email(user.getEmail())
                .displayName(adminUser.getDisplayName() != null ? adminUser.getDisplayName() : user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .roleCode(role != null ? role.getRoleCode() : null)
                .roleName(role != null ? role.getRoleName() : null)
                .roleColor(role != null ? role.getColor() : null)
                .isActive(adminUser.getIsActive())
                .permissions(role != null ? role.getPermissions().stream()
                        .map(Permission::getPermissionCode)
                        .collect(Collectors.toList()) : Collections.emptyList())
                .permissionNames(role != null ? role.getPermissions().stream()
                        .map(Permission::getPermissionName)
                        .collect(Collectors.toList()) : Collections.emptyList())
                .lastActivityAt(adminUser.getLastActivityAt())
                .lastActivityText(formatTimeAgo(adminUser.getLastActivityAt()))
                .lastLoginAt(lastLoginAt)
                .createdAt(adminUser.getCreatedAt())
                .createdBy("System Admin") // TODO: lưu người tạo
                .totalActions(totalActions)
                .recentActivities(recentLogs.stream()
                        .map(this::toActivityLogItem)
                        .collect(Collectors.toList()))
                .build();
    }

    /**
     * Lấy danh sách hoạt động gần đây (cho Audit Log)
     */
    @Transactional(readOnly = true)
    public List<AdminUserFullDetailResponse.ActivityLogItem> getRecentActivities(int limit) {
        return activityLogRepository.findTop100ByOrderByTimestampDesc().stream()
                .limit(limit)
                .map(this::toActivityLogItem)
                .collect(Collectors.toList());
    }

    /**
     * Convert AdminActivityLog to ActivityLogItem
     */
    private AdminUserFullDetailResponse.ActivityLogItem toActivityLogItem(AdminActivityLog log) {
        return AdminUserFullDetailResponse.ActivityLogItem.builder()
                .id(log.getId())
                .description(log.getDescription())
                .action(log.getAction())
                .entityType(log.getEntityType())
                .timestamp(log.getTimestamp())
                .timestampText(log.getTimestamp() != null ? log.getTimestamp().format(DATE_FORMATTER) : null)
                .ipAddress(log.getIpAddress())
                .status(log.getStatus())
                .adminName(log.getAdminUser() != null ? log.getAdminUser().getUsername() : null)
                .build();
    }

    /**
     * Format thời gian thành dạng "X phút trước", "X giờ trước"
     */
    private String formatTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) return "Chưa có hoạt động";

        Duration duration = Duration.between(dateTime, LocalDateTime.now());
        long minutes = duration.toMinutes();
        long hours = duration.toHours();
        long days = duration.toDays();

        if (minutes < 1) return "Vừa xong";
        if (minutes < 60) return minutes + " phút trước";
        if (hours < 24) return hours + " giờ trước";
        if (days < 7) return days + " ngày trước";
        
        return dateTime.format(DATE_FORMATTER);
    }

    /**
     * Lấy IP của client
     */
    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.debug("Không thể lấy IP address: {}", e.getMessage());
        }
        return "unknown";
    }

    /**
     * Lấy User-Agent từ request
     */
    private String getUserAgent() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                return attrs.getRequest().getHeader("User-Agent");
            }
        } catch (Exception e) {
            log.debug("Không thể lấy User-Agent: {}", e.getMessage());
        }
        return "";
    }
}
