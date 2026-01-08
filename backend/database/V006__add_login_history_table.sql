-- =====================================================
-- BẢNG LICH_SU_DANG_NHAP (LOGIN_HISTORY)
-- Lưu trữ lịch sử đăng nhập của người dùng
-- =====================================================

USE finpal_db;

CREATE TABLE IF NOT EXISTS lich_su_dang_nhap (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_nguoi_dung BIGINT NOT NULL COMMENT 'ID người dùng',
    thoi_gian_dang_nhap DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời gian đăng nhập',
    dia_chi_ip VARCHAR(50) COMMENT 'Địa chỉ IP',
    ten_thiet_bi VARCHAR(200) COMMENT 'Tên thiết bị (iPhone 15 Pro, MacBook Pro, ...)',
    loai_thiet_bi VARCHAR(50) COMMENT 'Loại thiết bị: MOBILE, TABLET, DESKTOP, WEB',
    he_dieu_hanh VARCHAR(100) COMMENT 'Hệ điều hành (iOS 17, Windows 11, ...)',
    trinh_duyet VARCHAR(100) COMMENT 'Trình duyệt (Chrome, Safari, ...)',
    vi_tri VARCHAR(200) COMMENT 'Vị trí (Thành phố, Quốc gia)',
    trang_thai ENUM('SUCCESS', 'FAILED', 'BLOCKED', 'EXPIRED') DEFAULT 'SUCCESS' COMMENT 'Trạng thái đăng nhập',
    ly_do_that_bai VARCHAR(255) COMMENT 'Lý do thất bại (nếu có)',
    fcm_token VARCHAR(500) COMMENT 'FCM Token của thiết bị',
    
    INDEX idx_nguoi_dung (id_nguoi_dung),
    INDEX idx_thoi_gian (thoi_gian_dang_nhap),
    INDEX idx_trang_thai (trang_thai),
    INDEX idx_nguoi_dung_thoi_gian (id_nguoi_dung, thoi_gian_dang_nhap DESC),
    
    FOREIGN KEY (id_nguoi_dung) REFERENCES nguoi_dung(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='Bảng lưu trữ lịch sử đăng nhập của người dùng';

-- Thêm dữ liệu mẫu cho demo user
INSERT INTO lich_su_dang_nhap (id_nguoi_dung, thoi_gian_dang_nhap, dia_chi_ip, ten_thiet_bi, loai_thiet_bi, vi_tri, trang_thai) 
SELECT 
    (SELECT id FROM nguoi_dung WHERE ten_dang_nhap = 'demo' LIMIT 1),
    DATE_SUB(NOW(), INTERVAL n DAY),
    CONCAT('192.168.1.', FLOOR(RAND() * 255)),
    CASE FLOOR(RAND() * 3) 
        WHEN 0 THEN 'iPhone 15 Pro'
        WHEN 1 THEN 'MacBook Pro'
        ELSE 'Samsung Galaxy S24'
    END,
    CASE FLOOR(RAND() * 3) 
        WHEN 0 THEN 'MOBILE'
        WHEN 1 THEN 'DESKTOP'
        ELSE 'MOBILE'
    END,
    'Hà Nội',
    CASE WHEN RAND() > 0.1 THEN 'SUCCESS' ELSE 'FAILED' END
FROM (
    SELECT 0 AS n UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
    UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9
) numbers
WHERE EXISTS (SELECT 1 FROM nguoi_dung WHERE ten_dang_nhap = 'demo');
