import React from 'react';
import { Modal, Button } from 'antd';
import { CloseOutlined } from '@ant-design/icons';

const LoginFailureDetailModal = ({ visible, onClose, loginData }) => {
  if (!loginData) return null;

  const attemptHistory = [
    'Lần #5 - 24/03/2024 11:20',
    'Lần #4 - 24/03/2024 11:19',
    'Lần #3 - 24/03/2024 11:18',
    'Lần #2 - 24/03/2024 11:17',
    'Lần #1 - 24/03/2024 11:15',
  ];

  return (
    <Modal
      open={visible}
      onCancel={onClose}
      footer={null}
      width={600}
      closeIcon={<CloseOutlined style={{ fontSize: 16, color: '#111827' }} />}
      styles={{
        body: { padding: 0 },
        header: { padding: '24px 24px 20px 24px' },
      }}
      style={{ borderRadius: 16 }}
    >
      <div style={{ padding: '24px' }}>
        {/* Header */}
        <h2 style={{ fontSize: 20, fontWeight: 600, color: '#111827', margin: 0, marginBottom: 24 }}>
          Chi tiết đăng nhập thất bại
        </h2>

        {/* Content Grid */}
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 24, marginBottom: 32 }}>
          {/* Left Column - Thông tin cơ bản */}
          <div>
            <h3 style={{ fontSize: 16, fontWeight: 600, color: '#111827', margin: 0, marginBottom: 16 }}>
              Thông tin cơ bản
            </h3>
            
            <div style={{ marginBottom: 16 }}>
              <p style={{ fontSize: 12, color: '#6b7280', margin: 0, marginBottom: 4 }}>Thời gian</p>
              <p style={{ fontSize: 14, color: '#111827', margin: 0 }}>{loginData.timestamp}</p>
            </div>

            <div style={{ marginBottom: 16 }}>
              <p style={{ fontSize: 12, color: '#6b7280', margin: 0, marginBottom: 4 }}>Email</p>
              <p style={{ fontSize: 14, color: '#111827', margin: 0 }}>{loginData.user}</p>
            </div>

            <div style={{ marginBottom: 16 }}>
              <p style={{ fontSize: 12, color: '#6b7280', margin: 0, marginBottom: 4 }}>Địa chỉ IP</p>
              <p style={{ fontSize: 14, color: '#111827', margin: 0 }}>{loginData.ipAddress}</p>
            </div>

            <div>
              <p style={{ fontSize: 12, color: '#6b7280', margin: 0, marginBottom: 4 }}>Số lần thử</p>
              <p style={{ fontSize: 14, color: '#dc2626', margin: 0, fontWeight: 500 }}>{loginData.attempts}</p>
            </div>
          </div>

          {/* Right Column - Hành động bảo mật */}
          <div>
            <h3 style={{ fontSize: 16, fontWeight: 600, color: '#111827', margin: 0, marginBottom: 16 }}>
              Hành động bảo mật
            </h3>

            <div style={{ marginBottom: 12 }}>
              <div
                style={{
                  padding: '12px 16px',
                  background: '#fff7ed',
                  border: '1px solid #fed7aa',
                  borderRadius: 8,
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                }}
              >
                <div>
                  <p style={{ fontSize: 14, fontWeight: 500, color: '#111827', margin: 0, marginBottom: 2 }}>
                    Chặn IP này
                  </p>
                  <p style={{ fontSize: 12, color: '#6b7280', margin: 0 }}>
                    Ngăn chặn từ {loginData.ipAddress}
                  </p>
                </div>
                <Button
                  style={{
                    background: '#dc2626',
                    border: 'none',
                    borderRadius: 6,
                    height: 32,
                    padding: '0 16px',
                    fontSize: 14,
                    color: '#fff',
                    fontWeight: 500,
                  }}
                >
                  Chặn IP
                </Button>
              </div>
            </div>

            <div style={{ marginBottom: 12 }}>
              <div
                style={{
                  padding: '12px 16px',
                  background: '#eff6ff',
                  border: '1px solid #bfdbfe',
                  borderRadius: 8,
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                }}
              >
                <div>
                  <p style={{ fontSize: 14, fontWeight: 500, color: '#111827', margin: 0, marginBottom: 2 }}>
                    Gửi cảnh báo
                  </p>
                  <p style={{ fontSize: 12, color: '#6b7280', margin: 0 }}>
                    Email cho quản trị viên
                  </p>
                </div>
                <Button
                  style={{
                    background: '#2563eb',
                    border: 'none',
                    borderRadius: 6,
                    height: 32,
                    padding: '0 16px',
                    fontSize: 14,
                    color: '#fff',
                    fontWeight: 500,
                  }}
                >
                  Gửi email
                </Button>
              </div>
            </div>

            <div>
              <div
                style={{
                  padding: '12px 16px',
                  background: '#f0fdf4',
                  border: '1px solid #bbf7d0',
                  borderRadius: 8,
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                }}
              >
                <div>
                  <p style={{ fontSize: 14, fontWeight: 500, color: '#111827', margin: 0, marginBottom: 2 }}>
                    Thêm whitelist
                  </p>
                  <p style={{ fontSize: 12, color: '#6b7280', margin: 0 }}>
                    Cho phép truy cập cập tư do
                  </p>
                </div>
                <Button
                  style={{
                    background: '#16a34a',
                    border: 'none',
                    borderRadius: 6,
                    height: 32,
                    padding: '0 16px',
                    fontSize: 14,
                    color: '#fff',
                    fontWeight: 500,
                  }}
                >
                  Whitelist
                </Button>
              </div>
            </div>
          </div>
        </div>

        {/* Technical Details & Attempt History Grid */}
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 24, marginBottom: 24 }}>
          {/* Left Column - Thông tin kỹ thuật */}
          <div>
            <h3 style={{ fontSize: 16, fontWeight: 600, color: '#111827', margin: 0, marginBottom: 16 }}>
              Thông tin kỹ thuật
            </h3>

            <div style={{ marginBottom: 12 }}>
              <p style={{ fontSize: 12, color: '#6b7280', margin: 0, marginBottom: 4 }}>Trình duyệt</p>
              <p style={{ fontSize: 14, color: '#111827', margin: 0 }}>Chrome 122.0.0.0</p>
            </div>

            <div style={{ marginBottom: 12 }}>
              <p style={{ fontSize: 12, color: '#6b7280', margin: 0, marginBottom: 4 }}>Vị trí</p>
              <p style={{ fontSize: 14, color: '#111827', margin: 0 }}>Hà Nội, Việt Nam</p>
            </div>

            <div style={{ marginBottom: 12 }}>
              <p style={{ fontSize: 12, color: '#6b7280', margin: 0, marginBottom: 4 }}>Trạng thái</p>
              <span
                style={{
                  display: 'inline-block',
                  padding: '2px 10px',
                  background: '#fee2e2',
                  color: '#dc2626',
                  borderRadius: 6,
                  fontSize: 12,
                  fontWeight: 500,
                }}
              >
                Đã chặn
              </span>
            </div>

            <div>
              <p style={{ fontSize: 12, color: '#6b7280', margin: 0, marginBottom: 4 }}>Lý do thất bại</p>
              <p style={{ fontSize: 14, color: '#111827', margin: 0 }}>Mật khẩu không đúng</p>
            </div>
          </div>

          {/* Right Column - Lịch sử thử đăng nhập */}
          <div>
            <h3 style={{ fontSize: 16, fontWeight: 600, color: '#111827', margin: 0, marginBottom: 16 }}>
              Lịch sử thử đăng nhập
            </h3>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              {attemptHistory.map((attempt, index) => (
                <div
                  key={index}
                  style={{
                    padding: '8px 12px',
                    background: '#f9fafb',
                    borderRadius: 6,
                    fontSize: 14,
                    color: '#111827',
                  }}
                >
                  {attempt}
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Footer */}
        <div style={{ display: 'flex', justifyContent: 'flex-end', paddingTop: 16, borderTop: '1px solid #e5e7eb' }}>
          <Button
            onClick={onClose}
            style={{
              background: '#fff',
              border: '1px solid #e5e7eb',
              borderRadius: 8,
              height: 40,
              padding: '0 24px',
              fontSize: 14,
              color: '#374151',
              fontWeight: 500,
            }}
          >
            Đóng
          </Button>
        </div>
      </div>
    </Modal>
  );
};

export default LoginFailureDetailModal;
