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
from app.constants.notification_templates import (
    SAVINGS_SUGGESTION,
    ANOMALY_DETECTED,
    SPENDING_ACHIEVEMENT,
    SPENDING_TIP,
)
from typing import List, Dict
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
    
    Sử dụng mã NOT00X làm key (đồng bộ với database)
    """
    templates = {
        # NOT008 - Gợi ý tiết kiệm thông minh
        SAVINGS_SUGGESTION: (
            "FinPal nhận thấy bạn chi trung bình {weekly_avg} cho '{category}' mỗi tuần. "
            "Nếu bạn giảm còn {suggested_weekly}, bạn sẽ tiết kiệm được {monthly_savings}/tháng!"
        ),
        # NOT006 - Cảnh báo ngân sách
        'NOT006': (
            "Bạn đã chi {percentage} hạn mức '{budget_name}' ({spent_amount}/{budget_amount}), "
            "còn {days_remaining} ngày nữa là hết kỳ ngân sách."
        ),
        # NOT007 - Ngân sách vượt quá
        'NOT007': (
            "Ngân sách '{budget_name}' đã vượt quá! Đã chi {percentage} ({spent_amount}/{budget_amount})"
        ),
        # NOT009 - Phát hiện chi tiêu bất thường
        ANOMALY_DETECTED: (
            "Chi tiêu '{category}' tháng này ({current_amount}) cao hơn {increase_percent} "
            "so với trung bình ({average_amount})."
        ),
        # NOT010 - Thành tích tiết kiệm
        SPENDING_ACHIEVEMENT: (
            "Tuyệt vời! Bạn đã tiết kiệm được trong danh mục '{category}' tháng này. "
            "Chi tiêu thấp hơn {save_percent} so với trung bình!"
        ),
        # NOT011 - Mẹo chi tiêu
        SPENDING_TIP: (
            "Chi tiêu '{category}' đang có xu hướng tăng. Cân nhắc xem xét lại các khoản chi này."
        ),
        # NOT012 - Nhắc mục tiêu 7 ngày
        'NOT012': (
            "Mục tiêu '{goal_name}' còn 7 ngày! Tiến độ: {progress} ({current_amount}/{target_amount}). "
            "Cố gắng thêm nhé! 💪"
        ),
        # NOT013 - Deadline hôm nay
        'NOT013': (
            "Hôm nay là deadline của mục tiêu '{goal_name}'! "
            "Tiến độ: {progress} ({current_amount}/{target_amount})"
        ),
        # NOT014 - Hoàn thành mục tiêu
        'NOT014': (
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
    
    # Lấy expense transactions 12 tháng gần nhất (tăng từ 6 lên 12)
    df = db.get_user_expense_transactions(user_id, months=12)
    
    if len(df) == 0:
        # Check if user has ANY transactions (even income or old ones)
        # This helps distinguish between "new user" vs "no recent expenses"
        logger.warning(f"No expense transactions found for user {user_id} in last 12 months")
        return pd.DataFrame() # Return empty DF instead of raising 404 immediately
    
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
    """
    data_path = "data/raw/transactions.csv"
    
    if not os.path.exists(data_path):
         return pd.DataFrame()
    
    df = pd.read_csv(data_path)
    user_df = df[df['user_id'] == user_id].copy()
    
    if len(user_df) == 0:
         return pd.DataFrame()
    
    # Convert timestamp
    user_df['timestamp'] = pd.to_datetime(user_df['timestamp'])
    
    # Filter only expenses
    user_df = user_df[user_df['transaction_type'] == 'EXPENSE'].copy()
    
    logger.info(f"📁 Loaded {len(user_df)} transactions from CSV for user {user_id}")
    return user_df


def load_user_transactions(user_id: int) -> pd.DataFrame:
    """
    Load transactions cho một user cụ thể
    """
    if USE_MYSQL:
        try:
            return load_user_transactions_from_mysql(user_id)
        except Exception as e:
            # Lỗi kết nối MySQL -> fallback sang CSV
            logger.warning(f"⚠️ MySQL connection failed: {e}. Falling back to CSV...")
            return load_user_transactions_from_csv(user_id)
    else:
        return load_user_transactions_from_csv(user_id)


