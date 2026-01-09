import api from './api';

const activityLogService = {
    // Get admin activity logs
    getActivityLogs: async (params = {}) => {
        const { limit = 50 } = params;
        const response = await api.get('/admin/roles/activity-logs', { 
            params: { limit } 
        });
        return response.data;
    },

    // Get login failure logs (if endpoint exists)
    getLoginFailures: async (params = {}) => {
        const response = await api.get('/admin/login-failures', { params });
        return response.data;
    },

    // Get all roles
    getAllRoles: async () => {
        const response = await api.get('/admin/roles');
        return response.data;
    },

    // Get all permissions
    getAllPermissions: async () => {
        const response = await api.get('/admin/roles/permissions');
        return response.data;
    },

    // Get permissions grouped by category
    getPermissionsGrouped: async () => {
        const response = await api.get('/admin/roles/permissions/grouped');
        return response.data;
    },

    // Get all admin users with roles
    getAllAdminUsers: async () => {
        const response = await api.get('/admin/roles/users');
        return response.data;
    },

    // Get admin user detail by ID
    getAdminUserDetail: async (adminUserId) => {
        const response = await api.get(`/admin/roles/users/${adminUserId}/detail`);
        return response.data;
    },

    // Create new admin: register user then assign role
    createAdminUser: async (userData) => {
        // Step 1: Register new user
        const registerResponse = await api.post('/auth/register', {
            username: userData.username,
            email: userData.email,
            password: userData.password,
            fullName: userData.fullName,
        });
        
        // Step 2: Get user ID from response and assign admin role
        if (registerResponse.data.success && registerResponse.data.user) {
            const userId = registerResponse.data.user.id;
            const roleCode = userData.roleCode || 'MODERATOR'; // Default to MODERATOR
            
            const assignResponse = await api.post(`/admin/roles/users/${userId}/assign`, null, {
                params: { roleCode }
            });
            
            return assignResponse.data;
        }
        
        throw new Error('Failed to create admin user');
    },

    // Assign role to user
    assignRoleToUser: async (userId, roleCode) => {
        const response = await api.post(`/admin/roles/users/${userId}/assign`, null, {
            params: { roleCode }
        });
        return response.data;
    },

    // Remove admin user
    removeAdminUser: async (adminUserId) => {
        const response = await api.delete(`/admin/roles/users/${adminUserId}`);
        return response.data;
    },

    // Create new role
    createRole: async (roleData) => {
        const response = await api.post('/admin/roles', roleData);
        return response.data;
    },

    // Get role by ID
    getRoleById: async (roleId) => {
        const response = await api.get(`/admin/roles/${roleId}`);
        return response.data;
    },

    // Update role
    updateRole: async (roleId, roleData) => {
        const response = await api.put(`/admin/roles/${roleId}`, roleData);
        return response.data;
    },

    // Delete role (soft delete)
    deleteRole: async (roleId) => {
        const response = await api.delete(`/admin/roles/${roleId}`);
        return response.data;
    },
};

export default activityLogService;
