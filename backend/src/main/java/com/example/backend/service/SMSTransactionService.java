package com.example.backend.service;

import com.example.backend.dto.ParsedSMSData;
import com.example.backend.dto.TransactionRequest;
import com.example.backend.dto.TransactionResponse;
import com.example.backend.model.Account;
import com.example.backend.model.User;
import com.example.backend.repository.AccountRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Service quản lý giao dịch từ SMS ngân hàng
 * Chức năng: Parse SMS → Tự động tạo giao dịch → Tự động phân loại category
 */
@Service
@RequiredArgsConstructor
public class SMSTransactionService {

    private final SMSParserService smsParserService;
    private final TransactionService transactionService;
    private final AccountRepository accountRepository;
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

        // Lấy thông tin user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Tìm hoặc tạo tài khoản ngân hàng tự động
        Account account = findOrCreateAccount(user, parsedData);

        // Xây dựng transaction request
        TransactionRequest transactionRequest = TransactionRequest.builder()
                .accountId(account.getId())
                .amount(parsedData.getAmount())
                .type(parsedData.getType())
                .description(parsedData.getMerchant())
                .transactionDate(parsedData.getTransactionDate())
                .isAuto(true) // Đánh dấu là tự động tạo từ SMS
                .build();

        // Tạo giao dịch (sẽ tự động phân loại category)
        return transactionService.createTransaction(transactionRequest, username);
    }

    /**
     * Tìm hoặc tạo tài khoản ngân hàng dựa trên thông tin SMS
     * 
     * @param user       User sở hữu tài khoản
     * @param parsedData Dữ liệu đã parse từ SMS (chứa bankCode và accountNumber)
     * @return Account đã tìm thấy hoặc vừa tạo mới
     */
    private Account findOrCreateAccount(User user, ParsedSMSData parsedData) {
        String bankCode = parsedData.getBankCode();
        String accountNumber = parsedData.getAccountNumber();

        // Thử tìm tài khoản đã tồn tại theo bank code và 4 chữ số cuối
        Account account = accountRepository
                .findByUserIdAndAccountNameContaining(user.getId(), bankCode + " " + accountNumber)
                .stream()
                .findFirst()
                .orElse(null);

        if (account == null) {
            // Tạo tài khoản ngân hàng mới
            account = Account.builder()
                    .userId(user.getId())
                    .bankName(getBankNameFromCode(bankCode))
                    .accountName(getBankNameFromCode(bankCode) + " " + accountNumber)
                    .accountNumber(accountNumber)
                    .accountType(Account.AccountType.BANK)
                    .balance(BigDecimal.ZERO)
                    .currency("VND")
                    .isActive(true)
                    .build();
            account = accountRepository.save(account);
        }

        return account;
    }

    /**
     * Chuyển mã ngân hàng sang tên đầy đủ (VCB → Vietcombank, TCB → Techcombank,
     * ...)
     */
    private String getBankNameFromCode(String bankCode) {
        switch (bankCode.toUpperCase()) {
            case "VCB":
                return "Vietcombank";
            case "TCB":
                return "Techcombank";
            case "ACB":
                return "ACB";
            case "VTB":
                return "VietinBank";
            case "BIDV":
                return "BIDV";
            case "MB":
                return "MB Bank";
            default:
                return bankCode;
        }
    }
}
