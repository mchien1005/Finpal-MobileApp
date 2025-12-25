import api from './api';

/**
 * FAQ Service - Quản lý các câu hỏi thường gặp
 */

// Lấy tất cả FAQs đang active
export const getActiveFAQs = async (category = null) => {
    const params = category ? { category } : {};
    const response = await api.get('/faqs', { params });
    return response.data;
};

// Lấy FAQ theo ID và tăng view count
export const getFAQById = async (id) => {
    const response = await api.get(`/faqs/${id}`);
    return response.data;
};

// Submit feedback cho FAQ (helpful/not helpful)
export const submitFAQFeedback = async (id, isHelpful) => {
    const response = await api.post(`/faqs/${id}/feedback`, { helpful: isHelpful });
    return response.data;
};

// Map category từ backend sang tiếng Việt
export const getCategoryLabel = (category) => {
    const labels = {
        GETTING_STARTED: 'Bắt đầu',
        SECURITY: 'Bảo mật & Quyền riêng tư',
        FEATURES: 'Tính năng',
        TROUBLESHOOTING: 'Khắc phục sự cố',
    };
    return labels[category] || category;
};

// Nhóm FAQs theo category
export const groupFAQsByCategory = (faqs) => {
    const grouped = {};

    faqs.forEach(faq => {
        const category = faq.category || 'GETTING_STARTED';
        if (!grouped[category]) {
            grouped[category] = {
                category: getCategoryLabel(category),
                categoryKey: category,
                items: []
            };
        }
        grouped[category].items.push({
            id: faq.id,
            q: faq.question,
            a: faq.answer,
            viewCount: faq.viewCount,
            helpfulCount: faq.helpfulCount,
            notHelpfulCount: faq.notHelpfulCount
        });
    });

    // Sắp xếp theo thứ tự ưu tiên
    const categoryOrder = ['GETTING_STARTED', 'SECURITY', 'FEATURES', 'TROUBLESHOOTING'];

    return categoryOrder
        .filter(cat => grouped[cat])
        .map(cat => grouped[cat]);
};

export default {
    getActiveFAQs,
    getFAQById,
    submitFAQFeedback,
    getCategoryLabel,
    groupFAQsByCategory
};
