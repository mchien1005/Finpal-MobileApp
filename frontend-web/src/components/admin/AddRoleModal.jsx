import React, { useState, useEffect } from 'react';
import { Modal, Input, Button, Checkbox, message } from 'antd';
import { CloseOutlined } from '@ant-design/icons';

const AddRoleModal = ({ visible, onClose, onAdd, permissions = [], loading = false }) => {
  const [formData, setFormData] = useState({
    roleCode: '',
    roleName: '',
    description: '',
    color: '#2563eb',
    displayOrder: 0,
    permissionCodes: [],
  });

  const handleChange = (field, value) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
  };

  const handlePermissionChange = (checkedValues) => {
    setFormData((prev) => ({ ...prev, permissionCodes: checkedValues }));
  };

  const handleSubmit = () => {
    if (!formData.roleCode.trim()) {
      message.error('Vui lòng nhập mã vai trò');
      return;
    }
    if (!formData.roleName.trim()) {
      message.error('Vui lòng nhập tên vai trò');
      return;
    }
    if (formData.permissionCodes.length === 0) {
      message.error('Vui lòng chọn ít nhất một quyền');
      return;
    }

    onAdd(formData);
  };

  const handleCancel = () => {
    setFormData({
      roleCode: '',
      roleName: '',
      description: '',
      color: '#2563eb',
      displayOrder: 0,
      permissionCodes: [],
    });
    onClose();
  };

  return (
    <Modal
      open={visible}
      onCancel={handleCancel}
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
          Thêm Vai trò
        </h2>
        <p style={{ fontSize: 15, color: '#6b7280', margin: 0 }}>
          Tạo vai trò mới và phân quyền
        </p>
      </div>

      <div style={{ marginBottom: 24 }}>
        <label style={{ display: 'block', fontSize: 15, color: '#374151', marginBottom: 8, fontWeight: 500 }}>
          Mã vai trò <span style={{ color: '#dc2626' }}>*</span>
        </label>
        <Input
          placeholder="MODERATOR"
          value={formData.roleCode}
          onChange={(e) => handleChange('roleCode', e.target.value.toUpperCase())}
          style={{
            height: 48,
            borderRadius: 8,
            border: '1px solid #d1d5db',
            fontSize: 15,
          }}
        />
      </div>

      <div style={{ marginBottom: 24 }}>
        <label style={{ display: 'block', fontSize: 15, color: '#374151', marginBottom: 8, fontWeight: 500 }}>
          Tên vai trò <span style={{ color: '#dc2626' }}>*</span>
        </label>
        <Input
          placeholder="Moderator"
          value={formData.roleName}
          onChange={(e) => handleChange('roleName', e.target.value)}
          style={{
            height: 48,
            borderRadius: 8,
            border: '1px solid #d1d5db',
            fontSize: 15,
          }}
        />
      </div>

      <div style={{ marginBottom: 24 }}>
        <label style={{ display: 'block', fontSize: 15, color: '#374151', marginBottom: 8, fontWeight: 500 }}>
          Mô tả
        </label>
        <Input.TextArea
          placeholder="Mô tả vai trò..."
          value={formData.description}
          onChange={(e) => handleChange('description', e.target.value)}
          rows={3}
          style={{
            borderRadius: 8,
            border: '1px solid #d1d5db',
            fontSize: 15,
          }}
        />
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 20, marginBottom: 24 }}>
        <div>
          <label style={{ display: 'block', fontSize: 15, color: '#374151', marginBottom: 8, fontWeight: 500 }}>
            Màu sắc
          </label>
          <Input
            type="color"
            value={formData.color}
            onChange={(e) => handleChange('color', e.target.value)}
            style={{
              height: 48,
              borderRadius: 8,
              border: '1px solid #d1d5db',
              cursor: 'pointer',
            }}
          />
        </div>

        <div>
          <label style={{ display: 'block', fontSize: 15, color: '#374151', marginBottom: 8, fontWeight: 500 }}>
            Thứ tự hiển thị
          </label>
          <Input
            type="number"
            min={0}
            value={formData.displayOrder}
            onChange={(e) => handleChange('displayOrder', parseInt(e.target.value) || 0)}
            style={{
              height: 48,
              borderRadius: 8,
              border: '1px solid #d1d5db',
              fontSize: 15,
            }}
          />
        </div>
      </div>

      <div style={{ marginBottom: 32 }}>
        <label style={{ display: 'block', fontSize: 15, color: '#374151', marginBottom: 12, fontWeight: 500 }}>
          Quyền <span style={{ color: '#dc2626' }}>*</span>
        </label>
        <Checkbox.Group
          value={formData.permissionCodes}
          onChange={handlePermissionChange}
          style={{ width: '100%' }}
        >
          <div style={{ 
            display: 'grid', 
            gridTemplateColumns: '1fr 1fr',
            gap: 12,
            maxHeight: 300,
            overflowY: 'auto',
            padding: '12px',
            border: '1px solid #e5e7eb',
            borderRadius: 8,
            background: '#f9fafb',
          }}>
            {permissions.map((permission) => (
              <Checkbox 
                key={permission.permissionCode} 
                value={permission.permissionCode}
                style={{ 
                  margin: 0,
                  padding: '8px',
                  borderRadius: 6,
                  background: '#fff',
                }}
              >
                <span style={{ fontSize: 14, color: '#374151' }}>
                  {permission.permissionName}
                </span>
              </Checkbox>
            ))}
          </div>
        </Checkbox.Group>
      </div>

      <div style={{ display: 'flex', gap: 12, justifyContent: 'flex-end' }}>
        <Button
          onClick={handleCancel}
          style={{
            height: 44,
            borderRadius: 8,
            fontSize: 15,
            fontWeight: 500,
            border: '1px solid #d1d5db',
            color: '#374151',
            minWidth: 100,
          }}
        >
          Hủy
        </Button>
        <Button
          type="primary"
          onClick={handleSubmit}
          loading={loading}
          style={{
            height: 44,
            borderRadius: 8,
            fontSize: 15,
            fontWeight: 500,
            background: '#2563eb',
            border: 'none',
            minWidth: 120,
          }}
        >
          Thêm vai trò
        </Button>
      </div>
    </Modal>
  );
};

export default AddRoleModal;
