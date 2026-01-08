import api from './api';

// Admin System Settings API
export const getSettingsMap = async () => {
  const res = await api.get('/admin/settings/map');
  return res.data;
};

export const getSettings = async () => {
  const res = await api.get('/admin/settings');
  return res.data;
};

export const updateSetting = async (key, value) => {
  const body = { key, value, settingKey: key, settingValue: value };
  const res = await api.put('/admin/settings', body);
  return res.data;
};

export const updateSettingsBatch = async (updates) => {
  // backend accepts either key/value or settingKey/settingValue; send both
  const payload = updates.map((u) => {
    const key = u.key ?? u.settingKey;
    const value = u.value ?? u.settingValue;
    const v = value === undefined || value === null ? '' : String(value);
    return {
      key,
      value: v,
      settingKey: u.settingKey ?? key,
      settingValue: u.settingValue !== undefined && u.settingValue !== null ? String(u.settingValue) : v,
    };
  });
  console.debug('updateSettingsBatch payload', payload);
  const res = await api.put('/admin/settings/batch', payload);
  return res.data;
};

export const updateAiSettings = async (body) => {
  const res = await api.put('/admin/settings/ai', body);
  return res.data;
};

export const updateNotificationSettings = async (body) => {
  const res = await api.put('/admin/settings/notification', body);
  return res.data;
};

export const getSettingsByGroup = async (group) => {
  const res = await api.get(`/admin/settings/group/${group}`);
  return res.data;
};

export default {
  getSettingsMap,
  getSettings,
  updateSetting,
  updateSettingsBatch,
  updateAiSettings,
  updateNotificationSettings,
  getSettingsByGroup,
};
