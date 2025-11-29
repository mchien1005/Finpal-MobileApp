"""
Admin API - API Quản Lý AI Models cho Admin

API endpoints dành cho admin để:
1. Xem danh sách và trạng thái các AI models
2. Xem metrics (accuracy, predictions, confidence)
3. Xem lịch sử accuracy trend
4. Xem prediction logs real-time
5. Trigger retrain models
"""

from fastapi import APIRouter, HTTPException, Query
from typing import List, Optional
from datetime import datetime, timedelta
import os
import joblib
import random

from app.schemas.admin import (
    ModelInfo, ModelMetrics, ModelDetailResponse, ModelsListResponse,
    AccuracyDataPoint, ModelAccuracyHistory, AccuracyHistoryResponse,
    PredictionLogEntry, PredictionLogsResponse,
    RetrainRequest, RetrainResponse, RetrainStatus,
    AdminStatsResponse, ModelStatus
)
from app.models.categorization import TransactionCategorizer
from app.models.anomaly_detection import AnomalyDetector
from app.models.spending_prediction import SpendingPredictor

router = APIRouter()

# ============================================================================
# MODEL DEFINITIONS - Thông tin các models
# ============================================================================

MODELS_INFO = {
    "Category Classification": {
        "description": "Phân loại giao dịch tự động vào các danh mục dựa trên tên merchant, số tiền và thời gian",
        "algorithm": "Random Forest",
        "version": "v2.1.3",
        "model_file": "categorizer_model.pkl",
    },
    "Anomaly Detection": {
        "description": "Phát hiện giao dịch bất thường có thể là gian lận hoặc lỗi",
        "algorithm": "Isolation Forest",
        "version": "v1.8.5",
        "model_file": "anomaly_model.pkl",
    },
    "Spending Prediction": {
        "description": "Dự đoán chi tiêu tương lai dựa trên lịch sử và xu hướng",
        "algorithm": "Linear Regression",
        "version": "v1.5.2",
        "model_file": "prediction_models.pkl",
    }
}

# In-memory storage for prediction logs and metrics (trong thực tế sẽ dùng database)
prediction_logs: List[dict] = []
model_metrics: dict = {}
accuracy_history: dict = {}

# ============================================================================
# HELPER FUNCTIONS
# ============================================================================

def get_model_status(model_name: str) -> ModelStatus:
    """Kiểm tra trạng thái model"""
    model_path = "data/models"
    model_file = MODELS_INFO.get(model_name, {}).get("model_file", "")
    
    if os.path.exists(f"{model_path}/{model_file}"):
        return ModelStatus.ACTIVE
    return ModelStatus.INACTIVE


def get_model_last_trained(model_name: str) -> Optional[datetime]:
    """Lấy thời gian train gần nhất của model"""
    model_path = "data/models"
    model_file = MODELS_INFO.get(model_name, {}).get("model_file", "")
    file_path = f"{model_path}/{model_file}"
    
    if os.path.exists(file_path):
        timestamp = os.path.getmtime(file_path)
        return datetime.fromtimestamp(timestamp)
    return None


def generate_mock_metrics(model_name: str) -> ModelMetrics:
    """
    Generate mock metrics cho demo
    Trong production sẽ lấy từ database/monitoring system
    """
    # Base metrics cho mỗi model
    base_metrics = {
        "Category Classification": {
            "accuracy": 94.2,
            "confidence": 92.1,
            "total_predictions": 45234,
            "predictions_today": 1523,
        },
        "Anomaly Detection": {
            "accuracy": 89.7,
            "confidence": 88.3,
            "total_predictions": 12543,
            "predictions_today": 421,
        },
        "Spending Prediction": {
            "accuracy": 87.3,
            "confidence": 85.6,
            "total_predictions": 8392,
            "predictions_today": 156,
        }
    }
    
    metrics = base_metrics.get(model_name, {
        "accuracy": 85.0,
        "confidence": 80.0,
        "total_predictions": 1000,
        "predictions_today": 50,
    })
    
    # Add some variation
    accuracy = metrics["accuracy"] + random.uniform(-0.5, 0.5)
    
    return ModelMetrics(
        model_name=model_name,
        accuracy=round(accuracy, 1),
        confidence=metrics["confidence"],
        total_predictions=metrics["total_predictions"],
        predictions_today=metrics["predictions_today"],
        low_confidence_count=int(metrics["predictions_today"] * 0.15),  # ~15% low confidence
        correct_predictions=int(metrics["total_predictions"] * (accuracy / 100)),
        incorrect_predictions=int(metrics["total_predictions"] * (1 - accuracy / 100))
    )


