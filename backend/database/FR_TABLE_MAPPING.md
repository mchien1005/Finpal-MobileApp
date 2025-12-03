# Phân Tích Mối Quan Hệ: Functional Requirements ↔ Database Tables

**Ngày tạo:** 25/11/2025  
**Cập nhật:** 26/11/2025 (Schema tiếng Việt)  
**Mục đích:** Mapping chi tiết giữa các yêu cầu chức năng (FR) và bảng cơ sở dữ liệu

---

## 📋 Tổng Quan

### Functional Requirements Modules

1. **FR1: Smart Scan** - Quét và phân loại giao dịch tự động
2. **FR2: Dashboard** - Hiển thị tổng quan tài chính
3. **FR3: AI Financial Coach** - Tư vấn thông minh

### Database Tables (14 bảng - Schema Tiếng Việt)

1. `nguoi_dung` (users) - Quản lý người dùng
2. `danh_muc` (categories) - Phân loại thu/chi
3. `tai_khoan` (accounts) - Tài khoản ngân hàng
4. `giao_dich` (transactions) - Giao dịch chính
5. `ngan_sach` (budgets) - Ngân sách
6. `muc_tieu_tiet_kiem` (savings_goals) - Mục tiêu tiết kiệm
7. `dong_gop_tiet_kiem` (savings_contributions) - Đóng góp tiết kiệm
8. `giao_dich_dinh_ky` (recurring_transactions) - Giao dịch định kỳ
9. `bo_phan_tich_sms` (sms_parsers) - Cấu hình phân tích SMS
10. `quy_tac_danh_muc` (category_rules) - Luật phân loại AI
11. `thong_bao` (notifications) - Thông báo hệ thống
12. `cai_dat_nguoi_dung` (user_preferences) - Cài đặt người dùng
13. `phan_tich_chi_tieu` (spending_insights) - Phân tích chi tiêu
14. `nhat_ky_he_thong` (audit_logs) - Nhật ký hệ thống

---

## 🔍 Chi Tiết Mapping: FR → Tables

### **FR1: Smart Scan Module**

#### **FR1.1: Quét tin nhắn SMS từ ngân hàng**

**Mô tả:** Tự động đọc và lọc SMS từ VCB, TCB, ACB

| Table (Tiếng Việt)   | Table (EN)  | Role    | Key Attributes                               | Query Pattern                                                                      |
| -------------------- | ----------- | ------- | -------------------------------------------- | ---------------------------------------------------------------------------------- |
| **bo_phan_tich_sms** | sms_parsers | PRIMARY | `ma_ngan_hang`, `mau_regex`, `anh_xa_truong` | `SELECT * FROM bo_phan_tich_sms WHERE ma_ngan_hang = 'VCB' AND dang_hoat_dong = 1` |
| nguoi_dung           | users       | Support | `id`, `so_dien_thoai`                        | Xác định user sở hữu SMS                                                           |

**Luồng dữ liệu:**

```
SMS (Raw)
  → bo_phan_tich_sms.mau_regex (Extract)
  → bo_phan_tich_sms.anh_xa_truong (Parse JSON)
  → giao_dich (Create)
```

**Ví dụ:**

```sql
-- Lấy parser cho VCB
SELECT mau_regex, anh_xa_truong
FROM bo_phan_tich_sms
WHERE ma_ngan_hang = 'VCB' AND dang_hoat_dong = 1;

-- Output anh_xa_truong (field_mapping):
{
  "type": "group2",
  "amount": "group3",
  "transaction_date": "group4",
  "merchant": "group5",
  "balance_after": "group6"
}
```

---

#### **FR1.2: Bóc tách thông tin giao dịch**

**Mô tả:** Trích xuất số tiền, ngày giờ, số dư, merchant

| Table (Tiếng Việt) | Table (EN)   | Role    | Key Attributes                                           | Relationship              |
| ------------------ | ------------ | ------- | -------------------------------------------------------- | ------------------------- |
| **giao_dich**      | transactions | PRIMARY | `so_tien`, `ngay_giao_dich`, `don_vi_chap_nhan`, `mo_ta` | Lưu dữ liệu đã parse      |
| **tai_khoan**      | accounts     | PRIMARY | `ten_ngan_hang`, `so_tai_khoan`, `so_du`                 | Auto-create từ SMS        |
| bo_phan_tich_sms   | sms_parsers  | Support | `anh_xa_truong`                                          | Định nghĩa cấu trúc parse |

