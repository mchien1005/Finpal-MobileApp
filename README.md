# 💰 Finpal - Hệ Thống Quản Lý Tài Chính Cá Nhân Thông Minh

> **Finpal** là giải pháp All-in-One giúp người dùng cá nhân theo dõi thu chi, lập ngân sách và đạt được mục tiêu tài chính thông qua sự hỗ trợ của Trí tuệ nhân tạo (AI).

---

## 📑 Mục lục

1. [Giới thiệu](#-giới-thiệu)
2. [Tính năng Nổi bật](#-tính-năng-nổi-bật)
3. [Kiến trúc Hệ thống](#-kiến-trúc-hệ-thống)
4. [Công nghệ Sử dụng](#-công-nghệ-sử-dụng)
5. [Cài đặt & Triển khai](#-cài-đặt--triển-khai)
6. [Tài liệu API](#-tài-liệu-api)
7. [Cấu hình Chi tiết](#-cấu-hình-chi-tiết)
8. [Cơ sở Dữ liệu](#-cơ-sở-dữ-liệu)

---

## 🌟 Giới thiệu

Finpal giải quyết bài toán "lười" ghi chép chi tiêu bằng cách tự động hóa tối đa quy trình nhập liệu. Hệ thống tự động đọc tin nhắn SMS biến động số dư ngân hàng, dùng AI để hiểu nội dung và phân loại vào đúng danh mục (Ăn uống, Di chuyển, Mua sắm...). Ngoài ra, ứng dụng cung cấp các báo cáo trực quan và cảnh báo thông minh giúp người dùng kiểm soát ví tiền hiệu quả.

---

## 🚀 Tính năng Nổi bật

### 📱 Mobile App (Dành cho Người dùng)

- **Quản lý Thu Chi:** Thêm, sửa, xóa giao dịch thủ công với giao diện tối ưu.
- **SMS Automation:** Tự động quét và xử lý SMS từ ngân hàng (Vietcombank, Techcombank, VPBank...) để tạo giao dịch.
- **Smart Budget:** Thiết lập ngân sách (theo tháng/tuần/danh mục) và cảnh báo khi sắp vượt hạn mức.
- **Savings Goals:** Tạo mục tiêu tiết kiệm (Mua nhà, Du lịch), theo dõi tiến độ visualize đẹp mắt.
- **AI Insights:**
  - Phân tích thói quen chi tiêu.
  - Dự báo chi tiêu tháng tới.
  - Gợi ý cắt giảm chi phí không cần thiết.
- **Báo cáo:** Biểu đồ tròn, biểu đồ cột, so sánh thu chi theo kỳ.

### 🖥️ Web Admin (Dành cho Quản trị viên)

- **Dashboard Tổng quan:** Theo dõi số lượng users, tổng volume giao dịch, health check hệ thống.
- **User Management:** Xem chi tiết hồ sơ người dùng, lịch sử đăng nhập, khóa/mở khóa tài khoản.
- **System Health:** Giám sát realtime CPU, RAM, Database Connections, AI Service status.
- **Transaction Analytics:** Thống kê xu hướng tiêu dùng toàn hệ thống để đưa ra chiến lược phát triển.

### 🧠 Backend AI & Core

- **NLP Categorization:** Mô hình học máy phân loại giao dịch chính xác >94%.
- **Fraud Detection:** Phát hiện giao dịch bất thường (số tiền lớn đột biến, địa điểm lạ).
- **Data Security:** Mã hóa mật khẩu (BCrypt), bảo mật JWT Token, HTTPS.

---

## 🏗️ Kiến trúc Hệ thống

Dự án được xây dựng theo mô hình Microservices-lite kết hợp Monolithic module hóa:

1.  **Backend Core (Java):** Trung tâm xử lý, giao tiếp với Mobile App và Database.
2.  **Backend AI (Python):** Service độc lập xử lý các tác vụ nặng về tính toán và ML.
3.  **Database (MySQL):** Lưu trữ dữ liệu tập trung.
4.  **Interface:** Mobile App (Flutter) và Web Admin (React).

---

## 🛠️ Công nghệ Sử dụng

### 1. Backend Core

- **Language:** Java 17
- **Framework:** Spring Boot 3.2
- **Database Access:** Spring Data JPA / Hibernate
- **Security:** Spring Security, JWT (Json Web Token)
- **Tools:** Maven, Lombok, Swagger UI (OpenAPI 3)

### 2. Backend AI Service

- **Language:** Python 3.10
- **Framework:** FastAPI
- **ML Libraries:** Scikit-learn (Classification), XGBoost, Prophet (Time-series)
- **Data Processing:** Pandas, NumPy
- **Database Driver:** SQLAlchemy

### 3. Mobile App

- **Framework:** Flutter (Dart 3)
- **State Management:** GetX
- **Key Packages:** `fl_chart` (Biểu đồ), `device_info_plus`, `flutter_local_notifications`, `firebase_messaging`.

### 4. Admin Web Dashboard

- **Framework:** React 18
- **Build Tool:** Vite
- **UI Library:** Ant Design 5.0
- **Charts:** Recharts
- **State/API:** Axios, React Hooks

### 5. Infrastructure

- **Containerization:** Docker, Docker Compose
- **Database:** MySQL 8.0
- **Server:** Ubuntu Linux (EC2)

## 📚 Tài liệu API

Hệ thống cung cấp tài liệu API tự động qua Swagger/OpenAPI:

- **Core API Docs:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

  - Cung cấp chi tiết các endpoints: Auth, Users, Transactions, Budgets...
  - Cho phép test API trực tiếp trên giao diện.

- **AI Service Docs:** [http://localhost:8000/docs](http://localhost:8000/docs)
  - Các endpoints xử lý NLP, Training model, Predict.

---

## ⚙️ Cấu hình Chi tiết

### File cấu hình

- **Backend:** `backend/src/main/resources/application.properties`
- **Docker:** `docker-compose.yml` (Ưu tiên dùng file này để override biến môi trường)

### Các biến môi trường quan trọng (Environment Variables)

| Biến                    | Ý nghĩa                       | Giá trị Mặc định                       |
| :---------------------- | :---------------------------- | :------------------------------------- |
| `JWT_EXPIRATION`        | Thời gian sống của Token (ms) | `604800000` (7 ngày)                   |
| `JWT_SECRET`            | Khóa bí mật ký Token          | (Chuỗi hash dài)                       |
| `SPRING_DATASOURCE_URL` | URL kết nối Database          | `jdbc:mysql://mysql:3306/finpal_db...` |
| `AI_SERVICE_URL`        | URL kết nối tới AI Service    | `http://backend-ai:8000`               |
| `FCM_ENABLED`           | Bật/Tắt Push Notification     | `true`                                 |
