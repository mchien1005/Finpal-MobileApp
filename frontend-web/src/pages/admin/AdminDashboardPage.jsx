import React from 'react';
import { Typography, Row, Col, Card, Statistic, Badge, Progress, Tag, List } from 'antd';
import {
  UserOutlined,
  TransactionOutlined,
  RobotOutlined,
  MessageOutlined,
  WarningOutlined,
  CloseCircleOutlined,
} from '@ant-design/icons';
import {
  LineChart,
  Line,
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts';
import AdminSidebar from '../../components/admin/AdminSidebar';
import { useSidebar } from '../../contexts/SidebarContext';

const { Title, Text } = Typography;

const AdminDashboardPage = () => {
  const { collapsed } = useSidebar();

  // Stats cards data
  const stats = [
    {
      title: 'Tổng người dùng',
      value: '12,543',
      badge: '+12.5%',
      badgeColor: '#dcfce7',
      badgeTextColor: '#008236',
      detail: '1,234 active today',
      icon: <img src="/images/ngdung.svg" alt="user icon" style={{ width: 24, height: 24 }} />,
      iconBg: '#dbeafe',
      iconColor: '#3b82f6',
    },
    {
      title: 'Giao dịch hôm nay',
      value: '8,392',
      badge: '+8.2%',
      badgeColor: '#dcfce7',
      badgeTextColor: '#008236',
      detail: '₫245.6M tổng giá trị',
      icon: <img src="/images/lenxuong.svg" alt="user icon" style={{ width: 24, height: 24 }} />,
      iconBg: '#dcfce7',
      iconColor: '#10b981',
    },
    {
      title: 'AI Accuracy',
      value: '94.2%',
      badge: '+2.1%',
      badgeColor: '#dcfce7',
      badgeTextColor: '#008236',
      detail: 'Category classification',
      icon: <img src="/images/accuracy.svg" alt="user icon" style={{ width: 24, height: 24 }} />,
      iconBg: '#f3e8ff',
      iconColor: '#8b5cf6',
    },
    {
      title: 'SMS Parsing',
      value: '98.7%',
      badge: '+0.5%',
      badgeColor: '#dcfce7',
      badgeTextColor: '#008236',
      detail: '128 failed today',
      icon: <img src="/images/warning.svg" alt="user icon" style={{ width: 24, height: 24 }} />,
      iconBg: '#ffedd4',
      iconColor: '#f59e0b',
    },
  ];

  // User growth chart data
  const userGrowthData = [
    { month: 'T1', activeUsers: 3200, totalUsers: 3500 },
    { month: 'T2', activeUsers: 4100, totalUsers: 4800 },
    { month: 'T3', activeUsers: 5300, totalUsers: 6200 },
    { month: 'T4', activeUsers: 6800, totalUsers: 8100 },
    { month: 'T5', activeUsers: 8500, totalUsers: 9800 },
    { month: 'T6', activeUsers: 10200, totalUsers: 11500 },
    { month: 'T7', activeUsers: 11800, totalUsers: 12543 },
  ];

  // Transaction volume data
  const transactionVolumeData = [
    { day: 'T2', value: 850, count: 420 },
    { day: 'T3', value: 1250, count: 580 },
    { day: 'T4', value: 650, count: 310 },
    { day: 'T5', value: 1850, count: 820 },
    { day: 'T6', value: 2100, count: 950 },
    { day: 'T7', value: 1850, count: 780 },
    { day: 'CN', value: 1450, count: 620 },
  ];

  // Category distribution data
  const categoryData = [
    { name: 'Ăn uống', value: 35, color: '#3b82f6' },
    { name: 'Di chuyển', value: 20, color: '#10b981' },
    { name: 'Mua sắm', value: 18, color: '#f59e0b' },
    { name: 'Giải trí', value: 12, color: '#ef4444' },
    { name: 'Hóa đơn', value: 10, color: '#8b5cf6' },
    { name: 'Khác', value: 5, color: '#6b7280' },
  ];

  // Bank distribution data
  const bankData = [
    { name: 'VCB', users: 3245, percentage: 25.9 },
    { name: 'TCB', users: 2891, percentage: 23.1 },
    { name: 'ACB', users: 2134, percentage: 17.0 },
    { name: 'VTB', users: 1678, percentage: 13.4 },
    { name: 'MBB', users: 1456, percentage: 11.6 },
    { name: 'Others', users: 1139, percentage: 9.0 },
  ];

  // System status data
  const systemStatus = [
    { name: 'CPU Usage', value: 45, color: '#00c950' },
    { name: 'RAM Usage', value: 68, color: '#f0b100' },
    { name: 'Disk Usage', value: 32, color: '#00c950' },
    { name: 'API Latency', value: 89, color: '#fb2c36' },
  ];

  // Recent errors data
  const recentErrors = [
    {
      title: 'SMS Parse Failed',
      description: 'VCB format not recognized',
      time: '10:45 AM',
      severity: 'high',
      icon: <WarningOutlined />,
    },
    {
      title: 'AI Low Confidence',
      description: 'Category: Shopping (62%)',
      time: '10:32 AM',
      severity: 'medium',
      icon: <CloseCircleOutlined />,
    },
    {
      title: 'API Error',
      description: 'Timeout on transaction sync',
      time: '10:18 AM',
      severity: 'high',
      icon: <WarningOutlined />,
    },
    {
      title: 'SMS Parse Failed',
      description: 'Unknown bank format',
      time: '09:54 AM',
      severity: 'medium',
      icon: <CloseCircleOutlined />,
    },
  ];

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
        <div style={{ marginBottom: 32 }}>
          <Title level={2} style={{ margin: 0, marginBottom: 4, color: '#101828' }}>
            Dashboard & Analytics
          </Title>
          <Text style={{ color: '#6a7282', fontSize: 16 }}>
            Tổng quan hệ thống và phân tích dữ liệu
          </Text>
        </div>

        {/* Stats Cards */}
        <Row gutter={[24, 24]} style={{ marginBottom: 32 }}>
          {stats.map((stat, index) => (
            <Col xs={24} sm={12} lg={6} key={index}>
              <Card
                style={{
                  borderRadius: 14,
                  border: '1px solid rgba(0, 0, 0, 0.1)',
                  boxShadow: 'none',
                }}
                bodyStyle={{ padding: 24 }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
                  <div style={{ flex: 1 }}>
                    <Text style={{ color: '#4a5565', fontSize: 14, display: 'block', marginBottom: 8 }}>
                      {stat.title}
                    </Text>
                    <Title level={4} style={{ margin: 0, marginBottom: 8, color: '#101828' }}>
                      {stat.value}
                    </Title>
                    <div
                      style={{
                        display: 'inline-block',
                        background: stat.badgeColor,
                        color: stat.badgeTextColor,
                        padding: '2px 8px',
                        borderRadius: 8,
                        fontSize: 12,
                        marginBottom: 8,
                      }}
                    >
                      {stat.badge}
                    </div>
                    <div>
                      <Text style={{ color: '#6a7282', fontSize: 12 }}>{stat.detail}</Text>
                    </div>
                  </div>
                  <div
                    style={{
                      width: 48,
                      height: 48,
                      borderRadius: 10,
                      background: stat.iconBg,
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      fontSize: 24,
                      color: stat.iconColor,
                    }}
                  >
                    {stat.icon}
                  </div>
                </div>
              </Card>
            </Col>
          ))}
        </Row>

        {/* Charts Row */}
        <Row gutter={[24, 24]} style={{ marginBottom: 32 }}>
          {/* User Growth Chart */}
          <Col xs={24} lg={12}>
            <Card
              title={<Text style={{ fontSize: 18, color: '#101828' }}>Tăng trưởng người dùng</Text>}
              style={{
                borderRadius: 14,
                border: '1px solid rgba(0, 0, 0, 0.1)',
                boxShadow: 'none',
              }}
              bodyStyle={{ padding: 24 }}
            >
              <ResponsiveContainer width="100%" height={300}>
                <LineChart 
                  data={userGrowthData}
                  margin={{ top: 5, right: 30, left: 20, bottom: 5 }}
                >
                  <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
                  <XAxis 
                    dataKey="month" 
                    tick={{ fontSize: 12, fill: '#6b7280' }}
                  />
                  <YAxis 
                    tick={{ fontSize: 12, fill: '#6b7280' }}
                  />
                  <Tooltip 
                    contentStyle={{ 
                      backgroundColor: '#fff', 
                      border: '1px solid #e5e7eb',
                      borderRadius: 8 
                    }}
                  />
                  <Legend 
                    wrapperStyle={{ fontSize: 14, paddingTop: 10 }}
                  />
                  <Line
                    name="Active users"
                    type="monotone"
                    dataKey="activeUsers"
                    stroke="#10b981"
                    strokeWidth={2}
                    dot={{ r: 4, fill: '#10b981' }}
                    activeDot={{ r: 6 }}
                  />
                  <Line
                    name="Tổng users"
                    type="monotone"
                    dataKey="totalUsers"
                    stroke="#2563eb"
                    strokeWidth={2}
                    dot={{ r: 4, fill: '#2563eb' }}
                    activeDot={{ r: 6 }}
                  />
                </LineChart>
              </ResponsiveContainer>
            </Card>
          </Col>

          {/* Transaction Volume Chart */}
          <Col xs={24} lg={12}>
            <Card
              title={
                <Text style={{ fontSize: 18, color: '#101828' }}>Khối lượng giao dịch (7 ngày qua)</Text>
              }
              style={{
                borderRadius: 14,
                border: '1px solid rgba(0, 0, 0, 0.1)',
                boxShadow: 'none',
              }}
              bodyStyle={{ padding: 24 }}
            >
              <ResponsiveContainer width="100%" height={300}>
                <BarChart 
                  data={transactionVolumeData}
                  margin={{ top: 5, right: 30, left: 20, bottom: 5 }}
                >
                  <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
                  <XAxis 
                    dataKey="day" 
                    tick={{ fontSize: 12, fill: '#6b7280' }}
                  />
                  <YAxis 
                    tick={{ fontSize: 12, fill: '#6b7280' }}
                  />
                  <Tooltip 
                    contentStyle={{ 
                      backgroundColor: '#fff', 
                      border: '1px solid #e5e7eb',
                      borderRadius: 8 
                    }}
                  />
                  <Legend 
                    wrapperStyle={{ fontSize: 14, paddingTop: 10 }}
                  />
                  <Bar 
                    name="Giá trị (M)"
                    dataKey="value" 
                    fill="#10b981" 
                    radius={[8, 8, 0, 0]} 
                  />
                  <Bar 
                    name="Số giao dịch"
                    dataKey="count" 
                    fill="#2563eb" 
                    radius={[8, 8, 0, 0]} 
                  />
                </BarChart>
              </ResponsiveContainer>
            </Card>
          </Col>
        </Row>

        {/* Bottom Row */}
        <Row gutter={[24, 24]} style={{ marginBottom: 32, alignItems: 'stretch' }}>
          {/* Category Distribution */}
          <Col xs={24} lg={8} style={{ display: 'flex' }}>
            <Card
              title={<Text style={{ fontSize: 16, color: '#101828' }}>Phân bố danh mục chi tiêu</Text>}
              style={{
                borderRadius: 14,
                border: '1px solid rgba(0, 0, 0, 0.1)',
                boxShadow: 'none',
                width: '100%',
              }}
              bodyStyle={{ padding: '24px', display: 'flex', flexDirection: 'column', height: 'calc(100% - 57px)' }}
            >
              <ResponsiveContainer width="100%" height={220}>
                <PieChart>
                  <Pie
                    data={categoryData}
                    cx="50%"
                    cy="50%"
                    labelLine={false}
                    label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                    outerRadius={75}
                    fill="#8884d8"
                    dataKey="value"
                  >
                    {categoryData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color} />
                    ))}
                  </Pie>
                  <Tooltip />
                </PieChart>
              </ResponsiveContainer>
              <div style={{ marginTop: 'auto', paddingTop: 16 }}>
                {categoryData.map((item, index) => (
                  <div key={index} style={{ display: 'flex', alignItems: 'center', marginBottom: 10 }}>
                    <div
                      style={{
                        width: 12,
                        height: 12,
                        borderRadius: '50%',
                        background: item.color,
                        marginRight: 8,
                      }}
                    />
                    <Text style={{ fontSize: 13, flex: 1 }}>{item.name}</Text>
                    <Text style={{ fontSize: 13, color: '#4a5565', fontWeight: 500 }}>{item.value}%</Text>
                  </div>
                ))}
              </div>
            </Card>
          </Col>

          {/* Bank Distribution */}
          <Col xs={24} lg={8} style={{ display: 'flex' }}>
            <Card
              title={<Text style={{ fontSize: 16, color: '#101828' }}>Phân bố ngân hàng</Text>}
              style={{
                borderRadius: 14,
                border: '1px solid rgba(0, 0, 0, 0.1)',
                boxShadow: 'none',
                width: '100%',
              }}
              bodyStyle={{ padding: '24px', display: 'flex', flexDirection: 'column', height: 'calc(100% - 57px)' }}
            >
              <div style={{ flex: 1, display: 'flex', flexDirection: 'column', justifyContent: 'space-evenly' }}>
                {bankData.map((item, index) => (
                  <div key={index} style={{ marginBottom: index < bankData.length - 1 ? 16 : 0 }}>
                    <div style={{ display: 'flex', gap: 12, alignItems: 'center', marginBottom: 4 }}>
                      <div
                        style={{
                          width: 40,
                          height: 40,
                          borderRadius: 10,
                          background: '#dbeafe',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                          color: '#155dfc',
                          fontSize: 13,
                          fontWeight: 500,
                        }}
                      >
                        {item.name}
                      </div>
                      <div style={{ flex: 1 }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4 }}>
                          <Text style={{ fontSize: 14 }}>{item.users.toLocaleString()} users</Text>
                          <Text style={{ fontSize: 14, color: '#4a5565' }}>{item.percentage}%</Text>
                        </div>
                        <Progress
                          percent={item.percentage}
                          strokeColor="#155dfc"
                          showInfo={false}
                          size="small"
                        />
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </Card>
          </Col>

          {/* System Status */}
          <Col xs={24} lg={8} style={{ display: 'flex' }}>
            <Card
              title={<Text style={{ fontSize: 16, color: '#101828' }}>Tình trạng hệ thống</Text>}
              style={{
                borderRadius: 14,
                border: '1px solid rgba(0, 0, 0, 0.1)',
                boxShadow: 'none',
                width: '100%',
              }}
              bodyStyle={{ padding: '24px', display: 'flex', flexDirection: 'column', height: 'calc(100% - 57px)' }}
            >
              <div style={{ flex: 1, display: 'flex', flexDirection: 'column', justifyContent: 'space-evenly' }}>
                {systemStatus.map((item, index) => (
                  <div key={index} style={{ marginBottom: index < systemStatus.length - 1 ? 18 : 0 }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
                      <Text style={{ fontSize: 14, color: '#364153' }}>{item.name}</Text>
                      <Text style={{ fontSize: 14, color: '#101828' }}>{item.value}%</Text>
                    </div>
                    <Progress
                      percent={item.value}
                      strokeColor={item.color}
                      showInfo={false}
                      size="small"
                    />
                  </div>
                ))}
              </div>
              <div
                style={{
                  borderTop: '1px solid rgba(0, 0, 0, 0.1)',
                  paddingTop: 16,
                  marginTop: 'auto',
                  display: 'flex',
                  alignItems: 'center',
                  gap: 8,
                }}
              >
                <div
                  style={{
                    width: 8,
                    height: 8,
                    borderRadius: '50%',
                    background: '#00c950',
                    opacity: 0.6,
                  }}
                />
                <Text style={{ color: '#00a63e', fontSize: 14 }}>System Operational</Text>
              </div>
            </Card>
          </Col>
        </Row>
                

        {/* Recent Errors */}
        <Card
          title={
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <Text style={{ fontSize: 16, color: '#101828' }}>Lỗi gần đây</Text>
              <Tag color="error">4 errors in last hour</Tag>
            </div>
          }
          style={{
            borderRadius: 14,
            border: '1px solid rgba(0, 0, 0, 0.1)',
            boxShadow: 'none',
          }}
          bodyStyle={{ padding: '24px' }}
        >
          <List
            itemLayout="horizontal"
            dataSource={recentErrors}
            renderItem={(item) => (
              <List.Item
                style={{
                  background: '#f9fafb',
                  padding: 16,
                  borderRadius: 10,
                  marginBottom: 12,
                }}
              >
                <List.Item.Meta
                  avatar={
                    <div
                      style={{
                        width: 20,
                        height: 20,
                        color: item.severity === 'high' ? '#fb2c36' : '#f0b100',
                        fontSize: 20,
                      }}
                    >
                      {item.icon}
                    </div>
                  }
                  title={
                    <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                      <Text style={{ fontSize: 14, color: '#101828' }}>{item.title}</Text>
                      <Tag
                        color={item.severity === 'high' ? 'error' : 'warning'}
                        style={{ fontSize: 12 }}
                      >
                        {item.severity}
                      </Tag>
                    </div>
                  }
                  description={
                    <Text style={{ fontSize: 14, color: '#4a5565' }}>{item.description}</Text>
                  }
                />
                <Text style={{ fontSize: 12, color: '#6a7282' }}>{item.time}</Text>
              </List.Item>
            )}
          />
        </Card>
      </div>
    </div>
  );
};

export default AdminDashboardPage;
