import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Switch, message, Spin } from 'antd';
import { LeftOutlined } from '@ant-design/icons';
import Sidebar from '../../components/user/Sidebar';
import Header from '../../components/common/Header';
import { useSidebar } from '../../contexts/SidebarContext';
import { getUserPreferences, updateNotificationSettings } from '../../services/userPreferencesService';

// Icons from Figma
const iconPushNotification = 'https://www.figma.com/api/mcp/asset/829cd787-0107-4222-833d-bda7070de407';
const iconTransaction = 'https://www.figma.com/api/mcp/asset/bfedf120-ac2f-4a80-8a4c-20e92f9332f0';
const iconBudget = 'https://www.figma.com/api/mcp/asset/22d17c35-fd98-49db-af2c-64723cc26750';
const iconGoal = 'https://www.figma.com/api/mcp/asset/a4f66280-2241-4f5a-86be-ea7b62604234';
const iconWeeklyReport = 'https://www.figma.com/api/mcp/asset/071733e3-5bc3-4156-ae95-9ed2638dfba8';
const iconMonthlyReport = 'https://www.figma.com/api/mcp/asset/6bcfcbd8-09e9-489b-9a3c-677589d0285f';

const NotificationSettingsPage = () => {
  const { collapsed } = useSidebar();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [initialLoading, setInitialLoading] = useState(true);
  
  // Notification settings state
  const [settings, setSettings] = useState({
    pushNotification: false,
    transactionAlert: true,
    budgetAlert: true,
    goalReminder: true,
    weeklyReport: false,
    monthlyReport: true,
  });

  // Kiểm tra trình duyệt có hỗ trợ Notification không
  const isNotificationSupported = () => {
    return 'Notification' in window;
  };

  // Load settings từ API
  useEffect(() => {
    const loadSettings = async () => {
      try {
        setInitialLoading(true);
        const response = await getUserPreferences();
        if (response.success && response.data) {
          const data = response.data;
          // Map API response to local state
          setSettings({
            pushNotification: isNotificationSupported() && 
                             Notification.permission === 'granted' && 
                             (data.pushNotifications ?? true),
            transactionAlert: data.transactionAlert ?? true,
            budgetAlert: data.budgetAlert ?? true,
            goalReminder: data.goalReminder ?? true,
            weeklyReport: data.weeklyReport ?? false,
            monthlyReport: data.monthlyReport ?? true,
          });
        }
      } catch (error) {
        console.error('Error loading preferences:', error);
        // Fallback to localStorage if API fails
        const savedSettings = localStorage.getItem('notificationSettings');
        if (savedSettings) {
          const parsed = JSON.parse(savedSettings);
          if (Notification.permission !== 'granted') {
            parsed.pushNotification = false;
          }
          setSettings(parsed);
        }
      } finally {
        setInitialLoading(false);
      }
    };

    loadSettings();
  }, []);

  // Gửi thông báo test
  const sendTestNotification = () => {
    if (Notification.permission === 'granted') {
      const notification = new Notification('FinPal - Thông báo', {
        body: 'Thông báo đẩy đã được bật thành công! 🎉',
        icon: '/favicon.ico',
        badge: '/favicon.ico',
        tag: 'test-notification',
        requireInteraction: false,
      });

      notification.onclick = () => {
        window.focus();
        notification.close();
      };

      // Tự động đóng sau 5 giây
      setTimeout(() => notification.close(), 5000);
    }
  };

  // Xử lý bật/tắt Push Notification
  const handlePushNotificationToggle = async () => {
    if (!isNotificationSupported()) {
      message.error('Trình duyệt của bạn không hỗ trợ thông báo đẩy!');
      return;
    }

    if (settings.pushNotification) {
      // Tắt thông báo
      setSettings(prev => ({ ...prev, pushNotification: false }));
      message.info('Đã tắt thông báo đẩy');
    } else {
      // Bật thông báo - yêu cầu quyền
      if (Notification.permission === 'denied') {
        message.error('Bạn đã chặn thông báo. Vui lòng vào cài đặt trình duyệt để bật lại.');
        return;
      }

      if (Notification.permission === 'default') {
        try {
          const permission = await Notification.requestPermission();
          
          if (permission === 'granted') {
            setSettings(prev => ({ ...prev, pushNotification: true }));
            message.success('Đã bật thông báo đẩy!');
            // Gửi thông báo test
            sendTestNotification();
          } else if (permission === 'denied') {
            message.warning('Bạn đã từ chối quyền thông báo');
          }
        } catch (error) {
          console.error('Error requesting notification permission:', error);
          message.error('Không thể yêu cầu quyền thông báo');
        }
      } else if (Notification.permission === 'granted') {
        setSettings(prev => ({ ...prev, pushNotification: true }));
        message.success('Đã bật thông báo đẩy!');
        sendTestNotification();
      }
    }
  };

  // Lưu hàm sendNotification vào window để có thể gọi từ nơi khác trong app
  useEffect(() => {
    window.sendFinPalNotification = (title, options = {}) => {
      if (Notification.permission === 'granted' && settings.pushNotification) {
        const notification = new Notification(title, {
          icon: '/favicon.ico',
          badge: '/favicon.ico',
          ...options,
        });

        notification.onclick = () => {
          window.focus();
          notification.close();
        };

        return notification;
      }
      return null;
    };
    
    return () => {
      delete window.sendFinPalNotification;
    };
  }, [settings.pushNotification]);

  const handleToggle = (key) => {
    if (key === 'pushNotification') {
      handlePushNotificationToggle();
      return;
    }
    setSettings((prev) => ({
      ...prev,
      [key]: !prev[key],
    }));
  };

  const handleSave = async () => {
    try {
      setLoading(true);
      
      // Gọi API để lưu settings
      const notificationData = {
        pushNotifications: settings.pushNotification,
        transactionAlert: settings.transactionAlert,
        budgetAlert: settings.budgetAlert,
        goalReminder: settings.goalReminder,
        weeklyReport: settings.weeklyReport,
        monthlyReport: settings.monthlyReport,
      };
      
      const response = await updateNotificationSettings(notificationData);
      
      if (response.success) {
        // Lưu vào localStorage như backup
        localStorage.setItem('notificationSettings', JSON.stringify(settings));
        message.success('Đã lưu cài đặt thông báo!');
      } else {
        throw new Error(response.message || 'Lỗi không xác định');
      }
    } catch (error) {
      console.error('Error saving settings:', error);
      // Fallback: lưu vào localStorage nếu API thất bại
      localStorage.setItem('notificationSettings', JSON.stringify(settings));
      message.warning('Đã lưu cài đặt cục bộ. Không thể đồng bộ với server.');
    } finally {
      setLoading(false);
    }
  };

  // Component cho mỗi setting item
  const SettingItem = ({ icon, title, description, checked, onChange, isLast = false, disabled = false, statusText = null }) => (
    <div
      style={{
        display: 'flex',
        alignItems: 'center',
        gap: 16,
        padding: 16,
        borderBottom: isLast ? 'none' : '1px solid #F3F4F6',
        opacity: disabled ? 0.6 : 1,
      }}
    >
      <div
        style={{
          width: 40,
          height: 40,
          borderRadius: 10,
          background: '#F9FAFB',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        }}
      >
        <img src={icon} alt="" style={{ width: 20, height: 20 }} />
      </div>
      <div style={{ flex: 1 }}>
        <div style={{ fontSize: 14, color: '#101828', marginBottom: 2 }}>{title}</div>
        <div style={{ fontSize: 14, color: '#6A7282' }}>
          {description}
          {statusText && (
            <span style={{ 
              marginLeft: 8, 
              fontSize: 12, 
              color: statusText.color,
              fontWeight: 500 
            }}>
              • {statusText.text}
            </span>
          )}
        </div>
      </div>
      <Switch
        checked={checked}
        onChange={onChange}
        disabled={disabled}
        style={{
          backgroundColor: checked ? '#030213' : '#CBCED4',
        }}
      />
    </div>
  );

  // Lấy trạng thái hiển thị cho Push Notification
  const getPushNotificationStatus = () => {
    if (!isNotificationSupported()) {
      return { text: 'Không hỗ trợ', color: '#EF4444' };
    }
    if (Notification.permission === 'denied') {
      return { text: 'Đã bị chặn', color: '#EF4444' };
    }
    if (Notification.permission === 'granted' && settings.pushNotification) {
      return { text: 'Đang bật', color: '#10B981' };
    }
    return null;
  };

  return (
    <div style={{ display: 'flex', minHeight: '100vh', background: '#F5F7FA' }}>
      <Sidebar />

      <div style={{ marginLeft: collapsed ? 80 : 280, flex: 1, transition: 'margin-left 0.3s' }}>
        <Header title="Cài đặt" />

        {initialLoading ? (
          <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '50vh' }}>
            <Spin size="large" tip="Đang tải cài đặt..." />
          </div>
        ) : (
        /* Content */
        <div style={{ padding: '0 16px', display: 'flex', flexDirection: 'column', gap: 24 }}>
          {/* Header Banner */}
          <div
            style={{
              background: 'linear-gradient(to right, #155DFC, #4F39F6)',
              borderRadius: '0 0 24px 24px',
              padding: '24px 16px',
              boxShadow: '0 10px 15px -3px rgba(0,0,0,0.1), 0 4px 6px -4px rgba(0,0,0,0.1)',
            }}
          >
            {/* Back + Title */}
            <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
              <div
                onClick={() => navigate('/settings')}
                style={{ cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 4 }}
              >
                <LeftOutlined style={{ fontSize: 12, color: '#FFFFFF' }} />
                <span style={{ fontSize: 14, color: '#FFFFFF' }}>Quay lại</span>
              </div>
              <div style={{ flex: 1 }}>
                <div style={{ fontSize: 16, color: '#FFFFFF', fontWeight: 400 }}>Cài đặt thông báo</div>
                <div style={{ fontSize: 14, color: '#DBEAFE' }}>Quản lý các loại thông báo</div>
              </div>
            </div>
          </div>

          {/* Kênh thông báo */}
          <div>
            <div style={{ fontSize: 16, color: '#364153', marginBottom: 12, paddingLeft: 8 }}>
              Kênh thông báo
            </div>
            <div
              style={{
                background: '#FFFFFF',
                borderRadius: 14,
                boxShadow: '0 4px 6px -1px rgba(0,0,0,0.1), 0 2px 4px -2px rgba(0,0,0,0.1)',
                overflow: 'hidden',
              }}
            >
              <SettingItem
                icon={iconPushNotification}
                title="Thông báo đẩy"
                description="Nhận thông báo trên trình duyệt"
                checked={settings.pushNotification}
                onChange={() => handleToggle('pushNotification')}
                disabled={!isNotificationSupported() || Notification.permission === 'denied'}
                statusText={getPushNotificationStatus()}
                isLast
              />
            </div>
          </div>

          {/* Loại thông báo */}
          <div>
            <div style={{ fontSize: 16, color: '#364153', marginBottom: 12, paddingLeft: 8 }}>
              Loại thông báo
            </div>
            <div
              style={{
                background: '#FFFFFF',
                borderRadius: 14,
                boxShadow: '0 4px 6px -1px rgba(0,0,0,0.1), 0 2px 4px -2px rgba(0,0,0,0.1)',
                overflow: 'hidden',
              }}
            >
              <SettingItem
                icon={iconTransaction}
                title="Cảnh báo giao dịch"
                description="Thông báo khi có giao dịch mới"
                checked={settings.transactionAlert}
                onChange={() => handleToggle('transactionAlert')}
              />
              <SettingItem
                icon={iconBudget}
                title="Cảnh báo ngân sách"
                description="Thông báo khi vượt ngân sách"
                checked={settings.budgetAlert}
                onChange={() => handleToggle('budgetAlert')}
              />
              <SettingItem
                icon={iconGoal}
                title="Nhắc nhở mục tiêu"
                description="Nhắc nhở về mục tiêu tiết kiệm"
                checked={settings.goalReminder}
                onChange={() => handleToggle('goalReminder')}
                isLast
              />
            </div>
          </div>

          {/* Báo cáo định kỳ */}
          <div>
            <div style={{ fontSize: 16, color: '#364153', marginBottom: 12, paddingLeft: 8 }}>
              Báo cáo định kỳ
            </div>
            <div
              style={{
                background: '#FFFFFF',
                borderRadius: 14,
                boxShadow: '0 4px 6px -1px rgba(0,0,0,0.1), 0 2px 4px -2px rgba(0,0,0,0.1)',
                overflow: 'hidden',
              }}
            >
              <SettingItem
                icon={iconWeeklyReport}
                title="Báo cáo tuần"
                description="Tóm tắt chi tiêu hàng tuần"
                checked={settings.weeklyReport}
                onChange={() => handleToggle('weeklyReport')}
              />
              <SettingItem
                icon={iconMonthlyReport}
                title="Báo cáo tháng"
                description="Tóm tắt chi tiêu hàng tháng"
                checked={settings.monthlyReport}
                onChange={() => handleToggle('monthlyReport')}
                isLast
              />
            </div>
          </div>

          {/* Save Button */}
          <div style={{ paddingBottom: 32 }}>
            <button
              onClick={handleSave}
              disabled={loading}
              style={{
                width: '100%',
                height: 36,
                background: 'linear-gradient(to right, #155DFC, #4F39F6)',
                border: 'none',
                borderRadius: 8,
                color: '#FFFFFF',
                fontSize: 14,
                cursor: loading ? 'not-allowed' : 'pointer',
                opacity: loading ? 0.7 : 1,
              }}
            >
              {loading ? 'Đang lưu...' : 'Lưu cài đặt'}
            </button>
          </div>
        </div>
        )}
      </div>
    </div>
  );
};

export default NotificationSettingsPage;