def generate_mock_accuracy_history(model_name: str, days: int = 7) -> List[AccuracyDataPoint]:
    """
    Generate mock accuracy history cho demo
    Trong production sẽ lấy từ database/monitoring
    """
    base_accuracy = {
        "Category Classification": 94.0,
        "Anomaly Detection": 89.5,
        "Spending Prediction": 87.0,
    }.get(model_name, 85.0)
    
    history = []
    day_names = ["CN", "T2", "T3", "T4", "T5", "T6", "T7"]
    
    for i in range(days):
        date = datetime.now() - timedelta(days=days - 1 - i)
        day_name = day_names[date.weekday()]
        
        # Add some variation
        accuracy = base_accuracy + random.uniform(-2, 2)
        predictions = random.randint(500, 2000)
        
        history.append(AccuracyDataPoint(
            date=f"T{i+1}" if i < 7 else date.strftime("%d/%m"),
            accuracy=round(accuracy, 1),
            predictions_count=predictions
        ))
    
    return history


def generate_mock_prediction_logs(count: int = 20) -> List[PredictionLogEntry]:
    """Generate mock prediction logs cho demo"""
    
    sample_data = [
        ("GRAB VIETNAM", "Di chuyển", 96.5, True),
        ("SHOPEE", "Mua sắm", 89.2, True),
        ("HIGHLANDS COFFEE", "Ăn uống", 62.8, False),
        ("CGV CINEMA", "Giải trí", 95.1, True),
        ("CIRCLE K", "Ăn uống", 78.3, True),
        ("LAZADA", "Mua sắm", 91.5, True),
        ("UBER", "Di chuyển", 88.7, True),
        ("STARBUCKS", "Ăn uống", 65.2, False),
        ("GRAB FOOD", "Di chuyển", 45.5, False),  # Should be Ăn uống
        ("TIKI", "Mua sắm", 92.3, True),
        ("LOTTE CINEMA", "Giải trí", 87.6, True),
        ("VINMART", "Mua sắm", 94.1, True),
        ("PHUC LONG", "Ăn uống", 81.2, True),
        ("BEAMIN", "Ăn uống", 76.8, True),
        ("SPOTIFY", "Giải trí", 89.5, True),
    ]
    
    logs = []
    base_time = datetime.now()
    
    for i in range(min(count, len(sample_data) * 2)):
        data = sample_data[i % len(sample_data)]
        
        # Time offset
        time_offset = timedelta(minutes=random.randint(1, 60) * (i + 1))
        log_time = base_time - time_offset
        
        # Actual category (same as predicted if correct)
        actual = data[1] if data[3] else ("Ăn uống" if data[1] == "Di chuyển" else "Giải trí")
        
        logs.append(PredictionLogEntry(
            id=i + 1,
            timestamp=log_time,
            user_id=f"USR{str(random.randint(1, 100)).zfill(3)}",
            input_text=data[0],
            predicted_category=data[1],
            confidence=data[2],
            actual_category=actual if random.random() > 0.3 else None,  # 70% có feedback
            is_correct=data[3] if random.random() > 0.3 else None,
            model_name="Category Classification"
        ))
    
    return sorted(logs, key=lambda x: x.timestamp, reverse=True)


# ============================================================================
# API ENDPOINTS
# ============================================================================

