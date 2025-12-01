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
        '$2a$10$PtD9U/K40h7.STHT9pV8ouDWPswAdb6eMgpgQn2vZtLNONsIA9OH.',
        'demo@finpal.vn',
        'Nguyễn Văn Demo',
        'USER',
        TRUE
    ),
    (
        'admin',
        '$2a$10$PtD9U/K40h7.STHT9pV8ouDWPswAdb6eMgpgQn2vZtLNONsIA9OH.',
        'admin@finpal.vn',
        'Quản Trị Viên',
        'ADMIN',
        TRUE
    );
-- =====================================================
-- 2. DANH_MUC - Danh mục hệ thống
-- =====================================================
INSERT INTO danh_muc (id_cha, ten_danh_muc, loai)
VALUES -- EXPENSE Categories (Chi tiêu - 15 danh mục)
    (NULL, 'Ăn uống', 'EXPENSE'),
    -- 1: Nhà hàng, quán ăn, cafe, trà sữa
    (NULL, 'Di chuyển', 'EXPENSE'),
    -- 2: Grab, taxi, xăng, gửi xe
    (NULL, 'Mua sắm', 'EXPENSE'),
    -- 3: Quần áo, giày dép, phụ kiện
    (NULL, 'Giải trí', 'EXPENSE'),
    -- 4: Phim, game, du lịch, sở thích
    (NULL, 'Sức khỏe', 'EXPENSE'),
    -- 5: Khám bệnh, thuốc, gym, spa
    (NULL, 'Giáo dục', 'EXPENSE'),
    -- 6: Học phí, sách, khóa học
    (NULL, 'Hóa đơn & Tiện ích', 'EXPENSE'),
    -- 7: Điện, nước, internet, điện thoại
    (NULL, 'Nhà ở', 'EXPENSE'),
    -- 8: Thuê nhà, sửa chữa, nội thất
    (NULL, 'Gia đình', 'EXPENSE'),
    -- 9: Biếu bố mẹ, nuôi con, thú cưng
    (NULL, 'Bảo hiểm', 'EXPENSE'),
    -- 10: BHYT, BHXH, bảo hiểm nhân thọ
    (NULL, 'Đầu tư', 'EXPENSE'),
    -- 11: Chứng khoán, crypto, vàng
    (NULL, 'Quà tặng', 'EXPENSE'),
    -- 12: Sinh nhật, cưới hỏi, lễ tết
    (NULL, 'Công việc', 'EXPENSE'),
    -- 13: Dụng cụ, phần mềm, họp hành
    (NULL, 'Làm đẹp', 'EXPENSE'),
    -- 14: Tóc, nail, mỹ phẩm
    (NULL, 'Khác (Chi)', 'EXPENSE'),
    -- 15: Chi tiêu khác
    -- INCOME Categories (Thu nhập - 8 danh mục)
    (NULL, 'Lương', 'INCOME'),
    -- 16: Lương chính
    (NULL, 'Thưởng', 'INCOME'),
    -- 17: Thưởng, KPI, lễ tết
    (NULL, 'Làm thêm', 'INCOME'),
    -- 18: Freelance, part-time, OT
    (NULL, 'Kinh doanh', 'INCOME'),
    -- 19: Bán hàng, dịch vụ
    (NULL, 'Đầu tư', 'INCOME'),
    -- 20: Cổ tức, lãi, crypto
    (NULL, 'Cho vay', 'INCOME'),
    -- 21: Thu nợ, cho vay lại
    (NULL, 'Được tặng', 'INCOME'),
    -- 22: Quà, lì xì, biếu
    (NULL, 'Khác (Thu)', 'INCOME');
