# 🎨 FIGMA DESIGN SPECIFICATIONS - FINPAL ADMIN DASHBOARD

> **Mục đích:** Tài liệu chi tiết các màn hình cần thiết để thiết kế Admin Dashboard trên Figma, chuẩn bị cho việc sử dụng Figma Make để generate code.

---

## 📐 THIẾT KẾ TỔNG QUAN

### **Layout Structure**

```
┌──────────────────────────────────────────────────────────┐
│  Header (Fixed Top)                                      │
├────────┬─────────────────────────────────────────────────┤
│        │                                                  │
│ Side   │  Main Content Area                              │
│ bar    │  (Dynamic based on selected menu)               │
│        │                                                  │
│ (200px)│                                                  │
│        │                                                  │
├────────┴─────────────────────────────────────────────────┤
│  Footer (Optional)                                       │
└──────────────────────────────────────────────────────────┘
```

---

## 🎯 DESIGN SYSTEM

### **Color Palette**

```
Primary Colors:
- Primary Blue:    #1890FF (Buttons, Links)
- Primary Dark:    #0050B3 (Hover states)
- Success Green:   #52C41A (Success messages)
- Warning Orange:  #FA8C16 (Warnings)
- Error Red:       #F5222D (Errors, Delete)
- Info Cyan:       #13C2C2 (Info badges)

Neutral Colors:
- Text Primary:    #262626 (Headings)
- Text Secondary:  #595959 (Body text)
- Text Disabled:   #BFBFBF (Disabled text)
- Border:          #D9D9D9 (Borders, Dividers)
- Background:      #F0F2F5 (Page background)
- White:           #FFFFFF (Cards, Modals)

Gradient:
- Primary Gradient: linear-gradient(135deg, #667EEA 0%, #764BA2 100%)
```

### **Typography**

```
Font Family: 'Inter' (Primary), 'San Francisco' (Fallback)

Headings:
- H1: 32px, Bold (Page titles)
- H2: 24px, Semibold (Section titles)
- H3: 20px, Semibold (Card titles)
- H4: 16px, Medium (Subsection titles)

Body:
- Large: 16px, Regular (Important content)
- Base: 14px, Regular (Default text)
- Small: 12px, Regular (Captions, Helper text)

Line Height: 1.5 (for readability)
```

### **Spacing System**

```
xs:  4px   (Tight spacing)
sm:  8px   (Close elements)
md:  16px  (Default spacing)
lg:  24px  (Section spacing)
xl:  32px  (Large gaps)
2xl: 48px  (Page sections)
```

### **Border Radius**

```
- Small: 4px  (Inputs, Tags)
- Medium: 8px (Cards, Buttons)
- Large: 12px (Modals, Panels)
- Full: 999px (Pills, Avatars)
```

### **Shadows**

```
- Card Shadow:  0 2px 8px rgba(0,0,0,0.08)
- Modal Shadow: 0 8px 24px rgba(0,0,0,0.15)
- Hover Shadow: 0 4px 16px rgba(0,0,0,0.12)
```

---

## 🧩 COMPONENT LIBRARY

### **1. Navigation Components**

#### **A. Header (Top Bar)**

**Figma Frame:** `1440 x 64px`

**Elements:**

```
├─ Logo + App Name (Left)
│  ├─ Logo Icon: 32x32px
│  └─ Text: "FinPal Admin" (H3, Primary Blue)
│
├─ Search Bar (Center)
│  └─ Input: 400px width, Placeholder: "Search users, transactions..."
│
└─ Right Actions
   ├─ Notifications Icon (Badge: number)
   ├─ User Avatar (32x32px, Circle)
   └─ Username Dropdown
```

**Interactions:**

- Notifications: Dropdown with list of notifications
- Avatar: Dropdown menu (Profile, Settings, Logout)

---

#### **B. Sidebar (Left Navigation)**

**Figma Frame:** `200 x 100vh`

**Menu Structure:**

```
┌─ MAIN
├─ 📊 Dashboard
├─ 👥 Users
│  ├─ User List
│  └─ User Analytics
├─ 📂 Categories
│  ├─ Category List
│  └─ Category Rules
├─ 🏦 SMS Parsers
├─ 🤖 AI Models
│
├─ ANALYTICS
├─ 📈 Analytics
├─ 📊 Reports
├─ 🔔 Notifications
│
├─ SYSTEM
├─ 🔐 Audit Logs
├─ ⚙️ Settings
└─ 📦 Backup
```

