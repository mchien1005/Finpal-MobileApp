# Kiến trúc Ứng dụng Finpal - Clean Architecture

## 🛠️ Tech Stack

## 📁 Chi tiết Cấu trúc & Chức năng

### 1️⃣ **CORE LAYER** (`lib/core/`)

Chứa các thành phần dùng chung cho toàn bộ ứng dụng

#### 📂 `core/constants/`

**Chức năng**: Định nghĩa các hằng số, màu sắc, cấu hình

##### `app_colors.dart`

```dart
// Định nghĩa bảng màu của ứng dụng
// - Màu chủ đạo (primary, secondary)
// - Màu theo danh mục (Ăn uống, Di chuyển, Mua sắm...)
// - Màu trạng thái (success, warning, error)
```

**Sử dụng cho**: Tất cả các module (Dashboard, Smart Scan, AI Coach)

##### `app_constants.dart`

```dart
// Các hằng số cấu hình
// - API endpoints (base URL, auth, transactions...)
// - Định dạng ngày tháng
// - Giới hạn phân trang
// - Regex patterns cho SMS parsing
```

**Sử dụng cho**:

- **FR1.2**: Regex patterns để parse SMS
- **FR2.3**: Pagination cho lịch sử giao dịch
- API configuration cho tất cả modules

---

#### 📂 `core/theme/`

**Chức năng**: Quản lý giao diện Material Design

##### `app_theme.dart`

```dart
// Theme configuration
// - Material Design 3 theme
// - Typography (fonts, sizes)
// - Component styling (buttons, cards, inputs)
```

**Sử dụng cho**:

- **FR2.1, FR2.2**: Styling cho Dashboard
- **FR3.1**: Styling cho thông báo AI
- Tất cả các màn hình UI

---

#### 📂 `core/utils/`

**Chức năng**: Các hàm tiện ích dùng chung

##### `validators.dart`

```dart
// Validation logic
// - Email validator
// - Phone number validator
// - Amount validator (số tiền)
// - Password strength checker
```

**Sử dụng cho**:

- **FR1.4**: Validate input khi ghi nhận thủ công
- Form validation cho Login/Register

##### `formatters.dart`

```dart
// Format dữ liệu hiển thị
// - Currency formatter (VND, USD...)
// - Date/Time formatter
// - Number formatter (phần trăm, decimal...)
```

**Sử dụng cho**:

- **FR2.1**: Format số tiền (thu nhập, chi tiêu)
- **FR2.2**: Format % trong biểu đồ
- **FR3.1**: Format số tiền trong thông báo

---

#### 📂 `core/widgets/`

**Chức năng**: Các widget tái sử dụng

##### `custom_button.dart`

```dart
// Custom button component
// - Primary button
// - Outline button
// - Loading state
```

**Sử dụng cho**: Tất cả các form, action buttons

##### `custom_text_field.dart`

```dart
// Custom input field
// - Validation support
// - Error message display
// - Icon support (prefix/suffix)
```

**Sử dụng cho**:

- **FR1.4**: Form nhập giao dịch thủ công
- Login/Register forms

---

### 2️⃣ **DATA LAYER** (`lib/data/`)

Quản lý dữ liệu, API calls, business logic

#### 📂 `data/models/`

**Chức năng**: Định nghĩa data models (entities)

##### `user.dart`

```dart
// User model
class User {
  String id;
  String username;
  String email;
  String? fullName;
  DateTime createdAt;

  // Serialization methods
  factory User.fromJson(Map<String, dynamic> json);
  Map<String, dynamic> toJson();
}
```

**Sử dụng cho**: Authentication, Profile management

##### `transaction.dart` ⭐ **QUAN TRỌNG**

```dart
// Transaction model
class Transaction {
  String id;
  double amount;
  String type; // "income" | "expense"
  String categoryId;
  String? categoryName; // "Ăn uống", "Di chuyển"...
  String? description;
  String? bankCode; // "VCB", "TCB", "ACB"
  String source; // "sms" | "manual" | "bank_api"
  DateTime transactionDate;
  String? smsContent; // Raw SMS content

  // AI classification data
  double? aiConfidence; // 0.0 - 1.0
  bool isVerified; // User confirmed or not

  factory Transaction.fromJson(Map<String, dynamic> json);
  factory Transaction.fromSMS(Map<String, dynamic> parsedData);
}
```

**Sử dụng cho**:

- **FR1.2**: Parse từ SMS → Transaction object
- **FR1.3**: Lưu kết quả AI classification
- **FR1.4**: Giao dịch thủ công
- **FR2.3**: Hiển thị lịch sử

##### `budget.dart`

```dart
// Budget model
class Budget {
  String id;
  String categoryId;
  String categoryName;
  double limitAmount; // Hạn mức
  double spentAmount; // Đã chi
  double percentage; // % đã sử dụng
  String period; // "monthly" | "weekly"
  DateTime startDate;
  DateTime endDate;
}
```

**Sử dụng cho**:

- **FR2.1**: Hiển thị tổng quan
- **FR3.1**: Cảnh báo vượt ngưỡng (70%, 90%, 100%)

##### `category.dart`

```dart
// Category model
class Category {
  String id;
  String name; // "Ăn uống", "Di chuyển", "Mua sắm"
  String icon; // Icon code
  String color; // Hex color
  String type; // "expense" | "income"

  // AI keywords mapping
  List<String> keywords; // ["GRAB", "GOJEK"] cho "Di chuyển"
}
```

