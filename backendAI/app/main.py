"""
FastAPI Main Application - Backend AI cho Finpal
Ứng dụng FastAPI cung cấp các API AI/ML cho hệ thống quản lý tài chính Finpal
"""

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager
from app.config import settings
from app.api import categorization, anomaly, prediction, insights, admin, smart_tips


@asynccontextmanager
async def lifespan(app: FastAPI):
    """
    Lifespan context manager - Quản lý startup và shutdown events
    
    Startup: Khởi động auto-retrain scheduler
    Shutdown: Dừng scheduler
    """
    # Startup
    print("🚀 Starting Finpal AI Backend...")
    
    # Khởi động auto-retrain scheduler (train lúc 2:00 AM hằng ngày)
    try:
        from app.services.auto_retrain import start_auto_retrain_scheduler
        start_auto_retrain_scheduler(retrain_hour=2)
        print("✅ Auto-retrain scheduler started (daily at 2:00 AM)")
    except Exception as e:
        print(f"⚠️ Could not start auto-retrain scheduler: {e}")
    
    yield
    
    # Shutdown
    print("🛑 Shutting down Finpal AI Backend...")
    try:
        from app.services.auto_retrain import stop_auto_retrain_scheduler
        stop_auto_retrain_scheduler()
        print("✅ Auto-retrain scheduler stopped")
    except Exception as e:
        print(f"⚠️ Error stopping scheduler: {e}")


# Tạo ứng dụng FastAPI với lifespan
app = FastAPI(
    title=settings.APP_NAME,
    version=settings.APP_VERSION,
    description="AI/ML Backend for Finpal - Smart Wallet Application",
    docs_url="/docs",
    redoc_url="/redoc",
    lifespan=lifespan
)

# Cấu hình CORS (Cross-Origin Resource Sharing)
origins = [origin.strip() for origin in settings.CORS_ORIGINS.split(",")]
if "http://localhost:5173" not in origins:
    origins.append("http://localhost:5173")
print(f"🔧 CORS Origins: {origins}")

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Đăng ký các router (API endpoints)
app.include_router(
    categorization.router,
    prefix=f"{settings.API_PREFIX}/categorization",
    tags=["Categorization"]
)

app.include_router(
    anomaly.router,
    prefix=f"{settings.API_PREFIX}/anomaly",
    tags=["Anomaly Detection"]
)

app.include_router(
    prediction.router,
    prefix=f"{settings.API_PREFIX}/prediction",
    tags=["Spending Prediction"]
)

app.include_router(
    insights.router,
    prefix=f"{settings.API_PREFIX}/insights",
    tags=["AI Insights"]
)

app.include_router(
    admin.router,
    prefix=f"{settings.API_PREFIX}/admin/ai",
    tags=["Admin - AI Management"]
)

app.include_router(
    smart_tips.router,
    prefix=f"{settings.API_PREFIX}/tips",
    tags=["Smart Tips"]
)


@app.get("/")
async def root():
    """Root endpoint - Thông tin cơ bản về API"""
    return {
        "app": settings.APP_NAME,
        "version": settings.APP_VERSION,
        "status": "running",
        "docs": "/docs"
    }


@app.get("/health")
async def health_check():
    """Health check endpoint"""
    from app.services.auto_retrain import get_scheduler_status
    
    scheduler = get_scheduler_status()
    
    return {
        "status": "healthy",
        "app": settings.APP_NAME,
        "version": settings.APP_VERSION,
        "auto_retrain": scheduler
    }


@app.get("/health/scheduler")
async def scheduler_status():
    """
    Kiểm tra trạng thái auto-retrain scheduler
    
    Returns:
        Trạng thái scheduler: is_running, next_run_at, retrain_hour
    """
    from app.services.auto_retrain import get_scheduler_status
    return get_scheduler_status()


@app.post("/health/scheduler/start")
async def start_scheduler():
    """
    Khởi động lại auto-retrain scheduler
    """
    from app.services.auto_retrain import start_auto_retrain_scheduler, get_scheduler_status
    
    start_auto_retrain_scheduler(retrain_hour=2)
    
    return {
        "message": "Auto-retrain scheduler started",
        "status": get_scheduler_status()
    }


