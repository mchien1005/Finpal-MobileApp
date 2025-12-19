package com.example.backend.service;

import com.example.backend.dto.SavingsSuggestionsResponse;
import com.example.backend.dto.SmartTip;
import com.example.backend.dto.SmartTipsResponse;
import com.example.backend.dto.SpendingInsight;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Service tích hợp AI Backend để lấy Insights thông minh
 * Chức năng: Gợi ý tiết kiệm, phân tích chi tiêu proactive, smart tips (dựa trên AI/ML)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AIInsightsService {

    private final RestTemplate restTemplate;

    @Value("${ai.backend.url:http://localhost:8000}")
    private String aiBackendUrl;

    @Value("${ai.backend.enabled:true}")
    private boolean aiEnabled;

    /**
     * Lấy gợi ý tiết kiệm từ AI (phân tích pattern chi tiêu và đề xuất cách giảm
     * chi)
     * 
     * @param userId ID của user
     * @return SavingsSuggestionsResponse chứa danh sách gợi ý và tổng tiềm năng
     *         tiết kiệm
     */
    public SavingsSuggestionsResponse getSavingsSuggestions(Long userId) {
        if (!aiEnabled) {
            return null;
        }

        try {
            String url = aiBackendUrl + "/api/insights/savings-suggestions/" + userId;

            log.debug("Fetching AI savings suggestions for user: {}", userId);
            ResponseEntity<SavingsSuggestionsResponse> response = restTemplate.getForEntity(
                    url,
                    SavingsSuggestionsResponse.class);

            SavingsSuggestionsResponse suggestions = response.getBody();

            if (suggestions != null) {
                log.info("Retrieved {} savings suggestions for user {}",
                        suggestions.getSuggestions().size(),
                        userId);
            }

            return suggestions;

        } catch (Exception e) {
            log.warn("Failed to get AI savings suggestions: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Lấy insights chủ động từ AI (cảnh báo, thành tích, mẹo quản lý chi tiêu)
     * 
     * @param userId ID của user
     * @return List<SpendingInsight> chứa các insight với impact score và insight
     *         type
     */
    public List<SpendingInsight> getProactiveInsights(Long userId) {
        if (!aiEnabled) {
            return new ArrayList<>();
        }

        try {
            String url = aiBackendUrl + "/api/insights/proactive-insights/" + userId;

            log.debug("Fetching AI proactive insights for user: {}", userId);
            ResponseEntity<List<SpendingInsight>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<SpendingInsight>>() {
                    });

            List<SpendingInsight> insights = response.getBody();

            if (insights != null) {
                log.info("Retrieved {} proactive insights for user {}",
                        insights.size(),
                        userId);
                return insights;
            }

            return new ArrayList<>();

        } catch (Exception e) {
            log.warn("Failed to get AI proactive insights: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Lấy danh sách Smart Tips từ AI
     * Tips bao gồm: gợi ý cá nhân hóa, tips theo mùa, tips chung về quản lý tài chính
     * 
     * @param userId ID của user
     * @param maxTips Số lượng tips tối đa
     * @return SmartTipsResponse chứa danh sách tips
     */
    public SmartTipsResponse getSmartTips(Long userId, int maxTips) {
        if (!aiEnabled) {
            return null;
        }

        try {
            String url = aiBackendUrl + "/api/tips/smart-tips/" + userId + "?max_tips=" + maxTips;

            log.debug("Fetching AI smart tips for user: {}", userId);
            ResponseEntity<SmartTipsResponse> response = restTemplate.getForEntity(
                    url,
                    SmartTipsResponse.class);

            SmartTipsResponse tips = response.getBody();

            if (tips != null) {
                log.info("Retrieved {} smart tips for user {}", tips.getTipsCount(), userId);
            }

            return tips;

        } catch (Exception e) {
            log.warn("Failed to get AI smart tips: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Lấy tip hàng ngày (Daily Tip) từ AI
     * Có thể dùng để hiển thị trên màn hình chính của app
     * 
     * @return SmartTip - Một tip ngẫu nhiên cho ngày hôm nay
     */
    public SmartTip getDailyTip() {
        if (!aiEnabled) {
            return null;
        }

        try {
            String url = aiBackendUrl + "/api/tips/daily-tip";

            log.debug("Fetching daily tip from AI");
            ResponseEntity<SmartTip> response = restTemplate.getForEntity(
                    url,
                    SmartTip.class);

            SmartTip tip = response.getBody();

            if (tip != null) {
                log.info("Retrieved daily tip: {}", tip.getTitle());
            }

            return tip;

        } catch (Exception e) {
            log.warn("Failed to get daily tip: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Kiểm tra giao dịch có bất thường không (realtime anomaly detection)
     * Gọi khi user tạo giao dịch mới để cảnh báo ngay lập tức
     * 
     * @param userId ID của user
     * @param amount Số tiền giao dịch
     * @param merchant Tên merchant/mô tả
     * @param category Danh mục giao dịch
     * @return AnomalyDetectionResult hoặc null nếu có lỗi
     */
    public com.example.backend.dto.AnomalyDetectionResult checkAnomaly(
            Long userId, 
            Double amount, 
            String merchant, 
            String category) {
        
        if (!aiEnabled) {
            return null;
        }

        try {
            String url = aiBackendUrl + "/api/anomaly/detect";

            log.debug("Checking anomaly for user {} - amount: {}, category: {}", 
                    userId, amount, category);
            
            // Tạo request body
            com.example.backend.dto.AnomalyDetectionRequest request = 
                com.example.backend.dto.AnomalyDetectionRequest.builder()
                    .userId(userId)
                    .amount(amount)
                    .merchant(merchant != null ? merchant : "Unknown")
                    .category(category != null ? category : "Khác")
                    .timestamp(java.time.LocalDateTime.now())
                    .build();
            
            ResponseEntity<com.example.backend.dto.AnomalyDetectionResult> response = 
                restTemplate.postForEntity(
                    url,
                    request,
                    com.example.backend.dto.AnomalyDetectionResult.class);

            com.example.backend.dto.AnomalyDetectionResult result = response.getBody();

            if (result != null && Boolean.TRUE.equals(result.getIsAnomaly())) {
                log.warn("🚨 Anomaly detected for user {}: {} - score: {}", 
                        userId, result.getReason(), result.getAnomalyScore());
            }

            return result;

        } catch (Exception e) {
            log.warn("Failed to check anomaly: {}", e.getMessage());
            return null;
        }
    }
}

