-- =====================================================
-- FINPAL SEED DATA
-- Dữ liệu mẫu cho hệ thống
-- =====================================================
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
USE finpal_db;
-- =====================================================
-- CLEAN UP - Xóa dữ liệu cũ (nếu có) để tránh duplicate
-- =====================================================
-- SET FOREIGN_KEY_CHECKS = 0;
-- TRUNCATE TABLE thong_bao;
-- TRUNCATE TABLE giao_dich_dinh_ky;
-- TRUNCATE TABLE ngan_sach;
-- TRUNCATE TABLE giao_dich;
-- TRUNCATE TABLE muc_tieu_tiet_kiem;
-- TRUNCATE TABLE tai_khoan;
-- TRUNCATE TABLE quy_tac_danh_muc;
-- TRUNCATE TABLE danh_muc;
-- TRUNCATE TABLE bo_phan_tich_sms;
-- TRUNCATE TABLE nguoi_dung;
-- SET FOREIGN_KEY_CHECKS = 1;
-- =====================================================
-- 1. NGUOI_DUNG - Người dùng demo
-- =====================================================
-- Password: demo123 (BCrypt hash)
INSERT INTO nguoi_dung (
        ten_dang_nhap,
        mat_khau,
        email,
        ho_ten,
        vai_tro,
        dang_hoat_dong
    )
VALUES (
        'demo',
        '$2a$10$N9qo8uLOickgx2ZMRZoMye1VdedWaK.ILPi4k6VYLaHjRg/ZcXq5u',
        'demo@finpal.vn',
        'Nguyễn Văn Demo',
        'USER',
        TRUE
    ),
    (
        'admin',
        '$2a$10$N9qo8uLOickgx2ZMRZoMye1VdedWaK.ILPi4k6VYLaHjRg/ZcXq5u',
        'admin@finpal.vn',
        'Quản Trị Viên',
        'ADMIN',
        TRUE
    );
-- =====================================================
-- 2. DANH_MUC - Danh mục hệ thống
-- =====================================================
INSERT INTO danh_muc (id_cha, ten_danh_muc, loai)
VALUES -- EXPENSE Categories (8)
    (NULL, 'Ăn uống', 'EXPENSE'),
    (NULL, 'Di chuyển', 'EXPENSE'),
    (NULL, 'Mua sắm', 'EXPENSE'),
    (NULL, 'Giải trí', 'EXPENSE'),
    (NULL, 'Sức khỏe', 'EXPENSE'),
    (NULL, 'Giáo dục', 'EXPENSE'),
    (NULL, 'Hóa đơn', 'EXPENSE'),
    (NULL, 'Khác', 'EXPENSE'),
    -- INCOME Categories (4)
    (NULL, 'Lương', 'INCOME'),
    (NULL, 'Thưởng', 'INCOME'),
    (NULL, 'Đầu tư', 'INCOME'),
    (NULL, 'Khác', 'INCOME');
-- =====================================================
-- 3. BO_PHAN_TICH_SMS - Cấu hình parse SMS ngân hàng
-- =====================================================
INSERT INTO bo_phan_tich_sms (
        ma_ngan_hang,
        ten_ngan_hang,
        mau_regex,
        anh_xa_truong,
        sms_mau
    )