**Design:**

- Active state: Primary Blue background, White text
- Hover state: Light blue background
- Icons: 20x20px (use Feather Icons or Heroicons)
- Collapsed state: Show only icons (48px width)

---

### **2. Data Display Components**

#### **A. Stat Card**

**Figma Frame:** `280 x 120px`

**Layout:**

```
┌────────────────────────────────────┐
│ 📊 Icon (32x32)     [Trend ↗ +15%]│
│                                    │
│ 1,234                              │
│ Total Users                        │
└────────────────────────────────────┘
```

**Variants:**

- Default (Blue icon background)
- Success (Green)
- Warning (Orange)
- Danger (Red)

**Elements:**

- Icon: Top-left, colored background circle
- Value: H1, Bold, Primary color
- Label: Small text, Secondary color
- Trend: Top-right, Green (↗ +) or Red (↘ -)

---

#### **B. Data Table**

**Figma Frame:** `Full width x Dynamic height`

**Structure:**

```
┌─────────────────────────────────────────────────────────────┐
│ Table Header                                    [+ Add] [⚙] │
├────┬──────────┬────────────┬─────────┬──────────┬──────────┤
│ ☐  │ Column 1 │ Column 2   │ Col 3   │ Status   │ Actions  │
├────┼──────────┼────────────┼─────────┼──────────┼──────────┤
│ ☐  │ Data 1   │ Data 2     │ Data 3  │ ✅Active │ ⋯        │
│ ☐  │ Data 1   │ Data 2     │ Data 3  │ ⚠️Warn   │ ⋯        │
│ ☐  │ Data 1   │ Data 2     │ Data 3  │ ❌Inactive│ ⋯        │
└────┴──────────┴────────────┴─────────┴──────────┴──────────┘
│ Showing 1-10 of 234          [< 1 2 3 ... 24 >]            │
└─────────────────────────────────────────────────────────────┘
```

**Components:**

- Header row: Semibold, Background #FAFAFA
- Checkbox column: 40px width
- Status badges: Colored pills (Green/Orange/Red)
- Actions: Dropdown menu (⋯ icon)
- Pagination: Bottom-right
- Hover: Row highlight with light blue background

**Actions Menu:**

```
⋯ Dropdown:
├─ 👁️ View Details
├─ ✏️ Edit
├─ 🗑️ Delete (Red text)
└─ 📄 Export
```

---

#### **C. Filter Bar**

**Figma Frame:** `Full width x 48px`

**Layout:**

```
┌────────────────────────────────────────────────────────────┐
│ [🔍 Search] [📅 Date] [📁 Category ▼] [⚙️ More] [Clear All]│
└────────────────────────────────────────────────────────────┘
```

**Elements:**

- Search input: 240px, Icon inside
- Dropdowns: 160px each
- Buttons: Outline style
- Clear All: Text button, right-aligned

---

#### **D. Chart Components**

**1. Line Chart** (Transaction Trend)

```
Frame: 600 x 300px
- X-axis: Dates (last 30 days)
- Y-axis: Amount (VND)
- Line color: Primary Blue
- Grid: Light gray dashed
- Tooltip on hover
```

**2. Bar Chart** (Category Distribution)

```
Frame: 400 x 300px
- Bars: Vertical, colored by category
- Labels: Category names
- Values: Above bars
```

**3. Pie Chart** (Expense Breakdown)

```
Frame: 300 x 300px (Circle)
- Segments: Different colors per category
- Legend: Right side
- Center: Total value
```

**4. Heatmap** (Activity)

```
Frame: 600 x 150px
- Grid: 7 days x 24 hours
- Color intensity: Light to Dark blue
- Tooltip: Show activity count
```

---

### **3. Form Components**

#### **A. Input Field**

**Figma Variants:**

**1. Text Input**

```
┌──────────────────────────────────┐
│ Label *                          │
│ ┌──────────────────────────────┐ │
│ │ Placeholder text...          │ │
│ └──────────────────────────────┘ │
│ Helper text or error message     │
└──────────────────────────────────┘
```

