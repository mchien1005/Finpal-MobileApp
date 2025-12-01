import { Card, Row, Col, Statistic, Typography } from 'antd';
import {
  UserOutlined,
  TransactionOutlined,
  DollarOutlined,
  RiseOutlined,
} from '@ant-design/icons';
import Sidebar from '../../components/user/Sidebar';
import { useSidebar } from '../../contexts/SidebarContext';
import Header from '../../components/common/Header';

const { Title, Text } = Typography;

const DashboardPage = () => {
  const { collapsed } = useSidebar();
  
  return (
    <div style={{ display: 'flex', minHeight: '100vh', background: '#F5F7FA' }}>
      <Sidebar />

      {/* Main Content */}
      <div style={{ marginLeft: collapsed ? 80 : 280, flex: 1, transition: 'margin-left 0.3s' }}>
        {/* Header */}
        <Header title="Tổng quan" />

        {/* Content */}
        <div style={{ padding: 24 }}>
          <Row gutter={16}>
            <Col xs={24} sm={12} lg={6}>
              <Card>
                <Statistic
                  title="Tổng người dùng"
                  value={1234}
                  prefix={<UserOutlined />}
                  valueStyle={{ color: '#1890ff' }}
                />
              </Card>
            </Col>
            <Col xs={24} sm={12} lg={6}>
              <Card>
                <Statistic
                  title="Giao dịch hôm nay"
                  value={56}
                  prefix={<TransactionOutlined />}
                  valueStyle={{ color: '#52c41a' }}
                />
              </Card>
            </Col>
            <Col xs={24} sm={12} lg={6}>
              <Card>
                <Statistic
                  title="Tổng thu nhập"
                  value={25000000}
                  prefix={<DollarOutlined />}
                  valueStyle={{ color: '#faad14' }}
                  suffix="đ"
                />
              </Card>
            </Col>
            <Col xs={24} sm={12} lg={6}>
              <Card>
                <Statistic
                  title="Tăng trưởng"
                  value={12.5}
                  prefix={<RiseOutlined />}
                  valueStyle={{ color: '#f5222d' }}
                  suffix="%"
                />
              </Card>
            </Col>
          </Row>
        </div>
      </div>
    </div>
  );
};

export default DashboardPage;
