"""
Notification Template Constants - Mã template thông báo

File này chứa các hằng số mã template (ma_mau) để đồng bộ giữa code và database.
Khi thay đổi ma_mau trong database, chỉ cần cập nhật file này.

Mapping với bảng mau_thong_bao:
- NOT001-NOT005: Templates cũ (đã có sẵn)
- NOT006-NOT015: Templates cho AI Insights
"""

# =====================================================
# AI Insights Templates (NOT006 - NOT015)
# =====================================================

# Cảnh báo ngân sách - Budget warning when approaching limit
# Placeholders: {percentage}, {budget_name}, {spent_amount}, {budget_amount}, {days_remaining}
BUDGET_WARNING = "NOT006"

# Ngân sách vượt quá - Budget exceeded
# Placeholders: {percentage}, {budget_name}, {spent_amount}, {budget_amount}
BUDGET_EXCEEDED = "NOT007"

# Gợi ý tiết kiệm thông minh - Smart savings suggestion
# Placeholders: {weekly_avg}, {category}, {suggested_weekly}, {monthly_savings}
SAVINGS_SUGGESTION = "NOT008"

# Phát hiện chi tiêu bất thường - Anomaly detected
# Placeholders: {category}, {current_amount}, {increase_percent}, {average_amount}
ANOMALY_DETECTED = "NOT009"

# Thành tích tiết kiệm - Spending achievement
# Placeholders: {category}, {save_percent}
SPENDING_ACHIEVEMENT = "NOT010"

# Mẹo quản lý chi tiêu - Spending tip
# Placeholders: {category}
SPENDING_TIP = "NOT011"

# Nhắc mục tiêu còn 7 ngày - Goal reminder 7 days
# Placeholders: {goal_name}, {progress}, {current_amount}, {target_amount}
GOAL_REMINDER_7DAYS = "NOT012"

# Deadline mục tiêu hôm nay - Goal deadline today
# Placeholders: {goal_name}, {progress}, {current_amount}, {target_amount}
GOAL_DEADLINE_TODAY = "NOT013"

# Chúc mừng đạt mục tiêu - Goal completed
# Placeholders: {goal_name}, {target_amount}
GOAL_COMPLETED = "NOT014"

# Tổng kết tháng - Monthly summary
# Placeholders: {month}, {total_income}, {total_expense}, {savings}, {savings_percent}
MONTHLY_SUMMARY = "NOT015"

# Tổng kết tuần - Weekly summary
# Placeholders: {total_income}, {total_expense}, {savings}, {savings_percent}
WEEKLY_SUMMARY = "NOT016"

# =====================================================
# Anomaly Detection & Prediction Templates (NOT017 - NOT020)
# =====================================================

# Giao dịch bất thường - Anomaly transaction detected
# Placeholders: {amount}, {merchant}, {category}, {times}, {average}
ANOMALY_TRANSACTION = "NOT017"

# Thời gian giao dịch lạ - Unusual transaction time
# Placeholders: {amount}, {merchant}, {time}
ANOMALY_TIME = "NOT018"

# Dự đoán chi tiêu tháng tới - Monthly spending prediction
# Placeholders: {month}, {predicted_amount}, {trend}, {change_percent}, {recommendation}
PREDICTION_MONTHLY = "NOT019"

# Dự đoán chi tiêu theo danh mục - Category spending prediction
# Placeholders: {category}, {month}, {predicted_amount}, {average}, {trend}
PREDICTION_CATEGORY = "NOT020"


# =====================================================
# Legacy Templates (NOT001 - NOT005)
# =====================================================

# Chi tiêu vượt ngân sách (legacy)
# Placeholders: {amount}, {category}, {percent}
LEGACY_BUDGET_ALERT = "NOT001"

# Giao dịch bất thường (legacy)
# Placeholders: {amount}, {location}
LEGACY_ANOMALY = "NOT002"

# Tiết kiệm tốt (legacy)
# Placeholders: {amount}
LEGACY_SAVINGS = "NOT003"

# Nhắc nhở thanh toán (legacy)
# Placeholders: {billName}, {dueDate}
LEGACY_PAYMENT_REMINDER = "NOT004"

# Mục tiêu tiết kiệm (legacy)
# Placeholders: {daysLeft}, {goalName}
LEGACY_GOAL_REMINDER = "NOT005"


# =====================================================
# Helper function
# =====================================================

def get_all_template_codes() -> dict:
    """
    Lấy tất cả template codes
    
    Returns:
        Dict mapping tên biến -> mã template
    """
    return {
        # AI Templates
        "BUDGET_WARNING": BUDGET_WARNING,
        "BUDGET_EXCEEDED": BUDGET_EXCEEDED,
        "SAVINGS_SUGGESTION": SAVINGS_SUGGESTION,
        "ANOMALY_DETECTED": ANOMALY_DETECTED,
        "SPENDING_ACHIEVEMENT": SPENDING_ACHIEVEMENT,
        "SPENDING_TIP": SPENDING_TIP,
        "GOAL_REMINDER_7DAYS": GOAL_REMINDER_7DAYS,
        "GOAL_DEADLINE_TODAY": GOAL_DEADLINE_TODAY,
        "GOAL_COMPLETED": GOAL_COMPLETED,
        "MONTHLY_SUMMARY": MONTHLY_SUMMARY,
        # Legacy
        "LEGACY_BUDGET_ALERT": LEGACY_BUDGET_ALERT,
        "LEGACY_ANOMALY": LEGACY_ANOMALY,
        "LEGACY_SAVINGS": LEGACY_SAVINGS,
        "LEGACY_PAYMENT_REMINDER": LEGACY_PAYMENT_REMINDER,
        "LEGACY_GOAL_REMINDER": LEGACY_GOAL_REMINDER,
    }
