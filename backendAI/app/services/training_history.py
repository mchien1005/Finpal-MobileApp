"""
Training History Service - Lưu trữ và quản lý lịch sử huấn luyện models

Service này:
1. Lưu accuracy sau mỗi lần train
2. Lưu prediction logs thực tế
3. Cung cấp dữ liệu thống kê cho admin dashboard
"""

import json
from datetime import datetime, timedelta
from typing import List, Dict, Optional
from pathlib import Path

# Path để lưu training history - sử dụng đường dẫn tuyệt đối
BASE_DIR = Path(__file__).parent.parent.parent  # backendAI/
DATA_DIR = BASE_DIR / "data"
HISTORY_FILE = DATA_DIR / "training_history.json"
PREDICTION_LOGS_FILE = DATA_DIR / "prediction_logs.json"


def _ensure_data_dir():
    """Đảm bảo thư mục data tồn tại"""
    DATA_DIR.mkdir(exist_ok=True)


def _load_history() -> Dict:
    """Load training history từ file"""
    _ensure_data_dir()
    if HISTORY_FILE.exists():
        try:
            with open(HISTORY_FILE, 'r', encoding='utf-8') as f:
                return json.load(f)
        except (json.JSONDecodeError, IOError):
            pass
    return {"models": {}, "created_at": datetime.now().isoformat()}


def _save_history(history: Dict):
    """Lưu training history vào file"""
    _ensure_data_dir()
    with open(HISTORY_FILE, 'w', encoding='utf-8') as f:
        json.dump(history, f, indent=2, ensure_ascii=False)


def record_training(model_name: str, accuracy: float, metrics: Optional[Dict] = None):
    """
    Ghi lại kết quả sau mỗi lần training
    
    Args:
        model_name: Tên model (Category Classification, Anomaly Detection, Spending Prediction)
        accuracy: Accuracy đạt được (0-100)
        metrics: Các metrics khác (precision, recall, f1, etc.)
    """
    history = _load_history()
    
    if model_name not in history["models"]:
        history["models"][model_name] = {
            "training_records": [],
            "total_trainings": 0,
            "best_accuracy": 0
        }
    
    model_history = history["models"][model_name]
    
    # Tạo record mới
    record = {
        "date": datetime.now().strftime("%Y-%m-%d"),
        "timestamp": datetime.now().isoformat(),
        "accuracy": round(accuracy, 2),
        "metrics": metrics or {}
    }
    
    # Thêm vào danh sách
    model_history["training_records"].append(record)
    model_history["total_trainings"] += 1
    
    # Cập nhật best accuracy
    if accuracy > model_history["best_accuracy"]:
        model_history["best_accuracy"] = round(accuracy, 2)
    
    # Giữ lại tối đa 90 ngày dữ liệu
    cutoff_date = (datetime.now() - timedelta(days=90)).strftime("%Y-%m-%d")
    model_history["training_records"] = [
        r for r in model_history["training_records"]
        if r["date"] >= cutoff_date
    ]
    
    _save_history(history)
    
    return record


def get_accuracy_history(model_name: str, days: int = 7) -> List[Dict]:
    """
    Lấy lịch sử accuracy của model trong N ngày gần nhất
    
    Args:
        model_name: Tên model
        days: Số ngày cần lấy
        
    Returns:
        List các records với date và accuracy
    """
    history = _load_history()
    
    if model_name not in history["models"]:
        return []
    
    records = history["models"][model_name]["training_records"]
    
    # Lọc theo số ngày
    cutoff_date = (datetime.now() - timedelta(days=days)).strftime("%Y-%m-%d")
    filtered = [r for r in records if r["date"] >= cutoff_date]
    
    # Group by date (lấy record cuối cùng của mỗi ngày)
    daily_records = {}
    for r in filtered:
        date = r["date"]
        if date not in daily_records or r["timestamp"] > daily_records[date]["timestamp"]:
            daily_records[date] = r
    
    # Sort by date
    result = sorted(daily_records.values(), key=lambda x: x["date"])
    
    return result


def get_all_models_accuracy_history(days: int = 7) -> Dict[str, List[Dict]]:
    """
    Lấy lịch sử accuracy của tất cả models
    
    Args:
        days: Số ngày cần lấy
        
    Returns:
        Dict với key là model_name và value là list records
    """
    history = _load_history()
    result = {}
    
    for model_name in history.get("models", {}):
        result[model_name] = get_accuracy_history(model_name, days)
    
    return result


