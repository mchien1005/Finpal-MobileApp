import React, { useState, useEffect } from 'react';
import { message, Spin, Empty, Button, Tabs, Badge } from 'antd';
import {
  BellOutlined,
  CheckOutlined,
  DeleteOutlined,
  ReloadOutlined,
} from '@ant-design/icons';
import AdminSidebar from '../../components/admin/AdminSidebar';
import { useSidebar } from '../../contexts/SidebarContext';
import notificationService from '../../services/notificationService';
import ConfirmModal from '../../components/common/ConfirmModal';

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

// Lấy icon theo loại thông báo
const getNotificationIcon = (type) => {
  const iconStyle = { fontSize: 20 };
  switch (type) {
    case 'BUDGET_ALERT':
      return <span style={{ ...iconStyle, color: '#F59E0B' }}>💰</span>;
    case 'GOAL_REMINDER':
      return <span style={{ ...iconStyle, color: '#10B981' }}>🎯</span>;
    case 'TRANSACTION_ALERT':
      return <span style={{ ...iconStyle, color: '#3B82F6' }}>💳</span>;
    case 'SECURITY_ALERT':
      return <span style={{ ...iconStyle, color: '#EF4444' }}>🔒</span>;
    case 'WEEKLY_REPORT':
    case 'MONTHLY_REPORT':
      return <span style={{ ...iconStyle, color: '#8B5CF6' }}>📊</span>;
    default:
      return <BellOutlined style={{ ...iconStyle, color: '#6B7280' }} />;
  }
};

