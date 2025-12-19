"""
Smart Tips API - Hệ thống Gợi ý Thông minh từ AI

API cung cấp các tips/gợi ý tài chính được tạo động dựa trên:
- Phân tích pattern chi tiêu của người dùng
- Thời điểm trong tháng/năm
- So sánh với thói quen chi tiêu trước đó
- Các mẹo quản lý tài chính cá nhân

Đặc điểm:
- Format riêng biệt, không phụ thuộc template DB
- Nội dung được cá nhân hóa theo từng user
- Kết hợp AI analysis với financial best practices
"""

from fastapi import APIRouter, HTTPException
from pydantic import BaseModel, Field
from typing import List, Optional
from datetime import datetime, timedelta
from app.services.database import get_database_service
import pandas as pd
import numpy as np
import logging
import random

router = APIRouter()
logger = logging.getLogger(__name__)

# Flag sử dụng MySQL
USE_MYSQL = True


# ======================== SCHEMAS ========================

class SmartTip(BaseModel):
    """
    Một tip/gợi ý thông minh từ AI
    """
    tip_id: str = Field(..., description="ID duy nhất của tip")
    tip_type: str = Field(..., description="Loại tip: 'saving', 'warning', 'achievement', 'habit', 'seasonal', 'general'")
    icon: str = Field(..., description="Emoji/icon đại diện")
    title: str = Field(..., description="Tiêu đề ngắn gọn")
    content: str = Field(..., description="Nội dung chi tiết của tip")
    priority: int = Field(..., ge=1, le=5, description="Độ ưu tiên 1-5 (5 = quan trọng nhất)")
    category: Optional[str] = Field(None, description="Danh mục liên quan (nếu có)")
    action_text: Optional[str] = Field(None, description="Text cho nút hành động (nếu có)")
    action_url: Optional[str] = Field(None, description="URL điều hướng khi click (nếu có)")
    is_personalized: bool = Field(default=False, description="Tip có được cá nhân hóa không")


class SmartTipsResponse(BaseModel):
    """
    Response chứa danh sách tips thông minh
    """
    user_id: int
    tips: List[SmartTip]
    generated_at: str = Field(..., description="Thời điểm tạo tips")
    tips_count: int


# ======================== GENERAL FINANCIAL TIPS ========================

GENERAL_TIPS = [
    {
        "tip_type": "general",
        "icon": "💡",
        "title": "Quy tắc 50/30/20",
        "content": "Hãy thử áp dụng quy tắc 50/30/20: 50% thu nhập cho nhu cầu thiết yếu, 30% cho mong muốn, và 20% cho tiết kiệm/đầu tư.",
        "priority": 3,
        "action_text": "Xem ngân sách",
        "action_url": "/budgets"
    },
    {
        "tip_type": "general",
        "icon": "🎯",
        "title": "Đặt mục tiêu SMART",
        "content": "Mục tiêu tiết kiệm hiệu quả cần: Cụ thể (Specific), Đo lường được (Measurable), Khả thi (Achievable), Thực tế (Realistic), Có thời hạn (Time-bound).",
        "priority": 3,
        "action_text": "Tạo mục tiêu",
        "action_url": "/savings-goals/new"
    },
    {
        "tip_type": "general",
        "icon": "📊",
        "title": "Theo dõi chi tiêu hàng ngày",
        "content": "Ghi lại mọi khoản chi tiêu ngay khi phát sinh. Điều này giúp bạn nhận biết rõ tiền đi đâu và kiểm soát tốt hơn.",
        "priority": 4,
        "action_text": "Thêm giao dịch",
        "action_url": "/transactions/add"
    },
    {
        "tip_type": "general",
        "icon": "🏦",
        "title": "Quỹ khẩn cấp",
        "content": "Hãy xây dựng quỹ khẩn cấp bằng 3-6 tháng chi tiêu. Đây là 'tấm đệm' tài chính giúp bạn yên tâm trước những tình huống bất ngờ.",
        "priority": 5,
        "action_text": "Tạo quỹ khẩn cấp",
        "action_url": "/savings-goals/new"
    },
    {
        "tip_type": "general",
        "icon": "💳",
        "title": "Hạn chế dùng thẻ tín dụng",
        "content": "Nếu chưa kiểm soát tốt chi tiêu, hãy ưu tiên dùng tiền mặt hoặc thẻ ghi nợ. Điều này giúp bạn cảm nhận rõ hơn giá trị của tiền.",
        "priority": 3
    },
    {
        "tip_type": "general",
        "icon": "🛒",
        "title": "Quy tắc 24 giờ",
        "content": "Trước khi mua sắm đồ không thiết yếu, hãy chờ 24 giờ. Nếu sau đó vẫn muốn mua, đó mới là nhu cầu thật sự.",
        "priority": 4
    },
    {
        "tip_type": "general",
        "icon": "📱",
        "title": "Tự động hóa tiết kiệm",
        "content": "Thiết lập chuyển khoản tự động vào tài khoản tiết kiệm ngay khi nhận lương. 'Trả cho bản thân trước' là bí quyết của người giàu.",
        "priority": 4
    },
    {
        "tip_type": "general",
        "icon": "☕",
        "title": "Chi phí nhỏ, tích lũy lớn",
        "content": "1 ly cà phê 50k/ngày = 1.5 triệu/tháng = 18 triệu/năm! Những khoản chi nhỏ hàng ngày có thể tích lũy thành con số đáng kể.",
        "priority": 3
    },
    {
        "tip_type": "general",
        "icon": "📈",
        "title": "Đầu tư sớm",
        "content": "Thời gian là yếu tố quan trọng nhất trong đầu tư. Bắt đầu sớm với số tiền nhỏ tốt hơn bắt đầu muộn với số tiền lớn.",
        "priority": 3
    },
    {
        "tip_type": "general",
        "icon": "🎓",
        "title": "Đầu tư vào bản thân",
        "content": "Học thêm kỹ năng mới, nâng cao chuyên môn là khoản đầu tư có lợi nhuận cao nhất. Thu nhập tăng = tiết kiệm tăng.",
        "priority": 3
    }
]

