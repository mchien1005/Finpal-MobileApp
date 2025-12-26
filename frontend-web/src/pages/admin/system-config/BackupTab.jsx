import React, { useState, useEffect } from 'react';
import { Button, Select, message, Modal, Upload } from 'antd';
import {
  CloudDownloadOutlined,
  DatabaseOutlined,
  DatabaseTwoTone,
  UploadOutlined,
  ExclamationCircleOutlined,
} from '@ant-design/icons';
import {
  getBackupHistory,
  createBackup,
  restoreBackup,
  deleteBackup,
  downloadBackupUrl,
  getBackupStatistics,
  cleanupBackup,
} from '../../../services/backupService';

// Tùy chọn tần suất backup
const frequencyOptions = [
  { value: 'daily', label: 'Hàng ngày lúc 2:00 sáng' },
  { value: 'weekly', label: 'Hàng tuần vào Chủ nhật' },
  { value: 'monthly', label: 'Hàng tháng vào ngày 1' },
];

// Tùy chọn thời gian lưu trữ
const retentionOptions = [
  { value: 7, label: '7 ngày' },
  { value: 30, label: '30 ngày' },
  { value: 90, label: '90 ngày' },
  { value: 365, label: '1 năm' },
];

// Hàm format file size
const formatFileSize = (bytes) => {
  if (!bytes || bytes === 0) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};

// Hàm format ngày giờ
const formatDateTime = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleDateString('vi-VN') + ' ' + date.toLocaleTimeString('vi-VN', { 
    hour: '2-digit', 
    minute: '2-digit', 
    hour12: true 
  });
};

