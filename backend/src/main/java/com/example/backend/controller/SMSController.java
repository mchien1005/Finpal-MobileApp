package com.example.backend.controller;

import com.example.backend.dto.SMSParserRequest;
import com.example.backend.dto.TransactionResponse;
import com.example.backend.service.SMSTransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xử lý SMS từ ngân hàng (Auto Transaction)
 * 
 * Chức năng:
 * - Nhận SMS từ mobile app
 * - Parse (phân tích) nội dung SMS
 * - Tự động tạo giao dịch từ thông tin SMS
 * 
 * Flow:
 * 1. Mobile app đọc SMS ngân hàng (VCB, TCB, ACB...)
 * 2. Gửi nội dung SMS lên backend
 * 3. Backend parse thông tin (số tiền, loại giao dịch, merchant...)
 * 4. Tự động tạo transaction và phân loại bằng AI
 */
@RestController
@RequestMapping("/api/sms")
@RequiredArgsConstructor
public class SMSController {

    // Service xử lý SMS và tạo transaction
    private final SMSTransactionService smsTransactionService;

    /**
     * API nhận SMS từ ngân hàng và tự động tạo giao dịch
     * 
     * @param request        - Chứa nội dung SMS và số điện thoại người gửi
     * @param authentication - Thông tin user đang đăng nhập
     * @return TransactionResponse - Giao dịch vừa được tạo tự động
     * 
     *         HTTP 201 CREATED - Tạo transaction thành công
     *         HTTP 400 BAD_REQUEST - SMS không hợp lệ hoặc không parse được
     * 
     *         Ví dụ SMS:
     *         "TK 1234xxxx5678 -100,000 VND luc 18/11/2025 15:30. ND: GRAB..."
     *         → Tự động tạo giao dịch chi tiêu 100,000 VND cho GRAB
     */
    @PostMapping("/receive")
    public ResponseEntity<?> receiveSMS(
            @Valid @RequestBody SMSParserRequest request,
            Authentication authentication) {
        try {
            // Lấy username của user đang đăng nhập
            String username = authentication.getName();

            // Xử lý SMS: parse + tạo transaction + auto-categorize
            TransactionResponse response = smsTransactionService.processSMSTransaction(
                    request.getSmsContent(),
                    request.getSenderPhone(),
                    username);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            // Trả về lỗi nếu không parse được SMS
            return ResponseEntity.badRequest().body(
                    new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * DTO đơn giản để trả về error message
     */
    record ErrorResponse(String message) {
    }
}