**Sử dụng cho**:

- **FR1.3**: AI category classification
- **FR2.2**: Hiển thị biểu đồ phân loại
- **FR1.4**: Chọn danh mục khi nhập thủ công

##### `account.dart`

```dart
// Bank Account model
class Account {
  String id;
  String accountNumber;
  String bankCode; // "VCB", "TCB"
  String bankName;
  double balance;
  bool isDefault;
  bool smsEnabled; // Có quét SMS không
}
```

**Sử dụng cho**:

- **FR1.1**: Quản lý tài khoản ngân hàng
- **FR2.1**: Hiển thị số dư

---

#### 📂 `data/repositories/`

**Chức năng**: Interface cho data access (Repository Pattern)

##### `auth_repository.dart`

```dart
// Authentication repository
abstract class AuthRepository {
  Future<User> login(String username, String password);
  Future<User> register(RegisterRequest request);
  Future<void> logout();
  Future<User?> getCurrentUser();
}

class AuthRepositoryImpl implements AuthRepository {
  final AuthService _authService;
  final StorageService _storage;

  // Implementation với error handling
}
```

**Sử dụng cho**: Login, Register, Token management

##### `transaction_repository.dart` ⭐ **CORE REPOSITORY**

```dart
// Transaction repository
abstract class TransactionRepository {
  // FR1.2 - Parse SMS
  Future<Transaction> parseFromSMS(String smsContent);

  // FR1.3 - AI Classification
  Future<String> classifyCategory(String description);

  // FR1.4 - Manual entry
  Future<Transaction> createManual(TransactionRequest request);

  // FR2.3 - History
  Future<List<Transaction>> getTransactions({
    DateTime? startDate,
    DateTime? endDate,
    String? categoryId,
    int page = 1,
  });

  // Update transaction (fix AI classification)
  Future<Transaction> updateTransaction(String id, UpdateRequest request);

  // Delete
  Future<void> deleteTransaction(String id);

  // FR2.1 - Statistics
  Future<TransactionSummary> getSummary(DateTime month);
}
```

**Sử dụng cho**: Tất cả chức năng liên quan đến giao dịch

##### `budget_repository.dart`

```dart
// Budget repository
abstract class BudgetRepository {
  Future<List<Budget>> getBudgets(DateTime month);
  Future<Budget> createBudget(BudgetRequest request);
  Future<Budget> updateBudget(String id, BudgetRequest request);
  Future<void> deleteBudget(String id);

  // FR3.1 - Check thresholds
  Future<List<BudgetAlert>> checkAlerts();
}
```

**Sử dụng cho**:

- **FR2.1**: Hiển thị ngân sách
- **FR3.1**: Cảnh báo vượt hạn mức

##### `category_repository.dart`

```dart
// Category repository
abstract class CategoryRepository {
  Future<List<Category>> getCategories();
  Future<Category> createCategory(CategoryRequest request);

  // FR1.3 - AI mapping
  Future<String> predictCategory(String keywords);
}
```

**Sử dụng cho**:

- **FR1.3**: AI classification
- Quản lý danh mục

---

#### 📂 `data/services/`

**Chức năng**: Implement các service thực tế (API, Storage...)

##### `api_service.dart`

```dart
// HTTP API service using Dio
class ApiService {
  final Dio _dio;

  // Generic methods
  Future<T> get<T>(String endpoint);
  Future<T> post<T>(String endpoint, Map<String, dynamic> data);
  Future<T> put<T>(String endpoint, Map<String, dynamic> data);
  Future<void> delete(String endpoint);

  // Interceptors for token, logging, error handling
}
```

**Sử dụng cho**: Tất cả API calls đến backend

##### `auth_service.dart`

```dart
// Authentication service
class AuthService {
  final ApiService _api;

  Future<LoginResponse> login(String username, String password);
  Future<RegisterResponse> register(RegisterRequest request);
  Future<void> refreshToken();
}
```

##### `storage_service.dart`

```dart
// Local storage service (SharedPreferences)
class StorageService {
  // Token storage
  Future<void> saveToken(String token);
  Future<String?> getToken();
  Future<void> clearToken();

  // User preferences
  Future<void> saveBool(String key, bool value);
  Future<bool?> getBool(String key);

  // FR1.1 - SMS permission
  Future<void> saveSmsPermission(bool granted);
}
```

**Sử dụng cho**:

- Token persistence
- User settings
- Offline data caching

---

### 3️⃣ **PRESENTATION LAYER** (`lib/presentation/`)

UI components, screens, state management

#### 📂 `presentation/screens/`

**Chức năng**: Các màn hình chính của ứng dụng

---

##### 📂 `screens/auth/`

###### `login_screen.dart`

```dart
// Màn hình đăng nhập
// - Username/Email input
// - Password input
// - Remember me checkbox
// - Forgot password link
// - Social login (optional)
```

###### `register_screen.dart`

```dart
// Màn hình đăng ký
// - Full name, username, email, password
// - Terms & conditions checkbox
// - Validation realtime
```

---

##### 📂 `screens/home/`

###### `dashboard_screen.dart` ⭐ **FR2: Dashboard**

