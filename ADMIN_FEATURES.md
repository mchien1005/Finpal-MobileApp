# 📊 CHỨC NĂNG ADMIN - FINPAL VÍ THÔNG MINH

> **Phân tích từ:** Mô tả dự án + Database Schema (finpal_schema.sql)

---

## 🎯 TẦM NHÌN TỔNG QUAN

Admin của FinPal không chỉ là "quản trị viên hệ thống" mà còn đóng vai trò:

1. **Data Curator** - Quản lý và cải thiện chất lượng dữ liệu AI
2. **System Guardian** - Giám sát, bảo trì, và tối ưu hệ thống
3. **User Support** - Hỗ trợ users khi có vấn đề với SMS parsing hoặc AI
4. **Business Analyst** - Phân tích hành vi users để cải thiện sản phẩm

---

## 📋 MỤC LỤC

1. [Quản Lý Người Dùng](#1-quản-lý-người-dùng)
2. [Quản Lý Danh Mục & AI Rules](#2-quản-lý-danh-mục--ai-rules)
3. [Quản Lý SMS Parsers](#3-quản-lý-sms-parsers)
4. [Giám Sát AI & Model Management](#4-giám-sát-ai--model-management)
5. [Analytics & Business Intelligence](#5-analytics--business-intelligence)
6. [Hệ Thống Thông Báo](#6-hệ-thống-thông-báo)
7. [Audit & Security](#7-audit--security)
8. [Báo Cáo & Insights](#8-báo-cáo--insights)
9. [Hệ Thống & Cấu Hình](#9-hệ-thống--cấu-hình)

---

## 1. QUẢN LÝ NGƯỜI DÙNG

### 1.1. Dashboard Users

**Mục tiêu:** Giám sát toàn bộ user base, phát hiện các patterns bất thường.

**Chức năng:**

#### **A. Tổng Quan Users** (Dashboard)

```sql
-- Database: users table
SELECT
    COUNT(*) as total_users,
    SUM(CASE WHEN is_active = TRUE THEN 1 ELSE 0 END) as active_users,
    SUM(CASE WHEN created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY) THEN 1 ELSE 0 END) as new_users_7d,
    SUM(CASE WHEN last_login_at >= DATE_SUB(NOW(), INTERVAL 30 DAY) THEN 1 ELSE 0 END) as active_last_30d
FROM users;
```

**Hiển thị:**

- Total Users / Active Users / Inactive Users
- New Registrations (Last 7/30 days)
- Daily Active Users (DAU)
- Monthly Active Users (MAU)
- Retention Rate (% users còn hoạt động sau 7/30 ngày)

**Biểu đồ:**

- 📈 User Growth Chart (theo ngày/tuần/tháng)
- 📊 User Activity Heatmap (giờ/ngày peak usage)

---

#### **B. Danh Sách Users**

**Bảng danh sách với các cột:**

| Column             | Source                | Mô tả           |
| ------------------ | --------------------- | --------------- |
| ID                 | `users.id`            | User ID         |
| Username           | `users.username`      | Username        |
| Full Name          | `users.full_name`     | Tên đầy đủ      |
| Email              | `users.email`         | Email           |
| Role               | `users.role`          | USER/ADMIN      |
| Status             | `users.is_active`     | Active/Inactive |
| Total Transactions | `COUNT(transactions)` | Số giao dịch    |
| Total Accounts     | `COUNT(accounts)`     | Số tài khoản    |
| Created At         | `users.created_at`    | Ngày đăng ký    |
| Last Login         | `users.last_login_at` | Lần login cuối  |

**Filters:**

- Role (USER/ADMIN)
- Status (Active/Inactive)
- Registration Date Range
- Has Transactions (Yes/No)
- Search (username, email, full name)

**Actions:**

- View Details
- Activate/Deactivate
- Reset Password
- Delete Account (Soft Delete)

---

#### **C. Chi Tiết User**

**Tabs:**

**Tab 1: Profile Information**

```
- Basic Info: ID, Username, Email, Full Name, Phone
- Avatar
- Role & Permissions
- Status (Active/Inactive/Banned)
- Email Verified
- Created At / Updated At / Last Login
```

**Tab 2: Financial Overview**

```sql
SELECT
    COUNT(DISTINCT a.id) as total_accounts,
    COUNT(t.id) as total_transactions,
    SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END) as total_income,
    SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END) as total_expense,
    (SELECT COUNT(*) FROM budgets WHERE user_id = ?) as total_budgets,
    (SELECT COUNT(*) FROM savings_goals WHERE user_id = ?) as total_savings_goals
FROM users u
LEFT JOIN accounts a ON u.id = a.user_id
LEFT JOIN transactions t ON u.id = t.user_id
WHERE u.id = ?;
```

**Hiển thị:**

- Tổng tài khoản ngân hàng
- Tổng giao dịch (Income/Expense)
- Chi tiêu trung bình/tháng
- Top 5 Categories chi tiêu
- Số ngân sách đang active
- Số mục tiêu tiết kiệm

**Tab 3: Activity Timeline**

```
- Lịch sử đăng nhập (IP, User Agent, Timestamp)
- Recent Transactions (10 giao dịch gần nhất)
- Recent Actions (từ audit_logs)
- Notifications sent
```

**Tab 4: AI Insights**

```
- Số giao dịch tự động (is_auto = TRUE)
- Số giao dịch thủ công
- Tỷ lệ AI categorization accuracy (user có sửa category không?)
- Anomaly detected (is_anomaly = TRUE)
- SMS parsing success rate
```

---

#### **D. User Actions**

**1. Activate/Deactivate Account**

```sql
UPDATE users SET is_active = ? WHERE id = ?;
-- Ghi log vào audit_logs
```

**2. Reset Password**

```
- Generate temporary password
- Send email to user
- Force change password on next login
```

**3. Delete Account (GDPR Compliance)**

```sql
-- Soft delete
UPDATE users SET is_active = FALSE, deleted_at = NOW() WHERE id = ?;

-- Hard delete (sau 30 ngày)
DELETE FROM users WHERE id = ? AND deleted_at < DATE_SUB(NOW(), INTERVAL 30 DAY);
-- Cascade delete: accounts, transactions, budgets, savings_goals...
```

**4. Export User Data**

```
- Export toàn bộ data của user (JSON/CSV)
- Bao gồm: profile, transactions, accounts, budgets, goals
- Tuân thủ GDPR
```

---

### 1.2. User Behavior Analytics

**Mục tiêu:** Hiểu hành vi users để cải thiện sản phẩm.

**Metrics:**

#### **A. Engagement Metrics**

```sql
-- Retention Cohort Analysis
SELECT
    DATE_FORMAT(created_at, '%Y-%m') as cohort_month,
    COUNT(*) as users_count,
    SUM(CASE WHEN last_login_at >= DATE_SUB(NOW(), INTERVAL 7 DAY) THEN 1 ELSE 0 END) as active_7d,
    SUM(CASE WHEN last_login_at >= DATE_SUB(NOW(), INTERVAL 30 DAY) THEN 1 ELSE 0 END) as active_30d
FROM users
GROUP BY cohort_month
ORDER BY cohort_month DESC;
```

**Hiển thị:**

- Cohort Retention Table
- Churn Rate (% users không còn active)
- Stickiness (DAU/MAU ratio)

#### **B. Feature Adoption**

```sql
SELECT
    SUM(CASE WHEN (SELECT COUNT(*) FROM budgets WHERE user_id = u.id) > 0 THEN 1 ELSE 0 END) as users_with_budgets,
    SUM(CASE WHEN (SELECT COUNT(*) FROM savings_goals WHERE user_id = u.id) > 0 THEN 1 ELSE 0 END) as users_with_goals,
    SUM(CASE WHEN (SELECT COUNT(*) FROM transactions WHERE user_id = u.id AND is_auto = TRUE) > 0 THEN 1 ELSE 0 END) as users_using_auto_scan
FROM users u;
```

**Biểu đồ:**

- % Users sử dụng từng feature (Auto Scan, Budgets, Savings Goals)
- Average features used per user

---

## 2. QUẢN LÝ DANH MỤC & AI RULES

### 2.1. Category Management

**Mục tiêu:** Quản lý hệ thống danh mục, đảm bảo AI có dữ liệu tốt để phân loại.

#### **A. Danh Sách Categories**

```sql
SELECT
    c.*,
    COUNT(cr.id) as rules_count,
    COUNT(t.id) as transactions_count,
    (SELECT name FROM categories WHERE id = c.parent_id) as parent_name
FROM categories c
LEFT JOIN category_rules cr ON c.id = cr.category_id
LEFT JOIN transactions t ON c.id = t.category_id
GROUP BY c.id
ORDER BY c.display_order, c.name;
```

**Bảng hiển thị:**

| ID  | Name      | Type    | Icon | Color   | Parent | Rules Count | Transactions | Is System | Order |
| --- | --------- | ------- | ---- | ------- | ------ | ----------- | ------------ | --------- | ----- |
| 1   | Ăn uống   | EXPENSE | 🍔   | #FF5733 | -      | 15          | 1,234        | ✅        | 1     |
| 2   | Di chuyển | EXPENSE | 🚗   | #33FF57 | -      | 8           | 567          | ✅        | 2     |

**Actions:**

- ➕ Create New Category
- ✏️ Edit
- 🗑️ Delete (chỉ non-system categories)
- 🔄 Reorder (drag & drop)

---

#### **B. Create/Edit Category Form**

**Fields:**

```
Name: [_______________________] *Required
Type: ( ) Income  (•) Expense
Icon: [Select Icon/Emoji]
Color: [Color Picker] (default: random)
Parent Category: [Dropdown - Optional]
Is System: [ ] (chỉ admin mới thấy)
Display Order: [___] (default: auto)
```

**Validation:**

- Name unique trong cùng type
- Không cho phép xóa system categories
- Không cho phép tạo parent-child quá 2 levels

---

### 2.2. Category Rules (AI Training Data)

**Mục tiêu:** Quản lý từ khóa để AI tự động phân loại giao dịch.

#### **A. Danh Sách Rules**

```sql
SELECT
    cr.*,
    c.name as category_name,
    c.type as category_type,
    COUNT(t.id) as matched_transactions
FROM category_rules cr
JOIN categories c ON cr.category_id = c.id
LEFT JOIN transactions t ON t.merchant LIKE CONCAT('%', cr.keyword, '%')
    AND t.category_id = cr.category_id
GROUP BY cr.id
ORDER BY cr.priority DESC, cr.keyword;
```

**Bảng hiển thị:**

| ID  | Keyword  | Category  | Match Type | Priority | Active | Matched Txns | Created At |
| --- | -------- | --------- | ---------- | -------- | ------ | ------------ | ---------- |
| 1   | GRAB     | Di chuyển | CONTAINS   | 100      | ✅     | 456          | 2025-01-15 |
| 2   | GRABFOOD | Ăn uống   | CONTAINS   | 200      | ✅     | 234          | 2025-01-15 |
| 3   | SHOPEE   | Mua sắm   | CONTAINS   | 50       | ✅     | 789          | 2025-01-16 |
| 4   | CGV      | Giải trí  | EXACT      | 80       | ✅     | 123          | 2025-01-17 |

**Filters:**

- Category
- Match Type
- Active/Inactive
- Search keyword

**Actions:**

- ➕ Add New Rule
- ✏️ Edit
- 🗑️ Delete
- 🔄 Bulk Import/Export (CSV)

---

#### **B. Create/Edit Rule Form**

**Fields:**

```
Keyword: [_______________________] *Required
  (Ví dụ: GRAB, SHOPEE, CGV, NETFLIX)

Category: [Dropdown Categories] *Required

Match Type:
  ( ) Exact - Khớp chính xác
  (•) Contains - Chứa từ khóa
  ( ) Starts With - Bắt đầu bằng
  ( ) Ends With - Kết thúc bằng
  ( ) Regex - Regular Expression

Priority: [___] (0-1000, cao hơn = ưu tiên hơn)
  Ví dụ: "GRABFOOD" (200) > "GRAB" (100)

Is Active: [✓]
```

**Test Zone:**

```
Test Merchant: [GRAB VN*GRABFOOD 123456]
[Test Rule] → Result: ✅ Di chuyển (Priority: 100)
```

---

#### **C. Rule Conflicts Detection**

**Mục tiêu:** Phát hiện rules conflict (nhiều rules match cùng merchant).

```sql
-- Tìm merchants có nhiều hơn 1 rule match
SELECT
    t.merchant,
    GROUP_CONCAT(DISTINCT c.name) as matched_categories,
    COUNT(DISTINCT cr.id) as rules_matched
FROM transactions t
JOIN category_rules cr ON t.merchant LIKE CONCAT('%', cr.keyword, '%')
JOIN categories c ON cr.category_id = c.id
WHERE cr.is_active = TRUE
GROUP BY t.merchant
HAVING COUNT(DISTINCT cr.id) > 1
ORDER BY rules_matched DESC;
```

**Hiển thị:**

- List các merchants gây conflict
- Gợi ý: Tăng priority của rule chính xác hơn

---

### 2.3. AI Category Accuracy Monitoring

**Mục tiêu:** Theo dõi độ chính xác của AI categorization.

```sql
-- Transactions AI phân loại SAI (user đã sửa)
SELECT
    t.merchant,
    c_ai.name as ai_predicted,
    c_user.name as user_corrected,
    COUNT(*) as occurrences
FROM transactions t
JOIN categories c_ai ON t.category_id = c_ai.id
JOIN audit_logs al ON al.entity_id = t.id
    AND al.entity_type = 'transaction'
    AND al.action = 'UPDATE'
    AND JSON_EXTRACT(al.old_value, '$.category_id') != JSON_EXTRACT(al.new_value, '$.category_id')
JOIN categories c_user ON JSON_EXTRACT(al.new_value, '$.category_id') = c_user.id
WHERE t.is_auto = TRUE
GROUP BY t.merchant, c_ai.id, c_user.id
ORDER BY occurrences DESC
LIMIT 50;
```

**Hiển thị:**

- Top 50 merchants bị AI phân loại SAI
- AI predicted vs User corrected
- Số lần xảy ra

**Action:**

- Review và update category_rules
- Retrain AI model với feedback data

---

## 3. QUẢN LÝ SMS PARSERS

**Mục tiêu:** Đảm bảo hệ thống parse được SMS từ mọi ngân hàng.

### 3.1. Bank Templates Management

#### **A. Danh Sách Banks**

```sql
SELECT
    sp.*,
    COUNT(t.id) as transactions_parsed,
    (SELECT COUNT(*) FROM transactions WHERE sms_bank_code = sp.bank_code AND is_auto = TRUE) as success_count
FROM sms_parsers sp
LEFT JOIN transactions t ON t.sms_bank_code = sp.bank_code
GROUP BY sp.id
ORDER BY sp.priority DESC, sp.bank_name;
```

**Bảng hiển thị:**

| ID  | Bank Name   | Bank Code | Sender Number | Priority | Active | Txns Parsed | Success Rate |
| --- | ----------- | --------- | ------------- | -------- | ------ | ----------- | ------------ |
| 1   | Vietcombank | VCB       | 8039          | 100      | ✅     | 1,234       | 98.5%        |
| 2   | Techcombank | TCB       | 9206          | 90       | ✅     | 567         | 95.2%        |
| 3   | ACB         | ACB       | 8386          | 80       | ✅     | 890         | 97.1%        |

---

#### **B. Create/Edit Bank Parser**

**Fields:**

```
Bank Name: [Vietcombank____] *Required
Bank Code: [VCB] *Required (unique)
Sender Number: [8039] (Optional)

Regex Pattern: *Required
[
  Bien dong so du TK (?P<account>\d+):
  (?P<type>-|\+)(?P<amount>[\d,]+)VND
  luc (?P<date>\d{2}/\d{2}/\d{4} \d{2}:\d{2}).
  ND: (?P<content>.+)
]

Field Mappings (JSON):
{
  "amount": "amount",
  "type": "type",
  "merchant": "content",
  "account": "account",
  "date": "date"
}

Sample SMS: *For Testing
[Bien dong so du TK 001...: -55,000VND luc 12/11/2025 09:00. ND: GRAB VN*GRABFOOD 123456]

Priority: [100]
Is Active: [✓]
```

**Test Zone:**

```
[Parse Sample SMS]

Result:
✅ Parsed Successfully
{
  "amount": 55000,
  "type": "EXPENSE",
  "merchant": "GRAB VN*GRABFOOD 123456",
  "account": "001...",
  "date": "2025-11-12 09:00:00"
}
```

---

#### **C. Parser Testing Tool**

**Interface:**

```
Select Bank: [Vietcombank ▼]

Paste SMS Content:
[____________________________________________]
[____________________________________________]

[Test Parse]

Results:
----------------------------------------
✅ Amount: 55,000 VND
✅ Type: EXPENSE
✅ Merchant: GRAB VN*GRABFOOD 123456
✅ Date: 2025-11-12 09:00:00
❌ Account: Not extracted (optional)
----------------------------------------

[Save as Transaction]
```

---

### 3.2. Failed SMS Logs

**Mục tiêu:** Theo dõi các SMS không parse được để cập nhật regex.

```sql
-- Giả sử có bảng sms_logs lưu failed SMS
SELECT
    bank_code,
    sender_number,
    sms_content,
    error_message,
    created_at
FROM sms_logs
WHERE parse_status = 'FAILED'
ORDER BY created_at DESC
LIMIT 100;
```

**Hiển thị:**

- Failed SMS content
- Bank code detected (if any)
- Error message
- Timestamp

**Actions:**

- View full SMS
- Manually parse and create transaction
- Update regex pattern for this bank
- Mark as resolved

---

## 4. GIÁM SÁT AI & MODEL MANAGEMENT

**Mục tiêu:** Quản lý, train, và monitor AI models.

### 4.1. AI Models Dashboard

**Models:**

1. **Categorization Model** - Phân loại giao dịch
2. **Anomaly Detection Model** - Phát hiện giao dịch bất thường
3. **Prediction Model** - Dự đoán chi tiêu tháng tới

**Dashboard hiển thị:**

| Model             | Version | Accuracy | Last Trained | Status          | Actions                |
| ----------------- | ------- | -------- | ------------ | --------------- | ---------------------- |
| Categorization    | v2.3    | 92.5%    | 2025-11-20   | ✅ Active       | Retrain / View Metrics |
| Anomaly Detection | v1.8    | 87.3%    | 2025-11-18   | ✅ Active       | Retrain / View Metrics |
| Prediction        | v1.5    | 81.2%    | 2025-11-15   | ⚠️ Low Accuracy | Retrain / View Metrics |

---

### 4.2. Model Training Interface

**Chọn Model:** [Categorization ▼]

**Training Data Source:**

- ( ) Use all transactions (last 6 months)
- (•) Use verified transactions only (user confirmed)
- ( ) Use custom dataset

**Training Parameters:**

```
Test Size: [20]%
Random State: [42]
Max Features: [5000]
```

**[Start Training]**

**Training Progress:**

```
[████████░░] 80% Complete
- Loading data... ✅
- Preprocessing... ✅
- Training model... 🔄
- Evaluating... ⏳
- Saving model... ⏳
```

**Training Results:**

```
✅ Training Completed

Metrics:
- Accuracy: 92.5%
- Precision: 91.8%
- Recall: 93.2%
- F1-Score: 92.5%

Confusion Matrix:
[Visualization]

Top Features:
1. merchant (0.35)
2. amount (0.22)
3. description (0.18)
...

[Deploy Model] [Download Model]
```

---

### 4.3. AI Predictions Log

**Mục tiêu:** Xem lịch sử predictions, phát hiện patterns sai.

```sql
-- Giả sử có bảng ai_predictions_log
SELECT
    t.id,
    t.merchant,
    t.amount,
    c_predicted.name as ai_predicted,
    c_actual.name as actual_category,
    apl.confidence,
    CASE
        WHEN t.category_id = apl.predicted_category_id THEN 'CORRECT'
        ELSE 'INCORRECT'
    END as prediction_result
FROM ai_predictions_log apl
JOIN transactions t ON apl.transaction_id = t.id
LEFT JOIN categories c_predicted ON apl.predicted_category_id = c_predicted.id
LEFT JOIN categories c_actual ON t.category_id = c_actual.id
WHERE apl.created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)
ORDER BY apl.created_at DESC;
```

**Filters:**

- Date Range
- Prediction Result (Correct/Incorrect)
- Confidence Level (< 50%, 50-80%, > 80%)
- Category

**Actions:**

- View transaction details
- Mark as training data
- Report false positive

---

## 5. ANALYTICS & BUSINESS INTELLIGENCE

**Mục tiêu:** Hiểu toàn cảnh hoạt động của app, đưa ra quyết định sản phẩm.

### 5.1. System Overview Dashboard

**Metrics (Real-time):**

```
┌─────────────────────────────────────────────────────┐
│ SYSTEM HEALTH                                       │
├─────────────────────────────────────────────────────┤
│ Total Users: 1,234        Active (30d): 567 (46%)  │
│ Total Transactions: 45,678   Today: 234            │
│ Total Accounts: 2,345        Active: 2,100         │
│ SMS Parsed (24h): 456        Success Rate: 97.8%   │
└─────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│ SERVER RESOURCES                                    │
├─────────────────────────────────────────────────────┤
│ CPU: [████░░] 68%    Memory: [███░░░] 54%          │
│ Disk: [██░░░░] 32%   Network: 125 MB/s             │
└─────────────────────────────────────────────────────┘
```

---

### 5.2. Transaction Analytics

#### **A. Transaction Volume**

```sql
SELECT
    DATE(transaction_date) as date,
    type,
    COUNT(*) as count,
    SUM(amount) as total_amount
FROM transactions
WHERE transaction_date >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY date, type
ORDER BY date DESC;
```

**Biểu đồ:**

- 📊 Daily Transaction Volume (bars: Income vs Expense)
- 📈 Transaction Trend (last 30 days)

#### **B. Category Distribution**

```sql
SELECT
    c.name as category,
    c.type,
    COUNT(t.id) as transaction_count,
    SUM(t.amount) as total_amount,
    ROUND(SUM(t.amount) / (SELECT SUM(amount) FROM transactions WHERE type = c.type) * 100, 2) as percentage
FROM transactions t
JOIN categories c ON t.category_id = c.id
WHERE t.transaction_date >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY c.id, c.type
ORDER BY total_amount DESC;
```

**Biểu đồ:**

- 🥧 Pie Chart: Chi tiêu theo category
- 📊 Bar Chart: Top 10 categories

#### **C. Merchant Analysis**

```sql
SELECT
    merchant,
    COUNT(*) as frequency,
    AVG(amount) as avg_amount,
    SUM(amount) as total_spent
FROM transactions
WHERE type = 'EXPENSE'
  AND transaction_date >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY merchant
ORDER BY total_spent DESC
LIMIT 50;
```

**Hiển thị:**

- Top 50 merchants users chi tiêu nhiều nhất
- Frequency, Average, Total

---

### 5.3. User Segmentation

**Mục tiêu:** Phân nhóm users theo hành vi.

#### **Segments:**

**1. Power Users** (>100 transactions/month)

```sql
SELECT u.*, COUNT(t.id) as txn_count
FROM users u
JOIN transactions t ON u.id = t.user_id
WHERE t.transaction_date >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY u.id
HAVING txn_count > 100;
```

**2. Dormant Users** (Không login 30 ngày)

```sql
SELECT *
FROM users
WHERE last_login_at < DATE_SUB(NOW(), INTERVAL 30 DAY)
  AND is_active = TRUE;
```

**3. Feature Adopters**

- Users sử dụng Auto SMS Scan
- Users có Budgets
- Users có Savings Goals

**Actions:**

- Send targeted notifications
- Re-engagement campaigns

---

### 5.4. Business Metrics

#### **A. Growth Metrics**

```
Monthly Recurring Revenue (MRR): $0 (app miễn phí)
User Growth Rate: +15% MoM
Transaction Growth Rate: +22% MoM
Churn Rate: 8% (users không login 60+ ngày)
```

#### **B. Engagement Metrics**

```
Daily Active Users (DAU): 234
Monthly Active Users (MAU): 567
DAU/MAU Ratio: 41% (Good stickiness)
Avg Transactions/User/Month: 37
```

#### **C. Feature Usage**

```
Auto SMS Scan: 78% users enabled
Manual Entry: 22% users only
Budgets: 45% users created
Savings Goals: 32% users created
AI Categorization Acceptance: 87%
```

---

## 6. HỆ THỐNG THÔNG BÁO

**Mục tiêu:** Quản lý notifications gửi đến users.

### 6.1. Notification Templates

**Loại notifications (từ FR3 - AI Financial Coach):**

#### **A. Budget Alerts**

**Template:**

```
Title: "Cảnh báo Ngân sách '{category_name}'"
Content: "Bạn đã chi {spent_percentage}% hạn mức '{category_name}' của tháng này ({spent}/{budget}). Chỉ còn {days_left} ngày nữa là hết tháng."
Type: BUDGET_ALERT
Priority: HIGH
```

**Triggers:**

- When budget reaches 70% (configurable)
- When budget exceeds 100%

#### **B. Saving Tips**

**Template:**

```
Title: "Gợi ý Tiết kiệm"
Content: "FinPal nhận thấy bạn chi trung bình {avg_amount} cho '{category}' mỗi tuần. Nếu bạn giảm còn {suggested_amount}, bạn sẽ tiết kiệm được {savings}/tháng."
Type: SAVING_TIP
Priority: MEDIUM
```

**Triggers:**

- Weekly spending analysis
- When category spending > average

#### **C. Anomaly Alerts**

**Template:**

```
Title: "Giao dịch Bất thường"
Content: "Hóa đơn {category} tháng này ({current_amount}) cao hơn {percentage}% so với trung bình ({avg_amount})."
Type: ANOMALY
Priority: HIGH
```

**Triggers:**

- Anomaly Detection Model flags transaction

#### **D. Goal Progress**

**Template:**

```
Title: "Tiến độ Mục tiêu '{goal_name}'"
Content: "Bạn đã đạt {percentage}% mục tiêu '{goal_name}' ({current}/{target}). Còn {remaining} nữa là đạt mục tiêu!"
Type: GOAL_PROGRESS
Priority: LOW
```

**Triggers:**

- Every 25% milestone (25%, 50%, 75%, 100%)

---

### 6.2. Notification Management

**Dashboard:**

```sql
SELECT
    type,
    priority,
    COUNT(*) as sent_count,
    SUM(CASE WHEN is_read = TRUE THEN 1 ELSE 0 END) as read_count,
    ROUND(SUM(CASE WHEN is_read = TRUE THEN 1 ELSE 0 END) / COUNT(*) * 100, 2) as read_rate
FROM notifications
WHERE created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY type, priority;
```

**Hiển thị:**

| Type         | Priority | Sent | Read | Read Rate | Avg Time to Read |
| ------------ | -------- | ---- | ---- | --------- | ---------------- |
| BUDGET_ALERT | HIGH     | 456  | 398  | 87.3%     | 2.5 hours        |
| SAVING_TIP   | MEDIUM   | 234  | 123  | 52.6%     | 12 hours         |
| ANOMALY      | HIGH     | 89   | 82   | 92.1%     | 1.2 hours        |

**Actions:**

- Create custom notification
- Edit template
- Send test notification
- Schedule bulk notification

---

### 6.3. Send Notification

**Form:**

```
Select Users:
( ) All Active Users
( ) Segment: [Power Users ▼]
(•) Specific Users: [Select...]

Type: [BUDGET_ALERT ▼]
Priority: [HIGH ▼]

Title: [_______________________________]
Content:
[_____________________________________________]
[_____________________________________________]

Action URL (Optional): [/budgets/123]

Send Time:
(•) Immediately
( ) Schedule: [2025-11-23 10:00]

[Send Notification]
```

---

## 7. AUDIT & SECURITY

**Mục tiêu:** Giám sát mọi thay đổi trong hệ thống, đảm bảo bảo mật.

### 7.1. Audit Logs Viewer

```sql
SELECT
    al.*,
    u.username,
    u.role
FROM audit_logs al
LEFT JOIN users u ON al.user_id = u.id
ORDER BY al.created_at DESC
LIMIT 1000;
```

**Bảng hiển thị:**

| Time             | User  | Role  | Action | Entity         | Changes                     | IP        | User Agent    |
| ---------------- | ----- | ----- | ------ | -------------- | --------------------------- | --------- | ------------- |
| 2025-11-23 10:15 | admin | ADMIN | UPDATE | category_rules | keyword: "GRAB" → "GRAB VN" | 27.68...  | Chrome/120... |
| 2025-11-23 10:10 | demo  | USER  | UPDATE | transaction    | category_id: 5 → 3          | 118.70... | Mobile App    |

**Filters:**

- Date Range
- User
- Action (CREATE/UPDATE/DELETE)
- Entity Type

**Actions:**

- View full diff (old_value vs new_value JSON)
- Rollback change (if possible)
- Export logs

---

### 7.2. Security Monitoring

#### **A. Login Activity**

```sql
SELECT
    user_id,
    username,
    COUNT(*) as login_attempts,
    COUNT(DISTINCT ip_address) as unique_ips,
    MAX(created_at) as last_login
FROM audit_logs
WHERE action = 'LOGIN'
  AND created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)
GROUP BY user_id, username;
```

**Alerts:**

- Multiple failed login attempts (>5 trong 10 phút)
- Login from unusual location/IP
- Concurrent logins from different IPs

#### **B. Suspicious Activities**

```
- Bulk data export
- Mass deletion
- Permission changes
- Password resets
```

---

### 7.3. Data Privacy (GDPR)

**Chức năng:**

**1. Export User Data**

```
User ID: [123]
[Export All Data]

Output: finpal_user_123_data_2025-11-23.zip
- profile.json
- accounts.json
- transactions.csv
- budgets.json
- savings_goals.json
```

**2. Anonymize User**

```
Replace personal data with placeholders:
- Full Name → "User-[ID]"
- Email → "user[ID]@anonymized.com"
- Phone → NULL
Keep financial data for analytics
```

**3. Delete User (GDPR Right to be Forgotten)**

```
Hard delete user and ALL related data
- Confirm deletion (type username)
- Cannot be undone
- Remove from all tables (cascade)
```

---

## 8. BÁO CÁO & INSIGHTS

**Mục tiêu:** Generate reports để hiểu sâu về users và business.

### 8.1. Monthly Reports

**Auto-generate vào đầu mỗi tháng:**

```
┌────────────────────────────────────────────┐
│ FINPAL MONTHLY REPORT - NOVEMBER 2025     │
├────────────────────────────────────────────┤
│ USERS                                      │
│ - Total Users: 1,234 (+15% vs Oct)        │
│ - New Signups: 156                         │
│ - Active Users: 567 (46%)                  │
│ - Churned Users: 98 (8%)                   │
├────────────────────────────────────────────┤
│ TRANSACTIONS                               │
│ - Total Txns: 45,678 (+22% vs Oct)        │
│ - Auto Scanned: 35,678 (78%)               │
│ - Manual Entry: 10,000 (22%)               │
│ - Avg Txns/User: 37                        │
├────────────────────────────────────────────┤
│ AI PERFORMANCE                             │
│ - Categorization Accuracy: 92.5%           │
│ - SMS Parse Success Rate: 97.8%            │
│ - Anomalies Detected: 234                  │
├────────────────────────────────────────────┤
│ FEATURE ADOPTION                           │
│ - Users with Budgets: 556 (45%)            │
│ - Users with Savings Goals: 395 (32%)      │
│ - Notifications Sent: 1,234                │
│ - Notification Read Rate: 68%              │
├────────────────────────────────────────────┤
│ TOP CATEGORIES                             │
│ 1. Ăn uống - 12,345 txns (27%)            │
│ 2. Di chuyển - 8,901 txns (19%)           │
│ 3. Mua sắm - 6,789 txns (15%)             │
└────────────────────────────────────────────┘
```

**Export:** PDF / Excel / Send Email

---

### 8.2. Custom Reports

**Report Builder:**

```
Report Name: [_______________________________]

Metrics: [☑] Users  [☑] Transactions  [☐] Revenue

Dimensions:
[☑] Time (Daily/Weekly/Monthly)
[☑] Category
[☐] Bank
[☐] User Segment

Filters:
Date Range: [2025-11-01] to [2025-11-30]
User Role: [All ▼]
Transaction Type: [All ▼]

Visualization:
( ) Table
(•) Chart (Line/Bar/Pie)
( ) Both

[Generate Report]
```

---

## 9. HỆ THỐNG & CẤU HÌNH

### 9.1. System Settings

**Global Configuration:**

```
App Settings:
  App Name: [FinPal Ví Thông Minh]
  Default Currency: [VND ▼]
  Default Timezone: [Asia/Ho_Chi_Minh ▼]
  Default Language: [vi ▼]

SMS Parsing:
  Enable Auto Scan: [✓]
  Confidence Threshold: [0.7] (0-1)
  Retry Failed SMS: [✓]
  Max Retry Attempts: [3]

AI Models:
  Categorization Confidence: [0.6]
  Anomaly Detection Sensitivity: [MEDIUM ▼]
  Auto-retrain Schedule: [Weekly ▼]

Notifications:
  Enable Push Notifications: [✓]
  Enable Email Notifications: [✓]
  Daily Digest Time: [19:00]
  Budget Alert Threshold: [70]%

Security:
  Password Min Length: [8]
  Password Expiry Days: [90] (0 = never)
  Max Login Attempts: [5]
  Session Timeout: [60] minutes

Backup:
  Auto Backup: [✓]
  Backup Schedule: [Daily ▼] at [02:00]
  Retention Days: [30]
  Backup Location: [AWS S3 ▼]
```

---

### 9.2. Database Management

**Tools:**

**1. Database Health Check**

```
[Run Health Check]

Results:
✅ Tables: 13/13 OK
✅ Indexes: All optimized
⚠️ Table 'transactions': 2.3GB (consider archiving old data)
✅ Connections: 12/100
```

**2. Backup & Restore**

```
Latest Backups:
- 2025-11-23 02:00 - finpal_db_20251123.sql (234 MB)
- 2025-11-22 02:00 - finpal_db_20251122.sql (232 MB)

[Create Backup Now]
[Restore from Backup]
[Download Backup]
```

**3. Data Archiving**

```
Archive transactions older than: [6] months
Keep in archive table: transactions_archive

[Run Archive]
```

---

### 9.3. API Management

**Endpoints:**

```
API Base URL: https://api.finpal.com

Rate Limiting:
  Default: 100 requests/minute/user
  Admin: Unlimited

API Keys:
  - Backend Mobile App: [sk-live-abc123...]
  - Admin Dashboard: [sk-live-xyz789...]

[Generate New API Key]

API Usage (Last 24h):
  Total Requests: 12,345
  Avg Response Time: 120ms
  Error Rate: 0.8%

Top Endpoints:
1. POST /api/auth/login - 2,345 calls
2. GET /api/transactions - 1,234 calls
3. POST /api/transactions - 890 calls
```

---

## 📊 TỔNG KẾT CHỨC NĂNG ADMIN

### **Core Modules (9 modules):**

1. ✅ **User Management** - Quản lý users, analytics, support
2. ✅ **Category & Rules** - Quản lý categories + AI training data
3. ✅ **SMS Parsers** - Quản lý bank templates, test parsing
4. ✅ **AI Model Management** - Train, monitor, improve models
5. ✅ **Analytics & BI** - System metrics, user behavior, business insights
6. ✅ **Notification System** - Templates, scheduling, tracking
7. ✅ **Audit & Security** - Logs, GDPR, security monitoring
8. ✅ **Reports** - Monthly reports, custom reports
9. ✅ **System Settings** - Configuration, backup, API management

---

### **Total Features Count:**

- **User Management:** 12 features
- **Category & Rules:** 8 features
- **SMS Parsers:** 7 features
- **AI Models:** 6 features
- **Analytics:** 10 features
- **Notifications:** 5 features
- **Audit & Security:** 6 features
- **Reports:** 4 features
- **System Settings:** 8 features

**TOTAL: 66+ Admin Features**

---

### **Database Tables Used:**

Core Admin Tables:

- ✅ `users` - User management
- ✅ `categories` - Category management
- ✅ `category_rules` - AI training rules
- ✅ `sms_parsers` - SMS parsing templates
- ✅ `transactions` - Transaction analytics
- ✅ `notifications` - Notification management
- ✅ `audit_logs` - Security & audit
- ✅ `spending_insights` - Pre-computed analytics
- ✅ `accounts` - Account analytics
- ✅ `budgets` - Budget feature adoption
- ✅ `savings_goals` - Savings feature adoption
- ✅ `user_preferences` - User settings

**New Tables Cần Thêm:**

- `sms_logs` - Log failed SMS parsing
- `ai_predictions_log` - Log AI predictions for monitoring
- `system_settings` - Global app configuration

---

## 🎯 PRIORITY ROADMAP

### **Phase 1: MVP Admin (Core Features)**

**Timeline: 2-3 weeks**

1. ✅ User Management Dashboard

   - List users, view details, activate/deactivate
   - Basic user analytics (total, active, new)

2. ✅ Category Management

   - CRUD categories
   - Basic category rules (keyword mapping)

3. ✅ System Dashboard

   - Total users, transactions, accounts
   - Server health (CPU, memory, disk)

4. ✅ Audit Logs
   - View recent actions
   - Basic filtering

---

### **Phase 2: AI & Automation**

**Timeline: 3-4 weeks**

5. ✅ SMS Parser Management

   - Bank templates CRUD
   - Parser testing tool
   - Failed SMS logs

6. ✅ AI Model Management

   - Model dashboard (accuracy, version)
   - Manual retrain trigger
   - Predictions log

7. ✅ Category Rules Advanced
   - Conflict detection
   - Accuracy monitoring
   - Bulk import/export

---

### **Phase 3: Analytics & Intelligence**

**Timeline: 2-3 weeks**

8. ✅ Advanced Analytics

   - Transaction analytics
   - Category distribution
   - Merchant analysis

9. ✅ User Segmentation

   - Power users, dormant users
   - Feature adoption tracking

10. ✅ Monthly Reports
    - Auto-generate reports
    - Custom report builder

---

### **Phase 4: Engagement & Optimization**

**Timeline: 2-3 weeks**

11. ✅ Notification Management

    - Template management
    - Send custom notifications
    - Tracking & analytics

12. ✅ Data Privacy (GDPR)

    - Export user data
    - Anonymize user
    - Delete user

13. ✅ System Settings
    - Global configuration
    - Backup/restore
    - API management

---

## 🔐 SECURITY REQUIREMENTS

**Admin Authentication:**

- Two-Factor Authentication (2FA) mandatory
- Strong password policy (min 12 chars)
- IP whitelist (chỉ cho phép admin từ office IP)
- Session timeout: 30 minutes

**Authorization:**

- Role-Based Access Control (RBAC)
  - Super Admin (full access)
  - Admin (cannot change system settings)
  - Moderator (read-only, can update categories/rules)

**Data Protection:**

- All sensitive data encrypted in transit (TLS 1.3)
- Sensitive fields in DB encrypted (account_number, sms_content)
- Audit all admin actions
- No direct access to user passwords (BCrypt hashed)

---

## 📱 ADMIN UI/UX NOTES

**Design Principles:**

1. **Data-Dense but Readable** - Admin cần thấy nhiều info, nhưng vẫn clean
2. **Fast Actions** - Bulk operations, keyboard shortcuts
3. **Powerful Filters** - Mọi list đều có advanced filters
4. **Real-time Updates** - Dashboard auto-refresh mỗi 30s
5. **Export Everything** - Mọi table đều có nút Export CSV/Excel

**Tech Stack Gợi Ý:**

- Frontend: React + Ant Design (data-heavy UI)
- Charts: Recharts / Chart.js
- Tables: Ant Design Table (có sẵn pagination, sorting, filtering)
- Real-time: WebSocket cho live updates

---

## 🎓 KEY TAKEAWAYS

**Admin của FinPal khác Admin thông thường:**

1. **Data Curator Role** - Admin phải actively improve AI data quality
2. **Banking Domain Expert** - Hiểu SMS format, regex parsing
3. **ML Ops** - Monitor và retrain AI models
4. **Product Manager** - Phân tích user behavior để improve features
5. **Customer Support** - Debug khi users report issues (SMS không parse được)

**Tính năng Đặc Biệt:**

- ✅ SMS Parser Testing Tool (unique cho fintech app)
- ✅ AI Prediction Accuracy Monitoring
- ✅ Category Rule Conflict Detection
- ✅ User Financial Behavior Segmentation

---

**END OF DOCUMENT**

_Tài liệu này được generate từ phân tích:_

- _Mô tả dự án FinPal_
- _Database Schema (finpal_schema.sql)_
- _Yêu cầu Chức năng (FRs) và Phi chức năng (NFRs)_

_Version: 1.0_  
_Last Updated: 2025-11-23_
