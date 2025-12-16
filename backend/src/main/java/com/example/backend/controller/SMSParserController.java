package com.example.backend.controller;

import com.example.backend.dto.*;
import com.example.backend.model.SMSParser;
import com.example.backend.repository.SMSParserRepository;
import com.example.backend.service.SMSParserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sms/parsers")
@RequiredArgsConstructor
public class SMSParserController {

    private final SMSParserRepository smsParserRepository;
    private final SMSParserService smsParserService;
    private final ObjectMapper objectMapper;

    /**
     * Lấy danh sách tất cả SMS parsers
     */
    @GetMapping
    public ResponseEntity<List<SMSParserResponse>> getAllParsers() {
        List<SMSParser> parsers = smsParserRepository.findAll();
        List<SMSParserResponse> response = parsers.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy parser theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<SMSParserResponse> getParserById(@PathVariable Long id) {
        SMSParser parser = smsParserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Parser not found with id: " + id));
        return ResponseEntity.ok(convertToResponse(parser));
    }

    /**
     * Tạo parser mới cho ngân hàng
     */
    @PostMapping
    public ResponseEntity<SMSParserResponse> createParser(@Valid @RequestBody SMSParserCreateRequest request) {
        // Validate regex pattern trước khi lưu
        try {
            Pattern.compile(request.getRegexPattern(), Pattern.DOTALL);
        } catch (Exception e) {
            throw new RuntimeException("Invalid regex pattern: " + e.getMessage());
        }

        // Validate JSON field mappings
        try {
            objectMapper.readValue(request.getFieldMappings(), new TypeReference<Map<String, Integer>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Invalid field mappings JSON: " + e.getMessage());
        }

        SMSParser parser = SMSParser.builder()
                .bankName(request.getBankName())
                .bankCode(request.getBankCode())
                .senderNumber(request.getSenderNumber())
                .regexPattern(request.getRegexPattern())
                .fieldMappings(request.getFieldMappings())
                .sampleSms(request.getSampleSms())
                .isActive(request.getIsActive())
                .priority(request.getPriority())
                .build();

        SMSParser saved = smsParserRepository.save(parser);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToResponse(saved));
    }

    /**
     * Cập nhật parser
     */
    @PutMapping("/{id}")
    public ResponseEntity<SMSParserResponse> updateParser(
            @PathVariable Long id,
            @Valid @RequestBody SMSParserUpdateRequest request) {
        
        SMSParser parser = smsParserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Parser not found with id: " + id));

        if (request.getBankName() != null) {
            parser.setBankName(request.getBankName());
        }
        if (request.getBankCode() != null) {
            parser.setBankCode(request.getBankCode());
        }
        if (request.getSenderNumber() != null) {
            parser.setSenderNumber(request.getSenderNumber());
        }
        if (request.getRegexPattern() != null) {
            // Validate regex pattern
            try {
                Pattern.compile(request.getRegexPattern(), Pattern.DOTALL);
            } catch (Exception e) {
                throw new RuntimeException("Invalid regex pattern: " + e.getMessage());
            }
            parser.setRegexPattern(request.getRegexPattern());
        }
        if (request.getFieldMappings() != null) {
            // Validate JSON
            try {
                objectMapper.readValue(request.getFieldMappings(), new TypeReference<Map<String, Integer>>() {});
            } catch (Exception e) {
                throw new RuntimeException("Invalid field mappings JSON: " + e.getMessage());
            }
            parser.setFieldMappings(request.getFieldMappings());
        }
        if (request.getSampleSms() != null) {
            parser.setSampleSms(request.getSampleSms());
        }
        if (request.getIsActive() != null) {
            parser.setIsActive(request.getIsActive());
        }
        if (request.getPriority() != null) {
            parser.setPriority(request.getPriority());
        }

        SMSParser updated = smsParserRepository.save(parser);
        return ResponseEntity.ok(convertToResponse(updated));
    }

    /**
     * Xóa parser
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteParser(@PathVariable Long id) {
        if (!smsParserRepository.existsById(id)) {
            throw new RuntimeException("Parser not found with id: " + id);
        }
        smsParserRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Test regex pattern với SMS mẫu trước khi lưu
     */
    @PostMapping("/test")
    public ResponseEntity<TestRegexResponse> testRegexPattern(@Valid @RequestBody TestRegexRequest request) {
        try {
            // Compile pattern với DOTALL flag
            Pattern pattern = Pattern.compile(request.getRegexPattern(), Pattern.DOTALL);
            Matcher matcher = pattern.matcher(request.getSmsContent());

            if (!matcher.find()) {
                return ResponseEntity.ok(TestRegexResponse.builder()
                        .matched(false)
                        .message("SMS không khớp với regex pattern")
                        .build());
            }

            // Parse field mappings
            Map<String, Integer> fieldMappings = objectMapper.readValue(
                    request.getFieldMappings(),
                    new TypeReference<Map<String, Integer>>() {}
            );

            // Extract fields
            Map<String, String> extractedFields = new HashMap<>();
            for (Map.Entry<String, Integer> entry : fieldMappings.entrySet()) {
                String fieldName = entry.getKey();
                Integer groupIndex = entry.getValue();
                try {
                    String value = matcher.group(groupIndex);
                    extractedFields.put(fieldName, value);
                } catch (Exception e) {
                    extractedFields.put(fieldName, "ERROR: Group " + groupIndex + " not found");
                }
            }

            return ResponseEntity.ok(TestRegexResponse.builder()
                    .matched(true)
                    .message("Pattern khớp thành công!")
                    .extractedFields(extractedFields)
                    .build());

        } catch (Exception e) {
            return ResponseEntity.ok(TestRegexResponse.builder()
                    .matched(false)
                    .message("Lỗi: " + e.getMessage())
                    .build());
        }
    }

    /**
     * Test SMS đơn giản - chỉ cần nhập nội dung SMS
     * Hệ thống sẽ tự động thử tất cả parsers trong database để tìm parser phù hợp
     */
    @PostMapping("/test-simple")
    public ResponseEntity<TestRegexResponse> testSmsSimple(@RequestBody Map<String, String> request) {
        String smsContent = request.get("smsContent");
        
        if (smsContent == null || smsContent.isBlank()) {
            return ResponseEntity.badRequest().body(TestRegexResponse.builder()
                    .matched(false)
                    .message("smsContent không được để trống")
                    .build());
        }

        // Lấy tất cả parsers đang active, sắp xếp theo priority
        List<SMSParser> parsers = smsParserRepository.findByIsActiveTrueOrderByPriorityDesc();
        
        if (parsers.isEmpty()) {
            return ResponseEntity.ok(TestRegexResponse.builder()
                    .matched(false)
                    .message("Không có parser nào trong database")
                    .build());
        }

        // Thử từng parser
        for (SMSParser parser : parsers) {
            try {
                Pattern pattern = Pattern.compile(parser.getRegexPattern(), Pattern.DOTALL);
                Matcher matcher = pattern.matcher(smsContent);

                if (matcher.find()) {
                    // Parse field mappings
                    Map<String, Integer> fieldMappings = objectMapper.readValue(
                            parser.getFieldMappings(),
                            new TypeReference<Map<String, Integer>>() {}
                    );

                    // Extract fields
                    Map<String, String> extractedFields = new HashMap<>();
                    extractedFields.put("_bankCode", parser.getBankCode());
                    extractedFields.put("_bankName", parser.getBankName());
                    
                    for (Map.Entry<String, Integer> entry : fieldMappings.entrySet()) {
                        String fieldName = entry.getKey();
                        Integer groupIndex = entry.getValue();
                        try {
                            String value = matcher.group(groupIndex);
                            extractedFields.put(fieldName, value);
                        } catch (Exception e) {
                            extractedFields.put(fieldName, "ERROR: Group " + groupIndex + " not found");
                        }
                    }

                    return ResponseEntity.ok(TestRegexResponse.builder()
                            .matched(true)
                            .message("Khớp với parser: " + parser.getBankName() + " (" + parser.getBankCode() + ")")
                            .extractedFields(extractedFields)
                            .build());
                }
            } catch (Exception e) {
                // Tiếp tục thử parser khác nếu parser này lỗi
                continue;
            }
        }

        // Không tìm thấy parser nào khớp
        return ResponseEntity.ok(TestRegexResponse.builder()
                .matched(false)
                .message("Không tìm thấy parser nào khớp với SMS này. Đã thử " + parsers.size() + " parsers.")
                .build());
    }

    /**
     * Convert entity to response DTO
     */
    private SMSParserResponse convertToResponse(SMSParser parser) {
        return SMSParserResponse.builder()
                .id(parser.getId())
                .bankName(parser.getBankName())
                .bankCode(parser.getBankCode())
                .senderNumber(parser.getSenderNumber())
                .regexPattern(parser.getRegexPattern())
                .fieldMappings(parser.getFieldMappings())
                .sampleSms(parser.getSampleSms())
                .isActive(parser.getIsActive())
                .priority(parser.getPriority())
                .createdAt(parser.getCreatedAt())
                .updatedAt(parser.getUpdatedAt())
                .build();
    }
}
