package com.example.backend.controller;

import com.example.backend.dto.PageResponse;
import com.example.backend.dto.TransactionFilter;
import com.example.backend.dto.TransactionRequest;
import com.example.backend.dto.TransactionResponse;
import com.example.backend.service.TransactionService;
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

/**
 * Controller quản lý Giao dịch (Transactions)
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "💳 Transactions", description = "API quản lý giao dịch thu chi. Hỗ trợ CRUD, filter, pagination và sắp xếp.")
public class TransactionController {

    // Service xử lý business logic cho transactions
    private final TransactionService transactionService;

    @Operation(
        summary = "Tạo giao dịch mới",
        description = """
            Tạo giao dịch thủ công (Manual Entry).
            
            **Lưu ý:**
            - Nếu không có `categoryId`, hệ thống sẽ tự động phân loại bằng AI
            - `type` phải là `INCOME` hoặc `EXPENSE`
            - `amount` phải là số dương
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Tạo giao dịch thành công"),
        @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody TransactionRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        TransactionResponse response = transactionService.createTransaction(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
        summary = "Lấy danh sách giao dịch",
        description = """
            Lấy danh sách giao dịch với các bộ lọc, phân trang và sắp xếp.
            
            **Ví dụ:**
            - Lấy chi tiêu tháng 11: `?type=EXPENSE&startDate=2025-11-01&endDate=2025-11-30`
            - Lọc theo ngân hàng: `?transactionSource=VCB`
            - Tìm kiếm: `?keyword=cafe`
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công"),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @GetMapping
    public ResponseEntity<PageResponse<TransactionResponse>> getTransactions(
            @Parameter(description = "Nguồn giao dịch: VCB, TCB, MOMO...") @RequestParam(required = false) String transactionSource,
            @Parameter(description = "ID danh mục") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Loại: INCOME hoặc EXPENSE") @RequestParam(required = false) String type,
            @Parameter(description = "Từ ngày (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Đến ngày (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Tên cửa hàng/merchant") @RequestParam(required = false) String merchant,
            @Parameter(description = "Giao dịch tự động (từ SMS)") @RequestParam(required = false) Boolean isAuto,
            @Parameter(description = "Đã xác minh") @RequestParam(required = false) Boolean isVerified,
            @Parameter(description = "Từ khóa tìm kiếm") @RequestParam(required = false) String keyword,
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số items mỗi trang") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sắp xếp theo field") @RequestParam(defaultValue = "transactionDate") String sortBy,
            @Parameter(description = "Hướng sắp xếp: ASC hoặc DESC") @RequestParam(defaultValue = "DESC") String sortDirection,
            Authentication authentication) {

        // Build filter object từ request params
        TransactionFilter filter = new TransactionFilter();
        filter.setTransactionSource(transactionSource);
        filter.setCategoryId(categoryId);
        filter.setType(type);
        filter.setStartDate(startDate != null ? startDate.atStartOfDay() : null);
        filter.setEndDate(endDate != null ? endDate.atTime(23, 59, 59) : null);
        filter.setMerchant(merchant);
        filter.setIsAuto(isAuto);
        filter.setIsVerified(isVerified);
        filter.setKeyword(keyword);

        String username = authentication.getName();
        // Service sẽ dùng JPA Criteria API để build dynamic query
        PageResponse<TransactionResponse> response = transactionService.getTransactions(
                username, filter, page, size, sortBy, sortDirection);

        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Lấy chi tiết giao dịch",
        description = "Lấy thông tin chi tiết của một giao dịch theo ID."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy giao dịch"),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransactionById(
            @Parameter(description = "ID giao dịch") @PathVariable Long id,
            Authentication authentication) {
        String username = authentication.getName();
        TransactionResponse response = transactionService.getTransactionById(id, username);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Cập nhật giao dịch",
        description = """
            Cập nhật thông tin giao dịch.
            
            **Use case:**
            - Sửa category nếu AI phân loại sai
            - Sửa description/notes
            - Sửa amount nếu nhập sai
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
        @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy giao dịch"),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @Parameter(description = "ID giao dịch") @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        TransactionResponse response = transactionService.updateTransaction(id, request, username);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Xóa giao dịch",
        description = "Xóa giao dịch khỏi hệ thống (hard delete)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Xóa thành công"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy giao dịch"),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(
            @Parameter(description = "ID giao dịch") @PathVariable Long id,
            Authentication authentication) {
        String username = authentication.getName();
        transactionService.deleteTransaction(id, username);
        return ResponseEntity.noContent().build();
    }
}
