package com.example.backend.controller;

import com.example.backend.dto.UpdateProfileRequest;
import com.example.backend.dto.UserProfileResponse;
import com.example.backend.model.User;
import com.example.backend.service.AuthService;
import com.example.backend.service.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "User", description = "API quản lý thông tin người dùng")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final AuthService authService;
    private final FileUploadService fileUploadService;

    /**
     * Lấy thông tin profile của user hiện tại
     */
    @GetMapping("/profile")
    @Operation(summary = "Lấy thông tin profile", description = "Lấy thông tin chi tiết của người dùng đang đăng nhập")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal User currentUser) {
        try {
            User user = authService.getUserProfile(currentUser.getId());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", UserProfileResponse.fromUser(user)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    /**
     * Cập nhật thông tin profile của user hiện tại
     */
    @PutMapping("/profile")
    @Operation(summary = "Cập nhật thông tin profile", description = "Cập nhật họ tên, email, số điện thoại, ngày sinh, giới tính, ảnh đại diện")
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody UpdateProfileRequest request) {
        try {
            User updatedUser = authService.updateProfile(
                    currentUser.getId(),
                    request.getFullName(),
                    request.getEmail(),
                    request.getPhone(),
                    request.getDateOfBirth(),
                    request.getGender(),
                    request.getAvatarUrl());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Cập nhật thông tin thành công",
                    "data", UserProfileResponse.fromUser(updatedUser)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    /**
     * Upload avatar cho user hiện tại
     */
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload ảnh đại diện", description = "Upload ảnh đại diện mới cho người dùng")
    public ResponseEntity<?> uploadAvatar(
            @AuthenticationPrincipal User currentUser,
            @RequestParam("file") MultipartFile file) {
        try {
            // Get current user's avatar to delete later
            User user = authService.getUserProfile(currentUser.getId());
            String oldAvatarUrl = user.getAvatarUrl();

            // Upload new avatar
            String avatarUrl = fileUploadService.uploadAvatar(file, currentUser.getId());

            // Update user profile with new avatar URL
            User updatedUser = authService.updateProfile(
                    currentUser.getId(),
                    null, // don't change fullName
                    null, // don't change email
                    null, // don't change phone
                    null, // don't change dateOfBirth
                    null, // don't change gender
                    avatarUrl);

            // Delete old avatar if exists
            if (oldAvatarUrl != null && !oldAvatarUrl.isEmpty()) {
                fileUploadService.deleteAvatar(oldAvatarUrl);
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Upload ảnh đại diện thành công",
                    "avatarUrl", avatarUrl,
                    "data", UserProfileResponse.fromUser(updatedUser)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }
}
