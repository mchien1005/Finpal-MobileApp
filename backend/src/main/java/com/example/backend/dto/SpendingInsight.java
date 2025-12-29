package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for AI Spending Insight
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpendingInsight {

    /**
     * Type: warning, tip, achievement
     */
    private String insightType;

    /**
     * Related category (optional)
     */
    private String category;

    /**
     * Human-readable message
     */
    private String message;

    /**
     * Whether user can take action
     */
    private Boolean actionable;

    /**
     * Impact score (0.0 to 1.0)
     */
    private Double impactScore;
}
