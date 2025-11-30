import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ConfigProvider } from 'antd';
import viVN from 'antd/locale/vi_VN';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/user/DashboardPage';
import OnboardingStep1 from './pages/user/OnboardingStep1';
import OnboardingStep2 from './pages/user/OnboardingStep2';
import OnboardingStep3 from './pages/user/OnboardingStep3';
import authService from './services/authService';



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
          <Route path="/" element={<Navigate to="/login" replace />} />
          <Route path="*" element={<Navigate to="/login" replace />} />
          </Routes>
        </BrowserRouter>
      </SidebarProvider>
    </ConfigProvider>
  );
}

export default App;
