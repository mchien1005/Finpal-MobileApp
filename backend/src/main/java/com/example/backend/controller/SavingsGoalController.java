package com.example.backend.controller;

import com.example.backend.dto.*;
import com.example.backend.service.SavingsGoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Savings Goals Management Controller (FR3.3)
 */
@RestController
@RequestMapping("/api/savings-goals")
@RequiredArgsConstructor
public class SavingsGoalController {

    private final SavingsGoalService savingsGoalService;

    /**
     * GET /api/savings-goals - Lấy tất cả savings goals
     * Query params: status (ACTIVE, COMPLETED, CANCELLED)
     */
    @GetMapping
    public ResponseEntity<List<SavingsGoalResponse>> getAllSavingsGoals(
            Authentication authentication,
            @RequestParam(required = false) String status) {
        String username = authentication.getName();
        List<SavingsGoalResponse> goals = savingsGoalService.getAllSavingsGoals(username, status);
        return ResponseEntity.ok(goals);
    }

    /**
     * GET /api/savings-goals/{id} - Lấy chi tiết savings goal
     */
    @GetMapping("/{id}")
    public ResponseEntity<SavingsGoalResponse> getSavingsGoalById(
            Authentication authentication,
            @PathVariable Long id) {
        String username = authentication.getName();
        SavingsGoalResponse goal = savingsGoalService.getSavingsGoalById(username, id);
        return ResponseEntity.ok(goal);
    }

    /**
     * POST /api/savings-goals - Tạo savings goal mới
     */
    @PostMapping
    public ResponseEntity<SavingsGoalResponse> createSavingsGoal(
            Authentication authentication,
            @Valid @RequestBody SavingsGoalRequest request) {
        String username = authentication.getName();
        SavingsGoalResponse goal = savingsGoalService.createSavingsGoal(username, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(goal);
    }

    /**
     * PUT /api/savings-goals/{id} - Cập nhật savings goal
     */
    @PutMapping("/{id}")
    public ResponseEntity<SavingsGoalResponse> updateSavingsGoal(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody SavingsGoalRequest request) {
        String username = authentication.getName();
        SavingsGoalResponse goal = savingsGoalService.updateSavingsGoal(username, id, request);
        return ResponseEntity.ok(goal);
    }

    /**
     * DELETE /api/savings-goals/{id} - Xóa savings goal
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSavingsGoal(
            Authentication authentication,
            @PathVariable Long id) {
        String username = authentication.getName();
        savingsGoalService.deleteSavingsGoal(username, id);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/savings-goals/{id}/contributions - Đóng góp vào savings goal
     */
    @PostMapping("/{id}/contributions")
    public ResponseEntity<SavingsGoalResponse> addContribution(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody SavingsContributionRequest request) {
        String username = authentication.getName();
        SavingsGoalResponse goal = savingsGoalService.addContribution(username, id, request);
        return ResponseEntity.ok(goal);
    }

    /**
     * GET /api/savings-goals/{id}/contributions - Lấy danh sách contributions
     */
    @GetMapping("/{id}/contributions")
    public ResponseEntity<List<SavingsContributionResponse>> getContributions(
            Authentication authentication,
            @PathVariable Long id) {
        String username = authentication.getName();
        List<SavingsContributionResponse> contributions = savingsGoalService.getContributions(username, id);
        return ResponseEntity.ok(contributions);
    }
}
