# 🚀 Hướng Dẫn Chạy Backend Finpal

## 📋 Yêu Cầu Hệ Thống

- **Docker Desktop** đã cài đặt và đang chạy
- **Git** (để clone project)
- **Cổng cần thiết phải trống:**
  - `3306` - MySQL Database
  - `8080` - Backend Java (Spring Boot)
  - `8000` - Backend AI (Python FastAPI)
  - `8081` - phpMyAdmin (quản lý database)

---

## ⚡ Cách Chạy Nhanh (Chỉ 2 Bước)

### Bước 1: Clone Project

```bash
git clone https://github.com/mchien1005/Finpal-MobileApp/tree/backend
cd Finpal-MobileApp
```

### Bước 2: Chạy Docker

```bash
docker-compose up -d
```

**Xong!** Đợi 2-3 phút để các service khởi động.

---

## 🔍 Kiểm Tra Trạng Thái

Kiểm tra tất cả container đã chạy chưa:

```bash
docker-compose ps
```

Kết quả mong đợi:

```
NAME                  STATUS
finpal-mysql          Up (healthy)
finpal-backend        Up (healthy)
finpal-backend-ai     Up (healthy)
finpal-phpmyadmin     Up
```

---

## 🌐 Truy Cập Các Service

| Service         | URL                                   | Mô tả                   |
| --------------- | ------------------------------------- | ----------------------- |
| **Backend API** | http://localhost:8080                 | API chính (Spring Boot) |
| **Swagger UI**  | http://localhost:8080/swagger-ui.html | Tài liệu API tương tác  |
| **Backend AI**  | http://localhost:8000                 | API phân loại giao dịch |
| **AI API Docs** | http://localhost:8000/docs            | Tài liệu FastAPI        |
| **phpMyAdmin**  | http://localhost:8081                 | Quản lý database MySQL  |

---

## 👤 Tài Khoản Demo

Hệ thống đã có sẵn 2 tài khoản để test:

### User Demo

- **Username:** `demo`
- **Password:** `123456`
- **Role:** USER
- **Có sẵn:** Tài khoản ngân hàng, giao dịch mẫu, ngân sách

### User Admin

- **Username:** `admin`
- **Password:** `123456`
- **Role:** ADMIN
- **Có sẵn:** Quyền quản trị viên

---

## 🔐 Thông Tin Database

### MySQL Connection

- **Host:** `localhost`
- **Port:** `3306`
- **Database:** `finpal_db`
- **Username:** `root`
- **Password:** `root`

### phpMyAdmin Login

- **Truy cập:** http://localhost:8081
- **Server:** `mysql`
- **Username:** `root`
- **Password:** `root`

---

## 📝 Test API với Swagger

1. Mở Swagger UI: http://localhost:8080/swagger-ui.html
2. Đăng nhập để lấy token:

   - Mở endpoint `POST /api/auth/login`
   - Click "Try it out"
   - Nhập:
     ```json
     {
       "username": "demo",
       "password": "123456"
     }
     ```
   - Click "Execute"
   - Copy **token** từ response

3. Authorize:

   - Click nút **Authorize** ở góc phải
   - Nhập: `Bearer <token-vừa-copy>`
   - Click "Authorize"

4. Giờ có thể test tất cả API!

---

## 🧪 Test API Bằng Curl/Postman

### 1. Đăng Nhập

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"123456"}'
```

**Response:**

```json
{
  "success": true,
  "message": "Đăng nhập thành công",
  "token": "eyJhbGc...",
  "user": {...}
}
```

### 2. Lấy Danh Sách Accounts (cần token)

```bash
curl -X GET http://localhost:8080/api/accounts \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

### 3. Test AI Categorization

```bash
curl -X POST http://localhost:8000/api/categorization/predict \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 50000,
    "description": "Grab đi làm",
    "merchant": "GRAB",
    "timestamp": "2025-11-22T10:30:00"
  }'
```

**Response:**

```json
{
  "category": "Di chuyển",
  "confidence": 0.9072,
  "alternatives": []
}
```

---

## 🛠️ Các Lệnh Docker Hữu Ích

### Xem Logs

```bash
# Tất cả services
docker-compose logs -f

# Chỉ backend Java
docker-compose logs -f backend

# Chỉ backend AI
docker-compose logs -f backend-ai

# Chỉ MySQL
docker-compose logs -f mysql
```

### Khởi Động Lại Services

```bash
# Restart tất cả
docker-compose restart

# Restart backend Java
docker-compose restart backend

# Restart backend AI
docker-compose restart backend-ai
```

### Tắt & Xóa Containers

```bash
# Tắt (giữ data)
docker-compose down

# Tắt & xóa volumes (MẤT DATA!)
docker-compose down -v
```

### Build Lại Image (khi có thay đổi code)

```bash
# Build lại backend Java
docker-compose build backend

# Build lại backend AI
docker-compose build backend-ai

# Build lại tất cả
docker-compose build

# Build không dùng cache (clean build)
docker-compose build --no-cache
```

---

## 🔄 Reset Database Về Trạng Thái Ban Đầu

Nếu muốn reset lại database với data mẫu:

