import React, { useState } from 'react';
import { Modal, Input, Select, message } from 'antd';
import { CloseOutlined } from '@ant-design/icons';

const { TextArea } = Input;

const typeOptions = [
  { value: 'INFO', label: 'Info' },
  { value: 'WARNING', label: 'Warning' },
  { value: 'ALERT', label: 'Alert' },
  { value: 'SUCCESS', label: 'Success' },
];

const statusOptions = [
  { value: 'ACTIVE', label: 'Active' },
  { value: 'INACTIVE', label: 'Inactive' },
];

const NotificationTemplateModal = ({
  open,
  onClose,
  onSubmit,
  template = null, // null = thêm mới, object = sửa
  loading = false,
}) => {
  const [title, setTitle] = useState('');
  const [messageTemplate, setMessageTemplate] = useState('');
  const [type, setType] = useState('INFO');
  const [status, setStatus] = useState('ACTIVE');

  const isEditing = template !== null;

  // Khi modal mở, khởi tạo giá trị
  const handleAfterOpenChange = (visible) => {
    if (visible) {
      if (template) {
        setTitle(template.title || '');
        setMessageTemplate(template.messageTemplate || '');
        setType(template.type || 'INFO');
        setStatus(template.status || 'ACTIVE');
      } else {
        setTitle('');
        setMessageTemplate('');
        setType('INFO');
        setStatus('ACTIVE');
      }
    }
  };

  const handleSubmit = () => {
    // Validate
    if (!title.trim()) {
      message.error('Vui lòng nhập tiêu đề');
      return;
    }
    if (!messageTemplate.trim()) {
      message.error('Vui lòng nhập nội dung');
      return;
    }

    const data = {
      title: title.trim(),
      messageTemplate: messageTemplate.trim(),
      type,
      status,
    };

    // Backend sẽ tự động generate templateCode (NOT001, NOT002, ...)

    onSubmit(data);
  };

  const handleCancel = () => {
    setTitle('');
    setMessageTemplate('');
    setType('INFO');
    setStatus('ACTIVE');
    onClose();
  };

  return (
    <Modal
      open={open}
      onCancel={handleCancel}
      footer={null}
      closable={false}
      centered
      width={510}
      afterOpenChange={handleAfterOpenChange}
      styles={{
        content: {
          padding: 0,
          borderRadius: 10,
          overflow: 'hidden',
        },
        body: {
          padding: 0,
        },
      }}
    >
      <div style={{ padding: 24, position: 'relative' }}>
        {/* Close Button */}
        <div
          onClick={handleCancel}
          style={{
            position: 'absolute',
            right: 16,
            top: 16,
            width: 16,
            height: 16,
            cursor: 'pointer',
            opacity: 0.7,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
          }}
        >
          <CloseOutlined style={{ fontSize: 14, color: '#0A0A0A' }} />
        </div>

        {/* Header */}
        <div style={{ marginBottom: 16 }}>
          <h2
            style={{
              fontSize: 18,
              fontWeight: 700,
              color: '#0A0A0A',
              margin: 0,
              marginBottom: 8,
              fontFamily: 'Arimo, sans-serif',
            }}
          >
            {isEditing ? 'Chỉnh sửa Notification Template' : 'Thêm Notification Template'}
          </h2>
          <p
            style={{
              fontSize: 14,
              color: '#717182',
              margin: 0,
              fontFamily: 'Arimo, sans-serif',
            }}
          >
            {isEditing ? 'Cập nhật template thông báo' : 'Tạo template thông báo mới'}
          </p>
        </div>

        {/* Form Content */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          {/* Tiêu đề */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
            <label
              style={{
                fontSize: 14,
                color: '#0A0A0A',
                fontFamily: 'Arimo, sans-serif',
              }}
            >
              Tiêu đề
            </label>
            <Input
              placeholder="VD: Chi tiêu vượt ngân sách"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              style={{
                height: 36,
                borderRadius: 8,
                background: '#F3F3F5',
                border: '1px solid transparent',
                fontSize: 14,
                fontFamily: 'Arimo, sans-serif',
              }}
            />
          </div>

          {/* Nội dung */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
            <label
              style={{
                fontSize: 14,
                color: '#0A0A0A',
                fontFamily: 'Arimo, sans-serif',
              }}
            >
              Nội dung
            </label>
            <TextArea
              placeholder="Sử dụng {amount}, {category}, {percent} làm placeholders"
              value={messageTemplate}
              onChange={(e) => setMessageTemplate(e.target.value)}
              rows={3}
              style={{
                borderRadius: 8,
                background: '#F3F3F5',
                border: '1px solid transparent',
                fontSize: 14,
                fontFamily: 'Arimo, sans-serif',
                resize: 'none',
              }}
            />
          </div>

          {/* Loại thông báo */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
            <label
              style={{
                fontSize: 14,
                color: '#0A0A0A',
                fontFamily: 'Arimo, sans-serif',
              }}
            >
              Loại thông báo
            </label>
            <Select
              value={type}
              onChange={setType}
              options={typeOptions}
              style={{ width: '100%', height: 39 }}
              classNames={{ popup: { root: 'custom-select-popup' } }}
            />
          </div>

          {/* Trạng thái */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
            <label
              style={{
                fontSize: 14,
                color: '#0A0A0A',
                fontFamily: 'Arimo, sans-serif',
              }}
            >
              Trạng thái
            </label>
            <Select
              value={status}
              onChange={setStatus}
              options={statusOptions}
              style={{ width: '100%', height: 39 }}
              classNames={{ popup: { root: 'custom-select-popup' } }}
            />
          </div>
        </div>

        {/* Footer Buttons */}
        <div
          style={{
            display: 'flex',
            justifyContent: 'flex-end',
            gap: 8,
            marginTop: 24,
          }}
        >
          <button
            onClick={handleCancel}
            style={{
              height: 36,
              padding: '0 17px',
              borderRadius: 8,
              border: '1px solid rgba(0,0,0,0.1)',
              background: '#FFFFFF',
              color: '#0A0A0A',
              fontSize: 14,
              fontFamily: 'Arimo, sans-serif',
              cursor: 'pointer',
            }}
          >
            Hủy
          </button>
          <button
            onClick={handleSubmit}
            disabled={loading}
            style={{
              height: 36,
              padding: '0 16px',
              borderRadius: 8,
              border: 'none',
              background: '#155DFC',
              color: '#FFFFFF',
              fontSize: 14,
              fontFamily: 'Arimo, sans-serif',
              cursor: loading ? 'not-allowed' : 'pointer',
              opacity: loading ? 0.7 : 1,
            }}
          >
            {loading
              ? 'Đang xử lý...'
              : isEditing
              ? 'Cập nhật Template'
              : 'Thêm Template'}
          </button>
        </div>
      </div>
    </Modal>
  );
};

export default NotificationTemplateModal;
