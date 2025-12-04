package com.example.backend.controller;

import com.example.backend.dto.NotificationTemplateDTO;
import com.example.backend.model.NotificationTemplate.TemplateStatus;
import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.NotificationTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin Controller - Quản lý Notification Templates
 */
@RestController
@RequestMapping("/api/admin/notification-templates")
@RequiredArgsConstructor
@Tag(name = "Admin - Notification Templates", description = "Quản lý mẫu thông báo (Admin only)")
@PreAuthorize("hasRole('ADMIN')")
public class AdminNotificationTemplateController {

    private final NotificationTemplateService templateService;
    private final UserRepository userRepository;

    /**
     * GET /api/admin/notification-templates - Lấy tất cả templates
     */
    @GetMapping
    @Operation(summary = "Lấy tất cả notification templates")
    public ResponseEntity<List<NotificationTemplateDTO>> getAllTemplates(
            @RequestParam(required = false) TemplateStatus status) {
        List<NotificationTemplateDTO> templates;
        if (status != null) {
            templates = templateService.getTemplatesByStatus(status);
        } else {
            templates = templateService.getAllTemplates();
        }
        return ResponseEntity.ok(templates);
    }

    /**
     * GET /api/admin/notification-templates/{id} - Lấy template theo ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Lấy notification template theo ID")
    public ResponseEntity<NotificationTemplateDTO> getTemplateById(@PathVariable Long id) {
        return ResponseEntity.ok(templateService.getTemplateById(id));
    }

    /**
     * POST /api/admin/notification-templates - Tạo template mới
     */
    @PostMapping
    @Operation(summary = "Tạo notification template mới")
    public ResponseEntity<NotificationTemplateDTO> createTemplate(
            @RequestBody NotificationTemplateDTO.CreateRequest request,
            Authentication authentication) {
        Long adminId = getAdminId(authentication);
        return ResponseEntity.ok(templateService.createTemplate(request, adminId));
    }

    /**
     * PUT /api/admin/notification-templates/{id} - Cập nhật template
     */
    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật notification template")
    public ResponseEntity<NotificationTemplateDTO> updateTemplate(
            @PathVariable Long id,
            @RequestBody NotificationTemplateDTO.UpdateRequest request) {
        return ResponseEntity.ok(templateService.updateTemplate(id, request));
    }

    /**
     * DELETE /api/admin/notification-templates/{id} - Xóa template
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa notification template")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        templateService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/admin/notification-templates/send - Gửi notification từ template
     */
    @PostMapping("/send")
    @Operation(summary = "Gửi notification cho users từ template")
    public ResponseEntity<Map<String, Object>> sendNotification(
            @RequestBody NotificationTemplateDTO.SendRequest request,
            Authentication authentication) {
        Long adminId = getAdminId(authentication);
        int sentCount = templateService.sendNotificationFromTemplate(request, adminId);
        return ResponseEntity.ok(Map.of(
                "message", "Notifications sent successfully",
                "sentCount", sentCount));
    }

    /**
     * GET /api/admin/notification-templates/statistics - Lấy thống kê
     */
    @GetMapping("/statistics")
    @Operation(summary = "Lấy thống kê notification templates")
    public ResponseEntity<Map<String, Long>> getStatistics() {
        return ResponseEntity.ok(templateService.getStatistics());
    }

    private Long getAdminId(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
}
