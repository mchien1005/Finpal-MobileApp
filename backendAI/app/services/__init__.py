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
from app.services.training_history import TrainingHistoryService
from app.services.auto_retrain import (
    start_auto_retrain_scheduler,
    stop_auto_retrain_scheduler,
    is_scheduler_running,
    get_scheduler_status,
    retrain_all_models
)

__all__ = [
    "DatabaseService",
    "get_database_service",
    "is_mysql_available",
    "PYMYSQL_AVAILABLE",
    "TrainingHistoryService",
    "start_auto_retrain_scheduler",
    "stop_auto_retrain_scheduler",
    "is_scheduler_running",
    "get_scheduler_status",
    "retrain_all_models"
]

