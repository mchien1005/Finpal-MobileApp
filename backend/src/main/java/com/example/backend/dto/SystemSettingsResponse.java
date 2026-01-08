package com.example.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO response cho SystemSettings
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin cài đặt hệ thống")
public class SystemSettingsResponse {

    @Schema(description = "ID cài đặt")
    private Long id;

    @Schema(description = "Khóa cài đặt", example = "ai.confidence.threshold")
    private String settingKey;

    @Schema(description = "Giá trị cài đặt", example = "0.70")
    private String settingValue;

    @Schema(description = "Mô tả cài đặt", example = "Ngưỡng độ tin cậy AI tối thiểu")
    private String description;

    @Schema(description = "Nhóm cài đặt", example = "AI")
    private String settingGroup;

    @Schema(description = "Kiểu dữ liệu", example = "DOUBLE")
    private String dataType;

    @Schema(description = "Ngày cập nhật cuối")
    private LocalDateTime updatedAt;
}
