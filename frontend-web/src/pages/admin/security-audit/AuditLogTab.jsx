import React, { useState } from 'react';
import { Card, Table, Button, Tag } from 'antd';
import { DownloadOutlined, EyeOutlined } from '@ant-design/icons';
import LoginFailureDetailModal from '../../../components/admin/LoginFailureDetailModal';

const AuditLogTab = () => {
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [selectedLogin, setSelectedLogin] = useState(null);

  const [adminLogs] = useState([
    {
      key: '1',
      timestamp: '24/03/2024 10:45:23',
      admin: 'admin@finpal.com',
      action: 'User deleted',
      object: 'USR12345',
      ipAddress: '192.168.1.1',
      riskLevel: 'high',
    },
    {
      key: '2',
      timestamp: '24/03/2024 10:32:15',
      admin: 'admin@finpal.com',
      action: 'Category created',
      object: 'CAT_NEW_001',
      ipAddress: '192.168.1.1',
      riskLevel: 'low',
    },
    {
      key: '3',
      timestamp: '24/03/2024 10:18:47',
      admin: 'moderator@finpal.com',
      action: 'Bank template updated',
      object: 'VCB',
      ipAddress: '192.168.1.5',
      riskLevel: 'medium',
    },
    {
      key: '4',
      timestamp: '24/03/2024 09:54:32',
      admin: 'admin@finpal.com',
      action: 'SMS parser modified',
      object: 'TCB',
      ipAddress: '192.168.1.1',
      riskLevel: 'high',
    },
  ]);

  const [loginLogs] = useState([
    {
      key: '1',
      timestamp: '24/03/2024 11:20',
      user: 'unknown@gmail.com',
      ipAddress: '203.123.45.67',
      attempts: '5 lần',
      status: 'Đã chặn',
    },
    {
      key: '2',
      timestamp: '24/03/2024 10:45',
      user: 'test@test.com',
      ipAddress: '123.45.67.89',
      attempts: '3 lần',
      status: 'Đang theo dõi',
    },
    {
      key: '3',
      timestamp: '23/03/2024 22:15',
      user: 'admin@fake.com',
      ipAddress: '98.76.54.32',
      attempts: '7 lần',
      status: 'Đã chặn',
    },
  ]);

  const adminColumns = [
    {
      title: 'Thời gian',
      dataIndex: 'timestamp',
      key: 'timestamp',
      width: 160,
      render: (text) => <span style={{ fontSize: 14, color: '#111827' }}>{text}</span>,
    },
    {
      title: 'Admin',
      dataIndex: 'admin',
      key: 'admin',
      width: 200,
      render: (text) => <span style={{ fontSize: 14, color: '#2563eb', cursor: 'pointer' }}>{text}</span>,
    },
    {
      title: 'Hành động',
      dataIndex: 'action',
      key: 'action',
      width: 180,
      render: (text) => <span style={{ fontSize: 14, color: '#111827' }}>{text}</span>,
    },
    {
      title: 'Đối tượng',
      dataIndex: 'object',
      key: 'object',
      width: 150,
      render: (text) => <span style={{ fontSize: 14, color: '#111827' }}>{text}</span>,
    },
    {
      title: 'Địa chỉ IP',
      dataIndex: 'ipAddress',
      key: 'ipAddress',
      width: 140,
      render: (text) => <span style={{ fontSize: 14, color: '#111827' }}>{text}</span>,
    },
    {
      title: 'Mức độ',
      dataIndex: 'riskLevel',
      key: 'riskLevel',
      width: 100,
      render: (level) => {
        const config = {
          high: { bg: '#fee2e2', color: '#dc2626', text: 'high' },
          medium: { bg: '#fef3c7', color: '#f59e0b', text: 'medium' },
          low: { bg: '#dcfce7', color: '#16a34a', text: 'low' },
        };
        const style = config[level] || config.low;
        return (
          <Tag
            style={{
              background: style.bg,
              color: style.color,
              border: 'none',
              borderRadius: 6,
              fontSize: 12,
              fontWeight: 500,
              padding: '2px 10px',
            }}
          >
            {style.text}
          </Tag>
        );
      },
    },
  ];

  const loginColumns = [
    {
      title: 'Thời gian',
      dataIndex: 'timestamp',
      key: 'timestamp',
      width: 160,
      render: (text) => <span style={{ fontSize: 14, color: '#111827' }}>{text}</span>,
    },
    {
      title: 'Người dùng/Email',
      dataIndex: 'user',
      key: 'user',
      width: 200,
      render: (text) => <span style={{ fontSize: 14, color: '#111827' }}>{text}</span>,
    },
    {
      title: 'Địa chỉ IP',
      dataIndex: 'ipAddress',
      key: 'ipAddress',
      width: 150,
      render: (text) => <span style={{ fontSize: 14, color: '#111827' }}>{text}</span>,
    },
    {
      title: 'Số lần thử',
      dataIndex: 'attempts',
      key: 'attempts',
      width: 120,
      render: (text) => (
        <Tag
          style={{
            background: '#fee2e2',
            color: '#dc2626',
            border: 'none',
            borderRadius: 6,
            fontSize: 12,
            fontWeight: 500,
            padding: '2px 10px',
          }}
        >
          {text}
        </Tag>
      ),
    },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      key: 'status',
      width: 140,
      render: (text) => {
        const isBlocked = text === 'Đã chặn';
        return (
          <Tag
            style={{
              background: isBlocked ? '#fee2e2' : '#fef3c7',
              color: isBlocked ? '#dc2626' : '#f59e0b',
              border: 'none',
              borderRadius: 6,
              fontSize: 12,
              fontWeight: 500,
              padding: '2px 10px',
            }}
          >
            {text}
          </Tag>
        );
      },
    },
    {
      title: 'Thao tác',
      key: 'action',
      width: 100,
      align: 'center',
      render: (_, record) => (
        <Button
          type="text"
          icon={<EyeOutlined style={{ fontSize: 16 }} />}
          style={{ color: '#111827' }}
          onClick={() => {
            setSelectedLogin(record);
            setDetailModalVisible(true);
          }}
        />
      ),
    },
  ];

  return (
    <div>
      {/* Nhật ký Hoạt động Admin */}
      <Card
        style={{
          borderRadius: 12,
          border: '1px solid #e5e7eb',
          boxShadow: 'none',
          background: '#fff',
          marginBottom: 24,
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
              Nhật ký Hoạt động Admin
            </h3>
            <p style={{ fontSize: 14, color: '#6b7280', margin: 0 }}>
              Lịch sử tất cả actions của admin
            </p>
          </div>
          <Button
            icon={<DownloadOutlined />}
            style={{
              background: '#fff',
              border: '1px solid #e5e7eb',
              borderRadius: 8,
              height: 36,
              padding: '0 16px',
              fontSize: 14,
              color: '#374151',
            }}
          >
            Xuất Logs
          </Button>
        </div>
        <Table
          columns={adminColumns}
          dataSource={adminLogs}
          pagination={false}
          tableLayout="fixed"
        />
      </Card>

      {/* Lịch sử Đăng nhập Thất bại */}
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
          }}
        >
          <h3 style={{ fontSize: 16, fontWeight: 600, color: '#111827', margin: 0 }}>
            Lịch sử Đăng nhập Thất bại
          </h3>
        </div>
        <Table
          columns={loginColumns}
          dataSource={loginLogs}
          pagination={false}
          tableLayout="fixed"
        />
      </Card>

      {/* Login Failure Detail Modal */}
      <LoginFailureDetailModal
        visible={detailModalVisible}
        onClose={() => setDetailModalVisible(false)}
        loginData={selectedLogin}
      />
    </div>
  );
};

export default AuditLogTab;
