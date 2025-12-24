-- =====================================================
-- V009: Thêm bảng cài đặt thông báo cho người dùng
-- Cho phép user bật/tắt từng loại thông báo
-- =====================================================

USE finpal_db;

-- Tạo bảng cài đặt thông báo
CREATE TABLE IF NOT EXISTS cai_dat_thong_bao (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_nguoi_dung BIGINT NOT NULL UNIQUE,
    
    -- Toggle tổng - Bật/tắt tất cả thông báo
    nhan_thong_bao BOOLEAN DEFAULT TRUE,
    
    -- Loại thông báo
    canh_bao_giao_dich BOOLEAN DEFAULT TRUE,  -- Cảnh báo giao dịch mới
    canh_bao_ngan_sach BOOLEAN DEFAULT TRUE,  -- Cảnh báo vượt ngân sách
    nhac_nho_muc_tieu BOOLEAN DEFAULT TRUE,   -- Nhắc nhở mục tiêu tiết kiệm
    canh_bao_bao_mat BOOLEAN DEFAULT TRUE,    -- Cảnh báo hoạt động bất thường
    
    -- Báo cáo định kỳ
    bao_cao_tuan BOOLEAN DEFAULT TRUE,        -- Báo cáo tuần
    bao_cao_thang BOOLEAN DEFAULT TRUE,       -- Báo cáo tháng
    
    -- Thông báo khác
    goi_y_tiet_kiem BOOLEAN DEFAULT TRUE,     -- Gợi ý tiết kiệm
    phan_tich_chi_tieu BOOLEAN DEFAULT TRUE,  -- Phân tích chi tiêu
    
    -- Timestamps
    ngay_tao DATETIME DEFAULT CURRENT_TIMESTAMP,
    ngay_cap_nhat DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign key
    CONSTRAINT fk_notification_settings_user 
        FOREIGN KEY (id_nguoi_dung) REFERENCES nguoi_dung(id) ON DELETE CASCADE,
    
    -- Index
    INDEX idx_notification_settings_user (id_nguoi_dung)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Comment cho bảng
ALTER TABLE cai_dat_thong_bao COMMENT = 'Cài đặt bật/tắt thông báo của người dùng';
