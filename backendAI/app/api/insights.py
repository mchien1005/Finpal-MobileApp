"""
AI Insights API - API Gợi Ý Tài Chính Thông Minh

API cung cấp các gợi ý proactive giúp người dùng:
- Tiết kiệm tiền dựa trên phân tích chi tiêu
- Phát hiện patterns chi tiêu (tần suất, thời điểm cao điểm)
- Nhận insights từ AI về cách tối ưu hóa tài chính
- So sánh chi tiêu giữa các danh mục và thời gian

Ví dụ: "Bạn chi 200k/tuần cho trà sữa, giảm còn 100k sẽ tiết kiệm 400k/tháng"

Nguồn dữ liệu:
- Ưu tiên: MySQL database (dữ liệu thật)
- Fallback: CSV file (dữ liệu mẫu nếu không kết nối được MySQL)
"""

from fastapi import APIRouter, HTTPException
from app.schemas.insights import (
    SavingsSuggestionsResponse,
    SavingsSuggestion,
    SpendingPattern,
    SpendingInsight
)
from app.models.spending_prediction import SpendingPredictor
from app.services.database import get_database_service
from typing import List
import pandas as pd
import numpy as np
from datetime import datetime, timedelta
import os
import logging

router = APIRouter()
logger = logging.getLogger(__name__)

# Khởi tạo predictor (để truy cập category stats)
predictor = None

# Flag để track nguồn dữ liệu
USE_MYSQL = True  # Đặt False để sử dụng CSV (cho testing)


def get_message_from_template(template_code: str, **kwargs) -> str:
    """
    Lấy message từ bảng mau_thong_bao và thay thế các placeholder
    
    Args:
        template_code: Mã template (ví dụ: 'SAVINGS_SUGGESTION', 'BUDGET_WARNING')
        **kwargs: Các giá trị để thay thế vào template
        
    Returns:
        str: Message đã được format với dữ liệu thực
        
    Example:
        get_message_from_template(
            'SAVINGS_SUGGESTION',
            category='Ăn uống',
            weekly_avg='200,000đ',
            suggested_weekly='150,000đ',
            monthly_savings='200,000đ'
        )
    """
    if USE_MYSQL:
        try:
            db = get_database_service()
            result = db.render_notification_template(template_code, **kwargs)
            if result:
                return result['content']
        except Exception as e:
            logger.warning(f"Could not load template {template_code}: {e}")
    
    # Fallback: Tạo message mặc định nếu không có template
    return _create_fallback_message(template_code, **kwargs)