VALUES -- Vietcombank (SMS format)
    (
        'VCB',
        'Vietcombank',
        'TK\\s+(\\d+).*?([+-])([\\d,]+)VND.*?luc\\s+([\\d/\\s:]+).*?ND:\\s*([^.]+).*?SD:\\s*([\\d,]+)',
        '{"type":"group2","amount":"group3","transaction_date":"group4","merchant":"group5","balance_after":"group6"}',
        'TK 001234567: -55,000VND luc 12/11/2025 09:00. ND: GRAB. SD: 2,450,000VND'
    ),
    -- Vietcombank (App notification format)
    (
        'VCB',
        'Vietcombank',
        'So du TK VCB\\s+(\\d+).*?([+-])([\\d,]+)\\s+VND\\s+luc\\s+([\\d-]+)\\s+([\\d:]+).*?So du\\s+([\\d,]+)\\s+VND.*?GD:(.+?)(?:\\s|$)',
        '{"type":"group2","amount":"group3","transaction_date":"group4 group5","balance_after":"group6","merchant":"group7"}',
        'Số dư TK VCB 0111000155751\n-20,000 VND lúc 26-06-2021 08:10:14.\nSố dư 877,172 VND. Ref POS.79900\n008.830963.20210626.081014.9704\n3668Tc4a111000000000762010 ..\n505471.0.000000.GD:ZALOPAY'
    ),
    -- Techcombank
    (
        'TCB',
        'Techcombank',
        'GD:\\s*([+-])([\\d,]+)VND.*?luc\\s+([\\d/\\s:]+).*?tai\\s+([^.]+).*?SD:\\s*([\\d,]+)',
        '{"type":"group1","amount":"group2","transaction_date":"group3","merchant":"group4","balance_after":"group5"}',
        'GD: -120,000VND luc 13/11 10:30 tai HIGHLANDS COFFEE. SD: 1,880,000VND'
    ),
    -- BIDV
    (
        'BIDV',
        'BIDV',
        'Thoi gian giao dich:\\s+(\\d{2}:\\d{2})\\s+(\\d{2}/\\d{2}/\\d{4}).*?So tien GD:\\s+([+-])([\\d,]+)\\s+VND.*?So du cuoi:\\s+([\\d,]+)\\s+VND.*?Noi dung giao dich:\\s*([^M]+?)(?:Ma giao dich|$)',
        '{"transaction_date":"group1 group2","type":"group3","amount":"group4","balance_after":"group5","merchant":"group6"}',
        'BIDV xin thông báo tới Quý khách\nThời gian giao dịch: 13:11 25/11/2025\nTài khoản thanh toán: 4260848570\nSố tiền GD: +10,000 VND\nSố dư cuối: 971,979 VND\nNội dung giao dịch: TKThe :1027779485, tai Vietcombank. MBVCB.11876304648.447013.VU XUAN HUY chuyen tien.CT tu 1027779445 VU XUAN HUY toi 4260848540 NGUYEN MINH CHIEN tai BIDV -CTLNHIDI000013514283774-1/1-CRE-002\nMã giao dịch: 0832ODV4-84v6kpcqo'
    ),
    -- MBBank
    (
        'MBB',
        'MBBank',
        'TK\\s+(\\d+x+\\d+)\\|GD:\\s+([+-])([\\d,]+)VND\\s+(\\d{2}/\\d{2}/\\d{2})\\s+(\\d{2}:\\d{2})\\s+\\|SD:\\s+([\\d,]+)VND\\|ND:\\s*([^-]+)',
        '{"type":"group2","amount":"group3","transaction_date":"group4 group5","balance_after":"group6","merchant":"group7"}',
        'Thông báo biến động số dư\nTK 10xxx969|GD: +25,000VND 25/11/25 20:35 |SD: 140,002VND|ND: 108609376869-HA VAN THANG chuyen tien qua MoMo-CHUYEN TIEN-OQCH00044xSs-MOMO108609376867MOMO'
    ),
    -- PVcomBank
    (
        'PVB',
        'PVcomBank',
        '([+-])([\\d,]+)\\s*₫.*?Tai khoan:\\s+(\\d+).*?So du:\\s+([\\d,]+)\\s*₫.*?Loi nhan:\\s*([^.]+)',
        '{"type":"group1","amount":"group2","balance_after":"group4","merchant":"group5"}',
        '+9000 ₫\n\nTài khoản: 107001384884\n\nSố dư: 50,570 ₫\n\nLời nhắn: Lai nhap goc .\n\nLai suat gui tiet kiem online cao hon tai quay toi 0.5%/nam. Liên hệ: 19006692/1900555592.\n\n23:23'
    ),
    -- VietinBank
    (
        'CTG',
        'VietinBank',
        'Thoi gian:\\s+(\\d{2}/\\d{2}/\\d{4})\\s+(\\d{2}:\\d{2}).*?Tai khoan:\\s+(\\d+).*?Giao dich:\\s+([+-])([\\d,]+)\\s+VND.*?So du hien tai:\\s+([\\d,]+)\\s+VND.*?Noi dung:\\s*(.+?)(?:;\\s*tai|$)',
        '{"transaction_date":"group1 group2","type":"group4","amount":"group5","balance_after":"group6","merchant":"group7"}',
        'Thời gian: 26/11/2025 10:05\nTài khoản: 103600583557\nGiao dich: -30,000 VND\nSố dư hiện tại: 696,634 VND\nNội dung: CT DI:533010651537 NGUYEN XUAN ANH chuye n tien; tai iPay'
    );
