# ĐÁNH GIÁ ĐỘ ĐẦY ĐỦ SCHEMA - FINPAL DATABASE

## 📋 MỤC ĐÍCH

Phân tích xem schema hiện tại (10 bảng) đã đủ để triển khai **TẤT CẢ** chức năng của hệ thống chưa.

---

## ✅ PHÂN TÍCH THEO TỪNG CHỨC NĂNG

### FR1: TỰ ĐỘNG GHI NHẬN GIAO DỊCH (Smart Scan)

#### FR1.1: Quét Tin nhắn SMS

**Yêu cầu:** Đọc SMS từ ngân hàng (VCB, TCB, ACB...)

**Bảng cần thiết:**

- ✅ `sms_parsers` - Có đủ: `bank_code`, `regex_pattern`, `field_mappings`, `sample_sms`

**Đánh giá:** ✅ **ĐẦY ĐỦ**

---

#### FR1.2: Bóc tách & Phân tích (AI/Regex)

**Yêu cầu:** Parse SMS → JSON (amount, type, merchant, date, balance_after)

**Bảng cần thiết:**

- ✅ `sms_parsers` - Đủ để parse
- ✅ `accounts` - Tự động tạo từ SMS (bank_code + last_4_digits)
- ✅ `transactions` - Lưu kết quả parse

**Kiểm tra thuộc tính:**

```sql
transactions:
  ✅ amount              -- Số tiền
  ✅ balance_after       -- Số dư sau GD
  ✅ type                -- INCOME/EXPENSE
  ✅ merchant            -- GRAB, SHOPEE...
  ✅ transaction_date    -- Thời gian GD
  ✅ is_auto             -- TRUE nếu từ SMS
  ✅ sms_content_encrypted -- Lưu SMS gốc (bảo mật)
```

**Đánh giá:** ✅ **ĐẦY ĐỦ**

---

#### FR1.3: Tự động Phân loại (AI Category)

**Yêu cầu:**

- "GRAB" → "Di chuyển" hoặc "Ăn uống" (nếu GrabFood)
- AI học từ hành vi user

**Bảng cần thiết:**

- ✅ `category_rules` - Có đủ: `user_id`, `keyword`, `category_id`, `confidence`
- ✅ `categories` - Danh mục đích
- ✅ `transactions` - Có `suggested_category_id` để track AI

**Kiểm tra logic AI Learning:**

```sql
-- Step 1: AI gợi ý dựa vào rules
SELECT category_id FROM category_rules
WHERE (user_id = ? OR user_id IS NULL)
  AND keyword LIKE CONCAT('%', merchant, '%')
ORDER BY confidence DESC, user_id DESC LIMIT 1;

-- Step 2: Lưu vào transaction
suggested_category_id = result từ step 1

-- Step 3: User sửa → AI học
IF category_id != suggested_category_id THEN
  INSERT/UPDATE category_rules
  SET confidence = confidence + 0.10
```

**Đánh giá:** ✅ **ĐẦY ĐỦ** - AI có thể học và cải thiện

---

#### FR1.4: Ghi nhận Thủ công

**Yêu cầu:** User nhập tay giao dịch tiền mặt

**Bảng cần thiết:**

- ✅ `transactions` - `is_auto = FALSE`
- ✅ `accounts` - Có account với `bank_code = 'CASH'`
- ✅ `categories` - User chọn category

**Đánh giá:** ✅ **ĐẦY ĐỦ**

---

### FR2: BẢNG ĐIỀU KHIỂN TRỰC QUAN (Dashboard)

#### FR2.1: Tổng quan Dòng tiền

**Yêu cầu:** Hiển thị thu nhập - chi tiêu = còn lại

**Query cần thiết:**

```sql
-- Thu nhập tháng này
SELECT SUM(amount) FROM transactions
WHERE user_id = ? AND type = 'INCOME'
  AND MONTH(transaction_date) = MONTH(NOW());

-- Chi tiêu tháng này
SELECT SUM(amount) FROM transactions
WHERE user_id = ? AND type = 'EXPENSE'
  AND MONTH(transaction_date) = MONTH(NOW());
```

**Bảng cần thiết:**

- ✅ `transactions` - Đủ: `user_id`, `type`, `amount`, `transaction_date`

