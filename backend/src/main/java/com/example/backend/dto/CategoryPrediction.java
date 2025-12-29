package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for AI Category Prediction Response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryPrediction {

    /**
     * Predicted category (e.g., "Ăn uống", "Di chuyển")
     */
    private String category;

    /**
     * Confidence score (0.0 to 1.0)
     */
    private Double confidence;

    /**
     * Alternative predictions with lower confidence
     */
    private List<Alternative> alternatives;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Alternative {
        private String category;
        private Double confidence;
    }
}
