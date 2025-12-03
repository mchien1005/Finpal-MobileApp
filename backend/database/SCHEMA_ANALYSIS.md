# PHÂN TÍCH SCHEMA DATABASE - DỰ ÁN VÍ THÔNG MINH (FINPAL)

## 📋 TỔNG QUAN

Phân tích này so sánh thiết kế database hiện tại với yêu cầu nghiệp vụ của dự án "Ví Thông Minh" để xác định các vấn đề cần khắc phục.

---

## ✅ ĐIỂM MẠNH CỦA SCHEMA HIỆN TẠI

### 1. Cấu trúc Tổng thể Tốt

- ✅ Có đầy đủ các bảng chính: `users`, `transactions`, `categories`, `accounts`
- ✅ Sử dụng UTF-8 MB4 đúng chuẩn cho tiếng Việt
- ✅ Có index hợp lý cho các trường quan trọng
- ✅ Foreign key constraints đảm bảo tính toàn vẹn dữ liệu
- ✅ Audit logs để theo dõi hệ thống

### 2. Hỗ trợ Tốt cho Tính năng SMS/AI

- ✅ Bảng `sms_parsers` - parser templates cho từng ngân hàng
- ✅ Bảng `category_rules` - rules tự động phân loại
- ✅ Trường `is_auto` trong `transactions` - phân biệt giao dịch tự động/thủ công
- ✅ Trường `sms_content_encrypted` - lưu SMS gốc an toàn

### 3. Tính năng Nâng cao

- ✅ Budgets (ngân sách) với alert threshold
- ✅ Savings goals (mục tiêu tiết kiệm) - "Hũ tiết kiệm"
- ✅ Spending insights (phân tích chi tiêu)
- ✅ Notifications system

---

## 🔴 VẤN ĐỀ CẦN SỬA - CRITICAL ISSUES

### ❌ ISSUE #1: Thiếu Bảng `recurring_transactions` (Giao dịch định kỳ)

**Vấn đề:** Dự án yêu cầu phát hiện bất thường:

> "Hóa đơn tiền điện tháng này của bạn (500.000đ) cao hơn 30% so với trung bình (350.000đ)."

**Hiện trạng:** Không có cách nào lưu trữ thông tin về các giao dịch định kỳ (tiền điện, nước, internet, Netflix...)

**Giải pháp:** Cần tạo bảng mới:

```sql
CREATE TABLE IF NOT EXISTS recurring_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    merchant VARCHAR(255) NOT NULL COMMENT 'EVN, VNPT, Netflix...',
    average_amount DECIMAL(15, 2) NOT NULL COMMENT 'Số tiền trung bình',
    min_amount DECIMAL(15, 2),
    max_amount DECIMAL(15, 2),
    frequency ENUM('DAILY', 'WEEKLY', 'MONTHLY', 'QUARTERLY', 'YEARLY') DEFAULT 'MONTHLY',
    last_transaction_date DATE,
    next_expected_date DATE COMMENT 'Ngày dự kiến giao dịch tiếp theo',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    INDEX idx_user_active (user_id, is_active),
    INDEX idx_next_expected (next_expected_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

### ❌ ISSUE #2: Bảng `transactions` - Thiếu trường `balance_after`

**Vấn đề:** SMS ngân hàng thường có cả số dư sau giao dịch:

> "Biến động số dư TK 001...: **-55,000VND** lúc 12/11/2025 09:00. **SD: 2,450,000VND**"

**Hiện trạng:** Chỉ lưu `amount` mà không lưu `balance_after`, gây khó khăn cho:

- Việc xác minh tính chính xác của giao dịch
- Phát hiện giao dịch bị thiếu (reconciliation)
- Hiển thị lịch sử số dư theo thời gian

**Giải pháp:** Thêm cột vào bảng `transactions`:

```sql
ALTER TABLE transactions
ADD COLUMN balance_after DECIMAL(15, 2) NULL
COMMENT 'Số dư sau giao dịch (từ SMS)'
AFTER amount;

