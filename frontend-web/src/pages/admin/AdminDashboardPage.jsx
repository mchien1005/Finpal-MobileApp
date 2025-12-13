import React from 'react';
import { Typography, Row, Col, Card, Statistic } from 'antd';
import {
  UserOutlined, 
  TransactionOutlined,
  DollarOutlined,
  RiseOutlined,
} from '@ant-design/icons';
import AdminSidebar from '../../components/admin/AdminSidebar';
import { useSidebar } from '../../contexts/SidebarContext';

const { Title, Text } = Typography;

const AdminDashboardPage = () => {
  const { collapsed } = useSidebar();

  const stats = [
    {
      title: 'Tổng người dùng',
      value: 1234,
      icon: <UserOutlined />,
      color: '#4F46E5',
      bgColor: 'rgba(79, 70, 229, 0.1)',
    },
    {
      title: 'Giao dịch hôm nay',
      value: 567,
      icon: <TransactionOutlined />,
      color: '#10B981',
      bgColor: 'rgba(16, 185, 129, 0.1)',
    },
    {
      title: 'Tổng giao dịch',
      value: '125.5M',
      prefix: '₫',
      icon: <DollarOutlined />,
      color: '#F59E0B',
      bgColor: 'rgba(245, 158, 11, 0.1)',
    },
    {
      title: 'Tăng trưởng',
      value: 12.5,
      suffix: '%',
      icon: <RiseOutlined />,
      color: '#EF4444',
      bgColor: 'rgba(239, 68, 68, 0.1)',
    },
  ];

  return (
    <div style={{ display: 'flex', minHeight: '100vh', background: '#F1F5F9' }}>
      <AdminSidebar />

      {/* Main Content */}
      <div
        style={{
          marginLeft: collapsed ? 80 : 280,
          flex: 1,
          transition: 'margin-left 0.3s',
          padding: 32,
        }}
      >
        {/* Header */}
        <div style={{ marginBottom: 32 }}>
          <Title level={2} style={{ margin: 0, marginBottom: 8 }}>
            Dashboard & Analytics
          </Title>
          <Text type="secondary">
            Tổng quan hệ thống FinPal
          </Text>
        </div>

        {/* Stats Cards */}
        <Row gutter={[24, 24]} style={{ marginBottom: 32 }}>
          {stats.map((stat, index) => (
            <Col xs={24} sm={12} lg={6} key={index}>
              <Card
                style={{
                  borderRadius: 16,
                  border: 'none',
                  boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
                }}
                bodyStyle={{ padding: 24 }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
                  <div
                    style={{
                      width: 56,
                      height: 56,
                      borderRadius: 12,
                      background: stat.bgColor,
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      fontSize: 24,
                      color: stat.color,
                    }}
                  >
                    {stat.icon}
                  </div>
                  <div>
                    <Text type="secondary" style={{ fontSize: 13 }}>
                      {stat.title}
                    </Text>
                    <div style={{ fontSize: 24, fontWeight: 700, color: '#1E293B' }}>
                      {stat.prefix}{stat.value}{stat.suffix}
                    </div>
                  </div>
                </div>
              </Card>
            </Col>
          ))}
        </Row>

        {/* Content Placeholder */}
        <Row gutter={[24, 24]}>
          <Col xs={24} lg={16}>
            <Card
              title="Biểu đồ giao dịch"
              style={{
                borderRadius: 16,
                border: 'none',
                boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
              }}
              bodyStyle={{ height: 300 }}
            >
              <div
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  height: '100%',
                  color: '#94A3B8',
                }}
              >
                Biểu đồ sẽ được hiển thị ở đây
              </div>
            </Card>
          </Col>
          <Col xs={24} lg={8}>
            <Card
              title="Hoạt động gần đây"
              style={{
                borderRadius: 16,
                border: 'none',
                boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
              }}
              bodyStyle={{ height: 300 }}
            >
              <div
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  height: '100%',
                  color: '#94A3B8',
                }}
              >
                Danh sách hoạt động
              </div>
            </Card>
          </Col>
        </Row>
      </div>
    </div>
  );
};

export default AdminDashboardPage;
