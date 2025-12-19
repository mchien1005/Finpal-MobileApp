package com.example.backend.controller;

import com.example.backend.dto.SmartTip;
import com.example.backend.dto.SmartTipsResponse;
import com.example.backend.model.User;
import com.example.backend.service.AIInsightsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controller cho Smart Tips từ AI
 * Cung cấp các gợi ý/tips thông minh về quản lý tài chính
 */
@RestController
@RequestMapping("/api/tips")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Smart Tips", description = "API gợi ý thông minh từ AI")
public class SmartTipsController {

    private final AIInsightsService aiInsightsService;

    /**
     * Lấy danh sách Smart Tips cho user hiện tại
     * Tips bao gồm: gợi ý cá nhân hóa, tips theo mùa, tips chung về quản lý tài chính
     */
    @GetMapping
    @Operation(summary = "Lấy Smart Tips", description = "Lấy danh sách gợi ý thông minh từ AI cho user hiện tại")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lấy tips thành công"),
        @ApiResponse(responseCode = "503", description = "AI service không khả dụng")
    })
    public ResponseEntity<SmartTipsResponse> getSmartTips(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Số lượng tips tối đa") 
            @RequestParam(defaultValue = "5") int maxTips) {
        
        log.info("Getting smart tips for user: {}", user.getId());
        
        SmartTipsResponse tips = aiInsightsService.getSmartTips(user.getId(), maxTips);
        
        if (tips == null) {
            return ResponseEntity.status(503).build();
        }
        
        return ResponseEntity.ok(tips);
    }

    /**
     * Lấy Smart Tips cho một user cụ thể (Admin endpoint)
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Lấy Smart Tips theo User ID", description = "Lấy gợi ý thông minh cho user cụ thể (admin)")
    public ResponseEntity<SmartTipsResponse> getSmartTipsForUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "5") int maxTips) {
        
        log.info("Admin getting smart tips for user: {}", userId);
        
        SmartTipsResponse tips = aiInsightsService.getSmartTips(userId, maxTips);
        
        if (tips == null) {
            return ResponseEntity.status(503).build();
        }
        
        return ResponseEntity.ok(tips);
    }

    /**
     * Lấy Daily Tip - Tip ngẫu nhiên cho ngày hôm nay
     * Dùng để hiển thị trên màn hình chính của app
     */
    @GetMapping("/daily")
    @Operation(summary = "Lấy Daily Tip", description = "Lấy một tip ngẫu nhiên cho ngày hôm nay")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lấy tip thành công"),
        @ApiResponse(responseCode = "503", description = "AI service không khả dụng")
    })
    public ResponseEntity<SmartTip> getDailyTip() {
        log.info("Getting daily tip");
        
        SmartTip tip = aiInsightsService.getDailyTip();
        
        if (tip == null) {
            return ResponseEntity.status(503).build();
        }
        
        return ResponseEntity.ok(tip);
    }
}
