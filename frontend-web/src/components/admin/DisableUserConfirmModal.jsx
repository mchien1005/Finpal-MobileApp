import React from 'react';
import { CloseOutlined, ExclamationCircleOutlined } from '@ant-design/icons';

const DisableUserConfirmModal = ({ open, onConfirm, onCancel, userName, userEmail, isActive = true }) => {
  if (!open) return null;

  const isDisabling = isActive; // true = đang vô hiệu hóa, false = đang kích hoạt lại

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
              background: isDisabling ? '#FEE2E2' : '#DCFCE7',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              flexShrink: 0,
            }}
          >
            <ExclamationCircleOutlined style={{ fontSize: 24, color: isDisabling ? '#DC2626' : '#16A34A' }} />
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
              {isDisabling ? 'Vô hiệu hóa tài khoản' : 'Kích hoạt lại tài khoản'}
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
              {isDisabling ? 'Bạn có chắc chắn muốn vô hiệu hóa tài khoản này?' : 'Bạn có chắc chắn muốn kích hoạt lại tài khoản này?'}
            </p>
          </div>
        </div>

        {/* User Info Box */}
        <div
          style={{
            background: isDisabling ? '#FEF2F2' : '#F0FDF4',
            border: isDisabling ? '1px solid #FECACA' : '1px solid #BBF7D0',
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
          {isDisabling 
            ? 'Người dùng sẽ không thể đăng nhập và truy cập hệ thống cho đến khi tài khoản được kích hoạt lại.'
            : 'Người dùng sẽ có thể đăng nhập và truy cập hệ thống trở lại sau khi kích hoạt.'}
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
              background: isDisabling ? '#DC2626' : '#16A34A',
              border: 'none',
              borderRadius: 8,
              color: '#FFFFFF',
              fontSize: 14,
              fontWeight: 500,
              cursor: 'pointer',
              transition: 'all 0.2s',
            }}
            onMouseEnter={(e) => {
              e.target.style.background = isDisabling ? '#B91C1C' : '#15803D';
            }}
            onMouseLeave={(e) => {
              e.target.style.background = isDisabling ? '#DC2626' : '#16A34A';
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