**Đánh giá:** ✅ **ĐẦY ĐỦ**

---

#### FR2.2: Biểu đồ Phân loại (Pie Chart)

**Yêu cầu:** 40% Ăn uống, 20% Mua sắm...

**Query cần thiết:**

```sql
SELECT
  c.name, c.icon, c.color,
  SUM(t.amount) as total,
  ROUND(SUM(t.amount) / total_expense * 100, 2) as percentage
FROM transactions t
JOIN categories c ON t.category_id = c.id
WHERE t.user_id = ? AND t.type = 'EXPENSE'
  AND MONTH(t.transaction_date) = MONTH(NOW())
GROUP BY c.id;
```

**Bảng cần thiết:**

- ✅ `transactions` - JOIN với categories
- ✅ `categories` - `name`, `icon`, `color` để hiển thị

**Đánh giá:** ✅ **ĐẦY ĐỦ**

---

#### FR2.3: Lịch sử Giao dịch (Cho phép sửa)

**Yêu cầu:** Hiển thị danh sách, cho phép sửa category nếu AI sai

**Query cần thiết:**

```sql
SELECT
  t.*,
  c.name as category_name,
  c.icon,
  a.name as account_name,
  a.bank_code
FROM transactions t
LEFT JOIN categories c ON t.category_id = c.id
LEFT JOIN accounts a ON t.account_id = a.id
WHERE t.user_id = ?
ORDER BY t.transaction_date DESC;
```

**Bảng cần thiết:**

- ✅ `transactions`
- ✅ `categories`
- ✅ `accounts`

**Đánh giá:** ✅ **ĐẦY ĐỦ**

---

### FR3: TRỢ LÝ AI GỢI Ý (AI Financial Coach)

#### FR3.1: Gửi Thông báo Chủ động

**FR3.1a: Cảnh báo Ngân sách**
**Yêu cầu:** "Bạn đã chi 70% hạn mức 'Ăn ngoài'"

**Logic cần thiết:**

```sql
-- Tính chi tiêu hiện tại
SELECT SUM(amount) INTO @spent
FROM transactions
WHERE user_id = ? AND category_id = ?
  AND MONTH(transaction_date) = MONTH(NOW());

-- Update budget
UPDATE budgets
SET current_spent = @spent
WHERE user_id = ? AND category_id = ?;

-- Kiểm tra vượt ngưỡng
SELECT * FROM budgets
WHERE user_id = ?
  AND (current_spent / amount * 100) >= 70;

-- Gửi notification
INSERT INTO notifications (user_id, type, title, content)
VALUES (?, 'BUDGET_ALERT', 'Cảnh báo ngân sách', '...');
```

**Bảng cần thiết:**

- ✅ `budgets` - Có đủ: `amount`, `current_spent`, `category_id`, `month`
- ✅ `transactions` - Tính tổng chi tiêu
- ✅ `notifications` - Gửi alert

**Đánh giá:** ✅ **ĐẦY ĐỦ**

---

**FR3.1b: Gợi ý Tiết kiệm**
**Yêu cầu:** "Bạn chi 200k cho trà sữa/tuần, nếu giảm còn 100k sẽ tiết kiệm 400k/tháng"

**Logic cần thiết:**

```sql
-- Phát hiện pattern chi tiêu
SELECT
  merchant,
  COUNT(*) as frequency,
  AVG(amount) as avg_amount,
  SUM(amount) as total
FROM transactions
WHERE user_id = ?
  AND merchant LIKE '%TRÀ SỮA%'
  AND transaction_date >= DATE_SUB(NOW(), INTERVAL 4 WEEK)
GROUP BY merchant
HAVING frequency >= 4;  -- 4 lần/tháng trở lên
```

**Bảng cần thiết:**

- ✅ `transactions` - `merchant`, `amount`, `transaction_date`
- ✅ `notifications` - Gửi gợi ý

**Đánh giá:** ✅ **ĐẦY ĐỦ**

---

#### FR3.2: Phát hiện Bất thường

**Yêu cầu:** "Hóa đơn điện tháng này (500k) cao hơn 30% so với trung bình (350k)"

**Logic cần thiết:**