```bash
# Cách 1: Reset qua phpMyAdmin
# 1. Truy cập http://localhost:8081
# 2. Chọn database finpal_db
# 3. Chạy file backend/database/reset_database.sql
# 4. Chạy file backend/database/seed_data.sql

# Cách 2: Reset bằng lệnh
docker exec -i finpal-mysql mysql -uroot -proot -e "DROP DATABASE IF EXISTS finpal_db; CREATE DATABASE finpal_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

cd backend
Get-Content .\database\finpal_schema.sql | docker exec -i finpal-mysql mysql -uroot -proot finpal_db
Get-Content .\database\seed_data.sql | docker exec -i finpal-mysql mysql -uroot -proot finpal_db
```

---

## 📂 Cấu Trúc Project

```
Finpal-MobileApp/
├── backend/                    # Backend Java (Spring Boot)
│   ├── src/
│   ├── database/
│   │   ├── finpal_schema.sql  # Schema database
│   │   └── seed_data.sql      # Dữ liệu mẫu
│   ├── Dockerfile
│   └── pom.xml
│
├── backendAI/                  # Backend AI (Python FastAPI)
│   ├── app/
│   ├── models/                 # ML models
│   ├── Dockerfile
│   └── requirements.txt
│
├── docker-compose.yml          # Cấu hình Docker
└── HUONG_DAN_CHAY_BACKEND.md  # File này
```

---

## ⚠️ Xử Lý Lỗi Thường Gặp

### 1. Port Already in Use (Cổng đã bị chiếm)

```
Error: bind: address already in use
```

**Giải pháp:**

- Tắt MySQL/XAMPP nếu đang chạy port 3306
- Tắt các ứng dụng đang dùng port 8080, 8000, 8081

### 2. Container Unhealthy

```bash
# Xem logs để debug
docker-compose logs backend
docker-compose logs backend-ai
docker-compose logs mysql

# Restart container
docker-compose restart backend
```

### 3. Database Connection Error

```
Communications link failure
```

**Giải pháp:**

- Đợi MySQL container healthy: `docker-compose ps`
- Restart backend: `docker-compose restart backend`

### 4. JWT Token Error

```
Illegal base64 character
```

**Giải pháp:**

- Token đã hết hạn (24h), đăng nhập lại để lấy token mới

### 5. Vietnamese Text Bị Lỗi Font (??????)

Database đã cấu hình UTF-8, nếu vẫn lỗi:

```bash
docker-compose down
docker-compose up -d
```

---

## 📊 Dữ Liệu Có Sẵn

### Categories (14 loại)

- **Thu nhập:** Lương, Thưởng, Đầu tư, Thu nhập khác
- **Chi tiêu:** Ăn uống, Mua sắm, Di chuyển, Giải trí, Hóa đơn, Sức khỏe, Giáo dục, Làm đẹp, Gia đình, Chi tiêu khác

### User Demo Có Sẵn

- 3 tài khoản ngân hàng (Vietcombank, Techcombank, Tiền mặt)
- 6 giao dịch mẫu
- 2 ngân sách (Ăn uống, Di chuyển)
- 2 mục tiêu tiết kiệm (AirPods, Du lịch Đà Lạt)

---

## 🎯 Các API Chính

### Authentication

- `POST /api/auth/register` - Đăng ký
- `POST /api/auth/login` - Đăng nhập

### Accounts

- `GET /api/accounts` - Danh sách tài khoản
- `POST /api/accounts` - Tạo tài khoản mới
- `PUT /api/accounts/{id}` - Cập nhật tài khoản
- `DELETE /api/accounts/{id}` - Xóa tài khoản

### Transactions

- `GET /api/transactions` - Danh sách giao dịch
- `POST /api/transactions` - Tạo giao dịch mới
- `GET /api/transactions/{id}` - Chi tiết giao dịch
- `PUT /api/transactions/{id}` - Cập nhật giao dịch
- `DELETE /api/transactions/{id}` - Xóa giao dịch

### Categories

- `GET /api/categories` - Danh sách danh mục
- `GET /api/categories/{id}/subcategories` - Danh mục con

### Dashboard & Statistics

- `GET /api/dashboard/summary` - Tổng quan tài chính
- `GET /api/statistics/spending-by-category` - Chi tiêu theo danh mục
- `GET /api/statistics/monthly-trend` - xu hướng theo tháng

### AI Categorization

- `POST /api/categorization/predict` - Phân loại giao dịch tự động

---

## 💡 Tips

1. **Luôn kiểm tra logs** khi có lỗi:

   ```bash
   docker-compose logs -f
   ```

2. **Token hết hạn sau 24h**, cần đăng nhập lại

3. **Swagger UI** là cách dễ nhất để test API

4. **phpMyAdmin** giúp xem và sửa data trực tiếp

5. Khi **thay đổi code**, cần build lại:
   ```bash
   docker-compose build
   docker-compose up -d
   ```

---

## 📞 Hỗ Trợ

Nếu gặp vấn đề:

1. Kiểm tra logs: `docker-compose logs -f`
2. Xem trạng thái: `docker-compose ps`
3. Restart: `docker-compose restart`
4. Build lại: `docker-compose build --no-cache`
5. Liên hệ người phát triển

---

## 🎉 Chúc Bạn Thành Công!

**Happy Coding! 🚀**
