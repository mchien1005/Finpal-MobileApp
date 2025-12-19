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
    
    # Tính số lần cao hơn trung bình
    times = round(amount / average_amount, 1) if average_amount > 0 else 0
    
    # Thử lấy từ DB template
    message = get_message_from_template(
        ANOMALY_TRANSACTION,
        amount=amount,
        merchant=merchant,
        category=category,
        times=times,
        average=average_amount
    )
    
    if message:
        return message
    
    # Fallback message
    if average_amount > 0:
        return (
            f"🚨 Phát hiện giao dịch bất thường: {amount:,.0f}đ tại '{merchant}' ({category}). "
            f"Số tiền này cao hơn {times}x so với trung bình của bạn ({average_amount:,.0f}đ)."
        )
    else:
        return f"🚨 Phát hiện giao dịch bất thường: {amount:,.0f}đ tại '{merchant}' ({category}). {reason}"


@router.post("/detect", response_model=AnomalyDetectionResult)
async def detect_anomaly(transaction: AnomalyDetectionInput):
    """
    Phát hiện xem giao dịch có bất thường không - Detect if transaction is anomalous
    
    So sánh giao dịch với thói quen chi tiêu của người dùng để xác định
    xem có bất thường không (số tiền quá cao, thời gian lạ, category không thường).
    
    Returns:
        AnomalyDetectionResult: Kết quả phát hiện bao gồm:
            - is_anomaly: Có bất thường không
            - anomaly_score: Điểm anomaly (0-1)
            - reason: Lý do bất thường
            - recommendation: Gợi ý xử lý
            - message: Thông báo chi tiết (từ template DB)
    """
    
    try:
        model = get_detector()
        
        # Gọi model để phát hiện anomaly
        is_anomaly, anomaly_score, reason, recommendation = model.detect(
            user_id=transaction.user_id,
            amount=transaction.amount,
            merchant=transaction.merchant,
            category=transaction.category,
            timestamp=pd.Timestamp(transaction.timestamp)
        )
        
        # Lấy average amount của user để tạo message
        average_amount = 0
        if transaction.user_id in model.user_stats:
            average_amount = model.user_stats[transaction.user_id].get('mean_amount', 0)
        
        # Tạo message từ template
        message = create_anomaly_message(
            is_anomaly=is_anomaly,
            amount=transaction.amount,
            merchant=transaction.merchant,
            category=transaction.category,
            anomaly_score=anomaly_score,
            average_amount=average_amount,
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
    Lấy thống kê chi tiêu của người dùng - Get user spending statistics
    """
    
    try:
        model = get_detector()
        
        if user_id not in model.user_stats:
            raise HTTPException(
                status_code=404,
                detail=f"No statistics found for user {user_id}"
            )
        
        stats = model.user_stats[user_id]
        
        return {
            "user_id": user_id,
            "statistics": {
                "mean_amount": stats['mean_amount'],
                "median_amount": stats['median_amount'],
                "std_amount": stats['std_amount'],
                "q75_amount": stats['q75_amount'],
                "q95_amount": stats['q95_amount'],
                "transaction_count": stats['transaction_count'],
                "top_categories": dict(list(stats['categories'].items())[:5])
            }
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

