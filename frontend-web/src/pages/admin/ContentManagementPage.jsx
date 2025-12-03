import React, { useState } from 'react';
import { Button, Space } from 'antd';
import {
  PlusOutlined,
  SendOutlined,
  EditOutlined,
  DeleteOutlined,
} from '@ant-design/icons';
import AdminSidebar from '../../components/admin/AdminSidebar';
import { useSidebar } from '../../contexts/SidebarContext';

// Dữ liệu mẫu thông báo
const NOTIFICATION_TEMPLATES = [
  {
    id: 'NOT001',
    title: 'Chi tiêu vượt ngân sách',
    message: 'Bạn đã chi {amount} cho {category}, vượt {percent}% so với kế hoạch!',
    type: 'warning',
    typeLabel: 'warning',
    sent: 234,
    status: 'Active',
  },
  {
    id: 'NOT002',
    title: 'Giao dịch bất thường',
    message: 'Phát hiện giao dịch {amount} tại {location}, không khớp với thói quen của bạn',
    type: 'alert',
    typeLabel: 'alert',
    sent: 45,
    status: 'Active',
  },
  {
    id: 'NOT003',
    title: 'Tiết kiệm tốt',
    message: 'Tuyệt vời! Bạn đã tiết kiệm được {amount} trong tháng này 🎉',
    type: 'success',
    typeLabel: 'success',
    sent: 567,
    status: 'Active',
  },
];

// Dữ liệu mẹo và gợi ý
const TIPS_DATA = [
  {
    id: 'TIP001',
    title: 'Quy tắc 50/30/20',
    content: 'Chia thu nhập: 50% nhu cầu thiết yếu, 30% mong muốn, 20% tiết kiệm',
    category: 'Tiết kiệm',
    views: 1234,
    status: 'Active',
  },
  {
    id: 'TIP002',
    title: 'Theo dõi chi tiêu hàng ngày',
    content: 'Ghi chép mọi khoản chi để hiểu rõ thói quen tiêu dùng của bạn',
    category: 'Quản lý',
    views: 856,
    status: 'Active',
  },
  {
    id: 'TIP003',
    title: 'Lập quỹ khẩn cấp',
    content: 'Nên có quỹ dự phòng bằng 3-6 tháng chi tiêu để đối phó tình huống bất ngờ',
    category: 'Tiết kiệm',
    views: 2341,
    status: 'Active',
  },
  {
    id: 'TIP004',
    title: 'Đầu tư sớm',
    content: 'Bắt đầu đầu tư càng sớm càng tốt để tận dụng lãi kép',
    category: 'Đầu tư',
    views: 1567,
    status: 'Draft',
  },
];

// Dữ liệu câu hỏi thường gặp
const FAQ_DATA = [
  {
    id: 'FAQ001',
    question: 'Làm thế nào để thêm giao dịch mới?',
    answer: 'Bạn có thể thêm giao dịch bằng cách nhấn nút "+" trên màn hình chính hoặc vào mục "Thêm giao dịch" trong menu.',
    category: 'Hướng dẫn',
    helpful: 156,
    status: 'Active',
  },
  {
    id: 'FAQ002',
    question: 'FinPal có bảo mật thông tin của tôi không?',
    answer: 'Có, FinPal sử dụng mã hóa AES-256 và không chia sẻ dữ liệu của bạn với bên thứ ba.',
    category: 'Bảo mật',
    helpful: 234,
    status: 'Active',
  },
  {
    id: 'FAQ003',
    question: 'Làm sao để đặt mục tiêu tiết kiệm?',
    answer: 'Vào mục "Mục tiêu tiết kiệm" trong menu, nhấn "Thêm mục tiêu" và nhập thông tin mục tiêu của bạn.',
    category: 'Hướng dẫn',
    helpful: 189,
    status: 'Active',
  },
  {
    id: 'FAQ004',
    question: 'AI gợi ý hoạt động như thế nào?',
    answer: 'AI phân tích thói quen chi tiêu của bạn và đưa ra gợi ý cá nhân hóa để tiết kiệm hiệu quả hơn.',
    category: 'Tính năng',
    helpful: 312,
    status: 'Active',
  },
];

const tabs = ['Mẫu thông báo', 'Mẹo và gợi ý', 'Câu hỏi thường gặp'];

const typeStyles = {
  warning: {
    background: '#FEF9C2',
    color: '#A65F00',
  },
  alert: {
    background: '#FFE2E2',
    color: '#C10007',
  },
  success: {
    background: '#DCFCE7',
    color: '#008236',
  },
};

