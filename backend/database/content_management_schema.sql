-- =====================================================
-- FINPAL - Content Management Tables
-- Mẫu thông báo, Mẹo tiết kiệm, FAQ
-- =====================================================
-- 1. Bảng Mẫu Thông báo (Notification Templates)
CREATE TABLE IF NOT EXISTS mau_thong_bao (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ma_mau VARCHAR(20) NOT NULL UNIQUE COMMENT 'Template code: NOT001, NOT002...',
    tieu_de VARCHAR(255) NOT NULL COMMENT 'Tiêu đề thông báo',
    noi_dung_mau TEXT NOT NULL COMMENT 'Message template với placeholders {amount}, {category}...',
    loai ENUM('WARNING', 'ALERT', 'SUCCESS', 'INFO') NOT NULL DEFAULT 'INFO' COMMENT 'Loại thông báo',
    so_lan_gui INT DEFAULT 0 COMMENT 'Số lần đã gửi',
    trang_thai ENUM('ACTIVE', 'INACTIVE', 'DRAFT') NOT NULL DEFAULT 'DRAFT' COMMENT 'Trạng thái',
    nguoi_tao BIGINT COMMENT 'ID admin tạo',
    ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ngay_cap_nhat TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_template_status (trang_thai),
    INDEX idx_template_type (loai),
    FOREIGN KEY (nguoi_tao) REFERENCES nguoi_dung(id) ON DELETE
    SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
-- 2. Bảng Mẹo và Gợi ý (Tips & Suggestions)
CREATE TABLE IF NOT EXISTS meo_goi_y (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ma_tip VARCHAR(20) NOT NULL UNIQUE COMMENT 'Tip code: TIP001, TIP002...',
    tieu_de VARCHAR(255) NOT NULL COMMENT 'Tiêu đề tip',
    noi_dung TEXT NOT NULL COMMENT 'Nội dung chi tiết',
    danh_muc ENUM(
        'SAVING',
        'BUDGETING',
        'INVESTING',
        'SPENDING',
        'GENERAL'
    ) DEFAULT 'GENERAL' COMMENT 'Danh mục',
    icon VARCHAR(100) COMMENT 'Icon name hoặc emoji',
    luot_xem INT DEFAULT 0 COMMENT 'Số lượt xem',
    luot_thich INT DEFAULT 0 COMMENT 'Số lượt thích',
    thu_tu INT DEFAULT 0 COMMENT 'Thứ tự hiển thị',
    trang_thai ENUM('ACTIVE', 'INACTIVE', 'DRAFT') NOT NULL DEFAULT 'DRAFT' COMMENT 'Trạng thái',
    nguoi_tao BIGINT COMMENT 'ID admin tạo',
    ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ngay_cap_nhat TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tip_status (trang_thai),
    INDEX idx_tip_category (danh_muc),
    INDEX idx_tip_order (thu_tu),
    FOREIGN KEY (nguoi_tao) REFERENCES nguoi_dung(id) ON DELETE
    SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
-- 3. Bảng Câu hỏi Thường gặp (FAQ)
CREATE TABLE IF NOT EXISTS cau_hoi_thuong_gap (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ma_faq VARCHAR(20) NOT NULL UNIQUE COMMENT 'FAQ code: FAQ001, FAQ002...',
    cau_hoi TEXT NOT NULL COMMENT 'Câu hỏi',
    cau_tra_loi TEXT NOT NULL COMMENT 'Câu trả lời',
    danh_muc ENUM(
        'ACCOUNT',
        'TRANSACTION',
        'BUDGET',
        'SAVINGS',
        'SECURITY',
        'PAYMENT',
        'GENERAL'
    ) DEFAULT 'GENERAL' COMMENT 'Danh mục',
    luot_xem INT DEFAULT 0 COMMENT 'Số lượt xem',
    co_huu_ich INT DEFAULT 0 COMMENT 'Số lượt đánh giá hữu ích',
    khong_huu_ich INT DEFAULT 0 COMMENT 'Số lượt đánh giá không hữu ích',
    thu_tu INT DEFAULT 0 COMMENT 'Thứ tự hiển thị',
    trang_thai ENUM('ACTIVE', 'INACTIVE', 'DRAFT') NOT NULL DEFAULT 'DRAFT' COMMENT 'Trạng thái',
    nguoi_tao BIGINT COMMENT 'ID admin tạo',
    ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ngay_cap_nhat TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_faq_status (trang_thai),
    INDEX idx_faq_category (danh_muc),
    INDEX idx_faq_order (thu_tu),
    FOREIGN KEY (nguoi_tao) REFERENCES nguoi_dung(id) ON DELETE
    SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
-- =====================================================
-- SEED DATA - Dữ liệu mẫu
-- =====================================================
-- Mẫu thông báo
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
-- FAQ
INSERT INTO cau_hoi_thuong_gap (
        ma_faq,
        cau_hoi,
        cau_tra_loi,
        danh_muc,
        thu_tu,
        trang_thai
    )
VALUES (
        'FAQ001',
        'Làm sao để liên kết tài khoản ngân hàng?',
        'Vào Cài đặt > Liên kết ngân hàng > Chọn ngân hàng của bạn > Đăng nhập và xác thực. Ứng dụng sử dụng kết nối bảo mật và không lưu thông tin đăng nhập ngân hàng.',
        'ACCOUNT',
        1,
        'ACTIVE'
    ),
    (
        'FAQ002',
        'Giao dịch SMS được đọc tự động như thế nào?',
        'FinPal đọc tin nhắn SMS từ các ngân hàng được hỗ trợ để tự động ghi nhận giao dịch. Bạn cần cấp quyền đọc SMS trong cài đặt điện thoại.',
        'TRANSACTION',
        2,
        'ACTIVE'
    ),
    (
        'FAQ003',
        'Tôi có thể tạo bao nhiêu ngân sách?',
        'Bạn có thể tạo không giới hạn số lượng ngân sách. Mỗi ngân sách có thể theo danh mục, theo tuần/tháng/năm với số tiền tùy chỉnh.',
        'BUDGET',
        3,
        'ACTIVE'
    ),
    (
        'FAQ004',
        'Dữ liệu của tôi có được bảo mật không?',
        'Có. Tất cả dữ liệu được mã hóa end-to-end, lưu trữ trên server bảo mật. Chúng tôi không bao giờ chia sẻ hoặc bán dữ liệu của bạn cho bên thứ ba.',
        'SECURITY',
        4,
        'ACTIVE'
    ),
    (
        'FAQ005',
        'Làm sao để xuất báo cáo chi tiêu?',
        'Vào Thống kê > Xuất báo cáo > Chọn khoảng thời gian > Chọn định dạng (PDF/Excel). Báo cáo sẽ được gửi qua email hoặc tải xuống trực tiếp.',
        'TRANSACTION',
        5,
        'ACTIVE'
    ),
    (
        'FAQ006',
        'Mục tiêu tiết kiệm hoạt động như thế nào?',
        'Bạn đặt mục tiêu với số tiền và thời hạn. Ứng dụng sẽ theo dõi tiến độ, nhắc nhở góp tiền và đề xuất số tiền cần tiết kiệm mỗi tháng để đạt mục tiêu.',
        'SAVINGS',
        6,
        'ACTIVE'
    );