```dart
// MAIN DASHBOARD - Màn hình chính
// Components:
// 1. Header: Tên user, avatar, notification icon
// 2. Summary Cards (FR2.1):
//    - Tổng Thu nhập tháng
//    - Tổng Chi tiêu tháng
//    - Còn lại (Income - Expense)
// 3. Pie Chart (FR2.2):
//    - Biểu đồ phân loại chi tiêu
//    - Legend với % và số tiền
// 4. Recent Transactions (FR2.3):
//    - 5 giao dịch gần nhất
//    - "Xem tất cả" button
// 5. Quick Actions:
//    - Thêm giao dịch thủ công (FR1.4)
//    - Scan SMS (FR1.1)
// 6. AI Insights Card (FR3):
//    - Hiển thị gợi ý/cảnh báo từ AI
```

**Widgets sử dụng**: `DashboardCard`, `TransactionItem`
**Providers**: `TransactionProvider`, `BudgetProvider`

---

##### 📂 `screens/transactions/`

###### `transactions_screen.dart` ⭐ **FR2.3: Lịch sử**

```dart
// Màn hình lịch sử giao dịch
// Features:
// 1. Tab filters: Tất cả | Thu nhập | Chi tiêu
// 2. Date range picker
// 3. Category filter dropdown
// 4. Search bar (tìm theo mô tả)
// 5. List view với infinite scroll:
//    - TransactionItem widget
//    - Swipe actions: Edit | Delete
//    - Tap to view detail
// 6. FAB: Add transaction (FR1.4)
// 7. Sort options: Mới nhất | Cũ nhất | Số tiền
```

**Widgets**: `TransactionItem`, `FilterChip`
**Providers**: `TransactionProvider`

###### `add_transaction_screen.dart` ⭐ **FR1.4: Nhập thủ công**

```dart
// Màn hình thêm/sửa giao dịch
// Form fields:
// 1. Type toggle: Thu nhập / Chi tiêu
// 2. Amount input (number keyboard)
// 3. Category selector (grid/dropdown)
//    - Icon + Color cho mỗi category
// 4. Description/Note (optional)
// 5. Date picker (default: hôm nay)
// 6. Time picker
// 7. Account selector (nếu có nhiều TK)
// 8. Attach photo (optional - hóa đơn)
//
// Actions:
// - Save button
// - Cancel button
// - Validation: amount > 0, category required
```

**Providers**: `TransactionProvider`, `CategoryRepository`

---

##### 📂 `screens/budget/`

###### `budget_screen.dart` ⭐ **FR2.1 + FR3.1: Ngân sách**

```dart
// Màn hình quản lý ngân sách
// Sections:
// 1. Overview Card:
//    - Tổng ngân sách tháng
//    - Đã chi
//    - Còn lại
// 2. Budget List (by category):
//    - Progress bar với color coding:
//      * Green: < 70%
//      * Yellow: 70-90%
//      * Red: > 90%
//    - Spent / Limit
//    - Edit/Delete actions
// 3. Add Budget FAB
// 4. AI Alerts Section (FR3.1):
//    - "Bạn đã chi 70% hạn mức Ăn uống"
//    - "Cảnh báo: vượt ngân sách Di chuyển"
```

**Widgets**: `BudgetProgressCard`
**Providers**: `BudgetProvider`

---

##### 📂 `screens/profile/`

###### `profile_screen.dart`

```dart
// Màn hình cá nhân
// Sections:
// 1. User info card
// 2. Menu items:
//    - Savings Goals (FR3.3)
//    - Quản lý tài khoản ngân hàng (FR1.1)
//    - SMS Settings (FR1.1 - Bật/tắt quét SMS)
//    - Quản lý danh mục
//    - Thông báo
//    - Cài đặt
//    - Về ứng dụng
//    - Đăng xuất
```

---

##### 📂 `screens/savings/`

###### `savings_goals_screen.dart` ⭐ **FR3.3: Hũ tiết kiệm**

```dart
// Màn hình mục tiêu tiết kiệm
// Features:
// 1. List of goals:
//    - Goal name + icon
//    - Target amount
//    - Current saved
//    - Progress bar
//    - Deadline
// 2. Add Goal:
//    - Name (e.g., "Mua tai nghe")
//    - Target amount (3.000.000đ)
//    - Deadline
//    - AI suggestion: "Bạn cần tiết kiệm 100.000đ/tuần"
// 3. Auto-save setup:
//    - Tự động trích % từ thu nhập
//    - Weekly/Monthly contribution
```

**Providers**: `SavingsProvider` (cần tạo thêm)

---

#### 📂 `presentation/widgets/`

**Chức năng**: Reusable UI components

##### `dashboard_card.dart`

```dart
// Card component cho dashboard
// Props:
// - title: String
// - amount: double
// - icon: IconData
// - gradient: Gradient
// - trend: double? (% tăng/giảm so với tháng trước)
//
// Sử dụng cho:
// - Thu nhập card
// - Chi tiêu card
// - Số dư card
```

**Dùng trong**: `dashboard_screen.dart` (FR2.1)

##### `transaction_item.dart`

```dart
// List item cho giao dịch
// Props:
// - transaction: Transaction
// - onTap: Function
// - onEdit: Function?
// - onDelete: Function?
//
// Display:
// - Category icon + color
// - Description
// - Date & time
// - Amount (+ green / - red)
// - AI confidence badge (nếu từ SMS)
```

**Dùng trong**:

- `dashboard_screen.dart` (recent transactions)
- `transactions_screen.dart` (history list)

##### `budget_progress_card.dart`

```dart
// Progress card cho ngân sách
// Props:
// - categoryName: String
// - spent: double
// - limit: double
// - color: Color
//
// Display:
// - Category icon
// - Progress bar (color based on %)
// - Spent / Limit
// - Warning icon nếu > 90%
```

