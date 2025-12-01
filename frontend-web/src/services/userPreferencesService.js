import api from './api';

/**
 * Service để quản lý cài đặt người dùng (User Preferences)
 */

/**
 * Lấy tất cả cài đặt của user
 * @returns {Promise} - { success: true, data: UserPreferencesDTO }
 */
export const getUserPreferences = async () => {
  const response = await api.get('/user/preferences');
  return response.data;
};

/**
 * Cập nhật tất cả cài đặt
 * @param {Object} preferences - Các cài đặt cần cập nhật
 * @returns {Promise} - { success: true, message: string, data: UserPreferencesDTO }
 */
export const updateUserPreferences = async (preferences) => {
  const response = await api.put('/user/preferences', preferences);
  return response.data;
};

/**
 * Cập nhật cài đặt thông báo
 * @param {Object} notificationSettings - Các cài đặt thông báo
 * @param {Boolean} notificationSettings.pushNotifications - Thông báo đẩy
 * @param {Boolean} notificationSettings.emailNotifications - Thông báo email
 * @param {Boolean} notificationSettings.transactionAlert - Cảnh báo giao dịch
 * @param {Boolean} notificationSettings.budgetAlert - Cảnh báo ngân sách
 * @param {Boolean} notificationSettings.goalReminder - Nhắc nhở mục tiêu
 * @param {Boolean} notificationSettings.weeklyReport - Báo cáo tuần
 * @param {Boolean} notificationSettings.monthlyReport - Báo cáo tháng
 * @returns {Promise} - { success: true, message: string, data: UserPreferencesDTO }
 */
export const updateNotificationSettings = async (notificationSettings) => {
  const response = await api.put('/user/preferences/notifications', notificationSettings);
  return response.data;
};

export default {
  getUserPreferences,
  updateUserPreferences,
  updateNotificationSettings,
};
