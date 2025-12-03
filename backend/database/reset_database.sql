-- =====================================================
-- RESET FINPAL DATABASE - Chạy script này để reset lại database
-- =====================================================
USE finpal_db;
-- Tắt foreign key checks để có thể xóa dữ liệu
SET FOREIGN_KEY_CHECKS = 0;
-- Xóa tất cả dữ liệu (giữ lại cấu trúc bảng)
TRUNCATE TABLE transactions;
TRUNCATE TABLE budgets;
TRUNCATE TABLE savings_goals;
TRUNCATE TABLE accounts;
TRUNCATE TABLE user_preferences;
TRUNCATE TABLE notifications;
TRUNCATE TABLE category_rules;
TRUNCATE TABLE sms_parsers;
TRUNCATE TABLE categories;
TRUNCATE TABLE users;
-- Bật lại foreign key checks
SET FOREIGN_KEY_CHECKS = 1;
SELECT 'Database đã được reset thành công!' AS status;