**Auto-Account Creation Logic:**

```sql
-- Kiểm tra account đã tồn tại
SELECT id FROM tai_khoan
WHERE id_nguoi_dung = ?
  AND ten_ngan_hang = 'VCB'
  AND so_tai_khoan = '****1234';

-- Nếu không có → Tạo mới
INSERT INTO tai_khoan (id_nguoi_dung, ten_ngan_hang, so_tai_khoan, ten_tai_khoan, so_du)
VALUES (?, 'VCB', '****1234', 'VCB Lương', 15000000.00);
```

**Transaction Creation:**

```sql
INSERT INTO giao_dich (
  id_nguoi_dung, id_tai_khoan, id_danh_muc,
  so_tien, ngay_giao_dich,
  don_vi_chap_nhan, mo_ta, loai, tu_dong
)
VALUES (
  1, 5, NULL, -- id_danh_muc = NULL → Chờ AI gán
  50000.00, '2025-11-25 14:30:00',
  'Circle K', 'Mua hang Circle K', 'EXPENSE', 1
);
```

---

#### **FR1.3: AI phân loại tự động**

**Mô tả:** Gán category cho giao dịch dựa vào merchant pattern + học từ user

| Table (Tiếng Việt)   | Table (EN)     | Role    | Key Attributes                         | AI Logic                     |
| -------------------- | -------------- | ------- | -------------------------------------- | ---------------------------- |
| **quy_tac_danh_muc** | category_rules | PRIMARY | `tu_khoa`, `id_danh_muc`, `do_uu_tien` | Learning model               |
| **giao_dich**        | transactions   | PRIMARY | `id_danh_muc`                          | AI suggestion vs User choice |
| danh_muc             | categories     | Support | `id`, `ten_danh_muc`, `loai`           | Category master data         |

**AI Classification Flow:**

```
1. Merchant = "Circle K"
   → Match quy_tac_danh_muc WHERE 'Circle K' LIKE CONCAT('%', tu_khoa, '%')

2. Found rule: tu_khoa = "CIRCLE K"
   → id_danh_muc = 1 (Ăn uống)
   → do_uu_tien = 75

3. Create transaction:
   - id_danh_muc = 1 (auto-assigned)
   - tu_dong = TRUE

4. User có thể sửa nếu không đồng ý
```

**Priority-Based Matching Query:**

```sql
-- Tìm rule phù hợp nhất (priority cao nhất)
SELECT id_danh_muc, do_uu_tien
FROM quy_tac_danh_muc
WHERE 'Circle K Nguyen Trai' LIKE CONCAT('%', tu_khoa, '%')
  AND dang_hoat_dong = TRUE
ORDER BY do_uu_tien DESC
LIMIT 1;
```

**Global Rules (Seed Data):**

```sql
-- Global rule cho tất cả users
INSERT INTO quy_tac_danh_muc (tu_khoa, id_danh_muc, do_uu_tien)
VALUES
  ('CIRCLE K', 1, 75),      -- Ăn uống
  ('GRAB FOOD', 1, 95),     -- Ăn uống (priority cao hơn)
  ('GRAB', 2, 85),          -- Di chuyển
  ('SHOPEE', 3, 95);        -- Mua sắm
```

---

#### **FR1.4: Nhập giao dịch thủ công**

**Mô tả:** User tự tạo transaction (không qua SMS)

| Table (Tiếng Việt) | Table (EN)   | Role     | Key Attributes | Difference vs Auto          |
| ------------------ | ------------ | -------- | -------------- | --------------------------- |
| **giao_dich**      | transactions | PRIMARY  | All fields     | `tu_dong = FALSE`           |
| tai_khoan          | accounts     | Optional | -              | User có thể chọn hoặc không |
| danh_muc           | categories   | Required | `id`           | User chọn trực tiếp         |

**Manual Transaction Insert:**

