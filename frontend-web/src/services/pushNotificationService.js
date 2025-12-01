/**
 * Push Notification Service
 * Quản lý thông báo đẩy trên trình duyệt
 */

// Kiểm tra trình duyệt có hỗ trợ Notification không
export const isNotificationSupported = () => {
    return 'Notification' in window;
};

// Lấy trạng thái quyền thông báo hiện tại
export const getPermissionStatus = () => {
    if (!isNotificationSupported()) {
        return 'unsupported';
    }
    return Notification.permission; // 'default', 'granted', 'denied'
};

// Yêu cầu quyền thông báo
export const requestPermission = async () => {
    if (!isNotificationSupported()) {
        throw new Error('Notifications are not supported in this browser');
    }

    const permission = await Notification.requestPermission();
    return permission;
};

// Gửi thông báo
export const sendNotification = (title, options = {}) => {
    if (!isNotificationSupported()) {
        console.warn('Notifications are not supported');
        return null;
    }

    if (Notification.permission !== 'granted') {
        console.warn('Notification permission not granted');
        return null;
    }

    // Kiểm tra settings từ localStorage
    const savedSettings = localStorage.getItem('notificationSettings');
    if (savedSettings) {
        const settings = JSON.parse(savedSettings);
        if (!settings.pushNotification) {
            console.log('Push notifications are disabled in settings');
            return null;
        }
    }

    const defaultOptions = {
        icon: '/favicon.ico',
        badge: '/favicon.ico',
        requireInteraction: false,
        silent: false,
    };

    const notification = new Notification(title, {
        ...defaultOptions,
        ...options,
    });

    notification.onclick = (event) => {
        event.preventDefault();
        window.focus();
        notification.close();
        
        // Nếu có callback onclick trong options
        if (options.onClick) {
            options.onClick(event);
        }
    };

    // Tự động đóng sau timeout (mặc định 5 giây)
    const timeout = options.timeout || 5000;
    if (timeout > 0) {
        setTimeout(() => notification.close(), timeout);
    }

    return notification;
};

// Gửi thông báo giao dịch mới
export const sendTransactionNotification = (transaction) => {
    const savedSettings = localStorage.getItem('notificationSettings');
    if (savedSettings) {
        const settings = JSON.parse(savedSettings);
        if (!settings.transactionAlert) {
            return null;
        }
    }

    const isExpense = transaction.type === 'EXPENSE';
    const amount = new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND',
    }).format(transaction.amount);

    return sendNotification(
        isExpense ? '💸 Chi tiêu mới' : '💰 Thu nhập mới',
        {
            body: `${transaction.description || transaction.categoryName}: ${amount}`,
            tag: `transaction-${transaction.id}`,
        }
    );
};

// Gửi thông báo vượt ngân sách
export const sendBudgetAlertNotification = (budget) => {
    const savedSettings = localStorage.getItem('notificationSettings');
    if (savedSettings) {
        const settings = JSON.parse(savedSettings);
        if (!settings.budgetAlert) {
            return null;
        }
    }

    const percentage = Math.round((budget.spent / budget.limit) * 100);
    
    return sendNotification(
        '⚠️ Cảnh báo ngân sách',
        {
            body: `Bạn đã chi ${percentage}% ngân sách ${budget.categoryName}`,
            tag: `budget-${budget.id}`,
        }
    );
};

// Gửi thông báo nhắc nhở mục tiêu tiết kiệm
export const sendGoalReminderNotification = (goal) => {
    const savedSettings = localStorage.getItem('notificationSettings');
    if (savedSettings) {
        const settings = JSON.parse(savedSettings);
        if (!settings.goalReminder) {
            return null;
        }
    }

    const percentage = Math.round((goal.currentAmount / goal.targetAmount) * 100);
    const remaining = new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND',
    }).format(goal.targetAmount - goal.currentAmount);

    return sendNotification(
        '🎯 Nhắc nhở mục tiêu',
        {
            body: `${goal.name}: Còn ${remaining} để đạt mục tiêu (${percentage}%)`,
            tag: `goal-${goal.id}`,
        }
    );
};

// Gửi thông báo báo cáo
export const sendReportNotification = (type, summary) => {
    const savedSettings = localStorage.getItem('notificationSettings');
    if (savedSettings) {
        const settings = JSON.parse(savedSettings);
        if (type === 'weekly' && !settings.weeklyReport) {
            return null;
        }
        if (type === 'monthly' && !settings.monthlyReport) {
            return null;
        }
    }

    const title = type === 'weekly' ? '📊 Báo cáo tuần' : '📊 Báo cáo tháng';
    const expense = new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND',
    }).format(summary.totalExpense);

    return sendNotification(title, {
        body: `Tổng chi tiêu: ${expense}. Nhấn để xem chi tiết.`,
        tag: `report-${type}`,
    });
};

export default {
    isNotificationSupported,
    getPermissionStatus,
    requestPermission,
    sendNotification,
    sendTransactionNotification,
    sendBudgetAlertNotification,
    sendGoalReminderNotification,
    sendReportNotification,
};
