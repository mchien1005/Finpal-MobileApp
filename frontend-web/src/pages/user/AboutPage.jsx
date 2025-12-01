import React from 'react';
import { LeftOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import Sidebar from '../../components/user/Sidebar';
import Header from '../../components/common/Header';
import { useSidebar } from '../../contexts/SidebarContext';

const AboutPage = () => {
  const { collapsed } = useSidebar();
  const navigate = useNavigate();

  const features = [
    {
      icon: <img src="/about/1.svg" alt="Tự động hóa thông minh" style={{ width: 48, height: 48 }} />,
      title: 'Tự động hóa thông minh',
      description: 'Đọc SMS và phân loại tự động',
    },
    {
      icon: <img src="/about/2.svg" alt="Bảo mật tối đa" style={{ width: 48, height: 48 }} />,
      title: 'Bảo mật tối đa',
      description: 'Mã hóa AES-256',
    },
    {
      icon: <img src="/about/3.svg" alt="Dễ sử dụng" style={{ width: 48, height: 48 }} />,
      title: 'Dễ sử dụng',
      description: 'Giao diện thân thiện',
    },
    {
      icon: <img src="/about/4.svg" alt="AI thông minh" style={{ width: 48, height: 48 }} />,
      title: 'AI thông minh',
      description: 'Học từ thói quen của bạn',
    },
  ];

  const links = [
    { title: 'Điều khoản sử dụng', url: '#' },
    { title: 'Chính sách bảo mật', url: '#' },
    { title: 'Giấy phép mã nguồn', url: '#' },
  ];

  return (
    <div style={{ display: 'flex', minHeight: '100vh', background: '#F5F7FA' }}>
      <Sidebar />

      {/* Main Content */}
      <div style={{ marginLeft: collapsed ? 80 : 280, flex: 1, transition: 'margin-left 0.3s' }}>
        {/* Header */}
        <Header title="Cài đặt" />

        {/* Content */}
        <div style={{ padding: '0 16px', display: 'flex', flexDirection: 'column', gap: 24 }}>
          {/* Title Banner */}
          <div
            style={{
              background: 'linear-gradient(to right, #155DFC, #4F39F6)',
              borderRadius: '0 0 24px 24px',
              padding: '12px 16px 32px 16px',
              boxShadow: '0 10px 15px -3px rgba(0,0,0,0.1), 0 4px 6px -4px rgba(0,0,0,0.1)',
              position: 'relative',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
            }}
          >
            {/* Back Button */}
            <div
              onClick={() => navigate('/settings')}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: 4,
                cursor: 'pointer',
                padding: '4px 8px',
                borderRadius: 8,
                width: 'fit-content',
                alignSelf: 'flex-start',
              }}
            >
              <LeftOutlined style={{ fontSize: 12, color: '#EFF6FF' }} />
              <span style={{ fontSize: 14, color: '#EFF6FF' }}>Quay lại</span>
            </div>

            {/* Centered Title */}
            <div
              style={{
                position: 'absolute',
                left: 0,
                right: 0,
                top: 12,
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                pointerEvents: 'none',
              }}
            >
              <div style={{ color: '#EFF6FF', fontSize: 18, fontWeight: 600 }}>Giới thiệu</div>
            </div>

            {/* Logo */}
            <div
              style={{
                width: 80,
                height: 80,
                background: '#FFFFFF',
                borderRadius: 20,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                marginTop: 24,
                marginBottom: 16,
                boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)',
              }}
            >
              <img
                src="/images/logo-white.svg"
                alt="FinPal"
                style={{ width: 80, height: 80 }}
                onError={(e) => {
                  e.target.style.display = 'none';
                  e.target.parentElement.innerHTML = '<span style="font-size: 32px;">💰</span>';
                }}
              />
            </div>

            {/* App Name */}
            <div style={{ color: '#FFFFFF', fontSize: 24, fontWeight: 700, marginBottom: 4 }}>
              FinPal
            </div>
            <div style={{ color: 'rgba(255, 255, 255, 0.9)', fontSize: 14, marginBottom: 4 }}>
              Ví Thông Minh
            </div>
            <div style={{ color: 'rgba(255, 255, 255, 0.7)', fontSize: 13 }}>
              Version 1.0.0
            </div>
          </div>

          {/* Mission Section */}
          <div
            style={{
              background: '#FFFFFF',
              borderRadius: 16,
              padding: 24,
              textAlign: 'center',
              boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -2px rgba(0, 0, 0, 0.1)',
            }}
          >
            <div style={{ marginBottom: 12 }}>
              <span style={{ fontSize: 32 }}><img src="/about/heart.svg" alt="Sứ mệnh" style={{ width: 33.33, height: 28.36 }} /></span>
            </div>
            <div style={{ fontSize: 16, fontWeight: 600, color: '#1F2937', marginBottom: 12 }}>
              Sứ mệnh của chúng tôi
            </div>
            <div style={{ fontSize: 14, color: '#6B7280', lineHeight: 1.6 }}>
              Giúp mọi người quản lý tài chính cá nhân một cách dễ dàng, thông minh và an toàn. 
              Chúng tôi tin rằng công nghệ AI có thể giúp bạn tiết kiệm nhiều hơn và đạt được mục tiêu tài chính.
            </div>
          </div>

          {/* Features Section */}
          <div>
            <div style={{ fontSize: 15, fontWeight: 600, color: '#1F2937', marginBottom: 12, paddingLeft: 4 }}>
              Tính năng nổi bật
            </div>
            <div
              style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(2, 1fr)',
                gap: 12,
              }}
            >
              {features.map((feature, index) => (
                <div
                  key={index}
                  style={{
                    background: '#FFFFFF',
                    borderRadius: 14,
                    padding: 20,
                    textAlign: 'center',
                    boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -2px rgba(0, 0, 0, 0.1)',
                  }}
                >
                  <div
                    style={{
                      width: 48,
                      height: 48,
                      background: feature.iconBg,
                      borderRadius: 12,
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      margin: '0 auto 12px auto',
                    }}
                  >
                    <span style={{ fontSize: 24 }}>{feature.icon}</span>
                  </div>
                  <div style={{ fontSize: 14, fontWeight: 600, color: '#1F2937', marginBottom: 4 }}>
                    {feature.title}
                  </div>
                  <div style={{ fontSize: 12, color: '#9CA3AF' }}>
                    {feature.description}
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Links Section */}
          <div
            style={{
              background: '#FFFFFF',
              borderRadius: 14,
              overflow: 'hidden',
              boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -2px rgba(0, 0, 0, 0.1)',
            }}
          >
            {links.map((link, index) => (
              <div
                key={index}
                onClick={() => window.open(link.url, '_blank')}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  padding: '16px 20px',
                  borderBottom: index < links.length - 1 ? '1px solid #F3F4F6' : 'none',
                  cursor: 'pointer',
                  transition: 'background 0.2s',
                }}
                onMouseEnter={(e) => {
                  e.currentTarget.style.background = '#F9FAFB';
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.background = '#FFFFFF';
                }}
              >
                <span style={{ fontSize: 14, color: '#1F2937' }}>{link.title}</span>
                <svg
                  width="16"
                  height="16"
                  viewBox="0 0 16 16"
                  fill="none"
                  xmlns="http://www.w3.org/2000/svg"
                  style={{ color: '#9CA3AF' }}
                >
                  <path
                    d="M4.66667 11.3333L11.3333 4.66667M11.3333 4.66667H4.66667M11.3333 4.66667V11.3333"
                    stroke="currentColor"
                    strokeWidth="1.5"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  />
                </svg>
              </div>
            ))}
          </div>

          {/* Footer */}
          <div style={{ textAlign: 'center', padding: '24px 0 32px 0' }}>
            <div style={{ fontSize: 14, color: '#6B7280', marginBottom: 4 }}>
              Made with <span style={{ color: '#EF4444' }}>❤️</span> in Vietnam
            </div>
            <div style={{ fontSize: 13, color: '#9CA3AF' }}>
              © 2025 FinPal. All rights reserved.
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AboutPage;
