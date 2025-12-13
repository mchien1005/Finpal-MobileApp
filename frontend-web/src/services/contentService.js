import api from './api';

/**
 * Content Service - Quản lý nội dung cho Admin (Notification Templates, Tips, FAQs)
 */

// ===================== NOTIFICATION TEMPLATES =====================

/**
 * Lấy tất cả notification templates
 * @param {string} status - Optional: 'ACTIVE', 'INACTIVE'
 */
export const getAllNotificationTemplates = async (status = null) => {
    const params = status ? { status } : {};
    const response = await api.get('/admin/notification-templates', { params });
    return response.data;
};

/**
 * Lấy template theo ID
 */
export const getNotificationTemplateById = async (id) => {
    const response = await api.get(`/admin/notification-templates/${id}`);
    return response.data;
};

/**
 * Tạo notification template mới
 */
export const createNotificationTemplate = async (templateData) => {
    const response = await api.post('/admin/notification-templates', templateData);
    return response.data;
};

/**
 * Cập nhật notification template
 */
export const updateNotificationTemplate = async (id, templateData) => {
    const response = await api.put(`/admin/notification-templates/${id}`, templateData);
    return response.data;
};

/**
 * Xóa notification template
 */
export const deleteNotificationTemplate = async (id) => {
    const response = await api.delete(`/admin/notification-templates/${id}`);
    return response.data;
};

/**
 * Gửi notification từ template
 */
export const sendNotificationFromTemplate = async (sendData) => {
    const response = await api.post('/admin/notification-templates/send', sendData);
    return response.data;
};

/**
 * Lấy thống kê notification templates
 */
export const getNotificationTemplateStatistics = async () => {
    const response = await api.get('/admin/notification-templates/statistics');
    return response.data;
};

// ===================== TIPS =====================

/**
 * Lấy tất cả tips (Admin)
 */
export const getAllTips = async () => {
    const response = await api.get('/admin/tips');
    return response.data;
};

/**
 * Lấy tip theo ID
 */
export const getTipById = async (id) => {
    const response = await api.get(`/admin/tips/${id}`);
    return response.data;
};

/**
 * Tạo tip mới
 */
export const createTip = async (tipData) => {
    const response = await api.post('/admin/tips', tipData);
    return response.data;
};

/**
 * Cập nhật tip
 */
export const updateTip = async (id, tipData) => {
    const response = await api.put(`/admin/tips/${id}`, tipData);
    return response.data;
};

/**
 * Xóa tip
 */
export const deleteTip = async (id) => {
    const response = await api.delete(`/admin/tips/${id}`);
    return response.data;
};

/**
 * Lấy thống kê tips
 */
export const getTipStatistics = async () => {
    const response = await api.get('/admin/tips/statistics');
    return response.data;
};

// ===================== FAQs =====================

/**
 * Lấy tất cả FAQs (Admin)
 */
export const getAllFAQs = async () => {
    const response = await api.get('/admin/faqs');
    return response.data;
};

/**
 * Lấy FAQ theo ID
 */
export const getFAQById = async (id) => {
    const response = await api.get(`/admin/faqs/${id}`);
    return response.data;
};

/**
 * Tạo FAQ mới
 */
export const createFAQ = async (faqData) => {
    const response = await api.post('/admin/faqs', faqData);
    return response.data;
};

/**
 * Cập nhật FAQ
 */
export const updateFAQ = async (id, faqData) => {
    const response = await api.put(`/admin/faqs/${id}`, faqData);
    return response.data;
};

/**
 * Xóa FAQ
 */
export const deleteFAQ = async (id) => {
    const response = await api.delete(`/admin/faqs/${id}`);
    return response.data;
};

/**
 * Lấy thống kê FAQs
 */
export const getFAQStatistics = async () => {
    const response = await api.get('/admin/faqs/statistics');
    return response.data;
};

// ===================== HELPER FUNCTIONS =====================

/**
 * Map template type sang label hiển thị
 */
export const getTemplateTypeLabel = (type) => {
    const labels = {
        BUDGET_WARNING: 'warning',
        ABNORMAL_TRANSACTION: 'alert',
        SAVING_GOAL: 'success',
        PROMOTION: 'info',
        SYSTEM: 'info',
    };
    return labels[type] || type?.toLowerCase() || 'info';
};

/**
 * Map template status sang label hiển thị
 */
export const getStatusLabel = (status) => {
    const labels = {
        ACTIVE: 'Active',
        INACTIVE: 'Inactive',
        DRAFT: 'Draft',
    };
    return labels[status] || status;
};

/**
 * Map tip category sang tiếng Việt
 */
export const getTipCategoryLabel = (category) => {
    const labels = {
        SAVING: 'Tiết kiệm',
        BUDGETING: 'Ngân sách',
        INVESTMENT: 'Đầu tư',
        SPENDING: 'Chi tiêu',
        GENERAL: 'Tổng quan',
    };
    return labels[category] || category;
};

/**
 * Map FAQ category sang tiếng Việt
 */
export const getFAQCategoryLabel = (category) => {
    const labels = {
        GETTING_STARTED: 'Hướng dẫn',
        SECURITY: 'Bảo mật',
        FEATURES: 'Tính năng',
        TROUBLESHOOTING: 'Khắc phục',
    };
    return labels[category] || category;
};

export default {
    // Notification Templates
    getAllNotificationTemplates,
    getNotificationTemplateById,
    createNotificationTemplate,
    updateNotificationTemplate,
    deleteNotificationTemplate,
    sendNotificationFromTemplate,
    getNotificationTemplateStatistics,
    // Tips
    getAllTips,
    getTipById,
    createTip,
    updateTip,
    deleteTip,
    getTipStatistics,
    // FAQs
    getAllFAQs,
    getFAQById,
    createFAQ,
    updateFAQ,
    deleteFAQ,
    getFAQStatistics,
    // Helpers
    getTemplateTypeLabel,
    getStatusLabel,
    getTipCategoryLabel,
    getFAQCategoryLabel,
};
