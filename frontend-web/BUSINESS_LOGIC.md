# 📱 Ví Thông Minh (FinPal) - Logic Nghiệp vụ

> **Phiên bản:** 1.0 | **Cập nhật:** 03/12/2025

---

## 🎯 TÓM TẮT NHANH

### Ứng dụng làm gì?

**FinPal** = "Kế toán cá nhân" tự động, giúp bạn quản lý tiền **mà không cần nhập liệu**.

```
📱 SMS ngân hàng → 🤖 AI tự động ghi & phân loại → 📊 Báo cáo & Gợi ý tiết kiệm
```

### 3 Vấn đề giải quyết:

| #   | Vấn đề                        | Giải pháp FinPal                      |
| --- | ----------------------------- | ------------------------------------- |
| 1️⃣  | **Lười nhập liệu**            | Tự động đọc SMS, không cần nhập tay   |
| 2️⃣  | **Không biết tiền đi đâu**    | Dashboard trực quan, biểu đồ chi tiêu |
| 3️⃣  | **Không biết cách tiết kiệm** | AI gợi ý cắt giảm cụ thể từng khoản   |

---

## 📋 Mục lục

1. [Vấn đề & Giải pháp](#1-vấn-đề--giải-pháp)
2. [3 Module Chính](#2-ba-module-chính)
3. [Luồng Hoạt động](#3-luồng-hoạt-động)
4. [Quy tắc Nghiệp vụ](#4-quy-tắc-nghiệp-vụ)
5. [Yêu cầu Bảo mật](#5-yêu-cầu-bảo-mật)

---

## 1. Vấn đề & Giải pháp

### 😫 3 "Nỗi đau" của người dùng

**Pain 1: Lười nhập liệu**

```
Các app khác: Mua cà phê 30k → Mở app → Nhập số tiền → Chọn danh mục → Lưu
                              (Quá nhiều bước → Bỏ cuộc sau 3 ngày)

FinPal:       Mua cà phê 30k → SMS đến → ✅ Xong (tự động)
```

**Pain 2: "Cú sốc cuối tháng"**

```
Đầu tháng: Lương 15 triệu 💰
           ↓ (chi tiêu không kiểm soát)
Cuối tháng: Còn 500k 😱 "Tiền đi đâu hết rồi?!"
```

**Pain 3: Không biết tiết kiệm từ đâu**

```
"Mình muốn tiết kiệm nhưng... cắt cái gì bây giờ?" 🤷
```

### 💡 Giải pháp FinPal

```
┌────────────────────────────────────────────────────────┐
│                                                        │
│   📱 SMS ngân hàng    →    🤖 AI FinPal    →    📊    │
│   "GD -55,000đ GRAB"       Tự động ghi         Báo cáo│
│                            + Phân loại         + Gợi ý│
│                                                        │
└────────────────────────────────────────────────────────┘
```

---

## 2. Ba Module Chính

### 📦 Module 1: Smart Scan (Tự động ghi nhận)

**Làm gì:** Đọc SMS ngân hàng → Tự động tạo giao dịch

```
INPUT (SMS gốc):
"Biến động TK 001***: -55,000VND lúc 12/11 09:00. ND: GRAB"

OUTPUT (Giao dịch):
├── Số tiền: 55,000đ
├── Loại: Chi tiêu
├── Merchant: GRAB
├── Danh mục: 🚗 Di chuyển (AI tự gán)
└── Ngày: 12/11/2025 09:00
```

**AI Phân loại tự động:**

| Từ khóa                | Danh mục     | Độ chính xác |
| ---------------------- | ------------ | ------------ |
| GRAB, BE, UBER         | 🚗 Di chuyển | 95%          |
| GRAB FOOD, SHOPEE FOOD | 🍔 Ăn uống   | 90%          |
| SHOPEE, LAZADA         | 🛍️ Mua sắm   | 92%          |
| CGV, LOTTE CINEMA      | 🎬 Giải trí  | 98%          |
| EVN, ĐIỆN LỰC          | 📄 Hóa đơn   | 99%          |

---

### 📦 Module 2: Dashboard (Bảng điều khiển)

**Làm gì:** Hiển thị tình hình tài chính trực quan

```
┌─────────────────────────────────────────┐
│         THÁNG 11/2025                   │
├─────────────────────────────────────────┤
│  💰 Thu nhập:    15,000,000đ            │
│  💸 Chi tiêu:     8,000,000đ            │
│  ─────────────────────────              │
│  💵 Còn lại:      7,000,000đ (47%)      │
└─────────────────────────────────────────┘

        CHI TIÊU THEO DANH MỤC

   🍔 Ăn uống     ████████░░ 40%  3.2tr
   🚗 Di chuyển   ████░░░░░░ 20%  1.6tr
   🛍️ Mua sắm    ███░░░░░░░ 15%  1.2tr
   🎬 Giải trí   ██░░░░░░░░ 10%  0.8tr
   📦 Khác       ███░░░░░░░ 15%  1.2tr
```

---

### 📦 Module 3: AI Coach (Trợ lý gợi ý)

**Làm gì:** Chủ động gợi ý cách tiết kiệm

**Loại 1: Cảnh báo vượt hạn mức**

```
⚠️ Bạn đã chi 70% hạn mức "Ăn ngoài"
   (2,100,000đ / 3,000,000đ)
   Còn 10 ngày nữa hết tháng!
```

**Loại 2: Gợi ý tiết kiệm cụ thể**

```
💡 Bạn chi 200,000đ/tuần cho TRÀ SỮA
   → Nếu giảm còn 100,000đ/tuần
   → Tiết kiệm 400,000đ/tháng
   → = 4,800,000đ/năm (đủ mua AirPods!)
```

**Loại 3: Phát hiện bất thường**

```
🔔 Hóa đơn điện tháng này: 500,000đ
   Cao hơn 30% so với trung bình (350,000đ)
   → Kiểm tra thiết bị điện?
```

**Loại 4: Hũ tiết kiệm**

```
🎧 Mục tiêu: Tai nghe Sony 3,000,000đ
   ████████░░░░ 33% (1,000,000đ)
   Còn 60 ngày → Cần tiết kiệm 33,333đ/ngày
```

---

## 3. Luồng Hoạt động

### 🔄 Luồng 1: Tự động ghi giao dịch

```
Bạn quẹt thẻ mua cà phê
        ↓
Ngân hàng gửi SMS: "GD -45,000đ HIGHLANDS"
        ↓
FinPal nhận SMS → Parse nội dung
        ↓
AI phân loại: "HIGHLANDS" → 🍔 Ăn uống (88%)
        ↓
Lưu giao dịch + Cập nhật Dashboard
        ↓
📱 Thông báo: "Đã ghi: 45,000đ - Ăn uống"
```

### 🔄 Luồng 2: Cảnh báo vượt hạn mức

```
Bạn đặt hạn mức "Ăn ngoài": 3,000,000đ/tháng
        ↓
Mỗi giao dịch → Cộng dồn
        ↓
Khi đạt 70% (2,100,000đ)
        ↓
📱 Thông báo cảnh báo!
```

### 🔄 Luồng 3: Tạo mục tiêu tiết kiệm

```
Tạo mục tiêu: "Mua tai nghe 3,000,000đ - Deadline 01/03"
        ↓
Hệ thống tính: 90 ngày → Cần 33,333đ/ngày
        ↓
Mỗi tuần nhắc: "Đã tiết kiệm được X%"
        ↓
Đóng góp tiền vào → Cập nhật tiến độ
```

---

## 4. Quy tắc Nghiệp vụ

### 📌 Quy tắc phân loại AI

```
IF merchant chứa "GRAB" AND amount < 100,000
   → Category = "Di chuyển"

IF merchant chứa "GRAB FOOD" OR "SHOPEEFOOD"
   → Category = "Ăn uống"

IF merchant chứa "SHOPEE" OR "LAZADA"
   → Category = "Mua sắm"

IF merchant không khớp pattern nào
   → Category = "Khác" (cần user xác nhận)
```

### 📌 Quy tắc cảnh báo

```
Ngưỡng cảnh báo = 70% hạn mức (mặc định)

IF chi_tiêu >= 70% hạn_mức
   → Gửi cảnh báo MEDIUM

IF chi_tiêu >= 100% hạn_mức
   → Gửi cảnh báo HIGH
```

### 📌 Quy tắc phát hiện bất thường

```
Trung bình 3 tháng = AVG(amount of last 3 months)

IF giao_dịch > trung_bình * 1.3
   → Đánh dấu "Bất thường" + Gửi thông báo
```

### 📌 Quy tắc gợi ý tiết kiệm

```
Với mỗi danh mục có thể cắt giảm (Trà sữa, Giải trí, Mua sắm):

weekly_avg = Tổng chi 4 tuần / 4
suggested_target = weekly_avg * 50%  (giảm 50%)
monthly_savings = (weekly_avg - suggested_target) * 4

→ Gợi ý: "Nếu giảm X còn Y, tiết kiệm Z/tháng"
```

---

## 5. Yêu cầu Bảo mật

### 🔒 Cam kết với người dùng

```
┌────────────────────────────────────────────────────────┐
│                   🔒 FINPAL CAM KẾT                    │
├────────────────────────────────────────────────────────┤
│                                                        │
│  ❌ KHÔNG BAO GIỜ:                                     │
│     • Yêu cầu mật khẩu ngân hàng                       │
│     • Yêu cầu mã OTP                                   │
│     • Truy cập tài khoản ngân hàng                     │
│                                                        │
│  ✅ CHỈ LÀM:                                           │
│     • Đọc NỘI DUNG SMS (với sự cho phép của bạn)       │
│     • Xử lý trên thiết bị CỦA BẠN                      │
│     • Mã hóa dữ liệu khi lưu trữ                       │
│                                                        │
└────────────────────────────────────────────────────────┘
```

### ⚠️ Giới hạn iOS

```
Android: ✅ Đọc SMS tự động
iOS:     ❌ Apple không cho phép đọc SMS
         → Giải pháp: Nhập tay (nhưng vẫn có AI phân loại)
```

---

## 📊 Tóm tắt Kiến trúc

```
┌─────────────────────────────────────────────────────────────┐
│                     FINPAL ARCHITECTURE                      │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  📱 MOBILE APP                                              │
│  ├── Đọc SMS (Android)                                      │
│  ├── Hiển thị Dashboard                                     │
│  └── Nhận thông báo                                         │
│                                                             │
│  ☕ JAVA BACKEND (Spring Boot)                              │
│  ├── Quản lý User, Account                                  │
│  ├── CRUD Transactions                                      │
│  ├── Budget & Savings Goals                                 │
│  └── Notifications                                          │
│                                                             │
│  🐍 AI BACKEND (Python FastAPI)                             │
│  ├── Phân loại giao dịch (ML)                               │
│  ├── Phát hiện bất thường                                   │
│  ├── Dự đoán chi tiêu                                       │
│  └── Gợi ý tiết kiệm                                        │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 🎯 KPI Mục tiêu

| Chỉ số                    | Mục tiêu |
| ------------------------- | -------- |
| Độ chính xác phân loại AI | > 90%    |
| Độ chính xác bóc tách SMS | > 95%    |
| Thời gian xử lý SMS       | < 2 giây |
| User giữ lại sau 1 tuần   | > 60%    |

---

_📱 FinPal - "Quản lý tài chính không cần nỗ lực"_
