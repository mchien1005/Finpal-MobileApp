# 🎉 Deploy Backend Finpal Lên AWS EC2 - HOÀN TẤT

## ✅ Trạng Thái Deploy

**Ngày deploy:** 22/11/2025  
**Server:** AWS EC2 - Ubuntu 22.04.5 LTS  
**Instance Type:** t2.medium (2 vCPU, 4GB RAM)  
**Elastic IP:** 175.41.150.228

---

ssh -i D:\finpal-key.pem ubuntu@175.41.150.228
cd ~/projects/Finpal-MobileApp
docker compose up -d
docker compose ps

ssh -i finpal-key.pem ubuntu@<ELASTIC_IP>

cd ~/projects/Finpal-MobileApp

# Pull code mới

git pull origin backend

# Rebuild & restart containers

docker compose -f docker-compose.production.yml build backend
docker compose -f docker-compose.production.yml up -d backend

# Kiểm tra logs

docker logs finpal-backend -f

## 📊 Thông Tin Services

### 1. **MySQL Database**

- **Container:** `finpal-mysql`
- **Image:** `mysql:8.0`
- **Port:** 3306 (internal only)
- **Status:** ✅ Healthy
- **Database:** `finpal_db`
- **Character Set:** UTF-8 (utf8mb4_unicode_ci)
- **Credentials:**
  - Username: `root`
  - Password: `FinpalSecure2024!@#`

### 2. **Backend Java (Spring Boot)**

- **Container:** `finpal-backend`
- **Port:** 8080
- **Status:** ✅ Running
- **Framework:** Spring Boot 3.x, Java 21
- **Features:**
  - JWT Authentication (24h expiration)
  - RESTful API
  - Swagger UI Documentation
  - MySQL Integration
  - Security Configuration
- **URLs:**
  - API: http://175.41.150.228:8080
  - Swagger UI: http://175.41.150.228:8080/swagger-ui.html
  - Available Servers in Swagger:
    - ✅ `http://175.41.150.228:8080` - AWS EC2 Server
    - `http://localhost:8080` - Local Development Server
    - `https://api.finpal.com` - Production Server (future)

### 3. **Backend AI (Python FastAPI)**

- **Container:** `finpal-backend-ai`
- **Port:** 8000
- **Status:** ✅ Running
- **Framework:** FastAPI, Python 3.11
- **ML Libraries:** scikit-learn 1.5.2
- **Features:**
  - AI Transaction Categorization
  - Anomaly Detection
  - Spending Prediction
  - Batch Processing
- **URLs:**
  - API: http://175.41.150.228:8000
  - API Docs (Swagger): http://175.41.150.228:8000/docs
  - ReDoc: http://175.41.150.228:8000/redoc
- **Models:**
  - ✅ Categorization Model Trained
  - ✅ Anomaly Detection Model Trained
  - ✅ Prediction Model Trained

### 4. **phpMyAdmin**

- **Container:** `finpal-phpmyadmin`
- **Port:** 8081
- **Status:** ✅ Running
- **URL:** http://175.41.150.228:8081
- **Login:**
  - Server: `mysql`
  - Username: `root`
  - Password: `FinpalSecure2024!@#`

---

## 🔐 Tài Khoản Demo

### User 1: Demo

- **Username:** `demo`
- **Password:** `123456`
- **Role:** USER
- **Email:** demo@finpal.com
- **Dữ liệu có sẵn:**
  - 3 tài khoản ngân hàng (Vietcombank, Techcombank, Tiền mặt)
  - 6 giao dịch mẫu
  - 2 ngân sách (Ăn uống, Di chuyển)
  - 2 mục tiêu tiết kiệm

### User 2: Admin

- **Username:** `admin`
- **Password:** `123456`
- **Role:** ADMIN
- **Email:** admin@finpal.com

---

## 🧪 Test API Examples

### 1. **Login** (Backend Java)

**Request:**

```bash
curl -X POST http://175.41.150.228:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "demo",
    "password": "123456"
  }'
```

**Response:**

```json
{
  "success": true,
  "message": "Đăng nhập thành công",
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": 1,
    "username": "demo",
    "email": "demo@finpal.com",
    "fullName": "Người dùng Demo",
    "role": "USER"
  }
}
```

### 2. **Get Accounts** (Cần Token)

**Request:**

```bash
curl -X GET http://175.41.150.228:8080/api/accounts \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### 3. **AI Categorization** (Backend AI)

**Request:**

```bash
curl -X POST http://175.41.150.228:8000/api/categorization/predict \
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

