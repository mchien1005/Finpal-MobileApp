//Bảo mật và nhật ký audit
import React, { useState } from 'react';
import AdminSidebar from '../../components/admin/AdminSidebar';
import { useSidebar } from '../../contexts/SidebarContext';

const tabs = ['Cài đặt Hệ thống', 'Sao lưu & Khôi phục', 'Quản lý API'];

const SystemAndConfigPage = () => {
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
            Hệ thống & Cấu hình
          </h1>
          <p
            style={{
              fontSize: 16,
              color: '#6A7282',
              margin: 0,
              fontFamily: 'Arimo, sans-serif',
            }}
          >
            Cấu hình toàn bộ hệ thống, backup và API
          </p>
        </div>

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
        {activeTab === 0 && <import tab vô đây />}
        {activeTab === 1 && <import tab vô đây />}
        {activeTab === 2 && <import tab vô đây />}
        {/* //ví dụ: */}
        {/* {activeTab === 0 && <NotificationTemplatesTab />}
        {activeTab === 1 && <TipsTab />}
        {activeTab === 2 && <FAQsTab />} */}
      </div>
    </div>
  );
};

export default SystemAndConfigPage;