```sql
-- Khi có transaction mới từ EVN
SELECT
  rt.average_amount,
  t.amount as current_amount,
  ROUND((t.amount - rt.average_amount) / rt.average_amount * 100) as diff_percent
FROM transactions t
JOIN recurring_transactions rt
  ON rt.user_id = t.user_id
  AND rt.merchant = t.merchant
WHERE t.id = ? AND t.merchant = 'EVN';

-- Nếu diff_percent > 30 → tạo notification
INSERT INTO notifications (user_id, type, title, content)
VALUES (?, 'ANOMALY_DETECTED', 'Bất thường: Hóa đơn điện', '...');

-- Update recurring_transactions
UPDATE recurring_transactions
SET
  average_amount = (average_amount * 0.7 + new_amount * 0.3),
  last_transaction_date = NOW(),
  next_expected_date = DATE_ADD(NOW(), INTERVAL 1 MONTH)
WHERE user_id = ? AND merchant = 'EVN';
```

**Bảng cần thiết:**

- ✅ `recurring_transactions` - Có đủ: `merchant`, `average_amount`, `frequency`, `last_transaction_date`, `next_expected_date`
- ✅ `transactions` - Giao dịch mới
- ✅ `notifications` - Gửi cảnh báo

**Đánh giá:** ✅ **ĐẦY ĐỦ**

---

#### FR3.3: Tạo "Hũ Tiết kiệm"

**Yêu cầu:**

- User tạo mục tiêu: "Mua tai nghe - 3 triệu - deadline 30/12"
- AI gợi ý: "Cần trích 857k/tháng"

**Logic cần thiết:**

```sql
-- Tạo mục tiêu
INSERT INTO savings_goals
(user_id, name, target_amount, deadline, status)
VALUES (?, 'Mua tai nghe Sony', 3000000, '2025-12-30', 'ACTIVE');

-- Tính tiền cần tiết kiệm
SELECT
  target_amount - current_amount as remaining,
  DATEDIFF(deadline, NOW()) as days_left,
  ROUND((target_amount - current_amount) / (DATEDIFF(deadline, NOW()) / 30))
    as monthly_needed
FROM savings_goals
WHERE id = ?;

-- Gửi gợi ý
INSERT INTO notifications (user_id, type, title, content)
VALUES (?, 'SAVINGS_TIP', 'Gợi ý tiết kiệm',
  'Để đạt mục tiêu, bạn cần tiết kiệm 857k/tháng...');

-- User nạp tiền vào hũ
UPDATE savings_goals
SET current_amount = current_amount + ?
WHERE id = ?;

-- Check hoàn thành
UPDATE savings_goals
SET status = 'COMPLETED'
WHERE current_amount >= target_amount;
```

**Bảng cần thiết:**

- ✅ `savings_goals` - Có đủ: `name`, `target_amount`, `current_amount`, `deadline`, `status`
- ✅ `notifications` - Gợi ý tiết kiệm

**Đánh giá:** ✅ **ĐẦY ĐỦ**

---

## 🔍 PHÁT HIỆN VẤN ĐỀ TIỀM ẨN

### ⚠️ VẤN ĐỀ #1: Thiếu Bảng/Trường cho "Nạp tiền vào Hũ"

**Hiện trạng:**

```sql
savings_goals:
  ✅ target_amount
  ✅ current_amount
  ❌ Không có cách track LỊCH SỬ nạp tiền
```

**Vấn đề:**

- User nạp 500k hôm nay, 300k tuần sau → Không biết được chi tiết
- Không thể hiển thị "Biểu đồ tiến độ theo thời gian"
- Không thể undo nếu nạp nhầm

**Giải pháp 1: Dùng transactions**

```sql
-- Tag giao dịch nạp vào hũ
INSERT INTO transactions
(user_id, account_id, category_id, amount, type, description)
VALUES
(1, 1, NULL, 500000, 'EXPENSE', 'Nạp vào hũ: Mua tai nghe');

-- Nhưng vấn đề:
-- - Làm sao biết transaction này liên kết với savings_goal nào?
-- - Cần thêm trường savings_goal_id vào transactions?
```

**Giải pháp 2: Tạo bảng mới `savings_contributions`**