def get_model_stats(model_name: str) -> Optional[Dict]:
    """
    Lấy thống kê tổng quan của model
    
    Returns:
        Dict với total_trainings, best_accuracy, latest_accuracy, trend
    """
    history = _load_history()
    
    if model_name not in history["models"]:
        return None
    
    model_data = history["models"][model_name]
    records = model_data["training_records"]
    
    # Calculate trend (so sánh 7 ngày gần nhất với 7 ngày trước đó)
    if len(records) >= 2:
        recent = [r["accuracy"] for r in records[-7:] if r]
        older = [r["accuracy"] for r in records[-14:-7] if r]
        
        recent_avg = sum(recent) / len(recent) if recent else 0
        older_avg = sum(older) / len(older) if older else recent_avg
        
        if recent_avg > older_avg + 1:
            trend = "increasing"
        elif recent_avg < older_avg - 1:
            trend = "decreasing"
        else:
            trend = "stable"
    else:
        trend = "stable"
    
    latest = records[-1] if records else None
    
    return {
        "total_trainings": model_data["total_trainings"],
        "best_accuracy": model_data["best_accuracy"],
        "latest_accuracy": latest["accuracy"] if latest else 0,
        "latest_training_date": latest["date"] if latest else None,
        "trend": trend
    }


# ============================================================================
# PREDICTION LOGS
# ============================================================================

def _load_prediction_logs() -> List[Dict]:
    """Load prediction logs từ file"""
    _ensure_data_dir()
    if PREDICTION_LOGS_FILE.exists():
        try:
            with open(PREDICTION_LOGS_FILE, 'r', encoding='utf-8') as f:
                return json.load(f)
        except (json.JSONDecodeError, IOError):
            pass
    return []


def _save_prediction_logs(logs: List[Dict]):
    """Lưu prediction logs vào file"""
    _ensure_data_dir()
    with open(PREDICTION_LOGS_FILE, 'w', encoding='utf-8') as f:
        json.dump(logs, f, indent=2, ensure_ascii=False)


def record_prediction(
    model_name: str,
    user_id: str,
    input_text: str,
    predicted_category: str,
    confidence: float,
    actual_category: Optional[str] = None,
    is_correct: Optional[bool] = None
):
    """
    Ghi lại prediction log
    
    Args:
        model_name: Tên model thực hiện prediction
        user_id: ID người dùng
        input_text: Input đầu vào
        predicted_category: Category được dự đoán
        confidence: Độ tin cậy (0-100)
        actual_category: Category thực tế (từ feedback)
        is_correct: Dự đoán có đúng không
    """
    logs = _load_prediction_logs()
    
    log_entry = {
        "id": len(logs) + 1,
        "timestamp": datetime.now().isoformat(),
        "model_name": model_name,
        "user_id": user_id,
        "input_text": input_text,
        "predicted_category": predicted_category,
        "confidence": round(confidence, 1),
        "actual_category": actual_category,
        "is_correct": is_correct
    }
    
    logs.append(log_entry)
    
    # Giữ tối đa 10000 logs gần nhất
    if len(logs) > 10000:
        logs = logs[-10000:]
    
    _save_prediction_logs(logs)
    
    return log_entry


def get_prediction_logs(
    page: int = 1,
    page_size: int = 20,
    model_name: Optional[str] = None,
    correct_only: Optional[bool] = None
) -> Dict:
    """
    Lấy prediction logs có phân trang
    
    Returns:
        Dict với logs, total, page, page_size, total_pages
    """
    logs = _load_prediction_logs()
    
    # Sort by timestamp desc
    logs = sorted(logs, key=lambda x: x["timestamp"], reverse=True)
    
    # Filter
    if model_name:
        logs = [l for l in logs if l["model_name"] == model_name]
    
    if correct_only is not None:
        logs = [l for l in logs if l["is_correct"] == correct_only]
    
    # Pagination
    total = len(logs)
    total_pages = (total + page_size - 1) // page_size
    start = (page - 1) * page_size
    end = start + page_size
    
    return {
        "logs": logs[start:end],
        "total": total,
        "page": page,
        "page_size": page_size,
        "total_pages": total_pages
    }


def update_prediction_feedback(
    log_id: int,
    actual_category: str,
    is_correct: bool
):
    """
    Cập nhật feedback cho prediction log
    
    Args:
        log_id: ID của log entry
        actual_category: Category thực tế
        is_correct: Dự đoán có đúng không
    """
    logs = _load_prediction_logs()
    
    for log in logs:
        if log["id"] == log_id:
            log["actual_category"] = actual_category
            log["is_correct"] = is_correct
            log["feedback_at"] = datetime.now().isoformat()
            break
    
    _save_prediction_logs(logs)


