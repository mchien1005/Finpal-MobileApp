package com.example.backend.service;

import com.example.backend.dto.SystemSettingsResponse;
import com.example.backend.dto.SystemSettingsUpdateRequest;
import com.example.backend.model.SystemSettings;
import com.example.backend.model.User;
import com.example.backend.repository.SystemSettingsRepository;
import com.example.backend.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service quản lý cài đặt hệ thống
 * Chức năng: CRUD cài đặt, khởi tạo giá trị mặc định
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AdminSystemSettingsService {

    private final SystemSettingsRepository settingsRepository;
    private final UserRepository userRepository;

    // Các khóa cài đặt hệ thống
    public static final String KEY_AI_CONFIDENCE_THRESHOLD = "ai.confidence.threshold";
    public static final String KEY_AI_RETRAINING_FREQUENCY = "ai.retraining.frequency";
    public static final String KEY_AI_STRATEGY = "ai.categorization.strategy";
    public static final String KEY_NOTIFICATION_PUSH_ENABLED = "notification.push.enabled";
    public static final String KEY_NOTIFICATION_EMAIL_ENABLED = "notification.email.enabled";
    public static final String KEY_SYSTEM_MAINTENANCE_MODE = "system.maintenance.mode";

    /**
     * Khởi tạo các cài đặt mặc định khi ứng dụng khởi động
     */
    @PostConstruct
    @Transactional
    public void initializeDefaultSettings() {
        log.info("Đang khởi tạo cài đặt hệ thống mặc định...");

        // Cài đặt AI
        createSettingIfNotExists(KEY_AI_CONFIDENCE_THRESHOLD, "0.70", 
            "Ngưỡng độ tin cậy AI tối thiểu (0.0 - 1.0)", "AI", "DOUBLE");
        
        createSettingIfNotExists(KEY_AI_RETRAINING_FREQUENCY, "DAILY", 
            "Tần suất huấn luyện lại AI (DAILY, WEEKLY, MONTHLY)", "AI", "STRING");
        
        createSettingIfNotExists(KEY_AI_STRATEGY, "RULE_FIRST", 
            "Chiến lược phân loại (AI_FIRST, RULE_FIRST, HYBRID, RULE_ONLY)", "AI", "STRING");

        // Cài đặt thông báo
        createSettingIfNotExists(KEY_NOTIFICATION_PUSH_ENABLED, "true", 
            "Bật/tắt thông báo đẩy hệ thống", "NOTIFICATION", "BOOLEAN");
        
        createSettingIfNotExists(KEY_NOTIFICATION_EMAIL_ENABLED, "true", 
            "Bật/tắt thông báo email hệ thống", "NOTIFICATION", "BOOLEAN");

        // Cài đặt hệ thống
        createSettingIfNotExists(KEY_SYSTEM_MAINTENANCE_MODE, "false", 
            "Bật/tắt chế độ bảo trì hệ thống", "SYSTEM", "BOOLEAN");

        log.info("Đã khởi tạo xong cài đặt hệ thống mặc định");
    }

    /**
     * Tạo cài đặt nếu chưa tồn tại
     */
    private void createSettingIfNotExists(String key, String value, String description, 
                                          String group, String dataType) {
        if (!settingsRepository.existsBySettingKey(key)) {
            SystemSettings setting = SystemSettings.builder()
                    .settingKey(key)
                    .settingValue(value)
                    .description(description)
                    .settingGroup(group)
                    .dataType(dataType)
                    .build();
            settingsRepository.save(setting);
            log.debug("Đã tạo cài đặt: {} = {}", key, value);
        }
    }

    /**
     * Lấy tất cả cài đặt hệ thống
     */
    @Transactional(readOnly = true)
    public List<SystemSettingsResponse> getAllSettings() {
        return settingsRepository.findAllByOrderBySettingGroupAscSettingKeyAsc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy cài đặt theo nhóm
     */
    @Transactional(readOnly = true)
    public List<SystemSettingsResponse> getSettingsByGroup(String group) {
        return settingsRepository.findBySettingGroup(group)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy giá trị của 1 cài đặt
     */
    @Transactional(readOnly = true)
    public Optional<String> getSettingValue(String key) {
        return settingsRepository.findBySettingKey(key)
                .map(SystemSettings::getSettingValue);
    }

    /**
     * Lấy giá trị cài đặt dạng Double
     */
    public Double getDoubleValue(String key, double defaultValue) {
        return getSettingValue(key)
                .map(v -> {
                    try {
                        return Double.parseDouble(v);
                    } catch (NumberFormatException e) {
                        return defaultValue;
                    }
                })
                .orElse(defaultValue);
    }

    /**
     * Lấy giá trị cài đặt dạng Boolean
     */
    public Boolean getBooleanValue(String key, boolean defaultValue) {
        return getSettingValue(key)
                .map(Boolean::parseBoolean)
                .orElse(defaultValue);
    }

    /**
     * Cập nhật giá trị cài đặt
     */
    @Transactional
    public SystemSettingsResponse updateSetting(SystemSettingsUpdateRequest request, String username) {
        SystemSettings setting = settingsRepository.findBySettingKey(request.getSettingKey())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cài đặt: " + request.getSettingKey()));

        // Validate giá trị theo kiểu dữ liệu
        validateSettingValue(setting.getDataType(), request.getSettingValue());

        // Lấy user ID để lưu người cập nhật
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        setting.setSettingValue(request.getSettingValue());
        setting.setUpdatedBy(user.getId());

        SystemSettings updated = settingsRepository.save(setting);
        log.info("Admin {} đã cập nhật cài đặt: {} = {}", username, request.getSettingKey(), request.getSettingValue());

        return toResponse(updated);
    }

    /**
     * Cập nhật nhiều cài đặt cùng lúc
     */
    @Transactional
    public List<SystemSettingsResponse> updateMultipleSettings(List<SystemSettingsUpdateRequest> requests, String username) {
        return requests.stream()
                .map(req -> updateSetting(req, username))
                .collect(Collectors.toList());
    }

    /**
     * Validate giá trị cài đặt theo kiểu dữ liệu
     */
    private void validateSettingValue(String dataType, String value) {
        if (dataType == null) return;

        switch (dataType.toUpperCase()) {
            case "INTEGER":
                try {
                    Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Giá trị phải là số nguyên: " + value);
                }
                break;
            case "DOUBLE":
                try {
                    double d = Double.parseDouble(value);
                    // Đặc biệt với confidence threshold, phải từ 0-1
                    if (d < 0 || d > 1) {
                        throw new RuntimeException("Giá trị phải từ 0.0 đến 1.0: " + value);
                    }
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Giá trị phải là số thực: " + value);
                }
                break;
            case "BOOLEAN":
                if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
                    throw new RuntimeException("Giá trị phải là true hoặc false: " + value);
                }
                break;
            // STRING không cần validate
        }
    }

    /**
     * Lấy tất cả cài đặt dưới dạng Map
     */
    @Transactional(readOnly = true)
    public Map<String, String> getAllSettingsAsMap() {
        return settingsRepository.findAll().stream()
                .collect(Collectors.toMap(
                        SystemSettings::getSettingKey,
                        s -> s.getSettingValue() != null ? s.getSettingValue() : ""
                ));
    }

    /**
     * Convert entity sang response DTO
     */
    private SystemSettingsResponse toResponse(SystemSettings setting) {
        return SystemSettingsResponse.builder()
                .id(setting.getId())
                .settingKey(setting.getSettingKey())
                .settingValue(setting.getSettingValue())
                .description(setting.getDescription())
                .settingGroup(setting.getSettingGroup())
                .dataType(setting.getDataType())
                .updatedAt(setting.getUpdatedAt())
                .build();
    }
}
