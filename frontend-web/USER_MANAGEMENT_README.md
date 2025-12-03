# FinPal Admin - User Management

## 📁 Cấu trúc thư mục

```
frontend-web/
├── src/
│   ├── components/
│   │   └── users/
│   │       ├── UserList.jsx        # Danh sách người dùng với bảng, filter, search
│   │       ├── UserForm.jsx        # Form thêm/sửa người dùng
│   │       ├── UserDetail.jsx      # Chi tiết người dùng với tabs
│   │       ├── UserStats.jsx       # Thống kê người dùng
│   │       └── index.js           # Export components
│   │
│   ├── layouts/
│   │   └── AdminLayout.jsx        # Layout chính với sidebar & header
│   │
│   ├── pages/
│   │   ├── LoginPage.jsx          # Trang đăng nhập
│   │   ├── DashboardPage.jsx      # Trang dashboard
│   │   └── UsersPage.jsx          # Trang quản lý người dùng
│   │
│   ├── services/
│   │   ├── api.js                 # Axios instance với interceptors
│   │   ├── authService.js         # Authentication service
│   │   └── userService.js         # User CRUD operations
│   │
│   ├── utils/
│   │   ├── formatters.js          # Format functions (currency, date, phone)
│   │   └── validators.js          # Validation functions & rules
│   │
│   ├── App.jsx                    # Main app với routing
│   └── main.jsx                   # Entry point
│
├── .env                           # Environment variables
└── package.json                   # Dependencies
```

## 🎯 Tính năng đã triển khai

### 1. **User List (UserList.jsx)**

- ✅ Bảng danh sách người dùng với pagination
- ✅ Tìm kiếm theo username/email/phone
- ✅ Filter theo role & status
- ✅ Bulk delete (xóa nhiều user)
- ✅ Export to CSV
- ✅ Actions dropdown (View/Edit/Lock/Delete)
- ✅ Checkbox selection

### 2. **User Form (UserForm.jsx)**

- ✅ Form thêm mới user
- ✅ Form chỉnh sửa user
- ✅ Validation đầy đủ (email, phone, password, username)
- ✅ Switch lock/unlock user
- ✅ Role selection (USER/ADMIN)

### 3. **User Detail (UserDetail.jsx)**

- ✅ Tab "Thông tin cá nhân"
- ✅ Tab "Tài chính" với thống kê (Thu/Chi/Số dư)
- ✅ Tab "Giao dịch gần đây"
- ✅ Button Edit/Close

### 4. **User Stats (UserStats.jsx)**

- ✅ 4 thẻ thống kê:
  - Tổng người dùng
  - Người dùng hoạt động
  - Người dùng mới (7 ngày)
  - Người dùng bị khóa

### 5. **Admin Layout (AdminLayout.jsx)**

- ✅ Sidebar menu với icons
- ✅ Header với notification & user dropdown
- ✅ Collapsible sidebar
- ✅ Routing navigation
- ✅ Logout functionality

### 6. **Authentication**

- ✅ Login page với form validation
- ✅ JWT token storage
- ✅ Protected routes
- ✅ Auto redirect on 401
- ✅ Logout với clear token

### 7. **Services**

- ✅ API service với axios interceptors
- ✅ Auth service (login/logout/getCurrentUser)
- ✅ User service (CRUD + stats + export)

### 8. **Utils**

- ✅ Format currency (VND)
- ✅ Format date/time
- ✅ Format phone number
- ✅ Validation rules (email/phone/password/username)
- ✅ Error message parser

## 🚀 Cách chạy

### 1. **Cài đặt dependencies:**

```bash
cd frontend-web
npm install
```

### 2. **Cấu hình environment:**

```bash
# Copy file .env.example thành .env
cp .env.example .env

# Hoặc sửa trực tiếp file .env
VITE_API_BASE_URL=http://175.41.150.228:8080/api
```

### 3. **Chạy development server:**

```bash
npm run dev
```

Ứng dụng sẽ chạy tại: http://localhost:5173

### 4. **Build cho production:**

```bash
npm run build
```

## 📡 API Endpoints được sử dụng

### Authentication:

- `POST /api/auth/login` - Đăng nhập
- `POST /api/auth/register` - Đăng ký

### Users:

