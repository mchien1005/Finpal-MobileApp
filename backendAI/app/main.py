"""
FastAPI Main Application - Backend AI cho Finpal
Ứng dụng FastAPI cung cấp các API AI/ML cho hệ thống quản lý tài chính Finpal
"""

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.config import settings
from app.api import categorization, anomaly, prediction, insights

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
origins = settings.CORS_ORIGINS.split(",")
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
