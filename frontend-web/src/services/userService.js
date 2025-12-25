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
};

export default userService;
