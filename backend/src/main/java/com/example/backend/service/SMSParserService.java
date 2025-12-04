package com.example.backend.service;

import com.example.backend.dto.ParsedSMSData;
import com.example.backend.model.SMSParser;
import com.example.backend.repository.SMSParserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service phân tích (parse) tin nhắn SMS từ ngân hàng
 * Chức năng: Dùng regex để trích xuất thông tin giao dịch từ SMS (số tiền,
 * loại, merchant, thời gian, ...)
 */
@Service
@RequiredArgsConstructor
public class SMSParserService {

    private final SMSParserRepository smsParserRepository;
    private final ObjectMapper objectMapper;

    /**
     * Phân tích nội dung SMS từ ngân hàng
     * 
     * @param smsContent  Nội dung tin nhắn SMS (ví dụ: "TK 1234 GD +500,000 VND luc
     *                    10:30 18/11/2025")
     * @param senderPhone Số điện thoại người gửi (dùng để tìm parser phù hợp)
     * @return ParsedSMSData chứa thông tin giao dịch đã trích xuất
     */
    public ParsedSMSData parseSMS(String smsContent, String senderPhone) {
        // Tìm parser (regex pattern) tương ứng với ngân hàng gửi SMS
        SMSParser parser = smsParserRepository.findBySenderNumberAndIsActiveTrue(senderPhone)
                .orElseThrow(() -> new RuntimeException("No parser found for sender: " + senderPhone));

        try {
            // Biên dịch regex pattern từ database
            Pattern pattern = Pattern.compile(parser.getRegexPattern());
            Matcher matcher = pattern.matcher(smsContent);

            if (!matcher.find()) {
                throw new RuntimeException("SMS content does not match expected pattern");
            }

            // Parse field mappings từ JSON (ví dụ: {"amount": 1, "type": 2, "merchant": 3,
            // ...})
            Map<String, Integer> fieldMappings = objectMapper.readValue(
                    parser.getFieldMappings(),
                    new TypeReference<Map<String, Integer>>() {
                    });

            // Xây dựng đối tượng ParsedSMSData từ các group trong regex
            ParsedSMSData parsedData = ParsedSMSData.builder()
                    .bankCode(parser.getBankCode())
                    .build();

            // Trích xuất số tiền (loại bỏ dấu phẩy, dấu chấm, khoảng trắng)
            if (fieldMappings.containsKey("amount")) {
                String amountStr = matcher.group(fieldMappings.get("amount"))
                        .replace(",", "")
                        .replace(".", "")
                        .replace(" ", "");
                parsedData.setAmount(new BigDecimal(amountStr));
            }

            // Trích xuất loại giao dịch (+ = INCOME, - = EXPENSE)
            if (fieldMappings.containsKey("type")) {
                String typeSymbol = matcher.group(fieldMappings.get("type")).trim();
                parsedData.setType(typeSymbol.equals("+") ? "INCOME" : "EXPENSE");
            }

            // Trích xuất merchant (tên cửa hàng/nơi giao dịch)
            if (fieldMappings.containsKey("merchant")) {
                parsedData.setMerchant(matcher.group(fieldMappings.get("merchant")).trim());
            }

            // Trích xuất số tài khoản (lấy 4 chữ số cuối)
            if (fieldMappings.containsKey("account")) {
                String accountFull = matcher.group(fieldMappings.get("account"));
                parsedData.setAccountNumber(accountFull.substring(Math.max(0, accountFull.length() - 4)));
            }

            // Trích xuất và parse thời gian giao dịch
            if (fieldMappings.containsKey("time")) {
                String timeStr = matcher.group(fieldMappings.get("time"));
                parsedData.setTransactionDate(parseDateTime(timeStr, parser.getBankCode()));
            }

            return parsedData;

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse SMS: " + e.getMessage(), e);
        }
    }

    /**
     * Parse chuỗi thời gian theo định dạng của từng ngân hàng
     * 
     * @param timeStr  Chuỗi thời gian (ví dụ: "18/11/2025 15:30:45")
     * @param bankCode Mã ngân hàng (VCB, TCB, ACB, ...)
     * @return LocalDateTime đã parse
     */
    private LocalDateTime parseDateTime(String timeStr, String bankCode) {
        try {
            // Mỗi ngân hàng dùng format khác nhau
            DateTimeFormatter formatter;

            switch (bankCode.toUpperCase()) {
                case "VCB":
                    // Format: "18/11/2025 15:30:45"
                    formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                    break;
                case "TCB":
                    // Format: "15:30 18/11/2025"
                    formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
                    break;
                case "ACB":
                    // Format: "18-11-2025 15:30"
                    formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
                    break;
                default:
                    // Format mặc định
                    formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            }

            return LocalDateTime.parse(timeStr.trim(), formatter);

        } catch (Exception e) {
            // Nếu parse lỗi, trả về thời gian hiện tại
            return LocalDateTime.now();
        }
    }
}
