-- =====================================================
-- FINPAL DATABASE SCHEMA
-- Ví Thông Minh - Smart Financial Assistant
-- =====================================================
-- Tạo database
CREATE DATABASE IF NOT EXISTS finpal_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE finpal_db;
-- =====================================================
-- 1. BẢNG USERS - Quản lý người dùng
-- =====================================================
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL COMMENT 'Mã hóa bằng BCrypt',
    email VARCHAR(100) UNIQUE NOT NULL,
    full_name VARCHAR(100),
    phone VARCHAR(20),
    avatar_url VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    email_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP NULL,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_created_at (created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
-- =====================================================
-- 2. BẢNG CATEGORIES - Danh mục chi tiêu
-- =====================================================
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type ENUM('INCOME', 'EXPENSE') NOT NULL DEFAULT 'EXPENSE',
    icon VARCHAR(50) COMMENT 'Icon name hoặc emoji',
    color VARCHAR(7) COMMENT 'Mã màu hex, ví dụ: #FF5733',
    parent_id BIGINT NULL COMMENT 'Category cha (cho sub-category)',
    is_system BOOLEAN DEFAULT FALSE COMMENT 'Category hệ thống không thể xóa',
    display_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE
    SET NULL,
        INDEX idx_type (type),
        INDEX idx_parent (parent_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
-- =====================================================
-- 3. BẢNG ACCOUNTS - Tài khoản ngân hàng
-- =====================================================
CREATE TABLE IF NOT EXISTS accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    bank_name VARCHAR(100) NOT NULL COMMENT 'Vietcombank, Techcombank, ACB...',
    account_name VARCHAR(100) COMMENT 'Tên tài khoản',
    account_number_encrypted VARCHAR(500) COMMENT 'Số tài khoản được mã hóa',
    account_type ENUM('BANK', 'CASH', 'CREDIT_CARD', 'E_WALLET') DEFAULT 'BANK',
    balance DECIMAL(15, 2) DEFAULT 0.00,
    currency VARCHAR(3) DEFAULT 'VND',
    is_active BOOLEAN DEFAULT TRUE,
    icon VARCHAR(50),
    color VARCHAR(7),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_bank_name (bank_name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
-- =====================================================
-- 4. BẢNG TRANSACTIONS - Giao dịch tài chính
-- =====================================================
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    account_id BIGINT NOT NULL,
    category_id BIGINT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    type ENUM('INCOME', 'EXPENSE') NOT NULL,
    merchant VARCHAR(255) COMMENT 'Đơn vị nhận tiền: GRAB, SHOPEE, CGV...',
    description TEXT COMMENT 'Mô tả giao dịch',
    transaction_date DATETIME NOT NULL,
    -- Thông tin SMS
    is_auto BOOLEAN DEFAULT FALSE COMMENT 'TRUE: từ SMS, FALSE: nhập tay',
    sms_content_encrypted TEXT COMMENT 'Nội dung SMS gốc (mã hóa)',
    sms_bank_code VARCHAR(20) COMMENT 'Mã ngân hàng trong SMS',
    -- Trạng thái
    is_verified BOOLEAN DEFAULT FALSE COMMENT 'Người dùng đã xác nhận',
    is_anomaly BOOLEAN DEFAULT FALSE COMMENT 'Giao dịch bất thường',
    -- Metadata
    notes TEXT COMMENT 'Ghi chú của người dùng',
    tags VARCHAR(500) COMMENT 'Tags, cách nhau bởi dấu phẩy',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE
    SET NULL,
        INDEX idx_user_date (user_id, transaction_date),
        INDEX idx_account (account_id),
        INDEX idx_category (category_id),
        INDEX idx_type (type),
        INDEX idx_merchant (merchant),
        INDEX idx_transaction_date (transaction_date)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
-- =====================================================
-- 5. BẢNG BUDGETS - Ngân sách
-- =====================================================
CREATE TABLE IF NOT EXISTS budgets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category_id BIGINT NULL COMMENT 'NULL = tổng ngân sách',
    name VARCHAR(100) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    period ENUM('WEEKLY', 'MONTHLY', 'QUARTERLY', 'YEARLY') DEFAULT 'MONTHLY',
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    alert_threshold INT DEFAULT 70 COMMENT 'Cảnh báo khi đạt % này',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE
    SET NULL,
        INDEX idx_user_period (user_id, period),
        INDEX idx_dates (start_date, end_date)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
-- =====================================================
-- 6. BẢNG SAVINGS_GOALS - Mục tiêu tiết kiệm (Hũ tiết kiệm)
-- =====================================================
CREATE TABLE IF NOT EXISTS savings_goals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL COMMENT 'Ví dụ: Mua tai nghe, Du lịch Đà Lạt',
    description TEXT,
    target_amount DECIMAL(15, 2) NOT NULL,
    current_amount DECIMAL(15, 2) DEFAULT 0.00,
    deadline DATE NULL,
    icon VARCHAR(50),
    color VARCHAR(7),
    status ENUM('ACTIVE', 'COMPLETED', 'CANCELLED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_status (user_id, status),
    INDEX idx_deadline (deadline)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
-- =====================================================
-- 7. BẢNG SAVINGS_CONTRIBUTIONS - Đóng góp vào mục tiêu
-- =====================================================
CREATE TABLE IF NOT EXISTS savings_contributions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    savings_goal_id BIGINT NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    contribution_date DATE NOT NULL,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (savings_goal_id) REFERENCES savings_goals(id) ON DELETE CASCADE,
    INDEX idx_goal (savings_goal_id),
    INDEX idx_date (contribution_date)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
-- =====================================================
-- 8. BẢNG SMS_PARSERS - Mẫu phân tích SMS ngân hàng
-- =====================================================
CREATE TABLE IF NOT EXISTS sms_parsers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bank_name VARCHAR(100) NOT NULL,
    bank_code VARCHAR(20) NOT NULL COMMENT 'VCB, TCB, ACB...',
    sender_number VARCHAR(20) COMMENT 'Số điện thoại gửi SMS',
    regex_pattern TEXT NOT NULL COMMENT 'Pattern để extract thông tin',
    field_mappings JSON COMMENT 'Mapping các field: amount, type, merchant, time...',
    sample_sms TEXT COMMENT 'SMS mẫu để test',
    is_active BOOLEAN DEFAULT TRUE,
    priority INT DEFAULT 0 COMMENT 'Thứ tự ưu tiên khi match',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_bank_code (bank_code),
    INDEX idx_active (is_active)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
-- =====================================================
-- 9. BẢNG CATEGORY_RULES - Luật phân loại tự động
-- =====================================================
CREATE TABLE IF NOT EXISTS category_rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    keyword VARCHAR(255) NOT NULL COMMENT 'Từ khóa: GRAB, SHOPEE, CGV...',
    category_id BIGINT NOT NULL,
    match_type ENUM(
        'EXACT',
        'CONTAINS',
        'STARTS_WITH',
        'ENDS_WITH',
        'REGEX'
    ) DEFAULT 'CONTAINS',
    priority INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE,
    INDEX idx_keyword (keyword),
    INDEX idx_category (category_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
-- =====================================================
-- 10. BẢNG NOTIFICATIONS - Thông báo
-- =====================================================
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL COMMENT 'BUDGET_ALERT, SAVING_TIP, ANOMALY, REPORT...',
    title VARCHAR(255) NOT NULL,
    content TEXT,
    action_url VARCHAR(500) COMMENT 'Deep link trong app',
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP NULL,
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') DEFAULT 'MEDIUM',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_read (user_id, is_read),
    INDEX idx_created (created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
-- =====================================================
-- 11. BẢNG USER_PREFERENCES - Cài đặt người dùng
-- =====================================================
CREATE TABLE IF NOT EXISTS user_preferences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    currency VARCHAR(3) DEFAULT 'VND',
    language VARCHAR(10) DEFAULT 'vi',
    timezone VARCHAR(50) DEFAULT 'Asia/Ho_Chi_Minh',
    notifications_enabled BOOLEAN DEFAULT TRUE,
    email_notifications BOOLEAN DEFAULT TRUE,
    push_notifications BOOLEAN DEFAULT TRUE,
    budget_alert_threshold INT DEFAULT 70,
    theme VARCHAR(20) DEFAULT 'light',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
-- =====================================================
-- 12. BẢNG SPENDING_INSIGHTS - Phân tích chi tiêu
-- =====================================================
CREATE TABLE IF NOT EXISTS spending_insights (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    period_type ENUM('DAILY', 'WEEKLY', 'MONTHLY') NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    total_income DECIMAL(15, 2) DEFAULT 0.00,
    total_expense DECIMAL(15, 2) DEFAULT 0.00,
    top_category_id BIGINT NULL COMMENT 'Category chi nhiều nhất',
    top_category_amount DECIMAL(15, 2),
    insights_data JSON COMMENT 'Chi tiết phân tích',
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (top_category_id) REFERENCES categories(id) ON DELETE
    SET NULL,
        INDEX idx_user_period (user_id, period_start, period_end),
        UNIQUE KEY unique_user_period (user_id, period_type, period_start)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
-- =====================================================
-- 13. BẢNG AUDIT_LOGS - Nhật ký hệ thống
-- =====================================================
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NULL,
    old_value JSON,
    new_value JSON,
    ip_address VARCHAR(50),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE
    SET NULL,
        INDEX idx_user (user_id),
        INDEX idx_created (created_at),
        INDEX idx_entity (entity_type, entity_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
-- =====================================================
-- DATABASE SCHEMA CREATED SUCCESSFULLY
-- =====================================================