-- =====================================================
-- 4. QUY_TAC_DANH_MUC - Luật AI phân loại (Global)
-- =====================================================
INSERT INTO quy_tac_danh_muc (tu_khoa, id_danh_muc, do_uu_tien)
VALUES -- Ăn uống (id_danh_muc sẽ là 1)
    ('GRAB FOOD', 1, 95),
    ('HIGHLANDS', 1, 90),
    ('THE COFFEE HOUSE', 1, 90),
    ('PHUC LONG', 1, 90),
    ('CIRCLE K', 1, 75),
    -- Di chuyển (id_danh_muc = 2)
    ('GRAB', 2, 85),
    ('BE', 2, 85),
    ('XANG', 2, 90),
    -- Mua sắm (id_danh_muc = 3)
    ('SHOPEE', 3, 95),
    ('LAZADA', 3, 95),
    ('TIKI', 3, 95),
    ('VINMART', 3, 85),
    -- Giải trí (id_danh_muc = 4)
    ('CGV', 4, 95),
    ('LOTTE CINEMA', 4, 95),
    ('NETFLIX', 4, 90),
    ('SPOTIFY', 4, 90),
    -- Hóa đơn (id_danh_muc = 7)
    ('EVN', 7, 100),
    ('VNPT', 7, 100),
    ('VIETTEL', 7, 95),
    ('FPT', 7, 95);
-- =====================================================
-- 5. DEMO DATA - Tai khoan cho user demo
-- =====================================================
SET @demo_user_id = (
        SELECT id
        FROM nguoi_dung
        WHERE ten_dang_nhap = 'demo'
        LIMIT 1
    );
INSERT INTO tai_khoan (
        id_nguoi_dung,
        ten_ngan_hang,
        so_tai_khoan,
        ten_tai_khoan,
        so_du
    )
VALUES (
        @demo_user_id,
        'VCB',
        '****1234',
        'VCB Lương',
        5000000.00
    ),
    (
        @demo_user_id,
        'TCB',
        '****5678',
        'TCB Tiết kiệm',
        10000000.00
    ),
    (
        @demo_user_id,
        'CASH',
        NULL,
        'Tiền mặt',
        500000.00
    );
-- =====================================================
-- 6. DEMO DATA - Giao dich (30 ngày gần nhất)
-- =====================================================
SET @vcb_account = (
        SELECT id
        FROM tai_khoan
        WHERE id_nguoi_dung = @demo_user_id
            AND ten_ngan_hang = 'VCB'
        LIMIT 1
    );
SET @tcb_account = (
        SELECT id
        FROM tai_khoan
        WHERE id_nguoi_dung = @demo_user_id
            AND ten_ngan_hang = 'TCB'
        LIMIT 1
    );
-- Category IDs
SET @cat_an_uong = (
        SELECT id
        FROM danh_muc
        WHERE ten_danh_muc = 'Ăn uống'
        LIMIT 1
    );
SET @cat_di_chuyen = (
        SELECT id
        FROM danh_muc
        WHERE ten_danh_muc = 'Di chuyển'
        LIMIT 1
    );
