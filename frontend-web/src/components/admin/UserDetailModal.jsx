import React, { useState, useEffect } from 'react';
import { Modal, Row, Col, Tag, Button, message } from 'antd';
import { CloseOutlined, SyncOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import DeleteUserConfirmModal from './DeleteUserConfirmModal';
import DisableUserConfirmModal from './DisableUserConfirmModal';
import ResetPasswordModal from './ResetPasswordModal';
import SuccessModal from '../common/SuccessModal';
import userService from '../../services/userService';

const UserDetailModal = ({ visible, onClose, user, onDeleteSuccess, onDisableSuccess }) => {
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [showDeleteSuccess, setShowDeleteSuccess] = useState(false);
  const [showDisableConfirm, setShowDisableConfirm] = useState(false);
  const [showDisableSuccess, setShowDisableSuccess] = useState(false);
  const [showResetPassword, setShowResetPassword] = useState(false);
  const [showResetSuccess, setShowResetSuccess] = useState(false);
  const [newGeneratedPassword, setNewGeneratedPassword] = useState('');
  const [currentStatus, setCurrentStatus] = useState(user?.status);
  const [userDetail, setUserDetail] = useState(null);
  const [loadingDetail, setLoadingDetail] = useState(false);

  // Fetch user detail when modal opens
  useEffect(() => {
    const fetchUserDetail = async () => {
      if (visible && user?.realId) {
        setLoadingDetail(true);
        try {
          const detail = await userService.admin.getUserDetail(user.realId);
          console.log('User detail from API:', detail);
          setUserDetail(detail);
        } catch (error) {
          console.error('Error fetching user detail:', error);
          message.error('Không thể tải chi tiết người dùng');
        } finally {
          setLoadingDetail(false);
        }
      }
    };
    fetchUserDetail();
  }, [visible, user?.realId]);

  // Update currentStatus when user prop changes
  useEffect(() => {
    if (user) {
      setCurrentStatus(user.status);
    }
  }, [user]);

  if (!user) return null;

  const handleDeleteClick = () => {
    setShowDeleteConfirm(true);
  };

  const handleConfirmDelete = async () => {
    setShowDeleteConfirm(false);
    try {
      // Try using realId first (numeric ID), fallback to userId (userCode)
      const idToUse = user.realId || user.userId;
      console.log('Attempting to delete user:', { userId: user.userId, realId: user.realId, using: idToUse });
      await userService.admin.deleteUser(idToUse);
      setTimeout(() => {
        setShowDeleteSuccess(true);
      }, 300);
    } catch (error) {
      console.error('Error deleting user:', error);
      message.error('Không thể xóa người dùng');
    }
  };

  const handleCancelDelete = () => {
    setShowDeleteConfirm(false);
  };

  const handleSuccessClose = () => {
    setShowDeleteSuccess(false);
    
    // Delay closing parent modal to ensure smooth transition
    setTimeout(() => {
      if (onDeleteSuccess) {
        onDeleteSuccess(user.userId); // Notify parent to remove user from list
      }
      onClose(); // Close the user detail modal
    }, 100);
  };

  const handleDisableClick = () => {
    setShowDisableConfirm(true);
  };

  const handleConfirmDisable = async () => {
    setShowDisableConfirm(false);
    try {
      // Try using realId first (numeric ID), fallback to userId (userCode)
      const idToUse = user.realId || user.userId;
      console.log('Attempting to toggle status for user:', { userId: user.userId, realId: user.realId, using: idToUse });
      const response = await userService.admin.toggleStatus(idToUse);
      console.log('Toggle status response:', response);
      
      // Update local status immediately
      const newStatus = currentStatus === 'Active' ? 'Inactive' : 'Active';
      setCurrentStatus(newStatus);
      
      setTimeout(() => {
        setShowDisableSuccess(true);
      }, 300);
    } catch (error) {
      console.error('Error disabling user:', error);
      console.error('Error response:', error.response?.data);
      console.error('Error status:', error.response?.status);
      console.error('Error message:', error.message);
      message.error(`Không thể vô hiệu hóa người dùng: ${error.response?.data?.message || error.message}`);
    }
  };

  const handleCancelDisable = () => {
    setShowDisableConfirm(false);
  };

  const handleDisableSuccessClose = () => {
    setShowDisableSuccess(false);
    
    // Delay closing parent modal to ensure smooth transition
    setTimeout(() => {
      if (onDisableSuccess) {
        onDisableSuccess(user.userId); // Notify parent to update user status
      }
      onClose(); // Close the user detail modal
    }, 100);
  };

  const handleResetPasswordClick = () => {
    setShowResetPassword(true);
  };

  const handleConfirmReset = async () => {
    setShowResetPassword(false);
    try {
      // Try using realId first (numeric ID), fallback to userId (userCode)
      const idToUse = user.realId || user.userId;
      console.log('Resetting password for user:', idToUse);
      const response = await userService.admin.resetPassword(idToUse);
      console.log('Password reset response:', response);
      console.log('Response keys:', Object.keys(response));
      
      // Try different possible field names from API
      const password = response.newPassword || response.adminPassword || response.password || 'N/A';
      console.log('Extracted password:', password);
      setNewGeneratedPassword(password);
      
      setTimeout(() => {
        setShowResetSuccess(true);
      }, 300);
    } catch (error) {
      console.error('Error resetting password:', error);
      message.error('Không thể reset mật khẩu');
    }
  };

  const handleCancelReset = () => {
    setShowResetPassword(false);
  };

  const handleResetSuccessClose = () => {
    setShowResetSuccess(false);
  };

  // Transform login history from API
  const loginHistory = userDetail?.loginHistory?.map(log => ({
    date: log.loginTime ? new Date(log.loginTime).toLocaleString('vi-VN') : 'N/A',
    device: log.deviceName || 'N/A',
    ip: log.ipAddress || 'N/A',
    location: log.location || 'N/A',
    status: log.status === 'SUCCESS' ? 'Success' : 'Failed',
  })) || [];

  return (
    <Modal
      open={visible}
      onCancel={onClose}
      footer={null}
      closeIcon={<CloseOutlined />}
      width={540}
      styles={{
        body: { padding: 32 },
      }}
    >
      <div>
        {/* Header */}
        <div style={{ marginBottom: 8 }}>
          <h2 style={{ fontSize: 20, fontWeight: 600, margin: 0, color: '#101828' }}>
            Chi tiết người dùng
          </h2>
        </div>
        <div style={{ marginBottom: 24 }}>
          <p style={{ margin: 0, color: '#6a7282', fontSize: 14 }}>
            Thông tin chi tiết và hoạt động của {user.user?.name}
          </p>
        </div>

        {/* User Info Grid */}
        <Row gutter={[16, 16]} style={{ marginBottom: 32 }}>
          <Col span={12}>
            <div style={{ marginBottom: 4 }}>
              <span style={{ fontSize: 12, color: '#6a7282' }}>User ID</span>
            </div>
            <div>
              <span style={{ fontSize: 14, fontWeight: 500, color: '#155dfc' }}>
                {user.userId}
              </span>
            </div>
          </Col>

          <Col span={12}>
            <div style={{ marginBottom: 4 }}>
              <span style={{ fontSize: 12, color: '#6a7282' }}>Trạng thái</span>
            </div>
            <div>
              <Tag
                style={{
                  background: currentStatus === 'Active' ? '#dcfce7' : (currentStatus === 'Inactive' ? '#f3f4f6' : '#fee2e2'),
                  color: currentStatus === 'Active' ? '#008236' : (currentStatus === 'Inactive' ? '#4b5563' : '#dc2626'),
                  border: 'none',
                  borderRadius: 6,
                  padding: '2px 8px',
                }}
              >
                {currentStatus}
              </Tag>
            </div>
          </Col>

          <Col span={12}>
            <div style={{ marginBottom: 4 }}>
              <span style={{ fontSize: 12, color: '#6a7282' }}>Email</span>
            </div>
            <div>
              <span style={{ 
                fontSize: 14, 
                color: '#101828',
                display: 'block',
                overflow: 'hidden',
                textOverflow: 'ellipsis',
                whiteSpace: 'nowrap',
                maxWidth: '200px'
              }}>
                {user.contact?.email}
              </span>
            </div>
          </Col>

          <Col span={12}>
            <div style={{ marginBottom: 4 }}>
              <span style={{ fontSize: 12, color: '#6a7282' }}>Số điện thoại</span>
            </div>
            <div>
              <span style={{ fontSize: 14, color: '#101828' }}>
                {user.contact?.phone}
              </span>
            </div>
          </Col>

          <Col span={12}>
            <div style={{ marginBottom: 4 }}>
              <span style={{ fontSize: 12, color: '#6a7282' }}>Ngày đăng ký</span>
            </div>
            <div>
              <span style={{ fontSize: 14, color: '#101828' }}>
                {user.registeredDate}
              </span>
            </div>
          </Col>
        </Row>

        {/* Financial Overview */}
        <div style={{ marginBottom: 24 }}>
          <h3 style={{ fontSize: 14, fontWeight: 600, marginBottom: 16, color: '#101828' }}>
            Thông tin giao dịch
          </h3>
          <Row gutter={12}>
            <Col span={24}>
              <div
                style={{
                  background: '#f9fafb',
                  border: '1px solid #e5e7eb',
                  borderRadius: 8,
                  padding: '16px 12px',
                }}
              >
                <div style={{ fontSize: 12, color: '#6a7282', marginBottom: 8 }}>
                  Tổng giao dịch
                </div>
                <div style={{ fontSize: 20, fontWeight: 600, color: '#101828' }}>
                  {user.transactions}
                </div>
              </div>
            </Col>
          </Row>
        </div>

        {/* Login History */}
        <div style={{ marginBottom: 24 }}>
          <h3 style={{ fontSize: 14, fontWeight: 600, marginBottom: 16, color: '#101828' }}>
            Lịch sử đăng nhập
          </h3>
          <div style={{ maxHeight: 280, overflowY: 'auto' }}>
            {loadingDetail ? (
              <div style={{ textAlign: 'center', padding: '40px 0', color: '#6b7280' }}>
                Đang tải...
              </div>
            ) : loginHistory.length === 0 ? (
              <div style={{ textAlign: 'center', padding: '40px 0', color: '#6b7280' }}>
                Chưa có lịch sử đăng nhập
              </div>
            ) : (
              loginHistory.map((login, index) => (
              <div
                key={index}
                style={{
                  padding: '12px 0',
                  borderBottom: index < loginHistory.length - 1 ? '1px solid #e5e7eb' : 'none',
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <div>
                    <div style={{ fontSize: 14, fontWeight: 500, color: '#101828', marginBottom: 4 }}>
                      {login.date}
                    </div>
                    <div style={{ fontSize: 12, color: '#6a7282' }}>
                      {login.device} • {login.ip} • {login.location}
                    </div>
                  </div>
                  <Tag
                    style={{
                      background: login.status === 'Success' ? '#dcfce7' : '#ffe2e2',
                      color: login.status === 'Success' ? '#008236' : '#c10007',
                      border: 'none',
                      borderRadius: 6,
                      padding: '2px 8px',
                      fontSize: 12,
                    }}
                  >
                    {login.status}
                  </Tag>
                </div>
              </div>
            )))
            }
          </div>
        </div>

        {/* Action Buttons */}
        <Row gutter={12}>
          <Col span={8}>
            <Button
              icon={<SyncOutlined />}
              onClick={handleResetPasswordClick}
              style={{
                width: '100%',
                height: 40,
                borderRadius: 8,
                border: '1px solid #d1d5db',
              }}
            >
              Reset Password
            </Button>
          </Col>
          <Col span={8}>
            {currentStatus === 'Active' ? (
              <Button
                icon={<EditOutlined />}
                onClick={handleDisableClick}
                style={{
                  width: '100%',
                  height: 40,
                  borderRadius: 8,
                  border: '1px solid #fca5a5',
                  color: '#dc2626',
                }}
              >
                Vô hiệu hóa
              </Button>
            ) : (
              <Button
                icon={<EditOutlined />}
                onClick={handleDisableClick}
                style={{
                  width: '100%',
                  height: 40,
                  borderRadius: 8,
                  border: '1px solid #86efac',
                  color: '#16a34a',
                  background: '#f0fdf4',
                }}
              >
                Kích hoạt lại
              </Button>
            )}
          </Col>
          <Col span={8}>
            <Button
              icon={<DeleteOutlined />}
              onClick={handleDeleteClick}
              style={{
                width: '100%',
                height: 40,
                borderRadius: 8,
                border: '1px solid #fca5a5',
                color: '#dc2626',
              }}
            >
              Xóa tài khoản
            </Button>
          </Col>
        </Row>
      </div>

      {/* Delete Confirmation Modal */}
      <DeleteUserConfirmModal
        open={showDeleteConfirm}
        onConfirm={handleConfirmDelete}
        onCancel={handleCancelDelete}
        userName={user.user?.name}
        userEmail={user.contact?.email}
      />

      {/* Disable Confirmation Modal */}
      <DisableUserConfirmModal
        open={showDisableConfirm}
        onConfirm={handleConfirmDisable}
        onCancel={handleCancelDisable}
        userName={user.user?.name}
        userEmail={user.contact?.email}
        isActive={currentStatus === 'Active'}
      />

      {/* Delete Success Modal */}
      <SuccessModal
        open={showDeleteSuccess}
        onClose={handleSuccessClose}
        message="Xóa người dùng thành công!"
        buttonText="Đồng ý"
      />

      {/* Disable Success Modal */}
      <SuccessModal
        open={showDisableSuccess}
        onClose={handleDisableSuccessClose}
        message={currentStatus === 'Inactive' ? "Vô hiệu hóa tài khoản thành công!" : "Kích hoạt tài khoản thành công!"}
        buttonText="Đồng ý"
      />

      {/* Reset Password Modal */}
      <ResetPasswordModal
        open={showResetPassword}
        onConfirm={handleConfirmReset}
        onCancel={handleCancelReset}
        user={user}
      />

      {/* Reset Password Success Modal */}
      <SuccessModal
        open={showResetSuccess}
        onClose={handleResetSuccessClose}
        message="Reset mật khẩu thành công!"
        buttonText="Đồng ý"
        newPassword={newGeneratedPassword}
      />
    </Modal>
  );
};

export default UserDetailModal;
