package com.example.backend.service;

import com.example.backend.dto.UserPreferencesDTO;
import com.example.backend.model.UserPreferences;
import com.example.backend.repository.UserPreferencesRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserPreferencesService {

    private final UserPreferencesRepository userPreferencesRepository;
    private final UserRepository userRepository;

    /**
     * Lấy preferences của user, nếu chưa có thì tạo mới với giá trị mặc định
     */
    public UserPreferencesDTO getUserPreferences(String username) {
        Long userId = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();

        UserPreferences preferences = userPreferencesRepository.findByUserId(userId)
                .orElse(null);

        return UserPreferencesDTO.fromEntity(preferences);
    }

    /**
     * Cập nhật preferences của user
     */
    @Transactional
    public UserPreferencesDTO updateUserPreferences(String username, UserPreferencesDTO dto) {
        Long userId = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();

        UserPreferences preferences = userPreferencesRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserPreferences newPrefs = new UserPreferences();
                    newPrefs.setUserId(userId);
                    return newPrefs;
                });

        // Update fields if provided
        if (dto.getCurrency() != null) {
            preferences.setCurrency(dto.getCurrency());
        }
        if (dto.getLanguage() != null) {
            preferences.setLanguage(dto.getLanguage());
        }
        if (dto.getTheme() != null) {
            preferences.setTheme(dto.getTheme());
        }
        if (dto.getPushNotifications() != null) {
            preferences.setPushNotifications(dto.getPushNotifications());
        }
        if (dto.getEmailNotifications() != null) {
            preferences.setEmailNotifications(dto.getEmailNotifications());
        }
        if (dto.getTransactionAlert() != null) {
            preferences.setTransactionAlert(dto.getTransactionAlert());
        }
        if (dto.getBudgetAlert() != null) {
            preferences.setBudgetAlert(dto.getBudgetAlert());
        }
        if (dto.getGoalReminder() != null) {
            preferences.setGoalReminder(dto.getGoalReminder());
        }
        if (dto.getWeeklyReport() != null) {
            preferences.setWeeklyReport(dto.getWeeklyReport());
        }
        if (dto.getMonthlyReport() != null) {
            preferences.setMonthlyReport(dto.getMonthlyReport());
        }
        if (dto.getBudgetAlertThreshold() != null) {
            preferences.setBudgetAlertThreshold(dto.getBudgetAlertThreshold());
        }

        UserPreferences saved = userPreferencesRepository.save(preferences);
        return UserPreferencesDTO.fromEntity(saved);
    }

    /**
     * Cập nhật notification settings
     */
    @Transactional
    public UserPreferencesDTO updateNotificationSettings(String username, UserPreferencesDTO dto) {
        Long userId = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();

        UserPreferences preferences = userPreferencesRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserPreferences newPrefs = new UserPreferences();
                    newPrefs.setUserId(userId);
                    return newPrefs;
                });

        // Only update notification-related fields
        if (dto.getPushNotifications() != null) {
            preferences.setPushNotifications(dto.getPushNotifications());
        }
        if (dto.getEmailNotifications() != null) {
            preferences.setEmailNotifications(dto.getEmailNotifications());
        }
        if (dto.getTransactionAlert() != null) {
            preferences.setTransactionAlert(dto.getTransactionAlert());
        }
        if (dto.getBudgetAlert() != null) {
            preferences.setBudgetAlert(dto.getBudgetAlert());
        }
        if (dto.getGoalReminder() != null) {
            preferences.setGoalReminder(dto.getGoalReminder());
        }
        if (dto.getWeeklyReport() != null) {
            preferences.setWeeklyReport(dto.getWeeklyReport());
        }
        if (dto.getMonthlyReport() != null) {
            preferences.setMonthlyReport(dto.getMonthlyReport());
        }

        UserPreferences saved = userPreferencesRepository.save(preferences);
        return UserPreferencesDTO.fromEntity(saved);
    }
}
