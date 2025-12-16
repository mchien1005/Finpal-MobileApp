import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ConfigProvider } from 'antd';
import viVN from 'antd/locale/vi_VN';
import LoginPage from './pages/LoginPage';
// Admin Pages
import AdminDashboardPage from './pages/admin/AdminDashboardPage';
import ContentManagementPage from './pages/admin/ContentManagementPage';
import AIModelManagementPage from './pages/admin/AIModelManagementPage';
import SecurityAndAuditPage from './pages/admin/SecurityAndAuditPage';
import SystemAndConfigPage from './pages/admin/SystemAndConfigPage';
import authService from './services/authService';
import { SidebarProvider } from './contexts/SidebarContext';

// Protected Route Component
const ProtectedRoute = ({ children }) => {
  const isAuthenticated = authService.isAuthenticated();
  return isAuthenticated ? children : <Navigate to="/login" replace />;
};

// Admin Protected Route Component
const AdminRoute = ({ children }) => {
  const isAuthenticated = authService.isAuthenticated();
  const user = authService.getCurrentUser();
  const isAdmin = user?.role === 'ADMIN';
  
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  if (!isAdmin) {
    return <Navigate to="/dashboard" replace />;
  }
  return children;
};

function App() {
  console.log('App component loaded');
  
  return (
    <ConfigProvider locale={viVN}>
      <SidebarProvider>
        <BrowserRouter>
          <Routes>
          <Route path="/login" element={<LoginPage />} />

          {/* Admin Routes */}
          <Route
            path="/admin/dashboard"
            element={
              <AdminRoute>
                <AdminDashboardPage />
              </AdminRoute>
            }
          />
          <Route
            path="/admin/content"
            element={
              <AdminRoute>
                <ContentManagementPage />
              </AdminRoute>
            }
          />
          <Route
            path="/admin/ai-models"
            element={
              <AdminRoute>
                <AIModelManagementPage />
              </AdminRoute>
            }
            />
            <Route
            path="/admin/security"
            element={
              <AdminRoute>
                <SecurityAndAuditPage />
              </AdminRoute>
            }
            />
            <Route
            path="/admin/settings"
            element={
              <AdminRoute>
                <SystemAndConfigPage />
              </AdminRoute>
            }
          />
          <Route
            path="/admin/*"
            element={
              <AdminRoute>
                <AdminDashboardPage />
              </AdminRoute>
            }
          />

          <Route path="/" element={<Navigate to="/login" replace />} />
          <Route path="*" element={<Navigate to="/login" replace />} />
          </Routes>
        </BrowserRouter>
      </SidebarProvider>
    </ConfigProvider>
  );
}

export default App;
