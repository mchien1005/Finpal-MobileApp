package com.example.backend.controller;

import com.example.backend.dto.BudgetRequest;
import com.example.backend.dto.BudgetResponse;
import com.example.backend.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * Controller quản lý Ngân sách (Budget Management) - FR3.2
 */
@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
@Tag(name = "💰 Budgets", description = "API quản lý ngân sách. Theo dõi chi tiêu theo kỳ hạn (tuần/tháng/quý/năm).")
public class BudgetController {

    private final BudgetService budgetService;

    @Operation(
        summary = "Lấy danh sách ngân sách",
        description = "Lấy tất cả ngân sách của user hiện tại. Có thể lọc theo trạng thái hoạt động."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công"),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @GetMapping
    public ResponseEntity<List<BudgetResponse>> getAllBudgets(
            Authentication authentication,
            @Parameter(description = "Lọc theo trạng thái: true=đang hoạt động, false=đã tắt") 
            @RequestParam(required = false) Boolean isActive) {
        String username = authentication.getName();
        List<BudgetResponse> budgets = budgetService.getAllBudgets(username, isActive);
        return ResponseEntity.ok(budgets);
    }

    @Operation(
        summary = "Lấy ngân sách đang hoạt động",
        description = "Lấy các ngân sách đang có hiệu lực cho một ngày cụ thể (mặc định là hôm nay)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công"),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @GetMapping("/active")
    public ResponseEntity<List<BudgetResponse>> getActiveBudgets(
            Authentication authentication,
            @Parameter(description = "Ngày cần kiểm tra (yyyy-MM-dd), mặc định là hôm nay")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        String username = authentication.getName();
        List<BudgetResponse> budgets = budgetService.getActiveBudgets(username, date);
        return ResponseEntity.ok(budgets);
    }

    @Operation(
        summary = "Lấy chi tiết ngân sách",
        description = "Lấy thông tin chi tiết của một ngân sách bao gồm tiến độ sử dụng."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy ngân sách"),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BudgetResponse> getBudgetById(
            Authentication authentication,
            @Parameter(description = "ID ngân sách") @PathVariable Long id) {
        String username = authentication.getName();
        BudgetResponse budget = budgetService.getBudgetById(username, id);
        return ResponseEntity.ok(budget);
    }

    @Operation(
        summary = "Tạo ngân sách mới",
        description = """
            Tạo ngân sách cho một danh mục hoặc tổng chi tiêu.
            
            **Lưu ý:**
            - `categoryId = null`: Ngân sách tổng (tất cả chi tiêu)
            - `period`: WEEKLY, MONTHLY, QUARTERLY, YEARLY
            - `alertThreshold`: Phần trăm cảnh báo (mặc định 70%)
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Tạo thành công"),
        @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @PostMapping
    public ResponseEntity<BudgetResponse> createBudget(
            Authentication authentication,
            @Valid @RequestBody BudgetRequest request) {
        String username = authentication.getName();
        BudgetResponse budget = budgetService.createBudget(username, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(budget);
    }

    @Operation(
        summary = "Cập nhật ngân sách",
        description = "Cập nhật thông tin ngân sách (số tiền, kỳ hạn, ngưỡng cảnh báo...)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
        @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy ngân sách"),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponse> updateBudget(
            Authentication authentication,
            @Parameter(description = "ID ngân sách") @PathVariable Long id,
            @Valid @RequestBody BudgetRequest request) {
        String username = authentication.getName();
        BudgetResponse budget = budgetService.updateBudget(username, id, request);
        return ResponseEntity.ok(budget);
    }

    @Operation(
        summary = "Xóa ngân sách",
        description = "Xóa ngân sách (soft delete - chỉ set isActive = false)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Xóa thành công"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy ngân sách"),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(
            Authentication authentication,
            @Parameter(description = "ID ngân sách") @PathVariable Long id) {
        String username = authentication.getName();
        budgetService.deleteBudget(username, id);
        return ResponseEntity.noContent().build();
    }
}