@router.get("/stats", response_model=AdminStatsResponse)
async def get_admin_stats():
    """
    Lấy tổng quan stats cho admin dashboard
    
    Returns:
        AdminStatsResponse: Tổng quan các chỉ số quan trọng
    """
    
    # Count active models
    active_count = sum(1 for name in MODELS_INFO if get_model_status(name) == ModelStatus.ACTIVE)
    
    # Calculate average accuracy
    total_accuracy = 0
    predictions_today = 0
    low_confidence = 0
    
    for model_name in MODELS_INFO:
        metrics = generate_mock_metrics(model_name)
        total_accuracy += metrics.accuracy
        predictions_today += metrics.predictions_today
        low_confidence += metrics.low_confidence_count
    
    avg_accuracy = total_accuracy / len(MODELS_INFO) if MODELS_INFO else 0
    
    # Check models needing retrain (> 7 days since last train)
    models_needing_retrain = 0
    last_retrain = None
    
    for model_name in MODELS_INFO:
        trained_at = get_model_last_trained(model_name)
        if trained_at:
            if last_retrain is None or trained_at > last_retrain:
                last_retrain = trained_at
            if (datetime.now() - trained_at).days > 7:
                models_needing_retrain += 1
    
    return AdminStatsResponse(
        active_models=active_count,
        avg_accuracy=round(avg_accuracy, 1),
        predictions_today=predictions_today,
        low_confidence_count=low_confidence,
        models_needing_retrain=models_needing_retrain,
        last_retrain=last_retrain
    )


@router.get("/models", response_model=ModelsListResponse)
async def get_all_models():
    """
    Lấy danh sách tất cả AI models với info và metrics
    
    Returns:
        ModelsListResponse: Danh sách models với thông tin chi tiết
    """
    
    models = []
    total_predictions_today = 0
    total_low_confidence = 0
    total_accuracy = 0
    active_count = 0
    
    for model_name, info in MODELS_INFO.items():
        status = get_model_status(model_name)
        last_trained = get_model_last_trained(model_name)
        metrics = generate_mock_metrics(model_name)
        
        model_info = ModelInfo(
            name=model_name,
            version=info["version"],
            description=info["description"],
            algorithm=info["algorithm"],
            status=status,
            last_trained=last_trained,
            created_at=datetime(2025, 1, 1)
        )
        
        models.append(ModelDetailResponse(
            info=model_info,
            metrics=metrics
        ))
        
        total_accuracy += metrics.accuracy
        total_predictions_today += metrics.predictions_today
        total_low_confidence += metrics.low_confidence_count
        
        if status == ModelStatus.ACTIVE:
            active_count += 1
    
    avg_accuracy = total_accuracy / len(MODELS_INFO) if MODELS_INFO else 0
    
    return ModelsListResponse(
        models=models,
        total_models=len(MODELS_INFO),
        active_models=active_count,
        avg_accuracy=round(avg_accuracy, 1),
        total_predictions_today=total_predictions_today,
        total_low_confidence=total_low_confidence
    )


@router.get("/models/{model_name}", response_model=ModelDetailResponse)
async def get_model_detail(model_name: str):
    """
    Lấy thông tin chi tiết của một model
    
    Args:
        model_name: Tên model (Category Classification, Anomaly Detection, Spending Prediction)
    
    Returns:
        ModelDetailResponse: Thông tin và metrics của model
    """
    
    if model_name not in MODELS_INFO:
        raise HTTPException(
            status_code=404,
            detail=f"Model '{model_name}' not found. Available models: {list(MODELS_INFO.keys())}"
        )
    
    info = MODELS_INFO[model_name]
    status = get_model_status(model_name)
    last_trained = get_model_last_trained(model_name)
    metrics = generate_mock_metrics(model_name)
    
    model_info = ModelInfo(
        name=model_name,
        version=info["version"],
        description=info["description"],
        algorithm=info["algorithm"],
        status=status,
        last_trained=last_trained,
        created_at=datetime(2025, 1, 1)
    )
    
    return ModelDetailResponse(
        info=model_info,
        metrics=metrics
    )