ADD INDEX idx_balance_after (balance_after);
```

---

### ❌ ISSUE #3: Bảng `sms_parsers` - Thiếu ví dụ cụ thể

**Vấn đề:** Trường `field_mappings` chỉ có kiểu `JSON` mà không có cấu trúc rõ ràng.

**Giải pháp:** Cần document cấu trúc JSON chuẩn:

```json
{
  "amount": "group1",
  "type": "group2",
  "merchant": "group3",
  "balance_after": "group4",
  "transaction_date": "group5",
  "account_number": "group6"
}
```

**Ví dụ parser cho Vietcombank:**

```sql
INSERT INTO sms_parsers (bank_name, bank_code, sender_number, regex_pattern, field_mappings, sample_sms) VALUES
('Vietcombank', 'VCB', 'Vietcombank',
 'TK\\s+(\\d+).*?([+-])?([\\d,]+)VND.*?luc\\s+([\\d/\\s:]+).*?ND:\\s*([^.]+).*?SD:\\s*([\\d,]+)',
 '{
   "account_number": "group1",
   "type": "group2",
   "amount": "group3",
   "transaction_date": "group4",
   "merchant": "group5",
   "balance_after": "group6"
 }',
 'TK 001234567: -55,000VND luc 12/11/2025 09:00. ND: GRAB. SD: 2,450,000VND'
);
```

---

### ⚠️ ISSUE #4: Bảng `categories` - Thiếu cột `is_user_custom`

**Vấn đề:** Cần phân biệt:

- Categories hệ thống (Ăn uống, Di chuyển...) - không được xóa
- Categories do user tự tạo - có thể xóa/sửa

**Hiện trạng:** Có `is_system` nhưng không rõ ràng về quyền sở hữu

**Giải pháp:** Thêm cột:

```sql
ALTER TABLE categories
ADD COLUMN user_id BIGINT NULL
COMMENT 'NULL = system category, NOT NULL = user custom category'
AFTER parent_id,
ADD FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
ADD INDEX idx_user_id (user_id);
```

---

### ⚠️ ISSUE #5: Bảng `category_rules` - Thiếu `user_id`

**Vấn đề:** AI sẽ học từ hành vi của từng user:

- User A: "GRAB" → "Di chuyển"
- User B: "GRAB" → "Ăn uống" (vì chủ yếu dùng GrabFood)

**Hiện trạng:** `category_rules` là global, không cá nhân hóa

**Giải pháp:**

```sql
ALTER TABLE category_rules
ADD COLUMN user_id BIGINT NULL
COMMENT 'NULL = global rule, NOT NULL = user specific rule'
AFTER id,
ADD COLUMN confidence_score DECIMAL(3, 2) DEFAULT 1.00
COMMENT 'Độ tin cậy của rule (0.00-1.00), tăng dần khi user xác nhận'
AFTER priority,
ADD FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
ADD INDEX idx_user_confidence (user_id, confidence_score);
```

---

### ⚠️ ISSUE #6: Bảng `transactions` - Thiếu trường `suggested_category_id`

**Vấn đề:** Khi AI tự động phân loại, cần lưu cả:

- Category mà AI gợi ý (`suggested_category_id`)
- Category mà user chọn sau khi sửa (`category_id`)

→ Điều này giúp AI học và cải thiện độ chính xác

**Giải pháp:**

```sql
ALTER TABLE transactions
ADD COLUMN suggested_category_id BIGINT NULL
COMMENT 'Category do AI gợi ý ban đầu'
AFTER category_id,
ADD COLUMN is_category_confirmed BOOLEAN DEFAULT FALSE
COMMENT 'User đã xác nhận category chưa'
AFTER is_verified,
ADD FOREIGN KEY (suggested_category_id) REFERENCES categories(id) ON DELETE SET NULL,
ADD INDEX idx_suggested_category (suggested_category_id);
```

---

### ⚠️ ISSUE #7: Bảng `notifications` - Thiếu các loại thông báo cụ thể

**Vấn đề:** Yêu cầu có nhiều loại notification:

- Budget alert: "Bạn đã chi 70% hạn mức..."
- Savings tip: "Nếu bạn giảm trà sữa..."
- Anomaly: "Hóa đơn điện cao hơn 30%..."
- Weekly/Monthly report

**Hiện trạng:** Chỉ có trường `type` VARCHAR nhưng không có constraint

**Giải pháp:**

```sql
ALTER TABLE notifications
MODIFY COLUMN type ENUM(
    'BUDGET_ALERT',           -- Cảnh báo vượt ngân sách
    'BUDGET_EXCEEDED',        -- Đã vượt ngân sách
    'SAVINGS_TIP',            -- Gợi ý tiết kiệm
    'ANOMALY_DETECTED',       -- Phát hiện bất thường
    'RECURRING_MISSED',       -- Thiếu giao dịch định kỳ
    'RECURRING_INCREASED',    -- Giao dịch định kỳ tăng
    'GOAL_PROGRESS',          -- Tiến độ mục tiêu
    'GOAL_ACHIEVED',          -- Đạt mục tiêu
    'WEEKLY_REPORT',          -- Báo cáo tuần
    'MONTHLY_REPORT',         -- Báo cáo tháng
    'CATEGORY_INSIGHT'        -- Insight về category
) NOT NULL;