**Dùng trong**: `budget_screen.dart` (FR2.1, FR3.1)

---

#### 📂 `presentation/providers/`

**Chức năng**: State management với Provider pattern

##### `auth_provider.dart`

```dart
// Authentication state management
class AuthProvider extends ChangeNotifier {
  User? _currentUser;
  bool _isAuthenticated = false;
  bool _isLoading = false;

  // Getters
  User? get currentUser => _currentUser;
  bool get isAuthenticated => _isAuthenticated;

  // Actions
  Future<void> login(String username, String password);
  Future<void> register(RegisterRequest request);
  Future<void> logout();
  Future<void> checkAuthStatus();
}
```

**Sử dụng**: Toàn bộ app (auth guard, user info)

##### `transaction_provider.dart` ⭐ **CORE PROVIDER**

```dart
// Transaction state management
class TransactionProvider extends ChangeNotifier {
  List<Transaction> _transactions = [];
  TransactionSummary? _summary;
  bool _isLoading = false;
  String? _error;

  // Getters
  List<Transaction> get transactions => _transactions;
  TransactionSummary? get summary => _summary;

  // FR1.1 + FR1.2 + FR1.3 - SMS Processing
  Future<void> processSMS(String smsContent) async {
    // 1. Parse SMS
    final parsedData = await _repo.parseFromSMS(smsContent);

    // 2. AI Classification
    final category = await _repo.classifyCategory(parsedData['content']);

    // 3. Create transaction
    final transaction = Transaction.fromSMS({
      ...parsedData,
      'categoryId': category,
      'source': 'sms',
    });

    // 4. Save to DB
    await _repo.createTransaction(transaction);

    // 5. Update UI
    _transactions.insert(0, transaction);
    notifyListeners();
  }

  // FR1.4 - Manual entry
  Future<void> addManualTransaction(TransactionRequest request);

  // FR2.3 - Load history
  Future<void> loadTransactions({filters});

  // FR2.1 - Summary
  Future<void> loadSummary(DateTime month);

  // Update/Delete
  Future<void> updateTransaction(String id, UpdateRequest request);
  Future<void> deleteTransaction(String id);
}
```

**Sử dụng**:

- `dashboard_screen.dart`
- `transactions_screen.dart`
- `add_transaction_screen.dart`

##### `budget_provider.dart`

```dart
// Budget state management
class BudgetProvider extends ChangeNotifier {
  List<Budget> _budgets = [];
  List<BudgetAlert> _alerts = [];

  // FR2.1 - Load budgets
  Future<void> loadBudgets(DateTime month);

  // FR3.1 - Check alerts
  Future<void> checkAlerts() async {
    _alerts = await _repo.checkAlerts();

    // Trigger notifications
    for (var alert in _alerts) {
      if (alert.threshold >= 0.7) {
        _showNotification(alert);
      }
    }

    notifyListeners();
  }

  // CRUD
  Future<void> createBudget(BudgetRequest request);
  Future<void> updateBudget(String id, BudgetRequest request);
  Future<void> deleteBudget(String id);
}
```

**Sử dụng**: `budget_screen.dart`, `dashboard_screen.dart`

---

## 🔄 Flow Diagram - Các Features chính

### **FR1.1 + FR1.2 + FR1.3: Smart SMS Scan**

```
┌─────────────────┐
│   SMS arrives   │
│  (Android OS)   │
└────────┬────────┘
         │
         ▼
┌─────────────────────────┐
│  SMS Receiver Service   │ ← (Android Native - cần thêm)
│  (Background Service)   │
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│  TransactionProvider    │
│  .processSMS()          │
└────────┬────────────────┘
         │
         ├─────► TransactionRepository.parseFromSMS()
         │       └─► Regex/AI parsing → JSON
         │
         ├─────► TransactionRepository.classifyCategory()
         │       └─► AI/ML model → Category ID
         │
         └─────► Save to Database
                 └─► UI update (notifyListeners)
```

**Files liên quan**:

- `data/services/sms_service.dart` ← **Cần tạo thêm** (Android platform channel)
- `data/repositories/transaction_repository.dart`
- `presentation/providers/transaction_provider.dart`
- `data/models/transaction.dart`

---

### **FR2.1 + FR2.2: Dashboard**

```
┌──────────────────┐
│ dashboard_screen │
│   .dart          │
└────────┬─────────┘
         │
         ▼
┌──────────────────────────┐
│ TransactionProvider      │
│ .loadSummary(thisMonth)  │
└────────┬─────────────────┘
         │
         ▼
┌──────────────────────────┐
│ TransactionRepository    │
│ .getSummary()            │
└────────┬─────────────────┘
         │
         ▼
┌──────────────────────────┐
│ API: GET /dashboard      │
│ Response:                │
│ {                        │
│   totalIncome: 15000000  │
│   totalExpense: 8500000  │
│   balance: 6500000       │
│   categoryBreakdown: [   │
│     {category: "Ăn uống",│
│      amount: 3400000,    │
│      percentage: 40}     │
│   ]                      │
│ }                        │
└──────────────────────────┘
```

**Files liên quan**:

- `presentation/screens/home/dashboard_screen.dart`
- `presentation/widgets/dashboard_card.dart`
- `presentation/providers/transaction_provider.dart`
- Chart library: `fl_chart` package

---

### **FR3.1: AI Alerts**