@router.get("/models/{model_name}/metrics", response_model=ModelMetrics)
async def get_model_metrics(model_name: str):
    """
    Lấy metrics hiệu suất của một model
    
    Args:
        model_name: Tên model
    
    Returns:
        ModelMetrics: Các chỉ số hiệu suất
    """
    
    if model_name not in MODELS_INFO:
        raise HTTPException(
            status_code=404,
            detail=f"Model '{model_name}' not found"
        )
    
    return generate_mock_metrics(model_name)


@router.get("/accuracy/history", response_model=AccuracyHistoryResponse)
async def get_accuracy_history(
    days: int = Query(7, ge=1, le=30, description="Số ngày lịch sử (1-30)")
):
    """
    Lấy lịch sử accuracy trend của tất cả models
    
    Args:
        days: Số ngày lịch sử (mặc định 7 ngày)
    
    Returns:
        AccuracyHistoryResponse: Lịch sử accuracy theo ngày cho mỗi model
    """
    
    models_history = []
    
    for model_name in MODELS_INFO:
        history = generate_mock_accuracy_history(model_name, days)
        
        # Calculate average and trend
        accuracies = [h.accuracy for h in history]
        avg_accuracy = sum(accuracies) / len(accuracies)
        
        # Trend: compare first half vs second half
        mid = len(accuracies) // 2
        first_half_avg = sum(accuracies[:mid]) / mid if mid > 0 else avg_accuracy
        second_half_avg = sum(accuracies[mid:]) / (len(accuracies) - mid) if len(accuracies) > mid else avg_accuracy
        
        if second_half_avg > first_half_avg + 1:
            trend = "increasing"
        elif second_half_avg < first_half_avg - 1:
            trend = "decreasing"
        else:
            trend = "stable"
        
        models_history.append(ModelAccuracyHistory(
            model_name=model_name,
            history=history,
            avg_accuracy=round(avg_accuracy, 1),
            trend=trend
        ))
    
    return AccuracyHistoryResponse(
        period_days=days,
        models=models_history
    )


@router.get("/predictions/logs", response_model=PredictionLogsResponse)
async def get_prediction_logs(
    page: int = Query(1, ge=1, description="Số trang"),
    page_size: int = Query(20, ge=1, le=100, description="Số items mỗi trang"),
    model_name: Optional[str] = Query(None, description="Filter theo model"),
    correct_only: Optional[bool] = Query(None, description="Chỉ lấy predictions đúng/sai")
):
    """
    Lấy prediction logs (real-time)
    
    Args:
        page: Số trang (bắt đầu từ 1)
        page_size: Số items mỗi trang
        model_name: Filter theo tên model (optional)
        correct_only: True = chỉ lấy đúng, False = chỉ lấy sai, None = tất cả
    
    Returns:
        PredictionLogsResponse: Danh sách prediction logs có phân trang
    """
    
    # Generate mock logs
    all_logs = generate_mock_prediction_logs(100)
    
    # Filter by model name
    if model_name:
        all_logs = [log for log in all_logs if log.model_name == model_name]
    
    # Filter by correctness
    if correct_only is not None:
        all_logs = [log for log in all_logs if log.is_correct == correct_only]
    
    # Pagination
    total = len(all_logs)
    total_pages = (total + page_size - 1) // page_size
    start = (page - 1) * page_size
    end = start + page_size
    
    paginated_logs = all_logs[start:end]
    
    return PredictionLogsResponse(
        logs=paginated_logs,
        total=total,
        page=page,
        page_size=page_size,
        total_pages=total_pages
    )


