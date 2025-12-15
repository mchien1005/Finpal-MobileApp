import React, { useState } from 'react';
import { Button, Select, message } from 'antd';
import {
  CloudDownloadOutlined,
  DatabaseOutlined,
  DatabaseTwoTone,
} from '@ant-design/icons';

// Mock data for backup history
const mockBackupHistory = [
  {
    id: 1,
    date: '24/03/2024 02:00 AM',
    type: 'auto',
    size: '2.3 GB',
    status: 'success',
  },
  {
    id: 2,
    date: '23/03/2024 02:00 AM',
    type: 'auto',
    size: '2.2 GB',
    status: 'success',
  },
  {
    id: 3,
    date: '22/03/2024 02:00 AM',
    type: 'auto',
    size: '2.1 GB',
    status: 'success',
  },
  {
    id: 4,
    date: '21/03/2024 14:30 PM',
    type: 'manual',
    size: '2.1 GB',
    status: 'success',
  },
];

const frequencyOptions = [
  { value: 'daily', label: 'Hàng ngày lúc 2:00 sáng' },
  { value: 'weekly', label: 'Hàng tuần vào Chủ nhật' },
  { value: 'monthly', label: 'Hàng tháng vào ngày 1' },
];

const retentionOptions = [
  { value: 7, label: '7 ngày' },
  { value: 30, label: '30 ngày' },
  { value: 90, label: '90 ngày' },
  { value: 365, label: '1 năm' },
];

const BackupTab = () => {
  const [frequency, setFrequency] = useState('daily');
  const [retention, setRetention] = useState(30);
  const [backupHistory, setBackupHistory] = useState(mockBackupHistory);
  const [isBackingUp, setIsBackingUp] = useState(false);

  // Handle frequency change
  const handleFrequencyChange = (value) => {
    setFrequency(value);
    message.success('Đã cập nhật lịch sao lưu');
  };

  // Handle retention change
  const handleRetentionChange = (value) => {
    setRetention(value);
    message.success('Đã cập nhật thời gian lưu trữ');
  };

  // Handle schedule update
  const handleUpdateSchedule = () => {
    message.success('Đã cập nhật lịch backup');
  };

  // Handle database backup
  const handleBackupDatabase = async () => {
    setIsBackingUp(true);
    message.loading('Đang sao lưu cơ sở dữ liệu...');
    setTimeout(() => {
      const now = new Date();
      const newBackup = {
        id: Date.now(),
        date: now.toLocaleDateString('vi-VN') + ' ' + now.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit', hour12: true }),
        type: 'manual',
        size: '2.4 GB',
        status: 'success',
      };
      setBackupHistory([newBackup, ...backupHistory]);
      setIsBackingUp(false);
      message.success('Sao lưu cơ sở dữ liệu thành công!');
    }, 2000);
  };

  // Handle restore from backup
  const handleRestoreFromBackup = () => {
    message.info('Chọn file backup để khôi phục...');
  };

  // Handle export data
  const handleExportData = () => {
    message.success('Đang xuất toàn bộ dữ liệu (CSV/JSON)...');
  };

  // Handle download backup
  const handleDownload = (backup) => {
    message.info(`Đang tải xuống backup ${backup.date}...`);
  };

  // Handle restore backup
  const handleRestore = (backup) => {
    message.loading(`Đang khôi phục từ backup ${backup.date}...`);
    setTimeout(() => {
      message.success('Khôi phục thành công!');
    }, 2000);
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
        </div>

        {/* Backup Items */}
        <div style={{ display: 'flex', flexDirection: 'column' }}>
          {backupHistory.length === 0 ? (
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
                      background: '#dbeafe',
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
                      {item.date}
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
                      Sao lưu {item.type === 'auto' ? 'Tự động' : 'Thủ công'} • {item.size}
                    </p>
                  </div>
                </div>

                {/* Right Section: Status and Actions */}
                <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
                  {/* Status Badge */}
                  <div
                    style={{
                      background: '#dcfce7',
                      border: '1px solid rgba(0, 0, 0, 0)',
                      borderRadius: 8,
                      padding: '4px 10px',
                      fontSize: 12,
                      color: '#008236',
                      fontFamily: 'Arimo, sans-serif',
                      fontWeight: 400,
                    }}
                  >
                    Hoàn thành
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
                  >
                    <img
                src="/images/admin/download.svg"
                alt="Database Backup"
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
                  >
                    <img
                src="/images/admin/restore.svg"
                alt="Database Backup"
                style={{ width: 16, height: 16, marginRight: 8 }}
              />
                    Khôi phục
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
