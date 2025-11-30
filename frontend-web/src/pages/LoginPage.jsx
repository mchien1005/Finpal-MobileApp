import { useState } from 'react';
import { Form, Input, Button, Checkbox, message } from 'antd';
import { MailOutlined, LockOutlined, EyeInvisibleOutlined, EyeOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import authService from '../services/authService';
import { parseErrorMessage } from '../utils/formatters';


const LoginPage = () => {
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleLogin = async (values) => {
    setLoading(true);
    try {
      const response = await authService.login(values.username, values.password);
      message.success('Đăng nhập thành công!');
      const destination = response?.user?.role === 'ADMIN' ? '/admin/dashboard' : '/dashboard';
      navigate(destination);
    } catch (error) {
      message.error(parseErrorMessage(error));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      style={{
        minHeight: '100vh',
        background: '#EFF6FF',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '24px',
      }}
    >
      {/* Logo and Title */}
      <div style={{ textAlign: 'center', marginBottom: 32 }}>
        <div
          style={{
            width: 80,
            height: 80,
            margin: '0 auto 16px',
            borderRadius: 16,
            overflow: 'hidden',
            background: '#1677ff',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
          }}
        >
          <img
            src="images/logo80.png"
            alt="FinPal Logo"
            style={{
              width: '100%',
              height: '100%',
              objectFit: 'cover',
            }}
            onError={(e) => {
              e.target.style.display = 'none';
              e.target.parentElement.innerHTML = '<span style="color: white; font-size: 32px; font-weight: bold;">FP</span>';
            }}
          />
        </div>
        <p
          style={{
            fontSize: 16,
            color: '#4A5565',
            fontWeight: 'bold',
            margin: 0,
          }}
        >
          Ví Thông Minh của bạn
        </p>
      </div>

      {/* Login Card */}
      <div
        style={{
          background: '#ffffff',
          borderRadius: 14,
          boxShadow: '0px 25px 50px -12px rgba(0,0,0,0.25)',
          width: '100%',
          maxWidth: 448,
          padding: '32px',
        }}
      >
        <h2
          style={{
            fontSize: 24,
            textAlign: 'center',
            marginBottom: 24,
            color: '#000000',
            fontWeight: 'normal',
          }}
        >
          Đăng nhập
        </h2>

        <Form onFinish={handleLogin} size="small" layout="vertical">
          <Form.Item
            label={<span style={{ fontSize: 14, marginBottom: -10 }}>Email hoặc Số điện thoại</span>}
            name="username"
            rules={[{ required: true, message: 'Vui lòng nhập email hoặc số điện thoại!' }]}
            required={false}
          >
            <Input
              prefix={<MailOutlined style={{ color: '#717182' }} />}
              placeholder="your@email.com"
              style={{
                background: '#F3F3F5',
                border: 'none',
                borderRadius: 8,
                height: 36,
                
              }}
            />
          </Form.Item>

          <Form.Item
            label={<span style={{ fontSize: 14, marginBottom: -10 }}>Mật khẩu</span>}
            name="password"
            rules={[{ required: true, message: 'Vui lòng nhập mật khẩu!' }]}
            required={false}
          >
            <Input.Password
              prefix={<LockOutlined style={{ color: '#717182' }} />}
              placeholder="••••••••"
              iconRender={(visible) =>
                visible ? (
                  <EyeOutlined style={{ color: '#717182' }} />
                ) : (
                  <EyeInvisibleOutlined style={{ color: '#717182' }} />
                )
              }
              style={{
                background: '#F3F3F5',
                border: 'none',
                borderRadius: 8,
                height: 36,
              }}
            />
          </Form.Item>

          <div
            style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
                marginBottom: 8,
            }}
          >
            <Form.Item name="remember" valuePropName="checked" noStyle>
              <Checkbox style={{ fontSize: 14 }}>Ghi nhớ đăng nhập</Checkbox>
            </Form.Item>
            <a
              href="/forgot-password"
              style={{
                color: '#155DFC',
                fontSize: 14,
                textDecoration: 'none',
              }}
            >
              Quên mật khẩu?
            </a>
          </div>

          <Form.Item>
            <Button
              type="primary"
              htmlType="submit"
              block
              loading={loading}
              style={{
                height: 48,
                borderRadius: 8,
                background: 'linear-gradient(to right, #155DFC, #4F39F6)',
                border: 'none',
                fontSize: 14,
                fontWeight: 'normal',
              }}
            >
              Đăng nhập
            </Button>
          </Form.Item>
        </Form>

        {/* Divider */}
        <div
          style={{
            position: 'relative',
            textAlign: 'center',
            marginTop: 24,
            marginBottom: 24,
          }}
        >
          <div
            style={{
              position: 'absolute',
              top: '50%',
              left: 0,
              right: 0,
              height: 1,
              background: '#D1D5DC',
            }}
          />
          <span
            style={{
              position: 'relative',
              background: '#ffffff',
              padding: '0 8px',
              color: '#6A7282',
              fontSize: 14,
            }}
          >
            Hoặc
          </span>
        </div>

        {/* Sign up link */}
        <div style={{ textAlign: 'center', marginTop: 24 }}>
          <span style={{ color: '#4A5565', fontSize: 14 }}>
            Chưa có tài khoản?{' '}
          </span>
          <a
            href="/register"
            style={{
              color: '#155DFC',
              fontSize: 14,
              textDecoration: 'none',
            }}
          >
            Đăng ký ngay
          </a>
        </div>
      </div>

      {/* Terms and Privacy */}
      <div
        style={{
          textAlign: 'center',
          marginTop: 32,
          maxWidth: 448,
          color: '#6A7282',
          fontSize: 14,
          lineHeight: 1.5,
        }}
      >
        Bằng việc đăng nhập, bạn đồng ý với{' '}
        <a href="/terms" style={{ color: '#155DFC', textDecoration: 'none' }}>
          Điều khoản sử dụng
        </a>{' '}
        và{' '}
        <a href="/privacy" style={{ color: '#155DFC', textDecoration: 'none' }}>
          Chính sách bảo mật
        </a>
      </div>
    </div>
  );
};

export default LoginPage;
