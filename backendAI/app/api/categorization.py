"""
Transaction Categorization API - API Phân Loại Giao Dịch Tự Động

API sử dụng Machine Learning (Random Forest) để tự động phân loại
giao dịch vào các danh mục dựa trên tên merchant, số tiền, thời gian.

Ví dụ: "GRAB" -> "Di chuyển", "SHOPEE" -> "Mua sắm"
"""

from fastapi import APIRouter, HTTPException
from typing import List
from app.schemas.transaction import TransactionInput, CategoryPrediction
from app.models.categorization import TransactionCategorizer
import pandas as pd

# Import training history để ghi prediction logs
try:
    from app.services.training_history import record_prediction
    HAS_TRAINING_HISTORY = True
except ImportError:
    HAS_TRAINING_HISTORY = False

router = APIRouter()

# Khởi tạo model (lazy loading - chỉ load khi cần dùng)
categorizer = None


def get_categorizer():
    """
    Lấy hoặc khởi tảo categorizer model
    
    Sử dụng lazy loading pattern: chỉ load model khi lần đầu tiên gọi API.
    Model được giữ trong memory để tái sử dụng cho các request tiếp theo.
    
    Returns:
        TransactionCategorizer: Instance của model phân loại
        
    Raises:
        HTTPException: Nếu model chưa được train
    """
    global categorizer
    if categorizer is None:
        categorizer = TransactionCategorizer()
        try:
            categorizer.load()  # Load trained model từ disk
        except FileNotFoundError:
            raise HTTPException(
                status_code=503,
                detail="Model not trained yet. Please train the model first."
            )
    return categorizer


@router.post("/predict", response_model=CategoryPrediction)
async def predict_category(transaction: TransactionInput):
    """
    Dự đoán danh mục cho một giao dịch - Predict category for a single transaction
    
    Sử dụng Random Forest model để phân loại giao dịch dựa trên:
    - Tên merchant (TF-IDF vectorization)
    - Số tiền (amount, log amount)
    - Thời gian (giờ, ngày trong tuần, cuối/đầu tháng)
    
    Args:
        transaction: Thông tin giao dịch cần phân loại
            - **merchant**: Tên cửa hàng/merchant (bắt buộc)
            - **amount**: Số tiền giao dịch (bắt buộc)
            - **description**: Mô tả (tùy chọn)
            - **timestamp**: Thời gian giao dịch (tùy chọn)
    
    Returns:
        CategoryPrediction: Kết quả dự đoán bao gồm:
            - category: Danh mục dự đoán (ví dụ: "Di chuyển")
            - confidence: Độ tin cậy (0-1)
            - alternatives: Các danh mục thay thế khác có thể
    
    Example:
        Input: {"merchant": "GRAB", "amount": 50000}
        Output: {"category": "Di chuyển", "confidence": 0.95, "alternatives": [...]}
    """
    
    try:
        model = get_categorizer()
        
        # Gọi model để dự đoán category
        category, confidence, alternatives = model.predict(
            merchant=transaction.merchant,
            amount=transaction.amount,
            timestamp=pd.Timestamp(transaction.timestamp) if transaction.timestamp else None
        )
        
        # Ghi prediction log (cho analytics và monitoring)
        if HAS_TRAINING_HISTORY:
            try:
                record_prediction(
                    model_name="Category Classification",
                    user_id=transaction.user_id or "anonymous",
                    input_text=transaction.merchant,
                    predicted_category=category,
                    confidence=confidence * 100
                )
            except Exception:
                pass  # Silently fail - không ảnh hưởng response
        
        return CategoryPrediction(
            category=category,
            confidence=confidence,
            alternatives=alternatives
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/batch-predict")
async def batch_predict(transactions: List[TransactionInput]):
    """
    Dự đoán danh mục cho nhiều giao dịch - Batch prediction for multiple transactions
    
    Xử lý phân loại nhiều giao dịch cùng một lúc, hữu ích khi:
    - Import giao dịch từ file CSV/Excel
    - Phân loại lại tất cả giao dịch chưa có category
    - Xử lý hàng loạt tin nhắn SMS ngân hàng
    
    Args:
        transactions: Danh sách các giao dịch cần phân loại
    
    Returns:
        dict: Kết quả phân loại cho từng giao dịch
            - predictions: List các kết quả (merchant, amount, category, confidence, alternatives)
    """
    
    try:
        model = get_categorizer()
        results = []
        
        # Phân loại từng giao dịch
        for transaction in transactions:
            category, confidence, alternatives = model.predict(
                merchant=transaction.merchant,
                amount=transaction.amount,
                timestamp=pd.Timestamp(transaction.timestamp) if transaction.timestamp else None
            )
            
            results.append({
                "merchant": transaction.merchant,
                "amount": float(transaction.amount),
                "category": str(category),
                "confidence": float(confidence),
                "alternatives": [
                    {"category": str(alt["category"]), "confidence": float(alt["confidence"])}
                    for alt in alternatives
                ]
            })
        
        return {"predictions": results}
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/categories")
async def get_categories():
    """
    Lấy danh sách tất cả các danh mục - Get list of all available categories
    
    Trả về tất cả các danh mục mà model được train để phân loại.
    Hữu ích cho việc hiển thị dropdown, filter, hoặc validate category.
    
    Returns:
        dict: Danh sách categories và số lượng
            - categories: List tên các danh mục
            - count: Tổng số danh mục
    
    Example:
        {"categories": ["Di chuyển", "Mua sắm", "Ăn uống", ...], "count": 15}
    """
    
    try:
        model = get_categorizer()
        categories = model.label_encoder.classes_.tolist()
        
        return {
            "categories": categories,
            "count": len(categories)
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
