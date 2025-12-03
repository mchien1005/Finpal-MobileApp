# 📘 Hướng Dẫn Kết Nối API - Finpal Frontend Web

10. [FR1: Module Tự động Ghi nhận Giao dịch (Smart Scan)](#10-fr1-module-tự-động-ghi-nhận-giao-dịch-smart-scan)
11. [FR2: Module Bảng điều khiển Trực quan (Dashboard)](#11-fr2-module-bảng-điều-khiển-trực-quan-dashboard)
12. [FR3: Module Trợ lý AI Gợi ý (AI Financial Coach)](#12-fr3-module-trợ-lý-ai-gợi-ý-ai-financial-coach)

## 10. FR1: Module Tự động Ghi nhận Giao dịch (Smart Scan)

### 10.1 Tổng quan chức năng

| FR    | Mô tả                          | API sử dụng                          |
| ----- | ------------------------------ | ------------------------------------ |
| FR1.1 | Quét Tin nhắn SMS ngân hàng    | `POST /api/sms/receive`              |
| FR1.2 | Bóc tách & Phân tích thông tin | `POST /api/sms/receive` (auto-parse) |
| FR1.3 | Tự động Phân loại bằng AI      | `POST /api/categorization/predict`   |
| FR1.4 | Ghi nhận Thủ công              | `POST /api/transactions`             |

---

### 10.2 FR1.1 & FR1.2: Quét SMS & Bóc tách thông tin

#### API: Nhận SMS từ ngân hàng

**Endpoint:** `POST /api/sms/receive`  
**Backend:** Java (Spring Boot)  
**Auth:** Bearer Token (JWT)

**Request Body:**

```json
{
  "smsContent": "TK 1234xxxx5678 -100,000 VND luc 18/11/2025 15:30. ND: GRAB VIETNAM. SD: 5,000,000 VND",
  "senderPhone": "Vietcombank"
}
```

**Response (201 Created):**

```json
{
  "id": 123,
  "amount": 100000,
  "type": "EXPENSE",
  "merchant": "GRAB VIETNAM",
  "categoryId": 5,
  "categoryName": "Di chuyển",
  "transactionDate": "2025-11-18T15:30:00",
  "isAuto": true,
  "aiConfidence": 0.95,
  "account": {
    "id": 1,
    "name": "Vietcombank",
    "accountNumber": "****5678"
  }
}
```

**Cách sử dụng trong Frontend:**

```javascript
// services/smsService.js
import api from "./api";

export const processSMS = async (smsContent, senderPhone) => {
  const response = await api.post("/sms/receive", {
    smsContent,
    senderPhone,
  });
  return response.data;
};

// Trong React Native / Mobile App
const handleSMSReceived = async (sms) => {
  try {
    // Kiểm tra SMS có phải từ ngân hàng không
    const bankNumbers = ["Vietcombank", "Techcombank", "ACB", "VCB", "TCB"];
    const isFromBank = bankNumbers.some((bank) =>
      sms.address.toLowerCase().includes(bank.toLowerCase())
    );

    if (isFromBank) {
      const transaction = await processSMS(sms.body, sms.address);
      message.success(
        `Đã ghi nhận: ${formatCurrency(transaction.amount)} - ${
          transaction.categoryName
        }`
      );
    }
  } catch (error) {
    console.error("Lỗi xử lý SMS:", error);
  }
};
```

**Các ngân hàng được hỗ trợ:**

- Vietcombank (VCB)
- Techcombank (TCB)
- ACB
- BIDV
- MB Bank
- VPBank
- TPBank

**Định dạng SMS được nhận diện:**

```
// Format 1: VCB
"TK 1234xxxx5678 -100,000 VND luc 18/11/2025 15:30. ND: GRAB. SD: 5,000,000 VND"

// Format 2: TCB
"GD: -100,000VND TK: *5678 luc 18/11 15:30. ND: GRAB VIETNAM"

// Format 3: ACB
"TK ACB: 1234567890 tru 100,000 VND luc 18/11/2025 15:30. ND: GRAB"
```

---

### 10.3 FR1.3: Tự động Phân loại bằng AI

#### API: Dự đoán Category cho giao dịch

**Endpoint:** `POST /api/categorization/predict`  
**Backend:** AI (FastAPI/Python)  
**Auth:** Không cần (internal API)

**Request Body:**

```json
{
  "merchant": "GRAB VIETNAM",
  "amount": 50000,
  "description": "Di chuyển đi làm",
  "timestamp": "2025-11-18T08:30:00"
}
```

**Response:**

```json
{
  "category": "Di chuyển",
  "confidence": 0.95,
  "alternatives": [
    { "category": "Ăn uống", "confidence": 0.15 },
    { "category": "Giải trí", "confidence": 0.05 }
  ]
}
```

**Cách sử dụng:**

```javascript
// services/aiService.js
const AI_API_URL = import.meta.env.VITE_AI_API_URL;

export const predictCategory = async (merchant, amount, description = null) => {
  const response = await fetch(`${AI_API_URL}/categorization/predict`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ merchant, amount, description }),
  });

  if (!response.ok) throw new Error("Failed to predict category");
  return await response.json();
};

// Sử dụng trong form tạo giao dịch
const handleMerchantChange = async (merchant, amount) => {
  try {
    const prediction = await predictCategory(merchant, amount);

    // Auto-fill category nếu confidence cao
    if (prediction.confidence >= 0.8) {
      setSelectedCategory(prediction.category);
      message.info(
        `Đã tự động chọn danh mục: ${prediction.category} (${(
          prediction.confidence * 100
        ).toFixed(0)}%)`
      );
    } else {
      // Hiển thị các gợi ý
      setCategorySuggestions([
        { name: prediction.category, confidence: prediction.confidence },
        ...prediction.alternatives,
      ]);
    }
  } catch (error) {
    console.error("Lỗi dự đoán category:", error);
  }
};
```

#### API: Batch Predict (nhiều giao dịch)

**Endpoint:** `POST /api/categorization/batch-predict`

**Request:**

```json
{
  "transactions": [
    { "merchant": "GRAB", "amount": 50000 },
    { "merchant": "SHOPEE", "amount": 200000 },
    { "merchant": "CGV CINEMA", "amount": 150000 }
  ]
}
```

**Response:**

```json
{
  "predictions": [
    {
      "merchant": "GRAB",
      "amount": 50000,
      "category": "Di chuyển",
      "confidence": 0.95
    },
    {
      "merchant": "SHOPEE",
      "amount": 200000,
      "category": "Mua sắm",
      "confidence": 0.92
    },
    {
      "merchant": "CGV CINEMA",
      "amount": 150000,
      "category": "Giải trí",
      "confidence": 0.98
    }
  ]
}
```

**Mapping Category phổ biến:**

| Merchant Pattern      | Category  | Confidence |
| --------------------- | --------- | ---------- |
| GRAB, UBER, BE        | Di chuyển | 95%+       |
| GRAB FOOD, SHOPEEFOOD | Ăn uống   | 90%+       |
| SHOPEE, LAZADA, TIKI  | Mua sắm   | 92%+       |
| CGV, LOTTE, GALAXY    | Giải trí  | 98%+       |
| CIRCLE K, MINISTOP    | Ăn uống   | 85%+       |
| HIGHLANDS, STARBUCKS  | Ăn uống   | 88%+       |
| ĐIỆN LỰC, EVN         | Hóa đơn   | 99%+       |
| FPT, VIETTEL          | Hóa đơn   | 95%+       |

---

### 10.4 FR1.4: Ghi nhận Thủ công

#### API: Tạo giao dịch thủ công

**Endpoint:** `POST /api/transactions`  
**Backend:** Java (Spring Boot)  
**Auth:** Bearer Token (JWT)

**Request Body:**

```json
{
  "accountId": 1,
  "amount": 25000,
  "type": "EXPENSE",
  "categoryId": 2,
  "merchant": "Tiền gửi xe",
  "description": "Gửi xe tháng 11",
  "transactionDate": "2025-11-18T08:00:00"
}
```

**Response (201 Created):**

```json
{
  "id": 124,
  "amount": 25000,
  "type": "EXPENSE",
  "merchant": "Tiền gửi xe",
  "description": "Gửi xe tháng 11",
  "categoryId": 2,
  "categoryName": "Di chuyển",
  "transactionDate": "2025-11-18T08:00:00",
  "isAuto": false,
  "createdAt": "2025-11-18T10:00:00"
}
```

**Cách sử dụng:**

```javascript
// services/transactionService.js
import api from "./api";

export const createTransaction = async (transactionData) => {
  const response = await api.post("/transactions", transactionData);
  return response.data;
};

// Component Form
const TransactionForm = () => {
  const [form] = Form.useForm();

  const handleSubmit = async (values) => {
    try {
      // Nếu không chọn category, gọi AI để dự đoán
      if (!values.categoryId && values.merchant) {
        const prediction = await predictCategory(
          values.merchant,
          values.amount
        );
        values.categoryId = getCategoryIdByName(prediction.category);
      }

      const transaction = await createTransaction({
        ...values,
        type: values.type || "EXPENSE",
        transactionDate: values.transactionDate || new Date().toISOString(),
      });

      message.success("Đã thêm giao dịch thành công!");
      form.resetFields();
    } catch (error) {
      message.error("Lỗi khi thêm giao dịch");
    }
  };

  return (
    <Form form={form} onFinish={handleSubmit}>
      <Form.Item name="amount" label="Số tiền" rules={[{ required: true }]}>
        <InputNumber min={0} style={{ width: "100%" }} />
      </Form.Item>
      <Form.Item
        name="merchant"
        label="Nơi chi tiêu"
        rules={[{ required: true }]}
      >
        <Input placeholder="VD: Tiền gửi xe, Bánh mì..." />
      </Form.Item>
      <Form.Item name="categoryId" label="Danh mục">
        <Select placeholder="Để trống để AI tự phân loại">
          {categories.map((cat) => (
            <Option key={cat.id} value={cat.id}>
              {cat.name}
            </Option>
          ))}
        </Select>
      </Form.Item>
      <Button type="primary" htmlType="submit">
        Thêm giao dịch
      </Button>
    </Form>
  );
};
```

---

## 11. FR2: Module Bảng điều khiển Trực quan (Dashboard)

### 11.1 Tổng quan chức năng

| FR    | Mô tả                         | API sử dụng                               |
| ----- | ----------------------------- | ----------------------------------------- |
| FR2.1 | Tổng quan Dòng tiền           | `GET /api/dashboard/cash-flow`            |
| FR2.2 | Biểu đồ Phân loại (Pie Chart) | `GET /api/dashboard/spending-by-category` |
| FR2.3 | Lịch sử Giao dịch             | `GET /api/transactions`                   |

---

### 11.2 FR2.1: Tổng quan Dòng tiền

#### API: Cash Flow Summary

**Endpoint:** `GET /api/dashboard/cash-flow`  
**Backend:** Java (Spring Boot)  
**Auth:** Bearer Token (JWT)

**Query Parameters:**

- `month` (optional): Tháng cần xem (format: YYYY-MM-DD). Mặc định là tháng hiện tại.

**Request:**

```
GET /api/dashboard/cash-flow?month=2025-11-01
Authorization: Bearer <token>
```

**Response:**

```json
{
  "monthlyIncome": 15000000,
  "monthlyExpense": 8000000,
  "netSavings": 7000000,
  "savingsRate": 46.67,
  "currentMonth": "2025-11",
  "totalBalance": 25000000,
  "comparedToLastMonth": {
    "incomeChange": 5.2,
    "expenseChange": -12.3,
    "savingsChange": 18.5
  }
}
```

**Cách sử dụng:**

```javascript
// services/dashboardService.js
import api from "./api";

export const getCashFlow = async (month = null) => {
  const params = month ? { month } : {};
  const response = await api.get("/dashboard/cash-flow", { params });
  return response.data;
};

// Component Dashboard
const CashFlowCard = () => {
  const [cashFlow, setCashFlow] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const data = await getCashFlow();
        setCashFlow(data);
      } catch (error) {
        message.error("Không thể tải dữ liệu dòng tiền");
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  if (loading) return <Spin />;

  return (
    <Row gutter={16}>
      <Col span={8}>
        <Card>
          <Statistic
            title="Thu nhập tháng này"
            value={cashFlow.monthlyIncome}
            prefix={<ArrowUpOutlined style={{ color: "#52c41a" }} />}
            formatter={(value) => formatCurrency(value)}
          />
        </Card>
      </Col>
      <Col span={8}>
        <Card>
          <Statistic
            title="Chi tiêu tháng này"
            value={cashFlow.monthlyExpense}
            prefix={<ArrowDownOutlined style={{ color: "#ff4d4f" }} />}
            formatter={(value) => formatCurrency(value)}
          />
        </Card>
      </Col>
      <Col span={8}>
        <Card>
          <Statistic
            title="Còn lại"
            value={cashFlow.netSavings}
            valueStyle={{
              color: cashFlow.netSavings >= 0 ? "#52c41a" : "#ff4d4f",
            }}
            formatter={(value) => formatCurrency(value)}
          />
          <Progress percent={cashFlow.savingsRate} status="active" />
        </Card>
      </Col>
    </Row>
  );
};
```

---

### 11.3 FR2.2: Biểu đồ Phân loại (Pie Chart)

#### API: Spending by Category

**Endpoint:** `GET /api/dashboard/spending-by-category`  
**Backend:** Java (Spring Boot)  
**Auth:** Bearer Token (JWT)

**Query Parameters:**

- `startDate` (optional): Ngày bắt đầu (YYYY-MM-DD)
- `endDate` (optional): Ngày kết thúc (YYYY-MM-DD)

**Request:**

```
GET /api/dashboard/spending-by-category?startDate=2025-11-01&endDate=2025-11-30
Authorization: Bearer <token>
```

**Response:**

```json
[
  {
    "categoryId": 1,
    "categoryName": "Ăn uống",
    "icon": "🍔",
    "color": "#FF5722",
    "totalAmount": 3200000,
    "percentage": 40.0,
    "transactionCount": 25
  },
  {
    "categoryId": 2,
    "categoryName": "Di chuyển",
    "icon": "🚗",
    "color": "#2196F3",
    "totalAmount": 1600000,
    "percentage": 20.0,
    "transactionCount": 15
  },
  {
    "categoryId": 3,
    "categoryName": "Mua sắm",
    "icon": "🛍️",
    "color": "#9C27B0",
    "totalAmount": 1200000,
    "percentage": 15.0,
    "transactionCount": 8
  },
  {
    "categoryId": 4,
    "categoryName": "Giải trí",
    "icon": "🎬",
    "color": "#4CAF50",
    "totalAmount": 800000,
    "percentage": 10.0,
    "transactionCount": 4
  },
  {
    "categoryId": 5,
    "categoryName": "Khác",
    "icon": "📦",
    "color": "#607D8B",
    "totalAmount": 1200000,
    "percentage": 15.0,
    "transactionCount": 10
  }
]
```

**Cách sử dụng với Recharts:**

```javascript
// services/dashboardService.js
export const getSpendingByCategory = async (startDate, endDate) => {
  const params = {};
  if (startDate) params.startDate = startDate;
  if (endDate) params.endDate = endDate;

  const response = await api.get("/dashboard/spending-by-category", { params });
  return response.data;
};

// Component Pie Chart
import {
  PieChart,
  Pie,
  Cell,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from "recharts";

const SpendingPieChart = () => {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        // Lấy dữ liệu tháng hiện tại
        const now = new Date();
        const startDate = new Date(now.getFullYear(), now.getMonth(), 1)
          .toISOString()
          .split("T")[0];
        const endDate = new Date(now.getFullYear(), now.getMonth() + 1, 0)
          .toISOString()
          .split("T")[0];

        const spendingData = await getSpendingByCategory(startDate, endDate);
        setData(spendingData);
      } catch (error) {
        message.error("Không thể tải biểu đồ chi tiêu");
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  const formatTooltip = (value, name, props) => {
    return [formatCurrency(value), props.payload.categoryName];
  };

  return (
    <Card title="Chi tiêu theo danh mục">
      <ResponsiveContainer width="100%" height={300}>
        <PieChart>
          <Pie
            data={data}
            dataKey="totalAmount"
            nameKey="categoryName"
            cx="50%"
            cy="50%"
            outerRadius={100}
            label={({ categoryName, percentage }) =>
              `${categoryName}: ${percentage.toFixed(1)}%`
            }
          >
            {data.map((entry, index) => (
              <Cell key={index} fill={entry.color} />
            ))}
          </Pie>
          <Tooltip formatter={formatTooltip} />
          <Legend
            formatter={(value, entry) => (
              <span>
                {entry.payload.icon} {value}
              </span>
            )}
          />
        </PieChart>
      </ResponsiveContainer>
    </Card>
  );
};
```

---

### 11.4 FR2.3: Lịch sử Giao dịch

#### API: Lấy danh sách giao dịch

**Endpoint:** `GET /api/transactions`  
**Backend:** Java (Spring Boot)  
**Auth:** Bearer Token (JWT)

**Query Parameters:**

| Parameter       | Type    | Mô tả                                         |
| --------------- | ------- | --------------------------------------------- |
| `accountId`     | Long    | Lọc theo tài khoản                            |
| `categoryId`    | Long    | Lọc theo danh mục                             |
| `type`          | String  | INCOME hoặc EXPENSE                           |
| `startDate`     | Date    | Từ ngày (YYYY-MM-DD)                          |
| `endDate`       | Date    | Đến ngày (YYYY-MM-DD)                         |
| `merchant`      | String  | Lọc theo merchant                             |
| `isAuto`        | Boolean | Giao dịch tự động (SMS) hay thủ công          |
| `keyword`       | String  | Tìm kiếm theo description/merchant            |
| `page`          | Integer | Số trang (bắt đầu từ 0)                       |
| `size`          | Integer | Số items/trang (mặc định 20)                  |
| `sortBy`        | String  | Sắp xếp theo field (mặc định transactionDate) |
| `sortDirection` | String  | ASC hoặc DESC                                 |

**Request:**

```
GET /api/transactions?type=EXPENSE&startDate=2025-11-01&endDate=2025-11-30&page=0&size=20&sortDirection=DESC
Authorization: Bearer <token>
```

**Response:**

```json
{
  "content": [
    {
      "id": 123,
      "amount": 50000,
      "type": "EXPENSE",
      "merchant": "GRAB VIETNAM",
      "description": "Di chuyển đi làm",
      "categoryId": 2,
      "categoryName": "Di chuyển",
      "categoryIcon": "🚗",
      "transactionDate": "2025-11-18T08:30:00",
      "isAuto": true,
      "isVerified": true,
      "aiConfidence": 0.95,
      "account": {
        "id": 1,
        "name": "Vietcombank",
        "accountNumber": "****5678"
      }
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 156,
  "totalPages": 8
}
```

**Cách sử dụng:**

```javascript
// services/transactionService.js
import api from "./api";

export const getTransactions = async (filters = {}) => {
  const response = await api.get("/transactions", { params: filters });
  return response.data;
};

// Cập nhật category (sửa phân loại sai)
export const updateTransaction = async (id, data) => {
  const response = await api.put(`/transactions/${id}`, data);
  return response.data;
};

// Component Transaction List
const TransactionList = () => {
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [pagination, setPagination] = useState({ page: 0, size: 20 });
  const [filters, setFilters] = useState({});

  useEffect(() => {
    fetchTransactions();
  }, [pagination, filters]);

  const fetchTransactions = async () => {
    setLoading(true);
    try {
      const data = await getTransactions({
        ...filters,
        ...pagination,
      });
      setTransactions(data.content);
    } catch (error) {
      message.error("Không thể tải lịch sử giao dịch");
    } finally {
      setLoading(false);
    }
  };

  // Sửa category khi AI phân loại sai
  const handleCategoryChange = async (transactionId, newCategoryId) => {
    try {
      await updateTransaction(transactionId, { categoryId: newCategoryId });
      message.success("Đã cập nhật danh mục");
      fetchTransactions(); // Refresh list
    } catch (error) {
      message.error("Lỗi cập nhật danh mục");
    }
  };

  const columns = [
    {
      title: "Thời gian",
      dataIndex: "transactionDate",
      render: (date) => formatDateTime(date),
    },
    {
      title: "Nội dung",
      dataIndex: "merchant",
      render: (merchant, record) => (
        <div>
          <div>{merchant}</div>
          <small style={{ color: "#888" }}>{record.description}</small>
          {record.isAuto && <Tag color="blue">Tự động</Tag>}
        </div>
      ),
    },
    {
      title: "Danh mục",
      dataIndex: "categoryName",
      render: (name, record) => (
        <Select
          defaultValue={record.categoryId}
          onChange={(value) => handleCategoryChange(record.id, value)}
          style={{ width: 120 }}
        >
          {categories.map((cat) => (
            <Option key={cat.id} value={cat.id}>
              {cat.icon} {cat.name}
            </Option>
          ))}
        </Select>
      ),
    },
    {
      title: "Số tiền",
      dataIndex: "amount",
      render: (amount, record) => (
        <span
          style={{
            color: record.type === "INCOME" ? "#52c41a" : "#ff4d4f",
          }}
        >
          {record.type === "INCOME" ? "+" : "-"}
          {formatCurrency(amount)}
        </span>
      ),
    },
  ];

  return (
    <Table
      columns={columns}
      dataSource={transactions}
      loading={loading}
      rowKey="id"
      pagination={{
        current: pagination.page + 1,
        pageSize: pagination.size,
        onChange: (page, size) => setPagination({ page: page - 1, size }),
      }}
    />
  );
};
```

---

## 12. FR3: Module Trợ lý AI Gợi ý (AI Financial Coach)

### 12.1 Tổng quan chức năng

| FR    | Mô tả                  | API sử dụng                                                                |
| ----- | ---------------------- | -------------------------------------------------------------------------- |
| FR3.1 | Gửi Thông báo Chủ động | `GET /api/insights/proactive-insights/{userId}` + `GET /api/notifications` |
| FR3.2 | Phát hiện Bất thường   | `POST /api/anomaly/detect` + `GET /api/statistics/anomalies`               |
| FR3.3 | Tạo Hũ Tiết kiệm       | `POST /api/savings-goals` + `POST /api/budgets`                            |

---

### 12.2 FR3.1: Gửi Thông báo Chủ động

#### API 1: AI Proactive Insights

**Endpoint:** `GET /api/insights/proactive-insights/{user_id}`  
**Backend:** AI (FastAPI/Python)

**Response:**

```json
[
  {
    "insight_type": "warning",
    "category": "Ăn uống",
    "message": "Chi tiêu 'Ăn uống' tháng này (3,200,000đ) cao hơn 30% so với trung bình (2,400,000đ).",
    "actionable": true,
    "impact_score": 0.85
  },
  {
    "insight_type": "tip",
    "category": "Trà sữa",
    "message": "Bạn chi trung bình 200,000đ/tuần cho 'Trà sữa'. Nếu giảm còn 100,000đ, bạn sẽ tiết kiệm được 400,000đ/tháng.",
    "actionable": true,
    "impact_score": 0.7
  },
  {
    "insight_type": "achievement",
    "category": "Di chuyển",
    "message": "Tuyệt vời! Bạn đã tiết kiệm được 15% trong danh mục 'Di chuyển' so với tháng trước.",
    "actionable": false,
    "impact_score": 0.5
  }
]
```

#### API 2: Savings Suggestions (Gợi ý tiết kiệm)

**Endpoint:** `POST /api/insights/savings-suggestions/{user_id}`  
**Backend:** AI (FastAPI/Python)

**Response:**

```json
{
  "user_id": 1,
  "suggestions": [
    {
      "category": "Trà sữa",
      "current_weekly_avg": 200000,
      "suggested_weekly_target": 100000,
      "monthly_savings": 400000,
      "message": "Bạn chi trung bình 200,000đ/tuần cho 'Trà sữa'. Nếu giảm còn 100,000đ, bạn sẽ tiết kiệm được 400,000đ/tháng."
    },
    {
      "category": "Ăn ngoài",
      "current_weekly_avg": 500000,
      "suggested_weekly_target": 350000,
      "monthly_savings": 600000,
      "message": "Bạn chi trung bình 500,000đ/tuần cho 'Ăn ngoài'. Nếu giảm còn 350,000đ, bạn sẽ tiết kiệm được 600,000đ/tháng."
    }
  ],
  "total_potential_savings": 1000000,
  "analyzed_months": 3
}
```

#### API 3: Lấy Notifications

**Endpoint:** `GET /api/notifications`  
**Backend:** Java (Spring Boot)  
**Auth:** Bearer Token (JWT)

**Query Parameters:**

- `isRead` (optional): Boolean - lọc theo trạng thái đọc

**Response:**

```json
[
  {
    "id": 1,
    "type": "BUDGET_WARNING",
    "title": "Cảnh báo hạn mức",
    "message": "Bạn đã chi 70% hạn mức 'Ăn ngoài' của tháng này, chỉ còn 10 ngày nữa là hết tháng.",
    "isRead": false,
    "createdAt": "2025-11-20T08:00:00",
    "metadata": {
      "categoryId": 1,
      "categoryName": "Ăn ngoài",
      "budgetLimit": 3000000,
      "currentSpent": 2100000,
      "percentage": 70
    }
  },
  {
    "id": 2,
    "type": "SAVINGS_TIP",
    "title": "Gợi ý tiết kiệm",
    "message": "FinPal nhận thấy bạn chi trung bình 200.000đ cho 'Trà sữa' mỗi tuần. Nếu bạn giảm còn 100.000đ, bạn sẽ tiết kiệm được 400.000đ/tháng.",
    "isRead": false,
    "createdAt": "2025-11-19T09:00:00"
  }
]
```

**Cách sử dụng:**

```javascript
// services/insightsService.js
const AI_API_URL = import.meta.env.VITE_AI_API_URL;

export const getProactiveInsights = async (userId) => {
  const response = await fetch(
    `${AI_API_URL}/insights/proactive-insights/${userId}`
  );
  if (!response.ok) throw new Error("Failed to fetch insights");
  return await response.json();
};

export const getSavingsSuggestions = async (userId) => {
  const response = await fetch(
    `${AI_API_URL}/insights/savings-suggestions/${userId}`,
    {
      method: "POST",
    }
  );
  if (!response.ok) throw new Error("Failed to fetch suggestions");
  return await response.json();
};

// services/notificationService.js
import api from "./api";

export const getNotifications = async (isRead = null) => {
  const params = isRead !== null ? { isRead } : {};
  const response = await api.get("/notifications", { params });
  return response.data;
};

export const markAsRead = async (notificationId) => {
  const response = await api.put(`/notifications/${notificationId}/read`);
  return response.data;
};

// Component Insights & Notifications
const InsightsPanel = () => {
  const [insights, setInsights] = useState([]);
  const [notifications, setNotifications] = useState([]);
  const user = JSON.parse(localStorage.getItem("user"));

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [aiInsights, notifs] = await Promise.all([
          getProactiveInsights(user.id),
          getNotifications(false), // Chỉ lấy chưa đọc
        ]);

        setInsights(aiInsights);
        setNotifications(notifs);
      } catch (error) {
        console.error("Error fetching insights:", error);
      }
    };
    fetchData();
  }, [user.id]);

  const getInsightIcon = (type) => {
    switch (type) {
      case "warning":
        return <WarningOutlined style={{ color: "#faad14" }} />;
      case "tip":
        return <BulbOutlined style={{ color: "#1890ff" }} />;
      case "achievement":
        return <TrophyOutlined style={{ color: "#52c41a" }} />;
      default:
        return <InfoCircleOutlined />;
    }
  };

  return (
    <Card title="🤖 Gợi ý từ AI">
      <List
        dataSource={insights.sort((a, b) => b.impact_score - a.impact_score)}
        renderItem={(insight) => (
          <List.Item>
            <List.Item.Meta
              avatar={getInsightIcon(insight.insight_type)}
              title={insight.category || "Tổng quan"}
              description={insight.message}
            />
            {insight.actionable && <Button type="link">Xem chi tiết</Button>}
          </List.Item>
        )}
      />
    </Card>
  );
};
```

---

### 12.3 FR3.2: Phát hiện Bất thường

#### API 1: Detect Anomaly (Realtime)

**Endpoint:** `POST /api/anomaly/detect`  
**Backend:** AI (FastAPI/Python)

**Request:**

```json
{
  "user_id": 1,
  "amount": 5000000,
  "merchant": "SHOPEE",
  "category": "Mua sắm",
  "timestamp": "2025-11-20T10:00:00"
}
```

**Response:**

```json
{
  "is_anomaly": true,
  "anomaly_score": 0.87,
  "reason": "Giao dịch cao hơn 3.5x trung bình (1,400,000đ)",
  "recommendation": "Xem xét lại giao dịch này. Đây có thể là giao dịch lớn bất thường hoặc lỗi."
}
```

#### API 2: Get Anomaly Transactions (History)

**Endpoint:** `GET /api/statistics/anomalies`  
**Backend:** Java (Spring Boot)  
**Auth:** Bearer Token (JWT)

**Query Parameters:**

- `startDate`: Ngày bắt đầu (YYYY-MM-DD)
- `endDate`: Ngày kết thúc (YYYY-MM-DD)

**Response:**

```json
[
  {
    "id": 456,
    "amount": 5000000,
    "merchant": "SHOPEE",
    "categoryName": "Mua sắm",
    "transactionDate": "2025-11-20T10:00:00",
    "isAnomaly": true,
    "anomalyScore": 0.87,
    "anomalyReason": "Giao dịch cao hơn 3.5x trung bình"
  },
  {
    "id": 457,
    "amount": 500000,
    "merchant": "EVN ĐIỆN LỰC",
    "categoryName": "Hóa đơn",
    "transactionDate": "2025-11-15T08:00:00",
    "isAnomaly": true,
    "anomalyScore": 0.72,
    "anomalyReason": "Hóa đơn tiền điện cao hơn 30% so với trung bình (350,000đ)"
  }
]
```

**Cách sử dụng:**

```javascript
// services/anomalyService.js
const AI_API_URL = import.meta.env.VITE_AI_API_URL;
import api from "./api";

// Kiểm tra giao dịch có bất thường không (realtime)
export const detectAnomaly = async (transaction) => {
  const response = await fetch(`${AI_API_URL}/anomaly/detect`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(transaction),
  });
  if (!response.ok) throw new Error("Failed to detect anomaly");
  return await response.json();
};

// Lấy danh sách giao dịch bất thường
export const getAnomalyTransactions = async (startDate, endDate) => {
  const response = await api.get("/statistics/anomalies", {
    params: { startDate, endDate },
  });
  return response.data;
};

// Component Anomaly Alert
const AnomalyAlert = () => {
  const [anomalies, setAnomalies] = useState([]);

  useEffect(() => {
    const fetchAnomalies = async () => {
      try {
        const now = new Date();
        const startDate = new Date(now.getFullYear(), now.getMonth(), 1)
          .toISOString()
          .split("T")[0];
        const endDate = now.toISOString().split("T")[0];

        const data = await getAnomalyTransactions(startDate, endDate);
        setAnomalies(data);
      } catch (error) {
        console.error("Error fetching anomalies:", error);
      }
    };
    fetchAnomalies();
  }, []);

  if (anomalies.length === 0) return null;

  return (
    <Alert
      type="warning"
      showIcon
      icon={<ExclamationCircleOutlined />}
      message={`Phát hiện ${anomalies.length} giao dịch bất thường trong tháng`}
      description={
        <ul>
          {anomalies.slice(0, 3).map((tx) => (
            <li key={tx.id}>
              {tx.merchant}: {formatCurrency(tx.amount)} - {tx.anomalyReason}
            </li>
          ))}
        </ul>
      }
      action={
        <Button
          size="small"
          onClick={() => navigate("/transactions/anomalies")}
        >
          Xem tất cả
        </Button>
      }
    />
  );
};
```

---

### 12.4 FR3.3: Tạo Hũ Tiết kiệm (Savings Goals)

#### API 1: Tạo Savings Goal mới

**Endpoint:** `POST /api/savings-goals`  
**Backend:** Java (Spring Boot)  
**Auth:** Bearer Token (JWT)

**Request:**

```json
{
  "name": "Mua tai nghe mới",
  "targetAmount": 3000000,
  "targetDate": "2026-03-01",
  "description": "Sony WH-1000XM5",
  "iconName": "🎧",
  "color": "#1890ff"
}
```

**Response (201 Created):**

```json
{
  "id": 1,
  "name": "Mua tai nghe mới",
  "targetAmount": 3000000,
  "currentAmount": 0,
  "progressPercentage": 0,
  "targetDate": "2026-03-01",
  "status": "ACTIVE",
  "suggestedMonthlyAmount": 750000,
  "suggestedWeeklyAmount": 187500,
  "remainingDays": 100,
  "remainingAmount": 3000000,
  "iconName": "🎧",
  "color": "#1890ff",
  "createdAt": "2025-11-20T10:00:00"
}
```

**Tính toán gợi ý số tiền:**

- `suggestedMonthlyAmount` = remainingAmount / số tháng còn lại
- `suggestedWeeklyAmount` = remainingAmount / số tuần còn lại

#### API 2: Đóng góp vào Savings Goal

**Endpoint:** `POST /api/savings-goals/{id}/contributions`  
**Auth:** Bearer Token (JWT)

**Request:**

```json
{
  "amount": 500000,
  "note": "Lương tháng 11"
}
```

**Response:**

```json
{
  "id": 1,
  "name": "Mua tai nghe mới",
  "targetAmount": 3000000,
  "currentAmount": 500000,
  "progressPercentage": 16.67,
  "remainingAmount": 2500000,
  "suggestedMonthlyAmount": 625000,
  "contributions": [
    {
      "id": 1,
      "amount": 500000,
      "note": "Lương tháng 11",
      "contributionDate": "2025-11-20T10:00:00"
    }
  ]
}
```

#### API 3: Lấy danh sách Savings Goals

**Endpoint:** `GET /api/savings-goals`  
**Query Parameters:** `status` (ACTIVE, COMPLETED, CANCELLED)

**Response:**

```json
[
  {
    "id": 1,
    "name": "Mua tai nghe mới",
    "targetAmount": 3000000,
    "currentAmount": 500000,
    "progressPercentage": 16.67,
    "targetDate": "2026-03-01",
    "status": "ACTIVE",
    "remainingDays": 100,
    "iconName": "🎧"
  },
  {
    "id": 2,
    "name": "Du lịch Đà Lạt",
    "targetAmount": 5000000,
    "currentAmount": 2000000,
    "progressPercentage": 40.0,
    "targetDate": "2026-01-15",
    "status": "ACTIVE",
    "remainingDays": 55,
    "iconName": "✈️"
  }
]
```

#### API 4: Tạo Budget (Hạn mức chi tiêu)

**Endpoint:** `POST /api/budgets`  
**Backend:** Java (Spring Boot)  
**Auth:** Bearer Token (JWT)

**Request:**

```json
{
  "categoryId": 1,
  "limitAmount": 3000000,
  "periodType": "MONTHLY",
  "startDate": "2025-11-01",
  "endDate": "2025-11-30",
  "alertThreshold": 70
}
```

**Response:**

```json
{
  "id": 1,
  "categoryId": 1,
  "categoryName": "Ăn uống",
  "limitAmount": 3000000,
  "spentAmount": 0,
  "remainingAmount": 3000000,
  "usagePercentage": 0,
  "periodType": "MONTHLY",
  "startDate": "2025-11-01",
  "endDate": "2025-11-30",
  "alertThreshold": 70,
  "isActive": true
}
```

**Cách sử dụng:**

```javascript
// services/savingsService.js
import api from "./api";

// Savings Goals
export const createSavingsGoal = async (goalData) => {
  const response = await api.post("/savings-goals", goalData);
  return response.data;
};

export const getSavingsGoals = async (status = "ACTIVE") => {
  const response = await api.get("/savings-goals", { params: { status } });
  return response.data;
};

export const addContribution = async (goalId, amount, note = "") => {
  const response = await api.post(`/savings-goals/${goalId}/contributions`, {
    amount,
    note,
  });
  return response.data;
};

// Budgets
export const createBudget = async (budgetData) => {
  const response = await api.post("/budgets", budgetData);
  return response.data;
};

export const getActiveBudgets = async () => {
  const response = await api.get("/budgets/active");
  return response.data;
};

// Component Savings Goals
const SavingsGoalsCard = () => {
  const [goals, setGoals] = useState([]);
  const [showModal, setShowModal] = useState(false);

  useEffect(() => {
    const fetchGoals = async () => {
      try {
        const data = await getSavingsGoals();
        setGoals(data);
      } catch (error) {
        message.error("Không thể tải mục tiêu tiết kiệm");
      }
    };
    fetchGoals();
  }, []);

  const handleCreateGoal = async (values) => {
    try {
      const newGoal = await createSavingsGoal(values);
      setGoals([newGoal, ...goals]);
      setShowModal(false);
      message.success(
        `Đã tạo mục tiêu "${newGoal.name}"! Bạn cần tiết kiệm ${formatCurrency(
          newGoal.suggestedMonthlyAmount
        )}/tháng.`
      );
    } catch (error) {
      message.error("Lỗi tạo mục tiêu");
    }
  };

  const handleContribute = async (goalId, amount) => {
    try {
      const updated = await addContribution(goalId, amount);
      setGoals(goals.map((g) => (g.id === goalId ? updated : g)));
      message.success(`Đã đóng góp ${formatCurrency(amount)}!`);
    } catch (error) {
      message.error("Lỗi đóng góp");
    }
  };

  return (
    <Card
      title="🏦 Hũ Tiết kiệm"
      extra={
        <Button type="primary" onClick={() => setShowModal(true)}>
          + Tạo mới
        </Button>
      }
    >
      <List
        dataSource={goals}
        renderItem={(goal) => (
          <List.Item
            actions={[
              <Button onClick={() => handleContribute(goal.id, 100000)}>
                +100k
              </Button>,
            ]}
          >
            <List.Item.Meta
              avatar={<span style={{ fontSize: 24 }}>{goal.iconName}</span>}
              title={goal.name}
              description={
                <div>
                  <Progress
                    percent={goal.progressPercentage}
                    status="active"
                    format={() =>
                      `${formatCurrency(goal.currentAmount)} / ${formatCurrency(
                        goal.targetAmount
                      )}`
                    }
                  />
                  <small>
                    Còn {goal.remainingDays} ngày • Gợi ý:{" "}
                    {formatCurrency(goal.suggestedWeeklyAmount)}/tuần
                  </small>
                </div>
              }
            />
          </List.Item>
        )}
      />

      <Modal
        title="Tạo mục tiêu tiết kiệm mới"
        open={showModal}
        onCancel={() => setShowModal(false)}
        footer={null}
      >
        <Form onFinish={handleCreateGoal}>
          <Form.Item
            name="name"
            label="Tên mục tiêu"
            rules={[{ required: true }]}
          >
            <Input placeholder="VD: Mua tai nghe mới" />
          </Form.Item>
          <Form.Item
            name="targetAmount"
            label="Số tiền mục tiêu"
            rules={[{ required: true }]}
          >
            <InputNumber
              min={0}
              style={{ width: "100%" }}
              formatter={(value) =>
                `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ",")
              }
              parser={(value) => value.replace(/\$\s?|(,*)/g, "")}
            />
          </Form.Item>
          <Form.Item
            name="targetDate"
            label="Ngày đạt mục tiêu"
            rules={[{ required: true }]}
          >
            <DatePicker style={{ width: "100%" }} />
          </Form.Item>
          <Form.Item name="iconName" label="Icon">
            <Select defaultValue="🎯">
              <Option value="🎧">🎧 Tai nghe</Option>
              <Option value="✈️">✈️ Du lịch</Option>
              <Option value="💻">💻 Laptop</Option>
              <Option value="📱">📱 Điện thoại</Option>
              <Option value="🏠">🏠 Nhà cửa</Option>
              <Option value="🚗">🚗 Xe</Option>
              <Option value="🎓">🎓 Học tập</Option>
              <Option value="🎯">🎯 Khác</Option>
            </Select>
          </Form.Item>
          <Button type="primary" htmlType="submit" block>
            Tạo mục tiêu
          </Button>
        </Form>
      </Modal>
    </Card>
  );
};
```

---

## 📊 Tóm tắt API theo Chức năng

### FR1: Smart Scan (Tự động ghi nhận giao dịch)

| Chức năng              | Method | Endpoint                            | Backend |
| ---------------------- | ------ | ----------------------------------- | ------- |
| Nhận SMS ngân hàng     | POST   | `/api/sms/receive`                  | Java    |
| Dự đoán category       | POST   | `/api/categorization/predict`       | AI      |
| Batch predict          | POST   | `/api/categorization/batch-predict` | AI      |
| Tạo giao dịch thủ công | POST   | `/api/transactions`                 | Java    |
| Lấy categories         | GET    | `/api/categories`                   | Java    |

### FR2: Dashboard (Bảng điều khiển)

| Chức năng              | Method | Endpoint                              | Backend |
| ---------------------- | ------ | ------------------------------------- | ------- |
| Dòng tiền tháng        | GET    | `/api/dashboard/cash-flow`            | Java    |
| Chi tiêu theo category | GET    | `/api/dashboard/spending-by-category` | Java    |
| Tổng quan dashboard    | GET    | `/api/dashboard/summary`              | Java    |
| Xu hướng thu chi       | GET    | `/api/dashboard/monthly-trend`        | Java    |
| Lịch sử giao dịch      | GET    | `/api/transactions`                   | Java    |
| Cập nhật giao dịch     | PUT    | `/api/transactions/{id}`              | Java    |

### FR3: AI Financial Coach (Trợ lý AI)

| Chức năng            | Method | Endpoint                                     | Backend |
| -------------------- | ------ | -------------------------------------------- | ------- |
| Insights chủ động    | GET    | `/api/insights/proactive-insights/{userId}`  | AI      |
| Gợi ý tiết kiệm      | POST   | `/api/insights/savings-suggestions/{userId}` | AI      |
| Phát hiện bất thường | POST   | `/api/anomaly/detect`                        | AI      |
| Lịch sử bất thường   | GET    | `/api/statistics/anomalies`                  | Java    |
| Notifications        | GET    | `/api/notifications`                         | Java    |
| Đánh dấu đã đọc      | PUT    | `/api/notifications/{id}/read`               | Java    |
| Tạo Savings Goal     | POST   | `/api/savings-goals`                         | Java    |
| Lấy Savings Goals    | GET    | `/api/savings-goals`                         | Java    |
| Đóng góp             | POST   | `/api/savings-goals/{id}/contributions`      | Java    |
| Tạo Budget           | POST   | `/api/budgets`                               | Java    |
| Lấy Active Budgets   | GET    | `/api/budgets/active`                        | Java    |

---
