import React, { useState } from 'react';
import { Form, Input, Button, Checkbox, message, Row, Col } from 'antd';
import { UserOutlined, MailOutlined, LockOutlined, PhoneOutlined, EyeInvisibleOutlined, EyeOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import authService from '../services/authService';
import { parseErrorMessage } from '../utils/formatters';


const RegisterPage = () => {
  const [loading, setLoading] = useState(false);
  const [form] = Form.useForm();
  const navigate = useNavigate();

  const handleRegister = async (values) => {
    // Check if passwords match
    if (values.password !== values.confirmPassword) {
      message.error('Mật khẩu không khớp!');
      return;
    }

    // Check if user agreed to terms
    if (!values.agreeToTerms) {
      message.error('Vui lòng đồng ý với Điều khoản sử dụng và Chính sách bảo mật!');
      return;
    }

    setLoading(true);
    try {
      await authService.register({
        fullName: values.fullName,
        username: values.username,
        email: values.email,
        password: values.password,
      });
      
      message.success('Đăng ký thành công!');
      navigate('/onboarding/step1');
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
        <h2
          style={{
            fontSize: 30,
            color: '#101828',
            fontWeight: 'normal',
            margin: '0 0 8px 0',
          }}
        >
          Tạo tài khoản
        </h2>
        <p
          style={{
            fontSize: 16,
            color: '#4A5565',
            margin: 0,
          }}
        >
          Bắt đầu quản lý tài chính thông minh
        </p>
      </div>

      {/* Register Card */}
      <div
        style={{
          background: '#ffffff',
          borderRadius: 14,
          boxShadow: '0px 25px 50px -12px rgba(0,0,0,0.25)',
          width: '100%',
          maxWidth: 853,
          padding: '32px',
        }}
      >
        {/* Custom CSS để giảm khoảng cách label-input */}
        <style>
          {`
            .register-form .ant-form-item-label {
              padding-bottom: 4px !important;
            }
            .register-form .ant-form-item-label > label {
              height: auto !important;
            }
          `}
        </style>
        <Form 
          form={form} 
          onFinish={handleRegister} 
          size="large" 
          layout="vertical"
          requiredMark={false}
          className="register-form"
        >
          <Row gutter={24}>
            {/* Cột trái */}
            <Col xs={24} md={12}>
              <Form.Item
                label={
                  <span style={{ fontSize: 14 }}>
                    Tên đăng nhập <span style={{ color: '#FB2C36' }}>*</span>
                  </span>
                }
                name="username"
                rules={[
                  { required: true, message: 'Vui lòng nhập tên đăng nhập!' },
                  { min: 3, message: 'Tên đăng nhập phải có ít nhất 3 ký tự!' },
                  { max: 20, message: 'Tên đăng nhập không được quá 20 ký tự!' },
                  { pattern: /^[a-zA-Z0-9_]+$/, message: 'Tên đăng nhập chỉ chứa chữ, số và dấu gạch dưới!' },
                ]}
                style={{ marginBottom: 16 }}
              >
                <Input
                  prefix={<PhoneOutlined style={{ color: '#717182' }} />}
                  placeholder="0901234567"
                  style={{
                    background: '#F3F3F5',
                    border: 'none',
                    borderRadius: 8,
                    height: 36,
                  }}
                />
              </Form.Item>

              <Form.Item
                label={
                  <span style={{ fontSize: 14 }}>
                    Mật khẩu <span style={{ color: '#FB2C36' }}>*</span>
                  </span>
                }
                name="password"
                rules={[
                  { required: true, message: 'Vui lòng nhập mật khẩu!' },
                  { min: 6, message: 'Mật khẩu phải có ít nhất 6 ký tự!' },
                ]}
                style={{ marginBottom: 16 }}
              >
                <Input.Password
                  prefix={<LockOutlined style={{ color: '#717182' }} />}
                  placeholder="Ít nhất 6 ký tự"
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

              <Form.Item
                label={
                  <span style={{ fontSize: 14 }}>
                    Xác nhận mật khẩu <span style={{ color: '#FB2C36' }}>*</span>
                  </span>
                }
                name="confirmPassword"
                rules={[
                  { required: true, message: 'Vui lòng nhập lại mật khẩu!' },
                  ({ getFieldValue }) => ({
                    validator(_, value) {
                      if (!value || getFieldValue('password') === value) {
                        return Promise.resolve();
                      }
                      return Promise.reject(new Error('Mật khẩu không khớp!'));
                    },
                  }),
                ]}
                style={{ marginBottom: 16 }}
              >
                <Input.Password
                  prefix={<LockOutlined style={{ color: '#717182' }} />}
                  placeholder="Nhập lại mật khẩu"
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
            </Col>

            {/* Cột phải */}
            <Col xs={24} md={12}>
              <Form.Item
                label={
                  <span style={{ fontSize: 14 }}>
                    Họ và tên <span style={{ color: '#FB2C36' }}>*</span>
                  </span>
                }
                name="fullName"
                rules={[
                  { required: true, message: 'Vui lòng nhập họ và tên!' },
                  { min: 2, message: 'Họ và tên phải có ít nhất 2 ký tự!' },
                ]}
                style={{ marginBottom: 16 }}
              >
                <Input
                  prefix={<UserOutlined style={{ color: '#717182' }} />}
                  placeholder="Nguyễn Văn A"
                  style={{
                    background: '#F3F3F5',
                    border: 'none',
                    borderRadius: 8,
                    height: 36,
                  }}
                />
              </Form.Item>

              <Form.Item
                label={
                  <span style={{ fontSize: 14 }}>
                    Email <span style={{ color: '#FB2C36' }}>*</span>
                  </span>
                }
                name="email"
                rules={[
                  { required: true, message: 'Vui lòng nhập email!' },
                  { type: 'email', message: 'Email không hợp lệ!' },
                ]}
                style={{ marginBottom: 16 }}
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
            </Col>
          </Row>

          {/* Checkbox và Button */}
          <div style={{ maxWidth: 465, margin: '0 auto' }}>
            <Form.Item
              name="agreeToTerms"
              valuePropName="checked"
              style={{ marginBottom: 16 }}
              rules={[
                {
                  validator: (_, value) =>
                    value
                      ? Promise.resolve()
                      : Promise.reject(new Error('Vui lòng đồng ý với Điều khoản sử dụng và Chính sách bảo mật!')),
                },
              ]}
            >
              <Checkbox style={{ fontSize: 14 }}>
                <span style={{ color: '#000000' }}>Tôi đồng ý với </span>
                <a
                  href="/terms"
                  style={{
                    color: '#155DFC',
                    textDecoration: 'none',
                  }}
                  onClick={(e) => e.stopPropagation()}
                >
                  Điều khoản sử dụng
                </a>
                <span style={{ color: '#000000' }}> và </span>
                <a
                  href="/privacy"
                  style={{
                    color: '#155DFC',
                    textDecoration: 'none',
                  }}
                  onClick={(e) => e.stopPropagation()}
                >
                  Chính sách bảo mật
                </a>
                <span style={{ color: '#000000' }}> của FinPal</span>
              </Checkbox>
            </Form.Item>

            <Form.Item style={{ marginBottom: 0 }}>
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
                  fontWeight: 500,
                }}
              >
                Đăng ký
              </Button>
            </Form.Item>
          </div>
        </Form>

        {/* Login link */}
        <div style={{ textAlign: 'center', marginTop: 24 }}>
          <span style={{ color: '#4A5565', fontSize: 14 }}>
            Đã có tài khoản?
          </span>
          <a
            href="/login"
            style={{
              color: '#155DFC',
              fontSize: 14,
              textDecoration: 'none',
            }}
          >
            Đăng nhập ngay
          </a>
        </div>
      </div>
    </div>
  );
};

export default RegisterPage;