-- 23: Thu nhập khác
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
VALUES -- ========== ĂN UỐNG (1) ==========
    -- Đồ uống
    ('HIGHLANDS', 1, 95),
    ('THE COFFEE HOUSE', 1, 95),
    ('PHUC LONG', 1, 95),
    ('STARBUCKS', 1, 95),
    ('KATINAT', 1, 95),
    ('GONGCHA', 1, 90),
    ('GONG CHA', 1, 90),
    ('BOBAPOP', 1, 90),
    ('DINGTEA', 1, 90),
    ('TOCOTOCO', 1, 90),
    ('TOCO TOCO', 1, 90),
    ('TRASUA', 1, 85),
    ('TRA SUA', 1, 85),
    ('CAFE', 1, 80),
    ('COFFEE', 1, 80),
    -- Đồ ăn nhanh
    ('GRAB FOOD', 1, 98),
    ('GRABFOOD', 1, 98),
    ('SHOPEE FOOD', 1, 98),
    ('SHOPEEFOOD', 1, 98),
    ('NOW', 1, 95),
    ('BAEMIN', 1, 95),
    ('GOJEK', 1, 95),
    ('KFC', 1, 95),
    ('LOTTERIA', 1, 95),
    ('MCDONALDS', 1, 95),
    ('BURGER KING', 1, 95),
    ('JOLLIBEE', 1, 95),
    ('PIZZA HUT', 1, 95),
    ('DOMINOS', 1, 95),
    ('PIZZA', 1, 85),
    -- Chuỗi nhà hàng
    ('KICHI KICHI', 1, 95),
    ('MANWAH', 1, 95),
    ('SUMO BBQ', 1, 95),
    ('GOGI', 1, 95),
    ('HOTPOT', 1, 90),
    ('BUFFET', 1, 90),
    ('LAU', 1, 85),
    ('NUONG', 1, 85),
    -- Cửa hàng tiện lợi
    ('CIRCLE K', 1, 80),
    ('MINISTOP', 1, 80),
    ('7ELEVEN', 1, 80),
    ('7-ELEVEN', 1, 80),
    ('FAMILYMART', 1, 80),
    ('GS25', 1, 80),
    ('VINMART+', 1, 75),
    ('BACHHOAXANH', 1, 75),
    ('BACH HOA XANH', 1, 75),
    -- Siêu thị
    ('COOPMART', 1, 70),
    ('BIGC', 1, 70),
    ('AEON', 1, 70),
    ('EMART', 1, 70),
    ('LOTTE MART', 1, 70),
    ('MEGA MARKET', 1, 70),
    ('WINMART', 1, 70),
    -- ========== DI CHUYỂN (2) ==========
    ('GRAB', 2, 90),
    ('BE', 2, 90),
    ('GOJEK', 2, 90),
    ('XANH SM', 2, 90),
    ('XANHSM', 2, 90),
    ('MAI LINH', 2, 90),
    ('VINASUN', 2, 90),
    ('TAXI', 2, 85),
    ('XE OM', 2, 85),
    -- Xăng dầu
    ('PETROLIMEX', 2, 100),
    ('PVOIL', 2, 100),
    ('XANG DAU', 2, 100),
    ('XANG', 2, 95),
    ('DO XANG', 2, 95),
    -- Gửi xe, phí đường
    ('GUI XE', 2, 90),
    ('PHI GIU XE', 2, 90),
    ('PHI DUONG', 2, 90),
    ('VETC', 2, 95),
    ('EPASS', 2, 95),
    -- Vé xe, máy bay
    ('VEXERE', 2, 95),
    ('VE XE', 2, 90),
    ('VIETNAM AIRLINES', 2, 95),
    ('VIETJET', 2, 95),
    ('BAMBOO', 2, 95),
    ('VE MAY BAY', 2, 90),
    -- ========== MUA SẮM (3) ==========
    -- E-commerce
    ('SHOPEE', 3, 98),
    ('LAZADA', 3, 98),
    ('TIKI', 3, 98),
    ('SENDO', 3, 95),
    ('THEGIOIDIDONG', 3, 95),
    ('THE GIOI DI DONG', 3, 95),
    ('CELLPHONES', 3, 95),
    ('FPT SHOP', 3, 95),
    ('DIEN MAY XANH', 3, 95),
    ('DIENMAYXANH', 3, 95),
    -- Thời trang
    ('UNIQLO', 3, 95),
    ('ZARA', 3, 95),
    ('H&M', 3, 95),
    ('CANIFA', 3, 90),
    ('ROUTINE', 3, 90),
    ('ELISE', 3, 90),
    ('IVY MODA', 3, 90),
    ('YAME', 3, 85),
    ('OWEN', 3, 85),
    ('ARISTINO', 3, 85),
    -- ========== GIẢI TRÍ (4) ==========
    -- Rạp phim
    ('CGV', 4, 100),
    ('LOTTE CINEMA', 4, 100),
    ('GALAXY', 4, 100),
    ('BHD', 4, 100),
    ('BETA', 4, 100),
    ('RAP PHIM', 4, 95),
    ('VE PHIM', 4, 95),
    -- Streaming
    ('NETFLIX', 4, 100),
    ('SPOTIFY', 4, 100),
    ('YOUTUBE PREMIUM', 4, 100),
    ('FPT PLAY', 4, 95),
    ('VTV GO', 4, 95),
    ('APPLE MUSIC', 4, 95),
    -- Game
    ('STEAM', 4, 95),
    ('GAME', 4, 85),
    ('GARENA', 4, 95),
    ('GOOGLE PLAY', 4, 90),
    ('APP STORE', 4, 90),
    -- Karaoke, giải trí
    ('KARAOKE', 4, 95),
    ('KICH', 4, 90),
    ('BILLIARD', 4, 90),
    ('BOWLING', 4, 90),
    -- ========== SỨC KHỎE (5) ==========
    -- Bệnh viện, phòng khám
    ('BENH VIEN', 5, 100),
    ('PHONG KHAM', 5, 100),
    ('VINMEC', 5, 100),
    ('MEDLATEC', 5, 100),
    ('NHA KHOA', 5, 100),
    ('KHAM BENH', 5, 95),
    -- Nhà thuốc
    ('NHA THUOC', 5, 100),
    ('PHARMACITY', 5, 100),
    ('LONG CHAU', 5, 100),
    ('AN KHANG', 5, 100),
    ('THUOC', 5, 85),
    -- Gym, Spa
    ('CALIFORNIA', 5, 95),
    ('CITIGYM', 5, 95),
    ('GYM', 5, 90),
    ('FITNESS', 5, 90),
    ('YOGA', 5, 90),
    ('SPA', 5, 85),
    ('MASSAGE', 5, 85),
    -- ========== GIÁO DỤC (6) ==========
    ('HOC PHI', 6, 100),
    ('TRUONG', 6, 90),
    ('DAI HOC', 6, 95),
    ('IELTS', 6, 95),
    ('TOEIC', 6, 95),
    ('ENGLISH', 6, 85),
    ('TIENG ANH', 6, 85),
    ('UDEMY', 6, 95),
    ('COURSERA', 6, 95),
    ('SACH', 6, 80),
    ('FAHASA', 6, 90),
    -- ========== HÓA ĐƠN & TIỆN ÍCH (7) ==========
    -- Điện
    ('EVN', 7, 100),
    ('DIEN LUC', 7, 100),
    ('TIEN DIEN', 7, 100),
    -- Nước
    ('NUOC', 7, 90),
    ('SAWACO', 7, 100),
    ('TIEN NUOC', 7, 100),
    -- Internet, điện thoại
    ('VNPT', 7, 100),
    ('VIETTEL', 7, 100),
    ('FPT TELECOM', 7, 100),
    ('MOBIFONE', 7, 100),
    ('VINAPHONE', 7, 100),
    ('NAP TIEN', 7, 90),
    ('CUOC', 7, 85),
    -- ========== NHÀ Ở (8) ==========
    ('TIEN NHA', 8, 100),
    ('THUE NHA', 8, 100),
    ('TIEN PHONG', 8, 100),
    ('NOI THAT', 8, 90),
    ('IKEA', 8, 95),
    ('UMA', 8, 90),
    ('SUA CHUA', 8, 85),
    -- ========== GIA ĐÌNH (9) ==========
    ('BIEU', 9, 85),
    ('BO ME', 9, 90),
    ('CON', 9, 85),
    ('THU CUNG', 9, 85),
    ('PET', 9, 85),
    ('CHO', 9, 80),
    ('MEO', 9, 80),
    -- ========== BẢO HIỂM (10) ==========
    ('BAO HIEM', 10, 100),
    ('BHXH', 10, 100),
    ('BHYT', 10, 100),
    ('PRUDENTIAL', 10, 100),
    ('MANULIFE', 10, 100),
    ('AIA', 10, 100),
    ('DAI LY', 10, 80),
    -- ========== ĐẦU TƯ CHI (11) ==========
    ('CHUNG KHOAN', 11, 95),
    ('CO PHIEU', 11, 95),
    ('TCBS', 11, 100),
    ('SSI', 11, 100),
    ('VNDIRECT', 11, 100),
    ('VPS', 11, 100),
    ('CRYPTO', 11, 95),
    ('BITCOIN', 11, 95),
    ('BINANCE', 11, 100),
    ('VANG', 11, 90),
    ('SJC', 11, 95),
    ('PNJ', 11, 90),
    -- ========== QUÀ TẶNG (12) ==========
    ('QUA', 12, 80),
    ('SINH NHAT', 12, 90),
    ('CUOI', 12, 90),
    ('DAM CUOI', 12, 95),
    ('LE TET', 12, 90),
    ('LI XI', 12, 95),
    -- ========== CÔNG VIỆC (13) ==========
    ('VAN PHONG', 13, 85),
    ('OFFICE', 13, 85),
    ('MICROSOFT', 13, 90),
    ('ZOOM', 13, 90),
    ('CANVA', 13, 90),
    ('ADOBE', 13, 90),
    ('NOTION', 13, 90),
    -- ========== LÀM ĐẸP (14) ==========
    ('TOC', 14, 85),
    ('CAT TOC', 14, 90),
    ('30SHINE', 14, 100),
    ('NAIL', 14, 90),
    ('MY PHAM', 14, 85),
    ('GUARDIAN', 14, 90),
    ('HASAKI', 14, 90),
    ('WATSONS', 14, 90),
    ('SOCIOLLA', 14, 95),
    -- ========== THU NHẬP - LƯƠNG (16) ==========
    ('LUONG', 16, 100),
    ('SALARY', 16, 100),
    ('CONG TY', 16, 80),
    ('COMPANY', 16, 80),
    -- ========== THU NHẬP - THƯỞNG (17) ==========
    ('THUONG', 17, 95),
    ('BONUS', 17, 95),
    ('KPI', 17, 90),
    ('HOA HONG', 17, 90),
    -- ========== THU NHẬP - LÀM THÊM (18) ==========
    ('FREELANCE', 18, 95),
    ('PART TIME', 18, 90),
    ('LAM THEM', 18, 90),
    ('OT', 18, 85),
    ('TANG CA', 18, 90),
    -- ========== THU NHẬP - KINH DOANH (19) ==========
    ('BAN HANG', 19, 90),
    ('DOANH THU', 19, 95),
    ('KHACH HANG', 19, 85),
    -- ========== THU NHẬP - ĐẦU TƯ (20) ==========
    ('CO TUC', 20, 100),
    ('LAI SUAT', 20, 95),
    ('LAI', 20, 80),
    ('TIEN LAI', 20, 95),
    -- ========== THU NHẬP - CHO VAY (21) ==========
    ('THU NO', 21, 95),
    ('TRA NO', 21, 90),
    ('HOAN TIEN', 21, 85),
    ('REFUND', 21, 90),
    -- ========== THU NHẬP - ĐƯỢC TẶNG (22) ==========
    ('DUOC TANG', 22, 90),
    ('NHAN QUA', 22, 90),
    ('LI XI', 22, 95);
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
-- Category IDs (Chi tiêu)
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
SET @cat_suc_khoe = (
        SELECT id
        FROM danh_muc
        WHERE ten_danh_muc = 'Sức khỏe'
        LIMIT 1
    );
