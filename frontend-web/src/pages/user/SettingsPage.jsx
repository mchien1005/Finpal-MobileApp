import React, { useState, useEffect } from 'react';
import { Typography, Avatar, message } from 'antd';
import {
  UserOutlined,
  BellOutlined,
  LockOutlined,
  QuestionCircleOutlined,
  InfoCircleOutlined,
  LogoutOutlined,
  RightOutlined,
  MailOutlined,
  CameraOutlined,
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import Sidebar from '../../components/user/Sidebar';
import Header from '../../components/common/Header';
import ConfirmModal from '../../components/common/ConfirmModal';
import { useSidebar } from '../../contexts/SidebarContext';
import authService from '../../services/authService';
import userService from '../../services/userService';

// Get API base URL for avatar (remove /api suffix if present)
const getBaseUrl = () => {
  const apiUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';
  return apiUrl.replace(/\/api$/, '');
};
const API_BASE_URL = getBaseUrl();

const { Text } = Typography;

const SettingsPage = () => {
  const { collapsed } = useSidebar();
  const navigate = useNavigate();
  const [showLogoutModal, setShowLogoutModal] = useState(false);
  const [avatarUrl, setAvatarUrl] = useState('');
  
  // Lấy thông tin user
  const user = authService.getCurrentUser();

  // Fetch avatar từ profile
  useEffect(() => {
    const fetchProfile = async () => {
      try {
        // Kiểm tra localStorage trước
        const storedUser = authService.getCurrentUser();
        if (storedUser?.avatarUrl) {
          setAvatarUrl(storedUser.avatarUrl);
          return;
        }
        // Nếu không có thì fetch từ API
        const response = await userService.getProfile();
        if (response.success && response.data?.avatarUrl) {
          setAvatarUrl(response.data.avatarUrl);
        }
      } catch (error) {
        console.error('Error fetching profile:', error);
      }
    };

    fetchProfile();
  }, []);

  const settingsItems = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      title: 'Hồ sơ',
      description: 'Quản lý thông tin cá nhân',
      onClick: () => navigate('/settings/profile'),
    },
    {
      key: 'notifications',
      icon: <BellOutlined />,
      title: 'Cài đặt thông báo',
      description: 'Tùy chỉnh thông báo của bạn',
      onClick: () => navigate('/settings/notifications'),
    },
    {
      key: 'password',
      icon: <LockOutlined />,
      title: 'Thiết lập mật khẩu',
      description: 'Thay đổi mật khẩu bảo mật',
      onClick: () => navigate('/settings/password'),
    },
    {
      key: 'help',
      icon: <QuestionCircleOutlined />,
      title: 'Trung tâm trợ giúp',
      description: 'Câu hỏi thường gặp & hỗ trợ',
      onClick: () => navigate('/settings/help'),
    },
    {
      key: 'about',
      icon: <InfoCircleOutlined />,
      title: 'Giới thiệu',
      description: 'Về FinPal v1.0.0',
      onClick: () => navigate('/settings/about'),
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      title: 'Đăng xuất',
      description: 'Thoát khỏi tài khoản',
      onClick: () => setShowLogoutModal(true),
    },
  ];

  const handleLogout = () => {
    authService.logout();
    message.success('Đăng xuất thành công');
    navigate('/login');
  };

  return (
    <div style={{ display: 'flex', minHeight: '100vh', background: '#F5F7FA' }}>
      <Sidebar />

      {/* Main Content */}
      <div style={{ marginLeft: collapsed ? 80 : 280, flex: 1, transition: 'margin-left 0.3s' }}>
        {/* Header */}
        <Header title="Cài đặt" />

        {/* Content */}
        <div style={{ padding: 32 }}>
          {/* User Profile Card */}
          <div
            style={{
              background: '#155DFC',
              borderRadius: 20,
              padding: '28px 32px',
              marginBottom: 24,
              display: 'flex',
              alignItems: 'center',
              gap: 16,
            }}
          >
            <div style={{ position: 'relative' }}>
              {avatarUrl ? (
                <Avatar
                  size={56}
                  src={avatarUrl.startsWith('http') ? avatarUrl : `${API_BASE_URL}${avatarUrl}`}
                  style={{
                    border: '2px solid rgba(255,255,255,0.3)',
                  }}
                />
              ) : (
                <Avatar
                  size={56}
                  style={{
                    background: '#6B7CC9',
                    color: '#FFFFFF',
                    fontSize: 22,
                    fontWeight: 600,
                    border: '2px solid rgba(255,255,255,0.3)',
                  }}
                >
                  {user?.fullName?.charAt(0) || user?.username?.charAt(0) || 'N'}
                </Avatar>
              )}
              <div
                onClick={() => navigate('/settings/profile')}
                style={{
                  position: 'absolute',
                  bottom: -2,
                  right: -2,
                  width: 22,
                  height: 22,
                  background: 'rgba(255,255,255,0.3)',
                  borderRadius: '50%',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  cursor: 'pointer',
                }}
              >
                <CameraOutlined style={{ fontSize: 11, color: '#FFFFFF' }} />
              </div>
            </div>
            <div>
              <div style={{ color: '#FFFFFF', fontSize: 18, fontWeight: 600, marginBottom: 4 }}>
                {user?.fullName || user?.username || 'Nguyễn Văn A'}
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: 6, color: 'rgba(255,255,255,0.85)' }}>
                <MailOutlined style={{ fontSize: 13 }} />
                <span style={{ fontSize: 13 }}>{user?.email || 'demo@finpal.com'}</span>
              </div>
            </div>
          </div>

          {/* Settings Items */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
            {settingsItems.map((item) => (
              <div
                key={item.key}
                onClick={item.onClick}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  padding: 16,
                  background: '#FFFFFF',
                  borderRadius: 12,
                  boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.10), 0 2px 4px -2px rgba(0, 0, 0, 0.10)',
                  cursor: 'pointer',
                  transition: 'background 0.2s',
                  gap: 16,
                }}
                onMouseEnter={(e) => {
                  e.currentTarget.style.background = '#F9FAFB';
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.background = '#FFFFFF';
                }}
              >
                {/* Icon */}
                <div
                  style={{
                    width: 48,
                    height: 48,
                    borderRadius: 12,
                    background: '#FFFFFF',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                  }}
                >
                  <span style={{ fontSize: 20, color: '#000000ff' }}>{item.icon}</span>
                </div>

                {/* Content */}
                <div style={{ flex: 1 }}>
                  <div style={{ fontSize: 15, fontWeight: 600, color: '#1F2937', marginBottom: 4 }}>
                    {item.title}
                  </div>
                  <Text type="secondary" style={{ fontSize: 13 }}>
                    {item.description}
                  </Text>
                </div>

                {/* Arrow */}
                <RightOutlined style={{ fontSize: 14, color: '#9CA3AF' }} />
              </div>
            ))}
          </div>

          {/* Footer */}
          <div style={{ textAlign: 'center', marginTop: 32, color: '#9CA3AF', fontSize: 14 }}>
            FinPal - Ví Thông Minh
          </div>
        </div>
      </div>

      {/* Logout Confirm Modal */}
      <ConfirmModal
        open={showLogoutModal}
        onConfirm={handleLogout}
        onCancel={() => setShowLogoutModal(false)}
        title="Xác nhận đăng xuất"
        content="Bạn có chắc chắn muốn đăng xuất khỏi tài khoản? Bạn sẽ cần đăng nhập lại để tiếp tục sử dụng FinPal."
        confirmText="Đăng xuất"
        cancelText="Hủy"
        danger={true}
      />
    </div>
  );
};

export default SettingsPage;
