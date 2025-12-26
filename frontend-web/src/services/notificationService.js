import api from './api';

// Lấy tất cả thông báo (có thể lọc theo isRead)
export const getNotifications = async (isRead = null) => {
    const params = {};
    if (isRead !== null) {
        params.isRead = isRead;
    }
    const res = await api.get('/notifications', { params });
    return res.data;
};

// Lấy số lượng thông báo chưa đọc
export const getUnreadCount = async () => {
    const res = await api.get('/notifications/unread-count');
    return res.data;
};

// Đánh dấu 1 thông báo đã đọc
export const markAsRead = async (id) => {
    const res = await api.put(`/notifications/${id}/read`);
    return res.data;
};

// Đánh dấu tất cả thông báo đã đọc
export const markAllAsRead = async () => {
    const res = await api.put('/notifications/read-all');
    return res.data;
};

// Xóa 1 thông báo
export const deleteNotification = async (id) => {
    const res = await api.delete(`/notifications/${id}`);
    return res.data;
};

// Xóa tất cả thông báo đã đọc
export const deleteAllRead = async () => {
    const res = await api.delete('/notifications/read');
    return res.data;
};

export default {
    getNotifications,
    getUnreadCount,
    markAsRead,
    markAllAsRead,
    deleteNotification,
    deleteAllRead,
};
