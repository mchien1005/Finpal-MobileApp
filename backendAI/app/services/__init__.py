"""
Services Package - Các service của ứng dụng

- database.py: Kết nối và truy vấn MySQL database
- training_history.py: Quản lý lịch sử training ML models
- auto_retrain.py: Tự động train lại models hằng ngày
"""

from app.services.database import (
    DatabaseService, 
    get_database_service,
    is_mysql_available,
    PYMYSQL_AVAILABLE
)

# Training history functions
from app.services.training_history import (
    record_training,
    record_prediction,
    get_accuracy_history,
    get_all_models_accuracy_history,
    get_model_stats,
    get_prediction_logs,
    get_model_accuracy_from_logs
)

# Auto-retrain functions (lazy import để tránh lỗi khi khởi động)
# from app.services.auto_retrain import ...

__all__ = [
    "DatabaseService",
    "get_database_service",
    "is_mysql_available",
    "PYMYSQL_AVAILABLE",
    "record_training",
    "record_prediction",
    "get_accuracy_history",
    "get_all_models_accuracy_history",
    "get_model_stats",
    "get_prediction_logs",
    "get_model_accuracy_from_logs"
]


