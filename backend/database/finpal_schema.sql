-- =====================================================
-- FINPAL DATABASE SCHEMA
-- Ví Thông Minh - Smart Financial Assistant
-- Phiên bản: 1.0
-- Ngày cập nhật: 06/01/2026
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
    bieu_tuong VARCHAR(100) NULL COMMENT 'Tên icon: food, shopping-cart',
    mo_ta TEXT NULL COMMENT 'Mô tả danh mục',
    thu_tu_hien_thi INT DEFAULT 0,
    ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ngay_cap_nhat TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_loai (loai)
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
    ghi_chu TEXT COMMENT 'Ghi chú của người dùng',
    ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ngay_cap_nhat TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_nguoi_dung) REFERENCES nguoi_dung(id) ON DELETE CASCADE,
    FOREIGN KEY (id_danh_muc) REFERENCES danh_muc(id) ON DELETE SET NULL,
    INDEX idx_nguoi_dung_ngay (id_nguoi_dung, ngay_giao_dich),
    INDEX idx_danh_muc (id_danh_muc),
    INDEX idx_loai (loai),
    INDEX idx_nguon_giao_dich (nguon_giao_dich),
    INDEX idx_ngay_giao_dich (ngay_giao_dich)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- =====================================================
-- 4. BẢNG CAI_DAT_HE_THONG (System Settings)
-- Cài đặt hệ thống cho admin quản lý
-- =====================================================
CREATE TABLE IF NOT EXISTS cai_dat_he_thong (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    khoa_cai_dat VARCHAR(100) NOT NULL UNIQUE COMMENT 'Khóa cài đặt: ai.confidence.threshold',
    gia_tri VARCHAR(500) COMMENT 'Giá trị cài đặt',
    mo_ta VARCHAR(255) COMMENT 'Mô tả cài đặt',
    nhom_cai_dat VARCHAR(50) COMMENT 'Nhóm: AI, NOTIFICATION, SYSTEM',
    kieu_du_lieu VARCHAR(20) COMMENT 'Kiểu: STRING, INTEGER, DOUBLE, BOOLEAN',
    ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ngay_cap_nhat TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    nguoi_cap_nhat BIGINT COMMENT 'ID admin cập nhật cuối',
    FOREIGN KEY (nguoi_cap_nhat) REFERENCES nguoi_dung(id) ON DELETE SET NULL,
    INDEX idx_nhom_cai_dat (nhom_cai_dat),
    INDEX idx_khoa_cai_dat (khoa_cai_dat)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Cài đặt hệ thống cho admin';

-- =====================================================
-- 5. BẢNG CAI_DAT_THONG_BAO (Notification Settings)
-- Cài đặt bật/tắt từng loại thông báo của người dùng
-- =====================================================
CREATE TABLE IF NOT EXISTS cai_dat_thong_bao (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_nguoi_dung BIGINT NOT NULL UNIQUE,
    canh_bao_giao_dich BOOLEAN DEFAULT TRUE COMMENT 'Cảnh báo giao dịch mới',
    canh_bao_ngan_sach BOOLEAN DEFAULT TRUE COMMENT 'Cảnh báo vượt ngân sách',
    nhac_nho_muc_tieu BOOLEAN DEFAULT TRUE COMMENT 'Nhắc nhở mục tiêu tiết kiệm',
    canh_bao_bao_mat BOOLEAN DEFAULT TRUE COMMENT 'Cảnh báo bảo mật',
    bao_cao_tuan BOOLEAN DEFAULT TRUE COMMENT 'Báo cáo chi tiêu hàng tuần',
    bao_cao_thang BOOLEAN DEFAULT TRUE COMMENT 'Báo cáo chi tiêu hàng tháng',
    goi_y_tiet_kiem BOOLEAN DEFAULT TRUE COMMENT 'Gợi ý tiết kiệm từ AI',
    phan_tich_chi_tieu BOOLEAN DEFAULT TRUE COMMENT 'Phân tích chi tiêu từ AI',
    ngay_tao DATETIME DEFAULT CURRENT_TIMESTAMP,
    ngay_cap_nhat DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_nguoi_dung) REFERENCES nguoi_dung(id) ON DELETE CASCADE,
    INDEX idx_notification_settings_user (id_nguoi_dung)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Cài đặt thông báo của người dùng';

-- =====================================================
-- 6. BẢNG NGAN_SACH (BUDGETS) - Ngân sách
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
    FOREIGN KEY (id_danh_muc) REFERENCES danh_muc(id) ON DELETE SET NULL,
    INDEX idx_nguoi_dung_ky_han (id_nguoi_dung, ky_han),
    INDEX idx_ngay (ngay_bat_dau, ngay_ket_thuc)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- =====================================================
-- 7. BẢNG MUC_TIEU_TIET_KIEM (SAVINGS_GOALS) - Hũ tiết kiệm
-- =====================================================
CREATE TABLE IF NOT EXISTS muc_tieu_tiet_kiem (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_nguoi_dung BIGINT NOT NULL,
    ten_muc_tieu VARCHAR(200) NOT NULL COMMENT 'Ví dụ: Mua tai nghe, Du lịch Đà Lạt',
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
-- 8. BẢNG DONG_GOP_TIET_KIEM (SAVINGS_CONTRIBUTIONS)
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
-- 9. BẢNG BO_PHAN_TICH_SMS (SMS_PARSERS)
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
-- 10. BẢNG QUY_TAC_DANH_MUC (CATEGORY_RULES)
-- =====================================================
CREATE TABLE IF NOT EXISTS quy_tac_danh_muc (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tu_khoa VARCHAR(255) NOT NULL COMMENT 'Từ khóa: GRAB, SHOPEE, CGV...',
    id_danh_muc BIGINT NOT NULL,
    loai_khop ENUM('EXACT', 'CONTAINS', 'STARTS_WITH', 'ENDS_WITH', 'REGEX') DEFAULT 'CONTAINS',
    do_uu_tien INT DEFAULT 0,
    dang_hoat_dong BOOLEAN DEFAULT TRUE,
    ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ngay_cap_nhat DATETIME(6) NULL,
    FOREIGN KEY (id_danh_muc) REFERENCES danh_muc(id) ON DELETE CASCADE,
    INDEX idx_tu_khoa (tu_khoa),
    INDEX idx_danh_muc (id_danh_muc)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- =====================================================
-- 11. BẢNG THONG_BAO (NOTIFICATIONS)
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
    FOREIGN KEY (id_danh_muc_hang_dau) REFERENCES danh_muc(id) ON DELETE SET NULL,
    INDEX idx_nguoi_dung_ky_han (id_nguoi_dung, ngay_bat_dau, ngay_ket_thuc),
    UNIQUE KEY unique_nguoi_dung_ky_han (id_nguoi_dung, loai_ky_han, ngay_bat_dau)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- =====================================================
-- 13. BẢNG MAU_THONG_BAO (NOTIFICATION_TEMPLATES)
-- =====================================================
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
    FOREIGN KEY (nguoi_tao) REFERENCES nguoi_dung(id) ON DELETE SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- =====================================================
-- 14. BẢNG MEO_GOI_Y (Tips & Suggestions)
-- =====================================================
CREATE TABLE IF NOT EXISTS meo_goi_y (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ma_tip VARCHAR(20) NOT NULL UNIQUE COMMENT 'Tip code: TIP001, TIP002...',
    tieu_de VARCHAR(255) NOT NULL COMMENT 'Tiêu đề tip',
    noi_dung TEXT NOT NULL COMMENT 'Nội dung chi tiết',
    danh_muc ENUM('SAVING', 'BUDGETING', 'INVESTING', 'SPENDING', 'GENERAL') DEFAULT 'GENERAL' COMMENT 'Danh mục',
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
    FOREIGN KEY (nguoi_tao) REFERENCES nguoi_dung(id) ON DELETE SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- =====================================================
-- 15. BẢNG CAU_HOI_THUONG_GAP (FAQ)
-- =====================================================
CREATE TABLE IF NOT EXISTS cau_hoi_thuong_gap (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ma_faq VARCHAR(20) NOT NULL UNIQUE COMMENT 'FAQ code: FAQ001, FAQ002...',
    cau_hoi TEXT NOT NULL COMMENT 'Câu hỏi',
    cau_tra_loi TEXT NOT NULL COMMENT 'Câu trả lời',
    danh_muc ENUM('GETTING_STARTED', 'SECURITY', 'FEATURES', 'TROUBLESHOOTING') DEFAULT 'GETTING_STARTED' COMMENT 'Danh mục',
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
    FOREIGN KEY (nguoi_tao) REFERENCES nguoi_dung(id) ON DELETE SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- =====================================================
-- 16. BẢNG YEU_CAU_NGUOI_DUNG (User Requests)
-- Yêu cầu xuất dữ liệu và xóa tài khoản từ người dùng
-- =====================================================
CREATE TABLE IF NOT EXISTS yeu_cau_nguoi_dung (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_nguoi_dung BIGINT NOT NULL,
    loai_yeu_cau ENUM('EXPORT_DATA', 'DELETE_ACCOUNT') NOT NULL COMMENT 'Loại yêu cầu',
    trang_thai ENUM('PENDING', 'APPROVED', 'REJECTED', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'PENDING' COMMENT 'Trạng thái',
    ly_do_yeu_cau TEXT COMMENT 'Lý do yêu cầu từ người dùng',
    ghi_chu_admin TEXT COMMENT 'Ghi chú từ admin khi duyệt/từ chối',
    ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày tạo yêu cầu',
    ngay_duyet TIMESTAMP NULL COMMENT 'Ngày admin duyệt/từ chối',
    id_admin_duyet BIGINT NULL COMMENT 'ID admin duyệt yêu cầu',
    duong_dan_file VARCHAR(500) NULL COMMENT 'Đường dẫn file PDF đã xuất (nếu là export data)',
    ngay_gui_email TIMESTAMP NULL COMMENT 'Ngày gửi email (nếu là export data)',
    ngay_du_kien_xoa DATETIME NULL COMMENT 'Thời gian dự kiến xóa tài khoản (24h sau khi phê duyệt)',
    
    FOREIGN KEY (id_nguoi_dung) REFERENCES nguoi_dung(id) ON DELETE CASCADE,
    FOREIGN KEY (id_admin_duyet) REFERENCES nguoi_dung(id) ON DELETE SET NULL,
    
    INDEX idx_nguoi_dung (id_nguoi_dung),
    INDEX idx_loai_yeu_cau (loai_yeu_cau),
    INDEX idx_trang_thai (trang_thai),
    INDEX idx_ngay_tao (ngay_tao),
    INDEX idx_scheduled_deletion (ngay_du_kien_xoa),
    INDEX idx_deletion_ready (loai_yeu_cau, trang_thai, ngay_du_kien_xoa)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Bảng lưu yêu cầu xuất dữ liệu và xóa tài khoản';

-- =====================================================
-- 17. BẢNG LICH_SU_XOA_TAI_KHOAN (Deleted Account History)
-- Lưu trữ lịch sử các tài khoản đã bị xóa
-- =====================================================
CREATE TABLE IF NOT EXISTS lich_su_xoa_tai_khoan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_nguoi_dung_bi_xoa BIGINT NOT NULL COMMENT 'ID gốc của người dùng bị xóa',
    ten_nguoi_dung VARCHAR(100) NOT NULL COMMENT 'Tên đăng nhập',
    email VARCHAR(200) NOT NULL COMMENT 'Email đã đăng ký',
    ho_ten VARCHAR(200) COMMENT 'Họ và tên đầy đủ',
    id_yeu_cau BIGINT NOT NULL COMMENT 'ID yêu cầu xóa tài khoản',
    ly_do_yeu_cau TEXT COMMENT 'Lý do yêu cầu xóa từ người dùng',
    ngay_yeu_cau DATETIME NOT NULL COMMENT 'Ngày gửi yêu cầu',
    id_admin_duyet BIGINT COMMENT 'ID admin phê duyệt',
    ten_admin_duyet VARCHAR(100) COMMENT 'Tên admin phê duyệt',
    ghi_chu_admin TEXT COMMENT 'Ghi chú từ admin',
    ngay_duyet DATETIME COMMENT 'Ngày phê duyệt',
    ngay_xoa DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày thực hiện xóa',
    da_gui_email_thong_bao BOOLEAN DEFAULT FALSE COMMENT 'Đã gửi email thông báo',
    trang_thai_xoa VARCHAR(20) DEFAULT 'COMPLETED' COMMENT 'Trạng thái: COMPLETED, FAILED',
    thong_bao_loi TEXT COMMENT 'Thông báo lỗi nếu có',
    
    INDEX idx_deleted_user_id (id_nguoi_dung_bi_xoa),
    INDEX idx_username (ten_nguoi_dung),
    INDEX idx_email (email),
    INDEX idx_ngay_xoa (ngay_xoa)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Lịch sử các tài khoản đã bị xóa';

-- =====================================================
-- 18. BẢNG LICH_SU_SAO_LUU (BACKUP_HISTORY)
-- =====================================================
CREATE TABLE IF NOT EXISTS lich_su_sao_luu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ten_file VARCHAR(255) NOT NULL COMMENT 'Tên file backup',
    duong_dan_file VARCHAR(500) NOT NULL COMMENT 'Đường dẫn file backup',
    kich_thuoc_file BIGINT COMMENT 'Kích thước file (bytes)',
    loai_sao_luu ENUM('FULL', 'MANUAL', 'SCHEDULED') NOT NULL DEFAULT 'MANUAL' COMMENT 'Loại backup',
    trang_thai ENUM('IN_PROGRESS', 'COMPLETED', 'FAILED') NOT NULL DEFAULT 'IN_PROGRESS' COMMENT 'Trạng thái',
    thong_bao_loi TEXT COMMENT 'Thông báo lỗi nếu có',
    ngay_tao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày tạo backup',
    hoan_thanh_luc DATETIME COMMENT 'Thời gian hoàn thành',
    id_nguoi_tao BIGINT COMMENT 'ID admin tạo backup',
    ghi_chu VARCHAR(500) COMMENT 'Ghi chú',
    
    INDEX idx_trang_thai (trang_thai),
    INDEX idx_loai_sao_luu (loai_sao_luu),
    INDEX idx_ngay_tao (ngay_tao),
    INDEX idx_nguoi_tao (id_nguoi_tao),
    
    FOREIGN KEY (id_nguoi_tao) REFERENCES nguoi_dung(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Lưu trữ lịch sử các lần backup database';

-- =====================================================
-- 19. BẢNG VAI_TRO_ADMIN (Admin Roles)
-- =====================================================
CREATE TABLE IF NOT EXISTS vai_tro_admin (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ma_vai_tro VARCHAR(50) NOT NULL UNIQUE COMMENT 'Mã vai trò: SUPER_ADMIN, MODERATOR',
    ten_vai_tro VARCHAR(100) NOT NULL COMMENT 'Tên vai trò hiển thị',
    mo_ta VARCHAR(255) COMMENT 'Mô tả vai trò',
    mau_sac VARCHAR(20) DEFAULT '#2196F3' COMMENT 'Màu hiển thị badge',
    thu_tu INT DEFAULT 0 COMMENT 'Thứ tự hiển thị',
    dang_hoat_dong BOOLEAN DEFAULT TRUE,
    ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ngay_cap_nhat TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_ma_vai_tro (ma_vai_tro),
    INDEX idx_dang_hoat_dong (dang_hoat_dong)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Bảng vai trò admin';

-- =====================================================
-- 20. BẢNG QUYEN_HAN (Permissions)
-- =====================================================
CREATE TABLE IF NOT EXISTS quyen_han (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ma_quyen VARCHAR(50) NOT NULL UNIQUE COMMENT 'Mã quyền: VIEW_USERS, EDIT_CONTENT',
    ten_quyen VARCHAR(100) NOT NULL COMMENT 'Tên quyền hiển thị',
    nhom_quyen VARCHAR(50) COMMENT 'Nhóm quyền: USER_MANAGEMENT, CONTENT, SYSTEM',
    mo_ta VARCHAR(255) COMMENT 'Mô tả chi tiết',
    thu_tu INT DEFAULT 0 COMMENT 'Thứ tự hiển thị',
    ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_ma_quyen (ma_quyen),
    INDEX idx_nhom_quyen (nhom_quyen)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Bảng danh sách quyền hạn trong hệ thống';

-- =====================================================
-- 21. BẢNG VAI_TRO_QUYEN (Role-Permission Mapping)
-- =====================================================
CREATE TABLE IF NOT EXISTS vai_tro_quyen (
    id_vai_tro BIGINT NOT NULL,
    id_quyen BIGINT NOT NULL,
    PRIMARY KEY (id_vai_tro, id_quyen),
    FOREIGN KEY (id_vai_tro) REFERENCES vai_tro_admin(id) ON DELETE CASCADE,
    FOREIGN KEY (id_quyen) REFERENCES quyen_han(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Bảng mapping vai trò - quyền hạn';

-- =====================================================
-- 22. BẢNG NGUOI_DUNG_ADMIN (Admin User Details)
-- =====================================================
CREATE TABLE IF NOT EXISTS nguoi_dung_admin (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_nguoi_dung BIGINT NOT NULL UNIQUE,
    id_vai_tro BIGINT NOT NULL,
    ten_hien_thi VARCHAR(100) COMMENT 'Tên hiển thị trong admin panel',
    ghi_chu TEXT,
    ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ngay_cap_nhat TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_nguoi_dung) REFERENCES nguoi_dung(id) ON DELETE CASCADE,
    FOREIGN KEY (id_vai_tro) REFERENCES vai_tro_admin(id) ON DELETE RESTRICT,
    INDEX idx_nguoi_dung (id_nguoi_dung),
    INDEX idx_vai_tro (id_vai_tro)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Thông tin chi tiết admin users';

-- =====================================================
-- 23. BẢNG LICH_SU_DANG_NHAP (Login History)
-- =====================================================
CREATE TABLE IF NOT EXISTS lich_su_dang_nhap (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_nguoi_dung BIGINT NOT NULL,
    thoi_gian_dang_nhap TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    dia_chi_ip VARCHAR(50) COMMENT 'Địa chỉ IP',
    ten_thiet_bi VARCHAR(255) COMMENT 'Tên thiết bị/trình duyệt',
    he_dieu_hanh VARCHAR(100) COMMENT 'Hệ điều hành',
    trang_thai ENUM('SUCCESS', 'FAILED', 'BLOCKED') DEFAULT 'SUCCESS',
    ly_do_that_bai VARCHAR(255) COMMENT 'Lý do đăng nhập thất bại',
    FOREIGN KEY (id_nguoi_dung) REFERENCES nguoi_dung(id) ON DELETE CASCADE,
    INDEX idx_nguoi_dung (id_nguoi_dung),
    INDEX idx_thoi_gian (thoi_gian_dang_nhap),
    INDEX idx_trang_thai (trang_thai)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Lịch sử đăng nhập người dùng';

-- =====================================================
-- 24. BẢNG LICH_SU_HOAT_DONG_ADMIN (Admin Activity Log)
-- =====================================================
CREATE TABLE IF NOT EXISTS lich_su_hoat_dong_admin (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_admin BIGINT NOT NULL,
    hanh_dong VARCHAR(100) NOT NULL COMMENT 'CREATE, UPDATE, DELETE, APPROVE, REJECT...',
    loai_doi_tuong VARCHAR(50) NOT NULL COMMENT 'USER, CATEGORY, BUDGET...',
    id_doi_tuong BIGINT COMMENT 'ID của đối tượng bị tác động',
    du_lieu_cu JSON COMMENT 'Dữ liệu trước khi thay đổi',
    du_lieu_moi JSON COMMENT 'Dữ liệu sau khi thay đổi',
    dia_chi_ip VARCHAR(50),
    ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_admin) REFERENCES nguoi_dung(id) ON DELETE CASCADE,
    INDEX idx_admin (id_admin),
    INDEX idx_ngay_tao (ngay_tao),
    INDEX idx_doi_tuong (loai_doi_tuong, id_doi_tuong)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Nhật ký hoạt động của admin';

-- =====================================================
-- KẾT THÚC SCHEMA - FINPAL DATABASE
-- Tổng cộng: 24 bảng
-- =====================================================