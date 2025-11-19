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

@Service
@RequiredArgsConstructor
public class SMSParserService {

    private final SMSParserRepository smsParserRepository;
    private final ObjectMapper objectMapper;

    public ParsedSMSData parseSMS(String smsContent, String senderPhone) {
        // Find parser for this sender
        SMSParser parser = smsParserRepository.findBySenderNumberAndIsActiveTrue(senderPhone)
                .orElseThrow(() -> new RuntimeException("No parser found for sender: " + senderPhone));

        try {
            // Compile regex pattern
            Pattern pattern = Pattern.compile(parser.getRegexPattern());
            Matcher matcher = pattern.matcher(smsContent);

            if (!matcher.find()) {
                throw new RuntimeException("SMS content does not match expected pattern");
            }

            // Parse field mappings from JSON
            Map<String, Integer> fieldMappings = objectMapper.readValue(
                    parser.getFieldMappings(),
                    new TypeReference<Map<String, Integer>>() {
                    });

            // Build parsed data
            ParsedSMSData parsedData = ParsedSMSData.builder()
                    .bankCode(parser.getBankCode())
                    .build();

            // Extract amount
            if (fieldMappings.containsKey("amount")) {
                String amountStr = matcher.group(fieldMappings.get("amount"))
                        .replace(",", "")
                        .replace(".", "")
                        .replace(" ", "");
                parsedData.setAmount(new BigDecimal(amountStr));
            }

            // Extract type (INCOME/EXPENSE)
            if (fieldMappings.containsKey("type")) {
                String typeSymbol = matcher.group(fieldMappings.get("type")).trim();
                parsedData.setType(typeSymbol.equals("+") ? "INCOME" : "EXPENSE");
            }

            // Extract merchant/description
            if (fieldMappings.containsKey("merchant")) {
                parsedData.setMerchant(matcher.group(fieldMappings.get("merchant")).trim());
            }

            // Extract account number (last 4 digits)
            if (fieldMappings.containsKey("account")) {
                String accountFull = matcher.group(fieldMappings.get("account"));
                parsedData.setAccountNumber(accountFull.substring(Math.max(0, accountFull.length() - 4)));
            }

            // Extract and parse transaction time
            if (fieldMappings.containsKey("time")) {
                String timeStr = matcher.group(fieldMappings.get("time"));
                parsedData.setTransactionDate(parseDateTime(timeStr, parser.getBankCode()));
            }

            return parsedData;

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse SMS: " + e.getMessage(), e);
        }
    }

    private LocalDateTime parseDateTime(String timeStr, String bankCode) {
        try {
            // Different banks use different date formats
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
                    // Default format
                    formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            }

            return LocalDateTime.parse(timeStr.trim(), formatter);

        } catch (Exception e) {
            // If parsing fails, return current time
            return LocalDateTime.now();
        }
    }
}
