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
from typing import Optional
from app.schemas.prediction import SpendingPredictionInput, SpendingPredictionResult
from app.models.spending_prediction import SpendingPredictor
from app.services.database import get_database_service, PYMYSQL_AVAILABLE
from app.constants.notification_templates import PREDICTION_MONTHLY, PREDICTION_CATEGORY
from datetime import datetime
import logging

router = APIRouter()
logger = logging.getLogger(__name__)

# Khởi tạo model (lazy loading)
predictor = None

# Flag sử dụng templates từ DB
USE_DB_TEMPLATES = True


def get_predictor():
    """
    Lấy hoặc khởi tạo spending predictor model
    """
    global predictor
    if predictor is None:
        predictor = SpendingPredictor()
        try:
            predictor.load()
        except FileNotFoundError:
            raise HTTPException(
                status_code=503,
                detail="Model not trained yet. Please train the model first."
            )
    return predictor


def get_message_from_template(template_code: str, **kwargs) -> Optional[str]:
    """
    Lấy message từ template trong database
    """
    if not USE_DB_TEMPLATES or not PYMYSQL_AVAILABLE:
        return None
    
    try:
        db = get_database_service()
        result = db.render_notification_template(template_code, **kwargs)
        if result:
            return result['content']
    except Exception as e:
        logger.warning(f"Could not load template {template_code}: {e}")
    
    return None


def create_prediction_message(
    predicted_amount: float,
    month: str,
    trend: str,
    change_pct: float,
    recommendation: str,
    category: Optional[str] = None,
    average_amount: float = 0
) -> str:
    """
    Tạo message cho kết quả dự đoán chi tiêu
    
    Ưu tiên lấy từ template DB, fallback sang message mặc định
    """
    # Translate trend
    trend_vi = {
        "increasing": "tăng",
        "decreasing": "giảm", 
        "stable": "ổn định"
    }.get(trend, trend)
    
    # Thử lấy từ DB template
    if category:
        message = get_message_from_template(
            PREDICTION_CATEGORY,
            category=category,
            month=month,
            predicted_amount=predicted_amount,
            average=average_amount,
            trend=trend_vi
        )
        if message:
            return message
        
        # Fallback
        return (
            f"📊 Dự đoán '{category}' tháng {month}: {predicted_amount:,.0f}đ. "
            f"Trung bình hiện tại: {average_amount:,.0f}đ. Xu hướng: {trend_vi}."
        )
    else:
        message = get_message_from_template(
            PREDICTION_MONTHLY,
            month=month,
            predicted_amount=predicted_amount,
            trend=trend_vi,
            change_percent=f"{change_pct:+.1f}%",
            recommendation=recommendation
        )
        if message:
            return message
        
        # Fallback
        return (
            f"📈 Dự đoán chi tiêu tháng {month}: {predicted_amount:,.0f}đ. "
            f"Xu hướng: {trend_vi} ({change_pct:+.1f}% so với tháng trước). {recommendation}"
        )


@router.post("/predict", response_model=SpendingPredictionResult)
async def predict_spending(input_data: SpendingPredictionInput):
    """
    Dự đoán chi tiêu tương lai - Predict future spending
    
    Returns:
        SpendingPredictionResult: Kết quả dự đoán bao gồm:
            - predicted_amount: Số tiền dự đoán
            - confidence: Độ tin cậy (0-1)
            - trend: Xu hướng ("increasing", "decreasing", "stable")
            - change_percentage: % thay đổi so với tháng trước
            - recommendation: Gợi ý điều chỉnh ngân sách
            - message: Thông báo chi tiết (từ template DB)
            - predicted_at: Thời điểm dự đoán
    """
    
    try:
        model = get_predictor()
        
        # Gọi model để dự đoán chi tiêu
        predicted_amount, confidence, trend, change_pct, recommendation = model.predict(
            user_id=input_data.user_id,
            month=input_data.month,
            category=input_data.category
        )
        
        # Lấy average amount nếu có category
        average_amount = 0
        if input_data.category and input_data.user_id in model.category_stats:
            cat_stats = model.category_stats[input_data.user_id].get(input_data.category, {})
            average_amount = cat_stats.get('mean', 0)
        
        # Tạo message từ template
        message = create_prediction_message(
            predicted_amount=predicted_amount,
            month=input_data.month,
            trend=trend,
            change_pct=change_pct,
            recommendation=recommendation,
            category=input_data.category,
            average_amount=average_amount
        )
        
        return SpendingPredictionResult(
            predicted_amount=predicted_amount,
            confidence=confidence,
            trend=trend,
            change_percentage=change_pct,
            recommendation=recommendation,
            message=message,
            predicted_at=datetime.now()
        )
        
    except Exception as e:
        logger.error(f"Error in predict_spending: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/categories/{user_id}")
async def get_user_spending_categories(user_id: int):
    """
    Lấy thống kê chi tiêu theo category - Get spending statistics by category
    
    Trả về thống kê chi tiêu của người dùng chia theo từng category,
    bao gồm trung bình hàng tháng, xu hướng, và số tháng theo dõi.
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
            trend = "increasing" if cat_stats['trend'] > 0 else "decreasing" if cat_stats['trend'] < 0 else "stable"
            trend_vi = {"increasing": "tăng", "decreasing": "giảm", "stable": "ổn định"}.get(trend, trend)
            
            # Tạo message cho từng category
            message = get_message_from_template(
                PREDICTION_CATEGORY,
                category=category,
                month="tháng tới",
                predicted_amount=cat_stats['recent_avg'],
                average=cat_stats['mean'],
                trend=trend_vi
            )
            
            if not message:
                message = f"📊 '{category}': Trung bình {cat_stats['mean']:,.0f}đ/tháng, xu hướng {trend_vi}."
            
            category_info.append({
                "category": category,
                "mean_monthly": cat_stats['mean'],
                "median_monthly": cat_stats['median'],
                "recent_avg": cat_stats['recent_avg'],
                "trend": trend,
                "months_tracked": cat_stats['months_count'],
                "message": message
            })
        
        # Sắp xếp theo trung bình chi tiêu (cao đến thấp)
        category_info.sort(key=lambda x: x['mean_monthly'], reverse=True)
        
        return {
            "user_id": user_id,
            "categories": category_info,
            "total_categories": len(category_info)
        }
        
    except Exception as e:
        logger.error(f"Error in get_user_spending_categories: {e}")
        raise HTTPException(status_code=500, detail=str(e))

