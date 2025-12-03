# PHÂN TÍCH CHỨC NĂNG VÀ BẢNG DATABASE - FINPAL

## 📊 TỔNG QUAN

Tài liệu này phân tích mối quan hệ giữa các **Functional Requirements (FR)** và **Database Tables**, giúp hiểu rõ bảng nào phục vụ chức năng gì.

---

## 🎯 FR1: MODULE "TỰ ĐỘNG GHI NHẬN GIAO DỊCH" (Smart Scan)

### FR1.1: Quét Tin nhắn (Core Feature)

**Mô tả:** Đọc SMS/thông báo từ ngân hàng (VCB, TCB, ACB...)

**Bảng sử dụng:**

| Bảng            | Vai trò                                   | Trường quan trọng                              |
| --------------- | ----------------------------------------- | ---------------------------------------------- |
| **sms_parsers** | Lưu mẫu regex để parse SMS từng ngân hàng | `bank_code`, `regex_pattern`, `field_mappings` |

**Quy trình:**

1. App Android đọc SMS từ ngân hàng
2. Gửi nội dung SMS lên Backend
3. Backend tìm parser phù hợp dựa trên `bank_code`
4. Sử dụng `regex_pattern` để bóc tách thông tin

**Ví dụ:**

```sql
SELECT * FROM sms_parsers WHERE bank_code = 'VCB';
-- Trả về: regex_pattern để parse tin nhắn Vietcombank
```

---

### FR1.2: Bóc tách & Phân tích (AI/Regex)

**Mô tả:**

- Input: `"Biến động số dư TK 001...: -55,000VND lúc 12/11/2025 09:00. ND: GRAB..."`
- Output: `{ "amount": 55000, "type": "expense", "bank": "VCB", "merchant": "GRAB", "date": "..." }`

**Bảng sử dụng:**

| Bảng            | Vai trò                                  | Trường quan trọng                        |
| --------------- | ---------------------------------------- | ---------------------------------------- |
| **sms_parsers** | Cung cấp regex pattern và field mappings | `regex_pattern`, `field_mappings` (JSON) |
| **accounts**    | Tự động tạo/nhận diện tài khoản từ SMS   | `bank_code`, `last_4_digits`             |

**Quy trình:**

1. Backend nhận SMS: `"TK 001234567: -55,000VND..."`
2. Match với regex từ `sms_parsers`
3. Extract: amount=55000, merchant="GRAB", balance_after=2450000
4. Tìm hoặc tạo `account` với `bank_code='VCB'` và `last_4_digits='4567'`

**Cấu trúc JSON trong `field_mappings`:**

```json
{
  "type": "group2", // '+' = income, '-' = expense
  "amount": "group3", // 55,000
  "transaction_date": "group4", // 12/11/2025 09:00
  "merchant": "group5", // GRAB
  "balance_after": "group6" // 2,450,000
}
```

---

### FR1.3: Tự động Phân loại (AI Category)

**Mô tả:**

- "GRAB" → "Di chuyển"
- "SHOPEE" → "Mua sắm"
- "CGV" → "Giải trí"

**Bảng sử dụng:**

| Bảng               | Vai trò                                         | Trường quan trọng                      |
| ------------------ | ----------------------------------------------- | -------------------------------------- |
| **category_rules** | Luật phân loại tự động (global + user-specific) | `keyword`, `category_id`, `confidence` |
| **categories**     | Danh mục đích (Ăn uống, Di chuyển...)           | `id`, `name`, `type`                   |
| **transactions**   | Lưu kết quả phân loại                           | `suggested_category_id`, `category_id` |

**Quy trình:**

1. Backend parse được `merchant = "GRAB"`
2. Tìm rule:
   ```sql
   SELECT category_id FROM category_rules
   WHERE keyword LIKE '%GRAB%' AND user_id = 1
   ORDER BY confidence DESC LIMIT 1;
   ```
