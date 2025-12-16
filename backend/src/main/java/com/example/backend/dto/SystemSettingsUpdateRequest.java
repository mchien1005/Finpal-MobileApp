package com.example.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO request để cập nhật cài đặt hệ thống
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request cập nhật cài đặt hệ thống")
public class SystemSettingsUpdateRequest {

    @NotBlank(message = "Khóa cài đặt không được để trống")
    @Schema(description = "Khóa cài đặt", example = "ai.confidence.threshold", required = true)
    private String settingKey;

    @NotBlank(message = "Giá trị cài đặt không được để trống")
    @Schema(description = "Giá trị mới", example = "0.75", required = true)
    private String settingValue;
}