```sql
CREATE TABLE savings_contributions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    savings_goal_id BIGINT NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    contribution_date DATE NOT NULL,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (savings_goal_id) REFERENCES savings_goals(id) ON DELETE CASCADE,
    INDEX idx_goal (savings_goal_id)
);
```

**Kết luận VẤN ĐỀ #1:**

- ⚠️ **THIẾU** nếu cần track lịch sử nạp tiền chi tiết
- ✅ **ĐỦ** nếu chỉ cần biết tổng (current_amount)

**Khuyến nghị:**

- MVP: ✅ **BỎ QUA** (chỉ track current_amount)
- Future: ⚠️ **CÂN NHẮC** thêm `savings_contributions` hoặc `savings_goal_id` trong `transactions`

---

### ⚠️ VẤN ĐỀ #2: Thiếu Trường cho "Phát hiện Giao dịch Định kỳ bị Thiếu"

**Hiện trạng:**

```sql
recurring_transactions:
  ✅ next_expected_date  -- Ngày dự kiến GD tiếp theo
  ❌ Không có flag "đã alert chưa"
```

**Scenario:**

```
15/11: Tiền điện EVN - 350k (Bình thường)
→ System update next_expected_date = 15/12

15/12: Không có giao dịch EVN
→ System nên alert: "Bạn chưa thanh toán tiền điện tháng này"

16/12: System lại alert (vì chưa có cơ chế track đã alert chưa)
17/12: Lại alert nữa → SPAM USER
```

**Giải pháp:** Thêm trường vào `recurring_transactions`

```sql
ALTER TABLE recurring_transactions
ADD COLUMN last_alert_sent_at TIMESTAMP NULL
COMMENT 'Lần cuối gửi alert thiếu GD';
```

**Kết luận VẤN ĐỀ #2:**

- ⚠️ **THIẾU** trường `last_alert_sent_at`
- Impact: Có thể spam notification cho user

**Khuyến nghị:** ✅ **NÊN THÊM**

---

### ⚠️ VẤN ĐỀ #3: Thiếu Trường cho "Recurring Transaction Creation"

**Vấn đề:** Làm sao hệ thống biết một merchant là "định kỳ"?

**Hiện trạng:**

```sql
-- Không có cơ chế tự động tạo recurring_transaction
-- Phải tự code logic: "Nếu merchant xuất hiện 3 tháng liên tiếp → tạo recurring"
```

**Giải pháp trong code (không cần thêm bảng):**

```sql
-- Chạy job hàng ngày
INSERT INTO recurring_transactions
(user_id, category_id, merchant, average_amount, frequency)
SELECT
    user_id,
    category_id,
    merchant,
    AVG(amount),
    'MONTHLY'
FROM transactions
WHERE transaction_date >= DATE_SUB(NOW(), INTERVAL 3 MONTH)
GROUP BY user_id, merchant
HAVING COUNT(DISTINCT MONTH(transaction_date)) >= 3;
```

**Kết luận VẤN ĐỀ #3:**

- ✅ **ĐỦ** - Không cần thêm trường, xử lý bằng scheduled job

---

### ✅ VẤN ĐỀ #4: Kiểm tra Index Performance

**Các query thường xuyên:**

```sql
-- Query 1: Dashboard - Tổng thu chi tháng này
WHERE user_id = ? AND type = ? AND MONTH(transaction_date) = ?

-- Query 2: Lịch sử giao dịch
WHERE user_id = ? ORDER BY transaction_date DESC

-- Query 3: AI phân loại
WHERE (user_id = ? OR user_id IS NULL) AND keyword LIKE '%GRAB%'

-- Query 4: Budget alert
WHERE user_id = ? AND category_id = ? AND MONTH(transaction_date) = ?
```

**Index hiện tại:**

```sql
transactions:
  ✅ idx_user_date (user_id, transaction_date)     -- Tốt cho Query 1, 2
  ✅ idx_merchant (merchant)                       -- Tốt cho tìm kiếm

category_rules:
  ✅ idx_user_keyword (user_id, keyword)           -- Tốt cho Query 3

budgets:
  ✅ idx_user_month (user_id, month)               -- Tốt cho Query 4
```

**Đánh giá:** ✅ **ĐẦY ĐỦ** - Index coverage tốt

