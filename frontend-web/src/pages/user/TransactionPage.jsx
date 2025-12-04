import React, { useState, useEffect } from 'react';
import {
  Button,
  Input,
  Select,
  DatePicker,
  Typography,
  message,
  Spin,
} from 'antd';
import {
  PlusOutlined,
  LoadingOutlined,
} from '@ant-design/icons';
import Sidebar from '../../components/user/Sidebar';
import Header from '../../components/common/Header';
import { useSidebar } from '../../contexts/SidebarContext';
import { createTransaction, getTransactions } from '../../services/transactionService';
import { getCategories } from '../../services/categoryService';
import dayjs from 'dayjs';
import SuccessModal from '../../components/common/SuccessModal';

const { TextArea } = Input;
const { Text } = Typography;

const TransactionPage = () => {
  const { collapsed } = useSidebar();
  const [activeTab, setActiveTab] = useState('expense'); // 'expense' or 'income'
  const [amount, setAmount] = useState('');
  const [category, setCategory] = useState(null);
  const [description, setDescription] = useState('');
  const [date, setDate] = useState(dayjs());
  const [transactionSource, setTransactionSource] = useState('CASH'); // VCB, TCB, CASH, MOMO...
  const [merchant, setMerchant] = useState('');
  
  // API data states
  const [categories, setCategories] = useState([]);
  const [recentTransactions, setRecentTransactions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [showSuccessModal, setShowSuccessModal] = useState(false);

  // Load categories và giao dịch gần đây từ API
  const fetchData = async () => {
    setLoading(true);
    try {
      const [categoriesData, transactionsData] = await Promise.all([
        getCategories(),
        getTransactions({ page: 0, size: 6 }),
      ]);
      setCategories(categoriesData || []);
      setRecentTransactions(transactionsData?.content || []);
    } catch (error) {
      console.error('Error fetching data:', error);
      message.error('Không thể tải dữ liệu. Vui lòng thử lại!');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  // Filter categories theo loại giao dịch
  const filteredCategories = categories.filter(
    (cat) => cat.type === (activeTab === 'expense' ? 'EXPENSE' : 'INCOME')
  );

  // Format options cho Select
  const categoryOptions = filteredCategories.map((cat) => ({
    value: cat.id,
    label: cat.name,
  }));

  // Nguồn giao dịch options
  const transactionSourceOptions = [
    { value: 'CASH', label: 'Tiền mặt' },
    { value: 'VCB', label: 'Vietcombank' },
    { value: 'TCB', label: 'Techcombank' },
    { value: 'MB', label: 'MB Bank' },
    { value: 'VPB', label: 'VPBank' },
    { value: 'ACB', label: 'ACB' },
    { value: 'BIDV', label: 'BIDV' },
    { value: 'VTB', label: 'Vietinbank' },
    { value: 'MOMO', label: 'Ví MoMo' },
    { value: 'ZALOPAY', label: 'ZaloPay' },
    { value: 'VNPAY', label: 'VNPay' },
    { value: 'OTHER', label: 'Khác' },
  ];

  // Reset category khi đổi tab
  useEffect(() => {
    setCategory(null);
  }, [activeTab]);

  // Helper function format tiền
  const formatMoney = (amount) => {
    return new Intl.NumberFormat('vi-VN').format(amount) + 'đ';
  };

  // Helper function format ngày
  const formatDate = (dateStr) => {
    const date = new Date(dateStr);
    return date.toLocaleDateString('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  // Xử lý thêm giao dịch
  const handleAddTransaction = async () => {
    // Validate
    if (!amount || parseFloat(amount.replace(/,/g, '')) <= 0) {
      message.error('Vui lòng nhập số tiền hợp lệ!');
      return;
    }

    setSubmitting(true);
    try {
      const transactionData = {
        transactionSource: transactionSource || 'CASH',
        categoryId: category || null,
        amount: parseFloat(amount.replace(/,/g, '')),
        type: activeTab === 'expense' ? 'EXPENSE' : 'INCOME',
        merchant: merchant || null,
        description: description || null,
        transactionDate: date.format('YYYY-MM-DDTHH:mm:ss'),
        isAuto: false,
      };

      await createTransaction(transactionData);
      
      // Reset form
      setAmount('');
      setCategory(null);
      setDescription('');
      setMerchant('');
      setDate(dayjs());
      setTransactionSource('CASH');
      
      // Refresh danh sách giao dịch
      const transactionsData = await getTransactions({ page: 0, size: 5 });
      setRecentTransactions(transactionsData?.content || []);
      
      // Hiển thị popup thành công
      setShowSuccessModal(true);
    } catch (error) {
      console.error('Error creating transaction:', error);
      const errorMsg = error.response?.data?.message || 'Không thể thêm giao dịch. Vui lòng thử lại!';
      message.error(errorMsg);
    } finally {
      setSubmitting(false);
    }
  };

  // Format số tiền với dấu phẩy
  const formatAmount = (value) => {
    const number = value.replace(/\D/g, '');
    return number.replace(/\B(?=(\d{3})+(?!\d))/g, ',');
  };

  const handleAmountChange = (e) => {
    const formatted = formatAmount(e.target.value);
    setAmount(formatted);
  };

  if (loading) {
    return (
      <div style={{ display: 'flex', minHeight: '100vh', background: '#F5F7FA' }}>
        <Sidebar />
        <div style={{ 
          marginLeft: collapsed ? 80 : 280, 
          flex: 1, 
          display: 'flex', 
          justifyContent: 'center', 
          alignItems: 'center' 
        }}>
          <Spin indicator={<LoadingOutlined style={{ fontSize: 48 }} spin />} />
        </div>
      </div>
    );
  }

  return (
    <div style={{ display: 'flex', minHeight: '100vh', background: '#F5F7FA' }}>
      <Sidebar />

      {/* Main Content */}
      <div style={{ marginLeft: collapsed ? 80 : 280, flex: 1, transition: 'margin-left 0.3s' }}>
        {/* Header */}
        <Header title="Thêm giao dịch mới" />

        {/* Content - 2 Column Layout */}
        <div style={{ padding: '16px 16px 0', display: 'flex', gap: 30 }}>
          {/* Left Column - Add Transaction Form */}
          <div
            style={{
              flex: 1,
              background: '#FFFFFF',
              borderRadius: 14,
              border: '1px solid rgba(0,0,0,0.1)',
              padding: 25,
            }}
          >
            <div style={{ fontSize: 16, fontWeight: 500, color: '#0a0a0a', marginBottom: 30 }}>
              Thêm giao dịch thủ công
            </div>

            {/* Loại giao dịch */}
            <div style={{ marginBottom: 16 }}>
              <Text style={{ display: 'block', marginBottom: 8, fontSize: 14, color: '#0a0a0a' }}>
                Loại giao dịch
              </Text>
              <div style={{ display: 'flex', gap: 22 }}>
                <Button
                  type={activeTab === 'expense' ? 'primary' : 'default'}
                  onClick={() => setActiveTab('expense')}
                  style={{
                    flex: 1,
                    height: 36,
                    borderRadius: 8,
                    fontSize: 14,
                    ...(activeTab === 'expense'
                      ? { background: '#FB2C36', borderColor: '#FB2C36' }
                      : { background: '#FFFFFF', borderColor: 'rgba(0,0,0,0.1)', color: '#0a0a0a' }),
                  }}
                >
                  Chi tiêu
                </Button>
                <Button
                  type={activeTab === 'income' ? 'primary' : 'default'}
                  onClick={() => setActiveTab('income')}
                  style={{
                    flex: 1,
                    height: 36,
                    borderRadius: 8,
                    fontSize: 14,
                    ...(activeTab === 'income'
                      ? { background: '#10B981', borderColor: '#10B981' }
                      : { background: '#FFFFFF', borderColor: 'rgba(0,0,0,0.1)', color: '#0a0a0a' }),
                  }}
                >
                  Thu nhập
                </Button>
              </div>
            </div>

            {/* Số tiền */}
            <div style={{ marginBottom: 16 }}>
              <Text style={{ display: 'block', marginBottom: 8, fontSize: 14, color: '#0a0a0a' }}>
                Số tiền (VNĐ) <span style={{ color: '#E7000B' }}>*</span>
              </Text>
              <Input
                placeholder="0"
                suffix={<span style={{ color: '#99A1AF' }}>đ</span>}
                value={amount}
                onChange={handleAmountChange}
                style={{
                  height: 36,
                  borderRadius: 8,
                  background: '#F3F3F5',
                  border: '1px solid transparent',
                }}
              />
            </div>

            {/* Nguồn giao dịch */}
            <div style={{ marginBottom: 16 }}>
              <Text style={{ display: 'block', marginBottom: 8, fontSize: 14, color: '#0a0a0a' }}>
                Nguồn giao dịch
              </Text>
              <Select
                placeholder="Chọn nguồn giao dịch"
                value={transactionSource}
                onChange={setTransactionSource}
                style={{ width: '100%', height: 36 }}
                options={transactionSourceOptions}
                dropdownStyle={{ borderRadius: 8 }}
                className="custom-select"
              />
            </div>

            {/* Danh mục */}
            <div style={{ marginBottom: 16 }}>
              <Text style={{ display: 'block', marginBottom: 8, fontSize: 14, color: '#0a0a0a' }}>
                Danh mục
              </Text>
              <Select
                placeholder="Chọn danh mục"
                value={category}
                onChange={setCategory}
                style={{ width: '100%', height: 36 }}
                options={categoryOptions}
                allowClear
                dropdownStyle={{ borderRadius: 8 }}
                className="custom-select"
              />
            </div>

            {/* Nơi giao dịch/Cửa hàng */}
            <div style={{ marginBottom: 16 }}>
              <Text style={{ display: 'block', marginBottom: 8, fontSize: 14, color: '#0a0a0a' }}>
                Nơi giao dịch/Cửa hàng
              </Text>
              <Input
                placeholder="Ví dụ: Grab, Shopee,.."
                value={merchant}
                onChange={(e) => setMerchant(e.target.value)}
                style={{
                  height: 36,
                  borderRadius: 8,
                  background: '#F3F3F5',
                  border: '1px solid transparent',
                }}
              />
            </div>

            {/* Mô tả */}
            <div style={{ marginBottom: 16 }}>
              <Text style={{ display: 'block', marginBottom: 8, fontSize: 14, color: '#0a0a0a' }}>
                Mô tả
              </Text>
              <TextArea
                placeholder="Ví dụ: Mua bánh mì sáng, đổ xăng..."
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                rows={2}
                style={{
                  borderRadius: 8,
                  background: '#F3F3F5',
                  border: '1px solid transparent',
                  resize: 'none',
                }}
              />
            </div>

            {/* Ngày giao dịch */}
            <div style={{ marginBottom: 24 }}>
              <Text style={{ display: 'block', marginBottom: 8, fontSize: 14, color: '#0a0a0a' }}>
                Ngày giao dịch
              </Text>
              <DatePicker
                value={date}
                onChange={setDate}
                style={{
                  width: '100%',
                  height: 36,
                  borderRadius: 8,
                  background: '#F3F3F5',
                  border: '1px solid transparent',
                }}
                format="DD/MM/YYYY"
              />
            </div>

            {/* Submit Button */}
            <Button
              type="primary"
              icon={submitting ? <LoadingOutlined /> : <PlusOutlined />}
              onClick={handleAddTransaction}
              loading={submitting}
              disabled={submitting}
              block
              style={{
                height: 36,
                borderRadius: 8,
                background: 'linear-gradient(to right, #155DFC, #4F39F6)',
                border: 'none',
                fontSize: 14,
              }}
            >
              {submitting ? 'Đang xử lý...' : 'Thêm giao dịch'}
            </Button>
          </div>

          {/* Right Column - SMS gần đây */}
          <div
            style={{
              flex: 1,
              background: '#FFFFFF',
              borderRadius: 14,
              border: '1px solid rgba(0,0,0,0.1)',
              padding: 25,
            }}
          >
            {/* Header */}
            <div
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                marginBottom: 24,
              }}
            >
              <span style={{ fontSize: 14, fontWeight: 500, color: '#0a0a0a' }}>
                SMS gần đây
              </span>
              <span
                style={{
                  background: '#ECEEF2',
                  borderRadius: 8,
                  padding: '3px 9px',
                  fontSize: 12,
                  color: '#030213',
                }}
              >
                {recentTransactions.length} tin mới
              </span>
            </div>

            {/* SMS/Transaction List */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              {recentTransactions.length === 0 ? (
                <div style={{ textAlign: 'center', padding: 24, color: '#9CA3AF' }}>
                  Chưa có giao dịch nào
                </div>
              ) : (
                recentTransactions.map((tx) => (
                  <div
                    key={tx.id}
                    style={{
                      padding: 13,
                      borderRadius: 10,
                      background: tx.type === 'INCOME' ? '#F0FDF4' : '#EFF6FF',
                      border: `1px solid ${tx.type === 'INCOME' ? '#B9F8CF' : '#BEDBFF'}`,
                      display: 'flex',
                      flexDirection: 'column',
                      gap: 4,
                    }}
                  >
                    {/* Row 1: Bank & Amount */}
                    <div
                      style={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                      }}
                    >
                      <span
                        style={{
                          fontSize: 14,
                          fontWeight: 500,
                          color: tx.type === 'INCOME' ? '#016630' : '#193CB8',
                        }}
                      >
                        {tx.transactionSource || tx.categoryName || 'Chưa phân loại'}
                      </span>
                      <span
                        style={{
                          fontSize: 14,
                          fontWeight: 500,
                          color: tx.type === 'INCOME' ? '#00A63E' : '#E7000B',
                        }}
                      >
                        {tx.type === 'INCOME' ? '+' : '-'}{formatMoney(tx.amount)}
                      </span>
                    </div>
                    {/* Row 2: Description */}
                    <div style={{ fontSize: 12, color: '#4A5565' }}>
                      ND: {tx.merchant || tx.description || 'Không có mô tả'}
                    </div>
                    {/* Row 3: Date */}
                    <div style={{ fontSize: 12, color: '#99A1AF' }}>
                      {formatDate(tx.transactionDate)}
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Success Modal */}
      <SuccessModal
        open={showSuccessModal}
        onClose={() => setShowSuccessModal(false)}
        message="Thêm giao dịch thành công!"
      />
    </div>
  );
};

export default TransactionPage;
