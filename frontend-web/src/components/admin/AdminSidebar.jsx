import React, { useState, useEffect } from 'react';
import { Avatar, message, Badge, Dropdown, Spin, Empty } from 'antd';
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
  BellOutlined,
  CheckOutlined,
  DeleteOutlined,
} from '@ant-design/icons';
import { useNavigate, useLocation } from 'react-router-dom';
import { useSidebar } from '../../contexts/SidebarContext';
import authService from '../../services/authService';
import notificationService from '../../services/notificationService';
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
  
  // State cho thông báo
  const [unreadCount, setUnreadCount] = useState(0);
  const [notifications, setNotifications] = useState([]);
  const [loadingNotifications, setLoadingNotifications] = useState(false);
  const [notificationDropdownOpen, setNotificationDropdownOpen] = useState(false);

  // Lấy thông tin admin
  const user = authService.getCurrentUser();

  // Load số lượng thông báo chưa đọc khi component mount
  useEffect(() => {
    loadUnreadCount();
    // Refresh mỗi 30 giây
    const interval = setInterval(loadUnreadCount, 30000);
    return () => clearInterval(interval);
  }, []);

  // Hàm load số lượng thông báo chưa đọc
  const loadUnreadCount = async () => {
    try {
      const data = await notificationService.getUnreadCount();
      setUnreadCount(data.count || data.unreadCount || 0);
    } catch (error) {
      console.error('Lỗi khi tải số thông báo:', error);
    }
  };

  // Hàm load danh sách thông báo
  const loadNotifications = async () => {
    setLoadingNotifications(true);
    try {
      const data = await notificationService.getNotifications();
      // Xử lý dữ liệu trả về có thể là array hoặc object
      const notifList = Array.isArray(data) ? data : (data.content || data.notifications || []);
      setNotifications(notifList.slice(0, 10)); // Chỉ lấy 10 thông báo mới nhất
    } catch (error) {
      console.error('Lỗi khi tải thông báo:', error);
    } finally {
      setLoadingNotifications(false);
    }
  };

  // Xử lý khi mở dropdown thông báo
  const handleNotificationDropdownChange = (open) => {
    setNotificationDropdownOpen(open);
    if (open) {
      loadNotifications();
    }
  };

  // Đánh dấu thông báo đã đọc
  const handleMarkAsRead = async (notifId, e) => {
    e.stopPropagation();
    try {
      await notificationService.markAsRead(notifId);
      setNotifications(prev => prev.map(n => n.id === notifId ? { ...n, isRead: true } : n));
      setUnreadCount(prev => Math.max(0, prev - 1));
    } catch {
      message.error('Không thể đánh dấu đã đọc');
    }
  };

  // Đánh dấu tất cả đã đọc
  const handleMarkAllAsRead = async () => {
    try {
      await notificationService.markAllAsRead();
      setNotifications(prev => prev.map(n => ({ ...n, isRead: true })));
      setUnreadCount(0);
      message.success('Đã đánh dấu tất cả đã đọc');
    } catch {
      message.error('Không thể đánh dấu tất cả đã đọc');
    }
  };

  // Format thời gian thông báo
  const formatNotificationTime = (dateString) => {
    if (!dateString) return '';
    
    let date;
    // Xử lý các định dạng thời gian khác nhau từ API
    if (typeof dateString === 'number') {
      // Timestamp (milliseconds hoặc seconds)
      date = new Date(dateString > 9999999999 ? dateString : dateString * 1000);
    } else if (typeof dateString === 'string') {
      // Nếu là ISO string không có timezone (VD: "2025-12-26T09:20:00")
      // Server trả về UTC nhưng không có 'Z', cần thêm 'Z' để parse đúng
      if (dateString.includes('T') && !dateString.includes('Z') && !dateString.includes('+') && !/\d{2}-\d{2}$/.test(dateString)) {
        // Thêm 'Z' để JavaScript parse như UTC, sau đó tự động convert sang local time
        date = new Date(dateString + 'Z');
      } else {
        date = new Date(dateString);
      }
    } else {
      date = new Date(dateString);
    }
    
    // Kiểm tra date có hợp lệ không
    if (isNaN(date.getTime())) return '';
    
    const now = new Date();
    const diffMs = now - date;
    
    // Nếu thời gian âm (thông báo trong tương lai), hiển thị như vừa xong
    if (diffMs < 0) return 'Vừa xong';
    
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);
    
    if (diffMins < 1) return 'Vừa xong';
    if (diffMins < 60) return `${diffMins} phút trước`;
    if (diffHours < 24) return `${diffHours} giờ trước`;
    if (diffDays < 7) return `${diffDays} ngày trước`;
    return date.toLocaleDateString('vi-VN') + ' ' + date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
  };

  // Render dropdown content cho thông báo
  const notificationDropdownContent = (
    <div
      style={{
        width: 360,
        maxHeight: 480,
        background: '#FFFFFF',
        borderRadius: 12,
        boxShadow: '0 10px 40px rgba(0, 0, 0, 0.15)',
        overflow: 'hidden',
      }}
    >
      {/* Header */}
      <div
        style={{
          padding: '16px 20px',
          borderBottom: '1px solid #E5E7EB',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
        }}
      >
        <span style={{ fontSize: 16, fontWeight: 600, color: '#111827' }}>
          Thông báo
        </span>
        {unreadCount > 0 && (
          <span
            onClick={handleMarkAllAsRead}
            style={{
              fontSize: 13,
              color: '#4F46E5',
              cursor: 'pointer',
            }}
          >
            Đánh dấu tất cả đã đọc
          </span>
        )}
      </div>

      {/* Notification List */}
      <div style={{ maxHeight: 380, overflowY: 'auto' }}>
        {loadingNotifications ? (
          <div style={{ padding: 40, textAlign: 'center' }}>
            <Spin />
          </div>
        ) : notifications.length === 0 ? (
          <div style={{ padding: 40, textAlign: 'center' }}>
            <Empty description="Không có thông báo" image={Empty.PRESENTED_IMAGE_SIMPLE} />
          </div>
        ) : (
          notifications.map((notif) => (
            <div
              key={notif.id}
              style={{
                padding: '12px 20px',
                borderBottom: '1px solid #F3F4F6',
                background: notif.isRead ? '#FFFFFF' : '#ebf7ffff',
                cursor: 'pointer',
                transition: 'background 0.2s',
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.background = notif.isRead ? '#F9FAFB' : '#E0F2FE';
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.background = notif.isRead ? '#FFFFFF' : '#F0F9FF';
              }}
            >
              <div style={{ display: 'flex', gap: 12, alignItems: 'flex-start' }}>
                {/* Icon theo loại thông báo */}
                <div
                  style={{
                    width: 36,
                    height: 36,
                    borderRadius: 8,
                    background: notif.isRead ? '#F3F4F6' : '#e0efffff',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    flexShrink: 0,
                  }}
                >
                  <BellOutlined style={{ fontSize: 16, color: notif.isRead ? '#6B7280' : '#3B82F6' }} />
                </div>

                {/* Content */}
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div
                    style={{
                      fontSize: 14,
                      fontWeight: notif.isRead ? 400 : 500,
                      color: '#111827',
                      marginBottom: 4,
                      overflow: 'hidden',
                      textOverflow: 'ellipsis',
                      whiteSpace: 'nowrap',
                    }}
                  >
                    {notif.title}
                  </div>
                  <div
                    style={{
                      fontSize: 13,
                      color: '#6B7280',
                      overflow: 'hidden',
                      textOverflow: 'ellipsis',
                      display: '-webkit-box',
                      WebkitLineClamp: 2,
                      WebkitBoxOrient: 'vertical',
                    }}
                  >
                    {notif.content}
                  </div>
                  <div style={{ fontSize: 12, color: '#9CA3AF', marginTop: 4 }}>
                    {formatNotificationTime(notif.createdAt)}
                  </div>
                </div>

                {/* Actions */}
                {!notif.isRead && (
                  <div
                    onClick={(e) => handleMarkAsRead(notif.id, e)}
                    title="Đánh dấu đã đọc"
                    style={{
                      width: 28,
                      height: 28,
                      borderRadius: 6,
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      cursor: 'pointer',
                      color: '#6B7280',
                      transition: 'all 0.2s',
                    }}
                    onMouseEnter={(e) => {
                      e.currentTarget.style.background = '#E5E7EB';
                      e.currentTarget.style.color = '#374151';
                    }}
                    onMouseLeave={(e) => {
                      e.currentTarget.style.background = 'transparent';
                      e.currentTarget.style.color = '#6B7280';
                    }}
                  >
                    <CheckOutlined style={{ fontSize: 14 }} />
                  </div>
                )}
              </div>
            </div>
          ))
        )}
      </div>

      {/* Footer */}
      {notifications.length > 0 && (
        <div
          style={{
            padding: '12px 20px',
            borderTop: '1px solid #E5E7EB',
            textAlign: 'center',
          }}
        >
          <span
            onClick={() => {
              setNotificationDropdownOpen(false);
              navigate('/admin/notifications');
            }}
            style={{
              fontSize: 14,
              color: '#4F46E5',
              cursor: 'pointer',
              fontWeight: 500,
            }}
          >
            Xem tất cả thông báo
          </span>
        </div>
      )}
    </div>
  );

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

        {/* Toggle Button and Notification - Only show when not collapsed */}
        {!collapsed && (
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            {/* Notification Bell */}
            <Dropdown
              dropdownRender={() => notificationDropdownContent}
              trigger={['click']}
              placement="bottomRight"
              open={notificationDropdownOpen}
              onOpenChange={handleNotificationDropdownChange}
            >
              <div
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  padding: '8px',
                  color: 'rgba(255, 255, 255, 0.7)',
                  cursor: 'pointer',
                  borderRadius: 8,
                  transition: 'all 0.3s',
                  position: 'relative',
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
                <Badge count={unreadCount} size="small" offset={[-2, 2]}>
                  <BellOutlined style={{ fontSize: 20, color: 'rgba(255, 255, 255, 0.7)' }} />
                </Badge>
              </div>
            </Dropdown>

            {/* Toggle Button */}
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
          </div>
        )}
      </div>

      {/* Collapsed Toggle and Notification */}
      {collapsed && (
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 8, marginBottom: 16 }}>
          {/* Notification Bell - Collapsed */}
          <Dropdown
            dropdownRender={() => notificationDropdownContent}
            trigger={['click']}
            placement="rightTop"
            open={notificationDropdownOpen}
            onOpenChange={handleNotificationDropdownChange}
          >
            <div
              title="Thông báo"
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
              <Badge count={unreadCount} size="small" offset={[-2, 2]}>
                <BellOutlined style={{ fontSize: 18, color: 'rgba(255, 255, 255, 0.7)' }} />
              </Badge>
            </div>
          </Dropdown>

          {/* Unfold Button */}
          <div
            onClick={() => setCollapsed(false)}
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
            <MenuUnfoldOutlined style={{ fontSize: 18 }} />
          </div>
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