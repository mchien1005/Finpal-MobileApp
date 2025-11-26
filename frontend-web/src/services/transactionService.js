import api from './api';

/**
 * Transaction Service
 * Quản lý các API liên quan đến giao dịch
 */

// Tạo giao dịch mới
export const createTransaction = async (transactionData) => {
    const response = await api.post('/transactions', transactionData);
    return response.data;
};

// Lấy danh sách giao dịch với filter và pagination
export const getTransactions = async (params = {}) => {
    const response = await api.get('/transactions', { params });
    return response.data;
};

// Lấy chi tiết một giao dịch
export const getTransactionById = async (id) => {
    const response = await api.get(`/transactions/${id}`);
    return response.data;
};

// Cập nhật giao dịch
export const updateTransaction = async (id, transactionData) => {
    const response = await api.put(`/transactions/${id}`, transactionData);
    return response.data;
};

// Xóa giao dịch
export const deleteTransaction = async (id) => {
    const response = await api.delete(`/transactions/${id}`);
    return response.data;
};

// Lấy giao dịch theo tháng
export const getTransactionsByMonth = async (year, month) => {
    const startDate = `${year}-${String(month).padStart(2, '0')}-01`;
    const lastDay = new Date(year, month, 0).getDate();
    const endDate = `${year}-${String(month).padStart(2, '0')}-${lastDay}`;
    
    return getTransactions({ startDate, endDate });
};

// Lấy tổng thu nhập/chi tiêu theo khoảng thời gian
export const getTransactionsSummary = async (startDate, endDate) => {
    const response = await api.get('/transactions', {
        params: { startDate, endDate, size: 1000 }
    });
    
    const transactions = response.data.content || [];
    
    const summary = transactions.reduce((acc, tx) => {
        if (tx.type === 'INCOME') {
            acc.totalIncome += tx.amount;
        } else {
            acc.totalExpense += tx.amount;
        }
        return acc;
    }, { totalIncome: 0, totalExpense: 0 });
    
    return summary;
};

export default {
    createTransaction,
    getTransactions,
    getTransactionById,
    updateTransaction,
    deleteTransaction,
    getTransactionsByMonth,
    getTransactionsSummary,
};
