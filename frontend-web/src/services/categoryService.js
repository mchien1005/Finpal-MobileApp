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

// Tạo danh mục mới
export const createCategory = async (categoryData) => {
    const response = await api.post('/categories', categoryData);
    return response.data;
};

// Cập nhật danh mục
export const updateCategory = async (id, categoryData) => {
    const response = await api.put(`/categories/${id}`, categoryData);
    return response.data;
};

// Xóa danh mục
export const deleteCategory = async (id) => {
    const response = await api.delete(`/categories/${id}`);
    return response.data;
};

// Lấy thống kê danh mục
export const getCategoryStats = async () => {
    const response = await api.get('/categories/stats');
    return response.data;
};

export default {
    getCategories,
    getCategoriesByType,
    getCategoryById,
    createCategory,
    updateCategory,
    deleteCategory,
    getCategoryStats,
};
