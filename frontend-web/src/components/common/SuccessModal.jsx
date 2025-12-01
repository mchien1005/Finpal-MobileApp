import React from 'react';
import { Modal } from 'antd';
import { CheckOutlined } from '@ant-design/icons';

const SuccessModal = ({ 
  open, 
  onClose, 
  message = 'Thao tác thành công!',
  buttonText = 'Đồng ý'
}) => {
  return (
    <Modal
      open={open}
      onCancel={onClose}
      footer={null}
      centered
      closable={false}
      width={340}
      styles={{
        content: {
          borderRadius: 16,
          padding: '40px 24px 32px',
        },
        mask: {
          backgroundColor: 'rgba(0, 0, 0, 0.5)',
        },
      }}
    >
      <div style={{ textAlign: 'center' }}>
        {/* Success Icon */}
        <div
          style={{
            width: 80,
            height: 80,
            borderRadius: '50%',
            border: '3px solid #22C55E',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            margin: '0 auto 24px',
          }}
        >
          <CheckOutlined
            style={{
              fontSize: 40,
              color: '#22C55E',
            }}
          />
        </div>

        {/* Message */}
        <div
          style={{
            fontSize: 18,
            fontWeight: 600,
            color: '#1F2937',
            marginBottom: 32,
          }}
        >
          {message}
        </div>

        {/* Button */}
        <button
          onClick={onClose}
          style={{
            width: '100%',
            height: 48,
            background: '#22C55E',
            border: 'none',
            borderRadius: 12,
            color: '#FFFFFF',
            fontSize: 16,
            fontWeight: 600,
            cursor: 'pointer',
            transition: 'background 0.2s',
          }}
          onMouseEnter={(e) => {
            e.target.style.background = '#16A34A';
          }}
          onMouseLeave={(e) => {
            e.target.style.background = '#22C55E';
          }}
        >
          {buttonText}
        </button>
      </div>
    </Modal>
  );
};

export default SuccessModal;
