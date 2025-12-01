import React from 'react';
import { Button, Alert } from 'antd';
import { RightOutlined, SafetyOutlined, LockOutlined, EyeInvisibleOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';

const OnboardingStep2 = () => {
  const navigate = useNavigate();

  const handleNext = () => {
    navigate('/onboarding/step3');
  };

  const handleBack = () => {
    navigate('/onboarding/step1');
  };

  const handleSkip = () => {
    navigate('/dashboard');
  };

  return (
    <div
      style={{
        minHeight: '100vh',
        background: 'linear-gradient(90deg, #2B7FFF 0%, #2B7FFF 100%)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '24px',
      }}
    >
      <div
        style={{
          width: '100%',
          maxWidth: 448,
        }}
      >
        {/* Progress indicator */}
        <div style={{ marginBottom: 24 }}>
          <div
            style={{
              display: 'flex',
              gap: 12,
              marginBottom: 8,
            }}
          >
            <div
              style={{
                flex: 1,
                height: 4,
                background: '#ffffff',
                borderRadius: 9999,
              }}
            />
            <div
              style={{
                flex: 1,
                height: 4,
                background: '#ffffff',
                borderRadius: 9999,
              }}
            />
            <div
              style={{
                flex: 1,
                height: 4,
                background: 'rgba(255, 255, 255, 0.3)',
                borderRadius: 9999,
              }}
            />
            <div
              style={{
                flex: 1,
                height: 4,
                background: 'rgba(255, 255, 255, 0.3)',
                borderRadius: 9999,
              }}
            />
          </div>
          <p
            style={{
              textAlign: 'center',
              color: '#ffffff',
              fontSize: 14,
              margin: 0,
            }}
          >
            Bước 2 / 3
          </p>
        </div>

        {/* Card content */}
        <div
          style={{
            background: '#ffffff',
            borderRadius: 14,
            boxShadow: '0px 25px 50px -12px rgba(0,0,0,0.25)',
            padding: 32,
            marginBottom: 24,
          }}
        >
          {/* Icon */}
          <div
            style={{
              width: 80,
              height: 80,
              margin: '0 auto 32px',
              background: '#52c41a',
              borderRadius: 9999,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <SafetyOutlined style={{ fontSize: 40, color: '#ffffff' }} />
          </div>

          {/* Title */}
          <h2
            style={{
              fontSize: 24,
              textAlign: 'center',
              marginBottom: 16,
              color: '#000000',
              fontWeight: 'normal',
            }}
          >
            Bảo mật & Quyền riêng tư
          </h2>

          {/* Description */}
          <p
            style={{
              fontSize: 16,
              textAlign: 'center',
              color: '#4A5565',
              marginBottom: 32,
            }}
          >
            Chúng tôi cam kết bảo vệ thông tin tài chính của bạn
          </p>

          {/* Security features */}
          <div style={{ marginBottom: 32 }}>
            <Alert
              message={
                <span style={{ fontWeight: 'bold', color: '#016630' }}>Mã hóa dữ liệu:</span>
              }
              description="Tất cả giao dịch được mã hóa AES-256"
              type="success"
              icon={<LockOutlined />}
              style={{ marginBottom: 16 }}
            />
            <Alert
              message={
                <span style={{ fontWeight: 'bold', color: '#193cb8' }}>Không thu thập nhạy cảm:</span>
              }
              description="Không yêu cầu mật khẩu/OTP ngân hàng"
              type="info"
              icon={<SafetyOutlined />}
              style={{ marginBottom: 16 }}
            />
            <Alert
              message={
                <span style={{ fontWeight: 'bold', color: '#6e11b0' }}>Chỉ đọc SMS:</span>
              }
              description="Không gửi hoặc xóa tin nhắn của bạn"
              type="warning"
              icon={<EyeInvisibleOutlined />}
              style={{
                background: '#f9f0ff',
                borderColor: '#e9d4ff',
              }}
            />
          </div>

          {/* Privacy note */}
          <div
            style={{
              background: '#f5f5f5',
              padding: 16,
              borderRadius: 10,
              marginBottom: 32,
            }}
          >
            <p style={{ margin: 0, fontSize: 12, color: '#4A5565', lineHeight: 1.6 }}>
              FinPal chỉ đọc nội dung SMS từ ngân hàng để tự động ghi nhận giao dịch. Dữ liệu được
              xử lý và lưu trữ cục bộ trên thiết bị của bạn. Chúng tôi tuân thủ các tiêu chuẩn bảo
              mật cao nhất.
            </p>
          </div>

          {/* Action buttons */}
          <div style={{ display: 'flex', gap: 12 }}>
            <Button
              block
              onClick={handleBack}
              style={{
                height: 36,
                borderRadius: 8,
                flex: 1,
              }}
            >
              Quay lại
            </Button>
            <Button
              type="primary"
              block
              onClick={handleNext}
              icon={<RightOutlined />}
              iconPosition="end"
              style={{
                height: 36,
                borderRadius: 8,
                background: 'linear-gradient(to right, #155DFC, #4F39F6)',
                border: 'none',
                fontSize: 14,
                flex: 1,
              }}
            >
              Tiếp tục
            </Button>
          </div>
        </div>

        {/* Skip button */}
        <div style={{ textAlign: 'center' }}>
          <Button
            type="text"
            onClick={handleSkip}
            style={{
              color: '#ffffff',
              fontSize: 14,
              opacity: 0.75,
            }}
          >
            Bỏ qua hướng dẫn
          </Button>
        </div>
      </div>
    </div>
  );
};

export default OnboardingStep2;
