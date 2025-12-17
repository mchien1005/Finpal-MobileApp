import React from 'react';
import { Card, Row, Col, Table, Tag, Button, Typography } from 'antd';
import {
  SafetyOutlined,
  WarningOutlined,
  LineChartOutlined,
  LockOutlined,
  DownloadOutlined,
} from '@ant-design/icons';

const { Title, Text } = Typography;

const DataPrivacyTab = () => {
  // Stats cards data
  const statsCards = [
    {
      title: 'Điểm Bảo mật',
      value: '98/100',
      icon: <SafetyOutlined style={{ fontSize: 24, color: '#00C950' }} />,
      bgColor: '#dcfce7',
    },
    {
      title: 'Đăng nhập Thất bại',
      value: '15 hôm nay',
      icon: <WarningOutlined style={{ fontSize: 24, color: '#e7000b' }} />,
      bgColor: '#ffe2e2',
    },
    {
      title: 'Admin Hoạt động',
      value: '2',
      icon: <LineChartOutlined style={{ fontSize: 24, color: '#155dfc' }} />,
      bgColor: '#dbeafe',
    },
    {
      title: 'Yêu cầu Dữ liệu',
      value: '3 chờ xử lý',
      icon: <LockOutlined style={{ fontSize: 24, color: '#9333ea' }} />,
      bgColor: '#f3e8ff',
    },
  ];

  // GDPR requests data
  const gdprData = [
    {
      key: '1',
      requestId: 'REQ001',
      user: 'user123@gmail.com',
      requestType: 'Export data',
      requestDate: '23/03/2024',
      status: 'Chờ xử lý',
      statusColor: '#fef9c2',
      statusTextColor: '#a65f00',
      requestTypeColor: '#dbeafe',
      requestTypeTextColor: '#1447e6',
    },
    {
      key: '2',
      requestId: 'REQ002',
      user: 'user456@gmail.com',
      requestType: 'Delete account',
      requestDate: '22/03/2024',
      status: 'Hoàn thành',
      statusColor: '#dcfce7',
      statusTextColor: '#008236',
      requestTypeColor: '#ffe2e2',
      requestTypeTextColor: '#c10007',
    },
    {
      key: '3',
      requestId: 'REQ003',
      user: 'user789@gmail.com',
      requestType: 'Anonymize data',
      requestDate: '21/03/2024',
      status: 'Đang xử lý',
      statusColor: '#dbeafe',
      statusTextColor: '#1447e6',
      requestTypeColor: '#dbeafe',
      requestTypeTextColor: '#1447e6',
    },
  ];

  // Table columns
  const columns = [
    {
      title: 'Mã yêu cầu',
      dataIndex: 'requestId',
      key: 'requestId',
      width: 120,
      render: (text) => (
        <Text style={{ color: '#155dfc', fontWeight: 500 }}>{text}</Text>
      ),
    },
    {
      title: 'Người dùng',
      dataIndex: 'user',
      key: 'user',
      width: 200,
    },
    {
      title: 'Loại yêu cầu',
      dataIndex: 'requestType',
      key: 'requestType',
      width: 170,
      render: (text, record) => (
        <Tag
          color={record.requestTypeColor}
          style={{
            border: 'none',
            borderRadius: 8,
            color: record.requestTypeTextColor,
          }}
        >
          {text}
        </Tag>
      ),
    },
    {
      title: 'Ngày yêu cầu',
      dataIndex: 'requestDate',
      key: 'requestDate',
      width: 140,
    },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      key: 'status',
      width: 130,
      render: (text, record) => (
        <Tag
          color={record.statusColor}
          style={{
            border: 'none',
            borderRadius: 8,
            color: record.statusTextColor,
          }}
        >
          {text}
        </Tag>
      ),
    },
    {
      title: 'Thao tác',
      key: 'action',
      width: 210,
      align: 'right',
      render: (_, record) => {
        if (record.status === 'Chờ xử lý') {
          return (
            <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
              <Button
                type="primary"
                size="small"
                style={{
                  background: '#155dfc',
                  borderColor: '#155dfc',
                  borderRadius: 8,
                  height: 32,
                  paddingLeft: 12,
                  paddingRight: 12,
                }}
              >
                Xử lý
              </Button>
              <Button
                size="small"
                style={{
                  color: '#e7000b',
                  borderRadius: 8,
                  height: 32,
                  paddingLeft: 12,
                  paddingRight: 12,
                  border: '1px solid rgba(0,0,0,0.1)',
                }}
              >
                Từ chối
              </Button>
            </div>
          );
        } else if (record.status === 'Hoàn thành') {
          return (
            <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
              <Button
                icon={<DownloadOutlined />}
                size="small"
                style={{
                  borderRadius: 8,
                  height: 32,
                  paddingLeft: 12,
                  paddingRight: 12,
                  border: '1px solid rgba(0,0,0,0.1)',
                }}
              >
                Tải xuống
              </Button>
            </div>
          );
        }
        return null;
      },
    },
  ];

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
      {/* Stats Cards */}
      <Row gutter={[24, 24]}>
        {statsCards.map((card, index) => (
          <Col xs={24} sm={12} lg={6} key={index}>
            <Card
              style={{
                borderRadius: 14,
                border: '1px solid rgba(0,0,0,0.1)',
                height: '100%',
              }}
              bodyStyle={{ padding: '24px' }}
            >
              <div style={{ display: 'flex', gap: 12, alignItems: 'flex-start' }}>
                <div
                  style={{
                    width: 48,
                    height: 48,
                    borderRadius: 10,
                    background: card.bgColor,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    flexShrink: 0,
                  }}
                >
                  {card.icon}
                </div>
                <div style={{ flex: 1, minWidth: 0 }}>
                  <Text
                    type="secondary"
                    style={{
                      fontSize: 14,
                      display: 'block',
                      marginBottom: 32,
                      whiteSpace: 'nowrap',
                      overflow: 'hidden',
                      textOverflow: 'ellipsis',
                    }}
                  >
                    {card.title.replace('\n', ' ')}
                  </Text>
                  <Text
                    style={{
                      fontSize: 16,
                      fontWeight: 400,
                      color: '#101828',
                      display: 'block',
                    }}
                  >
                    {card.value}
                  </Text>
                </div>
              </div>
            </Card>
          </Col>
        ))}
      </Row>

      {/* Section Header */}
      <div>
        <Title level={4} style={{ margin: 0, marginBottom: 4 }}>
          Quyền riêng tư Dữ liệu & GDPR
        </Title>
        <Text type="secondary">Xử lý yêu cầu về dữ liệu cá nhân</Text>
      </div>

      {/* GDPR Requests Table */}
      <Card
        style={{
          borderRadius: 14,
          border: '1px solid rgba(0,0,0,0.1)',
        }}
        bodyStyle={{ padding: 1 }}
      >
        <Table
          columns={columns}
          dataSource={gdprData}
          pagination={false}
          scroll={{ x: 1000 }}
        />
      </Card>

      {/* GDPR Compliance Info */}
      <Card
        style={{
          borderRadius: 14,
          border: '1px solid #bedbff',
          background: '#eff6ff',
        }}
        bodyStyle={{ padding: 24 }}
      >
        <div style={{ display: 'flex', gap: 12 }}>
          <SafetyOutlined style={{ fontSize: 24, color: '#1447e6' }} />
          <div>
            <Title level={5} style={{ margin: 0, marginBottom: 8, color: '#1c398e' }}>
              Tuân thủ GDPR
            </Title>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
              <Text style={{ color: '#1447e6' }}>
                ✓ Dữ liệu được mã hóa AES-256
              </Text>
              <Text style={{ color: '#1447e6' }}>
                ✓ Users có quyền xem, export, xóa dữ liệu cá nhân
              </Text>
              <Text style={{ color: '#1447e6' }}>
                ✓ Dữ liệu không được chia sẻ với bên thứ ba
              </Text>
              <Text style={{ color: '#1447e6' }}>
                ✓ Audit logs đầy đủ cho mọi thao tác
              </Text>
            </div>
          </div>
        </div>
      </Card>
    </div>
  );
};

export default DataPrivacyTab;