3. Nếu tìm thấy → gán `suggested_category_id` = 2 (Di chuyển)
4. User có thể sửa → gán `category_id` khác
5. AI học từ sửa đổi của user → tăng `confidence` của rule mới

**Ví dụ AI Learning:**

```sql
-- Lần đầu: AI gợi ý GRAB → Di chuyển
-- User sửa: GRAB → Ăn uống (vì là GrabFood)
-- Hệ thống tự động:
INSERT INTO category_rules (user_id, keyword, category_id, confidence)
VALUES (1, 'GRAB', 1, 0.50);  -- confidence thấp vì mới học

-- Lần sau user lại sửa tương tự → tăng confidence lên 0.75, 1.00...
```

---

### FR1.4: Ghi nhận Thủ công

**Mô tả:** User nhập tay: "Tiền gửi xe - 5,000đ", "Bánh mì - 15,000đ"

**Bảng sử dụng:**

| Bảng             | Vai trò                        | Trường quan trọng    |
| ---------------- | ------------------------------ | -------------------- |
| **transactions** | Lưu giao dịch thủ công         | `is_auto = FALSE`    |
| **accounts**     | Chọn ví (thường là "Tiền mặt") | `bank_code = 'CASH'` |
| **categories**   | User chọn danh mục             | `category_id`        |

**Quy trình:**

1. User mở màn hình "Thêm giao dịch"
2. Nhập: Amount=5000, Description="Gửi xe", Category="Di chuyển"
3. Chọn Account="Tiền mặt"
4. Backend tạo:
   ```sql
   INSERT INTO transactions
   (user_id, account_id, category_id, amount, type, description,
    transaction_date, is_auto)
   VALUES
   (1, 3, 2, 5000, 'EXPENSE', 'Gửi xe', NOW(), FALSE);
   ```

---

## 📈 FR2: MODULE "BẢNG ĐIỀU KHIỂN TRỰC QUAN" (Dashboard)

### FR2.1: Tổng quan Dòng tiền

**Mô tả:**

- Tổng Thu nhập (Tháng): 15,000,000đ
- Tổng Chi tiêu (Tháng): 8,500,000đ
- Còn lại: 6,500,000đ

**Bảng sử dụng:**

| Bảng             | Vai trò                            | Trường quan trọng                    |
| ---------------- | ---------------------------------- | ------------------------------------ |
| **transactions** | Query tất cả giao dịch trong tháng | `type`, `amount`, `transaction_date` |

**Query:**

```sql
-- Thu nhập tháng này
SELECT SUM(amount) as total_income
FROM transactions
WHERE user_id = 1
  AND type = 'INCOME'
  AND MONTH(transaction_date) = MONTH(CURRENT_DATE)
  AND YEAR(transaction_date) = YEAR(CURRENT_DATE);

-- Chi tiêu tháng này
SELECT SUM(amount) as total_expense
FROM transactions
WHERE user_id = 1
  AND type = 'EXPENSE'
  AND MONTH(transaction_date) = MONTH(CURRENT_DATE)
  AND YEAR(transaction_date) = YEAR(CURRENT_DATE);
```

---

### FR2.2: Biểu đồ Phân loại (Pie Chart)

**Mô tả:** 40% Ăn uống, 20% Mua sắm, 15% Di chuyển...

**Bảng sử dụng:**

| Bảng             | Vai trò            | Trường quan trọng             |
| ---------------- | ------------------ | ----------------------------- |
| **transactions** | Giao dịch chi tiêu | `category_id`, `amount`       |
| **categories**   | Tên danh mục       | `id`, `name`, `icon`, `color` |

**Query:**

