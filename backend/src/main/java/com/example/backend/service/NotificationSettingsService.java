package com.example.backend.service;

import com.example.backend.dto.NotificationSettingsDTO;
import com.example.backend.model.NotificationSettings;
import com.example.backend.model.User;
import com.example.backend.repository.NotificationSettingsRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service quản lý cài đặt thông báo của người dùng
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationSettingsService {

    private final NotificationSettingsRepository settingsRepository;
    private final UserRepository userRepository;

    /**
     * Lấy cài đặt thông báo của user
     * Nếu chưa có thì tạo mặc định
     */
    @Transactional
    public NotificationSettingsDTO getSettings(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        NotificationSettings settings = settingsRepository.findByUserId(user.getId())
                .orElseGet(() -> createDefaultSettings(user));

        return NotificationSettingsDTO.fromEntity(settings);
    }

    /**
     * Lấy cài đặt thông báo theo userId
     */
    @Transactional(readOnly = true)
    public NotificationSettings getSettingsByUserId(Long userId) {
        return settingsRepository.findByUserId(userId).orElse(null);
    }

    /**
     * Cập nhật cài đặt thông báo
     */
    @Transactional
    public NotificationSettingsDTO updateSettings(String username, NotificationSettingsDTO dto) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        NotificationSettings settings = settingsRepository.findByUserId(user.getId())
                .orElseGet(() -> createDefaultSettings(user));

        // Cập nhật các field từ DTO
        dto.updateEntity(settings);

        NotificationSettings saved = settingsRepository.save(settings);
        log.info("📱 Updated notification settings for user: {}", username);

        return NotificationSettingsDTO.fromEntity(saved);
    }

    /**
     * Tạo cài đặt mặc định cho user mới
     */
    private NotificationSettings createDefaultSettings(User user) {
        NotificationSettings settings = NotificationSettings.builder()
                .user(user)
                .pushEnabled(true)
                .transactionAlerts(true)
                .budgetAlerts(true)
                .goalReminders(true)
                .securityAlerts(true)
                .weeklyReport(true)
                .monthlyReport(true)
                .savingsTips(true)
                .spendingInsights(true)
                .build();

        NotificationSettings saved = settingsRepository.save(settings);
        log.info("📱 Created default notification settings for user: {}", user.getUsername());
        return saved;
    }

    // ============================================
    // Helper methods để check từng loại thông báo
    // ============================================

    /**
     * Kiểm tra user có bật thông báo push không
     */
    public boolean isPushEnabled(Long userId) {
        NotificationSettings settings = getSettingsByUserId(userId);
        return settings == null || Boolean.TRUE.equals(settings.getPushEnabled());
    }

    /**
     * Kiểm tra user có bật cảnh báo giao dịch không
     */
    public boolean isTransactionAlertsEnabled(Long userId) {
        NotificationSettings settings = getSettingsByUserId(userId);
        if (settings == null)
            return true; // Mặc định bật
        return Boolean.TRUE.equals(settings.getPushEnabled())
                && Boolean.TRUE.equals(settings.getTransactionAlerts());
    }

    /**
     * Kiểm tra user có bật cảnh báo ngân sách không
     */
    public boolean isBudgetAlertsEnabled(Long userId) {
        NotificationSettings settings = getSettingsByUserId(userId);
        if (settings == null)
            return true;
        return Boolean.TRUE.equals(settings.getPushEnabled())
                && Boolean.TRUE.equals(settings.getBudgetAlerts());
    }

    /**
     * Kiểm tra user có bật nhắc nhở mục tiêu không
     */
    public boolean isGoalRemindersEnabled(Long userId) {
        NotificationSettings settings = getSettingsByUserId(userId);
        if (settings == null)
            return true;
        return Boolean.TRUE.equals(settings.getPushEnabled())
                && Boolean.TRUE.equals(settings.getGoalReminders());
    }

    /**
     * Kiểm tra user có bật cảnh báo bảo mật không
     */
    public boolean isSecurityAlertsEnabled(Long userId) {
        NotificationSettings settings = getSettingsByUserId(userId);
        if (settings == null)
            return true;
        return Boolean.TRUE.equals(settings.getPushEnabled())
                && Boolean.TRUE.equals(settings.getSecurityAlerts());
    }

    /**
     * Kiểm tra user có bật báo cáo tuần không
     */
    public boolean isWeeklyReportEnabled(Long userId) {
        NotificationSettings settings = getSettingsByUserId(userId);
        if (settings == null)
            return true;
        return Boolean.TRUE.equals(settings.getPushEnabled())
                && Boolean.TRUE.equals(settings.getWeeklyReport());
    }

    /**
     * Kiểm tra user có bật báo cáo tháng không
     */
    public boolean isMonthlyReportEnabled(Long userId) {
        NotificationSettings settings = getSettingsByUserId(userId);
        if (settings == null)
            return true;
        return Boolean.TRUE.equals(settings.getPushEnabled())
                && Boolean.TRUE.equals(settings.getMonthlyReport());
    }

    /**
     * Kiểm tra user có bật gợi ý tiết kiệm không
     */
    public boolean isSavingsTipsEnabled(Long userId) {
        NotificationSettings settings = getSettingsByUserId(userId);
        if (settings == null)
            return true;
        return Boolean.TRUE.equals(settings.getPushEnabled())
                && Boolean.TRUE.equals(settings.getSavingsTips());
    }

    /**
     * Kiểm tra user có bật phân tích chi tiêu không
     */
    public boolean isSpendingInsightsEnabled(Long userId) {
        NotificationSettings settings = getSettingsByUserId(userId);
        if (settings == null)
            return true;
        return Boolean.TRUE.equals(settings.getPushEnabled())
                && Boolean.TRUE.equals(settings.getSpendingInsights());
    }
}
