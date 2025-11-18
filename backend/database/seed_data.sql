-- =====================================================
-- FINPAL SEED DATA
-- Dữ liệu mẫu cho hệ thống
-- =====================================================
USE finpal_db;
-- =====================================================
-- CATEGORIES - Danh mục hệ thống
-- =====================================================
-- Categories Thu nhập
INSERT INTO categories (
        name,
        type,
        icon,
        color,
        is_system,
        display_order
    )
VALUES ('Lương', 'INCOME', '💰', '#4CAF50', TRUE, 1),
    ('Thưởng', 'INCOME', '🎁', '#8BC34A', TRUE, 2),
    ('Đầu tư', 'INCOME', '📈', '#009688', TRUE, 3),
    (
        'Thu nhập khác',
        'INCOME',
        '💵',
        '#00BCD4',
        TRUE,
        4
    );
-- Categories Chi tiêu chính
INSERT INTO categories (
        name,
        type,
        icon,
        color,
        is_system,
        display_order
    )
VALUES ('Ăn uống', 'EXPENSE', '🍔', '#FF5722', TRUE, 1),
    ('Mua sắm', 'EXPENSE', '🛒', '#E91E63', TRUE, 2),
    ('Di chuyển', 'EXPENSE', '🚗', '#9C27B0', TRUE, 3),
    ('Giải trí', 'EXPENSE', '🎬', '#673AB7', TRUE, 4),
    ('Hóa đơn', 'EXPENSE', '📄', '#3F51B5', TRUE, 5),
    ('Sức khỏe', 'EXPENSE', '⚕️', '#2196F3', TRUE, 6),
    ('Giáo dục', 'EXPENSE', '📚', '#03A9F4', TRUE, 7),
    ('Làm đẹp', 'EXPENSE', '💄', '#FF4081', TRUE, 8),
    (
        'Gia đình',
        'EXPENSE',
        '👨‍👩‍👧‍👦',
        '#795548',
        TRUE,
        9
    ),
    (
        'Chi tiêu khác',
        'EXPENSE',
        '💸',
        '#9E9E9E',
        TRUE,
        10
    );
-- Sub-categories cho Ăn uống
SET @an_uong_id = (
        SELECT id
        FROM categories
        WHERE name = 'Ăn uống'
        LIMIT 1
    );
INSERT INTO categories (
        name,
        type,
        icon,
        color,
        parent_id,
        is_system,
        display_order
    )
VALUES (
        'Ăn sáng',
        'EXPENSE',
        '🌅',
        '#FF6F00',
        @an_uong_id,
        TRUE,
        1
    ),
    (
        'Ăn trưa',
        'EXPENSE',
        '☀️',
        '#FF8F00',
        @an_uong_id,
        TRUE,
        2
    ),
    (
        'Ăn tối',
        'EXPENSE',
        '🌙',
        '#FFA000',
        @an_uong_id,
        TRUE,
        3
    ),
    (
        'Cafe/Trà sữa',
        'EXPENSE',
        '☕',
        '#FFB300',
        @an_uong_id,
        TRUE,
        4
    ),
    (
        'Nhậu/Bar',
        'EXPENSE',
        '🍺',
        '#FFC107',
        @an_uong_id,
        TRUE,
        5
    );
-- Sub-categories cho Di chuyển
SET @di_chuyen_id = (
        SELECT id
        FROM categories
        WHERE name = 'Di chuyển'
        LIMIT 1
    );
INSERT INTO categories (
        name,
        type,
        icon,
        color,
        parent_id,
        is_system,
        display_order
    )
VALUES (
        'Grab/Taxi',
        'EXPENSE',
        '🚕',
        '#7B1FA2',
        @di_chuyen_id,
        TRUE,
        1
    ),
    (
        'Xe bus',
        'EXPENSE',
        '🚌',
        '#8E24AA',
        @di_chuyen_id,
        TRUE,
        2
    ),
    (
        'Xăng xe',
        'EXPENSE',
        '⛽',
        '#9C27B0',
        @di_chuyen_id,
        TRUE,
        3
    ),
    (
        'Gửi xe',
        'EXPENSE',
        '🅿️',
        '#AB47BC',
        @di_chuyen_id,
        TRUE,
        4
    );
