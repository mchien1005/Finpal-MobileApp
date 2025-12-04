import { Card, Row, Col, Progress, Typography } from 'antd';
import {
  ArrowUpOutlined,
  ArrowDownOutlined,
  WalletOutlined,
} from '@ant-design/icons';
import Sidebar from '../../components/user/Sidebar';
import { useSidebar } from '../../contexts/SidebarContext';
import Header from '../../components/common/Header';
import React, { useState, useEffect } from 'react';
import { getTransactions } from '../../services/transactionService';
import { PieChart, Pie, Cell, ResponsiveContainer } from 'recharts';

const { Title, Text } = Typography;

// Format số tiền
const formatCurrency = (amount) => {
  if (amount >= 1000000) {
    return `${(amount / 1000000).toFixed(1)}M`;
  }
  return amount.toLocaleString('vi-VN');
};

const formatFullCurrency = (amount) => {
  return amount.toLocaleString('vi-VN') + ' đ';
};

// Màu sắc cho các danh mục - matching Figma design
const CATEGORY_COLORS = {
  'Ăn uống': '#FF6B6B',
  'Di chuyển': '#4ECDC4',
  'Mua sắm': '#FFE66D',
  'Giải trí': '#A8E6CF',
  'Hóa đơn': '#FF8B94',
  'Sức khỏe': '#C7CEEA',
  'Giáo dục': '#FFDAC1',
  'Khác': '#B4B4B4',
};

