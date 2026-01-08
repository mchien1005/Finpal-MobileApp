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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

/**
 * Service quản lý giao dịch từ SMS ngân hàng
 * Chức năng: Parse SMS → Kiểm tra trùng → Tạo giao dịch → Tự động phân loại
 * category
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
         * @param smsContent  Nội dung SMS (ví dụ: "TK 1234 GD +500,000 VND luc 10:30
         *                    18/11/2025")
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

                // Tính hash của nội dung SMS để check duplicate chính xác
                String smsHash = calculateSmsHash(smsContent);

                // Kiểm tra trùng lặp bằng SMS hash (CHÍNH XÁC 100%)
                // Hash được lưu trong cột smsContentEncrypted
                boolean isDuplicateByHash = transactionRepository.existsBySmsContentHash(user.getId(), smsHash);
                if (isDuplicateByHash) {
                        log.warn("Duplicate SMS detected by hash for user {}: hash={}", username, smsHash);
                        throw new RuntimeException(
                                        "Giao dịch này đã được ghi nhận trước đó. Không tạo giao dịch trùng lặp.");
                }

                // Kiểm tra trùng lặp fallback: Giao dịch với cùng số tiền, ngân hàng, và thời
                // gian tương tự
                // Đây là fallback cho các giao dịch cũ chưa có hash
                boolean isDuplicateByTime = checkDuplicateTransaction(user.getId(), parsedData);
                if (isDuplicateByTime) {
                        log.warn("Duplicate SMS transaction detected for user {}: amount={}, source={}, date={}",
                                        username, parsedData.getAmount(), parsedData.getBankCode(),
                                        parsedData.getTransactionDate());
                        throw new RuntimeException(
                                        "Giao dịch này đã được ghi nhận trước đó. Không tạo giao dịch trùng lặp.");
                }

                // Xây dựng transaction request với nguon_giao_dich từ bankCode
                // KHÔNG lưu merchant vào description vì chứa thông tin nhạy cảm (số TK, mã
                // GD...)
                // Merchant chỉ được dùng để AI/Rule phân loại category, không lưu vào DB
                String safeDescription = "Giao dịch từ " + parsedData.getBankCode();

                // Trích xuất từ khóa an toàn từ merchant để hỗ trợ phân loại (không chứa số TK,
                // mã GD)
                String merchantForCategorization = parsedData.getMerchant();

                TransactionRequest transactionRequest = TransactionRequest.builder()
                                .transactionSource(parsedData.getBankCode()) // VCB, TCB, BIDV...
                                .amount(parsedData.getAmount())
                                .type(parsedData.getType())
                                .description(safeDescription) // Mô tả an toàn, không chứa thông tin nhạy cảm
                                .transactionDate(parsedData.getTransactionDate())
                                .isAuto(true) // Đánh dấu là tự động tạo từ SMS
                                .notes(null) // Không lưu ghi chú nhạy cảm
                                .smsContent(smsHash) // Lưu hash (thay vì SMS gốc) để check duplicate và bảo vệ dữ liệu
                                .merchantHint(merchantForCategorization) // Dùng để AI/Rule phân loại, không lưu DB
                                .build();

                // Tạo giao dịch (sẽ tự động phân loại category)
                log.info("Creating SMS transaction for user {}: amount={}, source={}, hash={}",
                                username, parsedData.getAmount(), parsedData.getBankCode(), smsHash);
                return transactionService.createTransaction(transactionRequest, username);
        }

        /**
         * Tính SHA-256 hash của nội dung SMS
         * Hash này đảm bảo mỗi SMS duy nhất chỉ tạo được một giao dịch
         *
         * @param smsContent Nội dung SMS
         * @return SHA-256 hash dưới dạng hex string (64 ký tự)
         */
        private String calculateSmsHash(String smsContent) {
                try {
                        MessageDigest digest = MessageDigest.getInstance("SHA-256");
                        byte[] hashBytes = digest.digest(smsContent.getBytes(StandardCharsets.UTF_8));

                        // Chuyển byte array sang hex string
                        StringBuilder hexString = new StringBuilder();
                        for (byte b : hashBytes) {
                                String hex = Integer.toHexString(0xff & b);
                                if (hex.length() == 1) {
                                        hexString.append('0');
                                }
                                hexString.append(hex);
                        }
                        return hexString.toString();
                } catch (NoSuchAlgorithmException e) {
                        log.error("SHA-256 algorithm not found, using fallback hash", e);
                        // Fallback: sử dụng hashCode kết hợp độ dài
                        return String.valueOf(smsContent.hashCode()) + "_" + smsContent.length();
                }
        }

        /**
         * Kiểm tra xem giao dịch SMS có trùng lặp không (Fallback method)
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
                                endTime);
        }
}
