import api from './api';
import axios from 'axios';

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

    // Admin APIs
    admin: {
        // Get all users
        getUsers: async (params) => {
            const response = await api.get('/admin/users', { params });
            return response.data;
        },

        // Get user by ID
        getUserById: async (userId) => {
            const response = await api.get(`/admin/users/${userId}`);
            return response.data;
        },

        // Delete user
        deleteUser: async (userId) => {
            const response = await api.delete(`/admin/users/${userId}`);
            return response.data;
        },

        // Reset user password
        resetPassword: async (userId) => {
            const response = await api.post(`/admin/users/${userId}/reset-password`);
            return response.data;
        },

        // Toggle user status (enable/disable)
        toggleStatus: async (userId) => {
            console.log('toggleStatus called with userId:', userId);
            const url = `http://175.41.150.228:8080/api/admin/users/${userId}/toggle-status`;
            console.log('Calling full URL:', url);
            const response = await axios.put(url, {}, {
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('token')}`,
                    'Content-Type': 'application/json'
                }
            });
            return response.data;
        },

        // Get user statistics
        getStatistics: async () => {
            const response = await api.get('/admin/users/statistics');
            return response.data;
        },
    },
};

export default userService;
