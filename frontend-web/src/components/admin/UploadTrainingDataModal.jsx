import React, { useState } from 'react';
import { Modal, Upload, Select, message, Progress } from 'antd';
import { CloseOutlined, InboxOutlined, FileTextOutlined, CheckCircleOutlined, DownloadOutlined } from '@ant-design/icons';

const { Dragger } = Upload;

const modelOptions = [
  { value: 'Category Classification', label: 'Category Classification' },
  { value: 'Anomaly Detection', label: 'Anomaly Detection' },
  { value: 'Spending Prediction', label: 'Spending Prediction' },
  { value: 'SMS Parser', label: 'SMS Parser' },
];

// Sample data files mapping
const sampleDataFiles = {
  'Category Classification': '/sample-data/category_classifier_training_data.csv',
  'Anomaly Detection': '/sample-data/anomaly_detector_training_data.csv',
  'Spending Prediction': '/sample-data/category_classifier_training_data.csv',
  'SMS Parser': '/sample-data/sms_parser_training_data.csv',
};

const UploadTrainingDataModal = ({
  open,
  onClose,
  onUpload,
  loading = false,
}) => {
  const [selectedModel, setSelectedModel] = useState('Category Classification');
  const [fileList, setFileList] = useState([]);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [uploadSuccess, setUploadSuccess] = useState(false);
  const [uploadResult, setUploadResult] = useState(null);

  const handleClose = () => {
    setFileList([]);
    setUploadProgress(0);
    setUploadSuccess(false);
    setUploadResult(null);
    setSelectedModel('Category Classification');
    onClose();
  };

  const handleDownloadSample = () => {
    const sampleUrl = sampleDataFiles[selectedModel];
    if (sampleUrl) {
      const link = document.createElement('a');
      link.href = sampleUrl;
      link.download = sampleUrl.split('/').pop();
      link.click();
    }
  };

  const handleUpload = async () => {
    if (fileList.length === 0) {
      message.error('Vui lòng chọn file để upload');
      return;
    }

    const file = fileList[0].originFileObj || fileList[0];
    
    // Simulate progress
    setUploadProgress(0);
    const progressInterval = setInterval(() => {
      setUploadProgress((prev) => {
        if (prev >= 90) {
          clearInterval(progressInterval);
          return prev;
        }
        return prev + 10;
      });
    }, 200);

    try {
      const result = await onUpload(file, selectedModel);
      clearInterval(progressInterval);
      setUploadProgress(100);
      setUploadSuccess(true);
      setUploadResult(result);
      message.success(`Upload thành công! Đã thêm ${result?.records_count || 0} records.`);
      
      // Auto close after 2s
      setTimeout(() => {
        handleClose();
      }, 2000);
    } catch (error) {
      clearInterval(progressInterval);
      setUploadProgress(0);
      message.error(error.message || 'Upload thất bại');
    }
  };

  const uploadProps = {
    name: 'file',
    multiple: false,
    accept: '.csv,.json',
    fileList,
    beforeUpload: (file) => {
      const isValidType = file.type === 'text/csv' || 
                          file.type === 'application/json' ||
                          file.name.endsWith('.csv') ||
                          file.name.endsWith('.json');
      
      if (!isValidType) {
        message.error('Chỉ hỗ trợ file CSV hoặc JSON!');
        return Upload.LIST_IGNORE;
      }

      const isLt10M = file.size / 1024 / 1024 < 10;
      if (!isLt10M) {
        message.error('File phải nhỏ hơn 10MB!');
        return Upload.LIST_IGNORE;
      }

      setFileList([file]);
      setUploadSuccess(false);
      setUploadProgress(0);
      return false; // Prevent auto upload
    },
    onRemove: () => {
      setFileList([]);
      setUploadSuccess(false);
      setUploadProgress(0);
    },
  };

  return (
    <Modal
      open={open}
      onCancel={handleClose}
      footer={null}
      closable={false}
      centered
      width={520}
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
          onClick={handleClose}
          style={{
            position: 'absolute',
            right: 16,
            top: 16,
            width: 24,
            height: 24,
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
        <div style={{ marginBottom: 20 }}>
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
            Upload Training Data
          </h2>
          <p
            style={{
              fontSize: 14,
              color: '#717182',
              margin: 0,
              fontFamily: 'Arimo, sans-serif',
            }}
          >
            Upload file CSV hoặc JSON để train AI model
          </p>
        </div>

        {/* Model Selection */}
        <div style={{ marginBottom: 20 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
            <label
              style={{
                fontSize: 14,
                color: '#0A0A0A',
                fontFamily: 'Arimo, sans-serif',
              }}
            >
              Chọn Model
            </label>
            <button
              onClick={handleDownloadSample}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: 4,
                background: 'none',
                border: 'none',
                color: '#155DFC',
                fontSize: 13,
                cursor: 'pointer',
                fontFamily: 'Arimo, sans-serif',
              }}
            >
              <DownloadOutlined /> Tải file mẫu
            </button>
          </div>
          <Select
            value={selectedModel}
            onChange={setSelectedModel}
            options={modelOptions}
            style={{ width: '100%', height: 40 }}
            classNames={{ popup: { root: 'custom-select-popup' } }}
          />
        </div>

        {/* Upload Area */}
        <div style={{ marginBottom: 20 }}>
          <Dragger
            {...uploadProps}
            style={{
              background: '#FAFAFA',
              border: '1px dashed #D9D9D9',
              borderRadius: 8,
            }}
          >
            {uploadSuccess ? (
              <div style={{ padding: '20px 0' }}>
                <CheckCircleOutlined style={{ fontSize: 48, color: '#52C41A', marginBottom: 16 }} />
                <p style={{ fontSize: 16, color: '#0A0A0A', margin: 0, fontFamily: 'Arimo, sans-serif', fontWeight: 600 }}>
                  Upload thành công!
                </p>
                {uploadResult && (
                  <p style={{ fontSize: 13, color: '#717182', margin: '8px 0 0', fontFamily: 'Arimo, sans-serif' }}>
                    Đã thêm {uploadResult.records_count} records (Tổng: {uploadResult.total_records})
                  </p>
                )}
              </div>
            ) : (
              <>
                <p className="ant-upload-drag-icon">
                  <InboxOutlined style={{ fontSize: 48, color: '#155DFC' }} />
                </p>
                <p
                  style={{
                    fontSize: 14,
                    color: '#0A0A0A',
                    margin: '8px 0',
                    fontFamily: 'Arimo, sans-serif',
                  }}
                >
                  Kéo thả file vào đây hoặc click để chọn
                </p>
                <p
                  style={{
                    fontSize: 12,
                    color: '#717182',
                    margin: 0,
                    fontFamily: 'Arimo, sans-serif',
                  }}
                >
                  Hỗ trợ: CSV, JSON (max 10MB)
                </p>
              </>
            )}
          </Dragger>
        </div>

        {/* Selected File Info */}
        {fileList.length > 0 && !uploadSuccess && (
          <div
            style={{
              background: '#F5F5F5',
              borderRadius: 8,
              padding: 12,
              marginBottom: 20,
              display: 'flex',
              alignItems: 'center',
              gap: 12,
            }}
          >
            <FileTextOutlined style={{ fontSize: 24, color: '#155DFC' }} />
            <div style={{ flex: 1 }}>
              <p
                style={{
                  fontSize: 14,
                  color: '#0A0A0A',
                  margin: 0,
                  fontFamily: 'Arimo, sans-serif',
                }}
              >
                {fileList[0].name}
              </p>
              <p
                style={{
                  fontSize: 12,
                  color: '#717182',
                  margin: 0,
                  fontFamily: 'Arimo, sans-serif',
                }}
              >
                {(fileList[0].size / 1024).toFixed(1)} KB
              </p>
            </div>
          </div>
        )}

        {/* Progress Bar */}
        {uploadProgress > 0 && !uploadSuccess && (
          <div style={{ marginBottom: 20 }}>
            <Progress 
              percent={uploadProgress} 
              status={uploadProgress === 100 ? 'success' : 'active'}
              strokeColor="#155DFC"
            />
          </div>
        )}

        {/* Data Format Guide */}
        <div
          style={{
            background: '#F0F9FF',
            borderRadius: 8,
            padding: 12,
            marginBottom: 20,
          }}
        >
          <p
            style={{
              fontSize: 13,
              color: '#0369A1',
              margin: 0,
              fontFamily: 'Arimo, sans-serif',
              fontWeight: 500,
              marginBottom: 8,
            }}
          >
            📋 Định dạng dữ liệu:
          </p>
          <p
            style={{
              fontSize: 12,
              color: '#0369A1',
              margin: 0,
              fontFamily: 'Cousine, monospace',
              lineHeight: 1.6,
            }}
          >
            CSV: input_text, category, amount<br />
            JSON: [{'{'}input_text, category, amount{'}'}]
          </p>
        </div>

        {/* Footer Buttons */}
        <div
          style={{
            display: 'flex',
            justifyContent: 'flex-end',
            gap: 8,
          }}
        >
          <button
            onClick={handleClose}
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
            onClick={handleUpload}
            disabled={loading || fileList.length === 0 || uploadSuccess}
            style={{
              height: 36,
              padding: '0 16px',
              borderRadius: 8,
              border: 'none',
              background: loading || fileList.length === 0 || uploadSuccess ? '#D1D5DB' : '#155DFC',
              color: '#FFFFFF',
              fontSize: 14,
              fontFamily: 'Arimo, sans-serif',
              cursor: loading || fileList.length === 0 || uploadSuccess ? 'not-allowed' : 'pointer',
            }}
          >
            {loading ? 'Đang upload...' : 'Upload & Train'}
          </button>
        </div>
      </div>
    </Modal>
  );
};

export default UploadTrainingDataModal;