SEASONAL_TIPS = {
    1: {  # Tháng 1 - Sau Tết
        "icon": "🧧",
        "title": "Kiểm soát chi tiêu sau Tết",
        "content": "Sau Tết là thời điểm dễ 'vung tay quá trán'. Hãy lập ngân sách chặt chẽ và tránh mua sắm không cần thiết trong tháng này.",
        "priority": 5
    },
    2: {  # Tháng 2 - Valentine
        "icon": "💝",
        "title": "Quà Valentine thông minh",
        "content": "Quà tặng ý nghĩa không cần đắt tiền. Hãy tập trung vào sự chân thành thay vì giá trị vật chất.",
        "priority": 3
    },
    3: {  # Tháng 3 - Quốc tế Phụ nữ
        "icon": "🌸",
        "title": "Lập kế hoạch chi tiêu Q2",
        "content": "Đầu quý là thời điểm tốt để đánh giá lại ngân sách và điều chỉnh mục tiêu tài chính cho 3 tháng tới.",
        "priority": 4
    },
    4: {  # Tháng 4
        "icon": "🌱",
        "title": "Dọn dẹp tài chính mùa xuân",
        "content": "Hãy rà soát các đăng ký, subscription không còn sử dụng và hủy bỏ để tiết kiệm chi phí định kỳ.",
        "priority": 4
    },
    5: {  # Tháng 5
        "icon": "👨‍👩‍👧",
        "title": "Lập quỹ cho kỳ nghỉ hè",
        "content": "Hè sắp đến! Bắt đầu tiết kiệm ngay cho kỳ nghỉ gia đình để tránh phải vay mượn hoặc dùng thẻ tín dụng.",
        "priority": 4
    },
    6: {  # Tháng 6 - Giữa năm
        "icon": "📊",
        "title": "Đánh giá giữa năm",
        "content": "Đã qua nửa năm! Hãy so sánh chi tiêu thực tế với kế hoạch đầu năm và điều chỉnh nếu cần.",
        "priority": 5
    },
    7: {  # Tháng 7
        "icon": "☀️",
        "title": "Tiết kiệm điện mùa hè",
        "content": "Chi phí điện thường tăng cao vào mùa hè. Hãy sử dụng điều hòa hợp lý và tắt thiết bị khi không dùng.",
        "priority": 4
    },
    8: {  # Tháng 8 - Back to school
        "icon": "📚",
        "title": "Chuẩn bị năm học mới",
        "content": "Mùa tựu trường đến! Lập danh sách đồ dùng học tập cần thiết và săn sale để tiết kiệm chi phí.",
        "priority": 4
    },
    9: {  # Tháng 9
        "icon": "🍂",
        "title": "Lên kế hoạch cuối năm",
        "content": "Còn 4 tháng nữa là hết năm. Hãy đánh giá tiến độ mục tiêu và tăng tốc tiết kiệm nếu còn thiếu.",
        "priority": 4
    },
    10: {  # Tháng 10
        "icon": "🎃",
        "title": "Chuẩn bị quỹ cuối năm",
        "content": "Cuối năm có nhiều sự kiện cần chi tiêu. Bắt đầu tiết kiệm ngay cho Giáng sinh, Tết Dương lịch.",
        "priority": 4
    },
    11: {  # Tháng 11 - Black Friday
        "icon": "🛍️",
        "title": "Mua sắm thông minh Black Friday",
        "content": "Black Friday đến! Chỉ mua những gì bạn đã lên kế hoạch từ trước. Đừng để 'sale' dụ dỗ mua đồ không cần.",
        "priority": 5
    },
    12: {  # Tháng 12 - Cuối năm
        "icon": "🎄",
        "title": "Kiểm soát chi tiêu cuối năm",
        "content": "Mùa lễ hội thường khiến chi tiêu tăng vọt. Hãy đặt giới hạn ngân sách cho quà tặng và tiệc tùng.",
        "priority": 5
    }
}