**States:**

- Default: Border #D9D9D9
- Focus: Border Primary Blue, Shadow
- Error: Border Red, Error message in red
- Disabled: Background #F5F5F5, Text gray

**2. Select Dropdown**

```
┌──────────────────────────────────┐
│ Select Category          ▼       │
├──────────────────────────────────┤
│ Option 1                         │
│ Option 2                         │
│ Option 3                         │
└──────────────────────────────────┘
```

**3. Date Picker**

```
┌──────────────────────────────────┐
│ 23/11/2025            📅         │
└──────────────────────────────────┘
```

**4. Checkbox & Radio**

```
☑ Checkbox label
⦿ Radio option 1
○ Radio option 2
```

**5. Toggle Switch**

```
Label: [●────] OFF
Label: [────●] ON (Green)
```

---

#### **B. Button Variants**

**Figma Components:**

**1. Primary Button**

```
┌──────────────┐
│ Save Changes │ (Blue background, White text)
└──────────────┘
```

**2. Secondary Button**

```
┌──────────────┐
│ Cancel       │ (White background, Blue border)
└──────────────┘
```

**3. Danger Button**

```
┌──────────────┐
│ Delete       │ (Red background, White text)
└──────────────┘
```

**4. Icon Button**

```
[+] Add    [✏️] Edit    [🗑️] Delete
```

**Sizes:**

- Large: 40px height, 16px padding
- Medium: 32px height, 12px padding
- Small: 24px height, 8px padding

**States:**

- Default
- Hover (darker shade)
- Active (pressed state)
- Disabled (gray, opacity 0.5)

---

#### **C. Modal/Dialog**

**Figma Frame:** `600 x 400px` (Centered overlay)

**Structure:**

```
┌────────────────────────────────────────┐
│ Modal Title                        [×] │
├────────────────────────────────────────┤
│                                        │
│  Modal Content                         │
│  (Forms, Text, Images)                 │
│                                        │
├────────────────────────────────────────┤
│              [Cancel]  [Save Changes]  │
└────────────────────────────────────────┘
```

**Background overlay:** Rgba(0,0,0,0.45)

**Variants:**

- Small: 400px
- Medium: 600px
- Large: 800px
- Full-screen: 90vw

---

#### **D. Alert/Notification**

**Figma Component:**

**1. Toast Notification**

```
┌────────────────────────────────────┐
│ ✅ Success! User created           │
└────────────────────────────────────┘
(Auto-dismiss after 3s)
```

**2. Alert Banner**

```
┌────────────────────────────────────────────┐
│ ⚠️ Warning: Your session will expire in 5m│
└────────────────────────────────────────────┘
```

**Types:**

- Success (Green background)
- Warning (Orange background)
- Error (Red background)
- Info (Blue background)

---

### **4. Advanced Components**

#### **A. User Profile Card**

**Figma Frame:** `320 x 180px`

```
┌──────────────────────────────────────┐
│  [Avatar]  John Doe                  │
│  64x64     @johndoe                  │
│            john@example.com          │
│                                      │
│  📊 1,234 Transactions               │
│  💰 $45,678 Total Spent              │
│  📅 Member since Jan 2025            │
│                                      │
│  [View Profile]      [Message]       │
└──────────────────────────────────────┘
```

---

#### **B. Timeline/Activity Feed**

**Figma Frame:** `400 x Dynamic`

```
┌────────────────────────────────────────┐
│ ● User created                         │
│ │ admin - 2 hours ago                  │
│ │                                      │
│ ● Transaction added                    │
│ │ demo - 5 hours ago                   │
│ │ Amount: $50 - Category: Food         │
│ │                                      │
│ ● Category updated                     │
│   admin - 1 day ago                    │
└────────────────────────────────────────┘
```

**Elements:**

- Dot: Colored by action type
- Connecting line: Gray
- Title: Bold
- Metadata: Gray, small text
- Details: Expandable/collapsible

---

#### **C. Progress Indicator**

**Figma Component:**

**1. Linear Progress**

```
Budget Progress: $700 / $1,000
[████████████░░░░░░] 70%
```

**2. Circular Progress**

