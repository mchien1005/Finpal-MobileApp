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
from typing import List
from app.schemas.transaction import AnomalyDetectionInput, AnomalyDetectionResult
from app.models.anomaly_detection import AnomalyDetector
import pandas as pd

router = APIRouter()

# Khởi tạo model (lazy loading)
detector = None


def get_detector():
    """
    Lấy hoặc khởi tạo anomaly detector model
    
    Lazy loading: chỉ load model khi lần đầu gọi API.
    Model Isolation Forest và user statistics được giữ trong memory.
    
    Returns:
        AnomalyDetector: Instance của model phát hiện anomaly
    
    Raises:
        HTTPException: Nếu model chưa được train
    """
    global detector
    if detector is None:
        detector = AnomalyDetector()
        try:
            detector.load()  # Load trained model từ disk
        except FileNotFoundError:
            raise HTTPException(
                status_code=503,
                detail="Model not trained yet. Please train the model first."
            )
    return detector


@router.post("/detect", response_model=AnomalyDetectionResult)
async def detect_anomaly(transaction: AnomalyDetectionInput):
    """
    Phát hiện xem giao dịch có bất thường không - Detect if transaction is anomalous
    
    So sánh giao dịch với thói quen chi tiêu của người dùng để xác định
    xem có bất thường không (số tiền quá cao, thời gian lạ, category không thường).
    
    Args:
        transaction: Thông tin giao dịch cần kiểm tra
            - **user_id**: ID người dùng (bắt buộc)
            - **amount**: Số tiền (bắt buộc)
            - **merchant**: Tên merchant (bắt buộc)
            - **category**: Danh mục (bắt buộc)
            - **timestamp**: Thời gian giao dịch (tùy chọn)
    
    Returns:
        AnomalyDetectionResult: Kết quả phát hiện bao gồm:
            - is_anomaly: Có bất thường không (true/false)
            - anomaly_score: Điểm anomaly (0-1, cao = bất thường hơn)
            - reason: Lý do bất thường (ví dụ: "Cao hơn 3x trung bình")
            - recommendation: Gợi ý xử lý
    
    Example:
        Input: {"user_id": 1, "amount": 5000000, "merchant": "SHOPEE", "category": "Mua sắm"}
        Output: {"is_anomaly": true, "anomaly_score": 0.87, "reason": "Số tiền cao bất thường", ...}
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
        
        return AnomalyDetectionResult(
            is_anomaly=is_anomaly,
            anomaly_score=anomaly_score,
            reason=reason,
            recommendation=recommendation
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/batch-detect")
async def batch_detect(transactions: List[AnomalyDetectionInput]):
    """
    Phát hiện anomaly cho nhiều giao dịch - Batch anomaly detection
    
    Kiểm tra nhiều giao dịch cùng lúc, hữu ích khi:
    - Import giao dịch từ bank statement
    - Kiểm tra lại toàn bộ giao dịch cũ
    - Audit hàng loạt giao dịch
    
    Args:
        transactions: Danh sách các giao dịch cần kiểm tra
    
    Returns:
        dict: Kết quả phát hiện cho từng giao dịch
            - detections: List kết quả (merchant, amount, is_anomaly, score, reason, recommendation)
    """
    
    try:
        model = get_detector()
        results = []
        
        # Kiểm tra từng giao dịch
        for transaction in transactions:
            is_anomaly, anomaly_score, reason, recommendation = model.detect(
                user_id=transaction.user_id,
                amount=transaction.amount,
                merchant=transaction.merchant,
                category=transaction.category,
                timestamp=pd.Timestamp(transaction.timestamp)
            )
            
            results.append({
                "merchant": transaction.merchant,
                "amount": float(transaction.amount),
                "is_anomaly": bool(is_anomaly),
                "anomaly_score": float(anomaly_score),
                "reason": str(reason),
                "recommendation": str(recommendation)
            })
        
        return {"detections": results}
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/stats/{user_id}")
async def get_user_stats(user_id: int):
    """
    Lấy thống kê chi tiêu của người dùng - Get user spending statistics
    
    Trả về thống kê chi tiêu lịch sử của người dùng, dùng làm baseline
    để so sánh và phát hiện anomaly.
    
    Args:
        user_id: ID người dùng
    
    Returns:
        dict: Thống kê chi tiêu bao gồm:
            - mean_amount: Trung bình số tiền giao dịch
            - median_amount: Trung vị (giá trị giữa)
            - std_amount: Độ lệch chuẩn
            - q75_amount: Ngưỡng 75% (75% giao dịch thấp hơn)
            - q95_amount: Ngưỡng 95% (chỉ 5% giao dịch cao hơn)
            - transaction_count: Tổng số giao dịch
            - top_categories: Top 5 category thường chi tiêu
    
    Example:
        {"user_id": 1, "statistics": {"mean_amount": 150000, "median_amount": 50000, ...}}
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
