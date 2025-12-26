import api from './api';

export const getBackupHealth = async () => {
  const res = await api.get('/backup/health');
  return res.data;
};

export const getBackupHistory = async () => {
  const res = await api.get('/backup/history');
  return res.data;
};

export const createBackup = async () => {
  const res = await api.post('/backup/create');
  return res.data;
};

export const cleanupBackup = async () => {
  const res = await api.post('/backup/cleanup');
  return res.data;
};

export const restoreBackup = async (payload) => {
  const res = await api.post('/backup/restore', payload);
  return res.data;
};

export const deleteBackup = async (id) => {
  const res = await api.delete(`/backup/${id}`);
  return res.data;
};

export const downloadBackupUrl = (id) => `${api.defaults.baseURL}/backup/download/${id}`;

export default {
  getBackupHealth,
  getBackupHistory,
  createBackup,
  cleanupBackup,
  restoreBackup,
  deleteBackup,
  downloadBackupUrl,
};