```sql
INSERT INTO giao_dich (
  id_nguoi_dung, id_tai_khoan, id_danh_muc,
  so_tien, ngay_giao_dich, loai, tu_dong
)
VALUES (
  1, NULL, 5, -- Không liên kết tài khoản
  200000.00, '2025-11-25', 'EXPENSE', FALSE
);
```

**Key Differences:**
| Field | Auto (tu_dong=TRUE) | Manual (tu_dong=FALSE) |
|-------|---------------------|------------------------|
| `id_tai_khoan` | REQUIRED (auto-created) | OPTIONAL |
| `id_danh_muc` | Auto từ AI | REQUIRED (user chọn) |
| `don_vi_chap_nhan` | Có từ SMS | Có thể trống |
| `mo_ta` | Parse từ SMS | User nhập |

---

### **FR2: Dashboard Module**

#### **FR2.1: Tổng quan thu chi**

**Mô tả:** Tổng thu, tổng chi, số dư hiện tại

| Table (Tiếng Việt) | Table (EN)   | Role    | Aggregation          | Example Query          |
| ------------------ | ------------ | ------- | -------------------- | ---------------------- |
| **giao_dich**      | transactions | PRIMARY | SUM(so_tien) BY loai | Income vs Expense      |
| **tai_khoan**      | accounts     | PRIMARY | SUM(so_du)           | Tổng số dư             |
| danh_muc           | categories   | Support | JOIN for breakdown   | Chi tiết theo danh mục |

**Dashboard Summary Query:**

```sql
-- Tổng thu/chi tháng này
SELECT
  loai,
  SUM(so_tien) as total,
  COUNT(*) as transaction_count
FROM giao_dich
WHERE id_nguoi_dung = 1
  AND DATE_FORMAT(ngay_giao_dich, '%Y-%m') = '2025-11'
GROUP BY loai;

-- Kết quả:
-- INCOME  | 15,000,000 | 2
-- EXPENSE | 8,500,000  | 45
```

**Account Balance Summary:**

```sql
SELECT
  ten_ngan_hang,
  so_tai_khoan,
  so_du
FROM tai_khoan
WHERE id_nguoi_dung = 1;

-- Tổng số dư:
SELECT SUM(so_du) as total_balance
FROM tai_khoan
WHERE id_nguoi_dung = 1;
```

---

#### **FR2.2: Biểu đồ phân bổ chi tiêu**

**Mô tả:** Pie chart/Bar chart theo category

| Table (Tiếng Việt) | Table (EN)   | Role    | Join Logic                | Visualization           |
| ------------------ | ------------ | ------- | ------------------------- | ----------------------- |
| **giao_dich**      | transactions | PRIMARY | SUM(so_tien) per category | Data points             |
| **danh_muc**       | categories   | PRIMARY | JOIN for labels           | Category names + colors |

**Spending by Category Query:**

```sql
SELECT
  c.ten_danh_muc as category_name,
  SUM(t.so_tien) as total_spent,
  COUNT(t.id) as transaction_count,
  (SUM(t.so_tien) / (
    SELECT SUM(so_tien)
    FROM giao_dich
    WHERE id_nguoi_dung = 1 AND loai = 'EXPENSE'
  )) * 100 as percentage
FROM giao_dich t
JOIN danh_muc c ON t.id_danh_muc = c.id
WHERE t.id_nguoi_dung = 1
  AND t.loai = 'EXPENSE'
  AND DATE_FORMAT(t.ngay_giao_dich, '%Y-%m') = '2025-11'
GROUP BY c.id
ORDER BY total_spent DESC;

-- Output:
-- Ăn uống    | 🍔 | 3,200,000 | 15 | 37.6%
-- Di chuyển  | 🚗 | 1,800,000 | 8  | 21.2%
-- Giải trí   | 🎮 | 1,500,000 | 5  | 17.6%
```

---

#### **FR2.3: Lịch sử giao dịch**

**Mô tả:** Danh sách chi tiết, filter, search

| Table (Tiếng Việt) | Table (EN)   | Role    | Filter Fields                           | Pagination   |
| ------------------ | ------------ | ------- | --------------------------------------- | ------------ |
| **giao_dich**      | transactions | PRIMARY | `loai`, `id_danh_muc`, `ngay_giao_dich` | LIMIT OFFSET |
| **danh_muc**       | categories   | Support | JOIN for name                           | Display      |
| **tai_khoan**      | accounts     | Support | JOIN for bank info                      | Display      |

