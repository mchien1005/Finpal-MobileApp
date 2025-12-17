import React from 'react';
import { Card } from 'antd';

const PermissionTab = () => {
  return (
    <Card
      style={{
        borderRadius: 12,
        border: '1px solid #e5e7eb',
        boxShadow: 'none',
        background: '#fff',
      }}
      bodyStyle={{ padding: '20px 24px' }}
    >
      <div style={{ textAlign: 'center', padding: '40px 0' }}>
        <h3 style={{ fontSize: 16, fontWeight: 600, color: '#111827', margin: 0, marginBottom: 8 }}>
          Phân quyền
        </h3>
        <p style={{ fontSize: 14, color: '#6b7280', margin: 0 }}>
          Nội dung đang được phát triển
        </p>
      </div>
    </Card>
  );
};

export default PermissionTab;
