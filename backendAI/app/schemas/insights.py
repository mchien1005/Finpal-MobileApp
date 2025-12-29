"""
Schemas for AI Insights API - Pydantic Schemas cho AI Insights

Chứa các data models cho AI insights và suggestions:
- SavingsSuggestion: Gợi ý tiết kiệm cho một category
- SavingsSuggestionsResponse: Tổng hợp tất cả gợi ý tiết kiệm
- SpendingPattern: Phân tích pattern chi tiêu
- SpendingInsight: Insight tổng quan về tài chính

Giúp người dùng nhận được gợi ý proactive để cải thiện tài chính.
"""

from pydantic import BaseModel, Field
from typing import List, Optional


class SavingsSuggestion(BaseModel):
    """
    Gợi ý tiết kiệm cho một category - Savings suggestion
    
    Phân tích chi tiêu hiện tại và đề xuất mức chi tiêu mục tiêu
    để tiết kiệm được tiền hàng tháng.
    """
    category: str = Field(..., description="Danh mục chi tiêu (ví dụ: 'Trà sữa', 'Ăn uống')")
    current_weekly_avg: float = Field(..., description="Trung bình chi tiêu hiện tại/tuần (VNĐ)")
    suggested_weekly_target: float = Field(..., description="Mục tiêu đề xuất/tuần (VNĐ)")
    monthly_savings: float = Field(..., description="Số tiền tiết kiệm được/tháng nếu đạt mục tiêu (VNĐ)")
    message: str = Field(..., description="Thông điệp gợi ý bằng tiếng Việt (ví dụ: 'Bạn chi 200k/tuần cho trà sữa...')")


class SavingsSuggestionsResponse(BaseModel):
    """
    Response chứa tất cả gợi ý tiết kiệm - Savings suggestions response
    
    Tổng hợp top 5 gợi ý tiết kiệm có tiềm năng cao nhất cho user.
    """
    user_id: int
    suggestions: List[SavingsSuggestion]
    total_potential_savings: float = Field(..., description="Tổng tiềm năng tiết kiệm/tháng từ tất cả gợi ý (VNĐ)")
    analyzed_months: int = Field(..., description="Số tháng dữ liệu đã phân tích")


class SpendingPattern(BaseModel):
    """
    Phân tích pattern chi tiêu - Spending pattern analysis
    
    Phân tích chi tiết về thói quen chi tiêu của user theo từng category:
    - Tần suất chi tiêu (hàng ngày/tuần/tháng)
    - Số tiền trung bình
    - Xu hướng (tăng/giảm)
    - Thời điểm cao điểm
    """
    category: str
    frequency: str = Field(..., description="Tần suất: 'daily'(hàng ngày)/'weekly'(hàng tuần)/'monthly'(hàng tháng)")
    average_amount: float = Field(..., description="Số tiền trung bình mỗi lần chi tiêu (VNĐ)")
    trend: str = Field(..., description="Xu hướng: 'increasing'(tăng)/'decreasing'(giảm)/'stable'(ổn định)")
    peak_times: List[str] = Field(..., description="Thời điểm chi tiêu nhiều nhất (ví dụ: ['Monday', '12:00-14:00'])")


class SpendingInsight(BaseModel):
    """
    Insight tổng quan về chi tiêu - General spending insight
    
    Cung cấp các insights thông minh về tài chính:
    - Warning: Cảnh báo (ví dụ: chi tiêu vượt ngân sách)
    - Tip: Mẹo tiết kiệm
    - Achievement: Thành tựu (ví dụ: tiết kiệm được 20% so với tháng trước)
    """
    insight_type: str = Field(..., description="Loại insight: 'warning'(cảnh báo)/'tip'(mẹo)/'achievement'(thành tựu)")
    category: Optional[str] = Field(None, description="Category liên quan (nếu có)")
    message: str = Field(..., description="Thông điệp insight bằng tiếng Việt")
    actionable: bool = Field(..., description="User có thể hành động được không (true/false)")
    impact_score: float = Field(..., ge=0, le=1, description="Mức độ ảnh hưởng đến tài chính 0-1 (1 = ảnh hưởng lớn)")


# =====================================================
# WEEKLY SPENDING TREND SCHEMAS
# =====================================================

class TopCategory(BaseModel):
    """
    Danh mục chi tiêu cao nhất trong ngày
    """
    name: str = Field(..., description="Tên danh mục")
    icon: str = Field(..., description="Icon MDI của danh mục (ví dụ: '<mdi:coffee>')")
    amount: float = Field(..., description="Số tiền chi cho danh mục này trong ngày (VNĐ)")


class DailySpending(BaseModel):
    """
    Chi tiêu theo từng ngày trong tuần
    """
    day_of_week: str = Field(..., description="Thứ trong tuần (T2, T3, T4, T5, T6, T7, CN)")
    date: str = Field(..., description="Ngày cụ thể (yyyy-MM-dd)")
    total_amount: float = Field(..., description="Tổng chi tiêu trong ngày (VNĐ)")
    top_category: Optional[TopCategory] = Field(None, description="Danh mục chi tiêu nhiều nhất ngày đó")
    percentage: float = Field(..., description="Phần trăm so với ngày chi tiêu cao nhất (0-100)")


class WeeklyInsight(BaseModel):
    """
    Insight về xu hướng chi tiêu trong tuần
    """
    message: str = Field(..., description="Thông điệp insight bằng tiếng Việt")
    peak_day: str = Field(..., description="Ngày chi tiêu nhiều nhất (T2-CN)")
    peak_amount: float = Field(..., description="Số tiền chi tiêu cao nhất (VNĐ)")


class WeeklySpendingTrendResponse(BaseModel):
    """
    Response cho API xu hướng chi tiêu tuần
    
    Cung cấp dữ liệu chi tiêu theo từng ngày trong tuần hiện tại,
    bao gồm insight về ngày chi tiêu nhiều nhất.
    """
    user_id: int = Field(..., description="ID người dùng")
    week_start_date: str = Field(..., description="Ngày bắt đầu tuần (yyyy-MM-dd)")
    week_end_date: str = Field(..., description="Ngày kết thúc tuần (yyyy-MM-dd)")
    daily_spending: List[DailySpending] = Field(..., description="Chi tiêu theo từng ngày")
    max_amount: float = Field(..., description="Số tiền chi tiêu cao nhất trong tuần (VNĐ)")
    max_day: str = Field(..., description="Ngày chi tiêu cao nhất (T2-CN)")
    total_week: float = Field(..., description="Tổng chi tiêu cả tuần (VNĐ)")
    insight: WeeklyInsight = Field(..., description="Insight về xu hướng chi tiêu")

