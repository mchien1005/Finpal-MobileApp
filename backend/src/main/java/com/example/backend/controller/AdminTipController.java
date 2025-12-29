package com.example.backend.controller;

import com.example.backend.dto.TipDTO;
import com.example.backend.model.Tip.TipCategory;
import com.example.backend.model.Tip.TipStatus;
import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.TipService;
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
 * Admin Controller - Quản lý Tips & Suggestions
 */
@RestController
@RequestMapping("/api/admin/tips")
@RequiredArgsConstructor
@Tag(name = "Admin - Tips", description = "Quản lý mẹo và gợi ý (Admin only)")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTipController {

    private final TipService tipService;
    private final UserRepository userRepository;

    /**
     * GET /api/admin/tips - Lấy tất cả tips
     */
    @GetMapping
    @Operation(summary = "Lấy tất cả tips")
    public ResponseEntity<List<TipDTO>> getAllTips() {
        return ResponseEntity.ok(tipService.getAllTips());
    }

    /**
     * GET /api/admin/tips/{id} - Lấy tip theo ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Lấy tip theo ID")
    public ResponseEntity<TipDTO> getTipById(@PathVariable Long id) {
        return ResponseEntity.ok(tipService.getTipById(id));
    }

    /**
     * POST /api/admin/tips - Tạo tip mới
     */
    @PostMapping
    @Operation(summary = "Tạo tip mới")
    public ResponseEntity<TipDTO> createTip(
            @RequestBody TipDTO.CreateRequest request,
            Authentication authentication) {
        Long adminId = getAdminId(authentication);
        return ResponseEntity.ok(tipService.createTip(request, adminId));
    }

    /**
     * PUT /api/admin/tips/{id} - Cập nhật tip
     */
    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật tip")
    public ResponseEntity<TipDTO> updateTip(
            @PathVariable Long id,
            @RequestBody TipDTO.UpdateRequest request) {
        return ResponseEntity.ok(tipService.updateTip(id, request));
    }

    /**
     * DELETE /api/admin/tips/{id} - Xóa tip
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa tip")
    public ResponseEntity<Void> deleteTip(@PathVariable Long id) {
        tipService.deleteTip(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/admin/tips/statistics - Lấy thống kê
     */
    @GetMapping("/statistics")
    @Operation(summary = "Lấy thống kê tips")
    public ResponseEntity<Map<String, Long>> getStatistics() {
        return ResponseEntity.ok(tipService.getStatistics());
    }

    private Long getAdminId(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
}

/**
 * Public Controller - Tips cho Users
 */
@RestController
@RequestMapping("/api/tips")
@RequiredArgsConstructor
@Tag(name = "Tips", description = "Mẹo và gợi ý tiết kiệm cho users")
class TipController {

    private final TipService tipService;

    /**
     * GET /api/tips - Lấy tất cả tips đang active
     */
    @GetMapping
    @Operation(summary = "Lấy tất cả tips đang active")
    public ResponseEntity<List<TipDTO>> getActiveTips(
            @RequestParam(required = false) TipCategory category) {
        List<TipDTO> tips;
        if (category != null) {
            tips = tipService.getTipsByCategory(category);
        } else {
            tips = tipService.getActiveTips();
        }
        return ResponseEntity.ok(tips);
    }

    /**
     * GET /api/tips/{id} - Lấy tip và tăng view count
     */
    @GetMapping("/{id}")
    @Operation(summary = "Lấy tip theo ID")
    public ResponseEntity<TipDTO> getTipById(@PathVariable Long id) {
        tipService.incrementViewCount(id);
        return ResponseEntity.ok(tipService.getTipById(id));
    }

    /**
     * POST /api/tips/{id}/like - Like tip
     */
    @PostMapping("/{id}/like")
    @Operation(summary = "Like tip")
    public ResponseEntity<Map<String, String>> likeTip(@PathVariable Long id) {
        tipService.likeTip(id);
        return ResponseEntity.ok(Map.of("message", "Tip liked successfully"));
    }
}