```sql
SELECT
    c.name as category_name,
    c.icon,
    c.color,
    SUM(t.amount) as total_amount,
    ROUND(SUM(t.amount) / (
        SELECT SUM(amount) FROM transactions
        WHERE user_id = 1 AND type = 'EXPENSE'
        AND MONTH(transaction_date) = MONTH(CURRENT_DATE)
    ) * 100, 2) as percentage
FROM transactions t
JOIN categories c ON t.category_id = c.id
WHERE t.user_id = 1
  AND t.type = 'EXPENSE'
  AND MONTH(t.transaction_date) = MONTH(CURRENT_DATE)
GROUP BY c.id, c.name, c.icon, c.color
ORDER BY total_amount DESC;
```

**Output:**
| Category | Amount | Percentage | Color |
|----------|--------|------------|-------|
| Ăn uống 🍔 | 3,400,000 | 40% | #FF6B6B |
| Mua sắm 🛒 | 1,700,000 | 20% | #95E1D3 |
| Di chuyển 🚗 | 1,275,000 | 15% | #4ECDC4 |

---

### FR2.3: Lịch sử Giao dịch

**Mô tả:** Hiển thị danh sách giao dịch, cho phép sửa category nếu AI phân loại sai

**Bảng sử dụng:**

| Bảng             | Vai trò               | Trường quan trọng                                    |
| ---------------- | --------------------- | ---------------------------------------------------- |
| **transactions** | Danh sách giao dịch   | `id`, `amount`, `merchant`, `description`, `is_auto` |
| **categories**   | Tên danh mục hiện tại | `name`, `icon`                                       |
| **accounts**     | Tên ví/TK             | `name`, `bank_code`                                  |

**Query:**

```sql
SELECT
    t.id,
    t.amount,
    t.type,
    t.merchant,
    t.description,
    t.transaction_date,
    t.is_auto,
    c.name as category_name,
    c.icon as category_icon,
    a.name as account_name,
    a.bank_code
FROM transactions t
LEFT JOIN categories c ON t.category_id = c.id
LEFT JOIN accounts a ON t.account_id = a.id
WHERE t.user_id = 1
ORDER BY t.transaction_date DESC
LIMIT 50;
```

**Chức năng sửa:**

```sql
-- User nhấn "Sửa" giao dịch #123: GRAB (Di chuyển) → Ăn uống
UPDATE transactions
SET category_id = 1  -- Ăn uống
WHERE id = 123 AND user_id = 1;

-- Đồng thời AI học:
INSERT INTO category_rules (user_id, keyword, category_id, confidence)
VALUES (1, 'GRAB', 1, 0.50)
ON DUPLICATE KEY UPDATE confidence = confidence + 0.10;
```

---

## 🤖 FR3: MODULE "TRỢ LÝ AI GỢI Ý" (AI Financial Coach)

### FR3.1: Gửi Thông báo Chủ động

**Mô tả:**

- "Bạn đã chi 70% hạn mức 'Ăn ngoài' của tháng này"
- "Bạn chi 200.000đ cho 'Trà sữa' mỗi tuần"

**Bảng sử dụng:**

| Bảng              | Vai trò                    | Trường quan trọng                        |
| ----------------- | -------------------------- | ---------------------------------------- |
| **budgets**       | Ngân sách đã đặt           | `amount`, `current_spent`, `category_id` |
| **transactions**  | Tính tổng chi tiêu         | `category_id`, `amount`, `merchant`      |
| **notifications** | Lưu thông báo gửi cho user | `type`, `title`, `content`               |

**Quy trình Cảnh báo Ngân sách:**

```sql
-- 1. Tính chi tiêu hiện tại cho category "Ăn ngoài" (id=1)
SELECT SUM(amount) INTO @current_spent
FROM transactions
WHERE user_id = 1
  AND category_id = 1
  AND type = 'EXPENSE'
  AND MONTH(transaction_date) = MONTH(CURRENT_DATE);

-- 2. Update budget
UPDATE budgets
SET current_spent = @current_spent
WHERE user_id = 1 AND category_id = 1
  AND month = DATE_FORMAT(CURRENT_DATE, '%Y-%m-01');

-- 3. Kiểm tra nếu vượt 70% → gửi alert
SELECT * FROM budgets
WHERE user_id = 1
  AND category_id = 1
  AND (current_spent / amount * 100) >= 70
  AND month = DATE_FORMAT(CURRENT_DATE, '%Y-%m-01');

-- 4. Tạo notification
INSERT INTO notifications (user_id, type, title, content)
VALUES (
    1,
    'BUDGET_ALERT',
    'Cảnh báo Ngân sách Ăn ngoài',
    'Bạn đã chi 70% hạn mức "Ăn ngoài" của tháng này (3.5tr/5tr), chỉ còn 15 ngày nữa là hết tháng.'
);
```

