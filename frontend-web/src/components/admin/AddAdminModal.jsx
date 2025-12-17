import React, { useState } from 'react';
import { Modal, Input, Button } from 'antd';
import { CloseOutlined } from '@ant-design/icons';

const AddAdminModal = ({ visible, onClose, onAdd }) => {
  const [formData, setFormData] = useState({
    email: '',
    role: 'Admin',
    name: '',
    permissions: '',
    fullName: '',
    password: '',
    phone: '',
    confirmPassword: '',
  });

  const handleChange = (field, value) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
  };

  const handleSubmit = () => {
    // Validation logic here
    onAdd(formData);
    setFormData({
      email: '',
      role: 'Admin',
      name: '',
      permissions: '',
      fullName: '',
      password: '',
      phone: '',
      confirmPassword: '',
    });
  };

  const handleCancel = () => {
    setFormData({
      email: '',
      role: 'Admin',
      name: '',
      permissions: '',
      fullName: '',
      password: '',
      phone: '',
      confirmPassword: '',
    });
    onClose();
  };

  return (
    <Modal
      open={visible}
      onCancel={handleCancel}
      footer={null}
      closeIcon={<CloseOutlined style={{ fontSize: 18, color: '#6b7280' }} />}
      width={840}
      style={{ top: 40 }}
      styles={{
        body: { padding: '32px' },
        content: { borderRadius: 16 },
      }}
    >
      <div style={{ marginBottom: 32 }}>
        <h2 style={{ fontSize: 24, fontWeight: 600, color: '#111827', margin: 0, marginBottom: 6 }}>
          Thêm Admin
        </h2>
        <p style={{ fontSize: 15, color: '#6b7280', margin: 0 }}>
          Thêm một admin mới vào hệ thống.
        </p>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 20, marginBottom: 32 }}>
        {/* Row 1 */}
        <div>
          <label style={{ display: 'block', fontSize: 15, color: '#374151', marginBottom: 8, fontWeight: 500 }}>
            Email
          </label>
          <Input
            placeholder="admin@finpal.com"
            value={formData.email}
            onChange={(e) => handleChange('email', e.target.value)}
            style={{
              height: 48,
              borderRadius: 8,
              border: '1px solid #d1d5db',
              fontSize: 15,
            }}
          />
        </div>

        <div>
          <label style={{ display: 'block', fontSize: 15, color: '#374151', marginBottom: 8, fontWeight: 500 }}>
            Vai trò
          </label>
          <Input
            placeholder="Admin"
            value={formData.role}
            onChange={(e) => handleChange('role', e.target.value)}
            style={{
              height: 48,
              borderRadius: 8,
              border: '1px solid #d1d5db',
              fontSize: 15,
            }}
          />
        </div>

        {/* Row 2 */}
        <div>
          <label style={{ display: 'block', fontSize: 15, color: '#374151', marginBottom: 8, fontWeight: 500 }}>
            Tên đăng nhập
          </label>
          <Input
            placeholder="Admin User"
            value={formData.name}
            onChange={(e) => handleChange('name', e.target.value)}
            style={{
              height: 48,
              borderRadius: 8,
              border: '1px solid #d1d5db',
              fontSize: 15,
            }}
          />
        </div>

        <div>
          <label style={{ display: 'block', fontSize: 15, color: '#374151', marginBottom: 8, fontWeight: 500 }}>
            Quyền
          </label>
          <Input
            placeholder="All"
            value={formData.permissions}
            onChange={(e) => handleChange('permissions', e.target.value)}
            style={{
              height: 48,
              borderRadius: 8,
              border: '1px solid #d1d5db',
              fontSize: 15,
            }}
          />
        </div>

        {/* Row 3 */}
        <div>
          <label style={{ display: 'block', fontSize: 15, color: '#374151', marginBottom: 8, fontWeight: 500 }}>
            Họ và tên
          </label>
          <Input
            placeholder="Super Admin"
            value={formData.fullName}
            onChange={(e) => handleChange('fullName', e.target.value)}
            style={{
              height: 48,
              borderRadius: 8,
              border: '1px solid #d1d5db',
              fontSize: 15,
            }}
          />
        </div>

        <div>
          <label style={{ display: 'block', fontSize: 15, color: '#374151', marginBottom: 8, fontWeight: 500 }}>
            Mật khẩu
          </label>
          <Input.Password
            placeholder="********"
            value={formData.password}
            onChange={(e) => handleChange('password', e.target.value)}
            style={{
              height: 48,
              borderRadius: 8,
              border: '1px solid #d1d5db',
              fontSize: 15,
            }}
          />
        </div>

        {/* Row 4 */}
        <div>
          <label style={{ display: 'block', fontSize: 15, color: '#374151', marginBottom: 8, fontWeight: 500 }}>
            Số điện thoại
          </label>
          <Input
            placeholder="0987654321"
            value={formData.phone}
            onChange={(e) => handleChange('phone', e.target.value)}
            style={{
              height: 48,
              borderRadius: 8,
              border: '1px solid #d1d5db',
              fontSize: 15,
            }}
          />
        </div>

        <div>
          <label style={{ display: 'block', fontSize: 15, color: '#374151', marginBottom: 8, fontWeight: 500 }}>
            Nhập lại mật khẩu
          </label>
          <Input.Password
            placeholder="********"
            value={formData.confirmPassword}
            onChange={(e) => handleChange('confirmPassword', e.target.value)}
            style={{
              height: 48,
              borderRadius: 8,
              border: '1px solid #d1d5db',
              fontSize: 15,
            }}
          />
        </div>
      </div>

      {/* Footer Buttons */}
      <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 12 }}>
        <Button
          onClick={handleCancel}
          style={{
            height: 44,
            padding: '0 24px',
            borderRadius: 8,
            border: '1px solid #d1d5db',
            background: '#fff',
            color: '#374151',
            fontSize: 15,
            fontWeight: 500,
          }}
        >
          Hủy
        </Button>
        <Button
          type="primary"
          onClick={handleSubmit}
          style={{
            height: 44,
            padding: '0 24px',
            borderRadius: 8,
            border: 'none',
            background: '#9333ea',
            color: '#fff',
            fontSize: 15,
            fontWeight: 500,
          }}
        >
          Thêm
        </Button>
      </div>
    </Modal>
  );
};

export default AddAdminModal;
