# API Dashboard - Thu Chi và Biểu Đồ

## Base URL
```
http://175.41.150.228:8080/api/dashboard
```

## Authentication
Tất cả các endpoints yêu cầu JWT token. Login trước để lấy token.

### Login
```bash
POST http://175.41.150.228:8080/api/auth/login
Content-Type: application/json

{
  "username": "your_username",
  "password": "your_password"
}
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "your_username"
}
```

---

## 1. Tổng Quan Thu Chi (Cash Flow)

Tính tổng thu nhập, chi tiêu, còn lại của tháng hiện tại hoặc tháng chỉ định.

### Endpoint
```
GET /api/dashboard/cash-flow?month=2024-12-01
```

### Parameters
- `month` (optional): Tháng cần xem (format: `yyyy-MM-dd`). Nếu không truyền → tháng hiện tại.

### Request Example
```bash
GET http://175.41.150.228:8080/api/dashboard/cash-flow
Authorization: Bearer {token}
```

Hoặc với tháng cụ thể:
```bash
GET http://175.41.150.228:8080/api/dashboard/cash-flow?month=2024-11-01
Authorization: Bearer {token}
```

### Response
```json
{
  "monthlyIncome": 15000000,
  "monthlyExpense": 8800000,
  "netSavings": 6200000,
  "savingsRate": 41.33,
  "currentMonth": "2024-12",
  "totalBalance": 25000000
}
```

### Response Fields
| Field | Type | Description |
|-------|------|-------------|
| `monthlyIncome` | BigDecimal | Tổng thu nhập trong tháng (VND) |
| `monthlyExpense` | BigDecimal | Tổng chi tiêu trong tháng (VND) |
| `netSavings` | BigDecimal | Còn lại = Thu nhập - Chi tiêu |
| `savingsRate` | BigDecimal | Tỷ lệ tiết kiệm (%) |
| `currentMonth` | String | Tháng hiện tại (yyyy-MM) |
| `totalBalance` | BigDecimal | Tổng số dư tất cả tài khoản |

### Frontend Mapping
```javascript
// Thẻ xanh lá - Thu nhập
Thu nhập: response.monthlyIncome / 1000000 + "M"

// Thẻ đỏ - Chi tiêu
Chi tiêu: response.monthlyExpense / 1000000 + "M"

// Thẻ xanh dương - Còn lại
Còn lại: response.netSavings / 1000000 + "M"

// Progress bar - Đã chi
Đã chi: (response.monthlyExpense / response.monthlyIncome) * 100 + "%"
```

---

## 2. Chi Tiêu Theo Danh Mục (Spending By Category)

Dữ liệu cho biểu đồ tròn (pie chart) phân loại chi tiêu theo category.

### Endpoint
```
GET /api/dashboard/spending-by-category?startDate=2024-12-01&endDate=2024-12-31
```

### Parameters
- `startDate` (optional): Ngày bắt đầu (format: `yyyy-MM-dd`). Mặc định: đầu tháng hiện tại
- `endDate` (optional): Ngày kết thúc (format: `yyyy-MM-dd`). Mặc định: cuối tháng hiện tại

### Request Example
```bash
GET http://175.41.150.228:8080/api/dashboard/spending-by-category
Authorization: Bearer {token}
```

Hoặc với khoảng thời gian cụ thể:
```bash
GET http://175.41.150.228:8080/api/dashboard/spending-by-category?startDate=2024-11-01&endDate=2024-11-30
Authorization: Bearer {token}
```

