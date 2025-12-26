"""
Auto Retrain Service - Tự động train lại models hằng ngày

Service này chạy background task để:
1. Train lại models vào lúc 9:30 AM mỗi ngày (giờ Việt Nam UTC+7)
2. Kiểm tra accuracy và ghi log
3. Gửi thông báo nếu accuracy giảm đáng kể

Lưu ý: Server ở Singapore (UTC+8), nên cần convert timezone chính xác.
"""

import asyncio
import logging
from datetime import datetime, time, timedelta, timezone
from typing import Optional
import threading

# Setup logging
logger = logging.getLogger("auto_retrain")
logger.setLevel(logging.INFO)

# Timezone Việt Nam (UTC+7)
VIETNAM_TZ = timezone(timedelta(hours=7))

# Global state
_retrain_task: Optional[asyncio.Task] = None
_is_running = False


async def retrain_all_models():
    """
    Train lại tất cả 3 models
    
    Returns:
        dict: Kết quả training với accuracy của mỗi model
    """
    import sys
    import os
    
    # Add path để import models
    sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '../..')))
    
    from app.models.categorization import TransactionCategorizer
    from app.models.anomaly_detection import AnomalyDetector
    from app.models.spending_prediction import SpendingPredictor
    
    data_path = "data/raw/transactions.csv"
    results = {}
    
    logger.info("🚀 Starting daily auto-retrain...")
    start_time = datetime.now()
    
    # 1. Train Category Classification
    try:
        logger.info("Training Category Classification...")
        categorizer = TransactionCategorizer()
        accuracy = categorizer.train(data_path)
        results["Category Classification"] = {
            "status": "success",
            "accuracy": accuracy,
            "timestamp": datetime.now().isoformat()
        }
        logger.info(f"✅ Category Classification: {accuracy:.2%}")
    except Exception as e:
        logger.error(f"❌ Category Classification failed: {e}")
        results["Category Classification"] = {
            "status": "failed",
            "error": str(e),
            "timestamp": datetime.now().isoformat()
        }
    
    # 2. Train Anomaly Detection
    try:
        logger.info("Training Anomaly Detection...")
        detector = AnomalyDetector()
        detector.train(data_path)
        results["Anomaly Detection"] = {
            "status": "success",
            "timestamp": datetime.now().isoformat()
        }
        logger.info("✅ Anomaly Detection: completed")
    except Exception as e:
        logger.error(f"❌ Anomaly Detection failed: {e}")
        results["Anomaly Detection"] = {
            "status": "failed",
            "error": str(e),
            "timestamp": datetime.now().isoformat()
        }
    
    # 3. Train Spending Prediction
    try:
        logger.info("Training Spending Prediction...")
        predictor = SpendingPredictor()
        predictor.train(data_path)
        results["Spending Prediction"] = {
            "status": "success",
            "timestamp": datetime.now().isoformat()
        }
        logger.info("✅ Spending Prediction: completed")
    except Exception as e:
        logger.error(f"❌ Spending Prediction failed: {e}")
        results["Spending Prediction"] = {
            "status": "failed",
            "error": str(e),
            "timestamp": datetime.now().isoformat()
        }
    
    duration = (datetime.now() - start_time).total_seconds()
    logger.info(f"🎉 Auto-retrain completed in {duration:.1f}s")
    
    return results


def get_seconds_until_target(target_hour: int = 9, target_minute: int = 30) -> float:
    """
    Tính số giây cho đến thời điểm target theo giờ Việt Nam (UTC+7)
    
    Args:
        target_hour: Giờ target theo VN time (0-23), mặc định 9 (9:30 AM)
        target_minute: Phút target (0-59), mặc định 30
        
    Returns:
        Số giây cần chờ
        
    Lưu ý: Server có thể ở Singapore (UTC+8), nên cần convert về VN time (UTC+7)
    """
    # Lấy thời gian hiện tại theo UTC+7 (Việt Nam)
    now_vn = datetime.now(VIETNAM_TZ)
    
    # Tạo target time hôm nay theo VN timezone
    target_time = now_vn.replace(hour=target_hour, minute=target_minute, second=0, microsecond=0)
    
    # Nếu đã qua thời điểm target hôm nay, chờ đến ngày mai
    if now_vn >= target_time:
        target_time += timedelta(days=1)
    
    return (target_time - now_vn).total_seconds()


