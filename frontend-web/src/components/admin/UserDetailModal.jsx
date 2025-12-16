import React, { useState } from 'react';
import { Modal, Row, Col, Tag, Button } from 'antd';
import { CloseOutlined, SyncOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import DeleteUserConfirmModal from './DeleteUserConfirmModal';
import DisableUserConfirmModal from './DisableUserConfirmModal';
import ResetPasswordModal from './ResetPasswordModal';
import SuccessModal from '../common/SuccessModal';

const UserDetailModal = ({ visible, onClose, user, onDeleteSuccess, onDisableSuccess }) => {
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [showDeleteSuccess, setShowDeleteSuccess] = useState(false);
  const [showDisableConfirm, setShowDisableConfirm] = useState(false);
  const [showDisableSuccess, setShowDisableSuccess] = useState(false);
  const [showResetPassword, setShowResetPassword] = useState(false);
  const [showResetSuccess, setShowResetSuccess] = useState(false);

  if (!user) return null;

  const handleDeleteClick = () => {
    setShowDeleteConfirm(true);
  };

  const handleConfirmDelete = () => {
    setShowDeleteConfirm(false);
    // Simulate delete API call
    setTimeout(() => {
      setShowDeleteSuccess(true);
    }, 300);
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

  const handleConfirmDisable = () => {
    setShowDisableConfirm(false);
    // Simulate disable API call
    setTimeout(() => {
      setShowDisableSuccess(true);
    }, 300);
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

  const handleConfirmReset = (newPassword) => {
    setShowResetPassword(false);
    // Simulate reset password API call
    console.log('New password:', newPassword);
    setTimeout(() => {
      setShowResetSuccess(true);
    }, 300);
  };

  const handleCancelReset = () => {
    setShowResetPassword(false);
  };

  const handleResetSuccessClose = () => {
    setShowResetSuccess(false);
  };

  const loginHistory = [
    {
      date: '24/03/2024 10:30',
      device: 'iPhone 15 Pro',
      ip: '192.168.1.1',
      location: 'Hà Nội',
      status: 'Success',
    },
    {
      date: '24/03/2024 09:15',
      device: 'iPhone 15 Pro',
      ip: '192.168.1.1',
      location: 'Hà Nội',
      status: 'Success',
    },
    {
      date: '23/03/2024 22:45',
      device: 'iPhone 15 Pro',
      ip: '192.168.1.1',
      location: 'Hà Nội',
      status: 'Success',
    },
    {
      date: '23/03/2024 14:20',
      device: 'MacBook Pro',
      ip: '192.168.1.2',
      location: 'Hà Nội',
      status: 'Failed',
    },
  ];

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
                  background: '#dcfce7',
                  color: '#008236',
                  border: 'none',
                  borderRadius: 6,
                  padding: '2px 8px',
                }}
              >
                {user.status}
              </Tag>
            </div>
          </Col>

          <Col span={12}>
            <div style={{ marginBottom: 4 }}>
              <span style={{ fontSize: 12, color: '#6a7282' }}>Email</span>
            </div>
            <div>
              <span style={{ fontSize: 14, color: '#101828' }}>
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
              <span style={{ fontSize: 12, color: '#6a7282' }}>Ngân hàng</span>
            </div>
            <div>
              <span style={{ fontSize: 14, color: '#101828' }}>
                {user.bank}
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
            Tổng quan tài chính
          </h3>
          <Row gutter={12}>
            <Col span={8}>
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
            <Col span={8}>
              <div
                style={{
                  background: '#f9fafb',
                  border: '1px solid #e5e7eb',
                  borderRadius: 8,
                  padding: '16px 12px',
                }}
              >
                <div style={{ fontSize: 12, color: '#6a7282', marginBottom: 8 }}>
                  Tổng chi tiêu
                </div>
                <div style={{ fontSize: 20, fontWeight: 600, color: '#101828' }}>
                  {user.totalSpending}
                </div>
              </div>
            </Col>
            <Col span={8}>
              <div
                style={{
                  background: '#f9fafb',
                  border: '1px solid #e5e7eb',
                  borderRadius: 8,
                  padding: '16px 12px',
                }}
              >
                <div style={{ fontSize: 12, color: '#6a7282', marginBottom: 8 }}>
                  TB/giao dịch
                </div>
                <div style={{ fontSize: 20, fontWeight: 600, color: '#101828' }}>
                  ₫186K
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
            {loginHistory.map((login, index) => (
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
            ))}
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
        message="Vô hiệu hóa tài khoản thành công!"
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
      />
    </Modal>
  );
};

export default UserDetailModal;
