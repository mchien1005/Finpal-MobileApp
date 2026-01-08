import React from 'react';
import { Modal } from 'antd';
import { CloseOutlined, WarningOutlined } from '@ant-design/icons';

const ConfirmModal = ({
  open,
  onConfirm,
  onCancel,
  title = 'Xác nhận',
  content = 'Bạn có chắc chắn muốn thực hiện hành động này?',
  confirmText = 'Xác nhận',
  cancelText = 'Hủy',
  danger = true,
  adminInfo = null,
}) => {
  return (
    <Modal
      open={open}
      onCancel={onCancel}
      footer={null}
      closeIcon={<CloseOutlined style={{ fontSize: 16, color: '#6b7280' }} />}
      width={440}
      centered
      styles={{
        body: { padding: '24px' },
        content: { borderRadius: 16 },
      }}
    >
      {/* Header with Icon and Title */}
      <div style={{ display: 'flex', alignItems: 'flex-start', gap: 12, marginBottom: 16 }}>
        <div
          style={{
            width: 40,
            height: 40,
            borderRadius: 8,
            background: '#fee2e2',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            flexShrink: 0,
          }}
        >
          <WarningOutlined style={{ fontSize: 20, color: '#dc2626' }} />
        </div>
        <div style={{ flex: 1 }}>
          <h3 style={{ fontSize: 18, fontWeight: 600, color: '#111827', margin: 0, marginBottom: 4 }}>
            {title}
          </h3>
          <p style={{ fontSize: 14, color: '#6b7280', margin: 0 }}>
            {content}
          </p>
        </div>
      </div>

      {/* Admin Info Card (if provided) */}
      {adminInfo && (
        <div
          style={{
            background: '#fef2f2',
            border: '1px solid #fecaca',
            borderRadius: 8,
            padding: '12px 16px',
            marginBottom: 16,
          }}
        >
          <div style={{ fontSize: 14, color: '#111827', marginBottom: 4 }}>
            <strong>Tên:</strong> {adminInfo.name}
          </div>
          <div style={{ fontSize: 14, color: '#111827' }}>
            <strong>Email:</strong>{' '}
            <span style={{ color: '#2563eb' }}>{adminInfo.email}</span>
          </div>
        </div>
      )}

      {/* Warning Text (if admin info provided) */}
      {adminInfo && (
        <p style={{ fontSize: 14, color: '#6b7280', marginBottom: 20, lineHeight: '20px' }}>
          Hành động này không thể hoàn tác. Admin sẽ bị xóa vĩnh viễn khỏi hệ thống và mất tất cả các quyền truy cập.
        </p>
      )}

      {/* Footer Buttons */}
      <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 12 }}>
        <button
          onClick={onCancel}
          style={{
            height: 40,
            padding: '0 20px',
            borderRadius: 8,
            border: '1px solid #d1d5db',
            background: '#fff',
            color: '#374151',
            fontSize: 14,
            fontWeight: 500,
            cursor: 'pointer',
            transition: 'background 0.2s',
            fontFamily: 'Arimo, sans-serif',
          }}
          onMouseEnter={(e) => {
            e.target.style.background = '#f9fafb';
          }}
          onMouseLeave={(e) => {
            e.target.style.background = '#fff';
          }}
        >
          {cancelText}
        </button>
        <button
          onClick={onConfirm}
          style={{
            height: 40,
            padding: '0 20px',
            borderRadius: 8,
            border: 'none',
            background: danger ? '#dc2626' : '#2563eb',
            color: '#fff',
            fontSize: 14,
            fontWeight: 500,
            cursor: 'pointer',
            transition: 'background 0.2s',
            fontFamily: 'Arimo, sans-serif',
          }}
          onMouseEnter={(e) => {
            e.target.style.background = danger ? '#b91c1c' : '#1d4ed8';
          }}
          onMouseLeave={(e) => {
            e.target.style.background = danger ? '#dc2626' : '#2563eb';
          }}
        >
          {confirmText}
        </button>
      </div>
    </Modal>
  );
};

export default ConfirmModal;