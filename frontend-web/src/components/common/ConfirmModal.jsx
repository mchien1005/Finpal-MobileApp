import React from 'react';
import { Modal } from 'antd';

const ConfirmModal = ({
  open,
  onConfirm,
  onCancel,
  title = 'Xác nhận',
  content = 'Bạn có chắc chắn muốn thực hiện hành động này?',
  confirmText = 'Xác nhận',
  cancelText = 'Hủy',
  danger = true,
}) => {
  return (
    <Modal
      open={open}
      onCancel={onCancel}
      footer={null}
      centered
      closable={false}
      width={340}
      styles={{
        content: {
          borderRadius: 16,
          padding: '32px 24px 24px',
        },
        mask: {
          backgroundColor: 'rgba(0, 0, 0, 0.5)',
        },
      }}
    >
      <div style={{ textAlign: 'center' }}>
        {/* Title */}
        <div
          style={{
            fontSize: 18,
            fontWeight: 600,
            color: '#1F2937',
            marginBottom: 12,
          }}
        >
          {title}
        </div>

        {/* Content */}
        <div
          style={{
            fontSize: 14,
            color: '#6B7280',
            marginBottom: 24,
            lineHeight: 1.5,
          }}
        >
          {content}
        </div>

        {/* Confirm Button */}
        <button
          onClick={onConfirm}
          style={{
            width: '100%',
            height: 48,
            background: danger ? '#EF4444' : '#2B7FFF',
            border: 'none',
            borderRadius: 12,
            color: '#FFFFFF',
            fontSize: 16,
            fontWeight: 600,
            cursor: 'pointer',
            marginBottom: 12,
            transition: 'background 0.2s',
          }}
          onMouseEnter={(e) => {
            e.target.style.background = danger ? '#DC2626' : '#1D4ED8';
          }}
          onMouseLeave={(e) => {
            e.target.style.background = danger ? '#EF4444' : '#2B7FFF';
          }}
        >
          {confirmText}
        </button>

        {/* Cancel Button */}
        <button
          onClick={onCancel}
          style={{
            width: '100%',
            height: 48,
            background: '#FFFFFF',
            border: '1px solid #E5E7EB',
            borderRadius: 12,
            color: '#1F2937',
            fontSize: 16,
            fontWeight: 500,
            cursor: 'pointer',
            transition: 'background 0.2s',
          }}
          onMouseEnter={(e) => {
            e.target.style.background = '#F9FAFB';
          }}
          onMouseLeave={(e) => {
            e.target.style.background = '#FFFFFF';
          }}
        >
          {cancelText}
        </button>
      </div>
    </Modal>
  );
};

export default ConfirmModal;
