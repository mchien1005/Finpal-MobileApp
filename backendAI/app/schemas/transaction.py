"""
Transaction Related Schemas - Pydantic Schemas cho Transaction APIs

Chứa các data models (schemas) dùng để validate input/output của API:
- TransactionInput: Input cho phân loại giao dịch
- CategoryPrediction: Kết quả dự đoán category
- AnomalyDetectionInput: Input cho phát hiện anomaly
- AnomalyDetectionResult: Kết quả phát hiện anomaly

Pydantic tự động validate type, range, và format của dữ liệu.
"""

from pydantic import BaseModel, Field
from datetime import datetime
from typing import Optional, List


class TransactionInput(BaseModel):
    """
    Input cho phân loại giao dịch - Transaction categorization input
    
    Chứa thông tin giao dịch cần phân loại vào category phù hợp.
    Được sử dụng bởi API /categorization/predict
    """
    merchant: str = Field(..., description="Tên cửa hàng/merchant (ví dụ: GRAB, SHOPEE)")
    amount: float = Field(..., gt=0, description="Số tiền giao dịch (phải > 0)")
    description: Optional[str] = Field(None, description="Mô tả giao dịch (tùy chọn)")
    timestamp: Optional[datetime] = Field(default_factory=datetime.now, description="Thời gian giao dịch")
    
    class Config:
        json_schema_extra = {
            "example": {
                "merchant": "GRAB",
                "amount": 50000,
                "description": "Grab đi làm",
                "timestamp": "2025-11-20T10:30:00"
            }
        }


class CategoryAlternative(BaseModel):
    """
    Category thay thế - Alternative category suggestion
    
    Khi model không chắc chắn 100%, trả về các category khác có thể phù hợp.
    """
    category: str
    confidence: float = Field(..., ge=0, le=1, description="Độ tin cậy (0-1)")


class CategoryPrediction(BaseModel):
    """
    Kết quả phân loại category - Category prediction result
    
    Response trả về từ API phân loại giao dịch, bao gồm:
    - Category dự đoán chính
    - Độ tin cậy (confidence score)
    - Các category thay thế khác
    """
    category: str = Field(..., description="Category được dự đoán (ví dụ: 'Di chuyển', 'Ăn uống')")
    confidence: float = Field(..., ge=0, le=1, description="Độ tin cậy 0-1 (0.95 = 95% chắc chắn)")
    alternatives: List[CategoryAlternative] = Field(
        default_factory=list,
        description="Danh sách các category khác có thể phù hợp"
    )
    
    class Config:
        json_schema_extra = {
            "example": {
                "category": "Di chuyển",
                "confidence": 0.95,
                "alternatives": [
                    {"category": "Giao thông", "confidence": 0.85}
                ]
            }
        }


class AnomalyDetectionInput(BaseModel):
    """
    Input cho phát hiện anomaly - Anomaly detection input
    
    Chứa thông tin giao dịch cần kiểm tra xem có bất thường không.
    So sánh với lịch sử chi tiêu của user để phát hiện các giao dịch khả nghi.
    """
    user_id: int = Field(..., description="ID người dùng")
    amount: float = Field(..., gt=0, description="Số tiền giao dịch (phải > 0)")
    merchant: str = Field(..., description="Tên merchant")
    category: str = Field(..., description="Danh mục giao dịch")
    timestamp: datetime = Field(default_factory=datetime.now, description="Thời gian giao dịch")
    
    class Config:
        json_schema_extra = {
            "example": {
                "user_id": 1,
                "amount": 5000000,
                "merchant": "SHOPEE",
                "category": "Mua sắm",
                "timestamp": "2025-11-20T10:30:00"
            }
        }


class AnomalyDetectionResult(BaseModel):
    """
    Kết quả phát hiện anomaly - Anomaly detection result
    
    Response trả về từ API phát hiện anomaly, bao gồm:
    - Có bất thường không (true/false)
    - Điểm anomaly (càng cao càng bất thường)
    - Lý do cụ thể
    - Gợi ý xử lý
    """
    is_anomaly: bool = Field(..., description="Giao dịch có bất thường không (true = có)")
    anomaly_score: float = Field(..., ge=0, le=1, description="Điểm anomaly 0-1 (càng cao càng bất thường)")
    reason: str = Field(..., description="Lý do bất thường (ví dụ: 'Số tiền cao hơn 3x trung bình')")
    recommendation: str = Field(..., description="Gợi ý cho người dùng (ví dụ: 'Kiểm tra lại giao dịch')")
    
    class Config:
        json_schema_extra = {
            "example": {
                "is_anomaly": True,
                "anomaly_score": 0.87,
                "reason": "Giao dịch cao hơn 3.5x trung bình",
                "recommendation": "Xem xét lại giao dịch này"
            }
        }
