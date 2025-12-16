-- =====================================================
-- BẢNG CAI_DAT_HE_THONG (SYSTEM_SETTINGS)
-- Lưu trữ cài đặt hệ thống cho admin quản lý
-- =====================================================

USE finpal_db;

CREATE TABLE IF NOT EXISTS cai_dat_he_thong (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    khoa_cai_dat VARCHAR(100) NOT NULL UNIQUE COMMENT 'Khóa cài đặt duy nhất, ví dụ: ai.confidence.threshold',
    gia_tri VARCHAR(500) COMMENT 'Giá trị cài đặt',
    mo_ta VARCHAR(255) COMMENT 'Mô tả cài đặt',
    nhom_cai_dat VARCHAR(50) COMMENT 'Nhóm cài đặt: AI, NOTIFICATION, SYSTEM',
    kieu_du_lieu VARCHAR(20) COMMENT 'Kiểu dữ liệu: STRING, INTEGER, DOUBLE, BOOLEAN',
    ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày tạo',
    ngay_cap_nhat TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Ngày cập nhật cuối',
    nguoi_cap_nhat BIGINT NULL COMMENT 'ID admin cập nhật cuối',
    
    INDEX idx_nhom_cai_dat (nhom_cai_dat),
    INDEX idx_khoa_cai_dat (khoa_cai_dat),
    FOREIGN KEY (nguoi_cap_nhat) REFERENCES nguoi_dung(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='Bảng lưu trữ cài đặt hệ thống cho admin quản lý';

-- Thêm dữ liệu mặc định
INSERT INTO cai_dat_he_thong (khoa_cai_dat, gia_tri, mo_ta, nhom_cai_dat, kieu_du_lieu) VALUES
-- Cài đặt AI
('ai.confidence.threshold', '0.70', 'Ngưỡng độ tin cậy AI tối thiểu (0.0 - 1.0)', 'AI', 'DOUBLE'),
('ai.retraining.frequency', 'DAILY', 'Tần suất huấn luyện lại AI (DAILY, WEEKLY, MONTHLY)', 'AI', 'STRING'),
('ai.categorization.strategy', 'RULE_FIRST', 'Chiến lược phân loại (AI_FIRST, RULE_FIRST, HYBRID, RULE_ONLY)', 'AI', 'STRING'),

-- Cài đặt thông báo
('notification.push.enabled', 'true', 'Bật/tắt thông báo đẩy hệ thống', 'NOTIFICATION', 'BOOLEAN'),
('notification.email.enabled', 'true', 'Bật/tắt thông báo email hệ thống', 'NOTIFICATION', 'BOOLEAN'),

-- Cài đặt hệ thống
('system.maintenance.mode', 'false', 'Bật/tắt chế độ bảo trì hệ thống', 'SYSTEM', 'BOOLEAN')
ON DUPLICATE KEY UPDATE khoa_cai_dat = khoa_cai_dat;
