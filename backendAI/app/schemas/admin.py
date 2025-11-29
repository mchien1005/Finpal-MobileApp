"""
Admin Schemas - Pydantic Schemas cho Admin APIs

Chứa các data models cho Admin AI Model Management:
- ModelInfo: Thông tin về một AI model
- ModelMetrics: Metrics của model (accuracy, predictions, etc.)
- AccuracyHistory: Lịch sử accuracy theo ngày
- PredictionLog: Log các predictions
- RetrainRequest/Response: Request/Response cho retrain
"""

from pydantic import BaseModel, Field
from datetime import datetime
from typing import Optional, List
from enum import Enum


class ModelStatus(str, Enum):
    """Trạng thái của AI model"""
    ACTIVE = "Active"
    TRAINING = "Training"
    INACTIVE = "Inactive"
    ERROR = "Error"


class ModelInfo(BaseModel):
    """
    Thông tin cơ bản của một AI model
    """
    name: str = Field(..., description="Tên model (ví dụ: Category Classification)")
    version: str = Field(..., description="Phiên bản model (ví dụ: v2.1.3)")
    description: str = Field(..., description="Mô tả chức năng của model")
    algorithm: str = Field(..., description="Thuật toán sử dụng (Random Forest, Isolation Forest, etc.)")
    status: ModelStatus = Field(..., description="Trạng thái hiện tại")
    last_trained: Optional[datetime] = Field(None, description="Thời gian train gần nhất")
    created_at: datetime = Field(default_factory=datetime.now, description="Thời gian tạo model")
    
    class Config:
        json_schema_extra = {
            "example": {
                "name": "Category Classification",
                "version": "v2.1.3",
                "description": "Phân loại giao dịch tự động vào các danh mục",
                "algorithm": "Random Forest",
                "status": "Active",
                "last_trained": "2025-11-20T10:30:00",
                "created_at": "2025-01-01T00:00:00"
            }
        }


class ModelMetrics(BaseModel):
    """
    Metrics hiệu suất của AI model
    """
    model_name: str = Field(..., description="Tên model")
    accuracy: float = Field(..., ge=0, le=100, description="Độ chính xác (%)")
    confidence: float = Field(..., ge=0, le=100, description="Độ tin cậy trung bình (%)")
    total_predictions: int = Field(..., ge=0, description="Tổng số predictions đã thực hiện")
    predictions_today: int = Field(..., ge=0, description="Số predictions hôm nay")
    low_confidence_count: int = Field(..., ge=0, description="Số predictions có confidence thấp (<70%)")
    correct_predictions: int = Field(..., ge=0, description="Số predictions đúng (có feedback)")
    incorrect_predictions: int = Field(..., ge=0, description="Số predictions sai (có feedback)")
    
    class Config:
        json_schema_extra = {
            "example": {
                "model_name": "Category Classification",
                "accuracy": 94.2,
                "confidence": 92.1,
                "total_predictions": 45234,
                "predictions_today": 1234,
                "low_confidence_count": 89,
                "correct_predictions": 42500,
                "incorrect_predictions": 2734
            }
        }


class ModelDetailResponse(BaseModel):
    """
    Response chi tiết của một model bao gồm info và metrics
    """
    info: ModelInfo
    metrics: ModelMetrics


class ModelsListResponse(BaseModel):
    """
    Response danh sách tất cả models
    """
    models: List[ModelDetailResponse]
    total_models: int
    active_models: int
    avg_accuracy: float
    total_predictions_today: int
    total_low_confidence: int


class AccuracyDataPoint(BaseModel):
    """
    Một điểm dữ liệu accuracy theo ngày
    """
    date: str = Field(..., description="Ngày (format: YYYY-MM-DD hoặc tên viết tắt)")
    accuracy: float = Field(..., ge=0, le=100, description="Accuracy (%)")
    predictions_count: int = Field(..., ge=0, description="Số predictions trong ngày")


class ModelAccuracyHistory(BaseModel):
    """
    Lịch sử accuracy của một model
    """
    model_name: str
    history: List[AccuracyDataPoint]
    avg_accuracy: float
    trend: str = Field(..., description="Xu hướng: increasing, decreasing, stable")


class AccuracyHistoryResponse(BaseModel):
    """
    Response lịch sử accuracy của tất cả models
    """
    period_days: int = Field(..., description="Số ngày trong khoảng thời gian")
    models: List[ModelAccuracyHistory]
    

class PredictionLogEntry(BaseModel):
    """
    Một entry trong prediction log
    """
    id: int
    timestamp: datetime
    user_id: str
    input_text: str = Field(..., description="Input (merchant name, etc.)")
    predicted_category: str
    confidence: float = Field(..., ge=0, le=100)
    actual_category: Optional[str] = Field(None, description="Category thực tế (từ user feedback)")
    is_correct: Optional[bool] = Field(None, description="Prediction có đúng không")
    model_name: str
    
    class Config:
        json_schema_extra = {
            "example": {
                "id": 1,
                "timestamp": "2025-11-20T10:45:00",
                "user_id": "USR001",
                "input_text": "GRAB VIETNAM",
                "predicted_category": "Di chuyển",
                "confidence": 96.5,
                "actual_category": "Di chuyển",
                "is_correct": True,
                "model_name": "Category Classification"
            }
        }


class PredictionLogsResponse(BaseModel):
    """
    Response danh sách prediction logs
    """
    logs: List[PredictionLogEntry]
    total: int
    page: int
    page_size: int
    total_pages: int


class RetrainRequest(BaseModel):
    """
    Request để retrain model(s)
    """
    model_names: Optional[List[str]] = Field(
        None, 
        description="Danh sách tên models cần retrain. None = retrain tất cả"
    )
    force: bool = Field(
        False, 
        description="Force retrain ngay cả khi chưa đến interval"
    )
    
    class Config:
        json_schema_extra = {
            "example": {
                "model_names": ["Category Classification", "Anomaly Detection"],
                "force": False
            }
        }


class RetrainStatus(BaseModel):
    """
    Trạng thái retrain của một model
    """
    model_name: str
    status: str = Field(..., description="started, completed, failed")
    message: str
    old_accuracy: Optional[float] = None
    new_accuracy: Optional[float] = None
    duration_seconds: Optional[float] = None


class RetrainResponse(BaseModel):
    """
    Response sau khi trigger retrain
    """
    success: bool
    message: str
    retrain_results: List[RetrainStatus]
    started_at: datetime
    completed_at: Optional[datetime] = None


class AdminStatsResponse(BaseModel):
    """
    Response tổng quan stats cho admin dashboard
    """
    active_models: int
    avg_accuracy: float
    predictions_today: int
    low_confidence_count: int
    models_needing_retrain: int
    last_retrain: Optional[datetime]
