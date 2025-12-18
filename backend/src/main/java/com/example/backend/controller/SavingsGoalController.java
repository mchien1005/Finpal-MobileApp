package com.example.backend.controller;

import com.example.backend.dto.*;
import com.example.backend.service.SavingsGoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller quản lý Mục tiêu tiết kiệm (Savings Goals) - FR3.3
 */
@RestController
@RequestMapping("/api/savings-goals")
@RequiredArgsConstructor
@Tag(name = "🎯 Savings Goals", description = "API quản lý mục tiêu tiết kiệm (hũ tiết kiệm). Đặt mục tiêu và theo dõi tiến độ.")
public class SavingsGoalController {

    private final SavingsGoalService savingsGoalService;

    @Operation(
        summary = "Lấy danh sách mục tiêu",
        description = "Lấy tất cả mục tiêu tiết kiệm. Có thể lọc theo trạng thái."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công"),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @GetMapping
    public ResponseEntity<List<SavingsGoalResponse>> getAllSavingsGoals(
            Authentication authentication,
            @Parameter(description = "Trạng thái: ACTIVE, COMPLETED, CANCELLED")
            @RequestParam(required = false) String status) {
        String username = authentication.getName();
        List<SavingsGoalResponse> goals = savingsGoalService.getAllSavingsGoals(username, status);
        return ResponseEntity.ok(goals);
    }

    @Operation(
        summary = "Lấy chi tiết mục tiêu",
        description = "Lấy thông tin chi tiết của một mục tiêu tiết kiệm bao gồm tiến độ."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy mục tiêu"),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @GetMapping("/{id}")
    public ResponseEntity<SavingsGoalResponse> getSavingsGoalById(
            Authentication authentication,
            @Parameter(description = "ID mục tiêu") @PathVariable Long id) {
        String username = authentication.getName();
        SavingsGoalResponse goal = savingsGoalService.getSavingsGoalById(username, id);
        return ResponseEntity.ok(goal);
    }

    @Operation(
        summary = "Tạo mục tiêu mới",
        description = """
            Tạo mục tiêu tiết kiệm mới (hũ tiết kiệm).
            
            **Ví dụ:**
            - Mua iPhone: 25,000,000 VND - deadline 6 tháng
            - Du lịch Nhật: 50,000,000 VND - deadline 1 năm
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Tạo thành công"),
        @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @PostMapping
    public ResponseEntity<SavingsGoalResponse> createSavingsGoal(
            Authentication authentication,
            @Valid @RequestBody SavingsGoalRequest request) {
        String username = authentication.getName();
        SavingsGoalResponse goal = savingsGoalService.createSavingsGoal(username, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(goal);
    }

    @Operation(
        summary = "Cập nhật mục tiêu",
        description = "Cập nhật thông tin mục tiêu tiết kiệm (tên, số tiền, deadline...)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
        @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy mục tiêu"),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @PutMapping("/{id}")
    public ResponseEntity<SavingsGoalResponse> updateSavingsGoal(
            Authentication authentication,
            @Parameter(description = "ID mục tiêu") @PathVariable Long id,
            @Valid @RequestBody SavingsGoalRequest request) {
        String username = authentication.getName();
        SavingsGoalResponse goal = savingsGoalService.updateSavingsGoal(username, id, request);
        return ResponseEntity.ok(goal);
    }

    @Operation(
        summary = "Xóa mục tiêu",
        description = "Xóa mục tiêu tiết kiệm."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Xóa thành công"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy mục tiêu"),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSavingsGoal(
            Authentication authentication,
            @Parameter(description = "ID mục tiêu") @PathVariable Long id) {
        String username = authentication.getName();
        savingsGoalService.deleteSavingsGoal(username, id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Đóng góp vào mục tiêu",
        description = "Thêm tiền vào mục tiêu tiết kiệm (nạp tiền vào hũ)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Đóng góp thành công"),
        @ApiResponse(responseCode = "400", description = "Số tiền không hợp lệ"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy mục tiêu"),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @PostMapping("/{id}/contributions")
    public ResponseEntity<SavingsGoalResponse> addContribution(
            Authentication authentication,
            @Parameter(description = "ID mục tiêu") @PathVariable Long id,
            @Valid @RequestBody SavingsContributionRequest request) {
        String username = authentication.getName();
        SavingsGoalResponse goal = savingsGoalService.addContribution(username, id, request);
        return ResponseEntity.ok(goal);
    }

    @Operation(
        summary = "Lịch sử đóng góp",
        description = "Lấy danh sách các lần đóng góp vào mục tiêu tiết kiệm."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy mục tiêu"),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @GetMapping("/{id}/contributions")
    public ResponseEntity<List<SavingsContributionResponse>> getContributions(
            Authentication authentication,
            @Parameter(description = "ID mục tiêu") @PathVariable Long id) {
        String username = authentication.getName();
        List<SavingsContributionResponse> contributions = savingsGoalService.getContributions(username, id);
        return ResponseEntity.ok(contributions);
    }
}
