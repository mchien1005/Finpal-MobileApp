package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for spending breakdown by category
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpendingByCategoryDTO {

    private Long categoryId;
    private String categoryName;
    private String categoryIcon;
    private String categoryColor;
    private BigDecimal totalAmount;
    private Long transactionCount;
    private Double percentage; // % of total spending

    public SpendingByCategoryDTO(Long categoryId, String categoryName, String categoryIcon,
            String categoryColor, BigDecimal totalAmount, Long transactionCount) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.categoryIcon = categoryIcon;
        this.categoryColor = categoryColor;
        this.totalAmount = totalAmount;
        this.transactionCount = transactionCount;
    }
}
