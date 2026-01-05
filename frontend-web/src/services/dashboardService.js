import api from './api';

const dashboardService = {
    // Get dashboard overview (stats cards)
    getOverview: async () => {
        const response = await api.get('/admin/dashboard/overview');
        return response.data;
    },

    // Get user growth data
    getUserGrowth: async () => {
        const response = await api.get('/admin/dashboard/user-growth');
        return response.data;
    },

    // Get transaction volume data
    getTransactionVolume: async () => {
        const response = await api.get('/admin/dashboard/transaction-volume');
        return response.data;
    },

    // Get category distribution
    getCategoryDistribution: async () => {
        const response = await api.get('/admin/dashboard/category-distribution');
        return response.data;
    },

    // Get bank distribution
    getBankDistribution: async () => {
        const response = await api.get('/admin/dashboard/bank-distribution');
        return response.data;
    },

    // Get system health status
    getSystemHealth: async () => {
        const response = await api.get('/admin/dashboard/system-health');
        return response.data;
    },
};

export default dashboardService;
