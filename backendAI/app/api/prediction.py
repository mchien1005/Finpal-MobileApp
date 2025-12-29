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
import pandas as pd
import numpy as np
from sklearn.linear_model import LinearRegression
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
    
    Kết hợp:
    - ML Model đã train (để học seasonality patterns)
    - Statistics từ MySQL (dữ liệu thực của user)
    
    Returns:
        SpendingPredictionResult: Kết quả dự đoán
    """
    
    try:
        user_id = input_data.user_id
        month = input_data.month
        category = input_data.category
        
        # Biến lưu kết quả
        predicted_amount = 0.0
        confidence = 0.0
        trend = "stable"
        change_pct = 0.0
        recommendation = ""
        average_amount = 0.0
        data_source = "unknown"
        
        # ==========================================
        # BƯỚC 1: Lấy statistics từ MySQL (realtime)
        # ==========================================
        user_stats = {}
        
        if PYMYSQL_AVAILABLE:
            try:
                db = get_database_service()
                df = db.get_user_expense_transactions(user_id=user_id, months=12)
                
                if len(df) > 0:
                    data_source = "mysql"
                    df['timestamp'] = pd.to_datetime(df['timestamp'])
                    df['month_period'] = df['timestamp'].dt.to_period('M').astype(str)
                    
                    # Group by month và category
                    monthly_spending = df.groupby(['month_period', 'category'])['amount'].sum().reset_index()
                    
                    # Tính statistics cho từng category
                    for cat in df['category'].unique():
                        cat_data = monthly_spending[monthly_spending['category'] == cat]
                        if len(cat_data) > 0:
                            # Tính trend
                            trend_value = 0
                            if len(cat_data) >= 2:
                                X = np.arange(len(cat_data)).reshape(-1, 1)
                                y = cat_data['amount'].values
                                lr = LinearRegression()
                                lr.fit(X, y)
                                trend_value = float(lr.coef_[0])
                            
                            user_stats[cat] = {
                                'mean': float(cat_data['amount'].mean()),
                                'median': float(cat_data['amount'].median()),
                                'recent_avg': float(cat_data.tail(3)['amount'].mean()),
                                'trend': trend_value,
                                'months_count': len(cat_data)
                            }
                    
                    logger.info(f"✅ Loaded MySQL stats for user {user_id}: {len(user_stats)} categories")
                    
            except Exception as e:
                logger.warning(f"Could not load MySQL stats: {e}")
        
        # Fallback: dùng model.category_stats
        if not user_stats:
            try:
                model = get_predictor()
                if user_id in model.category_stats:
                    user_stats = model.category_stats[user_id]
                    data_source = "model (fallback)"
                    logger.info(f"Using model stats for user {user_id}")
            except:
                pass
        
        # ==========================================
        # BƯỚC 2: Dự đoán
        # ==========================================
        
        if not user_stats:
            # Không có dữ liệu -> trả về message
            return SpendingPredictionResult(
                predicted_amount=0.0,
                confidence=0.0,
                trend="stable",
                change_percentage=0.0,
                recommendation="Chưa có đủ dữ liệu lịch sử để dự đoán. Hãy thêm giao dịch để nhận dự đoán chính xác hơn.",
                message="📊 Chưa có dữ liệu chi tiêu. Hãy thêm giao dịch để nhận dự đoán thông minh từ FinPal AI!",
                predicted_at=datetime.now()
            )
        
        # Dự đoán cho category cụ thể
        if category:
            cat_stats = user_stats.get(category, {})
            
            if not cat_stats:
                return SpendingPredictionResult(
                    predicted_amount=0.0,
                    confidence=0.0,
                    trend="stable",
                    change_percentage=0.0,
                    recommendation=f"Chưa có dữ liệu lịch sử cho danh mục '{category}'.",
                    message=f"📊 Chưa có dữ liệu chi tiêu cho '{category}'. Hãy thêm giao dịch vào danh mục này.",
                    predicted_at=datetime.now()
                )
            
            # Dự đoán dựa trên recent_avg + trend
            recent_avg = cat_stats['recent_avg']
            mean_amount = cat_stats['mean']
            trend_value = cat_stats.get('trend', 0)
            
            # Dự đoán = recent_avg + trend adjustment
            predicted_amount = recent_avg + (trend_value * 0.5)  # Trend smoothing
            predicted_amount = max(0, predicted_amount)  # Không âm
            
            # Confidence dựa vào số tháng có data
            months_count = cat_stats.get('months_count', 1)
            confidence = min(0.95, 0.5 + months_count * 0.1)
            
            # Xác định trend
            if trend_value > mean_amount * 0.05:
                trend = "increasing"
            elif trend_value < -mean_amount * 0.05:
                trend = "decreasing"
            else:
                trend = "stable"
            
            # Calculate change percentage
            change_pct = ((predicted_amount - recent_avg) / recent_avg * 100) if recent_avg > 0 else 0
            average_amount = mean_amount
            
            # Generate recommendation
            recommendation = f"Dự đoán chi tiêu '{category}': {predicted_amount:,.0f}đ. "
            if trend == "increasing":
                recommendation += f"Chi tiêu tăng {abs(change_pct):.1f}% so với trung bình. "
                if predicted_amount > mean_amount * 1.2:
                    recommendation += "⚠️ Cân nhắc giảm chi tiêu trong danh mục này."
            elif trend == "decreasing":
                recommendation += f"Chi tiêu giảm {abs(change_pct):.1f}%. ✅ Bạn đang tiết kiệm tốt!"
            else:
                recommendation += "Chi tiêu ổn định."
        
        # Dự đoán tổng chi tiêu (tất cả categories)
        else:
            total_predicted = 0.0
            total_recent = 0.0
            total_confidence = 0.0
            cat_count = 0
            
            for cat, cat_stats in user_stats.items():
                recent_avg = cat_stats['recent_avg']
                trend_value = cat_stats.get('trend', 0)
                
                cat_predicted = recent_avg + (trend_value * 0.5)
                cat_predicted = max(0, cat_predicted)
                
                total_predicted += cat_predicted
                total_recent += recent_avg
                
                months_count = cat_stats.get('months_count', 1)
                total_confidence += min(0.95, 0.5 + months_count * 0.1)
                cat_count += 1
            
            predicted_amount = total_predicted
            confidence = total_confidence / cat_count if cat_count > 0 else 0.5
            
            # Overall trend
            change_pct = ((total_predicted - total_recent) / total_recent * 100) if total_recent > 0 else 0
            
            if change_pct > 5:
                trend = "increasing"
            elif change_pct < -5:
                trend = "decreasing"
            else:
                trend = "stable"
            
            recommendation = f"Tổng chi tiêu dự kiến {predicted_amount:,.0f}đ. "
            if trend == "increasing":
                recommendation += f"Chi tiêu tăng {abs(change_pct):.1f}% so với trung bình gần đây."
            elif trend == "decreasing":
                recommendation += f"Chi tiêu giảm {abs(change_pct):.1f}%. Bạn đang tiết kiệm tốt!"
            else:
                recommendation += "Chi tiêu ổn định."
        
        # ==========================================
        # BƯỚC 3: Tạo message từ template
        # ==========================================
        message = create_prediction_message(
            predicted_amount=predicted_amount,
            month=month,
            trend=trend,
            change_pct=change_pct,
            recommendation=recommendation,
            category=category,
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
async def get_user_spending_categories(user_id: int, months: int = 6):
    """
    Lấy thống kê chi tiêu theo category từ MySQL - Get spending statistics by category
    
    Trả về thống kê chi tiêu THỰC TẾ của người dùng từ database,
    không phải từ dữ liệu train model.
    
    Args:
        user_id: ID người dùng
        months: Số tháng lấy dữ liệu (mặc định 6 tháng)
    """
    
    try:
        # Ưu tiên lấy từ MySQL
        if PYMYSQL_AVAILABLE:
            try:
                db = get_database_service()
                df = db.get_user_expense_transactions(user_id=user_id, months=months)
                
                if len(df) == 0:
                    return {
                        "user_id": user_id,
                        "categories": [],
                        "total_categories": 0,
                        "data_source": "mysql",
                        "message": "Người dùng chưa có giao dịch chi tiêu nào trong thời gian này."
                    }
                
                # Tính thống kê từ dữ liệu thực
                df['timestamp'] = pd.to_datetime(df['timestamp'])
                df['month'] = df['timestamp'].dt.to_period('M').astype(str)
                
                # Group by month và category
                monthly_spending = df.groupby(['month', 'category'])['amount'].sum().reset_index()
                
                category_info = []
                for category in df['category'].unique():
                    cat_data = monthly_spending[monthly_spending['category'] == category]
                    
                    if len(cat_data) == 0:
                        continue
                    
                    # Tính statistics
                    mean_amount = cat_data['amount'].mean()
                    median_amount = cat_data['amount'].median()
                    recent_avg = cat_data.tail(3)['amount'].mean()
                    months_count = len(cat_data)
                    
                    # Tính trend
                    trend_value = 0
                    if len(cat_data) >= 2:
                        X = np.arange(len(cat_data)).reshape(-1, 1)
                        y = cat_data['amount'].values
                        model = LinearRegression()
                        model.fit(X, y)
                        trend_value = float(model.coef_[0])
                    
                    if trend_value > mean_amount * 0.05:
                        trend = "increasing"
                    elif trend_value < -mean_amount * 0.05:
                        trend = "decreasing"
                    else:
                        trend = "stable"
                    
                    trend_vi = {"increasing": "tăng", "decreasing": "giảm", "stable": "ổn định"}.get(trend, trend)
                    
                    # Tạo message
                    message = get_message_from_template(
                        PREDICTION_CATEGORY,
                        category=category,
                        month="tháng tới",
                        predicted_amount=recent_avg,
                        average=mean_amount,
                        trend=trend_vi
                    )
                    
                    if not message:
                        message = f"📊 '{category}': Trung bình {mean_amount:,.0f}đ/tháng, xu hướng {trend_vi}."
                    
                    category_info.append({
                        "category": category,
                        "mean_monthly": float(mean_amount),
                        "median_monthly": float(median_amount),
                        "recent_avg": float(recent_avg),
                        "trend": trend,
                        "months_tracked": int(months_count),
                        "message": message
                    })
                
                # Sắp xếp theo trung bình chi tiêu (cao đến thấp)
                category_info.sort(key=lambda x: x['mean_monthly'], reverse=True)
                
                return {
                    "user_id": user_id,
                    "categories": category_info,
                    "total_categories": len(category_info),
                    "data_source": "mysql",
                    "analyzed_months": months
                }
                
            except Exception as e:
                logger.warning(f"Could not load from MySQL, falling back to model: {e}")
        
        # Fallback: dùng model.category_stats (dữ liệu train cũ)
        model = get_predictor()
        
        if user_id not in model.category_stats:
            return {
                "user_id": user_id,
                "categories": [],
                "total_categories": 0,
                "data_source": "model (fallback)",
                "message": f"Không tìm thấy dữ liệu chi tiêu cho người dùng {user_id}."
            }
        
        stats = model.category_stats[user_id]
        
        category_info = []
        for category, cat_stats in stats.items():
            trend = "increasing" if cat_stats['trend'] > 0 else "decreasing" if cat_stats['trend'] < 0 else "stable"
            trend_vi = {"increasing": "tăng", "decreasing": "giảm", "stable": "ổn định"}.get(trend, trend)
            
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
        
        category_info.sort(key=lambda x: x['mean_monthly'], reverse=True)
        
        return {
            "user_id": user_id,
            "categories": category_info,
            "total_categories": len(category_info),
            "data_source": "model (fallback)"
        }
        
    except Exception as e:
        logger.error(f"Error in get_user_spending_categories: {e}")
        raise HTTPException(status_code=500, detail=str(e))


