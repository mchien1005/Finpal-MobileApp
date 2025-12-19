"""
Anomaly Detection API - API Phát Hiện Giao Dịch Bất Thường

API sử dụng Isolation Forest algorithm để phát hiện các giao dịch bất thường,
bao gồm:
- Giao dịch có số tiền quá cao so với thói quen
- Giao dịch vào thời gian lạ (đêm khuya, cuối tuần)
- Chi tiêu bất thường ở category không quen thuộc

Giúp người dùng phát hiện gian lận, lạm dụng thẻ, hoặc lỗi kế toán.
"""

from fastapi import APIRouter, HTTPException
from typing import List, Optional
from app.schemas.transaction import AnomalyDetectionInput, AnomalyDetectionResult
from app.models.anomaly_detection import AnomalyDetector
from app.services.database import get_database_service, PYMYSQL_AVAILABLE
from app.constants.notification_templates import ANOMALY_TRANSACTION, ANOMALY_TIME
import pandas as pd
import logging

router = APIRouter()
logger = logging.getLogger(__name__)

# Khởi tạo model (lazy loading)
detector = None

# Flag sử dụng templates từ DB
USE_DB_TEMPLATES = True


def get_detector():
    """
    Lấy hoặc khởi tạo anomaly detector model
    """
    global detector
    if detector is None:
        detector = AnomalyDetector()
        try:
            detector.load()
        except FileNotFoundError:
            raise HTTPException(
                status_code=503,
                detail="Model not trained yet. Please train the model first."
            )
    return detector


