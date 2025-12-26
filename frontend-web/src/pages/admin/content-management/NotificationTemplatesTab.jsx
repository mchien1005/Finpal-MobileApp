import React, { useState, useEffect } from 'react';
import { Button, Space, Spin, message } from 'antd';
import {
  PlusOutlined,
  SendOutlined,
  EditOutlined,
  DeleteOutlined,
  LoadingOutlined,
} from '@ant-design/icons';
import ConfirmModal from '../../../components/common/ConfirmModal';
import SuccessModal from '../../../components/common/SuccessModal';
import NotificationTemplateModal from '../../../components/admin/NotificationTemplateModal';
import {
  getAllNotificationTemplates,
  getTemplateTypeLabel,
  getStatusLabel,
  sendNotificationFromTemplate,
  createNotificationTemplate,
  updateNotificationTemplate,
  deleteNotificationTemplate,
} from '../../../services/contentService';

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

const NotificationTemplatesTab = () => {
  // State for data
  const [notificationTemplates, setNotificationTemplates] = useState([]);
  
  // Loading states
  const [loadingTemplates, setLoadingTemplates] = useState(false);

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

  // Fetch data on mount
  useEffect(() => {
    fetchNotificationTemplates();
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
        // Tìm số lớn nhất từ templateCode hiện có (NOT001, NOT002,...)
        let maxNumber = 0;
        notificationTemplates.forEach((t) => {
          const code = t.templateCode || '';
          const match = code.match(/^NOT(\d+)$/);
          if (match) {
            const num = parseInt(match[1], 10);
            if (num > maxNumber) maxNumber = num;
          }
        });
        const nextNumber = maxNumber + 1;
        const templateData = {
          ...data,
          templateCode: `NOT${String(nextNumber).padStart(3, '0')}`,
        };
        await createNotificationTemplate(templateData);
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
    <>
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
    </>
  );
};

export default NotificationTemplatesTab;
