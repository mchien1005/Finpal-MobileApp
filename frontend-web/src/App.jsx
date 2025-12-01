import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ConfigProvider } from 'antd';
import viVN from 'antd/locale/vi_VN';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/user/DashboardPage';
import TransactionPage from './pages/user/TransactionPage';
import NotificationPage from './pages/user/NotificationPage';
import SettingsPage from './pages/user/SettingsPage';
import ChangePasswordPage from './pages/user/ChangePasswordPage';
import ProfilePage from './pages/user/ProfilePage';
import HelpCenterPage from './pages/user/HelpCenterPage';
import NotificationSettingsPage from './pages/user/NotificationSettingsPage';
import OnboardingStep1 from './pages/user/OnboardingStep1';
import OnboardingStep2 from './pages/user/OnboardingStep2';
import OnboardingStep3 from './pages/user/OnboardingStep3';
// Admin Pages
import AdminDashboardPage from './pages/admin/AdminDashboardPage';
import ContentManagementPage from './pages/admin/ContentManagementPage';
import AIModelManagementPage from './pages/admin/AIModelManagementPage';
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
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/onboarding/step1" element={<OnboardingStep1 />} />
          <Route path="/onboarding/step2" element={<OnboardingStep2 />} />
          <Route path="/onboarding/step3" element={<OnboardingStep3 />} />
          
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <DashboardPage />
              </ProtectedRoute>
            }
          />
          
          <Route
            path="/transactions/add"
            element={
              <ProtectedRoute>
                <TransactionPage />
              </ProtectedRoute>
            }
          />
          
          <Route
            path="/notifications"
            element={
              <ProtectedRoute>
                <NotificationPage />
              </ProtectedRoute>
            }
          />
          
          <Route
            path="/settings"
            element={
              <ProtectedRoute>
                <SettingsPage />
              </ProtectedRoute>
            }
          />
          
          <Route
            path="/settings/password"
            element={
              <ProtectedRoute>
                <ChangePasswordPage />
              </ProtectedRoute>
            }
          />

          <Route
            path="/settings/profile"
            element={
              <ProtectedRoute>
                <ProfilePage />
              </ProtectedRoute>
            }
            />
          <Route path="/settings/help" element={<ProtectedRoute><HelpCenterPage /></ProtectedRoute>} />
            <Route
            path="/settings/notifications"
            element={
              <ProtectedRoute>
                <NotificationSettingsPage />
              </ProtectedRoute>
            }
            />


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