SET @cat_mua_sam = (
        SELECT id
        FROM danh_muc
        WHERE ten_danh_muc = 'Mua sắm'
        LIMIT 1
    );
SET @cat_giai_tri = (
        SELECT id
        FROM danh_muc
        WHERE ten_danh_muc = 'Giải trí'
        LIMIT 1
    );
SET @cat_hoa_don = (
        SELECT id
        FROM danh_muc
        WHERE ten_danh_muc = 'Hóa đơn'
        LIMIT 1
    );
SET @cat_luong = (
        SELECT id
        FROM danh_muc
        WHERE ten_danh_muc = 'Lương'
        LIMIT 1
    );
INSERT INTO giao_dich (
        id_nguoi_dung,
        id_tai_khoan,
        id_danh_muc,
        so_tien,
        loai,
        don_vi_chap_nhan,
        mo_ta,
        ngay_giao_dich,
        tu_dong
    )
VALUES -- Thu nhập
    (
        @demo_user_id,
        @vcb_account,
        @cat_luong,
        15000000.00,
        'INCOME',
        'CONG TY ABC',
        'Lương tháng 11',
        '2025-11-01 09:00:00',
        FALSE
    ),
    -- Chi tiêu tuần 1 (01-07/11)
    (
        @demo_user_id,
        @vcb_account,
        @cat_an_uong,
        45000.00,
        'EXPENSE',
        'HIGHLANDS COFFEE',
        'Cafe sáng',
        '2025-11-02 08:30:00',
        TRUE
    ),
    (
        @demo_user_id,
        @vcb_account,
        @cat_di_chuyen,
        85000.00,
        'EXPENSE',
        'GRAB',
        'Đi làm về',
        '2025-11-02 18:00:00',
        TRUE
    ),
    (
        @demo_user_id,
        @vcb_account,
        @cat_an_uong,
        120000.00,
        'EXPENSE',
        'GRAB FOOD',
        'Ăn trưa',
        '2025-11-03 12:00:00',
        TRUE
    ),
    (
        @demo_user_id,
        @vcb_account,
        @cat_mua_sam,
        350000.00,
        'EXPENSE',
        'CIRCLE K',
        'Mua đồ ăn vặt',
        '2025-11-03 20:00:00',
        TRUE
    ),
    (
        @demo_user_id,
        @vcb_account,
        @cat_giai_tri,
        150000.00,
        'EXPENSE',
        'CGV',
        'Vé xem phim',
        '2025-11-05 19:00:00',
        TRUE
    ),
    (
        @demo_user_id,
        @vcb_account,
        @cat_an_uong,
        200000.00,
        'EXPENSE',
        'NHA HANG ABC',
        'Ăn tối cuối tuần',
        '2025-11-06 19:30:00',
        FALSE
    ),
    -- Chi tiêu tuần 2 (08-14/11)
    (
        @demo_user_id,
        @vcb_account,
        @cat_hoa_don,
        350000.00,
        'EXPENSE',
        'EVN',
        'Tiền điện tháng 10',
        '2025-11-08 10:00:00',
        TRUE
    ),
    (
        @demo_user_id,
        @vcb_account,
        @cat_hoa_don,
        200000.00,
        'EXPENSE',
        'VNPT',
        'Cước internet',
        '2025-11-08 10:05:00',
        TRUE
    ),
    (
        @demo_user_id,
        @vcb_account,
        @cat_di_chuyen,
        95000.00,
        'EXPENSE',
        'GRAB',
        'Đi làm',
        '2025-11-09 08:00:00',
        TRUE
    ),
    (
        @demo_user_id,
        @vcb_account,
        @cat_an_uong,
        55000.00,
        'EXPENSE',
        'THE COFFEE HOUSE',
        'Cafe',
        '2025-11-09 15:00:00',
        TRUE
    ),
    (
        @demo_user_id,
        @vcb_account,
        @cat_mua_sam,
        1200000.00,
        'EXPENSE',
        'SHOPEE',
        'Mua quần áo',
        '2025-11-10 21:00:00',
        TRUE
    ),
    (
        @demo_user_id,
        @vcb_account,
        @cat_an_uong,
        180000.00,
        'EXPENSE',
        'PHUC LONG',
        'Trà sữa + bánh',
        '2025-11-11 16:00:00',
        TRUE
    ),
    (
        @demo_user_id,
        @vcb_account,
        @cat_giai_tri,
        199000.00,
        'EXPENSE',
        'NETFLIX',
        'Gói Premium tháng',
        '2025-11-12 00:01:00',
        TRUE
    ),
    -- Chi tiêu tuần 3 (15-21/11)
    (
        @demo_user_id,
        @vcb_account,
        @cat_an_uong,
        50000.00,
        'EXPENSE',
        'HIGHLANDS COFFEE',
        'Cafe sáng',
        '2025-11-15 08:30:00',
        TRUE
    ),
    (
        @demo_user_id,
        @vcb_account,
        @cat_di_chuyen,
        75000.00,
        'EXPENSE',
        'BE',
        'Đi làm',
        '2025-11-15 08:00:00',
        TRUE
    ),
    (
        @demo_user_id,
        @vcb_account,
        @cat_mua_sam,
        850000.00,
        'EXPENSE',
        'LAZADA',
        'Mua đồ điện tử',
        '2025-11-16 14:00:00',
        TRUE
    ),
    (
        @demo_user_id,
        @vcb_account,
        @cat_an_uong,
        320000.00,
        'EXPENSE',
        'LOTTERIA',
        'Ăn trưa buffet',
        '2025-11-17 12:30:00',
        FALSE
    ),
    (
        @demo_user_id,
        @vcb_account,
        @cat_giai_tri,
        250000.00,
        'EXPENSE',
        'LOTTE CINEMA',
        'Vé xem phim IMAX',
        '2025-11-18 20:00:00',
        TRUE
    ),
    (
        @demo_user_id,
        @vcb_account,
        @cat_an_uong,
        45000.00,
        'EXPENSE',
        'HIGHLANDS COFFEE',
        'Cafe',
        '2025-11-19 09:00:00',
        TRUE
    ),
    -- Chi tiêu tuần 4 (22-26/11)
    (
        @demo_user_id,
        @vcb_account,
        @cat_di_chuyen,
        120000.00,
        'EXPENSE',
        'GRAB',
        'Đi chơi xa',
        '2025-11-22 14:00:00',
        TRUE
    ),
    (
        @demo_user_id,
        @vcb_account,
        @cat_an_uong,
        280000.00,
        'EXPENSE',
        'KICHI KICHI',
        'Lẩu buffet',
        '2025-11-23 18:00:00',
        FALSE
    ),
    (
        @demo_user_id,
        @vcb_account,
        @cat_mua_sam,
        650000.00,
        'EXPENSE',
        'TIKI',
        'Mua sách',
        '2025-11-24 10:00:00',
        TRUE
    ),
    (
        @demo_user_id,
        @vcb_account,
        @cat_an_uong,
        60000.00,
        'EXPENSE',
        'PHUC LONG',
        'Trà sữa',
        '2025-11-25 15:30:00',
        TRUE
    ),
    (
        @demo_user_id,
        @vcb_account,
        @cat_di_chuyen,
        90000.00,
        'EXPENSE',
        'GRAB',
        'Về nhà',
        '2025-11-26 17:00:00',
        TRUE
    );
