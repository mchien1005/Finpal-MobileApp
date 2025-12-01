import React from 'react';
import { Button } from 'antd';
import { BulbOutlined, BellOutlined, LineChartOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';

const OnboardingStep3 = () => {
  const navigate = useNavigate();

  const handleStart = () => {
    navigate('/dashboard');
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
          </div>
          <p
            style={{
              textAlign: 'center',
              color: '#ffffff',
              fontSize: 14,
              margin: 0,
            }}
          >
            Bước 3 / 3
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
              background: '#ad46ff',
              borderRadius: 9999,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <LineChartOutlined style={{ fontSize: 40, color: '#ffffff' }} />
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
            Sẵn sàng bắt đầu!
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
            Khám phá các tính năng thông minh của FinPal
          </p>

          {/* Features */}
          <div style={{ marginBottom: 32 }}>
            {/* Feature 1 */}
            <div
              style={{
                background: '#e6f7ff',
                padding: 16,
                borderRadius: 10,
                marginBottom: 16,
                display: 'flex',
                gap: 16,
              }}
            >
              <div
                style={{
                  width: 44,
                  height: 44,
                  background: '#2B7FFF',
                  borderRadius: 10,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  flexShrink: 0,
                }}
              >
                <BulbOutlined style={{ fontSize: 20, color: '#ffffff' }} />
              </div>
              <div>
                <p style={{ margin: '0 0 4px 0', fontSize: 14, fontWeight: 'normal', color: '#000000' }}>
                  AI phân loại thông minh
                </p>
                <p style={{ margin: 0, fontSize: 12, color: '#4A5565', lineHeight: 1.3 }}>
                  Tự động nhận diện "GRAB" là Di chuyển, "SHOPEE" là Mua sắm
                </p>
              </div>
            </div>

            {/* Feature 2 */}
            <div
              style={{
                background: '#f6ffed',
                padding: 16,
                borderRadius: 10,
                marginBottom: 16,
                display: 'flex',
                gap: 16,
              }}
            >
              <div
                style={{
                  width: 44,
                  height: 44,
                  background: '#00c950',
                  borderRadius: 10,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  flexShrink: 0,
                }}
              >
                <BellOutlined style={{ fontSize: 20, color: '#ffffff' }} />
              </div>
              <div>
                <p style={{ margin: '0 0 4px 0', fontSize: 14, fontWeight: 'normal', color: '#000000' }}>
                  Cảnh báo chủ động
                </p>
                <p style={{ margin: 0, fontSize: 12, color: '#4A5565', lineHeight: 1.3 }}>
                  "Bạn đã chi 70% hạn mức Ăn uống, còn 10 ngày nữa hết tháng"
                </p>
              </div>
            </div>

            {/* Feature 3 */}
            <div
              style={{
                background: '#f9f0ff',
                padding: 16,
                borderRadius: 10,
                display: 'flex',
                gap: 16,
              }}
            >
              <div
                style={{
                  width: 44,
                  height: 44,
                  background: '#ad46ff',
                  borderRadius: 10,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  flexShrink: 0,
                }}
              >
                <LineChartOutlined style={{ fontSize: 20, color: '#ffffff' }} />
              </div>
              <div>
                <p style={{ margin: '0 0 4px 0', fontSize: 14, fontWeight: 'normal', color: '#000000' }}>
                  Gợi ý tiết kiệm
                </p>
                <p style={{ margin: 0, fontSize: 12, color: '#4A5565', lineHeight: 1.3 }}>
                  "Giảm trà sữa từ 200k xuống 100k/tuần, tiết kiệm 400k/tháng"
                </p>
              </div>
            </div>
          </div>

          {/* Start button */}
          <Button
            type="primary"
            block
            onClick={handleStart}
            style={{
              height: 48,
              borderRadius: 8,
              background: 'linear-gradient(to right, #155DFC, #4F39F6)',
              border: 'none',
              fontSize: 14,
            }}
          >
            Bắt đầu sử dụng FinPal
          </Button>
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

export default OnboardingStep3;
