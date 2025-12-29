-- =============================================
-- Migration: Thêm FCM Token và Notification Settings vào bảng nguoi_dung
-- Date: 2024-12-15
-- Description: Thêm các cột cần thiết cho hệ thống Push Notification
-- =============================================

-- Thêm cột fcm_token để lưu Firebase Cloud Messaging token
ALTER TABLE nguoi_dung 
ADD COLUMN fcm_token VARCHAR(500) NULL 
COMMENT 'Firebase Cloud Messaging device token cho push notifications';

-- Thêm cột nhan_thong_bao để cho phép user bật/tắt nhận thông báo
ALTER TABLE nguoi_dung 
ADD COLUMN nhan_thong_bao BOOLEAN DEFAULT TRUE 
COMMENT 'Cho phép nhận push notifications hay không';

-- Tạo index cho fcm_token để tìm kiếm nhanh hơn
CREATE INDEX idx_nguoi_dung_fcm_token ON nguoi_dung(fcm_token);

-- Tạo index cho nhan_thong_bao để filter nhanh users có bật notification
CREATE INDEX idx_nguoi_dung_nhan_thong_bao ON nguoi_dung(nhan_thong_bao);

-- =============================================
-- Verify migration
-- =============================================
-- SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT, COLUMN_COMMENT
-- FROM INFORMATION_SCHEMA.COLUMNS 
-- WHERE TABLE_SCHEMA = 'finpal_db' 
-- AND TABLE_NAME = 'nguoi_dung' 
-- AND COLUMN_NAME IN ('fcm_token', 'nhan_thong_bao');
