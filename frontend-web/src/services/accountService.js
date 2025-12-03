import api from './api';

/**
 * Account Service
 * Quản lý các API liên quan đến tài khoản ngân hàng
 */

// Lấy danh sách tài khoản của user
export const getAccounts = async () => {
    const response = await api.get('/accounts');
    return response.data;
};

// Lấy chi tiết một tài khoản
export const getAccountById = async (id) => {
    const response = await api.get(`/accounts/${id}`);
    return response.data;
};

// Tạo tài khoản mới
export const createAccount = async (accountData) => {
    const response = await api.post('/accounts', accountData);
    return response.data;
};

// Cập nhật tài khoản
export const updateAccount = async (id, accountData) => {
    const response = await api.put(`/accounts/${id}`, accountData);
    return response.data;
};

// Xóa tài khoản
export const deleteAccount = async (id) => {
    const response = await api.delete(`/accounts/${id}`);
    return response.data;
};

export default {
    getAccounts,
    getAccountById,
    createAccount,
    updateAccount,
    deleteAccount,
};