**Advanced Transaction List:**

```sql
SELECT
  t.id,
  t.so_tien,
  t.loai,
  t.ngay_giao_dich,
  t.don_vi_chap_nhan,
  t.mo_ta,
  c.ten_danh_muc as category_name,
  a.ten_ngan_hang,
  a.so_tai_khoan
FROM giao_dich t
LEFT JOIN danh_muc c ON t.id_danh_muc = c.id
LEFT JOIN tai_khoan a ON t.id_tai_khoan = a.id
WHERE t.id_nguoi_dung = 1
  AND t.loai = 'EXPENSE' -- Filter by type
  AND t.id_danh_muc = 3 -- Filter by category
  AND t.ngay_giao_dich BETWEEN '2025-11-01' AND '2025-11-30'
  AND (t.don_vi_chap_nhan LIKE '%Circle%' OR t.mo_ta LIKE '%Circle%') -- Search
ORDER BY t.ngay_giao_dich DESC
LIMIT 20 OFFSET 0; -- Pagination
```

**Edit Transaction History Tracking:**

```sql
-- Khi user sửa category:
UPDATE giao_dich
SET id_danh_muc = 5, -- Từ "Ăn uống" → "Giải trí"
    ngay_cap_nhat = NOW()
WHERE id = 123;

-- ngay_cap_nhat track lần sửa cuối
SELECT * FROM giao_dich
WHERE id_nguoi_dung = 1
  AND ngay_cap_nhat > ngay_tao -- Đã từng sửa
ORDER BY ngay_cap_nhat DESC;
```

---

### **FR3: AI Financial Coach Module**

#### **FR3.1a: Cảnh báo vượt ngân sách**

**Mô tả:** Alert khi chi tiêu gần/vượt budget

| Table (Tiếng Việt) | Table (EN)    | Role    | Key Logic                  | Alert Condition            |
| ------------------ | ------------- | ------- | -------------------------- | -------------------------- |
| **ngan_sach**      | budgets       | PRIMARY | `so_tien` vs spent         | spent >= so_tien \* 0.8    |
| **giao_dich**      | transactions  | Trigger | SUM(so_tien) trigger alert | After INSERT               |
| **thong_bao**      | notifications | Store   | `loai = 'BUDGET_ALERT'`    | Created when threshold hit |

**Budget Tracking Query:**

```sql
-- Tính tổng chi tiêu trong kỳ ngân sách
SELECT
  b.id,
  c.ten_danh_muc as category_name,
  b.so_tien as budget_limit,
  COALESCE(SUM(t.so_tien), 0) as current_spent,
  (COALESCE(SUM(t.so_tien), 0) / b.so_tien) * 100 as usage_percentage
FROM ngan_sach b
JOIN danh_muc c ON b.id_danh_muc = c.id
LEFT JOIN giao_dich t ON t.id_nguoi_dung = b.id_nguoi_dung
  AND t.id_danh_muc = b.id_danh_muc
  AND t.loai = 'EXPENSE'
  AND t.ngay_giao_dich BETWEEN b.ngay_bat_dau AND b.ngay_ket_thuc
WHERE b.id_nguoi_dung = 1
  AND b.dang_hoat_dong = TRUE
  AND NOW() BETWEEN b.ngay_bat_dau AND b.ngay_ket_thuc
GROUP BY b.id
HAVING current_spent >= b.so_tien * (b.nguong_canh_bao / 100);
```

**Create Budget Alert Notification:**

```sql
INSERT INTO thong_bao (id_nguoi_dung, loai, tieu_de, noi_dung, da_doc)
VALUES (
  1,
  'BUDGET_ALERT',
  'Cảnh báo ngân sách',
  'Chi tiêu "Ăn uống" đạt 85% (2,720,000đ/3,200,000đ)',
  FALSE
);
```

---

#### **FR3.1b: Gợi ý tiết kiệm**

**Mô tả:** Phân tích merchant pattern và gợi ý

