import React from 'react';
import { CloseOutlined, ExclamationCircleOutlined } from '@ant-design/icons';

const DisableUserConfirmModal = ({ open, onConfirm, onCancel, userName, userEmail }) => {
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
          width: 448,
          background: '#FFFFFF',
          borderRadius: 24,
          boxShadow: '0px 20px 25px -5px rgba(0, 0, 0, 0.1), 0px 10px 10px -5px rgba(0, 0, 0, 0.04)',
          zIndex: 1001,
          padding: 24,
        }}
      >
        {/* Close button */}
        <button
          onClick={onCancel}
          style={{
            position: 'absolute',
            top: 16,
            right: 16,
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

        {/* Warning Icon */}
        <div style={{ display: 'flex', alignItems: 'flex-start', gap: 12, marginBottom: 16 }}>
          <div
            style={{
              width: 48,
              height: 48,
              borderRadius: '50%',
              background: '#FEE2E2',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              flexShrink: 0,
            }}
          >
            <ExclamationCircleOutlined style={{ fontSize: 24, color: '#DC2626' }} />
          </div>

          <div style={{ flex: 1 }}>
            {/* Title */}
            <h3
              style={{
                fontSize: 18,
                fontWeight: 600,
                color: '#111827',
                margin: '0 0 8px 0',
                lineHeight: '28px',
              }}
            >
              Vô hiệu hóa tài khoản
            </h3>

            {/* Description */}
            <p
              style={{
                fontSize: 14,
                fontWeight: 400,
                color: '#6B7280',
                margin: 0,
                lineHeight: '20px',
              }}
            >
              Bạn có chắc chắn muốn vô hiệu hóa tài khoản này?
            </p>
          </div>
        </div>

        {/* User Info Box */}
        <div
          style={{
            background: '#FEF2F2',
            border: '1px solid #FECACA',
            borderRadius: 12,
            padding: 16,
            marginBottom: 16,
          }}
        >
          <div style={{ marginBottom: 8 }}>
            <span style={{ fontSize: 14, color: '#374151' }}>
              <strong>Tên:</strong> {userName}
            </span>
          </div>
          <div>
            <span style={{ fontSize: 14, color: '#374151' }}>
              <strong>Email:</strong> {userEmail}
            </span>
          </div>
        </div>

        {/* Warning Message */}
        <p
          style={{
            fontSize: 14,
            color: '#6B7280',
            lineHeight: '20px',
            marginBottom: 24,
          }}
        >
          Người dùng sẽ không thể đăng nhập và truy cập hệ thống cho đến khi tài khoản được kích hoạt lại.
        </p>

        {/* Action Buttons */}
        <div style={{ display: 'flex', gap: 12, justifyContent: 'flex-end' }}>
          <button
            onClick={onCancel}
            style={{
              padding: '10px 20px',
              background: '#FFFFFF',
              border: '1px solid #D1D5DB',
              borderRadius: 8,
              color: '#374151',
              fontSize: 14,
              fontWeight: 500,
              cursor: 'pointer',
              transition: 'all 0.2s',
            }}
            onMouseEnter={(e) => {
              e.target.style.background = '#F9FAFB';
            }}
            onMouseLeave={(e) => {
              e.target.style.background = '#FFFFFF';
            }}
          >
            Hủy
          </button>

          <button
            onClick={onConfirm}
            style={{
              padding: '10px 20px',
              background: '#DC2626',
              border: 'none',
              borderRadius: 8,
              color: '#FFFFFF',
              fontSize: 14,
              fontWeight: 500,
              cursor: 'pointer',
              transition: 'all 0.2s',
            }}
            onMouseEnter={(e) => {
              e.target.style.background = '#B91C1C';
            }}
            onMouseLeave={(e) => {
              e.target.style.background = '#DC2626';
            }}
          >
            Xác nhận
          </button>
        </div>
      </div>
    </>
  );
};

export default DisableUserConfirmModal;
