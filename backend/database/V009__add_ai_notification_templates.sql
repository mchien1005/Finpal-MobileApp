-- =====================================================
-- V009: Thêm mẫu thông báo cho AI Insights
-- =====================================================
-- Thêm các mẫu thông báo mới cho hệ thống AI Insights

-- Template cảnh báo ngân sách chi tiết
INSERT INTO mau_thong_bao (ma_mau, tieu_de, noi_dung_mau, loai, trang_thai, so_lan_gui)
VALUES 
    ('BUDGET_WARNING', '⚠️ Cảnh báo Ngân sách', 
     'Bạn đã chi {percentage} hạn mức ''{budget_name}'' ({spent_amount}/{budget_amount}), còn {days_remaining} ngày nữa là hết kỳ ngân sách.',
     'WARNING', 'ACTIVE', 0),
     
    ('BUDGET_EXCEEDED', '🚨 Ngân sách Vượt quá!', 
     'Ngân sách ''{budget_name}'' đã vượt quá! Đã chi {percentage} ({spent_amount}/{budget_amount})',
     'ALERT', 'ACTIVE', 0),

    ('SAVINGS_SUGGESTION', '💡 Gợi ý Tiết kiệm Thông minh',
     'FinPal nhận thấy bạn chi trung bình {weekly_avg} cho ''{category}'' mỗi tuần. Nếu bạn giảm còn {suggested_weekly}, bạn sẽ tiết kiệm được {monthly_savings}/tháng!',
     'INFO', 'ACTIVE', 0),

    ('ANOMALY_DETECTED', '🔔 Phát hiện Chi tiêu Bất thường',
     'Chi tiêu ''{category}'' tháng này ({current_amount}) cao hơn {increase_percent} so với trung bình ({average_amount}).',
     'WARNING', 'ACTIVE', 0),

    ('GOAL_REMINDER_7DAYS', '🎯 Mục tiêu còn 7 ngày!',
     'Mục tiêu ''{goal_name}'' còn 7 ngày! Tiến độ: {progress} ({current_amount}/{target_amount}). Cố gắng thêm nhé! 💪',
     'WARNING', 'ACTIVE', 0),

    ('GOAL_DEADLINE_TODAY', '⏰ Deadline Mục tiêu Hôm nay!',
     'Hôm nay là deadline của mục tiêu ''{goal_name}''! Tiến độ: {progress} ({current_amount}/{target_amount})',
     'ALERT', 'ACTIVE', 0),

    ('GOAL_COMPLETED', '🎉 Chúc mừng! Đạt Mục tiêu!',
     'Tuyệt vời! Bạn đã hoàn thành mục tiêu ''{goal_name}'' ({target_amount})! 🎊',
     'SUCCESS', 'ACTIVE', 0),

    ('SPENDING_ACHIEVEMENT', '🎉 Thành tích Tiết kiệm',
     'Tuyệt vời! Bạn đã tiết kiệm được trong danh mục ''{category}'' tháng này. Chi tiêu thấp hơn {save_percent} so với trung bình!',
     'SUCCESS', 'ACTIVE', 0),

    ('SPENDING_TIP', '💡 Mẹo Quản lý Chi tiêu',
     'Chi tiêu ''{category}'' đang có xu hướng tăng. Cân nhắc xem xét lại các khoản chi này.',
     'INFO', 'ACTIVE', 0),

    ('MONTHLY_SUMMARY', '📊 Tổng kết Tháng',
     'Tháng {month}: Tổng thu {total_income} - Tổng chi {total_expense} = Tiết kiệm {savings} ({savings_percent})',
     'INFO', 'ACTIVE', 0)

ON DUPLICATE KEY UPDATE 
    tieu_de = VALUES(tieu_de),
    noi_dung_mau = VALUES(noi_dung_mau),
    loai = VALUES(loai),
    trang_thai = VALUES(trang_thai);