-- Sub-categories cho Hóa đơn
SET @hoa_don_id = (
        SELECT id
        FROM categories
        WHERE name = 'Hóa đơn'
        LIMIT 1
    );
INSERT INTO categories (
        name,
        type,
        icon,
        color,
        parent_id,
        is_system,
        display_order
    )
VALUES (
        'Điện',
        'EXPENSE',
        '💡',
        '#1976D2',
        @hoa_don_id,
        TRUE,
        1
    ),
    (
        'Nước',
        'EXPENSE',
        '💧',
        '#2196F3',
        @hoa_don_id,
        TRUE,
        2
    ),
    (
        'Internet',
        'EXPENSE',
        '🌐',
        '#42A5F5',
        @hoa_don_id,
        TRUE,
        3
    ),
    (
        'Điện thoại',
        'EXPENSE',
        '📱',
        '#64B5F6',
        @hoa_don_id,
        TRUE,
        4
    ),
    (
        'Nhà trọ',
        'EXPENSE',
        '🏠',
        '#1565C0',
        @hoa_don_id,
        TRUE,
        5
    );
-- =====================================================
-- CATEGORY RULES - Luật phân loại tự động
-- =====================================================
-- Rules cho Ăn uống
INSERT INTO category_rules (keyword, category_id, match_type, priority)
VALUES (
        'GRAB',
        (
            SELECT id
            FROM categories
            WHERE name = 'Grab/Taxi'
            LIMIT 1
        ), 'CONTAINS', 10
    ), (
        'GRABFOOD', (
            SELECT id
            FROM categories
            WHERE name = 'Ăn uống'
            LIMIT 1
        ), 'CONTAINS', 20
    ), (
        'SHOPEE', (
            SELECT id
            FROM categories
            WHERE name = 'Mua sắm'
            LIMIT 1
        ), 'CONTAINS', 10
    ), (
        'LAZADA', (
            SELECT id
            FROM categories
            WHERE name = 'Mua sắm'
            LIMIT 1
        ), 'CONTAINS', 10
    ), (
        'TIKI', (
            SELECT id
            FROM categories
            WHERE name = 'Mua sắm'
            LIMIT 1
        ), 'CONTAINS', 10
    ), (
        'CGV', (
            SELECT id
            FROM categories
            WHERE name = 'Giải trí'
            LIMIT 1
        ), 'CONTAINS', 10
    ), (
        'LOTTE', (
            SELECT id
            FROM categories
            WHERE name = 'Giải trí'
            LIMIT 1
        ), 'CONTAINS', 10
    ), (
        'HIGHLANDS', (
            SELECT id
            FROM categories
            WHERE name = 'Cafe/Trà sữa'
            LIMIT 1
        ), 'CONTAINS', 10
    ), (
        'STARBUCKS', (
            SELECT id
            FROM categories
            WHERE name = 'Cafe/Trà sữa'
            LIMIT 1
        ), 'CONTAINS', 10
    ), (
        'THE COFFEE', (
            SELECT id
            FROM categories
            WHERE name = 'Cafe/Trà sữa'
            LIMIT 1
        ), 'CONTAINS', 10
    ), (
        'PHUC LONG', (
            SELECT id
            FROM categories
            WHERE name = 'Cafe/Trà sữa'
            LIMIT 1
        ), 'CONTAINS', 10
    ), (
        'GONGCHA', (
            SELECT id
            FROM categories
            WHERE name = 'Cafe/Trà sữa'
            LIMIT 1
        ), 'CONTAINS', 10
    ), (
        'CIRCLE K', (
            SELECT id
            FROM categories
            WHERE name = 'Mua sắm'
            LIMIT 1
        ), 'CONTAINS', 10
    ), (
        'FAMILY MART', (
            SELECT id
            FROM categories
            WHERE name = 'Mua sắm'
            LIMIT 1
        ), 'CONTAINS', 10
    ), (
        'VINMART', (
            SELECT id
            FROM categories
            WHERE name = 'Mua sắm'
            LIMIT 1
        ), 'CONTAINS', 10
    ), (
        'COOPMART', (
            SELECT id
            FROM categories
            WHERE name = 'Mua sắm'
            LIMIT 1
        ), 'CONTAINS', 10
    ), (
        'BITI', (
            SELECT id
            FROM categories
            WHERE name = 'Mua sắm'
            LIMIT 1
        ), 'CONTAINS', 10
    ), (
        'GUARDIAN', (
            SELECT id
            FROM categories
            WHERE name = 'Sức khỏe'
            LIMIT 1
        ), 'CONTAINS', 10
    ), (
        'PHARMACITY', (
            SELECT id
            FROM categories
            WHERE name = 'Sức khỏe'
            LIMIT 1
        ), 'CONTAINS', 10
    );
