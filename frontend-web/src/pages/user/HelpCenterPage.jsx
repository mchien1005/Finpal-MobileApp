import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Input, Spin, message } from 'antd';
import {
  SearchOutlined,
  MessageOutlined,
  MailOutlined,
  PhoneOutlined,
  LeftOutlined,
  DownOutlined,
  UpOutlined,
  LikeOutlined,
  DislikeOutlined,
  LoadingOutlined,
} from '@ant-design/icons';
import Sidebar from '../../components/user/Sidebar';
import Header from '../../components/common/Header';
import { useSidebar } from '../../contexts/SidebarContext';
import { getActiveFAQs, groupFAQsByCategory, submitFAQFeedback } from '../../services/faqService';

// Icons from Figma
const iconChat = 'https://www.figma.com/api/mcp/asset/8565b25d-f0ec-4580-a313-a0b72cb7fb1c';
const iconEmail = 'https://www.figma.com/api/mcp/asset/0e62209a-5d9c-464d-a1df-06af306be242';
const iconPhone = 'https://www.figma.com/api/mcp/asset/86d240fa-f61a-40a2-9a0b-074ac393de39';

// Fallback FAQ data khi API không có dữ liệu
const defaultFaqSections = [
  {
    category: 'Bắt đầu',
    items: [
      { q: 'FinPal là gì?', a: 'FinPal là ứng dụng quản lý tài chính cá nhân thông minh, giúp bạn theo dõi thu chi, phân loại giao dịch tự động bằng AI và đặt mục tiêu tiết kiệm.' },
      { q: 'Làm sao để bắt đầu sử dụng FinPal?', a: 'Tải ứng dụng, đăng ký tài khoản, cấp quyền đọc SMS và bắt đầu theo dõi chi tiêu của bạn ngay.' },
      { q: 'FinPal có miễn phí không?', a: 'Có, FinPal hoàn toàn miễn phí với các tính năng cơ bản. Phiên bản Premium sẽ có thêm các tính năng nâng cao.' },
    ],
  },
  {
    category: 'Bảo mật & Quyền riêng tư',
    items: [
      { q: 'Dữ liệu của tôi có an toàn không?', a: 'Dữ liệu của bạn được mã hóa và lưu trữ an toàn. Chúng tôi không chia sẻ thông tin cá nhân với bên thứ ba.' },
      { q: 'FinPal có chia sẻ thông tin của tôi không?', a: 'Không, chúng tôi cam kết bảo mật thông tin của bạn và không chia sẻ với bất kỳ bên thứ ba nào.' },
      { q: 'Làm sao để bảo vệ tài khoản tốt hơn?', a: 'Sử dụng mật khẩu mạnh, bật xác thực 2 lớp và không chia sẻ thông tin đăng nhập với người khác.' },
    ],
  },
  {
    category: 'Tính năng',
    items: [
      { q: 'AI phân loại giao dịch hoạt động như thế nào?', a: 'AI của FinPal phân tích nội dung SMS ngân hàng để tự động nhận diện và phân loại giao dịch vào các danh mục phù hợp.' },
      { q: 'Tôi có thể chỉnh sửa giao dịch không?', a: 'Có, bạn có thể chỉnh sửa danh mục, ghi chú và thông tin giao dịch bất cứ lúc nào.' },
      { q: 'Làm sao để đặt mục tiêu tiết kiệm?', a: 'Vào tab "Mục tiêu", nhấn nút "+" để tạo mục tiêu mới. Nhập tên, số tiền mục tiêu và thời hạn, FinPal sẽ giúp bạn theo dõi tiến độ.' },
    ],
  },
  {
    category: 'Khắc phục sự cố',
    items: [
      { q: 'App không đọc được SMS từ ngân hàng?', a: 'Kiểm tra lại quyền đọc SMS trong cài đặt điện thoại. Đảm bảo FinPal có quyền truy cập tin nhắn.' },
      { q: 'Giao dịch bị phân loại sai?', a: 'Bạn có thể chỉnh sửa danh mục giao dịch. AI sẽ học từ các chỉnh sửa của bạn để cải thiện độ chính xác.' },
      { q: 'Quên mật khẩu thì làm sao?', a: 'Nhấn "Quên mật khẩu" trên màn hình đăng nhập, nhập email và làm theo hướng dẫn để đặt lại mật khẩu.' },
    ],
  },
];

