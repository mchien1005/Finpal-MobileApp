-- =====================================================
-- CẬP NHẬT CẤU HÌNH PARSE SMS NGÂN HÀNG BIDV
-- Ngày: 2026-01-02
-- Mô tả: Sửa regex để hỗ trợ format SMS mới có khoảng trắng sau "TK"
-- =====================================================

-- SMS Mẫu mới: TK 426xxx8540 tai BIDV +195,000VND vao 08:51 01/01/2026. So du:696,852VND. ND: TKThe :1005200496969, tai MSCBVNVX. Tra no -B2B020097042201010851412026Z48S930765

UPDATE `bo_phan_tich_sms` 
SET 
    -- Regex mới: Thêm \\s* sau TK để chấp nhận có hoặc không có khoảng trắng
    `mau_regex` = 'TK\\s*([\\dx]+)\\s+tai\\s+BIDV\\s+([+-])([\\d,]+)VND\\s+vao\\s+(\\d{2}:\\d{2})\\s+(\\d{2}/\\d{2}/\\d{4}).*?ND:\\s*(.+?)$',
    
    -- SMS mẫu mới theo format hiện tại
    `sms_mau` = 'TK 426xxx8540 tai BIDV +195,000VND vao 08:51 01/01/2026. So du:696,852VND. ND: TKThe :1005200496969, tai MSCBVNVX. Tra no -B2B020097042201010851412026Z48S930765',
    
    -- Cập nhật ngày sửa đổi
    `ngay_cap_nhat` = NOW()
WHERE 
    `id` = 4 
    AND `ma_ngan_hang` = 'BIDV';

-- Kiểm tra kết quả cập nhật
SELECT * FROM `bo_phan_tich_sms` WHERE `ma_ngan_hang` = 'BIDV';