```
    ╱────╲
   │  75% │  Goal Achievement
    ╲────╱
```

**3. Step Progress**

```
● ─────── ● ─────── ○ ─────── ○
Step 1    Step 2    Step 3    Step 4
(Complete)(Current)(Pending) (Pending)
```

---

#### **D. Tag/Badge Components**

**Figma Variants:**

```
[Active]      (Green background)
[Pending]     (Orange background)
[Inactive]    (Gray background)
[New]         (Blue background)
[5]           (Badge count, red dot)
```

**Sizes:**

- Small: 20px height
- Medium: 24px height
- Large: 28px height

---

## 📱 SCREEN DESIGNS (Pages)

### **SCREEN 1: Dashboard Overview**

**Figma Frame:** `1440 x 900px`

**Layout:**

```
┌─────────────────────────────────────────────────────────┐
│ Header                                                  │
├─────┬───────────────────────────────────────────────────┤
│Side │ Dashboard                                         │
│bar  │ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐     │
│     │ │Total   │ │Active  │ │New     │ │Revenue │     │
│     │ │Users   │ │Users   │ │Signups │ │        │     │
│     │ │1,234   │ │567     │ │156     │ │$45K    │     │
│     │ └────────┘ └────────┘ └────────┘ └────────┘     │
│     │                                                   │
│     │ ┌──────────────────────┐ ┌──────────────────┐   │
│     │ │ Transaction Trend    │ │ Top Categories   │   │
│     │ │ [Line Chart]         │ │ [Pie Chart]      │   │
│     │ └──────────────────────┘ └──────────────────┘   │
│     │                                                   │
│     │ ┌──────────────────────────────────────────────┐ │
│     │ │ Recent Transactions (Table)                  │ │
│     │ │ ID | User | Amount | Category | Date         │ │
│     │ │ ...                                           │ │
│     │ └──────────────────────────────────────────────┘ │
└─────┴───────────────────────────────────────────────────┘
```

**Components:**

- 4x Stat Cards (Top row)
- 2x Chart Cards (Middle row)
- 1x Data Table (Bottom)

---

### **SCREEN 2: User List**

**Figma Frame:** `1440 x 900px`

**Layout:**

```
┌─────────────────────────────────────────────────────────┐
│ Header                                                  │
├─────┬───────────────────────────────────────────────────┤
│Side │ Users                                [+ Add User] │
│bar  │ ┌─────────────────────────────────────────────┐  │
│     │ │ Filter Bar                                  │  │
│     │ │ [🔍 Search] [Role ▼] [Status ▼] [Clear]    │  │
│     │ └─────────────────────────────────────────────┘  │
│     │                                                   │
│     │ ┌─────────────────────────────────────────────┐  │
│     │ │ Users Table                                 │  │
│     │ │ ☐ | ID | Name | Email | Role | Status | ⋯  │  │
│     │ │ ☐ | 1  | John | john@ | USER | ✅ | ⋯       │  │
│     │ │ ☐ | 2  | Jane | jane@ | ADMIN| ✅ | ⋯       │  │
│     │ │ ...                                          │  │
│     │ └─────────────────────────────────────────────┘  │
│     │ Showing 1-10 of 234        [< 1 2 3 ... 24 >]   │
└─────┴───────────────────────────────────────────────────┘
```

**Components:**

- Page title + Add button
- Filter bar
- Data table with pagination
- Bulk actions (checkbox column)

---

### **SCREEN 3: User Detail View**

**Figma Frame:** `1440 x 900px`

**Layout:**

```
┌─────────────────────────────────────────────────────────┐
│ Header                                                  │
├─────┬───────────────────────────────────────────────────┤
│Side │ [← Back] User Details: John Doe                  │
│bar  │ ┌────────────────┐ ┌──────────────────────────┐  │
│     │ │ Profile Card   │ │ Tabs:                    │  │
│     │ │ [Avatar]       │ │ [Profile] [Financial]    │  │
│     │ │ John Doe       │ │ [Activity] [Settings]    │  │
│     │ │ @johndoe       │ │                          │  │
│     │ │ john@email.com │ │ Tab Content:             │  │
│     │ │                │ │ ┌──────────────────────┐ │  │
│     │ │ ✅ Active      │ │ │ Basic Info:          │ │  │
│     │ │ 📅 Since 2025  │ │ │ Full Name: [____]    │ │  │
│     │ │                │ │ │ Email: [____]        │ │  │
│     │ │ [Edit] [Delete]│ │ │ Phone: [____]        │ │  │
│     │ └────────────────┘ │ │ ...                  │ │  │
│     │                    │ └──────────────────────┘ │  │
│     │                    └──────────────────────────┘  │
└─────┴───────────────────────────────────────────────────┘
```

