import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ConfigProvider } from 'antd';
import viVN from 'antd/locale/vi_VN';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';



function App() {
  console.log('App component loaded');
  
  return (
    <ConfigProvider locale={viVN}>
      <SidebarProvider>
        <BrowserRouter>
          <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          </Routes>
        </BrowserRouter>
      </SidebarProvider>
    </ConfigProvider>
  );
}

export default App;