const categoryStyles = {
  'Tiết kiệm': { background: '#E0F2FE', color: '#0369A1' },
  'Quản lý': { background: '#FEF3C7', color: '#B45309' },
  'Đầu tư': { background: '#DCFCE7', color: '#15803D' },
  'Hướng dẫn': { background: '#E0E7FF', color: '#4338CA' },
  'Bảo mật': { background: '#FCE7F3', color: '#BE185D' },
  'Tính năng': { background: '#F3E8FF', color: '#7C3AED' },
};

const ContentManagementPage = () => {
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
            Quản lý nội dung
          </h1>
          <p
            style={{
              fontSize: 16,
              color: '#6A7282',
              margin: 0,
              fontFamily: 'Arimo, sans-serif',
            }}
          >
            Quản lý thông báo, tips tiết kiệm và câu hỏi thường gặp
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
        {activeTab === 0 && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
            {/* Section Header */}
            <div
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
              }}
            >
              <div>
                <h3
                  style={{
                    fontSize: 16,
                    fontWeight: 400,
                    color: '#101828',
                    margin: 0,
                    marginBottom: 0,
                    fontFamily: 'Arimo, sans-serif',
                  }}
                >
                  Mẫu thông báo
                </h3>
                <p
                  style={{
                    fontSize: 14,
                    color: '#6A7282',
                    margin: 0,
                    fontFamily: 'Arimo, sans-serif',
                  }}
                >
                  Quản lý template thông báo gửi cho users
                </p>
              </div>
              <Button
                type="primary"
                icon={<PlusOutlined />}
                style={{
                  background: '#155DFC',
                  borderRadius: 8,
                  height: 36,
                  fontFamily: 'Arimo, sans-serif',
                  fontSize: 14,
                }}
              >
                Thêm mẫu
              </Button>
            </div>

            {/* Table Card */}
            <div
              style={{
                background: '#FFFFFF',
                border: '1px solid rgba(0,0,0,0.1)',
                borderRadius: 14,
                overflow: 'hidden',
              }}
            >
              <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                <thead>
                  <tr style={{ borderBottom: '1px solid rgba(0,0,0,0.1)' }}>
                    <th style={{ padding: '10px 8px', textAlign: 'left', fontSize: 14, fontWeight: 400, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif', width: 70 }}>ID</th>
                    <th style={{ padding: '10px 8px', textAlign: 'left', fontSize: 14, fontWeight: 400, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif', width: 174 }}>Tiêu đề</th>
                    <th style={{ padding: '10px 8px', textAlign: 'left', fontSize: 14, fontWeight: 400, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif' }}>Message Template</th>
                    <th style={{ padding: '10px 8px', textAlign: 'left', fontSize: 14, fontWeight: 400, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif', width: 82 }}>Loại</th>
                    <th style={{ padding: '10px 8px', textAlign: 'left', fontSize: 14, fontWeight: 400, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif', width: 61 }}>Đã gửi</th>
                    <th style={{ padding: '10px 8px', textAlign: 'left', fontSize: 14, fontWeight: 400, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif', width: 84 }}>Trạng thái</th>
                    <th style={{ padding: '10px 8px', textAlign: 'right', fontSize: 14, fontWeight: 400, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif', width: 147 }}>Thao tác</th>
                  </tr>
                </thead>
                <tbody>
                  {NOTIFICATION_TEMPLATES.map((item, index) => (
                    <tr key={item.id} style={{ borderBottom: index < NOTIFICATION_TEMPLATES.length - 1 ? '1px solid rgba(0,0,0,0.1)' : 'none' }}>
                      <td style={{ padding: '13px 8px', fontSize: 14, color: '#155DFC', fontFamily: 'Arimo, sans-serif' }}>{item.id}</td>
                      <td style={{ padding: '13px 8px', fontSize: 14, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif' }}>{item.title}</td>
                      <td style={{ padding: '13px 8px', fontSize: 14, color: '#4A5565', fontFamily: 'Arimo, sans-serif' }}>{item.message}</td>
                      <td style={{ padding: '13px 8px' }}>
                        <span style={{ display: 'inline-block', padding: '2px 8px', borderRadius: 8, fontSize: 12, fontFamily: 'Arimo, sans-serif', background: typeStyles[item.type]?.background || '#ECECF0', color: typeStyles[item.type]?.color || '#6A7282' }}>{item.typeLabel}</span>
                      </td>
                      <td style={{ padding: '13px 8px', fontSize: 14, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif' }}>{item.sent}</td>
                      <td style={{ padding: '13px 8px' }}>
                        <span style={{ display: 'inline-block', padding: '2px 8px', borderRadius: 8, fontSize: 12, fontFamily: 'Arimo, sans-serif', background: '#DCFCE7', color: '#008236' }}>{item.status}</span>
                      </td>
                      <td style={{ padding: '13px 8px', textAlign: 'right' }}>
                        <Space size={8}>
                          <Button type="text" icon={<SendOutlined style={{ fontSize: 16, color: '#6B7280' }} />} style={{ width: 36, height: 32, padding: 0 }} />
                          <Button type="text" icon={<EditOutlined style={{ fontSize: 16, color: '#6B7280' }} />} style={{ width: 36, height: 32, padding: 0 }} />
                          <Button type="text" icon={<DeleteOutlined style={{ fontSize: 16, color: '#EF4444' }} />} style={{ width: 36, height: 32, padding: 0 }} />
                        </Space>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* Tab 2: Mẹo và gợi ý */}
        {activeTab === 1 && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div>
                <h3 style={{ fontSize: 16, fontWeight: 400, color: '#101828', margin: 0, fontFamily: 'Arimo, sans-serif' }}>Mẹo và gợi ý</h3>
                <p style={{ fontSize: 14, color: '#6A7282', margin: 0, fontFamily: 'Arimo, sans-serif' }}>Quản lý các tips tiết kiệm và gợi ý tài chính</p>
              </div>
              <Button type="primary" icon={<PlusOutlined />} style={{ background: '#155DFC', borderRadius: 8, height: 36, fontFamily: 'Arimo, sans-serif', fontSize: 14 }}>Thêm mẹo</Button>
            </div>

            <div style={{ background: '#FFFFFF', border: '1px solid rgba(0,0,0,0.1)', borderRadius: 14, overflow: 'hidden' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                <thead>
                  <tr style={{ borderBottom: '1px solid rgba(0,0,0,0.1)' }}>
                    <th style={{ padding: '10px 8px', textAlign: 'left', fontSize: 14, fontWeight: 400, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif', width: 70 }}>ID</th>
                    <th style={{ padding: '10px 8px', textAlign: 'left', fontSize: 14, fontWeight: 400, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif', width: 180 }}>Tiêu đề</th>
                    <th style={{ padding: '10px 8px', textAlign: 'left', fontSize: 14, fontWeight: 400, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif' }}>Nội dung</th>
                    <th style={{ padding: '10px 8px', textAlign: 'left', fontSize: 14, fontWeight: 400, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif', width: 100 }}>Danh mục</th>
                    <th style={{ padding: '10px 8px', textAlign: 'left', fontSize: 14, fontWeight: 400, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif', width: 80 }}>Lượt xem</th>
                    <th style={{ padding: '10px 8px', textAlign: 'left', fontSize: 14, fontWeight: 400, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif', width: 84 }}>Trạng thái</th>
                    <th style={{ padding: '10px 8px', textAlign: 'right', fontSize: 14, fontWeight: 400, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif', width: 120 }}>Thao tác</th>
                  </tr>
                </thead>
                <tbody>
                  {TIPS_DATA.map((item, index) => (
                    <tr key={item.id} style={{ borderBottom: index < TIPS_DATA.length - 1 ? '1px solid rgba(0,0,0,0.1)' : 'none' }}>
                      <td style={{ padding: '13px 8px', fontSize: 14, color: '#155DFC', fontFamily: 'Arimo, sans-serif' }}>{item.id}</td>
                      <td style={{ padding: '13px 8px', fontSize: 14, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif' }}>{item.title}</td>
                      <td style={{ padding: '13px 8px', fontSize: 14, color: '#4A5565', fontFamily: 'Arimo, sans-serif' }}>{item.content}</td>
                      <td style={{ padding: '13px 8px' }}>
                        <span style={{ display: 'inline-block', padding: '2px 8px', borderRadius: 8, fontSize: 12, fontFamily: 'Arimo, sans-serif', background: categoryStyles[item.category]?.background || '#ECECF0', color: categoryStyles[item.category]?.color || '#6A7282' }}>{item.category}</span>
                      </td>
                      <td style={{ padding: '13px 8px', fontSize: 14, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif' }}>{item.views}</td>
                      <td style={{ padding: '13px 8px' }}>
                        <span style={{ display: 'inline-block', padding: '2px 8px', borderRadius: 8, fontSize: 12, fontFamily: 'Arimo, sans-serif', background: item.status === 'Active' ? '#DCFCE7' : '#FEF3C7', color: item.status === 'Active' ? '#008236' : '#B45309' }}>{item.status}</span>
                      </td>
                      <td style={{ padding: '13px 8px', textAlign: 'right' }}>
                        <Space size={8}>
                          <Button type="text" icon={<EditOutlined style={{ fontSize: 16, color: '#6B7280' }} />} style={{ width: 36, height: 32, padding: 0 }} />
                          <Button type="text" icon={<DeleteOutlined style={{ fontSize: 16, color: '#EF4444' }} />} style={{ width: 36, height: 32, padding: 0 }} />
                        </Space>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* Tab 3: Câu hỏi thường gặp */}
        {activeTab === 2 && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div>
                <h3 style={{ fontSize: 16, fontWeight: 400, color: '#101828', margin: 0, fontFamily: 'Arimo, sans-serif' }}>Câu hỏi thường gặp</h3>
                <p style={{ fontSize: 14, color: '#6A7282', margin: 0, fontFamily: 'Arimo, sans-serif' }}>Quản lý FAQ cho người dùng</p>
              </div>
              <Button type="primary" icon={<PlusOutlined />} style={{ background: '#155DFC', borderRadius: 8, height: 36, fontFamily: 'Arimo, sans-serif', fontSize: 14 }}>Thêm FAQ</Button>
            </div>

            <div style={{ background: '#FFFFFF', border: '1px solid rgba(0,0,0,0.1)', borderRadius: 14, overflow: 'hidden' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                <thead>
                  <tr style={{ borderBottom: '1px solid rgba(0,0,0,0.1)' }}>
                    <th style={{ padding: '10px 8px', textAlign: 'left', fontSize: 14, fontWeight: 400, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif', width: 70 }}>ID</th>
                    <th style={{ padding: '10px 8px', textAlign: 'left', fontSize: 14, fontWeight: 400, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif', width: 250 }}>Câu hỏi</th>
                    <th style={{ padding: '10px 8px', textAlign: 'left', fontSize: 14, fontWeight: 400, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif' }}>Trả lời</th>
                    <th style={{ padding: '10px 8px', textAlign: 'left', fontSize: 14, fontWeight: 400, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif', width: 100 }}>Danh mục</th>
                    <th style={{ padding: '10px 8px', textAlign: 'left', fontSize: 14, fontWeight: 400, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif', width: 80 }}>Hữu ích</th>
                    <th style={{ padding: '10px 8px', textAlign: 'left', fontSize: 14, fontWeight: 400, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif', width: 84 }}>Trạng thái</th>
                    <th style={{ padding: '10px 8px', textAlign: 'right', fontSize: 14, fontWeight: 400, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif', width: 120 }}>Thao tác</th>
                  </tr>
                </thead>
                <tbody>
                  {FAQ_DATA.map((item, index) => (
                    <tr key={item.id} style={{ borderBottom: index < FAQ_DATA.length - 1 ? '1px solid rgba(0,0,0,0.1)' : 'none' }}>
                      <td style={{ padding: '13px 8px', fontSize: 14, color: '#155DFC', fontFamily: 'Arimo, sans-serif' }}>{item.id}</td>
                      <td style={{ padding: '13px 8px', fontSize: 14, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif' }}>{item.question}</td>
                      <td style={{ padding: '13px 8px', fontSize: 14, color: '#4A5565', fontFamily: 'Arimo, sans-serif', maxWidth: 300, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{item.answer}</td>
                      <td style={{ padding: '13px 8px' }}>
                        <span style={{ display: 'inline-block', padding: '2px 8px', borderRadius: 8, fontSize: 12, fontFamily: 'Arimo, sans-serif', background: categoryStyles[item.category]?.background || '#ECECF0', color: categoryStyles[item.category]?.color || '#6A7282' }}>{item.category}</span>
                      </td>
                      <td style={{ padding: '13px 8px', fontSize: 14, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif' }}>{item.helpful}</td>
                      <td style={{ padding: '13px 8px' }}>
                        <span style={{ display: 'inline-block', padding: '2px 8px', borderRadius: 8, fontSize: 12, fontFamily: 'Arimo, sans-serif', background: '#DCFCE7', color: '#008236' }}>{item.status}</span>
                      </td>
                      <td style={{ padding: '13px 8px', textAlign: 'right' }}>
                        <Space size={8}>
                          <Button type="text" icon={<EditOutlined style={{ fontSize: 16, color: '#6B7280' }} />} style={{ width: 36, height: 32, padding: 0 }} />
                          <Button type="text" icon={<DeleteOutlined style={{ fontSize: 16, color: '#EF4444' }} />} style={{ width: 36, height: 32, padding: 0 }} />
                        </Space>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default ContentManagementPage;
