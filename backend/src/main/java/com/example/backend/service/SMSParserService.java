package com.example.backend.service;

import com.example.backend.dto.ParsedSMSData;
import com.example.backend.model.SMSParser;
import com.example.backend.repository.SMSParserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service phân tích (parse) tin nhắn SMS từ ngân hàng
 * Chiến lược:
 * 1. Thử parse bằng regex (cấu hình trong DB)
 * 2. Nếu thất bại → Fallback sang AI parsing
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SMSParserService {

    private final SMSParserRepository smsParserRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${ai.backend.url:http://localhost:8000}")
    private String aiBackendUrl;

    /**
     * Phân tích nội dung SMS từ ngân hàng
     * Chiến lược: Regex first → AI fallback
     * 
     * @param smsContent  Nội dung tin nhắn SMS
     * @param senderPhone Số điện thoại người gửi (dùng để tìm parser phù hợp)
     * @return ParsedSMSData chứa thông tin giao dịch đã trích xuất
     */
    public ParsedSMSData parseSMS(String smsContent, String senderPhone) {
        // Bước 1: Thử parse bằng regex từ database
        try {
            ParsedSMSData regexResult = parseWithRegex(smsContent, senderPhone);
            if (regexResult != null && regexResult.getAmount() != null) {
                log.info("✅ SMS parsed with regex for sender: {}", senderPhone);
                return regexResult;
            }
        } catch (Exception e) {
            log.warn("⚠️ Regex parsing failed for sender {}: {}", senderPhone, e.getMessage());
        }

        // Bước 2: Fallback sang AI parsing
        try {
            ParsedSMSData aiResult = parseWithAI(smsContent, senderPhone);
            if (aiResult != null && aiResult.getAmount() != null) {
                log.info("🤖 SMS parsed with AI for sender: {}", senderPhone);
                return aiResult;
            }
        } catch (Exception e) {
            log.error("❌ AI parsing also failed for sender {}: {}", senderPhone, e.getMessage());
        }

        throw new RuntimeException("Không thể phân tích tin nhắn SMS. Vui lòng thử lại hoặc nhập giao dịch thủ công.");
    }

    /**
     * Parse SMS bằng regex từ database
     */
    private ParsedSMSData parseWithRegex(String smsContent, String senderPhone) {
        // Tìm parser (regex pattern) tương ứng với ngân hàng gửi SMS
        Optional<SMSParser> parserOpt = smsParserRepository.findBySenderNumberAndIsActiveTrue(senderPhone);

        if (parserOpt.isEmpty()) {
            // Thử tìm trong nội dung SMS
            List<SMSParser> allParsers = smsParserRepository.findByIsActiveTrue();
            for (SMSParser p : allParsers) {
                if (smsContent.toUpperCase().contains(p.getBankCode().toUpperCase()) ||
                        smsContent.toUpperCase().contains(p.getBankName().toUpperCase())) {
                    parserOpt = Optional.of(p);
                    break;
                }
            }
        }

        if (parserOpt.isEmpty()) {
            throw new RuntimeException("No parser found for sender: " + senderPhone);
        }

        SMSParser parser = parserOpt.get();

        try {
            // Biên dịch regex pattern từ database với DOTALL flag để . khớp cả \n
            Pattern pattern = Pattern.compile(parser.getRegexPattern(), Pattern.DOTALL);
            Matcher matcher = pattern.matcher(smsContent);

            if (!matcher.find()) {
                throw new RuntimeException("SMS content does not match expected pattern");
            }

            // Parse field mappings từ JSON
            Map<String, Integer> fieldMappings = objectMapper.readValue(
                    parser.getFieldMappings(),
                    new TypeReference<Map<String, Integer>>() {
                    });

            // Xây dựng đối tượng ParsedSMSData từ các group trong regex
            ParsedSMSData parsedData = ParsedSMSData.builder()
                    .bankCode(parser.getBankCode())
                    .build();

            // Trích xuất số tiền
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

            // Trích xuất số tài khoản
            if (fieldMappings.containsKey("account")) {
                String accountFull = matcher.group(fieldMappings.get("account"));
                parsedData.setAccountNumber(accountFull.substring(Math.max(0, accountFull.length() - 4)));
            }

            // Trích xuất và parse thời gian giao dịch
            if (fieldMappings.containsKey("datetime")) {
                String datetimeStr = matcher.group(fieldMappings.get("datetime"));
                parsedData.setTransactionDate(parseDateTime(datetimeStr, parser.getBankCode()));
            } else if (fieldMappings.containsKey("time")) {
                String timeStr = matcher.group(fieldMappings.get("time"));
                if (fieldMappings.containsKey("date")) {
                    String dateStr = matcher.group(fieldMappings.get("date"));
                    timeStr = timeStr + " " + dateStr;
                }
                parsedData.setTransactionDate(parseDateTime(timeStr, parser.getBankCode()));
            }

            return parsedData;

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse SMS with regex: " + e.getMessage(), e);
        }
    }

    /**
     * Parse SMS bằng AI (gọi Python AI backend)
     * AI có thể hiểu nhiều định dạng SMS khác nhau mà không cần cấu hình regex
     */
    @SuppressWarnings("unchecked")
    private ParsedSMSData parseWithAI(String smsContent, String senderPhone) {
        try {
            String url = aiBackendUrl + "/api/parse-sms";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("sms_content", smsContent);
            requestBody.put("sender", senderPhone);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

            if (response == null || !Boolean.TRUE.equals(response.get("success"))) {
                throw new RuntimeException("AI parsing returned unsuccessful result");
            }

            Map<String, Object> data = (Map<String, Object>) response.get("data");
            if (data == null) {
                throw new RuntimeException("AI parsing returned no data");
            }

            ParsedSMSData parsedData = ParsedSMSData.builder()
                    .bankCode(data.get("bank_code") != null ? data.get("bank_code").toString() : senderPhone)
                    .build();

            // Parse amount
            if (data.get("amount") != null) {
                String amountStr = data.get("amount").toString()
                        .replace(",", "")
                        .replace(".", "")
                        .replace(" ", "");
                parsedData.setAmount(new BigDecimal(amountStr));
            }

            // Parse type
            if (data.get("type") != null) {
                String type = data.get("type").toString().toUpperCase();
                parsedData.setType(type.contains("INCOME") || type.contains("+") ? "INCOME" : "EXPENSE");
            }

            // Parse merchant/description
            if (data.get("merchant") != null) {
                parsedData.setMerchant(data.get("merchant").toString());
            } else if (data.get("description") != null) {
                parsedData.setMerchant(data.get("description").toString());
            }

            // Parse account
            if (data.get("account") != null) {
                parsedData.setAccountNumber(data.get("account").toString());
            }

            // Parse datetime
            if (data.get("datetime") != null) {
                try {
                    parsedData.setTransactionDate(LocalDateTime.parse(data.get("datetime").toString()));
                } catch (Exception e) {
                    parsedData.setTransactionDate(LocalDateTime.now());
                }
            } else {
                parsedData.setTransactionDate(LocalDateTime.now());
            }

            return parsedData;

        } catch (Exception e) {
            throw new RuntimeException("AI SMS parsing failed: " + e.getMessage(), e);
        }
    }

    /**
     * Parse chuỗi thời gian theo định dạng của từng ngân hàng
     */
    private LocalDateTime parseDateTime(String timeStr, String bankCode) {
        try {
            DateTimeFormatter formatter;

            switch (bankCode.toUpperCase()) {
                case "VCB":
                    formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
                    break;
                case "TCB":
                    return LocalDateTime.now();
                case "ACB":
                    formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
                    break;
                case "BIDV":
                    formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
                    break;
                case "MBB":
                    formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yy");
                    break;
                case "CTG":
                    formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                    break;
                default:
                    formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            }

            return LocalDateTime.parse(timeStr.trim(), formatter);

        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
}