SET @cat_giao_duc = (
        SELECT id
        FROM danh_muc
        WHERE ten_danh_muc = 'Giáo dục'
        LIMIT 1
    );
SET @cat_hoa_don = (
        SELECT id
        FROM danh_muc
        WHERE ten_danh_muc = 'Hóa đơn & Tiện ích'
        LIMIT 1
    );
SET @cat_nha_o = (
        SELECT id
        FROM danh_muc
        WHERE ten_danh_muc = 'Nhà ở'
        LIMIT 1
    );
SET @cat_gia_dinh = (
        SELECT id
        FROM danh_muc
        WHERE ten_danh_muc = 'Gia đình'
        LIMIT 1
    );
SET @cat_bao_hiem = (
        SELECT id
        FROM danh_muc
        WHERE ten_danh_muc = 'Bảo hiểm'
        LIMIT 1
    );
SET @cat_dau_tu_chi = (
        SELECT id
        FROM danh_muc
        WHERE ten_danh_muc = 'Đầu tư'
            AND loai = 'EXPENSE'
        LIMIT 1
    );
SET @cat_qua_tang = (
        SELECT id
        FROM danh_muc
        WHERE ten_danh_muc = 'Quà tặng'
        LIMIT 1
    );
SET @cat_cong_viec = (
        SELECT id
        FROM danh_muc
        WHERE ten_danh_muc = 'Công việc'
        LIMIT 1
    );