```
┌──────────────────┐
│ Cron Job         │
│ (Every hour)     │
└────────┬─────────┘
         │
         ▼
┌──────────────────────────┐
│ BudgetProvider           │
│ .checkAlerts()           │
└────────┬─────────────────┘
         │
         ▼
┌──────────────────────────┐
│ BudgetRepository         │
│ .checkAlerts()           │
│                          │
│ Logic:                   │
│ - Lấy budgets của tháng  │
│ - Tính spent %           │
│ - Nếu >= 70%:            │
│   → Tạo BudgetAlert      │
└────────┬─────────────────┘
         │
         ▼
┌──────────────────────────┐
│ Notification Service     │ ← **Cần tạo thêm**
│ .showNotification()      │
│                          │
│ "Bạn đã chi 70% hạn mức  │
│  'Ăn uống' của tháng"    │
└──────────────────────────┘
```

**Files cần tạo thêm**:

- `data/services/notification_service.dart`
- `data/models/budget_alert.dart`
- Background job service (WorkManager for Android)

---

## 🎯 Mapping Features → Files

| Feature                 | Files chính                                             | Models                                   | Providers                                           | Repositories                    |
| ----------------------- | ------------------------------------------------------- | ---------------------------------------- | --------------------------------------------------- | ------------------------------- |
| **FR1.1: SMS Scan**     | `sms_service.dart` (new)                                | `transaction.dart`                       | `transaction_provider.dart`                         | `transaction_repository.dart`   |
| **FR1.2: Parse SMS**    | `transaction_repository.dart`                           | `transaction.dart`                       | `transaction_provider.dart`                         | -                               |
| **FR1.3: AI Classify**  | `transaction_repository.dart`                           | `category.dart`, `transaction.dart`      | `transaction_provider.dart`                         | `category_repository.dart`      |
| **FR1.4: Manual Entry** | `add_transaction_screen.dart`                           | `transaction.dart`                       | `transaction_provider.dart`                         | `transaction_repository.dart`   |
| **FR2.1: Dashboard**    | `dashboard_screen.dart`                                 | `transaction.dart`, `budget.dart`        | `transaction_provider.dart`, `budget_provider.dart` | `transaction_repository.dart`   |
| **FR2.2: Pie Chart**    | `dashboard_screen.dart`                                 | `category.dart`                          | `transaction_provider.dart`                         | -                               |
| **FR2.3: History**      | `transactions_screen.dart`                              | `transaction.dart`                       | `transaction_provider.dart`                         | `transaction_repository.dart`   |
| **FR3.1: AI Alerts**    | `budget_screen.dart`, `notification_service.dart` (new) | `budget.dart`, `budget_alert.dart` (new) | `budget_provider.dart`                              | `budget_repository.dart`        |
| **FR3.2: Anomaly**      | AI service (backend)                                    | -                                        | -                                                   | -                               |
| **FR3.3: Savings**      | `savings_goals_screen.dart`                             | `savings_goal.dart` (new)                | `savings_provider.dart` (new)                       | `savings_repository.dart` (new) |

---

## 📝 Files cần TẠO THÊM

### Data Layer

1. `data/services/sms_service.dart` - SMS reading (Android)
2. `data/services/notification_service.dart` - Push notifications
3. `data/models/budget_alert.dart` - Alert model
4. `data/models/savings_goal.dart` - Savings goal model
5. `data/repositories/savings_repository.dart` - Savings CRUD

### Presentation Layer

6. `presentation/providers/savings_provider.dart` - Savings state
7. `presentation/screens/notifications/notifications_screen.dart` - Notification list

### Platform-specific

8. `android/app/src/main/kotlin/.../SmsReceiver.kt` - SMS broadcast receiver
9. `android/app/src/main/kotlin/.../SmsParser.kt` - Native SMS parsing (optional)

---

## 🔐 Security Notes

**Permissions cần khai báo** (`android/app/src/main/AndroidManifest.xml`):

```xml
<uses-permission android:name="android.permission.READ_SMS" />
<uses-permission android:name="android.permission.RECEIVE_SMS" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
```

**iOS**: Không hỗ trợ đọc SMS do policy của Apple
→ Cần giải pháp thay thế: Manual import, Bank API integration

---

## 📦 Packages cần thêm vào `pubspec.yaml`

```yaml
dependencies:
  # Charts
  fl_chart: ^0.66.0

  # Notifications
  flutter_local_notifications: ^17.0.0

  # Background tasks
  workmanager: ^0.5.2

  # SMS (Android only)
  telephony: ^0.2.0

  # Permissions
  permission_handler: ^11.0.0

  # ML/AI (if on-device)
  tflite_flutter: ^0.10.0
```

---

## 🎨 UI/UX Flow

### Main Navigation (Bottom Tab Bar)

```
┌─────────┬─────────┬─────────┬─────────┐
│Dashboard│Giao dịch│Ngân sách│Cá nhân  │
│   🏠    │   📊    │   💰    │   👤    │
└─────────┴─────────┴─────────┴─────────┘
```

### Screen Hierarchy