**Components:**

- Back button
- Left sidebar: User profile card
- Right content: Tabbed interface
- Action buttons (Edit, Delete)

---

### **SCREEN 4: Category Management**

**Figma Frame:** `1440 x 900px`

**Layout:**

```
┌─────────────────────────────────────────────────────────┐
│ Header                                                  │
├─────┬───────────────────────────────────────────────────┤
│Side │ Categories                      [+ Add Category]  │
│bar  │ ┌──────────────────┐ ┌──────────────────────┐    │
│     │ │ Category List    │ │ Category Rules       │    │
│     │ │                  │ │                      │    │
│     │ │ 🍔 Ăn uống      │ │ Keyword | Category   │    │
│     │ │ 🚗 Di chuyển    │ │ GRAB    | Di chuyển  │    │
│     │ │ 🛒 Mua sắm      │ │ SHOPEE  | Mua sắm    │    │
│     │ │ 🎬 Giải trí     │ │ CGV     | Giải trí   │    │
│     │ │ ...             │ │ ...                  │    │
│     │ │                  │ │ [+ Add Rule]         │    │
│     │ └──────────────────┘ └──────────────────────┘    │
└─────┴───────────────────────────────────────────────────┘
```

**Components:**

- Split view (50/50)
- Left: Category list with icons
- Right: Rules table
- Drag & drop reordering for categories

---

### **SCREEN 5: Create/Edit Form**

**Figma Frame:** `600 x 700px` (Modal)

**Layout:**

```
┌──────────────────────────────────────────┐
│ Add New Category                     [×] │
├──────────────────────────────────────────┤
│                                          │
│ Category Name *                          │
│ ┌──────────────────────────────────────┐ │
│ │ Ăn uống                              │ │
│ └──────────────────────────────────────┘ │
│                                          │
│ Type *                                   │
│ ⦿ Income  ○ Expense                     │
│                                          │
│ Icon                                     │
│ ┌──────────────────────────────────────┐ │
│ │ 🍔 [Select Icon]                     │ │
│ └──────────────────────────────────────┘ │
│                                          │
│ Color                                    │
│ ┌────┐ #FF5733                          │
│ │████│                                  │
│ └────┘                                  │
│                                          │
│ Parent Category (Optional)               │
│ [Select Category ▼]                     │
│                                          │
│ ☐ System Category (Cannot be deleted)   │
│                                          │
├──────────────────────────────────────────┤
│                  [Cancel]  [Save]        │
└──────────────────────────────────────────┘
```

**Components:**

- Text inputs
- Radio buttons
- Icon picker
- Color picker
- Dropdown
- Checkbox
- Action buttons

---

### **SCREEN 6: Analytics Dashboard**

**Figma Frame:** `1440 x 1200px`

**Layout:**

```
┌─────────────────────────────────────────────────────────┐
│ Header                                                  │
├─────┬───────────────────────────────────────────────────┤
│Side │ Analytics                    [Date Range Picker] │
│bar  │                                                   │
│     │ ┌──────────────────────────────────────────────┐ │
│     │ │ KPI Cards Row                                │ │
│     │ │ [Total] [Active] [New] [Revenue]             │ │
│     │ └──────────────────────────────────────────────┘ │
│     │                                                   │
│     │ ┌────────────────────┐ ┌────────────────────┐   │
│     │ │ User Growth        │ │ Category Breakdown │   │
│     │ │ [Line Chart]       │ │ [Pie Chart]        │   │
│     │ └────────────────────┘ └────────────────────┘   │
│     │                                                   │
│     │ ┌────────────────────┐ ┌────────────────────┐   │
│     │ │ Activity Heatmap   │ │ Top Merchants      │   │
│     │ │ [Heatmap]          │ │ [Bar Chart]        │   │
│     │ └────────────────────┘ └────────────────────┘   │
│     │                                                   │
│     │ ┌──────────────────────────────────────────────┐ │
│     │ │ User Segmentation Table                      │ │
│     │ │ Segment | Users | Avg Txns | Retention       │ │
│     │ └──────────────────────────────────────────────┘ │
└─────┴───────────────────────────────────────────────────┘
```