ALTER TABLE notifications
ADD COLUMN metadata JSON NULL
COMMENT 'Dữ liệu bổ sung cho notification (amounts, percentages, dates...)'
AFTER content;
```

---

### ⚠️ ISSUE #8: Bảng `budgets` - Thiếu trạng thái hiện tại

**Vấn đề:** Cần biết ngân sách đang ở trạng thái nào để gửi alert đúng lúc

**Giải pháp:**

```sql
ALTER TABLE budgets
ADD COLUMN current_spent DECIMAL(15, 2) DEFAULT 0.00
COMMENT 'Số tiền đã chi trong kỳ hiện tại'
AFTER amount,
ADD COLUMN last_alert_sent_at TIMESTAMP NULL
COMMENT 'Lần cuối gửi alert (tránh spam)'
AFTER alert_threshold,
ADD INDEX idx_current_spent (current_spent);
```

---

### ⚠️ ISSUE #9: Thiếu Bảng `user_category_preferences`

**Vấn đề:** AI cần học preferences của từng user:

- User thích phân loại GrabFood vào "Ăn uống" hay "Di chuyển"?
- User có muốn split một giao dịch thành nhiều categories?

**Giải pháp:** Tạo bảng mới:

```sql
CREATE TABLE IF NOT EXISTS user_category_preferences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    merchant VARCHAR(255) NOT NULL,
    preferred_category_id BIGINT NOT NULL,
    times_selected INT DEFAULT 1 COMMENT 'Số lần user chọn category này cho merchant này',
    last_selected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (preferred_category_id) REFERENCES categories(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_merchant (user_id, merchant),
    INDEX idx_times_selected (times_selected)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

### ⚠️ ISSUE #10: Bảng `spending_insights` - Thiếu breakdown chi tiết

**Vấn đề:** `insights_data JSON` quá mơ hồ. Cần cấu trúc rõ ràng hơn.

**Giải pháp:** Document cấu trúc JSON chuẩn:

```json
{
  "total_transactions": 156,
  "categories_breakdown": [
    {
      "category_id": 1,
      "category_name": "Ăn uống",
      "amount": 3500000,
      "percentage": 35,
      "change_vs_last_period": "+15%",
      "top_merchants": ["The Coffee House", "Highlands", "Lotteria"]
    }
  ],
  "top_spending_days": ["2025-11-15", "2025-11-20"],
  "comparison_vs_last_period": {
    "income_change": "+10%",
    "expense_change": "-5%"
  },
  "anomalies": [
    {
      "type": "unusual_high",
      "category": "Tiện ích",
      "amount": 500000,
      "average": 350000,
      "date": "2025-11-15"
    }
  ],
  "savings_opportunities": [
    {
      "category": "Trà sữa",
      "current_weekly": 200000,
      "suggested_weekly": 100000,
      "potential_monthly_savings": 400000
    }
  ]
}
```

---

## 🟡 IMPROVEMENT SUGGESTIONS - NICE TO HAVE

### 💡 SUGGESTION #1: Thêm bảng `transaction_splits`

**Lý do:** Một giao dịch có thể thuộc nhiều categories:

- Mua sắm Vinmart: 500k (Thực phẩm: 300k, Đồ gia dụng: 200k)

```sql
CREATE TABLE IF NOT EXISTS transaction_splits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    notes VARCHAR(255),

    FOREIGN KEY (transaction_id) REFERENCES transactions(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE,
    INDEX idx_transaction (transaction_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

### 💡 SUGGESTION #2: Thêm bảng `sms_processing_logs`

**Lý do:** Debug khi parser thất bại

```sql
CREATE TABLE IF NOT EXISTS sms_processing_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    sms_content TEXT NOT NULL,
    parser_id BIGINT NULL COMMENT 'Parser được sử dụng',
    is_success BOOLEAN DEFAULT FALSE,
    parsed_data JSON NULL,
    error_message TEXT NULL,
    processed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (parser_id) REFERENCES sms_parsers(id) ON DELETE SET NULL,
    INDEX idx_user_success (user_id, is_success),
    INDEX idx_processed_at (processed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

### 💡 SUGGESTION #3: Thêm trường `confidence_score` cho transactions

**Lý do:** Đánh giá độ tin cậy của việc parse SMS

```sql
ALTER TABLE transactions
ADD COLUMN parse_confidence DECIMAL(3, 2) DEFAULT 1.00
COMMENT 'Độ tin cậy khi parse SMS (0.00-1.00)'
AFTER is_auto;
```

---

## 📊 THỐNG KÊ VẤN ĐỀ

| Mức độ              | Số lượng | Danh sách                                        |
| ------------------- | -------- | ------------------------------------------------ |
| 🔴 **Critical**     | 3        | #1 (recurring), #2 (balance_after), #3 (parsers) |
| ⚠️ **Important**    | 7        | #4-#10                                           |
| 💡 **Nice to have** | 3        | Suggestions #1-#3                                |
| **TỔNG**            | **13**   |                                                  |

---

## 🎯 ROADMAP KHẮC PHỤC

### Phase 1: Critical Fixes (Tuần 1)

1. ✅ Tạo bảng `recurring_transactions`
2. ✅ Thêm `balance_after` vào `transactions`
3. ✅ Seed data cho `sms_parsers` với ví dụ cụ thể

### Phase 2: Important Improvements (Tuần 2)

4. ✅ Thêm `user_id` vào `categories`
5. ✅ Cải thiện `category_rules` với user_id và confidence
6. ✅ Thêm `suggested_category_id` vào `transactions`
7. ✅ Cải thiện `notifications` với ENUM types
8. ✅ Thêm `current_spent` vào `budgets`

### Phase 3: AI Learning (Tuần 3)

9. ✅ Tạo bảng `user_category_preferences`
10. ✅ Document cấu trúc JSON cho `spending_insights`

### Phase 4: Optional Enhancements

11. 💡 `transaction_splits` (nếu có thời gian)
12. 💡 `sms_processing_logs` (cho debugging)
13. 💡 `parse_confidence` score

---

## 🔐 LƯU Ý BẢO MẬT

### Hiện trạng: ✅ TỐT

- `password` được hash bằng BCrypt
- `sms_content_encrypted` được mã hóa
- `account_number_encrypted` riêng biệt với plain text

### Cần bổ sung:

```sql
-- Thêm cột để lưu encryption key version (cho key rotation)
ALTER TABLE users
ADD COLUMN encryption_key_version INT DEFAULT 1
COMMENT 'Version của encryption key đang dùng';
```

---

## 📝 KẾT LUẬN

### Tóm tắt:

Schema hiện tại đã **rất tốt** (70/100 điểm) với nền tảng vững chắc. Tuy nhiên, để đáp ứng đầy đủ yêu cầu của một "AI Financial Coach" thông minh, cần:

1. **Bổ sung dữ liệu lịch sử** (recurring transactions, balance tracking)
2. **Cải thiện AI learning** (user preferences, confidence scores)
3. **Notification system mạnh hơn** (structured types, metadata)
4. **Debugging tools** (processing logs)

### Điểm mạnh nhất:

- Đã có sẵn `sms_parsers` và `category_rules` - nền tảng cho AI
- Security được quan tâm đúng mức
- Audit logs đầy đủ

### Cần ưu tiên:

1. **Recurring transactions** - Không có thì không phát hiện được anomaly
2. **balance_after** - Quan trọng cho reconciliation
3. **User-specific learning** - Cốt lõi của "AI cá nhân hóa"

---

**Tạo bởi:** AI Analysis  
**Ngày:** 2025-11-25  
**Version:** 1.0