const BackupTab = () => {
  const [frequency, setFrequency] = useState('daily');
  const [retention, setRetention] = useState(30);
  const [backupHistory, setBackupHistory] = useState([]);
  const [isBackingUp, setIsBackingUp] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  // eslint-disable-next-line no-unused-vars
  const [isRestoring, setIsRestoring] = useState(false);
  // eslint-disable-next-line no-unused-vars
  const [statistics, setStatistics] = useState(null);

  // Load dữ liệu khi component mount
  useEffect(() => {
    loadBackupData();
  }, []);

  // Hàm load dữ liệu backup
  const loadBackupData = async () => {
    setIsLoading(true);
    try {
      // Load lịch sử backup
      const historyData = await getBackupHistory();
      // Đảm bảo historyData luôn là mảng (API có thể trả về object chứa mảng)
      let backups = [];
      if (Array.isArray(historyData)) {
        backups = historyData;
      } else if (historyData && Array.isArray(historyData.data)) {
        backups = historyData.data;
      } else if (historyData && Array.isArray(historyData.backups)) {
        backups = historyData.backups;
      } else if (historyData && Array.isArray(historyData.content)) {
        backups = historyData.content;
      }
      setBackupHistory(backups);

      // Load thống kê backup
      try {
        const statsData = await getBackupStatistics();
        setStatistics(statsData);
      } catch (err) {
        console.log('Không thể tải thống kê backup:', err);
      }
    } catch (error) {
      console.error('Lỗi khi tải dữ liệu backup:', error);
      message.error('Không thể tải lịch sử backup');
    } finally {
      setIsLoading(false);
    }
  };

  // Xử lý thay đổi tần suất
  const handleFrequencyChange = (value) => {
    setFrequency(value);
    message.success('Đã cập nhật lịch sao lưu');
  };

  // Xử lý thay đổi thời gian lưu trữ
  const handleRetentionChange = (value) => {
    setRetention(value);
    message.success('Đã cập nhật thời gian lưu trữ');
  };

  // Xử lý cập nhật lịch
  const handleUpdateSchedule = () => {
    message.success('Đã cập nhật lịch backup');
  };

  // Xử lý tạo backup database
  const handleBackupDatabase = async () => {
    setIsBackingUp(true);
    const hideLoading = message.loading('Đang sao lưu cơ sở dữ liệu...', 0);
    
    try {
      await createBackup();
      hideLoading();
      message.success('Sao lưu cơ sở dữ liệu thành công!');
      // Reload lại danh sách backup
      await loadBackupData();
    } catch (error) {
      hideLoading();
      console.error('Lỗi khi tạo backup:', error);
      message.error(error.response?.data?.message || 'Không thể tạo backup. Vui lòng thử lại.');
    } finally {
      setIsBackingUp(false);
    }
  };

  // Xử lý khôi phục từ backup (upload file)
  const handleRestoreFromBackup = () => {
    // Mở modal để chọn file backup
    Modal.confirm({
      title: 'Khôi phục từ file Backup',
      icon: <ExclamationCircleOutlined />,
      content: (
        <div>
          <p>Bạn có chắc chắn muốn khôi phục database từ file backup?</p>
          <p style={{ color: '#ff4d4f' }}>
            <strong>Cảnh báo:</strong> Việc này sẽ ghi đè toàn bộ dữ liệu hiện tại!
          </p>
        </div>
      ),
      okText: 'Tiếp tục',
      cancelText: 'Hủy',
      onOk: () => {
        message.info('Chức năng upload file backup đang được phát triển');
      },
    });
  };

  // Xử lý xuất dữ liệu
  const handleExportData = async () => {
    message.info('Đang xuất toàn bộ dữ liệu...');
    // TODO: Implement export data functionality
  };

  // Xử lý tải xuống backup
  const handleDownload = (backup) => {
    const downloadUrl = downloadBackupUrl(backup.id);
    const token = localStorage.getItem('token');
    
    // Hiển thị thông báo đang tải
    const hideLoading = message.loading('Đang tải xuống backup...', 0);
    
    // Sử dụng fetch để tải với authentication
    fetch(downloadUrl, {
      headers: token ? { 'Authorization': `Bearer ${token}` } : {}
    })
    .then(response => {
      if (!response.ok) throw new Error('Không thể tải file');
      
      // Lấy tên file từ Content-Disposition header hoặc từ backup.fileName
      let fileName = backup.fileName || `backup_${backup.id}.sql`;
      const contentDisposition = response.headers.get('Content-Disposition');
      if (contentDisposition) {
        const matches = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/);
        if (matches && matches[1]) {
          fileName = matches[1].replace(/['"]/g, '');
        }
      }
      
      return response.blob().then(blob => ({ blob, fileName }));
    })
    .then(({ blob, fileName }) => {
      hideLoading();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = fileName;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
      message.success('Đã tải xuống backup thành công!');
    })
    .catch(error => {
      hideLoading();
      console.error('Lỗi tải backup:', error);
      message.error('Không thể tải xuống backup');
    });
  };

  // Xử lý khôi phục backup
  const handleRestore = (backup) => {
    Modal.confirm({
      title: 'Xác nhận Khôi phục',
      icon: <ExclamationCircleOutlined />,
      content: (
        <div>
          <p>Bạn có chắc chắn muốn khôi phục từ backup ngày <strong>{formatDateTime(backup.createdAt) || backup.date}</strong>?</p>
          <p style={{ color: '#ff4d4f' }}>
            <strong>Cảnh báo:</strong> Việc này sẽ ghi đè toàn bộ dữ liệu hiện tại!
          </p>
        </div>
      ),
      okText: 'Khôi phục',
      okButtonProps: { danger: true },
      cancelText: 'Hủy',
      onOk: async () => {
        setIsRestoring(true);
        const hideLoading = message.loading('Đang khôi phục database...', 0);
        
        try {
          await restoreBackup({ backupId: backup.id });
          hideLoading();
          message.success('Khôi phục database thành công!');
          await loadBackupData();
        } catch (error) {
          hideLoading();
          console.error('Lỗi khôi phục:', error);
          message.error(error.response?.data?.message || 'Không thể khôi phục. Vui lòng thử lại.');
        } finally {
          setIsRestoring(false);
        }
      },
    });
  };

  // Xử lý xóa backup
  const handleDelete = (backup) => {
    Modal.confirm({
      title: 'Xác nhận Xóa',
      icon: <ExclamationCircleOutlined />,
      content: `Bạn có chắc chắn muốn xóa backup ngày ${formatDateTime(backup.createdAt) || backup.date}?`,
      okText: 'Xóa',
      okButtonProps: { danger: true },
      cancelText: 'Hủy',
      onOk: async () => {
        try {
          await deleteBackup(backup.id);
          message.success('Đã xóa backup thành công!');
          await loadBackupData();
        } catch (error) {
          console.error('Lỗi xóa backup:', error);
          message.error(error.response?.data?.message || 'Không thể xóa backup');
        }
      },
    });
  };

  // Xử lý dọn dẹp backup cũ
  const handleCleanup = async () => {
    Modal.confirm({
      title: 'Dọn dẹp Backup cũ',
      icon: <ExclamationCircleOutlined />,
      content: 'Bạn có chắc chắn muốn dọn dẹp các backup cũ? Các backup quá thời gian lưu trữ sẽ bị xóa.',
      okText: 'Dọn dẹp',
      cancelText: 'Hủy',
      onOk: async () => {
        try {
          await cleanupBackup();
          message.success('Đã dọn dẹp backup cũ thành công!');
          await loadBackupData();
        } catch (error) {
          console.error('Lỗi dọn dẹp:', error);
          message.error(error.response?.data?.message || 'Không thể dọn dẹp backup');
        }
      },
    });
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
      {/* Top Row: Two Configuration Cards */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 24 }}>
        {/* Card 1: Backup Schedule */}
        <div
          style={{
            background: '#FFFFFF',
            border: '1px solid rgba(0, 0, 0, 0.1)',
            borderRadius: 14,
            padding: 24,
            display: 'flex',
            flexDirection: 'column',
            gap: 40,
          }}
        >
          <div>
            <h3
              style={{
                fontSize: 16,
                fontWeight: 400,
                color: '#101828',
                margin: 0,
                fontFamily: 'Arimo, sans-serif',
              }}
            >
              Lịch Sao lưu
            </h3>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
            {/* Frequency Selection */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              <label
                style={{
                  fontSize: 14,
                  fontWeight: 400,
                  color: '#0a0a0a',
                  fontFamily: 'Arimo, sans-serif',
                }}
              >
                Tần suất
              </label>
              <Select
                value={frequency}
                onChange={handleFrequencyChange}
                options={frequencyOptions}
                style={{ width: '100%' }}
                size="large"
              />
            </div>

            {/* Retention Selection */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              <label
                style={{
                  fontSize: 14,
                  fontWeight: 400,
                  color: '#0a0a0a',
                  fontFamily: 'Arimo, sans-serif',
                }}
              >
                Thời gian Lưu trữ
              </label>
              <Select
                value={retention}
                onChange={handleRetentionChange}
                options={retentionOptions}
                style={{ width: '100%' }}
                size="large"
              />
            </div>

            {/* Update Button */}
            <Button
              type="primary"
              onClick={handleUpdateSchedule}
              style={{
                background: '#155DFC',
                borderColor: '#155DFC',
                borderRadius: 8,
                height: 36,
                fontFamily: 'Arimo, sans-serif',
                fontSize: 14,
                fontWeight: 400,
                width: '100%',
              }}
            >
              Cập nhật Lịch
            </Button>
          </div>
        </div>

        {/* Card 2: Manual Backup */}
        <div
          style={{
            background: '#FFFFFF',
            border: '1px solid rgba(0, 0, 0, 0.1)',
            borderRadius: 14,
            padding: 24,
            display: 'flex',
            flexDirection: 'column',
            gap: 40,
          }}
        >
          <div>
            <h3
              style={{
                fontSize: 16,
                fontWeight: 400,
                color: '#101828',
                margin: 0,
                marginBottom: 8,
                fontFamily: 'Arimo, sans-serif',
              }}
            >
              Sao lưu Thủ công
            </h3>
            <p
              style={{
                fontSize: 14,
                fontWeight: 400,
                color: '#4a5565',
                margin: 0,
                fontFamily: 'Arimo, sans-serif',
              }}
            >
              Tạo backup thủ công cho database và file hệ thống
            </p>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
            {/* Database Backup Button */}
            <Button
              type="primary"
              onClick={handleBackupDatabase}
              loading={isBackingUp}
              style={{
                background: '#00a63e',
                borderColor: '#00a63e',
                borderRadius: 8,
                height: 36,
                fontFamily: 'Arimo, sans-serif',
                fontSize: 14,
                fontWeight: 400,
                width: '100%',
              }}
            >
              <img
                src="/images/admin/db.svg"
                alt="Database Backup"
                style={{ width: 16, height: 16, marginRight: 8 }}
              />
              Sao lưu Cơ sở dữ liệu
            </Button>

            {/* Restore from Backup Button */}
            <Button
              onClick={handleRestoreFromBackup}
              style={{
                borderRadius: 8,
                height: 36,
                fontFamily: 'Arimo, sans-serif',
                fontSize: 14,
                fontWeight: 400,
                width: '100%',
                borderColor: 'rgba(0, 0, 0, 0.1)',
                color: '#0a0a0a',
              }}
            >
              <img
                src="/images/admin/upload.svg"
                alt="Database Upload"
                style={{ width: 16, height: 16, marginRight: 8 }}
              />
              Khôi phục từ Backup
            </Button>

            {/* Export Data Button */}
            <Button
              onClick={handleExportData}
              style={{
                borderRadius: 8,
                height: 36,
                fontFamily: 'Arimo, sans-serif',
                fontSize: 14,
                fontWeight: 400,
                width: '100%',
                borderColor: 'rgba(0, 0, 0, 0.1)',
                color: '#0a0a0a',
              }}
            >
              <img
                src="/images/admin/download.svg"
                alt="Database Download"
                style={{ width: 16, height: 16, marginRight: 8 }}
              />
              Xuất Toàn bộ Dữ liệu (CSV/JSON)
            </Button>
          </div>
        </div>
      </div>

      {/* Backup History Table */}
      <div
        style={{
          background: '#FFFFFF',
          border: '1px solid rgba(0, 0, 0, 0.1)',
          borderRadius: 14,
          overflow: 'hidden',
        }}
      >
        {/* Header */}
        <div
          style={{
            borderBottom: '1px solid rgba(0, 0, 0, 0.1)',
            padding: 24,
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
          }}
        >
          <h3
            style={{
              fontSize: 18,
              fontWeight: 400,
              color: '#101828',
              margin: 0,
              fontFamily: 'Arimo, sans-serif',
            }}
          >
            Lịch sử Sao lưu
          </h3>
          {/* Nút dọn dẹp backup cũ */}
          <Button
            onClick={handleCleanup}
            style={{
              borderRadius: 8,
              height: 32,
              fontFamily: 'Arimo, sans-serif',
              fontSize: 14,
              fontWeight: 400,
              borderColor: 'rgba(0, 0, 0, 0.1)',
              color: '#0a0a0a',
            }}
          >
            Dọn dẹp Backup cũ
          </Button>
        </div>

        {/* Backup Items */}
        <div style={{ display: 'flex', flexDirection: 'column' }}>
          {isLoading ? (
            <div
              style={{
                padding: 40,
                textAlign: 'center',
                color: '#6a7282',
                fontFamily: 'Arimo, sans-serif',
                fontSize: 14,
              }}
            >
              Đang tải dữ liệu...
            </div>
          ) : backupHistory.length === 0 ? (
            <div
              style={{
                padding: 40,
                textAlign: 'center',
                color: '#6a7282',
                fontFamily: 'Arimo, sans-serif',
                fontSize: 14,
              }}
            >
              Chưa có lịch sử sao lưu
            </div>
          ) : (
            backupHistory.map((item, index) => (
              <div
                key={item.id}
                style={{
                  borderBottom: index < backupHistory.length - 1 ? '1px solid rgba(0, 0, 0, 0.1)' : 'none',
                  padding: '24px',
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  gap: 24,
                }}
              >
                {/* Left Section: Icon and Info */}
                <div style={{ display: 'flex', gap: 16, alignItems: 'center', flex: 1 }}>
                  {/* Icon Container */}
                  <div
                    style={{
                      background: item.status === 'FAILED' ? '#fee2e2' : '#dbeafe',
                      borderRadius: 10,
                      width: 48,
                      height: 48,
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      flexShrink: 0,
                    }}
                  >
                    <img
                      src="/images/admin/database_big.svg"
                      alt="Database"
                      style={{ width: 24, height: 24}}
                    />
                  </div>

                  {/* Text Info */}
                  <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                    <p
                      style={{
                        fontSize: 16,
                        fontWeight: 400,
                        color: '#101828',
                        margin: 0,
                        fontFamily: 'Arimo, sans-serif',
                      }}
                    >
                      {formatDateTime(item.createdAt) || item.date || 'N/A'}
                    </p>
                    <p
                      style={{
                        fontSize: 14,
                        fontWeight: 400,
                        color: '#6a7282',
                        margin: 0,
                        fontFamily: 'Arimo, sans-serif',
                      }}
                    >
                      Sao lưu {item.type === 'AUTO' || item.type === 'auto' ? 'Tự động' : 'Thủ công'} • {item.fileSize ? formatFileSize(item.fileSize) : item.size || 'N/A'}
                    </p>
                  </div>
                </div>

                {/* Right Section: Status and Actions */}
                <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
                  {/* Status Badge */}
                  <div
                    style={{
                      background: item.status === 'FAILED' || item.status === 'failed' ? '#fee2e2' : '#dcfce7',
                      border: '1px solid rgba(0, 0, 0, 0)',
                      borderRadius: 8,
                      padding: '4px 10px',
                      fontSize: 12,
                      color: item.status === 'FAILED' || item.status === 'failed' ? '#dc2626' : '#008236',
                      fontFamily: 'Arimo, sans-serif',
                      fontWeight: 400,
                    }}
                  >
                    {item.status === 'FAILED' || item.status === 'failed' ? 'Thất bại' : 'Hoàn thành'}
                  </div>

                  {/* Action Buttons */}
                  <Button
                    onClick={() => handleDownload(item)}
                    style={{
                      width: 115,
                      height: 32,
                      borderRadius: 8,
                      fontFamily: 'Arimo, sans-serif',
                      fontSize: 14,
                      fontWeight: 400,
                      borderColor: 'rgba(0, 0, 0, 0.1)',
                      color: '#0a0a0a',
                      background: '#FFFFFF',
                    }}
                    disabled={item.status === 'FAILED' || item.status === 'failed'}
                  >
                    <img
                      src="/images/admin/download.svg"
                      alt="Download Backup"
                      style={{ width: 16, height: 16, marginRight: 8 }}
                    />
                    Tải xuống
                  </Button>

                  <Button
                    onClick={() => handleRestore(item)}
                    style={{
                      width: 116,
                      height: 32,
                      borderRadius: 8,
                      fontFamily: 'Arimo, sans-serif',
                      fontSize: 14,
                      fontWeight: 400,
                      borderColor: 'rgba(0, 0, 0, 0.1)',
                      color: '#0a0a0a',
                      background: '#FFFFFF',
                    }}
                    disabled={item.status === 'FAILED' || item.status === 'failed'}
                  >
                    <img
                      src="/images/admin/restore.svg"
                      alt="Restore Backup"
                      style={{ width: 16, height: 16, marginRight: 8 }}
                    />
                    Khôi phục
                  </Button>

                  {/* Nút xóa backup */}
                  <Button
                    onClick={() => handleDelete(item)}
                    danger
                    style={{
                      width: 80,
                      height: 32,
                      borderRadius: 8,
                      fontFamily: 'Arimo, sans-serif',
                      fontSize: 14,
                      fontWeight: 400,
                    }}
                  >
                    Xóa
                  </Button>
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
};

export default BackupTab;