const DashboardPage = () => {
  const { collapsed } = useSidebar();
  const [totalIncome, setTotalIncome] = useState(15000000);
  const [totalExpense, setTotalExpense] = useState(8800000);
  const [budgetLimit] = useState(15000000);
  const [categoryData, setCategoryData] = useState([
    { name: 'Ăn uống', value: 3500000, percentage: 40 },
    { name: 'Di chuyển', value: 1500000, percentage: 17 },
    { name: 'Mua sắm', value: 2000000, percentage: 23 },
    { name: 'Giải trí', value: 800000, percentage: 9 },
    { name: 'Hóa đơn', value: 950000, percentage: 11 },
  ]);

  const remaining = totalIncome - totalExpense;
  const spentPercentage = budgetLimit > 0 ? ((totalExpense / budgetLimit) * 100).toFixed(1) : 0;

  // Lấy hạng mục chi nhiều nhất
  const topCategory = categoryData.reduce((max, cat) => 
    cat.value > max.value ? cat : max, categoryData[0]);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const now = new Date();
        const startDate = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-01`;
        const lastDay = new Date(now.getFullYear(), now.getMonth() + 1, 0).getDate();
        const endDate = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${lastDay}`;

        const response = await getTransactions({ startDate, endDate, size: 1000 });
        const transactions = response.content || [];

        // Tính tổng thu nhập và chi tiêu
        let income = 0;
        let expense = 0;
        const categoryMap = {};

        transactions.forEach(tx => {
          if (tx.type === 'INCOME') {
            income += tx.amount;
          } else {
            expense += tx.amount;
            const categoryName = tx.categoryName || 'Khác';
            categoryMap[categoryName] = (categoryMap[categoryName] || 0) + tx.amount;
          }
        });

        if (income > 0) setTotalIncome(income);
        if (expense > 0) setTotalExpense(expense);

        // Chuyển đổi category data
        if (Object.keys(categoryMap).length > 0) {
          const totalExp = Object.values(categoryMap).reduce((a, b) => a + b, 0);
          const cats = Object.entries(categoryMap).map(([name, value]) => ({
            name,
            value,
            percentage: Math.round((value / totalExp) * 100)
          }));
          setCategoryData(cats.sort((a, b) => b.value - a.value));
        }
      } catch (error) {
        console.error('Error fetching dashboard data:', error);
      }
    };

    fetchData();
  }, []);

  // Custom label cho Pie Chart
  const renderCustomLabel = ({ cx, cy, midAngle, outerRadius, name, percentage }) => {
    const RADIAN = Math.PI / 180;
    const radius = outerRadius + 40;
    const x = cx + radius * Math.cos(-midAngle * RADIAN);
    const y = cy + radius * Math.sin(-midAngle * RADIAN);
    const color = CATEGORY_COLORS[name] || '#6b7280';

    return (
      <text
        x={x}
        y={y}
        fill={color}
        textAnchor={x > cx ? 'start' : 'end'}
        dominantBaseline="central"
        style={{ fontSize: '14px', fontWeight: 500 }}
      >
        {`${name} ${percentage}%`}
      </text>
    );
  };

  return (
    <div style={{ display: 'flex', minHeight: '100vh', background: '#FAFBFC' }}>
      <Sidebar />

      {/* Main Content */}
      <div style={{ marginLeft: collapsed ? 80 : 280, flex: 1, transition: 'margin-left 0.3s ease' }}>
        {/* Header */}
        <Header title="Tổng quan tài chính" />

        {/* Content */}
        <div style={{ padding: 24 }}>
          {/* Summary Cards */}
          <Row gutter={16} style={{ marginBottom: 24 }}>
            <Col xs={24} sm={8}>
              <Card
                style={{
                  background: 'linear-gradient(135deg, #4CAF50 0%, #45A049 100%)',
                  border: 'none',
                  borderRadius: 16,
                  boxShadow: '0 4px 12px rgba(76, 175, 80, 0.2)',
                }}
                bodyStyle={{ padding: 20 }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 12 }}>
                  <ArrowUpOutlined style={{ color: '#fff', fontSize: 20 }} />
                  <Text style={{ color: '#fff', fontSize: 14, fontWeight: 500 }}>Thu nhập</Text>
                </div>
                <Title level={3} style={{ color: '#fff', margin: 0, fontSize: 28, fontWeight: 700 }}>
                  {formatCurrency(totalIncome)}
                </Title>
              </Card>
            </Col>
            <Col xs={24} sm={8}>
              <Card
                style={{
                  background: 'linear-gradient(135deg, #FF5252 0%, #F44336 100%)',
                  border: 'none',
                  borderRadius: 16,
                  boxShadow: '0 4px 12px rgba(255, 82, 82, 0.2)',
                }}
                bodyStyle={{ padding: 20 }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 12 }}>
                  <ArrowDownOutlined style={{ color: '#fff', fontSize: 20 }} />
                  <Text style={{ color: '#fff', fontSize: 14, fontWeight: 500 }}>Chi tiêu</Text>
                </div>
                <Title level={3} style={{ color: '#fff', margin: 0, fontSize: 28, fontWeight: 700 }}>
                  {formatCurrency(totalExpense)}
                </Title>
              </Card>
            </Col>
            <Col xs={24} sm={8}>
              <Card
                style={{
                  background: 'linear-gradient(135deg, #9C27B0 0%, #7B1FA2 100%)',
                  border: 'none',
                  borderRadius: 16,
                  boxShadow: '0 4px 12px rgba(156, 39, 176, 0.2)',
                }}
                bodyStyle={{ padding: 20 }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 12 }}>
                  <WalletOutlined style={{ color: '#fff', fontSize: 20 }} />
                  <Text style={{ color: '#fff', fontSize: 14, fontWeight: 500 }}>Còn lại</Text>
                </div>
                <Title level={3} style={{ color: '#fff', margin: 0, fontSize: 28, fontWeight: 700 }}>
                  {formatCurrency(remaining)}
                </Title>
              </Card>
            </Col>
          </Row>

          {/* Spending Progress Card */}
          <Card
            style={{
              borderRadius: 16,
              border: 'none',
              marginBottom: 24,
              boxShadow: '0 2px 8px rgba(0,0,0,0.08)',
            }}
            bodyStyle={{ padding: 28 }}
          >
            <Title level={5} style={{ marginBottom: 20, fontSize: 18, fontWeight: 600, color: '#1a1a1a' }}>
              Tình hình chi tiêu tháng này
            </Title>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 12 }}>
              <Text style={{ color: '#666', fontSize: 14 }}>Đã chi</Text>
              <Text strong style={{ fontSize: 16, color: '#1a1a1a' }}>{spentPercentage}%</Text>
            </div>
            <Progress
              percent={parseFloat(spentPercentage)}
              showInfo={false}
              strokeColor={{
                '0%': '#4CAF50',
                '100%': '#FF5252',
              }}
              trailColor="#f0f0f0"
              strokeWidth={12}
              style={{ marginBottom: 12 }}
            />
            <Text style={{ color: '#999', display: 'block', textAlign: 'center', fontSize: 13 }}>
              {totalExpense.toLocaleString('vi-VN')} / {budgetLimit.toLocaleString('vi-VN')} VNĐ
            </Text>
          </Card>

          {/* Category Breakdown Card */}
          <Card
            style={{
              borderRadius: 16,
              border: 'none',
              marginBottom: 24,
              boxShadow: '0 2px 8px rgba(0,0,0,0.08)',
            }}
            bodyStyle={{ padding: 28 }}
          >
            <Title level={5} style={{ marginBottom: 28, fontSize: 18, fontWeight: 600, color: '#1a1a1a' }}>
              Phân loại chi tiêu
            </Title>

            {/* Pie Chart */}
            <div style={{ height: 300, marginBottom: 28 }}>
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={categoryData}
                    cx="50%"
                    cy="50%"
                    innerRadius={70}
                    outerRadius={110}
                    paddingAngle={3}
                    dataKey="value"
                    label={renderCustomLabel}
                    labelLine={false}
                  >
                    {categoryData.map((entry, index) => (
                      <Cell
                        key={`cell-${index}`}
                        fill={CATEGORY_COLORS[entry.name] || '#B4B4B4'}
                      />
                    ))}
                  </Pie>
                </PieChart>
              </ResponsiveContainer>
            </div>

            {/* Category List */}
            <div style={{ borderTop: '1px solid #f0f0f0', paddingTop: 16 }}>
              {categoryData.map((cat, index) => (
                <div
                  key={index}
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    padding: '12px 0',
                    borderBottom: index < categoryData.length - 1 ? '1px solid #f5f5f5' : 'none',
                  }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                    <div
                      style={{
                        width: 14,
                        height: 14,
                        borderRadius: '50%',
                        background: CATEGORY_COLORS[cat.name] || '#B4B4B4',
                      }}
                    />
                    <Text style={{ fontSize: 14, color: '#333' }}>{cat.name}</Text>
                  </div>
                  <Text strong style={{ fontSize: 14, color: '#1a1a1a' }}>{formatFullCurrency(cat.value)}</Text>
                </div>
              ))}
            </div>
          </Card>

          {/* AI Suggestion Card */}
          <Card
            style={{
              borderRadius: 16,
              background: 'linear-gradient(135deg, #FFF9E6 0%, #FFF3D6 100%)',
              border: 'none',
              boxShadow: '0 2px 8px rgba(255, 193, 7, 0.15)',
            }}
            bodyStyle={{ padding: 20 }}
          >
            <div style={{ display: 'flex', gap: 16 }}>
              <div
                style={{
                  width: 48,
                  height: 48,
                  borderRadius: 12,
                  background: 'linear-gradient(135deg, #FFD54F 0%, #FFC107 100%)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: 24,
                  flexShrink: 0,
                  boxShadow: '0 2px 8px rgba(255, 193, 7, 0.3)',
                }}
              >
                💡
              </div>
              <div style={{ flex: 1 }}>
                <Text style={{ display: 'block', marginBottom: 6, fontSize: 13, color: '#666' }}>
                  Hạng mục chi nhiều nhất
                </Text>
                <div style={{ marginBottom: 6 }}>
                  <Text strong style={{ color: '#E65100', fontSize: 18 }}>
                    {topCategory.name}
                  </Text>
                  <Text style={{ color: '#F57C00', marginLeft: 8, fontSize: 14 }}>
                    - {formatCurrency(topCategory.value)} đ
                  </Text>
                </div>
                <Text style={{ color: '#FF6F00', fontSize: 13 }}>
                  Chiếm {topCategory.percentage}% tổng chi tiêu
                </Text>
              </div>
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
};

export default DashboardPage;