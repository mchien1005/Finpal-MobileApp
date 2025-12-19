"""
FastAPI Main Application - Backend AI cho Finpal
Ứng dụng FastAPI cung cấp các API AI/ML cho hệ thống quản lý tài chính Finpal
"""

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.config import settings
from app.api import categorization, anomaly, prediction, insights, admin, smart_tips

# Tạo ứng dụng FastAPI
# Create FastAPI application instance với metadata và cấu hình
app = FastAPI(
    title=settings.APP_NAME,
    version=settings.APP_VERSION,
    description="AI/ML Backend for Finpal - Smart Wallet Application",
    docs_url="/docs",      # Swagger UI documentation
    redoc_url="/redoc"    # ReDoc documentation
)

# Cấu hình CORS (Cross-Origin Resource Sharing)
# Cho phép frontend từ các domain khác gọi API
origins = [origin.strip() for origin in settings.CORS_ORIGINS.split(",")]
# Thêm localhost:5173 nếu chưa có
if "http://localhost:5173" not in origins:
    origins.append("http://localhost:5173")
print(f"🔧 CORS Origins: {origins}")

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,          # Cho phép các origin cụ thể
    allow_credentials=True,         # Cho phép gửi credentials (cookies, authorization headers)
    allow_methods=["*"],            # Cho phép tất cả HTTP methods (GET, POST, PUT, DELETE, ...)
    allow_headers=["*"],            # Cho phép tất cả headers
)

# Đăng ký các router (API endpoints)
# Register API routers - mỗi router xử lý một nhóm chức năng cụ thể

# Router phân loại giao dịch tự động (Transaction Categorization)
app.include_router(
    categorization.router,
    prefix=f"{settings.API_PREFIX}/categorization",  # /api/categorization
    tags=["Categorization"]  # Nhóm API trong Swagger docs
)

# Router phát hiện giao dịch bất thường (Anomaly Detection)
app.include_router(
    anomaly.router,
    prefix=f"{settings.API_PREFIX}/anomaly",  # /api/anomaly
    tags=["Anomaly Detection"]
)

# Router dự đoán chi tiêu (Spending Prediction)
app.include_router(
    prediction.router,
    prefix=f"{settings.API_PREFIX}/prediction",  # /api/prediction
    tags=["Spending Prediction"]
)

# Router gợi ý thông minh từ AI (AI Insights & Suggestions)
app.include_router(
    insights.router,
    prefix=f"{settings.API_PREFIX}/insights",  # /api/insights
    tags=["AI Insights"]
)

# Router quản lý AI Models cho Admin
app.include_router(
    admin.router,
    prefix=f"{settings.API_PREFIX}/admin/ai",  # /api/admin/ai
    tags=["Admin - AI Management"]
)

# Router gợi ý thông minh (Smart Tips)
app.include_router(
    smart_tips.router,
    prefix=f"{settings.API_PREFIX}/tips",  # /api/tips
    tags=["Smart Tips"]
)


@app.get("/")
async def root():
    """
    Root endpoint - Endpoint gốc của API
    
    Trả về thông tin cơ bản về ứng dụng.
    Sử dụng để kiểm tra xem API có đang chạy không.
    """
    return {
        "app": settings.APP_NAME,
        "version": settings.APP_VERSION,
        "status": "running",
        "docs": "/docs"  # Link tới Swagger documentation
    }


@app.get("/health")
async def health_check():
    """
    Health check endpoint - Endpoint kiểm tra sức khỏe hệ thống
    
    Được sử dụng bởi load balancer, monitoring tools để kiểm tra
    xem service có đang hoạt động bình thường không.
    """
    return {
        "status": "healthy",
        "app": settings.APP_NAME,
        "version": settings.APP_VERSION
    }


@app.get("/health/database")
async def database_health_check():
    """
    Database health check - Kiểm tra kết nối MySQL database
    
    Trả về:
    - status: "connected" hoặc "disconnected"
    - data_source: "mysql" hoặc "csv"
    - database: tên database
    - host: host của database
    - pymysql_installed: True/False
    """
    from app.services.database import get_database_service, is_mysql_available, PYMYSQL_AVAILABLE
    from app.api.insights import USE_MYSQL
    
    result = {
        "status": "unknown",
        "data_source": "mysql" if USE_MYSQL else "csv",
        "database": settings.DB_NAME,
        "host": f"{settings.DB_HOST}:{settings.DB_PORT}",
        "pymysql_installed": PYMYSQL_AVAILABLE
    }
    
    # Kiểm tra pymysql đã cài chưa
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
    """
    Debug endpoint - Kiểm tra dữ liệu của user trong database
    
    Trả về:
    - Số lượng transactions theo loại (INCOME/EXPENSE)
    - Sample transactions
    - Thông tin kết nối
    """
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
        
        # Lấy TẤT CẢ transactions (không lọc theo type)
        all_transactions = db.get_user_transactions(user_id=user_id)
        result["transactions"]["total"] = len(all_transactions)
        
        if len(all_transactions) > 0:
            # Đếm theo loại
            types_count = all_transactions['transaction_type'].value_counts().to_dict()
            result["transactions"]["types_found"] = list(types_count.keys())
            result["transactions"]["expense"] = types_count.get('EXPENSE', 0)
            result["transactions"]["income"] = types_count.get('INCOME', 0)
            
            # Sample 5 giao dịch gần nhất
            sample = all_transactions.head(5).to_dict('records')
            # Convert datetime to string for JSON
            for s in sample:
                if 'timestamp' in s and s['timestamp'] is not None:
                    s['timestamp'] = str(s['timestamp'])
            result["transactions"]["sample"] = sample
        
        # Lấy EXPENSE transactions (như insights API sử dụng)
        expense_transactions = db.get_user_expense_transactions(user_id=user_id, months=12)
        result["expense_query_result"] = len(expense_transactions)
        
    except Exception as e:
        result["error"] = str(e)
    
    return result


if __name__ == "__main__":
    # Chạy server khi file được execute trực tiếp
    # Run the application using Uvicorn ASGI server
    import uvicorn
    uvicorn.run(
        "app.main:app",          # Module:app_instance
        host="0.0.0.0",           # Listen trên tất cả network interfaces
        port=settings.PORT,       # Port từ config (mặc định 8000)
        reload=settings.DEBUG     # Auto-reload khi code thay đổi (chỉ trong debug mode)
    )