@router.get("/savings-suggestions/{user_id}", response_model=SavingsSuggestionsResponse)
async def get_savings_suggestions(user_id: int):
    """
    Phân tích chi tiêu và gợi ý cách tiết kiệm
    """
    
    try:
        # Load user transactions
        df = load_user_transactions(user_id)
        
        # Kiểm tra có dữ liệu expense không
        if len(df) == 0:
            # Return empty response instead of 404
            return SavingsSuggestionsResponse(
                user_id=user_id,
                suggestions=[],
                total_potential_savings=0,
                analyzed_months=0
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
                    SAVINGS_SUGGESTION,
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
        
    except Exception as e:
        logger.error(f"Error in get_savings_suggestions: {e}")
        # Return empty on error to prevent app crash
        return SavingsSuggestionsResponse(
            user_id=user_id,
            suggestions=[],
            total_potential_savings=0,
            analyzed_months=0
        )


@router.get("/spending-patterns/{user_id}", response_model=List[SpendingPattern])
async def get_spending_patterns(user_id: int):
    """
    Analyze detailed spending patterns for a user
    """
    
    try:
        df = load_user_transactions(user_id)
        
        if len(df) == 0:
            return []
            
        patterns = []
        
        for category in df['category'].unique():
            cat_df = df[df['category'] == category].copy()
            
            if len(cat_df) == 0: continue

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
            trend = "stable"
            if len(cat_df) >= 4:
                recent_avg = cat_df.tail(len(cat_df)//3)['amount'].mean()
                older_avg = cat_df.head(len(cat_df)//3)['amount'].mean()
                
                if recent_avg > older_avg * 1.1:
                    trend = "increasing"
                elif recent_avg < older_avg * 0.9:
                    trend = "decreasing"
            
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
        
    except Exception as e:
        logger.error(f"Error in get_spending_patterns: {e}")
        return []


@router.get("/proactive-insights/{user_id}", response_model=List[SpendingInsight])
async def get_proactive_insights(user_id: int):
    """
    Generate proactive insights and warnings for user
    
    Sử dụng dữ liệu THỰC TẾ từ MySQL để phân tích và đưa ra gợi ý.
    impact_score được tính động dựa trên mức độ thay đổi.
    """
    
    try:
        df = load_user_transactions(user_id)
        
        insights = []
        
        if len(df) == 0:
            insights.append(SpendingInsight(
                insight_type="tip",
                message="Hãy thêm các giao dịch chi tiêu để nhận phân tích thông minh từ FinPal AI!",
                actionable=False,
                impact_score=0.1
            ))
            return insights

        current_month = datetime.now().strftime("%Y-%m")
        
        # Get current month spending
        df['month'] = df['timestamp'].dt.to_period('M').astype(str)
        current_month_df = df[df['month'] == current_month]
        
        if len(current_month_df) == 0:
            insights.append(SpendingInsight(
                insight_type="tip",
                message="Chưa có giao dịch nào trong tháng này. Hãy bắt đầu theo dõi chi tiêu của bạn!",
                actionable=True,
                impact_score=0.3
            ))
            return insights
        
        # ==========================================
        # Tính statistics từ dữ liệu MySQL realtime
        # ==========================================
        
        # Group by month và category để tính statistics
        monthly_spending = df.groupby(['month', 'category'])['amount'].sum().reset_index()
        
        # Tính user stats cho từng category
        user_stats = {}
        for category in df['category'].unique():
            cat_data = monthly_spending[monthly_spending['category'] == category]
            if len(cat_data) > 0:
                # Tính trend
                trend_value = 0
                if len(cat_data) >= 2:
                    X = np.arange(len(cat_data)).reshape(-1, 1)
                    y = cat_data['amount'].values
                    from sklearn.linear_model import LinearRegression
                    lr = LinearRegression()
                    lr.fit(X, y)
                    trend_value = float(lr.coef_[0])
                
                user_stats[category] = {
                    'mean': float(cat_data['amount'].mean()),
                    'std': float(cat_data['amount'].std()) if len(cat_data) > 1 else 0,
                    'trend': trend_value,
                    'recent_avg': float(cat_data.tail(3)['amount'].mean())
                }
        
        if not user_stats:
            insights.append(SpendingInsight(
                insight_type="tip",
                message="Cần thêm dữ liệu chi tiêu để phân tích chính xác hơn.",
                actionable=False,
                impact_score=0.2
            ))
            return insights
        
        # ==========================================
        # 1. Check each category vs average
        # ==========================================
        for category, stats in user_stats.items():
            cat_current = current_month_df[current_month_df['category'] == category]['amount'].sum()
            cat_avg = stats['mean']
            
            if cat_avg <= 0:
                continue
            
            ratio = cat_current / cat_avg
            
            # CẢNH BÁO: Chi tiêu tăng > 30%
            if ratio > 1.3:
                increase_percent = (ratio - 1) * 100
                
                # Impact score động: 0.7 -> 0.95 dựa trên mức tăng
                # ratio 1.3 -> 0.7, ratio 2.0+ -> 0.95
                impact = min(0.95, 0.7 + (ratio - 1.3) * 0.35)
                
                message = get_message_from_template(
                    ANOMALY_DETECTED,
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
                    impact_score=round(impact, 2)
                ))
            
            # THÀNH TÍCH: Chi tiêu giảm > 30%
            elif ratio < 0.7:
                save_percent = (1 - ratio) * 100
                
                # Impact score động: 0.5 -> 0.8 dựa trên mức tiết kiệm
                # ratio 0.7 -> 0.5, ratio 0.3 -> 0.8
                impact = min(0.8, 0.5 + (0.7 - ratio) * 0.75)
                
                message = get_message_from_template(
                    SPENDING_ACHIEVEMENT,
                    category=category,
                    save_percent=save_percent
                )
                
                insights.append(SpendingInsight(
                    insight_type="achievement",
                    category=category,
                    message=message,
                    actionable=False,
                    impact_score=round(impact, 2)
                ))
        
        # ==========================================
        # 2. Check total spending trend
        # ==========================================
        total_current = current_month_df['amount'].sum()
        all_months = df.groupby('month')['amount'].sum()
        
        if len(all_months) > 1:
            # Trung bình các tháng trước (không tính tháng hiện tại)
            previous_months = all_months[all_months.index != current_month]
            if len(previous_months) > 0:
                avg_monthly = previous_months.mean()
                
                if avg_monthly > 0:
                    ratio = total_current / avg_monthly
                    
                    if ratio > 1.2:
                        increase_percent = (ratio - 1) * 100
                        
                        # Impact cao hơn cho tổng chi tiêu
                        impact = min(0.98, 0.85 + (ratio - 1.2) * 0.2)
                        
                        message = get_message_from_template(
                            ANOMALY_DETECTED,
                            category='Tổng chi tiêu',
                            current_amount=total_current,
                            increase_percent=increase_percent,
                            average_amount=avg_monthly
                        )
                        
                        insights.append(SpendingInsight(
                            insight_type="warning",
                            message=message,
                            actionable=True,
                            impact_score=round(impact, 2)
                        ))
        
        # ==========================================
        # 3. Find categories with increasing trend
        # ==========================================
        for category, stats in user_stats.items():
            if stats['mean'] > 0 and stats['trend'] > stats['mean'] * 0.1:
                # Trend tăng > 10% của mean
                trend_percent = (stats['trend'] / stats['mean']) * 100
                
                # Impact dựa trên trend
                impact = min(0.75, 0.5 + trend_percent * 0.005)
                
                message = get_message_from_template(
                    SPENDING_TIP,
                    category=category
                )
                
                insights.append(SpendingInsight(
                    insight_type="tip",
                    category=category,
                    message=message,
                    actionable=True,
                    impact_score=round(impact, 2)
                ))
        
        # Sort by impact score (most important first)
        insights.sort(key=lambda x: x.impact_score, reverse=True)
        
        # Return top 10 insights
        return insights[:10]
        
    except Exception as e:
        logger.error(f"Error in get_proactive_insights: {e}")
        return []


# =====================================================
# WEEKLY SPENDING TREND API
# =====================================================

# Import thêm schemas mới
from app.schemas.insights import (
    WeeklySpendingTrendResponse,
    DailySpending,
    TopCategory,
    WeeklyInsight
)


# Cache cho category icons từ database
_category_icons_cache: Dict[str, str] = {}
_cache_loaded = False


def load_category_icons_from_db() -> Dict[str, str]:
    """
    Load mapping category_name -> icon từ bảng danh_muc trong database
    
    Cột icon trong database: bieu_tuong
    
    Returns:
        Dict mapping category name -> icon string (ví dụ: "food", "car", ...)
    """
    global _category_icons_cache, _cache_loaded
    
    if _cache_loaded:
        return _category_icons_cache
    
    try:
        db = get_database_service()
        query = """
            SELECT ten_danh_muc as name, COALESCE(bieu_tuong, 'cash') as icon
            FROM danh_muc
        """
        with db._engine.connect() as conn:
            from sqlalchemy import text
            import pandas as pd
            df = pd.read_sql(text(query), conn)
        
        # Build cache
        _category_icons_cache = dict(zip(df['name'], df['icon']))
        _cache_loaded = True
        
        logger.info(f"✅ Loaded {len(_category_icons_cache)} category icons from database")
        return _category_icons_cache
        
    except Exception as e:
        logger.warning(f"⚠️ Could not load category icons from DB: {e}. Using fallback.")
        return {}


# Mapping fallback nếu không load được từ DB
CATEGORY_ICONS_FALLBACK = {
    "Ăn uống": "food",
    "Cafe": "coffee",
    "Trà sữa": "cup",
    "Ăn ngoài": "food-fork-drink",
    "Di chuyển": "car",
    "Xăng": "gas-station",
    "Mua sắm": "shopping",
    "Giải trí": "movie-open",
    "Điện nước": "lightning-bolt",
    "Sức khỏe": "hospital",
    "Học tập": "school",
    "Khác": "dots-horizontal",
}


# Mapping ngày tiếng Việt
DAY_OF_WEEK_VI = {
    0: "T2",  # Monday
    1: "T3",  # Tuesday
    2: "T4",  # Wednesday
    3: "T5",  # Thursday
    4: "T6",  # Friday
    5: "T7",  # Saturday
    6: "CN",  # Sunday
}


def get_category_icon(category_name: str) -> str:
    """
    Lấy icon cho danh mục
    
    Ưu tiên lấy từ database (bảng danh_muc, cột bieu_tuong).
    Fallback sang mapping cứng nếu không tìm thấy.
    
    Args:
        category_name: Tên danh mục
        
    Returns:
        Icon string (ví dụ: "food", "car", ...)
    """
    # Thử load từ database
    db_icons = load_category_icons_from_db()
    
    # Tìm exact match trong DB
    if category_name in db_icons:
        icon = db_icons[category_name]
        if icon:
            return icon
    
    # Fallback: tìm trong mapping cứng
    if category_name in CATEGORY_ICONS_FALLBACK:
        return CATEGORY_ICONS_FALLBACK[category_name]
    
    # Partial match trong fallback
    category_lower = category_name.lower()
    for key, icon in CATEGORY_ICONS_FALLBACK.items():
        if key.lower() in category_lower or category_lower in key.lower():
            return icon
    
    return "cash"  # Default icon


@router.get("/weekly-spending-trend/{user_id}", response_model=WeeklySpendingTrendResponse)
async def get_weekly_spending_trend(user_id: int):
    """
    Lấy xu hướng chi tiêu tuần này
    
    Trả về chi tiêu theo từng ngày trong tuần hiện tại (T2-CN),
    bao gồm danh mục chi tiêu nhiều nhất mỗi ngày và insight.
    
    Args:
        user_id: ID người dùng
        
    Returns:
        WeeklySpendingTrendResponse: Dữ liệu chi tiêu theo ngày trong tuần
    """
    try:
        # Xác định tuần hiện tại (T2 - CN)
        today = datetime.now()
        # Lấy thứ 2 của tuần hiện tại
        week_start = today - timedelta(days=today.weekday())
        week_start = week_start.replace(hour=0, minute=0, second=0, microsecond=0)
        # Chủ nhật của tuần
        week_end = week_start + timedelta(days=6)
        week_end = week_end.replace(hour=23, minute=59, second=59)
        
        logger.info(f"Fetching weekly spending trend for user {user_id}: {week_start.date()} to {week_end.date()}")
        
        # Load transactions
        df = load_user_transactions(user_id)
        
        # Khởi tạo dữ liệu cho 7 ngày
        daily_data = []
        for i in range(7):
            day_date = week_start + timedelta(days=i)
            daily_data.append({
                "day_of_week": DAY_OF_WEEK_VI[i],
                "date": day_date.strftime("%Y-%m-%d"),
                "total_amount": 0.0,
                "top_category": None,
                "categories": {}  # Để tính top category
            })
        
        # Nếu có transactions, tính toán chi tiêu
        if len(df) > 0:
            # Filter giao dịch trong tuần
            df['timestamp'] = pd.to_datetime(df['timestamp'])
            week_df = df[(df['timestamp'] >= week_start) & (df['timestamp'] <= week_end)].copy()
            
            if len(week_df) > 0:
                # Tính chi tiêu theo ngày
                week_df['day_index'] = week_df['timestamp'].dt.dayofweek
                
                for _, row in week_df.iterrows():
                    day_idx = int(row['day_index'])
                    if 0 <= day_idx <= 6:
                        amount = float(row['amount'])
                        category = row['category'] if 'category' in row else 'Khác'
                        
                        daily_data[day_idx]['total_amount'] += amount
                        
                        # Thêm vào categories để tính top
                        if category not in daily_data[day_idx]['categories']:
                            daily_data[day_idx]['categories'][category] = 0
                        daily_data[day_idx]['categories'][category] += amount
        
        # Tìm ngày chi tiêu cao nhất
        max_amount = max(d['total_amount'] for d in daily_data)
        if max_amount == 0:
            max_amount = 1  # Tránh chia cho 0
        
        # Tạo response
        daily_spending = []
        total_week = 0.0
        max_day = "T2"
        max_day_amount = 0.0
        
        for day in daily_data:
            total_week += day['total_amount']
            
            # Track max day
            if day['total_amount'] > max_day_amount:
                max_day_amount = day['total_amount']
                max_day = day['day_of_week']
            
            # Tìm top category cho ngày này
            top_cat = None
            if day['categories']:
                top_cat_name = max(day['categories'], key=day['categories'].get)
                top_cat = TopCategory(
                    name=top_cat_name,
                    icon=get_category_icon(top_cat_name),
                    amount=day['categories'][top_cat_name]
                )
            
            # Tính percentage
            percentage = (day['total_amount'] / max_amount * 100) if max_amount > 0 else 0
            
            daily_spending.append(DailySpending(
                day_of_week=day['day_of_week'],
                date=day['date'],
                total_amount=day['total_amount'],
                top_category=top_cat,
                percentage=round(percentage, 1)
            ))
        
        # Tạo insight
        if max_day_amount > 0:
            insight_message = f"Bạn thường chi nhiều nhất vào {max_day}. Hãy lập kế hoạch chi tiêu cẩn thận hơn vào ngày này."
        else:
            insight_message = "Chưa có dữ liệu chi tiêu trong tuần này. Hãy ghi chép giao dịch để xem xu hướng!"
        
        insight = WeeklyInsight(
            message=insight_message,
            peak_day=max_day,
            peak_amount=max_day_amount
        )
        
        response = WeeklySpendingTrendResponse(
            user_id=user_id,
            week_start_date=week_start.strftime("%Y-%m-%d"),
            week_end_date=week_end.strftime("%Y-%m-%d"),
            daily_spending=daily_spending,
            max_amount=max_day_amount,
            max_day=max_day,
            total_week=total_week,
            insight=insight
        )
        
        logger.info(f"Weekly spending trend for user {user_id}: total={total_week:,.0f}đ, peak={max_day}")
        
        return response
        
    except Exception as e:
        logger.error(f"Error in get_weekly_spending_trend: {e}")
        # Trả về response rỗng nếu lỗi
        today = datetime.now()
        week_start = today - timedelta(days=today.weekday())
        week_end = week_start + timedelta(days=6)
        
        return WeeklySpendingTrendResponse(
            user_id=user_id,
            week_start_date=week_start.strftime("%Y-%m-%d"),
            week_end_date=week_end.strftime("%Y-%m-%d"),
            daily_spending=[
                DailySpending(
                    day_of_week=DAY_OF_WEEK_VI[i],
                    date=(week_start + timedelta(days=i)).strftime("%Y-%m-%d"),
                    total_amount=0.0,
                    top_category=None,
                    percentage=0.0
                ) for i in range(7)
            ],
            max_amount=0.0,
            max_day="T2",
            total_week=0.0,
            insight=WeeklyInsight(
                message="Không thể tải dữ liệu. Vui lòng thử lại sau.",
                peak_day="T2",
                peak_amount=0.0
            )
        )