**Components:**

- Date range picker (top-right)
- 4x KPI cards
- 4x Chart components
- 1x Summary table

---

### **SCREEN 7: SMS Parser Management**

**Figma Frame:** `1440 x 900px`

**Layout:**

```
┌─────────────────────────────────────────────────────────┐
│ Header                                                  │
├─────┬───────────────────────────────────────────────────┤
│Side │ SMS Parsers                     [+ Add Parser]    │
│bar  │ ┌─────────────────────────────────────────────┐  │
│     │ │ Bank | Code | Status | Txns | Success Rate │  │
│     │ │ VCB  | VCB  | ✅     | 1234 | 98.5%        │  │
│     │ │ TCB  | TCB  | ✅     | 567  | 95.2%        │  │
│     │ └─────────────────────────────────────────────┘  │
│     │                                                   │
│     │ ┌─────────────────────────────────────────────┐  │
│     │ │ Test Parser                                 │  │
│     │ │ Bank: [Vietcombank ▼]                       │  │
│     │ │ SMS Content:                                 │  │
│     │ │ ┌─────────────────────────────────────────┐ │  │
│     │ │ │ Bien dong so du TK 001...: -55,000VND   │ │  │
│     │ │ │ luc 12/11/2025 09:00. ND: GRAB...       │ │  │
│     │ │ └─────────────────────────────────────────┘ │  │
│     │ │ [Test Parse]                                 │  │
│     │ │                                              │  │
│     │ │ ✅ Parsed Successfully:                      │  │
│     │ │ Amount: 55,000 VND                           │  │
│     │ │ Type: EXPENSE                                │  │
│     │ │ Merchant: GRAB...                            │  │
│     │ └─────────────────────────────────────────────┘  │
└─────┴───────────────────────────────────────────────────┘
```

**Components:**

- Parser list table
- Test interface card
- Result display

---

### **SCREEN 8: Notification Center**

**Figma Frame:** `1440 x 900px`

**Layout:**

```
┌─────────────────────────────────────────────────────────┐
│ Header                                                  │
├─────┬───────────────────────────────────────────────────┤
│Side │ Notifications                [+ Create] [Settings]│
│bar  │ ┌─────────────────────────────────────────────┐  │
│     │ │ Tabs: [Templates] [Sent] [Scheduled]        │  │
│     │ └─────────────────────────────────────────────┘  │
│     │                                                   │
│     │ ┌─────────────────────────────────────────────┐  │
│     │ │ Template List                               │  │
│     │ │ ┌─────────────────────────────────────────┐ │  │
│     │ │ │ 📊 Budget Alert                         │ │  │
│     │ │ │ "You've reached {percent}% of budget..." │ │  │
│     │ │ │ Type: BUDGET_ALERT | Priority: HIGH      │ │  │
│     │ │ │ [Edit] [Preview] [Send]                  │ │  │
│     │ │ └─────────────────────────────────────────┘ │  │
│     │ │                                              │  │
│     │ │ ┌─────────────────────────────────────────┐ │  │
│     │ │ │ 💡 Saving Tip                           │ │  │
│     │ │ │ "FinPal noticed you spend {amount}..."   │ │  │
│     │ │ │ Type: SAVING_TIP | Priority: MEDIUM      │ │  │
│     │ │ │ [Edit] [Preview] [Send]                  │ │  │
│     │ │ └─────────────────────────────────────────┘ │  │
│     │ └─────────────────────────────────────────────┘  │
└─────┴───────────────────────────────────────────────────┘
```

**Components:**

- Tabs navigation
- Template cards
- Action buttons per template
- Preview modal

---

### **SCREEN 9: Settings Page**

**Figma Frame:** `1440 x 900px`

**Layout:**