DAY_OF_WEEK_TIPS = {
    0: {  # Thứ 2
        "icon": "📅",
        "title": "Lập kế hoạch tuần mới",
        "content": "Đầu tuần là thời điểm tốt để đặt mục tiêu chi tiêu cho tuần. Hãy xem lại ngân sách và lên kế hoạch cụ thể.",
        "priority": 4
    },
    4: {  # Thứ 6
        "icon": "🍻",
        "title": "Tiết kiệm cuối tuần",
        "content": "Cuối tuần thường là thời điểm chi tiêu nhiều nhất. Hãy tìm các hoạt động vui chơi miễn phí hoặc giá rẻ.",
        "priority": 4
    },
    6: {  # Chủ nhật
        "icon": "📈",
        "title": "Tổng kết tuần",
        "content": "Cuối tuần là lúc nhìn lại chi tiêu 7 ngày qua. Bạn đã đạt mục tiêu chưa? Cần điều chỉnh gì?",
        "priority": 3
    }
}


# ======================== PERSONALIZED TIP GENERATORS ========================

def generate_spending_pattern_tips(df: pd.DataFrame, user_id: int) -> List[SmartTip]:
    """
    Tạo tips dựa trên pattern chi tiêu của user
    """
    tips = []
    
    if len(df) == 0:
        return tips
    
    try:
        # 1. Phát hiện category chi tiêu nhiều nhất
        category_spending = df.groupby('category')['amount'].sum().sort_values(ascending=False)
        if len(category_spending) > 0:
            top_category = category_spending.index[0]
            top_amount = category_spending.iloc[0]
            
            tips.append(SmartTip(
                tip_id=f"pattern_top_cat_{user_id}",
                tip_type="habit",
                icon="📊",
                title=f"Chi tiêu nhiều nhất: {top_category}",
                content=f"Trong thời gian gần đây, bạn chi nhiều nhất cho '{top_category}' ({top_amount:,.0f}đ). Hãy xem xét có thể cắt giảm không nhé!",
                priority=4,
                category=top_category,
                action_text="Xem chi tiết",
                action_url=f"/transactions?category={top_category}",
                is_personalized=True
            ))
        
        # 2. Phát hiện ngày chi tiêu nhiều trong tuần
        df['day_of_week'] = pd.to_datetime(df['timestamp']).dt.dayofweek
        daily_spending = df.groupby('day_of_week')['amount'].sum()
        if len(daily_spending) > 0:
            peak_day = daily_spending.idxmax()
            day_names = ['Thứ 2', 'Thứ 3', 'Thứ 4', 'Thứ 5', 'Thứ 6', 'Thứ 7', 'Chủ nhật']
            
            tips.append(SmartTip(
                tip_id=f"pattern_peak_day_{user_id}",
                tip_type="habit",
                icon="📆",
                title=f"Ngày chi tiêu nhiều nhất: {day_names[peak_day]}",
                content=f"Bạn thường chi tiêu nhiều nhất vào {day_names[peak_day]}. Hãy cẩn thận hơn vào ngày này nhé!",
                priority=3,
                is_personalized=True
            ))
        
        # 3. Phát hiện tần suất giao dịch cao
        transaction_count = len(df)
        days_range = (pd.to_datetime(df['timestamp']).max() - pd.to_datetime(df['timestamp']).min()).days
        if days_range > 0:
            avg_daily_transactions = transaction_count / days_range
            if avg_daily_transactions > 3:
                tips.append(SmartTip(
                    tip_id=f"pattern_high_freq_{user_id}",
                    tip_type="warning",
                    icon="⚡",
                    title="Tần suất giao dịch cao",
                    content=f"Bạn có trung bình {avg_daily_transactions:.1f} giao dịch/ngày. Nhiều giao dịch nhỏ có thể khó kiểm soát. Hãy thử gộp các khoản chi lại.",
                    priority=4,
                    is_personalized=True
                ))
        
        # 4. Phát hiện chi tiêu tăng so với trước
        df['month'] = pd.to_datetime(df['timestamp']).dt.to_period('M')
        monthly_spending = df.groupby('month')['amount'].sum()
        if len(monthly_spending) >= 2:
            current_month = monthly_spending.iloc[-1]
            prev_month = monthly_spending.iloc[-2]
            if current_month > prev_month * 1.2:  # Tăng > 20%
                increase_pct = ((current_month / prev_month) - 1) * 100
                tips.append(SmartTip(
                    tip_id=f"pattern_increase_{user_id}",
                    tip_type="warning",
                    icon="📈",
                    title="Chi tiêu tăng so với tháng trước",
                    content=f"Chi tiêu tháng này tăng {increase_pct:.0f}% so với tháng trước. Hãy xem lại các khoản chi và điều chỉnh nếu cần.",
                    priority=5,
                    action_text="Xem báo cáo",
                    action_url="/dashboard/reports",
                    is_personalized=True
                ))
            elif current_month < prev_month * 0.8:  # Giảm > 20%
                decrease_pct = (1 - (current_month / prev_month)) * 100
                tips.append(SmartTip(
                    tip_id=f"pattern_decrease_{user_id}",
                    tip_type="achievement",
                    icon="🎉",
                    title="Tuyệt vời! Chi tiêu giảm",
                    content=f"Chi tiêu tháng này giảm {decrease_pct:.0f}% so với tháng trước. Hãy tiếp tục phát huy nhé!",
                    priority=4,
                    is_personalized=True
                ))
        
    except Exception as e:
        logger.error(f"Error generating pattern tips: {e}")
    
    return tips


