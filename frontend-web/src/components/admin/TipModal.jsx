import React, { useState } from 'react';
import { Modal, Input, Select, message } from 'antd';
import { CloseOutlined } from '@ant-design/icons';

const { TextArea } = Input;

const categoryOptions = [
  { value: 'SAVING', label: 'Tiết kiệm' },
  { value: 'BUDGET', label: 'Ngân sách' },
  { value: 'INVESTMENT', label: 'Đầu tư' },
  { value: 'SPENDING', label: 'Chi tiêu' },
  { value: 'MANAGEMENT', label: 'Quản lý' },
];

const statusOptions = [
  { value: 'ACTIVE', label: 'Active' },
  { value: 'INACTIVE', label: 'Inactive' },
];

const TipModal = ({
  open,
  onClose,
  onSubmit,
  tip = null, // null = thêm mới, object = sửa
  loading = false,
}) => {
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [category, setCategory] = useState('SAVING');
  const [status, setStatus] = useState('ACTIVE');

  const isEditing = tip !== null;

  // Khi modal mở, khởi tạo giá trị
  const handleAfterOpenChange = (visible) => {
    if (visible) {
      if (tip) {
        setTitle(tip.title || '');
        setContent(tip.content || '');
        setCategory(tip.category || 'SAVING');
        setStatus(tip.status || 'ACTIVE');
      } else {
        setTitle('');
        setContent('');
        setCategory('SAVING');
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
    if (!content.trim()) {
      message.error('Vui lòng nhập nội dung');
      return;
    }

    const data = {
      title: title.trim(),
      content: content.trim(),
      category,
      status,
    };

    onSubmit(data);
  };

  const handleCancel = () => {
    setTitle('');
    setContent('');
    setCategory('SAVING');
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
            {isEditing ? 'Chỉnh sửa Tip' : 'Thêm Tip mới'}
          </h2>
          <p
            style={{
              fontSize: 14,
              color: '#717182',
              margin: 0,
              fontFamily: 'Arimo, sans-serif',
            }}
          >
            {isEditing ? 'Cập nhật tip tiết kiệm' : 'Tạo tip tiết kiệm mới cho người dùng'}
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
              placeholder="VD: Quy tắc 50/30/20"
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
              placeholder="Nhập nội dung chi tiết của tip..."
              value={content}
              onChange={(e) => setContent(e.target.value)}
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

          {/* Danh mục */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
            <label
              style={{
                fontSize: 14,
                color: '#0A0A0A',
                fontFamily: 'Arimo, sans-serif',
              }}
            >
              Danh mục
            </label>
            <Select
              value={category}
              onChange={setCategory}
              options={categoryOptions}
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
              ? 'Cập nhật Tip'
              : 'Thêm Tip'}
          </button>
        </div>
      </div>
    </Modal>
  );
};

export default TipModal;