const NotificationsPage = () => {
  const { collapsed } = useSidebar();
  const [loading, setLoading] = useState(true);
  const [notifications, setNotifications] = useState([]);
  const [activeTab, setActiveTab] = useState('all');
  
  // State cho modal xác nhận xóa
  const [deleteModalOpen, setDeleteModalOpen] = useState(false);
  const [deleteAllModalOpen, setDeleteAllModalOpen] = useState(false);
  const [notificationToDelete, setNotificationToDelete] = useState(null);

  // Load thông báo
  useEffect(() => {
    loadNotifications();
  }, []);

  const loadNotifications = async () => {
    setLoading(true);
    try {
      const data = await notificationService.getNotifications();
      // Xử lý dữ liệu trả về có thể là array hoặc object
      const notifList = Array.isArray(data) ? data : (data.content || data.notifications || []);
      setNotifications(notifList);
    } catch (error) {
      console.error('Lỗi khi tải thông báo:', error);
      message.error('Không thể tải thông báo');
    } finally {
      setLoading(false);
    }
  };

  // Lọc thông báo theo tab
  const getFilteredNotifications = () => {
    switch (activeTab) {
      case 'unread':
        return notifications.filter(n => !n.isRead);
      case 'read':
        return notifications.filter(n => n.isRead);
      default:
        return notifications;
    }
  };

  // Đánh dấu 1 thông báo đã đọc
  const handleMarkAsRead = async (id) => {
    try {
      await notificationService.markAsRead(id);
      setNotifications(prev => prev.map(n => n.id === id ? { ...n, isRead: true } : n));
      message.success('Đã đánh dấu đã đọc');
    } catch {
      message.error('Không thể đánh dấu đã đọc');
    }
  };

  // Đánh dấu tất cả đã đọc
  const handleMarkAllAsRead = async () => {
    try {
      await notificationService.markAllAsRead();
      setNotifications(prev => prev.map(n => ({ ...n, isRead: true })));
      message.success('Đã đánh dấu tất cả đã đọc');
    } catch {
      message.error('Không thể đánh dấu tất cả đã đọc');
    }
  };

  // Mở modal xóa 1 thông báo
  const handleOpenDeleteModal = (notif) => {
    setNotificationToDelete(notif);
    setDeleteModalOpen(true);
  };

  // Xác nhận xóa 1 thông báo
  const handleConfirmDelete = async () => {
    if (!notificationToDelete) return;
    
    try {
      await notificationService.deleteNotification(notificationToDelete.id);
      setNotifications(prev => prev.filter(n => n.id !== notificationToDelete.id));
      message.success('Đã xóa thông báo');
    } catch {
      message.error('Không thể xóa thông báo');
    } finally {
      setDeleteModalOpen(false);
      setNotificationToDelete(null);
    }
  };

  // Mở modal xóa tất cả đã đọc
  const handleOpenDeleteAllModal = () => {
    const readCount = notifications.filter(n => n.isRead).length;
    if (readCount === 0) {
      message.info('Không có thông báo đã đọc để xóa');
      return;
    }
    setDeleteAllModalOpen(true);
  };

  // Xác nhận xóa tất cả đã đọc
  const handleConfirmDeleteAll = async () => {
    try {
      await notificationService.deleteAllRead();
      setNotifications(prev => prev.filter(n => !n.isRead));
      message.success('Đã xóa tất cả thông báo đã đọc');
    } catch {
      message.error('Không thể xóa thông báo');
    } finally {
      setDeleteAllModalOpen(false);
    }
  };

  const filteredNotifications = getFilteredNotifications();
  const unreadCount = notifications.filter(n => !n.isRead).length;

  return (
    <div style={{ display: 'flex', minHeight: '100vh', background: '#F8FAFC' }}>
      <AdminSidebar />
      
      <div
        style={{
          flex: 1,
          marginLeft: collapsed ? 80 : 280,
          padding: 32,
          transition: 'margin-left 0.3s',
        }}
      >
        {/* Header */}
        <div
          style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            marginBottom: 24,
          }}
        >
          <div>
            <h1 style={{ fontSize: 28, fontWeight: 600, color: '#111827', margin: 0 }}>
              Thông báo
            </h1>
            <p style={{ fontSize: 14, color: '#6B7280', margin: '8px 0 0' }}>
              Quản lý tất cả thông báo của bạn
            </p>
          </div>
          
          <div style={{ display: 'flex', gap: 12 }}>
            <Button
              icon={<ReloadOutlined />}
              onClick={loadNotifications}
              loading={loading}
            >
              Làm mới
            </Button>
            {unreadCount > 0 && (
              <Button
                icon={<CheckOutlined />}
                onClick={handleMarkAllAsRead}
              >
                Đánh dấu tất cả đã đọc
              </Button>
            )}
            <Button
              icon={<DeleteOutlined />}
              danger
              onClick={handleOpenDeleteAllModal}
            >
              Xóa đã đọc
            </Button>
          </div>
        </div>

        {/* Tabs */}
        <div
          style={{
            background: '#FFFFFF',
            borderRadius: 16,
            boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
            overflow: 'hidden',
          }}
        >
          <Tabs
            activeKey={activeTab}
            onChange={setActiveTab}
            style={{ padding: '0 24px' }}
            items={[
              {
                key: 'all',
                label: (
                  <span>
                    Tất cả
                    <Badge count={notifications.length} style={{ marginLeft: 8 }} />
                  </span>
                ),
              },
              {
                key: 'unread',
                label: (
                  <span>
                    Chưa đọc
                    <Badge count={unreadCount} style={{ marginLeft: 8, backgroundColor: '#EF4444' }} />
                  </span>
                ),
              },
              {
                key: 'read',
                label: 'Đã đọc',
              },
            ]}
          />

          {/* Notification List */}
          <div style={{ minHeight: 400 }}>
            {loading ? (
              <div style={{ padding: 80, textAlign: 'center' }}>
                <Spin size="large" />
              </div>
            ) : filteredNotifications.length === 0 ? (
              <div style={{ padding: 80, textAlign: 'center' }}>
                <Empty
                  description={
                    activeTab === 'unread'
                      ? 'Không có thông báo chưa đọc'
                      : activeTab === 'read'
                      ? 'Không có thông báo đã đọc'
                      : 'Không có thông báo nào'
                  }
                  image={Empty.PRESENTED_IMAGE_SIMPLE}
                />
              </div>
            ) : (
              <div>
                {filteredNotifications.map((notif) => (
                  <div
                    key={notif.id}
                    style={{
                      padding: '20px 24px',
                      borderBottom: '1px solid #F3F4F6',
                      background: notif.isRead ? '#FFFFFF' : '#F0F9FF',
                      display: 'flex',
                      gap: 16,
                      alignItems: 'flex-start',
                      transition: 'background 0.2s',
                    }}
                    onMouseEnter={(e) => {
                      e.currentTarget.style.background = notif.isRead ? '#F9FAFB' : '#E0F2FE';
                    }}
                    onMouseLeave={(e) => {
                      e.currentTarget.style.background = notif.isRead ? '#FFFFFF' : '#F0F9FF';
                    }}
                  >
                    {/* Icon */}
                    <div
                      style={{
                        width: 48,
                        height: 48,
                        borderRadius: 12,
                        background: notif.isRead ? '#F3F4F6' : '#DBEAFE',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        flexShrink: 0,
                      }}
                    >
                      {getNotificationIcon(notif.type)}
                    </div>

                    {/* Content */}
                    <div style={{ flex: 1, minWidth: 0 }}>
                      <div
                        style={{
                          fontSize: 15,
                          fontWeight: notif.isRead ? 400 : 600,
                          color: '#111827',
                          marginBottom: 4,
                        }}
                      >
                        {notif.title}
                      </div>
                      <div
                        style={{
                          fontSize: 14,
                          color: '#6B7280',
                          lineHeight: 1.5,
                        }}
                      >
                        {notif.content}
                      </div>
                      <div style={{ fontSize: 13, color: '#9CA3AF', marginTop: 8 }}>
                        {formatNotificationTime(notif.createdAt)}
                      </div>
                    </div>

                    {/* Actions */}
                    <div style={{ display: 'flex', gap: 8, flexShrink: 0 }}>
                      {!notif.isRead && (
                        <Button
                          type="text"
                          size="small"
                          icon={<CheckOutlined />}
                          onClick={() => handleMarkAsRead(notif.id)}
                          title="Đánh dấu đã đọc"
                        />
                      )}
                      <Button
                        type="text"
                        size="small"
                        danger
                        icon={<DeleteOutlined />}
                        onClick={() => handleOpenDeleteModal(notif)}
                        title="Xóa"
                      />
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Modal xác nhận xóa 1 thông báo */}
      <ConfirmModal
        open={deleteModalOpen}
        onConfirm={handleConfirmDelete}
        onCancel={() => {
          setDeleteModalOpen(false);
          setNotificationToDelete(null);
        }}
        title="Xác nhận xóa thông báo"
        content={`Bạn có chắc chắn muốn xóa thông báo "${notificationToDelete?.title || ''}"?`}
        confirmText="Xóa"
        cancelText="Hủy"
        danger={true}
      />

      {/* Modal xác nhận xóa tất cả đã đọc */}
      <ConfirmModal
        open={deleteAllModalOpen}
        onConfirm={handleConfirmDeleteAll}
        onCancel={() => setDeleteAllModalOpen(false)}
        title="Xác nhận xóa thông báo đã đọc"
        content={`Bạn có chắc chắn muốn xóa ${notifications.filter(n => n.isRead).length} thông báo đã đọc? Hành động này không thể hoàn tác.`}
        confirmText="Xóa tất cả"
        cancelText="Hủy"
        danger={true}
      />
    </div>
  );
};

export default NotificationsPage;
