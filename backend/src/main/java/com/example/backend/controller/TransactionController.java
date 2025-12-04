package com.example.backend.controller;

import com.example.backend.dto.PageResponse;
import com.example.backend.dto.TransactionFilter;
import com.example.backend.dto.TransactionRequest;
import com.example.backend.dto.TransactionResponse;
import com.example.backend.service.TransactionService;
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
 * 
 * Chức năng CRUD đầy đủ:
 * - Tạo giao dịch thủ công (manual entry)
 * - Xem danh sách giao dịch (with filter, pagination, sort)
 * - Xem chi tiết 1 giao dịch
 * - Sửa giao dịch (ví dụ: sửa category nếu AI phân loại sai)
 * - Xóa giao dịch
 * 
 * Note:
 * - Tất cả giao dịch đều được auto-categorize bằng AI
 * - User chỉ thấy giao dịch của chính mình (ownership validation)
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    // Service xử lý business logic cho transactions
    private final TransactionService transactionService;

    /**
     * API tạo giao dịch thủ công (Manual Entry)
     * 
     * @param request        - Thông tin giao dịch (transactionSource, amount, type,
     *                       merchant...)
     * @param authentication - User đang đăng nhập
     * @return TransactionResponse - Giao dịch vừa tạo
     * 
     *         HTTP 201 CREATED - Tạo thành công
     *         HTTP 400 BAD_REQUEST - Dữ liệu không hợp lệ
     * 
     *         Note: Giao dịch sẽ được tự động phân loại bằng AI nếu không có
     *         categoryId
     */
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody TransactionRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        TransactionResponse response = transactionService.createTransaction(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * API lấy danh sách giao dịch với filter, pagination và sort
     * 
     * @param transactionSource - Lọc theo nguồn giao dịch: VCB, TCB, MOMO...
     *                          (optional)
     * @param categoryId        - Lọc theo danh mục (optional)
     * @param type              - Lọc theo loại: INCOME hoặc EXPENSE (optional)
     * @param startDate         - Lọc từ ngày (optional)
     * @param endDate           - Lọc đến ngày (optional)
     * @param merchant          - Lọc theo merchant/cửa hàng (optional)
     * @param isAuto            - Lọc giao dịch tự động (từ SMS) hoặc thủ công
     *                          (optional)
     * @param isVerified        - Lọc giao dịch đã xác minh (optional)
     * @param keyword           - Tìm kiếm theo description hoặc merchant (optional)
     * @param page              - Trang số (mặc định 0)
     * @param size              - Số lượng items per page (mặc định 20)
     * @param sortBy            - Sắp xếp theo field nào (mặc định transactionDate)
     * @param sortDirection     - ASC hoặc DESC (mặc định DESC)
     * @return PageResponse chứa list transactions và thông tin pagination
     * 
     *         Ví dụ:
     *         GET
     *         /api/transactions?type=EXPENSE&startDate=2025-11-01&endDate=2025-11-30&page=0&size=20
     *         → Lấy tất cả giao dịch chi tiêu trong tháng 11/2025
     */
    @GetMapping
    public ResponseEntity<PageResponse<TransactionResponse>> getTransactions(
            @RequestParam(required = false) String transactionSource,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String merchant,
            @RequestParam(required = false) Boolean isAuto,
            @RequestParam(required = false) Boolean isVerified,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "transactionDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
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

    /**
     * API lấy chi tiết 1 giao dịch
     * 
     * @param id - Transaction ID
     * @return TransactionResponse - Thông tin chi tiết giao dịch
     * 
     *         HTTP 200 OK - Tìm thấy
     *         HTTP 404 NOT_FOUND - Không tìm thấy hoặc không có quyền xem
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransactionById(
            @PathVariable Long id,
            Authentication authentication) {
        String username = authentication.getName();
        TransactionResponse response = transactionService.getTransactionById(id, username);
        return ResponseEntity.ok(response);
    }

    /**
     * API cập nhật giao dịch
     * 
     * @param id      - Transaction ID cần update
     * @param request - Thông tin mới
     * @return TransactionResponse - Giao dịch sau khi update
     * 
     *         Use case:
     *         - Sửa category nếu AI phân loại sai
     *         - Sửa description/notes
     *         - Sửa amount nếu nhập sai
     */
    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        TransactionResponse response = transactionService.updateTransaction(id, request, username);
        return ResponseEntity.ok(response);
    }

    /**
     * API xóa giao dịch
     * 
     * @param id - Transaction ID cần xóa
     * @return HTTP 204 NO_CONTENT - Xóa thành công
     * 
     *         Note: Xóa thật (hard delete) khỏi database
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(
            @PathVariable Long id,
            Authentication authentication) {
        String username = authentication.getName();
        transactionService.deleteTransaction(id, username);
        return ResponseEntity.noContent().build();
    }
}