```
├── Auth Flow
│   ├── LoginScreen
│   └── RegisterScreen
│
├── Main App (TabBar)
│   ├── DashboardScreen (FR2)
│   │   ├── Summary Cards
│   │   ├── Pie Chart
│   │   ├── Recent Transactions
│   │   └── AI Insights
│   │
│   ├── TransactionsScreen (FR2.3)
│   │   ├── Filter Tabs
│   │   ├── Transaction List
│   │   └── → AddTransactionScreen (FR1.4)
│   │
│   ├── BudgetScreen (FR3.1)
│   │   ├── Budget Overview
│   │   ├── Category Budgets
│   │   └── AI Alerts
│   │
│   └── ProfileScreen
│       ├── User Info
│       ├── → SavingsGoalsScreen (FR3.3)
│       ├── → Bank Accounts (FR1.1)
│       ├── → SMS Settings
│       └── → Settings
```

---

## ⚡ Performance Optimization

1. **Lazy Loading**: TransactionList dùng `ListView.builder` với pagination
2. **Caching**: Cache Dashboard summary trong `StorageService`
3. **Debouncing**: Search/Filter debounce 300ms
4. **Background Processing**: SMS parsing chạy background thread
5. **State Management**: Provider rebuild scope nhỏ nhất

---

## 🧪 Testing Strategy

```
test/
├── unit/
│   ├── models/
│   │   ├── transaction_test.dart
│   │   └── budget_test.dart
│   ├── repositories/
│   │   └── transaction_repository_test.dart
│   └── utils/
│       ├── validators_test.dart
│       └── formatters_test.dart
│
├── widget/
│   ├── dashboard_card_test.dart
│   ├── transaction_item_test.dart
│   └── screens/
│       └── dashboard_screen_test.dart
│
└── integration/
    ├── auth_flow_test.dart
    └── transaction_flow_test.dart
```

---

## 📱 Danh sách Màn hình Ứng dụng (Screen Development Checklist)

| Tên màn hình (Screen)               | Mức độ phức tạp | Trạng thái Code UI | Người phụ trách | Ghi chú                                             |
| ----------------------------------- | --------------- | ------------------ | --------------- | --------------------------------------------------- |
| **1. Authentication**               |                 |                    |                 |                                                     |
| VD: Màn hình Login/Register         | Thấp            | ☐ Chưa làm         | ...             | Auth flow, validation                               |
| VD: Màn hình Forgot Password        | Thấp            | ☐ Chưa làm         | ...             | Email recovery                                      |
| **2. Main App (Bottom Navigation)** |                 |                    |                 |                                                     |
| VD: Màn hình Home Dashboard         | Cao             | ☐ Đang làm (50%)   | Trần Thị B      | Summary cards, pie chart, recent transactions (FR2) |
| VD: Màn hình Transactions List      | Trung bình      | ☐ Chưa làm         | ...             | Filter, search, pagination (FR2.3)                  |
| VD: Màn hình Budget Management      | Trung bình      | ☐ Chưa làm         | ...             | Budget list, progress bars, alerts (FR3.1)          |
| VD: Màn hình Profile/Settings       | Thấp            | ☐ Chưa làm         | ...             | User info, menu items                               |
| **3. Transactions Module**          |                 |                    |                 |                                                     |
| VD: Màn hình Add/Edit Transaction   | Trung bình      | ☐ Chưa làm         | ...             | Form validation, category picker (FR1.4)            |
| VD: Màn hình Transaction Detail     | Thấp            | ☐ Chưa làm         | ...             | View full info, edit/delete                         |
| VD: Màn hình SMS Scanner            | Cao             | ☐ Chưa làm         | ...             | SMS list, parse status, AI confidence (FR1.1-1.3)   |
| **4. Budget & Savings**             |                 |                    |                 |                                                     |
| VD: Màn hình Create Budget          | Trung bình      | ☐ Chưa làm         | ...             | Category selection, amount limit                    |
| VD: Màn hình Savings Goals          | Trung bình      | ☐ Chưa làm         | ...             | Goal list, progress, AI suggestions (FR3.3)         |
| VD: Màn hình Add Savings Goal       | Trung bình      | ☐ Chưa làm         | ...             | Target amount, deadline, auto-save setup            |
| **5. AI & Insights**                |                 |                    |                 |                                                     |
| VD: Màn hình AI Insights/Alerts     | Cao             | ☐ Chưa làm         | ...             | Smart notifications, spending analysis (FR3.1-3.2)  |
| VD: Màn hình Analytics/Reports      | Cao             | ☐ Chưa làm         | ...             | Charts, trends, export                              |
| **6. Settings & Management**        |                 |                    |                 |                                                     |
| VD: Màn hình Bank Accounts          | Trung bình      | ☐ Chưa làm         | ...             | Add/edit accounts, SMS toggle (FR1.1)               |
| VD: Màn hình Categories Management  | Trung bình      | ☐ Chưa làm         | ...             | CRUD categories, custom icons                       |
| VD: Màn hình Notifications          | Thấp            | ☐ Chưa làm         | ...             | Notification list, mark as read                     |
| VD: Màn hình App Settings           | Thấp            | ☐ Chưa làm         | ...             | Theme, language, privacy                            |
| **7. Onboarding & Help**            |                 |                    |                 |                                                     |
| VD: Màn hình Onboarding/Tutorial    | Trung bình      | ☐ Chưa làm         | ...             | First-time user guide                               |
| VD: Màn hình Help/FAQ               | Thấp            | ☐ Chưa làm         | ...             | Common questions                                    |

### 📊 Thống kê Progress:

- **Tổng số màn hình**: 20 screens
- **Đã hoàn thành**: 0 ✅
- **Đang làm**: 1 🔄 (Dashboard - 50%)
- **Chưa làm**: 19 ⏳

