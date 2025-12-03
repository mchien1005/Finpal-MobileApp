import api from './api';

const userService = {
    // Get current user profile
    getProfile: async () => {
        const response = await api.get('/user/profile');
        return response.data;
    },

    // Update current user profile
    updateProfile: async (profileData) => {
        const response = await api.put('/user/profile', profileData);
        return response.data;
    },

    // Upload avatar
    uploadAvatar: async (file) => {
        const formData = new FormData();
        formData.append('file', file);
        const response = await api.post('/user/avatar', formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        });
        return response.data;
    },

    // Get all users with pagination and filters
    getAllUsers: async (params = {}) => {
        const response = await api.get('/admin/users', { params });
        return response.data;
    },

    // Get user by ID
    getUserById: async (userId) => {
        const response = await api.get(`/admin/users/${userId}`);
        return response.data;
    },

    // Create new user
    createUser: async (userData) => {
        const response = await api.post('/admin/users', userData);
        return response.data;
    },

    // Update user
    updateUser: async (userId, userData) => {
        const response = await api.put(`/admin/users/${userId}`, userData);
        return response.data;
    },

    // Delete user
    deleteUser: async (userId) => {
        const response = await api.delete(`/admin/users/${userId}`);
        return response.data;
    },

    // Lock/Unlock user
    toggleUserStatus: async (userId, isLocked) => {
        const response = await api.patch(`/admin/users/${userId}/status`, {
            isLocked,
        });
        return response.data;
    },

    // Reset user password
    resetPassword: async (userId) => {
        const response = await api.post(`/admin/users/${userId}/reset-password`);
        return response.data;
    },

    // Get user statistics
    getUserStats: async () => {
        const response = await api.get('/admin/users/statistics');
        return response.data;
    },

    // Get user transaction summary
    getUserTransactions: async (userId, params = {}) => {
        const response = await api.get(`/admin/users/${userId}/transactions`, {
            params,
        });
        return response.data;
    },

    // Get user financial summary
    getUserFinancialSummary: async (userId) => {
        const response = await api.get(`/admin/users/${userId}/financial-summary`);
        return response.data;
    },

    // Bulk delete users
    bulkDeleteUsers: async (userIds) => {
        const response = await api.post('/admin/users/bulk-delete', { userIds });
        return response.data;
    },

    // Export users to CSV
    exportUsers: async (params = {}) => {
        const response = await api.get('/admin/users/export', {
            params,
            responseType: 'blob',
        });
        return response.data;
    },
};

export default userService;
