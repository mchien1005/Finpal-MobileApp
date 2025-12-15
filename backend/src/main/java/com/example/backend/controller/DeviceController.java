package com.example.backend.controller;

import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Device Controller - API quản lý thiết bị và FCM tokens
 * 
 * Cho phép mobile app:
 * - Đăng ký FCM token khi đăng nhập/mở app
 * - Cập nhật FCM token khi token được refresh
 * - Xóa FCM token khi đăng xuất
 * - Bật/tắt nhận thông báo
 */
@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Device Management", description = "APIs quản lý thiết bị và FCM tokens cho push notifications")
public class DeviceController {

    private final UserRepository userRepository;

    /**
     * Đăng ký hoặc cập nhật FCM token cho user hiện tại
     * 
     * Mobile app gọi API này mỗi khi:
     * - User đăng nhập thành công
     * - App được mở lại
     * - FCM token được refresh bởi Firebase
     */
    @PostMapping("/register-token")
    @Operation(summary = "Đăng ký FCM Token", 
               description = "Đăng ký hoặc cập nhật FCM token để nhận push notifications")
    public ResponseEntity<?> registerFcmToken(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody FcmTokenRequest request) {
        
        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String oldToken = user.getFcmToken();
            user.setFcmToken(request.getFcmToken());
            userRepository.save(user);

            if (oldToken == null || oldToken.isEmpty()) {
                log.info("✅ FCM token registered for user {}", user.getUsername());
            } else if (!oldToken.equals(request.getFcmToken())) {
                log.info("🔄 FCM token updated for user {}", user.getUsername());
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "FCM token đã được đăng ký thành công"
            ));

        } catch (Exception e) {
            log.error("❌ Failed to register FCM token: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Không thể đăng ký FCM token: " + e.getMessage()
            ));
        }
    }

    /**
     * Xóa FCM token (khi user đăng xuất)
     * 
     * Ngăn không gửi push notification đến thiết bị đã đăng xuất
     */
    @DeleteMapping("/unregister-token")
    @Operation(summary = "Xóa FCM Token", 
               description = "Xóa FCM token khi đăng xuất để ngừng nhận notifications")
    public ResponseEntity<?> unregisterFcmToken(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            user.setFcmToken(null);
            userRepository.save(user);

            log.info("✅ FCM token unregistered for user {}", user.getUsername());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "FCM token đã được xóa"
            ));

        } catch (Exception e) {
            log.error("❌ Failed to unregister FCM token: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Không thể xóa FCM token: " + e.getMessage()
            ));
        }
    }

    /**
     * Lấy trạng thái notification settings của user
     */
    @GetMapping("/notification-settings")
    @Operation(summary = "Lấy Notification Settings", 
               description = "Lấy trạng thái cài đặt thông báo của user")
    public ResponseEntity<?> getNotificationSettings(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            return ResponseEntity.ok(Map.of(
                    "notificationEnabled", user.getNotificationEnabled() != null ? user.getNotificationEnabled() : true,
                    "hasFcmToken", user.getFcmToken() != null && !user.getFcmToken().isEmpty()
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Cập nhật notification settings (bật/tắt nhận thông báo)
     */
    @PutMapping("/notification-settings")
    @Operation(summary = "Cập nhật Notification Settings", 
               description = "Bật/tắt nhận push notifications")
    public ResponseEntity<?> updateNotificationSettings(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody NotificationSettingsRequest request) {
        
        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            user.setNotificationEnabled(request.getNotificationEnabled());
            userRepository.save(user);

            String status = request.getNotificationEnabled() ? "bật" : "tắt";
            log.info("🔔 Notification {} for user {}", status, user.getUsername());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đã " + status + " thông báo",
                    "notificationEnabled", request.getNotificationEnabled()
            ));

        } catch (Exception e) {
            log.error("❌ Failed to update notification settings: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Không thể cập nhật cài đặt: " + e.getMessage()
            ));
        }
    }

    // ==================== DTOs ====================

    @Data
    public static class FcmTokenRequest {
        @NotBlank(message = "FCM token không được để trống")
        private String fcmToken;
    }

    @Data
    public static class NotificationSettingsRequest {
        private Boolean notificationEnabled = true;
    }
}