**Quy trình Gợi ý Tiết kiệm:**

```sql
-- 1. Phát hiện pattern "Trà sữa"
SELECT
    merchant,
    COUNT(*) as frequency,
    AVG(amount) as avg_amount,
    SUM(amount) as total_amount
FROM transactions
WHERE user_id = 1
  AND merchant LIKE '%TRÀ SỮA%'
  AND transaction_date >= DATE_SUB(CURRENT_DATE, INTERVAL 4 WEEK)
GROUP BY merchant;

-- 2. Nếu frequency >= 4 (mỗi tuần 1 lần) → gửi gợi ý
INSERT INTO notifications (user_id, type, title, content)
VALUES (
    1,
    'SAVINGS_TIP',
    'Gợi ý Tiết kiệm: Trà sữa',
    'FinPal nhận thấy bạn chi trung bình 50.000đ cho Trà sữa, 4 lần/tuần (200.000đ/tuần). Nếu giảm còn 2 lần/tuần, bạn sẽ tiết kiệm 400.000đ/tháng = 4.8tr/năm! 🎯'
);
```

---

### FR3.2: Phát hiện Bất thường

**Mô tả:** "Hóa đơn tiền điện tháng này (500.000đ) cao hơn 30% so với trung bình (350.000đ)"

**Bảng sử dụng:**

| Bảng                       | Vai trò                                     | Trường quan trọng                         |
| -------------------------- | ------------------------------------------- | ----------------------------------------- |
| **recurring_transactions** | Lưu giao dịch định kỳ và số tiền trung bình | `merchant`, `average_amount`, `frequency` |
| **transactions**           | Giao dịch mới nhất                          | `merchant`, `amount`, `transaction_date`  |
| **notifications**          | Gửi cảnh báo                                | `type = 'ANOMALY_DETECTED'`               |

**Quy trình:**

```sql
-- 1. Khi có giao dịch mới từ EVN (tiền điện)
-- Backend tự động check trong recurring_transactions

SELECT
    rt.average_amount,
    rt.merchant,
    t.amount as current_amount,
    ROUND((t.amount - rt.average_amount) / rt.average_amount * 100) as percent_change
FROM transactions t
JOIN recurring_transactions rt
    ON rt.user_id = t.user_id
    AND rt.merchant = t.merchant
WHERE t.id = 456  -- Transaction mới vừa tạo
  AND t.merchant = 'EVN';

-- 2. Nếu percent_change > 30% → tạo alert
-- Output: average=350000, current=500000, percent_change=+43%

INSERT INTO notifications (user_id, type, title, content)
VALUES (
    1,
    'ANOMALY_DETECTED',
    'Bất thường: Hóa đơn tiền điện',
    'Hóa đơn tiền điện tháng này của bạn (500.000đ) cao hơn 43% so với trung bình 3 tháng gần đây (350.000đ). Có thể do sử dụng điều hòa nhiều hơn? 🔌'
);

-- 3. Update recurring_transactions
UPDATE recurring_transactions
SET
    average_amount = (average_amount * 0.7 + 500000 * 0.3), -- Weighted average
    last_transaction_date = CURRENT_DATE,
    next_expected_date = DATE_ADD(CURRENT_DATE, INTERVAL 1 MONTH)
WHERE user_id = 1 AND merchant = 'EVN';
```

