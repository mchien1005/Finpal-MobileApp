package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SMSParserCreateRequest {

    @NotBlank(message = "Tên ngân hàng không được để trống")
    private String bankName;

    @NotBlank(message = "Mã ngân hàng không được để trống")
    private String bankCode;

    private String senderNumber;

    @NotBlank(message = "Regex pattern không được để trống")
    private String regexPattern;

    @NotBlank(message = "Field mappings không được để trống")
    private String fieldMappings;

    private String sampleSms;

    @NotNull(message = "Trạng thái hoạt động không được null")
    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    private Integer priority = 0;
}
