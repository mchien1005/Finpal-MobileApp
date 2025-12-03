package com.example.backend.service;

import com.example.backend.dto.ParsedSMSData;
import com.example.backend.dto.TransactionRequest;
import com.example.backend.dto.TransactionResponse;
import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service quản lý giao dịch từ SMS ngân hàng
 * Chức năng: Parse SMS → Tự động tạo giao dịch → Tự động phân loại category
 */
@Service
@RequiredArgsConstructor
public class SMSTransactionService {

    private final SMSParserService smsParserService;
    private final TransactionService transactionService;
    private final UserRepository userRepository;

    /**
     * Xử lý tạo giao dịch từ SMS ngân hàng
     * 
     * @param smsContent  Nội dung SMS (ví dụ: "TK 1234 GD +500,000 VND luc 10:30
     *                    18/11/2025")
     * @param senderPhone Số điện thoại người gửi (dùng để xác định ngân hàng)
     * @param username    Tên đăng nhập của user
     * @return TransactionResponse chứa thông tin giao dịch vừa tạo
     */
    @Transactional
    public TransactionResponse processSMSTransaction(String smsContent, String senderPhone, String username) {
        // Parse nội dung SMS
        ParsedSMSData parsedData = smsParserService.parseSMS(smsContent, senderPhone);

        // Verify user exists
        userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Xây dựng transaction request với nguon_giao_dich từ bankCode
        TransactionRequest transactionRequest = TransactionRequest.builder()
                .transactionSource(parsedData.getBankCode()) // VCB, TCB, BIDV...
                .amount(parsedData.getAmount())
                .type(parsedData.getType())
                .merchant(parsedData.getMerchant())
                .description("Giao dịch từ SMS - " + parsedData.getMerchant())
                .transactionDate(parsedData.getTransactionDate())
                .isAuto(true) // Đánh dấu là tự động tạo từ SMS
                .build();

        // Tạo giao dịch (sẽ tự động phân loại category)
        return transactionService.createTransaction(transactionRequest, username);
    }
}
