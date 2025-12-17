package com.example.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("user_id")
    private Long userId;
    
    private List<SavingsSuggestion> suggestions;
    
    @JsonProperty("total_potential_savings")
    private Double totalPotentialSavings;
    
    @JsonProperty("analyzed_months")
    private Integer analyzedMonths;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SavingsSuggestion {
        private String category;
        
        @JsonProperty("current_weekly_avg")
        private Double currentWeeklyAvg;
        
        @JsonProperty("suggested_weekly_target")
        private Double suggestedWeeklyTarget;
        
        @JsonProperty("monthly_savings")
        private Double monthlySavings;
        
        private String message;
    }
}
