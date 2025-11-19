package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for spending insights and patterns
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpendingInsightDTO {

    private String insightType; // FREQUENT_MERCHANT, HIGH_SPENDING_CATEGORY, UNUSUAL_TRANSACTION...
    private String title;
    private String description;
    private String severity; // INFO, WARNING, ALERT

    private String categoryName;
    private String merchantName;
    private BigDecimal amount;
    private Integer frequency; // số lần xuất hiện

    private String suggestion; // khuyến nghị
}
