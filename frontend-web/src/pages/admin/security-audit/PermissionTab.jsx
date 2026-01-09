import React, { useState, useEffect } from 'react';
import { Card, Table, Button, Tag, message, Spin, Tabs } from 'antd';
import { PlusOutlined, EyeOutlined, DeleteOutlined, EditOutlined } from '@ant-design/icons';
import AddAdminModal from '../../../components/admin/AddAdminModal';
import AdminDetailModal from '../../../components/admin/AdminDetailModal';
import AddRoleModal from '../../../components/admin/AddRoleModal';
import EditRoleModal from '../../../components/admin/EditRoleModal';
import RoleDetailModal from '../../../components/admin/RoleDetailModal';
import SuccessModal from '../../../components/common/SuccessModal';
import ConfirmModal from '../../../components/common/ConfirmModal';
import activityLogService from '../../../services/activityLogService';

const PermissionTab = () => {
  const [activeTab, setActiveTab] = useState('admins');
  
  // Admin states
  const [addModalVisible, setAddModalVisible] = useState(false);
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [successModalVisible, setSuccessModalVisible] = useState(false);
  const [confirmModalVisible, setConfirmModalVisible] = useState(false);
  const [selectedAdmin, setSelectedAdmin] = useState(null);
  const [adminToDelete, setAdminToDelete] = useState(null);
  const [successMessage, setSuccessMessage] = useState('');
  const [admins, setAdmins] = useState([]);
  const [loading, setLoading] = useState(false);
  
  // Role states
  const [addRoleModalVisible, setAddRoleModalVisible] = useState(false);
  const [editRoleModalVisible, setEditRoleModalVisible] = useState(false);
  const [roleDetailModalVisible, setRoleDetailModalVisible] = useState(false);
  const [confirmRoleDeleteVisible, setConfirmRoleDeleteVisible] = useState(false);
  const [selectedRole, setSelectedRole] = useState(null);
  const [roleToDelete, setRoleToDelete] = useState(null);
  const [rolesData, setRolesData] = useState([]);
  const [rolesLoading, setRolesLoading] = useState(false);
  
  // Common states
  const [roles, setRoles] = useState([]);
  const [permissions, setPermissions] = useState([]);

  useEffect(() => {
    fetchAdminUsers();
    fetchRoles();
    fetchPermissions();
    fetchRolesData();
  }, []);

  const fetchRoles = async () => {
    try {
      const data = await activityLogService.getAllRoles();
      console.log('Roles data:', data);
      setRoles(data);
    } catch (error) {
      console.error('Error fetching roles:', error);
      message.error('Không thể tải danh sách vai trò');
      setRoles([]);
    }
  };

  const fetchRolesData = async () => {
    setRolesLoading(true);
    try {
      const data = await activityLogService.getAllRoles();
      console.log('Roles data for table:', data);
      // Transform to table format
      const transformedRoles = data.map((role, index) => ({
        key: role.id || index,
        id: role.id,
        roleCode: role.roleCode,
        roleName: role.roleName,
        description: role.description,
        color: role.color,
        displayOrder: role.displayOrder,
        permissionCodes: role.permissionCodes || [],
        permissionNames: role.permissionNames || [],
        adminCount: role.adminCount || 0,
        isActive: role.isActive !== false,
      }));
      setRolesData(transformedRoles);
    } catch (error) {
      console.error('Error fetching roles data:', error);
      message.error('Không thể tải danh sách vai trò');
      setRolesData([]);
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

  // Role handlers
  const handleAddRole = async (formData) => {
    try {
      setRolesLoading(true);
      await activityLogService.createRole(formData);
      await fetchRolesData();
      await fetchRoles(); // Refresh roles for admin dropdown
      setAddRoleModalVisible(false);
      setSuccessMessage('Thêm vai trò thành công!');
      setSuccessModalVisible(true);
    } catch (error) {
      console.error('Error creating role:', error);
      const errorMessage = error.response?.data?.message || error.message || 'Không thể tạo vai trò';
      message.error('Lỗi: ' + errorMessage);
    } finally {
      setRolesLoading(false);
    }
  };

  const handleViewRole = async (role) => {
    try {
      setRolesLoading(true);
      const detailData = await activityLogService.getRoleById(role.id);
      console.log('Role detail:', detailData);
      setSelectedRole(detailData);
      setRoleDetailModalVisible(true);
    } catch (error) {
      console.error('Error fetching role detail:', error);
      message.error('Không thể tải thông tin chi tiết vai trò');
    } finally {
      setRolesLoading(false);
    }
  };

  const handleEditRole = async (role) => {
    try {
      setRolesLoading(true);
      const detailData = await activityLogService.getRoleById(role.id);
      console.log('Role to edit:', detailData);
      setSelectedRole(detailData);
      setEditRoleModalVisible(true);
    } catch (error) {
      console.error('Error fetching role for edit:', error);
      message.error('Không thể tải thông tin vai trò');
    } finally {
      setRolesLoading(false);
    }
  };

  const handleUpdateRole = async (roleId, formData) => {
    try {
      setRolesLoading(true);
      await activityLogService.updateRole(roleId, formData);
      await fetchRolesData();
      await fetchRoles(); // Refresh roles for admin dropdown
      setEditRoleModalVisible(false);
      setSelectedRole(null);
      setSuccessMessage('Cập nhật vai trò thành công!');
      setSuccessModalVisible(true);
    } catch (error) {
      console.error('Error updating role:', error);
      const errorMessage = error.response?.data?.message || error.message || 'Không thể cập nhật vai trò';
      message.error('Lỗi: ' + errorMessage);
    } finally {
      setRolesLoading(false);
    }
  };

  const handleDeleteRoleClick = (role) => {
    setRoleToDelete(role);
    setConfirmRoleDeleteVisible(true);
  };

  const handleConfirmDeleteRole = async () => {
    try {
      setRolesLoading(true);
      await activityLogService.deleteRole(roleToDelete.id);
      await fetchRolesData();
      await fetchRoles(); // Refresh roles for admin dropdown
      setConfirmRoleDeleteVisible(false);
      setRoleToDelete(null);
      setSuccessMessage('Xóa vai trò thành công!');
      setSuccessModalVisible(true);
    } catch (error) {
      console.error('Error deleting role:', error);
      const errorMessage = error.response?.data?.message || error.message || 'Không thể xóa vai trò';
      message.error('Lỗi: ' + errorMessage);
      setConfirmRoleDeleteVisible(false);
      setRoleToDelete(null);
    } finally {
      setRolesLoading(false);
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

  // Role columns
  const roleColumns = [
    {
      title: 'Mã vai trò',
      dataIndex: 'roleCode',
      key: 'roleCode',
      width: 150,
      render: (text) => (
        <Tag
          style={{
            background: '#dbeafe',
            color: '#2563eb',
            border: 'none',
            borderRadius: 6,
            fontSize: 13,
            fontWeight: 500,
            padding: '4px 12px',
          }}
        >
          {text}
        </Tag>
      ),
    },
    {
      title: 'Tên vai trò',
      dataIndex: 'roleName',
      key: 'roleName',
      width: 180,
      render: (text) => <span style={{ fontSize: 14, color: '#111827', fontWeight: 500 }}>{text}</span>,
    },
    {
      title: 'Mô tả',
      dataIndex: 'description',
      key: 'description',
      width: 250,
      render: (text) => (
        <span style={{ fontSize: 14, color: '#6b7280' }}>
          {text || 'Không có mô tả'}
        </span>
      ),
    },
    {
      title: 'Màu sắc',
      dataIndex: 'color',
      key: 'color',
      width: 100,
      align: 'center',
      render: (color) => (
        <div
          style={{
            width: 40,
            height: 24,
            borderRadius: 6,
            background: color,
            border: '1px solid #e5e7eb',
            margin: '0 auto',
          }}
        />
      ),
    },
    {
      title: 'Quyền',
      dataIndex: 'permissionNames',
      key: 'permissionNames',
      width: 300,
      render: (permissions) => (
        <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
          {(permissions || []).slice(0, 3).map((permission, index) => (
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
          {permissions && permissions.length > 3 && (
            <Tag
              style={{
                background: '#f3f4f6',
                color: '#6b7280',
                border: 'none',
                borderRadius: 6,
                fontSize: 12,
                fontWeight: 500,
                padding: '2px 10px',
              }}
            >
              +{permissions.length - 3}
            </Tag>
          )}
        </div>
      ),
    },
    {
      title: 'Số Admin',
      dataIndex: 'adminCount',
      key: 'adminCount',
      width: 100,
      align: 'center',
      render: (count) => <span style={{ fontSize: 14, color: '#111827', fontWeight: 500 }}>{count}</span>,
    },
    {
      title: 'Trạng thái',
      dataIndex: 'isActive',
      key: 'isActive',
      width: 120,
      render: (isActive) => (
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
          {isActive ? 'Hoạt động' : 'Không hoạt động'}
        </Tag>
      ),
    },
    {
      title: 'Thao tác',
      key: 'action',
      width: 150,
      align: 'center',
      render: (_, record) => (
        <div style={{ display: 'flex', gap: 8, justifyContent: 'center' }}>
          <Button
            type="text"
            icon={<EyeOutlined style={{ fontSize: 16 }} />}
            onClick={() => handleViewRole(record)}
            style={{ color: '#111827' }}
          />
          <Button
            type="text"
            icon={<EditOutlined style={{ fontSize: 16 }} />}
            onClick={() => handleEditRole(record)}
            style={{ color: '#2563eb' }}
          />
          <Button
            type="text"
            icon={<DeleteOutlined style={{ fontSize: 16 }} />}
            onClick={() => handleDeleteRoleClick(record)}
            style={{ color: '#dc2626' }}
            disabled={record.roleCode === 'SUPER_ADMIN'}
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
      <Tabs
        activeKey={activeTab}
        onChange={setActiveTab}
        style={{ padding: '0 24px' }}
        items={[
          {
            key: 'admins',
            label: (
              <span style={{ fontSize: 15, fontWeight: 500 }}>
                Quản lý Admin
              </span>
            ),
            children: (
              <>
                <div
                  style={{
                    padding: '20px 0',
                    borderBottom: '1px solid #e5e7eb',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                  }}
                >
                  <div>
                    <h3 style={{ fontSize: 16, fontWeight: 600, color: '#111827', margin: 0, marginBottom: 4 }}>
                      Danh sách Admin
                    </h3>
                    <p style={{ fontSize: 14, color: '#6b7280', margin: 0 }}>
                      Quản lý tài khoản admin và phân quyền
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
              </>
            ),
          },
          {
            key: 'roles',
            label: (
              <span style={{ fontSize: 15, fontWeight: 500 }}>
                Quản lý Vai trò
              </span>
            ),
            children: (
              <>
                <div
                  style={{
                    padding: '20px 0',
                    borderBottom: '1px solid #e5e7eb',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                  }}
                >
                  <div>
                    <h3 style={{ fontSize: 16, fontWeight: 600, color: '#111827', margin: 0, marginBottom: 4 }}>
                      Danh sách Vai trò
                    </h3>
                    <p style={{ fontSize: 14, color: '#6b7280', margin: 0 }}>
                      Quản lý vai trò và quyền hạn
                    </p>
                  </div>
                  <Button
                    type="primary"
                    icon={<PlusOutlined />}
                    onClick={() => setAddRoleModalVisible(true)}
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
                    Thêm Vai trò
                  </Button>
                </div>
                <Spin spinning={rolesLoading}>
                  <Table
                    columns={roleColumns}
                    dataSource={rolesData}
                    pagination={{
                      pageSize: 10,
                      showSizeChanger: true,
                      showTotal: (total) => `Tổng ${total} vai trò`,
                    }}
                    tableLayout="fixed"
                  />
                </Spin>
              </>
            ),
          },
        ]}
      />

      {/* Admin Modals */}
      <AddAdminModal
        visible={addModalVisible}
        onClose={() => setAddModalVisible(false)}
        onAdd={handleAddAdmin}
        roles={roles}
        permissions={permissions}
        loading={loading}
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

      {/* Role Modals */}
      <AddRoleModal
        visible={addRoleModalVisible}
        onClose={() => setAddRoleModalVisible(false)}
        onAdd={handleAddRole}
        permissions={permissions}
        loading={rolesLoading}
      />

      <EditRoleModal
        visible={editRoleModalVisible}
        onClose={() => {
          setEditRoleModalVisible(false);
          setSelectedRole(null);
        }}
        onUpdate={handleUpdateRole}
        role={selectedRole}
        permissions={permissions}
        loading={rolesLoading}
      />

      <RoleDetailModal
        visible={roleDetailModalVisible}
        onClose={() => {
          setRoleDetailModalVisible(false);
          setSelectedRole(null);
        }}
        role={selectedRole}
      />

      <ConfirmModal
        open={confirmRoleDeleteVisible}
        onConfirm={handleConfirmDeleteRole}
        onCancel={() => {
          setConfirmRoleDeleteVisible(false);
          setRoleToDelete(null);
        }}
        title="Xóa Vai trò"
        content={`Bạn có chắc chắn muốn xóa vai trò "${roleToDelete?.roleName}"? Hành động này không thể hoàn tác.`}
        confirmText="Xác nhận xóa"
        cancelText="Hủy"
        danger={true}
      />

      {/* Success Modal */}
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
