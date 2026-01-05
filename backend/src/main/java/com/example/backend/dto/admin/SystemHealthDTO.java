package com.example.backend.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO cho tình trạng hệ thống
 * Hiển thị CPU, RAM, Database status
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin sức khỏe hệ thống realtime")
public class SystemHealthDTO {

    // === CPU & RAM ===
    @Schema(description = "Phần trăm sử dụng CPU (%)", example = "45.0")
    private Double cpuUsage;

    @Schema(description = "Phần trăm sử dụng RAM (%)", example = "68.0")
    private Double ramUsage;

    @Schema(description = "Tổng dung lượng RAM (MB)", example = "8192")
    private Long totalMemoryMB;

    @Schema(description = "RAM đang sử dụng (MB)", example = "5570")
    private Long usedMemoryMB;

    @Schema(description = "RAM còn trống (MB)", example = "2622")
    private Long freeMemoryMB;

    // === DATABASE ===
    @Schema(description = "Trạng thái kết nối database", example = "CONNECTED", allowableValues = { "CONNECTED",
            "DISCONNECTED", "SLOW" })
    private String databaseStatus;

    @Schema(description = "Số kết nối database hiện tại", example = "15")
    private Long databaseConnections;

    @Schema(description = "Thời gian phản hồi database (ms)", example = "25")
    private Long databaseResponseMs;

    // === BACKEND AI ===
    @Schema(description = "Trạng thái AI Backend", example = "ONLINE", allowableValues = { "ONLINE", "OFFLINE",
            "DEGRADED" })
    private String aiBackendStatus;

    @Schema(description = "Thời gian phản hồi AI Backend (ms)", example = "150")
    private Long aiResponseMs;

    // === GENERAL ===
    @Schema(description = "Thời gian cập nhật gần nhất", example = "2026-01-05T17:00:00")
    private LocalDateTime lastUpdated;

    @Schema(description = "Thời gian hoạt động của server (giây)", example = "86400")
    private Long uptimeSeconds;

    @Schema(description = "Phiên bản JVM đang chạy", example = "21.0.1")
    private String jvmVersion;

    @Schema(description = "Số threads đang hoạt động", example = "45")
    private Integer activeThreads;
}
