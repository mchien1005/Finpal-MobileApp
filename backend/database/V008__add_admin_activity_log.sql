-- =====================================================
-- BẢNG LICH_SU_HOAT_DONG_ADMIN (Admin Activity Log)
-- Lưu trữ lịch sử hoạt động của admin
-- =====================================================

USE finpal_db;

CREATE TABLE IF NOT EXISTS lich_su_hoat_dong_admin (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_admin_user BIGINT COMMENT 'ID admin user thực hiện hành động',
    hanh_dong VARCHAR(100) NOT NULL COMMENT 'Loại hành động: CREATE, UPDATE, DELETE, VIEW',
    loai_doi_tuong VARCHAR(50) COMMENT 'Loại đối tượng: USER, CATEGORY, SMS_PARSER, CONTENT',
    id_doi_tuong BIGINT COMMENT 'ID của đối tượng bị tác động',
    mo_ta VARCHAR(500) COMMENT 'Mô tả hành động chi tiết',
    du_lieu_cu TEXT COMMENT 'JSON data trước khi thay đổi',
    du_lieu_moi TEXT COMMENT 'JSON data sau khi thay đổi',
    dia_chi_ip VARCHAR(50) COMMENT 'Địa chỉ IP thực hiện',
    user_agent VARCHAR(500) COMMENT 'User-Agent của trình duyệt/app',
    thoi_gian DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời gian thực hiện',
    trang_thai VARCHAR(20) DEFAULT 'SUCCESS' COMMENT 'Trạng thái: SUCCESS, FAILED',
    
    INDEX idx_admin_user (id_admin_user),
    INDEX idx_thoi_gian (thoi_gian),
    INDEX idx_hanh_dong (hanh_dong),
    INDEX idx_loai_doi_tuong (loai_doi_tuong),
    INDEX idx_admin_thoi_gian (id_admin_user, thoi_gian DESC),
    
    FOREIGN KEY (id_admin_user) REFERENCES nguoi_dung_admin(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='Bảng lưu trữ lịch sử hoạt động của admin';

-- =====================================================
-- DỮ LIỆU MẪU - LỊCH SỬ HOẠT ĐỘNG
-- =====================================================
INSERT INTO lich_su_hoat_dong_admin (id_admin_user, hanh_dong, loai_doi_tuong, mo_ta, dia_chi_ip, thoi_gian, trang_thai)
SELECT 
    a.id,
    CASE n
        WHEN 0 THEN 'UPDATE'
        WHEN 1 THEN 'CREATE'
        WHEN 2 THEN 'VIEW'
        WHEN 3 THEN 'UPDATE'
        WHEN 4 THEN 'DELETE'
    END,
    CASE n
        WHEN 0 THEN 'CATEGORY'
        WHEN 1 THEN 'SMS_PARSER'
        WHEN 2 THEN 'USER'
        WHEN 3 THEN 'CONTENT'
        WHEN 4 THEN 'SYSTEM'
    END,
    CASE n
        WHEN 0 THEN 'Đã chỉnh sửa category "Ăn uống"'
        WHEN 1 THEN 'Đã thêm template SMS cho Vietcombank'
        WHEN 2 THEN 'Đã xem danh sách người dùng'
        WHEN 3 THEN 'Đã cập nhật nội dung Tips'
        WHEN 4 THEN 'Đã xóa bản ghi hệ thống'
    END,
    '192.168.1.1',
    DATE_SUB(NOW(), INTERVAL n DAY),
    'SUCCESS'
FROM nguoi_dung_admin a, 
(SELECT 0 AS n UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4) numbers
WHERE a.id = (SELECT MIN(id) FROM nguoi_dung_admin)
LIMIT 5;
