//Bảo mật và nhật ký audit
import React, { useState } from 'react';
import { Row, Col, Card } from 'antd';
import { CheckCircleOutlined, WarningOutlined, ThunderboltOutlined, LockOutlined } from '@ant-design/icons';
import AdminSidebar from '../../components/admin/AdminSidebar';
import { useSidebar } from '../../contexts/SidebarContext';
import AuditLogTab from './security-audit/AuditLogTab';
import PermissionTab from './security-audit/PermissionTab';
import DataPrivacyTab from './security-audit/DataPrivacyTab';

const tabs = ['Nhật ký Audit', 'Phân quyền', 'Quyền riêng tư Dữ liệu'];

const SecurityAndAuditPage = () => {
  const { collapsed } = useSidebar();
  const [activeTab, setActiveTab] = useState(0);

  return (
    <div style={{ display: 'flex', minHeight: '100vh', background: '#F9FAFB' }}>
      <AdminSidebar />
      <div
        style={{
          marginLeft: collapsed ? 80 : 288,
          flex: 1,
          transition: 'margin-left 0.3s',
          minHeight: '100vh',
          padding: '32px',
          overflow: 'auto',
        }}
      >
        {/* Header Section */}
        <div style={{ marginBottom: 24 }}>
          <h1
            style={{
              fontSize: 24,
              fontWeight: 400,
              color: '#101828',
              margin: 0,
              marginBottom: 4,
              fontFamily: 'Arimo, sans-serif',
            }}
          >
            Bảo mật & Audit
          </h1>
          <p
            style={{
              fontSize: 16,
              color: '#6A7282',
              margin: 0,
              fontFamily: 'Arimo, sans-serif',
            }}
          >
            Audit log, permissions và date privacy
          </p>
        </div>

        {/* Stats Cards */}
        <Row gutter={[24, 24]} style={{ marginBottom: 24 }}>
          <Col xs={24} sm={12} lg={6}>
            <Card
              style={{
                borderRadius: 12,
                border: '1px solid #e5e7eb',
                boxShadow: 'none',
                background: '#fff',
              }}
              bodyStyle={{ padding: '20px 24px' }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                <div
                  style={{
                    width: 48,
                    height: 48,
                    borderRadius: 12,
                    background: '#dcfce7',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    flexShrink: 0,
                  }}
                >
                  <CheckCircleOutlined style={{ fontSize: 24, color: '#16a34a' }} />
                </div>
                <div style={{ flex: 1 }}>
                  <p style={{ fontSize: 14, color: '#6b7280', margin: 0, marginBottom: 4 }}>
                    Điểm Bảo mật
                  </p>
                  <h3 style={{ fontSize: 24, fontWeight: 600, color: '#111827', margin: 0 }}>
                    98/100
                  </h3>
                </div>
              </div>
            </Card>
          </Col>

          <Col xs={24} sm={12} lg={6}>
            <Card
              style={{
                borderRadius: 12,
                border: '1px solid #e5e7eb',
                boxShadow: 'none',
                background: '#fff',
              }}
              bodyStyle={{ padding: '20px 24px' }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                <div
                  style={{
                    width: 48,
                    height: 48,
                    borderRadius: 12,
                    background: '#fee2e2',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    flexShrink: 0,
                  }}
                >
                  <WarningOutlined style={{ fontSize: 24, color: '#dc2626' }} />
                </div>
                <div style={{ flex: 1 }}>
                  <p style={{ fontSize: 14, color: '#6b7280', margin: 0, marginBottom: 4 }}>
                    Đăng nhập Thất bại
                  </p>
                  <h3 style={{ fontSize: 24, fontWeight: 600, color: '#111827', margin: 0 }}>
                    15 hôm nay
                  </h3>
                </div>
              </div>
            </Card>
          </Col>

          <Col xs={24} sm={12} lg={6}>
            <Card
              style={{
                borderRadius: 12,
                border: '1px solid #e5e7eb',
                boxShadow: 'none',
                background: '#fff',
              }}
              bodyStyle={{ padding: '20px 24px' }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                <div
                  style={{
                    width: 48,
                    height: 48,
                    borderRadius: 12,
                    background: '#dbeafe',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    flexShrink: 0,
                  }}
                >
                  <ThunderboltOutlined style={{ fontSize: 24, color: '#2563eb' }} />
                </div>
                <div style={{ flex: 1 }}>
                  <p style={{ fontSize: 14, color: '#6b7280', margin: 0, marginBottom: 4 }}>
                    Admin Hoạt động
                  </p>
                  <h3 style={{ fontSize: 24, fontWeight: 600, color: '#111827', margin: 0 }}>
                    2
                  </h3>
                </div>
              </div>
            </Card>
          </Col>

          <Col xs={24} sm={12} lg={6}>
            <Card
              style={{
                borderRadius: 12,
                border: '1px solid #e5e7eb',
                boxShadow: 'none',
                background: '#fff',
              }}
              bodyStyle={{ padding: '20px 24px' }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                <div
                  style={{
                    width: 48,
                    height: 48,
                    borderRadius: 12,
                    background: '#f3e8ff',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    flexShrink: 0,
                  }}
                >
                  <LockOutlined style={{ fontSize: 24, color: '#9333ea' }} />
                </div>
                <div style={{ flex: 1 }}>
                  <p style={{ fontSize: 14, color: '#6b7280', margin: 0, marginBottom: 4 }}>
                    Yêu cầu Dữ liệu
                  </p>
                  <h3 style={{ fontSize: 24, fontWeight: 600, color: '#111827', margin: 0 }}>
                    3 chờ xử lý
                  </h3>
                </div>
              </div>
            </Card>
          </Col>
        </Row>

        {/* Tab List */}
        <div
          style={{
            background: '#ECECF0',
            borderRadius: 14,
            padding: 4,
            display: 'inline-flex',
            gap: 0,
            marginBottom: 32,
          }}
        >
          {tabs.map((tab, index) => (
            <div
              key={tab}
              onClick={() => setActiveTab(index)}
              style={{
                padding: '5px 16px',
                borderRadius: 14,
                background: activeTab === index ? '#FFFFFF' : 'transparent',
                border: activeTab === index ? '1px solid rgba(0,0,0,0)' : '1px solid transparent',
                cursor: 'pointer',
                fontSize: 14,
                color: '#0A0A0A',
                fontFamily: 'Arimo, sans-serif',
                fontWeight: 400,
                transition: 'all 0.2s',
              }}
            >
              {tab}
            </div>
          ))}
        </div>

        {/* Tab Panel Content */}
        {activeTab === 0 && <AuditLogTab />}
        {activeTab === 1 && <PermissionTab />}
        {activeTab === 2 && <DataPrivacyTab />}
      </div>
    </div>
  );
};

export default SecurityAndAuditPage;