| Table (Tiếng Việt) | Table (EN)    | Role    | Analysis Pattern                     | Tip Logic    |
| ------------------ | ------------- | ------- | ------------------------------------ | ------------ |
| **giao_dich**      | transactions  | PRIMARY | GROUP BY don_vi_chap_nhan, COUNT(\*) | Tần suất cao |
| **danh_muc**       | categories    | Support | Identify expense type                | Context      |
| **thong_bao**      | notifications | Store   | `loai = 'SAVINGS_TIP'`               | Deliver tip  |

**High-Frequency Merchant Analysis:**

```sql
-- Tìm merchant chi tiêu nhiều nhất
SELECT
  don_vi_chap_nhan,
  COUNT(*) as visit_count,
  SUM(so_tien) as total_spent,
  AVG(so_tien) as avg_per_visit,
  c.ten_danh_muc as category_name
FROM giao_dich t
JOIN danh_muc c ON t.id_danh_muc = c.id
WHERE t.id_nguoi_dung = 1
  AND t.loai = 'EXPENSE'
  AND t.ngay_giao_dich >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY t.don_vi_chap_nhan, c.id
HAVING visit_count >= 5 -- Ghé >= 5 lần/tháng
ORDER BY total_spent DESC
LIMIT 5;

-- Output:
-- Circle K | 12 lần | 600,000đ | 50,000đ | Ăn uống
```

**Generate Savings Tip:**

```sql
INSERT INTO thong_bao (id_nguoi_dung, loai, tieu_de, noi_dung, da_doc)
VALUES (
  1,
  'SAVINGS_TIP',
  '💡 Tiết kiệm thông minh',
  'Bạn chi 600,000đ tại Circle K (12 lần). Nấu ăn tại nhà có thể tiết kiệm 400,000đ/tháng!',
  FALSE
);
```

---

#### **FR3.2: Phát hiện bất thường**

**Mô tả:** Alert khi thiếu giao dịch định kỳ (điện, nước, Netflix...)

| Table (Tiếng Việt)    | Table (EN)             | Role    | Detection Logic                 | Alert Trigger    |
| --------------------- | ---------------------- | ------- | ------------------------------- | ---------------- |
| **giao_dich_dinh_ky** | recurring_transactions | PRIMARY | `lan_tiep_theo` vs NOW()        | Overdue          |
| **giao_dich**         | transactions           | Update  | Match merchant → Update pattern | Pattern learning |
| **thong_bao**         | notifications          | Store   | `loai = 'ANOMALY_ALERT'`        | Missing payment  |

**Recurring Transaction Detection:**

```sql
-- Tìm giao dịch định kỳ đã quá hạn
SELECT
  rt.id,
  rt.mo_ta,
  c.ten_danh_muc as category_name,
  rt.so_tien,
  rt.tan_suat,
  rt.lan_tiep_theo,
  DATEDIFF(NOW(), rt.lan_tiep_theo) as days_overdue
FROM giao_dich_dinh_ky rt
JOIN danh_muc c ON rt.id_danh_muc = c.id
WHERE rt.id_nguoi_dung = 1
  AND rt.lan_tiep_theo < NOW() -- Đã quá hạn
  AND rt.dang_hoat_dong = TRUE
ORDER BY days_overdue DESC;

-- Output:
-- EVN (Điện) | Hóa đơn | 350,000đ | MONTHLY | 2025-11-20 | NULL | 5 ngày
```

**Create Anomaly Alert:**

```sql
-- Gửi thông báo
INSERT INTO thong_bao (id_nguoi_dung, loai, tieu_de, noi_dung, da_doc)
VALUES (
  1,
  'ANOMALY_ALERT',
  '⚠️ Giao dịch bất thường',
  'Chưa thấy thanh toán EVN (Điện) - Thường vào ngày 20 hàng tháng (350,000đ)',
  FALSE
);
```

**Auto-Update Recurring Pattern:**

```sql
-- Khi có transaction mới match pattern
UPDATE giao_dich_dinh_ky
SET lan_tiep_theo = CASE tan_suat
    WHEN 'DAILY' THEN DATE_ADD(NOW(), INTERVAL 1 DAY)
    WHEN 'WEEKLY' THEN DATE_ADD(NOW(), INTERVAL 1 WEEK)
    WHEN 'MONTHLY' THEN DATE_ADD(NOW(), INTERVAL 1 MONTH)
    WHEN 'YEARLY' THEN DATE_ADD(NOW(), INTERVAL 1 YEAR)
  END,
    ngay_cap_nhat = NOW()
WHERE id_nguoi_dung = 1
  AND mo_ta = 'EVN';
```

