# API Documentation - SMS Parser Management

## Base URL
```
http://175.41.150.228:8080/api/sms/parsers
```

## Authentication
Tất cả các endpoints yêu cầu quyền ADMIN. Cần login và lấy JWT token trước.

### 1. Login (Get JWT Token)
```bash
POST http://175.41.150.228:8080/api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "your_password"
}
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "admin"
}
```

## Endpoints

### 2. Lấy danh sách tất cả parsers
```bash
GET /api/sms/parsers
Authorization: Bearer {token}
```

Response:
```json
[
  {
    "id": 1,
    "bankName": "Vietcombank",
    "bankCode": "VCB",
    "senderNumber": "VIETCOMBANK",
    "regexPattern": "SD\\s+TK\\s+([\\dx]+)\\s+([+-])([\\d,]+)VND...",
    "fieldMappings": "{\"account\":\"1\",\"type\":\"2\",\"amount\":\"3\",\"datetime\":\"4\",\"merchant\":\"5\"}",
    "sampleSms": "SD TK 012xxxx896 -440,000VND luc 07-04-2023 10:40:40...",
    "isActive": true,
    "priority": 0,
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  }
]
```

### 3. Lấy parser theo ID
```bash
GET /api/sms/parsers/{id}
Authorization: Bearer {token}
```

### 4. Tạo parser mới
```bash
POST /api/sms/parsers
Authorization: Bearer {token}
Content-Type: application/json

{
  "bankName": "ACB",
  "bankCode": "ACB",
  "senderNumber": "ACB",
  "regexPattern": "TK ([\\dx]+) GD: ([+-])([\\d,]+)VND (\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}) (.+)",
  "fieldMappings": "{\"account\":\"1\",\"type\":\"2\",\"amount\":\"3\",\"datetime\":\"4\",\"merchant\":\"5\"}",
  "sampleSms": "TK 123456789 GD: +1,000,000VND 14/12/2024 10:30 Nap tien",
  "isActive": true,
  "priority": 0
}
```

Response:
```json
{
  "id": 6,
  "bankName": "ACB",
  "bankCode": "ACB",
  ...
}
```

### 5. Cập nhật parser
```bash
PUT /api/sms/parsers/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "bankName": "ACB - Asia Commercial Bank",
  "isActive": false
}
```

### 6. Xóa parser
```bash
DELETE /api/sms/parsers/{id}
Authorization: Bearer {token}
```

Response: `204 No Content`

### 7. Test regex pattern (QUAN TRỌNG!)
Test pattern trước khi lưu để đảm bảo regex khớp đúng:

```bash
POST /api/sms/parsers/test
Authorization: Bearer {token}
Content-Type: application/json

{
  "regexPattern": "TK ([\\dx]+) GD: ([+-])([\\d,]+)VND (\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}) (.+)",
  "smsContent": "TK 123xxx789 GD: +1,000,000VND 14/12/2024 10:30 Nap tien ATM",
  "fieldMappings": "{\"account\":\"1\",\"type\":\"2\",\"amount\":\"3\",\"datetime\":\"4\",\"merchant\":\"5\"}"
}
```

Response (Success):
```json
{
  "matched": true,
  "message": "Pattern khớp thành công!",
  "extractedFields": {
    "account": "123xxx789",
    "type": "+",
    "amount": "1,000,000",
    "datetime": "14/12/2024 10:30",
    "merchant": "Nap tien ATM"
  }
}
```

Response (Failed):
```json
{
  "matched": false,
  "message": "SMS không khớp với regex pattern"
}
```

## Field Mappings Format
`fieldMappings` là JSON string mapping tên field → group index trong regex:

```json
{
  "account": "1",      // Group 1: Số tài khoản
  "type": "2",         // Group 2: +/- (thu/chi)
  "amount": "3",       // Group 3: Số tiền
  "datetime": "4",     // Group 4: Datetime (hoặc dùng "time" + "date")
  "merchant": "5"      // Group 5: Mô tả giao dịch
}
```

### Datetime vs Time + Date
- **Cách 1**: Dùng `"datetime"` nếu thời gian trong 1 group (VD: `07-04-2023 10:40:40`)
- **Cách 2**: Dùng `"time"` + `"date"` nếu thời gian tách rời (VD: time=`22:39`, date=`22/03/2023`)

## Lưu ý quan trọng

1. **Regex Pattern**:
   - Sử dụng `[\dx]+` cho số tài khoản (hỗ trợ masking `xxx`)
   - Pattern tự động compile với `Pattern.DOTALL` (`.` match cả `\n`)
   - Test kỹ bằng endpoint `/test` trước khi lưu

2. **Field Mappings**:
   - Phải là JSON string hợp lệ
   - Group index bắt đầu từ 1 (không phải 0)
   - Required fields: `account`, `type`, `amount`, (`datetime` hoặc `time`+`date`), `merchant`

3. **Security**:
   - Chỉ ADMIN mới có quyền CRUD parsers
   - Token JWT có thời hạn, cần refresh khi hết hạn

4. **Sample SMS**:
   - Nên lưu SMS mẫu thực tế từ ngân hàng
   - Giúp test và debug sau này

## Test Flow
1. Login → Lấy token
2. POST `/test` → Test regex với SMS mẫu
3. Kiểm tra `extractedFields` có đúng không
4. Nếu OK → POST `/api/sms/parsers` → Tạo parser mới
5. Test thực tế: POST `/api/sms/receive` với SMS thật

## PowerShell Test Script Example

```powershell
# 1. Login
$loginResponse = Invoke-RestMethod -Uri "http://175.41.150.228:8080/api/auth/login" `
    -Method POST `
    -ContentType "application/json" `
    -Body '{"username":"admin","password":"your_password"}'

$token = $loginResponse.token

# 2. Test regex
$testBody = @{
    regexPattern = "TK ([\\dx]+) GD: ([+-])([\\d,]+)VND (\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}) (.+)"
    smsContent = "TK 123xxx789 GD: +1,000,000VND 14/12/2024 10:30 Nap tien ATM"
    fieldMappings = '{"account":"1","type":"2","amount":"3","datetime":"4","merchant":"5"}'
} | ConvertTo-Json

$testResult = Invoke-RestMethod -Uri "http://175.41.150.228:8080/api/sms/parsers/test" `
    -Method POST `
    -Headers @{Authorization="Bearer $token"} `
    -ContentType "application/json" `
    -Body $testBody

Write-Host "Test Result: $($testResult.matched)"
Write-Host "Extracted Fields: $($testResult.extractedFields | ConvertTo-Json)"

# 3. Tạo parser mới (nếu test OK)
if ($testResult.matched) {
    $createBody = @{
        bankName = "ACB"
        bankCode = "ACB"
        senderNumber = "ACB"
        regexPattern = "TK ([\\dx]+) GD: ([+-])([\\d,]+)VND (\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}) (.+)"
        fieldMappings = '{"account":"1","type":"2","amount":"3","datetime":"4","merchant":"5"}'
        sampleSms = "TK 123xxx789 GD: +1,000,000VND 14/12/2024 10:30 Nap tien ATM"
        isActive = $true
        priority = 0
    } | ConvertTo-Json

    $createResult = Invoke-RestMethod -Uri "http://175.41.150.228:8080/api/sms/parsers" `
        -Method POST `
        -Headers @{Authorization="Bearer $token"} `
        -ContentType "application/json" `
        -Body $createBody

    Write-Host "Parser created with ID: $($createResult.id)"
}
```
