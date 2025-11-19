package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SMSParserRequest {

    @NotBlank(message = "SMS content is required")
    private String smsContent;

    @NotBlank(message = "Sender phone number is required")
    private String senderPhone;

    @NotNull(message = "Received time is required")
    private LocalDateTime receivedAt;
}