---

#### **FR3.3: Theo dõi mục tiêu tiết kiệm**

**Mô tả:** Progress tracking cho savings goals

| Table (Tiếng Việt)     | Table (EN)    | Role    | Key Fields                                         | Progress Calculation     |
| ---------------------- | ------------- | ------- | -------------------------------------------------- | ------------------------ |
| **muc_tieu_tiet_kiem** | savings_goals | PRIMARY | `so_tien_muc_tieu`, `so_tien_hien_tai`, `han_chot` | (current/target) \* 100% |
| dong_gop_tiet_kiem     | contributions | Support | Track contributions                                | Sum contributions        |
| **thong_bao**          | notifications | Alert   | `loai = 'SAVINGS_REPORT'`                          | Weekly/monthly report    |

**Savings Goal Progress:**

```sql
SELECT
  id,
  ten_muc_tieu,
  so_tien_muc_tieu,
  so_tien_hien_tai,
  han_chot,
  (so_tien_hien_tai / so_tien_muc_tieu) * 100 as progress_percentage,
  (so_tien_muc_tieu - so_tien_hien_tai) as remaining_amount,
  DATEDIFF(han_chot, NOW()) as days_remaining,
  -- Số tiền cần tiết kiệm mỗi ngày
  (so_tien_muc_tieu - so_tien_hien_tai) / NULLIF(DATEDIFF(han_chot, NOW()), 0) as daily_required
FROM muc_tieu_tiet_kiem
WHERE id_nguoi_dung = 1
  AND trang_thai = 'ACTIVE'
ORDER BY han_chot ASC;

-- Output:
-- Mua iPhone 16 | 30,000,000 | 12,000,000 | 2026-03-01 | 40% | 18,000,000 | 95 ngày | 189,474đ/ngày
```

**Savings Goal Alert:**

```sql
-- Alert khi tiến độ chậm
SELECT
  sg.ten_muc_tieu,
  sg.so_tien_muc_tieu,
  sg.so_tien_hien_tai,
  sg.han_chot,
  DATEDIFF(sg.han_chot, NOW()) as days_left,
  -- Expected progress by now
  (DATEDIFF(NOW(), sg.ngay_tao) / DATEDIFF(sg.han_chot, sg.ngay_tao)) * 100 as expected_progress,
  (sg.so_tien_hien_tai / sg.so_tien_muc_tieu) * 100 as actual_progress
FROM muc_tieu_tiet_kiem sg
WHERE sg.id_nguoi_dung = 1
  AND sg.trang_thai = 'ACTIVE'
  AND (sg.so_tien_hien_tai / sg.so_tien_muc_tieu) * 100 <
      (DATEDIFF(NOW(), sg.ngay_tao) / DATEDIFF(sg.han_chot, sg.ngay_tao)) * 100 - 10
-- Chậm hơn 10% so với kế hoạch
;

-- Create notification
INSERT INTO thong_bao (id_nguoi_dung, loai, tieu_de, noi_dung, da_doc)
VALUES (
  1,
  'SAVINGS_REPORT',
  '📊 Báo cáo tiết kiệm',
  'Mục tiêu "Mua iPhone 16" đang chậm 15%. Cần tiết kiệm thêm 189,474đ/ngày!',
  FALSE
);
```

---

## 📊 Bảng Tổng Hợp: Table Usage Matrix

