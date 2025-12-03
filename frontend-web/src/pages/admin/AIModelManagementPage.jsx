import React, { useState, useEffect } from 'react';
import { Button, Space, message, Spin, Tag } from 'antd';
import {
  UploadOutlined,
  ThunderboltOutlined,
  RobotOutlined,
  CheckCircleOutlined,
  AimOutlined,
  WarningOutlined,
  SettingOutlined,
  SyncOutlined,
  CloseOutlined,
  LoadingOutlined,
} from '@ant-design/icons';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend } from 'recharts';
import AdminSidebar from '../../components/admin/AdminSidebar';
import { useSidebar } from '../../contexts/SidebarContext';
import aiService from '../../services/aiService';

const AIModelManagementPage = () => {
  const { collapsed } = useSidebar();
  
  // State
  const [loading, setLoading] = useState(true);
  const [retraining, setRetraining] = useState(false);
  const [retrainingModel, setRetrainingModel] = useState(null); // Track which model is retraining
  const [stats, setStats] = useState(null);
  const [models, setModels] = useState([]);
  const [predictionLogs, setPredictionLogs] = useState([]);
  const [logsFromApi, setLogsFromApi] = useState(false);
  const [modelsFromApi, setModelsFromApi] = useState(false); // Track if models data is from API
  const [accuracyHistory, setAccuracyHistory] = useState([]);

  // Fetch data on mount
  useEffect(() => {
    fetchData();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  

  const fetchData = async () => {
    setLoading(true);
    try {
      const [statsRes, modelsRes, logsRes, historyRes] = await Promise.all([
        aiService.getAdminStats(),
        aiService.getModels(),
        aiService.getPredictionLogs({ page: 1, page_size: 10 }),
        aiService.getAccuracyHistory(7),
      ]);
      
      setStats(statsRes);
      setModels(modelsRes.models || []);
      setModelsFromApi(Array.isArray(modelsRes?.models) && modelsRes.models.length > 0);
      setPredictionLogs(logsRes.logs || []);
      setLogsFromApi(Array.isArray(logsRes?.logs) && logsRes.logs.length > 0);
      
      // Transform accuracy history data for chart
      if (historyRes && historyRes.models) {
        const chartData = transformAccuracyData(historyRes.models);
        setAccuracyHistory(chartData);
      }
    } catch (error) {
      console.error('Error fetching data:', error);
      message.error('Không thể kết nối tới AI Backend. Đang sử dụng dữ liệu mẫu.');
      // Fallback to mock data
      setStats({
        active_models: 3,
        avg_accuracy: 90.4,
        predictions_today: 2100,
        low_confidence_count: 234,
      });
      setModels([
        {
          info: { name: 'Category Classification', version: 'v2.1.3', status: 'Active', last_trained: '2025-11-20T10:30:00' },
          metrics: { accuracy: 94.2, confidence: 92.1, total_predictions: 45234, predictions_today: 1523 },
        },
        {
          info: { name: 'Anomaly Detection', version: 'v1.8.5', status: 'Active', last_trained: '2025-11-18T14:00:00' },
          metrics: { accuracy: 89.7, confidence: 88.3, total_predictions: 12543, predictions_today: 421 },
        },
        {
          info: { name: 'Spending Prediction', version: 'v1.5.2', status: 'Training', last_trained: '2025-11-15T09:00:00' },
          metrics: { accuracy: 87.3, confidence: 85.6, total_predictions: 8392, predictions_today: 156 },
        },
      ]);
      setModelsFromApi(false);
      setPredictionLogs([
        { id: 1, timestamp: '2025-11-29T10:45:00', user_id: 'USR001', input_text: 'GRAB VIETNAM', predicted_category: 'Di chuyển', confidence: 96.5, actual_category: 'Di chuyển', is_correct: true },
        { id: 2, timestamp: '2025-11-29T10:32:00', user_id: 'USR002', input_text: 'SHOPEE', predicted_category: 'Mua sắm', confidence: 89.2, actual_category: 'Mua sắm', is_correct: true },
        { id: 3, timestamp: '2025-11-29T10:18:00', user_id: 'USR003', input_text: 'HIGHLANDS COFFEE', predicted_category: 'Ăn uống', confidence: 62.8, actual_category: 'Giải trí', is_correct: false },
        { id: 4, timestamp: '2025-11-29T10:05:00', user_id: 'USR004', input_text: 'CGV CINEMA', predicted_category: 'Giải trí', confidence: 95.1, actual_category: 'Giải trí', is_correct: true },
      ]);
      setLogsFromApi(false);
      // Mock accuracy history data
      setAccuracyHistory(generateMockAccuracyData());
    } finally {
      setLoading(false);
    }
  };

  

  // Transform API accuracy data to chart format
  const transformAccuracyData = (modelsData) => {
    const dateMap = {};

    const parseDateString = (s) => {
      if (!s) return null;
      // Try native parse first (ISO, full timestamp, etc.)
      const native = new Date(s);
      if (!isNaN(native.getTime())) return native;

      // Try DD/MM or DD/MM/YYYY or DD-MM[-YYYY]
      const m = String(s).trim().match(new RegExp('^(\\d{1,2})[\\/\\-](\\d{1,2})(?:[\\/\\-](\\d{2,4}))?$'));
      if (m) {
        const day = parseInt(m[1], 10);
        const month = parseInt(m[2], 10) - 1;
        let year = m[3] ? parseInt(m[3], 10) : new Date().getFullYear();
        if (year < 100) year += 2000;
        return new Date(year, month, day);
      }

      // Fallback: return null so caller can decide
      return null;
    };

    modelsData.forEach(model => {
      const modelKey = model.model_name.replace(/\s+/g, '_').toLowerCase();
      (model.history || []).forEach(point => {
        let dateObj = parseDateString(point.date);
        if (!dateObj) {
          // If parsing failed, attempt to use created_at / timestamp fields commonly returned by API
          if (point.timestamp) dateObj = parseDateString(point.timestamp);
        }
        if (!dateObj) {
          // As a last resort use today so chart still renders
          dateObj = new Date();
        }

        const day = dateObj.getDate().toString().padStart(2, '0');
        const month = (dateObj.getMonth() + 1).toString().padStart(2, '0');
        const dateStr = `${day}/${month}`;

        if (!dateMap[dateStr]) {
          dateMap[dateStr] = { date: dateStr, sortKey: dateObj.getTime() };
        }
        // Ensure value is numeric
        const val = typeof point.accuracy === 'number' ? point.accuracy : Number(point.accuracy);
        dateMap[dateStr][modelKey] = isNaN(val) ? null : val;
      });
    });

    return Object.values(dateMap).sort((a, b) => a.sortKey - b.sortKey);
  };

  // Generate mock accuracy data for fallback
  const generateMockAccuracyData = () => {
    const data = [];
    const today = new Date();
    for (let i = 6; i >= 0; i--) {
      const date = new Date(today);
      date.setDate(date.getDate() - i);
      const day = date.getDate().toString().padStart(2, '0');
      const month = (date.getMonth() + 1).toString().padStart(2, '0');
      data.push({
        date: `${day}/${month}`,
        category_classification: 92 + Math.random() * 4,
        anomaly_detection: 87 + Math.random() * 5,
        spending_prediction: 84 + Math.random() * 6,
      });
    }
    return data;
  };

  const handleRetrain = async () => {
    setRetraining(true);
    try {
      const result = await aiService.retrainModels({ force: true });
      if (result.success) {
        message.success('Đã retrain tất cả models thành công!');
        fetchData(); // Refresh data
      } else {
        message.warning(result.message);
      }
    } catch {
      message.error('Lỗi khi retrain models');
    } finally {
      setRetraining(false);
    }
  };

  const handleRetrainSingle = async (modelName) => {
    setRetrainingModel(modelName);
    try {
      const result = await aiService.retrainSingleModel(modelName, true);
      if (result.status === 'completed') {
        message.success(`Đã retrain ${modelName} thành công! Accuracy: ${result.old_accuracy}% → ${result.new_accuracy}%`);
        fetchData(); // Refresh data
      } else if (result.status === 'skipped') {
        message.info(result.message);
      } else {
        message.warning(result.message);
      }
    } catch {
      message.error(`Lỗi khi retrain ${modelName}`);
    } finally {
      setRetrainingModel(null);
    }
  };

  const getConfidenceColor = (value) => {
    if (value >= 90) return '#00A63E';
    if (value >= 80) return '#D08700';
    return '#E7000B';
  };

  const getStatusBadge = (status) => {
    const styles = {
      Active: { bg: '#DCFCE7', color: '#008236' },
      Training: { bg: '#FEF9C2', color: '#A65F00' },
      Inactive: { bg: '#F3F4F6', color: '#6B7280' },
      Error: { bg: '#FFE2E2', color: '#C10007' },
    };
    const style = styles[status] || styles.Active;
    return (
      <span
        style={{
          display: 'inline-block',
          padding: '2px 8px',
          borderRadius: 8,
          fontSize: 12,
          background: style.bg,
          color: style.color,
          fontFamily: 'Arimo, sans-serif',
        }}
      >
        {status}
      </span>
    );
  };

  const formatDate = (dateString) => {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });
  };

  const formatTime = (dateString) => {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
  };

  const formatNumber = (num) => {
    if (num === undefined || num === null) return '-';
    return num.toLocaleString('vi-VN');
  };

  // Stats cards data
  const statsCards = [
    {
      label: 'Active Models',
      value: stats?.active_models || 0,
      icon: <RobotOutlined style={{ fontSize: 24, color: '#8B5CF6' }} />,
      bgColor: '#F3E8FF',
    },
    {
      label: 'Avg Accuracy',
      value: `${stats?.avg_accuracy?.toFixed(1) || 0}%`,
      icon: <CheckCircleOutlined style={{ fontSize: 24, color: '#22C55E' }} />,
      bgColor: '#DCFCE7',
    },
    {
      label: 'Predictions Today',
      value: formatNumber(stats?.predictions_today),
      icon: <AimOutlined style={{ fontSize: 24, color: '#3B82F6' }} />,
      bgColor: '#DBEAFE',
    },
    {
      label: 'Low Confidence',
      value: formatNumber(stats?.low_confidence_count),
      icon: <WarningOutlined style={{ fontSize: 24, color: '#EAB308' }} />,
      bgColor: '#FEF9C2',
    },
  ];

  if (loading) {
    return (
      <div style={{ display: 'flex', minHeight: '100vh', background: '#F9FAFB' }}>
        <AdminSidebar />
        <div
          style={{
            marginLeft: collapsed ? 80 : 288,
            flex: 1,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
          }}
        >
          <Spin indicator={<LoadingOutlined style={{ fontSize: 48 }} spin />} />
        </div>
      </div>
    );
  }

  return (
    <div style={{ display: 'flex', minHeight: '100vh', background: '#F9FAFB' }}>
      <AdminSidebar />
      <div
        style={{
          marginLeft: collapsed ? 80 : 288,
          flex: 1,
          transition: 'margin-left 0.3s',
          minHeight: '100vh',
          padding: '32px',
          overflow: 'auto',
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
            <h1
              style={{
                fontSize: 24,
                fontWeight: 400,
                color: '#101828',
                margin: 0,
                marginBottom: 4,
                fontFamily: 'Arimo, sans-serif',
              }}
            >
              Quản lý AI Model
            </h1>
            <p
              style={{
                fontSize: 16,
                color: '#6A7282',
                margin: 0,
                fontFamily: 'Arimo, sans-serif',
              }}
            >
              Model training, monitoring và feedback loop
            </p>
          </div>
          <Space size={12}>
            <Button
              icon={<UploadOutlined />}
              style={{
                height: 36,
                borderRadius: 8,
                fontFamily: 'Arimo, sans-serif',
              }}
            >
              Upload Training Data
            </Button>
            <Button
              type="primary"
              icon={retraining ? <LoadingOutlined /> : <ThunderboltOutlined />}
              loading={retraining}
              onClick={handleRetrain}
              style={{
                height: 36,
                borderRadius: 8,
                background: '#155DFC',
                fontFamily: 'Arimo, sans-serif',
              }}
            >
              Re-train Models
            </Button>
          </Space>
        </div>

        {/* Stats Cards */}
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(4, 1fr)',
            gap: 24,
            marginBottom: 24,
          }}
        >
          {statsCards.map((stat) => (
            <div
              key={stat.label}
              style={{
                background: '#FFFFFF',
                border: '1px solid rgba(0,0,0,0.1)',
                borderRadius: 14,
                padding: 24,
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                <div
                  style={{
                    width: 48,
                    height: 48,
                    borderRadius: 10,
                    background: stat.bgColor,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                  }}
                >
                  {stat.icon}
                </div>
                <div>
                  <p
                    style={{
                      fontSize: 14,
                      color: '#4A5565',
                      margin: 0,
                      fontFamily: 'Arimo, sans-serif',
                    }}
                  >
                    {stat.label}
                  </p>
                  <p
                    style={{
                      fontSize: 16,
                      color: '#101828',
                      margin: 0,
                      fontFamily: 'Arimo, sans-serif',
                    }}
                  >
                    {stat.value}
                  </p>
                </div>
              </div>
            </div>
          ))}
        </div>

        {/* Model Accuracy Trend Chart */}
        <div
          style={{
            background: '#FFFFFF',
            border: '1px solid rgba(0,0,0,0.1)',
            borderRadius: 14,
            padding: 24,
            marginBottom: 24,
          }}
        >
          <h3
            style={{
              fontSize: 18,
              fontWeight: 400,
              color: '#101828',
              margin: 0,
              marginBottom: 24,
              fontFamily: 'Arimo, sans-serif',
            }}
          >
            Model Accuracy Trend (7 ngày)
          </h3>
          {/* Real Chart */}
          <div style={{ height: 280, marginBottom: 16 }}>
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={accuracyHistory} margin={{ top: 5, right: 30, left: 0, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#E5E7EB" />
                <XAxis 
                  dataKey="date" 
                  tick={{ fontSize: 12, fill: '#6A7282', fontFamily: 'Arimo, sans-serif' }}
                  axisLine={{ stroke: '#E5E7EB' }}
                  tickLine={false}
                />
                <YAxis 
                  domain={[80, 100]} 
                  tick={{ fontSize: 12, fill: '#6A7282', fontFamily: 'Arimo, sans-serif' }}
                  axisLine={{ stroke: '#E5E7EB' }}
                  tickLine={false}
                  tickFormatter={(value) => `${value}%`}
                />
                <Tooltip 
                  contentStyle={{ 
                    background: '#fff', 
                    border: '1px solid #E5E7EB', 
                    borderRadius: 8,
                    fontFamily: 'Arimo, sans-serif',
                  }}
                  formatter={(value) => {
                    if (value === null || value === undefined || Number.isNaN(value)) return ['-', ''];
                    const num = Number(value);
                    if (Number.isNaN(num)) return ['-', ''];
                    return [`${num.toFixed(1)}%`, ''];
                  }}
                  labelStyle={{ fontWeight: 500, marginBottom: 4 }}
                />
                <Legend 
                  verticalAlign="bottom" 
                  height={36}
                  iconType="circle"
                  formatter={(value) => {
                    const labels = {
                      category_classification: 'Category Classification',
                      anomaly_detection: 'Anomaly Detection',
                      spending_prediction: 'Spending Prediction',
                    };
                    return <span style={{ color: '#374151', fontFamily: 'Arimo, sans-serif' }}>{labels[value] || value}</span>;
                  }}
                />
                <Line 
                  type="monotone" 
                  dataKey="category_classification" 
                  stroke="#8B5CF6" 
                  strokeWidth={2}
                  dot={{ fill: '#8B5CF6', strokeWidth: 0, r: 4 }}
                  activeDot={{ r: 6, strokeWidth: 0 }}
                  name="category_classification"
                />
                <Line 
                  type="monotone" 
                  dataKey="anomaly_detection" 
                  stroke="#3B82F6" 
                  strokeWidth={2}
                  dot={{ fill: '#3B82F6', strokeWidth: 0, r: 4 }}
                  activeDot={{ r: 6, strokeWidth: 0 }}
                  name="anomaly_detection"
                />
                <Line 
                  type="monotone" 
                  dataKey="spending_prediction" 
                  stroke="#10B981" 
                  strokeWidth={2}
                  dot={{ fill: '#10B981', strokeWidth: 0, r: 4 }}
                  activeDot={{ r: 6, strokeWidth: 0 }}
                  name="spending_prediction"
                />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* AI Models Status Table */}
        <div
          style={{
            background: '#FFFFFF',
            border: '1px solid rgba(0,0,0,0.1)',
            borderRadius: 14,
            marginBottom: 24,
            overflow: 'hidden',
          }}
        >
          <div style={{ padding: 24, borderBottom: '1px solid rgba(0,0,0,0.1)', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <h3
              style={{
                fontSize: 18,
                fontWeight: 400,
                color: '#101828',
                margin: 0,
                fontFamily: 'Arimo, sans-serif',
              }}
            >
              AI Models Status
            </h3>
            <div>
              {modelsFromApi ? (
                <Tag color="green" style={{ fontFamily: 'Arimo, sans-serif' }}>Live</Tag>
              ) : (
                <Tag color="default" style={{ fontFamily: 'Arimo, sans-serif' }}>Mock</Tag>
              )}
            </div>
          </div>
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr style={{ borderBottom: '1px solid rgba(0,0,0,0.1)' }}>
                {['Model Name', 'Version', 'Accuracy', 'Confidence', 'Last Trained', 'Predictions', 'Status', 'Actions'].map((header, idx) => (
                  <th
                    key={header}
                    style={{
                      padding: '10px 8px',
                      textAlign: idx === 7 ? 'right' : 'left',
                      fontSize: 14,
                      fontWeight: 400,
                      color: '#0A0A0A',
                      fontFamily: 'Arimo, sans-serif',
                    }}
                  >
                    {header}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {models.map((model, index) => (
                <tr
                  key={model.info?.name || index}
                  style={{
                    borderBottom: index < models.length - 1 ? '1px solid rgba(0,0,0,0.1)' : 'none',
                  }}
                >
                  <td style={{ padding: '13px 8px', fontSize: 14, color: '#0A0A0A', fontFamily: 'Arimo, sans-serif' }}>
                    {model.info?.name}
                  </td>
                  <td style={{ padding: '13px 8px' }}>
                    <span
                      style={{
                        display: 'inline-block',
                        padding: '2px 8px',
                        borderRadius: 8,
                        fontSize: 12,
                        background: '#F3F4F6',
                        color: '#364153',
                        fontFamily: 'Arimo, sans-serif',
                      }}
                    >
                      {model.info?.version}
                    </span>
                  </td>
                  <td style={{ padding: '13px 8px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                      <div style={{ width: 64, height: 8, background: '#E5E7EB', borderRadius: 999 }}>
                        <div
                          style={{
                            width: `${model.metrics?.accuracy || 0}%`,
                            height: '100%',
                            background: '#00C950',
                            borderRadius: 999,
                          }}
                        />
                      </div>
                      <span style={{ fontSize: 14, fontFamily: 'Arimo, sans-serif' }}>{model.metrics?.accuracy?.toFixed(1)}%</span>
                    </div>
                  </td>
                  <td style={{ padding: '13px 8px', fontSize: 14, fontFamily: 'Arimo, sans-serif' }}>
                    {model.metrics?.confidence?.toFixed(1)}%
                  </td>
                  <td style={{ padding: '13px 8px', fontSize: 14, fontFamily: 'Arimo, sans-serif' }}>
                    {formatDate(model.info?.last_trained)}
                  </td>
                  <td style={{ padding: '13px 8px', fontSize: 14, fontFamily: 'Arimo, sans-serif' }}>
                    {formatNumber(model.metrics?.total_predictions)}
                  </td>
                  <td style={{ padding: '13px 8px' }}>{getStatusBadge(model.info?.status)}</td>
                  <td style={{ padding: '13px 8px', textAlign: 'right' }}>
                    <Space size={8}>
                      <Button
                        type="text"
                        icon={<SettingOutlined style={{ fontSize: 16, color: '#6B7280' }} />}
                        style={{ width: 36, height: 32, padding: 0 }}
                        title="Cài đặt model"
                      />
                      <Button
                        type="text"
                        icon={retrainingModel === model.info?.name ? <LoadingOutlined style={{ fontSize: 16, color: '#155DFC' }} spin /> : <SyncOutlined style={{ fontSize: 16, color: '#6B7280' }} />}
                        style={{ width: 36, height: 32, padding: 0 }}
                        onClick={() => handleRetrainSingle(model.info?.name)}
                        disabled={retrainingModel !== null}
                        title={`Retrain ${model.info?.name}`}
                      />
                    </Space>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Prediction Log (Real-time) */}
        <div
          style={{
            background: '#FFFFFF',
            border: '1px solid rgba(0,0,0,0.1)',
            borderRadius: 14,
            overflow: 'hidden',
          }}
        >
          <div style={{ padding: 24, borderBottom: '1px solid rgba(0,0,0,0.1)', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <h3
              style={{
                fontSize: 18,
                fontWeight: 400,
                color: '#101828',
                margin: 0,
                fontFamily: 'Arimo, sans-serif',
              }}
            >
              Prediction Log (Real-time)
            </h3>
            <div>
              {logsFromApi ? (
                <Tag color="green" style={{ fontFamily: 'Arimo, sans-serif' }}>Live</Tag>
              ) : (
                <Tag color="default" style={{ fontFamily: 'Arimo, sans-serif' }}>Mock</Tag>
              )}
            </div>
          </div>
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr style={{ borderBottom: '1px solid rgba(0,0,0,0.1)' }}>
                {['Time', 'User', 'Input', 'Predicted', 'Confidence', 'Actual', 'Result'].map((header) => (
                  <th
                    key={header}
                    style={{
                      padding: '10px 8px',
                      textAlign: 'left',
                      fontSize: 14,
                      fontWeight: 400,
                      color: '#0A0A0A',
                      fontFamily: 'Arimo, sans-serif',
                    }}
                  >
                    {header}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {predictionLogs.map((log, index) => (
                <tr
                  key={log.id || index}
                  style={{
                    borderBottom: index < predictionLogs.length - 1 ? '1px solid rgba(0,0,0,0.1)' : 'none',
                  }}
                >
                  <td style={{ padding: '10px 8px', fontSize: 14, color: '#6A7282', fontFamily: 'Arimo, sans-serif' }}>
                    {formatTime(log.timestamp)}
                  </td>
                  <td style={{ padding: '10px 8px', fontSize: 14, color: '#155DFC', fontFamily: 'Arimo, sans-serif' }}>
                    {log.user_id}
                  </td>
                  <td style={{ padding: '10px 8px', fontSize: 14, color: '#0A0A0A', fontFamily: 'Cousine, monospace' }}>
                    {log.input_text}
                  </td>
                  <td style={{ padding: '10px 8px' }}>
                    <span
                      style={{
                        display: 'inline-block',
                        padding: '2px 8px',
                        borderRadius: 8,
                        fontSize: 12,
                        background: '#F3E8FF',
                        color: '#8200DB',
                        fontFamily: 'Arimo, sans-serif',
                      }}
                    >
                      {log.predicted_category}
                    </span>
                  </td>
                  <td
                    style={{
                      padding: '10px 8px',
                      fontSize: 14,
                      color: getConfidenceColor(log.confidence),
                      fontFamily: 'Arimo, sans-serif',
                    }}
                  >
                    {log.confidence?.toFixed(1)}%
                  </td>
                  <td style={{ padding: '10px 8px' }}>
                    {log.actual_category ? (
                      <span
                        style={{
                          display: 'inline-block',
                          padding: '2px 8px',
                          borderRadius: 8,
                          fontSize: 12,
                          background: '#DBEAFE',
                          color: '#1447E6',
                          fontFamily: 'Arimo, sans-serif',
                        }}
                      >
                        {log.actual_category}
                      </span>
                    ) : (
                      <span style={{ color: '#9CA3AF', fontSize: 12 }}>-</span>
                    )}
                  </td>
                  <td style={{ padding: '10px 8px' }}>
                    {log.is_correct === true ? (
                      <CheckCircleOutlined style={{ fontSize: 20, color: '#22C55E' }} />
                    ) : log.is_correct === false ? (
                      <CloseOutlined style={{ fontSize: 20, color: '#EF4444' }} />
                    ) : (
                      <span style={{ color: '#9CA3AF', fontSize: 12 }}>-</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default AIModelManagementPage;
