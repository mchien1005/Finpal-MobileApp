-- ============================================
-- V010: Thêm bảng lưu lịch sử xóa tài khoản người dùng
-- Dùng cho mục đích audit - giữ lại thông tin sau khi user đã bị xóa
-- Tạo ngày: 2025-12-25
-- ============================================

CREATE TABLE IF NOT EXISTS lich_su_xoa_tai_khoan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Thông tin người dùng bị xóa (lưu lại vì user sẽ bị xóa)
    id_nguoi_dung_bi_xoa BIGINT NOT NULL COMMENT 'ID gốc của người dùng trước khi bị xóa',
    ten_nguoi_dung VARCHAR(100) NOT NULL COMMENT 'Username của người dùng',
    email VARCHAR(200) NOT NULL COMMENT 'Email của người dùng',
    ho_ten VARCHAR(200) COMMENT 'Họ tên của người dùng',
    
    -- Thông tin yêu cầu xóa
    id_yeu_cau BIGINT NOT NULL COMMENT 'ID của yêu cầu xóa tài khoản',
    ly_do_yeu_cau TEXT COMMENT 'Lý do người dùng yêu cầu xóa tài khoản',
    ngay_yeu_cau DATETIME NOT NULL COMMENT 'Ngày người dùng tạo yêu cầu xóa',
    
    -- Thông tin admin phê duyệt
    id_admin_duyet BIGINT COMMENT 'ID của admin đã phê duyệt yêu cầu',
    ten_admin_duyet VARCHAR(100) COMMENT 'Username của admin đã phê duyệt',
    ghi_chu_admin TEXT COMMENT 'Ghi chú của admin khi phê duyệt',
    ngay_duyet DATETIME COMMENT 'Ngày admin phê duyệt yêu cầu',
    
    -- Thông tin xóa
    ngay_xoa DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày thực sự xóa tài khoản',
    da_gui_email_thong_bao BOOLEAN DEFAULT FALSE COMMENT 'Đã gửi email thông báo cho người dùng chưa',
    trang_thai_xoa VARCHAR(20) DEFAULT 'COMPLETED' COMMENT 'Trạng thái xóa: COMPLETED, FAILED',
    thong_bao_loi TEXT COMMENT 'Thông báo lỗi nếu xóa thất bại',
    
    -- Indexes để tìm kiếm nhanh
    INDEX idx_deleted_user_id (id_nguoi_dung_bi_xoa),
    INDEX idx_username (ten_nguoi_dung),
    INDEX idx_email (email),
    INDEX idx_deleted_at (ngay_xoa),
    INDEX idx_admin_id (id_admin_duyet),
    INDEX idx_status (trang_thai_xoa)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bảng lưu lịch sử xóa tài khoản người dùng cho mục đích audit';

-- ============================================
-- HƯỚNG DẪN SỬ DỤNG
-- ============================================
-- Chạy script này trên database MySQL để thêm bảng lich_su_xoa_tai_khoan
-- 
-- Cách chạy:
-- mysql -u root -p finpal_db < V010__add_account_deletion_log_table.sql
--
-- Hoặc copy nội dung và chạy trong MySQL Workbench / phpMyAdmin
-- ============================================
--
-- Bảng này lưu lại đầy đủ thông tin khi một tài khoản bị xóa:
-- 1. Thông tin người dùng (username, email, họ tên) - vì sau khi xóa user không còn trong DB
-- 2. Thông tin yêu cầu xóa (lý do, ngày tạo)
-- 3. Thông tin admin phê duyệt (ai duyệt, khi nào, ghi chú gì)
-- 4. Thông tin thực thi xóa (ngày xóa, có gửi email không, thành công hay thất bại)
--
-- Dùng để:
-- - Audit log: theo dõi ai đã xóa tài khoản nào, khi nào
-- - Compliance: tuân thủ các quy định về bảo vệ dữ liệu (GDPR, v.v.)
-- - Investigation: điều tra khi có vấn đề phát sinh
-- ============================================
