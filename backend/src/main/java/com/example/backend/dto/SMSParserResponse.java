package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SMSParserResponse {

    private Long id;
    private String bankName;
    private String bankCode;
    private String senderNumber;
    private String regexPattern;
    private String fieldMappings;
    private String sampleSms;
    private Boolean isActive;
    private Integer priority;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
