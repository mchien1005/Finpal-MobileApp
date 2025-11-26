package com.example.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Gửi SMS ngân hàng để tự động tạo giao dịch")
public class SMSParserRequest {

    @NotBlank(message = "SMS content is required")
    @Schema(description = "Nội dung tin nhắn SMS từ ngân hàng", example = "VCB: TK 1234567890 -150,000VND luc 20/11/2025 08:30. So du: 4,850,000VND. GD: GRAB", requiredMode = Schema.RequiredMode.REQUIRED)
    private String smsContent;

    @NotBlank(message = "Sender phone number is required")
    @Schema(description = "Số điện thoại người gửi (ngân hàng)", example = "Vietcombank", requiredMode = Schema.RequiredMode.REQUIRED)
    private String senderPhone;

    @NotNull(message = "Received time is required")
    @Schema(description = "Thời gian nhận tin nhắn (ISO 8601)", example = "2025-11-20T08:30:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime receivedAt;
}
