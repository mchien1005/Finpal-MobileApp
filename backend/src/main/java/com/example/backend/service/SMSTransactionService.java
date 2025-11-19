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

@Service
@RequiredArgsConstructor
public class SMSTransactionService {

    private final SMSParserService smsParserService;
    private final TransactionService transactionService;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Transactional
    public TransactionResponse processSMSTransaction(String smsContent, String senderPhone, String username) {
        // Parse SMS content
        ParsedSMSData parsedData = smsParserService.parseSMS(smsContent, senderPhone);

        // Get user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Find or create account
        Account account = findOrCreateAccount(user, parsedData);

        // Build transaction request
        TransactionRequest transactionRequest = TransactionRequest.builder()
                .accountId(account.getId())
                .amount(parsedData.getAmount())
                .type(parsedData.getType())
                .description(parsedData.getMerchant())
                .transactionDate(parsedData.getTransactionDate())
                .isAuto(true) // Mark as auto-created from SMS
                .build();

        // Create transaction using existing service (will auto-categorize)
        return transactionService.createTransaction(transactionRequest, username);
    }

    private Account findOrCreateAccount(User user, ParsedSMSData parsedData) {
        String bankCode = parsedData.getBankCode();
        String accountNumber = parsedData.getAccountNumber();

        // Try to find existing account by bank code and last 4 digits
        Account account = accountRepository
                .findByUserIdAndAccountNameContaining(user.getId(), bankCode + " " + accountNumber)
                .stream()
                .findFirst()
                .orElse(null);

        if (account == null) {
            // Create new bank account
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