```
┌─────────────────────────────────────────────────────────┐
│ Header                                                  │
├─────┬───────────────────────────────────────────────────┤
│Side │ Settings                                          │
│bar  │ ┌───────────┐ ┌──────────────────────────────┐   │
│     │ │ Tabs      │ │ Tab Content                  │   │
│     │ │           │ │                              │   │
│     │ │ General   │ │ App Name:                    │   │
│     │ │ SMS       │ │ [FinPal Ví Thông Minh]       │   │
│     │ │ AI        │ │                              │   │
│     │ │ Security  │ │ Default Currency:            │   │
│     │ │ Backup    │ │ [VND ▼]                      │   │
│     │ │           │ │                              │   │
│     │ │           │ │ Timezone:                    │   │
│     │ │           │ │ [Asia/Ho_Chi_Minh ▼]         │   │
│     │ │           │ │                              │   │
│     │ │           │ │ ...                          │   │
│     │ │           │ │                              │   │
│     │ │           │ │ [Reset] [Save Changes]       │   │
│     │ └───────────┘ └──────────────────────────────┘   │
└─────┴───────────────────────────────────────────────────┘
```

**Components:**

- Vertical tabs (left sidebar)
- Settings form (right content)
- Action buttons (bottom)

---

### **SCREEN 10: Login Page**

**Figma Frame:** `1440 x 900px` (Full screen)

**Layout:**

```
┌──────────────────────────────────────────────────────┐
│                                                      │
│              ┌────────────────────┐                  │
│              │                    │                  │
│              │   [Logo 64x64]     │                  │
│              │   FinPal Admin     │                  │
│              │                    │                  │
│              │   Username         │                  │
│              │   ┌──────────────┐ │                  │
│              │   │              │ │                  │
│              │   └──────────────┘ │                  │
│              │                    │                  │
│              │   Password         │                  │
│              │   ┌──────────────┐ │                  │
│              │   │              │ │                  │
│              │   └──────────────┘ │                  │
│              │                    │                  │
│              │   ☐ Remember me    │                  │
│              │                    │                  │
│              │   [   Login   ]    │                  │
│              │                    │                  │
│              │   Forgot password? │                  │
│              └────────────────────┘                  │
│                                                      │
└──────────────────────────────────────────────────────┘
```

**Components:**

- Centered login card (400px width)
- Logo & app name
- Input fields
- Checkbox
- Primary button
- Link

---

## 🎨 RESPONSIVE DESIGN

### **Breakpoints:**

```
Desktop:  1440px (Default)
Laptop:   1024px (Sidebar collapsible)
Tablet:   768px  (Sidebar hidden by default)
Mobile:   375px  (Stack all components)
```

### **Mobile Adaptations:**

**1. Header:**

- Hamburger menu icon (replaces sidebar)
- Logo only (hide app name)
- Avatar only (hide username)

**2. Stat Cards:**

- Stack vertically (1 per row)

**3. Charts:**

- Full width
- Reduce height

**4. Tables:**

- Horizontal scroll
- Or card view (mobile-first)

**5. Forms:**

- Full width inputs
- Stack buttons vertically

---

## 🔧 INTERACTIVE STATES

### **Hover States:**

