package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SMSParserUpdateRequest {

    private String bankName;
    private String bankCode;
    private String senderNumber;
    private String regexPattern;
    private String fieldMappings;
    private String sampleSms;
    private Boolean isActive;
    private Integer priority;
}
