package com.example.backend.controller;

import com.example.backend.dto.BudgetRequest;
import com.example.backend.dto.BudgetResponse;
import com.example.backend.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Budget Management Controller (FR3.2)
 */
@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    /**
     * GET /api/budgets - Lấy tất cả budgets của user
     * Query params: isActive (Boolean)
     */
    @GetMapping
    public ResponseEntity<List<BudgetResponse>> getAllBudgets(
            Authentication authentication,
            @RequestParam(required = false) Boolean isActive) {
        String username = authentication.getName();
        List<BudgetResponse> budgets = budgetService.getAllBudgets(username, isActive);
        return ResponseEntity.ok(budgets);
    }

    /**
     * GET /api/budgets/active - Lấy budgets đang active cho một ngày cụ thể
     * Query params: date (LocalDate, default = today)
     */
    @GetMapping("/active")
    public ResponseEntity<List<BudgetResponse>> getActiveBudgets(
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        String username = authentication.getName();
        List<BudgetResponse> budgets = budgetService.getActiveBudgets(username, date);
        return ResponseEntity.ok(budgets);
    }

    /**
     * GET /api/budgets/{id} - Lấy chi tiết budget
     */
    @GetMapping("/{id}")
    public ResponseEntity<BudgetResponse> getBudgetById(
            Authentication authentication,
            @PathVariable Long id) {
        String username = authentication.getName();
        BudgetResponse budget = budgetService.getBudgetById(username, id);
        return ResponseEntity.ok(budget);
    }

    /**
     * POST /api/budgets - Tạo budget mới
     */
    @PostMapping
    public ResponseEntity<BudgetResponse> createBudget(
            Authentication authentication,
            @Valid @RequestBody BudgetRequest request) {
        String username = authentication.getName();
        BudgetResponse budget = budgetService.createBudget(username, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(budget);
    }

    /**
     * PUT /api/budgets/{id} - Cập nhật budget
     */
    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponse> updateBudget(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody BudgetRequest request) {
        String username = authentication.getName();
        BudgetResponse budget = budgetService.updateBudget(username, id, request);
        return ResponseEntity.ok(budget);
    }

    /**
     * DELETE /api/budgets/{id} - Xóa budget (soft delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(
            Authentication authentication,
            @PathVariable Long id) {
        String username = authentication.getName();
        budgetService.deleteBudget(username, id);
        return ResponseEntity.noContent().build();
    }
}
