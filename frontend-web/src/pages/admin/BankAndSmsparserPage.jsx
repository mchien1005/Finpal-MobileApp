import React, { useState, useEffect } from 'react';
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
  Input,
  message,
  Spin 
} from 'antd';
import * as smsParserService from '../../services/smsParserService';
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
  const [loading, setLoading] = useState(false);
  const [bankTemplatesData, setBankTemplatesData] = useState([]);
  const [form] = Form.useForm();
  const [editForm] = Form.useForm();
  const [testForm] = Form.useForm();

  // Fetch parsers on mount
  useEffect(() => {
    fetchParsers();
  }, []);

  // Fetch all parsers from API
  const fetchParsers = async () => {
    try {
      setLoading(true);
      const data = await smsParserService.getAllParsers();
      // Transform API response to table format
      const formattedData = data.map((parser, index) => ({
        key: parser.id?.toString() || index.toString(),
        id: parser.id,
        icon: '🏦',
        name: parser.bankName || parser.bankCode,
        code: parser.bankCode,
        template: parser.fieldMappings,
        regex: parser.regexPattern,
      }));
      setBankTemplatesData(formattedData);
    } catch (error) {
      message.error('Không thể tải danh sách ngân hàng');
      console.error('Error fetching parsers:', error);
    } finally {
      setLoading(false);
    }
  };

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
  const handleUpdateBank = async (values) => {
    try {
      setLoading(true);
      await smsParserService.updateParser(editingBank.id, {
        bankCode: values.code,
        bankName: values.name,
        senderNumber: values.code,
        regexPattern: values.regex,
        fieldMappings: values.template,
        isActive: true,
        priority: 1,
      });
      setIsEditBankModalOpen(false);
      editForm.resetFields();
      setEditingBank(null);
      setSuccessMessage('Cập nhật ngân hàng thành công!');
      setIsSuccessModalOpen(true);
      // Refresh list
      await fetchParsers();
    } catch (error) {
      message.error('Không thể cập nhật ngân hàng');
      console.error('Error updating parser:', error);
    } finally {
      setLoading(false);
    }
  };

  // Handler to test SMS parser
  const handleTestParser = async (values) => {
    try {
      setLoading(true);
      const smsText = values.smsText;
      const response = await smsParserService.testParserSimple(smsText);
      
      console.log('API Response:', response); // Debug log
      
      // Response structure: { matched, message, extractedFields, parsedData }
      if (response && response.matched === true && response.extractedFields) {
        setTestResult({
          success: true,
          bank: response.extractedFields._bankCode || response.extractedFields._bankName || 'Unknown',
          data: {
            account: response.extractedFields.account || 'N/A',
            amount: response.extractedFields.amount || 'N/A',
            date: response.extractedFields.date || 'N/A',
            description: response.extractedFields.merchant || response.extractedFields.description || 'N/A',
            balance: response.extractedFields.balance || 'N/A',
          },
        });
      } else {
        // Parsing failed
        const errorMsg = response?.message || 'Không thể parse SMS - format không khớp với template nào';
        setTestResult({
          success: false,
          error: errorMsg,
        });
      }
    } catch (error) {
      console.error('Test Parser Error:', error); // Debug log
      setTestResult({
        success: false,
        error: error.response?.data?.message || error.message || 'Lỗi khi test SMS parser',
      });
    } finally {
      setLoading(false);
    }
  };

  // Handler to open delete confirmation
  const handleDeleteBank = (bank) => {
    setDeletingBank(bank);
    setIsDeleteConfirmOpen(true);
  };

  // Handler to confirm delete
  const handleConfirmDelete = async () => {
    try {
      setLoading(true);
      await smsParserService.deleteParser(deletingBank.id);
      setIsDeleteConfirmOpen(false);
      setDeletingBank(null);
      setSuccessMessage('Xóa ngân hàng thành công!');
      setIsSuccessModalOpen(true);
      // Refresh list
      await fetchParsers();
    } catch (error) {
      message.error('Không thể xóa ngân hàng');
      console.error('Error deleting parser:', error);
      setIsDeleteConfirmOpen(false);
      setDeletingBank(null);
    } finally {
      setLoading(false);
    }
  };



  // Bank templates data is now fetched from API and stored in state

  // Bank templates columns
  const bankTemplatesColumns = [
    {
      title: 'Ngân hàng',
      dataIndex: 'name',
      key: 'name',
      width: 200,
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
      title: 'Field Mappings',
      dataIndex: 'template',
      key: 'template',
      width: 300,
      render: (text) => (
        <div
          style={{
            background: '#f3f4f6',
            padding: '8px 12px',
            borderRadius: 4,
            fontFamily: 'Cousine, monospace',
            fontSize: 12,
            maxWidth: 300,
            whiteSpace: 'pre-wrap',
            wordBreak: 'break-word',
            overflow: 'hidden',
          }}
        >
          {text}
        </div>
      ),
    },
    {
      title: 'Regex Pattern',
      dataIndex: 'regex',
      key: 'regex',
      width: 350,
      render: (text) => (
        <div
          style={{
            background: '#f3f4f6',
            padding: '8px 12px',
            borderRadius: 4,
            fontFamily: 'Cousine, monospace',
            fontSize: 11,
            maxWidth: 350,
            whiteSpace: 'pre-wrap',
            wordBreak: 'break-word',
            overflow: 'hidden',
          }}
        >
          {text}
        </div>
      ),
    },
    {
      title: 'Thao tác',
      key: 'action',
      width: 120,
      align: 'right',
      fixed: 'right',
      render: (_, record) => (
        <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
          <Button
            type="text"
            icon={<EditOutlined />}
            onClick={() => handleEditBank(record)}
            style={{ color: '#f59e0b' }}
          />
          <Button
            type="text"
            icon={<DeleteOutlined />}
            onClick={() => handleDeleteBank(record)}
            style={{ color: '#e7000b' }}
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
          <Spin spinning={loading}>
            <Table
              columns={bankTemplatesColumns}
              dataSource={bankTemplatesData}
              pagination={false}
              scroll={{ x: 1100 }}
            />
          </Spin>
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
          onFinish={async (values) => {
            try {
              setLoading(true);
              await smsParserService.createParser({
                bankCode: values.code,
                bankName: values.name,
                senderNumber: values.code,
                regexPattern: values.regex,
                fieldMappings: values.template,
                sampleSms: '',
                isActive: true,
                priority: 1,
              });
              setIsAddBankModalOpen(false);
              form.resetFields();
              setSuccessMessage('Thêm ngân hàng thành công!');
              setIsSuccessModalOpen(true);
              // Refresh list
              await fetchParsers();
            } catch (error) {
              message.error('Không thể thêm ngân hàng');
              console.error('Error creating parser:', error);
            } finally {
              setLoading(false);
            }
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
            label="Field Mappings"
            name="template"
            rules={[{ required: true, message: 'Vui lòng nhập Field Mappings' }]}
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
            label="Field Mappings"
            name="template"
            rules={[{ required: true, message: 'Vui lòng nhập Field Mappings' }]}
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
