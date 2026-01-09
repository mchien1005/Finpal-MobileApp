import React from 'react';
import { Modal, Tag, Descriptions } from 'antd';
import { CloseOutlined } from '@ant-design/icons';

const RoleDetailModal = ({ visible, onClose, role }) => {
  if (!role) return null;

  return (
    <Modal
      open={visible}
      onCancel={onClose}
      footer={null}
      closeIcon={<CloseOutlined style={{ fontSize: 18, color: '#6b7280' }} />}
      width={700}
      style={{ top: 40 }}
      styles={{
        body: { padding: '32px' },
        content: { borderRadius: 16 },
      }}
    >
      <div style={{ marginBottom: 32 }}>
        <h2 style={{ fontSize: 24, fontWeight: 600, color: '#111827', margin: 0, marginBottom: 6 }}>
          Chi tiết Vai trò
        </h2>
        <p style={{ fontSize: 15, color: '#6b7280', margin: 0 }}>
          Thông tin chi tiết của vai trò
        </p>
      </div>

      <Descriptions 
        column={1} 
        bordered
        labelStyle={{
          fontSize: 15,
          fontWeight: 500,
          color: '#374151',
          background: '#f9fafb',
          width: '180px',
        }}
        contentStyle={{
          fontSize: 15,
          color: '#111827',
          background: '#fff',
        }}
      >
        <Descriptions.Item label="Mã vai trò">
          <Tag
            style={{
              background: '#dbeafe',
              color: '#2563eb',
              border: 'none',
              borderRadius: 6,
              fontSize: 13,
              fontWeight: 500,
              padding: '4px 12px',
            }}
          >
            {role.roleCode}
          </Tag>
        </Descriptions.Item>

        <Descriptions.Item label="Tên vai trò">
          {role.roleName}
        </Descriptions.Item>

        <Descriptions.Item label="Mô tả">
          {role.description || 'Không có mô tả'}
        </Descriptions.Item>

        <Descriptions.Item label="Màu sắc">
          <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
            <div
              style={{
                width: 40,
                height: 28,
                borderRadius: 6,
                background: role.color,
                border: '1px solid #e5e7eb',
              }}
            />
            <span style={{ fontSize: 14, color: '#6b7280', fontFamily: 'monospace' }}>
              {role.color}
            </span>
          </div>
        </Descriptions.Item>

        <Descriptions.Item label="Thứ tự hiển thị">
          {role.displayOrder}
        </Descriptions.Item>

        <Descriptions.Item label="Trạng thái">
          <Tag
            style={{
              background: role.isActive ? '#dcfce7' : '#f3f4f6',
              color: role.isActive ? '#16a34a' : '#6b7280',
              border: 'none',
              borderRadius: 6,
              fontSize: 13,
              fontWeight: 500,
              padding: '4px 12px',
            }}
          >
            {role.isActive ? 'Hoạt động' : 'Không hoạt động'}
          </Tag>
        </Descriptions.Item>

        <Descriptions.Item label="Số lượng Admin">
          {role.adminCount || 0}
        </Descriptions.Item>

        <Descriptions.Item label="Quyền">
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
            {(role.permissionNames || role.permissionCodes || []).map((permission, index) => (
              <Tag
                key={index}
                style={{
                  background: '#dcfce7',
                  color: '#16a34a',
                  border: 'none',
                  borderRadius: 6,
                  fontSize: 13,
                  fontWeight: 500,
                  padding: '4px 12px',
                }}
              >
                {permission}
              </Tag>
            ))}
          </div>
        </Descriptions.Item>
      </Descriptions>
    </Modal>
  );
};

export default RoleDetailModal;
