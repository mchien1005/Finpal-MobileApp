package com.example.backend.controller;

import com.example.backend.dto.UserPreferencesDTO;
import com.example.backend.service.UserPreferencesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user/preferences")
@RequiredArgsConstructor
@Tag(name = "User Preferences", description = "API quản lý cài đặt người dùng")
@SecurityRequirement(name = "bearerAuth")
public class UserPreferencesController {

    private final UserPreferencesService userPreferencesService;

    /**
     * GET /api/user/preferences - Lấy tất cả preferences của user
     */
    @GetMapping
    @Operation(summary = "Lấy cài đặt người dùng", description = "Lấy tất cả cài đặt của người dùng hiện tại")
    public ResponseEntity<?> getPreferences(Authentication authentication) {
        try {
            String username = authentication.getName();
            UserPreferencesDTO preferences = userPreferencesService.getUserPreferences(username);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", preferences));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    /**
     * PUT /api/user/preferences - Cập nhật tất cả preferences
     */
    @PutMapping
    @Operation(summary = "Cập nhật cài đặt người dùng", description = "Cập nhật tất cả cài đặt của người dùng")
    public ResponseEntity<?> updatePreferences(
            Authentication authentication,
            @RequestBody UserPreferencesDTO request) {
        try {
            String username = authentication.getName();
            UserPreferencesDTO updated = userPreferencesService.updateUserPreferences(username, request);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Cập nhật cài đặt thành công",
                    "data", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    /**
     * PUT /api/user/preferences/notifications - Cập nhật cài đặt thông báo
     */
    @PutMapping("/notifications")
    @Operation(summary = "Cập nhật cài đặt thông báo", description = "Cập nhật các cài đặt liên quan đến thông báo")
    public ResponseEntity<?> updateNotificationSettings(
            Authentication authentication,
            @RequestBody UserPreferencesDTO request) {
        try {
            String username = authentication.getName();
            UserPreferencesDTO updated = userPreferencesService.updateNotificationSettings(username, request);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Cập nhật cài đặt thông báo thành công",
                    "data", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }
}
