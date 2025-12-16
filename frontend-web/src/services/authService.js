import api from './api';

const authService = {
    // Login
    login: async (username, password) => {
        const response = await api.post('/auth/login', { username, password });
        const { token, user } = response.data;

        // Store token and user info
        localStorage.setItem('token', token);
        localStorage.setItem('user', JSON.stringify(user));

        return response.data;
    },

    // Register
    register: async (userData) => {
        const response = await api.post('/auth/register', userData);
        return response.data;
    },

    // Logout
    logout: () => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
    },

    // Get current user
    getCurrentUser: () => {
        const userStr = localStorage.getItem('user');
        return userStr ? JSON.parse(userStr) : null;
    },

    // Check if user is authenticated
    isAuthenticated: () => {
        return !!localStorage.getItem('token');
    },

    // Check if user is admin
    isAdmin: () => {
        const user = authService.getCurrentUser();
        return user?.role === 'ADMIN';
    },

    // Change password
    changePassword: async ({ currentPassword, newPassword }) => {
        const response = await api.put('/auth/change-password', {
            currentPassword,
            newPassword,
        });
        return response.data;
    },
};

export default authService;
