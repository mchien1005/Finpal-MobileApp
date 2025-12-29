package com.example.backend.controller;

import com.example.backend.dto.SystemSettingsResponse;
import com.example.backend.dto.SystemSettingsUpdateRequest;
import com.example.backend.service.AdminSystemSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller quản lý cài đặt hệ thống (Admin only)
 * 
 * Chức năng:
 * - Xem tất cả cài đặt hệ thống
 * - Cập nhật cài đặt AI (ngưỡng confidence, tần suất huấn luyện, strategy)
 * - Cập nhật cài đặt thông báo (push, email)
 * - Cập nhật cài đặt hệ thống (maintenance mode)
 */
@RestController
@RequestMapping("/api/admin/settings")
@RequiredArgsConstructor
@Tag(name = "Admin System Settings", description = "API quản lý cài đặt hệ thống (chỉ Admin)")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSystemSettingsController {

    private final AdminSystemSettingsService settingsService;

    /**
     * Lấy tất cả cài đặt hệ thống
     */
    @GetMapping
    @Operation(summary = "Lấy tất cả cài đặt hệ thống", 
               description = "Trả về danh sách tất cả cài đặt, sắp xếp theo nhóm và key")
    public ResponseEntity<List<SystemSettingsResponse>> getAllSettings() {
        List<SystemSettingsResponse> settings = settingsService.getAllSettings();
        return ResponseEntity.ok(settings);
    }

    /**
     * Lấy cài đặt theo nhóm
     */
    @GetMapping("/group/{group}")
    @Operation(summary = "Lấy cài đặt theo nhóm", 
               description = "Lấy danh sách cài đặt theo nhóm: AI, NOTIFICATION, SYSTEM")
    public ResponseEntity<List<SystemSettingsResponse>> getSettingsByGroup(@PathVariable String group) {
        List<SystemSettingsResponse> settings = settingsService.getSettingsByGroup(group.toUpperCase());
        return ResponseEntity.ok(settings);
    }

    /**
     * Lấy tất cả cài đặt dạng key-value map
     */
    @GetMapping("/map")
    @Operation(summary = "Lấy cài đặt dạng Map", 
               description = "Trả về tất cả cài đặt dưới dạng key-value map để dễ sử dụng trong frontend")
    public ResponseEntity<Map<String, String>> getSettingsAsMap() {
        Map<String, String> settings = settingsService.getAllSettingsAsMap();
        return ResponseEntity.ok(settings);
    }

    /**
     * Cập nhật 1 cài đặt
     */
    @PutMapping
    @Operation(summary = "Cập nhật 1 cài đặt", 
               description = "Cập nhật giá trị của 1 cài đặt dựa trên key")
    public ResponseEntity<SystemSettingsResponse> updateSetting(
            @Valid @RequestBody SystemSettingsUpdateRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        SystemSettingsResponse updated = settingsService.updateSetting(request, username);
        return ResponseEntity.ok(updated);
    }

    /**
     * Cập nhật nhiều cài đặt cùng lúc
     */
    @PutMapping("/batch")
    @Operation(summary = "Cập nhật nhiều cài đặt", 
               description = "Cập nhật nhiều cài đặt cùng lúc, phù hợp khi Save form cài đặt")
    public ResponseEntity<List<SystemSettingsResponse>> updateMultipleSettings(
            @Valid @RequestBody List<SystemSettingsUpdateRequest> requests,
            Authentication authentication) {
        String username = authentication.getName();
        List<SystemSettingsResponse> updated = settingsService.updateMultipleSettings(requests, username);
        return ResponseEntity.ok(updated);
    }

    /**
     * Cập nhật cài đặt AI nhanh
     */
    @PutMapping("/ai")
    @Operation(summary = "Cập nhật cài đặt AI", 
               description = "Endpoint nhanh để cập nhật các cài đặt AI phổ biến")
    public ResponseEntity<List<SystemSettingsResponse>> updateAISettings(
            @RequestParam(required = false) Double confidenceThreshold,
            @RequestParam(required = false) String retrainingFrequency,
            @RequestParam(required = false) String strategy,
            Authentication authentication) {
        
        String username = authentication.getName();
        java.util.ArrayList<SystemSettingsUpdateRequest> requests = new java.util.ArrayList<>();

        if (confidenceThreshold != null) {
            requests.add(SystemSettingsUpdateRequest.builder()
                    .settingKey(AdminSystemSettingsService.KEY_AI_CONFIDENCE_THRESHOLD)
                    .settingValue(String.valueOf(confidenceThreshold))
                    .build());
        }
        if (retrainingFrequency != null) {
            requests.add(SystemSettingsUpdateRequest.builder()
                    .settingKey(AdminSystemSettingsService.KEY_AI_RETRAINING_FREQUENCY)
                    .settingValue(retrainingFrequency)
                    .build());
        }
        if (strategy != null) {
            requests.add(SystemSettingsUpdateRequest.builder()
                    .settingKey(AdminSystemSettingsService.KEY_AI_STRATEGY)
                    .settingValue(strategy)
                    .build());
        }

        if (requests.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<SystemSettingsResponse> updated = settingsService.updateMultipleSettings(requests, username);
        return ResponseEntity.ok(updated);
    }

    /**
     * Cập nhật cài đặt thông báo nhanh
     */
    @PutMapping("/notification")
    @Operation(summary = "Cập nhật cài đặt thông báo", 
               description = "Endpoint nhanh để bật/tắt thông báo push và email")
    public ResponseEntity<List<SystemSettingsResponse>> updateNotificationSettings(
            @RequestParam(required = false) Boolean pushEnabled,
            @RequestParam(required = false) Boolean emailEnabled,
            Authentication authentication) {
        
        String username = authentication.getName();
        java.util.ArrayList<SystemSettingsUpdateRequest> requests = new java.util.ArrayList<>();

        if (pushEnabled != null) {
            requests.add(SystemSettingsUpdateRequest.builder()
                    .settingKey(AdminSystemSettingsService.KEY_NOTIFICATION_PUSH_ENABLED)
                    .settingValue(String.valueOf(pushEnabled))
                    .build());
        }
        if (emailEnabled != null) {
            requests.add(SystemSettingsUpdateRequest.builder()
                    .settingKey(AdminSystemSettingsService.KEY_NOTIFICATION_EMAIL_ENABLED)
                    .settingValue(String.valueOf(emailEnabled))
                    .build());
        }

        if (requests.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<SystemSettingsResponse> updated = settingsService.updateMultipleSettings(requests, username);
        return ResponseEntity.ok(updated);
    }
}
