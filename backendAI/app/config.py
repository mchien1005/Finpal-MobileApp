"""
Application Configuration - Cấu hình ứng dụng

Quản lý tất cả các biến môi trường và settings của ứng dụng.
Sử dụng Pydantic Settings để validate và type-check các config.
"""

from pydantic_settings import BaseSettings
from functools import lru_cache


class Settings(BaseSettings):
    """
    Application Settings - Cài đặt ứng dụng
    
    Class này chứa tất cả các cấu hình của ứng dụng.
    Giá trị có thể được override bằng biến môi trường hoặc file .env
    """
    
    # Application - Thông tin ứng dụng
    APP_NAME: str = "Finpal AI Backend"
    APP_VERSION: str = "1.0.0"
    DEBUG: bool = True              # Chế độ debug (auto-reload, verbose logging)
    PORT: int = 8000                # Port chạy server
    
    # API - Cấu hình API endpoints
    API_PREFIX: str = "/api"        # Tiền tố cho tất cả API routes
    API_VERSION: str = "v1"         # Phiên bản API
    
    # CORS - Cross-Origin Resource Sharing
    CORS_ORIGINS: str = "http://localhost:3000,http://localhost:8080"  # Các origin được phép gọi API
    
    # Database (optional) - Cấu hình database (hiện tại chưa dùng)
    DB_HOST: str = "localhost"
    DB_PORT: int = 3306
    DB_NAME: str = "finpal_db"
    DB_USER: str = "root"
    DB_PASSWORD: str = ""
    
    # Model Settings - Cài đặt ML models
    MODEL_PATH: str = "data/models"  # Thư mục lưu trained models
    RETRAIN_INTERVAL: int = 7        # Khoảng thời gian retrain model (ngày)
    
    # Java Backend Integration - Tích hợp với backend Java
    JAVA_BACKEND_URL: str = "http://localhost:8080"  # URL của backend Java (Spring Boot)
    
    # Logging - Cấu hình logging
    LOG_LEVEL: str = "INFO"         # Level: DEBUG, INFO, WARNING, ERROR
    LOG_FILE: str = "logs/app.log"  # File lưu logs
    
    # ML Model Parameters - Tham số cho các ML models
    CATEGORIZATION_MODEL: str = "random_forest"  # Model phân loại: random_forest, svm, neural_net
    ANOMALY_THRESHOLD: float = 0.7               # Ngưỡng phát hiện anomaly (0-1)
    PREDICTION_WINDOW: int = 30                  # Số ngày dự đoán chi tiêu tương lai
    
    class Config:
        env_file = ".env"           # Đọc config từ file .env
        case_sensitive = True       # Phân biệt hoa/thường cho tên biến


@lru_cache()
def get_settings() -> Settings:
    """
    Get cached settings instance - Lấy instance settings đã cache
    
    Sử dụng lru_cache để chỉ khởi tạo Settings một lần duy nhất,
    các lần gọi sau sẽ trả về instance đã cache.
    
    Returns:
        Settings: Singleton instance của Settings
    """
    return Settings()


# Singleton instance - Khởi tạo settings một lần duy nhất
# Sử dụng settings này trong toàn bộ ứng dụng
settings = get_settings()
