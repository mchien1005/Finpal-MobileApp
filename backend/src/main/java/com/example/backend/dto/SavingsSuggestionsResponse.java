package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for AI Savings Suggestions Response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SavingsSuggestionsResponse {

    private Long userId;
    private List<SavingsSuggestion> suggestions;
    private Double totalPotentialSavings;
    private Integer analyzedMonths;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SavingsSuggestion {
        private String category;
        private Double currentWeeklyAvg;
        private Double suggestedWeeklyTarget;
        private Double monthlySavings;
        private String message;
    }
}