-- =====================================================
-- 7. DEMO DATA - Ngan sach
-- =====================================================
INSERT INTO ngan_sach (
        id_nguoi_dung,
        id_danh_muc,
        so_tien,
        ngay_bat_dau,
        ngay_ket_thuc
    )
VALUES (
        @demo_user_id,
        @cat_an_uong,
        3000000.00,
        '2025-11-01',
        '2025-11-30'
    ),
    (
        @demo_user_id,
        @cat_di_chuyen,
        1500000.00,
        '2025-11-01',
        '2025-11-30'
    ),
    (
        @demo_user_id,
        @cat_mua_sam,
        2000000.00,
        '2025-11-01',
        '2025-11-30'
    ),
    (
        @demo_user_id,
        @cat_giai_tri,
        1000000.00,
        '2025-11-01',
        '2025-11-30'
    ),
    (
        @demo_user_id,
        @cat_hoa_don,
        800000.00,
        '2025-11-01',
        '2025-11-30'
    );
-- =====================================================
-- 8. DEMO DATA - Muc tieu tiet kiem
-- =====================================================
INSERT INTO muc_tieu_tiet_kiem (
        id_nguoi_dung,
        ten_muc_tieu,
        so_tien_muc_tieu,
        so_tien_hien_tai,
        han_chot,
        trang_thai
    )
