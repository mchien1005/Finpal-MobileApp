package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestRegexResponse {

    private Boolean matched;
    private String message;
    private Map<String, String> extractedFields;
    private ParsedSMSData parsedData;
}
