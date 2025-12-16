import React from 'react';
import AdminSidebar from '../components/admin/AdminSidebar';
import { useSidebar } from '../contexts/SidebarContext';

const AdminLayout = ({ children }) => {
  const { collapsed } = useSidebar();

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
        {children}
      </div>
    </div>
  );
};

export default AdminLayout;
