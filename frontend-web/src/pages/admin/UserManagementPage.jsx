import React, { useState } from 'react';
import { Typography, Row, Col, Card, Table, Input, Button, Tag, Avatar, Dropdown, Space, DatePicker } from 'antd';
import {
  SearchOutlined,
  FilterOutlined,
  DownloadOutlined,
  DownOutlined,
  EyeOutlined,
  MoreOutlined,
} from '@ant-design/icons';
import AdminSidebar from '../../components/admin/AdminSidebar';
import { useSidebar } from '../../contexts/SidebarContext';
import { useUserSearch } from '../../hooks/useUserSearch';
import UserDetailModal from '../../components/admin/UserDetailModal';

const { Title, Text } = Typography;

const UserManagementPage = () => {
  const { collapsed } = useSidebar();
  const [showAdvancedFilter, setShowAdvancedFilter] = useState(false);
  const [selectedUser, setSelectedUser] = useState(null);
  const [userDetailVisible, setUserDetailVisible] = useState(false);

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
      width: 100,
      align: 'center',
      render: (text) => <Text style={{ color: '#155dfc' }}>{text}</Text>,
    },
    {
      title: 'Người dùng',
      dataIndex: 'user',
      key: 'user',
      width: 200,
      align: 'center',
      render: (user) => (
        <div style={{ display: 'flex', alignItems: 'center', gap: 12, width: '100%' }}>
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
      width: 220,
      align: 'center',
      render: (contact) => (
        <div>
          <div style={{ marginBottom: 4 }}><Text>{contact.email}</Text></div>
          <div><Text type="secondary" style={{ fontSize: 12 }}>{contact.phone}</Text></div>
        </div>
      ),
    },
    {
      title: 'Ngân hàng',
      dataIndex: 'bank',
      key: 'bank',
      width: 100,
      align: 'center',
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
      width: 110,
      align: 'center',
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
      align: 'center',
    },
    {
      title: 'Tổng chi tiêu',
      dataIndex: 'totalSpending',
      key: 'totalSpending',
      width: 130,
      align: 'center',
    },
    {
      title: 'Ngày đăng ký',
      dataIndex: 'registeredDate',
      key: 'registeredDate',
      width: 120,
      align: 'center',
    },
    {
      title: 'Hoạt động',
      dataIndex: 'lastActive',
      key: 'lastActive',
      width: 130,
      align: 'center',
      render: (text) => <Text type="secondary" style={{ fontSize: 14 }}>{text}</Text>,
    },
    {
      title: 'Thao tác',
      key: 'action',
      width: 100,
      align: 'center',
      render: (_, record) => (
        <Button
          onClick={() => {
            setSelectedUser(record);
            setUserDetailVisible(true);
          }}
          type="text"
          icon={<EyeOutlined />}
          style={{ color: '#0a0a0a' }}
        />
      ),
    },
  ];

  // Table data - convert to state so we can delete users
  const [dataSource, setDataSource] = useState([
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
  ]);

  const statusMenuItems = [
    { key: 'all', label: 'Tất cả' },
    { key: 'active', label: 'Active' },
    { key: 'inactive', label: 'Inactive' },
    { key: 'banned', label: 'Banned' },
  ];

  // Use custom search hook
  const {
    searchText,
    setSearchText,
    selectedStatus,
    setSelectedStatus,
    advancedFilters,
    setAdvancedFilters,
    filteredUsers,
    totalFiltered,
  } = useUserSearch(dataSource);

  const handleApplyFilters = (filters) => {
    setAdvancedFilters(filters);
  };

  const handleToggleAdvancedFilter = () => {
    setShowAdvancedFilter(!showAdvancedFilter);
  };

  const handleDeleteUser = (userId) => {
    // Remove user from dataSource
    setDataSource(prev => prev.filter(user => user.userId !== userId));
  };

  const handleDisableUser = (userId) => {
    // Update user status to Inactive
    setDataSource(prev => prev.map(user => 
      user.userId === userId ? { ...user, status: 'Inactive' } : user
    ));
  };

  return (
    <div style={{ display: 'flex', minHeight: '100vh', background: '#F9FAFB' }}>
      <AdminSidebar />

      {/* Main Content */}
      <div
        style={{
          marginLeft: collapsed ? 80 : 280,
          flex: 1,
          transition: 'margin-left 0.3s',
          padding: 32,
          background: 'linear-gradient(142deg, #F9FAFB 0%, rgba(239, 246, 255, 0.3) 100%)',
        }}
      >
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
          <Row gutter={16} align="middle">
            <Col style={{ width: 480 }}>
              <Input
                placeholder="Tìm kiếm theo tên, email, ID..."
                prefix={<SearchOutlined style={{ color: '#717182' }} />}
                value={searchText}
                onChange={(e) => setSearchText(e.target.value)}
                style={{
                  background: '#f3f3f5',
                  border: 'none',
                  borderRadius: 8,
                  height: 40,
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
                    height: 40,
                    minWidth: 130,
                    padding: '0 20px',
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
                onClick={handleToggleAdvancedFilter}
                style={{
                  borderRadius: 8,
                  height: 40,
                  padding: '0 20px',
                  background: showAdvancedFilter ? '#155dfc' : 'transparent',
                  color: showAdvancedFilter ? '#fff' : '#000',
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
                  height: 40,
                  padding: '0 20px',
                }}
              >
                Export CSV
              </Button>
            </Col>
          </Row>

          {/* Advanced Filter Panel - show below when toggled */}
          {showAdvancedFilter && (
            <div style={{ marginTop: 24, paddingTop: 24, borderTop: '1px solid #e5e7eb' }}>
              <Row gutter={[16, 16]}>
                <Col xs={24} sm={12}>
                  <div style={{ marginBottom: 8 }}>
                    <label style={{ fontSize: 14, fontWeight: 500, color: '#101828' }}>
                      Ngân hàng
                    </label>
                  </div>
                  <Dropdown
                    menu={{
                      items: [
                        { key: 'all', label: 'Tất cả' },
                        { key: 'VCB', label: 'VCB' },
                        { key: 'TCB', label: 'TCB' },
                        { key: 'ACB', label: 'ACB' },
                        { key: 'VTB', label: 'VTB' },
                        { key: 'MBB', label: 'MBB' },
                      ],
                      onClick: ({ key }) => {
                        setAdvancedFilters({
                          ...advancedFilters,
                          bank: key === 'all' ? null : key
                        });
                      },
                    }}
                    trigger={['click']}
                  >
                    <Button
                      style={{
                        width: '100%',
                        height: 40,
                        background: '#f3f3f5',
                        border: 'none',
                        borderRadius: 8,
                        textAlign: 'left',
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                      }}
                    >
                      <span>{advancedFilters.bank || 'VCB'}</span>
                      <DownOutlined />
                    </Button>
                  </Dropdown>
                </Col>

                <Col xs={24} sm={12}>
                  <div style={{ marginBottom: 8 }}>
                    <label style={{ fontSize: 14, fontWeight: 500, color: '#101828' }}>
                      Tổng giao dịch tối thiểu
                    </label>
                  </div>
                  <Input
                    placeholder="245"
                    type="number"
                    value={advancedFilters.minTransactions}
                    onChange={(e) => setAdvancedFilters({ ...advancedFilters, minTransactions: e.target.value })}
                    style={{
                      height: 40,
                      background: '#f3f3f5',
                      border: 'none',
                      borderRadius: 8,
                    }}
                  />
                </Col>

                <Col xs={24} sm={12}>
                  <div style={{ marginBottom: 8 }}>
                    <label style={{ fontSize: 14, fontWeight: 500, color: '#101828' }}>
                      Tổng chi tiêu tối thiểu (triệu)
                    </label>
                  </div>
                  <Input
                    placeholder="45"
                    type="number"
                    value={advancedFilters.minSpending}
                    onChange={(e) => setAdvancedFilters({ ...advancedFilters, minSpending: e.target.value })}
                    style={{
                      height: 40,
                      background: '#f3f3f5',
                      border: 'none',
                      borderRadius: 8,
                    }}
                  />
                </Col>

                <Col xs={24} sm={12}>
                  <div style={{ marginBottom: 8 }}>
                    <label style={{ fontSize: 14, fontWeight: 500, color: '#101828' }}>
                      Ngày đăng ký
                    </label>
                  </div>
                  <DatePicker
                    placeholder="15/03/2024"
                    value={advancedFilters.registeredDate}
                    onChange={(date) => setAdvancedFilters({ ...advancedFilters, registeredDate: date })}
                    format="DD/MM/YYYY"
                    style={{
                      width: '100%',
                      height: 40,
                      background: '#f3f3f5',
                      border: 'none',
                      borderRadius: 8,
                    }}
                  />
                </Col>
              </Row>
            </div>
          )}
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
            dataSource={filteredUsers}
            pagination={{
              current: 1,
              pageSize: 5,
              total: totalFiltered,
              showSizeChanger: false,
              showTotal: (total) => `Hiển thị 1-${Math.min(5, total)} trong tổng số ${total.toLocaleString()} người dùng`,
              style: { padding: '24px' },
            }}
            tableLayout="fixed"
            style={{ borderRadius: 14 }}
          />
        </Card>

        {/* User Detail Modal */}
        <UserDetailModal
          visible={userDetailVisible}
          onClose={() => setUserDetailVisible(false)}
          user={selectedUser}
          onDeleteSuccess={handleDeleteUser}
          onDisableSuccess={handleDisableUser}
        />
      </div>
    </div>
  );
};

export default UserManagementPage;