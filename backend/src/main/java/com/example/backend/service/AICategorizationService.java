package com.example.backend.service;

import com.example.backend.dto.CategoryPrediction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Service tích hợp với AI Backend để phân loại giao dịch tự động
 * Gọi API đến AI service (Python FastAPI) để dự đoán category dựa trên merchant
 * và amount
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AICategorizationService {

    private final RestTemplate restTemplate;

    @Value("${ai.backend.url:http://localhost:8000}")
    private String aiBackendUrl;

    @Value("${ai.backend.enabled:true}")
    private boolean aiEnabled;

    /**
     * Dự đoán category của giao dịch bằng AI model
     * 
     * @param merchant    Tên merchant (ví dụ: "GRAB", "SHOPEE")
     * @param amount      Số tiền giao dịch
     * @param description Mô tả giao dịch (tùy chọn)
     * @param userId      ID người dùng (để tracking trong AI logs)
     * @return CategoryPrediction chứa category và độ confidence, hoặc null nếu AI
     *         lỗi
     */
    public CategoryPrediction predictCategory(String merchant, Double amount, String description, Long userId) {
        if (!aiEnabled) {
            log.debug("AI categorization is disabled");
            return null;
        }

        try {
            String url = aiBackendUrl + "/api/categorization/predict";

            // Chuẩn bị request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("merchant", merchant != null ? merchant : "");
            requestBody.put("amount", amount != null ? amount : 0.0);
            requestBody.put("description", description != null ? description : "");
            requestBody.put("user_id", userId != null ? "USR" + String.format("%03d", userId) : null);

            // Thiết lập headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // Gọi AI service (Python FastAPI backend)
            log.debug("Calling AI categorization for merchant: {}", merchant);
            ResponseEntity<CategoryPrediction> response = restTemplate.postForEntity(
                    url,
                    request,
                    CategoryPrediction.class);

            CategoryPrediction prediction = response.getBody();

            if (prediction != null) {
                log.info("AI predicted category '{}' with confidence {} for merchant '{}'",
                        prediction.getCategory(),
                        prediction.getConfidence(),
                        merchant);
                return prediction;
            }

            return null;

        } catch (RestClientException e) {
            log.warn("Failed to call AI categorization service: {}. Falling back to rule-based.",
                    e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Unexpected error in AI categorization", e);
            return null;
        }
    }

    /**
     * Kiểm tra AI service có hoạt động không
     */
    public boolean isAIServiceAvailable() {
        if (!aiEnabled) {
            return false;
        }

        try {
            String healthUrl = aiBackendUrl + "/health";
            ResponseEntity<Object> response = restTemplate.getForEntity(healthUrl, Object.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.debug("AI service health check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Ngưỡng confidence tối thiểu để chấp nhận kết quả AI (mặc định 70%)
     */
    @Value("${ai.confidence.threshold:0.70}")
    private double confidenceThreshold;

    /**
     * Kiểm tra kết quả AI có đủ confidence hay không
     * 
     * @return true nếu confidence >= threshold (70%)
     */
    public boolean isConfidentPrediction(CategoryPrediction prediction) {
        return prediction != null
                && prediction.getConfidence() != null
                && prediction.getConfidence() >= confidenceThreshold;
    }
}
