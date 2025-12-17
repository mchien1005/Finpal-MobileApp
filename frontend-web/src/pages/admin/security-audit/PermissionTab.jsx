import React, { useState } from 'react';
import { Card, Table, Button, Tag } from 'antd';
import { PlusOutlined, EyeOutlined, DeleteOutlined } from '@ant-design/icons';
import AddAdminModal from '../../../components/admin/AddAdminModal';
import AdminDetailModal from '../../../components/admin/AdminDetailModal';
import SuccessModal from '../../../components/common/SuccessModal';
import ConfirmModal from '../../../components/common/ConfirmModal';

const PermissionTab = () => {
  const [addModalVisible, setAddModalVisible] = useState(false);
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [successModalVisible, setSuccessModalVisible] = useState(false);
  const [confirmModalVisible, setConfirmModalVisible] = useState(false);
  const [selectedAdmin, setSelectedAdmin] = useState(null);
  const [adminToDelete, setAdminToDelete] = useState(null);
  const [successMessage, setSuccessMessage] = useState('');
  const [admins, setAdmins] = useState([
    {
      key: '1',
      email: 'moderator@finpal.com',
      name: 'Moderator',
      role: 'Moderator',
      permissions: ['View Users', 'Edit Content'],
      lastActive: '2 giờ trước',
      status: 'Hoạt động',
    },
    {
      key: '2',
      email: 'support@finpal.com',
      name: 'Support Team',
      role: 'Support',
      permissions: ['View Users', 'View Logs'],
      lastActive: '1 ngày trước',
      status: 'Không hoạt động',
    },
  ]);

  const handleAddAdmin = (formData) => {
    const newAdmin = {
      key: String(admins.length + 1),
      email: formData.email,
      name: formData.name,
      role: formData.role,
      permissions: formData.permissions.split(',').map((p) => p.trim()),
      lastActive: 'Vừa xong',
      status: 'Hoạt động',
    };
    setAdmins([...admins, newAdmin]);
    setAddModalVisible(false);
    setSuccessMessage('Thêm Admin thành công!');
    setSuccessModalVisible(true);
  };

  const handleViewAdmin = (admin) => {
    setSelectedAdmin(admin);
    setDetailModalVisible(true);
  };

  const handleDeleteClick = (admin) => {
    setAdminToDelete(admin);
    setConfirmModalVisible(true);
  };

  const handleConfirmDelete = () => {
    setAdmins(admins.filter((admin) => admin.key !== adminToDelete.key));
    setConfirmModalVisible(false);
    setAdminToDelete(null);
    setSuccessMessage('Xóa Admin thành công!');
    setSuccessModalVisible(true);
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
      <Table
        columns={columns}
        dataSource={admins}
        pagination={false}
        tableLayout="fixed"
      />

      <AddAdminModal
        visible={addModalVisible}
        onClose={() => setAddModalVisible(false)}
        onAdd={handleAddAdmin}
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