- **Buttons:** Darken background (-10% brightness)
- **Table Rows:** Light blue background (#F0F5FF)
- **Cards:** Lift shadow (0 4px 12px rgba(0,0,0,0.1))
- **Links:** Underline + Primary color

### **Active States:**

- **Buttons:** Darken background (-20% brightness)
- **Inputs:** Blue border + shadow
- **Sidebar Items:** Blue background + white text

### **Disabled States:**

- **Buttons:** Gray background, opacity 0.5, cursor not-allowed
- **Inputs:** Gray background (#F5F5F5), gray text

### **Loading States:**

- **Skeleton screens** for data loading
- **Spinner** for button actions
- **Progress bar** for long operations

---

## 📦 EXPORT GUIDELINES

### **For Figma to Code (Figma Make):**

**1. Naming Convention:**

```
Components:
- Button/Primary/Default
- Button/Primary/Hover
- Button/Primary/Disabled

Screens:
- Screen/Dashboard
- Screen/UserList
- Screen/UserDetail
```

**2. Auto Layout:**

- Use Auto Layout for all containers
- Set constraints properly (Left/Right/Top/Bottom)
- Define spacing values (8px, 16px, 24px)

**3. Variants:**

- Create variants for all interactive components
- Name properties clearly (Size: Small/Medium/Large)
- Use boolean properties for states (isActive, isDisabled)

**4. Components:**

- Break down complex UI into nested components
- Use instance swapping for icons
- Create text styles for consistency

**5. Colors & Styles:**

- Use Color Styles (not hex values)
- Create Text Styles for typography
- Use Effect Styles for shadows

**6. Assets:**

- Export icons as SVG (24x24, 32x32)
- Export logos as SVG + PNG (@1x, @2x, @3x)
- Export images as PNG/WebP

---

## 🚀 FIGMA TO CODE WORKFLOW

### **Step 1: Design in Figma**

1. Create Design System (colors, typography, components)
2. Design all screens using components
3. Add interactions (hover, click, scroll)
4. Create prototypes (flows)

### **Step 2: Prepare for Export**

1. Organize layers properly
2. Name everything clearly
3. Use Auto Layout everywhere
4. Create variants for states

### **Step 3: Export with Figma Make**

1. Select screen/component
2. Export to React/Vue/HTML
3. Review generated code
4. Customize as needed

### **Step 4: Integration**

1. Copy component code to project
2. Connect to backend APIs
3. Add business logic
4. Test functionality

---

## 📋 CHECKLIST

### **Before Starting Design:**

- [ ] Review all 10 screens described above
- [ ] Understand component structure
- [ ] Set up Design System (colors, typography, spacing)
- [ ] Create component library

### **During Design:**

- [ ] Use consistent spacing (8px grid)
- [ ] Apply color styles (not hex)
- [ ] Use text styles (not manual formatting)
- [ ] Add Auto Layout to all containers
- [ ] Name layers properly
- [ ] Create variants for interactive states

### **Before Exporting:**

- [ ] Check naming convention
- [ ] Verify Auto Layout constraints
- [ ] Test responsive behavior
- [ ] Review component variants
- [ ] Add comments/documentation
- [ ] Create prototype flows

### **After Export:**

- [ ] Review generated code
- [ ] Test on different screen sizes
- [ ] Customize styling as needed
- [ ] Add accessibility attributes
- [ ] Integrate with backend
- [ ] Test functionality

---

## 🎯 KEY SCREENS PRIORITY

### **Phase 1 (MVP):**

1. ✅ Login Page
2. ✅ Dashboard Overview
3. ✅ User List
4. ✅ User Detail
5. ✅ Category Management

### **Phase 2:**

6. ✅ Analytics Dashboard
7. ✅ SMS Parser Management
8. ✅ Settings Page

### **Phase 3:**

9. ✅ Notification Center
10. ✅ Additional screens (Reports, Audit Logs, etc.)

---

## 💡 DESIGN TIPS

**1. Consistency:**

- Use the same spacing values throughout
- Stick to color palette
- Use consistent icon set

**2. Hierarchy:**

- Use size/weight/color to create visual hierarchy
- Important info = larger, bolder, darker
- Less important = smaller, lighter, grayer

**3. Whitespace:**

- Don't cram everything together
- Use generous padding (16-24px)
- Let content breathe

**4. Accessibility:**

- Contrast ratio ≥ 4.5:1 for text
- Touch targets ≥ 44x44px (mobile)
- Clear focus states

**5. Performance:**

- Optimize image sizes
- Use SVG for icons
- Minimize custom fonts

---

## 📚 RESOURCES

**Icon Libraries:**

- Feather Icons: https://feathericons.com/
- Heroicons: https://heroicons.com/
- Material Icons: https://fonts.google.com/icons

**Figma Plugins:**

- Auto Layout: Built-in
- Iconify: Icon library
- Content Reel: Dummy data
- Contrast: Check accessibility

**Design Inspiration:**

- Ant Design: https://ant.design/
- Tailwind UI: https://tailwindui.com/
- Dribbble: https://dribbble.com/tags/admin-dashboard

---

**END OF DESIGN SPECIFICATIONS**

_Use this document as a blueprint for creating the Admin Dashboard in Figma._

_Version: 1.0_  
_Last Updated: 2025-11-24_