### 🎯 Ưu tiên phát triển (Sprint Planning):

**Sprint 1 - Core Features (2 tuần):**

1. ✅ Login/Register Screen
2. ✅ Home Dashboard Screen (FR2.1, FR2.2)
3. ✅ Add Transaction Screen (FR1.4)
4. ✅ Transactions List Screen (FR2.3)

**Sprint 2 - Smart Scan (2 tuần):** 5. ✅ SMS Scanner Screen (FR1.1) 6. ✅ Transaction Detail Screen 7. ✅ Bank Accounts Management

**Sprint 3 - Budget & AI (2 tuần):** 8. ✅ Budget Management Screen (FR3.1) 9. ✅ Create Budget Screen 10. ✅ AI Insights Screen

**Sprint 4 - Savings & Polish (2 tuần):** 11. ✅ Savings Goals Screen (FR3.3) 12. ✅ Analytics/Reports Screen 13. ✅ Profile & Settings Screens

**Sprint 5 - Enhancement (1 tuần):** 14. ✅ Onboarding, Help, Notifications 15. ✅ Bug fixes, performance optimization

---

### 📝 Checklist Symbols:

- ☑ **Đã xong**: UI + Logic hoàn thành, tested
- ☐ **Đang làm**: Đang implement (kèm %)
- ☐ **Chưa làm**: Chưa bắt đầu
- ⚠ **Blocked**: Chờ API/Design/Dependencies

---

## 📚 Additional Resources

