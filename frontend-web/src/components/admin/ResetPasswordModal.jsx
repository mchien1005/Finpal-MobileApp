import React, { useState } from 'react';
import { CloseOutlined, CopyOutlined, ReloadOutlined } from '@ant-design/icons';

const ResetPasswordModal = ({ open, onConfirm, onCancel, user }) => {
  const [newPassword, setNewPassword] = useState('');

  // Generate random password
  const generatePassword = () => {
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    let password = '';
    for (let i = 0; i < 12; i++) {
      password += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    setNewPassword(password);
    return password;
  };

  // Copy password to clipboard
  const copyPassword = () => {
    navigator.clipboard.writeText(newPassword);
  };

  // Initialize with a generated password when modal opens
  React.useEffect(() => {
    if (open && !newPassword) {
      generatePassword();
    }
  }, [open]);

  if (!open) return null;

  return (
    <>
      {/* Backdrop */}
      <div
        onClick={onCancel}
        style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0, 0, 0, 0.5)',
          zIndex: 1000,
        }}
      />

      {/* Modal */}
      <div
        style={{
          position: 'fixed',
          top: '50%',
          left: '50%',
          transform: 'translate(-50%, -50%)',
          width: 480,
          background: '#FFFFFF',
          borderRadius: 24,
          boxShadow: '0px 20px 25px -5px rgba(0, 0, 0, 0.1), 0px 10px 10px -5px rgba(0, 0, 0, 0.04)',
          zIndex: 1001,
          padding: '24px 24px 32px',
        }}
      >
        {/* Close button */}
        <button
          onClick={onCancel}
          style={{
            position: 'absolute',
            top: 20,
            right: 20,
            background: 'transparent',
            border: 'none',
            cursor: 'pointer',
            padding: 8,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: '#6B7280',
          }}
        >
          <CloseOutlined style={{ fontSize: 16 }} />
        </button>

        {/* Header */}
        <div style={{ marginBottom: 24 }}>
          <h2 style={{ fontSize: 20, fontWeight: 600, color: '#111827', margin: '0 0 8px 0' }}>
            Reset Password
          </h2>
          <p style={{ fontSize: 14, color: '#6B7280', margin: 0 }}>
            Reset password for {user?.user?.name}
          </p>
        </div>

        {/* User Info Grid */}
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: '1fr 1fr',
            gap: '16px 24px',
            marginBottom: 24,
            paddingBottom: 24,
            borderBottom: '1px solid #E5E7EB',
          }}
        >
          <div>
            <div style={{ fontSize: 12, color: '#6B7280', marginBottom: 4 }}>User ID</div>
            <div style={{ fontSize: 14, color: '#111827', fontWeight: 500 }}>{user?.userId}</div>
          </div>

          <div>
            <div style={{ fontSize: 12, color: '#6B7280', marginBottom: 4 }}>Trạng thái</div>
            <div>
              <span
                style={{
                  fontSize: 14,
                  color: '#00a63e',
                  fontWeight: 500,
                }}
              >
                {user?.status}
              </span>
            </div>
          </div>

          <div>
            <div style={{ fontSize: 12, color: '#6B7280', marginBottom: 4 }}>Email</div>
            <div style={{ fontSize: 14, color: '#111827' }}>{user?.contact?.email}</div>
          </div>

          <div>
            <div style={{ fontSize: 12, color: '#6B7280', marginBottom: 4 }}>Số điện thoại</div>
            <div style={{ fontSize: 14, color: '#111827' }}>{user?.contact?.phone}</div>
          </div>

          <div>
            <div style={{ fontSize: 12, color: '#6B7280', marginBottom: 4 }}>Ngân hàng</div>
            <div style={{ fontSize: 14, color: '#111827' }}>{user?.bank}</div>
          </div>

          <div>
            <div style={{ fontSize: 12, color: '#6B7280', marginBottom: 4 }}>Ngày đăng ký</div>
            <div style={{ fontSize: 14, color: '#111827' }}>{user?.registeredDate}</div>
          </div>
        </div>

        {/* New Password Section */}
        <div style={{ marginBottom: 24 }}>
          <label style={{ fontSize: 14, color: '#374151', fontWeight: 500, display: 'block', marginBottom: 8 }}>
            New Password
          </label>
          <div style={{ position: 'relative' }}>
            <input
              type="text"
              value={newPassword}
              readOnly
              style={{
                width: '100%',
                height: 44,
                padding: '0 80px 0 16px',
                border: '1px solid #D1D5DB',
                borderRadius: 8,
                fontSize: 14,
                color: '#111827',
                background: '#F9FAFB',
                fontFamily: 'monospace',
              }}
            />
            <div
              style={{
                position: 'absolute',
                right: 8,
                top: '50%',
                transform: 'translateY(-50%)',
                display: 'flex',
                gap: 4,
              }}
            >
              <button
                onClick={copyPassword}
                style={{
                  width: 32,
                  height: 32,
                  border: 'none',
                  background: 'transparent',
                  cursor: 'pointer',
                  borderRadius: 6,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  color: '#6B7280',
                  transition: 'background 0.2s',
                }}
                onMouseEnter={(e) => {
                  e.target.style.background = '#E5E7EB';
                }}
                onMouseLeave={(e) => {
                  e.target.style.background = 'transparent';
                }}
              >
                <CopyOutlined style={{ fontSize: 16 }} />
              </button>
              <button
                onClick={generatePassword}
                style={{
                  width: 32,
                  height: 32,
                  border: 'none',
                  background: 'transparent',
                  cursor: 'pointer',
                  borderRadius: 6,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  color: '#6B7280',
                  transition: 'background 0.2s',
                }}
                onMouseEnter={(e) => {
                  e.target.style.background = '#E5E7EB';
                }}
                onMouseLeave={(e) => {
                  e.target.style.background = 'transparent';
                }}
              >
                <ReloadOutlined style={{ fontSize: 16 }} />
              </button>
            </div>
          </div>
        </div>

        {/* Confirm Button */}
        <button
          onClick={() => onConfirm(newPassword)}
          style={{
            width: '100%',
            height: 48,
            background: '#7C3AED',
            border: 'none',
            borderRadius: 12,
            color: '#FFFFFF',
            fontSize: 15,
            fontWeight: 600,
            cursor: 'pointer',
            transition: 'background 0.2s',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: 8,
          }}
          onMouseEnter={(e) => {
            e.target.style.background = '#6D28D9';
          }}
          onMouseLeave={(e) => {
            e.target.style.background = '#7C3AED';
          }}
        >
          <ReloadOutlined style={{ fontSize: 16 }} />
          Confirm Reset
        </button>
      </div>
    </>
  );
};

export default ResetPasswordModal;
