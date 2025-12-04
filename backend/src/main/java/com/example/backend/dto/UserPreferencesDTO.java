package com.example.backend.dto;

import com.example.backend.model.UserPreferences;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferencesDTO {

    // General settings
    private String currency;
    private String language;
    private String theme;

    // Notification channel
    private Boolean pushNotifications;
    private Boolean emailNotifications;

    // Notification types
    private Boolean transactionAlert;
    private Boolean budgetAlert;
    private Boolean goalReminder;

    // Periodic reports
    private Boolean weeklyReport;
    private Boolean monthlyReport;

    // Budget alert threshold (percentage)
    private Integer budgetAlertThreshold;

    public static UserPreferencesDTO fromEntity(UserPreferences preferences) {
        if (preferences == null) {
            return getDefaultPreferences();
        }
        return UserPreferencesDTO.builder()
                .currency(preferences.getCurrency())
                .language(preferences.getLanguage())
                .theme(preferences.getTheme())
                .pushNotifications(preferences.getPushNotifications())
                .emailNotifications(preferences.getEmailNotifications())
                .transactionAlert(preferences.getTransactionAlert())
                .budgetAlert(preferences.getBudgetAlert())
                .goalReminder(preferences.getGoalReminder())
                .weeklyReport(preferences.getWeeklyReport())
                .monthlyReport(preferences.getMonthlyReport())
                .budgetAlertThreshold(preferences.getBudgetAlertThreshold())
                .build();
    }

    public static UserPreferencesDTO getDefaultPreferences() {
        return UserPreferencesDTO.builder()
                .currency("VND")
                .language("vi")
                .theme("light")
                .pushNotifications(true)
                .emailNotifications(true)
                .transactionAlert(true)
                .budgetAlert(true)
                .goalReminder(true)
                .weeklyReport(false)
                .monthlyReport(true)
                .budgetAlertThreshold(70)
                .build();
    }
}
