package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO response từ API phát hiện giao dịch bất thường
 * Endpoint: POST /api/anomaly/detect
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnomalyDetectionResult {
    
    /**
     * Giao dịch có bất thường không (true = có)
     */
    @JsonProperty("is_anomaly")
    private Boolean isAnomaly;
    
    /**
     * Điểm anomaly 0-1 (càng cao càng bất thường)
     */
    @JsonProperty("anomaly_score")
    private Double anomalyScore;
    
    /**
     * Lý do bất thường (ví dụ: 'Số tiền cao hơn 3x trung bình')
     */
    private String reason;
    
    /**
     * Gợi ý cho người dùng (ví dụ: 'Xem xét lại giao dịch')
     */
    private String recommendation;
    
    /**
     * Thông báo chi tiết (từ template mau_thong_bao)
     */
    private String message;
}
