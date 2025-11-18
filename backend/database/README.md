# Hướng dẫn Setup Database cho FinPal

## 📋 Yêu cầu

- MySQL Server (XAMPP, WAMP hoặc MySQL standalone)
- phpMyAdmin (tùy chọn, để quản lý database dễ dàng)

## 🚀 Các bước cài đặt

### Bước 1: Tạo Database và Schema

#### Cách 1: Sử dụng phpMyAdmin

1. Mở phpMyAdmin: `http://localhost/phpmyadmin`
2. Chọn tab **SQL**
3. Copy toàn bộ nội dung file `database/finpal_schema.sql`
4. Paste vào và click **Go** để thực thi

#### Cách 2: Sử dụng MySQL Command Line

```bash
mysql -u root -p < database/finpal_schema.sql
```

### Bước 2: Insert Dữ liệu mẫu

#### Cách 1: Sử dụng phpMyAdmin

1. Chọn database `finpal_db`
2. Chọn tab **SQL**
3. Copy toàn bộ nội dung file `database/seed_data.sql`
4. Paste vào và click **Go**

#### Cách 2: Sử dụng MySQL Command Line

```bash
mysql -u root -p finpal_db < database/seed_data.sql
```

### Bước 3: Kiểm tra Database

Chạy các câu lệnh sau để kiểm tra:

```sql
USE finpal_db;

-- Kiểm tra các bảng đã tạo
SHOW TABLES;

-- Kiểm tra categories
SELECT * FROM categories;

-- Kiểm tra users demo
SELECT * FROM users;

-- Kiểm tra dữ liệu demo
SELECT * FROM transactions WHERE user_id = 1;
SELECT * FROM accounts WHERE user_id = 1;
```

## 📊 Cấu trúc Database

### Các bảng chính:

1. **users** - Quản lý người dùng
2. **categories** - Danh mục thu/chi
3. **accounts** - Tài khoản ngân hàng
4. **transactions** - Giao dịch tài chính
5. **budgets** - Ngân sách
6. **savings_goals** - Mục tiêu tiết kiệm
7. **notifications** - Thông báo
8. **sms_parsers** - Mẫu phân tích SMS
9. **category_rules** - Luật phân loại tự động
10. **user_preferences** - Cài đặt người dùng
11. **spending_insights** - Phân tích chi tiêu
12. **audit_logs** - Nhật ký hệ thống

## 👤 Tài khoản Demo

### User thường:

- Username: `demo`
- Password: `123456`
- Email: `demo@finpal.com`

### Admin:

- Username: `admin`
- Password: `123456`
- Email: `admin@finpal.com`

## 🔧 Cấu hình trong application.properties

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/finpal_db
spring.datasource.username=root
spring.datasource.password=         # Để trống nếu dùng XAMPP
```

**Lưu ý:** Nếu MySQL của bạn có password, hãy cập nhật trong `application.properties`

## 📝 Dữ liệu mẫu có sẵn

Sau khi chạy seed data, hệ thống sẽ có:

- ✅ 4 categories thu nhập
- ✅ 10 categories chi tiêu chính
- ✅ 14 sub-categories
- ✅ 19 luật phân loại tự động (GRAB, SHOPEE, CGV...)
- ✅ 3 SMS parsers (Vietcombank, Techcombank, ACB)
- ✅ 2 users demo
- ✅ 3 tài khoản ngân hàng
- ✅ 6 giao dịch mẫu
- ✅ 2 ngân sách
- ✅ 2 mục tiêu tiết kiệm

## 🎯 Kiểm tra kết nối

Sau khi setup xong, chạy Spring Boot application và kiểm tra log:

```
Hibernate: select ... from users
```

Nếu thấy các câu query SQL trong console là đã kết nối thành công!

## 🔐 Bảo mật

- Password trong database đã được hash bằng BCrypt
- Số tài khoản ngân hàng sẽ được mã hóa AES-256 (implement sau)
- Nội dung SMS sẽ được mã hóa khi lưu trữ

## 📞 Hỗ trợ

Nếu gặp lỗi khi setup database, kiểm tra:

1. MySQL Server đã chạy chưa
2. Port 3306 có bị chiếm dụng không
3. User root có quyền tạo database không
4. Charset của database phải là utf8mb4