- **State Management**: [Provider documentation](https://pub.dev/packages/provider)
- **Charts**: [FL Chart examples](https://github.com/imaNNeo/fl_chart)
- **SMS**: [Telephony package](https://pub.dev/packages/telephony)
- **Clean Architecture**: [ResoCoder tutorial](https://resocoder.com/flutter-clean-architecture-tdd/)

### **1. Mô hình kiến trúc:**

- [x] **Clean Architecture** (3-layer: Core → Data → Presentation)

    - **Lý do chọn**: Tách biệt business logic, dễ test, dễ scale, phù hợp dự án phức tạp (Smart Scan + AI + Dashboard)
    - **Layers**:
        - `Core`: Constants, Theme, Utils, Validators (domain logic)
        - `Data`: Models, Repositories, Services (data access & API)
        - `Presentation`: Screens, Widgets, Providers (UI & state)
    - **Benefits**: Testable, maintainable, swappable components (có thể đổi API/DB mà không ảnh hưởng UI)

- [ ] MVVM (Quá đơn giản cho dự án này)
- [ ] MVC (Thiếu tách biệt concerns)

---

### **2. State Management:**

- [x] **Provider** v6.1.1 (ChangeNotifier pattern)

    - **Lý do chọn**:
        - Đơn giản, dễ học, performance tốt
        - Tích hợp tốt với Clean Architecture
        - Hỗ trợ chính thức của Flutter team
    - **Use cases**:
        - `AuthProvider`: Quản lý login state, user session
        - `TransactionProvider`: Quản lý danh sách giao dịch, summary
        - `BudgetProvider`: Quản lý ngân sách, alerts
    - **Pattern**:
      ```dart
      class TransactionProvider extends ChangeNotifier {
        List<Transaction> _transactions = [];
        void addTransaction(Transaction t) {
          _transactions.add(t);
          notifyListeners(); // Rebuild UI
        }
      }
      ```

- [ ] BLoC/Cubit (Overkill cho dự án này, boilerplate nhiều)
- [ ] GetX (Không follow Flutter best practices)
- [ ] Riverpod (Learning curve cao, dự án không cần compile-time safety phức tạp)

---

### **3. Backend:**

- [x] **REST API**
    - **Java Spring Boot** (`http://175.41.150.228:8080/api`)
        - Endpoints: Auth, Transactions, Budgets, Categories, Dashboard
        - Authentication: JWT Bearer Token
        - Response format: JSON
    - **Python FastAPI** (AI Backend)
        - SMS Parsing (Regex + NLP)
        - Category Classification (ML model)
        - Anomaly Detection (spending patterns)
- [x] **MySQL Database** (Backend storage)
    - Tables: users, transactions, budgets, categories, accounts
    - Relationships: user → transactions → categories
- [ ] Firebase (Không dùng - không phù hợp với backend Java đã có)
- [ ] Local Only (Không phù hợp - cần sync multi-device)

---

### **4. Networking:**

- [x] **Dio** v5.4.0 (HTTP client)
    - **Features**:
        - Interceptors: Auto-add JWT token, logging, error handling
        - Timeout configuration (30s)
        - Retry logic (3 lần)
        - Request/Response transformation
    - **Usage**:
      ```dart
      // final response = await dio.get('/transactions',
      //   queryParameters: {'month': '2025-12'}
      // );
      ```
- [x] **Repository Pattern** (Retrofit-style)

    - Abstract interfaces → Testable
    - Mock implementations cho testing
    - Separation: UI không biết API details

- **Alternatives considered**:
    - `http` package: Quá basic, thiếu interceptors
    - `chopper`: Code generation phức tạp

---

### **5. Local Storage:**

- [x] **SharedPreferences** v2.2.2
    - **Lưu trữ**:
        - JWT Token (authentication)
        - User preferences (theme, language)
        - SMS permission status
        - Last sync timestamp
    - **Không lưu**: Sensitive data (passwords, card numbers)
- [x] **SQLite** (Dự kiến - chưa implement)

    - **Mục đích**: Offline caching cho transactions
    - **Package**: `sqflite` v2.3.0
    - **Use case**: Xem giao dịch khi không có mạng

- **Security**:
    - Token encrypted với `flutter_secure_storage`
    - Auto-logout sau 30 ngày inactive

---

### **6. UI Framework:**

- [x] **Material Design 3**
    - **Theme**: Custom Material 3 với brand colors
    - **Components**: Cards, Buttons, TextFields theo MD3 spec
    - **Dark Mode**: Support (future)
- [x] **Google Fonts** v6.1.0
    - **Primary Font**: Inter (headings)
    - **Secondary Font**: Roboto (body text)
    - **Vietnamese**: Full support
- [x] **FL Chart** v0.66.0 (Data visualization)

    - **Dashboard**: Pie chart (chi tiêu theo danh mục)
    - **Analytics**: Line chart (xu hướng theo tháng)
    - **Budget**: Bar chart (so sánh budget vs actual)
    - **Customizable**: Colors, animations, tooltips

- **Other UI packages**:
    - `intl` v0.19.0: Format tiền VND, dates
    - `cached_network_image`: Cache avatars, icons
    - `shimmer`: Loading placeholders

---

### **7. Platform Support:**

- [x] **Android** (Primary target - API 21+)
    - **Core features**:
        - SMS reading (`READ_SMS`, `RECEIVE_SMS` permissions)
        - Background SMS receiver (BroadcastReceiver)
        - Local notifications (`flutter_local_notifications`)
        - Biometric authentication (future)
    - **Packages**:
        - `permission_handler` v11.0.0
        - `telephony` v0.2.0 (SMS)
- [ ] **iOS** (Limited support - future)
    - **Limitations**:
        - Không đọc được SMS (Apple policy)
        - Phải dùng Bank API hoặc manual import
    - **Alternative**: Open Banking API integration
- [x] **Windows/Web** (Development only)
    - **Purpose**: Faster development, testing UI
    - **Not for production**: Thiếu SMS, notifications

---

### **8. AI/ML:**

- [x] **Backend AI** (Python FastAPI)

    - **SMS Parsing**:

        - Regex patterns cho từng ngân hàng (VCB, TCB, ACB...)
        - Extract: amount, type, merchant, datetime
        - Accuracy: ~95%

    - **Category Classification**:

        - Model: Naive Bayes / Random Forest
        - Input: Transaction description (e.g., "GRAB", "SHOPEE")
        - Output: Category ID + confidence score (0-1)
        - Training data: 10,000+ labeled transactions
        - Accuracy: ~85%

    - **Anomaly Detection** (Future):
        - Detect unusual spending (e.g., tiền điện tăng 30%)
        - Time-series analysis

- [ ] **On-device ML** (Future optimization)
    - **Package**: `tflite_flutter` v0.10.0
    - **Benefits**: Offline classification, faster response
    - **Challenges**: Model size (~5MB), training data

---

### **9. Navigation:**

- [x] **go_router** v13.0.0
    - **Features**:
        - Declarative routing
        - Deep linking support
        - Auth guard (redirect to login if not authenticated)
    - **Routes**:

[//]: # (    ```dart)
    /login
    /register
    /home (TabBar: dashboard, transactions, budget, profile)
    /transactions/add
    /transactions/:id
    /budget
    /savings
    /profile
    ```
- **Alternatives**:
    - `Navigator 2.0`: Quá phức tạp
    - `auto_route`: Code generation overhead

---

### **10. Additional Packages:**

**Authentication & Security:**

- `flutter_secure_storage` v9.0.0: Encrypted token storage
- `local_auth` v2.1.0: Biometric login (future)

**UI/UX:**

- `shimmer` v3.0.0: Loading skeleton
- `flutter_svg` v2.0.0: Vector icons
- `image_picker` v1.0.0: Upload hóa đơn (future)

**Utils:**

- `intl` v0.19.0: i18n, date/number formatting
- `timeago` v3.5.0: "2 giờ trước"
- `url_launcher` v6.2.0: Open web links

**Development:**

- `flutter_lints` v5.0.0: Code quality
- `mockito` v5.4.0: Mock testing
- `flutter_test`: Widget testing

---

### **11. Development Tools:**

**Version Control:**

- Git + GitHub
- Branch strategy: `main`, `develop`, feature branches
- PR reviews required

**CI/CD:**

- GitHub Actions (future)
- Auto-build APK on push
- Run tests on PR

**Code Quality:**

- Linting: `flutter analyze`
- Formatting: `dart format`
- Test coverage: Target 80%

---

### **12. Performance Considerations:**

**Optimization:**

- Image caching: `cached_network_image`
- List pagination: Load 20 items/page
- Debouncing: Search delay 300ms
- Lazy loading: `ListView.builder`

**Bundle Size:**

- Target: <20MB APK
- Minification: Enabled in release mode
- Remove unused resources

**Memory:**

- Dispose controllers properly
- Avoid memory leaks (check with DevTools)
- Stream subscriptions cleanup

---

## 📐 Tổng quan Cấu trúc

Dự án sử dụng **Clean Architecture** với 3 lớp chính:

- **Core Layer**: Các thành phần dùng chung
- **Data Layer**: Xử lý dữ liệu (API, Database, Models)
- **Presentation Layer**: Giao diện người dùng (UI, State Management)

---

**Tài liệu này sẽ được cập nhật khi có thêm features hoặc thay đổi kiến trúc.**