def generate_time_based_tips() -> List[SmartTip]:
    """
    Tạo tips dựa trên thời điểm (tháng, ngày trong tuần, ngày trong tháng)
    """
    tips = []
    now = datetime.now()
    
    # 1. Tip theo tháng (seasonal)
    month = now.month
    if month in SEASONAL_TIPS:
        seasonal = SEASONAL_TIPS[month]
        tips.append(SmartTip(
            tip_id=f"seasonal_{month}",
            tip_type="seasonal",
            icon=seasonal["icon"],
            title=seasonal["title"],
            content=seasonal["content"],
            priority=seasonal["priority"],
            is_personalized=False
        ))
    
    # 2. Tip theo ngày trong tuần
    day_of_week = now.weekday()
    if day_of_week in DAY_OF_WEEK_TIPS:
        day_tip = DAY_OF_WEEK_TIPS[day_of_week]
        tips.append(SmartTip(
            tip_id=f"day_{day_of_week}",
            tip_type="general",
            icon=day_tip["icon"],
            title=day_tip["title"],
            content=day_tip["content"],
            priority=day_tip["priority"],
            is_personalized=False
        ))
    
    # 3. Tip đầu tháng / cuối tháng
    day_of_month = now.day
    if day_of_month <= 5:
        tips.append(SmartTip(
            tip_id="month_start",
            tip_type="general",
            icon="📅",
            title="Đầu tháng mới!",
            content="Hãy lập ngân sách cho tháng này ngay! Phân bổ thu nhập vào các danh mục và đặt giới hạn chi tiêu.",
            priority=5,
            action_text="Lập ngân sách",
            action_url="/budgets/new",
            is_personalized=False
        ))
    elif day_of_month >= 25:
        tips.append(SmartTip(
            tip_id="month_end",
            tip_type="general",
            icon="⏰",
            title="Sắp hết tháng!",
            content="Còn vài ngày nữa là hết tháng. Hãy kiểm tra lại ngân sách và hạn chế chi tiêu không cần thiết.",
            priority=4,
            action_text="Xem ngân sách",
            action_url="/budgets",
            is_personalized=False
        ))
    
    return tips