VALUES (
        @demo_user_id,
        'Mua iPhone 16 Pro',
        30000000.00,
        8500000.00,
        '2026-03-01',
        'ACTIVE'
    ),
    (
        @demo_user_id,
        'Du lịch Nhật Bản',
        50000000.00,
        12000000.00,
        '2026-07-01',
        'ACTIVE'
    ),
    (
        @demo_user_id,
        'Quỹ khẩn cấp',
        20000000.00,
        20000000.00,
        '2025-12-31',
        'COMPLETED'
    );
-- =====================================================
-- 9. DEMO DATA - Giao dich dinh ky (AI đã học)
-- =====================================================
INSERT INTO giao_dich_dinh_ky (
        id_nguoi_dung,
        id_danh_muc,
        mo_ta,
        so_tien,
        tan_suat,
        ngay_bat_dau,
        lan_tiep_theo
    )
VALUES (
        @demo_user_id,
        @cat_hoa_don,
        'EVN',
        350000.00,
        'MONTHLY',
        '2025-11-08',
        '2025-12-08'
    ),
    (
        @demo_user_id,
        @cat_hoa_don,
        'VNPT',
        200000.00,
        'MONTHLY',
        '2025-11-08',
        '2025-12-08'
    ),
    (
        @demo_user_id,
        @cat_giai_tri,
        'NETFLIX',
        199000.00,
        'MONTHLY',
        '2025-11-12',
        '2025-12-12'
    ),
    (
        @demo_user_id,
        @cat_an_uong,
        'HIGHLANDS COFFEE',
        47500.00,
        'WEEKLY',
        '2025-11-19',
        '2025-11-26'
    );
-- =====================================================
-- 10. DEMO DATA - Thong bao
-- =====================================================
INSERT INTO thong_bao (id_nguoi_dung, loai, tieu_de, noi_dung, da_doc)
VALUES (
        @demo_user_id,
        'BUDGET_ALERT',
        '⚠️ Vượt ngân sách Mua sắm',
        'Bạn đã chi 3,050,000đ/2,000,000đ (153%). Hãy cân nhắc giảm chi tiêu!',
        FALSE
    ),
    (
        @demo_user_id,
        'SAVINGS_TIP',
        '💡 Tiết kiệm thông minh',
        'Bạn uống cafe 4 lần/tuần (190k). Nấu cafe tại nhà giúp tiết kiệm 120k/tuần!',
        FALSE
    ),
    (
        @demo_user_id,
        'ANOMALY_DETECTED',
        '🔍 Phát hiện bất thường',
        'Chi tiêu Mua sắm tháng này cao hơn trung bình 85%. Kiểm tra lại nhé!',
        TRUE
    ),
    (
        @demo_user_id,
        'MONTHLY_REPORT',
        '📊 Báo cáo tháng 11',
        'Tổng thu: 15tr. Tổng chi: 7.5tr. Tiết kiệm: 7.5tr (50%). Tuyệt vời! 🎉',
        TRUE
    );