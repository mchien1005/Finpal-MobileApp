"""
Prediction Related Schemas - Pydantic Schemas cho Prediction API

Chứa các data models cho dự đoán chi tiêu tương lai:
- SpendingPredictionInput: Input cho dự đoán chi tiêu
- SpendingPredictionResult: Kết quả dự đoán (số tiền, xu hướng, confidence)

Sử dụng Linear Regression để dự đoán chi tiêu dựa trên lịch sử.
"""

from pydantic import BaseModel, Field
from typing import Optional
from datetime import datetime


class SpendingPredictionInput(BaseModel):
    """
    Input cho dự đoán chi tiêu - Spending prediction input
    
    Chứa thông tin để dự đoán chi tiêu tháng tới của user.
    Có thể dự đoán tổng chi tiêu hoặc theo category cụ thể.
    """
    user_id: int = Field(..., description="ID người dùng")
    month: str = Field(..., pattern=r"^\d{4}-\d{2}$", description="Tháng cần dự đoán (định dạng YYYY-MM, ví dụ: 2025-12)")
    category: Optional[str] = Field(None, description="Category cụ thể (None = tổng tất cả categories)")
    
    class Config:
        json_schema_extra = {
            "example": {
                "user_id": 1,
                "month": "2025-12",
                "category": "Ăn uống"
            }
        }


class SpendingPredictionResult(BaseModel):
    """
    Kết quả dự đoán chi tiêu - Spending prediction result
    
    Response trả về từ API dự đoán chi tiêu, bao gồm:
    - Số tiền dự đoán
    - Độ tin cậy của dự đoán
    - Xu hướng (tăng/giảm/ổn định)
    - % thay đổi so với tháng trước
    - Gợi ý điều chỉnh ngân sách
    - Message thông báo (từ template DB)
    """
    predicted_amount: float = Field(..., description="Số tiền chi tiêu dự đoán (VNĐ)")
    confidence: float = Field(..., ge=0, le=1, description="Độ tin cậy 0-1 (0.85 = 85% tin cậy)")
    trend: str = Field(..., description="Xu hướng: 'increasing'(tăng)/'decreasing'(giảm)/'stable'(ổn định)")
    change_percentage: float = Field(..., description="% thay đổi so với tháng trước (15.5 = tăng 15.5%)")
    recommendation: str = Field(..., description="Gợi ý cho người dùng (ví dụ: 'Nên tăng ngân sách 500k')")
    message: Optional[str] = Field(None, description="Thông báo chi tiết (từ template mau_thong_bao)")
    predicted_at: datetime = Field(default_factory=datetime.now, description="Thời điểm thực hiện dự đoán")
    
    class Config:
        json_schema_extra = {
            "example": {
                "predicted_amount": 3200000,
                "confidence": 0.82,
                "trend": "increasing",
                "change_percentage": 15.5,
                "recommendation": "Chi tiêu dự kiến tăng 15.5% so với tháng trước",
                "message": "📈 Dự đoán chi tiêu tháng 2025-12: 3,200,000đ. Xu hướng: tăng (+15.5% so với tháng trước).",
                "predicted_at": "2025-11-20T10:30:00"
            }
        }

