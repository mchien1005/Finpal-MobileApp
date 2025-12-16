import React, { useState } from 'react';
import { Typography, Row, Col, Card, Table, Input, Button, Tag, Avatar, Dropdown, Space } from 'antd';
import {
  SearchOutlined,
  FilterOutlined,
  DownloadOutlined,
  DownOutlined,
  EyeOutlined,
  MoreOutlined,
} from '@ant-design/icons';
import AdminLayout from '../../layouts/AdminLayout';

const { Title, Text } = Typography;

const UserManagementPage = () => {
  const [searchText, setSearchText] = useState('');
  const [selectedStatus, setSelectedStatus] = useState('Tất cả');

  // Stats cards data
  const statsCards = [
    {
      title: 'Tổng người dùng',
      value: '12,543',
      change: '+12.5% từ tháng trước',
      changeColor: '#00a63e',
    },
    {
      title: 'Active Users',
      value: '11,234',
      change: '89.5% tổng số',
      changeColor: '#00a63e',
    },
    {
      title: 'Người dùng mới (tháng này)',
      value: '1,456',
      change: '+8.2% so tháng trước',
      changeColor: '#00a63e',
    },
    {
      title: 'Tài khoản bị khóa',
      value: '89',
      change: '0.7% tổng số',
      changeColor: '#e7000b',
    },
  ];

  // Table columns
  const columns = [
    {
      title: 'User ID',
      dataIndex: 'userId',
      key: 'userId',
      width: 80,
      render: (text) => <Text style={{ color: '#155dfc' }}>{text}</Text>,
    },
    {
      title: 'Người dùng',
      dataIndex: 'user',
      key: 'user',
      width: 180,
      render: (user) => (
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <Avatar
            style={{
              background: 'linear-gradient(135deg, #51a2ff 0%, #615fff 100%)',
            }}
            size={32}
          >
            {user.name.charAt(0)}
          </Avatar>
          <Text>{user.name}</Text>
        </div>
      ),
    },
    {
      title: 'Liên hệ',
      dataIndex: 'contact',
      key: 'contact',
      width: 200,
      render: (contact) => (
        <div>
          <div><Text>{contact.email}</Text></div>
          <div><Text type="secondary" style={{ fontSize: 14 }}>{contact.phone}</Text></div>
        </div>
      ),
    },
    {
      title: 'Ngân hàng',
      dataIndex: 'bank',
      key: 'bank',
      width: 100,
      render: (bank) => (
        <Tag
          style={{
            background: '#dbeafe',
            color: '#1447e6',
            border: 'none',
            borderRadius: 8,
          }}
        >
          {bank}
        </Tag>
      ),
    },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status) => {
        const statusConfig = {
          Active: { bg: '#dcfce7', color: '#008236' },
          Inactive: { bg: '#f3f4f6', color: '#364153' },
          Banned: { bg: '#ffe2e2', color: '#c10007' },
        };
        const config = statusConfig[status] || statusConfig.Active;
        return (
          <Tag
            style={{
              background: config.bg,
              color: config.color,
              border: 'none',
              borderRadius: 8,
            }}
          >
            {status}
          </Tag>
        );
      },
    },
    {
      title: 'Giao dịch',
      dataIndex: 'transactions',
      key: 'transactions',
      width: 100,
    },
    {
      title: 'Tổng chi tiêu',
      dataIndex: 'totalSpending',
      key: 'totalSpending',
      width: 120,
    },
    {
      title: 'Ngày đăng ký',
      dataIndex: 'registeredDate',
      key: 'registeredDate',
      width: 120,
    },
    {
      title: 'Hoạt động',
      dataIndex: 'lastActive',
      key: 'lastActive',
      width: 120,
      render: (text) => <Text type="secondary">{text}</Text>,
    },
    {
      title: 'Thao tác',
      key: 'action',
      width: 100,
      align: 'right',
      render: (_, record) => (
        <Button
          type="text"
          icon={<EyeOutlined />}
          style={{ color: '#0a0a0a' }}
        />
      ),
    },
  ];

  // Table data
  const dataSource = [
    {
      key: '1',
      userId: 'USR001',
      user: { name: 'Nguyễn Văn A' },
      contact: { email: 'nguyenvana@gmail.com', phone: '0901234567' },
      bank: 'VCB',
      status: 'Active',
      transactions: '245',
      totalSpending: '₫45.6M',
      registeredDate: '15/03/2024',
      lastActive: '2 giờ trước',
    },
    {
      key: '2',
      userId: 'USR002',
      user: { name: 'Trần Thị B' },
      contact: { email: 'tranthib@gmail.com', phone: '0907654321' },
      bank: 'TCB',
      status: 'Active',
      transactions: '189',
      totalSpending: '₫38.9M',
      registeredDate: '20/03/2024',
      lastActive: '5 phút trước',
    },
    {
      key: '3',
      userId: 'USR003',
      user: { name: 'Lê Văn C' },
      contact: { email: 'levanc@gmail.com', phone: '0912345678' },
      bank: 'ACB',
      status: 'Inactive',
      transactions: '67',
      totalSpending: '₫12.3M',
      registeredDate: '10/02/2024',
      lastActive: '3 ngày trước',
    },
    {
      key: '4',
      userId: 'USR004',
      user: { name: 'Phạm Thị D' },
      contact: { email: 'phamthid@gmail.com', phone: '0909876543' },
      bank: 'VTB',
      status: 'Active',
      transactions: '312',
      totalSpending: '₫67.8M',
      registeredDate: '25/03/2024',
      lastActive: '1 giờ trước',
    },
    {
      key: '5',
      userId: 'USR005',
      user: { name: 'Hoàng Văn E' },
      contact: { email: 'hoangvane@gmail.com', phone: '0903456789' },
      bank: 'MBB',
      status: 'Banned',
      transactions: '45',
      totalSpending: '₫8.9M',
      registeredDate: '05/01/2024',
      lastActive: '1 tuần trước',
    },
  ];

  const statusMenuItems = [
    { key: 'all', label: 'Tất cả' },
    { key: 'active', label: 'Active' },
    { key: 'inactive', label: 'Inactive' },
    { key: 'banned', label: 'Banned' },
  ];

  return (
    <AdminLayout>
      {/* Header */}
        <div style={{ marginBottom: 24 }}>
          <Title level={2} style={{ margin: 0, marginBottom: 4, color: '#101828' }}>
            Quản lý người dùng
          </Title>
          <Text style={{ color: '#6a7282', fontSize: 16 }}>
            Xem và quản lý tất cả người dùng trong hệ thống
          </Text>
        </div>

        {/* Stats Cards */}
        <Row gutter={[24, 24]} style={{ marginBottom: 24 }}>
          {statsCards.map((card, index) => (
            <Col xs={24} sm={12} lg={6} key={index}>
              <Card
                style={{
                  borderRadius: 14,
                  border: '1px solid rgba(0, 0, 0, 0.1)',
                  boxShadow: 'none',
                }}
                bodyStyle={{ padding: 24 }}
              >
                <div style={{ marginBottom: 32 }}>
                  <Text style={{ color: '#4a5565', fontSize: 14, display: 'block' }}>
                    {card.title}
                  </Text>
                </div>
                <div style={{ marginBottom: 32 }}>
                  <Text style={{ fontSize: 16, color: '#101828', display: 'block' }}>
                    {card.value}
                  </Text>
                </div>
                <div>
                  <Text style={{ color: card.changeColor, fontSize: 14 }}>
                    {card.change}
                  </Text>
                </div>
              </Card>
            </Col>
          ))}
        </Row>

        {/* Search and Filters */}
        <Card
          style={{
            borderRadius: 14,
            border: '1px solid rgba(0, 0, 0, 0.1)',
            boxShadow: 'none',
            marginBottom: 24,
          }}
          bodyStyle={{ padding: 24 }}
        >
          <Row gutter={12} align="middle">
            <Col style={{ width: 540 }}>
              <Input
                placeholder="Tìm kiếm theo tên, email, ID..."
                prefix={<SearchOutlined style={{ color: '#717182' }} />}
                value={searchText}
                onChange={(e) => setSearchText(e.target.value)}
                style={{
                  background: '#f3f3f5',
                  border: 'none',
                  borderRadius: 8,
                  height: 36,
                }}
              />
            </Col>
            <Col>
              <Dropdown
                menu={{
                  items: statusMenuItems,
                  onClick: ({ key }) => setSelectedStatus(statusMenuItems.find(item => item.key === key)?.label || 'Tất cả'),
                }}
                trigger={['click']}
              >
                <Button
                  style={{
                    background: '#f3f3f5',
                    border: 'none',
                    borderRadius: 8,
                    height: 36,
                  }}
                >
                  <Space>
                    {selectedStatus}
                    <DownOutlined />
                  </Space>
                </Button>
              </Dropdown>
            </Col>
            <Col>
              <Button
                icon={<FilterOutlined />}
                style={{
                  borderRadius: 8,
                  height: 36,
                }}
              >
                Lọc nâng cao
              </Button>
            </Col>
            <Col>
              <Button
                type="primary"
                icon={<DownloadOutlined />}
                style={{
                  background: '#155dfc',
                  borderRadius: 8,
                  height: 36,
                }}
              >
                Export CSV
              </Button>
            </Col>
          </Row>
        </Card>

        {/* Users Table */}
        <Card
          style={{
            borderRadius: 14,
            border: '1px solid rgba(0, 0, 0, 0.1)',
            boxShadow: 'none',
          }}
          bodyStyle={{ padding: 0 }}
        >
          <Table
            columns={columns}
            dataSource={dataSource}
            pagination={{
              current: 1,
              pageSize: 5,
              total: 12543,
              showSizeChanger: false,
              showTotal: (total) => `Hiển thị 1-5 trong tổng số ${total.toLocaleString()} người dùng`,
              style: { padding: '24px' },
            }}
            style={{ borderRadius: 14 }}
          />
        </Card>
    </AdminLayout>
  );
};

export default UserManagementPage;