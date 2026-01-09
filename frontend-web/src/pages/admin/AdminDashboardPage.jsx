import React, { useState, useEffect } from 'react';
import { Typography, Row, Col, Card, Statistic, Badge, Progress, Tag, List, Spin, message } from 'antd';
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
import AdminLayout from '../../layouts/AdminLayout';
import dashboardService from '../../services/dashboardService';

const { Title, Text } = Typography;

const AdminDashboardPage = () => {
  const [loading, setLoading] = useState(false);
  const [stats, setStats] = useState([
    {
      title: 'Tổng người dùng',
      value: '0',
      badge: '+0%',
      badgeColor: '#dcfce7',
      badgeTextColor: '#008236',
      detail: 'Đang tải...',
      icon: <img src="/images/ngdung.svg" alt="user icon" style={{ width: 24, height: 24 }} />,
      iconBg: '#dbeafe',
      iconColor: '#3b82f6',
    },
    {
      title: 'Giao dịch hôm nay',
      value: '0',
      badge: '+0%',
      badgeColor: '#dcfce7',
      badgeTextColor: '#008236',
      detail: 'Đang tải...',
      icon: <img src="/images/lenxuong.svg" alt="transaction icon" style={{ width: 24, height: 24 }} />,
      iconBg: '#dcfce7',
      iconColor: '#10b981',
    },
  ]);
  const [userGrowthData, setUserGrowthData] = useState([]);
  const [transactionVolumeData, setTransactionVolumeData] = useState([]);
  const [categoryData, setCategoryData] = useState([]);
  const [bankData, setBankData] = useState([]);
  const [systemStatus, setSystemStatus] = useState([]);

  // Load all dashboard data
  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    try {
      setLoading(true);
      
      // Load all data in parallel
      const [overview, userGrowth, transactionVolume, categoryDist, bankDist, systemHealth] = await Promise.all([
        dashboardService.getOverview().catch(err => {
          console.error('Error loading overview:', err);
          return null;
        }),
        dashboardService.getUserGrowth().catch(err => {
          console.error('Error loading user growth:', err);
          return null;
        }),
        dashboardService.getTransactionVolume().catch(err => {
          console.error('Error loading transaction volume:', err);
          return null;
        }),
        dashboardService.getCategoryDistribution().catch(err => {
          console.error('Error loading category distribution:', err);
          return null;
        }),
        dashboardService.getBankDistribution().catch(err => {
          console.error('Error loading bank distribution:', err);
          return null;
        }),
        dashboardService.getSystemHealth().catch(err => {
          console.error('Error loading system health:', err);
          return null;
        }),
      ]);

      // Process overview data (stats cards)
      if (overview) {
        console.log('Overview data loaded successfully:', overview);
        
        setStats([
          {
            title: 'Tổng người dùng',
            value: overview.totalUsers?.toLocaleString() || '0',
            badge: `+${overview.userGrowthPercent || 0}%`,
            badgeColor: '#dcfce7',
            badgeTextColor: '#008236',
            detail: overview.activeUsersToday ? `${overview.activeUsersToday.toLocaleString()} active today` : 'N/A',
            icon: <img src="/images/ngdung.svg" alt="user icon" style={{ width: 24, height: 24 }} />,
            iconBg: '#dbeafe',
            iconColor: '#3b82f6',
          },
          {
            title: 'Giao dịch hôm nay',
            value: overview.transactionsToday?.toLocaleString() || '0',
            badge: `+${overview.transactionGrowthPercent || 0}%`,
            badgeColor: '#dcfce7',
            badgeTextColor: '#008236',
            detail: overview.totalValueToday ? `₫${(overview.totalValueToday / 1000000).toFixed(1)}M tổng giá trị` : '₫0M tổng giá trị',
            icon: <img src="/images/lenxuong.svg" alt="transaction icon" style={{ width: 24, height: 24 }} />,
            iconBg: '#dcfce7',
            iconColor: '#10b981',
          },
        ]);
      } else {
        console.warn('No overview data received from API');
      }

      // Process user growth data
      if (userGrowth && userGrowth.data) {
        console.log('User growth loaded:', userGrowth.data);
        
        // Map to expected format for LineChart
        const growthArray = userGrowth.data.map(item => ({
          month: item.month || '',
          totalUsers: item.totalUsers || 0,
          activeUsers: item.activeUsers || 0,
          newUsers: item.newUsers || 0,
        }));
        
        setUserGrowthData(growthArray);
      }

      // Process transaction volume data
      if (transactionVolume && transactionVolume.data) {
        console.log('Transaction volume loaded:', transactionVolume.data);
        
        // Map to expected format for BarChart
        const volumeArray = transactionVolume.data.map(item => ({
          day: item.day || '',
          count: item.transactionCount || 0,
          value: item.totalValue || 0, // Already in millions
        }));
        
        setTransactionVolumeData(volumeArray);
      }

      // Process category distribution
      if (categoryDist && categoryDist.categories) {
        console.log('Category distribution loaded:', categoryDist.categories);
        
        // Map to expected format for PieChart
        const categoryArray = categoryDist.categories.map(cat => ({
          name: cat.categoryName || cat.name,
          value: cat.percentage || 0,
          color: cat.color || '#cccccc',
          count: cat.transactionCount || cat.count || 0,
          amount: cat.amount || 0,
        }));
        
        setCategoryData(categoryArray);
      }

      // Process bank distribution
      if (bankDist && bankDist.banks) {
        console.log('Bank distribution loaded:', bankDist.banks);
        
        // Map to expected format
        const bankArray = bankDist.banks.map(bank => ({
          name: bank.bankCode || bank.bankName,
          users: bank.userCount || 0,
          percentage: bank.percentage || 0,
        }));
        
        setBankData(bankArray);
      }

      // Process system health
      if (systemHealth) {
        console.log('System health loaded:', systemHealth);
        
        // Convert object to array for Progress bars
        const healthArray = [
          {
            name: 'CPU Usage',
            value: systemHealth.cpuUsage || 0,
            color: systemHealth.cpuUsage > 80 ? '#ef4444' : systemHealth.cpuUsage > 60 ? '#f59e0b' : '#10b981',
          },
          {
            name: 'RAM Usage',
            value: systemHealth.ramUsage || 0,
            color: systemHealth.ramUsage > 80 ? '#ef4444' : systemHealth.ramUsage > 60 ? '#f59e0b' : '#10b981',
          },
          {
            name: 'Database',
            value: systemHealth.databaseStatus === 'CONNECTED' ? 100 : systemHealth.databaseStatus === 'SLOW' ? 70 : 0,
            color: systemHealth.databaseStatus === 'CONNECTED' ? '#3b82f6' : systemHealth.databaseStatus === 'SLOW' ? '#f59e0b' : '#ef4444',
          },
          {
            name: 'AI Backend',
            value: systemHealth.aiBackendStatus === 'ONLINE' ? 100 : systemHealth.aiBackendStatus === 'DEGRADED' ? 60 : 0,
            color: systemHealth.aiBackendStatus === 'ONLINE' ? '#8b5cf6' : systemHealth.aiBackendStatus === 'DEGRADED' ? '#f59e0b' : '#ef4444',
          },
        ];
        
        setSystemStatus(healthArray);
      }

    } catch (error) {
      console.error('Error loading dashboard data:', error);
      message.error('Không thể tải dữ liệu dashboard');
    } finally {
      setLoading(false);
    }
  };

  return (
    <AdminLayout>
      <Spin spinning={loading} tip="Đang tải dữ liệu dashboard...">
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
            <Col xs={24} sm={12} lg={12} key={index}>
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
                    label={false}
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
                        {item.name.length > 4 ? item.name.substring(0, 4) : item.name}
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
      </Spin>
    </AdminLayout>
  );
};

export default AdminDashboardPage;
