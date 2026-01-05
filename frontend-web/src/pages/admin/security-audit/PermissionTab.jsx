import React, { useState, useEffect } from 'react';
import { Card, Table, Button, Tag, message, Spin } from 'antd';
import { PlusOutlined, EyeOutlined, DeleteOutlined } from '@ant-design/icons';
import AddAdminModal from '../../../components/admin/AddAdminModal';
import AdminDetailModal from '../../../components/admin/AdminDetailModal';
import SuccessModal from '../../../components/common/SuccessModal';
import ConfirmModal from '../../../components/common/ConfirmModal';
import activityLogService from '../../../services/activityLogService';

const PermissionTab = () => {
  const [addModalVisible, setAddModalVisible] = useState(false);
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [successModalVisible, setSuccessModalVisible] = useState(false);
  const [confirmModalVisible, setConfirmModalVisible] = useState(false);
  const [selectedAdmin, setSelectedAdmin] = useState(null);
  const [adminToDelete, setAdminToDelete] = useState(null);
  const [successMessage, setSuccessMessage] = useState('');
  const [admins, setAdmins] = useState([]);
  const [roles, setRoles] = useState([]);
  const [permissions, setPermissions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [rolesLoading, setRolesLoading] = useState(false);

  useEffect(() => {
    fetchAdminUsers();
    fetchRoles();
    fetchPermissions();
  }, []);

  const fetchRoles = async () => {
    setRolesLoading(true);
    try {
      const data = await activityLogService.getAllRoles();
      console.log('Roles data:', data);
      setRoles(data);
    } catch (error) {
      console.error('Error fetching roles:', error);
      message.error('Không thể tải danh sách vai trò');
      setRoles([]);
    } finally {
      setRolesLoading(false);
    }
  };

  const fetchPermissions = async () => {
    try {
      const data = await activityLogService.getAllPermissions();
      console.log('Permissions data:', data);
      setPermissions(data);
    } catch (error) {
      console.error('Error fetching permissions:', error);
      message.error('Không thể tải danh sách quyền');
      setPermissions([]);
    }
  };

  const fetchAdminUsers = async () => {
    setLoading(true);
    try {
      const data = await activityLogService.getAllAdminUsers();
      // Transform API data to match table format
      const transformedData = data.map((admin, index) => ({
        key: admin.id || index,
        id: admin.id,
        email: admin.email || 'N/A',
        name: admin.displayName || 'N/A',
        role: admin.roleName || admin.roleCode || 'N/A',
        roleCode: admin.roleCode,
        roleColor: admin.roleColor,
        permissions: admin.permissionNames || [],
        lastActive: formatLastActive(admin.lastActivityAt),
        status: admin.isActive ? 'Hoạt động' : 'Không hoạt động',
      }));
      setAdmins(transformedData);
    } catch (error) {
      console.error('Error fetching admin users:', error);
      message.error('Không thể tải danh sách admin');
      setAdmins([]);
    } finally {
      setLoading(false);
    }
  };

  const formatLastActive = (lastActivityAt) => {
    if (!lastActivityAt) return 'Chưa có hoạt động';
    const date = new Date(lastActivityAt);
    const now = new Date();
    const diff = now - date;
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);
    const days = Math.floor(diff / 86400000);

    if (minutes < 60) return `${minutes} phút trước`;
    if (hours < 24) return `${hours} giờ trước`;
    return `${days} ngày trước`;
  };

  const handleAddAdmin = async (formData) => {
    try {
      // Validate form data
      if (!formData.email || !formData.name || !formData.password) {
        message.error('Vui lòng điền đầy đủ thông tin bắt buộc');
        return;
      }

      if (formData.password !== formData.confirmPassword) {
        message.error('Mật khẩu xác nhận không khớp');
        return;
      }

      if (!formData.roleCode) {
        message.error('Vui lòng chọn vai trò');
        return;
      }

      setLoading(true);

      // Create admin user with selected role code
      await activityLogService.createAdminUser({
        username: formData.name,
        email: formData.email,
        password: formData.password,
        fullName: formData.fullName || formData.name,
        roleCode: formData.roleCode,
      });

      // Refresh list after adding
      await fetchAdminUsers();
      
      // Close modal and show success message
      setAddModalVisible(false);
      setSuccessMessage('Thêm Admin thành công!');
      setSuccessModalVisible(true);
    } catch (error) {
      console.error('Error creating admin:', error);
      const errorMessage = error.response?.data?.message || error.message || 'Không thể tạo admin';
      message.error('Lỗi: ' + errorMessage);
      // Don't close modal on error - let user fix the issue
    } finally {
      setLoading(false);
    }
  };

  const handleViewAdmin = async (admin) => {
    try {
      setLoading(true);
      // Fetch detailed admin info from API
      const detailData = await activityLogService.getAdminUserDetail(admin.id);
      console.log('Admin detail:', detailData);
      setSelectedAdmin(detailData);
      setDetailModalVisible(true);
    } catch (error) {
      console.error('Error fetching admin detail:', error);
      message.error('Không thể tải thông tin chi tiết admin');
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteClick = (admin) => {
    setAdminToDelete(admin);
    setConfirmModalVisible(true);
  };

  const handleConfirmDelete = async () => {
    try {
      // TODO: Backend chưa có endpoint DELETE admin user
      // Tạm thời sử dụng message để thông báo
      message.warning('Chức năng xóa admin đang được phát triển. Vui lòng liên hệ Super Admin để revoke quyền.');
      setConfirmModalVisible(false);
      setAdminToDelete(null);
      
      /* Code sẽ dùng khi backend có endpoint:
      await activityLogService.removeAdminUser(adminToDelete.id);
      await fetchAdminUsers();
      setConfirmModalVisible(false);
      setAdminToDelete(null);
      setSuccessMessage('Xóa Admin thành công!');
      setSuccessModalVisible(true);
      */
    } catch (error) {
      console.error('Error deleting admin:', error);
      message.error('Không thể xóa admin: ' + (error.response?.data?.message || error.message));
      setConfirmModalVisible(false);
      setAdminToDelete(null);
    }
  };

  const columns = [
    {
      title: 'Email',
      dataIndex: 'email',
      key: 'email',
      width: 200,
      render: (text) => <span style={{ fontSize: 14, color: '#2563eb', cursor: 'pointer' }}>{text}</span>,
    },
    {
      title: 'Tên',
      dataIndex: 'name',
      key: 'name',
      width: 150,
      render: (text) => <span style={{ fontSize: 14, color: '#111827' }}>{text}</span>,
    },
    {
      title: 'Vai trò',
      dataIndex: 'role',
      key: 'role',
      width: 120,
      render: (role) => (
        <Tag
          style={{
            background: '#dbeafe',
            color: '#2563eb',
            border: 'none',
            borderRadius: 6,
            fontSize: 12,
            fontWeight: 500,
            padding: '2px 10px',
          }}
        >
          {role}
        </Tag>
      ),
    },
    {
      title: 'Quyền',
      dataIndex: 'permissions',
      key: 'permissions',
      width: 300,
      render: (permissions) => (
        <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
          {permissions.map((permission, index) => (
            <Tag
              key={index}
              style={{
                background: '#dcfce7',
                color: '#16a34a',
                border: 'none',
                borderRadius: 6,
                fontSize: 12,
                fontWeight: 500,
                padding: '2px 10px',
              }}
            >
              {permission}
            </Tag>
          ))}
        </div>
      ),
    },
    {
      title: 'Hoạt động lần cuối',
      dataIndex: 'lastActive',
      key: 'lastActive',
      width: 150,
      render: (text) => <span style={{ fontSize: 14, color: '#6b7280' }}>{text}</span>,
    },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      key: 'status',
      width: 140,
      render: (status) => {
        const isActive = status === 'Hoạt động';
        return (
          <Tag
            style={{
              background: isActive ? '#dcfce7' : '#f3f4f6',
              color: isActive ? '#16a34a' : '#6b7280',
              border: 'none',
              borderRadius: 6,
              fontSize: 12,
              fontWeight: 500,
              padding: '2px 10px',
            }}
          >
            {status}
          </Tag>
        );
      },
    },
    {
      title: 'Thao tác',
      key: 'action',
      width: 120,
      align: 'center',
      render: (_, record) => (
        <div style={{ display: 'flex', gap: 8, justifyContent: 'center' }}>
          <Button
            type="text"
            icon={<EyeOutlined style={{ fontSize: 16 }} />}
            onClick={() => handleViewAdmin(record)}
            style={{ color: '#111827' }}
          />
          <Button
            type="text"
            icon={<DeleteOutlined style={{ fontSize: 16 }} />}
            onClick={() => handleDeleteClick(record)}
            style={{ color: '#dc2626' }}
          />
        </div>
      ),
    },
  ];

  return (
    <Card
      style={{
        borderRadius: 12,
        border: '1px solid #e5e7eb',
        boxShadow: 'none',
        background: '#fff',
      }}
      bodyStyle={{ padding: 0 }}
    >
      <div
        style={{
          padding: '20px 24px',
          borderBottom: '1px solid #e5e7eb',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
        }}
      >
        <div>
          <h3 style={{ fontSize: 16, fontWeight: 600, color: '#111827', margin: 0, marginBottom: 4 }}>
            Vai trò & Phân quyền Admin
          </h3>
          <p style={{ fontSize: 14, color: '#6b7280', margin: 0 }}>
            Quản lý phân quyền admin
          </p>
        </div>
        <Button
          type="primary"
          icon={<PlusOutlined />}
          onClick={() => setAddModalVisible(true)}
          style={{
            background: '#2563eb',
            border: 'none',
            borderRadius: 8,
            height: 36,
            padding: '0 16px',
            fontSize: 14,
            fontWeight: 500,
          }}
        >
          Thêm Admin
        </Button>
      </div>
      <Spin spinning={loading}>
        <Table
          columns={columns}
          dataSource={admins}
          pagination={{
            pageSize: 10,
            showSizeChanger: true,
            showTotal: (total) => `Tổng ${total} admin`,
          }}
          tableLayout="fixed"
        />
      </Spin>

      <AddAdminModal
        visible={addModalVisible}
        onClose={() => setAddModalVisible(false)}
        onAdd={handleAddAdmin}
        roles={roles}
        permissions={permissions}
        loading={rolesLoading}
      />

      <AdminDetailModal
        visible={detailModalVisible}
        onClose={() => setDetailModalVisible(false)}
        admin={selectedAdmin}
      />

      <ConfirmModal
        open={confirmModalVisible}
        onConfirm={handleConfirmDelete}
        onCancel={() => {
          setConfirmModalVisible(false);
          setAdminToDelete(null);
        }}
        title="Xóa Admin"
        content="Bạn có chắc chắn muốn xóa admin này?"
        confirmText="Xác nhận"
        cancelText="Hủy"
        danger={true}
        adminInfo={adminToDelete}
      />

      <SuccessModal
        open={successModalVisible}
        onClose={() => setSuccessModalVisible(false)}
        message={successMessage}
        buttonText="Đồng ý"
      />
    </Card>
  );
};

export default PermissionTab;