### Response
```json
[
  {
    "categoryId": 1,
    "categoryName": "Ăn uống",
    "categoryIcon": "🍔",
    "categoryColor": "#FF5722",
    "totalAmount": 3500000,
    "transactionCount": 25,
    "percentage": 40.0
  },
  {
    "categoryId": 2,
    "categoryName": "Di chuyển",
    "categoryIcon": "🚗",
    "categoryColor": "#FFA726",
    "totalAmount": 1500000,
    "transactionCount": 12,
    "percentage": 17.0
  },
  {
    "categoryId": 3,
    "categoryName": "Mua sắm",
    "categoryIcon": "🛍️",
    "categoryColor": "#AB47BC",
    "totalAmount": 2000000,
    "transactionCount": 8,
    "percentage": 23.0
  },
  {
    "categoryId": 4,
    "categoryName": "Giải trí",
    "categoryIcon": "🎮",
    "categoryColor": "#EC407A",
    "totalAmount": 800000,
    "transactionCount": 5,
    "percentage": 9.0
  },
  {
    "categoryId": 5,
    "categoryName": "Hóa đơn",
    "categoryIcon": "📄",
    "categoryColor": "#26C6DA",
    "totalAmount": 1000000,
    "transactionCount": 3,
    "percentage": 11.0
  }
]
```

### Response Fields
| Field | Type | Description |
|-------|------|-------------|
| `categoryId` | Long | ID danh mục |
| `categoryName` | String | Tên danh mục |
| `categoryIcon` | String | Icon emoji của danh mục |
| `categoryColor` | String | Màu sắc (hex code) |
| `totalAmount` | BigDecimal | Tổng số tiền chi (VND) |
| `transactionCount` | Long | Số lượng giao dịch |
| `percentage` | Double | Phần trăm trong tổng chi tiêu |

### Frontend Mapping (Pie Chart)
```javascript
// Mảng data cho pie chart
const pieData = response.map(item => ({
  name: item.categoryName,
  value: item.totalAmount,
  percentage: item.percentage,
  color: item.categoryColor,
  icon: item.categoryIcon
}));

// Danh sách dưới pie chart
response.forEach(item => {
  // Hiển thị: [●] Ăn uống    3,500,000 đ
  console.log(`[${item.categoryIcon}] ${item.categoryName}    ${formatMoney(item.totalAmount)} đ`);
});
```

---

## 3. Tổng Quan Dashboard (Summary)

Lấy tất cả thông tin dashboard trong 1 request (cash flow + top categories).

### Endpoint
```
GET /api/dashboard/summary
```

### Request Example
```bash
GET http://175.41.150.228:8080/api/dashboard/summary
Authorization: Bearer {token}
```

### Response
```json
{
  "cashFlow": {
    "monthlyIncome": 15000000,
    "monthlyExpense": 8800000,
    "netSavings": 6200000,
    "savingsRate": 41.33,
    "currentMonth": "2024-12",
    "totalBalance": 25000000
  },
  "topSpendingCategories": [
    {
      "categoryId": 1,
      "categoryName": "Ăn uống",
      "categoryIcon": "🍔",
      "categoryColor": "#FF5722",
      "totalAmount": 3500000,
      "transactionCount": 25,
      "percentage": 40.0
    }
  ],
  "recentTransactions": [],
  "budgetProgress": []
}
```

---

## 4. Xu Hướng Thu Chi Theo Tháng (Monthly Trend)

Dữ liệu cho biểu đồ line chart xu hướng 6 tháng gần nhất.

### Endpoint
```
GET /api/dashboard/monthly-trend?months=6
```

### Parameters
- `months` (optional): Số tháng muốn lấy (mặc định: 6)

### Request Example
```bash
GET http://175.41.150.228:8080/api/dashboard/monthly-trend?months=6
Authorization: Bearer {token}
```

### Response
```json
[
  {
    "month": "2024-07",
    "income": 12000000,
    "expense": 7500000,
    "netSavings": 4500000
  },
  {
    "month": "2024-08",
    "income": 13500000,
    "expense": 8200000,
    "netSavings": 5300000
  },
  {
    "month": "2024-09",
    "income": 14000000,
    "expense": 8500000,
    "netSavings": 5500000
  },
  {
    "month": "2024-10",
    "income": 15000000,
    "expense": 9000000,
    "netSavings": 6000000
  },
  {
    "month": "2024-11",
    "income": 14500000,
    "expense": 8800000,
    "netSavings": 5700000
  },
  {
    "month": "2024-12",
    "income": 15000000,
    "expense": 8800000,
    "netSavings": 6200000
  }
]
```

