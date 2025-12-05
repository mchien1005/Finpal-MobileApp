import React, { useState, useEffect } from 'react';
import { Button, Space, Spin, message } from 'antd';
import {
  PlusOutlined,
  SendOutlined,
  EditOutlined,
  DeleteOutlined,
  LoadingOutlined,
} from '@ant-design/icons';
import AdminSidebar from '../../components/admin/AdminSidebar';
import { useSidebar } from '../../contexts/SidebarContext';
import ConfirmModal from '../../components/common/ConfirmModal';
import SuccessModal from '../../components/common/SuccessModal';
import NotificationTemplateModal from '../../components/admin/NotificationTemplateModal';
import {
  getAllNotificationTemplates,
  getAllTips,
  getAllFAQs,
  getTemplateTypeLabel,
  getStatusLabel,
  getTipCategoryLabel,
  getFAQCategoryLabel,
  sendNotificationFromTemplate,
  createNotificationTemplate,
  updateNotificationTemplate,
  deleteNotificationTemplate,
} from '../../services/contentService';

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
  info: {
    background: '#E0F2FE',
    color: '#0369A1',
  },
};

const categoryStyles = {
  'Tiết kiệm': { background: '#E0F2FE', color: '#0369A1' },
  'Ngân sách': { background: '#FEF3C7', color: '#B45309' },
  'Quản lý': { background: '#FEF3C7', color: '#B45309' },
  'Đầu tư': { background: '#DCFCE7', color: '#15803D' },
  'Chi tiêu': { background: '#FFE2E2', color: '#C10007' },
  'Tổng quan': { background: '#F3E8FF', color: '#7C3AED' },
  'Hướng dẫn': { background: '#E0E7FF', color: '#4338CA' },
  'Bảo mật': { background: '#FCE7F3', color: '#BE185D' },
  'Tính năng': { background: '#F3E8FF', color: '#7C3AED' },
  'Khắc phục': { background: '#FEF3C7', color: '#B45309' },
};

