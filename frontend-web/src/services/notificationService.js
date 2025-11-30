import api from './api';

/**
 * Notification Service - Quản lý thông báo
 */

// Lấy tất cả thông báo
export const getNotifications = async (isRead = null) => {
    const params = {};
    if (isRead !== null) {
        params.isRead = isRead;
    }
    const response = await api.get('/notifications', { params });
    return response.data;
};

// Lấy số thông báo chưa đọc
export const getUnreadCount = async () => {
    const response = await api.get('/notifications/unread-count');
    return response.data.unreadCount;
};

// Đánh dấu đã đọc một thông báo
export const markAsRead = async (id) => {
    const response = await api.put(`/notifications/${id}/read`);
    return response.data;
};

// Đánh dấu tất cả đã đọc
export const markAllAsRead = async () => {
    const response = await api.put('/notifications/read-all');
    return response.data;
};

// Xóa một thông báo
export const deleteNotification = async (id) => {
    await api.delete(`/notifications/${id}`);
};

// Xóa tất cả thông báo đã đọc
export const deleteAllRead = async () => {
    const response = await api.delete('/notifications/read');
    return response.data;
};

export default {
    getNotifications,
    getUnreadCount,
    markAsRead,
    markAllAsRead,
    deleteNotification,
    deleteAllRead,
};