def get_random_general_tips(count: int = 2) -> List[SmartTip]:
    """
    Lấy ngẫu nhiên một số tips chung về tài chính
    """
    selected = random.sample(GENERAL_TIPS, min(count, len(GENERAL_TIPS)))
    tips = []
    
    for i, tip_data in enumerate(selected):
        tips.append(SmartTip(
            tip_id=f"general_{i}_{random.randint(1000, 9999)}",
            tip_type=tip_data["tip_type"],
            icon=tip_data["icon"],
            title=tip_data["title"],
            content=tip_data["content"],
            priority=tip_data["priority"],
            action_text=tip_data.get("action_text"),
            action_url=tip_data.get("action_url"),
            is_personalized=False
        ))
    
    return tips


# ======================== MAIN ENDPOINT ========================

@router.get("/smart-tips/{user_id}", response_model=SmartTipsResponse)
async def get_smart_tips(user_id: int, max_tips: int = 5):
    """
    Lấy danh sách tips thông minh cho user
    
    Kết hợp:
    - Tips cá nhân hóa dựa trên pattern chi tiêu
    - Tips theo thời điểm (tháng, tuần)
    - Tips chung về quản lý tài chính
    
    Args:
        user_id: ID người dùng
        max_tips: Số lượng tips tối đa trả về (mặc định 5)
    
    Returns:
        SmartTipsResponse: Danh sách tips được sắp xếp theo độ ưu tiên
    """
    all_tips = []
    
    try:
        # 1. Lấy dữ liệu chi tiêu của user để phân tích
        if USE_MYSQL:
            try:
                db = get_database_service()
                df = db.get_user_expense_transactions(user_id, months=3)
                
                # Tạo tips dựa trên pattern chi tiêu
                pattern_tips = generate_spending_pattern_tips(df, user_id)
                all_tips.extend(pattern_tips)
                
            except Exception as e:
                logger.warning(f"Could not load user data for personalized tips: {e}")
        
        # 2. Thêm tips theo thời điểm
        time_tips = generate_time_based_tips()
        all_tips.extend(time_tips)
        
        # 3. Thêm tips chung (random)
        general_tips = get_random_general_tips(count=2)
        all_tips.extend(general_tips)
        
        # Sắp xếp theo độ ưu tiên (cao -> thấp), ưu tiên personalized
        all_tips.sort(key=lambda x: (x.is_personalized, x.priority), reverse=True)
        
        # Giới hạn số lượng
        final_tips = all_tips[:max_tips]
        
        return SmartTipsResponse(
            user_id=user_id,
            tips=final_tips,
            generated_at=datetime.now().isoformat(),
            tips_count=len(final_tips)
        )
        
    except Exception as e:
        logger.error(f"Error generating smart tips: {e}")
        # Fallback: Trả về tips chung
        fallback_tips = get_random_general_tips(count=max_tips)
        return SmartTipsResponse(
            user_id=user_id,
            tips=fallback_tips,
            generated_at=datetime.now().isoformat(),
            tips_count=len(fallback_tips)
        )


@router.get("/daily-tip", response_model=SmartTip)
async def get_daily_tip():
    """
    Lấy một tip ngẫu nhiên cho ngày hôm nay
    
    Có thể dùng để hiển thị trên màn hình chính của app
    
    Returns:
        SmartTip: Một tip ngẫu nhiên
    """
    # Ưu tiên tip theo mùa/thời điểm
    now = datetime.now()
    month = now.month
    
    if month in SEASONAL_TIPS:
        seasonal = SEASONAL_TIPS[month]
        return SmartTip(
            tip_id=f"daily_seasonal_{month}",
            tip_type="seasonal",
            icon=seasonal["icon"],
            title=seasonal["title"],
            content=seasonal["content"],
            priority=seasonal["priority"],
            is_personalized=False
        )
    
    # Fallback: Random general tip
    tip_data = random.choice(GENERAL_TIPS)
    return SmartTip(
        tip_id=f"daily_general_{random.randint(1000, 9999)}",
        tip_type=tip_data["tip_type"],
        icon=tip_data["icon"],
        title=tip_data["title"],
        content=tip_data["content"],
        priority=tip_data["priority"],
        action_text=tip_data.get("action_text"),
        action_url=tip_data.get("action_url"),
        is_personalized=False
    )
