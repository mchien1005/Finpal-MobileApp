-- ============================================
-- V011: Thêm index cho cột ngay_du_kien_xoa
-- Cột đã được JPA tự động tạo (ddl-auto=update)
-- Tạo ngày: 2025-12-25
-- ============================================

-- LƯU Ý: Cột ngay_du_kien_xoa đã được JPA tự động tạo
-- Nếu cột chưa tồn tại, chạy lệnh sau (bỏ comment):
-- ALTER TABLE yeu_cau_nguoi_dung 
-- ADD COLUMN ngay_du_kien_xoa DATETIME NULL 
-- COMMENT 'Thời gian dự kiến xóa tài khoản (24h sau khi phê duyệt)';

-- Thêm index cho việc query các yêu cầu đến hạn (bỏ qua nếu đã tồn tại)
-- MySQL không hỗ trợ CREATE INDEX IF NOT EXISTS, dùng cách kiểm tra thủ công
-- Chỉ chạy nếu index chưa tồn tại

-- Kiểm tra và tạo index idx_scheduled_deletion
SET @exist := (SELECT COUNT(*) FROM information_schema.statistics 
               WHERE table_schema = DATABASE() 
               AND table_name = 'yeu_cau_nguoi_dung' 
               AND index_name = 'idx_scheduled_deletion');
SET @sqlstmt := IF(@exist > 0, 'SELECT ''Index idx_scheduled_deletion already exists''',
    'CREATE INDEX idx_scheduled_deletion ON yeu_cau_nguoi_dung (ngay_du_kien_xoa)');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Kiểm tra và tạo index idx_deletion_ready
SET @exist := (SELECT COUNT(*) FROM information_schema.statistics 
               WHERE table_schema = DATABASE() 
               AND table_name = 'yeu_cau_nguoi_dung' 
               AND index_name = 'idx_deletion_ready');
SET @sqlstmt := IF(@exist > 0, 'SELECT ''Index idx_deletion_ready already exists''',
    'CREATE INDEX idx_deletion_ready ON yeu_cau_nguoi_dung (loai_yeu_cau, trang_thai, ngay_du_kien_xoa)');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Trạng thái mới CANCELLED đã được thêm vào enum RequestStatus trong code Java
-- JPA sẽ tự động hỗ trợ giá trị mới này cho cột trang_thai (VARCHAR)

