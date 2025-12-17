import React, { useState } from 'react';
import { 
  Card, 
  Row, 
  Col, 
  Table, 
  Tag, 
  Button, 
  Typography, 
  Progress,
  Modal,
  Form,
  Input 
} from 'antd';
import { 
  PlusOutlined, 
  PlayCircleOutlined, 
  EditOutlined, 
  DeleteOutlined,
  WarningOutlined 
} from '@ant-design/icons';
import AdminSidebar from '../../components/admin/AdminSidebar';
import { useSidebar } from '../../contexts/SidebarContext';
import SuccessModal from '../../components/common/SuccessModal';
import ConfirmModal from '../../components/common/ConfirmModal';

const { Title, Text } = Typography;
const { TextArea } = Input;

const BankAndSmsparserPage = () => {
  const { collapsed } = useSidebar();
  const [isAddBankModalOpen, setIsAddBankModalOpen] = useState(false);
  const [isEditBankModalOpen, setIsEditBankModalOpen] = useState(false);
  const [editingBank, setEditingBank] = useState(null);
  const [isSuccessModalOpen, setIsSuccessModalOpen] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');
  const [isTestParserModalOpen, setIsTestParserModalOpen] = useState(false);
  const [testSmsValue, setTestSmsValue] = useState('');
  const [testResult, setTestResult] = useState(null);
  const [isDeleteConfirmOpen, setIsDeleteConfirmOpen] = useState(false);
  const [deletingBank, setDeletingBank] = useState(null);
  const [form] = Form.useForm();
  const [editForm] = Form.useForm();
  const [testForm] = Form.useForm();

  // Handler to open edit modal
  const handleEditBank = (bank) => {
    setEditingBank(bank);
    editForm.setFieldsValue({
      code: bank.code,
      name: bank.name,
      template: bank.template,
      regex: bank.regex || '',
    });
    setIsEditBankModalOpen(true);
  };

  // Handler to update bank
  const handleUpdateBank = (values) => {
    console.log('Update bank:', editingBank.key, values);
    // TODO: Call API to update bank
    setIsEditBankModalOpen(false);
    editForm.resetFields();
    setEditingBank(null);
    setSuccessMessage('Cập nhật ngân hàng thành công!');
    setIsSuccessModalOpen(true);
  };

  // Handler to test SMS parser
  const handleTestParser = (values) => {
    const smsText = values.smsText;
    console.log('Testing SMS:', smsText);
    
    // Mock parsing logic - in production this would call API
    // Example: "TK 1234567890 GD: -500,000d 24/03 GRAB. SD: 15,234,567d"
    const vcbPattern = /TK\s+(\d+)\s+GD:\s+-([\d,]+)d?\s+(\d{2}\/\d{2})\s+(.+?)\s*\.\s*SD:\s+([\d,]+)d?/;
    const match = smsText.match(vcbPattern);
    
    if (match) {
      setTestResult({
        success: true,
        bank: 'VCB',
        data: {
          account: match[1],
          amount: match[2],
          date: match[3] + '/2024',
          description: match[4],
          balance: match[5],
        },
      });
    } else {
      setTestResult({
        success: false,
        error: 'Không thể parse SMS - format không khớp với template nào',
      });
    }
  };

  // Handler to update pattern from failed case
  const handleUpdatePattern = (failCase) => {
    // Find the bank from bankTemplatesData based on the bank code
    const bank = bankTemplatesData.find(b => b.code === failCase.bank);
    if (bank) {
      handleEditBank(bank);
    }
  };

  // Handler to open delete confirmation
  const handleDeleteBank = (bank) => {
    setDeletingBank(bank);
    setIsDeleteConfirmOpen(true);
  };

  // Handler to confirm delete
  const handleConfirmDelete = () => {
    console.log('Delete bank:', deletingBank);
    // TODO: Call API to delete bank
    setIsDeleteConfirmOpen(false);
    setDeletingBank(null);
    setSuccessMessage('Xóa ngân hàng thành công!');
    setIsSuccessModalOpen(true);
  };

  // Stats data
  const statsCards = [
    {
      title: 'Tổng ngân hàng',
      value: '15',
      description: 'Đã cấu hình',
      color: '#4F46E5',
    },
    {
      title: 'Tỷ lệ thành công',
      value: '96.8%',
      description: '+0.5% tuần này',
      descColor: '#00a63e',
      color: '#10B981',
    },
    {
      title: 'SMS xử lý hôm nay',
      value: '5,482',
      description: '128 thất bại',
      color: '#F59E0B',
    },
    {
      title: 'Cần xem xét',
      value: '23',
      description: 'Failed cases',
      descColor: '#e7000b',
      color: '#EF4444',
    },
  ];

  // Parser statistics data
  const parserData = [
    {
      key: '1',
      icon: '🏦',
      bank: 'VCB',
      totalSMS: '1,234',
      success: '1,216',
      failed: 18,
      successRate: 98.5,
      status: 'Excellent',
      statusColor: '#dcfce7',
      statusTextColor: '#008236',
      progressColor: '#00c950',
    },
    {
      key: '2',
      icon: '🏦',
      bank: 'TCB',
      totalSMS: '1,089',
      success: '1,065',
      failed: 24,
      successRate: 97.8,
      status: 'Good',
      statusColor: '#fef9c2',
      statusTextColor: '#a65f00',
      progressColor: '#f0b100',
    },
    {
      key: '3',
      icon: '🏦',
      bank: 'ACB',
      totalSMS: '876',
      success: '843',
      failed: 33,
      successRate: 96.2,
      status: 'Good',
      statusColor: '#fef9c2',
      statusTextColor: '#a65f00',
      progressColor: '#f0b100',
    },
    {
      key: '4',
      icon: '🏦',
      bank: 'VTB',
      totalSMS: '654',
      success: '624',
      failed: 30,
      successRate: 95.4,
      status: 'Good',
      statusColor: '#fef9c2',
      statusTextColor: '#a65f00',
      progressColor: '#f0b100',
    },
    {
      key: '5',
      icon: '🏦',
      bank: 'MBB',
      totalSMS: '543',
      success: '511',
      failed: 32,
      successRate: 94.1,
      status: 'Needs Review',
      statusColor: '#ffe2e2',
      statusTextColor: '#c10007',
      progressColor: '#fb2c36',
    },
  ];

  // Parser columns
  const parserColumns = [
    {
      title: 'Ngân hàng',
      dataIndex: 'bank',
      key: 'bank',
      width: 150,
      render: (text, record) => (
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <div
            style={{
              width: 32,
              height: 32,
              borderRadius: 10,
              background: '#dbeafe',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: 18,
            }}
          >
            {record.icon}
          </div>
          <Text strong>{text}</Text>
        </div>
      ),
    },
    {
      title: 'Tổng SMS',
      dataIndex: 'totalSMS',
      key: 'totalSMS',
      width: 130,
    },
    {
      title: 'Thành công',
      dataIndex: 'success',
      key: 'success',
      width: 130,
      render: (text) => <Text style={{ color: '#00a63e' }}>{text}</Text>,
    },
    {
      title: 'Thất bại',
      dataIndex: 'failed',
      key: 'failed',
      width: 100,
      render: (text) => <Text style={{ color: '#e7000b' }}>{text}</Text>,
    },
    {
      title: 'Tỷ lệ thành công',
      dataIndex: 'successRate',
      key: 'successRate',
      width: 200,
      render: (rate, record) => (
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <Progress
            percent={rate}
            strokeColor={record.progressColor}
            showInfo={false}
            style={{ width: 100 }}
          />
          <Text>{rate}%</Text>
        </div>
      ),
    },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      key: 'status',
      width: 150,
      render: (text, record) => (
        <Tag
          color={record.statusColor}
          style={{
            border: 'none',
            borderRadius: 8,
            color: record.statusTextColor,
          }}
        >
          {text}
        </Tag>
      ),
    },
  ];

  // Bank templates data
  const bankTemplatesData = [
    {
      key: '1',
      icon: '🏦',
      name: 'Vietcombank',
      code: 'VCB',
      template: 'TK {account} GD: -{amount}d {date} {description}. SD: {balance}d',
      users: '3,245',
      successRate: 98.5,
      failedToday: 12,
      successRateColor: '#dcfce7',
      successRateTextColor: '#008236',
    },
    {
      key: '2',
      icon: '🏦',
      name: 'Techcombank',
      code: 'TCB',
      template: 'Tai khoan {account} -{amount}VND {date} {description}',
      users: '2,891',
      successRate: 97.8,
      failedToday: 18,
      successRateColor: '#fef9c2',
      successRateTextColor: '#a65f00',
    },
    {
      key: '3',
      icon: '🏦',
      name: 'ACB Bank',
      code: 'ACB',
      template: '{account} GD -{amount} {date} tai {description}',
      users: '2,134',
      successRate: 96.2,
      failedToday: 34,
      failedColor: '#e7000b',
      successRateColor: '#fef9c2',
      successRateTextColor: '#a65f00',
    },
    {
      key: '4',
      icon: '🏦',
      name: 'Vietinbank',
      code: 'VTB',
      template: 'TK {account} tru {amount}VND {date} {description}',
      users: '1,678',
      successRate: 95.4,
      failedToday: 28,
      successRateColor: '#fef9c2',
      successRateTextColor: '#a65f00',
    },
    {
      key: '5',
      icon: '🏦',
      name: 'MB Bank',
      code: 'MBB',
      template: 'GD {account}: -{amount}d ngay {date}. ND: {description}',
      users: '1,456',
      successRate: 94.1,
      failedToday: 45,
      failedColor: '#e7000b',
      successRateColor: '#ffe2e2',
      successRateTextColor: '#c10007',
    },
  ];

  // Bank templates columns
  const bankTemplatesColumns = [
    {
      title: 'Ngân hàng',
      dataIndex: 'name',
      key: 'name',
      width: 150,
      render: (text, record) => (
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <div
            style={{
              fontSize: 24,
            }}
          >
            {record.icon}
          </div>
          <div>
            <div>
              <Text strong>{text}</Text>
            </div>
            <div>
              <Text type="secondary" style={{ fontSize: 14 }}>
                {record.code}
              </Text>
            </div>
          </div>
        </div>
      ),
    },
    {
      title: 'SMS Template',
      dataIndex: 'template',
      key: 'template',
      width: 450,
      render: (text) => (
        <div
          style={{
            background: '#f3f4f6',
            padding: '4px 8px',
            borderRadius: 4,
            fontFamily: 'Cousine, monospace',
            fontSize: 12,
          }}
        >
          {text}
        </div>
      ),
    },
    {
      title: 'Người dùng',
      dataIndex: 'users',
      key: 'users',
      width: 100,
    },
    {
      title: 'Success Rate',
      dataIndex: 'successRate',
      key: 'successRate',
      width: 100,
      render: (rate, record) => (
        <Tag
          color={record.successRateColor}
          style={{
            border: 'none',
            borderRadius: 8,
            color: record.successRateTextColor,
          }}
        >
          {rate}%
        </Tag>
      ),
    },
    {
      title: 'Failed Today',
      dataIndex: 'failedToday',
      key: 'failedToday',
      width: 100,
      render: (text, record) => (
        <Text style={{ color: record.failedColor || '#4a5565' }}>{text}</Text>
      ),
    },
    {
      title: 'Thao tác',
      key: 'action',
      width: 100,
      align: 'right',
      render: (_, record) => (
        <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
          <Button
            type="text"
            icon={<EditOutlined />}
            onClick={() => handleEditBank(record)}
            style={{ color: '#6a7282' }}
          />
          <Button
            type="text"
            icon={<DeleteOutlined />}
            onClick={() => handleDeleteBank(record)}
            style={{ color: '#6a7282' }}
          />
        </div>
      ),
    },
  ];

  // Failed cases data
  const failedCases = [
    {
      key: '1',
      bank: 'VCB',
      priority: 'high',
      priorityColor: '#ffe2e2',
      priorityTextColor: '#c10007',
      time: '10:45 AM',
      smsContent:
        'TK 1234567890 GD: -500,000d 24/03 GRAB. Tai GRAB VIETNAM. So du: 15,234,567d',
      error: 'Format not recognized - missing date pattern',
    },
    {
      key: '2',
      bank: 'TCB',
      priority: 'medium',
      priorityColor: '#fef9c2',
      priorityTextColor: '#a65f00',
      time: '10:32 AM',
      smsContent: 'Tai khoan 9876543210 -250000VND 24/03/2024 SHOPEE',
      error: 'Amount parsing failed',
    },
    {
      key: '3',
      bank: 'Unknown',
      priority: 'high',
      priorityColor: '#ffe2e2',
      priorityTextColor: '#c10007',
      time: '10:18 AM',
      smsContent: 'Your account was debited 100000 on 24/03',
      error: 'Bank not identified',
    },
  ];

  return (
    <div
      style={{
        minHeight: '100vh',
        background: 'linear-gradient(180deg, #F8F9FF 0%, #FFFFFF 100%)',
      }}
    >
      <AdminSidebar />
      <div
        style={{
          marginLeft: collapsed ? 80 : 280,
          padding: '32px',
          transition: 'margin-left 0.3s',
        }}
      >
        {/* Header */}
        <div
          style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'flex-start',
            marginBottom: 24,
          }}
        >
          <div>
            <Title level={2} style={{ margin: 0, marginBottom: 4 }}>
              Quản lý Ngân hàng & SMS Parser
            </Title>
            <Text type="secondary">
              Cấu hình template và regex cho SMS parsing
            </Text>
          </div>
          <div style={{ display: 'flex', gap: 12 }}>
            <Button
              icon={<PlayCircleOutlined />}
              onClick={() => {
                setIsTestParserModalOpen(true);
                setTestResult(null);
                testForm.resetFields();
              }}
              style={{
                height: 36,
                borderRadius: 8,
                border: '1px solid rgba(0,0,0,0.1)',
              }}
            >
              Test SMS Parser
            </Button>
            <Button
              type="primary"
              icon={<PlusOutlined />}
              onClick={() => setIsAddBankModalOpen(true)}
              style={{
                height: 36,
                borderRadius: 8,
                background: '#155dfc',
                borderColor: '#155dfc',
              }}
            >
              Thêm ngân hàng
            </Button>
          </div>
        </div>

        {/* Stats Cards */}
        <Row gutter={[24, 24]} style={{ marginBottom: 24 }}>
          {statsCards.map((card, index) => (
            <Col xs={24} sm={12} lg={6} key={index}>
              <Card
                style={{
                  borderRadius: 14,
                  border: '1px solid rgba(0,0,0,0.1)',
                }}
                bodyStyle={{ padding: '24px' }}
              >
                <Text
                  type="secondary"
                  style={{ fontSize: 14, display: 'block', marginBottom: 32 }}
                >
                  {card.title}
                </Text>
                <Title level={3} style={{ margin: '0 0 32px 0' }}>
                  {card.value}
                </Title>
                <Text
                  style={{
                    fontSize: 14,
                    color: card.descColor || '#6a7282',
                  }}
                >
                  {card.description}
                </Text>
              </Card>
            </Col>
          ))}
        </Row>

        {/* Parser Statistics */}
        <Card
          title="Thống kê Parser (Hôm nay)"
          style={{
            marginBottom: 24,
            borderRadius: 14,
            border: '1px solid rgba(0,0,0,0.1)',
          }}
          headStyle={{
            borderBottom: '1px solid rgba(0,0,0,0.1)',
            fontSize: 18,
          }}
        >
          <Table
            columns={parserColumns}
            dataSource={parserData}
            pagination={false}
            scroll={{ x: 1000 }}
          />
        </Card>

        {/* Bank Templates List */}
        <Card
          title="Danh sách ngân hàng & Templates"
          style={{
            marginBottom: 24,
            borderRadius: 14,
            border: '1px solid rgba(0,0,0,0.1)',
          }}
          headStyle={{
            borderBottom: '1px solid rgba(0,0,0,0.1)',
            fontSize: 18,
          }}
        >
          <Table
            columns={bankTemplatesColumns}
            dataSource={bankTemplatesData}
            pagination={false}
            scroll={{ x: 1200 }}
          />
        </Card>

        {/* Failed Parsing Cases */}
        <Card
          title={
            <div
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
              }}
            >
              <Text style={{ fontSize: 16 }}>Failed Parsing Cases</Text>
              <Tag
                color="#ffe2e2"
                style={{
                  border: 'none',
                  borderRadius: 8,
                  color: '#c10007',
                }}
              >
                3 cases cần xem xét
              </Tag>
            </div>
          }
          style={{
            borderRadius: 14,
            border: '1px solid rgba(0,0,0,0.1)',
          }}
          headStyle={{
            borderBottom: '1px solid rgba(0,0,0,0.1)',
          }}
          bodyStyle={{ padding: '24px' }}
        >
          <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
            {failedCases.map((failCase) => (
              <div
                key={failCase.key}
                style={{
                  background: '#fef2f2',
                  border: '1px solid #ffc9c9',
                  borderRadius: 10,
                  padding: '16px',
                }}
              >
                <div style={{ display: 'flex', gap: 12 }}>
                  <WarningOutlined
                    style={{ color: '#e7000b', fontSize: 20, marginTop: 4 }}
                  />
                  <div style={{ flex: 1 }}>
                    {/* Header */}
                    <div
                      style={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        marginBottom: 8,
                      }}
                    >
                      <div style={{ display: 'flex', gap: 8 }}>
                        <Text strong>{failCase.bank}</Text>
                        <Tag
                          color={failCase.priorityColor}
                          style={{
                            border: 'none',
                            borderRadius: 8,
                            color: failCase.priorityTextColor,
                          }}
                        >
                          {failCase.priority}
                        </Tag>
                      </div>
                      <Text type="secondary" style={{ fontSize: 12 }}>
                        {failCase.time}
                      </Text>
                    </div>

                    {/* SMS Content */}
                    <div
                      style={{
                        background: 'white',
                        border: '1px solid rgba(0,0,0,0.1)',
                        borderRadius: 4,
                        padding: '12px',
                        marginBottom: 8,
                        fontFamily: 'Cousine, monospace',
                        fontSize: 12,
                      }}
                    >
                      {failCase.smsContent}
                    </div>

                    {/* Error Message */}
                    <Text style={{ color: '#e7000b', marginBottom: 8, display: 'block' }}>
                      <Text strong style={{ color: '#e7000b' }}>Error: </Text>
                      {failCase.error}
                    </Text>

                    {/* Action Buttons */}
                    <div style={{ display: 'flex', gap: 8 }}>
                      <Button
                        size="small"
                        onClick={() => handleUpdatePattern(failCase)}
                        style={{
                          borderRadius: 8,
                          border: '1px solid rgba(0,0,0,0.1)',
                        }}
                      >
                        Update Pattern
                      </Button>
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </Card>
      </div>

      {/* Add Bank Modal */}
      <Modal
        title={
          <div>
            <div style={{ fontSize: 18, fontWeight: 700, color: '#0a0a0a', marginBottom: 4 }}>
              Thêm ngân hàng mới
            </div>
            <Text type="secondary" style={{ fontSize: 14 }}>
              Cấu hình SMS template cho ngân hàng
            </Text>
          </div>
        }
        open={isAddBankModalOpen}
        onCancel={() => {
          setIsAddBankModalOpen(false);
          form.resetFields();
        }}
        footer={null}
        width={548}
        centered
        styles={{
          header: { paddingBottom: 16, borderBottom: '1px solid #f0f0f0' },
          body: { paddingTop: 24 },
        }}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={(values) => {
            console.log('Add bank:', values);
            // TODO: Call API to add bank
            setIsAddBankModalOpen(false);
            form.resetFields();
            setSuccessMessage('Thêm ngân hàng thành công!');
            setIsSuccessModalOpen(true);
          }}
        >
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                label="Mã ngân hàng"
                name="code"
                rules={[{ required: true, message: 'Vui lòng nhập mã ngân hàng' }]}
              >
                <Input
                  placeholder="VCB, TCB, ACB..."
                  style={{
                    height: 36,
                    borderRadius: 8,
                    background: '#F3F3F5',
                    border: '1px solid transparent',
                  }}
                />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                label="Tên ngân hàng"
                name="name"
                rules={[{ required: true, message: 'Vui lòng nhập tên ngân hàng' }]}
              >
                <Input
                  placeholder="Vietcombank"
                  style={{
                    height: 36,
                    borderRadius: 8,
                    background: '#F3F3F5',
                    border: '1px solid transparent',
                  }}
                />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item
            label="SMS Template"
            name="template"
            rules={[{ required: true, message: 'Vui lòng nhập SMS template' }]}
          >
            <TextArea
              placeholder="TK {account} GD: -{amount}d {date} {description}. SD: {balance}d"
              rows={3}
              style={{
                borderRadius: 8,
                background: '#F3F3F5',
                border: '1px solid transparent',
                fontFamily: 'Cousine, monospace',
                fontSize: 14,
              }}
            />
          </Form.Item>
          <Text type="secondary" style={{ fontSize: 12, display: 'block', marginTop: -16, marginBottom: 16 }}>
            Sử dụng placeholders: {'{account}'}, {'{amount}'}, {'{date}'}, {'{description}'}, {'{balance}'}
          </Text>

          <Form.Item
            label="Regex Pattern"
            name="regex"
            rules={[{ required: true, message: 'Vui lòng nhập regex pattern' }]}
          >
            <TextArea
              placeholder="TK\s(\d+)\sGD:\s-(\d+)d\s(\d{2}/\d{2})\s(.+)\.\sSD:\s(\d+)d"
              rows={3}
              style={{
                borderRadius: 8,
                background: '#F3F3F5',
                border: '1px solid transparent',
                fontFamily: 'Cousine, monospace',
                fontSize: 14,
              }}
            />
          </Form.Item>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8, marginTop: 24 }}>
            <Button
              onClick={() => {
                setIsAddBankModalOpen(false);
                form.resetFields();
              }}
              style={{
                height: 36,
                borderRadius: 8,
                paddingLeft: 16,
                paddingRight: 16,
              }}
            >
              Hủy
            </Button>
            <Button
              type="primary"
              htmlType="submit"
              style={{
                height: 36,
                borderRadius: 8,
                background: '#155dfc',
                borderColor: '#155dfc',
                paddingLeft: 16,
                paddingRight: 16,
              }}
            >
              Thêm ngân hàng
            </Button>
          </div>
        </Form>
      </Modal>

      {/* Edit Bank Modal */}
      <Modal
        title={
          <div>
            <div style={{ fontSize: 18, fontWeight: 700, color: '#0a0a0a', marginBottom: 4 }}>
              Chỉnh sửa ngân hàng
            </div>
            <Text type="secondary" style={{ fontSize: 14 }}>
              Cập nhật SMS template cho ngân hàng
            </Text>
          </div>
        }
        open={isEditBankModalOpen}
        onCancel={() => {
          setIsEditBankModalOpen(false);
          editForm.resetFields();
          setEditingBank(null);
        }}
        footer={null}
        width={548}
        centered
        styles={{
          header: { paddingBottom: 16, borderBottom: '1px solid #f0f0f0' },
          body: { paddingTop: 24 },
        }}
      >
        <Form
          form={editForm}
          layout="vertical"
          onFinish={handleUpdateBank}
        >
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                label="Mã ngân hàng"
                name="code"
                rules={[{ required: true, message: 'Vui lòng nhập mã ngân hàng' }]}
              >
                <Input
                  placeholder="VCB, TCB, ACB..."
                  style={{
                    height: 36,
                    borderRadius: 8,
                    background: '#F3F3F5',
                    border: '1px solid transparent',
                  }}
                />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                label="Tên ngân hàng"
                name="name"
                rules={[{ required: true, message: 'Vui lòng nhập tên ngân hàng' }]}
              >
                <Input
                  placeholder="Vietcombank"
                  style={{
                    height: 36,
                    borderRadius: 8,
                    background: '#F3F3F5',
                    border: '1px solid transparent',
                  }}
                />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item
            label="SMS Template"
            name="template"
            rules={[{ required: true, message: 'Vui lòng nhập SMS template' }]}
          >
            <TextArea
              placeholder="TK {account} GD: -{amount}d {date} {description}. SD: {balance}d"
              rows={3}
              style={{
                borderRadius: 8,
                background: '#F3F3F5',
                border: '1px solid transparent',
                fontFamily: 'Cousine, monospace',
                fontSize: 14,
              }}
            />
          </Form.Item>
          <Text type="secondary" style={{ fontSize: 12, display: 'block', marginTop: -16, marginBottom: 16 }}>
            Sử dụng placeholders: {'{account}'}, {'{amount}'}, {'{date}'}, {'{description}'}, {'{balance}'}
          </Text>

          <Form.Item
            label="Regex Pattern"
            name="regex"
            rules={[{ required: true, message: 'Vui lòng nhập regex pattern' }]}
          >
            <TextArea
              placeholder="TK\s(\d+)\sGD:\s-(\d+)d\s(\d{2}/\d{2})\s(.+)\.\sSD:\s(\d+)d"
              rows={3}
              style={{
                borderRadius: 8,
                background: '#F3F3F5',
                border: '1px solid transparent',
                fontFamily: 'Cousine, monospace',
                fontSize: 14,
              }}
            />
          </Form.Item>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8, marginTop: 24 }}>
            <Button
              onClick={() => {
                setIsEditBankModalOpen(false);
                editForm.resetFields();
                setEditingBank(null);
              }}
              style={{
                height: 36,
                borderRadius: 8,
                paddingLeft: 16,
                paddingRight: 16,
              }}
            >
              Hủy
            </Button>
            <Button
              type="primary"
              htmlType="submit"
              style={{
                height: 36,
                borderRadius: 8,
                background: '#155dfc',
                borderColor: '#155dfc',
                paddingLeft: 16,
                paddingRight: 16,
              }}
            >
              Cập nhật ngân hàng
            </Button>
          </div>
        </Form>
      </Modal>

      {/* Test SMS Parser Modal */}
      <Modal
        title={
          <div>
            <div style={{ fontSize: 18, fontWeight: 700, color: '#0a0a0a', marginBottom: 4 }}>
              Test SMS Parser
            </div>
            <Text type="secondary" style={{ fontSize: 14 }}>
              Kiểm tra parsing SMS real-time
            </Text>
          </div>
        }
        open={isTestParserModalOpen}
        onCancel={() => {
          setIsTestParserModalOpen(false);
          testForm.resetFields();
          setTestResult(null);
        }}
        footer={null}
        width={548}
        centered
        styles={{
          header: { paddingBottom: 16, borderBottom: '1px solid #f0f0f0' },
          body: { paddingTop: 24 },
        }}
      >
        <Form
          form={testForm}
          layout="vertical"
          onFinish={handleTestParser}
        >
          <Form.Item
            label="Nhập SMS cần test"
            name="smsText"
            rules={[{ required: true, message: 'Vui lòng nhập SMS cần test' }]}
          >
            <TextArea
              placeholder="TK 1234567890 GD: -500,000d 24/03 GRAB. SD: 15,234,567d"
              rows={3}
              style={{
                borderRadius: 8,
                background: '#F3F3F5',
                border: '1px solid transparent',
                fontFamily: 'Cousine, monospace',
                fontSize: 14,
              }}
            />
          </Form.Item>

          <Button
            type="primary"
            htmlType="submit"
            icon={<PlayCircleOutlined />}
            block
            style={{
              height: 44,
              borderRadius: 8,
              background: '#155dfc',
              borderColor: '#155dfc',
              fontSize: 16,
              fontWeight: 600,
              marginBottom: 16,
            }}
          >
            Test Parse
          </Button>

          {/* Test Result Display */}
          {testResult && (
            <div
              style={{
                background: testResult.success ? '#dcfce7' : '#fee2e2',
                border: `1px solid ${testResult.success ? '#86efac' : '#fca5a5'}`,
                borderRadius: 10,
                padding: '16px',
              }}
            >
              {testResult.success ? (
                <>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 12 }}>
                    <div
                      style={{
                        width: 20,
                        height: 20,
                        borderRadius: '50%',
                        border: '2px solid #00C950',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                      }}
                    >
                      <span style={{ color: '#00C950', fontSize: 12, fontWeight: 'bold' }}>✓</span>
                    </div>
                    <Text strong style={{ color: '#008236' }}>
                      Parsing thành công - Bank: {testResult.bank}
                    </Text>
                  </div>
                  <div
                    style={{
                      background: 'white',
                      borderRadius: 8,
                      padding: '12px',
                    }}
                  >
                    <Row gutter={[16, 8]}>
                      <Col span={12}>
                        <Text type="secondary" style={{ fontSize: 12 }}>Account:</Text>
                        <div style={{ fontWeight: 500 }}>{testResult.data.account}</div>
                      </Col>
                      <Col span={12}>
                        <Text type="secondary" style={{ fontSize: 12 }}>Amount:</Text>
                        <div style={{ fontWeight: 500 }}>₫{testResult.data.amount}</div>
                      </Col>
                      <Col span={12}>
                        <Text type="secondary" style={{ fontSize: 12 }}>Date:</Text>
                        <div style={{ fontWeight: 500 }}>{testResult.data.date}</div>
                      </Col>
                      <Col span={12}>
                        <Text type="secondary" style={{ fontSize: 12 }}>Balance:</Text>
                        <div style={{ fontWeight: 500 }}>₫{testResult.data.balance}</div>
                      </Col>
                      <Col span={24}>
                        <Text type="secondary" style={{ fontSize: 12 }}>Description:</Text>
                        <div style={{ fontWeight: 500 }}>{testResult.data.description}</div>
                      </Col>
                    </Row>
                  </div>
                </>
              ) : (
                <div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8 }}>
                    <WarningOutlined style={{ color: '#e7000b', fontSize: 18 }} />
                    <Text strong style={{ color: '#c10007' }}>
                      Parsing thất bại
                    </Text>
                  </div>
                  <Text style={{ color: '#c10007' }}>{testResult.error}</Text>
                </div>
              )}
            </div>
          )}
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
        onConfirm={handleConfirmDelete}
        onCancel={() => {
          setIsDeleteConfirmOpen(false);
          setDeletingBank(null);
        }}
        title="Xác nhận xóa"
        content={`Bạn có chắc chắn muốn xóa ngân hàng ${deletingBank?.name || ''} (${deletingBank?.code || ''})?\nHành động này không thể hoàn tác.`}
        confirmText="Xóa"
        cancelText="Hủy"
        danger={true}
      />
    </div>
  );
};

export default BankAndSmsparserPage;
