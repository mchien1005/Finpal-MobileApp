package com.example.backend.service;

import com.example.backend.dto.ParsedSMSData;
import com.example.backend.dto.TransactionRequest;
import com.example.backend.dto.TransactionResponse;
import com.example.backend.model.User;
import com.example.backend.repository.TransactionRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service quản lý giao dịch từ SMS ngân hàng
 * Chức năng: Parse SMS → Kiểm tra trùng → Tạo giao dịch → Tự động phân loại category
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SMSTransactionService {

    private final SMSParserService smsParserService;
    private final TransactionService transactionService;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    /**
     * Xử lý tạo giao dịch từ SMS ngân hàng
     * 
     * @param smsContent  Nội dung SMS (ví dụ: "TK 1234 GD +500,000 VND luc 10:30 18/11/2025")
     * @param senderPhone Số điện thoại người gửi (dùng để xác định ngân hàng)
     * @param username    Tên đăng nhập của user
     * @return TransactionResponse chứa thông tin giao dịch vừa tạo
     * @throws RuntimeException nếu giao dịch đã tồn tại (trùng lặp)
     */
    @Transactional
    public TransactionResponse processSMSTransaction(String smsContent, String senderPhone, String username) {
        // Parse nội dung SMS
        ParsedSMSData parsedData = smsParserService.parseSMS(smsContent, senderPhone);

        // Verify user exists
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Kiểm tra trùng lặp: Giao dịch với cùng số tiền, ngân hàng, và thời gian tương tự
        boolean isDuplicate = checkDuplicateTransaction(
                user.getId(),
                parsedData
        );

        if (isDuplicate) {
            log.warn("Duplicate SMS transaction detected for user {}: amount={}, source={}, date={}",
                    username, parsedData.getAmount(), parsedData.getBankCode(), parsedData.getTransactionDate());
            throw new RuntimeException("Giao dịch này đã được ghi nhận trước đó. Không tạo giao dịch trùng lặp.");
        }

        // Xây dựng transaction request với nguon_giao_dich từ bankCode
        // Gộp merchant vào description để phân loại
        String description = parsedData.getMerchant() != null ? parsedData.getMerchant() : "Giao dịch từ SMS";
        
        TransactionRequest transactionRequest = TransactionRequest.builder()
                .transactionSource(parsedData.getBankCode()) // VCB, TCB, BIDV...
                .amount(parsedData.getAmount())
                .type(parsedData.getType())
                .description(description) // Merchant info gộp vào description
                .transactionDate(parsedData.getTransactionDate())
                .isAuto(true) // Đánh dấu là tự động tạo từ SMS
                .notes("Quét từ SMS ngân hàng " + parsedData.getBankCode()) // Ghi chú
                .smsContent(smsContent) // Lưu nội dung SMS gốc
                .build();

        // Tạo giao dịch (sẽ tự động phân loại category)
        log.info("Creating SMS transaction for user {}: amount={}, source={}",
                username, parsedData.getAmount(), parsedData.getBankCode());
        return transactionService.createTransaction(transactionRequest, username);
    }

    /**
     * Kiểm tra xem giao dịch SMS có trùng lặp không
     * 
     * Tiêu chí kiểm tra:
     * - Cùng user
     * - Cùng số tiền
     * - Cùng ngân hàng (transactionSource)
     * - Thời gian giao dịch trong khoảng ±5 phút
     * - Là giao dịch tự động (isAuto = true)
     */
    private boolean checkDuplicateTransaction(Long userId, ParsedSMSData parsedData) {
        LocalDateTime transactionTime = parsedData.getTransactionDate();
        
        // Kiểm tra trong khoảng ±5 phút
        LocalDateTime startTime = transactionTime.minusMinutes(5);
        LocalDateTime endTime = transactionTime.plusMinutes(5);

        return transactionRepository.existsDuplicateSMSTransaction(
                userId,
                parsedData.getAmount(),
                parsedData.getBankCode(),
                startTime,
                endTime
        );
    }
}
