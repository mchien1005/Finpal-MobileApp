import api from './api';

/**
 * Category Service
 * Quản lý các API liên quan đến danh mục
 */

// Lấy tất cả danh mục
export const getCategories = async () => {
    const response = await api.get('/categories');
    return response.data;
};

// Lấy danh mục theo loại (INCOME hoặc EXPENSE)
export const getCategoriesByType = async (type) => {
    const response = await api.get('/categories', { params: { type } });
    return response.data;
};

// Lấy chi tiết danh mục
export const getCategoryById = async (id) => {
    const response = await api.get(`/categories/${id}`);
    return response.data;
};

export default {
    getCategories,
    getCategoriesByType,
    getCategoryById,
};