SET @cat_lam_dep = (
        SELECT id
        FROM danh_muc
        WHERE ten_danh_muc = 'Làm đẹp'
        LIMIT 1
    );
-- Category IDs (Thu nhập)
SET @cat_luong = (
        SELECT id
        FROM danh_muc
        WHERE ten_danh_muc = 'Lương'
        LIMIT 1
    );
SET @cat_thuong = (
        SELECT id
        FROM danh_muc
        WHERE ten_danh_muc = 'Thưởng'
        LIMIT 1
    );
SET @cat_lam_them = (
        SELECT id
        FROM danh_muc
        WHERE ten_danh_muc = 'Làm thêm'
        LIMIT 1
    );
SET @cat_kinh_doanh = (
        SELECT id
        FROM danh_muc
        WHERE ten_danh_muc = 'Kinh doanh'
        LIMIT 1
    );
SET @cat_dau_tu_thu = (
        SELECT id
        FROM danh_muc
        WHERE ten_danh_muc = 'Đầu tư'
            AND loai = 'INCOME'
        LIMIT 1
    );
SET @cat_cho_vay = (
        SELECT id
        FROM danh_muc
        WHERE ten_danh_muc = 'Cho vay'
        LIMIT 1
    );
SET @cat_duoc_tang = (
        SELECT id
        FROM danh_muc
        WHERE ten_danh_muc = 'Được tặng'
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
        ten_ngan_sach,
        so_tien,
        ngay_bat_dau,
        ngay_ket_thuc
    )
