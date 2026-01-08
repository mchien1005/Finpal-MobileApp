import React, { useState, useEffect } from 'react';
import { Typography, Row, Col, Card, Button, Table, Tag, Modal, Form, Input, Select, message, Spin } from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
} from '@ant-design/icons';
import AdminSidebar from '../../components/admin/AdminSidebar';
import { useSidebar } from '../../contexts/SidebarContext';
import SuccessModal from '../../components/common/SuccessModal';
import ConfirmModal from '../../components/common/ConfirmModal';
import * as categoryService from '../../services/categoryService';

const { Title, Text } = Typography;

const CategoryManagementPage = () => {
  const { collapsed } = useSidebar();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isEditCategoryModalOpen, setIsEditCategoryModalOpen] = useState(false);
  const [isSuccessModalOpen, setIsSuccessModalOpen] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');
  const [editingCategory, setEditingCategory] = useState(null);
  const [form] = Form.useForm();
  const [editCategoryForm] = Form.useForm();
  const [selectedColor, setSelectedColor] = useState('#1E5EFF');
  const [editCategoryColor, setEditCategoryColor] = useState('#1E5EFF');
  const [isDeleteConfirmOpen, setIsDeleteConfirmOpen] = useState(false);
  const [deletingCategory, setDeletingCategory] = useState(null);
  
  // Data states
  const [categories, setCategories] = useState([]);
  const [categoryStats, setCategoryStats] = useState(null);
  const [loading, setLoading] = useState(false);
  const [tableLoading, setTableLoading] = useState(false);

  // Load data on mount
  useEffect(() => {
    loadCategories();
  }, []);
  
  // Update stats when data changes
  useEffect(() => {
    loadStats();
  }, [categories]);

  // Load categories
  const loadCategories = async () => {
    try {
      setTableLoading(true);
      const data = await categoryService.getCategories();
      setCategories(data);
    } catch (error) {
      message.error('Không thể tải danh sách danh mục');
      console.error('Load categories error:', error);
    } finally {
      setTableLoading(false);
    }
  };

  // Load statistics
  const loadStats = async () => {
    try {
      // Tính toán stats cơ bản từ data có sẵn
      const totalCategories = categories.length;

      // Gọi API AI metrics để lấy accuracy
      const metricsUrl = 'http://175.41.150.228:8000/api/admin/ai/models/Category%20Classification/metrics';
      let metrics = null;

      try {
        const res = await fetch(metricsUrl, { method: 'GET' });
        if (res.ok) {
          metrics = await res.json();
        } else {
          console.warn('AI metrics fetch failed with status', res.status);
        }
      } catch (err) {
        console.warn('Error fetching AI metrics:', err);
      }

      setCategoryStats({
        total: totalCategories.toString(),
        classifications: metrics?.total_predictions?.toString() || '0',
        // Use API accuracy if available, otherwise fallback to 0%
        accuracy: metrics?.accuracy != null ? `${metrics.accuracy}%` : '0%',
        // Use confidence or predictions_today as a short description fallback
        accuracyChange: metrics?.confidence != null
          ? `${metrics.confidence}%`
          : (metrics?.predictions_today != null ? `${metrics.predictions_today} hôm nay` : 'Đang cập nhật'),
      });
    } catch (error) {
      console.error('Load stats error:', error);
    }
  };

  // Reload keywords when filters change
  // useEffect(() => {
  //   loadKeywords();
  // }, [keywordSearchText, selectedCategoryFilter]);

  // Stats data
  const statsCards = [
    {
      title: 'Tổng danh mục',
      value: categoryStats?.total || '0',
      color: '#1E40AF', // deep blue
    },
    {
      title: 'Độ chính xác',
      value: categoryStats?.accuracy || '0%',
      descColor: '#047857', // green for description
      color: '#10B981', // green for value
    },
  ];

  // Helper: convert hex color to rgba string with given alpha
  const hexToRgba = (hex, alpha = 0.08) => {
    if (!hex) return `rgba(0,0,0,${alpha})`;
    let h = hex.replace('#', '');
    if (h.length === 3) h = h.split('').map((c) => c + c).join('');
    const int = parseInt(h, 16);
    const r = (int >> 16) & 255;
    const g = (int >> 8) & 255;
    const b = int & 255;
    return `rgba(${r}, ${g}, ${b}, ${alpha})`;
  };

  // Predefined colors
  const colorOptions = [
    '#1E5EFF', '#10B981', '#F59E0B', '#EF4444', 
    '#8B5CF6', '#EC4899', '#06B6D4', '#84CC16'
  ];

  const handleAddCategory = () => {
    setIsModalOpen(true);
  };

  const handleCancel = () => {
    setIsModalOpen(false);
    form.resetFields();
    setSelectedColor('#1E5EFF');
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      setLoading(true);
      
      await categoryService.createCategory(values);
      
      setIsModalOpen(false);
      form.resetFields();
      setSelectedColor('#1E5EFF');
      setSuccessMessage('Thêm danh mục thành công!');
      setIsSuccessModalOpen(true);
      
      // Reload data
      await loadCategories();
      await loadStats();
    } catch (error) {
      if (error.response) {
        message.error(error.response.data.message || 'Không thể thêm danh mục');
      } else {
        message.error('Đã xảy ra lỗi khi thêm danh mục');
      }
      console.error('Create category error:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleEditCategory = (record) => {
    setEditingCategory(record);
    const colorMatch = record.iconBg ? record.iconBg.match(/rgba\((\d+), (\d+), (\d+)/) : null;
    let hexColor = '#1E5EFF';
    if (colorMatch) {
      const r = parseInt(colorMatch[1]);
      const g = parseInt(colorMatch[2]);
      const b = parseInt(colorMatch[3]);
      hexColor = `#${((1 << 24) + (r << 16) + (g << 8) + b).toString(16).slice(1)}`;
    }
    setEditCategoryColor(hexColor);
    editCategoryForm.setFieldsValue({
      name: record.name,
      type: record.type,
      icon: record.icon,
      color: hexColor,
    });
    setIsEditCategoryModalOpen(true);
  };

  const handleUpdateCategory = async () => {
    try {
      const values = await editCategoryForm.validateFields();
      setLoading(true);
      
      await categoryService.updateCategory(editingCategory.id, values);
      
      setIsEditCategoryModalOpen(false);
      editCategoryForm.resetFields();
      setEditCategoryColor('#1E5EFF');
      setSuccessMessage('Cập nhật danh mục thành công!');
      setIsSuccessModalOpen(true);
      
      // Reload data
      await loadCategories();
      await loadStats();
    } catch (error) {
      if (error.response) {
        message.error(error.response.data.message || 'Không thể cập nhật danh mục');
      } else {
        message.error('Đã xảy ra lỗi khi cập nhật danh mục');
      }
      console.error('Update category error:', error);
    } finally {
      setLoading(false);
    }
  };

  // Handle delete category
  const handleDeleteCategory = async () => {
    try {
      setLoading(true);
      await categoryService.deleteCategory(deletingCategory.id);
      setIsDeleteConfirmOpen(false);
      setDeletingCategory(null);
      setSuccessMessage('Xóa danh mục thành công!');
      setIsSuccessModalOpen(true);
      await loadCategories();
      await loadStats();
    } catch (error) {
      message.error('Không thể xóa danh mục');
      console.error('Delete category error:', error);
    } finally {
      setLoading(false);
    }
  };

  // Table columns
  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 100,
      render: (text) => <Text style={{ color: '#155dfc' }}>{text}</Text>,
    },
    {
      title: 'Biểu tượng',
      dataIndex: 'icon',
      key: 'icon',
      width: 100,
      align: 'center',
      render: (icon) => (
        <span style={{ fontSize: 14 }}>
          {icon || '📁'}
        </span>
      ),
    },
    {
      title: 'Tên danh mục',
      dataIndex: 'name',
      key: 'name',
      width: 150,
      render: (text) => <Text>{text}</Text>,
    },
    {
      title: 'Mô tả',
      dataIndex: 'description',
      key: 'description',
      width: 300,
      render: (text) => (
        <Text type="secondary" style={{ fontSize: 14 }}>
          {text || '-'}
        </Text>
      ),
    },
    {
      title: 'Loại',
      dataIndex: 'type',
      key: 'type',
      width: 100,
      render: (type) => (
        <Tag 
          color={type === 'EXPENSE' ? 'red' : 'green'} 
          style={{ borderRadius: 8 }}
        >
          {type === 'EXPENSE' ? 'Chi tiêu' : type === 'INCOME' ? 'Thu nhập' : type}
        </Tag>
      ),
    },
    {
      title: 'Thao tác',
      key: 'action',
      width: 120,
      align: 'right',
      render: (_, record) => (
        <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
          <Button
            type="text"
            icon={<EditOutlined />}
            onClick={() => handleEditCategory(record)}
            style={{
              color: '#f59e0b',
            }}
          />
          <Button
            type="text"
            icon={<DeleteOutlined />}
            onClick={() => {
              setDeletingCategory(record);
              setIsDeleteConfirmOpen(true);
            }}
            style={{
              color: '#e7000b',
            }}
          />
        </div>
      ),
    },
  ];

  return (
    <div style={{ 
      display: 'flex', 
      minHeight: '100vh',
      background: 'linear-gradient(142deg, rgb(249, 250, 251) 0%, rgba(239, 246, 255, 0.3) 100%)',
    }}>
      <AdminSidebar />

      {/* Main Content */}
      <div
        style={{
          marginLeft: collapsed ? 80 : 280,
          flex: 1,
          transition: 'margin-left 0.3s',
          padding: 32,
        }}
      >
        {/* Header */}
        <div style={{ 
          display: 'flex', 
          justifyContent: 'space-between', 
          alignItems: 'center',
          marginBottom: 24,
        }}>
          <div>
            <Title level={2} style={{ margin: 0, marginBottom: 4, fontSize: 24 }}>
              Quản lý danh mục
            </Title>
            <Text type="secondary" style={{ fontSize: 16 }}>
              Quản lý danh mục chi tiêu
            </Text>
          </div>
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={handleAddCategory}
            style={{
              height: 36,
              borderRadius: 8,
              background: '#155dfc',
              borderColor: '#155dfc',
            }}
          >
            Thêm danh mục
          </Button>
        </div>

        {/* Stats Cards */}
        <Row gutter={24} style={{ marginBottom: 24 }}>
          {statsCards.map((stat, index) => (
            <Col xs={24} sm={12} lg={12} key={index}>
              <Card
                style={{
                  borderRadius: 14,
                  border: '0.8px solid rgba(0,0,0,0.06)',
                  background: hexToRgba(stat.color, 0.08),
                  height: '100%',
                }}
                styles={{ body: { padding: '24.8px' } }}
              >
                <Text type="secondary" style={{ fontSize: 14, display: 'block', marginBottom: 32 }}>
                  {stat.title}
                </Text>
                <div style={{ fontSize: 16, fontWeight: 600, color: stat.color || '#101828', marginBottom: 32 }}>
                  {stat.value}
                </div>
                <Text 
                  style={{ 
                    fontSize: 14, 
                    color: stat.descColor || '#6a7282',
                    display: 'block',
                  }}
                >
                  {stat.description}
                </Text>
              </Card>
            </Col>
          ))}
        </Row>

        {/* Categories Table */}
        <Card
          style={{
            borderRadius: 14,
            border: '0.8px solid rgba(0,0,0,0.1)',
            background: 'white',
          }}
          styles={{ body: { padding: 0 } }}
        >
          <div style={{ 
            padding: '24px 24px 0.8px 24px',
            borderBottom: '0.8px solid rgba(0,0,0,0.1)',
          }}>
            <Title level={4} style={{ margin: 0, fontSize: 18, marginBottom: 24 }}>
              Danh sách danh mục
            </Title>
          </div>
          <Table
            columns={columns}
            dataSource={categories}
            rowKey={(record) => record.id || record.key}
            loading={tableLoading}
            pagination={false}
            style={{ 
              fontFamily: 'Arimo, sans-serif',
            }}
            rowClassName={() => 'table-row'}
          />
        </Card>
      </div>

      {/* Add Category Modal */}
      <Modal
        title={
          <div>
            <div style={{ fontSize: 20, fontWeight: 600, color: '#101828', marginBottom: 4 }}>
              Thêm danh mục mới
            </div>
            <Text type="secondary" style={{ fontSize: 14 }}>
              Tạo danh mục chi tiêu mới cho hệ thống
            </Text>
          </div>
        }
        open={isModalOpen}
        onCancel={handleCancel}
        footer={null}
        width={560}
        centered
        styles={{
          header: { paddingBottom: 16, borderBottom: '1px solid #f0f0f0' },
          body: { paddingTop: 24 },
        }}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
        >
          <Form.Item
            label="Tên danh mục"
            name="name"
            rules={[{ required: true, message: 'Vui lòng nhập tên danh mục' }]}
          >
            <Input
              placeholder="VD: Ăn uống, Di chuyển..."
              style={{
                height: 40,
                borderRadius: 8,
                background: '#F3F4F6',
                border: '1px solid transparent',
              }}
            />
          </Form.Item>

          <Form.Item
            label="Loại danh mục"
            name="type"
            rules={[{ required: true, message: 'Vui lòng chọn loại danh mục' }]}
            initialValue="EXPENSE"
          >
            <Select
              placeholder="Chọn loại"
              style={{ height: 40 }}
              options={[
                { value: 'EXPENSE', label: 'Chi tiêu' },
                { value: 'INCOME', label: 'Thu nhập' },
              ]}
            />
          </Form.Item>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                label="Icon"
                name="icon"
                rules={[{ required: true, message: 'Vui lòng nhập icon' }]}
              >
                <Input
                  placeholder="VD: 🍽️, 🚗, 🛍️..."
                  style={{
                    height: 40,
                    borderRadius: 8,
                    background: '#F3F4F6',
                    border: '1px solid transparent',
                  }}
                />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                label="Màu sắc"
                name="color"
                initialValue={selectedColor}
              >
                <input
                  type="color"
                  value={selectedColor}
                  onChange={(e) => {
                    setSelectedColor(e.target.value);
                    form.setFieldValue('color', e.target.value);
                  }}
                  style={{
                    width: '100%',
                    height: 40,
                    borderRadius: 8,
                    border: '1px solid #E5E7EB',
                    cursor: 'pointer',
                  }}
                />
              </Form.Item>
              
            </Col>
          </Row>
          <Form.Item
            label="Mô tả"
            name="description"
            rules={[{ required: true, message: 'Vui lòng nhập mô tả' }]}
          >
            <Input.TextArea
              placeholder="VD: Chi phí ăn uống, nhà hàng, quán cà phê..."
              rows={3}
              style={{
                borderRadius: 8,
                background: '#F3F4F6',
                border: '1px solid transparent',
              }}
            />
          </Form.Item>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 12, marginTop: 24 }}>
            <Button
              onClick={handleCancel}
              disabled={loading}
              style={{
                height: 40,
                borderRadius: 8,
                paddingLeft: 24,
                paddingRight: 24,
              }}
            >
              Hủy
            </Button>
            <Button
              type="primary"
              htmlType="submit"
              loading={loading}
              style={{
                height: 40,
                borderRadius: 8,
                background: '#155dfc',
                borderColor: '#155dfc',
                paddingLeft: 24,
                paddingRight: 24,
              }}
            >
              Thêm danh mục
            </Button>
          </div>
        </Form>
      </Modal>

      {/* Edit Category Modal */}
      <Modal
        title={
          <div>
            <div style={{ fontSize: 20, fontWeight: 600, color: '#101828', marginBottom: 4 }}>
              Chỉnh sửa danh mục
            </div>
            <Text type="secondary" style={{ fontSize: 14 }}>
              Cập nhật thông tin danh mục chi tiêu
            </Text>
          </div>
        }
        open={isEditCategoryModalOpen}
        onCancel={() => {
          setIsEditCategoryModalOpen(false);
          editCategoryForm.resetFields();
          setEditCategoryColor('#1E5EFF');
        }}
        footer={null}
        width={560}
        centered
        styles={{
          header: { paddingBottom: 16, borderBottom: '1px solid #f0f0f0' },
          body: { paddingTop: 24 },
        }}
      >
        <Form
          form={editCategoryForm}
          layout="vertical"
          onFinish={handleUpdateCategory}
        >
          <Form.Item
            label="Tên danh mục"
            name="name"
            rules={[{ required: true, message: 'Vui lòng nhập tên danh mục' }]}
          >
            <Input
              placeholder="VD: Ăn uống, Di chuyển..."
              style={{
                height: 40,
                borderRadius: 8,
                background: '#F3F4F6',
                border: '1px solid transparent',
              }}
            />
          </Form.Item>

          <Form.Item
            label="Mô tả"
            name="description"
            rules={[{ required: true, message: 'Vui lòng nhập mô tả' }]}
          >
            <Input.TextArea
              placeholder="VD: Chi phí ăn uống, nhà hàng, quán cà phê..."
              rows={3}
              style={{
                borderRadius: 8,
                background: '#F3F4F6',
                border: '1px solid transparent',
              }}
            />
          </Form.Item>

          <Form.Item
            label="Loại danh mục"
            name="type"
            rules={[{ required: true, message: 'Vui lòng chọn loại danh mục' }]}
          >
            <Select
              placeholder="Chọn loại"
              style={{ height: 40 }}
              options={[
                { value: 'EXPENSE', label: 'Chi tiêu' },
                { value: 'INCOME', label: 'Thu nhập' },
              ]}
            />
          </Form.Item>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                label="Icon"
                name="icon"
                rules={[{ required: true, message: 'Vui lòng nhập icon' }]}
              >
                <Input
                  placeholder="VD: 🍽️, 🚗, 🛍️..."
                  style={{
                    height: 40,
                    borderRadius: 8,
                    background: '#F3F4F6',
                    border: '1px solid transparent',
                  }}
                />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                label="Màu sắc"
                name="color"
              >
                <input
                  type="color"
                  value={editCategoryColor}
                  onChange={(e) => {
                    setEditCategoryColor(e.target.value);
                    editCategoryForm.setFieldValue('color', e.target.value);
                  }}
                  style={{
                    width: '100%',
                    height: 40,
                    borderRadius: 8,
                    border: '1px solid #E5E7EB',
                    cursor: 'pointer',
                  }}
                />
              </Form.Item>
            </Col>
          </Row>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 12, marginTop: 24 }}>
            <Button
              onClick={() => {
                setIsEditCategoryModalOpen(false);
                editCategoryForm.resetFields();
                setEditCategoryColor('#1E5EFF');
              }}
              disabled={loading}
              style={{
                height: 40,
                borderRadius: 8,
                paddingLeft: 24,
                paddingRight: 24,
              }}
            >
              Hủy
            </Button>
            <Button
              type="primary"
              htmlType="submit"
              loading={loading}
              style={{
                height: 40,
                borderRadius: 8,
                background: '#155dfc',
                borderColor: '#155dfc',
                paddingLeft: 24,
                paddingRight: 24,
              }}
            >
              Cập nhật danh mục
            </Button>
          </div>
        </Form>
      </Modal>

      {/* Success Modal */}
      <SuccessModal
        open={isSuccessModalOpen}
        onClose={() => setIsSuccessModalOpen(false)}
        message={successMessage}
        buttonText="Đồng ý"
      />

      {/* Delete Confirmation Modal */}
      <ConfirmModal
        open={isDeleteConfirmOpen}
        onConfirm={handleDeleteCategory}
        onCancel={() => {
          setIsDeleteConfirmOpen(false);
          setDeletingCategory(null);
        }}
        title="Xác nhận xóa"
        content={`Bạn có chắc chắn muốn xóa danh mục "${deletingCategory?.name || ''}"?\nHành động này không thể hoàn tác.`}
        confirmText="Xóa"
        cancelText="Hủy"
        danger={true}
      />

      {/* Custom Styles */}
      <style>
        {`
          .ant-table-thead > tr > th {
            background: transparent !important;
            color: #0a0a0a !important;
            font-size: 14px !important;
            font-weight: 400 !important;
            border-bottom: 0.8px solid rgba(0,0,0,0.1) !important;
            padding: 8px !important;
          }
          
          .ant-table-tbody > tr > td {
            border-bottom: 0.8px solid rgba(0,0,0,0.1) !important;
            padding: 17.2px 8px !important;
            font-size: 14px !important;
          }
          
          .ant-table-tbody > tr:hover > td {
            background: #fafafa !important;
          }
        `}
      </style>
    </div>
  );
};

export default CategoryManagementPage;