const ContentManagementPage = () => {
  const { collapsed } = useSidebar();
  const [activeTab, setActiveTab] = useState(0);
  
  // State for data
  const [notificationTemplates, setNotificationTemplates] = useState([]);
  const [tips, setTips] = useState([]);
  const [faqs, setFaqs] = useState([]);
  
  // Loading states
  const [loadingTemplates, setLoadingTemplates] = useState(false);
  const [loadingTips, setLoadingTips] = useState(false);
  const [loadingFaqs, setLoadingFaqs] = useState(false);

  // Send notification states
  const [sendingTemplate, setSendingTemplate] = useState(null);
  const [showConfirmSend, setShowConfirmSend] = useState(false);
  const [showSuccessSend, setShowSuccessSend] = useState(false);
  const [sendResult, setSendResult] = useState(null);
  const [isSending, setIsSending] = useState(false);

  // Template modal states (thêm/sửa)
  const [showTemplateModal, setShowTemplateModal] = useState(false);
  const [editingTemplate, setEditingTemplate] = useState(null);
  const [isSubmittingTemplate, setIsSubmittingTemplate] = useState(false);

  // Delete template states
  const [deletingTemplate, setDeletingTemplate] = useState(null);
  const [showConfirmDelete, setShowConfirmDelete] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);

  // Fetch notification templates
  const fetchNotificationTemplates = async () => {
    setLoadingTemplates(true);
    try {
      const data = await getAllNotificationTemplates();
      setNotificationTemplates(data);
    } catch (error) {
      console.error('Error fetching notification templates:', error);
      message.error('Không thể tải mẫu thông báo');
    } finally {
      setLoadingTemplates(false);
    }
  };

  // Fetch tips
  const fetchTips = async () => {
    setLoadingTips(true);
    try {
      const data = await getAllTips();
      setTips(data);
    } catch (error) {
      console.error('Error fetching tips:', error);
      message.error('Không thể tải mẹo và gợi ý');
    } finally {
      setLoadingTips(false);
    }
  };

  // Fetch FAQs
  const fetchFaqs = async () => {
    setLoadingFaqs(true);
    try {
      const data = await getAllFAQs();
      setFaqs(data);
    } catch (error) {
      console.error('Error fetching FAQs:', error);
      message.error('Không thể tải câu hỏi thường gặp');
    } finally {
      setLoadingFaqs(false);
    }
  };

  // Fetch data on mount
  useEffect(() => {
    fetchNotificationTemplates();
    fetchTips();
    fetchFaqs();
  }, []);

  // Handle send notification
  const handleSendClick = (template) => {
    if (template.status !== 'ACTIVE') {
      message.warning('Chỉ có thể gửi mẫu thông báo đang Active');
      return;
    }
    setSendingTemplate(template);
    setShowConfirmSend(true);
  };

  const handleConfirmSend = async () => {
    if (!sendingTemplate) return;
    
    setIsSending(true);
    try {
      const result = await sendNotificationFromTemplate({
        templateId: sendingTemplate.id,
        userIds: null, // null = gửi cho tất cả users
      });
      setSendResult(result);
      setShowConfirmSend(false);
      setShowSuccessSend(true);
      // Refresh to update sentCount
      fetchNotificationTemplates();
    } catch (error) {
      console.error('Error sending notification:', error);
      message.error('Không thể gửi thông báo: ' + (error.response?.data?.message || error.message));
    } finally {
      setIsSending(false);
    }
  };

  const handleCancelSend = () => {
    setShowConfirmSend(false);
    setSendingTemplate(null);
  };

  const handleCloseSuccess = () => {
    setShowSuccessSend(false);
    setSendingTemplate(null);
    setSendResult(null);
  };

  // Handle Add Template
  const handleAddClick = () => {
    setEditingTemplate(null);
    setShowTemplateModal(true);
  };

  // Handle Edit Template
  const handleEditClick = (template) => {
    setEditingTemplate(template);
    setShowTemplateModal(true);
  };

  // Handle Submit Template (Add/Edit)
  const handleTemplateSubmit = async (data) => {
    setIsSubmittingTemplate(true);
    try {
      if (editingTemplate) {
        // Update existing template
        await updateNotificationTemplate(editingTemplate.id, data);
        message.success('Cập nhật template thành công!');
      } else {
        // Create new template
        await createNotificationTemplate(data);
        message.success('Thêm template thành công!');
      }
      setShowTemplateModal(false);
      setEditingTemplate(null);
      fetchNotificationTemplates();
    } catch (error) {
      console.error('Error submitting template:', error);
      message.error(
        error.response?.data?.message || 
        (editingTemplate ? 'Không thể cập nhật template' : 'Không thể thêm template')
      );
    } finally {
      setIsSubmittingTemplate(false);
    }
  };

  // Handle Close Template Modal
  const handleCloseTemplateModal = () => {
    setShowTemplateModal(false);
    setEditingTemplate(null);
  };

  // Handle Delete Click
  const handleDeleteClick = (template) => {
    setDeletingTemplate(template);
    setShowConfirmDelete(true);
  };

  // Handle Confirm Delete
  const handleConfirmDelete = async () => {
    if (!deletingTemplate) return;
    
    setIsDeleting(true);
    try {
      await deleteNotificationTemplate(deletingTemplate.id);
      message.success('Xóa template thành công!');
      setShowConfirmDelete(false);
      setDeletingTemplate(null);
      fetchNotificationTemplates();
    } catch (error) {
      console.error('Error deleting template:', error);
      message.error(error.response?.data?.message || 'Không thể xóa template');
    } finally {
      setIsDeleting(false);
    }
  };

  // Handle Cancel Delete
  const handleCancelDelete = () => {
    setShowConfirmDelete(false);
    setDeletingTemplate(null);
  };

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
                onClick={handleAddClick}
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
              {loadingTemplates ? (
                <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', padding: 40 }}>
                  <Spin indicator={<LoadingOutlined style={{ fontSize: 24 }} spin />} />
                </div>
              ) : (
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
                  {notificationTemplates.length === 0 ? (
                    <tr>
                      <td colSpan={7} style={{ padding: '40px', textAlign: 'center', color: '#6A7282', fontFamily: 'Arimo, sans-serif' }}>
                        Chưa có mẫu thông báo nào
                      </td>
                    </tr>
                  ) : (
                  notificationTemplates.map((item, index) => {
                    const typeLabel = getTemplateTypeLabel(item.type);
                    const statusLabel = getStatusLabel(item.status);
                    return (
                    <tr key={item.id} style={{ borderBottom: index < notificationTemplates.length - 1 ? '1px solid rgba(0,0,0,0.1)' : 'none' }}>
                      <td style={{ padding: '13px 8px', fontSize: 14, color: '#155DFC', fontFamily: 'Arimo, sans-serif' }}>{item.templateCode || `NOT${String(item.id).padStart(3, '0')}`}</td>
                      <td style={{ padding: '13px 8px', fontSize: 14, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif' }}>{item.title}</td>
                      <td style={{ padding: '13px 8px', fontSize: 14, color: '#4A5565', fontFamily: 'Arimo, sans-serif' }}>{item.messageTemplate}</td>
                      <td style={{ padding: '13px 8px' }}>
                        <span style={{ display: 'inline-block', padding: '2px 8px', borderRadius: 8, fontSize: 12, fontFamily: 'Arimo, sans-serif', background: typeStyles[typeLabel]?.background || '#ECECF0', color: typeStyles[typeLabel]?.color || '#6A7282' }}>{typeLabel}</span>
                      </td>
                      <td style={{ padding: '13px 8px', fontSize: 14, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif' }}>{item.sentCount || 0}</td>
                      <td style={{ padding: '13px 8px' }}>
                        <span style={{ display: 'inline-block', padding: '2px 8px', borderRadius: 8, fontSize: 12, fontFamily: 'Arimo, sans-serif', background: item.status === 'ACTIVE' ? '#DCFCE7' : '#FEF3C7', color: item.status === 'ACTIVE' ? '#008236' : '#B45309' }}>{statusLabel}</span>
                      </td>
                      <td style={{ padding: '13px 8px', textAlign: 'right' }}>
                        <Space size={8}>
                          <Button 
                            type="text" 
                            icon={<SendOutlined style={{ fontSize: 16, color: item.status === 'ACTIVE' ? '#155DFC' : '#D1D5DB' }} />} 
                            style={{ width: 36, height: 32, padding: 0 }} 
                            onClick={() => handleSendClick(item)}
                            disabled={item.status !== 'ACTIVE'}
                            title={item.status === 'ACTIVE' ? 'Gửi thông báo' : 'Chỉ có thể gửi mẫu Active'}
                          />
                          <Button 
                            type="text" 
                            icon={<EditOutlined style={{ fontSize: 16, color: '#6B7280' }} />} 
                            style={{ width: 36, height: 32, padding: 0 }} 
                            onClick={() => handleEditClick(item)}
                            title="Chỉnh sửa"
                          />
                          <Button 
                            type="text" 
                            icon={<DeleteOutlined style={{ fontSize: 16, color: '#EF4444' }} />} 
                            style={{ width: 36, height: 32, padding: 0 }} 
                            onClick={() => handleDeleteClick(item)}
                            title="Xóa"
                          />
                        </Space>
                      </td>
                    </tr>
                    );
                  })
                  )}
                </tbody>
              </table>
              )}
            </div>
          </div>
        )}

        {/* Confirm Send Modal */}
        <ConfirmModal
          open={showConfirmSend}
          onConfirm={handleConfirmSend}
          onCancel={handleCancelSend}
          title="Xác nhận gửi thông báo"
          content={sendingTemplate ? `Bạn có chắc muốn gửi thông báo "${sendingTemplate.title}" đến tất cả người dùng?` : ''}
          confirmText={isSending ? 'Đang gửi...' : 'Gửi thông báo'}
          cancelText="Hủy"
          danger={false}
        />

        {/* Success Send Modal */}
        <SuccessModal
          open={showSuccessSend}
          onClose={handleCloseSuccess}
          message={sendResult ? `Đã gửi thông báo đến ${sendResult.sentCount} người dùng.` : 'Thông báo đã được gửi thành công.'}
          buttonText="Đóng"
        />

        {/* Template Add/Edit Modal */}
        <NotificationTemplateModal
          open={showTemplateModal}
          onClose={handleCloseTemplateModal}
          onSubmit={handleTemplateSubmit}
          template={editingTemplate}
          loading={isSubmittingTemplate}
        />

        {/* Delete Confirmation Modal */}
        <ConfirmModal
          open={showConfirmDelete}
          onConfirm={handleConfirmDelete}
          onCancel={handleCancelDelete}
          title="Xác nhận xóa"
          content={deletingTemplate ? `Bạn có chắc muốn xóa mẫu thông báo "${deletingTemplate.title}"?` : ''}
          confirmText={isDeleting ? 'Đang xóa...' : 'Xóa'}
          cancelText="Hủy"
          danger={true}
        />

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
              {loadingTips ? (
                <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', padding: 40 }}>
                  <Spin indicator={<LoadingOutlined style={{ fontSize: 24 }} spin />} />
                </div>
              ) : (
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
                  {tips.length === 0 ? (
                    <tr>
                      <td colSpan={7} style={{ padding: '40px', textAlign: 'center', color: '#6A7282', fontFamily: 'Arimo, sans-serif' }}>
                        Chưa có mẹo nào
                      </td>
                    </tr>
                  ) : (
                  tips.map((item, index) => {
                    const categoryLabel = getTipCategoryLabel(item.category);
                    const statusLabel = getStatusLabel(item.status);
                    return (
                    <tr key={item.id} style={{ borderBottom: index < tips.length - 1 ? '1px solid rgba(0,0,0,0.1)' : 'none' }}>
                      <td style={{ padding: '13px 8px', fontSize: 14, color: '#155DFC', fontFamily: 'Arimo, sans-serif' }}>{item.tipCode || `TIP${String(item.id).padStart(3, '0')}`}</td>
                      <td style={{ padding: '13px 8px', fontSize: 14, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif' }}>{item.title}</td>
                      <td style={{ padding: '13px 8px', fontSize: 14, color: '#4A5565', fontFamily: 'Arimo, sans-serif' }}>{item.content}</td>
                      <td style={{ padding: '13px 8px' }}>
                        <span style={{ display: 'inline-block', padding: '2px 8px', borderRadius: 8, fontSize: 12, fontFamily: 'Arimo, sans-serif', background: categoryStyles[categoryLabel]?.background || '#ECECF0', color: categoryStyles[categoryLabel]?.color || '#6A7282' }}>{categoryLabel}</span>
                      </td>
                      <td style={{ padding: '13px 8px', fontSize: 14, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif' }}>{item.viewCount || 0}</td>
                      <td style={{ padding: '13px 8px' }}>
                        <span style={{ display: 'inline-block', padding: '2px 8px', borderRadius: 8, fontSize: 12, fontFamily: 'Arimo, sans-serif', background: item.status === 'ACTIVE' ? '#DCFCE7' : '#FEF3C7', color: item.status === 'ACTIVE' ? '#008236' : '#B45309' }}>{statusLabel}</span>
                      </td>
                      <td style={{ padding: '13px 8px', textAlign: 'right' }}>
                        <Space size={8}>
                          <Button type="text" icon={<EditOutlined style={{ fontSize: 16, color: '#6B7280' }} />} style={{ width: 36, height: 32, padding: 0 }} />
                          <Button type="text" icon={<DeleteOutlined style={{ fontSize: 16, color: '#EF4444' }} />} style={{ width: 36, height: 32, padding: 0 }} />
                        </Space>
                      </td>
                    </tr>
                    );
                  })
                  )}
                </tbody>
              </table>
              )}
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
              {loadingFaqs ? (
                <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', padding: 40 }}>
                  <Spin indicator={<LoadingOutlined style={{ fontSize: 24 }} spin />} />
                </div>
              ) : (
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
                  {faqs.length === 0 ? (
                    <tr>
                      <td colSpan={7} style={{ padding: '40px', textAlign: 'center', color: '#6A7282', fontFamily: 'Arimo, sans-serif' }}>
                        Chưa có câu hỏi nào
                      </td>
                    </tr>
                  ) : (
                  faqs.map((item, index) => {
                    const categoryLabel = getFAQCategoryLabel(item.category);
                    const statusLabel = getStatusLabel(item.status);
                    return (
                    <tr key={item.id} style={{ borderBottom: index < faqs.length - 1 ? '1px solid rgba(0,0,0,0.1)' : 'none' }}>
                      <td style={{ padding: '13px 8px', fontSize: 14, color: '#155DFC', fontFamily: 'Arimo, sans-serif' }}>{item.faqCode || `FAQ${String(item.id).padStart(3, '0')}`}</td>
                      <td style={{ padding: '13px 8px', fontSize: 14, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif' }}>{item.question}</td>
                      <td style={{ padding: '13px 8px', fontSize: 14, color: '#4A5565', fontFamily: 'Arimo, sans-serif', maxWidth: 300, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{item.answer}</td>
                      <td style={{ padding: '13px 8px' }}>
                        <span style={{ display: 'inline-block', padding: '2px 8px', borderRadius: 8, fontSize: 12, fontFamily: 'Arimo, sans-serif', background: categoryStyles[categoryLabel]?.background || '#ECECF0', color: categoryStyles[categoryLabel]?.color || '#6A7282' }}>{categoryLabel}</span>
                      </td>
                      <td style={{ padding: '13px 8px', fontSize: 14, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif' }}>{item.helpfulCount || 0}</td>
                      <td style={{ padding: '13px 8px' }}>
                        <span style={{ display: 'inline-block', padding: '2px 8px', borderRadius: 8, fontSize: 12, fontFamily: 'Arimo, sans-serif', background: item.status === 'ACTIVE' ? '#DCFCE7' : '#FEF3C7', color: item.status === 'ACTIVE' ? '#008236' : '#B45309' }}>{statusLabel}</span>
                      </td>
                      <td style={{ padding: '13px 8px', textAlign: 'right' }}>
                        <Space size={8}>
                          <Button type="text" icon={<EditOutlined style={{ fontSize: 16, color: '#6B7280' }} />} style={{ width: 36, height: 32, padding: 0 }} />
                          <Button type="text" icon={<DeleteOutlined style={{ fontSize: 16, color: '#EF4444' }} />} style={{ width: 36, height: 32, padding: 0 }} />
                        </Space>
                      </td>
                    </tr>
                    );
                  })
                  )}
                </tbody>
              </table>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default ContentManagementPage;