VALUES (
        @demo_user_id,
        @cat_an_uong,
        'Ngân sách Ăn uống T11',
        3000000.00,
        '2025-11-01',
        '2025-11-30'
    ),
    (
        @demo_user_id,
        @cat_di_chuyen,
        'Ngân sách Di chuyển T11',
        1500000.00,
        '2025-11-01',
        '2025-11-30'
    ),
    (
        @demo_user_id,
        @cat_mua_sam,
        'Ngân sách Mua sắm T11',
        2000000.00,
        '2025-11-01',
        '2025-11-30'
    ),
    (
        @demo_user_id,
        @cat_giai_tri,
        'Ngân sách Giải trí T11',
        1000000.00,
        '2025-11-01',
        '2025-11-30'
    ),
    (
        @demo_user_id,
        @cat_hoa_don,
        'Ngân sách Hóa đơn T11',
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
INSERT INTO mau_thong_bao (
        ma_mau,
        tieu_de,
        noi_dung_mau,
        loai,
        trang_thai,
        so_lan_gui
    )
VALUES (
        'NOT001',
        'Chi tiêu vượt ngân sách',
        'Bạn đã chi {amount} cho {category}, vượt {percent}% so với kế hoạch!',
        'WARNING',
        'ACTIVE',
        234
    ),
    (
        'NOT002',
        'Giao dịch bất thường',
        'Phát hiện giao dịch {amount} tại {location}, không khớp với thói quen của bạn',
        'ALERT',
        'ACTIVE',
        45
    ),
    (
        'NOT003',
        'Tiết kiệm tốt',
        'Tuyệt vời! Bạn đã tiết kiệm được {amount} trong tháng này 🎉',
        'SUCCESS',
        'ACTIVE',
        567
    ),
    (
        'NOT004',
        'Nhắc nhở thanh toán',
        'Bạn có hóa đơn {billName} sắp đến hạn vào {dueDate}',
        'INFO',
        'ACTIVE',
        123
    ),
    (
        'NOT005',
        'Mục tiêu tiết kiệm',
        'Chỉ còn {daysLeft} ngày nữa để hoàn thành mục tiêu {goalName}!',
        'WARNING',
        'ACTIVE',
        89
    );
-- Mẹo tiết kiệm
INSERT INTO meo_goi_y (
        ma_tip,
        tieu_de,
        noi_dung,
        danh_muc,
        icon,
        thu_tu,
        trang_thai
    )
VALUES (
        'TIP001',
        'Quy tắc 50/30/20',
        'Chia thu nhập: 50% cho nhu cầu thiết yếu, 30% cho mong muốn, 20% cho tiết kiệm. Đây là công thức đơn giản giúp bạn quản lý tài chính hiệu quả.',
        'BUDGETING',
        '💰',
        1,
        'ACTIVE'
    ),
    (
        'TIP002',
        'Tự động chuyển tiết kiệm',
        'Thiết lập lệnh chuyển tự động ngay sau khi nhận lương. Bạn sẽ tiết kiệm được nhiều hơn khi không thấy số tiền đó trong tài khoản chi tiêu.',
        'SAVING',
        '🏦',
        2,
        'ACTIVE'
    ),
    (
        'TIP003',
        'Theo dõi chi tiêu hàng ngày',
        'Ghi lại mọi khoản chi, dù nhỏ nhất. Điều này giúp bạn nhận ra những khoản chi không cần thiết và điều chỉnh thói quen.',
        'SPENDING',
        '📝',
        3,
        'ACTIVE'
    ),
    (
        'TIP004',
        'Chờ 24h trước khi mua',
        'Với những món đồ không cấp bách, hãy chờ 24 giờ trước khi quyết định mua. Thường bạn sẽ nhận ra mình không thực sự cần nó.',
        'SPENDING',
        '⏰',
        4,
        'ACTIVE'
    ),
    (
        'TIP005',
        'Quỹ khẩn cấp 6 tháng',
        'Cố gắng tiết kiệm đủ chi phí sinh hoạt 6 tháng cho quỹ khẩn cấp. Đây là "tấm đệm" an toàn cho những tình huống bất ngờ.',
        'SAVING',
        '🛡️',
        5,
        'ACTIVE'
    );
-- FAQ - Câu hỏi thường gặp
INSERT INTO cau_hoi_thuong_gap (
        ma_faq,
        cau_hoi,
        cau_tra_loi,
        danh_muc,
        thu_tu,
        trang_thai
    )
VALUES 
    -- Bắt đầu
    (
        'FAQ001',
        'FinPal là gì?',
        'FinPal là ứng dụng quản lý tài chính cá nhân thông minh, giúp bạn theo dõi thu chi, phân loại giao dịch tự động bằng AI và đặt mục tiêu tiết kiệm.',
        'GETTING_STARTED',
        1,
        'ACTIVE'
    ),
    (
        'FAQ002',
        'Làm sao để bắt đầu sử dụng FinPal?',
        'Tải ứng dụng, đăng ký tài khoản, cấp quyền đọc SMS và bắt đầu theo dõi chi tiêu của bạn ngay.',
        'GETTING_STARTED',
        2,
        'ACTIVE'
    ),
    (
        'FAQ003',
        'FinPal có miễn phí không?',
        'Có, FinPal hoàn toàn miễn phí với các tính năng cơ bản. Phiên bản Premium sẽ có thêm các tính năng nâng cao.',
        'GETTING_STARTED',
        3,
        'ACTIVE'
    ),
    -- Bảo mật & Quyền riêng tư
    (
        'FAQ004',
        'Dữ liệu của tôi có an toàn không?',
        'Dữ liệu của bạn được mã hóa và lưu trữ an toàn. Chúng tôi không chia sẻ thông tin cá nhân với bên thứ ba.',
        'SECURITY',
        4,
        'ACTIVE'
    ),
    (
        'FAQ005',
        'FinPal có chia sẻ thông tin của tôi không?',
        'Không, chúng tôi cam kết bảo mật thông tin của bạn và không chia sẻ với bất kỳ bên thứ ba nào.',
        'SECURITY',
        5,
        'ACTIVE'
    ),
    (
        'FAQ006',
        'Làm sao để bảo vệ tài khoản tốt hơn?',
        'Sử dụng mật khẩu mạnh, bật xác thực 2 lớp và không chia sẻ thông tin đăng nhập với người khác.',
        'SECURITY',
        6,
        'ACTIVE'
    ),
    -- Tính năng
    (
        'FAQ007',
        'AI phân loại giao dịch hoạt động như thế nào?',
        'AI của FinPal phân tích nội dung SMS ngân hàng để tự động nhận diện và phân loại giao dịch vào các danh mục phù hợp.',
        'FEATURES',
        7,
        'ACTIVE'
    ),
    (
        'FAQ008',
        'Tôi có thể chỉnh sửa giao dịch không?',
        'Có, bạn có thể chỉnh sửa danh mục, ghi chú và thông tin giao dịch bất cứ lúc nào.',
        'FEATURES',
        8,
        'ACTIVE'
    ),
    (
        'FAQ009',
        'Làm sao để đặt mục tiêu tiết kiệm?',
        'Vào tab "Mục tiêu", nhấn nút "+" để tạo mục tiêu mới. Nhập tên, số tiền mục tiêu và thời hạn, FinPal sẽ giúp bạn theo dõi tiến độ.',
        'FEATURES',
        9,
        'ACTIVE'
    ),
    -- Khắc phục sự cố
    (
        'FAQ010',
        'App không đọc được SMS từ ngân hàng?',
        'Kiểm tra lại quyền đọc SMS trong cài đặt điện thoại. Đảm bảo FinPal có quyền truy cập tin nhắn.',
        'TROUBLESHOOTING',
        10,
        'ACTIVE'
    ),
    (
        'FAQ011',
        'Giao dịch bị phân loại sai?',
        'Bạn có thể chỉnh sửa danh mục giao dịch. AI sẽ học từ các chỉnh sửa của bạn để cải thiện độ chính xác.',
        'TROUBLESHOOTING',
        11,
        'ACTIVE'
    ),
    (
        'FAQ012',
        'Quên mật khẩu thì làm sao?',
        'Nhấn "Quên mật khẩu" trên màn hình đăng nhập, nhập email và làm theo hướng dẫn để đặt lại mật khẩu.',
        'TROUBLESHOOTING',
        12,
        'ACTIVE'
    );