---

## PowerShell Test Script

```powershell
# 1. Login
$loginResponse = Invoke-RestMethod -Uri "http://175.41.150.228:8080/api/auth/login" `
    -Method POST `
    -ContentType "application/json" `
    -Body '{"username":"testuser","password":"password123"}'

$token = $loginResponse.token
Write-Host "Token: $token"

# 2. Lấy cash flow tháng hiện tại
$cashFlow = Invoke-RestMethod -Uri "http://175.41.150.228:8080/api/dashboard/cash-flow" `
    -Method GET `
    -Headers @{Authorization="Bearer $token"}

Write-Host "`n=== CASH FLOW ==="
Write-Host "Thu nhập: $($cashFlow.monthlyIncome / 1000000)M VND"
Write-Host "Chi tiêu: $($cashFlow.monthlyExpense / 1000000)M VND"
Write-Host "Còn lại: $($cashFlow.netSavings / 1000000)M VND"
Write-Host "Tỷ lệ tiết kiệm: $($cashFlow.savingsRate)%"

# 3. Lấy chi tiêu theo category
$spending = Invoke-RestMethod -Uri "http://175.41.150.228:8080/api/dashboard/spending-by-category" `
    -Method GET `
    -Headers @{Authorization="Bearer $token"}

Write-Host "`n=== CHI TIÊU THEO DANH MỤC ==="
foreach ($item in $spending) {
    Write-Host "$($item.categoryIcon) $($item.categoryName): $($item.totalAmount / 1000000)M VND ($($item.percentage)%)"
}

# 4. Lấy xu hướng 6 tháng
$trend = Invoke-RestMethod -Uri "http://175.41.150.228:8080/api/dashboard/monthly-trend?months=6" `
    -Method GET `
    -Headers @{Authorization="Bearer $token"}

Write-Host "`n=== XU HƯỚNG 6 THÁNG ==="
foreach ($month in $trend) {
    Write-Host "$($month.month): Thu $($month.income / 1000000)M - Chi $($month.expense / 1000000)M = Còn $($month.netSavings / 1000000)M"
}

# 5. Lấy tổng quan dashboard
$summary = Invoke-RestMethod -Uri "http://175.41.150.228:8080/api/dashboard/summary" `
    -Method GET `
    -Headers @{Authorization="Bearer $token"}

Write-Host "`n=== DASHBOARD SUMMARY ==="
Write-Host ($summary | ConvertTo-Json -Depth 5)
```

---

## JavaScript/Flutter Example

### JavaScript (React/Vue)
```javascript
const API_BASE = 'http://175.41.150.228:8080';

