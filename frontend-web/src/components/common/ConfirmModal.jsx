import React from 'react';

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
          width: 350,
          height: 200,
          background: '#FFFFFF',
          borderRadius: 24,
          border: '1px solid rgba(0,0,0,0.1)',
          boxShadow: '0px 10px 15px -3px rgba(0,0,0,0.1), 0px 4px 6px -4px rgba(0,0,0,0.1)',
          zIndex: 1001,
          overflow: 'hidden',
        }}
      >
        {/* Header Content */}
        <div style={{ 
          padding: '33px 10px 0',
          textAlign: 'center',
        }}>
          {/* Title */}
          <p
            style={{
              fontSize: 18,
              fontWeight: 700,
              color: '#0a0a0a',
              lineHeight: '28px',
              fontFamily: 'Arimo, sans-serif',
              margin: 0,
            }}
          >
            {title}
          </p>

          {/* Content */}
          <p
            style={{
              fontSize: 14,
              fontWeight: 400,
              color: '#717182',
              lineHeight: '20px',
              fontFamily: 'Arimo, sans-serif',
              margin: '8px 0 0 0',
              whiteSpace: 'pre-wrap',
            }}
          >
            {content}
          </p>
        </div>

        {/* Footer Buttons */}
        <div style={{ 
          display: 'flex', 
          width: 350,
          position: 'absolute',
          bottom: 0,
          left: 0,
        }}>
          {/* Cancel Button */}
          <button
            onClick={onCancel}
            style={{
              width: 175,
              height: 48,
              background: '#FFFFFF',
              border: '1px solid rgba(0,0,0,0.1)',
              borderRadius: '0 0 0 24px',
              color: '#0a0a0a',
              fontSize: 14,
              fontWeight: 700,
              cursor: 'pointer',
              transition: 'background 0.2s',
              fontFamily: 'Arimo, sans-serif',
              lineHeight: '20px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
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

          {/* Confirm Button */}
          <button
            onClick={onConfirm}
            style={{
              width: 175,
              height: 48,
              background: danger ? '#E7000B' : '#2B7FFF',
              border: 'none',
              borderRadius: '0 0 24px 0',
              color: '#FFFFFF',
              fontSize: 14,
              fontWeight: 700,
              cursor: 'pointer',
              transition: 'background 0.2s',
              fontFamily: 'Arimo, sans-serif',
              lineHeight: '20px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
            onMouseEnter={(e) => {
              e.target.style.background = danger ? '#CC000A' : '#1D4ED8';
            }}
            onMouseLeave={(e) => {
              e.target.style.background = danger ? '#E7000B' : '#2B7FFF';
            }}
          >
            {confirmText}
          </button>
        </div>
      </div>
    </>
  );
};

export default ConfirmModal;