def _create_fallback_message(template_code: str, **kwargs) -> str:
    """
    Tạo message mặc định khi không load được template từ database
    """
    templates = {
        'SAVINGS_SUGGESTION': (
            "FinPal nhận thấy bạn chi trung bình {weekly_avg} cho '{category}' mỗi tuần. "
            "Nếu bạn giảm còn {suggested_weekly}, bạn sẽ tiết kiệm được {monthly_savings}/tháng!"
        ),
        'BUDGET_WARNING': (
            "Bạn đã chi {percentage} hạn mức '{budget_name}' ({spent_amount}/{budget_amount}), "
            "còn {days_remaining} ngày nữa là hết kỳ ngân sách."
        ),
        'BUDGET_EXCEEDED': (
            "Ngân sách '{budget_name}' đã vượt quá! Đã chi {percentage} ({spent_amount}/{budget_amount})"
        ),
        'ANOMALY_DETECTED': (
            "Chi tiêu '{category}' tháng này ({current_amount}) cao hơn {increase_percent} "
            "so với trung bình ({average_amount})."
        ),
        'SPENDING_ACHIEVEMENT': (
            "Tuyệt vời! Bạn đã tiết kiệm được trong danh mục '{category}' tháng này. "
            "Chi tiêu thấp hơn {save_percent} so với trung bình!"
        ),
        'SPENDING_TIP': (
            "Chi tiêu '{category}' đang có xu hướng tăng. Cân nhắc xem xét lại các khoản chi này."
        ),
        'GOAL_REMINDER_7DAYS': (
            "Mục tiêu '{goal_name}' còn 7 ngày! Tiến độ: {progress} ({current_amount}/{target_amount}). "
            "Cố gắng thêm nhé! 💪"
        ),
        'GOAL_DEADLINE_TODAY': (
            "Hôm nay là deadline của mục tiêu '{goal_name}'! "
            "Tiến độ: {progress} ({current_amount}/{target_amount})"
        ),
        'GOAL_COMPLETED': (
            "Tuyệt vời! Bạn đã hoàn thành mục tiêu '{goal_name}' ({target_amount})! 🎊"
        ),
    }
    
    template = templates.get(template_code, "Thông báo từ FinPal")
    
    # Format số tiền với dấu phẩy
    formatted_kwargs = {}
    for key, value in kwargs.items():
        if key in ['weekly_avg', 'suggested_weekly', 'monthly_savings', 'spent_amount', 
                   'budget_amount', 'current_amount', 'average_amount', 'target_amount']:
            if isinstance(value, (int, float)):
                formatted_kwargs[key] = f"{value:,.0f}đ"
            else:
                formatted_kwargs[key] = str(value)
        elif key in ['percentage', 'increase_percent', 'save_percent', 'progress']:
            if isinstance(value, (int, float)):
                formatted_kwargs[key] = f"{value:.0f}%"
            else:
                formatted_kwargs[key] = str(value)
        else:
            formatted_kwargs[key] = str(value)
    
    try:
        return template.format(**formatted_kwargs)
    except KeyError as e:
        logger.warning(f"Missing placeholder {e} in template {template_code}")
        return template