| Table (Tiếng Việt)     | Table (EN)             | FR1.1 | FR1.2 | FR1.3 | FR1.4 | FR2.1 | FR2.2 | FR2.3 | FR3.1a | FR3.1b | FR3.2 | FR3.3 |
| ---------------------- | ---------------------- | ----- | ----- | ----- | ----- | ----- | ----- | ----- | ------ | ------ | ----- | ----- |
| **nguoi_dung**         | users                  | ✓     | ✓     | ✓     | ✓     | ✓     | ✓     | ✓     | ✓      | ✓      | ✓     | ✓     |
| **danh_muc**           | categories             | -     | -     | ✓✓    | ✓✓    | ✓     | ✓✓    | ✓     | ✓✓     | ✓      | ✓     | -     |
| **tai_khoan**          | accounts               | -     | ✓✓    | -     | ○     | ✓✓    | -     | ✓     | -      | -      | -     | -     |
| **giao_dich**          | transactions           | -     | ✓✓    | ✓✓    | ✓✓    | ✓✓    | ✓✓    | ✓✓    | ✓      | ✓✓     | ✓     | ○     |
| **ngan_sach**          | budgets                | -     | -     | -     | -     | -     | -     | -     | ✓✓     | -      | -     | -     |
| **muc_tieu_tiet_kiem** | savings_goals          | -     | -     | -     | -     | -     | -     | -     | -      | -      | -     | ✓✓    |
| **dong_gop_tiet_kiem** | savings_contributions  | -     | -     | -     | -     | -     | -     | -     | -      | -      | -     | ✓     |
| **giao_dich_dinh_ky**  | recurring_transactions | -     | -     | -     | -     | -     | -     | -     | -      | -      | ✓✓    | -     |
| **bo_phan_tich_sms**   | sms_parsers            | ✓✓    | ✓     | -     | -     | -     | -     | -     | -      | -      | -     | -     |
| **quy_tac_danh_muc**   | category_rules         | -     | -     | ✓✓    | -     | -     | -     | -     | -      | -      | -     | -     |
| **thong_bao**          | notifications          | -     | -     | -     | -     | -     | -     | -     | ✓      | ✓      | ✓     | ✓     |
| **cai_dat_nguoi_dung** | user_preferences       | -     | -     | -     | -     | ✓     | -     | -     | ✓      | -      | -     | -     |
| **phan_tich_chi_tieu** | spending_insights      | -     | -     | -     | -     | ✓     | ✓     | -     | -      | ✓✓     | -     | -     |
| **nhat_ky_he_thong**   | audit_logs             | -     | -     | ✓     | ✓     | -     | -     | ✓     | ✓      | -      | ✓     | ✓     |

**Ký hiệu:**

- `✓✓` = Primary table (chức năng chính)
- `✓` = Support table (hỗ trợ)
- `○` = Optional (tùy chọn)
- `-` = Không liên quan

---

## 🎯 Insights & Recommendations

### 1. **Core Tables** (Sử dụng ≥ 7 FRs)

- `giao_dich` (transactions) → 9/11 FRs (81.8%) - **Bảng quan trọng nhất**
- `nguoi_dung` (users) → 11/11 FRs (100%) - Global requirement
- `danh_muc` (categories) → 7/11 FRs (63.6%)

### 2. **Specialized Tables** (1-3 FRs)

- `bo_phan_tich_sms` (sms_parsers) → FR1.1, FR1.2 (SMS processing)
- `quy_tac_danh_muc` (category_rules) → FR1.3 (AI classification)
- `ngan_sach` (budgets) → FR3.1a (Budget alerts)
- `muc_tieu_tiet_kiem` (savings_goals) → FR3.3 (Savings tracking)
- `giao_dich_dinh_ky` (recurring_transactions) → FR3.2 (Anomaly detection)
- `phan_tich_chi_tieu` (spending_insights) → FR2.1, FR2.2, FR3.1b (Analytics)
- `cai_dat_nguoi_dung` (user_preferences) → FR2.1, FR3.1a (Settings)
- `nhat_ky_he_thong` (audit_logs) → FR1.3, FR1.4, FR2.3, FR3.1a, FR3.2, FR3.3 (Tracking)

### 3. **Join Patterns** (Schema Tiếng Việt)

```sql
-- Most common join (8 occurrences)
giao_dich t
JOIN danh_muc c ON t.id_danh_muc = c.id

-- Financial overview
giao_dich t
LEFT JOIN tai_khoan a ON t.id_tai_khoan = a.id

-- AI classification
giao_dich t
JOIN quy_tac_danh_muc cr ON t.don_vi_chap_nhan LIKE CONCAT('%', cr.tu_khoa, '%')
```

### 4. **Performance Optimization**