-- =====================================================
-- SMS PARSERS - Mẫu phân tích SMS ngân hàng
-- =====================================================
-- Parser cho Vietcombank
INSERT INTO sms_parsers (
        bank_name,
        bank_code,
        sender_number,
        regex_pattern,
        field_mappings,
        sample_sms,
        is_active,
        priority
    )
VALUES (
        'Vietcombank',
        'VCB',
        'Vietcombank',
        'TK (\\d+) GD (-|\\+)([\\d,]+)VND luc (\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}:\\d{2}). SD ([\\d,]+)VND. (.*)',
        '{"account": 1, "type": 2, "amount": 3, "time": 4, "balance": 5, "description": 6}',
        'TK 1234567890 GD -500,000VND luc 17/11/2024 14:30:00. SD 2,500,000VND. GRAB VIETNAM',
        TRUE,
        1
    );
-- Parser cho Techcombank
INSERT INTO sms_parsers (
        bank_name,
        bank_code,
        sender_number,
        regex_pattern,
        field_mappings,
        sample_sms,
        is_active,
        priority
    )
VALUES (
        'Techcombank',
        'TCB',
        'Techcombank',
        'TK (\\d+) (-|\\+)([\\d,]+)d (\\d{2}/\\d{2}/\\d{2} \\d{2}:\\d{2}) SD ([\\d,]+)d (.*)',
        '{"account": 1, "type": 2, "amount": 3, "time": 4, "balance": 5, "description": 6}',
        'TK 9876543210 -200,000d 17/11/24 15:45 SD 1,800,000d SHOPEE',
        TRUE,
        1
    );
-- Parser cho ACB
INSERT INTO sms_parsers (
        bank_name,
        bank_code,
        sender_number,
        regex_pattern,
        field_mappings,
        sample_sms,
        is_active,
        priority
    )
VALUES (
        'ACB',
        'ACB',
        'ACB',
        'TK (\\d+) (-|\\+)([\\d,]+) \\d{2}/\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2} SD:([\\d,]+) (.*)',
        '{"account": 1, "type": 2, "amount": 3, "balance": 4, "description": 5}',
        'TK 1122334455 -150,000 17/11/24 16:20:30 SD:950,000 CGV CINEMAS',
        TRUE,
        1
    );
-- =====================================================
-- DEMO USERS
-- =====================================================
-- Password: 123456 (đã hash bằng BCrypt)
INSERT INTO users (
        username,
        password,
        email,
        full_name,
        phone,
        is_active,
        email_verified
    )
VALUES (
        'demo',
        '$2a$10$xQGVF3VDqPVvQqJ9xVHKAO5rZRq1VQH5P8LjPGTJP3pW8fCwN8fDK',
        'demo@finpal.com',
        'Người dùng Demo',
        '0901234567',
        TRUE,
        TRUE
    ),
    (
        'admin',
        '$2a$10$xQGVF3VDqPVvQqJ9xVHKAO5rZRq1VQH5P8LjPGTJP3pW8fCwN8fDK',
        'admin@finpal.com',
        'Quản trị viên',
        '0987654321',
        TRUE,
        TRUE
    );
-- =====================================================
-- DEMO DATA cho user 'demo'
-- =====================================================
-- Tài khoản ngân hàng
INSERT INTO accounts (
        user_id,
        bank_name,
        account_name,
        account_type,
        balance,
        is_active,
        icon,
        color
    )
VALUES (
        1,
        'Vietcombank',
        'Tài khoản chính',
        'BANK',
        5000000.00,
        TRUE,
        '🏦',
        '#007AC2'
    ),
    (
        1,
        'Techcombank',
        'Thẻ tín dụng',
        'CREDIT_CARD',
        10000000.00,
        TRUE,
        '💳',
        '#FF6B00'
    ),
    (
        1,
        'Tiền mặt',
        'Ví tiền',
        'CASH',
        500000.00,
        TRUE,
        '💵',
        '#4CAF50'
    );
