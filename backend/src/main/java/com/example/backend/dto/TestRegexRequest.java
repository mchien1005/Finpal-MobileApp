package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestRegexRequest {

    @NotBlank(message = "Regex pattern không được để trống")
    private String regexPattern;

    @NotBlank(message = "SMS content không được để trống")
    private String smsContent;

    @NotBlank(message = "Field mappings không được để trống")
    private String fieldMappings;
}
