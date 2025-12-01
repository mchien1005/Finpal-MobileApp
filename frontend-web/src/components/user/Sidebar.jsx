import {
  UnorderedListOutlined,
  PlusCircleOutlined,
  SettingOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
} from '@ant-design/icons';
import { useNavigate, useLocation } from 'react-router-dom';
import { useSidebar } from '../../contexts/SidebarContext';
import React from 'react';
const Sidebar = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { collapsed, setCollapsed } = useSidebar();



  const menuItems = [
    {
      key: '/dashboard',
      iconName: 'home',
      label: 'Tổng quan',
    },
    {
      key: '/transactions/history',
      icon: <UnorderedListOutlined />,
      label: 'Lịch sử giao dịch',
    },
    {
      key: '/transactions/add',
      icon: <PlusCircleOutlined />,
      label: 'Thêm giao dịch',
    },
    {
      key: '/ai-suggestions',
      iconName: 'ai',
      label: 'AI Gợi ý',
    },
    {
      key: '/savings',
      iconName: 'savemoney',
      label: 'Mục tiêu tiết kiệm',
    },
    {
      key: '/settings',
      icon: <SettingOutlined />,
      label: 'Cài đặt',
    },
  ];

  const handleMenuClick = (key) => {
    navigate(key);
  };

  return (
    <div
      style={{
        width: collapsed ? 80 : 280,
        height: '100vh',
        background: '#155DFC',
        display: 'flex',
        flexDirection: 'column',
        position: 'fixed',
        left: 0,
        top: 0,
        padding: collapsed ? '24px 12px' : '24px 16px',
        transition: 'all 0.3s',
      }}
    >
      {/* Logo Section */}
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: collapsed ? 'center' : 'space-between',
          marginBottom: 16,
          paddingBottom: 16,
          borderBottom: '2px solid rgba(255, 255, 255, 0.2)',
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
                width: 48,
                height: 48,
                borderRadius: 12,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}
            >
              <img
                src="/images/logo-white.svg"
                alt="FinPal"
                style={{ width: 40, height: 40 }}
              />
            </div>
            <div>
              <div style={{ color: '#ffffff', fontSize: 18, fontWeight: 700 }}>
                FinPal
              </div>
              <div style={{ color: 'rgba(255, 255, 255, 0.8)', fontSize: 13 }}>
                Vì Thông Minh
              </div>
            </div>
          </div>
        )}

        {/* Toggle Button */}
        <div
          onClick={() => setCollapsed(!collapsed)}
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            padding: '8px',
            color: '#ffffff',
            cursor: 'pointer',
            borderRadius: 8,
            transition: 'all 0.3s',
          }}
          onMouseEnter={(e) => {
            e.currentTarget.style.background = 'rgba(255, 255, 255, 0.1)';
          }}
          onMouseLeave={(e) => {
            e.currentTarget.style.background = 'transparent';
          }}
        >
          {collapsed ? (
            <MenuUnfoldOutlined style={{ fontSize: 20 }} />
          ) : (
            <MenuFoldOutlined style={{ fontSize: 20 }} />
          )}
        </div>
      </div>

      

      {/* Menu Section */}
      <div style={{ flex: 1 }}>
        {menuItems.map((item) => {
          const isActive = location.pathname === item.key;
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
                height: 48,
                marginBottom: 8,
                color: isActive ? '#2B7FFF' : '#ffffff',
                background: isActive ? '#ffffff' : 'transparent',
                borderRadius: 10,
                cursor: 'pointer',
                fontSize: 15,
                fontWeight: isActive ? 600 : 400,
                transition: 'all 0.3s',
                boxShadow: isActive ? '0 10px 15px -3px rgba(0, 0, 0, 0.10), 0 4px 6px -4px rgba(0, 0, 0, 0.10)' : 'none',
              }}
              onMouseEnter={(e) => {
                if (!isActive) {
                  e.currentTarget.style.background = 'rgba(255, 255, 255, 0.1)';
                  
                }
              }}
              onMouseLeave={(e) => {
                if (!isActive) {
                  e.currentTarget.style.background = 'transparent';
                }
              }}
            >
              <span style={{ fontSize: 18, display: 'inline-flex', alignItems: 'center' }}>
                {item.iconName ? (
                  <img
                    src={isActive ? `/images/${item.iconName}blue.svg` : `/images/${item.iconName}.svg`}
                    alt={item.label}
                    style={{ width: 20, height: 20, display: 'block' }}
                  />
                ) : (
                  item.icon
                )}
              </span>
              {!collapsed && <span>{item.label}</span>}
            </div>
          );
        })}
      </div>


    </div>
  );
};

export default Sidebar;