-- Giao dịch mẫu
SET @luong_id = (
        SELECT id
        FROM categories
        WHERE name = 'Lương'
        LIMIT 1
    );
SET @cafe_id = (
        SELECT id
        FROM categories
        WHERE name = 'Cafe/Trà sữa'
        LIMIT 1
    );
SET @grab_id = (
        SELECT id
        FROM categories
        WHERE name = 'Grab/Taxi'
        LIMIT 1
    );
SET @an_uong_id = (
        SELECT id
        FROM categories
        WHERE name = 'Ăn uống'
        LIMIT 1
    );
SET @mua_sam_id = (
        SELECT id
        FROM categories
        WHERE name = 'Mua sắm'
        LIMIT 1
    );
SET @giai_tri_id = (
        SELECT id
        FROM categories
        WHERE name = 'Giải trí'
        LIMIT 1
    );
INSERT INTO transactions (
        user_id,
        account_id,
        category_id,
        amount,
        type,
        merchant,
        description,
        transaction_date,
        is_auto
    )
VALUES (
        1,
        1,
        @luong_id,
        15000000.00,
        'INCOME',
        'CÔNG TY ABC',
        'Lương tháng 11',
        '2024-11-01 09:00:00',
        FALSE
    ),
    (
        1,
        1,
        @cafe_id,
        45000.00,
        'EXPENSE',
        'HIGHLANDS COFFEE',
        'Cafe sáng',
        '2024-11-15 08:30:00',
        TRUE
    ),
    (
        1,
        1,
        @grab_id,
        85000.00,
        'EXPENSE',
        'GRAB VIETNAM',
        'Đi làm',
        '2024-11-15 08:00:00',
        TRUE
    ),
    (
        1,
        1,
        @an_uong_id,
        150000.00,
        'EXPENSE',
        'GRABFOOD',
        'Ăn trưa',
        '2024-11-15 12:00:00',
        TRUE
    ),
    (
        1,
        1,
        @mua_sam_id,
        500000.00,
        'EXPENSE',
        'SHOPEE',
        'Mua đồ',
        '2024-11-14 20:00:00',
        TRUE
    ),
    (
        1,
        1,
        @giai_tri_id,
        200000.00,
        'EXPENSE',
        'CGV CINEMAS',
        'Xem phim',
        '2024-11-13 19:00:00',
        TRUE
    );
-- Ngân sách
SET @an_uong_id = (
        SELECT id
        FROM categories
        WHERE name = 'Ăn uống'
        LIMIT 1
    );
SET @di_chuyen_id = (
        SELECT id
        FROM categories
        WHERE name = 'Di chuyển'
        LIMIT 1
    );
INSERT INTO budgets (
        user_id,
        category_id,
        name,
        amount,
        period,
        start_date,
        end_date,
        alert_threshold
    )
VALUES (
        1,
        @an_uong_id,
        'Ngân sách Ăn uống tháng 11',
        3000000.00,
        'MONTHLY',
        '2024-11-01',
        '2024-11-30',
        70
    ),
    (
        1,
        @di_chuyen_id,
        'Ngân sách Di chuyển tháng 11',
        1500000.00,
        'MONTHLY',
        '2024-11-01',
        '2024-11-30',
        70
    );
-- Mục tiêu tiết kiệm
INSERT INTO savings_goals (
        user_id,
        name,
        description,
        target_amount,
        current_amount,
        deadline,
        icon,
        color,
        status
    )
VALUES (
        1,
        'Mua tai nghe AirPods',
        'Tiết kiệm để mua tai nghe không dây',
        5000000.00,
        1500000.00,
        '2024-12-31',
        '🎧',
        '#1976D2',
        'ACTIVE'
    ),
    (
        1,
        'Du lịch Đà Lạt',
        'Chuyến đi cuối năm',
        10000000.00,
        3000000.00,
        '2024-12-20',
        '✈️',
        '#4CAF50',
        'ACTIVE'
    );
-- User preferences
INSERT INTO user_preferences (
        user_id,
        currency,
        language,
        timezone,
        budget_alert_threshold
    )
VALUES (1, 'VND', 'vi', 'Asia/Ho_Chi_Minh', 70),
    (2, 'VND', 'vi', 'Asia/Ho_Chi_Minh', 70);