def get_predictor():
    """
    Lấy hoặc khởi tạo spending predictor
    
    Sử dụng SpendingPredictor để truy cập thống kê category và dữ liệu lịch sử.
    
    Returns:
        SpendingPredictor: Instance của spending predictor
    
    Raises:
        HTTPException: Nếu model chưa được train
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


def load_user_transactions_from_mysql(user_id: int) -> pd.DataFrame:
    """
    Load transactions từ MySQL database
    
    Args:
        user_id: ID người dùng cần load transactions
    
    Returns:
        pd.DataFrame: DataFrame chứa expense transactions của user
        
    Raises:
        Exception: Nếu không kết nối được MySQL
    """
    db = get_database_service()
    
    # Lấy expense transactions 6 tháng gần nhất
    df = db.get_user_expense_transactions(user_id, months=6)
    
    if len(df) == 0:
        raise HTTPException(
            status_code=404,
            detail=f"No transactions found for user {user_id}"
        )
    
    # Rename columns để tương thích với code cũ
    if 'timestamp' not in df.columns and 'transaction_date' in df.columns:
        df = df.rename(columns={'transaction_date': 'timestamp'})
    
    # Đảm bảo timestamp là datetime
    df['timestamp'] = pd.to_datetime(df['timestamp'])
    
    logger.info(f"✅ Loaded {len(df)} transactions from MySQL for user {user_id}")
    return df


def load_user_transactions_from_csv(user_id: int) -> pd.DataFrame:
    """
    Load transactions từ CSV file (fallback)
    
    Args:
        user_id: ID người dùng cần load transactions
    
    Returns:
        pd.DataFrame: DataFrame chứa expense transactions của user
    
    Raises:
        HTTPException: Nếu không tìm thấy file hoặc không có dữ liệu
    """
    data_path = "data/raw/transactions.csv"
    
    if not os.path.exists(data_path):
        raise HTTPException(
            status_code=404,
            detail="Transaction data not found"
        )
    
    df = pd.read_csv(data_path)
    user_df = df[df['user_id'] == user_id].copy()
    
    if len(user_df) == 0:
        raise HTTPException(
            status_code=404,
            detail=f"No transactions found for user {user_id}"
        )
    
    # Convert timestamp
    user_df['timestamp'] = pd.to_datetime(user_df['timestamp'])
    
    # Filter only expenses
    user_df = user_df[user_df['transaction_type'] == 'EXPENSE'].copy()
    
    logger.info(f"📁 Loaded {len(user_df)} transactions from CSV for user {user_id}")
    return user_df


def load_user_transactions(user_id: int) -> pd.DataFrame:
    """
    Load transactions cho một user cụ thể
    
    Ưu tiên lấy dữ liệu từ MySQL database.
    Nếu không kết nối được MySQL, fallback sang CSV file.
    
    Args:
        user_id: ID người dùng cần load transactions
    
    Returns:
        pd.DataFrame: DataFrame chứa expense transactions của user
    
    Raises:
        HTTPException: Nếu không tìm thấy dữ liệu
    """
    if USE_MYSQL:
        try:
            return load_user_transactions_from_mysql(user_id)
        except HTTPException:
            # Re-raise HTTP exceptions (như 404)
            raise
        except Exception as e:
            # Lỗi kết nối MySQL -> fallback sang CSV
            logger.warning(f"⚠️ MySQL connection failed: {e}. Falling back to CSV...")
            return load_user_transactions_from_csv(user_id)
    else:
        return load_user_transactions_from_csv(user_id)


@router.get("/savings-suggestions/{user_id}", response_model=SavingsSuggestionsResponse)
async def get_savings_suggestions(user_id: int):
    """
    Phân tích chi tiêu và gợi ý cách tiết kiệm - Analyze spending and suggest savings
    
    Phân tích thói quen chi tiêu theo từng category và đề xuất cách giảm chi tiêu
    để tiết kiệm được nhiều tiền hơn mỗi tháng.
    
    Thuật toán:
    1. Tính trung bình chi tiêu hàng tuần cho mỗi category
    2. Tìm categories có chi tiêu cao (>200k/tháng)
    3. Đề xuất giảm 20-25% tùy theo mức độ chi tiêu
    4. Tính toán tiềm năng tiết kiệm hàng tháng
    
    Args:
        user_id: ID người dùng
    
    Returns:
        SavingsSuggestionsResponse: Danh sách gợi ý tiết kiệm bao gồm:
            - user_id: ID người dùng
            - suggestions: List các gợi ý (top 5):
                * category: Danh mục
                * current_weekly_avg: Chi tiêu trung bình/tuần hiện tại
                * suggested_weekly_target: Mục tiêu đề xuất/tuần
                * monthly_savings: Số tiền tiết kiệm được/tháng
                * message: Thông điệp gợi ý (tiếng Việt)
            - total_potential_savings: Tổng tiềm năng tiết kiệm
            - analyzed_months: Số tháng dữ liệu được phân tích
    
    Example:
        Output: {
            "user_id": 1,
            "suggestions": [
                {
                    "category": "Trà sữa",
                    "current_weekly_avg": 200000,
                    "suggested_weekly_target": 100000,
                    "monthly_savings": 400000,
                    "message": "Bạn chi trung bình 200,000đ/tuần cho 'Trà sữa'. Nếu giảm còn 100,000đ, bạn sẽ tiết kiệm được 400,000đ/tháng."
                }
            ],
            "total_potential_savings": 1500000,
            "analyzed_months": 3
        }
    """
    
    try:
        # Load user transactions
        df = load_user_transactions(user_id)
        
        # Kiểm tra có dữ liệu expense không
        if len(df) == 0:
            raise HTTPException(
                status_code=404,
                detail=f"No expense transactions found for user {user_id}"
            )
        
        # Tính số tháng dữ liệu có
        date_range = (df['timestamp'].max() - df['timestamp'].min()).days
        analyzed_months = max(1, date_range // 30)
        
        # Group theo category và tính weekly average
        df['week'] = df['timestamp'].dt.isocalendar().week
        df['year'] = df['timestamp'].dt.year
        
        weekly_spending = df.groupby(['category', 'year', 'week'])['amount'].sum().reset_index()
        category_weekly_avg = weekly_spending.groupby('category')['amount'].mean()
        
        suggestions = []
        
        for category, weekly_avg in category_weekly_avg.items():
            # Chỉ gợi ý cho categories có chi tiêu đáng kể
            monthly_avg = weekly_avg * 4
            
            if monthly_avg > 200000:  # Ngưỡng: 200k/tháng
                # Đề xuất giảm 20-30% tùy theo mức chi tiêu
                reduction_pct = 0.25 if monthly_avg > 1000000 else 0.20
                suggested_weekly = weekly_avg * (1 - reduction_pct)
                monthly_savings = (weekly_avg - suggested_weekly) * 4
                
                # Lấy message từ template trong database
                message = get_message_from_template(
                    'SAVINGS_SUGGESTION',
                    category=category,
                    weekly_avg=weekly_avg,
                    suggested_weekly=suggested_weekly,
                    monthly_savings=monthly_savings
                )
                
                suggestions.append(SavingsSuggestion(
                    category=category,
                    current_weekly_avg=float(weekly_avg),
                    suggested_weekly_target=float(suggested_weekly),
                    monthly_savings=float(monthly_savings),
                    message=message
                ))
        
        # Sort by potential savings (descending)
        suggestions.sort(key=lambda x: x.monthly_savings, reverse=True)
        
        # Take top 5 suggestions
        top_suggestions = suggestions[:5]
        
        total_savings = sum(s.monthly_savings for s in top_suggestions)
        
        return SavingsSuggestionsResponse(
            user_id=user_id,
            suggestions=top_suggestions,
            total_potential_savings=total_savings,
            analyzed_months=analyzed_months
        )
        
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/spending-patterns/{user_id}", response_model=List[SpendingPattern])
async def get_spending_patterns(user_id: int):
    """
    Analyze detailed spending patterns for a user
    
    Returns frequency, trends, and peak times for each category
    """
    
    try:
        df = load_user_transactions(user_id)
        
        patterns = []
        
        for category in df['category'].unique():
            cat_df = df[df['category'] == category].copy()
            
            # Calculate frequency
            days_between = cat_df['timestamp'].diff().dt.days.median()
            if pd.isna(days_between):
                frequency = "monthly"
            elif days_between <= 1:
                frequency = "daily"
            elif days_between <= 7:
                frequency = "weekly"
            else:
                frequency = "monthly"
            
            # Calculate average amount
            avg_amount = cat_df['amount'].mean()
            
            # Calculate trend
            cat_df = cat_df.sort_values('timestamp')
            if len(cat_df) >= 4:
                recent_avg = cat_df.tail(len(cat_df)//3)['amount'].mean()
                older_avg = cat_df.head(len(cat_df)//3)['amount'].mean()
                
                if recent_avg > older_avg * 1.1:
                    trend = "increasing"
                elif recent_avg < older_avg * 0.9:
                    trend = "decreasing"
                else:
                    trend = "stable"
            else:
                trend = "stable"
            
            # Find peak times (day of week and hour)
            cat_df['day_of_week'] = cat_df['timestamp'].dt.day_name()
            cat_df['hour'] = cat_df['timestamp'].dt.hour
            
            peak_day = cat_df['day_of_week'].mode().iloc[0] if len(cat_df) > 0 else "Unknown"
            peak_hour = cat_df['hour'].mode().iloc[0] if len(cat_df) > 0 else 12
            
            peak_times = [
                f"{peak_day}",
                f"{peak_hour}:00 - {peak_hour+2}:00"
            ]
            
            patterns.append(SpendingPattern(
                category=category,
                frequency=frequency,
                average_amount=float(avg_amount),
                trend=trend,
                peak_times=peak_times
            ))
        
        return patterns
        
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/proactive-insights/{user_id}", response_model=List[SpendingInsight])
async def get_proactive_insights(user_id: int):
    """
    Generate proactive insights and warnings for user
    
    Examples:
    - "Bạn đã chi 70% hạn mức 'Ăn ngoài' của tháng này"
    - "Chi tiêu tháng này cao hơn 30% so với trung bình"
    """
    
    try:
        df = load_user_transactions(user_id)
        model = get_predictor()
        
        insights = []
        current_month = datetime.now().strftime("%Y-%m")
        
        # Get current month spending
        df['month'] = df['timestamp'].dt.to_period('M').astype(str)
        current_month_df = df[df['month'] == current_month]
        
        if len(current_month_df) == 0:
            # No spending this month yet
            insights.append(SpendingInsight(
                insight_type="tip",
                message="Chưa có giao dịch nào trong tháng này. Hãy bắt đầu theo dõi chi tiêu của bạn!",
                actionable=True,
                impact_score=0.3
            ))
            return insights
        
        # Check if user stats exist
        user_stats = model.category_stats.get(user_id, {})
        
        if not user_stats:
            return insights
        
        # 1. Check each category vs average
        for category, stats in user_stats.items():
            cat_current = current_month_df[current_month_df['category'] == category]['amount'].sum()
            cat_avg = stats['mean']
            
            if cat_current > cat_avg * 1.3:
                # Tính % tăng so với trung bình
                increase_percent = ((cat_current / cat_avg) - 1) * 100
                
                # Lấy message từ template
                message = get_message_from_template(
                    'ANOMALY_DETECTED',
                    category=category,
                    current_amount=cat_current,
                    increase_percent=increase_percent,
                    average_amount=cat_avg
                )
                
                insights.append(SpendingInsight(
                    insight_type="warning",
                    category=category,
                    message=message,
                    actionable=True,
                    impact_score=0.8
                ))
            elif cat_current < cat_avg * 0.7:
                # Tính % tiết kiệm được
                save_percent = (1 - (cat_current / cat_avg)) * 100
                
                message = get_message_from_template(
                    'SPENDING_ACHIEVEMENT',
                    category=category,
                    save_percent=save_percent
                )
                
                insights.append(SpendingInsight(
                    insight_type="achievement",
                    category=category,
                    message=message,
                    actionable=False,
                    impact_score=0.6
                ))
        
        # 2. Check total spending trend
        total_current = current_month_df['amount'].sum()
        all_months = df.groupby('month')['amount'].sum()
        
        if len(all_months) > 1:
            avg_monthly = all_months[:-1].mean()  # Exclude current month
            
            if total_current > avg_monthly * 1.2:
                increase_percent = ((total_current / avg_monthly) - 1) * 100
                
                message = get_message_from_template(
                    'ANOMALY_DETECTED',
                    category='Tổng chi tiêu',
                    current_amount=total_current,
                    increase_percent=increase_percent,
                    average_amount=avg_monthly
                )
                
                insights.append(SpendingInsight(
                    insight_type="warning",
                    message=message,
                    actionable=True,
                    impact_score=0.9
                ))
        
        # 3. Find categories with increasing trend
        for category, stats in user_stats.items():
            if stats['trend'] > stats['mean'] * 0.1:  # Significant upward trend
                message = get_message_from_template(
                    'SPENDING_TIP',
                    category=category
                )
                
                insights.append(SpendingInsight(
                    insight_type="tip",
                    category=category,
                    message=message,
                    actionable=True,
                    impact_score=0.7
                ))
        
        # Sort by impact score (most important first)
        insights.sort(key=lambda x: x.impact_score, reverse=True)
        
        # Return top 10 insights
        return insights[:10]
        
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
