-- =====================================================
-- FINPAL DATABASE SCHEMA
-- Ví Thông Minh - Smart Financial Assistant
-- =====================================================
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
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
    role ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_role (role)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
-- =====================================================
-- 2. BẢNG CATEGORIES - Danh mục chi tiêu
-- =====================================================
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL COMMENT 'NULL = hệ thống, NOT NULL = user tự tạo',
    name VARCHAR(100) NOT NULL,
    type ENUM('INCOME', 'EXPENSE') NOT NULL,
    icon VARCHAR(50),
    color VARCHAR(7),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_type (type),
    INDEX idx_user_id (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
-- =====================================================
-- 3. BẢNG ACCOUNTS - Ví/Nguồn tiền (tự động từ SMS)
-- Ví dụ: "VCB *1234", "TCB *5678", "Tiền mặt"
-- =====================================================
CREATE TABLE IF NOT EXISTS accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    bank_code VARCHAR(20) COMMENT 'VTB, TCB, MBB, BIDV, PVB, CASH',
    account_number_encrypted VARCHAR(500) COMMENT 'Số TK đầy đủ (mã hóa AES-256)',
    last_4_digits VARCHAR(4) COMMENT '4 số cuối TK (hiển thị)',
    name VARCHAR(100) COMMENT 'Tên tùy chỉnh: "VCB lương", "Tiền mặt"',
    balance DECIMAL(15, 2) DEFAULT 0.00,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_bank (user_id, bank_code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
-- =====================================================
-- 4. BẢNG TRANSACTIONS - Giao dịch tài chính
-- =====================================================
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    account_id BIGINT NOT NULL,
    category_id BIGINT NULL,
    -- Thông tin giao dịch
    amount DECIMAL(15, 2) NOT NULL,
    balance_after DECIMAL(15, 2) NULL COMMENT 'Số dư sau GD (từ SMS)',
    type ENUM('INCOME', 'EXPENSE') NOT NULL,
    merchant VARCHAR(255) COMMENT 'GRAB, SHOPEE, EVN...',
    description TEXT,
    transaction_date DATETIME NOT NULL,
    -- AI & SMS
    is_auto BOOLEAN DEFAULT FALSE COMMENT 'Từ SMS hay nhập tay',
    sms_content_encrypted TEXT COMMENT 'Nội dung SMS gốc (mã hóa AES-256)',
    suggested_category_id BIGINT NULL COMMENT 'AI gợi ý',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE
    SET NULL,
        FOREIGN KEY (suggested_category_id) REFERENCES categories(id) ON DELETE
    SET NULL,
        INDEX idx_user_date (user_id, transaction_date),
        INDEX idx_merchant (merchant)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
-- =====================================================
-- 5. BẢNG BUDGETS - Ngân sách
-- =====================================================
CREATE TABLE IF NOT EXISTS budgets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category_id BIGINT NULL COMMENT 'NULL = tổng ngân sách',
    amount DECIMAL(15, 2) NOT NULL COMMENT 'Hạn mức',
    current_spent DECIMAL(15, 2) DEFAULT 0.00 COMMENT 'Đã chi',
    month DATE NOT NULL COMMENT 'Tháng áp dụng',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE
    SET NULL,
        INDEX idx_user_month (user_id, month)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
-- =====================================================
-- 6. BẢNG SAVINGS_GOALS - Mục tiêu tiết kiệm (Hũ tiết kiệm)
-- =====================================================
CREATE TABLE IF NOT EXISTS savings_goals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL COMMENT 'Mua iPhone, Du lịch...',
    target_amount DECIMAL(15, 2) NOT NULL,
    current_amount DECIMAL(15, 2) DEFAULT 0.00,
    deadline DATE NULL,
    status ENUM('ACTIVE', 'COMPLETED') DEFAULT 'ACTIVE',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_status (user_id, status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
-- =====================================================
-- 7. BẢNG RECURRING_TRANSACTIONS - Giao dịch định kỳ
-- Phát hiện bất thường: "Tiền điện tháng này cao hơn 30%"
-- =====================================================
CREATE TABLE IF NOT EXISTS recurring_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    merchant VARCHAR(255) NOT NULL COMMENT 'EVN, VNPT, Netflix...',
    average_amount DECIMAL(15, 2) NOT NULL,
    frequency ENUM('WEEKLY', 'MONTHLY', 'YEARLY') DEFAULT 'MONTHLY',
    last_transaction_date DATE,
    next_expected_date DATE,
    last_alert_sent_at TIMESTAMP NULL COMMENT 'Lần cuối gửi alert thiếu GD (tránh spam)',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE,
    INDEX idx_user (user_id),
    INDEX idx_next_expected (next_expected_date)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
-- =====================================================
-- 8. BẢNG SMS_PARSERS - Mẫu phân tích SMS ngân hàng
-- =====================================================
CREATE TABLE IF NOT EXISTS sms_parsers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bank_code VARCHAR(20) NOT NULL COMMENT 'VCB, TCB, ACB',
    regex_pattern TEXT NOT NULL,
    field_mappings JSON NOT NULL COMMENT '{"amount":"group1", "merchant":"group2"...}',
    sample_sms TEXT COMMENT 'SMS mẫu để test',
    INDEX idx_bank_code (bank_code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
-- =====================================================
-- 9. BẢNG CATEGORY_RULES - Luật phân loại tự động
-- =====================================================
CREATE TABLE IF NOT EXISTS category_rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL COMMENT 'NULL = global, NOT NULL = user riêng',
    keyword VARCHAR(255) NOT NULL COMMENT 'GRAB, SHOPEE...',
    category_id BIGINT NOT NULL,
    confidence DECIMAL(3, 2) DEFAULT 1.00 COMMENT 'Độ tin cậy 0.00-1.00',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE,
    INDEX idx_user_keyword (user_id, keyword)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
-- =====================================================
-- 10. BẢNG NOTIFICATIONS - Thông báo
-- =====================================================
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type ENUM(
        'BUDGET_ALERT',
        'ANOMALY_DETECTED',
        'SAVINGS_TIP',
        'MONTHLY_REPORT'
    ) NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_read (user_id, is_read)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
-- =====================================================
-- SCHEMA HOÀN TẤT - 10 BẢNG CỐT LÕI
-- =====================================================
-- 1. users - Người dùng & phân quyền
-- 2. categories - Danh mục thu/chi (hệ thống + user)
-- 3. accounts - Ví/TK tự động từ SMS
-- 4. transactions - Giao dịch (có AI suggestion)
-- 5. budgets - Ngân sách (có current_spent)
-- 6. savings_goals - Mục tiêu tiết kiệm
-- 7. recurring_transactions - Phát hiện bất thường
-- 8. sms_parsers - Parse SMS ngân hàng
-- 9. category_rules - AI phân loại (có confidence)
-- 10. notifications - Thông báo AI
-- =====================================================