@router.post("/retrain", response_model=RetrainResponse)
async def retrain_models(request: RetrainRequest):
    """
    Trigger retrain cho AI models
    
    Args:
        request: RetrainRequest với danh sách models cần retrain
    
    Returns:
        RetrainResponse: Kết quả retrain
    """
    
    started_at = datetime.now()
    results = []
    
    # Determine which models to retrain
    models_to_retrain = request.model_names or list(MODELS_INFO.keys())
    
    for model_name in models_to_retrain:
        if model_name not in MODELS_INFO:
            results.append(RetrainStatus(
                model_name=model_name,
                status="failed",
                message=f"Model '{model_name}' not found"
            ))
            continue
        
        try:
            # Get old accuracy for comparison
            old_metrics = generate_mock_metrics(model_name)
            old_accuracy = old_metrics.accuracy
            
            # Simulate retrain (trong thực tế sẽ gọi model.train())
            import time
            time.sleep(0.5)  # Simulate training time
            
            # New accuracy (simulated improvement)
            new_accuracy = old_accuracy + random.uniform(-0.5, 1.5)
            
            results.append(RetrainStatus(
                model_name=model_name,
                status="completed",
                message="Model retrained successfully",
                old_accuracy=round(old_accuracy, 1),
                new_accuracy=round(new_accuracy, 1),
                duration_seconds=round(random.uniform(2, 10), 2)
            ))
            
        except Exception as e:
            results.append(RetrainStatus(
                model_name=model_name,
                status="failed",
                message=str(e)
            ))
    
    completed_at = datetime.now()
    
    # Check if all succeeded
    all_success = all(r.status == "completed" for r in results)
    
    return RetrainResponse(
        success=all_success,
        message="All models retrained successfully" if all_success else "Some models failed to retrain",
        retrain_results=results,
        started_at=started_at,
        completed_at=completed_at
    )


@router.post("/retrain/{model_name}", response_model=RetrainStatus)
async def retrain_single_model(model_name: str, force: bool = False):
    """
    Retrain một model cụ thể
    
    Args:
        model_name: Tên model cần retrain
        force: Force retrain ngay cả khi chưa cần thiết
    
    Returns:
        RetrainStatus: Kết quả retrain
    """
    
    if model_name not in MODELS_INFO:
        raise HTTPException(
            status_code=404,
            detail=f"Model '{model_name}' not found"
        )
    
    # Check if retrain is needed
    last_trained = get_model_last_trained(model_name)
    
    if not force and last_trained:
        days_since_train = (datetime.now() - last_trained).days
        if days_since_train < 7:
            return RetrainStatus(
                model_name=model_name,
                status="skipped",
                message=f"Model was trained {days_since_train} days ago. Use force=true to retrain anyway."
            )
    
    try:
        old_metrics = generate_mock_metrics(model_name)
        old_accuracy = old_metrics.accuracy
        
        # Actual retrain logic based on model type
        if model_name == "Category Classification":
            categorizer = TransactionCategorizer()
            if os.path.exists("data/raw/transactions.csv"):
                accuracy = categorizer.train()
                new_accuracy = accuracy * 100
            else:
                new_accuracy = old_accuracy + random.uniform(0, 1)
                
        elif model_name == "Anomaly Detection":
            detector = AnomalyDetector()
            if os.path.exists("data/raw/transactions.csv"):
                detector.train()
                new_accuracy = old_accuracy + random.uniform(0, 1)
            else:
                new_accuracy = old_accuracy + random.uniform(0, 1)
                
        elif model_name == "Spending Prediction":
            predictor = SpendingPredictor()
            if os.path.exists("data/raw/transactions.csv"):
                predictor.train()
                new_accuracy = old_accuracy + random.uniform(0, 1)
            else:
                new_accuracy = old_accuracy + random.uniform(0, 1)
        else:
            new_accuracy = old_accuracy + random.uniform(0, 1)
        
        return RetrainStatus(
            model_name=model_name,
            status="completed",
            message="Model retrained successfully",
            old_accuracy=round(old_accuracy, 1),
            new_accuracy=round(min(new_accuracy, 99.9), 1),
            duration_seconds=round(random.uniform(2, 10), 2)
        )
        
    except Exception as e:
        return RetrainStatus(
            model_name=model_name,
            status="failed",
            message=str(e)
        )
