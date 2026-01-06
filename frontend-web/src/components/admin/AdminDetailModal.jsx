import React from 'react';
import { Modal, Tag } from 'antd';
import { CloseOutlined } from '@ant-design/icons';

const AdminDetailModal = ({ visible, onClose, admin }) => {
  if (!admin) return null;

  // Format date
  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleString('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  // Use data from API or fallback to old structure
  const displayName = admin.displayName || admin.name || 'N/A';
  const roleName = admin.roleName || admin.role || 'N/A';
  const permissionList = admin.permissionNames || admin.permissions || [];
  const activities = admin.recentActivities || [];
  const isActive = admin.isActive !== undefined ? admin.isActive : (admin.status === 'Hoạt động');

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
              {displayName}
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
                background: admin.roleColor || '#f3e8ff',
                color: '#9333ea',
                border: 'none',
                borderRadius: 6,
                fontSize: 12,
                fontWeight: 500,
                padding: '2px 10px',
              }}
            >
              {roleName}
            </Tag>
          </div>
          <div>
            <label style={{ display: 'block', fontSize: 12, color: '#6b7280', marginBottom: 4 }}>
              Trạng thái
            </label>
            <Tag
              style={{
                background: isActive ? '#dcfce7' : '#f3f4f6',
                color: isActive ? '#16a34a' : '#6b7280',
                border: 'none',
                borderRadius: 6,
                fontSize: 12,
                fontWeight: 500,
                padding: '2px 10px',
              }}
            >
              {isActive ? 'Hoạt động' : 'Không hoạt động'}
            </Tag>
          </div>
        </div>

        <div>
          <label style={{ display: 'block', fontSize: 12, color: '#6b7280', marginBottom: 4 }}>
            Hoạt động lần cuối
          </label>
          <div style={{ fontSize: 14, color: '#111827', fontWeight: 500 }}>
            {admin.lastActivityText || admin.lastActive || 'Chưa có hoạt động'}
          </div>
        </div>
      </div>

      {/* Permissions Section */}
      <div style={{ marginBottom: 24 }}>
        <label style={{ display: 'block', fontSize: 14, color: '#111827', fontWeight: 600, marginBottom: 8 }}>
          Quyền hạn
        </label>
        <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
          {permissionList.length > 0 ? (
            permissionList.map((permission, index) => (
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
            ))
          ) : (
            <span style={{ fontSize: 14, color: '#6b7280' }}>Không có quyền</span>
          )}
        </div>
      </div>

      {/* Activity Logs Section */}
      <div style={{ marginBottom: 24 }}>
        <label style={{ display: 'block', fontSize: 14, color: '#111827', fontWeight: 600, marginBottom: 12 }}>
          Lịch sử hoạt động gần đây
        </label>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
          {activities.length > 0 ? (
            activities.map((log, index) => (
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
                  {log.description}
                </div>
                <div style={{ fontSize: 12, color: '#6b7280' }}>
                  {log.timestampText} • IP: {log.ipAddress || 'N/A'}
                </div>
              </div>
            ))
          ) : (
            <div style={{ padding: '12px', textAlign: 'center', color: '#6b7280', fontSize: 14 }}>
              Chưa có hoạt động
            </div>
          )}
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
              {formatDate(admin.createdAt)}
            </div>
          </div>
          <div>
            <label style={{ display: 'block', fontSize: 12, color: '#6b7280', marginBottom: 4 }}>
              Người tạo
            </label>
            <div style={{ fontSize: 14, color: '#111827', fontWeight: 500 }}>
              {admin.createdBy || 'System'}
            </div>
          </div>
          <div>
            <label style={{ display: 'block', fontSize: 12, color: '#6b7280', marginBottom: 4 }}>
              Tổng số hành động
            </label>
            <div style={{ fontSize: 14, color: '#111827', fontWeight: 500 }}>
              {admin.totalActions !== undefined ? admin.totalActions.toLocaleString() : 'N/A'}
            </div>
          </div>
          <div>
            <label style={{ display: 'block', fontSize: 12, color: '#6b7280', marginBottom: 4 }}>
              Đăng nhập lần cuối
            </label>
            <div style={{ fontSize: 14, color: '#2563eb', fontWeight: 500 }}>
              {formatDate(admin.lastLoginAt)}
            </div>
          </div>
        </div>
      </div>
    </Modal>
  );
};

export default AdminDetailModal;