@app.post("/health/scheduler/stop")
async def stop_scheduler():
    """
    Dừng auto-retrain scheduler
    """
    from app.services.auto_retrain import stop_auto_retrain_scheduler, get_scheduler_status
    
    stop_auto_retrain_scheduler()
    
    return {
        "message": "Auto-retrain scheduler stopped",
        "status": get_scheduler_status()
    }


@app.post("/health/scheduler/run-now")
async def run_retrain_now():
    """
    Trigger retrain ngay lập tức (không chờ đến 2:00 AM)
    
    Hữu ích khi muốn train lại models sau khi upload data mới.
    """
    from app.services.auto_retrain import retrain_all_models
    
    results = await retrain_all_models()
    
    success_count = sum(1 for r in results.values() if r.get("status") == "success")
    
    return {
        "message": f"Retrain completed: {success_count}/{len(results)} models succeeded",
        "results": results
    }


@app.get("/health/database")
async def database_health_check():
    """Database health check - Kiểm tra kết nối MySQL database"""
    from app.services.database import get_database_service, is_mysql_available, PYMYSQL_AVAILABLE
    from app.api.insights import USE_MYSQL
    
    result = {
        "status": "unknown",
        "data_source": "mysql" if USE_MYSQL else "csv",
        "database": settings.DB_NAME,
        "host": f"{settings.DB_HOST}:{settings.DB_PORT}",
        "pymysql_installed": PYMYSQL_AVAILABLE
    }
    
    if not PYMYSQL_AVAILABLE:
        result["status"] = "error"
        result["data_source"] = "csv (fallback)"
        result["message"] = "❌ pymysql chưa được cài đặt. Chạy: pip install pymysql"
        result["fix_command"] = "pip install pymysql"
        return result
    
    if USE_MYSQL:
        try:
            db = get_database_service()
            if db.check_connection():
                result["status"] = "connected"
                result["message"] = "✅ Kết nối MySQL thành công. Đang sử dụng dữ liệu thật."
            else:
                result["status"] = "disconnected"
                result["message"] = "❌ Không thể kết nối MySQL. Sẽ fallback sang CSV."
        except ImportError as e:
            result["status"] = "error"
            result["message"] = f"❌ Lỗi import: {str(e)}"
            result["data_source"] = "csv (fallback)"
            result["fix_command"] = "pip install pymysql"
        except Exception as e:
            result["status"] = "error"
            result["message"] = f"❌ Lỗi kết nối: {str(e)}"
            result["data_source"] = "csv (fallback)"
    else:
        result["status"] = "disabled"
        result["message"] = "ℹ️ MySQL đã tắt. Đang sử dụng dữ liệu mẫu từ CSV."
    
    return result


@app.get("/health/debug/{user_id}")
async def debug_user_data(user_id: int):
    """Debug endpoint - Kiểm tra dữ liệu của user trong database"""
    from app.services.database import get_database_service, PYMYSQL_AVAILABLE
    
    result = {
        "user_id": user_id,
        "pymysql_available": PYMYSQL_AVAILABLE,
        "transactions": {
            "total": 0,
            "expense": 0,
            "income": 0,
            "types_found": [],
            "sample": []
        },
        "error": None
    }
    
    if not PYMYSQL_AVAILABLE:
        result["error"] = "pymysql not installed"
        return result
    
    try:
        db = get_database_service()
        
        all_transactions = db.get_user_transactions(user_id=user_id)
        result["transactions"]["total"] = len(all_transactions)
        
        if len(all_transactions) > 0:
            types_count = all_transactions['transaction_type'].value_counts().to_dict()
            result["transactions"]["types_found"] = list(types_count.keys())
            result["transactions"]["expense"] = types_count.get('EXPENSE', 0)
            result["transactions"]["income"] = types_count.get('INCOME', 0)
            
            sample = all_transactions.head(5).to_dict('records')
            for s in sample:
                if 'timestamp' in s and s['timestamp'] is not None:
                    s['timestamp'] = str(s['timestamp'])
            result["transactions"]["sample"] = sample
        
        expense_transactions = db.get_user_expense_transactions(user_id=user_id, months=12)
        result["expense_query_result"] = len(expense_transactions)
        
    except Exception as e:
        result["error"] = str(e)
    
    return result


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "app.main:app",
        host="0.0.0.0",
        port=settings.PORT,
        reload=settings.DEBUG
    )