def get_model_accuracy_from_logs(model_name: str, days: int = 7) -> float:
    """
    Tính accuracy thực tế từ prediction logs
    
    Args:
        model_name: Tên model
        days: Số ngày tính
        
    Returns:
        Accuracy percentage (0-100)
    """
    logs = _load_prediction_logs()
    
    cutoff = (datetime.now() - timedelta(days=days)).isoformat()
    
    # Filter logs
    filtered = [
        l for l in logs
        if l["model_name"] == model_name
        and l["timestamp"] >= cutoff
        and l["is_correct"] is not None
    ]
    
    if not filtered:
        return 0.0
    
    correct_count = sum(1 for l in filtered if l["is_correct"])
    
    return round(correct_count / len(filtered) * 100, 1)


# ============================================================================
# INITIALIZATION - Tạo dữ liệu mẫu ban đầu nếu chưa có
# ============================================================================

def initialize_sample_data():
    """
    Tạo dữ liệu mẫu cho demo nếu chưa có data
    """
    history = _load_history()
    
    # Nếu đã có data, không tạo lại
    if history.get("models"):
        return
    
    # Tạo sample training history cho 30 ngày gần nhất
    models_base = {
        "Category Classification": {"base": 92.0, "variance": 2.0},
        "Anomaly Detection": {"base": 87.0, "variance": 3.0},
        "Spending Prediction": {"base": 84.0, "variance": 4.0}
    }
    
    import random
    
    for model_name, config in models_base.items():
        for i in range(30, 0, -1):
            date = datetime.now() - timedelta(days=i)
            
            # Accuracy tăng dần theo thời gian (simulate improvement)
            improvement = (30 - i) * 0.05  # 0.05% improvement per day
            accuracy = config["base"] + improvement + random.uniform(-config["variance"], config["variance"])
            accuracy = min(accuracy, 99.0)  # Cap at 99%
            
            record = {
                "date": date.strftime("%Y-%m-%d"),
                "timestamp": date.isoformat(),
                "accuracy": round(accuracy, 2),
                "metrics": {
                    "precision": round(accuracy - random.uniform(0, 2), 2),
                    "recall": round(accuracy - random.uniform(0, 3), 2),
                    "f1_score": round(accuracy - random.uniform(0, 2.5), 2)
                }
            }
            
            if model_name not in history["models"]:
                history["models"][model_name] = {
                    "training_records": [],
                    "total_trainings": 0,
                    "best_accuracy": 0
                }
            
            history["models"][model_name]["training_records"].append(record)
            history["models"][model_name]["total_trainings"] += 1
            
            if accuracy > history["models"][model_name]["best_accuracy"]:
                history["models"][model_name]["best_accuracy"] = round(accuracy, 2)
    
    _save_history(history)
    
    # Tạo sample prediction logs
    sample_predictions = [
        ("GRAB VIETNAM", "Di chuyển", 96.5, True),
        ("SHOPEE", "Mua sắm", 89.2, True),
        ("HIGHLANDS COFFEE", "Ăn uống", 78.3, True),
        ("CGV CINEMA", "Giải trí", 95.1, True),
        ("CIRCLE K", "Ăn uống", 85.3, True),
        ("LAZADA", "Mua sắm", 91.5, True),
        ("UBER", "Di chuyển", 88.7, True),
        ("STARBUCKS", "Ăn uống", 82.2, True),
        ("GRAB FOOD", "Ăn uống", 75.5, True),
        ("TIKI", "Mua sắm", 92.3, True),
        ("LOTTE CINEMA", "Giải trí", 87.6, True),
        ("VINMART", "Mua sắm", 94.1, True),
        ("PHUC LONG", "Ăn uống", 81.2, True),
        ("BEAMIN", "Ăn uống", 76.8, True),
        ("SPOTIFY", "Giải trí", 89.5, True),
        ("NETFLIX", "Giải trí", 93.2, True),
        ("FACEBOOK ADS", "Khác", 62.8, False),
        ("GOOGLE CLOUD", "Khác", 58.3, False),
    ]
    
    logs = []
    for i, (text, category, conf, correct) in enumerate(sample_predictions * 5):  # 90 logs
        timestamp = datetime.now() - timedelta(minutes=random.randint(1, 60 * 24 * 7))
        logs.append({
            "id": i + 1,
            "timestamp": timestamp.isoformat(),
            "model_name": "Category Classification",
            "user_id": f"USR{str(random.randint(1, 100)).zfill(3)}",
            "input_text": text,
            "predicted_category": category,
            "confidence": conf + random.uniform(-5, 5),
            "actual_category": category if correct else "Khác",
            "is_correct": correct
        })
    
    _save_prediction_logs(logs)
    
    print("✅ Sample training history and prediction logs created!")


# Auto-initialize khi import
initialize_sample_data()