**Cách hệ thống học giao dịch định kỳ:**

```sql
-- Khi phát hiện cùng 1 merchant xuất hiện 3 tháng liên tiếp → tạo recurring
INSERT INTO recurring_transactions
(user_id, category_id, merchant, average_amount, frequency, next_expected_date)
SELECT
    user_id,
    category_id,
    merchant,
    AVG(amount) as avg_amount,
    'MONTHLY',
    DATE_ADD(MAX(transaction_date), INTERVAL 1 MONTH)
FROM transactions
WHERE merchant = 'EVN' AND user_id = 1
GROUP BY user_id, merchant
HAVING COUNT(*) >= 3;
```

---

### FR3.3: Tạo "Hũ Tiết kiệm"

**Mô tả:**

- Mục tiêu: "Mua tai nghe mới - 3.000.000đ"
- Deadline: 30/12/2025
- Gợi ý: "Cần trích 500.000đ/tháng"

**Bảng sử dụng:**

| Bảng              | Vai trò                                             | Trường quan trọng                                     |
| ----------------- | --------------------------------------------------- | ----------------------------------------------------- |
| **savings_goals** | Lưu mục tiêu tiết kiệm                              | `name`, `target_amount`, `current_amount`, `deadline` |
| **transactions**  | Theo dõi tiến độ (optional: tag giao dịch cho goal) | Có thể link qua `description`                         |

**Quy trình:**

```sql
-- 1. User tạo mục tiêu
INSERT INTO savings_goals
(user_id, name, target_amount, current_amount, deadline, status)
VALUES
(1, 'Mua tai nghe Sony WH-1000XM5', 3000000, 0, '2025-12-30', 'ACTIVE');

-- 2. Tính số tiền cần tiết kiệm mỗi tháng
SELECT
    name,
    target_amount,
    current_amount,
    target_amount - current_amount as remaining,
    DATEDIFF(deadline, CURRENT_DATE) as days_left,
    ROUND((target_amount - current_amount) /
          (DATEDIFF(deadline, CURRENT_DATE) / 30)) as monthly_saving_needed
FROM savings_goals
WHERE id = 1 AND user_id = 1;

-- Output:
-- remaining: 3,000,000đ
-- days_left: 35 ngày
-- monthly_saving_needed: 857,143đ/tháng (hoặc 214,286đ/tuần)

-- 3. Gửi gợi ý
INSERT INTO notifications (user_id, type, title, content)
VALUES (
    1,
    'SAVINGS_TIP',
    'Gợi ý cho mục tiêu: Mua tai nghe',
    'Để đạt được mục tiêu "Mua tai nghe Sony" (3 triệu) trước 30/12, bạn cần tiết kiệm khoảng 857.000đ/tháng. Gợi ý: Giảm trà sữa và ăn ngoài có thể giúp bạn đạt được! 💪'
);

-- 4. User nạp tiền vào hũ (ví dụ: 500.000đ)
UPDATE savings_goals
SET current_amount = current_amount + 500000
WHERE id = 1 AND user_id = 1;

-- 5. Kiểm tra nếu đạt mục tiêu
UPDATE savings_goals
SET status = 'COMPLETED'
WHERE id = 1
  AND current_amount >= target_amount;
```

---

## 📋 BẢNG TÓM TẮT: CHỨC NĂNG ↔ BẢNG

