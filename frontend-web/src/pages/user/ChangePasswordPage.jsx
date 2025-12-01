import React, { useState } from 'react';
import { Input, Button, message } from 'antd';
import { LockOutlined, EyeOutlined, EyeInvisibleOutlined, LeftOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import Sidebar from '../../components/user/Sidebar';
import Header from '../../components/common/Header';
import SuccessModal from '../../components/common/SuccessModal';
import { useSidebar } from '../../contexts/SidebarContext';
import authService from '../../services/authService';

const ChangePasswordPage = () => {
  const { collapsed } = useSidebar();
  const navigate = useNavigate();
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showCurrentPassword, setShowCurrentPassword] = useState(false);
  const [showNewPassword, setShowNewPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [showSuccessModal, setShowSuccessModal] = useState(false);

  const isFormValid = currentPassword && newPassword && confirmPassword;

  const handleChangePassword = async () => {
    // Validate
    if (!currentPassword) {
      message.error('Vui lòng nhập mật khẩu hiện tại');
      return;
    }
    if (!newPassword) {
      message.error('Vui lòng nhập mật khẩu mới');
      return;
    }
    if (newPassword.length < 6) {
      message.error('Mật khẩu mới phải có ít nhất 6 ký tự');
      return;
    }
    if (newPassword !== confirmPassword) {
      message.error('Mật khẩu xác nhận không khớp');
      return;
    }

    setLoading(true);
    try {
      await authService.changePassword({
        currentPassword,
        newPassword,
      });
      
      // Reset form
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
      
      // Show success modal
      setShowSuccessModal(true);
    } catch (error) {
      console.error('Error changing password:', error);
      const errorMsg = error.response?.data?.message || 'Không thể đổi mật khẩu. Vui lòng kiểm tra lại mật khẩu hiện tại.';
      message.error(errorMsg);
    } finally {
      setLoading(false);
    }
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
          {/* Title Banner */}
          <div
            style={{
              background: 'linear-gradient(to right, #155DFC, #4F39F6)',
              borderRadius: '0 0 24px 24px',
              padding: '24px 16px',
              boxShadow: '0 10px 15px -3px rgba(0,0,0,0.1), 0 4px 6px -4px rgba(0,0,0,0.1)',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
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
                }}
              >
                <LeftOutlined style={{ fontSize: 12, color: '#EFF6FF' }} />
                <span style={{ fontSize: 14, color: '#EFF6FF' }}>Quay lại</span>
              </div>
              <div style={{ flex: 1 }}>
                <div style={{ fontSize: 16, fontWeight: 500, color: '#FFFFFF', marginBottom: 4 }}>
                  Thiết lập mật khẩu
                </div>
                <div style={{ fontSize: 14, color: '#DBEAFE' }}>
                  Thay đổi mật khẩu bảo mật
                </div>
              </div>
            </div>
          </div>

          {/* Form */}
          <div style={{ padding: '0 16px' }}>
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
              {/* Current Password */}
              <div style={{ marginBottom: 16 }}>
                <div style={{ fontSize: 14, color: '#0a0a0a', marginBottom: 6 }}>
                  Mật khẩu hiện tại
                </div>
                <Input
                  type={showCurrentPassword ? 'text' : 'password'}
                  placeholder="Nhập mật khẩu hiện tại"
                  value={currentPassword}
                  onChange={(e) => setCurrentPassword(e.target.value)}
                  suffix={
                    <span
                      onClick={() => setShowCurrentPassword(!showCurrentPassword)}
                      style={{ cursor: 'pointer', color: '#9CA3AF' }}
                    >
                      {showCurrentPassword ? <EyeOutlined /> : <EyeInvisibleOutlined />}
                    </span>
                  }
                  style={{
                    height: 36,
                    borderRadius: 8,
                    background: '#F3F3F5',
                    border: '1px solid transparent',
                  }}
                />
              </div>

              {/* New Password */}
              <div style={{ marginBottom: 16 }}>
                <div style={{ fontSize: 14, color: '#0a0a0a', marginBottom: 6 }}>
                  Mật khẩu mới
                </div>
                <Input
                  type={showNewPassword ? 'text' : 'password'}
                  placeholder="Nhập mật khẩu mới"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  suffix={
                    <span
                      onClick={() => setShowNewPassword(!showNewPassword)}
                      style={{ cursor: 'pointer', color: '#9CA3AF' }}
                    >
                      {showNewPassword ? <EyeOutlined /> : <EyeInvisibleOutlined />}
                    </span>
                  }
                  style={{
                    height: 36,
                    borderRadius: 8,
                    background: '#F3F3F5',
                    border: '1px solid transparent',
                  }}
                />
              </div>

              {/* Confirm Password */}
              <div>
                <div style={{ fontSize: 14, color: '#0a0a0a', marginBottom: 6 }}>
                  Xác nhận mật khẩu mới
                </div>
                <Input
                  type={showConfirmPassword ? 'text' : 'password'}
                  placeholder="Nhập lại mật khẩu mới"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  suffix={
                    <span
                      onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                      style={{ cursor: 'pointer', color: '#9CA3AF' }}
                    >
                      {showConfirmPassword ? <EyeOutlined /> : <EyeInvisibleOutlined />}
                    </span>
                  }
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
              icon={<LockOutlined />}
              onClick={handleChangePassword}
              loading={loading}
              disabled={!isFormValid || loading}
              block
              style={{
                height: 36,
                borderRadius: 8,
                background: isFormValid 
                  ? 'linear-gradient(to right, #155DFC, #4F39F6)' 
                  : 'linear-gradient(to right, #155DFC, #4F39F6)',
                border: 'none',
                fontSize: 14,
                opacity: isFormValid ? 1 : 0.5,
              }}
            >
              Đổi mật khẩu
            </Button>
          </div>
        </div>
      </div>

      {/* Success Modal */}
      <SuccessModal
        open={showSuccessModal}
        onClose={() => setShowSuccessModal(false)}
        message="Đổi mật khẩu thành công!"
      />
    </div>
  );
};

export default ChangePasswordPage;
