"""
Spending Prediction API - API Dự Đoán Chi Tiêu

API sử dụng Linear Regression và Moving Average để dự đoán chi tiêu tương lai
của người dùng dựa trên lịch sử chi tiêu.

Chức năng:
- Dự đoán tổng chi tiêu tháng tới
- Dự đoán chi tiêu theo category cụ thể
- Phân tích xu hướng (tăng/giảm)
- Gợi ý điều chỉnh ngân sách
"""

from fastapi import APIRouter, HTTPException
from app.schemas.prediction import SpendingPredictionInput, SpendingPredictionResult
from app.models.spending_prediction import SpendingPredictor
from datetime import datetime

router = APIRouter()

# Khởi tạo model (lazy loading)
predictor = None


def get_predictor():
    """
    Lấy hoặc khởi tạo spending predictor model
    
    Lazy loading: chỉ load model khi lần đầu gọi API.
    Model Linear Regression và category statistics được giữ trong memory.
    
    Returns:
        SpendingPredictor: Instance của model dự đoán chi tiêu
    
    Raises:
        HTTPException: Nếu model chưa được train
    """
    global predictor
    if predictor is None:
        predictor = SpendingPredictor()
        try:
            predictor.load()  # Load trained model từ disk
        except FileNotFoundError:
            raise HTTPException(
                status_code=503,
                detail="Model not trained yet. Please train the model first."
            )
    return predictor


@router.post("/predict", response_model=SpendingPredictionResult)
async def predict_spending(input_data: SpendingPredictionInput):
    """
    Dự đoán chi tiêu tương lai - Predict future spending
    
    Dự đoán số tiền sẽ chi tiêu trong tháng tới dựa trên:
    - Lịch sử chi tiêu 3-6 tháng gần nhất
    - Xu hướng tăng/giảm theo thời gian
    - Mùa (seasonality): cuối năm, tết, vào học
    
    Args:
        input_data: Thông tin input cho dự đoán
            - **user_id**: ID người dùng (bắt buộc)
            - **month**: Tháng cần dự đoán định dạng YYYY-MM (ví dụ: "2025-12")
            - **category**: Category cụ thể (tùy chọn, None = tổng chi tiêu)
    
    Returns:
        SpendingPredictionResult: Kết quả dự đoán bao gồm:
            - predicted_amount: Số tiền dự đoán
            - confidence: Độ tin cậy (0-1)
            - trend: Xu hướng ("increasing", "decreasing", "stable")
            - change_percentage: % thay đổi so với tháng trước
            - recommendation: Gợi ý điều chỉnh ngân sách
            - predicted_at: Thời điểm dự đoán
    
    Example:
        Input: {"user_id": 1, "month": "2025-12", "category": null}
        Output: {"predicted_amount": 8500000, "confidence": 0.85, "trend": "increasing", ...}
    """
    
    try:
        model = get_predictor()
        
        # Gọi model để dự đoán chi tiêu
        predicted_amount, confidence, trend, change_pct, recommendation = model.predict(
            user_id=input_data.user_id,
            month=input_data.month,
            category=input_data.category
        )
        
        return SpendingPredictionResult(
            predicted_amount=predicted_amount,
            confidence=confidence,
            trend=trend,
            change_percentage=change_pct,
            recommendation=recommendation,
            predicted_at=datetime.now()
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/categories/{user_id}")
async def get_user_spending_categories(user_id: int):
    """
    Lấy thống kê chi tiêu theo category - Get spending statistics by category
    
    Trả về thống kê chi tiêu của người dùng chia theo từng category,
    bao gồm trung bình hàng tháng, xu hướng, và số tháng theo dõi.
    
    Args:
        user_id: ID người dùng
    
    Returns:
        dict: Thống kê chi tiêu theo category bao gồm:
            - user_id: ID người dùng
            - categories: List thống kê cho từng category:
                * category: Tên category
                * mean_monthly: Trung bình chi tiêu/tháng
                * median_monthly: Trung vị chi tiêu/tháng
                * recent_avg: Trung bình 3 tháng gần nhất
                * trend: Xu hướng ("increasing"/"decreasing"/"stable")
                * months_tracked: Số tháng đã theo dõi
            - total_categories: Tổng số categories
    
    Example:
        {"user_id": 1, "categories": [{"category": "Ăn uống", "mean_monthly": 2000000, ...}], ...}
    """
    
    try:
        model = get_predictor()
        
        if user_id not in model.category_stats:
            raise HTTPException(
                status_code=404,
                detail=f"No spending data found for user {user_id}"
            )
        
        stats = model.category_stats[user_id]
        
        category_info = []
        for category, cat_stats in stats.items():
            category_info.append({
                "category": category,
                "mean_monthly": cat_stats['mean'],
                "median_monthly": cat_stats['median'],
                "recent_avg": cat_stats['recent_avg'],
                "trend": "increasing" if cat_stats['trend'] > 0 else "decreasing" if cat_stats['trend'] < 0 else "stable",
                "months_tracked": cat_stats['months_count']
            })
        
        # Sắp xếp theo trung bình chi tiêu (cao đến thấp)
        category_info.sort(key=lambda x: x['mean_monthly'], reverse=True)
        
        return {
            "user_id": user_id,
            "categories": category_info,
            "total_categories": len(category_info)
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
