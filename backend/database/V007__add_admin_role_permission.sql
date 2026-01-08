-- =====================================================
-- HỆ THỐNG PHÂN QUYỀN ADMIN
-- Bao gồm: Quyền hạn, Vai trò, Admin User
-- =====================================================

USE finpal_db;

-- =====================================================
-- BẢNG QUYEN_HAN (PERMISSIONS)
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='Bảng danh sách quyền hạn trong hệ thống';

-- =====================================================
-- BẢNG VAI_TRO_ADMIN (ADMIN ROLES)
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='Bảng vai trò admin';

-- =====================================================
-- BẢNG VAI_TRO_QUYEN (ROLE-PERMISSION MAPPING)
-- =====================================================
CREATE TABLE IF NOT EXISTS vai_tro_quyen (
    id_vai_tro BIGINT NOT NULL,
    id_quyen BIGINT NOT NULL,
    PRIMARY KEY (id_vai_tro, id_quyen),
    
    FOREIGN KEY (id_vai_tro) REFERENCES vai_tro_admin(id) ON DELETE CASCADE,
    FOREIGN KEY (id_quyen) REFERENCES quyen_han(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='Bảng mapping vai trò - quyền hạn';

-- =====================================================
-- BẢNG NGUOI_DUNG_ADMIN (ADMIN USERS)
-- =====================================================
CREATE TABLE IF NOT EXISTS nguoi_dung_admin (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_nguoi_dung BIGINT UNIQUE COMMENT 'Liên kết với bảng nguoi_dung',
    id_vai_tro BIGINT COMMENT 'Vai trò admin',
    ten_hien_thi VARCHAR(100) COMMENT 'Tên hiển thị riêng cho admin',
    ghi_chu VARCHAR(500),
    dang_hoat_dong BOOLEAN DEFAULT TRUE,
    ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ngay_cap_nhat TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    hoat_dong_lan_cuoi TIMESTAMP NULL,
    
    INDEX idx_id_nguoi_dung (id_nguoi_dung),
    INDEX idx_id_vai_tro (id_vai_tro),
    
    FOREIGN KEY (id_nguoi_dung) REFERENCES nguoi_dung(id) ON DELETE CASCADE,
    FOREIGN KEY (id_vai_tro) REFERENCES vai_tro_admin(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='Bảng thông tin admin users';

-- =====================================================
-- DỮ LIỆU MẪU - QUYỀN HẠN
-- =====================================================
INSERT INTO quyen_han (ma_quyen, ten_quyen, nhom_quyen, mo_ta, thu_tu) VALUES
-- System
('ALL', 'Tất cả quyền', 'SYSTEM', 'Super Admin - có tất cả quyền', 0),

-- Dashboard
('VIEW_DASHBOARD', 'Xem Dashboard', 'DASHBOARD', 'Xem tổng quan hệ thống', 1),
('VIEW_ANALYTICS', 'Xem Analytics', 'DASHBOARD', 'Xem báo cáo phân tích', 2),

-- User Management
('VIEW_USERS', 'Xem người dùng', 'USER_MANAGEMENT', 'Xem danh sách và chi tiết người dùng', 10),
('EDIT_USERS', 'Sửa người dùng', 'USER_MANAGEMENT', 'Cập nhật thông tin người dùng', 11),
('DELETE_USERS', 'Xóa người dùng', 'USER_MANAGEMENT', 'Xóa/vô hiệu hóa người dùng', 12),
('RESET_PASSWORD', 'Reset mật khẩu', 'USER_MANAGEMENT', 'Đặt lại mật khẩu người dùng', 13),

-- Category
('VIEW_CATEGORIES', 'Xem danh mục', 'CATEGORY', 'Xem danh sách danh mục', 20),
('EDIT_CATEGORIES', 'Sửa danh mục', 'CATEGORY', 'Thêm/sửa/xóa danh mục', 21),

-- SMS Parser
('VIEW_SMS_PARSERS', 'Xem SMS Parser', 'SMS_PARSER', 'Xem cấu hình SMS parser', 30),
('EDIT_SMS_PARSERS', 'Sửa SMS Parser', 'SMS_PARSER', 'Thêm/sửa/xóa SMS parser', 31),

-- AI Model
('VIEW_AI_MODEL', 'Xem AI Model', 'AI', 'Xem thông tin mô hình AI', 40),
('MANAGE_AI_MODEL', 'Quản lý AI Model', 'AI', 'Cấu hình và huấn luyện AI', 41),

-- Content
('VIEW_CONTENT', 'Xem nội dung', 'CONTENT', 'Xem tips, FAQs, templates', 50),
('EDIT_CONTENT', 'Sửa nội dung', 'CONTENT', 'Thêm/sửa/xóa nội dung', 51),

-- Audit
('VIEW_LOGS', 'Xem Logs', 'AUDIT', 'Xem logs hệ thống', 60),
('VIEW_AUDIT', 'Xem Audit', 'AUDIT', 'Xem lịch sử audit', 61),

-- System Management
('MANAGE_SYSTEM', 'Quản lý hệ thống', 'SYSTEM', 'Cấu hình hệ thống', 70),
('MANAGE_ROLES', 'Quản lý vai trò', 'SYSTEM', 'Thêm/sửa/xóa vai trò admin', 71),
('MANAGE_BACKUP', 'Quản lý backup', 'SYSTEM', 'Sao lưu và phục hồi dữ liệu', 72)
ON DUPLICATE KEY UPDATE ten_quyen = VALUES(ten_quyen);

-- =====================================================
-- DỮ LIỆU MẪU - VAI TRÒ
-- =====================================================
INSERT INTO vai_tro_admin (ma_vai_tro, ten_vai_tro, mo_ta, mau_sac, thu_tu) VALUES
('SUPER_ADMIN', 'Super Admin', 'Quản trị viên cao nhất, có tất cả quyền', '#4CAF50', 1),
('MODERATOR', 'Moderator', 'Quản lý nội dung và hỗ trợ người dùng', '#2196F3', 2),
('SUPPORT', 'Support Team', 'Nhóm hỗ trợ khách hàng', '#9C27B0', 3)
ON DUPLICATE KEY UPDATE ten_vai_tro = VALUES(ten_vai_tro);

-- =====================================================
-- GÁN QUYỀN CHO VAI TRÒ
-- =====================================================

-- Super Admin - có quyền ALL
INSERT IGNORE INTO vai_tro_quyen (id_vai_tro, id_quyen)
SELECT r.id, p.id 
FROM vai_tro_admin r, quyen_han p 
WHERE r.ma_vai_tro = 'SUPER_ADMIN' AND p.ma_quyen = 'ALL';

-- Moderator - có các quyền cụ thể
INSERT IGNORE INTO vai_tro_quyen (id_vai_tro, id_quyen)
SELECT r.id, p.id 
FROM vai_tro_admin r, quyen_han p 
WHERE r.ma_vai_tro = 'MODERATOR' 
AND p.ma_quyen IN ('VIEW_DASHBOARD', 'VIEW_ANALYTICS', 'VIEW_USERS', 'EDIT_USERS', 'VIEW_CONTENT', 'EDIT_CONTENT', 'VIEW_LOGS');

-- Support - quyền hạn chế
INSERT IGNORE INTO vai_tro_quyen (id_vai_tro, id_quyen)
SELECT r.id, p.id 
FROM vai_tro_admin r, quyen_han p 
WHERE r.ma_vai_tro = 'SUPPORT' 
AND p.ma_quyen IN ('VIEW_DASHBOARD', 'VIEW_USERS', 'VIEW_LOGS');

-- =====================================================
-- GÁN VAI TRÒ SUPER_ADMIN CHO USER ADMIN HIỆN TẠI
-- =====================================================
INSERT IGNORE INTO nguoi_dung_admin (id_nguoi_dung, id_vai_tro, ten_hien_thi, dang_hoat_dong)
SELECT u.id, r.id, u.ho_ten, TRUE
FROM nguoi_dung u, vai_tro_admin r
WHERE u.vai_tro = 'ADMIN' AND r.ma_vai_tro = 'SUPER_ADMIN';
