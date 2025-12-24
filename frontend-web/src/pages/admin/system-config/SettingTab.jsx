import React, { useState } from 'react';

// Icon URLs from Figma
const iconDatabase = "https://www.figma.com/api/mcp/asset/d9d4172f-cabb-4e57-a71f-188268b10679";
const iconBackup = "https://www.figma.com/api/mcp/asset/cb24b91d-0ec6-4580-9981-4e169c5d0f62";
const iconKey = "https://www.figma.com/api/mcp/asset/6ce36782-2459-4ba0-a409-36a7725ac53d";
const iconSettings = "https://www.figma.com/api/mcp/asset/518027d9-fef8-424c-b90c-d510d2585667";
const iconCurrency = "https://www.figma.com/api/mcp/asset/6ed24d8c-ff5f-4c64-9e21-5a0ebf226274";
const iconTimezone = "https://www.figma.com/api/mcp/asset/38a98382-10a6-4aba-8cb7-29adab344b7c";
const iconSave = "https://www.figma.com/api/mcp/asset/eec8541b-c55f-479c-b515-0c98bb482c22";
const iconChevronDown = "https://www.figma.com/api/mcp/asset/be7d686f-65c0-49d0-a5b2-09b9778f4ab8";

const SettingTab = () => {
  const [currency, setCurrency] = useState('VND-VietNamese Dong');
  const [timezone, setTimezone] = useState('Asia/Ho Chi Minh(GMT +7)');
  const [confidenceThreshold, setConfidenceThreshold] = useState('75');
  const [analysisDelay, setAnalysisDelay] = useState('5');
  const [aiConfidence, setAiConfidence] = useState('70');
  const [trainingFrequency, setTrainingFrequency] = useState('Hàng Ngày');
  const [enablePushNotif, setEnablePushNotif] = useState(false);
  const [dailySummary, setDailySummary] = useState(false);
  const [unusualTransAlert, setUnusualTransAlert] = useState(false);

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 32 }}>
      {/* Stats Cards */}
      <div style={{ display: 'flex', gap: 65, flexWrap: 'wrap' }}>
        {/* Database Card */}
        <div
          style={{
            background: '#FFFFFF',
            border: '0.889px solid rgba(0,0,0,0.1)',
            borderRadius: 14,
            padding: '24.889px 0.889px 0.889px 24.889px',
            width: 224.222,
            height: 117.778,
          }}
        >
          <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
            <div
              style={{
                background: '#DCFCE7',
                borderRadius: 10,
                width: 48,
                height: 48,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}
            >
              <img src={iconDatabase} alt="" style={{ width: 24, height: 24 }} />
            </div>
            <div>
              <p
                style={{
                  fontFamily: 'Arimo, sans-serif',
                  fontSize: 14,
                  color: '#4A5565',
                  margin: 0,
                  lineHeight: '20px',
                }}
              >
                Cơ sở dữ liệu
              </p>
              <p
                style={{
                  fontFamily: 'Arimo, sans-serif',
                  fontSize: 16,
                  color: '#101828',
                  margin: 0,
                  lineHeight: '24px',
                }}
              >
                Hoạt động tốt
              </p>
            </div>
          </div>
        </div>

        {/* Backup Card */}
        <div
          style={{
            background: '#FFFFFF',
            border: '0.889px solid rgba(0,0,0,0.1)',
            borderRadius: 14,
            padding: '24.889px 0.889px 0.889px 24.889px',
            width: 224.222,
            height: 117.778,
          }}
        >
          <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
            <div
              style={{
                background: '#DBEAFE',
                borderRadius: 10,
                width: 48,
                height: 48,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}
            >
              <img src={iconBackup} alt="" style={{ width: 24, height: 24 }} />
            </div>
            <div>
              <p
                style={{
                  fontFamily: 'Arimo, sans-serif',
                  fontSize: 14,
                  color: '#4A5565',
                  margin: 0,
                  lineHeight: '20px',
                }}
              >
                Backup lần cuối
              </p>
              <p
                style={{
                  fontFamily: 'Arimo, sans-serif',
                  fontSize: 16,
                  color: '#101828',
                  margin: 0,
                  lineHeight: '24px',
                }}
              >
                2 giờ trước
              </p>
            </div>
          </div>
        </div>

        {/* API Keys Card */}
        <div
          style={{
            background: '#FFFFFF',
            border: '0.889px solid rgba(0,0,0,0.1)',
            borderRadius: 14,
            padding: '24.889px 0.889px 0.889px 24.889px',
            width: 224.222,
            height: 117.778,
          }}
        >
          <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
            <div
              style={{
                background: '#F3E8FF',
                borderRadius: 10,
                width: 48,
                height: 48,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}
            >
              <img src={iconKey} alt="" style={{ width: 24, height: 24 }} />
            </div>
            <div>
              <p
                style={{
                  fontFamily: 'Arimo, sans-serif',
                  fontSize: 14,
                  color: '#4A5565',
                  margin: 0,
                  lineHeight: '20px',
                }}
              >
                Khóa API
              </p>
              <p
                style={{
                  fontFamily: 'Arimo, sans-serif',
                  fontSize: 16,
                  color: '#101828',
                  margin: 0,
                  lineHeight: '24px',
                }}
              >
                3 Đang hoạt động
              </p>
            </div>
          </div>
        </div>

        {/* Config Status Card */}
        <div
          style={{
            background: '#FFFFFF',
            border: '0.889px solid rgba(0,0,0,0.1)',
            borderRadius: 14,
            padding: '24.889px 0.889px 0.889px 24.889px',
            width: 224.222,
            height: 117.778,
          }}
        >
          <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
            <div
              style={{
                background: '#FFEDD4',
                borderRadius: 10,
                width: 48,
                height: 48,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}
            >
              <img src={iconSettings} alt="" style={{ width: 24, height: 24 }} />
            </div>
            <div>
              <p
                style={{
                  fontFamily: 'Arimo, sans-serif',
                  fontSize: 14,
                  color: '#4A5565',
                  margin: 0,
                  lineHeight: '20px',
                }}
              >
                Trạng thái Cấu hình
              </p>
              <p
                style={{
                  fontFamily: 'Arimo, sans-serif',
                  fontSize: 16,
                  color: '#101828',
                  margin: 0,
                  lineHeight: '24px',
                }}
              >
                Đã cập nhật
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Main Settings Card */}
      <div
        style={{
          background: '#FFFFFF',
          border: '0.889px solid rgba(0,0,0,0.1)',
          borderRadius: 14,
          padding: '24.889px',
          display: 'flex',
          flexDirection: 'column',
          gap: 48,
        }}
      >
        {/* Cài đặt Chung */}
        <div>
          <h3
            style={{
              fontFamily: 'Arimo, sans-serif',
              fontSize: 16,
              color: '#101828',
              margin: 0,
              marginBottom: 16,
              fontWeight: 400,
            }}
          >
            Cài đặt Chung
          </h3>
          <div style={{ display: 'flex', gap: 83, marginBottom: 24 }}>
            {/* Đơn vị tiền tệ */}
            <div style={{ flex: 1, maxWidth: 488 }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8 }}>
                <img src={iconCurrency} alt="" style={{ width: 16, height: 16 }} />
                <label
                  style={{
                    fontFamily: 'Arimo, sans-serif',
                    fontSize: 14,
                    color: '#0A0A0A',
                  }}
                >
                  Đơn vị tiền tệ
                </label>
              </div>
              <select
                value={currency}
                onChange={(e) => setCurrency(e.target.value)}
                style={{
                  width: '100%',
                  height: 40.889,
                  border: '0.889px solid rgba(0,0,0,0.1)',
                  borderRadius: 10,
                  padding: '10px 9px',
                  fontFamily: 'Arimo, sans-serif',
                  fontSize: 16,
                  color: '#000000',
                  background: '#FFFFFF',
                  cursor: 'pointer',
                }}
              >
                <option>VND-VietNamese Dong</option>
                <option>USD - US Dollar</option>
                <option>EUR - Euro</option>
              </select>
            </div>

            {/* Múi giờ */}
            <div style={{ flex: 1, maxWidth: 488 }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8 }}>
                <img src={iconTimezone} alt="" style={{ width: 16, height: 16 }} />
                <label
                  style={{
                    fontFamily: 'Arimo, sans-serif',
                    fontSize: 14,
                    color: '#0A0A0A',
                  }}
                >
                  Múi giờ
                </label>
              </div>
              <select
                value={timezone}
                onChange={(e) => setTimezone(e.target.value)}
                style={{
                  width: '100%',
                  height: 40.889,
                  border: '0.889px solid rgba(0,0,0,0.1)',
                  borderRadius: 10,
                  padding: '10px 9px',
                  fontFamily: 'Arimo, sans-serif',
                  fontSize: 16,
                  color: '#000000',
                  background: '#FFFFFF',
                  cursor: 'pointer',
                }}
              >
                <option>Asia/Ho Chi Minh(GMT +7)</option>
                <option>Asia/Bangkok (GMT+7)</option>
                <option>Asia/Singapore (GMT+8)</option>
              </select>
            </div>
          </div>
        </div>

        {/* Cài đặt Phân tích SMS */}
        <div style={{ borderTop: '0.889px solid rgba(0,0,0,0.1)', paddingTop: 24.889 }}>
          <h3
            style={{
              fontFamily: 'Arimo, sans-serif',
              fontSize: 16,
              color: '#101828',
              margin: 0,
              marginBottom: 16,
              fontWeight: 400,
            }}
          >
            Cài đặt Phân tích SMS
          </h3>
          <div style={{ display: 'flex', gap: 83 }}>
            {/* Ngưỡng Độ tin cậy */}
            <div style={{ flex: 1, maxWidth: 488 }}>
              <label
                style={{
                  fontFamily: 'Arimo, sans-serif',
                  fontSize: 14,
                  color: '#0A0A0A',
                  display: 'block',
                  marginBottom: 8,
                }}
              >
                Ngưỡng Độ tin cậy (%)
              </label>
              <input
                type="text"
                value={confidenceThreshold}
                onChange={(e) => setConfidenceThreshold(e.target.value)}
                style={{
                  width: '100%',
                  height: 36,
                  background: '#F3F3F5',
                  border: '0.889px solid rgba(0,0,0,0)',
                  borderRadius: 8,
                  padding: '4px 12px',
                  fontFamily: 'Arimo, sans-serif',
                  fontSize: 14,
                  color: '#0A0A0A',
                }}
              />
              <p
                style={{
                  fontFamily: 'Arimo, sans-serif',
                  fontSize: 12,
                  color: '#6A7282',
                  margin: 0,
                  marginTop: 4,
                  lineHeight: '16px',
                }}
              >
                SMS có confidence thấp hơn sẽ được đánh dấu
              </p>
            </div>

            {/* Độ trễ Tự động phân tích */}
            <div style={{ flex: 1, maxWidth: 488 }}>
              <label
                style={{
                  fontFamily: 'Arimo, sans-serif',
                  fontSize: 14,
                  color: '#0A0A0A',
                  display: 'block',
                  marginBottom: 8,
                }}
              >
                Độ trễ Tự động phân tích (giây)
              </label>
              <input
                type="text"
                value={analysisDelay}
                onChange={(e) => setAnalysisDelay(e.target.value)}
                style={{
                  width: '100%',
                  height: 36,
                  background: '#F3F3F5',
                  border: '0.889px solid rgba(0,0,0,0)',
                  borderRadius: 8,
                  padding: '4px 12px',
                  fontFamily: 'Arimo, sans-serif',
                  fontSize: 14,
                  color: '#0A0A0A',
                }}
              />
              <p
                style={{
                  fontFamily: 'Arimo, sans-serif',
                  fontSize: 12,
                  color: '#6A7282',
                  margin: 0,
                  marginTop: 4,
                  lineHeight: '16px',
                }}
              >
                Thời gian chờ trước khi parse SMS
              </p>
            </div>
          </div>
        </div>

        {/* Cài đặt Mô hình AI */}
        <div style={{ borderTop: '0.889px solid rgba(0,0,0,0.1)', paddingTop: 24.889 }}>
          <h3
            style={{
              fontFamily: 'Arimo, sans-serif',
              fontSize: 16,
              color: '#101828',
              margin: 0,
              marginBottom: 16,
              fontWeight: 400,
            }}
          >
            Cài đặt Mô hình AI
          </h3>
          <div style={{ display: 'flex', gap: 83 }}>
            {/* Ngưỡng Độ tin cậy AI */}
            <div style={{ flex: 1, maxWidth: 488 }}>
              <label
                style={{
                  fontFamily: 'Arimo, sans-serif',
                  fontSize: 14,
                  color: '#0A0A0A',
                  display: 'block',
                  marginBottom: 8,
                }}
              >
                Ngưng Độ tin cậy AI (%)
              </label>
              <input
                type="text"
                value={aiConfidence}
                onChange={(e) => setAiConfidence(e.target.value)}
                style={{
                  width: '100%',
                  height: 36,
                  background: '#F3F3F5',
                  border: '0.889px solid rgba(0,0,0,0)',
                  borderRadius: 8,
                  padding: '4px 12px',
                  fontFamily: 'Arimo, sans-serif',
                  fontSize: 14,
                  color: '#0A0A0A',
                }}
              />
              <p
                style={{
                  fontFamily: 'Arimo, sans-serif',
                  fontSize: 12,
                  color: '#6A7282',
                  margin: 0,
                  marginTop: 4,
                  lineHeight: '16px',
                }}
              >
                Phân loại có confidence thấp hơn cần review
              </p>
            </div>

            {/* Tần suất Huấn luyện lại */}
            <div style={{ flex: 1, maxWidth: 488 }}>
              <label
                style={{
                  fontFamily: 'Arimo, sans-serif',
                  fontSize: 14,
                  color: '#0A0A0A',
                  display: 'block',
                  marginBottom: 8,
                }}
              >
                Tần suất Huấn luyện lại Tự động
              </label>
              <select
                value={trainingFrequency}
                onChange={(e) => setTrainingFrequency(e.target.value)}
                style={{
                  width: '100%',
                  height: 40.889,
                  border: '0.889px solid rgba(0,0,0,0.1)',
                  borderRadius: 10,
                  padding: '10px 9px',
                  fontFamily: 'Arimo, sans-serif',
                  fontSize: 16,
                  color: '#000000',
                  background: '#FFFFFF',
                  cursor: 'pointer',
                }}
              >
                <option>Hàng Ngày</option>
                <option>Hàng tuần</option>
                <option>Hàng tháng</option>
                <option>Chỉ Thủ công</option>
              </select>
            </div>
          </div>
        </div>

        {/* Cài đặt Thông báo */}
        <div style={{ borderTop: '0.889px solid rgba(0,0,0,0.1)', paddingTop: 24.889 }}>
          <h3
            style={{
              fontFamily: 'Arimo, sans-serif',
              fontSize: 16,
              color: '#101828',
              margin: 0,
              marginBottom: 16,
              fontWeight: 400,
            }}
          >
            Cài đặt Thông báo
          </h3>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
              <input
                type="checkbox"
                checked={enablePushNotif}
                onChange={(e) => setEnablePushNotif(e.target.checked)}
                style={{ width: 16, height: 16, cursor: 'pointer' }}
              />
              <label
                style={{
                  fontFamily: 'Arimo, sans-serif',
                  fontSize: 14,
                  color: '#0A0A0A',
                  lineHeight: '20px',
                }}
              >
                Bật thông báo đẩy
              </label>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
              <input
                type="checkbox"
                checked={dailySummary}
                onChange={(e) => setDailySummary(e.target.checked)}
                style={{ width: 16, height: 16, cursor: 'pointer' }}
              />
              <label
                style={{
                  fontFamily: 'Arimo, sans-serif',
                  fontSize: 14,
                  color: '#0A0A0A',
                  lineHeight: '20px',
                }}
              >
                Gửi tóm tắt chi tiêu hàng ngày
              </label>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
              <input
                type="checkbox"
                checked={unusualTransAlert}
                onChange={(e) => setUnusualTransAlert(e.target.checked)}
                style={{ width: 16, height: 16, cursor: 'pointer' }}
              />
              <label
                style={{
                  fontFamily: 'Arimo, sans-serif',
                  fontSize: 14,
                  color: '#0A0A0A',
                  lineHeight: '20px',
                }}
              >
                Cảnh báo giao dịch bất thường
              </label>
            </div>
          </div>
        </div>

        {/* Save Button */}
        <div style={{ borderTop: '0.889px solid rgba(0,0,0,0.1)', paddingTop: 24.889 }}>
          <button
            style={{
              background: '#155DFC',
              borderRadius: 8,
              border: 'none',
              height: 36,
              width: 160,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: 8,
              cursor: 'pointer',
              padding: '6px 12px',
            }}
          >
            <img src={iconSave} alt="" style={{ width: 16, height: 16 }} />
            <span
              style={{
                fontFamily: 'Arimo, sans-serif',
                fontSize: 14,
                color: '#FFFFFF',
                lineHeight: '20px',
              }}
            >
              Lưu Cài đặt
            </span>
          </button>
        </div>
      </div>
    </div>
  );
};

export default SettingTab;
