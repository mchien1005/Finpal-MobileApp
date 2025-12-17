package com.example.backend.service;

import com.example.backend.dto.SavingsSuggestionsResponse;
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
 * Chức năng: Gợi ý tiết kiệm, phân tích chi tiêu proactive (dựa trên AI/ML)
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
}
