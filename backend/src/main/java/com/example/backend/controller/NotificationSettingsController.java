package com.example.backend.controller;

import com.example.backend.dto.NotificationSettingsDTO;
import com.example.backend.service.NotificationSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller quản lý cài đặt thông báo của người dùng
 */
@RestController
@RequestMapping("/api/notifications/settings")
@RequiredArgsConstructor
@Tag(name = "🔔 Notification Settings", description = "API quản lý cài đặt bật/tắt thông báo của người dùng")
public class NotificationSettingsController {

    private final NotificationSettingsService settingsService;

    @Operation(summary = "Lấy cài đặt thông báo", description = """
            Lấy cài đặt thông báo hiện tại của user.
            Nếu user chưa có cài đặt, sẽ trả về cài đặt mặc định (tất cả đều bật).
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @GetMapping
    public ResponseEntity<NotificationSettingsDTO> getSettings(Authentication authentication) {
        String username = authentication.getName();
        NotificationSettingsDTO settings = settingsService.getSettings(username);
        return ResponseEntity.ok(settings);
    }

    @Operation(summary = "Cập nhật cài đặt thông báo", description = """
            Cập nhật cài đặt thông báo của user.
            Chỉ cần gửi các field cần thay đổi, các field null sẽ giữ nguyên.

            **Các loại thông báo:**
            - `pushEnabled`: Toggle tổng - bật/tắt tất cả thông báo
            - `transactionAlerts`: Cảnh báo giao dịch mới
            - `budgetAlerts`: Cảnh báo vượt ngân sách
            - `goalReminders`: Nhắc nhở mục tiêu tiết kiệm
            - `securityAlerts`: Cảnh báo bảo mật (hoạt động bất thường)
            - `weeklyReport`: Báo cáo chi tiêu hàng tuần
            - `monthlyReport`: Báo cáo chi tiêu hàng tháng
            - `savingsTips`: Gợi ý tiết kiệm thông minh
            - `spendingInsights`: Phân tích chi tiêu
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @PutMapping
    public ResponseEntity<NotificationSettingsDTO> updateSettings(
            @RequestBody NotificationSettingsDTO dto,
            Authentication authentication) {
        String username = authentication.getName();
        NotificationSettingsDTO updated = settingsService.updateSettings(username, dto);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Reset cài đặt về mặc định", description = "Reset tất cả cài đặt thông báo về mặc định (tất cả đều bật)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reset thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @PostMapping("/reset")
    public ResponseEntity<NotificationSettingsDTO> resetSettings(Authentication authentication) {
        String username = authentication.getName();
        NotificationSettingsDTO defaultSettings = NotificationSettingsDTO.getDefault();
        NotificationSettingsDTO updated = settingsService.updateSettings(username, defaultSettings);
        return ResponseEntity.ok(updated);
    }
}
