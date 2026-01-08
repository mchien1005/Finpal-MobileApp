import React from 'react';
import { Modal } from 'antd';
import { CheckOutlined, CopyOutlined } from '@ant-design/icons';

const SuccessModal = ({ 
  open, 
  onClose, 
  message = 'Thêm giao dịch thành công!',
  buttonText = 'Đồng ý',
  newPassword = null
}) => {
  const copyPassword = () => {
    if (newPassword) {
      navigator.clipboard.writeText(newPassword);
    }
  };

  return (
    <Modal
      open={open}
      onCancel={onClose}
      footer={null}
      centered
      closable={false}
      width={360}
      styles={{
        content: {
          borderRadius: 24,
          padding: '24px 20px 24px',
          boxShadow: '13px 5px 50px 49px rgba(0, 0, 0, 0.25)',
        },
        mask: {
          backgroundColor: 'rgba(0, 0, 0, 0.5)',
        },
      }}
    >
      <div style={{ textAlign: 'center' }}>
        {/* Success Icon with glow effect */}
        <div
          style={{
            position: 'relative',
            width: 60,
            height: 60,
            margin: '0 auto 14px',
          }}
        >
          {/* Glow background */}
          <div
            style={{
              position: 'absolute',
              width: 60,
              height: 60,
              borderRadius: '50%',
              background: '#dcfce7',
              filter: 'blur(16px)',
            }}
          />
          {/* Icon container */}
          <div
            style={{
              position: 'relative',
              width: 60,
              height: 60,
              borderRadius: '50%',
              border: '3px solid #00C950',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              background: '#FFFFFF',
            }}
          >
            <CheckOutlined
              style={{
                fontSize: 28,
                color: '#00C950',
                fontWeight: 'bold',
              }}
            />
          </div>
        </div>

        {/* Message */}
        <div
          style={{
            fontSize: 16,
            fontWeight: 400,
            color: '#101828',
            marginBottom: newPassword ? 16 : 14,
            lineHeight: '30px',
            fontFamily: 'Arimo, sans-serif',
          }}
        >
          {message}
        </div>

        {/* New Password Display */}
        {newPassword && (
          <div
            style={{
              background: '#f9fafb',
              border: '1px solid #e5e7eb',
              borderRadius: 12,
              padding: '12px 16px',
              marginBottom: 16,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              gap: 12,
            }}
          >
            <div style={{ flex: 1, textAlign: 'left' }}>
              <div style={{ fontSize: 12, color: '#6b7280', marginBottom: 4 }}>Mật khẩu mới</div>
              <div style={{ fontSize: 14, fontWeight: 600, color: '#111827', fontFamily: 'monospace' }}>
                {newPassword}
              </div>
            </div>
            <button
              onClick={copyPassword}
              style={{
                background: '#fff',
                border: '1px solid #e5e7eb',
                borderRadius: 8,
                padding: '8px 12px',
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                gap: 6,
                fontSize: 13,
                color: '#374151',
              }}
            >
              <CopyOutlined /> Copy
            </button>
          </div>
        )}

        {/* Button */}
        <button
          onClick={onClose}
    style={{
            width: 320,
            maxWidth: '100%',
            height: 48,
            background: '#00C950',
            border: 'none',
            borderRadius: 14,
            color: '#FFFFFF',
            fontSize: 14,
            fontWeight: 700,
            cursor: 'pointer',
            transition: 'background 0.2s',
            fontFamily: 'Arimo, sans-serif',
            lineHeight: '20px',
          }}
          onMouseEnter={(e) => {
            e.target.style.background = '#00B347';
          }}
          onMouseLeave={(e) => {
            e.target.style.background = '#00C950';
          }}
        >
          {buttonText}
        </button>
      </div>
    </Modal>
  );
};

export default SuccessModal;