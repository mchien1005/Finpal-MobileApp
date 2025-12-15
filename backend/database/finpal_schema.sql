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
-- 1. BẢNG NGUOI_DUNG (USERS) - Quản lý người dùng
-- =====================================================
CREATE TABLE IF NOT EXISTS nguoi_dung (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ten_dang_nhap VARCHAR(50) UNIQUE NOT NULL,
    mat_khau VARCHAR(255) NOT NULL COMMENT 'Mã hóa bằng BCrypt',
    email VARCHAR(100) UNIQUE NOT NULL,
    ho_ten VARCHAR(100),
    so_dien_thoai VARCHAR(20),
    ngay_sinh DATE NULL COMMENT 'Ngày sinh của người dùng',
    gioi_tinh ENUM('NAM', 'NU', 'KHAC') NULL COMMENT 'Giới tính của người dùng',
    anh_dai_dien VARCHAR(500),
    vai_tro VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT 'USER hoặc ADMIN',
    dang_hoat_dong BOOLEAN DEFAULT TRUE,
    ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ngay_cap_nhat TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    lan_dang_nhap_cuoi TIMESTAMP NULL,
    lan_hoat_dong_cuoi TIMESTAMP NULL COMMENT 'Lần hoạt động cuối cùng của người dùng',
    INDEX idx_ten_dang_nhap (ten_dang_nhap),
    INDEX idx_email (email),
    INDEX idx_ngay_tao (ngay_tao),
    INDEX idx_vai_tro (vai_tro)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
-- =====================================================
-- 2. BẢNG DANH_MUC (CATEGORIES) - Danh mục chi tiêu
-- =====================================================
CREATE TABLE IF NOT EXISTS danh_muc (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ten_danh_muc VARCHAR(100) NOT NULL,
    loai ENUM('INCOME', 'EXPENSE') NOT NULL DEFAULT 'EXPENSE',
    mau_sac VARCHAR(20) NULL COMMENT 'Màu hiển thị: #00C950 hoặc 0xFF00C950',
    icon VARCHAR(100) NULL COMMENT 'Tên icon: mdi:food, fa:shopping-cart',
    id_cha BIGINT NULL COMMENT 'Category cha (cho sub-category)',
    la_he_thong BOOLEAN DEFAULT FALSE COMMENT 'Category hệ thống không thể xóa',
    thu_tu_hien_thi INT DEFAULT 0,
    ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ngay_cap_nhat TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_cha) REFERENCES danh_muc(id) ON DELETE
    SET NULL,
        INDEX idx_loai (loai),
        INDEX idx_id_cha (id_cha)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
-- =====================================================
-- 3. BẢNG GIAO_DICH (TRANSACTIONS) - Giao dịch tài chính
-- =====================================================
CREATE TABLE IF NOT EXISTS giao_dich (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_nguoi_dung BIGINT NOT NULL,
    id_danh_muc BIGINT NULL,
    so_tien DECIMAL(15, 2) NOT NULL,
    loai ENUM('INCOME', 'EXPENSE') NOT NULL,
    nguon_giao_dich VARCHAR(50) COMMENT 'Nguồn giao dịch: VCB, TCB, CASH, MOMO...',
    don_vi_chap_nhan VARCHAR(255) COMMENT 'Đơn vị nhận tiền: GRAB, SHOPEE, CGV...',
    mo_ta TEXT COMMENT 'Mô tả giao dịch',
    ngay_giao_dich DATETIME NOT NULL,
    -- Thông tin SMS
    tu_dong BOOLEAN DEFAULT FALSE COMMENT 'TRUE: từ SMS, FALSE: nhập tay',
    noi_dung_sms_ma_hoa TEXT COMMENT 'Nội dung SMS gốc (mã hóa)',
    -- Trạng thái
    da_xac_nhan BOOLEAN DEFAULT FALSE COMMENT 'Người dùng đã xác nhận',
    bat_thuong BOOLEAN DEFAULT FALSE COMMENT 'Giao dịch bất thường',
    -- AI Categorization
    nguon_phan_loai VARCHAR(20) COMMENT 'Nguồn phân loại: AI, RULE_BASED, USER',
    do_tin_cay_ai DOUBLE COMMENT 'Độ tin cậy AI (0.0 - 1.0)',
    -- Metadata
    anh_hoa_don VARCHAR(500) COMMENT 'Ảnh hóa đơn/bill',
    ghi_chu TEXT COMMENT 'Ghi chú của người dùng',
    ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ngay_cap_nhat TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_nguoi_dung) REFERENCES nguoi_dung(id) ON DELETE CASCADE,
    FOREIGN KEY (id_danh_muc) REFERENCES danh_muc(id) ON DELETE
    SET NULL,
        INDEX idx_nguoi_dung_ngay (id_nguoi_dung, ngay_giao_dich),
        INDEX idx_danh_muc (id_danh_muc),
        INDEX idx_loai (loai),
        INDEX idx_nguon_giao_dich (nguon_giao_dich),
        INDEX idx_don_vi_chap_nhan (don_vi_chap_nhan),
        INDEX idx_ngay_giao_dich (ngay_giao_dich)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
-- =====================================================
-- 4. BẢNG NGAN_SACH (BUDGETS) - Ngân sách
-- =====================================================
CREATE TABLE IF NOT EXISTS ngan_sach (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_nguoi_dung BIGINT NOT NULL,
    id_danh_muc BIGINT NULL COMMENT 'NULL = tổng ngân sách',
    ten_ngan_sach VARCHAR(100) NOT NULL,
    so_tien DECIMAL(15, 2) NOT NULL,
    ky_han ENUM('WEEKLY', 'MONTHLY', 'QUARTERLY', 'YEARLY') DEFAULT 'MONTHLY',
    ngay_bat_dau DATE NOT NULL,
    ngay_ket_thuc DATE NOT NULL,
    dang_hoat_dong BOOLEAN DEFAULT TRUE,
    nguong_canh_bao INT DEFAULT 70 COMMENT 'Cảnh báo khi đạt % này',
    ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ngay_cap_nhat TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_nguoi_dung) REFERENCES nguoi_dung(id) ON DELETE CASCADE,
    FOREIGN KEY (id_danh_muc) REFERENCES danh_muc(id) ON DELETE
    SET NULL,
        INDEX idx_nguoi_dung_ky_han (id_nguoi_dung, ky_han),
        INDEX idx_ngay (ngay_bat_dau, ngay_ket_thuc)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
-- =====================================================
-- 5. BẢNG MUC_TIEU_TIET_KIEM (SAVINGS_GOALS) - Hũ tiết kiệm
-- =====================================================
CREATE TABLE IF NOT EXISTS muc_tieu_tiet_kiem (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_nguoi_dung BIGINT NOT NULL,
    ten_muc_tieu VARCHAR(200) NOT NULL COMMENT 'Ví dụ: Mua tai nghe, Du lịch Đà Lạt',
    mo_ta TEXT,
    so_tien_muc_tieu DECIMAL(15, 2) NOT NULL,
    so_tien_hien_tai DECIMAL(15, 2) DEFAULT 0.00,
    han_chot DATE NULL,
    trang_thai ENUM('ACTIVE', 'COMPLETED', 'CANCELLED') DEFAULT 'ACTIVE',
    ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ngay_cap_nhat TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    ngay_hoan_thanh TIMESTAMP NULL,
    FOREIGN KEY (id_nguoi_dung) REFERENCES nguoi_dung(id) ON DELETE CASCADE,
    INDEX idx_nguoi_dung_trang_thai (id_nguoi_dung, trang_thai),
    INDEX idx_han_chot (han_chot)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
-- =====================================================
-- 6. BẢNG DONG_GOP_TIET_KIEM (SAVINGS_CONTRIBUTIONS)
-- =====================================================
CREATE TABLE IF NOT EXISTS dong_gop_tiet_kiem (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_muc_tieu BIGINT NOT NULL,
    so_tien DECIMAL(15, 2) NOT NULL,
    ngay_dong_gop DATE NOT NULL,
    ghi_chu TEXT,
    ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_muc_tieu) REFERENCES muc_tieu_tiet_kiem(id) ON DELETE CASCADE,
    INDEX idx_muc_tieu (id_muc_tieu),
    INDEX idx_ngay (ngay_dong_gop)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
-- =====================================================
-- 7. BẢNG GIAO_DICH_DINH_KY (RECURRING_TRANSACTIONS)
-- =====================================================
CREATE TABLE IF NOT EXISTS giao_dich_dinh_ky (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_nguoi_dung BIGINT NOT NULL,
    id_danh_muc BIGINT NOT NULL,
    so_tien DECIMAL(15, 2) NOT NULL COMMENT 'Số tiền dự kiến',
    tan_suat ENUM('DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY') NOT NULL DEFAULT 'MONTHLY',
    ngay_bat_dau DATE NOT NULL,
    ngay_ket_thuc DATE NULL,
    lan_tiep_theo DATE NOT NULL COMMENT 'Ngày dự kiến tiếp theo',
    mo_ta VARCHAR(255),
    dang_hoat_dong BOOLEAN DEFAULT TRUE,
    ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ngay_cap_nhat TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_nguoi_dung) REFERENCES nguoi_dung(id) ON DELETE CASCADE,
    FOREIGN KEY (id_danh_muc) REFERENCES danh_muc(id) ON DELETE CASCADE,
    INDEX idx_nguoi_dung_lan_tiep (id_nguoi_dung, lan_tiep_theo)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
-- =====================================================
-- 8. BẢNG BO_PHAN_TICH_SMS (SMS_PARSERS)
-- =====================================================
CREATE TABLE IF NOT EXISTS bo_phan_tich_sms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ten_ngan_hang VARCHAR(100) NOT NULL,
    ma_ngan_hang VARCHAR(20) NOT NULL COMMENT 'VCB, TCB, ACB...',
    so_dien_thoai_gui VARCHAR(20) COMMENT 'Số điện thoại gửi SMS',
    mau_regex TEXT NOT NULL COMMENT 'Pattern để extract thông tin',
    anh_xa_truong JSON COMMENT 'Mapping các field: amount, type, merchant, time...',
    sms_mau TEXT COMMENT 'SMS mẫu để test',
    dang_hoat_dong BOOLEAN DEFAULT TRUE,
    do_uu_tien INT DEFAULT 0 COMMENT 'Thứ tự ưu tiên khi match',
    ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ngay_cap_nhat TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_ma_ngan_hang (ma_ngan_hang),
    INDEX idx_dang_hoat_dong (dang_hoat_dong)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
-- =====================================================
-- 9. BẢNG QUY_TAC_DANH_MUC (CATEGORY_RULES)
-- =====================================================
CREATE TABLE IF NOT EXISTS quy_tac_danh_muc (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tu_khoa VARCHAR(255) NOT NULL COMMENT 'Từ khóa: GRAB, SHOPEE, CGV...',
    id_danh_muc BIGINT NOT NULL,
    loai_khop ENUM(
        'EXACT',
        'CONTAINS',
        'STARTS_WITH',
        'ENDS_WITH',
        'REGEX'
    ) DEFAULT 'CONTAINS',
    do_uu_tien INT DEFAULT 0,
    dang_hoat_dong BOOLEAN DEFAULT TRUE,
    ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_danh_muc) REFERENCES danh_muc(id) ON DELETE CASCADE,
    INDEX idx_tu_khoa (tu_khoa),
    INDEX idx_danh_muc (id_danh_muc)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
-- =====================================================
-- 10. BẢNG THONG_BAO (NOTIFICATIONS)
-- =====================================================
CREATE TABLE IF NOT EXISTS thong_bao (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_nguoi_dung BIGINT NOT NULL,
    loai VARCHAR(50) NOT NULL COMMENT 'BUDGET_ALERT, SAVING_TIP, ANOMALY, REPORT...',
    tieu_de VARCHAR(255) NOT NULL,
    noi_dung TEXT,
    duong_dan_hanh_dong VARCHAR(500) COMMENT 'Deep link trong app',
    da_doc BOOLEAN DEFAULT FALSE,
    thoi_gian_doc TIMESTAMP NULL,
    do_uu_tien ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') DEFAULT 'MEDIUM',
    ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_nguoi_dung) REFERENCES nguoi_dung(id) ON DELETE CASCADE,
    INDEX idx_nguoi_dung_da_doc (id_nguoi_dung, da_doc),
    INDEX idx_ngay_tao (ngay_tao)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
-- =====================================================
-- 11. BẢNG CAI_DAT_NGUOI_DUNG (USER_PREFERENCES)
-- =====================================================
CREATE TABLE IF NOT EXISTS cai_dat_nguoi_dung (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_nguoi_dung BIGINT NOT NULL UNIQUE,
    don_vi_tien_te VARCHAR(3) DEFAULT 'VND',
    ngon_ngu VARCHAR(10) DEFAULT 'vi',
    bat_thong_bao BOOLEAN DEFAULT TRUE,
    thong_bao_email BOOLEAN DEFAULT TRUE,
    thong_bao_day BOOLEAN DEFAULT TRUE,
    canh_bao_giao_dich BOOLEAN DEFAULT TRUE COMMENT 'Cảnh báo giao dịch mới',
    canh_bao_ngan_sach BOOLEAN DEFAULT TRUE COMMENT 'Cảnh báo vượt ngân sách',
    nhac_nho_muc_tieu BOOLEAN DEFAULT TRUE COMMENT 'Nhắc nhở mục tiêu tiết kiệm',
    bao_cao_tuan BOOLEAN DEFAULT FALSE COMMENT 'Báo cáo chi tiêu hàng tuần',
    bao_cao_thang BOOLEAN DEFAULT TRUE COMMENT 'Báo cáo chi tiêu hàng tháng',
    nguong_canh_bao_ngan_sach INT DEFAULT 70,
    giao_dien VARCHAR(20) DEFAULT 'light',
    ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ngay_cap_nhat TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_nguoi_dung) REFERENCES nguoi_dung(id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
-- =====================================================
-- 12. BẢNG PHAN_TICH_CHI_TIEU (SPENDING_INSIGHTS)
-- =====================================================
CREATE TABLE IF NOT EXISTS phan_tich_chi_tieu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_nguoi_dung BIGINT NOT NULL,
    loai_ky_han ENUM('DAILY', 'WEEKLY', 'MONTHLY') NOT NULL,
    ngay_bat_dau DATE NOT NULL,
    ngay_ket_thuc DATE NOT NULL,
    tong_thu_nhap DECIMAL(15, 2) DEFAULT 0.00,
    tong_chi_tieu DECIMAL(15, 2) DEFAULT 0.00,
    id_danh_muc_hang_dau BIGINT NULL COMMENT 'Category chi nhiều nhất',
    so_tien_danh_muc_hang_dau DECIMAL(15, 2),
    du_lieu_phan_tich JSON COMMENT 'Chi tiết phân tích',
    thoi_gian_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_nguoi_dung) REFERENCES nguoi_dung(id) ON DELETE CASCADE,
    FOREIGN KEY (id_danh_muc_hang_dau) REFERENCES danh_muc(id) ON DELETE
    SET NULL,
        INDEX idx_nguoi_dung_ky_han (id_nguoi_dung, ngay_bat_dau, ngay_ket_thuc),
        UNIQUE KEY unique_nguoi_dung_ky_han (id_nguoi_dung, loai_ky_han, ngay_bat_dau)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
-- =====================================================
-- 13. BẢNG NHAT_KY_HE_THONG (AUDIT_LOGS)
-- =====================================================
CREATE TABLE IF NOT EXISTS nhat_ky_he_thong (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_nguoi_dung BIGINT NULL,
    hanh_dong VARCHAR(100) NOT NULL,
    loai_doi_tuong VARCHAR(50) NOT NULL,
    id_doi_tuong BIGINT NULL,
    gia_tri_cu JSON,
    gia_tri_moi JSON,
    ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_nguoi_dung) REFERENCES nguoi_dung(id) ON DELETE
    SET NULL,
        INDEX idx_nguoi_dung (id_nguoi_dung),
        INDEX idx_ngay_tao (ngay_tao),
        INDEX idx_doi_tuong (loai_doi_tuong, id_doi_tuong)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
CREATE TABLE IF NOT EXISTS mau_thong_bao (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ma_mau VARCHAR(20) NOT NULL UNIQUE COMMENT 'Template code: NOT001, NOT002...',
    tieu_de VARCHAR(255) NOT NULL COMMENT 'Tiêu đề thông báo',
    noi_dung_mau TEXT NOT NULL COMMENT 'Message template với placeholders {amount}, {category}...',
    loai ENUM('WARNING', 'ALERT', 'SUCCESS', 'INFO') NOT NULL DEFAULT 'INFO' COMMENT 'Loại thông báo',
    so_lan_gui INT DEFAULT 0 COMMENT 'Số lần đã gửi',
    trang_thai ENUM('ACTIVE', 'INACTIVE', 'DRAFT') NOT NULL DEFAULT 'DRAFT' COMMENT 'Trạng thái',
    nguoi_tao BIGINT COMMENT 'ID admin tạo',
    ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ngay_cap_nhat TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_template_status (trang_thai),
    INDEX idx_template_type (loai),
    FOREIGN KEY (nguoi_tao) REFERENCES nguoi_dung(id) ON DELETE
    SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
-- . Bảng Mẹo và Gợi ý (Tips & Suggestions)
CREATE TABLE IF NOT EXISTS meo_goi_y (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ma_tip VARCHAR(20) NOT NULL UNIQUE COMMENT 'Tip code: TIP001, TIP002...',
    tieu_de VARCHAR(255) NOT NULL COMMENT 'Tiêu đề tip',
    noi_dung TEXT NOT NULL COMMENT 'Nội dung chi tiết',
    danh_muc ENUM(
        'SAVING',
        'BUDGETING',
        'INVESTING',
        'SPENDING',
        'GENERAL'
    ) DEFAULT 'GENERAL' COMMENT 'Danh mục',
    icon VARCHAR(100) COMMENT 'Icon name hoặc emoji',
    luot_xem INT DEFAULT 0 COMMENT 'Số lượt xem',
    luot_thich INT DEFAULT 0 COMMENT 'Số lượt thích',
    thu_tu INT DEFAULT 0 COMMENT 'Thứ tự hiển thị',
    trang_thai ENUM('ACTIVE', 'INACTIVE', 'DRAFT') NOT NULL DEFAULT 'DRAFT' COMMENT 'Trạng thái',
    nguoi_tao BIGINT COMMENT 'ID admin tạo',
    ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ngay_cap_nhat TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tip_status (trang_thai),
    INDEX idx_tip_category (danh_muc),
    INDEX idx_tip_order (thu_tu),
    FOREIGN KEY (nguoi_tao) REFERENCES nguoi_dung(id) ON DELETE
    SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
-- . Bảng Câu hỏi Thường gặp (FAQ)
CREATE TABLE IF NOT EXISTS cau_hoi_thuong_gap (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ma_faq VARCHAR(20) NOT NULL UNIQUE COMMENT 'FAQ code: FAQ001, FAQ002...',
    cau_hoi TEXT NOT NULL COMMENT 'Câu hỏi',
    cau_tra_loi TEXT NOT NULL COMMENT 'Câu trả lời',
    danh_muc ENUM(
        'GETTING_STARTED',
        'SECURITY',
        'FEATURES',
        'TROUBLESHOOTING'
    ) DEFAULT 'GETTING_STARTED' COMMENT 'Danh mục',
    luot_xem INT DEFAULT 0 COMMENT 'Số lượt xem',
    co_huu_ich INT DEFAULT 0 COMMENT 'Số lượt đánh giá hữu ích',
    khong_huu_ich INT DEFAULT 0 COMMENT 'Số lượt đánh giá không hữu ích',
    thu_tu INT DEFAULT 0 COMMENT 'Thứ tự hiển thị',
    trang_thai ENUM('ACTIVE', 'INACTIVE', 'DRAFT') NOT NULL DEFAULT 'DRAFT' COMMENT 'Trạng thái',
    nguoi_tao BIGINT COMMENT 'ID admin tạo',
    ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ngay_cap_nhat TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_faq_status (trang_thai),
    INDEX idx_faq_category (danh_muc),
    INDEX idx_faq_order (thu_tu),
    FOREIGN KEY (nguoi_tao) REFERENCES nguoi_dung(id) ON DELETE
    SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Bảng yêu cầu của người dùng (xuất dữ liệu và xóa tài khoản)
CREATE TABLE IF NOT EXISTS yeu_cau_nguoi_dung (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_nguoi_dung BIGINT NOT NULL,
    loai_yeu_cau ENUM('EXPORT_DATA', 'DELETE_ACCOUNT') NOT NULL COMMENT 'Loại yêu cầu: EXPORT_DATA (Xuất dữ liệu), DELETE_ACCOUNT (Xóa tài khoản)',
    trang_thai ENUM('PENDING', 'APPROVED', 'REJECTED', 'COMPLETED') NOT NULL DEFAULT 'PENDING' COMMENT 'Trạng thái: PENDING (Chờ duyệt), APPROVED (Đã duyệt), REJECTED (Từ chối), COMPLETED (Hoàn thành)',
    ly_do_yeu_cau TEXT COMMENT 'Lý do yêu cầu từ người dùng',
    ghi_chu_admin TEXT COMMENT 'Ghi chú từ admin khi duyệt/từ chối',
    ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày tạo yêu cầu',
    ngay_duyet TIMESTAMP NULL COMMENT 'Ngày admin duyệt/từ chối',
    id_admin_duyet BIGINT NULL COMMENT 'ID admin duyệt yêu cầu',
    duong_dan_file VARCHAR(500) NULL COMMENT 'Đường dẫn file PDF đã xuất (nếu là export data)',
    ngay_gui_email TIMESTAMP NULL COMMENT 'Ngày gửi email (nếu là export data)',
    
    FOREIGN KEY (id_nguoi_dung) REFERENCES nguoi_dung(id) ON DELETE CASCADE,
    FOREIGN KEY (id_admin_duyet) REFERENCES nguoi_dung(id) ON DELETE SET NULL,
    
    INDEX idx_nguoi_dung (id_nguoi_dung),
    INDEX idx_loai_yeu_cau (loai_yeu_cau),
    INDEX idx_trang_thai (trang_thai),
    INDEX idx_ngay_tao (ngay_tao)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Bảng lưu yêu cầu xuất dữ liệu và xóa tài khoản';