- `GET /api/admin/users` - Lấy danh sách user (pagination)
- `GET /api/admin/users/{id}` - Lấy chi tiết user
- `POST /api/admin/users` - Tạo user mới
- `PUT /api/admin/users/{id}` - Cập nhật user
- `DELETE /api/admin/users/{id}` - Xóa user
- `PATCH /api/admin/users/{id}/status` - Lock/Unlock user
- `POST /api/admin/users/{id}/reset-password` - Reset password
- `GET /api/admin/users/statistics` - Thống kê users
- `GET /api/admin/users/{id}/transactions` - Giao dịch của user
- `GET /api/admin/users/{id}/financial-summary` - Tóm tắt tài chính
- `POST /api/admin/users/bulk-delete` - Xóa nhiều users
- `GET /api/admin/users/export` - Export CSV

## 🎨 UI Components

### Ant Design Components đã sử dụng:

- **Layout:** Layout, Header, Sider, Content
- **Navigation:** Menu, Breadcrumb, Dropdown
- **Data Display:** Table, Card, Descriptions, Statistic, Tag, Badge, Avatar
- **Data Entry:** Form, Input, Select, Switch, DatePicker
- **Feedback:** Modal, Message, Notification, Spin, Popconfirm
- **Other:** Space, Row, Col, Button

### Icons:

- Ant Design Icons (@ant-design/icons)
- Feather Icons (có thể thêm nếu cần)

## 🔐 Authentication Flow

1. User vào trang login
2. Nhập username/password
3. Call API `/api/auth/login`
4. Nhận JWT token & user info
5. Lưu vào localStorage
6. Redirect to Dashboard
7. Mọi request sau đó tự động thêm token vào header
8. Nếu token expired (401) → Auto redirect to login

## 📱 Responsive Design

- ✅ Desktop (>1024px): Full sidebar + header
- ✅ Tablet (768-1024px): Collapsible sidebar
- ✅ Mobile (<768px): Hidden sidebar, hamburger menu

## 🛠️ Technologies

- **React 19.2.0**
- **React Router DOM 7.9.6**
- **Ant Design 6.0.0**
- **Axios 1.13.2**
- **Day.js 1.11.19**
- **Recharts 3.5.0** (cho charts)
- **Vite 7.2.4** (Build tool)

## 📝 TODO - Các tính năng cần thêm

### Backend API cần có:

- [ ] `/api/admin/users/statistics` - Endpoint thống kê users
- [ ] `/api/admin/users/{id}/financial-summary` - Tổng hợp tài chính user
- [ ] `/api/admin/users/{id}/transactions` - Danh sách giao dịch
- [ ] `/api/admin/users/export` - Export CSV
- [ ] `/api/admin/users/bulk-delete` - Xóa nhiều users
- [ ] `/api/admin/users/{id}/reset-password` - Reset mật khẩu

### Frontend cần thêm:

- [ ] Forgot password page
- [ ] Profile page (user profile)
- [ ] Settings page
- [ ] Categories management
- [ ] SMS Parsers management
- [ ] AI Models management
- [ ] Analytics dashboard
- [ ] Reports
- [ ] Notifications
- [ ] Audit logs

## 🎯 Cách sử dụng components

### Import và sử dụng UserList:

```jsx
import { UserList } from "../components/users";

// Trong component:
<UserList />;
```

### Import và sử dụng UserStats:

```jsx
import { UserStats } from "../components/users";

// Trong component:
<UserStats />;
```

### Sử dụng formatters:

```jsx
import {
  formatCurrency,
  formatDate,
  formatPhoneNumber,
} from "../utils/formatters";

formatCurrency(50000); // "50.000 ₫"
formatDate("2025-11-24"); // "24/11/2025"
formatPhoneNumber("0123456789"); // "0123 456 789"
```

### Sử dụng validators:

```jsx
import { formRules } from "../utils/validators";

// Trong Form.Item:
<Form.Item name="email" rules={[formRules.required, formRules.email]}>
  <Input />
</Form.Item>;
```

## 🐛 Debugging

### Check token:

```javascript
localStorage.getItem("token");
```

### Check user info:

```javascript
JSON.parse(localStorage.getItem("user"));
```

### Clear authentication:

```javascript
localStorage.removeItem("token");
localStorage.removeItem("user");
```

## 📞 Support

Nếu có vấn đề hoặc câu hỏi, vui lòng tạo issue hoặc liên hệ team.

---

**Version:** 1.0.0  
**Last Updated:** 2025-11-24