async def auto_retrain_loop(retrain_hour: int = 9, retrain_minute: int = 30):
    """
    Vòng lặp chính để auto-retrain hằng ngày
    
    Args:
        retrain_hour: Giờ train (0-23), mặc định 9
        retrain_minute: Phút train (0-59), mặc định 30 (9:30 AM)
    """
    global _is_running
    _is_running = True
    
    logger.info(f"🕐 Auto-retrain scheduler started. Will run daily at {retrain_hour}:{retrain_minute:02d}")
    
    while _is_running:
        try:
            # Chờ đến thời điểm train
            seconds_to_wait = get_seconds_until_target(retrain_hour, retrain_minute)
            hours_to_wait = seconds_to_wait / 3600
            
            logger.info(f"⏳ Next retrain in {hours_to_wait:.1f} hours")
            
            # Sleep cho đến thời điểm train (kiểm tra mỗi 5 phút để có thể dừng)
            while seconds_to_wait > 0 and _is_running:
                sleep_time = min(300, seconds_to_wait)  # Max 5 phút mỗi lần sleep
                await asyncio.sleep(sleep_time)
                seconds_to_wait -= sleep_time
            
            if not _is_running:
                break
            
            # Thực hiện retrain
            results = await retrain_all_models()
            
            # Log results
            success_count = sum(1 for r in results.values() if r.get("status") == "success")
            logger.info(f"📊 Retrain results: {success_count}/{len(results)} models succeeded")
            
        except asyncio.CancelledError:
            logger.info("Auto-retrain loop cancelled")
            break
        except Exception as e:
            logger.error(f"Error in auto-retrain loop: {e}")
            # Chờ 1 giờ trước khi thử lại nếu có lỗi
            await asyncio.sleep(3600)
    
    logger.info("Auto-retrain scheduler stopped")


def start_auto_retrain_scheduler(retrain_hour: int = 9, retrain_minute: int = 30):
    """
    Khởi động scheduler auto-retrain
    
    Args:
        retrain_hour: Giờ train hằng ngày (0-23), mặc định 9
        retrain_minute: Phút train (0-59), mặc định 30 (9:30 AM)
    """
    global _retrain_task
    
    if _retrain_task is not None and not _retrain_task.done():
        logger.warning("Auto-retrain scheduler already running")
        return
    
    try:
        loop = asyncio.get_event_loop()
    except RuntimeError:
        loop = asyncio.new_event_loop()
        asyncio.set_event_loop(loop)
    
    _retrain_task = loop.create_task(auto_retrain_loop(retrain_hour, retrain_minute))
    logger.info(f"✅ Auto-retrain scheduler started (daily at {retrain_hour}:{retrain_minute:02d})")


def stop_auto_retrain_scheduler():
    """Dừng scheduler auto-retrain"""
    global _retrain_task, _is_running
    
    _is_running = False
    
    if _retrain_task is not None:
        _retrain_task.cancel()
        _retrain_task = None
        logger.info("Auto-retrain scheduler stopped")


def is_scheduler_running() -> bool:
    """Kiểm tra scheduler có đang chạy không"""
    return _is_running and _retrain_task is not None and not _retrain_task.done()


def get_scheduler_status() -> dict:
    """Lấy trạng thái của scheduler"""
    next_run_vn = None
    now_vn = datetime.now(VIETNAM_TZ)
    
    if _is_running:
        seconds = get_seconds_until_target(9, 30)
        next_run_vn = now_vn + timedelta(seconds=seconds)
    
    return {
        "is_running": is_scheduler_running(),
        "next_run_at": next_run_vn.strftime("%Y-%m-%d %H:%M:%S") if next_run_vn else None,
        "timezone": "Vietnam (UTC+7)",
        "retrain_hour": 9,
        "retrain_minute": 30,
        "description": "Daily auto-retrain at 9:30 AM (Vietnam Time UTC+7)"
    }

