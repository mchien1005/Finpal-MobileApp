import React, { useState, useEffect } from 'react';
import { Typography, Row, Col, Card, Table, Input, Button, Tag, Avatar, Dropdown, Space, DatePicker, message, Spin } from 'antd';
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
import userService from '../../services/userService';

const { Title, Text } = Typography;

const UserManagementPage = () => {
  const { collapsed } = useSidebar();
  const [showAdvancedFilter, setShowAdvancedFilter] = useState(false);
  const [selectedUser, setSelectedUser] = useState(null);
  const [userDetailVisible, setUserDetailVisible] = useState(false);
  const [loading, setLoading] = useState(false);
  const [statsData, setStatsData] = useState(null);
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(5);

  // Stats cards data
  const statsCards = statsData ? [
    {
      title: 'Tổng người dùng',
      value: statsData.totalUsers?.toLocaleString() || '0',
      change: statsData.totalUsersChange || '+0% từ tháng trước',
      changeColor: '#00a63e',
    },
    {
      title: 'Active Users',
      value: statsData.activeUsers?.toLocaleString() || '0',
      change: statsData.activeUsersPercentage || '0% tổng số',
      changeColor: '#00a63e',
    },
    {
      title: 'Người dùng mới (tháng này)',
      value: statsData.newUsersThisMonth?.toLocaleString() || '0',
      change: statsData.newUsersChange || '+0% so tháng trước',
      changeColor: '#00a63e',
    },
    {
      title: 'Tài khoản bị khóa',
      value: statsData.bannedUsers?.toLocaleString() || '0',
      change: statsData.bannedUsersPercentage || '0% tổng số',
      changeColor: '#e7000b',
    },
  ] : [
    {
      title: 'Tổng người dùng',
      value: '0',
      change: 'Đang tải...',
      changeColor: '#00a63e',
    },
    {
      title: 'Active Users',
      value: '0',
      change: 'Đang tải...',
      changeColor: '#00a63e',
    },
    {
      title: 'Người dùng mới (tháng này)',
      value: '0',
      change: 'Đang tải...',
      changeColor: '#00a63e',
    },
    {
      title: 'Tài khoản bị khóa',
      value: '0',
      change: 'Đang tải...',
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
      align: 'left',
      render: (text) => <Text style={{ color: '#2563eb', fontSize: 14, fontWeight: 500 }}>{text}</Text>,
    },
    {
      title: 'Người dùng',
      dataIndex: 'user',
      key: 'user',
      width: 200,
      align: 'left',
      render: (user) => (
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <Avatar
            style={{
              background: 'linear-gradient(135deg, #60a5fa 0%, #8b5cf6 100%)',
            }}
            size={32}
          >
            {user.name.charAt(0)}
          </Avatar>
          <Text style={{ fontSize: 14, color: '#111827' }}>{user.name}</Text>
        </div>
      ),
    },
    {
      title: 'Liên hệ',
      dataIndex: 'contact',
      key: 'contact',
      width: 220,
      align: 'left',
      render: (contact) => (
        <div>
          <div style={{ marginBottom: 2 }}>
            <Text 
              style={{ 
                fontSize: 14, 
                color: '#111827',
                display: 'block',
                overflow: 'hidden',
                textOverflow: 'ellipsis',
                whiteSpace: 'nowrap',
                maxWidth: '200px'
              }}
            >
              {contact.email}
            </Text>
          </div>
          <div><Text type="secondary" style={{ fontSize: 12, color: '#6b7280' }}>{contact.phone}</Text></div>
        </div>
      ),
    },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      key: 'status',
      width: 110,
      align: 'left',
      render: (status) => {
        const statusConfig = {
          Active: { bg: '#dcfce7', color: '#16a34a' },
          Inactive: { bg: '#f3f4f6', color: '#4b5563' },
          Banned: { bg: '#fee2e2', color: '#dc2626' },
        };
        const config = statusConfig[status] || statusConfig.Active;
        return (
          <Tag
            style={{
              background: config.bg,
              color: config.color,
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
      title: 'Giao dịch',
      dataIndex: 'transactions',
      key: 'transactions',
      width: 100,
      align: 'left',
      render: (text) => <Text style={{ fontSize: 14, color: '#111827' }}>{text}</Text>,
    },
    {
      title: 'Ngày đăng ký',
      dataIndex: 'registeredDate',
      key: 'registeredDate',
      width: 120,
      align: 'left',
      render: (text) => <Text style={{ fontSize: 14, color: '#111827' }}>{text}</Text>,
    },
    {
      title: 'Hoạt động',
      dataIndex: 'lastActive',
      key: 'lastActive',
      width: 130,
      align: 'left',
      render: (text) => <Text type="secondary" style={{ fontSize: 14, color: '#6b7280' }}>{text}</Text>,
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
          icon={<EyeOutlined style={{ fontSize: 16 }} />}
          style={{ color: '#111827' }}
        />
      ),
    },
  ];

  // Table data - convert to state so we can delete users
  const [dataSource, setDataSource] = useState([]);

  // Load data from API
  useEffect(() => {
    loadUsers();
    loadStatistics();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []); // Run only once on mount

  const loadUsers = async () => {
    try {
      setLoading(true);
      console.log('Fetching users from API...');
      const response = await userService.admin.getUsers();
      console.log('API Response:', response);
      
      // Handle different response formats
      let users = [];
      if (Array.isArray(response)) {
        users = response;
      } else if (response?.data && Array.isArray(response.data)) {
        users = response.data;
      } else if (response?.content && Array.isArray(response.content)) {
        users = response.content;
      } else {
        console.warn('Unexpected response format:', response);
        throw new Error('Invalid response format');
      }
      
      console.log('Users array:', users);
      
      // Log first user to see structure
      if (users.length > 0) {
        console.log('First user structure:', JSON.stringify(users[0], null, 2));
        console.log('Available fields:', Object.keys(users[0]));
      }
      
      // Transform API data to match table format
      const transformedData = users.map((user, index) => {
        return {
          key: index.toString(),
          userId: user.userCode || user.id?.toString() || `USR${String(index + 1).padStart(3, '0')}`,
          realId: user.id, // Store the actual numeric ID for API calls
          user: { name: user.fullName || user.username || user.name || 'N/A' },
          contact: { 
            email: user.email || 'N/A', 
            phone: user.phone || user.phoneNumber || user.mobile || 'Chưa cập nhật'
          },
          status: user.isActive === true ? 'Active' : (user.isActive === false ? 'Inactive' : (user.status || 'Active')),
          transactions: user.totalTransactions?.toString() || user.transactionCount?.toString() || '0',
          registeredDate: user.createdAt ? new Date(user.createdAt).toLocaleDateString('vi-VN') : user.registeredDate || 'N/A',
          lastActive: user.lastLoginAt ? new Date(user.lastLoginAt).toLocaleString('vi-VN') : (user.lastActive || 'N/A'),
        };
      });
      
      console.log('Transformed data:', transformedData);
      setDataSource(transformedData);
      
      // Calculate stats from users if API doesn't provide them
      if (!statsData) {
        calculateStatsFromUsers(transformedData);
      }
      
      message.success(`Đã tải ${transformedData.length} người dùng`);
    } catch (error) {
      console.error('Error loading users:', error);
      console.error('Error details:', {
        message: error.message,
        response: error.response?.data,
        status: error.response?.status,
      });
      message.error(`Không thể tải danh sách người dùng: ${error.response?.data?.message || error.message}`);
      // Set empty array on error
      setDataSource([]);
    } finally {
      setLoading(false);
    }
  };

  const calculateStatsFromUsers = (users) => {
    const totalUsers = users.length;
    const activeUsers = users.filter(u => u.status === 'Active').length;
    const bannedUsers = users.filter(u => u.status === 'Banned').length;
    
    // Calculate users registered this month
    const now = new Date();
    const currentMonth = now.getMonth();
    const currentYear = now.getFullYear();
    const newUsersThisMonth = users.filter(u => {
      if (u.registeredDate && u.registeredDate !== 'N/A') {
        const parts = u.registeredDate.split('/');
        if (parts.length === 3) {
          const month = parseInt(parts[1]) - 1; // JS months are 0-indexed
          const year = parseInt(parts[2]);
          return month === currentMonth && year === currentYear;
        }
      }
      return false;
    }).length;
    
    const stats = {
      totalUsers,
      activeUsers,
      bannedUsers,
      newUsersThisMonth,
      activeUsersPercentage: `${((activeUsers / totalUsers) * 100).toFixed(1)}% tổng số`,
      bannedUsersPercentage: `${((bannedUsers / totalUsers) * 100).toFixed(1)}% tổng số`,
      totalUsersChange: '+0% từ tháng trước',
      newUsersChange: '+0% so tháng trước',
    };
    
    console.log('Calculated stats from users:', stats);
    setStatsData(stats);
  };

  const loadStatistics = async () => {
    try {
      console.log('Fetching statistics from API...');
      const response = await userService.admin.getStatistics();
      console.log('Statistics Response:', response);
      
      // Handle different response formats
      let stats = response;
      if (response?.data) {
        stats = response.data;
      }
      
      // Check if stats has valid data
      if (stats && (stats.totalUsers !== undefined || stats.total !== undefined)) {
        setStatsData(stats);
        console.log('Stats data set from API:', stats);
      } else {
        console.warn('Statistics API returned no valid data, will calculate from users');
      }
    } catch (error) {
      console.error('Error loading statistics:', error);
      console.error('Error details:', {
        message: error.message,
        response: error.response?.data,
        status: error.response?.status,
      });
      // Don't show error message, will calculate from users instead
      console.log('Will calculate stats from users list instead');
    }
  };

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
    // Remove user from dataSource (API already called by modal)
    const newData = dataSource.filter(user => user.userId !== userId);
    setDataSource(newData);
    // Recalculate statistics
    calculateStatsFromUsers(newData);
  };

  const handleDisableUser = (userId) => {
    // Update user status in dataSource (API already called by modal)
    setDataSource(prev => prev.map(user => {
      if (user.userId === userId) {
        const newStatus = user.status === 'Active' ? 'Inactive' : 'Active';
        return { ...user, status: newStatus };
      }
      return user;
    }));
    // Reload statistics
    calculateStatsFromUsers(dataSource);
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
                  borderRadius: 12,
                  border: '1px solid #e5e7eb',
                  boxShadow: 'none',
                  background: '#fff',
                }}
                bodyStyle={{ padding: '20px 24px' }}
              >
                <div style={{ marginBottom: 20 }}>
                  <Text style={{ color: '#6b7280', fontSize: 14, display: 'block', fontWeight: 400 }}>
                    {card.title}
                  </Text>
                </div>
                <div style={{ marginBottom: 16 }}>
                  <Text style={{ fontSize: 24, color: '#0a0a0a', display: 'block', fontWeight: 600 }}>
                    {card.value}
                  </Text>
                </div>
                <div>
                  <Text style={{ color: card.changeColor, fontSize: 12, fontWeight: 400 }}>
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
            borderRadius: 12,
            border: '1px solid #e5e7eb',
            boxShadow: 'none',
            marginBottom: 24,
            background: '#fff',
          }}
          bodyStyle={{ padding: '20px 24px' }}
        >
          <Row gutter={12} align="middle" wrap={false}>
            <Col flex="auto">
              <Input
                placeholder="Tìm kiếm theo tên, email, ID..."
                prefix={<SearchOutlined style={{ color: '#9ca3af' }} />}
                value={searchText}
                onChange={(e) => setSearchText(e.target.value)}
                style={{
                  background: '#f9fafb',
                  border: '1px solid #e5e7eb',
                  borderRadius: 8,
                  height: 40,
                  fontSize: 14,
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
                    background: '#fff',
                    border: '1px solid #e5e7eb',
                    borderRadius: 8,
                    height: 40,
                    minWidth: 100,
                    padding: '0 16px',
                    fontSize: 14,
                    color: '#374151',
                  }}
                >
                  <Space>
                    {selectedStatus}
                    <DownOutlined style={{ fontSize: 12 }} />
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
                  padding: '0 16px',
                  background: showAdvancedFilter ? '#fff' : '#fff',
                  color: '#374151',
                  border: '1px solid #e5e7eb',
                  fontSize: 14,
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
                  background: '#2563eb',
                  borderRadius: 8,
                  height: 40,
                  padding: '0 16px',
                  border: 'none',
                  fontSize: 14,
                  fontWeight: 500,
                }}
              >
                Export CSV
              </Button>
            </Col>
          </Row>

          {/* Advanced Filter Panel - show below when toggled */}
          {showAdvancedFilter && (
            <div style={{ marginTop: 20, paddingTop: 20, borderTop: '1px solid #e5e7eb' }}>
              <Row gutter={[12, 12]}>
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
            borderRadius: 12,
            border: '1px solid #e5e7eb',
            boxShadow: 'none',
            background: '#fff',
          }}
          bodyStyle={{ padding: 0 }}
        >
          <Spin spinning={loading} tip="Đang tải dữ liệu...">
            <Table
              columns={columns}
              dataSource={filteredUsers}
              pagination={{
                current: currentPage,
                pageSize: pageSize,
                total: totalFiltered,
                showSizeChanger: true,
                pageSizeOptions: ['5', '10', '20', '50'],
                showTotal: (total, range) => `Hiển thị ${range[0]}-${range[1]} trong tổng số ${total.toLocaleString()} người dùng`,
                onChange: (page, pageSize) => {
                  setCurrentPage(page);
                  setPageSize(pageSize);
                },
                onShowSizeChange: (current, size) => {
                  setCurrentPage(1);
                  setPageSize(size);
                },
                style: { padding: '16px 24px', marginBottom: 0 },
              }}
              tableLayout="fixed"
              style={{ borderRadius: 12 }}
            />
          </Spin>
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