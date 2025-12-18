-- =====================================================
-- Script: Xóa và tạo lại templates thông báo
-- Chạy trên production server để cập nhật templates
-- =====================================================

-- Xóa tất cả templates cũ
DELETE FROM mau_thong_bao;

-- Thêm templates mới (NOT001-NOT015)
INSERT INTO mau_thong_bao (
    ma_mau,
    tieu_de,
    noi_dung_mau,
    loai,
    trang_thai,
    so_lan_gui
)
VALUES 
    -- =====================================================
    -- NOT001-NOT005: Templates cho Backend Java (Legacy)
    -- =====================================================
    (
        'NOT001',
        '⚠️ Chi tiêu vượt ngân sách',
        'Bạn đã chi {amount} cho {category}, vượt {percent}% so với kế hoạch!',
        'WARNING',
        'ACTIVE',
        0
    ),
    (
        'NOT002',
        '🔔 Giao dịch bất thường',
        'Phát hiện giao dịch {amount} tại {location}, không khớp với thói quen của bạn',
        'ALERT',
        'ACTIVE',
        0
    ),
    (
        'NOT003',
        '🎉 Tiết kiệm tốt',
        'Tuyệt vời! Bạn đã tiết kiệm được {amount} trong tháng này 🎉',
        'SUCCESS',
        'ACTIVE',
        0
    ),
    (
        'NOT004',
        '📅 Nhắc nhở thanh toán',
        'Bạn có hóa đơn {billName} sắp đến hạn vào {dueDate}',
        'INFO',
        'ACTIVE',
        0
    ),
    (
        'NOT005',
        '🎯 Mục tiêu tiết kiệm',
        'Chỉ còn {daysLeft} ngày nữa để hoàn thành mục tiêu {goalName}!',
        'WARNING',
        'ACTIVE',
        0
    ),
    -- =====================================================
    -- NOT006-NOT015: Templates cho BackendAI (AI Insights)
    -- =====================================================
    (
        'NOT006',
        '⚠️ Cảnh báo Ngân sách',
        'Bạn đã chi {percentage} hạn mức ''{budget_name}'' ({spent_amount}/{budget_amount}), còn {days_remaining} ngày nữa là hết kỳ ngân sách.',
        'WARNING',
        'ACTIVE',
        0
    ),
    (
        'NOT007',
        '🚨 Ngân sách Vượt quá!',
        'Ngân sách ''{budget_name}'' đã vượt quá! Đã chi {percentage} ({spent_amount}/{budget_amount})',
        'ALERT',
        'ACTIVE',
        0
    ),
    (
        'NOT008',
        '💡 Gợi ý Tiết kiệm Thông minh',
        'FinPal nhận thấy bạn chi trung bình {weekly_avg} cho ''{category}'' mỗi tuần. Nếu bạn giảm còn {suggested_weekly}, bạn sẽ tiết kiệm được {monthly_savings}/tháng!',
        'INFO',
        'ACTIVE',
        0
    ),
    (
        'NOT009',
        '🔔 Phát hiện Chi tiêu Bất thường',
        'Chi tiêu ''{category}'' tháng này ({current_amount}) cao hơn {increase_percent} so với trung bình ({average_amount}).',
        'WARNING',
        'ACTIVE',
        0
    ),
    (
        'NOT010',
        '🎉 Thành tích Tiết kiệm',
        'Tuyệt vời! Bạn đã tiết kiệm được trong danh mục ''{category}'' tháng này. Chi tiêu thấp hơn {save_percent} so với trung bình!',
        'SUCCESS',
        'ACTIVE',
        0
    ),
    (
        'NOT011',
        '💡 Mẹo Quản lý Chi tiêu',
        'Chi tiêu ''{category}'' đang có xu hướng tăng. Cân nhắc xem xét lại các khoản chi này.',
        'INFO',
        'ACTIVE',
        0
    ),
    (
        'NOT012',
        '🎯 Mục tiêu còn 7 ngày!',
        'Mục tiêu ''{goal_name}'' còn 7 ngày! Tiến độ: {progress} ({current_amount}/{target_amount}). Cố gắng thêm nhé! 💪',
        'WARNING',
        'ACTIVE',
        0
    ),
    (
        'NOT013',
        '⏰ Deadline Mục tiêu Hôm nay!',
        'Hôm nay là deadline của mục tiêu ''{goal_name}''! Tiến độ: {progress} ({current_amount}/{target_amount})',
        'ALERT',
        'ACTIVE',
        0
    ),
    (
        'NOT014',
        '🎉 Chúc mừng! Đạt Mục tiêu!',
        'Tuyệt vời! Bạn đã hoàn thành mục tiêu ''{goal_name}'' ({target_amount})! 🎊',
        'SUCCESS',
        'ACTIVE',
        0
    ),
    (
        'NOT015',
        '📊 Tổng kết Tháng',
        'Tháng {month}: Tổng thu {total_income} - Tổng chi {total_expense} = Tiết kiệm {savings} ({savings_percent})',
        'INFO',
        'ACTIVE',
        0
    ),
    (
        'NOT016',
        '📅 Tổng kết Tuần',
        'Tuần vừa qua: Tổng thu {total_income} - Tổng chi {total_expense} = Tiết kiệm {savings} ({savings_percent})',
        'INFO',
        'ACTIVE',
        0
    );

-- Kiểm tra kết quả
SELECT ma_mau, tieu_de, loai, trang_thai FROM mau_thong_bao ORDER BY ma_mau;
