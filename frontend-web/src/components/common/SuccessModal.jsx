import React from 'react';
import { Modal } from 'antd';
import { CheckOutlined } from '@ant-design/icons';

const SuccessModal = ({ 
  open, 
  onClose, 
  message = 'Thêm giao dịch thành công!',
  buttonText = 'Đồng ý'
}) => {
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
            marginBottom: 14,
            lineHeight: '30px',
            fontFamily: 'Arimo, sans-serif',
          }}
        >
          {message}
        </div>

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