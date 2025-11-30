import { Button } from 'antd';
import { RightOutlined, CheckCircleOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';

const OnboardingStep1 = () => {
  const navigate = useNavigate();

  const handleNext = () => {
    navigate('/onboarding/step2');
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
            Bước 1 / 3
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
          {/* Logo */}
          <div style={{ textAlign: 'center', marginBottom: 24 }}>
            <img
              src="/images/logo.svg"
              alt="FinPal Logo"
              style={{
                width: 80,
                height: 80,
              }}
            />
          </div>

          {/* Title */}
          <h2
            style={{
              fontSize: 24,
              textAlign: 'center',
              marginBottom: 16,
              marginTop: 0,
              color: '#000000',
              fontWeight: 'normal',
            }}
          >
            Chào mừng đến với FinPal!
          </h2>

          {/* Description */}
          <p
            style={{
              fontSize: 16,
              textAlign: 'center',
              color: '#4A5565',
              marginBottom: 32,
              lineHeight: 1.5,
            }}
          >
            Trợ lý tài chính thông minh giúp bạn quản lý chi tiêu tự động, tiết kiệm thời gian và
            đạt được mục tiêu tài chính.
          </p>

          {/* Features list */}
          <div style={{ marginBottom: 32 }}>
            <div
              style={{
                display: 'flex',
                alignItems: 'flex-start',
                gap: 12,
                marginBottom: 12,
              }}
            >
              <CheckCircleOutlined
                style={{
                  color: '#52c41a',
                  fontSize: 20,
                  marginTop: 2,
                }}
              />
              <p style={{ margin: 0, fontSize: 14, color: '#000000', lineHeight: 1.5 }}>
                Tự động ghi nhận chi tiêu từ SMS ngân hàng
              </p>
            </div>
            <div
              style={{
                display: 'flex',
                alignItems: 'flex-start',
                gap: 12,
                marginBottom: 12,
              }}
            >
              <CheckCircleOutlined
                style={{
                  color: '#52c41a',
                  fontSize: 20,
                  marginTop: 2,
                }}
              />
              <p style={{ margin: 0, fontSize: 14, color: '#000000', lineHeight: 1.5 }}>
                AI phân tích và đưa ra gợi ý tiết kiệm
              </p>
            </div>
            <div
              style={{
                display: 'flex',
                alignItems: 'flex-start',
                gap: 12,
              }}
            >
              <CheckCircleOutlined
                style={{
                  color: '#52c41a',
                  fontSize: 20,
                  marginTop: 2,
                }}
              />
              <p style={{ margin: 0, fontSize: 14, color: '#000000', lineHeight: 1.5 }}>
                Theo dõi mục tiêu và báo cáo chi tiết
              </p>
            </div>
          </div>

          {/* Next button */}
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
            }}
          >
            Bắt đầu
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

export default OnboardingStep1;
