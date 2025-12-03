# 📱 Ví Thông Minh (FinPal) - Tài liệu Logic Nghiệp vụ

> **Phiên bản:** 1.0  
> **Cập nhật lần cuối:** 03/12/2025  
> **Dự án:** Finpal - Smart Wallet Application

---

## 📋 Mục lục

1. [Tổng quan Dự án](#1-tổng-quan-dự-án)
2. [Bối cảnh & Vấn đề Kinh doanh](#2-bối-cảnh--vấn-đề-kinh-doanh)
3. [Đối tượng Người dùng](#3-đối-tượng-người-dùng)
4. [Yêu cầu Chức năng (FRs)](#4-yêu-cầu-chức-năng-functional-requirements)
5. [Yêu cầu Phi chức năng (NFRs)](#5-yêu-cầu-phi-chức-năng-non-functional-requirements)
6. [Ràng buộc & Giả định](#6-ràng-buộc--giả-định)
7. [Luồng Nghiệp vụ Chính](#7-luồng-nghiệp-vụ-chính)
8. [Mô hình Dữ liệu Nghiệp vụ](#8-mô-hình-dữ-liệu-nghiệp-vụ)
9. [Quy tắc Nghiệp vụ](#9-quy-tắc-nghiệp-vụ)

---

## 1. Tổng quan Dự án

### 1.1 Giới thiệu

**"Ví Thông Minh" (FinPal)** là một ứng dụng di động được xây dựng để giải quyết vấn đề cốt lõi của việc quản lý tài chính cá nhân: **Sự lười biếng**.

> 💡 Hầu hết mọi người đều thất bại trong việc theo dõi chi tiêu vì họ ghét phải nhập liệu thủ công mỗi khi uống một cốc cà phê hay trả tiền xăng.

### 1.2 Tầm nhìn Sản phẩm

Dự án này là một **"trợ lý" tài chính thông minh**, có khả năng:

| Tính năng               | Mô tả                                                              |
| ----------------------- | ------------------------------------------------------------------ |
| 🔄 **Tự động ghi nhận** | Phân tích tin nhắn SMS hoặc thông báo biến động số dư từ ngân hàng |
| 🤖 **AI Phân loại**     | Sử dụng AI để tự động phân loại chi tiêu vào các danh mục          |
| 📊 **Trực quan hóa**    | Cung cấp biểu đồ trực quan về tình hình tài chính                  |
| 💡 **Gợi ý chủ động**   | Đưa ra các gợi ý thông minh để giúp người dùng tiết kiệm tiền      |

### 1.3 Giá trị Cốt lõi

```
┌─────────────────────────────────────────────────────────────┐
│                    GIÁ TRỊ CỐT LÕI                          │
├─────────────────────────────────────────────────────────────┤
│  🎯 Zero-Effort: Không cần nỗ lực nhập liệu                 │
│  🔒 Bảo mật: Dữ liệu tài chính được bảo vệ tuyệt đối        │
│  🧠 Thông minh: AI hiểu và gợi ý như một kế toán cá nhân    │
│  📈 Actionable: Gợi ý cụ thể, có thể hành động ngay         │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. Bối cảnh & Vấn đề Kinh doanh

### 2.1 Hiện trạng (Current State)

Người trẻ hiện nay chủ yếu chi tiêu qua ngân hàng:

- ✅ Quẹt thẻ
- ✅ Chuyển khoản
- ✅ Quét QR

**Mỗi giao dịch** đều được ngân hàng gửi thông báo (qua SMS hoặc push notification).

**Tuy nhiên**, các thông báo này:

- ❌ Rời rạc
- ❌ Không cung cấp bức tranh tổng thể
- ❌ Không có phân tích hay gợi ý

### 2.2 Nỗi đau Người dùng (Pain Points)

#### 😫 Pain Point 1: "Hội chứng Lười Nhập liệu"

```
┌──────────────────────────────────────────────────────────────┐
│  📱 Các app hiện tại (Money Lover, Misa...)                  │
│  ├── Yêu cầu: Người dùng tự nhập TAY mọi khoản chi           │
│  └── Kết quả: Hầu hết từ bỏ sau VÀI NGÀY                     │
│                                                              │
│  ➜ Nguyên nhân: Quá nhiều effort cho mỗi giao dịch nhỏ       │
└──────────────────────────────────────────────────────────────┘
```

#### 😱 Pain Point 2: "Cú sốc Cuối tháng"

```
Đầu tháng                                      Cuối tháng
    │                                              │
    ▼                                              ▼
┌────────┐     Chi tiêu không kiểm soát      ┌────────────┐
│Lương   │ ──────────────────────────────▶   │ TK cạn kiệt│
│15 triệu│     (không biết đi đâu)           │ ???        │
└────────┘                                   └────────────┘
```

- Người dùng hoàn toàn "mù mờ" về dòng tiền
- Biết mình đã tiêu tiền, nhưng **không biết bao nhiêu** và **cho cái gì**
- Đến cuối tháng: **SỐC** khi thấy tài khoản cạn kiệt

#### 🤷 Pain Point 3: "Không biết Tiết kiệm"

| Vấn đề                    | Hệ quả                                    |
| ------------------------- | ----------------------------------------- |
| Không biết lãng phí ở đâu | Không đặt được mục tiêu tiết kiệm thực tế |
| Không có dữ liệu          | Không biết nên cắt giảm khoản nào         |
| Không có gợi ý            | Không có động lực thay đổi hành vi        |

### 2.3 Cơ hội (Opportunity)

```
┌─────────────────────────────────────────────────────────────────┐
│                        💡 GIẢI PHÁP                             │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   SMS/Thông báo      ──▶    AI Phân tích    ──▶    Insights    │
│   (Dữ liệu có sẵn)         (Tự động hóa)         (Gợi ý)       │
│                                                                 │
│   "Biến AI thành một KẾ TOÁN CÁ NHÂN"                          │
│   Giúp người dùng hiểu sức khỏe tài chính MÀ KHÔNG CẦN NỖ LỰC  │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 3. Đối tượng Người dùng

### 3.1 Chân dung Người dùng (Persona)

#### 👤 "Người trẻ Muốn tiết kiệm nhưng Lười"

```
┌─────────────────────────────────────────────────────────────┐
│  👤 PERSONA: Minh - 24 tuổi - Nhân viên văn phòng           │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  📝 MÔ TẢ:                                                  │
│  • Sinh viên mới ra trường / Người mới đi làm               │
│  • Nhận lương qua tài khoản ngân hàng                       │
│  • Chi tiêu chủ yếu qua chuyển khoản, quét QR               │
│                                                             │
│  💰 TÀI CHÍNH:                                              │
│  • Thu nhập: 10-20 triệu/tháng (trung bình)                 │
│  • Dòng tiền: Ra vào thường xuyên                           │
│  • Khoản chi: Nhỏ lẻ (ăn uống, đi lại, mua sắm online)      │
│                                                             │
│  🎯 NHU CẦU:                                                │
│  ✓ Muốn biết tiền "biến mất" đi đâu                         │
│  ✓ Muốn tiết kiệm nhưng cần "cú hích" hoặc lời khuyên       │
│  ✓ QUAN TRỌNG: Không muốn tốn thời gian nhập liệu           │
│                                                             │
│  😤 FRUSTRATIONS:                                           │
│  ✗ Ghét nhập liệu thủ công                                  │
│  ✗ Không có thời gian theo dõi chi tiêu                     │
│  ✗ Các app hiện tại quá phức tạp                            │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 3.2 Hành trình Người dùng (User Journey)

```
TRƯỚC KHI DÙNG FINPAL:
──────────────────────

Nhận SMS        Đọc lướt       Quên ngay       Cuối tháng
"GD -55.000đ"   "À, ok"        (Không ghi)     "Tiền đâu rồi?!"
     │              │               │               │
     ▼              ▼               ▼               ▼
   [📱]  ──▶    [👀]    ──▶     [🗑️]    ──▶    [😱]


SAU KHI DÙNG FINPAL:
────────────────────

Nhận SMS        FinPal tự      Phân loại       Gợi ý tiết kiệm
"GD -55.000đ"   động ghi       "Di chuyển"     "Giảm 30% được 200k"
     │              │               │               │
     ▼              ▼               ▼               ▼
   [📱]  ──▶    [🤖]    ──▶     [📊]    ──▶    [💡]
```

---

## 4. Yêu cầu Chức năng (Functional Requirements)

### 4.1 FR1: Module "Tự động Ghi nhận Giao dịch" (Smart Scan)

#### FR1.1: Quét Tin nhắn SMS ⭐ Core Feature

| Thuộc tính           | Chi tiết                                                        |
| -------------------- | --------------------------------------------------------------- |
| **Mô tả**            | Ứng dụng đọc tin nhắn SMS từ các ngân hàng                      |
| **Platform**         | Android (yêu cầu cấp quyền READ_SMS)                            |
| **Ngân hàng hỗ trợ** | Vietcombank, Techcombank, ACB, BIDV, MB Bank, VPBank, TPBank... |
| **Trigger**          | Realtime khi có SMS mới hoặc Scan lịch sử                       |

#### FR1.2: Bóc tách & Phân tích (AI/Regex)

**Input (SMS gốc):**

```
Biến động số dư TK 001...: -55,000VND lúc 12/11/2025 09:00. ND: GRAB...
```

**Output (JSON đã bóc tách):**

```json
{
  "amount": 55000,
  "type": "EXPENSE",
  "bank": "VCB",
  "merchant": "GRAB",
  "transactionDate": "2025-11-12T09:00:00",
  "accountNumber": "001***"
}
```

**Quy tắc bóc tách:**

| Field             | Quy tắc                           | Ví dụ               |
| ----------------- | --------------------------------- | ------------------- |
| `amount`          | Regex: `[\-\+]?\d{1,3}(,\d{3})*`  | `-55,000` → `55000` |
| `type`            | `-` = EXPENSE, `+` = INCOME       | `-55,000` → EXPENSE |
| `bank`            | Map từ sender hoặc content        | `VCB`, `TCB`, `ACB` |
| `merchant`        | Substring sau `ND:` hoặc tương tự | `GRAB`, `SHOPEE`    |
| `transactionDate` | Parse từ pattern ngày giờ         | `12/11/2025 09:00`  |

#### FR1.3: Tự động Phân loại bằng AI ⭐ Intelligence Feature

**Mapping Merchant → Category:**

```
┌──────────────────────────────────────────────────────────────┐
│                    AI CATEGORIZATION                          │
├────────────────────┬─────────────────┬───────────────────────┤
│     Merchant       │    Category     │     Confidence        │
├────────────────────┼─────────────────┼───────────────────────┤
│ GRAB               │ Di chuyển       │ 95%                   │
│ GRAB FOOD          │ Ăn uống         │ 90% (context-aware)   │
│ SHOPEE             │ Mua sắm         │ 92%                   │
│ LAZADA             │ Mua sắm         │ 92%                   │
│ CGV CINEMA         │ Giải trí        │ 98%                   │
│ THE COFFEE HOUSE   │ Ăn uống         │ 88%                   │
│ HIGHLANDS          │ Ăn uống         │ 88%                   │
│ EVN ĐIỆN LỰC       │ Hóa đơn         │ 99%                   │
│ VIETTEL            │ Hóa đơn         │ 95%                   │
│ Unknown            │ Khác            │ 50% (cần xác nhận)    │
└────────────────────┴─────────────────┴───────────────────────┘
```

**Thách thức đặc biệt:** Phân biệt `GRAB` (Di chuyển) vs `GRAB FOOD` (Ăn uống)

- Giải pháp: Phân tích context (giờ, số tiền, pattern)

#### FR1.4: Ghi nhận Thủ công

Cho phép người dùng nhập tay các khoản chi tiêu **tiền mặt**:

```
┌─────────────────────────────────────────┐
│  ➕ Thêm giao dịch thủ công             │
├─────────────────────────────────────────┤
│  Số tiền:    [25,000 VND        ]       │
│  Nội dung:   [Tiền gửi xe       ]       │
│  Danh mục:   [Di chuyển      ▼  ]       │
│  Ngày:       [03/12/2025        ]       │
│                                         │
│         [  Thêm giao dịch  ]            │
└─────────────────────────────────────────┘
```

---

### 4.2 FR2: Module "Bảng điều khiển Trực quan" (Dashboard)

#### FR2.1: Tổng quan Dòng tiền

```
┌─────────────────────────────────────────────────────────────┐
│                  📊 TỔNG QUAN THÁNG 11/2025                 │
├───────────────────┬───────────────────┬─────────────────────┤
│   💰 THU NHẬP     │   💸 CHI TIÊU     │   💵 CÒN LẠI       │
│   15,000,000đ     │   8,000,000đ      │   7,000,000đ       │
│   ▲ +5% vs T10    │   ▼ -12% vs T10   │   ▲ +18% vs T10    │
└───────────────────┴───────────────────┴─────────────────────┘
                              │
                              ▼
                    ┌─────────────────┐
                    │  Tỷ lệ tiết kiệm │
                    │      46.7%       │
                    │   ████████░░░    │
                    └─────────────────┘
```

**Công thức:**

```
Còn lại = Thu nhập - Chi tiêu
Tỷ lệ tiết kiệm = (Còn lại / Thu nhập) × 100%
```

#### FR2.2: Biểu đồ Phân loại (Pie Chart)

```
              CHI TIÊU THEO DANH MỤC

                    ┌───┐
               ╱────│40%│────╲    🍔 Ăn uống: 3,200,000đ
              │     └───┘     │
         ┌───┐│               │┌───┐
         │20%│▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓│15%│  🚗 Di chuyển: 1,600,000đ
         └───┘│               │└───┘
              │     ┌───┐     │   🛍️ Mua sắm: 1,200,000đ
               ╲────│10%│────╱
                    └───┘        🎬 Giải trí: 800,000đ
                    ┌───┐
                    │15%│        📦 Khác: 1,200,000đ
                    └───┘
```

#### FR2.3: Lịch sử Giao dịch

```
┌─────────────────────────────────────────────────────────────┐
│  📋 LỊCH SỬ GIAO DỊCH                      [Lọc ▼] [🔍]    │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  📅 Hôm nay - 03/12/2025                                    │
│  ├── 🚗 GRAB VIETNAM           -55,000đ    09:30  [Tự động] │
│  ├── ☕ HIGHLANDS COFFEE        -45,000đ    10:15  [Tự động] │
│  └── 🛍️ SHOPEE                 -250,000đ   14:20  [Tự động] │
│                                                             │
│  📅 Hôm qua - 02/12/2025                                    │
│  ├── 🍔 GRAB FOOD               -89,000đ   12:30  [Tự động] │
│  ├── 🅿️ Tiền gửi xe             -25,000đ   08:00  [Thủ công]│
│  └── 💰 LƯƠNG THÁNG 12      +15,000,000đ   00:05  [Tự động] │
│                                                             │
│  [Xem thêm...]                                              │
└─────────────────────────────────────────────────────────────┘

📝 Cho phép: Sửa danh mục nếu AI phân loại sai
```

---

### 4.3 FR3: Module "Trợ lý AI Gợi ý" (AI Financial Coach)

#### FR3.1: Gửi Thông báo Chủ động

**Loại 1: Cảnh báo vượt hạn mức**

```
┌─────────────────────────────────────────────────────────────┐
│  ⚠️ CẢNH BÁO HẠN MỨC                                        │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Bạn đã chi 70% hạn mức "Ăn ngoài" của tháng này.          │
│                                                             │
│  ████████████████████░░░░░░░░░░  70%                       │
│  2,100,000đ / 3,000,000đ                                    │
│                                                             │
│  ⏰ Chỉ còn 10 ngày nữa là hết tháng.                       │
│  💡 Gợi ý: Giảm 50% chi tiêu "Ăn ngoài" trong 10 ngày tới  │
│                                                             │
│              [Xem chi tiết]  [Bỏ qua]                       │
└─────────────────────────────────────────────────────────────┘
```

**Loại 2: Gợi ý tiết kiệm**

```
┌─────────────────────────────────────────────────────────────┐
│  💡 GỢI Ý TIẾT KIỆM TỪ FINPAL                               │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  🧋 FinPal nhận thấy bạn chi trung bình:                    │
│     200,000đ/tuần cho "Trà sữa"                             │
│                                                             │
│  📉 Nếu bạn giảm còn 100,000đ/tuần:                         │
│     → Tiết kiệm được 400,000đ/tháng                         │
│     → Tiết kiệm được 4,800,000đ/năm                         │
│                                                             │
│  🎯 Đủ để: Mua AirPods Pro sau 5 tháng!                     │
│                                                             │
│        [Tạo mục tiêu tiết kiệm]  [Để sau]                   │
└─────────────────────────────────────────────────────────────┘
```

#### FR3.2: Phát hiện Bất thường (Anomaly Detection)

```
┌─────────────────────────────────────────────────────────────┐
│  🔔 PHÁT HIỆN BẤT THƯỜNG                                    │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ⚡ Hóa đơn tiền điện tháng này: 500,000đ                   │
│                                                             │
│        Trung bình 3 tháng gần đây                           │
│              350,000đ                                        │
│                 │                                           │
│     ────────────┼────────────────────                       │
│                 │            ▲                              │
│                 │         +30%                              │
│                 │            │                              │
│     ────────────┴────────────┴────                          │
│        TB 350k        Tháng này 500k                        │
│                                                             │
│  ❓ Nguyên nhân có thể:                                     │
│     • Sử dụng điều hòa nhiều hơn                            │
│     • Thêm thiết bị điện mới                                │
│     • Rò rỉ điện?                                           │
│                                                             │
│           [Xem gợi ý tiết kiệm điện]                        │
└─────────────────────────────────────────────────────────────┘
```

**Công thức phát hiện bất thường:**

```
Anomaly Score = (Current - Average) / StandardDeviation

Nếu Anomaly Score > 2.0 → Cảnh báo bất thường
Nếu Anomaly Score > 3.0 → Cảnh báo nghiêm trọng
```

#### FR3.3: Tạo "Hũ Tiết kiệm" (Savings Goals)

```
┌─────────────────────────────────────────────────────────────┐
│  🏦 HŨ TIẾT KIỆM CỦA BẠN                                    │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  🎧 Mua tai nghe Sony WH-1000XM5                            │
│     ████████░░░░░░░░░░░░░░  33%                            │
│     1,000,000đ / 3,000,000đ                                 │
│     ⏰ Còn 60 ngày │ 💡 Cần: 33,333đ/ngày                   │
│                                                             │
│  ✈️ Du lịch Đà Lạt                                          │
│     ████████████░░░░░░░░░░  50%                            │
│     2,500,000đ / 5,000,000đ                                 │
│     ⏰ Còn 45 ngày │ 💡 Cần: 55,555đ/ngày                   │
│                                                             │
│  📱 Mua iPhone 16                                           │
│     ██░░░░░░░░░░░░░░░░░░░░  10%                            │
│     2,500,000đ / 25,000,000đ                                │
│     ⏰ Còn 365 ngày │ 💡 Cần: 61,644đ/ngày                  │
│                                                             │
│              [+ Tạo mục tiêu mới]                           │
└─────────────────────────────────────────────────────────────┘
```

**Công thức gợi ý:**

```
Số tiền cần/ngày = (Mục tiêu - Đã tích lũy) / Số ngày còn lại
Số tiền cần/tuần = Số tiền cần/ngày × 7
Số tiền cần/tháng = Số tiền cần/ngày × 30
```

---

## 5. Yêu cầu Phi chức năng (Non-Functional Requirements)

### NFR1: Bảo mật & Quyền riêng tư ⭐ Quan trọng Tuyệt đối

```
┌─────────────────────────────────────────────────────────────┐
│                    🔒 CAM KẾT BẢO MẬT                        │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ✅ CHÚNG TÔI KHÔNG BAO GIỜ:                                │
│     • Yêu cầu mật khẩu ngân hàng                            │
│     • Yêu cầu mã OTP                                        │
│     • Truy cập tài khoản ngân hàng của bạn                  │
│                                                             │
│  ✅ CHÚNG TÔI CHỈ:                                          │
│     • Đọc NỘI DUNG tin nhắn SMS (với sự cho phép)           │
│     • Xử lý nội dung TRÊN THIẾT BỊ CỦA BẠN                  │
│     • Mã hóa toàn bộ dữ liệu giao dịch (AES-256)            │
│                                                             │
│  ✅ DỮ LIỆU CỦA BẠN:                                        │
│     • Được mã hóa trong CSDL                                │
│     • Không bao giờ được bán cho bên thứ 3                  │
│     • Có thể xóa bất cứ lúc nào                             │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### NFR2: Tính Chính xác (Accuracy)

| Chức năng            | Mục tiêu | Đo lường                          |
| -------------------- | -------- | --------------------------------- |
| Bóc tách SMS (FR1.2) | > 95%    | Số SMS parse đúng / Tổng SMS      |
| Phân loại AI (FR1.3) | > 90%    | Số category đúng / Tổng giao dịch |
| Phát hiện bất thường | > 85%    | Precision & Recall                |

### NFR3: Khả năng Thích ứng (Adaptability)

```
Cấu trúc SMS mỗi ngân hàng KHÁC NHAU:

VCB: "Biến động số dư TK 001...: -55,000VND lúc 12/11/2025 09:00. ND: GRAB"
TCB: "GD: -55,000VND TK: *5678 luc 12/11 09:00. ND: GRAB VIETNAM"
ACB: "TK ACB: 1234567890 tru 55,000 VND luc 12/11/2025 09:00. ND: GRAB"

→ Hệ thống phải linh hoạt xử lý NHIỀU ĐỊNH DẠNG khác nhau
→ Sử dụng AI + Cấu hình parser cho từng ngân hàng
```

### NFR4: Trải nghiệm Người dùng (UX)

| Nguyên tắc                 | Mô tả                                        |
| -------------------------- | -------------------------------------------- |
| **Zero-Effort**            | Người dùng không cần làm gì, mọi thứ tự động |
| **Clean UI**               | Giao diện sạch, tối giản, dễ hiểu            |
| **Instant Feedback**       | Phản hồi ngay lập tức sau mỗi hành động      |
| **Progressive Disclosure** | Thông tin chi tiết chỉ hiện khi cần          |

---

## 6. Ràng buộc & Giả định

### 6.1 Ràng buộc (Constraints)

#### Ràng buộc 1: iOS không thể đọc SMS tự động ⚠️ Lớn nhất

```
┌─────────────────────────────────────────────────────────────┐
│                    📱 PLATFORM LIMITATION                    │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ANDROID:                                                   │
│  ✅ Có thể đọc SMS tự động (sau khi được cấp quyền)         │
│  ✅ Tính năng Smart Scan hoạt động đầy đủ                   │
│                                                             │
│  iOS:                                                       │
│  ❌ KHÔNG THỂ tự động đọc SMS (Apple không cho phép)        │
│                                                             │
│  GIẢI PHÁP THAY THẾ CHO iOS:                                │
│  ├── Option 1: Forward email sao kê ngân hàng               │
│  ├── Option 2: OCR ảnh chụp màn hình (ít khả thi)           │
│  └── Option 3: Nhập tay (vẫn được AI tự động phân loại)     │
│                                                             │
│  ➜ Chấp nhận: Smart Scan chỉ hoạt động trên Android         │
│               iOS phải nhập tay nhưng vẫn có AI phân loại   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 6.2 Giả định (Assumptions)

| #   | Giả định                                                                | Tác động nếu sai                 |
| --- | ----------------------------------------------------------------------- | -------------------------------- |
| 1   | Người dùng sử dụng mobile banking và nhận thông báo biến động số dư     | Giảm hiệu quả tính năng tự động  |
| 2   | Người dùng sẵn sàng cấp quyền đọc SMS sau khi tin tưởng cam kết bảo mật | Người dùng phải nhập tay         |
| 3   | Đa số giao dịch qua ngân hàng (không phải tiền mặt)                     | Bức tranh tài chính không đầy đủ |

---

## 7. Luồng Nghiệp vụ Chính

### 7.1 Luồng 1: Tự động Ghi nhận Giao dịch từ SMS

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    LUỒNG TỰ ĐỘNG GHI NHẬN GIAO DỊCH                     │
└─────────────────────────────────────────────────────────────────────────┘

     Người dùng           Ngân hàng              FinPal App           AI Service
         │                    │                      │                    │
         │  Chi tiêu (Quẹt thẻ/CK)                   │                    │
         │──────────────────►│                       │                    │
         │                    │                      │                    │
         │                    │  SMS: "-55,000đ..."  │                    │
         │                    │─────────────────────►│                    │
         │                    │                      │                    │
         │                    │                      │ Parse SMS          │
         │                    │                      │──────────┐         │
         │                    │                      │          │         │
         │                    │                      │◄─────────┘         │
         │                    │                      │                    │
         │                    │                      │ Predict Category   │
         │                    │                      │───────────────────►│
         │                    │                      │                    │
         │                    │                      │  {category, conf}  │
         │                    │                      │◄───────────────────│
         │                    │                      │                    │
         │                    │                      │ Save Transaction   │
         │                    │                      │──────────┐         │
         │                    │                      │          │         │
         │                    │                      │◄─────────┘         │
         │                    │                      │                    │
         │  Push Notification: "Đã ghi: GRAB 55k"    │                    │
         │◄──────────────────────────────────────────│                    │
         │                    │                      │                    │
```

### 7.2 Luồng 2: Xem Dashboard và Insights

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    LUỒNG XEM DASHBOARD & INSIGHTS                        │
└─────────────────────────────────────────────────────────────────────────┘

     Người dùng              FinPal App           Java Backend         AI Service
         │                      │                      │                    │
         │  Mở Dashboard        │                      │                    │
         │─────────────────────►│                      │                    │
         │                      │                      │                    │
         │                      │  GET /dashboard/summary                   │
         │                      │─────────────────────►│                    │
         │                      │                      │                    │
         │                      │  {income, expense, balance}               │
         │                      │◄─────────────────────│                    │
         │                      │                      │                    │
         │                      │  GET /dashboard/spending-by-category      │
         │                      │─────────────────────►│                    │
         │                      │                      │                    │
         │                      │  [{category, amount, %}]                  │
         │                      │◄─────────────────────│                    │
         │                      │                      │                    │
         │                      │  GET /insights/proactive                  │
         │                      │─────────────────────────────────────────►│
         │                      │                      │                    │
         │                      │  [{insight_type, message, action}]        │
         │                      │◄─────────────────────────────────────────│
         │                      │                      │                    │
         │  Hiển thị Dashboard + Insights              │                    │
         │◄─────────────────────│                      │                    │
         │                      │                      │                    │
```

### 7.3 Luồng 3: Tạo và Theo dõi Mục tiêu Tiết kiệm

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    LUỒNG TẠO MỤC TIÊU TIẾT KIỆM                         │
└─────────────────────────────────────────────────────────────────────────┘

     Người dùng              FinPal App           Java Backend
         │                      │                      │
         │  Tạo mục tiêu mới    │                      │
         │  "Tai nghe 3tr,      │                      │
         │   deadline 01/03"    │                      │
         │─────────────────────►│                      │
         │                      │                      │
         │                      │  POST /savings-goals │
         │                      │  {name, target, date}│
         │                      │─────────────────────►│
         │                      │                      │
         │                      │                      │ Tính toán:
         │                      │                      │ - Số ngày còn lại
         │                      │                      │ - Số tiền/ngày cần tiết kiệm
         │                      │                      │
         │                      │  {id, suggested_daily_amount}
         │                      │◄─────────────────────│
         │                      │                      │
         │  "Bạn cần tiết kiệm  │                      │
         │   33,333đ/ngày"      │                      │
         │◄─────────────────────│                      │
         │                      │                      │
         │                      │                      │
         │  [Mỗi tuần]          │                      │
         │  Đóng góp 250,000đ   │                      │
         │─────────────────────►│                      │
         │                      │                      │
         │                      │  POST /savings-goals/{id}/contributions
         │                      │─────────────────────►│
         │                      │                      │
         │                      │  {new_balance, progress_percentage}
         │                      │◄─────────────────────│
         │                      │                      │
         │  "Đã tích lũy 33%!   │                      │
         │   Còn 67 ngày nữa"   │                      │
         │◄─────────────────────│                      │
         │                      │                      │
```

---

## 8. Mô hình Dữ liệu Nghiệp vụ

### 8.1 Entity Relationship Diagram (ERD)

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         MÔ HÌNH DỮ LIỆU                                 │
└─────────────────────────────────────────────────────────────────────────┘

    ┌─────────────┐         ┌─────────────────┐         ┌─────────────┐
    │    USER     │         │   TRANSACTION   │         │  CATEGORY   │
    ├─────────────┤         ├─────────────────┤         ├─────────────┤
    │ id          │◄───────┤│ user_id (FK)    │         │ id          │
    │ email       │     1:N││ category_id (FK)│────────►│ name        │
    │ password    │         │ account_id (FK) │   N:1   │ icon        │
    │ name        │         │ amount          │         │ color       │
    │ created_at  │         │ type            │         │ is_system   │
    └─────────────┘         │ merchant        │         └─────────────┘
          │                 │ description     │
          │                 │ transaction_date│
          │                 │ is_auto         │
          │                 │ ai_confidence   │
          │                 │ created_at      │
          │                 └─────────────────┘
          │
          │ 1:N
          ▼
    ┌─────────────┐         ┌─────────────────┐
    │   ACCOUNT   │         │  SAVINGS_GOAL   │
    ├─────────────┤         ├─────────────────┤
    │ id          │         │ id              │
    │ user_id (FK)│◄───────┤│ user_id (FK)    │
    │ bank_name   │     1:N││ name            │
    │ account_num │         │ target_amount   │
    │ balance     │         │ current_amount  │
    └─────────────┘         │ target_date     │
                            │ status          │
          │                 └─────────────────┘
          │                         │
          │ 1:N                     │ 1:N
          ▼                         ▼
    ┌─────────────┐         ┌─────────────────┐
    │   BUDGET    │         │  CONTRIBUTION   │
    ├─────────────┤         ├─────────────────┤
    │ id          │         │ id              │
    │ user_id (FK)│         │ goal_id (FK)    │
    │ category_id │         │ amount          │
    │ limit_amount│         │ note            │
    │ period_type │         │ created_at      │
    │ alert_thresh│         └─────────────────┘
    │ start_date  │
    │ end_date    │
    └─────────────┘

    ┌─────────────┐
    │NOTIFICATION │
    ├─────────────┤
    │ id          │
    │ user_id (FK)│
    │ type        │
    │ title       │
    │ message     │
    │ is_read     │
    │ metadata    │
    │ created_at  │
    └─────────────┘
```

### 8.2 Các Entity Chính

| Entity           | Mô tả               | Thuộc tính chính                          |
| ---------------- | ------------------- | ----------------------------------------- |
| **User**         | Người dùng ứng dụng | id, email, password, name                 |
| **Account**      | Tài khoản ngân hàng | bank_name, account_number, balance        |
| **Transaction**  | Giao dịch thu/chi   | amount, type, merchant, category, is_auto |
| **Category**     | Danh mục chi tiêu   | name, icon, color                         |
| **Budget**       | Hạn mức chi tiêu    | category, limit_amount, period            |
| **SavingsGoal**  | Mục tiêu tiết kiệm  | target_amount, current_amount, deadline   |
| **Notification** | Thông báo cho user  | type, title, message, is_read             |

---

## 9. Quy tắc Nghiệp vụ

### 9.1 Quy tắc Phân loại Giao dịch

```python
# BR-CAT-001: Quy tắc phân loại theo merchant
CATEGORY_RULES = {
    # Di chuyển
    "GRAB|UBER|BE|GOJEK": "Di chuyển",

    # Ăn uống (ưu tiên cao hơn nếu có keyword FOOD)
    "GRAB FOOD|SHOPEEFOOD|GOFOOD|BAEMIN": "Ăn uống",
    "COFFEE|HIGHLANDS|STARBUCKS|PHUC LONG": "Ăn uống",
    "LOTTERIA|KFC|JOLLIBEE|MCDONALDS": "Ăn uống",

    # Mua sắm
    "SHOPEE|LAZADA|TIKI|SENDO": "Mua sắm",
    "VINMART|COOPMART|BIGC|LOTTE": "Mua sắm",

    # Giải trí
    "CGV|LOTTE CINEMA|GALAXY|BHD": "Giải trí",
    "NETFLIX|SPOTIFY|YOUTUBE": "Giải trí",

    # Hóa đơn & Tiện ích
    "EVN|DIEN LUC|NUOC|WATER": "Hóa đơn",
    "VIETTEL|FPT|VNPT|MOBIFONE": "Hóa đơn",

    # Mặc định
    "DEFAULT": "Khác"
}
```

### 9.2 Quy tắc Cảnh báo Ngân sách

```python
# BR-BUD-001: Cảnh báo khi vượt ngưỡng
def check_budget_alert(budget, current_spent):
    percentage = (current_spent / budget.limit_amount) * 100

    if percentage >= 100:
        return {
            "type": "BUDGET_EXCEEDED",
            "severity": "HIGH",
            "message": f"Bạn đã VƯỢT hạn mức '{budget.category}'"
        }
    elif percentage >= budget.alert_threshold:  # Thường là 70%
        days_left = (budget.end_date - today()).days
        return {
            "type": "BUDGET_WARNING",
            "severity": "MEDIUM",
            "message": f"Bạn đã chi {percentage}% hạn mức, còn {days_left} ngày"
        }

    return None
```

### 9.3 Quy tắc Phát hiện Bất thường

```python
# BR-ANO-001: Phát hiện giao dịch bất thường
def detect_anomaly(transaction, historical_avg, historical_std):
    # Z-score based anomaly detection
    z_score = (transaction.amount - historical_avg) / historical_std

    if z_score > 3.0:
        return {
            "is_anomaly": True,
            "severity": "HIGH",
            "score": z_score,
            "reason": f"Cao hơn {((transaction.amount / historical_avg - 1) * 100):.0f}% so với trung bình"
        }
    elif z_score > 2.0:
        return {
            "is_anomaly": True,
            "severity": "MEDIUM",
            "score": z_score,
            "reason": f"Cao hơn bình thường đáng kể"
        }

    return {"is_anomaly": False}
```

### 9.4 Quy tắc Gợi ý Tiết kiệm

```python
# BR-SAV-001: Tính toán gợi ý tiết kiệm
def calculate_savings_suggestion(category_spending, weeks_analyzed=4):
    weekly_avg = sum(category_spending) / weeks_analyzed

    # Gợi ý giảm 30-50% cho các danh mục có thể cắt giảm
    if category in ["Trà sữa", "Cà phê", "Giải trí", "Mua sắm"]:
        suggested_reduction = 0.5  # 50%
        suggested_target = weekly_avg * (1 - suggested_reduction)
        monthly_savings = (weekly_avg - suggested_target) * 4

        return {
            "category": category,
            "current_weekly": weekly_avg,
            "suggested_weekly": suggested_target,
            "monthly_savings": monthly_savings,
            "yearly_savings": monthly_savings * 12
        }

    return None
```

### 9.5 Quy tắc Tính toán Mục tiêu Tiết kiệm

```python
# BR-GOAL-001: Tính số tiền cần tiết kiệm
def calculate_savings_plan(goal):
    remaining = goal.target_amount - goal.current_amount
    days_left = (goal.target_date - today()).days

    if days_left <= 0:
        return {"status": "OVERDUE", "message": "Đã quá hạn"}

    daily_required = remaining / days_left
    weekly_required = daily_required * 7
    monthly_required = daily_required * 30

    # Đánh giá khả thi
    if monthly_required > user.avg_monthly_savings * 0.8:
        feasibility = "CHALLENGING"
    elif monthly_required > user.avg_monthly_savings * 0.5:
        feasibility = "MODERATE"
    else:
        feasibility = "EASY"

    return {
        "daily_required": daily_required,
        "weekly_required": weekly_required,
        "monthly_required": monthly_required,
        "feasibility": feasibility,
        "days_remaining": days_left
    }
```

---

## 📎 Phụ lục

### A. Danh mục Chi tiêu Mặc định

| ID  | Tên       | Icon | Màu     | Mô tả                             |
| --- | --------- | ---- | ------- | --------------------------------- |
| 1   | Ăn uống   | 🍔   | #FF5722 | Thực phẩm, đồ ăn, đồ uống         |
| 2   | Di chuyển | 🚗   | #2196F3 | Xăng, taxi, grab, xe buýt         |
| 3   | Mua sắm   | 🛍️   | #9C27B0 | Quần áo, đồ dùng, online shopping |
| 4   | Giải trí  | 🎬   | #4CAF50 | Phim, game, du lịch               |
| 5   | Hóa đơn   | 📄   | #FF9800 | Điện, nước, internet, điện thoại  |
| 6   | Sức khỏe  | 💊   | #E91E63 | Thuốc, bệnh viện, bảo hiểm        |
| 7   | Giáo dục  | 📚   | #3F51B5 | Học phí, sách, khóa học           |
| 8   | Khác      | 📦   | #607D8B | Các khoản khác                    |

### B. Loại Thông báo

| Type               | Severity | Trigger                   |
| ------------------ | -------- | ------------------------- |
| `BUDGET_WARNING`   | MEDIUM   | Vượt 70% ngân sách        |
| `BUDGET_EXCEEDED`  | HIGH     | Vượt 100% ngân sách       |
| `ANOMALY_DETECTED` | MEDIUM   | Giao dịch bất thường      |
| `SAVINGS_TIP`      | LOW      | Gợi ý tiết kiệm định kỳ   |
| `GOAL_PROGRESS`    | LOW      | Cập nhật tiến độ mục tiêu |
| `GOAL_ACHIEVED`    | LOW      | Đạt mục tiêu tiết kiệm    |

### C. Trạng thái Mục tiêu Tiết kiệm

| Status      | Mô tả            |
| ----------- | ---------------- |
| `ACTIVE`    | Đang thực hiện   |
| `COMPLETED` | Đã đạt mục tiêu  |
| `CANCELLED` | Đã hủy           |
| `OVERDUE`   | Quá hạn chưa đạt |

---

_Tài liệu được tạo cho dự án Finpal - Smart Wallet Application_  
_© 2025 FinPal Team_