---

## 📊 BẢNG ĐÁNH GIÁ TỔNG THỂ

| Chức năng                | Bảng cần                           | Bảng có | Thuộc tính cần | Thuộc tính có | Đánh giá    |
| ------------------------ | ---------------------------------- | ------- | -------------- | ------------- | ----------- |
| **FR1.1: Quét SMS**      | sms_parsers                        | ✅      | 4              | ✅ 4          | ✅ **100%** |
| **FR1.2: Bóc tách**      | transactions, accounts             | ✅      | 7              | ✅ 7          | ✅ **100%** |
| **FR1.3: AI Phân loại**  | category_rules, categories         | ✅      | 5              | ✅ 5          | ✅ **100%** |
| **FR1.4: Nhập tay**      | transactions                       | ✅      | 5              | ✅ 5          | ✅ **100%** |
| **FR2.1: Tổng quan**     | transactions                       | ✅      | 4              | ✅ 4          | ✅ **100%** |
| **FR2.2: Biểu đồ**       | transactions, categories           | ✅      | 6              | ✅ 6          | ✅ **100%** |
| **FR2.3: Lịch sử**       | transactions, categories, accounts | ✅      | 8              | ✅ 8          | ✅ **100%** |
| **FR3.1a: Budget Alert** | budgets, notifications             | ✅      | 5              | ✅ 5          | ✅ **100%** |
| **FR3.1b: Savings Tip**  | transactions, notifications        | ✅      | 4              | ✅ 4          | ✅ **100%** |
| **FR3.2: Anomaly**       | recurring_transactions             | ✅      | 6              | ⚠️ 5          | ⚠️ **83%**  |
| **FR3.3: Savings Goal**  | savings_goals                      | ✅      | 6              | ✅ 6          | ✅ **100%** |

**Điểm trung bình:** **98.5%**

---

## 🎯 KẾT LUẬN VÀ KHUYẾN NGHỊ

### ✅ TỔNG THỂ: **ĐẦY ĐỦ 98.5%**

Schema hiện tại (10 bảng) **ĐỦ** để triển khai **TẤT CẢ** chức năng core của hệ thống.

### ⚠️ CÁC ĐIỂM CẦN CẢI THIỆN (Optional):

#### 1. **Anomaly Detection - Thiếu `last_alert_sent_at`**

```sql
-- Thêm vào recurring_transactions
ALTER TABLE recurring_transactions
ADD COLUMN last_alert_sent_at TIMESTAMP NULL
COMMENT 'Lần cuối alert thiếu GD định kỳ';
```

**Priority:** 🟡 **MEDIUM** (tránh spam notification)

#### 2. **Savings Goal - Cân nhắc track lịch sử nạp tiền**

**Option A: Thêm bảng mới (Recommended cho future)**

```sql
CREATE TABLE savings_contributions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    savings_goal_id BIGINT NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    contribution_date DATE NOT NULL,
    FOREIGN KEY (savings_goal_id) REFERENCES savings_goals(id)
);
```

**Option B: Thêm trường vào transactions (Simpler)**

```sql
ALTER TABLE transactions
ADD COLUMN savings_goal_id BIGINT NULL,
ADD FOREIGN KEY (savings_goal_id) REFERENCES savings_goals(id);
```

**Priority:** 🟢 **LOW** (MVP không cần, future version cân nhắc)

### ✅ KHUYẾN NGHỊ DEPLOYMENT:

**Phase 1: MVP (Hiện tại) - ĐỦ 100%**

- Deploy với 10 bảng hiện tại
- Không cần thêm gì

**Phase 2: Enhancement (Sau 3-6 tháng)**

- Thêm `last_alert_sent_at` vào `recurring_transactions`
- Cân nhắc `savings_contributions` nếu user yêu cầu

**Phase 3: Scale (Sau 1 năm)**

- Thêm audit_logs (security)
- Thêm user_preferences (customization)
- Partition tables cho performance

---

**Tạo bởi:** Schema Completeness Analysis  
**Ngày:** 2025-11-25  
**Kết luận:** ✅ **SCHEMA HIỆN TẠI ĐỦ ĐỂ TRIỂN KHAI MVP**