### 4. **Batch Categorization**

**Request:**

```bash
curl -X POST http://175.41.150.228:8000/api/categorization/batch-predict \
  -H "Content-Type: application/json" \
  -d '{
    "transactions": [
      {
        "amount": 50000,
        "description": "Grab đi làm",
        "merchant": "GRAB",
        "timestamp": "2025-11-22T10:30:00"
      }
    ]
  }'
```

---

## 🛠️ Các Lệnh Quản Lý

### **SSH vào Server**

**Windows PowerShell:**

```powershell
ssh -i D:\finpal-key.pem ubuntu@175.41.150.228
```

### **Kiểm Tra Trạng Thái**

```bash
cd ~/projects/Finpal-MobileApp

# Xem tất cả containers
docker compose ps

# Xem logs
docker compose logs -f

# Xem logs của service cụ thể
docker compose logs -f backend
docker compose logs -f backend-ai
docker compose logs -f mysql
```

### **Restart Services**

```bash
# Restart tất cả
docker compose restart

# Restart service cụ thể
docker compose restart backend
docker compose restart backend-ai
docker compose restart mysql
```

### **Stop/Start Services**

```bash
# Stop tất cả (giữ data)
docker compose down

# Start tất cả
docker compose up -d

# Rebuild sau khi thay đổi code
docker compose build
docker compose up -d
```

### **Train Lại AI Models**

```bash
# Vào container AI
docker exec -it finpal-backend-ai bash

# Generate sample data (nếu cần)
python scripts/generate_sample_data.py

# Train models
python scripts/train_models.py

# Exit container
exit

# Restart AI service
docker compose restart backend-ai
```

### **Backup Database**

```bash
# Tạo backup
docker exec finpal-mysql mysqldump -uroot -pFinpalSecure2024!@# finpal_db > ~/backups/finpal_db_$(date +%Y%m%d_%H%M%S).sql

# Restore từ backup
cat ~/backups/finpal_db_20251122_103000.sql | docker exec -i finpal-mysql mysql -uroot -pFinpalSecure2024!@# finpal_db
```

---

## 🔒 Bảo Mật

### **Security Groups đã cấu hình:**

| Port | Protocol | Source    | Description              |
| ---- | -------- | --------- | ------------------------ |
| 22   | TCP      | My IP     | SSH                      |
| 80   | TCP      | 0.0.0.0/0 | HTTP                     |
| 443  | TCP      | 0.0.0.0/0 | HTTPS                    |
| 8080 | TCP      | 0.0.0.0/0 | Backend Java API         |
| 8000 | TCP      | 0.0.0.0/0 | Backend AI API           |
| 8081 | TCP      | 0.0.0.0/0 | phpMyAdmin (nên hạn chế) |
| 3306 | TCP      | Internal  | MySQL (internal only)    |

### **Khuyến Nghị Bảo Mật:**

1. **Giới hạn phpMyAdmin:**

   ```bash
   # Chỉnh docker-compose.yml
   phpmyadmin:
     ports:
       - "127.0.0.1:8081:80"  # Chỉ localhost
   ```

2. **Setup Firewall (UFW):**

   ```bash
   sudo ufw allow OpenSSH
   sudo ufw allow 'Nginx Full'
   sudo ufw enable
   ```

3. **Đổi mật khẩu mạnh hơn:**

   - MySQL root password
   - JWT secret key
   - Demo user passwords

4. **Setup SSL/HTTPS** (Bước 5 trong HUONG_DAN_DEPLOY_AWS.md)

---

## 🚀 Các Bước Đã Hoàn Thành

- [x] **Bước 1:** Tạo EC2 Instance (t2.medium, Ubuntu 22.04.5)
- [x] **Bước 1.2:** Cấu hình Security Groups (6 ports)
- [x] **Bước 1.3:** Allocate & Associate Elastic IP (175.41.150.228)
- [x] **Bước 2.1:** SSH vào server (fix .pem permissions)
- [x] **Bước 2.2:** Update system packages
- [x] **Bước 2.3:** Cài đặt Docker Engine (v29.0.2)
- [x] **Bước 2.4:** Cài đặt Git (v2.34.1)
- [x] **Bước 3:** Clone code từ GitHub (branch: backend)
- [x] **Bước 4.1:** Tạo file `.env` với production config
- [x] **Bước 4.2:** Build Docker images
- [x] **Bước 4.3:** Start services với Docker Compose
- [x] **Fix:** SecurityConfig - thêm Swagger endpoints vào permitAll()
- [x] **Fix:** SwaggerConfig - thêm AWS server URL vào dropdown
- [x] **Fix:** Train AI models (categorization, anomaly, prediction)
- [x] **Test:** Tất cả APIs hoạt động bình thường

