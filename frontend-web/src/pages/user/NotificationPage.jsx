import { useState, useEffect } from 'react';
import { Typography, message, Spin } from 'antd';
import {
  CheckOutlined,
  DeleteOutlined,
  TransactionOutlined,
  WarningOutlined,
  TrophyOutlined,
  DollarOutlined,
  MobileOutlined,
  LoadingOutlined,
  FileTextOutlined,
  BulbOutlined,
  AlertOutlined,
} from '@ant-design/icons';
import Sidebar from '../../components/user/Sidebar';
import Header from '../../components/common/Header';
import ConfirmModal from '../../components/common/ConfirmModal';
import { useSidebar } from '../../contexts/SidebarContext';
import {
  getNotifications,
  markAsRead,
  markAllAsRead,
  deleteNotification,
  deleteAllRead,
} from '../../services/notificationService';

const { Text } = Typography;

const NotificationPage = () => {
  const { collapsed } = useSidebar();
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showConfirmModal, setShowConfirmModal] = useState(false);

  // Fetch notifications từ API
  const fetchNotifications = async () => {
    setLoading(true);
    try {
      const data = await getNotifications();
      setNotifications(data || []);
    } catch (error) {
      console.error('Error fetching notifications:', error);
      message.error('Không thể tải thông báo');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchNotifications();
  }, []);

  // Lấy icon theo loại thông báo
  const getNotificationIcon = (type) => {
    const iconStyle = {
      fontSize: 24,
      padding: 12,
      borderRadius: 12,
    };

    switch (type) {
      case 'BUDGET_ALERT':
        return (
          <div style={{ ...iconStyle, background: '#FEF3C7', color: '#F59E0B' }}>
            <WarningOutlined />
          </div>
        );
      case 'SAVING_TIP':
        return (
          <div style={{ ...iconStyle, background: '#D1FAE5', color: '#10B981' }}>
            <BulbOutlined />
          </div>
        );
      case 'ANOMALY':
        return (
          <div style={{ ...iconStyle, background: '#FEE2E2', color: '#EF4444' }}>
            <AlertOutlined />
          </div>
        );
      case 'REPORT':
        return (
          <div style={{ ...iconStyle, background: '#DBEAFE', color: '#3B82F6' }}>
            <FileTextOutlined />
          </div>
        );
      case 'GOAL_REMINDER':
        return (
          <div style={{ ...iconStyle, background: '#D1FAE5', color: '#10B981' }}>
            <TrophyOutlined />
          </div>
        );
      case 'TRANSACTION':
        return (
          <div style={{ ...iconStyle, background: '#FEE2E2', color: '#EF4444' }}>
            <TransactionOutlined />
          </div>
        );
      case 'INCOME':
        return (
          <div style={{ ...iconStyle, background: '#DBEAFE', color: '#3B82F6' }}>
            <DollarOutlined />
          </div>
        );
      case 'LOGIN':
        return (
          <div style={{ ...iconStyle, background: '#E5E7EB', color: '#6B7280' }}>
            <MobileOutlined />
          </div>
        );
      default:
        return (
          <div style={{ ...iconStyle, background: '#E5E7EB', color: '#6B7280' }}>
            <TransactionOutlined />
          </div>
        );
    }
  };

  // Format thời gian
  const formatTime = (dateString) => {
    const date = new Date(dateString);
    const now = new Date();
    const diff = now - date;
    
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);
    const days = Math.floor(diff / 86400000);

    if (minutes < 1) return 'Vừa xong';
    if (minutes < 60) return `${minutes} phút trước`;
    if (hours < 24) return `${hours} giờ trước`;
    if (days < 7) return `${days} ngày trước`;
    
    return date.toLocaleDateString('vi-VN');
  };

  // Đánh dấu đã đọc
  const handleMarkAsRead = async (id) => {
    try {
      await markAsRead(id);
      setNotifications(prev =>
        prev.map(n => (n.id === id ? { ...n, isRead: true } : n))
      );
    } catch (error) {
      console.error('Error marking as read:', error);
      message.error('Không thể đánh dấu đã đọc');
    }
  };

  // Đánh dấu tất cả đã đọc
  const handleMarkAllAsRead = async () => {
    try {
      await markAllAsRead();
      setNotifications(prev => prev.map(n => ({ ...n, isRead: true })));
      message.success('Đã đánh dấu tất cả là đã đọc');
    } catch (error) {
      console.error('Error marking all as read:', error);
      message.error('Không thể đánh dấu tất cả đã đọc');
    }
  };

  // Xóa thông báo
  const handleDeleteNotification = async (id) => {
    try {
      await deleteNotification(id);
      setNotifications(prev => prev.filter(n => n.id !== id));
      message.success('Đã xóa thông báo');
    } catch (error) {
      console.error('Error deleting notification:', error);
      message.error('Không thể xóa thông báo');
    }
  };

  // Xóa tất cả đã đọc
  const handleDeleteAllRead = async () => {
    try {
      await deleteAllRead();
      setNotifications(prev => prev.filter(n => !n.isRead));
      setShowConfirmModal(false);
      message.success('Đã xóa tất cả thông báo đã đọc');
    } catch (error) {
      console.error('Error deleting all read:', error);
      message.error('Không thể xóa thông báo');
    }
  };

  // Phân loại thông báo
  const unreadNotifications = notifications.filter(n => !n.isRead);
  const readNotifications = notifications.filter(n => n.isRead);

  // Render notification item
  const renderNotificationItem = (notification) => (
    <div
      key={notification.id}
      style={{
        display: 'flex',
        alignItems: 'flex-start',
        padding: 16,
        background: '#FFFFFF',
        borderRadius: 12,
        marginBottom: 12,
        boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.10), 0 2px 4px -2px rgba(0, 0, 0, 0.10)',
        gap: 16,
      }}
    >
      {/* Icon */}
      {getNotificationIcon(notification.type)}

      {/* Content */}
      <div style={{ flex: 1 }}>
        <div style={{ fontSize: 15, fontWeight: 600, color: '#1F2937', marginBottom: 4 }}>
          {notification.title}
        </div>
        <div style={{ fontSize: 14, color: '#6B7280', marginBottom: 4 }}>
          {notification.content}
        </div>
        <div style={{ fontSize: 13, color: '#9CA3AF' }}>
          {formatTime(notification.createdAt)}
        </div>
      </div>

      {/* Actions */}
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: 8 }}>
        <DeleteOutlined
          style={{ fontSize: 16, color: '#9CA3AF', cursor: 'pointer' }}
          onClick={() => handleDeleteNotification(notification.id)}
        />
        {!notification.isRead && (
          <Text
            style={{ fontSize: 13, color: '#2B7FFF', cursor: 'pointer' }}
            onClick={() => handleMarkAsRead(notification.id)}
          >
            Đánh dấu đã đọc
          </Text>
        )}
      </div>
    </div>
  );

  return (
    <div style={{ display: 'flex', minHeight: '100vh', background: '#F5F7FA' }}>
      <Sidebar />

      {/* Main Content */}
      <div style={{ marginLeft: collapsed ? 80 : 280, flex: 1, transition: 'margin-left 0.3s' }}>
        {/* Header */}
        <Header title="Thông báo" />

        {/* Action Bar */}
        <div
          style={{
            background: '#155DFC',
                      padding: '25px 32px',
                      marginRight: 30,
            marginLeft: 30,
            display: 'flex',
            justifyContent: 'flex-end',
            gap: 24,
            borderRadius: '0 0 24px 24px',
          }}
        >
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: 8,
              color: '#FFFFFF',
              cursor: 'pointer',
              fontSize: 14,
            }}
            onClick={handleMarkAllAsRead}
          >
            <CheckOutlined />
            <span>Đánh dấu tất cả đã đọc</span>
          </div>
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: 8,
              color: '#FFFFFF',
              cursor: 'pointer',
              fontSize: 14,
            }}
            onClick={() => setShowConfirmModal(true)}
          >
            <DeleteOutlined />
            <span>Xóa đã đọc</span>
          </div>
        </div>

        {/* Content */}
        <div style={{ padding: 32 }}>
          {loading ? (
            <div style={{ textAlign: 'center', padding: 48 }}>
              <Spin indicator={<LoadingOutlined style={{ fontSize: 48 }} spin />} />
            </div>
          ) : (
            <>
              {/* Chưa đọc */}
              {unreadNotifications.length > 0 && (
                <div style={{ marginBottom: 32 }}>
                  <Text
                    style={{
                      fontSize: 14,
                      fontWeight: 500,
                      color: '#6B7280',
                      display: 'block',
                      marginBottom: 16,
                    }}
                  >
                    Chưa đọc
                  </Text>
                  {unreadNotifications.map(renderNotificationItem)}
                </div>
              )}

              {/* Đã đọc */}
              {readNotifications.length > 0 && (
                <div>
                  <Text
                    style={{
                      fontSize: 14,
                      fontWeight: 500,
                      color: '#6B7280',
                      display: 'block',
                      marginBottom: 16,
                    }}
                  >
                    Đã đọc
                  </Text>
                  {readNotifications.map(renderNotificationItem)}
                </div>
              )}

              {/* Empty state */}
              {notifications.length === 0 && (
                <div
                  style={{
                    textAlign: 'center',
                    padding: 48,
                    color: '#9CA3AF',
                  }}
                >
                  <div style={{ fontSize: 48, marginBottom: 16 }}>🔔</div>
                  <div style={{ fontSize: 16 }}>Không có thông báo nào</div>
                </div>
              )}
            </>
          )}
        </div>
      </div>

      {/* Confirm Delete Modal */}
      <ConfirmModal
        open={showConfirmModal}
        onConfirm={handleDeleteAllRead}
        onCancel={() => setShowConfirmModal(false)}
        title="Xóa tất cả thông báo đã đọc"
        content="Bạn có chắc chắn muốn xóa tất cả thông báo đã đọc? Hành động này không thể hoàn tác."
        confirmText="Xóa tất cả"
        cancelText="Hủy"
        danger={true}
      />
    </div>
  );
};

export default NotificationPage;
