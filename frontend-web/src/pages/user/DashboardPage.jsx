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

// Màu sắc cho các danh mục
const CATEGORY_COLORS = {
  'Ăn uống': '#ef4444',
  'Di chuyển': '#f59e0b',
  'Mua sắm': '#8b5cf6',
  'Giải trí': '#ec4899',
  'Hóa đơn': '#06b6d4',
  'Khác': '#6b7280',
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
    <div style={{ display: 'flex', minHeight: '100vh', background: '#F5F7FA' }}>
      <Sidebar />

      {/* Main Content */}
      <div style={{ marginLeft: collapsed ? 80 : 280, flex: 1, transition: 'margin-left 0.3s' }}>
        {/* Header */}
        <Header title="Tổng quan tài chính" />

        {/* Content */}
        <div style={{ padding: 24 }}>
          {/* Summary Cards */}
          <Row gutter={16} style={{ marginBottom: 24 }}>
            <Col xs={24} sm={8}>
              <Card
                style={{
                  background: 'linear-gradient(135deg, #22c55e 0%, #16a34a 100%)',
                  border: 'none',
                  borderRadius: 14,
                }}
                bodyStyle={{ padding: 16 }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8 }}>
                  <ArrowUpOutlined style={{ color: 'rgba(255,255,255,0.9)', fontSize: 18 }} />
                  <Text style={{ color: 'rgba(255,255,255,0.9)', fontSize: 16 }}>Thu nhập</Text>
                </div>
                <Title level={4} style={{ color: '#fff', margin: 0, fontSize: 20 }}>
                  {formatCurrency(totalIncome)}
                </Title>
              </Card>
            </Col>
            <Col xs={24} sm={8}>
              <Card
                style={{
                  background: 'linear-gradient(135deg, #ef4444 0%, #dc2626 100%)',
                  border: 'none',
                  borderRadius: 14,
                }}
                bodyStyle={{ padding: 16 }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8 }}>
                  <ArrowDownOutlined style={{ color: 'rgba(255,255,255,0.9)', fontSize: 18 }} />
                  <Text style={{ color: 'rgba(255,255,255,0.9)', fontSize: 16 }}>Chi tiêu</Text>
                </div>
                <Title level={4} style={{ color: '#fff', margin: 0, fontSize: 20 }}>
                  {formatCurrency(totalExpense)}
                </Title>
              </Card>
            </Col>
            <Col xs={24} sm={8}>
              <Card
                style={{
                  background: 'linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%)',
                  border: 'none',
                  borderRadius: 14,
                }}
                bodyStyle={{ padding: 16 }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8 }}>
                  <WalletOutlined style={{ color: 'rgba(255,255,255,0.9)', fontSize: 18 }} />
                  <Text style={{ color: 'rgba(255,255,255,0.9)', fontSize: 16 }}>Còn lại</Text>
                </div>
                <Title level={4} style={{ color: '#fff', margin: 0, fontSize: 20 }}>
                  {formatCurrency(remaining)}
                </Title>
              </Card>
            </Col>
          </Row>

          {/* Spending Progress Card */}
          <Card
            style={{
              borderRadius: 14,
              border: '1px solid rgba(0,0,0,0.1)',
              marginBottom: 24,
            }}
            bodyStyle={{ padding: 24 }}
          >
            <Title level={5} style={{ marginBottom: 24 }}>
              Tình hình chi tiêu tháng này
            </Title>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
              <Text style={{ color: '#4a5565' }}>Đã chi</Text>
              <Text strong>{spentPercentage}%</Text>
            </div>
            <Progress
              percent={parseFloat(spentPercentage)}
              showInfo={false}
              strokeColor={{
                '0%': '#2b7fff',
                '100%': '#615fff',
              }}
              trailColor="#e5e7eb"
              style={{ marginBottom: 8 }}
            />
            <Text style={{ color: '#6a7282', display: 'block', textAlign: 'center' }}>
              {totalExpense.toLocaleString('vi-VN')} / {budgetLimit.toLocaleString('vi-VN')} VNĐ
            </Text>
          </Card>

          {/* Category Breakdown Card */}
          <Card
            style={{
              borderRadius: 14,
              border: '1px solid rgba(0,0,0,0.1)',
              marginBottom: 24,
            }}
            bodyStyle={{ padding: 24 }}
          >
            <Title level={5} style={{ marginBottom: 24 }}>
              Phân loại chi tiêu
            </Title>

            {/* Pie Chart */}
            <div style={{ height: 280, marginBottom: 24 }}>
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={categoryData}
                    cx="50%"
                    cy="50%"
                    innerRadius={60}
                    outerRadius={100}
                    paddingAngle={2}
                    dataKey="value"
                    label={renderCustomLabel}
                    labelLine={false}
                  >
                    {categoryData.map((entry, index) => (
                      <Cell
                        key={`cell-${index}`}
                        fill={CATEGORY_COLORS[entry.name] || '#6b7280'}
                      />
                    ))}
                  </Pie>
                </PieChart>
              </ResponsiveContainer>
            </div>

            {/* Category List */}
            <div>
              {categoryData.map((cat, index) => (
                <div
                  key={index}
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    padding: '8px 0',
                  }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                    <div
                      style={{
                        width: 12,
                        height: 12,
                        borderRadius: '50%',
                        background: CATEGORY_COLORS[cat.name] || '#6b7280',
                      }}
                    />
                    <Text>{cat.name}</Text>
                  </div>
                  <Text strong>{formatFullCurrency(cat.value)}</Text>
                </div>
              ))}
            </div>
          </Card>

          {/* AI Suggestion Card */}
          <Card
            style={{
              borderRadius: 14,
              background: 'linear-gradient(135deg, #fffbeb 0%, #fff7ed 100%)',
              border: '1px solid #fee685',
            }}
            bodyStyle={{ padding: 16 }}
          >
            <div style={{ display: 'flex', gap: 12 }}>
              <div
                style={{
                  width: 40,
                  height: 40,
                  borderRadius: 10,
                  background: '#fe9a00',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: 18,
                }}
              >
                💡
              </div>
              <div>
                <Text style={{ display: 'block', marginBottom: 4 }}>
                  Hạng mục chi nhiều nhất
                </Text>
                <div style={{ marginBottom: 4 }}>
                  <Text strong style={{ color: '#7b3306', fontSize: 16 }}>
                    {topCategory.name}
                  </Text>
                  <Text style={{ color: '#7b3306', marginLeft: 8 }}>
                    - {formatCurrency(topCategory.value)} đ
                  </Text>
                </div>
                <Text style={{ color: '#bb4d00', fontSize: 12 }}>
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
