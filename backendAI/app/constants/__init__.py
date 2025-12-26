"""
Constants Package - Các hằng số của ứng dụng

- notification_templates.py: Mã template thông báo (ma_mau)
"""

from app.constants.notification_templates import (
    # AI Templates
    BUDGET_WARNING,
    BUDGET_EXCEEDED,
    SAVINGS_SUGGESTION,
    ANOMALY_DETECTED,
    SPENDING_ACHIEVEMENT,
    SPENDING_TIP,
    GOAL_REMINDER_7DAYS,
    GOAL_DEADLINE_TODAY,
    GOAL_COMPLETED,
    MONTHLY_SUMMARY,
    WEEKLY_SUMMARY,
    # Prediction & Anomaly
    ANOMALY_TRANSACTION,
    ANOMALY_TIME,
    PREDICTION_MONTHLY,
    PREDICTION_CATEGORY,
    # Legacy
    LEGACY_BUDGET_ALERT,
    LEGACY_ANOMALY,
    LEGACY_SAVINGS,
    LEGACY_PAYMENT_REMINDER,
    LEGACY_GOAL_REMINDER,
    # Helper
    get_all_template_codes,
)

__all__ = [
    "BUDGET_WARNING",
    "BUDGET_EXCEEDED",
    "SAVINGS_SUGGESTION",
    "ANOMALY_DETECTED",
    "SPENDING_ACHIEVEMENT",
    "SPENDING_TIP",
    "GOAL_REMINDER_7DAYS",
    "GOAL_DEADLINE_TODAY",
    "GOAL_COMPLETED",
    "MONTHLY_SUMMARY",
    "WEEKLY_SUMMARY",
    "ANOMALY_TRANSACTION",
    "ANOMALY_TIME",
    "PREDICTION_MONTHLY",
    "PREDICTION_CATEGORY",
    "LEGACY_BUDGET_ALERT",
    "LEGACY_ANOMALY",
    "LEGACY_SAVINGS",
    "LEGACY_PAYMENT_REMINDER",
    "LEGACY_GOAL_REMINDER",
    "get_all_template_codes",
]