// Contact support options
const supportOptions = [
  {
    icon: iconChat,
    bgColor: '#EFF6FF',
    title: 'Chat trực tiếp',
    subtitle: 'Phản hồi trong vài phút',
  },
  {
    icon: iconEmail,
    bgColor: '#F0FDF4',
    title: 'Email',
    subtitle: 'support@finpal.vn',
  },
  {
    icon: iconPhone,
    bgColor: '#FFF7ED',
    title: 'Hotline',
    subtitle: '1900-xxxx (8h-22h)',
  },
];

const HelpCenterPage = () => {
  const { collapsed } = useSidebar();
  const navigate = useNavigate();
  const [expandedItems, setExpandedItems] = useState({});
  const [faqSections, setFaqSections] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchText, setSearchText] = useState('');
  const [feedbackLoading, setFeedbackLoading] = useState({});
  const [feedbackSubmitted, setFeedbackSubmitted] = useState({});

  // Fetch FAQs từ API
  useEffect(() => {
    const fetchFAQs = async () => {
      try {
        setLoading(true);
        const data = await getActiveFAQs();
        if (data && data.length > 0) {
          const grouped = groupFAQsByCategory(data);
          setFaqSections(grouped);
        } else {
          // Sử dụng dữ liệu mặc định nếu API không có dữ liệu
          setFaqSections(defaultFaqSections);
        }
      } catch (error) {
        console.error('Error fetching FAQs:', error);
        // Fallback to default data
        setFaqSections(defaultFaqSections);
      } finally {
        setLoading(false);
      }
    };

    fetchFAQs();
  }, []);

  const toggleItem = (sectionIdx, itemIdx) => {
    const key = `${sectionIdx}-${itemIdx}`;
    setExpandedItems((prev) => ({ ...prev, [key]: !prev[key] }));
  };

  // Xử lý feedback (helpful/not helpful)
  const handleFeedback = async (faqId, isHelpful, sectionIdx, itemIdx) => {
    const key = `${sectionIdx}-${itemIdx}`;
    
    // Nếu đã feedback rồi thì bỏ qua
    if (feedbackSubmitted[key]) {
      message.info('Bạn đã gửi phản hồi cho câu hỏi này rồi!');
      return;
    }
    
    try {
      setFeedbackLoading((prev) => ({ ...prev, [key]: true }));
      
      // Chỉ gọi API nếu có ID (dữ liệu từ API)
      if (faqId) {
        await submitFAQFeedback(faqId, isHelpful);
      }
      
      setFeedbackSubmitted((prev) => ({ ...prev, [key]: isHelpful ? 'helpful' : 'not_helpful' }));
      message.success(isHelpful ? 'Cảm ơn phản hồi của bạn!' : 'Cảm ơn, chúng tôi sẽ cải thiện!');
    } catch (error) {
      console.error('Error submitting feedback:', error);
      message.error('Không thể gửi phản hồi. Vui lòng thử lại.');
    } finally {
      setFeedbackLoading((prev) => ({ ...prev, [key]: false }));
    }
  };

  // Lọc FAQs theo từ khóa tìm kiếm
  const filteredFaqSections = searchText.trim()
    ? faqSections
        .map((section) => ({
          ...section,
          items: section.items.filter(
            (item) =>
              item.q.toLowerCase().includes(searchText.toLowerCase()) ||
              item.a.toLowerCase().includes(searchText.toLowerCase())
          ),
        }))
        .filter((section) => section.items.length > 0)
    : faqSections;

  return (
    <div style={{ display: 'flex', minHeight: '100vh', background: '#F5F7FA' }}>
      <Sidebar />

      <div style={{ marginLeft: collapsed ? 80 : 280, flex: 1, transition: 'margin-left 0.3s' }}>
        <Header title="Cài đặt" />

        {/* Content */}
        <div style={{ padding: '0 16px', display: 'flex', flexDirection: 'column', gap: 24 }}>
          {/* Header Banner */}
          <div
            style={{
              background: 'linear-gradient(to right, #155DFC, #4F39F6)',
              borderRadius: '0 0 24px 24px',
              padding: '24px 16px 16px 16px',
              boxShadow: '0 10px 15px -3px rgba(0,0,0,0.1), 0 4px 6px -4px rgba(0,0,0,0.1)',
            }}
          >
            {/* Back + Title */}
            <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 16 }}>
              <div
                onClick={() => navigate('/settings')}
                style={{ cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 4 }}
              >
                <LeftOutlined style={{ fontSize: 12, color: '#FFFFFF' }} />
                <span style={{ fontSize: 14, color: '#FFFFFF' }}>Quay lại</span>
              </div>
              <div style={{ flex: 1 }}>
                <div style={{ fontSize: 16, color: '#FFFFFF', fontWeight: 400 }}>Trung tâm trợ giúp</div>
                <div style={{ fontSize: 14, color: '#DBEAFE' }}>Chúng tôi sẵn sàng hỗ trợ bạn</div>
              </div>
            </div>

            {/* Search */}
            <div style={{ position: 'relative' }}>
              <Input
                placeholder="Tìm kiếm câu hỏi..."
                prefix={<SearchOutlined style={{ color: '#717182' }} />}
                value={searchText}
                onChange={(e) => setSearchText(e.target.value)}
                allowClear
                style={{
                  height: 36,
                  borderRadius: 8,
                  background: 'rgba(255,255,255,0.9)',
                  border: 'none',
                }}
              />
            </div>
          </div>

          {/* Support Contact Section */}
          <div style={{ padding: '0 0px' }}>
            <div style={{ fontSize: 16, color: '#364153', marginBottom: 12 }}>Liên hệ hỗ trợ</div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
              {supportOptions.map((opt, idx) => (
                <div
                  key={idx}
                  style={{
                    background: '#FFFFFF',
                    borderRadius: 14,
                    padding: 16,
                    boxShadow: '0 4px 6px -1px rgba(0,0,0,0.1), 0 2px 4px -2px rgba(0,0,0,0.1)',
                    display: 'flex',
                    alignItems: 'center',
                    gap: 12,
                    cursor: 'pointer',
                  }}
                >
                  <div
                    style={{
                      width: 48,
                      height: 48,
                      borderRadius: 14,
                      background: opt.bgColor,
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                    }}
                  >
                    <img src={opt.icon} alt="" style={{ width: 24, height: 24 }} />
                  </div>
                  <div>
                    <div style={{ fontSize: 16, color: '#101828', fontWeight: 400 }}>{opt.title}</div>
                    <div style={{ fontSize: 14, color: '#6A7282' }}>{opt.subtitle}</div>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* FAQ Section */}
          <div style={{ padding: '0 0px', paddingBottom: 32 }}>
            <div style={{ fontSize: 16, color: '#364153', marginBottom: 12 }}>Câu hỏi thường gặp</div>

            {loading ? (
              <div style={{ display: 'flex', justifyContent: 'center', padding: '40px 0' }}>
                <Spin indicator={<LoadingOutlined style={{ fontSize: 24 }} spin />} />
              </div>
            ) : filteredFaqSections.length === 0 ? (
              <div
                style={{
                  background: '#FFFFFF',
                  borderRadius: 14,
                  padding: 24,
                  textAlign: 'center',
                  color: '#6B7280',
                }}
              >
                {searchText ? 'Không tìm thấy câu hỏi phù hợp' : 'Chưa có câu hỏi nào'}
              </div>
            ) : (
              filteredFaqSections.map((section, sectionIdx) => (
                <div key={sectionIdx} style={{ marginBottom: 8 }}>
                  {/* Category label */}
                  <div style={{ fontSize: 14, color: '#4A5565', padding: '0 8px', marginBottom: 8 }}>
                    {section.category}
                  </div>

                  {/* FAQ Card */}
                  <div
                    style={{
                      background: '#FFFFFF',
                      borderRadius: 14,
                      boxShadow: '0 4px 6px -1px rgba(0,0,0,0.1), 0 2px 4px -2px rgba(0,0,0,0.1)',
                      overflow: 'hidden',
                      marginBottom: 12,
                    }}
                  >
                    {section.items.map((item, itemIdx) => {
                      const key = `${sectionIdx}-${itemIdx}`;
                      const isExpanded = expandedItems[key];
                      const isLast = itemIdx === section.items.length - 1;

                      return (
                        <div key={item.id || itemIdx}>
                          <div
                            onClick={() => toggleItem(sectionIdx, itemIdx)}
                            style={{
                              display: 'flex',
                              alignItems: 'center',
                              justifyContent: 'space-between',
                              padding: '14px 12px',
                              cursor: 'pointer',
                              borderBottom: isLast && !isExpanded ? 'none' : '1px solid rgba(0,0,0,0.1)',
                            }}
                          >
                            <span style={{ fontSize: 14, color: '#0A0A0A' }}>{item.q}</span>
                            {isExpanded ? (
                              <UpOutlined style={{ fontSize: 12, color: '#6B7280' }} />
                            ) : (
                              <DownOutlined style={{ fontSize: 12, color: '#6B7280' }} />
                            )}
                          </div>
                          {isExpanded && (
                            <div
                              style={{
                                padding: '0 12px 12px 12px',
                                borderBottom: isLast ? 'none' : '1px solid rgba(0,0,0,0.1)',
                              }}
                            >
                              <div
                              style={{
                                fontSize: 14,
                                color: '#4A5565',
                                lineHeight: 1.5,
                                marginBottom: 12,
                              }}
                            >
                              {item.a}
                            </div>
                            {/* Feedback buttons */}
                            <div
                              style={{
                                display: 'flex',
                                alignItems: 'center',
                                gap: 16,
                                paddingTop: 8,
                                borderTop: '1px solid rgba(0,0,0,0.05)',
                              }}
                            >
                              {feedbackSubmitted[key] ? (
                                <span style={{ fontSize: 12, color: '#059669' }}>
                                  ✓ Cảm ơn phản hồi của bạn!
                                </span>
                              ) : (
                                <>
                                  <span style={{ fontSize: 12, color: '#9CA3AF' }}>
                                    Câu trả lời này có hữu ích không?
                                  </span>
                                  <button
                                    onClick={(e) => {
                                      e.stopPropagation();
                                      handleFeedback(item.id, true, sectionIdx, itemIdx);
                                    }}
                                    disabled={feedbackLoading[key]}
                                    style={{
                                      display: 'flex',
                                      alignItems: 'center',
                                      gap: 4,
                                      padding: '4px 8px',
                                      border: '1px solid #E5E7EB',
                                      borderRadius: 6,
                                      background: '#F9FAFB',
                                      cursor: feedbackLoading[key] ? 'not-allowed' : 'pointer',
                                      fontSize: 12,
                                      color: '#059669',
                                    }}
                                  >
                                    <LikeOutlined /> Có
                                  </button>
                                  <button
                                    onClick={(e) => {
                                      e.stopPropagation();
                                      handleFeedback(item.id, false, sectionIdx, itemIdx);
                                    }}
                                    disabled={feedbackLoading[key]}
                                    style={{
                                      display: 'flex',
                                      alignItems: 'center',
                                      gap: 4,
                                      padding: '4px 8px',
                                      border: '1px solid #E5E7EB',
                                      borderRadius: 6,
                                      background: '#F9FAFB',
                                      cursor: feedbackLoading[key] ? 'not-allowed' : 'pointer',
                                      fontSize: 12,
                                      color: '#DC2626',
                                    }}
                                  >
                                    <DislikeOutlined /> Không
                                  </button>
                                </>
                              )}
                            </div>
                            </div>
                          )}
                        </div>
                      );
                    })}
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default HelpCenterPage;
