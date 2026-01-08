import React, { useState, useEffect } from 'react';
import { Card, Table, Tag, message, Spin } from 'antd';
import activityLogService from '../../../services/activityLogService';

const AuditLogTab = () => {
  const [adminLogs, setAdminLogs] = useState([]);
  const [loading, setLoading] = useState(false);

  // Fetch activity logs from API
  useEffect(() => {
    fetchActivityLogs();
  }, []);

  const fetchActivityLogs = async () => {
    setLoading(true);
    try {
      const data = await activityLogService.getActivityLogs({ limit: 50 });
      console.log('Activity logs data:', data);
      // Transform API data to match table format
      const transformedData = data.map((log, index) => ({
        key: log.id || index,
        timestamp: log.timestampText || formatTimestamp(log.timestamp),
        admin: log.adminName || 'N/A',
        action: log.action || 'N/A',
        object: log.description || 'N/A',
        ipAddress: log.ipAddress || 'N/A',
        riskLevel: determineRiskLevel(log.action),
        status: log.status || 'SUCCESS',
        entityType: log.entityType || 'N/A',
      }));
      setAdminLogs(transformedData);
    } catch (error) {
      console.error('Error fetching activity logs:', error);
      message.error('Không thể tải nhật ký hoạt động');
      // Set empty array on error
      setAdminLogs([]);
    } finally {
      setLoading(false);
    }
  };

  // Helper function to format timestamp
  const formatTimestamp = (timestamp) => {
    if (!timestamp) return 'N/A';
    const date = new Date(timestamp);
    return date.toLocaleString('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
    });
  };

  // Helper function to determine risk level based on action
  const determineRiskLevel = (action) => {
    if (!action) return 'low';
    const actionLower = action.toLowerCase();
    if (actionLower.includes('delete') || actionLower.includes('remove')) return 'high';
    if (actionLower.includes('update') || actionLower.includes('modify')) return 'medium';
    return 'low';
  };

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
          }}
        >
          <h3 style={{ fontSize: 16, fontWeight: 600, color: '#111827', margin: 0, marginBottom: 4 }}>
            Nhật ký Hoạt động Admin
          </h3>
          <p style={{ fontSize: 14, color: '#6b7280', margin: 0 }}>
            Lịch sử tất cả actions của admin
          </p>
        </div>
        <Spin spinning={loading}>
          <Table
            columns={adminColumns}
            dataSource={adminLogs}
            pagination={{
              pageSize: 10,
              showSizeChanger: true,
              showTotal: (total) => `Tổng ${total} bản ghi`,
            }}
            tableLayout="fixed"
          />
        </Spin>
      </Card>
    </div>
  );
};

export default AuditLogTab;