---

## 📝 Các Bước Tiếp Theo (Tùy Chọn)

### **Bước 5: Setup Domain & SSL**

1. **Mua domain** (GoDaddy, Namecheap, etc.)
2. **Cấu hình DNS Records:**
   ```
   A Record: api.finpal.com → 175.41.150.228
   A Record: ai.finpal.com → 175.41.150.228
   ```
3. **Cài Nginx Reverse Proxy:**
   ```bash
   sudo apt install -y nginx
   ```
4. **Setup Let's Encrypt SSL:**
   ```bash
   sudo apt install -y certbot python3-certbot-nginx
   sudo certbot --nginx -d api.finpal.com -d ai.finpal.com
   ```

Chi tiết xem **Bước 5** trong `HUONG_DAN_DEPLOY_AWS.md`

### **Bước 6: Monitoring & Backup**

1. **Setup cron job backup database** (hàng ngày 2AM)
2. **Configure Docker logging**
3. **Setup AWS CloudWatch** (monitoring)
4. **Fail2Ban** (bảo vệ SSH)

Chi tiết xem **Bước 6** trong `HUONG_DAN_DEPLOY_AWS.md`

---

## ⚠️ Troubleshooting

### **Container không start:**

```bash
docker compose logs <service_name>
docker compose restart <service_name>
```

### **Out of Memory:**

```bash
# Tạo swap file
sudo fallocate -l 4G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
```

### **Disk Full:**

```bash
# Clean Docker
docker system prune -a -f

# Clean old logs
sudo journalctl --vacuum-time=7d
```

### **Backend không kết nối MySQL:**

```bash
# Check MySQL healthy
docker compose ps

# Restart backend
docker compose restart backend

# Check logs
docker compose logs backend | grep -i error
```

### **AI Models không load:**

```bash
# Retrain models
docker exec -it finpal-backend-ai bash
python scripts/train_models.py
exit
docker compose restart backend-ai
```

---

## 💰 Chi Phí Ước Tính (AWS)

| Dịch vụ               | Chi phí/tháng  |
| --------------------- | -------------- |
| EC2 t2.medium (24/7)  | ~$35           |
| Elastic IP (attached) | $0 (miễn phí)  |
| EBS Storage (20GB)    | ~$2            |
| Data Transfer (100GB) | ~$9            |
| **Tổng cộng**         | **~$46/tháng** |

**Lưu ý:**

- Free Tier: 750h t2.micro miễn phí (12 tháng đầu)
- Reserved Instances: Giảm 30-70% chi phí
- AWS Lightsail: $10-40/tháng (đơn giản hơn)

---

## 📞 Liên Hệ & Hỗ Trợ

**Repository:** https://github.com/mchien1005/Finpal-MobileApp  
**Branch:** backend  
**Tài liệu:**

- `README.md` - Tổng quan dự án
- `HUONG_DAN_CHAY_BACKEND.md` - Hướng dẫn chạy local
- `HUONG_DAN_DEPLOY_AWS.md` - Hướng dẫn deploy AWS EC2
- `DEPLOY_SUCCESS.md` - File này

---

## ✨ Kết Luận

✅ **Backend Finpal đã được deploy thành công lên AWS EC2!**

**Các services đang hoạt động:**

- ✅ MySQL Database (8.0, UTF-8)
- ✅ Backend Java API (Spring Boot, JWT Auth, Swagger UI)
- ✅ Backend AI API (FastAPI, ML Models Trained)
- ✅ phpMyAdmin (Database Management)

**URLs công khai:**

- API: http://175.41.150.228:8080
- AI API: http://175.41.150.228:8000
- Swagger UI: http://175.41.150.228:8080/swagger-ui.html
- AI Docs: http://175.41.150.228:8000/docs
- phpMyAdmin: http://175.41.150.228:8081

**Bước tiếp theo:**

- Setup domain & SSL (HTTPS)
- Cấu hình backup tự động
- Monitoring & alerting

---

**🎊 Chúc mừng! Deploy thành công! 🚀**

_Cập nhật lần cuối: 22/11/2025_