| Module              | Chức năng           | Bảng chính                     | Bảng phụ                        |
| ------------------- | ------------------- | ------------------------------ | ------------------------------- |
| **FR1: Smart Scan** | FR1.1: Quét SMS     | `sms_parsers`                  | -                               |
|                     | FR1.2: Bóc tách     | `sms_parsers`, `accounts`      | -                               |
|                     | FR1.3: Phân loại AI | `category_rules`, `categories` | `transactions`                  |
|                     | FR1.4: Nhập tay     | `transactions`                 | `accounts`, `categories`        |
| **FR2: Dashboard**  | FR2.1: Tổng quan    | `transactions`                 | -                               |
|                     | FR2.2: Biểu đồ      | `transactions`, `categories`   | -                               |
|                     | FR2.3: Lịch sử      | `transactions`                 | `categories`, `accounts`        |
| **FR3: AI Coach**   | FR3.1: Thông báo    | `budgets`, `notifications`     | `transactions`                  |
|                     | FR3.2: Bất thường   | `recurring_transactions`       | `transactions`, `notifications` |
|                     | FR3.3: Hũ tiết kiệm | `savings_goals`                | `notifications`                 |

---

## 🎯 MỨC ĐỘ SỬ DỤNG BẢNG

### 🔥 BẢNG CỐT LÕI (Dùng nhiều nhất)

1. **transactions** - Trung tâm của mọi chức năng (100%)
2. **categories** - Phân loại chi tiêu (90%)
3. **notifications** - Tất cả AI suggestions (80%)
4. **category_rules** - AI tự động phân loại (75%)

### ⚙️ BẢNG HỖ TRỢ (Dùng theo chức năng)

5. **accounts** - Quản lý ví/TK (60%)
6. **budgets** - Cảnh báo ngân sách (50%)
7. **recurring_transactions** - Phát hiện bất thường (40%)
8. **savings_goals** - Mục tiêu tiết kiệm (35%)

### 🛠️ BẢNG CẤU HÌNH (Dùng 1 lần hoặc ít)

9. **sms_parsers** - Setup ban đầu, ít thay đổi (10%)
10. **users** - Đăng ký/đăng nhập (5%)

---

## 🔗 LƯU ĐỒ DỮ LIỆU

```
┌─────────────────────────────────────────────────┐
│              USER (Người dùng)                  │
└───────────────────┬─────────────────────────────┘
                    │
        ┌───────────┼───────────┬─────────────┐
        │           │           │             │
        ▼           ▼           ▼             ▼
   ┌────────┐  ┌────────┐  ┌─────────┐  ┌──────────┐
   │accounts│  │budgets │  │savings_ │  │category_ │
   │        │  │        │  │ goals   │  │ rules    │
   └────┬───┘  └────┬───┘  └────┬────┘  └────┬─────┘
        │           │           │            │
        │           │           │            │
        ▼           │           │            ▼
   ┌────────────────┴───────────┴──────┐ ┌──────────┐
   │         TRANSACTIONS              │ │categories│
   │  (Bảng chính - Core của hệ thống) ├─┤          │
   └───────────────┬───────────────────┘ └──────────┘
                   │
        ┌──────────┼──────────┐
        │          │          │
        ▼          ▼          ▼
   ┌─────────┐ ┌──────┐ ┌─────────────┐
   │recurring│ │notify│ │sms_parsers  │
   │_trans   │ │      │ │             │
   └─────────┘ └──────┘ └─────────────┘
```

---

## 💡 GỢI Ý IMPLEMENTATION

### Thứ tự Phát triển API:

**Phase 1: Core Features (FR1.1 - FR1.4)**

1. API Parse SMS → Table: `sms_parsers`, `transactions`, `accounts`
2. API Create Transaction (thủ công) → Table: `transactions`, `categories`
3. API Auto-categorize → Table: `category_rules`

**Phase 2: Dashboard (FR2.1 - FR2.3)** 4. API Get Summary → Query: `transactions` 5. API Get Pie Chart → Query: `transactions` + `categories` 6. API Get Transaction History → Query: `transactions` + JOIN

**Phase 3: AI Coach (FR3.1 - FR3.3)** 7. API Budget Alerts → Table: `budgets`, `notifications` 8. API Detect Anomaly → Table: `recurring_transactions`, `notifications` 9. API Savings Goals → Table: `savings_goals`, `notifications`

---

**Tạo bởi:** Database Analysis  
**Ngày:** 2025-11-25  
**Phiên bản:** 1.0
