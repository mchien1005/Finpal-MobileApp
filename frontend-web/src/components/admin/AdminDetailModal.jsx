import React from 'react';
import { Modal, Tag } from 'antd';
import { CloseOutlined } from '@ant-design/icons';

const AdminDetailModal = ({ visible, onClose, admin }) => {
  if (!admin) return null;

  const activityLogs = [
    {
      action: 'Đã chỉnh sửa category "Ăn uống"',
      timestamp: '24/03/2024 10:30',
      ip: '192.168.1.1',
    },
    {
      action: 'Đã thêm template SMS cho Vietcombank',
      timestamp: '23/03/2024 15:20',
      ip: '192.168.1.1',
    },
    {
      action: 'Đã xem danh sách người dùng',
      timestamp: '23/03/2024 09:15',
      ip: '192.168.1.1',
    },
  ];

  return (
    <Modal
      open={visible}
      onCancel={onClose}
      footer={null}
      closeIcon={<CloseOutlined style={{ fontSize: 18, color: '#6b7280' }} />}
      width={520}
      style={{ top: 40 }}
      styles={{
        body: { padding: '24px' },
        content: { borderRadius: 16 },
      }}
    >
      <div style={{ marginBottom: 24 }}>
        <h2 style={{ fontSize: 20, fontWeight: 600, color: '#111827', margin: 0, marginBottom: 4 }}>
          Chi tiết Admin
        </h2>
        <p style={{ fontSize: 14, color: '#6b7280', margin: 0 }}>
          Thông tin chi tiết và phân quyền của Admin User
        </p>
      </div>

      {/* Basic Info Section */}
      <div style={{ marginBottom: 24 }}>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16, marginBottom: 16 }}>
          <div>
            <label style={{ display: 'block', fontSize: 12, color: '#6b7280', marginBottom: 4 }}>
              Email
            </label>
            <div style={{ fontSize: 14, color: '#111827', fontWeight: 500 }}>
              {admin.email}
            </div>
          </div>
          <div>
            <label style={{ display: 'block', fontSize: 12, color: '#6b7280', marginBottom: 4 }}>
              Tên
            </label>
            <div style={{ fontSize: 14, color: '#111827', fontWeight: 500 }}>
              {admin.name}
            </div>
          </div>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16, marginBottom: 16 }}>
          <div>
            <label style={{ display: 'block', fontSize: 12, color: '#6b7280', marginBottom: 4 }}>
              Vai trò
            </label>
            <Tag
              style={{
                background: '#f3e8ff',
                color: '#9333ea',
                border: 'none',
                borderRadius: 6,
                fontSize: 12,
                fontWeight: 500,
                padding: '2px 10px',
              }}
            >
              {admin.role}
            </Tag>
          </div>
          <div>
            <label style={{ display: 'block', fontSize: 12, color: '#6b7280', marginBottom: 4 }}>
              Trạng thái
            </label>
            <Tag
              style={{
                background: admin.status === 'Hoạt động' ? '#dcfce7' : '#f3f4f6',
                color: admin.status === 'Hoạt động' ? '#16a34a' : '#6b7280',
                border: 'none',
                borderRadius: 6,
                fontSize: 12,
                fontWeight: 500,
                padding: '2px 10px',
              }}
            >
              {admin.status}
            </Tag>
          </div>
        </div>

        <div>
          <label style={{ display: 'block', fontSize: 12, color: '#6b7280', marginBottom: 4 }}>
            Hoạt động lần cuối
          </label>
          <div style={{ fontSize: 14, color: '#111827', fontWeight: 500 }}>
            {admin.lastActive}
          </div>
        </div>
      </div>

      {/* Permissions Section */}
      <div style={{ marginBottom: 24 }}>
        <label style={{ display: 'block', fontSize: 14, color: '#111827', fontWeight: 600, marginBottom: 8 }}>
          Quyền hạn
        </label>
        <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
          {admin.permissions?.map((permission, index) => (
            <Tag
              key={index}
              style={{
                background: '#dcfce7',
                color: '#16a34a',
                border: 'none',
                borderRadius: 6,
                fontSize: 12,
                fontWeight: 500,
                padding: '4px 12px',
              }}
            >
              {permission}
            </Tag>
          ))}
        </div>
      </div>

      {/* Activity Logs Section */}
      <div style={{ marginBottom: 24 }}>
        <label style={{ display: 'block', fontSize: 14, color: '#111827', fontWeight: 600, marginBottom: 12 }}>
          Lịch sử hoạt động gần đây
        </label>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
          {activityLogs.map((log, index) => (
            <div
              key={index}
              style={{
                padding: '12px',
                background: '#f9fafb',
                borderRadius: 8,
                border: '1px solid #e5e7eb',
              }}
            >
              <div style={{ fontSize: 14, color: '#111827', fontWeight: 500, marginBottom: 4 }}>
                {log.action}
              </div>
              <div style={{ fontSize: 12, color: '#6b7280' }}>
                {log.timestamp} • IP: {log.ip}
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Account Info Section */}
      <div>
        <label style={{ display: 'block', fontSize: 14, color: '#111827', fontWeight: 600, marginBottom: 12 }}>
          Thông tin tài khoản
        </label>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
          <div>
            <label style={{ display: 'block', fontSize: 12, color: '#6b7280', marginBottom: 4 }}>
              Ngày tạo tài khoản
            </label>
            <div style={{ fontSize: 14, color: '#111827', fontWeight: 500 }}>
              15/01/2024
            </div>
          </div>
          <div>
            <label style={{ display: 'block', fontSize: 12, color: '#6b7280', marginBottom: 4 }}>
              Người tạo
            </label>
            <div style={{ fontSize: 14, color: '#111827', fontWeight: 500 }}>
              System Admin
            </div>
          </div>
          <div>
            <label style={{ display: 'block', fontSize: 12, color: '#6b7280', marginBottom: 4 }}>
              Tổng số hành động
            </label>
            <div style={{ fontSize: 14, color: '#111827', fontWeight: 500 }}>
              1,234
            </div>
          </div>
          <div>
            <label style={{ display: 'block', fontSize: 12, color: '#6b7280', marginBottom: 4 }}>
              Đăng nhập lần cuối
            </label>
            <div style={{ fontSize: 14, color: '#2563eb', fontWeight: 500 }}>
              24/03/2024 10:00
            </div>
          </div>
        </div>
      </div>
    </Modal>
  );
};

export default AdminDetailModal;
