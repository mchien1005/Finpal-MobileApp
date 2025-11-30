import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Avatar, Badge, Typography } from 'antd';
import { BellOutlined, UserOutlined } from '@ant-design/icons';
import { getUnreadCount } from '../../services/notificationService';
import authService from '../../services/authService';
import userService from '../../services/userService';

const { Title, Text } = Typography;

// Get API base URL for avatar
const getBaseUrl = () => {
  const apiUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';
  return apiUrl.replace(/\/api$/, '');
};
const API_BASE_URL = getBaseUrl();

const Header = ({ title = 'Tổng quan' }) => {
  const navigate = useNavigate();
  const [currentDate, setCurrentDate] = useState(new Date());
  const [unreadCount, setUnreadCount] = useState(0);
  const [avatarUrl, setAvatarUrl] = useState('');
  
  // Lấy thông tin user từ localStorage
  const user = authService.getCurrentUser();

  useEffect(() => {
    // Cập nhật thời gian mỗi phút
    const timer = setInterval(() => {
      setCurrentDate(new Date());
    }, 60000);

    return () => clearInterval(timer);
  }, []);

  // Fetch số thông báo chưa đọc
  useEffect(() => {
    const fetchUnreadCount = async () => {
      try {
        const count = await getUnreadCount();
        setUnreadCount(count);
      } catch (error) {
        console.error('Error fetching unread count:', error);
      }
    };

    fetchUnreadCount();
    
    // Refresh mỗi 30 giây
    const interval = setInterval(fetchUnreadCount, 30000);
    return () => clearInterval(interval);
  }, []);

  // Fetch avatar từ profile
  useEffect(() => {
    const fetchAvatar = async () => {
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
        console.error('Error fetching avatar:', error);
      }
    };

    fetchAvatar();
  }, []);

  // Format ngày theo tiếng Việt
  const formatDate = (date) => {
    const days = ['Chủ Nhật', 'Thứ Hai', 'Thứ Ba', 'Thứ Tư', 'Thứ Năm', 'Thứ Sáu', 'Thứ Bảy'];
    const dayName = days[date.getDay()];
    const day = date.getDate();
    const month = date.getMonth() + 1;
    const year = date.getFullYear();
    
    return `${dayName}, ${day} tháng ${month}, ${year}`;
  };

  return (
    <div
      style={{
        background: '#ffffff',
        padding: '16px 32px',
        borderBottom: '1px solid #E5E7EB',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
      }}
    >
      <div>
        <Title level={4} style={{ margin: 0, marginBottom: 4 }}>
          {title}
        </Title>
        <Text type="secondary" style={{ fontSize: 14 }}>
          {formatDate(currentDate)}
        </Text>
      </div>
      <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
        <Badge count={unreadCount} offset={[-2, 0]}>
          <BellOutlined 
            style={{ fontSize: 20, cursor: 'pointer' }} 
            onClick={() => navigate('/notifications')}
          />
        </Badge>
        {avatarUrl ? (
          <Avatar
            size={40}
            src={avatarUrl.startsWith('http') ? avatarUrl : `${API_BASE_URL}${avatarUrl}`}
            style={{ cursor: 'pointer' }}
            onClick={() => navigate('/settings/profile')}
          />
        ) : (
          <Avatar
            size={40}
            style={{ background: '#2B7FFF', cursor: 'pointer' }}
            onClick={() => navigate('/settings/profile')}
          >
            {user?.fullName?.charAt(0) || user?.username?.charAt(0) || <UserOutlined />}
          </Avatar>
        )}
        <div>
          <div style={{ fontSize: 14, fontWeight: 500 }}>
            {user?.fullName || user?.username || 'Người dùng'}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Header;
