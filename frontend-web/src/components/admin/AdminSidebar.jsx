import React from 'react';
import { useState } from 'react';
import { Avatar, message } from 'antd';
import {
  AppstoreOutlined,
  TeamOutlined,
  TagsOutlined,
  BankOutlined,
  RobotOutlined,
  FileTextOutlined,
  SafetyOutlined,
  SettingOutlined,
  LogoutOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
} from '@ant-design/icons';
import { useNavigate, useLocation } from 'react-router-dom';
import { useSidebar } from '../../contexts/SidebarContext';
import authService from '../../services/authService';
import ConfirmModal from '../../components/common/ConfirmModal';

// Get API base URL for avatar
const getBaseUrl = () => {
  const apiUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';
  return apiUrl.replace(/\/api$/, '');
};
const API_BASE_URL = getBaseUrl();

const AdminSidebar = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { collapsed, setCollapsed } = useSidebar();
  const [showLogoutModal, setShowLogoutModal] = useState(false);

  // Lấy thông tin admin
  const user = authService.getCurrentUser();

  const menuItems = [
    {
      key: '/admin/dashboard',
      icon: <img src="/images/admin/dashboard.svg" alt="Dashboard" style={{ width: 20, height: 20, display: 'block' }}/>,
      label: 'Dashboard & Analytics',
    },
    {
      key: '/admin/users',
      icon: <img src='/images/admin/usermanage.svg' alt="Users" style={{ width: 20, height: 20, display: 'block' }}/>,
      label: 'Quản lý người dùng',
    },
    {
      key: '/admin/categories',
      icon: <img src="/images/admin/category.svg" alt="Categories" style={{ width: 20, height: 20, display: 'block' }}/>,
      label: 'Quản lý danh mục',
    },
    {
      key: '/admin/banks',
      icon: <img src="/images/admin/banksms.svg" alt="Banks" style={{ width: 20, height: 20, display: 'block' }}/>,
      label: 'Ngân hàng & SMS Parser',
    },
    {
      key: '/admin/ai-models',
      icon: <img src="/images/ai.svg" alt="AI Models" style={{ width: 20, height: 20, display: 'block' }}/>,
      label: 'Quản lý AI Model',
    },
    {
      key: '/admin/content',
      icon: <img src="/images/admin/content.svg" alt="Content Management" style={{ width: 20, height: 20, display: 'block' }}/>,
      label: 'Quản lý nội dung',
    },
    {
      key: '/admin/security',
      icon: <img src="/images/admin/protect.svg" alt="Security" style={{ width: 20, height: 20, display: 'block' }}/>,
      label: 'Bảo mật & Audit',
    },
    {
      key: '/admin/settings',
      icon: <SettingOutlined />,
      label: 'Hệ thống & Cấu hình',
    },
  ];

  const handleMenuClick = (key) => {
    navigate(key);
  };

  const handleLogout = () => {
    authService.logout();
    message.success('Đăng xuất thành công');
    navigate('/login');
  };

  return (
    <div
      style={{
        width: collapsed ? 80 : 280,
        height: '100vh',
        background: 'linear-gradient(180deg, #1E293B 0%, #0F172A 100%)',
        display: 'flex',
        flexDirection: 'column',
        position: 'fixed',
        left: 0,
        top: 0,
        padding: collapsed ? '24px 12px' : '24px 16px',
        transition: 'all 0.3s',
        zIndex: 100,
      }}
    >
      {/* Logo Section */}
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: collapsed ? 'center' : 'space-between',
          marginBottom: 32,
          paddingBottom: 24,
          borderBottom: '1px solid rgba(255, 255, 255, 0.1)',
        }}
      >
        {!collapsed && (
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: 12,
            }}
          >
            <div
              style={{
                width: 40,
                height: 40,
                borderRadius: 12,
                background: '#FFFFFF',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)',
              }}
            >
              <img
                src="/images/logo.svg"
                alt="FinPal"
                style={{ width: 40, height: 40 }}
                onError={(e) => {
                  e.target.style.display = 'none';
                  // e.target.parentElement.innerHTML = '<span style="font-size: 18px; font-weight: 700; color: #2B7FFF;">FP</span>';
                }}
              />
            </div>
            <div>
              <div style={{ color: '#FFFFFF', fontSize: 18, fontWeight: 700 }}>
                FinPal Admin
              </div>
              <div style={{ color: 'rgba(255, 255, 255, 0.6)', fontSize: 13 }}>
                Quản trị hệ thống
              </div>
            </div>
          </div>
        )}

        {collapsed && (
          <div
            style={{
              width: 40,
              height: 40,
              borderRadius: 10,
              background: '#FFFFFF',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            {/* <span style={{ fontSize: 14, fontWeight: 700, color: '#2B7FFF' }}>FP</span> */}
            <img src="/images/logo.svg" alt="" style={{ width: 40, height: 40, display: 'block' }} />
          </div>
        )}

        {/* Toggle Button - Only show when not collapsed */}
        {!collapsed && (
          <div
            onClick={() => setCollapsed(!collapsed)}
            style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              padding: '8px',
              color: 'rgba(255, 255, 255, 0.7)',
              cursor: 'pointer',
              borderRadius: 8,
              transition: 'all 0.3s',
            }}
            onMouseEnter={(e) => {
              e.currentTarget.style.background = 'rgba(255, 255, 255, 0.1)';
              e.currentTarget.style.color = '#FFFFFF';
            }}
            onMouseLeave={(e) => {
              e.currentTarget.style.background = 'transparent';
              e.currentTarget.style.color = 'rgba(255, 255, 255, 0.7)';
            }}
          >
            <MenuFoldOutlined style={{ fontSize: 18 }} />
          </div>
        )}
      </div>

      {/* Collapsed Toggle */}
      {collapsed && (
        <div
          onClick={() => setCollapsed(false)}
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            padding: '8px',
            marginBottom: 16,
            color: 'rgba(255, 255, 255, 0.7)',
            cursor: 'pointer',
            borderRadius: 8,
            transition: 'all 0.3s',
          }}
          onMouseEnter={(e) => {
            e.currentTarget.style.background = 'rgba(255, 255, 255, 0.1)';
            e.currentTarget.style.color = '#FFFFFF';
          }}
          onMouseLeave={(e) => {
            e.currentTarget.style.background = 'transparent';
            e.currentTarget.style.color = 'rgba(255, 255, 255, 0.7)';
          }}
        >
          <MenuUnfoldOutlined style={{ fontSize: 18 }} />
        </div>
      )}

      {/* Menu Section */}
      <div style={{ flex: 1, overflowY: 'auto' }}>
        {menuItems.map((item) => {
          const isActive = location.pathname === item.key || location.pathname.startsWith(item.key + '/');
          return (
            <div
              key={item.key}
              onClick={() => handleMenuClick(item.key)}
              title={collapsed ? item.label : ''}
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: collapsed ? 'center' : 'flex-start',
                gap: collapsed ? 0 : 12,
                padding: collapsed ? '12px 8px' : '12px 16px',
                marginBottom: 4,
                color: isActive ? '#FFFFFF' : 'rgba(255, 255, 255, 0.7)',
                background: isActive ? '#4F46E5' : 'transparent',
                borderRadius: 12,
                cursor: 'pointer',
                fontSize: 14,
                fontWeight: isActive ? 500 : 400,
                transition: 'all 0.2s',
              }}
              onMouseEnter={(e) => {
                if (!isActive) {
                  e.currentTarget.style.background = 'rgba(255, 255, 255, 0.08)';
                  e.currentTarget.style.color = '#FFFFFF';
                }
              }}
              onMouseLeave={(e) => {
                if (!isActive) {
                  e.currentTarget.style.background = 'transparent';
                  e.currentTarget.style.color = 'rgba(255, 255, 255, 0.7)';
                }
              }}
            >
              <span style={{ fontSize: 18 }}>{item.icon}</span>
              {!collapsed && <span>{item.label}</span>}
            </div>
          );
        })}
      </div>

      {/* User Profile Section */}
      <div style={{ marginTop: 'auto', paddingTop: 16 }}>
        {/* User Info Card */}
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: collapsed ? 0 : 12,
            padding: collapsed ? '12px 8px' : '12px 16px',
            background: 'rgba(79, 70, 229, 0.3)',
            borderRadius: 12,
            marginBottom: 12,
            justifyContent: collapsed ? 'center' : 'flex-start',
          }}
        >
          {user?.avatarUrl ? (
            <Avatar
              size={collapsed ? 32 : 40}
              src={user.avatarUrl.startsWith('http') ? user.avatarUrl : `${API_BASE_URL}${user.avatarUrl}`}
            />
          ) : (
            <Avatar
              size={collapsed ? 32 : 40}
              style={{
                background: '#4F46E5',
                color: '#FFFFFF',
                fontSize: collapsed ? 14 : 16,
                fontWeight: 600,
              }}
            >
              {user?.fullName?.charAt(0) || user?.username?.charAt(0) || 'A'}
            </Avatar>
          )}
          {!collapsed && (
            <div style={{ flex: 1, minWidth: 0 }}>
              <div
                style={{
                  color: '#FFFFFF',
                  fontSize: 14,
                  fontWeight: 500,
                  overflow: 'hidden',
                  textOverflow: 'ellipsis',
                  whiteSpace: 'nowrap',
                }}
              >
                {user?.fullName || 'Admin User'}
              </div>
              <div
                style={{
                  color: 'rgba(255, 255, 255, 0.6)',
                  fontSize: 12,
                  overflow: 'hidden',
                  textOverflow: 'ellipsis',
                  whiteSpace: 'nowrap',
                }}
              >
                {user?.email || 'admin@finpal.com'}
              </div>
            </div>
          )}
        </div>

        {/* Logout Button */}
        <div
          onClick={() => setShowLogoutModal(true)}
          title={collapsed ? 'Đăng xuất' : ''}
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: collapsed ? 'center' : 'flex-start',
            gap: collapsed ? 0 : 8,
            padding: collapsed ? '12px 8px' : '12px 16px',
            color: '#EF4444',
            cursor: 'pointer',
            borderRadius: 12,
            fontSize: 14,
            fontWeight: 500,
            transition: 'all 0.2s',
          }}
          onMouseEnter={(e) => {
            e.currentTarget.style.background = 'rgba(239, 68, 68, 0.1)';
          }}
          onMouseLeave={(e) => {
            e.currentTarget.style.background = 'transparent';
          }}
        >
          <LogoutOutlined style={{ fontSize: 18 }} />
          {!collapsed && <span>Đăng xuất</span>}
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

export default AdminSidebar;