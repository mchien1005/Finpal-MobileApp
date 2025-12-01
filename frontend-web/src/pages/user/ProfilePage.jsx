import { useState, useEffect, useRef } from 'react';
import { Input, Button, message, Avatar } from 'antd';
import { SaveOutlined, CameraOutlined, LeftOutlined, LoadingOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import Sidebar from '../../components/user/Sidebar';
import Header from '../../components/common/Header';
import SuccessModal from '../../components/common/SuccessModal';
import { useSidebar } from '../../contexts/SidebarContext';
import userService from '../../services/userService';
import authService from '../../services/authService';

// Get API base URL for avatar (remove /api suffix if present)
const getBaseUrl = () => {
  const apiUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';
  // Remove /api suffix to get base URL for static files
  return apiUrl.replace(/\/api$/, '');
};
const API_BASE_URL = getBaseUrl();

const ProfilePage = () => {
  const { collapsed } = useSidebar();
  const navigate = useNavigate();
  const fileInputRef = useRef(null);
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [avatarUrl, setAvatarUrl] = useState('');
  const [loading, setLoading] = useState(false);
  const [uploadingAvatar, setUploadingAvatar] = useState(false);
  const [showSuccessModal, setShowSuccessModal] = useState(false);

  // Lấy thông tin user hiện tại
  const currentUser = authService.getCurrentUser();

  useEffect(() => {
    // Load profile data
    loadProfile();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const loadProfile = async () => {
    try {
      const response = await userService.getProfile();
      console.log('Profile response:', response);
      if (response.success && response.data) {
        setFullName(response.data.fullName || '');
        setEmail(response.data.email || '');
        setPhone(response.data.phone || '');
        setAvatarUrl(response.data.avatarUrl || '');
        console.log('Avatar URL:', response.data.avatarUrl);
        console.log('Full avatar URL:', response.data.avatarUrl ? (response.data.avatarUrl.startsWith('http') ? response.data.avatarUrl : `${API_BASE_URL}${response.data.avatarUrl}`) : 'none');
      }
    } catch (error) {
      console.error('Error loading profile:', error);
      // Fallback to current user from localStorage
      if (currentUser) {
        setFullName(currentUser.fullName || '');
        setEmail(currentUser.email || '');
      }
    }
  };

  const handleAvatarClick = () => {
    fileInputRef.current?.click();
  };

  const handleAvatarChange = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    // Validate file type
    if (!file.type.startsWith('image/')) {
      message.error('Vui lòng chọn file ảnh');
      return;
    }

    // Validate file size (max 5MB)
    if (file.size > 5 * 1024 * 1024) {
      message.error('File ảnh không được vượt quá 5MB');
      return;
    }

    setUploadingAvatar(true);
    try {
      const response = await userService.uploadAvatar(file);
      console.log('Upload response:', response);
      if (response.success) {
        console.log('New avatar URL:', response.avatarUrl);
        setAvatarUrl(response.avatarUrl);
        message.success('Cập nhật ảnh đại diện thành công');
        
        // Update localStorage
        const storedUser = authService.getCurrentUser();
        if (storedUser) {
          storedUser.avatarUrl = response.avatarUrl;
          localStorage.setItem('user', JSON.stringify(storedUser));
        }
      } else {
        message.error(response.message || 'Không thể upload ảnh');
      }
    } catch (error) {
      console.error('Error uploading avatar:', error);
      const errorMsg = error.response?.data?.message || 'Không thể upload ảnh. Vui lòng thử lại.';
      message.error(errorMsg);
    } finally {
      setUploadingAvatar(false);
      // Reset input
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
    }
  };

  const handleSave = async () => {
    // Validate
    if (!fullName.trim()) {
      message.error('Vui lòng nhập họ và tên');
      return;
    }
    if (!email.trim()) {
      message.error('Vui lòng nhập email');
      return;
    }
    // Simple email validation
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      message.error('Email không hợp lệ');
      return;
    }

    setLoading(true);
    try {
      const response = await userService.updateProfile({
        fullName: fullName.trim(),
        email: email.trim(),
        phone: phone.trim() || null,
      });

      if (response.success) {
        // Update localStorage user info
        const storedUser = authService.getCurrentUser();
        if (storedUser) {
          storedUser.fullName = fullName.trim();
          storedUser.email = email.trim();
          localStorage.setItem('user', JSON.stringify(storedUser));
        }

        setShowSuccessModal(true);
      } else {
        message.error(response.message || 'Không thể cập nhật thông tin');
      }
    } catch (error) {
      console.error('Error updating profile:', error);
      const errorMsg = error.response?.data?.message || 'Không thể cập nhật thông tin. Vui lòng thử lại.';
      message.error(errorMsg);
    } finally {
      setLoading(false);
    }
  };

  const getInitial = () => {
    if (fullName) return fullName.charAt(0).toUpperCase();
    if (currentUser?.fullName) return currentUser.fullName.charAt(0).toUpperCase();
    if (currentUser?.username) return currentUser.username.charAt(0).toUpperCase();
    return 'N';
  };

  return (
    <div style={{ display: 'flex', minHeight: '100vh', background: '#F5F7FA' }}>
      <Sidebar />

      {/* Main Content */}
      <div style={{ marginLeft: collapsed ? 80 : 280, flex: 1, transition: 'margin-left 0.3s' }}>
        {/* Header */}
        <Header title="Cài đặt" />

        {/* Content */}
        <div style={{ padding: '0 16px', display: 'flex', flexDirection: 'column', gap: 24 }}>
          {/* Title Banner with Avatar */}
          <div
            style={{
              background: 'linear-gradient(to right, #155DFC, #4F39F6)',
              borderRadius: '0 0 24px 24px',
              padding: '12px 16px 80px 16px',
              boxShadow: '0 10px 15px -3px rgba(0,0,0,0.1), 0 4px 6px -4px rgba(0,0,0,0.1)',
              position: 'relative',
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
                top: '50%',
                transform: 'translateY(-50%)',
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                pointerEvents: 'none',
              }}
            >
              <div style={{ color: '#EFF6FF', fontSize: 18, fontWeight: 600 }}>Thông tin cá nhân</div>
            </div>

            {/* Avatar - positioned to overflow */}
            <div
              style={{
                position: 'absolute',
                bottom: -56,
                left: 16,
              }}
            >
              <div style={{ position: 'relative' }}>
                {avatarUrl ? (
                  <Avatar
                    size={112}
                    src={avatarUrl.startsWith('http') ? avatarUrl : `${API_BASE_URL}${avatarUrl}`}
                    style={{
                      border: '4px solid #FFFFFF',
                      boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -2px rgba(0, 0, 0, 0.1)',
                    }}
                  />
                ) : (
                  <Avatar
                    size={112}
                    style={{
                      background: 'linear-gradient(135deg, #4F6BF6 0%, #7C5CEA 100%)',
                      color: '#FFFFFF',
                      fontSize: 36,
                      fontWeight: 400,
                      border: '4px solid #FFFFFF',
                      boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -2px rgba(0, 0, 0, 0.1)',
                    }}
                  >
                    {getInitial()}
                  </Avatar>
                )}
                {/* Camera button */}
                <div
                  onClick={handleAvatarClick}
                  style={{
                    position: 'absolute',
                    bottom: 0,
                    right: 0,
                    width: 36,
                    height: 36,
                    background: '#FFFFFF',
                    borderRadius: '50%',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    cursor: uploadingAvatar ? 'not-allowed' : 'pointer',
                    boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -2px rgba(0, 0, 0, 0.1)',
                  }}
                >
                  {uploadingAvatar ? (
                    <LoadingOutlined style={{ fontSize: 18, color: '#6B7280' }} />
                  ) : (
                    <CameraOutlined style={{ fontSize: 18, color: '#6B7280' }} />
                  )}
                </div>
                {/* Hidden file input */}
                <input
                  ref={fileInputRef}
                  type="file"
                  accept="image/*"
                  onChange={handleAvatarChange}
                  style={{ display: 'none' }}
                />
              </div>
            </div>
          </div>

          {/* Form - with left margin to account for avatar */}
          <div style={{ padding: '0 16px', marginLeft: 160, marginTop: 8 }}>
            {/* Form Card */}
            <div
              style={{
                background: '#FFFFFF',
                borderRadius: 14,
                padding: 16,
                boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -2px rgba(0, 0, 0, 0.1)',
                marginBottom: 16,
              }}
            >
              {/* Full Name */}
              <div style={{ marginBottom: 16 }}>
                <div style={{ fontSize: 14, color: '#0a0a0a', marginBottom: 6 }}>
                  Họ và tên
                </div>
                <Input
                  placeholder="Nhập họ và tên"
                  value={fullName}
                  onChange={(e) => setFullName(e.target.value)}
                  style={{
                    height: 36,
                    borderRadius: 8,
                    background: '#F3F3F5',
                    border: '1px solid transparent',
                  }}
                />
              </div>

              {/* Email */}
              <div style={{ marginBottom: 16 }}>
                <div style={{ fontSize: 14, color: '#0a0a0a', marginBottom: 6 }}>
                  Email
                </div>
                <Input
                  placeholder="Nhập email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  style={{
                    height: 36,
                    borderRadius: 8,
                    background: '#F3F3F5',
                    border: '1px solid transparent',
                  }}
                />
              </div>

              {/* Phone */}
              <div>
                <div style={{ fontSize: 14, color: '#0a0a0a', marginBottom: 6 }}>
                  Số điện thoại
                </div>
                <Input
                  placeholder="Nhập số điện thoại"
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
                  style={{
                    height: 36,
                    borderRadius: 8,
                    background: '#F3F3F5',
                    border: '1px solid transparent',
                  }}
                />
              </div>
            </div>

            {/* Submit Button */}
            <Button
              type="primary"
              icon={<SaveOutlined />}
              onClick={handleSave}
              loading={loading}
              block
              style={{
                height: 36,
                borderRadius: 8,
                background: 'linear-gradient(to right, #155DFC, #4F39F6)',
                border: 'none',
                fontSize: 14,
              }}
            >
              Lưu thay đổi
            </Button>
          </div>
        </div>
      </div>

      {/* Success Modal */}
      <SuccessModal
        open={showSuccessModal}
        onClose={() => setShowSuccessModal(false)}
        message="Cập nhật thông tin thành công!"
      />
    </div>
  );
};

export default ProfilePage;