**Critical Indexes:**

```sql
-- FR2.1, FR2.2, FR2.3 (Dashboard queries)
CREATE INDEX idx_nguoi_dung_ngay ON giao_dich(id_nguoi_dung, ngay_giao_dich, loai);

-- FR3.2 (Recurring detection)
CREATE INDEX idx_nguoi_dung_lan_tiep ON giao_dich_dinh_ky(id_nguoi_dung, lan_tiep_theo);

-- FR3.1a (Budget tracking)
CREATE INDEX idx_nguoi_dung_ky_han ON ngan_sach(id_nguoi_dung, ngay_bat_dau, ngay_ket_thuc);

-- FR1.3 (AI categorization)
CREATE INDEX idx_tu_khoa ON quy_tac_danh_muc(tu_khoa);
CREATE INDEX idx_danh_muc ON quy_tac_danh_muc(id_danh_muc);
```

### 5. **Data Flow Diagram**

```
SMS → bo_phan_tich_sms → giao_dich → {
  ├─ quy_tac_danh_muc → AI suggest category
  ├─ ngan_sach → Budget alerts
  ├─ giao_dich_dinh_ky → Anomaly detection
  ├─ muc_tieu_tiet_kiem → Savings progress
  ├─ phan_tich_chi_tieu → Spending insights
  ├─ thong_bao → User notifications
  └─ nhat_ky_he_thong → Audit trail
}
```

---

## 📌 Kết Luận

### Thiết Kế Đạt Chuẩn

✅ **1 Table : N FRs** - Mỗi bảng phục vụ nhiều chức năng (reusability cao)  
✅ **1 FR : N Tables** - Mỗi chức năng sử dụng đúng bảng cần thiết (modularity tốt)  
✅ **Separation of Concerns** - Config (bo_phan_tich_sms, quy_tac_danh_muc) tách khỏi Data (giao_dich)  
✅ **AI-Ready** - Priority-based matching, pattern recognition được thiết kế sẵn  
✅ **Schema Tiếng Việt** - Tên bảng và cột đã được bản địa hóa hoàn toàn  
✅ **Audit Trail** - Nhật ký hệ thống (nhat_ky_he_thong) tracking mọi thay đổi quan trọng

### Coverage

- **FR1 (Smart Scan):** 4/4 bảng chuyên dụng (bo_phan_tich_sms, quy_tac_danh_muc, tai_khoan, giao_dich)
- **FR2 (Dashboard):** Tận dụng giao_dich + danh_muc + phan_tich_chi_tieu (analytics-ready)
- **FR3 (AI Coach):** 5/5 bảng chuyên dụng (ngan_sach, giao_dich_dinh_ky, muc_tieu_tiet_kiem, dong_gop_tiet_kiem, thong_bao)

### Bổ Sung So Với Schema Ban Đầu

1. **dong_gop_tiet_kiem** - Track chi tiết đóng góp cho mục tiêu tiết kiệm
2. **cai_dat_nguoi_dung** - Lưu preferences (currency, language, theme, notifications)
3. **phan_tich_chi_tieu** - Cache analytics data (daily/weekly/monthly insights)
4. **nhat_ky_he_thong** - Audit logs cho compliance và debugging

**→ Schema 100% đáp ứng tất cả Functional Requirements + Enhanced với 4 bảng mới!**

---

## 🔄 Migration Notes

### Backend Code Updates Required

✅ **Entity Models** - Tất cả `@Table` và `@Column` annotations đã được cập nhật  
✅ **Repositories** - JPQL queries tự động hoạt động với tên Entity (không cần sửa)  
✅ **New Entities** - RecurringTransaction, UserPreferences, SpendingInsightEntity, AuditLog đã được tạo  
✅ **Seed Data** - File `seed_data.sql` đã được viết lại hoàn toàn với schema tiếng Việt

### Breaking Changes

⚠️ **Column Removed**: `tags` field đã bị xóa khỏi `giao_dich` (không có trong new schema)  
⚠️ **Column Added**: `anh_hoa_don` (receipt_image) đã được thêm vào `giao_dich`  
⚠️ **Field Mapping**: SMS parser `field_mapping` → `anh_xa_truong` (JSON structure same)
