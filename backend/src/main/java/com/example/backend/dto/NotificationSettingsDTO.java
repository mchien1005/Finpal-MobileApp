package com.example.backend.dto;

import com.example.backend.model.NotificationSettings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho cài đặt thông báo
 * Dùng để truyền dữ liệu giữa client và server
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettingsDTO {

    // Toggle tổng - Bật/tắt nhận thông báo trên thiết bị
    private Boolean pushEnabled;

    // Loại thông báo
    private Boolean transactionAlerts; // Cảnh báo giao dịch
    private Boolean budgetAlerts; // Cảnh báo ngân sách
    private Boolean goalReminders; // Nhắc nhở mục tiêu
    private Boolean securityAlerts; // Cảnh báo bảo mật

    // Báo cáo định kỳ
    private Boolean weeklyReport; // Báo cáo tuần
    private Boolean monthlyReport; // Báo cáo tháng

    // Thông báo khác
    private Boolean savingsTips; // Gợi ý tiết kiệm
    private Boolean spendingInsights; // Phân tích chi tiêu

    /**
     * Chuyển từ Entity sang DTO
     */
    public static NotificationSettingsDTO fromEntity(NotificationSettings entity) {
        return NotificationSettingsDTO.builder()
                .pushEnabled(entity.getPushEnabled())
                .transactionAlerts(entity.getTransactionAlerts())
                .budgetAlerts(entity.getBudgetAlerts())
                .goalReminders(entity.getGoalReminders())
                .securityAlerts(entity.getSecurityAlerts())
                .weeklyReport(entity.getWeeklyReport())
                .monthlyReport(entity.getMonthlyReport())
                .savingsTips(entity.getSavingsTips())
                .spendingInsights(entity.getSpendingInsights())
                .build();
    }

    /**
     * Cập nhật Entity từ DTO (chỉ update các field không null)
     */
    public void updateEntity(NotificationSettings entity) {
        if (pushEnabled != null)
            entity.setPushEnabled(pushEnabled);
        if (transactionAlerts != null)
            entity.setTransactionAlerts(transactionAlerts);
        if (budgetAlerts != null)
            entity.setBudgetAlerts(budgetAlerts);
        if (goalReminders != null)
            entity.setGoalReminders(goalReminders);
        if (securityAlerts != null)
            entity.setSecurityAlerts(securityAlerts);
        if (weeklyReport != null)
            entity.setWeeklyReport(weeklyReport);
        if (monthlyReport != null)
            entity.setMonthlyReport(monthlyReport);
        if (savingsTips != null)
            entity.setSavingsTips(savingsTips);
        if (spendingInsights != null)
            entity.setSpendingInsights(spendingInsights);
    }

    /**
     * Tạo cài đặt mặc định (tất cả đều bật)
     */
    public static NotificationSettingsDTO getDefault() {
        return NotificationSettingsDTO.builder()
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
    }
}