def get_message_from_template(template_code: str, **kwargs) -> Optional[str]:
    """
    Lấy message từ template trong database
    
    Args:
        template_code: Mã template (NOT017, NOT018, ...)
        **kwargs: Các giá trị để thay thế placeholder
        
    Returns:
        str: Message đã render hoặc None nếu không tìm thấy
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


def create_anomaly_message(
    is_anomaly: bool,
    amount: float,
    merchant: str,
    category: str,
    anomaly_score: float,
    average_amount: float = 0,
    reason: str = ""
) -> str:
    """
    Tạo message cho kết quả phát hiện bất thường
    
    Ưu tiên lấy từ template DB, fallback sang message mặc định
    """
    if not is_anomaly:
        return "✅ Giao dịch bình thường, không phát hiện bất thường."
    
    # Format số thành số nguyên (không có phần thập phân)
    amount_int = int(amount)
    average_int = int(average_amount) if average_amount > 0 else 0
    
    # Tạo message dựa trên loại anomaly
    if "cao hơn" in reason.lower() and "trung bình" in reason.lower():
        # Rule 1: Cao hơn 3x trung bình
        times = round(amount / average_amount, 1) if average_amount > 0 else 0
        return (
            f"🚨 Chi tiêu lớn: {amount_int:,}đ tại '{merchant}' ({category}). "
            f"Khoản này gấp {times}x mức chi thường ngày của bạn ({average_int:,}đ). "
            f"Hãy kiểm tra lại giao dịch này!"
        )
    
    elif "top 5%" in reason.lower() or "95%" in reason.lower():
        # Rule 2: Nằm trong top 5% cao nhất
        return (
            f"🔔 Giao dịch đáng chú ý: {amount_int:,}đ tại '{merchant}' ({category}). "
            f"Đây là một trong những khoản chi lớn nhất của bạn gần đây. "
            f"Xem xét lại nếu cần thiết."
        )
    
    elif "z-score" in reason.lower():
        # Rule 3: Z-score cao
        return (
            f"⚠️ Chi tiêu khác thường: {amount_int:,}đ tại '{merchant}' ({category}). "
            f"Giao dịch này khác biệt đáng kể so với thói quen chi tiêu của bạn. "
            f"Hãy kiểm tra lại giao dịch này."
        )
    
    elif "ai" in reason.lower() or "pattern" in reason.lower():
        # Rule 4: Isolation Forest / AI detection
        return (
            f"🤖 AI phát hiện bất thường: {amount_int:,}đ tại '{merchant}' ({category}). "
            f"Mô hình AI nhận thấy giao dịch này có đặc điểm khác với thói quen chi tiêu của bạn. "
            f"Hãy kiểm tra lại giao dịch này."
        )
    
    else:
        # Fallback message chung
        return (
            f"🚨 Phát hiện chi tiêu bất thường: {amount_int:,}đ tại '{merchant}' ({category}). "
            f"{reason}. Hãy kiểm tra lại giao dịch này."
        )


def get_user_stats_from_mysql(user_id: int) -> dict:
    """
    Lấy user statistics từ MySQL realtime
    
    Returns:
        dict với mean_amount, std_amount, median_amount, q75_amount, q95_amount, transaction_count, categories
    """
    if not PYMYSQL_AVAILABLE:
        return {}
    
    try:
        db = get_database_service()
        df = db.get_user_expense_transactions(user_id=user_id, months=12)
        
        if len(df) == 0:
            return {}
        
        return {
            'mean_amount': float(df['amount'].mean()),
            'std_amount': float(df['amount'].std()) if len(df) > 1 else 0,
            'median_amount': float(df['amount'].median()),
            'q75_amount': float(df['amount'].quantile(0.75)),
            'q95_amount': float(df['amount'].quantile(0.95)),
            'transaction_count': len(df),
            'categories': df['category'].value_counts().to_dict()
        }
    except Exception as e:
        logger.warning(f"Could not load MySQL stats for user {user_id}: {e}")
        return {}


@router.post("/detect", response_model=AnomalyDetectionResult)
async def detect_anomaly(transaction: AnomalyDetectionInput):
    """
    Phát hiện xem giao dịch có bất thường không - Detect if transaction is anomalous
    
    Kết hợp:
    - Isolation Forest model đã train (phát hiện pattern bất thường)
    - User statistics từ MySQL (dữ liệu thực của user)
    
    Returns:
        AnomalyDetectionResult: Kết quả phát hiện
    """
    
    try:
        user_id = transaction.user_id
        amount = transaction.amount
        merchant = transaction.merchant
        category = transaction.category
        # Handle timestamp None
        timestamp = pd.Timestamp(transaction.timestamp) if transaction.timestamp else pd.Timestamp.now()
        
        # ==========================================
        # BƯỚC 1: Lấy user stats từ MySQL (ưu tiên)
        # ==========================================
        user_stats = get_user_stats_from_mysql(user_id)
        data_source = "mysql" if user_stats else "unknown"
        
        # Fallback: dùng model.user_stats từ pkl
        if not user_stats:
            try:
                model = get_detector()
                if user_id in model.user_stats:
                    user_stats = model.user_stats[user_id]
                    data_source = "model (fallback)"
            except:
                pass
        
        # ==========================================
        # BƯỚC 2: Isolation Forest Prediction
        # ==========================================
        isolation_forest_result = None
        try:
            model = get_detector()
            if model and model.model is not None:
                # Gọi method detect của Isolation Forest
                if_is_anomaly, if_score, if_reason, if_recommendation = model.detect(
                    user_id=user_id,
                    amount=amount,
                    merchant=merchant,
                    category=category,
                    timestamp=timestamp
                )
                isolation_forest_result = {
                    'is_anomaly': if_is_anomaly,
                    'score': if_score,
                    'reason': if_reason,
                    'recommendation': if_recommendation
                }
                logger.debug(f"Isolation Forest: is_anomaly={if_is_anomaly}, score={if_score:.2f}")
        except Exception as e:
            logger.warning(f"Isolation Forest prediction failed: {e}")
        
        # ==========================================
        # BƯỚC 3: Rule-based Detection (nếu có user stats)
        # ==========================================
        
        # Nếu không có user stats -> không thể đánh giá chính xác
        if not user_stats:
            # Dùng kết quả Isolation Forest nếu có
            if isolation_forest_result:
                message = create_anomaly_message(
                    is_anomaly=isolation_forest_result['is_anomaly'],
                    amount=amount,
                    merchant=merchant,
                    category=category,
                    anomaly_score=isolation_forest_result['score'],
                    average_amount=0,
                    reason=isolation_forest_result['reason']
                )
                return AnomalyDetectionResult(
                    is_anomaly=isolation_forest_result['is_anomaly'],
                    anomaly_score=isolation_forest_result['score'],
                    reason=isolation_forest_result['reason'],
                    recommendation=isolation_forest_result['recommendation'],
                    message=message
                )
            
            return AnomalyDetectionResult(
                is_anomaly=False,
                anomaly_score=0.0,
                reason="Chưa có đủ dữ liệu lịch sử để đánh giá",
                recommendation="Thêm giao dịch để FinPal AI có thể phát hiện bất thường chính xác hơn.",
                message="📊 Chưa có dữ liệu chi tiêu. Hãy thêm giao dịch để nhận phân tích thông minh!"
            )
        
        mean_amount = user_stats.get('mean_amount', 0)
        std_amount = user_stats.get('std_amount', 1)
        q95_amount = user_stats.get('q95_amount', 0)
        
        # ==========================================
        # BƯỚC 4: Tính anomaly score HYBRID (Rule + IF)
        # ==========================================
        
        # Z-score: số độ lệch chuẩn so với mean
        z_score = (amount - mean_amount) / std_amount if std_amount > 0 else 0
        
        # Rule-based score dựa trên z-score (sigmoid)
        import math
        rule_score = 1 / (1 + math.exp(-z_score + 2))  # Center at z=2
        rule_score = min(1.0, max(0.0, rule_score))
        
        # Kết hợp score: 60% Rule-based + 40% Isolation Forest
        if isolation_forest_result:
            anomaly_score = 0.6 * rule_score + 0.4 * isolation_forest_result['score']
        else:
            anomaly_score = rule_score
        
        # Xác định có phải anomaly không
        is_anomaly = False
        reason = "Giao dịch bình thường"
        recommendation = "Giao dịch này nằm trong phạm vi thông thường của bạn."
        
        # Rule 1: Cao hơn 3x trung bình
        if amount > mean_amount * 3:
            is_anomaly = True
            multiplier = amount / mean_amount
            reason = f"Giao dịch cao hơn {multiplier:.1f}x mức trung bình"
            recommendation = f"⚠️ Số tiền này cao bất thường. Trung bình bạn chi {int(mean_amount):,}đ, nhưng giao dịch này là {int(amount):,}đ. Xác nhận lại giao dịch."
            anomaly_score = min(1.0, 0.7 + multiplier * 0.05)
        
        # Rule 2: Cao hơn 95th percentile
        elif amount > q95_amount and q95_amount > 0:
            is_anomaly = True
            reason = "Giao dịch nằm trong top 5% cao nhất"
            recommendation = f"⚠️ Giao dịch này cao hơn 95% các giao dịch trước đây của bạn ({int(q95_amount):,}đ). Xem xét lại nếu cần."
            anomaly_score = max(anomaly_score, 0.65)
        
        # Rule 3: Z-score cao (> 3 std)
        elif z_score > 3:
            is_anomaly = True
            reason = f"Giao dịch bất thường (z-score: {z_score:.1f})"
            recommendation = "⚠️ Giao dịch có đặc điểm khác lạ so với thói quen chi tiêu của bạn. Kiểm tra lại."
        
        # Rule 4: Isolation Forest phát hiện bất thường (ML-based)
        elif isolation_forest_result and isolation_forest_result['is_anomaly']:
            # Chỉ đánh dấu nếu IF score đủ cao (> 0.6)
            if isolation_forest_result['score'] > 0.6:
                is_anomaly = True
                reason = "AI phát hiện pattern bất thường"
                recommendation = f"🤖 Mô hình AI nhận thấy giao dịch này có đặc điểm khác thường so với thói quen chi tiêu của bạn. Kiểm tra lại."
                anomaly_score = max(anomaly_score, isolation_forest_result['score'])
        
        # ==========================================
        # BƯỚC 4: Tạo message từ template
        # ==========================================
        message = create_anomaly_message(
            is_anomaly=is_anomaly,
            amount=amount,
            merchant=merchant,
            category=category,
            anomaly_score=anomaly_score,
            average_amount=mean_amount,
            reason=reason
        )
        
        return AnomalyDetectionResult(
            is_anomaly=is_anomaly,
            anomaly_score=anomaly_score,
            reason=reason,
            recommendation=recommendation,
            message=message
        )
        
    except Exception as e:
        logger.error(f"Error in detect_anomaly: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/batch-detect")
async def batch_detect(transactions: List[AnomalyDetectionInput]):
    """
    Phát hiện anomaly cho nhiều giao dịch - Batch anomaly detection
    """
    
    try:
        model = get_detector()
        results = []
        
        for transaction in transactions:
            is_anomaly, anomaly_score, reason, recommendation = model.detect(
                user_id=transaction.user_id,
                amount=transaction.amount,
                merchant=transaction.merchant,
                category=transaction.category,
                timestamp=pd.Timestamp(transaction.timestamp)
            )
            
            # Lấy average amount
            average_amount = 0
            if transaction.user_id in model.user_stats:
                average_amount = model.user_stats[transaction.user_id].get('mean_amount', 0)
            
            # Tạo message
            message = create_anomaly_message(
                is_anomaly=is_anomaly,
                amount=transaction.amount,
                merchant=transaction.merchant,
                category=transaction.category,
                anomaly_score=anomaly_score,
                average_amount=average_amount,
                reason=reason
            )
            
            results.append({
                "merchant": transaction.merchant,
                "amount": float(transaction.amount),
                "is_anomaly": bool(is_anomaly),
                "anomaly_score": float(anomaly_score),
                "reason": str(reason),
                "recommendation": str(recommendation),
                "message": message
            })
        
        return {"detections": results}
        
    except Exception as e:
        logger.error(f"Error in batch_detect: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/stats/{user_id}")
async def get_user_stats(user_id: int):
    """
    Lấy thống kê chi tiêu của người dùng từ MySQL - Get user spending statistics
    
    Trả về thống kê THỰC TẾ của user để làm baseline cho anomaly detection.
    """
    
    try:
        # Ưu tiên lấy từ MySQL
        stats = get_user_stats_from_mysql(user_id)
        data_source = "mysql"
        
        # Fallback: dùng model.user_stats
        if not stats:
            try:
                model = get_detector()
                if user_id in model.user_stats:
                    stats = model.user_stats[user_id]
                    data_source = "model (fallback)"
            except:
                pass
        
        if not stats:
            return {
                "user_id": user_id,
                "statistics": None,
                "data_source": "none",
                "message": f"Không tìm thấy dữ liệu chi tiêu cho người dùng {user_id}."
            }
        
        return {
            "user_id": user_id,
            "statistics": {
                "mean_amount": stats.get('mean_amount', 0),
                "median_amount": stats.get('median_amount', 0),
                "std_amount": stats.get('std_amount', 0),
                "q75_amount": stats.get('q75_amount', 0),
                "q95_amount": stats.get('q95_amount', 0),
                "transaction_count": stats.get('transaction_count', 0),
                "top_categories": dict(list(stats.get('categories', {}).items())[:5])
            },
            "data_source": data_source
        }
        
    except Exception as e:
        logger.error(f"Error in get_user_stats: {e}")
        raise HTTPException(status_code=500, detail=str(e))