// 1. Login
async function login(username, password) {
  const response = await fetch(`${API_BASE}/api/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password })
  });
  const data = await response.json();
  localStorage.setItem('token', data.token);
  return data.token;
}

// 2. Get Cash Flow
async function getCashFlow(month = null) {
  const token = localStorage.getItem('token');
  const url = month 
    ? `${API_BASE}/api/dashboard/cash-flow?month=${month}`
    : `${API_BASE}/api/dashboard/cash-flow`;
    
  const response = await fetch(url, {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  return await response.json();
}

// 3. Get Spending By Category
async function getSpendingByCategory(startDate = null, endDate = null) {
  const token = localStorage.getItem('token');
  let url = `${API_BASE}/api/dashboard/spending-by-category`;
  
  if (startDate && endDate) {
    url += `?startDate=${startDate}&endDate=${endDate}`;
  }
  
  const response = await fetch(url, {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  return await response.json();
}

// Usage
const cashFlow = await getCashFlow();
console.log(`Thu nhập: ${cashFlow.monthlyIncome / 1000000}M`);
console.log(`Chi tiêu: ${cashFlow.monthlyExpense / 1000000}M`);
console.log(`Còn lại: ${cashFlow.netSavings / 1000000}M`);

const spending = await getSpendingByCategory();
spending.forEach(item => {
  console.log(`${item.categoryIcon} ${item.categoryName}: ${item.percentage}% (${item.totalAmount.toLocaleString()} đ)`);
});
```

### Flutter (Dart)
```dart
import 'package:http/http.dart' as http;
import 'dart:convert';

const API_BASE = 'http://175.41.150.228:8080';

class DashboardService {
  String? token;
  
  Future<Map<String, dynamic>> login(String username, String password) async {
    final response = await http.post(
      Uri.parse('$API_BASE/api/auth/login'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({'username': username, 'password': password}),
    );
    
    final data = jsonDecode(response.body);
    token = data['token'];
    return data;
  }
  
  Future<Map<String, dynamic>> getCashFlow({String? month}) async {
    final url = month != null
        ? '$API_BASE/api/dashboard/cash-flow?month=$month'
        : '$API_BASE/api/dashboard/cash-flow';
        
    final response = await http.get(
      Uri.parse(url),
      headers: {'Authorization': 'Bearer $token'},
    );
    
    return jsonDecode(response.body);
  }
  
  Future<List<dynamic>> getSpendingByCategory({String? startDate, String? endDate}) async {
    var url = '$API_BASE/api/dashboard/spending-by-category';
    
    if (startDate != null && endDate != null) {
      url += '?startDate=$startDate&endDate=$endDate';
    }
    
    final response = await http.get(
      Uri.parse(url),
      headers: {'Authorization': 'Bearer $token'},
    );
    
    return jsonDecode(response.body);
  }
}

// Usage
final service = DashboardService();
await service.login('testuser', 'password123');

final cashFlow = await service.getCashFlow();
print('Thu nhập: ${cashFlow['monthlyIncome'] / 1000000}M');
print('Chi tiêu: ${cashFlow['monthlyExpense'] / 1000000}M');
print('Còn lại: ${cashFlow['netSavings'] / 1000000}M');

final spending = await service.getSpendingByCategory();
for (var item in spending) {
  print('${item['categoryIcon']} ${item['categoryName']}: ${item['percentage']}%');
}
```

---

## Lưu Ý Quan Trọng

1. **Authentication**: Tất cả endpoints yêu cầu JWT token trong header `Authorization: Bearer {token}`

2. **Date Format**: Sử dụng ISO date format `yyyy-MM-dd` (VD: `2024-12-14`)

3. **Default Values**: 
   - Nếu không truyền `month` cho cash-flow → tháng hiện tại
   - Nếu không truyền `startDate/endDate` cho spending-by-category → tháng hiện tại

4. **Money Format**: 
   - Backend trả về số nguyên (VD: `15000000` = 15 triệu VND)
   - Frontend cần format: `15000000 / 1000000 = 15M` hoặc `15.0M`

5. **Percentage Calculation**:
   - `percentage` đã được tính sẵn từ backend (0-100)
   - Ví dụ: `40.0` = 40%

6. **Color Codes**: Backend trả về hex color (VD: `#FF5722`) để frontend dùng cho pie chart

7. **Empty Data**: Nếu không có giao dịch → API trả về array rỗng `[]` hoặc giá trị `0`

---

## Test Data

Backend có seed data mẫu. Sau khi login với user test, có thể thấy:
- Thu nhập: ~15M VND
- Chi tiêu: ~8.8M VND
- Các categories: Ăn uống, Di chuyển, Mua sắm, Giải trí, Hóa đơn

Để test với data thật, cần:
1. Tạo user mới
2. Thêm transactions qua API `/api/transactions` hoặc parse SMS
3. Gọi dashboard APIs để xem kết quả
