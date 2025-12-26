import React, { useState } from 'react';
import { Modal, Select, Input, DatePicker, Button, Row, Col } from 'antd';
import { FilterOutlined } from '@ant-design/icons';

const AdvancedFilterModal = ({ visible, onClose, onApply }) => {
  const [filters, setFilters] = useState({
    bank: null,
    minTransactions: '',
    minSpending: '',
    registeredDate: null,
  });

  const bankOptions = [
    { label: 'Tất cả', value: null },
    { label: 'VCB', value: 'VCB' },
    { label: 'TCB', value: 'TCB' },
    { label: 'ACB', value: 'ACB' },
    { label: 'VTB', value: 'VTB' },
    { label: 'MBB', value: 'MBB' },
  ];

  const handleApply = () => {
    onApply(filters);
    onClose();
  };

  const handleReset = () => {
    const resetFilters = {
      bank: null,
      minTransactions: '',
      minSpending: '',
      registeredDate: null,
    };
    setFilters(resetFilters);
    onApply(resetFilters);
  };

  return (
    <Modal
      title={
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <FilterOutlined style={{ color: '#155dfc' }} />
          <span>Lọc nâng cao</span>
        </div>
      }
      open={visible}
      onCancel={onClose}
      footer={[
        <Button key="reset" onClick={handleReset}>
          Đặt lại
        </Button>,
        <Button key="cancel" onClick={onClose}>
          Hủy
        </Button>,
        <Button key="apply" type="primary" onClick={handleApply} style={{ background: '#155dfc' }}>
          Áp dụng
        </Button>,
      ]}
      width={500}
    >
      <div style={{ padding: '16px 0' }}>
        <Row gutter={[16, 24]}>
          <Col span={24}>
            <div style={{ marginBottom: 8 }}>
              <label style={{ fontSize: 14, fontWeight: 500, color: '#101828' }}>
                Ngân hàng
              </label>
            </div>
            <Select
              placeholder="VCB"
              value={filters.bank}
              onChange={(value) => setFilters({ ...filters, bank: value })}
              options={bankOptions}
              style={{ width: '100%', height: 40 }}
            />
          </Col>

          <Col span={24}>
            <div style={{ marginBottom: 8 }}>
              <label style={{ fontSize: 14, fontWeight: 500, color: '#101828' }}>
                Tổng giao dịch tối thiểu
              </label>
            </div>
            <Input
              placeholder="245"
              type="number"
              value={filters.minTransactions}
              onChange={(e) => setFilters({ ...filters, minTransactions: e.target.value })}
              style={{
                height: 40,
                background: '#f3f3f5',
                border: 'none',
                borderRadius: 8,
              }}
            />
          </Col>

          <Col span={24}>
            <div style={{ marginBottom: 8 }}>
              <label style={{ fontSize: 14, fontWeight: 500, color: '#101828' }}>
                Tổng chi tiêu tối thiểu (triệu)
              </label>
            </div>
            <Input
              placeholder="45"
              type="number"
              value={filters.minSpending}
              onChange={(e) => setFilters({ ...filters, minSpending: e.target.value })}
              style={{
                height: 40,
                background: '#f3f3f5',
                border: 'none',
                borderRadius: 8,
              }}
            />
          </Col>

          <Col span={24}>
            <div style={{ marginBottom: 8 }}>
              <label style={{ fontSize: 14, fontWeight: 500, color: '#101828' }}>
                Ngày đăng ký
              </label>
            </div>
            <DatePicker
              placeholder="15/03/2024"
              value={filters.registeredDate}
              onChange={(date) => setFilters({ ...filters, registeredDate: date })}
              format="DD/MM/YYYY"
              style={{
                width: '100%',
                height: 40,
                background: '#f3f3f5',
                border: 'none',
                borderRadius: 8,
              }}
            />
          </Col>
        </Row>
      </div>
    </Modal>
  );
};

export default AdvancedFilterModal;
